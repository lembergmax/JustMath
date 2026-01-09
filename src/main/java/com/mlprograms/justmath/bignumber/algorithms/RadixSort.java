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
import com.mlprograms.justmath.bignumber.BigNumbers;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class RadixSort extends SortingAlgorithm {

    private static final int RADIX = 10;

    /**
     * Returns a new {@link List} of {@link BigNumber} instances sorted in ascending order.
     * <p>
     * The original list is not modified. If the input list is {@code null}, empty,
     * or contains fewer than two elements, the original list is returned.
     *
     * <p><b>Important:</b> This RadixSort implementation supports only <b>integer</b> values (scale == 0).
     * If any input value is not an integer, the method falls back to returning a sorted copy
     * using a stable comparison-based approach (MergeSort).</p>
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

        if (!areAllIntegers(sortedList)) {
            return new MergeSort().sort(sortedList);
        }

        radixSortIntegers(sortedList);
        return sortedList;
    }

    /**
     * Sorts the list in-place using LSD RadixSort (base 10) for integer values.
     *
     * <p>This implementation supports negative integers by separating values into
     * negative and non-negative lists. The negative part is sorted by absolute value
     * and then reversed (because more negative means smaller) before being combined
     * with the sorted non-negative part.</p>
     *
     * @param numbers list of integer {@link BigNumber} values to sort
     */
    private void radixSortIntegers(@NonNull final List<BigNumber> numbers) {
        final List<BigNumber> negatives = new ArrayList<>();
        final List<BigNumber> nonNegatives = new ArrayList<>();

        for (final BigNumber value : numbers) {
            if (isNegative(value)) {
                negatives.add(value);
            } else {
                nonNegatives.add(value);
            }
        }

        lsdRadixSortByAbsValue(nonNegatives);
        lsdRadixSortByAbsValue(negatives);

        numbers.clear();

        for (int i = negatives.size() - 1; i >= 0; i--) {
            numbers.add(negatives.get(i));
        }
        numbers.addAll(nonNegatives);
    }

    /**
     * Performs an LSD RadixSort (base 10) on the list using the absolute integer value.
     *
     * <p>The list is mutated in-place and the algorithm is stable for values with the same
     * absolute digits.</p>
     *
     * @param values integer values to sort by absolute value
     */
    private void lsdRadixSortByAbsValue(@NonNull final List<BigNumber> values) {
        if (values.size() < 2) {
            return;
        }

        final BigInteger maxAbs = maxAbs(values);
        BigInteger exp = BigInteger.ONE;

        while (maxAbs.compareTo(exp) >= 0) {
            countingSortByDigit(values, exp);
            exp = exp.multiply(BigInteger.TEN);
        }
    }

    /**
     * Stable counting sort step for one digit (base 10) at exponent {@code exp}.
     *
     * @param values list to sort in-place
     * @param exp    digit exponent (1, 10, 100, ...)
     */
    private void countingSortByDigit(@NonNull final List<BigNumber> values, @NonNull final BigInteger exp) {
        final int size = values.size();
        final int[] count = new int[RADIX];
        final List<BigNumber> output = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            output.add(null);
        }

        for (final BigNumber value : values) {
            final int digit = digitAtExp(value, exp);
            count[digit]++;
        }

        for (int i = 1; i < RADIX; i++) {
            count[i] += count[i - 1];
        }

        for (int i = size - 1; i >= 0; i--) {
            final BigNumber value = values.get(i);
            final int digit = digitAtExp(value, exp);
            final int writeIndex = --count[digit];
            output.set(writeIndex, value);
        }

        for (int i = 0; i < size; i++) {
            values.set(i, output.get(i));
        }
    }

    /**
     * Extracts the digit (0..9) of the absolute integer value at exponent {@code exp}.
     *
     * @param value integer {@link BigNumber}
     * @param exp   digit exponent (1, 10, 100, ...)
     * @return digit at the given exponent
     */
    private int digitAtExp(@NonNull final BigNumber value, @NonNull final BigInteger exp) {
        final BigInteger absInt = toBigIntegerAbs(value);
        return absInt.divide(exp).mod(BigInteger.TEN).intValue();
    }

    /**
     * Determines the maximum absolute integer value in the list.
     *
     * @param values list of integer {@link BigNumber} values
     * @return max absolute value as {@link BigInteger}
     */
    private BigInteger maxAbs(@NonNull final List<BigNumber> values) {
        BigInteger max = BigInteger.ZERO;

        for (final BigNumber value : values) {
            final BigInteger absValue = toBigIntegerAbs(value);
            if (absValue.compareTo(max) > 0) {
                max = absValue;
            }
        }

        return max;
    }

    /**
     * Checks whether all values are integers (no fractional part).
     *
     * @param values list to validate
     * @return {@code true} if all values represent integers
     */
    private boolean areAllIntegers(@NonNull final List<BigNumber> values) {
        for (final BigNumber value : values) {
            if (!value.isInteger()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the given {@link BigNumber} represents a negative value.
     *
     * @param value value to check
     * @return {@code true} if negative, otherwise {@code false}
     */
    private boolean isNegative(@NonNull final BigNumber value) {
        return value.isLessThan(BigNumbers.ZERO);
    }

    /**
     * Converts the given integer {@link BigNumber} to its absolute {@link BigInteger} value.
     *
     * @param value integer {@link BigNumber}
     * @return absolute integer value
     */
    private BigInteger toBigIntegerAbs(@NonNull final BigNumber value) {
        final BigDecimal decimal = new BigDecimal(value.toString().replace(',', '.'));
        return decimal.toBigIntegerExact().abs();
    }

}
