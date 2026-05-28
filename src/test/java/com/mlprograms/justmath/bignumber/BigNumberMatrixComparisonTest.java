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

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Audit fix K6 (subset): {@code isSymmetric} and {@code equalsMatrix} now scan with int
 * indices and exit on the first mismatch. This guards that the rewrite preserves correctness.
 */
class BigNumberMatrixComparisonTest {

    @Test
    @DisplayName("isSymmetric distinguishes symmetric, asymmetric and non-square matrices")
    void isSymmetric() {
        assertTrue(new BigNumberMatrix("1,2;2,1", Locale.US).isSymmetric());
        assertTrue(new BigNumberMatrix("1,2,3;2,5,6;3,6,9", Locale.US).isSymmetric());
        assertFalse(new BigNumberMatrix("1,2;3,1", Locale.US).isSymmetric());
        assertFalse(new BigNumberMatrix("1,2,3;4,5,6", Locale.US).isSymmetric());
        // First mismatch is the very first off-diagonal pair — exercises the early exit.
        assertFalse(new BigNumberMatrix("1,9;0,1", Locale.US).isSymmetric());
    }

    @Test
    @DisplayName("equalsMatrix compares dimensions and every cell")
    void equalsMatrix() {
        final BigNumberMatrix base = new BigNumberMatrix("1,2;3,4", Locale.US);
        assertTrue(base.equalsMatrix(new BigNumberMatrix("1,2;3,4", Locale.US)));
        // Numerically equal but differently scaled entries still compare equal.
        assertTrue(base.equalsMatrix(new BigNumberMatrix("1.0,2;3,4.00", Locale.US)));
        assertFalse(base.equalsMatrix(new BigNumberMatrix("1,2;3,5", Locale.US)));
        assertFalse(base.equalsMatrix(new BigNumberMatrix("9,2;3,4", Locale.US)));
        assertFalse(base.equalsMatrix(new BigNumberMatrix("1,2,3;4,5,6", Locale.US)));
    }
}
