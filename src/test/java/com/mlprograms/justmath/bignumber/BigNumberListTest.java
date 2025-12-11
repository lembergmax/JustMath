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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class BigNumberListTest {

    private static BigNumber bn(String value) {
        return new BigNumber(value);
    }

    private static BigNumberList listOf(String... values) {
        List<BigNumber> numbers = new ArrayList<>(values.length);
        for (String value : values) {
            numbers.add(bn(value));
        }
        return new BigNumberList(numbers);
    }

    private static void assertBigNumberEquals(BigNumber expected, BigNumber actual) {
        assertEquals(expected, actual, () ->
                "Expected BigNumber <" + expected + "> but was <" + actual + ">");
    }

    @Nested
    @DisplayName("Constructors and factory methods")
    class ConstructorAndFactoryTests {

        @Test
        @DisplayName("of(...) should create list with given elements")
        void ofCreatesListWithElements() {
            BigNumberList list = BigNumberList.of(bn("1"), bn("2"), bn("3"));

            assertEquals(3, list.size());
            assertBigNumberEquals(bn("1"), list.get(0));
            assertBigNumberEquals(bn("2"), list.get(1));
            assertBigNumberEquals(bn("3"), list.get(2));
        }

        @Test
        @DisplayName("fromStrings(...) should parse all values correctly")
        void fromStringsParsesValues() {
            List<String> values = List.of("10", "-2", "3.5");
            BigNumberList list = BigNumberList.fromStrings(values);

            assertEquals(3, list.size());
            assertBigNumberEquals(bn("10"), list.get(0));
            assertBigNumberEquals(bn("-2"), list.get(1));
            assertBigNumberEquals(bn("3.5"), list.get(2));
        }

        @Test
        @DisplayName("copy() should create independent internal list but share element references")
        void copyCreatesIndependentStorage() {
            BigNumberList original = listOf("1", "2", "3");
            BigNumberList copy = original.copy();

            assertEquals(original.size(), copy.size());
            assertBigNumberEquals(original.get(0), copy.get(0));

            original.add(bn("4"));
            assertEquals(3, copy.size(), "Copy must not change when original is structurally modified");
        }

        @Test
        @DisplayName("clone() should share the internal list reference")
        void cloneSharesInternalStorage() {
            BigNumberList original = listOf("1", "2");
            BigNumberList clone = original.clone();

            original.add(bn("3"));

            assertEquals(3, clone.size(), "Clone shares internal list with original");
        }
    }

    @Nested
    @DisplayName("Sorting methods")
    class SortingTests {

        @Test
        @DisplayName("sort(Class) should use provided SortingAlgorithm implementation")
        void sortWithAlgorithmClass() {
            BigNumberList list = listOf("3", "1", "2");

            list.sort(QuickSort.class);

            assertEquals(3, list.size());
            assertBigNumberEquals(bn("1"), list.get(0));
            assertBigNumberEquals(bn("2"), list.get(1));
            assertBigNumberEquals(bn("3"), list.get(2));
        }

        @Test
        @DisplayName("sort(Class) should throw IllegalStateException if algorithm returns null")
        void sortThrowsOnNullResult() {
            BigNumberList list = listOf("3", "1", "2");

            assertThrows(IllegalStateException.class,
                    () -> list.sort(NullReturningAlgorithm.class),
                    "Sorting should fail when algorithm returns null");
        }

        @Test
        @DisplayName("sort(Class) should throw IllegalArgumentException if no default constructor exists")
        void sortThrowsWhenNoDefaultConstructor() {
            BigNumberList list = listOf("1", "2");

            assertThrows(IllegalArgumentException.class,
                    () -> list.sort(NoDefaultConstructorAlgorithm.class),
                    "Sorting should fail for algorithms without default constructor");
        }

        @Test
        @DisplayName("sortAscending() should sort using natural order")
        void sortAscendingSortsNaturally() {
            BigNumberList list = listOf("5", "1", "3");

            list.sortAscending();

            assertBigNumberEquals(bn("1"), list.get(0));
            assertBigNumberEquals(bn("3"), list.get(1));
            assertBigNumberEquals(bn("5"), list.get(2));
        }

        @Test
        @DisplayName("sortDescending() should sort in reverse natural order")
        void sortDescendingSortsReversed() {
            BigNumberList list = listOf("5", "1", "3");

            list.sortDescending();

            assertBigNumberEquals(bn("5"), list.get(0));
            assertBigNumberEquals(bn("3"), list.get(1));
            assertBigNumberEquals(bn("1"), list.get(2));
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
    @DisplayName("Aggregation and statistical methods")
    class StatisticsTests {

        @Test
        @DisplayName("sum() should compute the sum of all elements")
        void sumComputesCorrectResult() {
            BigNumberList list = listOf("1", "2", "3", "4");

            BigNumber sum = list.sum();

            assertBigNumberEquals(bn("10"), sum);
        }

        @Test
        @DisplayName("sum() should throw when list is empty")
        void sumOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::sum);
        }

        @Test
        @DisplayName("average() should compute arithmetic mean")
        void averageComputesCorrectResult() {
            BigNumberList list = listOf("2", "4", "6", "8");

            BigNumber avg = list.average();

            assertBigNumberEquals(bn("5"), avg);
        }

        @Test
        @DisplayName("average() should throw when list is empty")
        void averageOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::average);
        }

        @Test
        @DisplayName("median() should return middle element for odd number of elements")
        void medianOddCount() {
            BigNumberList list = listOf("3", "1", "2");

            BigNumber median = list.median();

            assertBigNumberEquals(bn("2"), median);
        }

        @Test
        @DisplayName("median() should return average of two middle elements for even number of elements")
        void medianEvenCount() {
            BigNumberList list = listOf("1", "2", "4", "3"); // sorted → 1,2,3,4

            BigNumber median = list.median();

            assertBigNumberEquals(bn("2.5"), median);
        }

        @Test
        @DisplayName("median() should throw when list is empty")
        void medianOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::median);
        }

        @Test
        @DisplayName("modes() should return all values with highest frequency")
        void modesReturnMostFrequentValues() {
            BigNumberList list = listOf("1", "2", "2", "3", "3");

            Set<BigNumber> modes = list.modes();

            assertEquals(2, modes.size());
            assertTrue(modes.contains(bn("2")));
            assertTrue(modes.contains(bn("3")));
        }

        @Test
        @DisplayName("modes() should return empty set for empty list")
        void modesOnEmptyListReturnsEmptySet() {
            BigNumberList list = listOf();

            Set<BigNumber> modes = list.modes();

            assertTrue(modes.isEmpty());
        }

        @Test
        @DisplayName("min() should return smallest element")
        void minReturnsSmallestValue() {
            BigNumberList list = listOf("5", "1", "3");

            BigNumber min = list.min();

            assertBigNumberEquals(bn("1"), min);
        }

        @Test
        @DisplayName("min() should throw on empty list")
        void minOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::min);
        }

        @Test
        @DisplayName("max() should return largest element")
        void maxReturnsLargestValue() {
            BigNumberList list = listOf("5", "1", "3");

            BigNumber max = list.max();

            assertBigNumberEquals(bn("5"), max);
        }

        @Test
        @DisplayName("max() should throw on empty list")
        void maxOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::max);
        }

        @Test
        @DisplayName("range() should return max() - min()")
        void rangeComputesDifference() {
            BigNumberList list = listOf("10", "2", "8");

            BigNumber range = list.range();

            assertBigNumberEquals(bn("8"), range); // 10 - 2
        }

        @Test
        @DisplayName("variance() should compute population variance")
        void varianceComputesCorrectResult() {
            BigNumberList list = listOf("1", "3"); // mean = 2, deviations: 1,1 → variance = 1

            BigNumber variance = list.variance();

            assertBigNumberEquals(bn("1"), variance);
        }

        @Test
        @DisplayName("variance() should throw on list with fewer than two elements")
        void varianceRequiresAtLeastTwoElements() {
            BigNumberList single = listOf("1");
            BigNumberList empty = listOf();

            assertThrows(IllegalStateException.class, single::variance);
            assertThrows(IllegalStateException.class, empty::variance);
        }

        @Test
        @DisplayName("standardDeviation() should be square root of variance")
        void standardDeviationIsSqrtOfVariance() {
            BigNumberList list = listOf("1", "3"); // variance = 1, std dev = 1

            BigNumber variance = list.variance();
            BigNumber stddev = list.standardDeviation();

            assertBigNumberEquals(variance, stddev.multiply(stddev),
                    "stddev squared should equal variance (for these values)");
        }

        @Test
        @DisplayName("geometricMean() should compute correct value for positive numbers")
        void geometricMeanComputesCorrectValue() {
            BigNumberList list = listOf("4", "1"); // product=4, n=2 → sqrt(4)=2

            BigNumber geoMean = list.geometricMean();

            assertBigNumberEquals(bn("2"), geoMean);
        }

        @Test
        @DisplayName("geometricMean() should throw on empty list")
        void geometricMeanOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::geometricMean);
        }

        @Test
        @DisplayName("geometricMean() should throw when list contains negative value")
        void geometricMeanThrowsOnNegative() {
            BigNumberList list = listOf("4", "-1");

            assertThrows(IllegalStateException.class, list::geometricMean);
        }

        @Test
        @DisplayName("harmonicMean() should compute correct value")
        void harmonicMeanComputesCorrectValue() {
            BigNumberList list = listOf("1", "2"); // N=2, reciprocals=1,0.5 → sum=1.5 → 2/1.5=1.333...

            BigNumber harmonicMean = list.harmonicMean();

            // TODO
            // 4/3 = 1.333...
            BigNumber expected = bn("1.3333333333"); // depends on BigNumber precision; primarily a behavioural check
            // We cannot reliably assert exact equality without knowing precision,
            // but we can check that the value is between 1.3 and 1.4.
            assertTrue(harmonicMean.doubleValue() > 1.3 && harmonicMean.doubleValue() < 1.4,
                    "Harmonic mean should be approximately 4/3");
        }

        @Test
        @DisplayName("harmonicMean() should throw on empty list")
        void harmonicMeanOnEmptyListThrows() {
            BigNumberList list = listOf();

            assertThrows(IllegalStateException.class, list::harmonicMean);
        }

        @Test
        @DisplayName("harmonicMean() should throw when list contains zero")
        void harmonicMeanThrowsOnZero() {
            BigNumberList list = listOf("1", "0");

            assertThrows(ArithmeticException.class, list::harmonicMean);
        }
    }

    @Nested
    @DisplayName("Transformation methods")
    class TransformationTests {

        @Test
        @DisplayName("absAll() should replace each element with its absolute value")
        void absAllAppliesAbsoluteValue() {
            BigNumberList list = listOf("-1", "2", "-3");

            list.absAll();

            assertBigNumberEquals(bn("1"), list.get(0));
            assertBigNumberEquals(bn("2"), list.get(1));
            assertBigNumberEquals(bn("3"), list.get(2));
        }

        @Test
        @DisplayName("negateAll() should replace each element with its negation")
        void negateAllNegatesValues() {
            BigNumberList list = listOf("1", "-2", "3");

            list.negateAll();

            assertBigNumberEquals(bn("-1"), list.get(0));
            assertBigNumberEquals(bn("2"), list.get(1));
            assertBigNumberEquals(bn("-3"), list.get(2));
        }

        @Test
        @DisplayName("scale(factor) should multiply each element by factor")
        void scaleMultipliesEachElement() {
            BigNumberList list = listOf("1", "2", "3");

            list.scale(bn("10"));

            assertBigNumberEquals(bn("10"), list.get(0));
            assertBigNumberEquals(bn("20"), list.get(1));
            assertBigNumberEquals(bn("30"), list.get(2));
        }

        @Test
        @DisplayName("translate(offset) should add offset to each element")
        void translateAddsOffset() {
            BigNumberList list = listOf("1", "2", "3");

            list.translate(bn("5"));

            assertBigNumberEquals(bn("6"), list.get(0));
            assertBigNumberEquals(bn("7"), list.get(1));
            assertBigNumberEquals(bn("8"), list.get(2));
        }

        @Test
        @DisplayName("powEach(exponent) should raise each element to power")
        void powEachRaisesToPower() {
            BigNumberList list = listOf("2", "3");

            list.powEach(bn("3")); // 2^3=8, 3^3=27

            assertBigNumberEquals(bn("8"), list.get(0));
            assertBigNumberEquals(bn("27"), list.get(1));
        }

        @Test
        @DisplayName("clampAll(min, max) should clamp values into inclusive range")
        void clampAllClampsToRange() {
            BigNumberList list = listOf("-1", "5", "10");

            list.clampAll(bn("0"), bn("9"));

            assertBigNumberEquals(bn("0"), list.get(0));
            assertBigNumberEquals(bn("5"), list.get(1));
            assertBigNumberEquals(bn("9"), list.get(2));
        }

        @Test
        @DisplayName("clampAll(min, max) should throw when max < min")
        void clampAllThrowsWhenMaxLessThanMin() {
            BigNumberList list = listOf("1", "2");

            assertThrows(IllegalArgumentException.class,
                    () -> list.clampAll(bn("5"), bn("1")));
        }

        @Test
        @DisplayName("normalizeToSum(target) should scale elements so that sum becomes target")
        void normalizeToSumScalesListToTargetSum() {
            BigNumberList list = listOf("1", "1", "2"); // sum=4

            list.normalizeToSum(bn("8"));

            BigNumber newSum = list.sum();
            assertTrue(Math.abs(newSum.doubleValue() - 8.0) < 1e-9,
                    "Normalized sum should be approximately 8");
        }

        @Test
        @DisplayName("normalizeToSum(target) should throw when current sum is zero")
        void normalizeToSumThrowsWhenSumIsZero() {
            BigNumberList list = listOf("1", "-1"); // sum=0

            assertThrows(IllegalStateException.class,
                    () -> list.normalizeToSum(bn("5")));
        }

        @Test
        @DisplayName("map(operator) should create a new list with transformed values")
        void mapCreatesTransformedCopy() {
            BigNumberList list = listOf("1", "2", "3");

            UnaryOperator<BigNumber> op = value -> value.multiply(bn("2"));
            BigNumberList doubled = list.map(op);

            assertEquals(3, doubled.size());
            assertBigNumberEquals(bn("2"), doubled.get(0));
            assertBigNumberEquals(bn("4"), doubled.get(1));
            assertBigNumberEquals(bn("6"), doubled.get(2));

            // original must remain unchanged
            assertBigNumberEquals(bn("1"), list.get(0));
        }

        @Test
        @DisplayName("reverse() should reverse the order of elements")
        void reverseReversesList() {
            BigNumberList list = listOf("1", "2", "3");

            list.reverse();

            assertBigNumberEquals(bn("3"), list.get(0));
            assertBigNumberEquals(bn("2"), list.get(1));
            assertBigNumberEquals(bn("1"), list.get(2));
        }
    }

    @Nested
    @DisplayName("Structural operations and queries")
    class StructuralTests {

        @Test
        @DisplayName("immutableCopy() should return unmodifiable list")
        void immutableCopyReturnsUnmodifiableList() {
            BigNumberList list = listOf("1", "2");

            List<BigNumber> copy = list.immutableCopy();

            assertEquals(2, copy.size());
            assertThrows(UnsupportedOperationException.class,
                    () -> copy.add(bn("3")));
        }

        @Test
        @DisplayName("shuffle() should permute elements (basic sanity check)")
        void shufflePermutesList() {
            BigNumberList list = listOf("1", "2", "3", "4");
            BigNumberList originalCopy = list.copy();

            list.shuffle(new Random(12345));

            // same elements, possibly different order
            assertEquals(originalCopy.size(), list.size());
            assertTrue(list.containsAll(originalCopy));
        }

        @Test
        @DisplayName("rotate(distance) should rotate elements as Collections.rotate does")
        void rotateRotatesValues() {
            BigNumberList list = listOf("1", "2", "3", "4");

            list.rotate(1);

            assertBigNumberEquals(bn("4"), list.get(0));
            assertBigNumberEquals(bn("1"), list.get(1));
            assertBigNumberEquals(bn("2"), list.get(2));
            assertBigNumberEquals(bn("3"), list.get(3));
        }

        @Test
        @DisplayName("distinct() should return list containing each element only once preserving order")
        void distinctPreservesOrderAndRemovesDuplicates() {
            BigNumberList list = listOf("1", "2", "2", "3", "1");

            BigNumberList distinct = list.distinct();

            assertEquals(3, distinct.size());
            assertBigNumberEquals(bn("1"), distinct.get(0));
            assertBigNumberEquals(bn("2"), distinct.get(1));
            assertBigNumberEquals(bn("3"), distinct.get(2));
        }

        @Test
        @DisplayName("append(other) should concatenate two BigNumberLists")
        void appendConcatenatesLists() {
            BigNumberList list1 = listOf("1", "2");
            BigNumberList list2 = listOf("3", "4");

            BigNumberList appended = list1.append(list2);

            assertEquals(4, appended.size());
            assertBigNumberEquals(bn("1"), appended.get(0));
            assertBigNumberEquals(bn("2"), appended.get(1));
            assertBigNumberEquals(bn("3"), appended.get(2));
            assertBigNumberEquals(bn("4"), appended.get(3));
        }

        @Test
        @DisplayName("subListCopy(from, to) should return independent copy of sub range")
        void subListCopyCreatesIndependentCopy() {
            BigNumberList list = listOf("1", "2", "3", "4");

            BigNumberList sub = list.subListCopy(1, 3); // "2","3"

            assertEquals(2, sub.size());
            assertBigNumberEquals(bn("2"), sub.get(0));
            assertBigNumberEquals(bn("3"), sub.get(1));

            list.set(1, bn("99"));
            assertBigNumberEquals(bn("2"), sub.get(0));
        }

        @Test
        @DisplayName("anyMatch(predicate) should return true if any element matches")
        void anyMatchReturnsTrueWhenConditionHolds() {
            BigNumberList list = listOf("1", "2", "3");

            Predicate<BigNumber> isEven = value -> value.mod(bn("2")).compareTo(bn("0")) == 0;

            assertTrue(list.anyMatch(isEven));
        }

        @Test
        @DisplayName("anyMatch(predicate) should return false if no element matches")
        void anyMatchReturnsFalseWhenNoElementMatches() {
            BigNumberList list = listOf("1", "3", "5");

            Predicate<BigNumber> isEven = value -> value.mod(bn("2")).compareTo(bn("0")) == 0;

            assertFalse(list.anyMatch(isEven));
        }

        @Test
        @DisplayName("allMatch(predicate) should return true when all elements match")
        void allMatchReturnsTrueWhenAllMatch() {
            BigNumberList list = listOf("2", "4", "6");

            Predicate<BigNumber> isEven = value -> value.mod(bn("2")).compareTo(bn("0")) == 0;

            assertTrue(list.allMatch(isEven));
        }

        @Test
        @DisplayName("allMatch(predicate) should return false when at least one element mismatches")
        void allMatchReturnsFalseWhenAnyFails() {
            BigNumberList list = listOf("2", "3", "4");

            Predicate<BigNumber> isEven = value -> value.mod(bn("2")).compareTo(bn("0")) == 0;

            assertFalse(list.allMatch(isEven));
        }

        @Test
        @DisplayName("allMatch(predicate) should return true for empty list")
        void allMatchOnEmptyListReturnsTrue() {
            BigNumberList list = listOf();

            Predicate<BigNumber> any = value -> true;

            assertTrue(list.allMatch(any));
        }

        @Test
        @DisplayName("findFirst(predicate) should return first matching element")
        void findFirstReturnsFirstMatch() {
            BigNumberList list = listOf("1", "4", "3", "4");

            Predicate<BigNumber> equalsFour = value -> value.compareTo(bn("4")) == 0;

            Optional<BigNumber> first = list.findFirst(equalsFour);

            assertTrue(first.isPresent());
            assertBigNumberEquals(bn("4"), first.get());
        }

        @Test
        @DisplayName("findFirst(predicate) should return empty Optional when no match exists")
        void findFirstReturnsEmptyWhenNoMatchFound() {
            BigNumberList list = listOf("1", "2", "3");

            Predicate<BigNumber> equalsTen = value -> value.compareTo(bn("10")) == 0;

            Optional<BigNumber> first = list.findFirst(equalsTen);

            assertTrue(first.isEmpty());
        }

        @Test
        @DisplayName("filter(predicate) should return list containing only matching elements")
        void filterReturnsMatchingElements() {
            BigNumberList list = listOf("1", "2", "3", "4");

            Predicate<BigNumber> isEven = value -> value.mod(bn("2")).compareTo(bn("0")) == 0;
            BigNumberList evens = list.filter(isEven);

            assertEquals(2, evens.size());
            assertBigNumberEquals(bn("2"), evens.get(0));
            assertBigNumberEquals(bn("4"), evens.get(1));

            // original list must stay unchanged
            assertEquals(4, list.size());
        }

        @Test
        @DisplayName("isSortedAscending() should detect ascending order")
        void isSortedAscendingDetectsOrder() {
            BigNumberList sorted = listOf("1", "2", "3");
            BigNumberList unsorted = listOf("2", "1", "3");

            assertTrue(sorted.isSortedAscending());
            assertFalse(unsorted.isSortedAscending());
        }

        @Test
        @DisplayName("isSortedDescending() should detect descending order")
        void isSortedDescendingDetectsOrder() {
            BigNumberList sorted = listOf("3", "2", "1");
            BigNumberList unsorted = listOf("2", "3", "1");

            assertTrue(sorted.isSortedDescending());
            assertFalse(unsorted.isSortedDescending());
        }

        @Test
        @DisplayName("isMonotonicIncreasing() should allow equal neighbours")
        void isMonotonicIncreasingAllowsEqualNeighbours() {
            BigNumberList list = listOf("1", "2", "2", "3");

            assertTrue(list.isMonotonicIncreasing());
        }

        @Test
        @DisplayName("isMonotonicDecreasing() should allow equal neighbours")
        void isMonotonicDecreasingAllowsEqualNeighbours() {
            BigNumberList list = listOf("3", "3", "2", "1");

            assertTrue(list.isMonotonicDecreasing());
        }
    }

    @Nested
    @DisplayName("Conversion utilities")
    class ConversionTests {

        @Test
        @DisplayName("toUnmodifiableList() should return snapshot copy")
        void toUnmodifiableListReturnsSnapshotCopy() {
            BigNumberList list = listOf("1", "2");

            List<BigNumber> snapshot = list.toUnmodifiableList();

            assertEquals(2, snapshot.size());

            list.add(bn("3"));

            assertEquals(2, snapshot.size(),
                    "Snapshot should not change when original list changes");
        }

        @Test
        @DisplayName("toBigNumberArray() should return array with all elements in order")
        void toBigNumberArrayContainsAllElements() {
            BigNumberList list = listOf("1", "2", "3");

            BigNumber[] array = list.toBigNumberArray();

            assertEquals(3, array.length);
            assertBigNumberEquals(bn("1"), array[0]);
            assertBigNumberEquals(bn("2"), array[1]);
            assertBigNumberEquals(bn("3"), array[2]);
        }

        @Test
        @DisplayName("toStringList() should return string representations of all elements")
        void toStringListContainsStringRepresentations() {
            BigNumberList list = listOf("1", "2.5", "-3");

            List<String> strings = list.toStringList();

            assertEquals(List.of("1", "2.5", "-3"), strings);
        }

        @Test
        @DisplayName("toDoubleArray() should return primitive double array with all values")
        void toDoubleArrayContainsDoubleValues() {
            BigNumberList list = listOf("1.5", "2.5");

            double[] doubles = list.toDoubleArray();

            assertEquals(2, doubles.length);
            assertEquals(1.5, doubles[0], 1e-9);
            assertEquals(2.5, doubles[1], 1e-9);
        }
    }

    @Nested
    @DisplayName("Delegated List behaviour (sanity checks)")
    class DelegationSanityTests {

        @Test
        @DisplayName("add(BigNumber) should append element to underlying list")
        void addAppendsElement() {
            BigNumberList list = listOf("1", "2");

            list.add(bn("3"));

            assertEquals(3, list.size());
            assertBigNumberEquals(bn("3"), list.get(2));
        }

        @Test
        @DisplayName("forEach(...) should process all elements")
        void forEachProcessesAllElements() {
            BigNumberList list = listOf("1", "2", "3");

            AtomicBoolean sawOne = new AtomicBoolean(false);
            AtomicBoolean sawTwo = new AtomicBoolean(false);
            AtomicBoolean sawThree = new AtomicBoolean(false);

            list.forEach(value -> {
                if (value.equals(bn("1"))) {
                    sawOne.set(true);
                }
                if (value.equals(bn("2"))) {
                    sawTwo.set(true);
                }
                if (value.equals(bn("3"))) {
                    sawThree.set(true);
                }
            });

            assertTrue(sawOne.get());
            assertTrue(sawTwo.get());
            assertTrue(sawThree.get());
        }
    }

}
