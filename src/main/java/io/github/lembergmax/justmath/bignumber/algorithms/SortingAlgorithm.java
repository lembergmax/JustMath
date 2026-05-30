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

import java.util.ArrayList;
import java.util.List;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import lombok.NonNull;

public abstract class SortingAlgorithm {

    public abstract List<BigNumber> sort(@NonNull final List<BigNumber> bigNumbers);

    /**
     * Create a shallow copy of the given list of BigNumber instances.
     *
     * <p>The returned list is a new {@link ArrayList} containing the same elements
     * (references) as the provided list. Modifications to the returned list (add/remove)
     * will not affect the original list, but modifications to the elements themselves
     * will be visible from both lists.</p>
     *
     * @param list non-null list of {@link BigNumber} to clone
     * @return a new {@link ArrayList} containing the same elements as {@code list}
     */
    protected static List<BigNumber> cloneList(@NonNull final List<BigNumber> list) {
        return new ArrayList<>(list);
    }

    /**
     * Validates whether the given list contains enough elements to be worth sorting.
     *
     * <p>The previous body checked for {@code null} as well, but {@link #sort(List)} declares
     * {@link NonNull @NonNull} on its parameter and Lombok therefore throws a
     * {@link NullPointerException} before this method is ever entered. Keeping the redundant
     * null guard masked that contract and produced dead code.</p>
     *
     * @param bigNumbers the non-null list to inspect
     * @return {@code true} when the list has at least two elements; {@code false} otherwise
     */
    protected static boolean isListValid(@NonNull final List<BigNumber> bigNumbers) {
        return bigNumbers.size() >= 2;
    }

    /**
     * Swaps the elements at {@code firstIndex} and {@code secondIndex} in the provided list.
     *
     * <p>Used by the concrete sort implementations to avoid duplicating the same three-line
     * swap helper in every subclass. Does nothing when both indices are equal; index-bounds
     * violations propagate from the underlying {@link List#set(int, Object)} call.</p>
     *
     * @param numbers     the list whose elements should be swapped; must not be {@code null}
     * @param firstIndex  index of the first element
     * @param secondIndex index of the second element
     */
    protected static void swap(@NonNull final List<BigNumber> numbers, final int firstIndex, final int secondIndex) {
        if (firstIndex == secondIndex) {
            return;
        }
        final BigNumber temporary = numbers.get(firstIndex);
        numbers.set(firstIndex, numbers.get(secondIndex));
        numbers.set(secondIndex, temporary);
    }

    /**
     * Throws a {@link CancellationException} when the current thread has been interrupted.
     *
     * <p>Long-running sorts (BubbleSort and friends on tens of thousands of {@link BigNumber}s)
     * are CPU bound and otherwise ignore JUnit {@code @Timeout} interrupts and external
     * cancellation requests. Calling this guard inside each pass of the outer loop lets the
     * caller terminate the algorithm promptly when the thread is asked to stop.</p>
     *
     * @throws java.util.concurrent.CancellationException when {@link Thread#interrupted()} reports
     *                                                    {@code true}; the thread's interrupt status
     *                                                    is cleared by this check
     */
    protected static void abortIfInterrupted() {
        if (Thread.interrupted()) {
            throw new java.util.concurrent.CancellationException("Sorting was interrupted before completion");
        }
    }

}
