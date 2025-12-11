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

/**
 * BigNumberList
 *
 * <p>
 * A mutable, list-like container for {@link BigNumber} instances that implements
 * both {@link java.lang.Comparable} (comparing lists as a whole) and {@link java.util.List}.
 * This class acts as a lightweight, domain-specific wrapper around a {@link List} of
 * {@link BigNumber} objects and exposes list behaviour while adding domain operations
 * such as {@link #sort(Class)} and {@link #reverse()}.
 * </p>
 * <p><b>Usage notes</b></p>
 * <ul>
 *   <li>Most {@link java.util.List} methods are delegated to the internal {@code values} list
 *       or forwarded to {@code List.super} where the default interface behaviour is
 *       appropriate.</li>
 *   <li>Because several {@code List} default methods are referenced (via {@code List.super}),
 *       the concrete runtime type of {@code values} determines actual behaviour for some
 *       operations (for example immutability or supported optional operations).</li>
 * </ul>
 */
@Getter
public class BigNumberList implements Comparable<BigNumberList>, List<BigNumber> {

    /**
     * Internal storage for the elements of this BigNumberList.
     * This list holds the sequence of {@link BigNumber} instances managed by this class.
     * It is never intended to be null; constructors initialize it to a concrete list.
     * Note: the list reference may be replaced by operations such as {@link #sort(Class)} or {@link #reverse()}.
     */
    private List<BigNumber> values;

    /**
     * Create an empty BigNumberList.
     * <p>
     * The created instance uses an empty list as its internal storage. The list returned by
     * {@link List#of()} is immutable; some mutating operations on this instance will replace
     * the internal reference with a mutable list when necessary.
     */
    public BigNumberList() {
        values = List.of();
    }

    /**
     * Create a BigNumberList backed by the provided list reference.
     * <p>
     * The provided list becomes the internal storage for this instance. No defensive copy
     * is made, so callers should not mutate the list externally if independent ownership is required.
     *
     * @param values the list to use as internal storage, must not be null
     */
    public BigNumberList(@NonNull final List<BigNumber> values) {
        this.values = values;
    }

    /**
     * Create a new BigNumberList that shares the internal storage of the provided instance.
     * <p>
     * This constructor performs a shallow copy of the reference to the internal list: both
     * instances will refer to the same list object. Use {@link #clone()} to obtain a semantic copy
     * if independent lists are required.
     *
     * @param bigNumberList the instance whose internal storage will be referenced, must not be null
     */
    public BigNumberList(@NonNull final BigNumberList bigNumberList) {
        this.values = bigNumberList.values;
    }

    /**
     * Sort the elements of this list using the given sorting algorithm implementation.
     * <p>
     * The method instantiates the provided {@code algorithmClass} using its no-argument constructor,
     * delegates the sorting to the algorithm's {@code sort(List<BigNumber>)} method and replaces the
     * internal storage with the returned list. The method mutates this instance and returns {@code this}
     * to allow fluent calls.
     * <p>
     * The sorting algorithm is free to return a new list or mutate and return the same list. If the
     * algorithm returns {@code null}, an {@link IllegalStateException} is thrown.
     *
     * @param algorithmClass implementation of {@link SortingAlgorithm} to use for sorting, must not be null
     * @return this instance after sorting
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
        } catch (ReflectiveOperationException illegalArgumentException) {
            throw new IllegalArgumentException("Unable to instantiate sorting algorithm: " + algorithmClass.getName(), illegalArgumentException);
        }
    }

    /**
     * Reverse the order of elements in this BigNumberList.
     * <p>
     * The method constructs a new temporary BigNumberList and inserts each value from the current
     * internal list at the front of the temporary list using {@link #addFirst(BigNumber)}. After
     * iteration, the internal storage is replaced with the reversed list's storage. The operation
     * mutates this instance and returns {@code this} to allow fluent usage.
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
    public Object[] toArray() {
        return new Object[0];
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
    public boolean addAll(int index, @NonNull final Collection<? extends BigNumber> collection) {
        return values.addAll(index, collection);
    }

    @Override
    public boolean removeAll(@NonNull final Collection<?> c) {
        return values.removeAll(c);
    }

    @Override
    public boolean removeIf(@NonNull final Predicate<? super BigNumber> filter) {
        return List.super.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return values.retainAll(c);
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
    public int indexOf(@NonNull final Object o) {
        return values.indexOf(o);
    }

    @Override
    public int lastIndexOf(@NonNull final Object o) {
        return values.lastIndexOf(o);
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
