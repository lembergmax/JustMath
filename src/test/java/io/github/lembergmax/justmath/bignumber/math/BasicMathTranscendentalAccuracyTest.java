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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Locale;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArithmeticException;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * {@code exp} and the power with a fractional exponent were accurate to only one or two units in the last place,
 * and wrong for large arguments, because the range reduction and the guard digits did not grow with the argument
 * (#205).
 */
class BasicMathTranscendentalAccuracyTest {

    private static final Locale LOCALE = Locale.US;

    private static final int REFERENCE_GUARD_DIGITS = 40;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private static final int LARGE_ARGUMENT = 1_000_000;

    private static final int TOO_LARGE_ARGUMENT = 10_000_000;

    private static BigNumber number(final String value) {
        return new BigNumber(value, LOCALE);
    }

    private static BigDecimal correctlyRoundedExp(final String argument, final MathContext mathContext) {
        return BigDecimalMath.exp(new BigDecimal(argument), new MathContext(mathContext.getPrecision() + REFERENCE_GUARD_DIGITS))
                .round(mathContext);
    }

    private static void assertWithinOneUnitInTheLastPlace(final BigDecimal reference, final BigDecimal actual, final int precision, final String description) {
        final BigDecimal tolerance = reference.abs().movePointLeft(precision - 1);
        assertTrue(reference.subtract(actual).abs().compareTo(tolerance) <= 0,
                () -> description + ": reference " + reference.round(new MathContext(precision + 3)) + " but was " + actual.round(new MathContext(precision + 3)));
    }

    @ParameterizedTest(name = "exp({0}) with {1} digits")
    @DisplayName("exp equals the correctly rounded value for ordinary arguments")
    @CsvSource({"-1, 20", "-5, 20", "1, 30", "0.5, 25", "10, 20", "-10, 20", "3.14159, 40"})
    void expIsCorrectlyRoundedForOrdinaryArguments(final String argument, final int precision) {
        for (final RoundingMode roundingMode : new RoundingMode[]{RoundingMode.HALF_UP, RoundingMode.HALF_EVEN}) {
            final MathContext mathContext = new MathContext(precision, roundingMode);

            final BigDecimal actual = BasicMath.exp(number(argument), mathContext, LOCALE).toBigDecimal();

            assertEquals(0, correctlyRoundedExp(argument, mathContext).compareTo(actual),
                    () -> "exp(" + argument + ") with " + mathContext + " was " + actual.toPlainString());
        }
    }

    @ParameterizedTest(name = "exp({0}) with {1} digits")
    @DisplayName("exp of a large argument is accurate to every requested digit")
    @CsvSource({"2000, 10", "-2000, 10", "17656, 11", "-17656.0, 11", "100000, 15", "-100000, 15", "300, 50"})
    void expOfLargeArgumentIsAccurate(final String argument, final int precision) {
        final MathContext mathContext = new MathContext(precision, RoundingMode.HALF_UP);

        final BigDecimal actual = BasicMath.exp(number(argument), mathContext, LOCALE).toBigDecimal();

        assertWithinOneUnitInTheLastPlace(correctlyRoundedExp(argument, mathContext), actual, precision, "exp(" + argument + ")");
    }

    @ParameterizedTest(name = "{0} ^ {1} with {2} digits")
    @DisplayName("a power with a fractional exponent is accurate to every requested digit")
    @CsvSource({"4, 2.01, 22", "630990, -2.70, 22", "2, 0.5, 50", "10, 0.25, 30", "7, 33.3, 40", "1.0001, 12345.5, 30"})
    void fractionalPowerIsAccurate(final String base, final String exponent, final int precision) {
        for (final RoundingMode roundingMode : new RoundingMode[]{RoundingMode.HALF_UP, RoundingMode.HALF_EVEN}) {
            final MathContext mathContext = new MathContext(precision, roundingMode);
            final BigDecimal reference = BigDecimalMath.pow(new BigDecimal(base), new BigDecimal(exponent), new MathContext(precision + REFERENCE_GUARD_DIGITS))
                    .round(mathContext);

            final BigDecimal actual = BasicMath.power(number(base), number(exponent), mathContext, LOCALE).toBigDecimal();

            assertEquals(0, reference.compareTo(actual),
                    () -> base + " ^ " + exponent + " with " + mathContext + ": expected " + reference.toPlainString() + " but was " + actual.toPlainString());
        }
    }

    @Test
    @DisplayName("exp of an argument below the size limit is returned with its full magnitude")
    void expOfLargeArgumentKeepsItsMagnitude() {
        final MathContext mathContext = new MathContext(10, RoundingMode.HALF_UP);

        final BigDecimal actual = assertTimeoutPreemptively(TIMEOUT,
                () -> BasicMath.exp(number(Integer.toString(LARGE_ARGUMENT)), mathContext, LOCALE).toBigDecimal());

        assertEquals(434_295, actual.precision() - actual.scale(), "digits before the decimal point of e^1000000");
    }

    @ParameterizedTest(name = "exp({0})")
    @DisplayName("exp of an argument whose result would exceed one million digits is rejected")
    @CsvSource({"10000000", "-10000000", "99999999999999999999999"})
    void expBeyondTheSizeLimitIsRejected(final String argument) {
        final MathContext mathContext = new MathContext(10, RoundingMode.HALF_UP);

        final MathArithmeticException thrown = assertTimeoutPreemptively(TIMEOUT,
                () -> assertThrows(MathArithmeticException.class, () -> BasicMath.exp(number(argument), mathContext, LOCALE)));

        assertEquals(CalculatorErrorCode.MATH_OVERFLOW, thrown.getErrorCode());
    }

    @ParameterizedTest
    @EnumSource(value = RoundingMode.class, names = {"HALF_UP", "DOWN"})
    @DisplayName("a fractional power whose result would exceed one million digits is rejected")
    void fractionalPowerBeyondTheSizeLimitIsRejected(final RoundingMode roundingMode) {
        final MathContext mathContext = new MathContext(20, roundingMode);

        final MathArithmeticException thrown = assertTimeoutPreemptively(TIMEOUT,
                () -> assertThrows(MathArithmeticException.class,
                        () -> BasicMath.power(number("10"), number(Integer.toString(TOO_LARGE_ARGUMENT) + ".5"), mathContext, LOCALE)));

        assertEquals(CalculatorErrorCode.MATH_OVERFLOW, thrown.getErrorCode());
    }

    @Test
    @DisplayName("exp with a precision beyond the supported range fails instead of returning a wrong value")
    void expWithUnsupportedPrecisionFails() {
        final MathContext mathContext = new MathContext(20_000, RoundingMode.HALF_UP);

        final MathArithmeticException thrown = assertTimeoutPreemptively(TIMEOUT,
                () -> assertThrows(MathArithmeticException.class, () -> BasicMath.exp(number("1"), mathContext, LOCALE)));

        assertEquals(CalculatorErrorCode.MATH_OVERFLOW, thrown.getErrorCode());
    }

}
