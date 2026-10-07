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

package io.github.lembergmax.justmath.bignumber;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the (hyperbolic / inverse-hyperbolic) {@link MathContext} contract: the public
 * methods must return a value rounded to the caller's requested precision — neither carrying the internal
 * guard digits (over-precision) nor silently truncating to {@code double} precision (under-precision).
 */
class HyperbolicPrecisionContractTest {

    private static final MathContext MC_20 = new MathContext(20, RoundingMode.HALF_UP);

    @Test
    @DisplayName("sinh/cosh/tanh/coth round to the requested precision (no leaked guard digits)")
    void hyperbolicResultsRespectRequestedPrecision() {
        final BigNumber x = new BigNumber("2", Locale.US);
        assertAtMostSignificantDigits(x.sinh(MC_20), 20);
        assertAtMostSignificantDigits(x.cosh(MC_20), 20);
        assertAtMostSignificantDigits(x.tanh(MC_20), 20);
        assertAtMostSignificantDigits(x.coth(MC_20), 20);
    }

    @Test
    @DisplayName("asinh/acosh/atanh/acoth round to the requested precision (no leaked guard digits)")
    void inverseHyperbolicResultsRespectRequestedPrecision() {
        assertAtMostSignificantDigits(new BigNumber("2", Locale.US).asinh(MC_20), 20);
        assertAtMostSignificantDigits(new BigNumber("3", Locale.US).acosh(MC_20), 20);
        assertAtMostSignificantDigits(new BigNumber("0.5", Locale.US).atanh(MC_20), 20);
        assertAtMostSignificantDigits(new BigNumber("2", Locale.US).acoth(MC_20), 20);
    }

    @Test
    @DisplayName("asinh(2) at 40 digits is accurate far beyond double precision (round-trips through sinh)")
    void inverseHyperbolicIsAccurateBeyondDoublePrecision() {
        final MathContext mc40 = new MathContext(40, RoundingMode.HALF_UP);
        final MathContext mc50 = new MathContext(50, RoundingMode.HALF_UP);

        final BigNumber asinh2 = new BigNumber("2", Locale.US).asinh(mc40);
        final BigNumber roundTrip = asinh2.sinh(mc50);

        final BigDecimal error = roundTrip.toBigDecimal().subtract(BigDecimal.valueOf(2)).abs();
        // A double-only asinh would be accurate to only ~1e-16, far short of this bound.
        assertTrue(error.compareTo(new BigDecimal("1e-30")) < 0,
                "asinh(2) must be accurate to far more than double precision; round-trip error was " + error.toPlainString());
    }

    private static void assertAtMostSignificantDigits(final BigNumber value, final int maxSignificantDigits) {
        final int actual = value.toBigDecimal().stripTrailingZeros().precision();
        assertTrue(actual <= maxSignificantDigits,
                "expected at most " + maxSignificantDigits + " significant digits but got " + actual + " (value=" + value + ")");
    }
}
