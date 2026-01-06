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
import com.mlprograms.justmath.bignumber.algorithms.BubbleSort;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class AbstractSortAlgorithmTest {

    @FunctionalInterface
    public interface BigNumberSortAlgorithm {
        List<BigNumber> sort(List<BigNumber> input);
    }

    protected abstract BigNumberSortAlgorithm createAlgorithm();

    protected Class<?> algorithmType() {
        return null;
    }

    protected final List<BigNumber> createBigNumberList(final String... values) {
        final List<BigNumber> numbers = new ArrayList<>(values.length);
        for (final String value : values) {
            numbers.add(new BigNumber(value));
        }
        return numbers;
    }

    protected final List<String> toStringList(final List<BigNumber> numbers) {
        final List<String> result = new ArrayList<>(numbers.size());
        for (final BigNumber number : numbers) {
            result.add(number.toString());
        }
        return result;
    }

    protected final void assertSortedValues(final List<BigNumber> sortedList, final String... expectedValues) {
        assertEquals(Arrays.asList(expectedValues), toStringList(sortedList));
    }

    private static void assertIsNonDecreasing(@SuppressWarnings("SameParameterValue") final List<BigNumber> list) {
        for (int i = 1; i < list.size(); i++) {
            final BigNumber prev = list.get(i - 1);
            final BigNumber cur = list.get(i);

            int finalI = i;
            assertTrue(prev.isLessThanOrEqualTo(cur), () -> "List not sorted at index " + (finalI - 1) + " -> " + finalI + " (" + prev + " > " + cur + ")");
        }
    }

    private static Map<String, Integer> multiset(final List<BigNumber> list) {
        final Map<String, Integer> counts = new HashMap<>();
        for (final BigNumber n : list) {
            final String key = n.toString();
            counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }

    @Nested
    class SortBehavior {

        @Test
        void sortUnsortedListAscending() {
            final List<BigNumber> input = createBigNumberList("5", "1", "4", "2", "3");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, "1", "2", "3", "4", "5");
        }

        @Test
        void sortListWithNegativeAndDecimalValues() {
            final List<BigNumber> input = createBigNumberList("3.5", "-2", "0", "10", "-2.5");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, "-2.5", "-2", "0", "3.5", "10");
        }

        @Test
        void sortListWithDuplicateValues() {
            final List<BigNumber> input = createBigNumberList("2", "3", "2", "1", "3");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, "1", "2", "2", "3", "3");
        }

        @ParameterizedTest
        @CsvSource({"5, 1, 3, 1, 3, 5", "10, -1, 0, -1, 0, 10", "2, 2, 1, 1, 2, 2"})
        void sortThreeElementLists(final String first, final String second, final String third, final String expectedFirst, final String expectedSecond, final String expectedThird) {
            final List<BigNumber> input = createBigNumberList(first, second, third);
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, expectedFirst, expectedSecond, expectedThird);
        }

        @Test
        void originalListIsNotModifiedForNonTrivialInput() {
            final List<BigNumber> input = createBigNumberList("5", "1", "4", "2", "3");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertEquals(Arrays.asList("5", "1", "4", "2", "3"), toStringList(input));
            assertSortedValues(sorted, "1", "2", "3", "4", "5");
            assertNotSame(input, sorted);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void sortSingleElementListReturnsEquivalentList() {
            final List<BigNumber> input = createBigNumberList("42");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertEquals(toStringList(input), toStringList(sorted));
        }

        @Test
        void sortTwoElementsAlreadySorted() {
            final List<BigNumber> input = createBigNumberList("1", "2");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, "1", "2");
            assertEquals(Arrays.asList("1", "2"), toStringList(input));
        }

        @Test
        void sortTwoElementsReversed() {
            final List<BigNumber> input = createBigNumberList("2", "1");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, "1", "2");
            assertEquals(Arrays.asList("2", "1"), toStringList(input));
        }

        @Test
        void sortAlreadySortedLargerListDoesNotBreak() {
            final List<BigNumber> input = createBigNumberList("1", "2", "3", "4", "5", "6");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, "1", "2", "3", "4", "5", "6");
            assertEquals(Arrays.asList("1", "2", "3", "4", "5", "6"), toStringList(input));
        }

        @Test
        void sortReverseSortedLargerList() {
            final List<BigNumber> input = createBigNumberList("6", "5", "4", "3", "2", "1");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, "1", "2", "3", "4", "5", "6");
        }

        @Test
        void sortListWithManyEqualElements() {
            final List<BigNumber> input = createBigNumberList("7", "7", "7", "7", "7");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, "7", "7", "7", "7", "7");
        }

        @Test
        void sortListWithZerosAndNegativeZerosIfSupported() {
            final List<BigNumber> input = createBigNumberList("0", "-0", "0", "-0");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertIsNonDecreasing(sorted);
            assertEquals(multiset(input), multiset(sorted));
        }

        @Test
        void sortListWithVeryLargeMagnitudes() {
            final List<BigNumber> input = createBigNumberList("999999999999999999999999999999999999999999", "-999999999999999999999999999999999999999999", "0", "1", "-1");

            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertSortedValues(sorted, "-999999999999999999999999999999999999999999", "-1", "0", "1", "999999999999999999999999999999999999999999");
        }

        @Test
        void sortListWithCloseDecimals() {
            final List<BigNumber> input = createBigNumberList("1.000", "1", "1.0", "0.9999", "1.0001");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertIsNonDecreasing(sorted);
            assertEquals(multiset(input), multiset(sorted));
        }

        @Test
        void algorithmMustNotReturnNull() {
            final List<BigNumber> input = createBigNumberList("2", "1");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertNotNull(sorted);
        }

        @Test
        void algorithmMustPreserveElementCountAndValues() {
            final List<BigNumber> input = createBigNumberList("3", "1", "2", "2", "-5", "10", "10", "0");
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertEquals(input.size(), sorted.size());
            assertEquals(multiset(input), multiset(sorted));
            assertIsNonDecreasing(sorted);
        }

        @Test
        void sortEmptyListReturnsEmptyList() {
            final List<BigNumber> input = new ArrayList<>();
            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertTrue(sorted.isEmpty());
            assertTrue(input.isEmpty());
        }

        @Test
        void sortNullListThrowsNullPointerException() {
            assertThrows(NullPointerException.class, () -> createAlgorithm().sort(null));
        }
    }

    @Nested
    class LargeDataSets {

        @Test
        @Timeout(5)
        void sortLargeRandomDataSet_fastAlgorithms_only_isSortedAndPreservesAllValues() {
            assumeTrue(algorithmType() != null, "THIS IS NO ERROR: Skipping: algorithmType() not provided by this test class");
            assumeFalse(BubbleSort.class.equals(algorithmType()), "THIS IS NO ERROR: Skipping huge dataset test for BubbleSort");

            final int size = 50_000;
            final long seed = 123456789L;

            final Random random = new Random(seed);
            final List<BigNumber> input = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                final int value = random.nextInt(200_001) - 100_000; // [-100000..100000]
                input.add(new BigNumber(Integer.toString(value)));
            }

            final Map<String, Integer> before = multiset(input);

            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertNotNull(sorted);
            assertNotSame(input, sorted, "Algorithm should return a new list (non-trivial input)");
            assertEquals(size, sorted.size());
            assertIsNonDecreasing(sorted);

            final Map<String, Integer> after = multiset(sorted);
            assertEquals(before, after, "Sorted list must contain the same multiset of values");
        }

        @Test
        @Timeout(5)
        void sortLargeRandomDataSet_bubbleSort_only_isSortedAndPreservesAllValues() {
            // Run ONLY for BubbleSort (smaller dataset)
            assumeTrue(BubbleSort.class.equals(algorithmType()), "THIS IS NO ERROR: Skipping: only relevant for BubbleSort");

            final int size = 1_000;
            final long seed = 123456789L;

            final Random random = new Random(seed);
            final List<BigNumber> input = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                final int value = random.nextInt(20_001) - 10_000;
                input.add(new BigNumber(Integer.toString(value)));
            }

            final Map<String, Integer> before = multiset(input);

            final List<BigNumber> sorted = createAlgorithm().sort(input);

            assertNotNull(sorted);
            assertNotSame(input, sorted, "Algorithm should return a new list (non-trivial input)");
            assertEquals(size, sorted.size());
            assertIsNonDecreasing(sorted);

            final Map<String, Integer> after = multiset(sorted);
            assertEquals(before, after, "Sorted list must contain the same multiset of values");
        }
    }

}
