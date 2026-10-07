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
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for the previously broken {@code atan} near {@code |x| = 1}, where the hand-rolled
 * Maclaurin series converged only harmonically and stalled at ~4–5 correct digits regardless of the
 * requested precision. {@code atan(1)} must equal {@code π/4} to the requested precision.
 */
class AtanPrecisionTest {

    @Test
    @DisplayName("atan(1) equals π/4 to 50 digits (was ~4 digits with the old series)")
    void atanOneMatchesPiOverFourAtHighPrecision() {
        final MathContext mc50 = new MathContext(50, RoundingMode.HALF_UP);
        final MathContext mc60 = new MathContext(60, RoundingMode.HALF_UP);

        final BigNumber atanOne = new BigNumber("1", Locale.US).atan(mc50, TrigonometricMode.RAD, Locale.US);
        final BigDecimal piOverFour = BigNumbers.pi(mc60).toBigDecimal()
                .divide(BigDecimal.valueOf(4), mc60);

        final BigDecimal error = atanOne.toBigDecimal().subtract(piOverFour).abs();
        assertTrue(error.compareTo(new BigDecimal("1e-45")) < 0,
                "atan(1) must equal π/4 to ~50 digits; error was " + error.toPlainString());
    }

    @Test
    @DisplayName("atan(-1) equals -π/4 to 50 digits")
    void atanMinusOneMatchesNegativePiOverFour() {
        final MathContext mc50 = new MathContext(50, RoundingMode.HALF_UP);
        final MathContext mc60 = new MathContext(60, RoundingMode.HALF_UP);

        final BigNumber atanMinusOne = new BigNumber("-1", Locale.US).atan(mc50, TrigonometricMode.RAD, Locale.US);
        final BigDecimal negPiOverFour = BigNumbers.pi(mc60).toBigDecimal()
                .divide(BigDecimal.valueOf(4), mc60).negate();

        final BigDecimal error = atanMinusOne.toBigDecimal().subtract(negPiOverFour).abs();
        assertTrue(error.compareTo(new BigDecimal("1e-45")) < 0,
                "atan(-1) must equal -π/4 to ~50 digits; error was " + error.toPlainString());
    }
}
