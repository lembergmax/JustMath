/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.bignumber.algorithms;

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@NoArgsConstructor
public class BubbleSort extends SortingAlgorithm {

    /**
     * Returns a new {@link List} of {@link BigNumber} instances sorted in ascending order.
     * <p>
     * The original list is not modified. If the input list is {@code null}, empty,
     * or contains fewer than two elements, the original list is returned.
     *
     * @param bigNumbers the list of {@link BigNumber} values to sort
     * @return a new sorted list of {@link BigNumber} values in ascending order
     */
    @Override
    public List<BigNumber> sort(@NonNull final List<BigNumber> bigNumbers) {
        if (!isListValid(bigNumbers)) {
            return bigNumbers;
        }

        final List<BigNumber> sortedList = cloneList(bigNumbers);
        bubbleSort(sortedList);
        return sortedList;
    }

    /**
     * Sorts the provided list in-place using the BubbleSort algorithm.
     *
     * <p>The algorithm repeatedly steps through the list, compares adjacent elements,
     * and swaps them if they are in the wrong order. After each pass, the largest
     * remaining element is guaranteed to be at its final position. The implementation
     * uses an early-exit optimization: if no swaps occur in a pass, the list is already sorted.</p>
     *
     * @param numbers the list to sort in-place
     */
    private void bubbleSort(@NonNull final List<BigNumber> numbers) {
        final int size = numbers.size();

        for (int pass = 0; pass < size - 1; pass++) {
            // BubbleSort is O(n^2). On the inputs the algorithm is actually used for (≤ 32
            // elements per {@link BigNumberList#sort()}'s adaptive cutoff) this is fine, but
            // tests deliberately throw larger payloads at it; checking interruption per pass
            // makes the algorithm responsive to {@code @Timeout} and other cancellation signals.
            abortIfInterrupted();
            boolean swapped = false;

            for (int index = 0; index < size - 1 - pass; index++) {
                final BigNumber left = numbers.get(index);
                final BigNumber right = numbers.get(index + 1);

                if (right.isLessThan(left)) {
                    swap(numbers, index, index + 1);
                    swapped = true;
                }
            }

            if (!swapped) {
                return;
            }
        }
    }

}
