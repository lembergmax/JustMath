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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;
import static org.junit.jupiter.api.Assertions.*;

class BigNumberParserTest {

    private static final Locale LOCALE_US = Locale.US;
    private static final Locale LOCALE_DE = Locale.GERMANY;

    private BigNumberParser parser;

    @BeforeEach
    void setUp() {
        parser = new BigNumberParser();
    }

    @Nested
    class ParseTests {

        @Test
        void parse_blank_returnsZero() {
            assertSame(ZERO, parser.parse("", LOCALE_US));
            assertSame(ZERO, parser.parse("   ", LOCALE_US));
        }

        @Test
        void parse_invalid_returnsZero() {
            assertSame(ZERO, parser.parse("abc", LOCALE_US));
            assertSame(ZERO, parser.parse("12a3", LOCALE_US));
            assertSame(ZERO, parser.parse("--1", LOCALE_US));
        }

        @Test
        void parse_us_grouping_and_decimal() {
            BigNumber result = parser.parse("1,234.56", LOCALE_US);
            assertBigNumberParts(result, "1234", "56", false, LOCALE_US);
        }

        @Test
        void parse_us_integer_afterDecimalIsZero() {
            BigNumber result = parser.parse("1,234", LOCALE_US);
            assertBigNumberParts(result, "1234", "0", false, LOCALE_US);
        }

        @Test
        void parse_negative() {
            BigNumber result = parser.parse("-1,234.56", LOCALE_US);
            assertBigNumberParts(result, "1234", "56", true, LOCALE_US);
        }

        @Test
        void parse_de_grouping_and_decimalComma() {
            BigNumber result = parser.parse("1.234,56", LOCALE_DE);
            assertBigNumberParts(result, "1234", "56", false, LOCALE_DE);
        }

        @Test
        void parse_de_integer_groupingDots() {
            BigNumber result = parser.parse("1.234.567", LOCALE_DE);
            assertBigNumberParts(result, "1234567", "0", false, LOCALE_DE);
        }

        @Test
        void parse_de_negative() {
            BigNumber result = parser.parse("-1.234,56", LOCALE_DE);
            assertBigNumberParts(result, "1234", "56", true, LOCALE_DE);
        }

        @Test
        void parse_trimsWhitespace() {
            BigNumber result = parser.parse("   1.234,56   ", LOCALE_DE);
            assertBigNumberParts(result, "1234", "56", false, LOCALE_DE);
        }
    }

    @Nested
    class GroupingTests {

        @ParameterizedTest
        @CsvSource({"0, 0", "12, 12", "123, 123", "'1234', '1,234'", "'12345', '12,345'", "'123456', '123,456'", "'1234567', '1,234,567'", "'123456789', '123,456,789'"})
        void grouping_insertsEveryThreeDigits(String integerPart, String expected) {
            String actual = parser.getGroupedBeforeDecimal(integerPart, ',').toString();
            assertEquals(expected, actual);
        }
    }

    @Nested
    class FormatTests {

        @Test
        void format_groupsInUs() {
            BigNumber input = BigNumber.builder().mathContext(BigNumbers.DEFAULT_MATH_CONTEXT).trigonometricMode(TrigonometricMode.DEG).locale(LOCALE_US).valueBeforeDecimalPoint("1234567").valueAfterDecimalPoint("0").isNegative(false).build();

            BigNumber formatted = parser.format(input, LOCALE_US);

            assertBigNumberParts(formatted, "1234567", "0", false, LOCALE_US);
        }

        @Test
        void format_doesNotAppendDecimalWhenAfterDecimalIsZero() {
            BigNumber input = BigNumber.builder().mathContext(BigNumbers.DEFAULT_MATH_CONTEXT).trigonometricMode(TrigonometricMode.DEG).locale(LOCALE_US).valueBeforeDecimalPoint("1234").valueAfterDecimalPoint("0").isNegative(false).build();

            BigNumber formatted = parser.format(input, LOCALE_US);

            assertEquals("0", formatted.getValueAfterDecimalPoint());
            assertEquals("1234", formatted.getValueBeforeDecimalPoint());
        }

        @Test
        void format_preservesFractionDigits() {
            BigNumber input = BigNumber.builder().mathContext(BigNumbers.DEFAULT_MATH_CONTEXT).trigonometricMode(TrigonometricMode.DEG).locale(LOCALE_US).valueBeforeDecimalPoint("1234").valueAfterDecimalPoint("0500").isNegative(false).build();

            BigNumber formatted = parser.format(input, LOCALE_US);

            assertBigNumberParts(formatted, "1234", "0500", false, LOCALE_US);
        }

        @Test
        void format_negative() {
            BigNumber input = BigNumber.builder().mathContext(BigNumbers.DEFAULT_MATH_CONTEXT).trigonometricMode(TrigonometricMode.DEG).locale(LOCALE_US).valueBeforeDecimalPoint("1234567").valueAfterDecimalPoint("89").isNegative(true).build();

            BigNumber formatted = parser.format(input, LOCALE_DE);

            assertBigNumberParts(formatted, "1234567", "89", true, LOCALE_DE);
        }
    }

    @Nested
    class ParseAndFormatTests {

        @Test
        void supportedLocales_precondition() {
            assertNotNull(LocalesConfig.SUPPORTED_LOCALES, "SUPPORTED_LOCALES darf nicht null sein.");
            assertTrue(LocalesConfig.SUPPORTED_LOCALES.length > 0, "SUPPORTED_LOCALES darf nicht leer sein.");

            assertTrue(Arrays.asList(LocalesConfig.SUPPORTED_LOCALES).contains(LOCALE_US),
                    "LocalesConfig.SUPPORTED_LOCALES muss Locale.US enthalten für diese Tests.");
            assertTrue(Arrays.asList(LocalesConfig.SUPPORTED_LOCALES).contains(LOCALE_DE),
                    "LocalesConfig.SUPPORTED_LOCALES muss Locale.GERMANY enthalten für diese Tests.");
        }

        @Test
        void parseAndFormat_deToUs() {
            BigNumber result = parser.parseAndFormat("1.234,56", LOCALE_US);
            assertBigNumberParts(result, "1234", "56", false, LOCALE_US);
        }

        @Test
        void parseAndFormat_usToDe() {
            BigNumber result = parser.parseAndFormat("1,234.56", LOCALE_DE);
            assertBigNumberParts(result, "1234", "56", false, LOCALE_DE);
        }

        @Test
        void parseAndFormat_negative() {
            BigNumber result = parser.parseAndFormat("-1.234,56", LOCALE_US);
            assertBigNumberParts(result, "1234", "56", true, LOCALE_US);
        }

        @Test
        void parseAndFormat_invalid_throws() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parser.parseAndFormat("not_a_number", LOCALE_US));

            assertTrue(ex.getMessage().contains("not_a_number"));
        }

        @Test
        void parseAndFormat_trimsWhitespace() {
            BigNumber result = parser.parseAndFormat("   1.234,56   ", LOCALE_US);
            assertBigNumberParts(result, "1234", "56", false, LOCALE_US);
        }
    }

    private static void assertBigNumberParts(BigNumber actual, String expectedBeforeDecimal, String expectedAfterDecimal, boolean expectedNegative, Locale expectedLocale) {
        assertNotNull(actual);
        assertEquals(expectedBeforeDecimal, actual.getValueBeforeDecimalPoint(), "valueBeforeDecimalPoint");
        assertEquals(expectedAfterDecimal, actual.getValueAfterDecimalPoint(), "valueAfterDecimalPoint");
        assertEquals(expectedNegative, actual.isNegative(), "isNegative");
        assertEquals(expectedLocale, actual.getLocale(), "locale");
    }

}
