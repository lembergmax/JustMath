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
public class MergeSort extends SortingAlgorithm {

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
        final List<BigNumber> buffer = cloneList(sortedList);
        mergeSort(sortedList, buffer, 0, sortedList.size());
        return sortedList;
    }

    /**
     * Recursively sorts the half-open range {@code [fromInclusive, toExclusive)} using MergeSort.
     *
     * <p>This implementation uses a single reusable buffer list to avoid repeated allocations.
     * It sorts in-place in {@code target} while using {@code buffer} as temporary storage.</p>
     *
     * @param target        list to sort in-place
     * @param buffer        temporary storage (must be at least {@code target.size()})
     * @param fromInclusive inclusive start index
     * @param toExclusive   exclusive end index
     */
    private void mergeSort(@NonNull final List<BigNumber> target, @NonNull final List<BigNumber> buffer, final int fromInclusive, final int toExclusive) {
        final int length = toExclusive - fromInclusive;
        if (length < 2) {
            return;
        }

        final int middle = fromInclusive + (length / 2);

        mergeSort(target, buffer, fromInclusive, middle);
        mergeSort(target, buffer, middle, toExclusive);

        merge(target, buffer, fromInclusive, middle, toExclusive);
    }

    /**
     * Merges two sorted ranges from {@code target} into a single sorted range:
     * {@code [leftFrom, leftTo)} and {@code [leftTo, rightTo)}.
     *
     * <p>The merged result is written back into {@code target[leftFrom..rightTo)}.
     * The operation is stable: equal elements keep their original relative order.</p>
     *
     * @param target   source and destination list
     * @param buffer   temporary buffer (same size as target)
     * @param leftFrom start of left range (inclusive)
     * @param leftTo   end of left range / start of right range (exclusive)
     * @param rightTo  end of right range (exclusive)
     */
    private void merge(@NonNull final List<BigNumber> target, @NonNull final List<BigNumber> buffer, final int leftFrom, final int leftTo, final int rightTo) {
        for (int index = leftFrom; index < rightTo; index++) {
            buffer.set(index, target.get(index));
        }

        int leftIndex = leftFrom;
        int rightIndex = leftTo;
        int writeIndex = leftFrom;

        while (leftIndex < leftTo && rightIndex < rightTo) {
            final BigNumber leftValue = buffer.get(leftIndex);
            final BigNumber rightValue = buffer.get(rightIndex);

            if (leftValue.isLessThanOrEqualTo(rightValue)) {
                target.set(writeIndex++, leftValue);
                leftIndex++;
            } else {
                target.set(writeIndex++, rightValue);
                rightIndex++;
            }
        }

        while (leftIndex < leftTo) {
            target.set(writeIndex++, buffer.get(leftIndex++));
        }

        while (rightIndex < rightTo) {
            target.set(writeIndex++, buffer.get(rightIndex++));
        }
    }

}
