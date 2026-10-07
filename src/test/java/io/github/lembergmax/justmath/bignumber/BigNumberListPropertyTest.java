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

import static io.github.lembergmax.justmath.bignumber.math.DecimalArbitraries.SEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.github.lembergmax.justmath.bignumber.algorithms.BubbleSort;
import io.github.lembergmax.justmath.bignumber.algorithms.GnomeSort;
import io.github.lembergmax.justmath.bignumber.algorithms.InsertionSort;
import io.github.lembergmax.justmath.bignumber.algorithms.MergeSort;
import io.github.lembergmax.justmath.bignumber.algorithms.QuickSort;
import io.github.lembergmax.justmath.bignumber.algorithms.RadixSort;
import io.github.lembergmax.justmath.bignumber.algorithms.SelectionSort;
import io.github.lembergmax.justmath.bignumber.algorithms.SortingAlgorithm;
import io.github.lembergmax.justmath.bignumber.algorithms.TimSort;
import io.github.lembergmax.justmath.bignumber.math.AccuracyAssertions;
import io.github.lembergmax.justmath.bignumber.math.DecimalArbitraries;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Compares the statistics and the sorting of {@link BigNumberList} with {@link BigDecimal} and
 * {@link java.util.List#sort}. Sums, minima, maxima and medians are exact; the mean and the variance divide, so they
 * are compared within one unit in the last place of the default precision.
 */
class BigNumberListPropertyTest {

    private static final int TRIES = 250;

    private static final int MAX_SIZE = 40;

    private static final int REFERENCE_PRECISION = 400;

    private static final double MAX_ULPS = 1.0;

    private static final BigDecimal VARIANCE_TOLERANCE = new BigDecimal("1E-90");

    private static final MathContext DEFAULT_PRECISION = new MathContext(100, RoundingMode.HALF_UP);

    private static final List<Class<? extends SortingAlgorithm>> ALGORITHMS = List.of(
            BubbleSort.class, GnomeSort.class, InsertionSort.class, MergeSort.class, QuickSort.class,
            RadixSort.class, SelectionSort.class, TimSort.class);

    @Provide
    Arbitrary<List<BigDecimal>> longLists() {
        return DecimalArbitraries.decimals().list().ofMinSize(1).ofMaxSize(MAX_SIZE);
    }

    @Provide
    Arbitrary<List<BigDecimal>> shortLists() {
        return DecimalArbitraries.smallDecimals().list().ofMinSize(2).ofMaxSize(MAX_SIZE);
    }

    @Provide
    Arbitrary<Class<? extends SortingAlgorithm>> algorithms() {
        return Arbitraries.of(ALGORITHMS);
    }

    private static BigNumberList listOf(final List<BigDecimal> values) {
        final List<BigNumber> numbers = new ArrayList<>();
        values.forEach(value -> numbers.add(new BigNumber(value.toPlainString())));
        return new BigNumberList(numbers);
    }

    private static List<BigDecimal> valuesOf(final BigNumberList list) {
        final List<BigDecimal> values = new ArrayList<>();
        list.forEach(number -> values.add(number.toBigDecimal()));
        return values;
    }

    private static BigDecimal sumOf(final List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void assertSameValues(final List<BigDecimal> expected, final List<BigDecimal> actual, final String description) {
        assertEquals(expected.size(), actual.size(), description + ": size");
        for (int index = 0; index < expected.size(); index++) {
            assertEquals(0, expected.get(index).compareTo(actual.get(index)), description + ": element " + index + " expected " + expected.get(index) + " but was " + actual.get(index));
        }
    }

    @Property(tries = TRIES, seed = SEED)
    void sumIsExact(@ForAll("longLists") final List<BigDecimal> values) {
        assertEquals(0, sumOf(values).compareTo(listOf(values).sum().toBigDecimal()), "sum of " + values);
    }

    @Property(tries = TRIES, seed = SEED)
    void minAndMaxAreTheExtremes(@ForAll("longLists") final List<BigDecimal> values) {
        final BigNumberList list = listOf(values);

        assertEquals(0, values.stream().min(Comparator.naturalOrder()).orElseThrow().compareTo(list.min().toBigDecimal()), "min of " + values);
        assertEquals(0, values.stream().max(Comparator.naturalOrder()).orElseThrow().compareTo(list.max().toBigDecimal()), "max of " + values);
    }

    @Property(tries = TRIES, seed = SEED)
    void medianIsTheMiddleOfTheSortedValues(@ForAll("longLists") final List<BigDecimal> values) {
        final List<BigDecimal> sorted = values.stream().sorted().toList();
        final int middle = sorted.size() / 2;
        final BigDecimal actual = listOf(values).median().toBigDecimal();

        if (sorted.size() % 2 == 1) {
            assertEquals(0, sorted.get(middle).compareTo(actual), "median of " + values);
            return;
        }
        final BigDecimal mean = sorted.get(middle - 1).add(sorted.get(middle)).divide(BigDecimal.TWO);
        AccuracyAssertions.assertWithinUlps(mean.round(DEFAULT_PRECISION), actual, DEFAULT_PRECISION.getPrecision(), MAX_ULPS, "median of " + values);
    }

    @Property(tries = TRIES, seed = SEED)
    void meanIsTheSumOverTheCount(@ForAll("shortLists") final List<BigDecimal> values) {
        final BigDecimal expected = sumOf(values).divide(BigDecimal.valueOf(values.size()), new MathContext(REFERENCE_PRECISION));

        AccuracyAssertions.assertWithinUlps(expected.round(DEFAULT_PRECISION), listOf(values).average().toBigDecimal(), DEFAULT_PRECISION.getPrecision(), MAX_ULPS, "mean of " + values);
    }

    @Property(tries = TRIES, seed = SEED)
    void populationVarianceMatchesTheDefinition(@ForAll("shortLists") final List<BigDecimal> values) {
        final MathContext reference = new MathContext(REFERENCE_PRECISION);
        final BigDecimal count = BigDecimal.valueOf(values.size());
        final BigDecimal mean = sumOf(values).divide(count, reference);
        final BigDecimal squares = values.stream().map(value -> value.subtract(mean).pow(2, reference)).reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal expected = squares.divide(count, reference);

        final BigDecimal actual = listOf(values).variance().toBigDecimal();

        final BigDecimal tolerance = expected.signum() == 0 ? VARIANCE_TOLERANCE : expected.abs().multiply(VARIANCE_TOLERANCE);
        assertTrue(expected.subtract(actual).abs().compareTo(tolerance) <= 0,
                "variance of " + values + " expected " + expected.round(new MathContext(30)) + " but was " + actual.round(new MathContext(30)));
    }

    @Property(tries = TRIES, seed = SEED)
    void everySortingAlgorithmProducesTheSortedValues(
            @ForAll("longLists") final List<BigDecimal> values,
            @ForAll("algorithms") final Class<? extends SortingAlgorithm> algorithm
    ) {
        final BigNumberList list = listOf(values);

        list.sort(algorithm);

        assertSameValues(values.stream().sorted().toList(), valuesOf(list), algorithm.getSimpleName());
    }

    @Property(tries = TRIES, seed = SEED)
    void sortAscendingAndDescendingAreReverseOfEachOther(@ForAll("longLists") final List<BigDecimal> values) {
        final List<BigDecimal> ascending = valuesOf(listOf(values).sortAscending());
        final List<BigDecimal> descending = valuesOf(listOf(values).sortDescending());

        assertSameValues(values.stream().sorted().toList(), ascending, "ascending");
        assertSameValues(values.stream().sorted(Comparator.reverseOrder()).toList(), descending, "descending");
    }

    @Property(tries = TRIES, seed = SEED)
    void aCopyAndAClonedListAreIndependentOfTheOriginal(@ForAll("longLists") final List<BigDecimal> values) {
        final BigNumberList original = listOf(values);
        final BigNumberList copy = original.copy();
        final BigNumberList clone = original.clone();

        original.negateAll();

        assertNotSame(original, copy);
        assertNotSame(original, clone);
        assertSameValues(values, valuesOf(copy), "copy");
        assertSameValues(values, valuesOf(clone), "clone");
    }

}
