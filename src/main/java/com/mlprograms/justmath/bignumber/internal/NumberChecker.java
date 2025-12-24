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

package com.mlprograms.justmath.bignumber.internal;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance numeric string validator for locale-aware expressions.
 * <p>
 * This utility is optimized for hot paths (e.g. tokenizers/parsers). It avoids the overhead of
 * {@link java.text.NumberFormat} parsing and instead validates the input in a single linear scan.
 * <p>
 * Supported grammar (locale-dependent):
 * <ul>
 *   <li>Optional leading sign: {@code +} or {@code -}</li>
 *   <li>Digits with optional decimal separator</li>
 *   <li>Optional grouping separator (permissive; grouping sizes are not strictly validated)</li>
 *   <li>Optional scientific exponent: {@code e/E} with optional sign and at least one digit</li>
 * </ul>
 * Leading and trailing whitespace is ignored without allocating a trimmed string.
 */
public final class NumberChecker {

    private static final ConcurrentHashMap<Locale, LocaleSeparators> SEPARATORS_CACHE = new ConcurrentHashMap<>();

    private NumberChecker() {
        throw new AssertionError("Utility class must not be instantiated.");
    }

    /**
     * Checks whether the given input represents a valid locale-aware number.
     * <p>
     * This method is intended for fast validation and does not parse the number into a numeric type.
     * It validates that the trimmed input matches the supported grammar and contains no trailing junk.
     *
     * @param input  the input string to validate; may be {@code null}
     * @param locale the locale that defines decimal and grouping separators; may be {@code null}
     * @return {@code true} if the input is a valid number for the provided locale; {@code false} otherwise
     */
    public static boolean isNumber(final String input, final Locale locale) {
        if (input == null || locale == null) {
            return false;
        }

        final int length = input.length();
        int start = 0;
        int end = length;

        // Manual trim without allocating a new String
        while (start < end && isWhitespace(input.charAt(start))) {
            start++;
        }
        while (end > start && isWhitespace(input.charAt(end - 1))) {
            end--;
        }
        if (start >= end) {
            return false;
        }

        final LocaleSeparators sep = separators(locale);
        final char decimalSep = sep.decimalSeparator();
        final char groupingSep = sep.groupingSeparator();

        int i = start;

        // Optional sign
        final char first = input.charAt(i);
        if (first == '+' || first == '-') {
            i++;
            if (i >= end) {
                return false;
            }
        }

        boolean sawDigit = false;
        boolean sawDecimal = false;

        // Mantissa: digits, optional grouping (before decimal), optional decimal separator
        while (i < end) {
            final char charAt = input.charAt(i);

            if (isAsciiDigit(charAt)) {
                sawDigit = true;
                i++;
                continue;
            }

            if (charAt == groupingSep) {
                // grouping allowed only after at least one digit and only before decimal
                if (!sawDigit || sawDecimal) {
                    return false;
                }
                i++;
                continue;
            }

            if (charAt == decimalSep) {
                if (sawDecimal) {
                    return false;
                }
                sawDecimal = true;
                i++;
                continue;
            }

            break;
        }

        if (!sawDigit) {
            return false;
        }

        // Optional exponent
        if (i < end && isExponentMarker(input.charAt(i))) {
            i++;
            if (i >= end) {
                return false;
            }

            final char expFirst = input.charAt(i);
            if (expFirst == '+' || expFirst == '-') {
                i++;
                if (i >= end) {
                    return false;
                }
            }

            boolean sawExpDigit = false;
            while (i < end) {
                final char c = input.charAt(i);
                if (isAsciiDigit(c)) {
                    sawExpDigit = true;
                    i++;
                    continue;
                }
                return false;
            }
            return sawExpDigit;
        }

        // No trailing characters allowed
        return i == end;
    }

    /**
     * Returns cached locale-specific separators for the given locale.
     * <p>
     * Caching avoids repeated creation of {@link DecimalFormatSymbols} objects in hot paths.
     *
     * @param locale the locale to resolve separators for; must not be {@code null}
     * @return cached decimal and grouping separators for the locale
     */
    private static LocaleSeparators separators(final Locale locale) {
        return SEPARATORS_CACHE.computeIfAbsent(locale, l -> {
            final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(l);
            return new LocaleSeparators(symbols.getDecimalSeparator(), symbols.getGroupingSeparator());
        });
    }

    /**
     * Fast ASCII digit check.
     *
     * @param c character to test
     * @return {@code true} if {@code c} is between '0' and '9'
     */
    private static boolean isAsciiDigit(final char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks for scientific exponent markers.
     *
     * @param c character to test
     * @return {@code true} if the character is 'e' or 'E'
     */
    private static boolean isExponentMarker(final char c) {
        return c == 'e' || c == 'E';
    }

    /**
     * Fast whitespace check.
     * <p>
     * Uses {@link Character#isWhitespace(char)} for correctness (covers tabs/newlines and Unicode whitespace).
     *
     * @param c character to test
     * @return {@code true} if {@code c} is whitespace
     */
    private static boolean isWhitespace(final char c) {
        return Character.isWhitespace(c);
    }

    /**
     * Small immutable container for locale separators.
     *
     * @param decimalSeparator  the locale-specific decimal separator
     * @param groupingSeparator the locale-specific grouping separator
     */
    private record LocaleSeparators(char decimalSeparator, char groupingSeparator) {
    }

}
