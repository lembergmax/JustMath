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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test: {@link BigNumberMatrix} now overrides {@code equals}/{@code hashCode} by numeric value,
 * so equal-content matrices behave correctly as hash keys and in {@code contains}.
 */
class BigNumberMatrixEqualsHashCodeTest {

    @Test
    @DisplayName("equal-content matrices are equal and hash equally (scale-insensitive)")
    void valueEqualityAndHash() {
        final BigNumberMatrix a = new BigNumberMatrix("1.0,2;3,4.00", Locale.US);
        final BigNumberMatrix b = new BigNumberMatrix("1,2;3,4", Locale.US);

        assertEquals(a, b, "1.0/4.00 must equal 1/4 numerically");
        assertEquals(a.hashCode(), b.hashCode(), "equal matrices must hash equally");

        final Set<BigNumberMatrix> set = new HashSet<>();
        set.add(a);
        assertTrue(set.contains(b), "a value-equal matrix must be found in a HashSet");
    }

    @Test
    @DisplayName("matrices that differ in value or dimension are not equal")
    void inequality() {
        final BigNumberMatrix a = new BigNumberMatrix("1,2;3,4", Locale.US);
        final BigNumberMatrix differentValue = new BigNumberMatrix("1,2;3,5", Locale.US);
        final BigNumberMatrix differentShape = new BigNumberMatrix("1,2,3", Locale.US);

        assertNotEquals(a, differentValue);
        assertNotEquals(a, differentShape);
        assertNotEquals(a, null);
        assertNotEquals(a, "not a matrix");
    }
}
