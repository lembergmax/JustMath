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

import java.util.ArrayList;
import java.util.List;

public class Algorithm {

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
     * Validate a list of {@link BigNumber} for algorithmic operations.
     *
     * <p>Validity rules:
     * <ul>
     *   <li>the list must not be {@code null}</li>
     *   <li>the list must not be empty</li>
     *   <li>the list must contain more than one element</li>
     * </ul>
     * </p>
     *
     * @param bigNumbers the list to validate
     * @return {@code true} if the list meets the validity rules, {@code false} otherwise
     */
    protected static boolean isListValid(final List<BigNumber> bigNumbers) {
        if (bigNumbers == null) {
            return false;
        }

        if (bigNumbers.isEmpty()) {
            return false;
        }

        if (bigNumbers.size() == 1) {
            return false;
        }

        return true;
    }

}
