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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class BigNumberValueOfCacheTest {

    @Test
    void valueOfReturnsEqualInstancesForCachedRange() {
        // valueOf still uses an internal cache as an optimisation, but returns a defensive
        // clone of the cached template so that callers' mutations (setters / negateThis)
        // cannot corrupt the shared instance. The contract is therefore value equality,
        // not reference identity.
        for (int i = -16; i <= 256; i++) {
            assertEquals(BigNumber.valueOf(i), BigNumber.valueOf(i),
                    "valueOf(" + i + ") should produce equal numbers");
        }
    }

    @Test
    void valueOfOutsideCachedRangeReturnsDistinctInstances() {
        BigNumber a = BigNumber.valueOf(1000L);
        BigNumber b = BigNumber.valueOf(1000L);
        assertNotSame(a, b);
        assertEquals(a, b);
    }

    @Test
    void valueOfIntAndLongAgreeForOverlap() {
        assertEquals(BigNumber.valueOf(5), BigNumber.valueOf(5L));
    }

    @Test
    void valueOfBigDecimalProducesEqualNumber() {
        BigNumber n = BigNumber.valueOf(new BigDecimal("42.5"));
        assertEquals("42.5", n.toString());
    }

    @Test
    void valueOfStringRoundTrips() {
        assertEquals("7", BigNumber.valueOf("7").toString());
    }
}
