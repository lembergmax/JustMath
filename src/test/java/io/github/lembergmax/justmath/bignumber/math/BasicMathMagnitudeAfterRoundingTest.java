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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
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
 * A result with more integer digits than the precision must keep its magnitude: the digits beyond the
 * precision are zeros, not gone (#202). Before the fix {@code 100!/3} lost 58 powers of ten without an error.
 */
class BasicMathMagnitudeAfterRoundingTest {

    private static final Locale LOCALE = Locale.US;

    private static final int DEFAULT_PRECISION = 100;

    private static final BigDecimal RELATIVE_TOLERANCE_20_DIGITS = new BigDecimal("1E-18");

    private static final BigDecimal RELATIVE_TOLERANCE_100_DIGITS = new BigDecimal("1E-90");

    private static BigNumber number(final String value) {
        return new BigNumber(value, LOCALE);
    }

    @ParameterizedTest(name = "{0} / {1} with {2} digits")
    @DisplayName("an integer quotient with more digits than the precision keeps its magnitude")
    @CsvSource(textBlock = """
            1000000000000000000000000000000, 3, 5
            2000000000000000000000000000000, 3, 5
            999999999999, 1, 3
            999999999999, 7, 3
            123456789012345678901234567890, 1, 10
            -123456789012345678901234567890, 7, 10
            100000000000000000000, 1, 1
            1, 3, 1
            """)
    void divisionKeepsTheMagnitude(final String dividend, final String divisor, final int precision) {
        for (final RoundingMode roundingMode : new RoundingMode[]{RoundingMode.HALF_UP, RoundingMode.HALF_EVEN, RoundingMode.DOWN}) {
            final MathContext mathContext = new MathContext(precision, roundingMode);
            final BigDecimal expected = new BigDecimal(dividend).divide(new BigDecimal(divisor), mathContext);

            final BigDecimal actual = BasicMath.divide(number(dividend), number(divisor), mathContext, LOCALE).toBigDecimal();

            assertEquals(0, expected.compareTo(actual),
                    () -> dividend + " / " + divisor + " with " + mathContext + ": expected " + expected.toPlainString()
                            + " but was " + actual.toPlainString());
        }
    }

    @ParameterizedTest
    @EnumSource(value = RoundingMode.class, names = {"HALF_UP", "HALF_EVEN", "DOWN"})
    @DisplayName("rounding a carry in the kept digits still restores the removed integer digits")
    void carryInTheKeptDigitsKeepsTheMagnitude(final RoundingMode roundingMode) {
        final MathContext mathContext = new MathContext(3, roundingMode);
        final BigDecimal expected = new BigDecimal("999999999999").divide(BigDecimal.ONE, mathContext);

        final BigDecimal actual = BasicMath.divide(number("999999999999"), number("1"), mathContext, LOCALE).toBigDecimal();

        assertEquals(0, expected.compareTo(actual), expected.toPlainString() + " but was " + actual.toPlainString());
    }

    @Test
    @DisplayName("100!/3 has 158 digits at the default precision")
    void factorialQuotientKeepsItsMagnitude() {
        BigInteger factorial = BigInteger.ONE;
        for (int factor = 2; factor <= DEFAULT_PRECISION; factor++) {
            factorial = factorial.multiply(BigInteger.valueOf(factor));
        }
        final BigDecimal expected = new BigDecimal(factorial).divide(BigDecimal.valueOf(3), new MathContext(DEFAULT_PRECISION, RoundingMode.HALF_UP));

        final BigDecimal actual = new CalculatorEngine().evaluate("100!/3").toBigDecimal();

        assertEquals(0, expected.compareTo(actual), "100!/3 expected " + expected + " but was " + actual.toPlainString());
        assertEquals(158, actual.precision() - actual.scale(), "digits before the decimal point");
    }

    @Test
    @DisplayName("10^150/3 has 150 digits at the default precision")
    void largePowerQuotientKeepsItsMagnitude() {
        final BigDecimal expected = BigDecimal.TEN.pow(150).divide(BigDecimal.valueOf(3), new MathContext(DEFAULT_PRECISION, RoundingMode.HALF_UP));

        final BigDecimal actual = new CalculatorEngine().evaluate("10^150/3").toBigDecimal();

        assertEquals(0, expected.compareTo(actual), "10^150/3 expected " + expected + " but was " + actual.toPlainString());
    }

    @ParameterizedTest(name = "exp({0}) with 20 digits")
    @DisplayName("exp keeps its magnitude for large and small results")
    @CsvSource({"50", "100", "-50", "300"})
    void expKeepsItsMagnitude(final String argument) {
        final MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);
        final BigDecimal reference = BigDecimalMath.exp(new BigDecimal(argument), new MathContext(40));

        final BigDecimal actual = BasicMath.exp(number(argument), mathContext, LOCALE).toBigDecimal();

        assertSameMagnitudeAndLeadingDigits(reference, actual, RELATIVE_TOLERANCE_20_DIGITS, "exp(" + argument + ")");
    }

    @ParameterizedTest(name = "10^{0} at the default precision")
    @DisplayName("a non-integer power keeps its magnitude beyond 100 integer digits")
    @CsvSource({"140.5", "250.25"})
    void nonIntegerPowerKeepsItsMagnitude(final String exponent) {
        final MathContext mathContext = new MathContext(DEFAULT_PRECISION, RoundingMode.HALF_UP);
        final BigDecimal reference = BigDecimalMath.pow(BigDecimal.TEN, new BigDecimal(exponent), new MathContext(DEFAULT_PRECISION + 20));

        final BigDecimal actual = BasicMath.power(number("10"), number(exponent), mathContext, LOCALE).toBigDecimal();

        assertSameMagnitudeAndLeadingDigits(reference, actual, RELATIVE_TOLERANCE_100_DIGITS, "10^" + exponent);
    }

    private static void assertSameMagnitudeAndLeadingDigits(
            final BigDecimal reference,
            final BigDecimal actual,
            final BigDecimal relativeTolerance,
            final String description
    ) {
        final BigDecimal error = reference.subtract(actual).abs();
        assertTrue(error.compareTo(reference.abs().multiply(relativeTolerance)) <= 0,
                () -> description + ": reference " + reference.round(new MathContext(25)) + " but was " + actual.round(new MathContext(25))
                        + " (" + actual.toPlainString().length() + " characters)");
    }

}
