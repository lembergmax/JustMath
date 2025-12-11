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

import com.mlprograms.justmath.bignumber.algorithms.SortingAlgorithm;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.mlprograms.justmath.bignumber.BigNumbers.ONE;
import static com.mlprograms.justmath.bignumber.BigNumbers.TWO;
import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;

/**
 * A mutable, list-like container for {@link BigNumber} instances implementing the full {@link List} interface.
 *
 * <p>
 * This class acts as a domain-specific wrapper around a {@link List} of {@link BigNumber} objects.
 * It delegates core list operations to an internal list and adds convenience methods for
 * numerical and statistical operations that are natural for lists of numbers.
 * </p>
 *
 * <p><b>Design notes</b></p>
 * <ul>
 *   <li>The internal storage is held in the {@link #values} field and is never {@code null}.</li>
 *   <li>Most {@link List} methods are simple delegations to {@code values} or use
 *       {@code List.super} to leverage default implementations.</li>
 *   <li>Several domain-specific methods (e.g. {@link #sum()}, {@link #average()}, {@link #median()},
 *       {@link #variance()}, {@link #standardDeviation()}) are provided for typical numeric use cases.</li>
 *   <li>Many methods mutate this instance and return {@code this} for fluent usage; methods that
 *       create independent lists explicitly document that behaviour.</li>
 * </ul>
 */
@Getter
public class BigNumberList implements List<BigNumber> {

    /**
     * Internal storage for the elements of this {@code BigNumberList}.
     *
     * <p>This list holds the sequence of {@link BigNumber} instances managed by this class.
     * It is initialized by the constructors and must never be {@code null}. Some operations
     * may replace this reference entirely (for example {@link #sort(Class)} or {@link #reverse()}).</p>
     */
    private List<BigNumber> values;

    /**
     * Creates an empty {@code BigNumberList}.
     *
     * <p>The created instance starts with an empty internal list. Mutating operations that
     * rely on a modifiable backing list assume that the internal storage will eventually be
     * replaced with a mutable implementation (e.g. via {@link #add(BigNumber)} or factory methods).</p>
     */
    public BigNumberList() {
        values = new ArrayList<>();
    }

    /**
     * Creates a {@code BigNumberList} backed by the provided list reference.
     *
     * <p>
     * The given list becomes the internal storage for this instance. No defensive copy is made, so
     * external modifications to the provided list will be reflected in this {@code BigNumberList}
     * and vice versa. If independent ownership is required, use {@link #copy()} instead.
     * </p>
     *
     * @param values the list to use as internal storage; must not be {@code null}
     */
    public BigNumberList(@NonNull final List<BigNumber> values) {
        this.values = values;
    }

    /**
     * Creates a new {@code BigNumberList} that shares the internal storage of the provided instance.
     *
     * <p>This constructor performs a shallow copy of the internal list reference. Both the
     * original and the new {@code BigNumberList} instances will point to the same underlying list.
     * If you need an independent copy of the list contents, use {@link #copy()}.</p>
     *
     * @param bigNumberList the list whose internal storage should be shared; must not be {@code null}
     */
    public BigNumberList(@NonNull final BigNumberList bigNumberList) {
        this.values = bigNumberList.values;
    }

    /**
     * Creates a new {@code BigNumberList} from the provided {@link BigNumber} vararg array.
     *
     * <p>A new mutable {@link ArrayList} is created and populated with the given values.</p>
     *
     * @param numbers the numbers to include in the list; must not be {@code null} and must not contain {@code null} elements
     * @return a new {@code BigNumberList} containing all provided numbers
     * @throws NullPointerException if {@code numbers} or any of its elements is {@code null}
     */
    public static BigNumberList of(@NonNull final BigNumber... numbers) {
        Objects.requireNonNull(numbers, "numbers must not be null");

        final List<BigNumber> list = new ArrayList<>(numbers.length);
        for (BigNumber number : numbers) {
            Objects.requireNonNull(number, "numbers must not contain null elements");
            list.add(number);
        }

        return new BigNumberList(list);
    }

    /**
     * Creates a new {@code BigNumberList} from a list of string representations.
     *
     * <p>Each string is passed to the {@link BigNumber#BigNumber(String)} constructor. This is a
     * simple convenience factory for test data, parsing scenarios, or manual list construction.</p>
     *
     * @param stringValues a list of string representations of numbers; must not be {@code null} and must not contain {@code null} elements
     * @return a new {@code BigNumberList} containing the parsed {@link BigNumber} values
     * @throws NullPointerException     if {@code stringValues} or any element is {@code null}
     * @throws IllegalArgumentException if any string cannot be parsed by {@link BigNumber#BigNumber(String)}
     */
    public static BigNumberList fromStrings(@NonNull final List<String> stringValues) {
        Objects.requireNonNull(stringValues, "stringValues must not be null");

        final List<BigNumber> numbers = new ArrayList<>(stringValues.size());
        for (String value : stringValues) {
            Objects.requireNonNull(value, "stringValues must not contain null elements");
            numbers.add(new BigNumber(value));
        }

        return new BigNumberList(numbers);
    }

    /**
     * Sorts the elements of this list using the specified {@link SortingAlgorithm} implementation.
     *
     * <p>
     * The method instantiates the provided {@code algorithmClass} using its no-argument constructor,
     * delegates the sorting to, and replaces the internal storage
     * with the returned list. The operation mutates this instance and returns {@code this} for
     * fluent usage.
     * </p>
     *
     * <p>If the algorithm returns {@code null}, an {@link IllegalStateException} is thrown.</p>
     *
     * @param algorithmClass implementation of {@link SortingAlgorithm} to use for sorting; must not be {@code null}
     * @return this {@code BigNumberList} instance after sorting
     * @throws IllegalArgumentException if the algorithm class cannot be instantiated
     * @throws IllegalStateException    if the algorithm returns {@code null}
     */
    public BigNumberList sort(@NonNull final Class<? extends SortingAlgorithm> algorithmClass) {
        try {
            final SortingAlgorithm algorithm = algorithmClass.getDeclaredConstructor().newInstance();
            final List<BigNumber> sorted = algorithm.sort(values);

            if (sorted == null) {
                throw new IllegalStateException("Sorting algorithm returned null");
            }

            values = sorted;
            return this;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("Unable to instantiate sorting algorithm: " + algorithmClass.getName(), exception);
        }
    }

    /**
     * Sorts this list in ascending order using the natural ordering of {@link BigNumber}.
     *
     * <p>This is a convenience wrapper around {@link #sort(Comparator)} with
     * {@link Comparator#naturalOrder()}.</p>
     *
     * @return this {@code BigNumberList} instance after sorting
     */
    public BigNumberList sortAscending() {
        sort(Comparator.naturalOrder());
        return this;
    }

    /**
     * Sorts this list in descending order using the natural ordering of {@link BigNumber}.
     *
     * <p>This is a convenience wrapper around {@link #sort(Comparator)} with
     * {@link Comparator#reverseOrder()}.</p>
     *
     * @return this {@code BigNumberList} instance after sorting
     */
    public BigNumberList sortDescending() {
        sort(Comparator.reverseOrder());
        return this;
    }

    /**
     * Computes the sum of all {@link BigNumber} values contained in this list.
     *
     * <p>
     * The behaviour and numeric characteristics (precision, rounding, locale) are delegated to the
     * {@link BigNumber#sum(List)} implementation of the first element, following the pattern
     * used in the {@code BigNumber} API itself. The first element is treated as the "receiver"
     * and the remaining elements are passed as arguments.
     * </p>
     *
     * @return a new {@link BigNumber} representing the sum of all elements
     * @throws IllegalStateException if this list is empty
     */
    public BigNumber sum() {
        if (isEmpty()) {
            throw new IllegalStateException("sum requires at least one element, but the list is empty.");
        }

        if (values.size() == 1) {
            return values.getFirst();
        }

        return values.getFirst().sum(values.subList(1, values.size()));
    }

    /**
     * Computes the arithmetic mean (average) of the values in this list.
     *
     * <p>
     * The computation is delegated to {@link BigNumber#average(List)} on the first element.
     * This ensures consistent usage of the default {@link java.math.MathContext} and
     * {@link java.util.Locale} configuration defined by the underlying {@code BigNumber}.
     * </p>
     *
     * @return a new {@link BigNumber} representing the arithmetic mean of all elements
     * @throws IllegalStateException if this list is empty
     */
    public BigNumber average() {
        if (isEmpty()) {
            throw new IllegalStateException("average requires at least one element, but the list is empty.");
        }

        if (values.size() == 1) {
            return values.getFirst();
        }

        return values.getFirst().average(values.subList(1, values.size()));
    }

    /**
     * Computes the median of the values in this list.
     *
     * <p>
     * The median is defined as:
     * </p>
     * <ul>
     *   <li>For an odd number of elements: the middle element of the sorted list.</li>
     *   <li>For an even number of elements: the arithmetic mean of the two middle elements.</li>
     * </ul>
     *
     * <p>The original list is not modified; a defensive copy is sorted internally.</p>
     *
     * @return a new {@link BigNumber} representing the median
     * @throws IllegalStateException if this list is empty
     */
    public BigNumber median() {
        if (isEmpty()) {
            throw new IllegalStateException("median requires at least one element, but the list is empty.");
        }

        final List<BigNumber> sorted = new ArrayList<>(values);
        sorted.sort(Comparator.naturalOrder());

        final int size = sorted.size();
        final int middleIndex = size / 2;

        if (size % 2 == 1) {
            return sorted.get(middleIndex);
        }

        final BigNumber lower = sorted.get(middleIndex - 1);
        final BigNumber upper = sorted.get(middleIndex);

        return lower.add(upper).divide(TWO);
    }

    /**
     * Computes all mode values (the most frequently occurring values) of this list.
     *
     * <p>
     * If multiple distinct values share the same highest frequency, all of them are returned.
     * If the list is empty, an empty {@link Set} is returned.
     * </p>
     *
     * @return a {@link Set} of {@link BigNumber} values that occur most frequently
     */
    public Set<BigNumber> modes() {
        if (isEmpty()) {
            return Set.of();
        }

        List<BigNumber> unique = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (BigNumber value : values) {
            int index = -1;
            for (int i = 0; i < unique.size(); i++) {
                if (value.compareTo(unique.get(i)) == 0) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                unique.add(value);
                counts.add(1);
            } else {
                counts.set(index, counts.get(index) + 1);
            }
        }

        int maxCount = 0;
        for (int count : counts) {
            if (count > maxCount) {
                maxCount = count;
            }
        }

        Set<BigNumber> result = new LinkedHashSet<>();
        for (int i = 0; i < unique.size(); i++) {
            if (counts.get(i) == maxCount) {
                result.add(unique.get(i));
            }
        }

        return result;
    }

    /**
     * Returns the smallest {@link BigNumber} in this list according to the natural ordering.
     *
     * @return the minimum value in this list
     * @throws IllegalStateException if this list is empty
     */
    public BigNumber min() {
        if (isEmpty()) {
            throw new IllegalStateException("min requires at least one element, but the list is empty.");
        }

        BigNumber currentMin = values.getFirst();
        for (int i = 1; i < values.size(); i++) {
            final BigNumber candidate = values.get(i);
            if (candidate.isLessThan(currentMin)) {
                currentMin = candidate;
            }
        }

        return currentMin;
    }

    /**
     * Returns the largest {@link BigNumber} in this list according to the natural ordering.
     *
     * @return the maximum value in this list
     * @throws IllegalStateException if this list is empty
     */
    public BigNumber max() {
        if (isEmpty()) {
            throw new IllegalStateException("max requires at least one element, but the list is empty.");
        }

        BigNumber currentMax = values.getFirst();
        for (int i = 1; i < values.size(); i++) {
            final BigNumber candidate = values.get(i);
            if (candidate.isGreaterThan(currentMax)) {
                currentMax = candidate;
            }
        }

        return currentMax;
    }

    /**
     * Computes the numeric range of this list, defined as {@code max() - min()}.
     *
     * @return a new {@link BigNumber} representing {@code max() - min()}
     * @throws IllegalStateException if this list is empty
     */
    public BigNumber range() {
        return max().subtract(min());
    }

    /**
     * Computes the population variance of the values in this list.
     *
     * <p>
     * The population variance is defined as the arithmetic mean of squared deviations
     * from the mean:
     * </p>
     *
     * <pre>
     * variance = Σ (xᵢ - μ)² / N
     * </pre>
     *
     * <p>where {@code μ} is the arithmetic mean and {@code N} the number of elements.</p>
     *
     * @return a new {@link BigNumber} representing the population variance
     * @throws IllegalStateException if this list contains fewer than two elements
     */
    public BigNumber variance() {
        int minSize = 2;
        if (size() < minSize) {
            throw new IllegalStateException("variance requires at least " + minSize + " elements, but the list contains " + size() + ".");
        }

        final BigNumber mean = average();
        final List<BigNumber> squaredDeviations = new ArrayList<>(values.size());

        for (BigNumber value : values) {
            final BigNumber deviation = value.subtract(mean);
            squaredDeviations.add(deviation.multiply(deviation));
        }

        return new BigNumberList(squaredDeviations).average();
    }

    /**
     * Computes the population standard deviation of the values in this list.
     *
     * <p>The standard deviation is the square root of the population variance:
     * {@code sqrt(variance())}.</p>
     *
     * @return a new {@link BigNumber} representing the population standard deviation
     * @throws IllegalStateException if this list contains fewer than two elements
     */
    public BigNumber standardDeviation() {
        return variance().squareRoot();
    }

    /**
     * Computes the geometric mean of the values in this list.
     *
     * <p>
     * The geometric mean is defined as:
     * </p>
     * <pre>
     * geometricMean = (Π xᵢ)^(1/N)
     * </pre>
     *
     * <p>
     * This method requires that all values are greater than or equal to zero. If any value is
     * strictly negative, an {@link IllegalStateException} is thrown. Zero values are permitted
     * and will yield a geometric mean of zero if the product becomes zero.
     * </p>
     *
     * @return a new {@link BigNumber} representing the geometric mean
     * @throws IllegalStateException if this list is empty or contains a negative value
     */
    public BigNumber geometricMean() {
        if (isEmpty()) {
            throw new IllegalStateException("geometricMean requires at least one element, but the list is empty.");
        }

        BigNumber product = values.getFirst();
        if (product.isLessThan(ZERO)) {
            throw new IllegalStateException("Geometric mean is undefined for negative values.");
        }

        for (int i = 1; i < values.size(); i++) {
            final BigNumber value = values.get(i);
            if (value.isLessThan(ZERO)) {
                throw new IllegalStateException("Geometric mean is undefined for negative values.");
            }
            product = product.multiply(value);
        }

        final BigNumber count = new BigNumber(String.valueOf(values.size()));
        return product.nthRoot(count);
    }

    /**
     * Computes the harmonic mean of the values in this list.
     *
     * <p>
     * The harmonic mean is defined as:
     * </p>
     *
     * <pre>
     * harmonicMean = N / Σ (1 / xᵢ)
     * </pre>
     *
     * <p>
     * This method requires that no element is equal to zero, as division by zero would occur.
     * If a zero value is encountered, an {@link ArithmeticException} is thrown.
     * </p>
     *
     * @return a new {@link BigNumber} representing the harmonic mean
     * @throws IllegalStateException if this list is empty
     * @throws ArithmeticException   if any value in the list is zero
     */
    public BigNumber harmonicMean() {
        if (isEmpty()) {
            throw new IllegalStateException("harmonicMean requires at least one element, but the list is empty.");
        }

        BigNumber sumOfReciprocals = null;
        for (BigNumber value : values) {
            if (value.compareTo(ZERO) == 0) {
                throw new ArithmeticException("Harmonic mean is undefined for value 0.");
            }

            final BigNumber reciprocal = ONE.divide(value);
            sumOfReciprocals = (sumOfReciprocals == null) ? reciprocal : sumOfReciprocals.add(reciprocal);
        }

        // TODO: sumOfReciprocals is maybe null: check it
        final BigNumber count = new BigNumber(String.valueOf(values.size()));
        return count.divide(sumOfReciprocals);
    }

    /**
     * Applies {@link BigNumber#abs()} to every element in this list.
     *
     * <p>The transformation is performed in-place and this instance is returned
     * to support fluent usage.</p>
     *
     * @return this {@code BigNumberList} after applying the absolute value to all elements
     */
    public BigNumberList absAll() {
        return transformInPlace(BigNumber::abs, "absAll");
    }

    /**
     * Applies {@link BigNumber#negate()} to every element in this list.
     *
     * <p>The transformation is performed in-place and this instance is returned
     * to support fluent usage.</p>
     *
     * @return this {@code BigNumberList} after negating all elements
     */
    public BigNumberList negateAll() {
        return transformInPlace(BigNumber::negate, "negateAll");
    }

    /**
     * Scales every element in this list by the provided factor.
     *
     * <p>
     * Each value {@code x} is replaced with {@code x * factor}. The transformation is performed
     * in-place and this instance is returned for fluent usage.
     * </p>
     *
     * @param factor the factor by which to multiply each element; must not be {@code null}
     * @return this {@code BigNumberList} after scaling all elements
     * @throws NullPointerException if {@code factor} is {@code null}
     */
    public BigNumberList scale(@NonNull final BigNumber factor) {
        Objects.requireNonNull(factor, "factor must not be null");
        return transformInPlace(value -> value.multiply(factor), "scale");
    }

    /**
     * Translates every element in this list by the provided offset.
     *
     * <p>
     * Each value {@code x} is replaced with {@code x + offset}. The transformation is performed
     * in-place and this instance is returned for fluent usage.
     * </p>
     *
     * @param offset the value to add to each element; must not be {@code null}
     * @return this {@code BigNumberList} after translating all elements
     * @throws NullPointerException if {@code offset} is {@code null}
     */
    public BigNumberList translate(@NonNull final BigNumber offset) {
        Objects.requireNonNull(offset, "offset must not be null");
        return transformInPlace(value -> value.add(offset), "translate");
    }

    /**
     * Raises every element in this list to the given exponent.
     *
     * <p>
     * Each value {@code x} is replaced with {@code x.power(exponent)}. The transformation is
     * performed in-place and this instance is returned for fluent usage.
     * </p>
     *
     * @param exponent the exponent to use for each element; must not be {@code null}
     * @return this {@code BigNumberList} after exponentiation
     * @throws NullPointerException if {@code exponent} is {@code null}
     */
    public BigNumberList powEach(@NonNull final BigNumber exponent) {
        Objects.requireNonNull(exponent, "exponent must not be null");
        return transformInPlace(value -> value.power(exponent), "powEach");
    }

    /**
     * Clamps every element in this list to the given inclusive range {@code [min, max]}.
     *
     * <p>
     * Values smaller than {@code min} are replaced with {@code min}. Values larger than
     * {@code max} are replaced with {@code max}. Values already within the range are
     * left unchanged.
     * </p>
     *
     * @param min the lower bound (inclusive); must not be {@code null}
     * @param max the upper bound (inclusive); must not be {@code null} and must not be less than {@code min}
     * @return this {@code BigNumberList} after clamping all elements
     * @throws NullPointerException     if {@code min} or {@code max} is {@code null}
     * @throws IllegalArgumentException if {@code max} is less than {@code min}
     */
    public BigNumberList clampAll(@NonNull final BigNumber min, @NonNull final BigNumber max) {
        Objects.requireNonNull(min, "min must not be null");
        Objects.requireNonNull(max, "max must not be null");

        if (max.isLessThan(min)) {
            throw new IllegalArgumentException("max must be greater than or equal to min");
        }

        return transformInPlace(value -> {
            if (value.isLessThan(min)) {
                return min;
            }

            if (value.isGreaterThan(max)) {
                return max;
            }

            return value;
        }, "clampAll");
    }

    /**
     * Normalizes all elements in this list so that their sum becomes equal to the given target sum.
     *
     * <p>
     * Let {@code currentSum = sum()}. Each element {@code x} is replaced with
     * {@code x * (targetSum / currentSum)}. If the current sum is zero, the operation is undefined
     * and an {@link IllegalStateException} is thrown.
     * </p>
     *
     * @param targetSum the desired sum after normalization; must not be {@code null}
     * @return this {@code BigNumberList} after normalization
     * @throws NullPointerException  if {@code targetSum} is {@code null}
     * @throws IllegalStateException if the current sum is zero
     */
    public BigNumberList normalizeToSum(@NonNull final BigNumber targetSum) {
        Objects.requireNonNull(targetSum, "targetSum must not be null");

        final BigNumber currentSum = sum();
        if (currentSum.compareTo(ZERO) == 0) {
            throw new IllegalStateException("Cannot normalize list with sum 0.");
        }

        final BigNumber scaleFactor = targetSum.divide(currentSum);
        return transformInPlace(value -> value.multiply(scaleFactor), "normalizeToSum");
    }

    /**
     * Produces a new {@code BigNumberList} where each element is the result of applying
     * the provided operator to the corresponding element of this list.
     *
     * <p>The original list is left unchanged.</p>
     *
     * @param operator the transformation to apply to each element; must not be {@code null}
     * @return a new {@code BigNumberList} containing the transformed elements
     * @throws NullPointerException  if {@code operator} is {@code null}
     * @throws IllegalStateException if the operator returns {@code null} for any element
     */
    public BigNumberList map(@NonNull final UnaryOperator<BigNumber> operator) {
        Objects.requireNonNull(operator, "operator must not be null");
        final List<BigNumber> result = new ArrayList<>(values.size());

        for (BigNumber value : values) {
            final BigNumber transformed = operator.apply(value);
            if (transformed == null) {
                throw new IllegalStateException("map operator must not produce null elements");
            }

            result.add(transformed);
        }

        return new BigNumberList(result);
    }

    /**
     * Reverses the order of elements in this {@code BigNumberList}.
     *
     * <p>The operation is performed by constructing an intermediate {@code BigNumberList},
     * inserting elements at the front, and then replacing the internal storage reference.</p>
     *
     * @return this instance with elements in reversed order
     */
    public BigNumberList reverse() {
        BigNumberList reversedBigNumberList = new BigNumberList();
        for (BigNumber value : values) {
            reversedBigNumberList.addFirst(value);
        }

        values = reversedBigNumberList.getValues();
        return this;
    }

    /**
     * Returns a new {@code BigNumberList} that is a deep structural copy of this list.
     *
     * <p>
     * The internal {@link List} storage is copied into a new {@link ArrayList}. The individual
     * {@link BigNumber} instances are not cloned; references are reused. If element-level
     * independence is required, callers must clone individual {@code BigNumber} objects themselves.
     * </p>
     *
     * @return a new {@code BigNumberList} with its own internal list
     */
    public BigNumberList copy() {
        return new BigNumberList(new ArrayList<>(values));
    }

    /**
     * Returns an immutable {@link List} containing the same {@link BigNumber} elements as this list.
     *
     * <p>
     * The returned list is backed by a new {@link ArrayList} and wrapped with
     * {@link Collections#unmodifiableList(List)}. Modifications to the returned list are not allowed
     * and will result in {@link UnsupportedOperationException}.
     * </p>
     *
     * @return an unmodifiable list containing the same elements as this {@code BigNumberList}
     */
    public List<BigNumber> immutableCopy() {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    /**
     * Randomly shuffles the elements of this list using a new {@link Random} instance.
     *
     * @return this {@code BigNumberList} after shuffling
     */
    public BigNumberList shuffle() {
        Collections.shuffle(values);
        return this;
    }

    /**
     * Randomly shuffles the elements of this list using the provided {@link Random} source.
     *
     * @param random the random source to use; must not be {@code null}
     * @return this {@code BigNumberList} after shuffling
     * @throws NullPointerException if {@code random} is {@code null}
     */
    public BigNumberList shuffle(@NonNull final Random random) {
        Objects.requireNonNull(random, "random must not be null");
        Collections.shuffle(values, random);
        return this;
    }

    /**
     * Rotates the elements in this list by the specified distance.
     *
     * <p>
     * The semantics are identical to {@link Collections#rotate(List, int)}:
     * </p>
     * <ul>
     *   <li>A positive distance moves elements from the end to the front.</li>
     *   <li>A negative distance moves elements from the front to the end.</li>
     * </ul>
     *
     * @param distance the distance to rotate the list
     * @return this {@code BigNumberList} after rotation
     */
    public BigNumberList rotate(final int distance) {
        Collections.rotate(values, distance);
        return this;
    }

    /**
     * Returns a new {@code BigNumberList} containing only distinct elements from this list,
     * preserving their first encountered order.
     *
     * <p>
     * Internally, a {@link LinkedHashSet} is used to track seen elements while preserving
     * insertion order.
     * </p>
     *
     * @return a new {@code BigNumberList} containing distinct elements
     */
    public BigNumberList distinct() {
        List<BigNumber> unique = new ArrayList<>();

        outer:
        for (BigNumber candidate : values) {
            for (BigNumber existing : unique) {
                if (candidate.compareTo(existing) == 0) {
                    continue outer;
                }
            }

            unique.add(candidate);
        }

        return new BigNumberList(unique);
    }

    /**
     * Returns a new {@code BigNumberList} representing the concatenation of this list
     * and the provided {@code other} list.
     *
     * @param other the list to append; must not be {@code null}
     * @return a new {@code BigNumberList} containing all elements of this list followed by all elements of {@code other}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public BigNumberList append(@NonNull final BigNumberList other) {
        Objects.requireNonNull(other, "other must not be null");

        final List<BigNumber> combined = new ArrayList<>(this.values.size() + other.values.size());
        combined.addAll(this.values);
        combined.addAll(other.values);

        return new BigNumberList(combined);
    }

    /**
     * Returns a new {@code BigNumberList} that is a copy of the specified range of this list.
     *
     * <p>
     * This method creates a new {@link ArrayList} backed by a copy of the sublist
     * {@code values[fromIndex, toIndex)}, ensuring that subsequent structural changes do not affect
     * the original list.
     * </p>
     *
     * @param fromIndex low endpoint (inclusive) of the sublist
     * @param toIndex   high endpoint (exclusive) of the sublist
     * @return a new {@code BigNumberList} containing the specified range
     * @throws IndexOutOfBoundsException if the range is invalid
     */
    public BigNumberList subListCopy(final int fromIndex, final int toIndex) {
        return new BigNumberList(new ArrayList<>(values.subList(fromIndex, toIndex)));
    }

    /**
     * Determines whether at least one element in this list matches the given predicate.
     *
     * @param predicate the condition to test; must not be {@code null}
     * @return {@code true} if any element satisfies the predicate, {@code false} otherwise
     * @throws NullPointerException if {@code predicate} is {@code null}
     */
    public boolean anyMatch(@NonNull final Predicate<BigNumber> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");

        for (BigNumber value : values) {
            if (predicate.test(value)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines whether all elements in this list match the given predicate.
     *
     * <p>For an empty list, this method returns {@code true}, matching the semantics of
     * {@link java.util.stream.Stream#allMatch(Predicate)}.</p>
     *
     * @param predicate the condition to test; must not be {@code null}
     * @return {@code true} if all elements satisfy the predicate, or the list is empty; {@code false} otherwise
     * @throws NullPointerException if {@code predicate} is {@code null}
     */
    public boolean allMatch(@NonNull final Predicate<BigNumber> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");

        for (BigNumber value : values) {
            if (!predicate.test(value)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Finds the first element in this list matching the given predicate.
     *
     * @param predicate the condition to test; must not be {@code null}
     * @return an {@link Optional} describing the first matching element, or empty if none match
     * @throws NullPointerException if {@code predicate} is {@code null}
     */
    public Optional<BigNumber> findFirst(@NonNull final Predicate<BigNumber> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");

        for (BigNumber value : values) {
            if (predicate.test(value)) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    /**
     * Creates a new {@code BigNumberList} containing only the elements that match the given predicate.
     *
     * <p>The original list is left unchanged.</p>
     *
     * @param predicate the condition to use for filtering; must not be {@code null}
     * @return a new {@code BigNumberList} containing only elements for which {@code predicate} is {@code true}
     * @throws NullPointerException if {@code predicate} is {@code null}
     */
    public BigNumberList filter(@NonNull final Predicate<BigNumber> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");

        final List<BigNumber> filtered = new ArrayList<>();
        for (BigNumber value : values) {
            if (predicate.test(value)) {
                filtered.add(value);
            }
        }

        return new BigNumberList(filtered);
    }

    /**
     * Checks whether this list is sorted in non-decreasing (ascending) order according to
     * the natural ordering of {@link BigNumber}.
     *
     * @return {@code true} if the list is sorted ascending or contains fewer than two elements, {@code false} otherwise
     */
    public boolean isSortedAscending() {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i).isLessThan(values.get(i - 1))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether this list is sorted in non-increasing (descending) order according to
     * the natural ordering of {@link BigNumber}.
     *
     * @return {@code true} if the list is sorted descending or contains fewer than two elements, {@code false} otherwise
     */
    public boolean isSortedDescending() {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i).isGreaterThan(values.get(i - 1))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether this list is monotonically non-decreasing (each element greater than or equal
     * to the previous one).
     *
     * @return {@code true} if the sequence is monotonically non-decreasing or has fewer than two elements, {@code false} otherwise
     */
    public boolean isMonotonicIncreasing() {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i).isLessThan(values.get(i - 1))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether this list is monotonically non-increasing (each element less than or equal
     * to the previous one).
     *
     * @return {@code true} if the sequence is monotonically non-increasing or has fewer than two elements, {@code false} otherwise
     */
    public boolean isMonotonicDecreasing() {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i).isGreaterThan(values.get(i - 1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an unmodifiable list view containing the same elements as this {@code BigNumberList}.
     *
     * <p>
     * The returned list is backed by a defensive copy, so changes to this {@code BigNumberList}
     * after calling this method do not affect the returned list.
     * </p>
     *
     * @return an unmodifiable list containing the current elements of this {@code BigNumberList}
     */
    public List<BigNumber> toUnmodifiableList() {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    /**
     * Returns an array containing all of the elements in this list in proper sequence
     * (from first to last).
     *
     * <p>This method delegates directly to the internal list's {@link List#toArray()} implementation.</p>
     *
     * @return an array containing all of the elements in this list
     */
    @Override
    public Object[] toArray() {
        return values.toArray();
    }

    /**
     * Returns a {@code BigNumber[]} array containing all the elements in this list in order.
     *
     * <p>This is a strongly-typed convenience method around {@link #toArray(Object[])}.</p>
     *
     * @return a {@code BigNumber[]} containing all elements of this list
     */
    public BigNumber[] toBigNumberArray() {
        return values.toArray(new BigNumber[0]);
    }

    /**
     * Returns a list of {@link String} representations of the elements in this list.
     *
     * <p>Each element's {@link BigNumber#toString()} method is used for conversion.</p>
     *
     * @return a new list containing string representations of each element
     */
    public List<String> toStringList() {
        final List<String> result = new ArrayList<>(values.size());
        for (BigNumber value : values) {
            result.add(value.toString());
        }

        return result;
    }

    /**
     * Returns a primitive {@code double[]} array containing all of the elements in this list,
     * converted via {@link BigNumber#doubleValue()}.
     *
     * <p>
     * This is useful for interoperability with APIs that operate on primitive doubles.
     * Note that {@code BigNumber} values may lose precision during this conversion.
     * </p>
     *
     * @return a {@code double[]} containing the double values of each element
     */
    public double[] toDoubleArray() {
        final double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).doubleValue();
        }

        return result;
    }

    /**
     * Creates and returns a copy of this {@code BigNumberList}.
     *
     * <p>This method shares the underlying list storage with the original instance.
     * For an independent list copy, use {@link #copy()} instead.</p>
     *
     * @return a new {@code BigNumberList} instance referencing the same internal list
     */
    public BigNumberList clone() {
        return new BigNumberList(this);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean contains(@NonNull final Object object) {
        return values.contains(object);
    }

    @Override
    public Iterator<BigNumber> iterator() {
        return values.iterator();
    }

    @Override
    public void forEach(@NonNull final Consumer<? super BigNumber> action) {
        List.super.forEach(action);
    }

    @Override
    public <T> T[] toArray(@NonNull final T[] array) {
        return values.toArray(array);
    }

    @Override
    public <T> T[] toArray(@NonNull final IntFunction<T[]> generator) {
        return List.super.toArray(generator);
    }

    @Override
    public boolean add(@NonNull final BigNumber bigNumber) {
        return values.add(bigNumber);
    }

    @Override
    public boolean remove(@NonNull final Object object) {
        return values.remove(object);
    }

    @Override
    public boolean containsAll(@NonNull final Collection<?> collection) {
        return new HashSet<>(values).containsAll(collection);
    }

    @Override
    public boolean addAll(@NonNull final Collection<? extends BigNumber> collection) {
        return values.addAll(collection);
    }

    @Override
    public boolean addAll(final int index, @NonNull final Collection<? extends BigNumber> collection) {
        return values.addAll(index, collection);
    }

    @Override
    public boolean removeAll(@NonNull final Collection<?> collection) {
        return values.removeAll(collection);
    }

    @Override
    public boolean removeIf(@NonNull final Predicate<? super BigNumber> filter) {
        return List.super.removeIf(filter);
    }

    @Override
    public boolean retainAll(final Collection<?> collection) {
        return values.retainAll(collection);
    }

    @Override
    public void replaceAll(@NonNull final UnaryOperator<BigNumber> operator) {
        List.super.replaceAll(operator);
    }

    @Override
    public void sort(@NonNull final Comparator<? super BigNumber> comparator) {
        List.super.sort(comparator);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public BigNumber get(final int index) {
        return values.get(index);
    }

    @Override
    public BigNumber set(final int index, @NonNull final BigNumber bigNumber) {
        return values.set(index, bigNumber);
    }

    @Override
    public void add(final int index, @NonNull final BigNumber bigNumber) {
        values.add(index, bigNumber);
    }

    @Override
    public BigNumber remove(final int index) {
        return values.remove(index);
    }

    @Override
    public int indexOf(@NonNull final Object object) {
        return values.indexOf(object);
    }

    @Override
    public int lastIndexOf(@NonNull final Object object) {
        return values.lastIndexOf(object);
    }

    @Override
    public ListIterator<BigNumber> listIterator() {
        return values.listIterator();
    }

    @Override
    public ListIterator<BigNumber> listIterator(final int index) {
        return values.listIterator(index);
    }

    @Override
    public List<BigNumber> subList(final int fromIndex, final int toIndex) {
        return values.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<BigNumber> spliterator() {
        return List.super.spliterator();
    }

    @Override
    public Stream<BigNumber> stream() {
        return List.super.stream();
    }

    @Override
    public Stream<BigNumber> parallelStream() {
        return List.super.parallelStream();
    }

    @Override
    public void addFirst(@NonNull final BigNumber bigNumber) {
        List.super.addFirst(bigNumber);
    }

    @Override
    public void addLast(@NonNull final BigNumber bigNumber) {
        List.super.addLast(bigNumber);
    }

    @Override
    public BigNumber getFirst() {
        return List.super.getFirst();
    }

    @Override
    public BigNumber getLast() {
        return List.super.getLast();
    }

    @Override
    public BigNumber removeFirst() {
        return List.super.removeFirst();
    }

    @Override
    public BigNumber removeLast() {
        return List.super.removeLast();
    }

    @Override
    public List<BigNumber> reversed() {
        return List.super.reversed();
    }

    @Override
    public String toString() {
        return values.toString();
    }

    /**
     * Applies the given transformation operator to each element in this list in-place.
     *
     * <p>The operator must not return {@code null} for any element.</p>
     *
     * @param operator      the operator used to transform each element; must not be {@code null}
     * @param operationName a human-readable name of the operation, used for exception messages
     * @return this {@code BigNumberList} instance after transformation
     * @throws NullPointerException  if {@code operator} is {@code null}
     * @throws IllegalStateException if the operator returns {@code null} for any element
     */
    private BigNumberList transformInPlace(@NonNull final UnaryOperator<BigNumber> operator, @NonNull final String operationName) {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(operationName, "operationName must not be null");

        for (int i = 0; i < values.size(); i++) {
            final BigNumber original = values.get(i);
            final BigNumber transformed = operator.apply(original);

            if (transformed == null) {
                throw new IllegalStateException(operationName + " must not produce null elements");
            }

            values.set(i, transformed);
        }

        return this;
    }

}
