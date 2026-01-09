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

package com.mlprograms.justmath.bignumber.algorithms;

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@NoArgsConstructor
public class TimSort extends SortingAlgorithm {

    private static final int MIN_MERGE = 32;

    /**
     * Returns a new {@link List} of {@link BigNumber} instances sorted in ascending order.
     * <p>
     * The original list is not modified. If the input list is {@code null}, empty,
     * or contains fewer than two elements, the original list is returned.
     *
     * <p>
     * This is a TimSort-style hybrid:
     * <ul>
     *   <li>Splits the list into small runs (minimum run length derived from {@code MIN_MERGE})</li>
     *   <li>Sorts each run using insertion sort</li>
     *   <li>Merges runs iteratively (stable merge)</li>
     * </ul>
     * </p>
     *
     * <p>This implementation is stable: equal elements keep their original relative order.</p>
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
        final int size = sortedList.size();

        final List<BigNumber> buffer = cloneList(sortedList);

        final int minimumRun = computeMinimumRunLength(size);

        for (int runStart = 0; runStart < size; runStart += minimumRun) {
            final int runEndExclusive = Math.min(runStart + minimumRun, size);
            insertionSortRange(sortedList, runStart, runEndExclusive);
        }

        for (int runSize = minimumRun; runSize < size; runSize *= 2) {
            for (int leftStart = 0; leftStart < size; leftStart += 2 * runSize) {
                final int middle = Math.min(leftStart + runSize, size);
                final int rightEndExclusive = Math.min(leftStart + 2 * runSize, size);

                if (middle >= rightEndExclusive) {
                    continue; // only one run present
                }

                merge(sortedList, buffer, leftStart, middle, rightEndExclusive);
            }
        }

        return sortedList;
    }

    /**
     * Computes the minimum run length used by TimSort.
     * <p>
     * For TimSort, the minimum run length is chosen so that the number of runs is close to a power of two,
     * improving merge balance. This method mirrors the classic TimSort minRun calculation.
     *
     * @param size the input size
     * @return the minimum run length (>= 1)
     */
    private int computeMinimumRunLength(final int size) {
        int value = size;
        int remainder = 0;

        while (value >= MIN_MERGE) {
            remainder |= (value & 1);
            value >>= 1;
        }

        return value + remainder;
    }

    /**
     * Sorts the half-open range {@code [fromInclusive, toExclusive)} using insertion sort.
     * This is stable when shifting elements and inserting the key.
     *
     * @param list          list to sort in-place
     * @param fromInclusive inclusive start index
     * @param toExclusive   exclusive end index
     */
    private void insertionSortRange(@NonNull final List<BigNumber> list, final int fromInclusive, final int toExclusive) {
        for (int index = fromInclusive + 1; index < toExclusive; index++) {
            final BigNumber key = list.get(index);

            int insertIndex = index - 1;
            while (insertIndex >= fromInclusive && !list.get(insertIndex).isLessThanOrEqualTo(key)) {
                list.set(insertIndex + 1, list.get(insertIndex));
                insertIndex--;
            }

            list.set(insertIndex + 1, key);
        }
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
