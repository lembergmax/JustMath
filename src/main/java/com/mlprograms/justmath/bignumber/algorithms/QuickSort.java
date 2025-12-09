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
import lombok.NonNull;

import java.util.List;

public class QuickSort extends Algorithm {

    /**
     * Returns a new {@link List} of {@link BigNumber} instances sorted in ascending order.
     * <p>
     * The original list is not modified. If the input list is {@code null}, empty,
     * or contains fewer than two elements, the original list is returned.
     *
     * @param bigNumbers the list of {@link BigNumber} values to sort
     * @return a new sorted list of {@link BigNumber} values in ascending order
     */
    public static List<BigNumber> sort(@NonNull final List<BigNumber> bigNumbers) {
        if (!isListValid(bigNumbers)) {
            return bigNumbers;
        }

        final List<BigNumber> sortedList = cloneList(bigNumbers);
        quickSort(sortedList, 0, sortedList.size() - 1);
        return sortedList;
    }

    private static void quickSort(@NonNull final List<BigNumber> numbers, final int leftIndex, final int rightIndex) {
        if (leftIndex >= rightIndex) {
            return;
        }

        final int pivotIndex = partition(numbers, leftIndex, rightIndex);
        quickSort(numbers, leftIndex, pivotIndex - 1);
        quickSort(numbers, pivotIndex + 1, rightIndex);
    }

    private static int partition(@NonNull final List<BigNumber> numbers, final int leftIndex, final int rightIndex) {
        final BigNumber pivot = numbers.get(rightIndex);
        int partitionIndex = leftIndex - 1;

        for (int currentIndex = leftIndex; currentIndex < rightIndex; currentIndex++) {
            final BigNumber currentValue = numbers.get(currentIndex);
            if (currentValue.isLessThanOrEqualTo(pivot)) {
                partitionIndex++;
                swap(numbers, partitionIndex, currentIndex);
            }
        }

        final int pivotFinalIndex = partitionIndex + 1;
        swap(numbers, pivotFinalIndex, rightIndex);
        return pivotFinalIndex;
    }

    private static void swap(@NonNull final List<BigNumber> numbers, final int firstIndex, final int secondIndex) {
        if (firstIndex == secondIndex) {
            return;
        }

        final BigNumber temp = numbers.get(firstIndex);
        numbers.set(firstIndex, numbers.get(secondIndex));
        numbers.set(secondIndex, temp);
    }

}
