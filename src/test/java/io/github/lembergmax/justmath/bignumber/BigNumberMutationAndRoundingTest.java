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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;

/**
 * Audit fixes K1 (no hidden mutation) and H10 (correct floor/ceil semantics).
 *
 * <p>{@code abs}, {@code floor}, {@code ceil} and {@code truncate} are documented to
 * return a new value; none may mutate the receiver. {@code floor}/{@code ceil} must use
 * mathematically correct rounding direction for negative numbers (the previous
 * {@code floor} merely truncated toward zero).</p>
 */
class BigNumberMutationAndRoundingTest {

    @Test
    @DisplayName("abs() does not mutate the receiver")
    void absDoesNotMutate() {
        final BigNumber negative = new BigNumber("-5.25");
        final BigNumber result = negative.abs();
        assertEquals("-5.25", negative.toString(), "receiver must be unchanged");
        assertEquals("5.25", result.toString());
    }

    @Test
    @DisplayName("floor() / ceil() / truncate() do not mutate the receiver")
    void roundingDoesNotMutate() {
        final BigNumber value = new BigNumber("-3.7");
        value.floor();
        value.ceil();
        value.truncate();
        assertEquals("-3.7", value.toString(), "receiver must be unchanged after non-mutating ops");
    }

    @Test
    @DisplayName("floor() rounds toward negative infinity")
    void floorSemantics() {
        assertEquals("3", new BigNumber("3.7").floor().toString());
        assertEquals("3", new BigNumber("3.0").floor().toString());
        assertEquals("-4", new BigNumber("-3.7").floor().toString());
        assertEquals("-3", new BigNumber("-3").floor().toString());
        assertEquals("-1", new BigNumber("-0.5").floor().toString());
        assertEquals("0", new BigNumber("0.5").floor().toString());
    }

    @Test
    @DisplayName("ceil() rounds toward positive infinity")
    void ceilSemantics() {
        assertEquals("4", new BigNumber("3.2").floor().add(new BigNumber("1")).toString());
        assertEquals("4", new BigNumber("3.2").ceil().toString());
        assertEquals("-2", new BigNumber("-2.7").ceil().toString());
        assertEquals("5", new BigNumber("5.0").ceil().toString());
        assertEquals("0", new BigNumber("-0.5").ceil().toString());
    }

    @Test
    @DisplayName("truncate() rounds toward zero and never returns a corrupted shared zero")
    void truncateSemantics() {
        assertEquals("3", new BigNumber("3.7").truncate().toString());
        assertEquals("-3", new BigNumber("-3.7").truncate().toString());
        assertEquals("0", new BigNumber("-0.5").truncate().toString());
        // Mutating a truncate()-to-zero result must not corrupt the global ZERO constant.
        new BigNumber("-0.5").truncate().negateThis();
        assertEquals("0", BigNumbers.ZERO.toString(), "BigNumbers.ZERO must stay 0");
    }

    @Test
    @DisplayName("atan of a negative argument keeps its sign (regression: mutable abs)")
    void atanNegativeKeepsSign() {
        // |x| <= 1 series branch
        assertEquals("-45", new BigNumber("-1").atan(TrigonometricMode.DEG).roundAfterDecimals(0).toString());
        // |x| > 1 reciprocal branch
        assertEquals("-1", new BigNumber("-1000000").atan(TrigonometricMode.DEG).signum() + "");
    }
}
