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

package io.github.lembergmax.justmath.bignumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import io.github.lembergmax.justmath.bignumber.algorithms.MergeSort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@code BigNumberList.sort()} picks a sorting algorithm by the size of the list and by whether every element is an
 * integer. Whichever algorithm it picks, the result is the sorted list, and the list itself is returned.
 */
class BigNumberListSortSelectionTest {

    private static final long SEED = 20_261_006L;

    private static final int VALUE_RANGE = 100_000;

    private static List<BigNumber> integers(final int size) {
        final Random random = new Random(SEED + size);
        final List<BigNumber> numbers = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            numbers.add(BigNumber.valueOf(random.nextInt(2 * VALUE_RANGE) - VALUE_RANGE));
        }
        return numbers;
    }

    private static List<BigNumber> decimals(final int size) {
        final Random random = new Random(SEED - size);
        final List<BigNumber> numbers = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            final int hundredths = random.nextInt(2 * VALUE_RANGE) - VALUE_RANGE;
            numbers.add(new BigNumber(BigDecimal.valueOf(hundredths, 2).toPlainString()));
        }
        return numbers;
    }

    private static List<BigNumber> sortedCopy(final List<BigNumber> numbers) {
        final List<BigNumber> sorted = new ArrayList<>(numbers);
        sorted.sort(BigNumber::compareTo);
        return sorted;
    }

    static Stream<Arguments> sizesAndKinds() {
        return Stream.of(0, 1, 2, 32, 33, 999, 1_000, 1_001, 2_500).flatMap(size -> Stream.of(
                Arguments.of(size, "integers", integers(size)),
                Arguments.of(size, "decimals", decimals(size))));
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("sizesAndKinds")
    @DisplayName("sort() sorts the list for every size and returns it")
    void sortSortsEveryKindOfList(final int size, final String kind, final List<BigNumber> numbers) {
        final BigNumberList list = new BigNumberList(new ArrayList<>(numbers));

        assertSame(list, list.sort());
        assertEquals(sortedCopy(numbers), list, size + " " + kind);
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("sizesAndKinds")
    @DisplayName("sortAscending() and sortDescending() sort the list and return it")
    void sortAscendingAndDescendingReturnTheList(final int size, final String kind, final List<BigNumber> numbers) {
        final BigNumberList list = new BigNumberList(new ArrayList<>(numbers));
        final List<BigNumber> ascending = sortedCopy(numbers);
        final List<BigNumber> descending = new ArrayList<>(ascending).reversed();

        assertSame(list, list.sortAscending());
        assertEquals(ascending, list, size + " " + kind);
        assertSame(list, list.sortDescending());
        assertEquals(descending, list, size + " " + kind);
    }

    @Test
    @DisplayName("sort(Class) sorts with the given algorithm and returns the list")
    void sortWithAnAlgorithmReturnsTheList() {
        final List<BigNumber> numbers = integers(50);
        final BigNumberList list = new BigNumberList(new ArrayList<>(numbers));

        assertSame(list, list.sort(MergeSort.class));
        assertEquals(sortedCopy(numbers), list);
    }
}
