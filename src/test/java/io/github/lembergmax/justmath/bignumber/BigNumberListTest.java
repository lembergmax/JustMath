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

package io.github.lembergmax.justmath.bignumber;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import io.github.lembergmax.justmath.bignumber.algorithms.QuickSort;
import io.github.lembergmax.justmath.bignumber.algorithms.SortingAlgorithm;

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

    /**
     * Overload that prefixes the standard failure message with caller-supplied context. Used by
     * assertions where the bare "Expected X but was Y" diagnostic does not explain why a given
     * value was expected (for example tests that exercise live-view vs. snapshot semantics).
     */
    private static void assertBigNumberEquals(BigNumber expected, BigNumber actual, String contextMessage) {
        assertEquals(expected.toString(), actual.toString(), () ->
                contextMessage + " — expected BigNumber <" + expected + "> but was <" + actual + ">");
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

            BigNumber two = getNewBigNumber("2");
            BigNumber three = getNewBigNumber("3");

            boolean hasTwo = modes.stream().anyMatch(bn -> bn.compareTo(two) == 0);
            boolean hasThree = modes.stream().anyMatch(bn -> bn.compareTo(three) == 0);

            assertTrue(hasTwo);
            assertTrue(hasThree);
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

    /**
     * Tests for the {@link List} interface contract, added when {@link BigNumberList} grew an
     * {@code implements List<BigNumber>} declaration. They verify that the class behaves like
     * a regular {@link List} for equality, hashing, iteration, sub-views and the Java 21
     * {@code SequencedCollection} default methods.
     */
    @Nested
    class ListContractTests {

        // -----------------------------------------------------------------------------------
        // Group 1: equals / hashCode contract
        // -----------------------------------------------------------------------------------

        @Test
        void equals_reflexive() {
            BigNumberList list = listOf("1", "2", "3");
            assertEquals(list, list);
        }

        @Test
        void equals_symmetric_betweenBigNumberLists() {
            BigNumberList left = listOf("1", "2", "3");
            BigNumberList right = listOf("1", "2", "3");
            assertEquals(left, right);
            assertEquals(right, left);
        }

        @Test
        void equals_betweenBigNumberListAndPlainList() {
            BigNumberList bigNumberList = listOf("1", "2", "3");
            List<BigNumber> plainArrayList = new ArrayList<>(List.of(
                    getNewBigNumber("1"), getNewBigNumber("2"), getNewBigNumber("3")));
            assertEquals(bigNumberList, plainArrayList,
                    "List.equals contract: BigNumberList must equal any List with same elements in same order");
        }

        @Test
        void equals_differentOrder_returnsFalse() {
            BigNumberList ascending = listOf("1", "2", "3");
            BigNumberList descending = listOf("3", "2", "1");
            assertNotEquals(ascending, descending);
        }

        @Test
        void equals_differentSize_returnsFalse() {
            BigNumberList shorter = listOf("1", "2");
            BigNumberList longer = listOf("1", "2", "3");
            assertNotEquals(shorter, longer);
        }

        @Test
        void equals_againstNonListType_returnsFalse() {
            BigNumberList list = listOf("1");
            assertNotEquals(list, "1");
            assertNotEquals(list, getNewBigNumber("1"));
        }

        @Test
        void hashCode_consistentWithEquals() {
            BigNumberList first = listOf("1", "2", "3");
            BigNumberList second = listOf("1", "2", "3");
            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode(),
                    "Equal lists must have equal hash codes");
        }

        @Test
        void hashCode_consistentWithPlainArrayList() {
            BigNumberList bigNumberList = listOf("1", "2", "3");
            List<BigNumber> plainArrayList = new ArrayList<>(List.of(
                    getNewBigNumber("1"), getNewBigNumber("2"), getNewBigNumber("3")));
            assertEquals(plainArrayList.hashCode(), bigNumberList.hashCode(),
                    "BigNumberList hashCode must follow the List#hashCode contract");
        }

        // -----------------------------------------------------------------------------------
        // Group 2: ListIterator contract
        // -----------------------------------------------------------------------------------

        @Test
        void listIterator_traverseForward() {
            BigNumberList list = listOf("10", "20", "30");
            ListIterator<BigNumber> iterator = list.listIterator();
            assertTrue(iterator.hasNext());
            assertBigNumberEquals(getNewBigNumber("10"), iterator.next());
            assertBigNumberEquals(getNewBigNumber("20"), iterator.next());
            assertBigNumberEquals(getNewBigNumber("30"), iterator.next());
            assertFalse(iterator.hasNext());
        }

        @Test
        void listIterator_traverseBackward() {
            BigNumberList list = listOf("10", "20", "30");
            ListIterator<BigNumber> iterator = list.listIterator(list.size());
            assertTrue(iterator.hasPrevious());
            assertBigNumberEquals(getNewBigNumber("30"), iterator.previous());
            assertBigNumberEquals(getNewBigNumber("20"), iterator.previous());
            assertBigNumberEquals(getNewBigNumber("10"), iterator.previous());
            assertFalse(iterator.hasPrevious());
        }

        @Test
        void listIterator_atIndex_startsAtPosition() {
            BigNumberList list = listOf("10", "20", "30");
            ListIterator<BigNumber> iterator = list.listIterator(2);
            assertEquals(2, iterator.nextIndex());
            assertBigNumberEquals(getNewBigNumber("30"), iterator.next());
        }

        @Test
        void listIterator_setReplacesCurrentElement() {
            BigNumberList list = listOf("10", "20", "30");
            ListIterator<BigNumber> iterator = list.listIterator();
            iterator.next();
            iterator.set(getNewBigNumber("99"));
            assertBigNumberEquals(getNewBigNumber("99"), list.get(0));
        }

        @Test
        void listIterator_addInsertsAtCursor() {
            BigNumberList list = listOf("10", "20", "30");
            ListIterator<BigNumber> iterator = list.listIterator();
            iterator.next();
            iterator.add(getNewBigNumber("15"));
            assertEquals(4, list.size());
            assertBigNumberEquals(getNewBigNumber("15"), list.get(1));
        }

        @Test
        void listIterator_removeAfterNext() {
            BigNumberList list = listOf("10", "20", "30");
            ListIterator<BigNumber> iterator = list.listIterator();
            iterator.next();
            iterator.remove();
            assertEquals(2, list.size());
            assertBigNumberEquals(getNewBigNumber("20"), list.get(0));
        }

        // -----------------------------------------------------------------------------------
        // Group 3: Iterator fail-fast contract
        // -----------------------------------------------------------------------------------

        @Test
        void iterator_failsFast_onStructuralModification() {
            BigNumberList list = listOf("1", "2", "3");
            Iterator<BigNumber> iterator = list.iterator();
            iterator.next();
            list.add(getNewBigNumber("4"));
            assertThrows(ConcurrentModificationException.class, iterator::next);
        }

        @Test
        void listIterator_failsFast_onExternalModification() {
            BigNumberList list = listOf("1", "2", "3");
            ListIterator<BigNumber> iterator = list.listIterator();
            iterator.next();
            list.add(getNewBigNumber("4"));
            assertThrows(ConcurrentModificationException.class, iterator::next);
        }

        // -----------------------------------------------------------------------------------
        // Group 4: subList live-view contract vs. subListCopy snapshot
        // -----------------------------------------------------------------------------------

        @Test
        void subList_returnsLiveView_writeThroughToParent() {
            BigNumberList parent = listOf("10", "20", "30", "40");
            List<BigNumber> sub = parent.subList(1, 3);
            sub.set(0, getNewBigNumber("99"));
            assertBigNumberEquals(getNewBigNumber("99"), parent.get(1),
                    "subList is a live view; writes must surface in the parent");
        }

        @Test
        void subList_parentStructuralChange_invalidatesSubView() {
            BigNumberList parent = listOf("10", "20", "30", "40");
            List<BigNumber> sub = parent.subList(1, 3);
            parent.add(getNewBigNumber("50"));
            assertThrows(ConcurrentModificationException.class, () -> sub.get(0));
        }

        @Test
        void subListCopy_returnsIndependentSnapshot() {
            BigNumberList parent = listOf("10", "20", "30", "40");
            BigNumberList copy = parent.subListCopy(1, 3);
            parent.set(1, getNewBigNumber("99"));
            assertBigNumberEquals(getNewBigNumber("20"), copy.get(0),
                    "subListCopy is a snapshot; parent mutations must not leak in");
        }

        // -----------------------------------------------------------------------------------
        // Group 5: Stream / Spliterator
        // -----------------------------------------------------------------------------------

        @Test
        void stream_yieldsAllElementsInOrder() {
            BigNumberList list = listOf("1", "2", "3");
            List<String> collected = list.stream()
                    .map(BigNumber::toString)
                    .toList();
            assertEquals(List.of("1", "2", "3"), collected);
        }

        @Test
        void parallelStream_yieldsSameMultiset() {
            BigNumberList list = listOf("1", "2", "3", "4", "5");
            // Parallel traversal does not guarantee order; compare as multisets via sorted view.
            List<String> collected = list.parallelStream()
                    .map(BigNumber::toString)
                    .sorted()
                    .toList();
            assertEquals(List.of("1", "2", "3", "4", "5"), collected);
        }

        @Test
        void spliterator_reportsSizedAndOrdered() {
            BigNumberList list = listOf("1", "2", "3");
            Spliterator<BigNumber> spliterator = list.spliterator();
            assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED),
                    "List spliterators must report SIZED");
            assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED),
                    "List spliterators must report ORDERED");
            assertEquals(3L, spliterator.estimateSize());
        }

        // -----------------------------------------------------------------------------------
        // Group 6: SequencedCollection defaults (Java 21)
        // -----------------------------------------------------------------------------------

        @Test
        void sequencedAccessors_matchFirstAndLastElement() {
            BigNumberList list = listOf("10", "20", "30");
            assertBigNumberEquals(list.get(0), list.getFirst());
            assertBigNumberEquals(list.get(list.size() - 1), list.getLast());
        }
    }

}
