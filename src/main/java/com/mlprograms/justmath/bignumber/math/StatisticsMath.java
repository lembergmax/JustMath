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

package com.mlprograms.justmath.bignumber.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;

import java.math.MathContext;
import java.util.List;
import java.util.Locale;

import com.mlprograms.justmath.bignumber.algorithms.QuickSort;
import com.mlprograms.justmath.bignumber.math.exceptions.InsufficientElementsException;
import lombok.NonNull;

public class StatisticsMath {

    /**
     * Calculates the arithmetic mean (average) of the provided list of {@link BigNumber} values.
     *
     * <p>The method first computes the total sum of all elements by delegating to {@link #sum(List, Locale)}
     * and then divides that sum by the number of elements using the supplied {@link MathContext} to control
     * precision and rounding behaviour.</p>
     *
     * <p>Important notes:</p>
     * <ul>
     *   <li>The {@code locale} parameter is forwarded to {@link BigNumber} operations and may influence
     *       parsing/formatting rules used internally by those operations.</li>
     *   <li>If {@code numbers} is empty, this method will attempt to divide by zero; the exact outcome
     *       depends on the implementation of {@link BigNumber#divide(BigNumber, MathContext, Locale)}
     *       and may result in an exception.</li>
     * </ul>
     *
     * @param numbers     the list of values to average; must not be {@code null}
     * @param mathContext controls precision and rounding for the division; must not be {@code null}
     * @param locale      locale used for BigNumber operations; must not be {@code null}
     * @return the arithmetic mean of the input values as a {@link BigNumber}
     */
    public static BigNumber average(@NonNull final List<BigNumber> numbers, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        return sum(numbers, locale).divide(new BigNumber(numbers.size()), mathContext, locale);
    }

    /**
     * Computes the sum of all {@link BigNumber} instances in the provided list.
     *
     * <p>The accumulation starts from {@link BigNumbers#ZERO} and each element is added using
     * {@link BigNumber#add(BigNumber, Locale)}. The supplied {@code locale} is passed to each
     * addition operation and can affect locale-sensitive behaviour in {@link BigNumber}.</p>
     *
     * @param numbers the list of values to sum; must not be {@code null}
     * @param locale  locale used for BigNumber operations; must not be {@code null}
     * @return the total sum of the list, or {@link BigNumbers#ZERO} if the list is empty
     */
    public static BigNumber sum(@NonNull final List<BigNumber> numbers, @NonNull final Locale locale) {
        BigNumber sum = BigNumbers.ZERO;
        for (BigNumber number : numbers) {
            sum = sum.add(number, locale);
        }

        return sum;
    }

    /**
     * Calculates the median of the provided list of {@link BigNumber} values.
     *
     * <p>Behavior:
     * <ul>
     *   <li>If {@code numbers} is empty, an {@link InsufficientElementsException} is thrown.</li>
     *   <li>The input list is sorted using {@link QuickSort#sort(List)} to determine the middle element(s).</li>
     *   <li>If the number of elements is odd, the exact middle element is returned.</li>
     *   <li>If the number of elements is even, the median is defined as the arithmetic mean of the two
     *       middle elements. That mean is computed by adding the two middle {@link BigNumber} values and
     *       dividing the sum by {@link BigNumbers#TWO} using the provided {@link MathContext} and
     *       {@link Locale} to control precision, rounding and any locale-sensitive behavior.</li>
     * </ul>
     *
     * <p>Note: The time complexity is dominated by the sorting step. The method delegates locale-aware
     * operations to {@link BigNumber} methods and forwards the supplied {@code locale} to those calls.
     *
     * @param numbers     the list of values to compute the median for; must not be {@code null}
     * @param mathContext controls precision and rounding when averaging two middle elements; must not be {@code null}
     * @param locale      locale passed to {@link BigNumber} operations; must not be {@code null}
     * @return the median as a {@link BigNumber}
     * @throws InsufficientElementsException if {@code numbers} is empty
     */
    public static BigNumber median(@NonNull final List<BigNumber> numbers, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        if (numbers.isEmpty()) {
            throw new InsufficientElementsException();
        }

        final List<BigNumber> sortedNumbers = QuickSort.sort(numbers);
        final int size = sortedNumbers.size();
        final int middleIndex = size / 2;

        if ((size & 1) == 1) {
            return sortedNumbers.get(middleIndex);
        }

        final BigNumber lowerMiddle = sortedNumbers.get(middleIndex - 1);
        final BigNumber upperMiddle = sortedNumbers.get(middleIndex);
        final BigNumber sumOfMiddles = lowerMiddle.add(upperMiddle, locale);

        return sumOfMiddles.divide(BigNumbers.TWO, mathContext, locale);
    }

}
