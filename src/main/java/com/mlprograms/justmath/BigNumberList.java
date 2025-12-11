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

package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.algorithms.SortingAlgorithm;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Getter
public class BigNumberList implements Comparable<BigNumberList>, List<BigNumber> {

    private List<BigNumber> values;

    public BigNumberList() {
        values = List.of();
    }

    public BigNumberList(@NonNull final List<BigNumber> values) {
        this.values = values;
    }

    public BigNumberList(@NonNull final BigNumberList bigNumberList) {
        this.values = bigNumberList.values;
    }

    public BigNumberList sort(@NonNull final Class<? extends SortingAlgorithm> algorithmClass) {
        try {
            final SortingAlgorithm algorithm = algorithmClass.getDeclaredConstructor().newInstance();

            final List<BigNumber> sorted = algorithm.sort(values);
            if (sorted == null) {
                throw new IllegalStateException("Sorting algorithm returned null");
            }

            return new BigNumberList(sorted);
        } catch (ReflectiveOperationException illegalArgumentException) {
            throw new IllegalArgumentException("Unable to instantiate sorting algorithm: " + algorithmClass.getName(), illegalArgumentException);
        }
    }

    // TODO: reverse()
    public void reverse() {
        BigNumberList reversedBigNumberList = new BigNumberList();
        for (BigNumber value : values) {
            reversedBigNumberList.addFirst(value);
        }

        values = reversedBigNumberList.getValues();
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
