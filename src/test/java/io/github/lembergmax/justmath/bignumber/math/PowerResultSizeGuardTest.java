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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for the integer-power result-size guard: it must reject only powers whose result is
 * genuinely huge, never a power whose result is trivially small. A base of magnitude {@code 1} produces
 * {@code ±1} for any exponent and must therefore always be accepted, even for exponents far beyond the
 * digit limit.
 */
class PowerResultSizeGuardTest {

    private static final BigNumber HUGE_EXPONENT = new BigNumber("2000000", Locale.US);
    private static final BigNumber HUGE_ODD_EXPONENT = new BigNumber("2000001", Locale.US);
    private static final BigNumber ASTRONOMICAL_EXPONENT = new BigNumber("999999999999999999999", Locale.US);

    @Test
    @Timeout(10)
    @DisplayName("1^n is exactly 1 for an exponent far beyond the result-size limit")
    void oneToHugePowerIsOne() {
        assertEquals("1", new BigNumber("1", Locale.US).power(HUGE_EXPONENT).trim().toString());
        assertEquals("1", new BigNumber("1", Locale.US).power(ASTRONOMICAL_EXPONENT).trim().toString());
    }

    @Test
    @Timeout(10)
    @DisplayName("(-1)^n is +1 / -1 by parity for an exponent far beyond the result-size limit")
    void minusOneToHugePowerRespectsParity() {
        assertEquals("1", new BigNumber("-1", Locale.US).power(HUGE_EXPONENT).trim().toString());
        assertEquals("-1", new BigNumber("-1", Locale.US).power(HUGE_ODD_EXPONENT).trim().toString());
    }
}
