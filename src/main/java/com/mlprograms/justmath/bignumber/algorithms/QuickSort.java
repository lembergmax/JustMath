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

/**
 * Sorts {@link BigNumber} values using an iterative (non-recursive) QuickSort implementation.
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li><b>Returns a new list:</b> The input list is never modified. Sorting is performed on a cloned copy.</li>
 *   <li><b>Ascending order:</b> Elements are ordered from smallest to largest.</li>
 *   <li><b>Not stable:</b> QuickSort does not guarantee that equal elements preserve their original relative order.</li>
 * </ul>
 *
 * <h2>Implementation details</h2>
 * <p>
 * This version avoids recursion completely by using an explicit stack of index ranges.
 * To keep the stack small, it always <i>pushes the larger partition</i> onto the stack and continues
 * sorting the smaller partition in a loop (classic tail-recursion elimination). This guarantees
 * an {@code O(log n)} stack depth in practice and in the typical worst-case scenarios for partitioning.
 * </p>
 *
 * <p>
 * For better performance on partially sorted input, the algorithm uses:
 * </p>
 * <ul>
 *   <li><b>Median-of-three pivot selection</b> (left, middle, right)</li>
 *   <li><b>Insertion sort cutoff</b> for small ranges (reduces overhead and improves locality)</li>
 * </ul>
 *
 * <h2>Complexity</h2>
 * <ul>
 *   <li>Average time: {@code O(n log n)}</li>
 *   <li>Worst-case time: {@code O(n^2)} (can still occur, but is less likely with median-of-three)</li>
 *   <li>Extra memory: {@code O(log n)} for the explicit stack (plus the cloned list)</li>
 * </ul>
 */
@NoArgsConstructor
public class QuickSort extends SortingAlgorithm {

    /**
     * If a range length is at most this threshold, insertion sort is used instead of further partitioning.
     * <p>
     * This typically speeds up QuickSort because insertion sort has low constant overhead and is fast
     * on small arrays due to cache friendliness.
     * </p>
     */
    private static final int INSERTION_SORT_THRESHOLD = 16;

    /**
     * Returns a new {@link List} of {@link BigNumber} instances sorted in ascending order.
     * <p>
     * The original list is not modified. If the input list is {@code null}, empty,
     * or contains fewer than two elements, the original list is returned.
     * </p>
     *
     * @param bigNumbers the list of {@link BigNumber} values to sort (must not be {@code null})
     * @return a new sorted list of {@link BigNumber} values in ascending order
     */
    @Override
    public List<BigNumber> sort(@NonNull final List<BigNumber> bigNumbers) {
        isListValid(bigNumbers);

        final List<BigNumber> sortedList = cloneList(bigNumbers);
        sortInPlace(sortedList);
        return sortedList;
    }

    /**
     * Sorts the given list in-place using an iterative QuickSort.
     *
     * <p>
     * This method assumes the list is valid for sorting (size at least 2).
     * It uses an explicit stack to store ranges that still need processing.
     * </p>
     *
     * @param numbers the list to sort in-place
     */
    private void sortInPlace(@NonNull final List<BigNumber> numbers) {
        final int lastIndex = numbers.size() - 1;
        if (lastIndex <= 0) {
            return;
        }

        final int[] leftBounds = new int[requiredStackCapacity(numbers.size())];
        final int[] rightBounds = new int[leftBounds.length];

        int stackSize = 0;
        stackSize = pushRange(leftBounds, rightBounds, stackSize, 0, lastIndex);

        while (stackSize > 0) {
            stackSize--;

            int leftIndex = leftBounds[stackSize];
            int rightIndex = rightBounds[stackSize];

            while (rangeLength(leftIndex, rightIndex) > INSERTION_SORT_THRESHOLD) {
                final int pivotIndex = partitionWithMedianOfThreePivot(numbers, leftIndex, rightIndex);

                final int leftStart = leftIndex;
                final int leftEnd = pivotIndex - 1;

                final int rightStart = pivotIndex + 1;
                final int rightEnd = rightIndex;

                if (rangeLength(leftStart, leftEnd) <= rangeLength(rightStart, rightEnd)) {
                    if (isNonTrivialRange(rightStart, rightEnd)) {
                        stackSize = pushRange(leftBounds, rightBounds, stackSize, rightStart, rightEnd);
                    }
                    rightIndex = leftEnd; // continue with smaller left partition
                } else {
                    if (isNonTrivialRange(leftStart, leftEnd)) {
                        stackSize = pushRange(leftBounds, rightBounds, stackSize, leftStart, leftEnd);
                    }
                    leftIndex = rightStart; // continue with smaller right partition
                }
            }

            insertionSort(numbers, leftIndex, rightIndex);
        }
    }

    /**
     * Partitions the inclusive range {@code [leftIndex, rightIndex]} using a Lomuto-style partition scheme.
     *
     * <p>
     * A pivot is chosen using median-of-three (left, middle, right), moved to {@code rightIndex},
     * and then the range is partitioned so that:
     * </p>
     * <ul>
     *   <li>All elements {@code <= pivot} end up left of the returned pivot index</li>
     *   <li>All elements {@code > pivot} end up right of the returned pivot index</li>
     * </ul>
     *
     * @param numbers    the list containing the range to partition
     * @param leftIndex  left bound of the range (inclusive)
     * @param rightIndex right bound of the range (inclusive)
     * @return the final index of the pivot after partitioning
     */
    private int partitionWithMedianOfThreePivot(@NonNull final List<BigNumber> numbers, final int leftIndex, final int rightIndex) {
        final int pivotIndex = chooseMedianOfThreePivotIndex(numbers, leftIndex, rightIndex);
        swap(numbers, pivotIndex, rightIndex);

        final BigNumber pivot = numbers.get(rightIndex);
        int boundaryIndex = leftIndex - 1;

        for (int currentIndex = leftIndex; currentIndex < rightIndex; currentIndex++) {
            final BigNumber currentValue = numbers.get(currentIndex);
            if (currentValue.isLessThanOrEqualTo(pivot)) {
                boundaryIndex++;
                swap(numbers, boundaryIndex, currentIndex);
            }
        }

        final int pivotFinalIndex = boundaryIndex + 1;
        swap(numbers, pivotFinalIndex, rightIndex);
        return pivotFinalIndex;
    }

    /**
     * Chooses a pivot index using median-of-three selection.
     *
     * <p>
     * This method compares the values at {@code leftIndex}, {@code middleIndex} and {@code rightIndex}
     * and returns the index whose value is the median (i.e., neither minimum nor maximum among the three).
     * </p>
     *
     * <p>
     * Median-of-three reduces the probability of hitting worst-case partitioning on nearly sorted data
     * compared to always choosing the left/right element as pivot.
     * </p>
     *
     * @param numbers    the list containing the candidate pivot values
     * @param leftIndex  left bound index (inclusive)
     * @param rightIndex right bound index (inclusive)
     * @return index of the median-of-three pivot candidate
     */
    private int chooseMedianOfThreePivotIndex(@NonNull final List<BigNumber> numbers, final int leftIndex, final int rightIndex) {
        final int middleIndex = leftIndex + ((rightIndex - leftIndex) >>> 1);

        final BigNumber leftValue = numbers.get(leftIndex);
        final BigNumber middleValue = numbers.get(middleIndex);
        final BigNumber rightValue = numbers.get(rightIndex);

        final boolean leftLessOrEqualMiddle = leftValue.isLessThanOrEqualTo(middleValue);
        final boolean middleLessOrEqualRight = middleValue.isLessThanOrEqualTo(rightValue);
        final boolean leftLessOrEqualRight = leftValue.isLessThanOrEqualTo(rightValue);

        if (leftLessOrEqualMiddle) {
            if (middleLessOrEqualRight) {
                return middleIndex; // left <= middle <= right
            }
            return leftLessOrEqualRight ? rightIndex : leftIndex; // left <= right < middle OR right < left <= middle
        } else {
            if (!middleLessOrEqualRight) {
                return middleIndex; // right < middle < left
            }
            return leftLessOrEqualRight ? leftIndex : rightIndex; // middle <= left <= right OR middle <= right < left
        }
    }

    /**
     * Performs insertion sort on the inclusive range {@code [leftIndex, rightIndex]}.
     *
     * <p>
     * Insertion sort is efficient for small ranges and mostly sorted data.
     * This implementation shifts elements to the right until the correct insertion position is found.
     * </p>
     *
     * @param numbers    the list to sort
     * @param leftIndex  left bound of the range (inclusive)
     * @param rightIndex right bound of the range (inclusive)
     */
    private void insertionSort(@NonNull final List<BigNumber> numbers, final int leftIndex, final int rightIndex) {
        if (!isNonTrivialRange(leftIndex, rightIndex)) {
            return;
        }

        for (int index = leftIndex + 1; index <= rightIndex; index++) {
            final BigNumber valueToInsert = numbers.get(index);

            int shiftIndex = index - 1;
            while (shiftIndex >= leftIndex && numbers.get(shiftIndex).isGreaterThan(valueToInsert)) {
                numbers.set(shiftIndex + 1, numbers.get(shiftIndex));
                shiftIndex--;
            }

            numbers.set(shiftIndex + 1, valueToInsert);
        }
    }

    /**
     * Pushes a new inclusive range onto the explicit stack.
     *
     * @param leftBounds  stack for left indices
     * @param rightBounds stack for right indices
     * @param stackSize   current stack size (next write position)
     * @param leftIndex   left bound (inclusive)
     * @param rightIndex  right bound (inclusive)
     * @return the new stack size after pushing
     */
    private int pushRange(@NonNull final int[] leftBounds, @NonNull final int[] rightBounds, final int stackSize, final int leftIndex, final int rightIndex) {
        leftBounds[stackSize] = leftIndex;
        rightBounds[stackSize] = rightIndex;
        return stackSize + 1;
    }

    /**
     * Returns {@code true} if the given inclusive range contains at least two elements.
     *
     * @param leftIndex  left bound (inclusive)
     * @param rightIndex right bound (inclusive)
     * @return {@code true} if the range has two or more elements; otherwise {@code false}
     */
    private boolean isNonTrivialRange(final int leftIndex, final int rightIndex) {
        return leftIndex < rightIndex;
    }

    /**
     * Computes the length of an inclusive range.
     *
     * @param leftIndex  left bound (inclusive)
     * @param rightIndex right bound (inclusive)
     * @return the number of elements in the range; returns {@code 0} if {@code leftIndex > rightIndex}
     */
    private int rangeLength(final int leftIndex, final int rightIndex) {
        if (leftIndex > rightIndex) {
            return 0;
        }
        return rightIndex - leftIndex + 1;
    }

    /**
     * Calculates a safe capacity for the explicit stack.
     *
     * <p>
     * Because this implementation always continues with the smaller partition and pushes the larger one,
     * the number of simultaneously stored ranges is bounded and typically logarithmic. A capacity of
     * {@code 2 * (log2(n) + 2)} is sufficient for all practical inputs.
     * </p>
     *
     * @param size number of elements in the list
     * @return the stack capacity (number of ranges) to allocate
     */
    private int requiredStackCapacity(final int size) {
        final int safeSize = Math.max(2, size);
        final int log2 = 32 - Integer.numberOfLeadingZeros(safeSize);
        final int rangesCapacity = (log2 + 2) * 2;

        return Math.max(16, rangesCapacity);
    }

    /**
     * Swaps the elements at {@code firstIndex} and {@code secondIndex} in the provided list.
     *
     * <p>
     * This method does nothing if the two indices are equal. If an index is out of range,
     * the underlying {@link List} implementation will throw an {@link IndexOutOfBoundsException}.
     * </p>
     *
     * @param numbers     the list in which to swap elements
     * @param firstIndex  index of the first element to swap
     * @param secondIndex index of the second element to swap
     */
    private static void swap(@NonNull final List<BigNumber> numbers, final int firstIndex, final int secondIndex) {
        if (firstIndex == secondIndex) {
            return;
        }

        final BigNumber temp = numbers.get(firstIndex);
        numbers.set(firstIndex, numbers.get(secondIndex));
        numbers.set(secondIndex, temp);
    }

}
