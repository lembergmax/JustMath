/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package io.github.lembergmax.justmath.bignumber.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import io.github.lembergmax.justmath.bignumber.BigNumber;

public class RadixSortTest extends AbstractSortAlgorithmTest {

    @Override
    protected BigNumberSortAlgorithm createAlgorithm() {
        return new RadixSort()::sort;
    }

    @Override
    protected Class<?> algorithmType() {
        return RadixSort.class;
    }

    @Test
    void radixSortStableForEqualNegatives() {
        // "-5.0" and "-5.00" are numerically equal but distinguishable by their stored fractional
        // part ("0" vs "00"). A stable sort must keep their input order; a plain reverse of the
        // ascending-by-absolute-value pass would flip them.
        BigNumber firstFive = new BigNumber("-5.0", Locale.US);
        BigNumber secondFive = new BigNumber("-5.00", Locale.US);
        List<BigNumber> input = List.of(
                firstFive,
                new BigNumber("-3", Locale.US),
                secondFive,
                new BigNumber("-7", Locale.US));

        List<BigNumber> sorted = new RadixSort().sort(input);

        assertEquals("[-7, -5, -5, -3]", sorted.toString(), "result must be ascending by value");

        List<String> fiveRepresentations = new ArrayList<>();
        for (BigNumber value : sorted) {
            if (value.isEqualTo(new BigNumber("-5", Locale.US))) {
                fiveRepresentations.add(value.getValueAfterDecimalPoint());
            }
        }
        assertEquals(List.of("0", "00"), fiveRepresentations,
                "equal negatives must retain their input order (stability)");
    }

}