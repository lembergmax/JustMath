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

package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.bignumber.algorithms.QuickSort;
import com.mlprograms.justmath.bignumber.algorithms.SortingAlgorithm;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class BigNumberListTest {

    private static BigNumber getNewBigNumber(String value) {
        return new BigNumber(value);
    }

    private static BigNumberList listOf(String... values) {
        List<BigNumber> numbers = new ArrayList<>(values.length);
        for (String value : values) {
            numbers.add(getNewBigNumber(value));
        }
        return new BigNumberList(numbers);
    }

    private static void assertBigNumberEquals(BigNumber expected, BigNumber actual) {
        assertEquals(expected.toString(), actual.toString(), () ->
                "Expected BigNumber <" + expected + "> but was <" + actual + ">");
    }

    @Nested
    class ConstructorAndFactoryTests {

        @Test
        void ofCreatesListWithElements() {
            BigNumberList list = BigNumberList.of(getNewBigNumber("1"), getNewBigNumber("2"), getNewBigNumber("3"));

            assertEquals(3, list.size());
            assertBigNumberEquals(getNewBigNumber("1"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("2"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("3"), list.get(2));
        }

        @Test
        void fromStringsParsesValues() {
            List<String> values = List.of("10", "-2", "3.5");
            BigNumberList list = BigNumberList.fromStrings(values);

            assertEquals(3, list.size());
            assertBigNumberEquals(getNewBigNumber("10"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("-2"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("3.5"), list.get(2));
        }

        @Test
        void copyCreatesIndependentStorage() {
            BigNumberList original = listOf("1", "2", "3");
            BigNumberList copy = original.copy();

            assertEquals(original.size(), copy.size());
            assertBigNumberEquals(original.get(0), copy.get(0));

            original.add(getNewBigNumber("4"));
            assertEquals(3, copy.size(), "Copy must not change when original is structurally modified");
        }

        @Test
        void cloneSharesInternalStorage() {
            BigNumberList original = listOf("1", "2");
            BigNumberList clone = original.clone();

            original.add(getNewBigNumber("3"));

            assertEquals(3, clone.size(), "Clone shares internal list with original");
        }
    }

    @Nested
    class SortingTests {

        @Test
        void sortWithAlgorithmClass() {
            BigNumberList list = listOf("3", "1", "2");

            list.sort(QuickSort.class);

            assertEquals(3, list.size());
            assertBigNumberEquals(getNewBigNumber("1"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("2"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("3"), list.get(2));
        }

        @Test
        void sortThrowsOnNullResult() {
            BigNumberList list = listOf("3", "1", "2");

            assertThrows(IllegalStateException.class,
                    () -> list.sort(NullReturningAlgorithm.class),
                    "Sorting should fail when algorithm returns null");
        }

        @Test
        void sortThrowsWhenNoDefaultConstructor() {
            BigNumberList list = listOf("1", "2");

            assertThrows(IllegalArgumentException.class,
                    () -> list.sort(NoDefaultConstructorAlgorithm.class),
                    "Sorting should fail for algorithms without default constructor");
        }

        @Test
        void sortAscendingSortsNaturally() {
            BigNumberList list = listOf("5", "1", "3");

            list.sortAscending();

            assertBigNumberEquals(getNewBigNumber("1"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("3"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("5"), list.get(2));
        }

        @Test
        void sortDescendingSortsReversed() {
            BigNumberList list = listOf("5", "1", "3");

            list.sortDescending();

            assertBigNumberEquals(getNewBigNumber("5"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("3"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("1"), list.get(2));
        }

        public static class NullReturningAlgorithm extends SortingAlgorithm {
            @Override
            public List<BigNumber> sort(List<BigNumber> bigNumbers) {
                return null;
            }
        }

        public static class NoDefaultConstructorAlgorithm extends SortingAlgorithm {
            public NoDefaultConstructorAlgorithm(String unused) {
                // no-op
            }

            @Override
            public List<BigNumber> sort(List<BigNumber> bigNumbers) {
                return bigNumbers;
            }
        }
    }

    @Nested
    class StatisticsTests {

        @Test
        void sumComputesCorrectResult() {
            BigNumberList list = listOf("1", "2", "3", "4");

            BigNumber sum = list.sum();

            assertBigNumberEquals(getNewBigNumber("10"), sum);
        }

        @Test
        void sumOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::sum);
        }

        @Test
        void averageComputesCorrectResult() {
            BigNumberList list = listOf("2", "4", "6", "8");

            BigNumber avg = list.average();

            assertBigNumberEquals(getNewBigNumber("5"), avg);
        }

        @Test
        void averageOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::average);
        }

        @Test
        void medianOddCount() {
            BigNumberList list = listOf("3", "1", "2");

            BigNumber median = list.median();

            assertBigNumberEquals(getNewBigNumber("2"), median);
        }

        @Test
        void medianEvenCount() {
            BigNumberList list = listOf("1", "2", "4", "3"); // sorted → 1,2,3,4

            BigNumber median = list.median();

            assertBigNumberEquals(getNewBigNumber("2.5"), median);
        }

        @Test
        void medianOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::median);
        }

        @Test
        void modesReturnMostFrequentValues() {
            BigNumberList list = listOf("1", "2", "2", "3", "3");

            Set<BigNumber> modes = list.modes();

            assertEquals(2, modes.size());
            assertTrue(modes.contains(getNewBigNumber("2")));
            assertTrue(modes.contains(getNewBigNumber("3")));
        }

        @Test
        void modesOnEmptyListReturnsEmptySet() {
            BigNumberList list = listOf();

            Set<BigNumber> modes = list.modes();

            assertTrue(modes.isEmpty());
        }

        @Test
        void minReturnsSmallestValue() {
            BigNumberList list = listOf("5", "1", "3");

            BigNumber min = list.min();

            assertBigNumberEquals(getNewBigNumber("1"), min);
        }

        @Test
        void minOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::min);
        }

        @Test
        void maxReturnsLargestValue() {
            BigNumberList list = listOf("5", "1", "3");

            BigNumber max = list.max();

            assertBigNumberEquals(getNewBigNumber("5"), max);
        }

        @Test
        void maxOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::max);
        }

        @Test
        void rangeComputesDifference() {
            BigNumberList list = listOf("10", "2", "8");

            BigNumber range = list.range();

            assertBigNumberEquals(getNewBigNumber("8"), range); // 10 - 2
        }

        @Test
        void varianceComputesCorrectResult() {
            BigNumberList list = listOf("1", "3"); // mean = 2, deviations: 1,1 → variance = 1

            BigNumber variance = list.variance();

            assertBigNumberEquals(getNewBigNumber("1"), variance);
        }

        @Test
        void varianceRequiresAtLeastTwoElements() {
            BigNumberList single = listOf("1");
            BigNumberList empty = listOf();

            assertThrows(IllegalStateException.class, single::variance);
            assertThrows(IllegalStateException.class, empty::variance);
        }

        @Test
        void standardDeviationIsSqrtOfVariance() {
            BigNumberList list = listOf("1", "3"); // variance = 1, std dev = 1

            BigNumber variance = list.variance();
            BigNumber stddev = list.standardDeviation();

            assertBigNumberEquals(variance, stddev.multiply(stddev));
        }

        @Test
        void geometricMeanComputesCorrectValue() {
            BigNumberList list = listOf("4", "1"); // product=4, n=2 → sqrt(4)=2

            BigNumber geoMean = list.geometricMean();

            assertBigNumberEquals(getNewBigNumber("2"), geoMean);
        }

        @Test
        void geometricMeanOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::geometricMean);
        }

        @Test
        void geometricMeanThrowsOnNegative() {
            BigNumberList list = listOf("4", "-1");

            assertThrows(IllegalStateException.class, list::geometricMean);
        }

        @Test
        void harmonicMeanComputesCorrectValue() {
            BigNumberList list = listOf("1", "1"); // N=2, 1/(1)+1/(1)=2 → 2/2=1

            BigNumber harmonicMean = list.harmonicMean();

            assertBigNumberEquals(getNewBigNumber("1"), harmonicMean);
        }

        @Test
        void harmonicMeanOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::harmonicMean);
        }

        @Test
        void harmonicMeanThrowsOnZero() {
            BigNumberList list = listOf("1", "0");

            assertThrows(ArithmeticException.class, list::harmonicMean);
        }
    }

    @Nested
    class TransformationTests {

        @Test
        void absAllAppliesAbsoluteValue() {
            BigNumberList list = listOf("-1", "2", "-3");

            list.absAll();

            assertBigNumberEquals(getNewBigNumber("1"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("2"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("3"), list.get(2));
        }

        @Test
        void negateAllNegatesValues() {
            BigNumberList list = listOf("1", "-2", "3");

            list.negateAll();

            assertBigNumberEquals(getNewBigNumber("-1"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("2"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("-3"), list.get(2));
        }

        @Test
        void scaleMultipliesEachElement() {
            BigNumberList list = listOf("1", "2", "3");

            list.scale(getNewBigNumber("10"));

            assertBigNumberEquals(getNewBigNumber("10"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("20"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("30"), list.get(2));
        }

        @Test
        void translateAddsOffset() {
            BigNumberList list = listOf("1", "2", "3");

            list.translate(getNewBigNumber("5"));

            assertBigNumberEquals(getNewBigNumber("6"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("7"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("8"), list.get(2));
        }

        @Test
        void powEachRaisesToPower() {
            BigNumberList list = listOf("2", "3");

            list.powEach(getNewBigNumber("3")); // 2^3=8, 3^3=27

            assertBigNumberEquals(getNewBigNumber("8"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("27"), list.get(1));
        }

        @Test
        void clampAllClampsToRange() {
            BigNumberList list = listOf("-1", "5", "10");

            list.clampAll(getNewBigNumber("0"), getNewBigNumber("9"));

            assertBigNumberEquals(getNewBigNumber("0"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("5"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("9"), list.get(2));
        }

        @Test
        void clampAllThrowsWhenMaxLessThanMin() {
            BigNumberList list = listOf("1", "2");

            assertThrows(IllegalArgumentException.class,
                    () -> list.clampAll(getNewBigNumber("5"), getNewBigNumber("1")));
        }

        @Test
        void normalizeToSumScalesListToTargetSum() {
            BigNumberList list = listOf("1", "1", "2"); // sum=4

            list.normalizeToSum(getNewBigNumber("8"));

            BigNumber newSum = list.sum();
            assertTrue(Math.abs(newSum.doubleValue() - 8.0) < 1e-9,
                    "Normalized sum should be approximately 8");
        }

        @Test
        void normalizeToSumThrowsWhenSumIsZero() {
            BigNumberList list = listOf("1", "-1"); // sum=0

            assertThrows(IllegalStateException.class,
                    () -> list.normalizeToSum(getNewBigNumber("5")));
        }

        @Test
        void mapCreatesTransformedCopy() {
            BigNumberList list = listOf("1", "2", "3");

            UnaryOperator<BigNumber> op = value -> value.multiply(getNewBigNumber("2"));
            BigNumberList doubled = list.map(op);

            assertEquals(3, doubled.size());
            assertBigNumberEquals(getNewBigNumber("2"), doubled.get(0));
            assertBigNumberEquals(getNewBigNumber("4"), doubled.get(1));
            assertBigNumberEquals(getNewBigNumber("6"), doubled.get(2));

            // original must remain unchanged
            assertBigNumberEquals(getNewBigNumber("1"), list.get(0));
        }

        @Test
        void reverseReversesList() {
            BigNumberList list = listOf("1", "2", "3");

            list.reverse();

            assertBigNumberEquals(getNewBigNumber("3"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("2"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("1"), list.get(2));
        }
    }

    @Nested
    class StructuralTests {

        @Test
        void immutableCopyReturnsUnmodifiableList() {
            BigNumberList list = listOf("1", "2");

            List<BigNumber> copy = list.immutableCopy();

            assertEquals(2, copy.size());
            assertThrows(UnsupportedOperationException.class,
                    () -> copy.add(getNewBigNumber("3")));
        }

        @Test
        void shufflePermutesList() {
            BigNumberList list = listOf("1", "2", "3", "4");
            BigNumberList originalCopy = list.copy();

            list.shuffle(new Random(12345));

            // same elements, possibly different order
            assertEquals(originalCopy.size(), list.size());
            assertTrue(list.containsAll(originalCopy));
        }

        @Test
        void rotateRotatesValues() {
            BigNumberList list = listOf("1", "2", "3", "4");

            list.rotate(1);

            assertBigNumberEquals(getNewBigNumber("4"), list.get(0));
            assertBigNumberEquals(getNewBigNumber("1"), list.get(1));
            assertBigNumberEquals(getNewBigNumber("2"), list.get(2));
            assertBigNumberEquals(getNewBigNumber("3"), list.get(3));
        }

        @Test
        void distinctPreservesOrderAndRemovesDuplicates() {
            BigNumberList list = listOf("1", "2", "2", "3", "1");

            BigNumberList distinct = list.distinct();

            assertEquals(3, distinct.size());
            assertBigNumberEquals(getNewBigNumber("1"), distinct.get(0));
            assertBigNumberEquals(getNewBigNumber("2"), distinct.get(1));
            assertBigNumberEquals(getNewBigNumber("3"), distinct.get(2));
        }

        @Test
        void appendConcatenatesLists() {
            BigNumberList list1 = listOf("1", "2");
            BigNumberList list2 = listOf("3", "4");

            BigNumberList appended = list1.append(list2);

            assertEquals(4, appended.size());
            assertBigNumberEquals(getNewBigNumber("1"), appended.get(0));
            assertBigNumberEquals(getNewBigNumber("2"), appended.get(1));
            assertBigNumberEquals(getNewBigNumber("3"), appended.get(2));
            assertBigNumberEquals(getNewBigNumber("4"), appended.get(3));
        }

        @Test
        void subListCopyCreatesIndependentCopy() {
            BigNumberList list = listOf("1", "2", "3", "4");

            BigNumberList sub = list.subListCopy(1, 3); // "2","3"

            assertEquals(2, sub.size());
            assertBigNumberEquals(getNewBigNumber("2"), sub.get(0));
            assertBigNumberEquals(getNewBigNumber("3"), sub.get(1));

            list.set(1, getNewBigNumber("99"));
            assertBigNumberEquals(getNewBigNumber("2"), sub.get(0));
        }

        @Test
        void anyMatchReturnsTrueWhenConditionHolds() {
            BigNumberList list = listOf("1", "2", "3");

            Predicate<BigNumber> isEven = value -> value.modulo(getNewBigNumber("2")).compareTo(getNewBigNumber("0")) == 0;

            assertTrue(list.anyMatch(isEven));
        }

        @Test
        void anyMatchReturnsFalseWhenNoElementMatches() {
            BigNumberList list = listOf("1", "3", "5");

            Predicate<BigNumber> isEven = value -> value.modulo(getNewBigNumber("2")).compareTo(getNewBigNumber("0")) == 0;

            assertFalse(list.anyMatch(isEven));
        }

        @Test
        void allMatchReturnsTrueWhenAllMatch() {
            BigNumberList list = listOf("2", "4", "6");

            Predicate<BigNumber> isEven = value -> value.modulo(getNewBigNumber("2")).compareTo(getNewBigNumber("0")) == 0;

            assertTrue(list.allMatch(isEven));
        }

        @Test
        void allMatchReturnsFalseWhenAnyFails() {
            BigNumberList list = listOf("2", "3", "4");

            Predicate<BigNumber> isEven = value -> value.modulo(getNewBigNumber("2")).compareTo(getNewBigNumber("0")) == 0;

            assertFalse(list.allMatch(isEven));
        }

        @Test
        void allMatchOnEmptyListReturnsTrue() {
            BigNumberList list = listOf();

            Predicate<BigNumber> any = value -> true;

            assertTrue(list.allMatch(any));
        }

        @Test
        void findFirstReturnsFirstMatch() {
            BigNumberList list = listOf("1", "4", "3", "4");

            Predicate<BigNumber> equalsFour = value -> value.compareTo(getNewBigNumber("4")) == 0;

            Optional<BigNumber> first = list.findFirst(equalsFour);

            assertTrue(first.isPresent());
            assertBigNumberEquals(getNewBigNumber("4"), first.get());
        }

        @Test
        void findFirstReturnsEmptyWhenNoMatchFound() {
            BigNumberList list = listOf("1", "2", "3");

            Predicate<BigNumber> equalsTen = value -> value.compareTo(getNewBigNumber("10")) == 0;

            Optional<BigNumber> first = list.findFirst(equalsTen);

            assertTrue(first.isEmpty());
        }

        @Test
        void filterReturnsMatchingElements() {
            BigNumberList list = listOf("1", "2", "3", "4");

            Predicate<BigNumber> isEven = value -> value.modulo(getNewBigNumber("2")).compareTo(getNewBigNumber("0")) == 0;
            BigNumberList evens = list.filter(isEven);

            assertEquals(2, evens.size());
            assertBigNumberEquals(getNewBigNumber("2"), evens.get(0));
            assertBigNumberEquals(getNewBigNumber("4"), evens.get(1));

            // original list must stay unchanged
            assertEquals(4, list.size());
        }

        @Test
        void isSortedAscendingDetectsOrder() {
            BigNumberList sorted = listOf("1", "2", "3");
            BigNumberList unsorted = listOf("2", "1", "3");

            assertTrue(sorted.isSortedAscending());
            assertFalse(unsorted.isSortedAscending());
        }

        @Test
        void isSortedDescendingDetectsOrder() {
            BigNumberList sorted = listOf("3", "2", "1");
            BigNumberList unsorted = listOf("2", "3", "1");

            assertTrue(sorted.isSortedDescending());
            assertFalse(unsorted.isSortedDescending());
        }

        @Test
        void isMonotonicIncreasingAllowsEqualNeighbours() {
            BigNumberList list = listOf("1", "2", "2", "3");

            assertTrue(list.isMonotonicIncreasing());
        }

        @Test
        void isMonotonicDecreasingAllowsEqualNeighbours() {
            BigNumberList list = listOf("3", "3", "2", "1");

            assertTrue(list.isMonotonicDecreasing());
        }
    }

    @Nested
    class ConversionTests {

        @Test
        void toUnmodifiableListReturnsSnapshotCopy() {
            BigNumberList list = listOf("1", "2");

            List<BigNumber> snapshot = list.toUnmodifiableList();

            assertEquals(2, snapshot.size());

            list.add(getNewBigNumber("3"));

            assertEquals(2, snapshot.size(),
                    "Snapshot should not change when original list changes");
        }

        @Test
        void toBigNumberArrayContainsAllElements() {
            BigNumberList list = listOf("1", "2", "3");

            BigNumber[] array = list.toBigNumberArray();

            assertEquals(3, array.length);
            assertBigNumberEquals(getNewBigNumber("1"), array[0]);
            assertBigNumberEquals(getNewBigNumber("2"), array[1]);
            assertBigNumberEquals(getNewBigNumber("3"), array[2]);
        }

        @Test
        void toStringListContainsStringRepresentations() {
            BigNumberList list = listOf("1", "2.5", "-3");

            List<String> strings = list.toStringList();

            assertEquals(List.of("1", "2.5", "-3"), strings);
        }

        @Test
        void toDoubleArrayContainsDoubleValues() {
            BigNumberList list = listOf("1", "2");

            double[] doubles = list.toDoubleArray();

            assertEquals(2, doubles.length);
            assertEquals(1.0, doubles[0], 1e-9);
            assertEquals(2.0, doubles[1], 1e-9);
        }
    }

    @Nested
    class DelegationSanityTests {

        @Test
        void addAppendsElement() {
            BigNumberList list = listOf("1", "2");

            list.add(getNewBigNumber("3"));

            assertEquals(3, list.size());
            assertBigNumberEquals(getNewBigNumber("3"), list.get(2));
        }

        @Test
        void forEachProcessesAllElements() {
            BigNumberList list = listOf("1", "2", "3");

            AtomicBoolean sawOne = new AtomicBoolean(false);
            AtomicBoolean sawTwo = new AtomicBoolean(false);
            AtomicBoolean sawThree = new AtomicBoolean(false);

            BigNumber one = getNewBigNumber("1");
            BigNumber two = getNewBigNumber("2");
            BigNumber three = getNewBigNumber("3");

            list.forEach(value -> {
                if (value.compareTo(one) == 0) {
                    sawOne.set(true);
                }
                if (value.compareTo(two) == 0) {
                    sawTwo.set(true);
                }
                if (value.compareTo(three) == 0) {
                    sawThree.set(true);
                }
            });

            assertTrue(sawOne.get());
            assertTrue(sawTwo.get());
            assertTrue(sawThree.get());
        }
    }

}
