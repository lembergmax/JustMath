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
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Audit fix H5: {@link BigNumberList} implements {@link java.util.List}; the
 * {@code Object}-typed query methods ({@code contains}, {@code indexOf},
 * {@code lastIndexOf}, {@code remove(Object)}) must accept {@code null} and behave
 * per the {@code List} contract instead of throwing {@link NullPointerException}.
 */
class BigNumberListContractTest {

    @Test
    @DisplayName("contains(null) returns false for a list without null elements")
    void containsNullReturnsFalse() {
        final BigNumberList list = BigNumberList.of(new BigNumber("1"), new BigNumber("2"));
        assertFalse(list.contains(null));
    }

    @Test
    @DisplayName("indexOf(null) / lastIndexOf(null) return -1")
    void indexOfNullReturnsMinusOne() {
        final BigNumberList list = BigNumberList.of(new BigNumber("1"));
        assertEquals(-1, list.indexOf(null));
        assertEquals(-1, list.lastIndexOf(null));
    }

    @Test
    @DisplayName("remove(null) returns false and leaves the list unchanged")
    void removeNullReturnsFalse() {
        final BigNumberList list = BigNumberList.of(new BigNumber("1"));
        assertFalse(list.remove(null));
        assertEquals(1, list.size());
    }
}
