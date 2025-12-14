/*
 * Copyright (c) 2025 Max Lemberg
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
     * <p>
     * Iterates through supported locales to find one that matches the input format. If a match is found,
     * parses the input and formats it for the target locale. Returns zero if parsing fails for all locales.
     *
     * @param input        the numeric string to parse and format
     * @param targetLocale the locale to format the parsed number into
     * @return a {@link BigNumber} formatted for the target locale, or zero if parsing fails
     */
    public BigNumber parseAndFormat(@NonNull final String input, @NonNull final Locale targetLocale) {
        for (Locale sourceLocale : LocalesConfig.SUPPORTED_LOCALES) {
            if (isScientificNotation(input) || isNumber(input, sourceLocale)) {
                return format(parse(input, sourceLocale), targetLocale);
            }
        }

        throw new IllegalArgumentException("Input is not a valid number in any supported locale: " + input);
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
        String beforeDecimal = parts[0];
        String afterDecimal = (parts.length > 1) ? parts[1] : "0";

        return BigNumber.builder()
                .mathContext(BigNumbers.DEFAULT_MATH_CONTEXT)
                .trigonometricMode(TrigonometricMode.DEG)
                .locale(originalLocale)
                .valueBeforeDecimalPoint(beforeDecimal)
                .valueAfterDecimalPoint(afterDecimal)
                .isNegative(isNegative)
                .build();
    }

    /**
     * Parses a numeric string that uses scientific notation (for example "1e-6" or "-2.5E3").
     *
     * <p>
     * This method delegates parsing to {@link BigDecimal} to ensure correct interpretation
     * of the exponent and to obtain a plain string representation (no scientific notation).
     * The resulting plain string is then passed to {@link #extractParts(String, Locale)}
     * to construct a {@link BigNumber} using the provided locale.
     * </p>
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