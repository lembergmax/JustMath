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
package com.mlprograms.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class BigNumbersConstantsCacheTest {

    @Test
    void piIsConsistentAcrossCallsForSameContext() {
        MathContext mc = new MathContext(50, RoundingMode.HALF_UP);
        BigNumber a = BigNumbers.pi(mc);
        BigNumber b = BigNumbers.pi(mc);
        assertEquals(a.toString(), b.toString());
        assertTrue(a.toString().startsWith("3.14159"));
    }

    @Test
    void eIsConsistentAcrossCallsForSameContext() {
        MathContext mc = new MathContext(50, RoundingMode.HALF_UP);
        BigNumber a = BigNumbers.e(mc);
        BigNumber b = BigNumbers.e(mc);
        assertEquals(a.toString(), b.toString());
        assertTrue(a.toString().startsWith("2.71828"));
    }

    @Test
    void piDiffersBetweenDifferentPrecisions() {
        BigNumber low = BigNumbers.pi(new MathContext(10, RoundingMode.HALF_UP));
        BigNumber high = BigNumbers.pi(new MathContext(50, RoundingMode.HALF_UP));
        assertNotEquals(low.toString(), high.toString());
    }
}
