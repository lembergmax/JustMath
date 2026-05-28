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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Audit fix K4: {@code equals} compares numeric value (compareTo == 0); {@code hashCode}
 * must therefore be identical for numerically equal values that differ only in scale
 * ({@code "1"}, {@code "1.0"}, {@code "1.00"}), so {@link BigNumber} works correctly in
 * hash-based collections.
 */
class BigNumberEqualsHashCodeTest {

    @Test
    @DisplayName("Numerically equal values share equals() and hashCode()")
    void equalValuesShareHashCode() {
        final String[] ones = {"1", "1.0", "1.00", "1.000000"};
        final BigNumber reference = new BigNumber("1");
        for (final String form : ones) {
            final BigNumber value = new BigNumber(form);
            assertEquals(reference, value, form + " should equal 1");
            assertEquals(reference.hashCode(), value.hashCode(),
                    "hashCode of " + form + " must equal hashCode of 1");
        }
    }

    @Test
    @DisplayName("Scaled tens and zeros agree on hashCode")
    void scaledValuesShareHashCode() {
        assertEquals(new BigNumber("10").hashCode(), new BigNumber("10.0").hashCode());
        assertEquals(new BigNumber("0").hashCode(), new BigNumber("0.0").hashCode());
        assertEquals(new BigNumber("0").hashCode(), new BigNumber("0.000").hashCode());
        assertEquals(new BigNumber("-0").hashCode(), new BigNumber("0").hashCode());
    }

    @Test
    @DisplayName("HashSet collapses numerically equal but differently-scaled values")
    void hashSetDedupesEqualValues() {
        final Set<BigNumber> set = new HashSet<>();
        set.add(new BigNumber("2"));
        set.add(new BigNumber("2.0"));
        set.add(new BigNumber("2.00"));
        assertEquals(1, set.size(), "all three represent the value 2");
        assertTrue(set.contains(new BigNumber("2.000")));
    }
}
