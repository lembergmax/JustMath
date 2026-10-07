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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Audit fix K7: {@code randomIntegerBigNumberInRange} is documented as the half-open
 * interval {@code [min, max)}. The previous implementation added one to {@code max},
 * making it inclusive ({@code [min, max]}), and sampled with a biased {@code mod}.
 */
class BigNumbersRandomRangeTest {

    @Test
    @DisplayName("[0, 1) yields only 0 — the exclusive upper bound never appears")
    void degenerateRangeYieldsOnlyMin() {
        final BigNumber min = new BigNumber("0");
        final BigNumber max = new BigNumber("1");
        for (int i = 0; i < 2_000; i++) {
            assertEquals("0",
                    BigNumbers.randomIntegerBigNumberInRange(min, max, Locale.US).toString(),
                    "value must stay inside [0, 1)");
        }
    }

    @Test
    @DisplayName("[0, 2) yields {0, 1} but never the exclusive upper bound 2")
    void upperBoundExcluded() {
        final BigNumber min = new BigNumber("0");
        final BigNumber max = new BigNumber("2");
        final Set<String> seen = new HashSet<>();
        for (int i = 0; i < 5_000; i++) {
            final String value = BigNumbers.randomIntegerBigNumberInRange(min, max, Locale.US).toString();
            assertTrue(value.equals("0") || value.equals("1"),
                    "value must be 0 or 1, never 2; was " + value);
            seen.add(value);
        }
        assertTrue(seen.contains("0") && seen.contains("1"),
                "both 0 and 1 should occur across 5000 draws");
    }

    @Test
    @DisplayName("min >= max is rejected")
    void emptyRangeRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> BigNumbers.randomIntegerBigNumberInRange(new BigNumber("5"), new BigNumber("5"), Locale.US));
        assertThrows(IllegalArgumentException.class,
                () -> BigNumbers.randomIntegerBigNumberInRange(new BigNumber("6"), new BigNumber("5"), Locale.US));
    }

    @Test
    @DisplayName("non-integer bounds are rejected with IllegalArgumentException, not ArithmeticException")
    void nonIntegerBoundsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> BigNumbers.randomIntegerBigNumberInRange(new BigNumber("1.5"), new BigNumber("5"), Locale.US));
        assertThrows(IllegalArgumentException.class,
                () -> BigNumbers.randomIntegerBigNumberInRange(new BigNumber("1"), new BigNumber("5.5"), Locale.US));
    }
}
