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

    /**
     * Parses a number string by auto-detecting its locale from the input.
     *
     * <p>
     * If the input is ambiguous (e.g. "1234" without any separators), the formatter default locale
     * is preferred if it is supported; otherwise the first supported locale is used as fallback.
     * </p>
     *
     * @param input the raw numeric string to parse
     * @return the parsed {@link BigNumber}
     * @throws IllegalArgumentException if the input is not a valid number in any supported locale
     */
    BigNumber parse(@NonNull final String input) {
        if (input.isBlank()) {
            return ZERO;
        }

        final Locale resolvedLocale = resolveLocale(input);
        return parse(input, resolvedLocale);
    }

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
        if (input.isBlank()) {
            return ZERO;
        }

        final String trimmedInput = input.trim();

        if (isScientificNotation(trimmedInput)) {
            return parseScientificNotation(trimmedInput, locale);
        }

        if (!isNumber(trimmedInput, locale)) {
            return ZERO;
        }

        final String normalized = normalize(trimmedInput, locale);
        return extractParts(normalized, locale);
    }

    /**
     * Attempts to parse the input string by auto-detecting its locale, then formats the result
     * according to the specified target locale.
     *
     * @param input        the numeric string to parse and format
     * @param targetLocale the locale to format the parsed number into
     * @return a {@link BigNumber} formatted for the target locale
     * @throws IllegalArgumentException if parsing fails for all supported locales
     */
    public BigNumber parseAndFormat(@NonNull final String input, @NonNull final Locale targetLocale) {
        if (input.isBlank()) {
            return ZERO;
        }

        final Locale sourceLocale = resolveLocale(input);
        return format(parse(input, sourceLocale), targetLocale);
    }

    /**
     * Formats a BigNumber to the targetLocale.
     *
     * @param number       the BigNumber to format
     * @param targetLocale the target targetLocale
     * @return formatted string with grouping and targetLocale decimal separator
     */
    BigNumber format(@NonNull final BigNumber number, @NonNull final Locale targetLocale) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(targetLocale);
        char groupingSeparator = symbols.getGroupingSeparator();
        char decimalSeparator = symbols.getDecimalSeparator();

        String beforeDecimal = number.getValueBeforeDecimalPoint();
        String afterDecimal = number.getValueAfterDecimalPoint();
        boolean isNegative = number.isNegative();

        StringBuilder groupedBeforeDecimal = getGroupedBeforeDecimal(beforeDecimal, groupingSeparator);

        StringBuilder formattedNumber = new StringBuilder();
        if (isNegative) {
            formattedNumber.append("-");
        }

        formattedNumber.append(groupedBeforeDecimal);
        if (!afterDecimal.equals("0") && !afterDecimal.isEmpty()) {
            formattedNumber.append(decimalSeparator).append(afterDecimal);
        }

        return parse(formattedNumber.toString(), targetLocale);
    }

    /**
     * Inserts grouping separators every 3 digits from right to left for the integer part.
     *
     * @param integerPart       the string of digits before decimal
     * @param groupingSeparator the grouping separator character (e.g. ',' or '.')
     * @return string with grouping separators inserted
     */
    StringBuilder getGroupedBeforeDecimal(@NonNull final String integerPart, final char groupingSeparator) {
        StringBuilder grouped = new StringBuilder();

        int count = 0;
        for (int i = integerPart.length() - 1; i >= 0; i--) {
            grouped.insert(0, integerPart.charAt(i));
            count++;

            if (count == 3 && i != 0) {
                grouped.insert(0, groupingSeparator);
                count = 0;
            }
        }

        return grouped;
    }

    /**
     * Normalizes the input by removing grouping separators
     * and converting the decimal separator to '.' (US format).
     */
    private String normalize(@NonNull final String value, @NonNull final Locale fromLocale) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(fromLocale);
        char groupingSeparator = symbols.getGroupingSeparator();
        char decimalSeparator = symbols.getDecimalSeparator();

        String noGrouping = value.replace(String.valueOf(groupingSeparator), "");

        // Some locales use NBSP for grouping; users often input normal spaces.
        if (groupingSeparator == '\u00A0') {
            noGrouping = noGrouping.replace(" ", "").replace("\u00A0", "");
        }

        return noGrouping.replace(decimalSeparator, '.');
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
        String value = normalizedValue;
        boolean isNegative = value.startsWith("-");
        if (isNegative) {
            value = value.substring(1);
        }

        String[] parts = value.split("\\.", 2);
        String valueBeforeDecimalPoint = parts[0];
        String valueAfterDecimalPoint = (parts.length > 1) ? parts[1] : "0";

        return new BigNumber(
                originalLocale,
                valueBeforeDecimalPoint,
                valueAfterDecimalPoint,
                isNegative,
                BigNumbers.DEFAULT_MATH_CONTEXT,
                TrigonometricMode.DEG
        );
    }

    /**
     * Parses a numeric string that uses scientific notation (for example "1e-6" or "-2,5E3").
     *
     * <p>
     * This method normalizes the input according to the given locale first (grouping removed,
     * decimal separator -> '.'). Then it delegates parsing to {@link BigDecimal} to ensure correct
     * interpretation of the exponent and to obtain a plain string representation.
     * </p>
     *
     * @param input  the input numeric string expected to be in scientific notation
     * @param locale the locale to associate with the produced {@link BigNumber}
     * @return a {@link BigNumber} representing the value, or {@link BigNumbers#ZERO}
     * if the input cannot be parsed as a number
     */
    private BigNumber parseScientificNotation(@NonNull final String input, @NonNull final Locale locale) {
        try {
            final String normalized = normalize(input, locale);
            final BigDecimal decimal = new BigDecimal(normalized);
            final String plainString = decimal.toPlainString();
            return extractParts(plainString, locale);
        } catch (NumberFormatException exception) {
            return ZERO;
        }
    }

    /**
     * Resolves the most likely locale for the given numeric input.
     *
     * <p>
     * Strategy:
     * <ul>
     *   <li>Try a fast separator heuristic (dot/comma position) to narrow candidates.</li>
     *   <li>Validate candidates by actually parsing (isNumber / BigDecimal for scientific).</li>
     *   <li>Fallback to default format locale if supported, else first supported locale, else US.</li>
     * </ul>
     * </p>
     *
     * @param input the raw input
     * @return resolved locale
     * @throws IllegalArgumentException if none of the supported locales can parse the input
     */
    private Locale resolveLocale(@NonNull final String input) {
        final String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return resolveFallbackLocale();
        }

        final Locale heuristic = resolveBySeparatorHeuristic(trimmed);
        if (heuristic != null) {
            return heuristic;
        }

        final boolean scientific = isScientificNotation(trimmed);

        for (Locale candidate : LocalesConfig.SUPPORTED_LOCALES) {
            if (scientific) {
                if (canParseScientific(trimmed, candidate)) {
                    return candidate;
                }
            } else if (isNumber(trimmed, candidate)) {
                return candidate;
            }
        }

        // Ambiguous / separator-free numbers: prefer system format locale if supported, else first supported, else US.
        final Locale fallback = resolveFallbackLocale();
        if (scientific ? canParseScientific(trimmed, fallback) : isNumber(trimmed, fallback)) {
            return fallback;
        }

        throw new IllegalArgumentException("Input is not a valid number in any supported locale: " + input);
    }

    /**
     * Separator heuristic:
     * - If both '.' and ',' exist in the significand, the last one is assumed to be the decimal separator.
     *   The other is assumed to be the grouping separator.
     * - If only one of them exists, it is assumed to be the decimal separator (grouping remains unknown).
     *
     * @param inputTrimmed trimmed input
     * @return a supported locale that matches the heuristic and validates, or null
     */
    private Locale resolveBySeparatorHeuristic(@NonNull final String inputTrimmed) {
        final String significand = stripExponentPart(inputTrimmed);

        final int lastDot = significand.lastIndexOf('.');
        final int lastComma = significand.lastIndexOf(',');

        if (lastDot < 0 && lastComma < 0) {
            return null;
        }

        final boolean hasDot = lastDot >= 0;
        final boolean hasComma = lastComma >= 0;

        final char decimalCandidate;
        final Character groupingCandidate;

        if (hasDot && hasComma) {
            if (lastDot > lastComma) {
                decimalCandidate = '.';
                groupingCandidate = ',';
            } else {
                decimalCandidate = ',';
                groupingCandidate = '.';
            }
        } else if (hasComma) {
            decimalCandidate = ',';
            groupingCandidate = null;
        } else {
            decimalCandidate = '.';
            groupingCandidate = null;
        }

        final boolean scientific = isScientificNotation(inputTrimmed);

        for (Locale locale : LocalesConfig.SUPPORTED_LOCALES) {
            final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);

            if (symbols.getDecimalSeparator() != decimalCandidate) {
                continue;
            }
            if (groupingCandidate != null && symbols.getGroupingSeparator() != groupingCandidate) {
                continue;
            }

            if (scientific) {
                if (canParseScientific(inputTrimmed, locale)) {
                    return locale;
                }
            } else {
                if (isNumber(inputTrimmed, locale)) {
                    return locale;
                }
            }
        }

        return null;
    }

    private String stripExponentPart(@NonNull final String inputTrimmed) {
        final int eLower = inputTrimmed.indexOf('e');
        final int eUpper = inputTrimmed.indexOf('E');

        final int idx;
        if (eLower < 0) {
            idx = eUpper;
        } else if (eUpper < 0) {
            idx = eLower;
        } else {
            idx = Math.min(eLower, eUpper);
        }

        return idx < 0 ? inputTrimmed : inputTrimmed.substring(0, idx);
    }

    private boolean canParseScientific(@NonNull final String inputTrimmed, @NonNull final Locale locale) {
        try {
            new BigDecimal(normalize(inputTrimmed, locale));
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private Locale resolveFallbackLocale() {
        final Locale preferred = Locale.getDefault(Locale.Category.FORMAT);

        for (Locale supported : LocalesConfig.SUPPORTED_LOCALES) {
            if (supported.equals(preferred)) {
                return supported;
            }
        }

        for (Locale supported : LocalesConfig.SUPPORTED_LOCALES) {
            return supported;
        }

        return Locale.US;
    }

    /**
     * Determines whether the given string contains a scientific notation exponent
     * indicator (either lowercase 'e' or uppercase 'E').
     *
     * @param value the string to inspect
     * @return {@code true} if the string contains 'e' or 'E', {@code false} otherwise
     */
    private boolean isScientificNotation(@NonNull final String value) {
        return value.indexOf('e') >= 0 || value.indexOf('E') >= 0;
    }

}