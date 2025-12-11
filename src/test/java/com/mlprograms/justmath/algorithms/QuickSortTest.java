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

package com.mlprograms.justmath.algorithms;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.algorithms.QuickSort;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuickSortTest {

    private List<BigNumber> createBigNumberList(String... values) {
        final List<BigNumber> numbers = new ArrayList<>(values.length);
        for (String value : values) {
            numbers.add(new BigNumber(value));
        }
        return numbers;
    }

    private List<String> toStringList(List<BigNumber> numbers) {
        final List<String> result = new ArrayList<>(numbers.size());
        for (BigNumber number : numbers) {
            result.add(number.toString());
        }
        return result;
    }

    private void assertSortedValues(List<BigNumber> sortedList, String... expectedValues) {
        assertEquals(Arrays.asList(expectedValues), toStringList(sortedList));
    }

    @Nested
    class SortBehavior {

        @Test
        void sortUnsortedListAscending() {
            List<BigNumber> input = createBigNumberList("5", "1", "4", "2", "3");
            List<BigNumber> sorted = new QuickSort().sort(input);
            assertSortedValues(sorted, "1", "2", "3", "4", "5");
        }

        @Test
        void sortListWithNegativeAndDecimalValues() {
            List<BigNumber> input = createBigNumberList("3.5", "-2", "0", "10", "-2.5");
            List<BigNumber> sorted = new QuickSort().sort(input);
            assertSortedValues(sorted, "-2.5", "-2", "0", "3.5", "10");
        }

        @Test
        void sortListWithDuplicateValues() {
            List<BigNumber> input = createBigNumberList("2", "3", "2", "1", "3");
            List<BigNumber> sorted = new QuickSort().sort(input);
            assertSortedValues(sorted, "1", "2", "2", "3", "3");
        }

        @ParameterizedTest
        @CsvSource({"5, 1, 3, 1, 3, 5", "10, -1, 0, -1, 0, 10", "2, 2, 1, 1, 2, 2"})
        void sortThreeElementLists(String first, String second, String third, String expectedFirst, String expectedSecond, String expectedThird) {
            List<BigNumber> input = createBigNumberList(first, second, third);
            List<BigNumber> sorted = new QuickSort().sort(input);
            assertSortedValues(sorted, expectedFirst, expectedSecond, expectedThird);
        }

        @Test
        void originalListIsNotModifiedForNonTrivialInput() {
            List<BigNumber> input = createBigNumberList("5", "1", "4", "2", "3");
            List<BigNumber> sorted = new QuickSort().sort(input);

            assertEquals(Arrays.asList("5", "1", "4", "2", "3"), toStringList(input));
            assertSortedValues(sorted, "1", "2", "3", "4", "5");
            assertNotSame(input, sorted);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void sortSingleElementListReturnsEquivalentList() {
            List<BigNumber> input = createBigNumberList("42");
            List<BigNumber> sorted = new QuickSort().sort(input);
            assertEquals(toStringList(input), toStringList(sorted));
        }

        @Test
        void sortEmptyListReturnsEmptyList() {
            List<BigNumber> input = new ArrayList<>();
            List<BigNumber> sorted = new QuickSort().sort(input);

            assertTrue(sorted.isEmpty());
            assertTrue(input.isEmpty());
        }

        @Test
        void sortNullListThrowsNullPointerException() {
            assertThrows(NullPointerException.class, () -> new QuickSort().sort(null));
        }
    }

}
