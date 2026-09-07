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

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests proving that {@code exp} and non-integer {@code power} honor the requested
 * {@link MathContext} precision instead of silently returning a ~16-digit {@code double} approximation.
 */
class BasicMathDoublePrecisionTest {

    private static final MathContext MC_50 = new MathContext(50, RoundingMode.HALF_UP);

    @Test
    @DisplayName("2^0.5 at 50 digits is a true high-precision square root (squares back to 2)")
    void nonIntegerPowerHonorsHighPrecision() {
        final BigNumber root = new BigNumber("2", Locale.US).power(new BigNumber("0.5", Locale.US), MC_50);
        final BigDecimal squared = root.toBigDecimal().multiply(root.toBigDecimal());
        final BigDecimal error = squared.subtract(BigDecimal.valueOf(2)).abs();
        // A double-only result would square back to 2 only to ~1e-16.
        assertTrue(error.compareTo(new BigDecimal("1e-40")) < 0,
                "2^0.5 must be accurate well beyond double precision; (2^0.5)^2 - 2 = " + error.toPlainString());
    }

    @Test
    @DisplayName("exp(1) at 50 digits matches e to far beyond double precision")
    void expHonorsHighPrecision() {
        final BigNumber expOne = new BigNumber("1", Locale.US).exp(MC_50);
        final BigDecimal error = expOne.toBigDecimal().subtract(BigNumbers.e(MC_50).toBigDecimal()).abs();
        assertTrue(error.compareTo(new BigDecimal("1e-40")) < 0,
                "exp(1) must equal e well beyond double precision; error = " + error.toPlainString());
    }
}
