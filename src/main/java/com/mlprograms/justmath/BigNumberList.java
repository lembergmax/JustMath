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

package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.algorithms.SortingAlgorithm;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
public class BigNumberList implements Comparable<BigNumberList> {

    private final List<BigNumber> values;

    public BigNumberList(@NonNull final List<BigNumber> values) {
        this.values = values;
    }

    public BigNumberList(@NonNull final BigNumberList bigNumberList) {
        this.values = bigNumberList.values;
    }

    // TODO: add
    // TODO: addAll
    // TODO: remove(index)
    // TODO: remove(value)
    // TODO: get(index)
    // TODO: get(value)
    // TODO: getAll(value)
    // TODO: sort(Class extends Algorithm::sort)

    // TODO: sort(Class extends Algorithm::sort, ascending/descending)
    public BigNumberList sort(@NonNull final Class<? extends SortingAlgorithm> algorithmClass) {
        try {
            final SortingAlgorithm algorithm = algorithmClass.getDeclaredConstructor().newInstance();

            final List<BigNumber> sorted = algorithm.sort(values);
            if (sorted == null) {
                throw new IllegalStateException("Sorting algorithm returned null");
            }

            return new BigNumberList(sorted);
        } catch (ReflectiveOperationException illegalArgumentException) {
            throw new IllegalArgumentException("Unable to instantiate sorting algorithm: " + algorithmClass.getName(), illegalArgumentException);
        }
    }

    // TODO: reverse()
    // TODO: shuffle()
    // TODO:

    /**
     * Compares this {@code BigNumberList} with the specified {@code BigNumberList} for order.
     *
     * @param other the {@code BigNumberList} to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
     * specified object.
     */
    @Override
    public int compareTo(@NonNull final BigNumberList other) {
        // TODO
        // return toBigDecimal().compareTo(other.toBigDecimal());
        return 0;
    }

    /**
     * Creates and returns a copy of this BigNumberList.
     *
     * @return a new BigNumberList instance with the same value and properties as this one
     */
    public BigNumberList clone() {
        return new BigNumberList(this);
    }

}
