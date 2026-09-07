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

import java.math.BigInteger;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests proving combinations and permutations are computed exactly (no rounding to the default
 * 100-digit division precision) and without materializing the full {@code n!}.
 */
class CombinatoricsExactnessTest {

    @Test
    @DisplayName("C(450, 225) is an exact ~134-digit integer (not rounded to 100 digits) and obeys Pascal's rule")
    void largeBinomialIsExact() {
        final BigNumber c = new BigNumber("450", Locale.US).combination(new BigNumber("225", Locale.US));

        assertTrue(c.isInteger(), "a binomial coefficient must be an exact integer, was " + c);
        final BigInteger value = c.toBigDecimal().toBigIntegerExact();
        assertTrue(value.toString().length() > 130,
                "C(450,225) has ~134 digits; a 100-digit rounding would have truncated it, got " + value.toString().length() + " digits");

        // Pascal's rule: C(450,225) = C(449,224) + C(449,225). Holds only if every value is exact.
        final BigNumber a = new BigNumber("449", Locale.US).combination(new BigNumber("224", Locale.US));
        final BigNumber b = new BigNumber("449", Locale.US).combination(new BigNumber("225", Locale.US));
        assertEquals(a.add(b).toBigDecimal().toBigIntegerExact(), value, "Pascal's rule must hold exactly");
    }

    @Test
    @DisplayName("P(100000, 5) is exact and computed from the bounded product, not the full 100000!")
    void largePermutationIsBoundedAndExact() {
        final BigNumber p = new BigNumber("100000", Locale.US).permutation(new BigNumber("5", Locale.US));

        BigInteger expected = BigInteger.ONE;
        for (int i = 0; i < 5; i++) {
            expected = expected.multiply(BigInteger.valueOf(100000L - i));
        }
        assertEquals(expected, p.toBigDecimal().toBigIntegerExact(),
                "P(100000,5) must equal 100000·99999·99998·99997·99996 exactly");
    }

    @Test
    @Timeout(20)
    @DisplayName("P(n, k) / C(n, k) with a huge term count are rejected before the product loop runs")
    void hugeCombinatoricsAreRejected() {
        final BigNumber huge = new BigNumber("999999999", Locale.US);

        assertThrows(ArithmeticException.class, () -> huge.permutation(huge),
                "P(1e9, 1e9) would multiply ~1e9 factors and must be rejected");
        assertThrows(ArithmeticException.class, () -> huge.combination(new BigNumber("400000000", Locale.US)),
                "C(1e9, 4e8) would multiply ~4e8 factors and must be rejected");
    }

    @Test
    @Timeout(20)
    @DisplayName("P(n, k) / C(n, k) with a small k but astronomically large n are rejected on projected size")
    void largeNWithSmallKIsRejectedOnSize() {
        final BigNumber astronomicalN = new BigNumber("1" + "0".repeat(2000), Locale.US);

        assertThrows(ArithmeticException.class, () -> astronomicalN.permutation(new BigNumber("100000", Locale.US)),
                "P(1e2000, 1e5) has ~2e8 digits and must be rejected even though the loop count is modest");
    }
}
