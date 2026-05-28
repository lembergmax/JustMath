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
package com.mlprograms.justmath.bignumber.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Audit fix K3: {@code BasicMath.divide} must honour the caller-supplied
 * {@link RoundingMode} from the {@link MathContext}, not the library default.
 *
 * <p>The previous implementation hard-coded
 * {@code BigNumbers.DEFAULT_MATH_CONTEXT.getRoundingMode()} (HALF_UP) inside the
 * rounding decision, so every directed-rounding request was silently ignored.</p>
 */
class BasicMathRoundingModeTest {

    private static String divide(final String dividend, final String divisor, final int precision, final RoundingMode mode) {
        return new BigNumber(dividend)
                .divide(new BigNumber(divisor), new MathContext(precision, mode), Locale.US)
                .toString();
    }

    @Test
    @DisplayName("1/3 at 1 significant digit respects UP vs DOWN")
    void oneThirdDirectedRounding() {
        assertEquals("0.4", divide("1", "3", 1, RoundingMode.UP));
        assertEquals("0.3", divide("1", "3", 1, RoundingMode.DOWN));
        assertEquals("0.4", divide("1", "3", 1, RoundingMode.CEILING));
        assertEquals("0.3", divide("1", "3", 1, RoundingMode.FLOOR));
        assertEquals("0.3", divide("1", "3", 1, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("-1/3 at 1 significant digit respects CEILING vs FLOOR sign semantics")
    void negativeOneThirdDirectedRounding() {
        assertEquals("-0.3", divide("-1", "3", 1, RoundingMode.CEILING));
        assertEquals("-0.4", divide("-1", "3", 1, RoundingMode.FLOOR));
        assertEquals("-0.4", divide("-1", "3", 1, RoundingMode.UP));
        assertEquals("-0.3", divide("-1", "3", 1, RoundingMode.DOWN));
    }

    @Test
    @DisplayName("Exact tie 0.25 distinguishes HALF_UP / HALF_DOWN / HALF_EVEN")
    void halfModeTieBreaking() {
        // 1/4 = 0.25 — a clean tie at one significant digit.
        assertEquals("0.3", divide("1", "4", 1, RoundingMode.HALF_UP));
        assertEquals("0.2", divide("1", "4", 1, RoundingMode.HALF_DOWN));
        assertEquals("0.2", divide("1", "4", 1, RoundingMode.HALF_EVEN));
    }
}
