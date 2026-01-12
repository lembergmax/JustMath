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
     * <p>This implementation uses an iterative (bottom-up) merge sort to avoid recursion.
     * It is stable and runs in {@code O(n log n)} time with {@code O(n)} additional space.</p>
     *
     * @param bigNumbers the list of {@link BigNumber} values to sort
     * @return a new sorted list of {@link BigNumber} values in ascending order
     */
    @Override
    public List<BigNumber> sort(@NonNull final List<BigNumber> bigNumbers) {
        if (!isListValid(bigNumbers)) {
            return bigNumbers;
        }

        final List<BigNumber> working = cloneList(bigNumbers);
        final List<BigNumber> buffer = cloneList(working);

        final int size = working.size();

        List<BigNumber> source = working;
        List<BigNumber> destination = buffer;

        for (int runSize = 1; runSize > 0 && runSize < size; runSize <<= 1) {
            final int blockSize = (int) Math.min(size, (long) runSize * 2L);

            for (int leftFrom = 0; leftFrom < size; leftFrom += blockSize) {
                final int leftTo = Math.min(leftFrom + runSize, size);
                final int rightTo = Math.min(leftFrom + blockSize, size);

                if (leftTo >= rightTo) {
                    copyRange(source, destination, leftFrom, rightTo);
                } else {
                    merge(source, destination, leftFrom, leftTo, rightTo);
                }
            }

            final List<BigNumber> temp = source;
            source = destination;
            destination = temp;
        }

        return source;
    }

    /**
     * Merges two sorted ranges from {@code source} into {@code destination}:
     * {@code [leftFrom, leftTo)} and {@code [leftTo, rightTo)}.
     *
     * <p>The merged result is written into {@code destination[leftFrom..rightTo)}.
     * The operation is stable: equal elements keep their original relative order.</p>
     *
     * @param source      source list (read-only for this operation)
     * @param destination destination list (write target)
     * @param leftFrom    start of left range (inclusive)
     * @param leftTo      end of left range / start of right range (exclusive)
     * @param rightTo     end of right range (exclusive)
     */
    private void merge(@NonNull final List<BigNumber> source, @NonNull final List<BigNumber> destination, final int leftFrom, final int leftTo, final int rightTo) {
        int leftIndex = leftFrom;
        int rightIndex = leftTo;
        int writeIndex = leftFrom;

        while (leftIndex < leftTo && rightIndex < rightTo) {
            final BigNumber leftValue = source.get(leftIndex);
            final BigNumber rightValue = source.get(rightIndex);

            if (leftValue.isLessThanOrEqualTo(rightValue)) {
                destination.set(writeIndex++, leftValue);
                leftIndex++;
            } else {
                destination.set(writeIndex++, rightValue);
                rightIndex++;
            }
        }

        while (leftIndex < leftTo) {
            destination.set(writeIndex++, source.get(leftIndex++));
        }

        while (rightIndex < rightTo) {
            destination.set(writeIndex++, source.get(rightIndex++));
        }
    }

    /**
     * Copies {@code source[fromInclusive..toExclusive)} into {@code destination[fromInclusive..toExclusive)}.
     */
    private void copyRange(@NonNull final List<BigNumber> source, @NonNull final List<BigNumber> destination, final int fromInclusive, final int toExclusive) {
        for (int index = fromInclusive; index < toExclusive; index++) {
            destination.set(index, source.get(index));
        }
    }

}
