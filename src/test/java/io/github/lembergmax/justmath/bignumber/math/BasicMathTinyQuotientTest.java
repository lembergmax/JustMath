/*
 * Copyright (c) 2026 Max Lemberg
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

package io.github.lembergmax.justmath.bignumber.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Locale;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.calculator.CalculatorEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * A quotient far below 1 is a valid result, not zero: the precision counts significant digits, not decimal
 * places (#203). Before the fix {@code 2^-1000} and {@code 1/10^200} returned 0.
 */
class BasicMathTinyQuotientTest {

    private static final Locale LOCALE = Locale.US;

    private static final int DEFAULT_PRECISION = 100;

    private static final MathContext DEFAULT_CONTEXT = new MathContext(DEFAULT_PRECISION, RoundingMode.HALF_UP);

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private static final int HUGE_EXPONENT = 100_000;

    private static BigNumber number(final String value) {
        return new BigNumber(value, LOCALE);
    }

    @ParameterizedTest(name = "{0} / {1} with {2} digits")
    @DisplayName("a quotient below 10^-(precision+2) keeps its value")
    @CsvSource(textBlock = """
            1, 1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000, 100
            -0.1, -1001, 1
            1, 3000000000000000000000000000000, 1
            7, 123456789012345678901234567890123456789, 5
            0.000000000000000001, 98765432109876543210, 8
            """)
    void tinyQuotientKeepsItsValue(final String dividend, final String divisor, final int precision) {
        for (final RoundingMode roundingMode : new RoundingMode[]{RoundingMode.HALF_UP, RoundingMode.HALF_EVEN, RoundingMode.DOWN}) {
            final MathContext mathContext = new MathContext(precision, roundingMode);
            final BigDecimal expected = new BigDecimal(dividend).divide(new BigDecimal(divisor), mathContext);

            final BigDecimal actual = BasicMath.divide(number(dividend), number(divisor), mathContext, LOCALE).toBigDecimal();

            assertEquals(0, expected.compareTo(actual),
                    () -> dividend + " / " + divisor + " with " + mathContext + ": expected " + expected.toPlainString()
                            + " but was " + actual.toPlainString());
        }
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("the engine returns the tiny result instead of 0")
    @CsvSource(delimiter = '|', textBlock = """
            2^(0-1000)  | 1 | 2^1000
            1/2^1000    | 1 | 2^1000
            1/10^200    | 1 | 10^200
            10^(0-200)  | 1 | 10^200
            1/10^300    | 1 | 10^300
            """)
    void engineReturnsTheTinyResult(final String expression, final int numerator, final String denominator) {
        final String[] powerParts = denominator.split("\\^");
        final BigDecimal expected = BigDecimal.valueOf(numerator)
                .divide(new BigDecimal(powerParts[0]).pow(Integer.parseInt(powerParts[1])), DEFAULT_CONTEXT);

        final BigDecimal actual = new CalculatorEngine().evaluate(expression).toBigDecimal();

        assertEquals(0, expected.compareTo(actual), expression + ": expected " + expected + " but was " + actual.toPlainString());
    }

    @Test
    @DisplayName("a negative integer power with a large exponent keeps its value, with two rounding modes")
    void negativeIntegerPowerKeepsItsValue() {
        for (final RoundingMode roundingMode : new RoundingMode[]{RoundingMode.HALF_UP, RoundingMode.UP}) {
            final MathContext mathContext = new MathContext(4, roundingMode);
            final BigDecimal expected = BigDecimal.ONE.divide(new BigDecimal(-941478).pow(8), mathContext);

            final BigDecimal actual = BasicMath.power(number("-941478"), number("-8"), mathContext, LOCALE).toBigDecimal();

            assertEquals(0, expected.compareTo(actual), mathContext + ": expected " + expected + " but was " + actual.toPlainString());
        }
    }

    @ParameterizedTest(name = "exp({0}) with 20 digits")
    @DisplayName("exp of a large negative argument keeps its value")
    @CsvSource({"-100", "-1000", "-50"})
    void expOfLargeNegativeArgumentKeepsItsValue(final String argument) {
        final MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);
        final BigDecimal reference = BigDecimalMath.exp(new BigDecimal(argument), new MathContext(40));

        final BigDecimal actual = BasicMath.exp(number(argument), mathContext, LOCALE).toBigDecimal();

        assertTrue(reference.subtract(actual).abs().compareTo(reference.multiply(new BigDecimal("1E-18"))) <= 0,
                () -> "exp(" + argument + "): reference " + reference.round(new MathContext(25)) + " but was " + actual.round(new MathContext(25)));
    }

    @Test
    @DisplayName("10^-140.5 keeps its value at the default precision")
    void negativeFractionalPowerKeepsItsValue() {
        final BigDecimal reference = BigDecimalMath.pow(BigDecimal.TEN, new BigDecimal("-140.5"), new MathContext(DEFAULT_PRECISION + 20));

        final BigDecimal actual = BasicMath.power(number("10"), number("-140.5"), DEFAULT_CONTEXT, LOCALE).toBigDecimal();

        assertTrue(reference.subtract(actual).abs().compareTo(reference.multiply(new BigDecimal("1E-90"))) <= 0,
                () -> "reference " + reference.round(new MathContext(25)) + " but was " + actual.round(new MathContext(25)));
    }

    @ParameterizedTest
    @EnumSource(value = RoundingMode.class, names = {"HALF_UP", "DOWN"})
    @DisplayName("a quotient whose first digit is 100,000 places after the decimal point is computed quickly")
    void hugeLeadingZeroRunIsComputedQuickly(final RoundingMode roundingMode) {
        final String divisor = "1" + "0".repeat(HUGE_EXPONENT);
        final MathContext mathContext = new MathContext(DEFAULT_PRECISION, roundingMode);

        final BigDecimal actual = assertTimeoutPreemptively(TIMEOUT,
                () -> BasicMath.divide(number("1"), number(divisor), mathContext, LOCALE).toBigDecimal());

        assertEquals(0, new BigDecimal(BigDecimal.ONE.unscaledValue(), HUGE_EXPONENT).compareTo(actual));
    }

}
