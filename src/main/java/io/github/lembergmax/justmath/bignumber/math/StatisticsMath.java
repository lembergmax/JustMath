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

package io.github.lembergmax.justmath.bignumber.math;

import java.math.MathContext;
import java.util.List;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.bignumber.algorithms.QuickSort;
import io.github.lembergmax.justmath.bignumber.math.exceptions.InsufficientElementsException;
import lombok.NonNull;

public final class StatisticsMath {

    /** Non-instantiable utility class. */
    private StatisticsMath() {
    }


    /**
     * Calculates the arithmetic mean (average) of the provided list of {@link BigNumber} values.
     *
     * <p>The method first computes the total sum of all elements by delegating to {@link #sum(List, Locale)}
     * and then divides that sum by the number of elements using the supplied {@link MathContext} to control
     * precision and rounding behaviour.</p>
     *
     * @param numbers     the list of values to average; must not be {@code null} and must not be empty
     * @param mathContext controls precision and rounding for the division; must not be {@code null}
     * @param locale      locale used for BigNumber operations; must not be {@code null}
     * @return the arithmetic mean of the input values as a {@link BigNumber}
     * @throws InsufficientElementsException if {@code numbers} is empty — the previous implementation
     *                                       silently triggered a {@code 0/0} {@link ArithmeticException}
     *                                       at the call site; this guard surfaces the structural problem
     *                                       (no data to average) up front with the same exception type
     *                                       that {@link #median(List, MathContext, Locale)} already uses
     */
    public static BigNumber average(@NonNull final List<BigNumber> numbers, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        if (numbers.isEmpty()) {
            throw new InsufficientElementsException();
        }
        return sum(numbers, locale).divide(BigNumber.valueOf(numbers.size()), mathContext, locale);
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

        return new BigNumber(sum);
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

        final List<BigNumber> sortedNumbers = new QuickSort().sort(numbers);
        final int size = sortedNumbers.size();
        final int middleIndex = size / 2;

        if ((size & 1) == 1) {
            return sortedNumbers.get(middleIndex);
        }

        final BigNumber lowerMiddle = sortedNumbers.get(middleIndex - 1);
        final BigNumber upperMiddle = sortedNumbers.get(middleIndex);
        final BigNumber sumOfMiddles = lowerMiddle.add(upperMiddle, locale);

        return new BigNumber(sumOfMiddles.divide(BigNumbers.TWO, mathContext, locale));
    }

}
