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
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Audit fix H6: {@link BigNumberList#reverse()} reverses in place in O(n) and returns the
 * same instance. The previous implementation was O(n^2) (prepending to an ArrayList).
 */
class BigNumberListReverseTest {

    @Test
    @DisplayName("reverse() reverses element order in place and returns this")
    void reversesInPlace() {
        final BigNumberList list = BigNumberList.of(
                new BigNumber("1"), new BigNumber("2"), new BigNumber("3"));
        final BigNumberList result = list.reverse();
        assertSame(list, result, "reverse() returns the same instance");
        assertEquals("[3, 2, 1]", list.toString());
    }

    @Test
    @DisplayName("reverse() twice restores the original order")
    void doubleReverseIsIdentity() {
        final BigNumberList list = BigNumberList.of(
                new BigNumber("1"), new BigNumber("2"), new BigNumber("3"), new BigNumber("4"));
        list.reverse().reverse();
        assertEquals("[1, 2, 3, 4]", list.toString());
    }

    @Test
    @DisplayName("reverse() handles empty and single-element lists")
    void edgeCases() {
        assertEquals("[]", new BigNumberList().reverse().toString());
        assertEquals("[7]", BigNumberList.of(new BigNumber("7")).reverse().toString());
    }
}
