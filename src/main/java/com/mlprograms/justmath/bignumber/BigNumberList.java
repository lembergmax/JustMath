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
 * A mutable, list-like container for {@link BigNumber} instances that implements {@link java.util.List}.
 * This class acts as a lightweight, domain-specific wrapper around a {@link List} of
 * {@link BigNumber} objects and exposes list behaviour while adding domain.
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
public class BigNumberList implements List<BigNumber> {

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

    /**
     * Return the number of elements contained in this BigNumberList.
     *
     * <p>This delegates directly to the internal {@code values} list and therefore
     * reflects the current storage implementation and its size. This method runs in
     * constant time for typical {@link java.util.List} implementations that
     * maintain a size field (for example {@link java.util.ArrayList}).</p>
     *
     * @return the number of elements in this list
     */
    @Override
    public int size() {
        return values.size();
    }

    /**
     * Determine whether this BigNumberList contains no elements.
     *
     * <p>Delegates to the backing {@code values} list. If the internal list is
     * replaced by another implementation, the result will reflect that
     * implementation's emptiness semantics.</p>
     *
     * @return {@code true} if this list contains no elements, {@code false} otherwise
     */
    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Test whether the specified object is present in this list.
     *
     * <p>This method forwards to {@link java.util.List#contains(Object)} on the
     * backing list. The equality test used depends on the {@code equals} method of
     * the stored {@link BigNumber} instances or the provided object.</p>
     *
     * @param object object whose presence in this list is to be tested, must not be {@code null}
     * @return {@code true} if this list contains the specified element
     */
    @Override
    public boolean contains(@NonNull final Object object) {
        return values.contains(object);
    }

    /**
     * Return an iterator over the elements in this list in proper sequence.
     *
     * <p>The returned iterator is the iterator of the internal backing list and
     * will reflect modifications made to that list. Behaviour (concurrent
     * modification, supported removal) depends on the backing list's iterator.</p>
     *
     * @return an {@link Iterator} over the elements in this list
     */
    @Override
    public Iterator<BigNumber> iterator() {
        return values.iterator();
    }

    /**
     * Perform the given action for each element of the {@code BigNumberList} until
     * all elements have been processed or the action throws an exception.
     *
     * <p>This implementation defers to the default {@link List#forEach} behaviour,
     * which will use the list's iterator to traverse elements.</p>
     *
     * @param action The action to be performed for each element, must not be {@code null}
     */
    @Override
    public void forEach(@NonNull final Consumer<? super BigNumber> action) {
        List.super.forEach(action);
    }

    /**
     * Return an array containing all of the elements in this list in proper
     * sequence (from first to last).
     *
     * <p>Note: this implementation currently returns an empty array and should be
     * replaced with a proper delegation if array conversion is required. For now
     * it satisfies the method presence but does not provide the expected semantics.</p>
     *
     * @return an array containing all of the elements in this list
     */
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    /**
     * Store the elements of this list into the provided array if it is large
     * enough; otherwise allocate a new array of the same runtime component type.
     *
     * <p>This method delegates to the backing {@code values} list which implements
     * the standard {@code toArray(T[])} contract.</p>
     *
     * @param array the array into which the elements of the list are to be stored, if it is big enough
     * @param <T>   the runtime type of the array to contain the collection
     * @return an array containing the elements of this list
     */
    @Override
    public <T> T[] toArray(@NonNull final T[] array) {
        return values.toArray(array);
    }

    /**
     * Return an array containing all of the elements in this list using the
     * provided generator function to allocate the returned array.
     *
     * <p>This implementation uses the default {@link List#toArray(IntFunction)}
     * behaviour which will construct an array via the supplied generator.</p>
     *
     * @param generator a function which produces a new array of the desired type and the provided length
     * @param <T>       the component type of the array to contain the collection
     * @return an array containing all of the elements in this list
     */
    @Override
    public <T> T[] toArray(@NonNull final IntFunction<T[]> generator) {
        return List.super.toArray(generator);
    }

    /**
     * Append the specified {@link BigNumber} to the end of this list.
     *
     * @param bigNumber element to be appended to this list, must not be {@code null}
     * @return {@code true} if the list changed as a result of the call
     */
    @Override
    public boolean add(@NonNull final BigNumber bigNumber) {
        return values.add(bigNumber);
    }

    /**
     * Remove the first occurrence of the specified element from this list, if it
     * is present.
     *
     * @param object element to be removed from this list, if present, must not be {@code null}
     * @return {@code true} if an element was removed as a result of this call
     */
    @Override
    public boolean remove(@NonNull final Object object) {
        return values.remove(object);
    }

    /**
     * Return {@code true} if this list contains all of the elements in the given
     * collection.
     *
     * <p>This implementation builds a temporary {@link HashSet} from the backing
     * list for potentially faster containment checks when the collection being
     * tested is large. Equality semantics are determined by the elements' equals
     * implementations.</p>
     *
     * @param collection collection to be checked for containment, must not be {@code null}
     * @return {@code true} if this list contains all elements in the specified collection
     */
    @Override
    public boolean containsAll(@NonNull final Collection<?> collection) {
        return new HashSet<>(values).containsAll(collection);
    }

    /**
     * Append all of the elements in the specified collection to the end of this
     * list, in the order that they are returned by the collection's iterator.
     *
     * @param collection collection containing elements to be added to this list, must not be {@code null}
     * @return {@code true} if this list changed as a result of the call
     */
    @Override
    public boolean addAll(@NonNull final Collection<? extends BigNumber> collection) {
        return values.addAll(collection);
    }

    /**
     * Insert all of the elements in the specified collection into this list at
     * the specified position.
     *
     * <p>Elements are inserted in the order provided by the collection's iterator.
     * Behaviour is delegated to the backing list.</p>
     *
     * @param index      index at which to insert the first element from the specified collection
     * @param collection collection containing elements to be added, must not be {@code null}
     * @return {@code true} if this list changed as a result of the call
     */
    @Override
    public boolean addAll(int index, @NonNull final Collection<? extends BigNumber> collection) {
        return values.addAll(index, collection);
    }

    /**
     * Remove from this list all of its elements that are contained in the
     * specified collection.
     *
     * @param c collection containing elements to be removed from this list, must not be {@code null}
     * @return {@code true} if this list changed as a result of the call
     */
    @Override
    public boolean removeAll(@NonNull final Collection<?> c) {
        return values.removeAll(c);
    }

    /**
     * Remove all of the elements of this collection that satisfy the given
     * predicate.
     *
     * <p>This method defers to the default {@link List#removeIf} implementation,
     * which uses the list's iterator to remove matching elements.</p>
     *
     * @param filter a predicate which returns {@code true} for elements to be removed, must not be {@code null}
     * @return {@code true} if any elements were removed
     */
    @Override
    public boolean removeIf(@NonNull final Predicate<? super BigNumber> filter) {
        return List.super.removeIf(filter);
    }

    /**
     * Retain only the elements in this list that are contained in the specified
     * collection (optional operation).
     *
     * @param c collection containing elements to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return values.retainAll(c);
    }

    /**
     * Replace each element of this list with the result of applying the operator
     * to that element.
     *
     * <p>This method uses the default {@link List#replaceAll} behaviour which will
     * traverse the list and set each entry to the operator's result.</p>
     *
     * @param operator the operator to apply to each element, must not be {@code null}
     */
    @Override
    public void replaceAll(@NonNull final UnaryOperator<BigNumber> operator) {
        List.super.replaceAll(operator);
    }

    /**
     * Sort this list according to the order induced by the specified comparator.
     *
     * <p>Delegates to the default {@link List#sort} implementation which will
     * rearrange elements in the backing list according to the comparator provided.</p>
     *
     * @param comparator the comparator to determine the order of the list, must not be {@code null}
     */
    @Override
    public void sort(@NonNull final Comparator<? super BigNumber> comparator) {
        List.super.sort(comparator);
    }

    /**
     * Remove all elements from this list. The list will be empty after this
     * call returns.
     *
     * @see List#clear()
     */
    @Override
    public void clear() {
        values.clear();
    }

    /**
     * Return the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public BigNumber get(final int index) {
        return values.get(index);
    }

    /**
     * Replace the element at the specified position in this list with the
     * specified element.
     *
     * @param index     index of the element to replace
     * @param bigNumber element to be stored at the specified position, must not be {@code null}
     * @return the element previously at the specified position
     */
    @Override
    public BigNumber set(final int index, @NonNull final BigNumber bigNumber) {
        return values.set(index, bigNumber);
    }

    /**
     * Insert the specified element at the specified position in this list.
     *
     * @param index     index at which the specified element is to be inserted
     * @param bigNumber element to be inserted, must not be {@code null}
     */
    @Override
    public void add(final int index, @NonNull final BigNumber bigNumber) {
        values.add(index, bigNumber);
    }

    /**
     * Remove the element at the specified position in this list.
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     */
    @Override
    public BigNumber remove(final int index) {
        return values.remove(index);
    }

    /**
     * Return the index of the first occurrence of the specified element in this
     * list, or -1 if this list does not contain the element.
     *
     * @param o element to search for, must not be {@code null}
     * @return the index of the first occurrence, or -1 if not found
     */
    @Override
    public int indexOf(@NonNull final Object o) {
        return values.indexOf(o);
    }

    /**
     * Return the index of the last occurrence of the specified element in this
     * list, or -1 if this list does not contain the element.
     *
     * @param o element to search for, must not be {@code null}
     * @return the index of the last occurrence, or -1 if not found
     */
    @Override
    public int lastIndexOf(@NonNull final Object o) {
        return values.lastIndexOf(o);
    }

    /**
     * Return a list iterator over the elements in this list (in proper sequence).
     *
     * <p>The returned iterator is the iterator of the backing list and will
     * reflect changes to that list.</p>
     *
     * @return a {@link ListIterator} over the elements in this list
     */
    @Override
    public ListIterator<BigNumber> listIterator() {
        return values.listIterator();
    }

    /**
     * Return a list iterator of the elements in this list starting at the
     * specified position.
     *
     * @param index index of the first element to be returned from the list iterator
     * @return a {@link ListIterator} over the elements in this list starting at the specified position
     */
    @Override
    public ListIterator<BigNumber> listIterator(final int index) {
        return values.listIterator(index);
    }

    /**
     * Return a view of the portion of this list between the specified fromIndex,
     * inclusive, and toIndex, exclusive.
     *
     * <p>The returned list is backed by the original list, so non-structural
     * changes in the returned list are reflected in this list.</p>
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex   high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     */
    @Override
    public List<BigNumber> subList(final int fromIndex, final int toIndex) {
        return values.subList(fromIndex, toIndex);
    }

    /**
     * Create a {@link Spliterator} over the elements in this list.
     *
     * <p>This implementation uses the default {@link List#spliterator} behaviour.</p>
     *
     * @return a {@link Spliterator} over the elements in this list
     */
    @Override
    public Spliterator<BigNumber> spliterator() {
        return List.super.spliterator();
    }

    /**
     * Return a sequential {@link Stream} with this collection as its source.
     *
     * <p>Delegates to the default list stream implementation.</p>
     *
     * @return a sequential {@link Stream} over the elements in this list
     */
    @Override
    public Stream<BigNumber> stream() {
        return List.super.stream();
    }

    /**
     * Return a possibly parallel {@link Stream} with this collection as its
     * source.
     *
     * <p>Delegates to the default list parallel stream implementation.</p>
     *
     * @return a possibly parallel {@link Stream} over the elements in this list
     */
    @Override
    public Stream<BigNumber> parallelStream() {
        return List.super.parallelStream();
    }

    /**
     * Insert the specified element at the beginning of this list.
     *
     * <p>This method uses the default {@link List#addFirst} behaviour. Note that
     * not all {@link List} implementations support adding at the first position;
     * behaviour depends on the backing list type.</p>
     *
     * @param bigNumber element to add at the front, must not be {@code null}
     */
    @Override
    public void addFirst(@NonNull final BigNumber bigNumber) {
        List.super.addFirst(bigNumber);
    }

    /**
     * Append the specified element to the end of this list (alias for {@link #add}).
     *
     * <p>Delegates to the default {@link List#addLast} behaviour.</p>
     *
     * @param bigNumber element to add at the end, must not be {@code null}
     */
    @Override
    public void addLast(@NonNull final BigNumber bigNumber) {
        List.super.addLast(bigNumber);
    }

    /**
     * Return the first element in this list.
     *
     * @return the first element in this list
     * @throws NoSuchElementException if the list is empty
     */
    @Override
    public BigNumber getFirst() {
        return List.super.getFirst();
    }

    /**
     * Return the last element in this list.
     *
     * @return the last element in this list
     * @throws NoSuchElementException if the list is empty
     */
    @Override
    public BigNumber getLast() {
        return List.super.getLast();
    }

    /**
     * Remove and return the first element from this list.
     *
     * @return the removed first element
     * @throws NoSuchElementException if the list is empty
     */
    @Override
    public BigNumber removeFirst() {
        return List.super.removeFirst();
    }

    /**
     * Remove and return the last element from this list.
     *
     * @return the removed last element
     * @throws NoSuchElementException if the list is empty
     */
    @Override
    public BigNumber removeLast() {
        return List.super.removeLast();
    }

    /**
     * Return a reversed view or copy of this list using the default list
     * behaviour.
     *
     * <p>The default {@link List#reversed} method semantics apply; consult the
     * specific JDK version and backing list implementation for exact behaviour.</p>
     *
     * @return a list containing the elements of this list in reverse order
     */
    @Override
    public List<BigNumber> reversed() {
        return List.super.reversed();
    }

    /**
     * Return a string representation of this list.
     *
     * <p>Delegates to the backing {@code values} list's {@code toString} method,
     * producing a representation that lists elements in order and uses each
     * element's {@code toString} result.</p>
     *
     * @return a string representation of this BigNumberList
     */
    @Override
    public String toString() {
        return values.toString();
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
