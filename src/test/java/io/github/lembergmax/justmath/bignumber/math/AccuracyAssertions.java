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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Assertions for the accuracy of a result, measured in units in the last place (ulp) of the requested precision.
 *
 * <p>One ulp of a value with {@code precision} significant digits is {@code 10^(e - precision + 1)}, where
 * {@code e} is the decimal exponent of its leading digit. The measure is relative to the size of the value, so a
 * result near zero must be as accurate in its own digits as a result near one.</p>
 */
public final class AccuracyAssertions {

    private static final int REPORT_DIGITS = 5;

    private AccuracyAssertions() {
    }

    /**
     * Fails if {@code actual} differs from {@code reference} by more than {@code maxUlps} units in the last place.
     *
     * @param reference   the correct value, computed with more digits than requested
     * @param actual      the value under test
     * @param precision   the requested number of significant digits
     * @param maxUlps     the largest accepted difference in units in the last place
     * @param description what was computed, for the failure message
     */
    public static void assertWithinUlps(final BigDecimal reference, final BigDecimal actual, final int precision, final double maxUlps, final String description) {
        if (reference.signum() == 0) {
            assertTrue(actual.signum() == 0, () -> description + ": reference 0 but was " + actual.toPlainString());
            return;
        }

        final BigDecimal ulp = unitInTheLastPlace(reference, precision);
        final BigDecimal tolerance = ulp.multiply(BigDecimal.valueOf(maxUlps));
        final BigDecimal error = reference.subtract(actual).abs();

        assertTrue(error.compareTo(tolerance) <= 0,
                () -> description + ": reference " + reference.round(new MathContext(precision + REPORT_DIGITS)).toPlainString()
                        + " but was " + actual.toPlainString() + " (" + error.divide(ulp, new MathContext(REPORT_DIGITS)).toPlainString() + " ulp)");
    }

    /**
     * Returns one unit in the last place of {@code value} rounded to {@code precision} significant digits.
     *
     * @param value     a non-zero value
     * @param precision the number of significant digits
     * @return {@code 10^(exponent of the leading digit - precision + 1)}
     */
    public static BigDecimal unitInTheLastPlace(final BigDecimal value, final int precision) {
        final int leadingDigitExponent = value.precision() - value.scale() - 1;
        return BigDecimal.ONE.scaleByPowerOfTen(leadingDigitExponent - precision + 1);
    }

}
