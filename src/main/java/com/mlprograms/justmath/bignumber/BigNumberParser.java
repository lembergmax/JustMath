/*
 * Copyright (c) 2025-2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.bignumber.internal.LocalesConfig;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;
import static com.mlprograms.justmath.bignumber.internal.NumberChecker.isNumber;

/**
 * Utility class responsible for parsing numeric strings into {@link BigNumber} instances,
 * considering locale-specific grouping and decimal separators.
 *
 * <p>
 * Supports standard decimal notation as well as scientific notation
 * (e.g. {@code 1e-12}, {@code -3.5E+4}).
 * </p>
 */
class BigNumberParser {

    private static final ConcurrentHashMap<Locale, LocaleSeparators> LOCALE_SEPARATORS_CACHE = new ConcurrentHashMap<>();

    /**
     * Parses a number string using the specified locale.
     *
     * <p>
     * Scientific notation is detected explicitly and parsed via {@link BigDecimal}
     * to ensure correct normalization and precision.
     * </p>
     *
     * @param input  the raw numeric string to parse
     * @param locale the locale defining grouping and decimal separators for the input string
     * @return the parsed {@link BigNumber}
     */
    BigNumber parse(@NonNull final String input, @NonNull final Locale locale) {
        final int inputLength = input.length();
        final int startIndex = firstNonWhitespaceIndex(input, inputLength);
        if (startIndex >= inputLength) {
            return ZERO;
        }

        final int endExclusiveIndex = lastNonWhitespaceExclusiveIndex(input, startIndex, inputLength);
        if (endExclusiveIndex <= startIndex) {
            return ZERO;
        }

        if (containsScientificExponent(input, startIndex, endExclusiveIndex)) {
            return parseScientificNotation(input.substring(startIndex, endExclusiveIndex), locale);
        }

        final BigNumber fastParsed = tryParsePlainNumberFast(input, startIndex, endExclusiveIndex, locale);
        if (fastParsed != null) {
            return fastParsed;
        }

        // Fallback: keep legacy acceptance rules (NumberChecker) for edge-cases
        final String trimmedInput = input.substring(startIndex, endExclusiveIndex);
        if (!isNumber(trimmedInput, locale)) {
            return ZERO;
        }

        final String normalized = normalize(trimmedInput, locale);
        return extractParts(normalized, locale);
    }

    /**
     * Attempts to parse the input string by auto-detecting its locale, then formats the result
     * according to the specified target locale.
     * <p>
     * Iterates through supported locales to find one that matches the input format. If a match is found,
     * parses the input and formats it for the target locale. Returns zero if parsing fails for all locales.
     *
     * @param input        the numeric string to parse and format
     * @param targetLocale the locale to format the parsed number into
     * @return a {@link BigNumber} formatted for the target locale, or zero if parsing fails
     */
    public BigNumber parseAndFormat(@NonNull final String input, @NonNull final Locale targetLocale) {
        final int inputLength = input.length();
        final int startIndex = firstNonWhitespaceIndex(input, inputLength);
        final int endExclusiveIndex = lastNonWhitespaceExclusiveIndex(input, startIndex, inputLength);

        // Keep legacy behavior: blank does NOT auto-return ZERO here -> will throw.
        final String trimmed = input.substring(Math.min(startIndex, inputLength), Math.max(endExclusiveIndex, 0));

        if (containsScientificExponent(trimmed, 0, trimmed.length())) {
            return format(parseScientificNotation(trimmed, targetLocale), targetLocale);
        }

        final Locale guessedLocale = guessLocaleBySeparators(trimmed);
        if (guessedLocale != null) {
            final BigNumber guessedParsed = tryParsePlainNumberFast(trimmed, 0, trimmed.length(), guessedLocale);
            if (guessedParsed != null) {
                return format(guessedParsed, targetLocale);
            }
        }

        for (final Locale sourceLocale : LocalesConfig.SUPPORTED_LOCALES) {
            final BigNumber fastParsed = tryParsePlainNumberFast(trimmed, 0, trimmed.length(), sourceLocale);
            if (fastParsed != null) {
                return format(fastParsed, targetLocale);
            }
        }

        // Final fallback: preserve legacy matching rules (regex/NumberChecker) for uncommon formats.
        for (final Locale sourceLocale : LocalesConfig.SUPPORTED_LOCALES) {
            if (isNumber(trimmed, sourceLocale)) {
                return format(parse(trimmed, sourceLocale), targetLocale);
            }
        }

        throw new IllegalArgumentException("Input is not a valid number in any supported locale: " + trimmed);
    }

    /**
     * Formats a BigNumber to the targetLocale.
     *
     * @param number       the BigNumber to format
     * @param targetLocale the target targetLocale
     * @return formatted string with grouping and targetLocale decimal separator
     */
    BigNumber format(@NonNull final BigNumber number, @NonNull final Locale targetLocale) {
        final String valueBeforeDecimalPoint = number.getValueBeforeDecimalPoint();
        final String valueAfterDecimalPoint = number.getValueAfterDecimalPoint();
        final boolean isNegative = number.isNegative();

        final String normalizedFraction;
        if (valueAfterDecimalPoint == null || valueAfterDecimalPoint.isEmpty() || "0".equals(valueAfterDecimalPoint)) {
            normalizedFraction = "0";
        } else {
            normalizedFraction = valueAfterDecimalPoint;
        }

        return new BigNumber(
                targetLocale,
                valueBeforeDecimalPoint,
                normalizedFraction,
                isNegative,
                number.getMathContext(),
                number.getTrigonometricMode()
        );
    }

    /**
     * Inserts grouping separators every 3 digits from right to left for the integer part.
     *
     * @param integerPart       the string of digits before decimal
     * @param groupingSeparator the grouping separator character (e.g. ',' or '.')
     * @return string with grouping separators inserted
     */
    StringBuilder getGroupedBeforeDecimal(@NonNull final String integerPart, final char groupingSeparator) {
        final int digitsLength = integerPart.length();
        if (digitsLength <= 3) {
            return new StringBuilder(integerPart);
        }

        final int separatorsCount = (digitsLength - 1) / 3;
        final char[] result = new char[digitsLength + separatorsCount];

        int sourceIndex = digitsLength - 1;
        int targetIndex = result.length - 1;
        int digitsInGroup = 0;

        while (sourceIndex >= 0) {
            result[targetIndex--] = integerPart.charAt(sourceIndex--);
            digitsInGroup++;

            if (digitsInGroup == 3 && sourceIndex >= 0) {
                result[targetIndex--] = groupingSeparator;
                digitsInGroup = 0;
            }
        }

        return new StringBuilder(result.length).append(result);
    }

    /**
     * Normalizes the input by removing grouping separators
     * and converting the decimal separator to '.' (US format).
     */
    private String normalize(@NonNull final String value, @NonNull final Locale fromLocale) {
        final LocaleSeparators separators = localeSeparators(fromLocale);
        final char groupingSeparator = separators.groupingSeparator();
        final char decimalSeparator = separators.decimalSeparator();

        final int length = value.length();
        final StringBuilder normalized = new StringBuilder(length);

        for (int index = 0; index < length; index++) {
            final char character = value.charAt(index);
            if (character == groupingSeparator) {
                continue;
            }
            normalized.append(character == decimalSeparator ? '.' : character);
        }

        return normalized.toString();
    }

    /**
     * Extracts the integer and fractional parts from a normalized numeric string,
     * determines if the value is negative, and constructs a {@link BigNumber} instance.
     *
     * @param normalizedValue the numeric string in normalized (US) format, possibly starting with '-'
     * @param originalLocale  the locale to associate with the resulting BigNumber
     * @return a BigNumber representing the parsed value, with correct sign and parts
     */
    private BigNumber extractParts(@NonNull final String normalizedValue, @NonNull final Locale originalLocale) {
        int startIndex = 0;
        boolean isNegative = false;

        if (!normalizedValue.isEmpty() && normalizedValue.charAt(0) == '-') {
            isNegative = true;
            startIndex = 1;
        }

        final int decimalPointIndex = normalizedValue.indexOf('.', startIndex);

        final String valueBeforeDecimalPoint;
        final String valueAfterDecimalPoint;

        if (decimalPointIndex < 0) {
            valueBeforeDecimalPoint = normalizedValue.substring(startIndex);
            valueAfterDecimalPoint = "0";
        } else {
            valueBeforeDecimalPoint = normalizedValue.substring(startIndex, decimalPointIndex);
            valueAfterDecimalPoint = normalizedValue.substring(decimalPointIndex + 1);
        }

        return new BigNumber(
                originalLocale,
                valueBeforeDecimalPoint,
                valueAfterDecimalPoint.isEmpty() ? "0" : valueAfterDecimalPoint,
                isNegative,
                BigNumbers.DEFAULT_MATH_CONTEXT,
                TrigonometricMode.DEG
        );
    }

    /**
     * Parses a numeric string that uses scientific notation (for example "1e-6" or "-2.5E3").
     *
     * @param input  the input numeric string expected to be in scientific notation
     * @param locale the locale to associate with the produced {@link BigNumber}
     * @return a {@link BigNumber} representing the value, or {@link BigNumbers#ZERO}
     * if the input cannot be parsed as a number
     */
    private BigNumber parseScientificNotation(@NonNull final String input, @NonNull final Locale locale) {
        try {
            final BigDecimal decimal = new BigDecimal(input);
            final String plainString = decimal.toPlainString();
            return extractParts(plainString, locale);
        } catch (NumberFormatException exception) {
            return ZERO;
        }
    }

    private static int firstNonWhitespaceIndex(final String value, final int length) {
        int index = 0;
        while (index < length && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }

    private static int lastNonWhitespaceExclusiveIndex(final String value, final int startIndex, final int length) {
        int index = length;
        while (index > startIndex && Character.isWhitespace(value.charAt(index - 1))) {
            index--;
        }
        return index;
    }

    private static boolean containsScientificExponent(final String value, final int startIndex, final int endExclusiveIndex) {
        for (int index = startIndex; index < endExclusiveIndex; index++) {
            final char character = value.charAt(index);
            if (character == 'e' || character == 'E') {
                return true;
            }
        }
        return false;
    }

    private static LocaleSeparators localeSeparators(@NonNull final Locale locale) {
        return LOCALE_SEPARATORS_CACHE.computeIfAbsent(locale, BigNumberParser::createSeparators);
    }

    private static LocaleSeparators createSeparators(final Locale locale) {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
        return new LocaleSeparators(symbols.getGroupingSeparator(), symbols.getDecimalSeparator());
    }

    /**
     * Super-schneller Parse-Pfad für "normale" Zahlen (ohne scientific notation), inkl. korrekter 3er-Gruppierung.
     * Gibt {@code null} zurück, wenn ein Edge-Case vorliegt -> dann wird auf den Legacy-Pfad (NumberChecker) gefallen.
     */
    private static BigNumber tryParsePlainNumberFast(
            @NonNull final String input,
            final int startIndex,
            final int endExclusiveIndex,
            @NonNull final Locale locale
    ) {
        int index = startIndex;

        boolean isNegative = false;
        if (index < endExclusiveIndex && input.charAt(index) == '-') {
            isNegative = true;
            index++;
        }

        if (index >= endExclusiveIndex) {
            return null;
        }

        final LocaleSeparators separators = localeSeparators(locale);
        final char groupingSeparator = separators.groupingSeparator();
        final char decimalSeparator = separators.decimalSeparator();

        boolean hasDecimalSeparator = false;
        boolean hasGroupingSeparator = false;

        int integerDigitsTotal = 0;
        int fractionDigitsTotal = 0;

        int digitsSinceLastGrouping = 0;
        boolean groupingStructureStarted = false;

        for (int scanIndex = index; scanIndex < endExclusiveIndex; scanIndex++) {
            final char character = input.charAt(scanIndex);

            if (character >= '0' && character <= '9') {
                if (!hasDecimalSeparator) {
                    integerDigitsTotal++;
                    digitsSinceLastGrouping++;
                } else {
                    fractionDigitsTotal++;
                }
                continue;
            }

            if (character == groupingSeparator) {
                if (hasDecimalSeparator) {
                    return null; // grouping after decimal -> fallback (legacy)
                }
                if (digitsSinceLastGrouping == 0) {
                    return null;
                }

                hasGroupingSeparator = true;

                if (!groupingStructureStarted) {
                    if (digitsSinceLastGrouping > 3) {
                        return null;
                    }
                    groupingStructureStarted = true;
                } else if (digitsSinceLastGrouping != 3) {
                    return null;
                }

                digitsSinceLastGrouping = 0;
                continue;
            }

            if (character == decimalSeparator) {
                if (hasDecimalSeparator) {
                    return null;
                }
                if (integerDigitsTotal == 0) {
                    return null;
                }
                if (hasGroupingSeparator && groupingStructureStarted && digitsSinceLastGrouping != 3) {
                    return null;
                }
                hasDecimalSeparator = true;
                continue;
            }

            // Anything else (including '+') -> fallback (legacy rules)
            return null;
        }

        if (integerDigitsTotal == 0) {
            return null;
        }

        if (hasGroupingSeparator && !hasDecimalSeparator && groupingStructureStarted && digitsSinceLastGrouping != 3) {
            return null;
        }

        if (hasDecimalSeparator && fractionDigitsTotal == 0) {
            return null; // edge-case like "1234," / "1234." -> leave to legacy checker
        }

        final char[] integerDigits = new char[integerDigitsTotal];
        final char[] fractionDigits = hasDecimalSeparator ? new char[fractionDigitsTotal] : null;

        int integerWriteIndex = 0;
        int fractionWriteIndex = 0;
        boolean nowInFraction = false;

        for (int scanIndex = index; scanIndex < endExclusiveIndex; scanIndex++) {
            final char character = input.charAt(scanIndex);

            if (character >= '0' && character <= '9') {
                if (!nowInFraction) {
                    integerDigits[integerWriteIndex++] = character;
                } else {
                    fractionDigits[fractionWriteIndex++] = character;
                }
                continue;
            }

            if (character == decimalSeparator) {
                nowInFraction = true;
                continue;
            }

            if (character == groupingSeparator) {
                continue;
            }
        }

        final String valueBeforeDecimalPoint = new String(integerDigits);
        final String valueAfterDecimalPoint = (fractionDigits == null) ? "0" : new String(fractionDigits);

        return new BigNumber(
                locale,
                valueBeforeDecimalPoint,
                valueAfterDecimalPoint,
                isNegative,
                BigNumbers.DEFAULT_MATH_CONTEXT,
                TrigonometricMode.DEG
        );
    }

    /**
     * Heuristik: wenn sowohl '.' als auch ',' vorkommen, ist der rechte Separator fast immer das Dezimalzeichen.
     * Dann versuchen wir zuerst das Locale zu finden, dessen Dezimal-/Gruppierungszeichen dazu passt.
     */
    private static Locale guessLocaleBySeparators(@NonNull final String trimmedInput) {
        final int lastDot = trimmedInput.lastIndexOf('.');
        final int lastComma = trimmedInput.lastIndexOf(',');

        if (lastDot < 0 || lastComma < 0) {
            return null;
        }

        final char decimalSeparator = (lastDot > lastComma) ? '.' : ',';
        final char groupingSeparator = (decimalSeparator == '.') ? ',' : '.';

        for (final Locale locale : LocalesConfig.SUPPORTED_LOCALES) {
            final LocaleSeparators separators = localeSeparators(locale);
            if (separators.decimalSeparator() == decimalSeparator && separators.groupingSeparator() == groupingSeparator) {
                return locale;
            }
        }

        return null;
    }

    private record LocaleSeparators(char groupingSeparator, char decimalSeparator) { }

}
