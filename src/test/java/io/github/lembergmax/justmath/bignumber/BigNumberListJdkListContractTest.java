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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A {@link BigNumberList} is a {@link List}: every operation of the interface must give the same result, the same
 * exception and the same elements afterwards as on an {@link ArrayList}. Each operation runs on both lists, starting
 * from an empty list and from a list with a repeated element.
 */
class BigNumberListJdkListContractTest {

    private static final List<String> SAMPLE = List.of("3", "1", "2", "1", "5");

    private record Outcome(Object value, Class<? extends Throwable> failure) {
    }

    private record Operation(String name, Function<List<BigNumber>, Object> action) {

        @Override
        public String toString() {
            return name;
        }
    }

    private static BigNumber number(final String text) {
        return new BigNumber(text);
    }

    private static List<BigNumber> numbers(final List<String> texts) {
        return texts.stream().map(BigNumberListJdkListContractTest::number).collect(Collectors.toCollection(ArrayList::new));
    }

    private static Operation operation(final String name, final Function<List<BigNumber>, Object> action) {
        return new Operation(name, action);
    }

    private static Outcome run(final Operation operation, final List<BigNumber> list) {
        try {
            return new Outcome(operation.action().apply(list), null);
        } catch (final RuntimeException failure) {
            return new Outcome(null, failure.getClass());
        }
    }

    private static List<BigNumber> drain(final Iterator<BigNumber> iterator) {
        final List<BigNumber> drained = new ArrayList<>();
        iterator.forEachRemaining(drained::add);
        return drained;
    }

    static Stream<Operation> operations() {
        return Stream.of(
                operation("add(element)", list -> list.add(number("9"))),
                operation("add(index, element)", list -> {
                    list.add(2, number("9"));
                    return null;
                }),
                operation("add(index, element) at the end", list -> {
                    list.add(list.size(), number("9"));
                    return null;
                }),
                operation("add(index, element) out of range", list -> {
                    list.add(list.size() + 1, number("9"));
                    return null;
                }),
                operation("addFirst", list -> {
                    list.addFirst(number("9"));
                    return null;
                }),
                operation("addLast", list -> {
                    list.addLast(number("9"));
                    return null;
                }),
                operation("addAll(collection)", list -> list.addAll(List.of(number("7"), number("8")))),
                operation("addAll(empty collection)", list -> list.addAll(List.of())),
                operation("addAll(index, collection)", list -> list.addAll(1, List.of(number("7"), number("8")))),
                operation("addAll(index, empty collection)", list -> list.addAll(1, List.of())),
                operation("addAll(index, collection) out of range", list -> list.addAll(list.size() + 1, List.of(number("7")))),
                operation("remove(object) present", list -> list.remove(number("1"))),
                operation("remove(object) equal in value", list -> list.remove(number("1.0"))),
                operation("remove(object) absent", list -> list.remove(number("42"))),
                operation("remove(null)", list -> list.remove((Object) null)),
                operation("remove(index)", list -> list.remove(1)),
                operation("remove(index) out of range", list -> list.remove(list.size())),
                operation("removeFirst", List::removeFirst),
                operation("removeLast", List::removeLast),
                operation("removeAll with a match", list -> list.removeAll(List.of(number("1"), number("5")))),
                operation("removeAll without a match", list -> list.removeAll(List.of(number("42")))),
                operation("retainAll with a change", list -> list.retainAll(List.of(number("1"), number("5")))),
                operation("retainAll without a change", list -> list.retainAll(numbers(SAMPLE))),
                operation("removeIf with a match", list -> list.removeIf(element -> element.isGreaterThan(number("2")))),
                operation("removeIf without a match", list -> list.removeIf(element -> element.isGreaterThan(number("100")))),
                operation("replaceAll", list -> {
                    list.replaceAll(element -> element.add(number("1")));
                    return null;
                }),
                operation("sort", list -> {
                    list.sort(Comparator.naturalOrder());
                    return null;
                }),
                operation("sort descending", list -> {
                    list.sort(Comparator.<BigNumber>naturalOrder().reversed());
                    return null;
                }),
                operation("clear", list -> {
                    list.clear();
                    return null;
                }),
                operation("set", list -> list.set(1, number("8"))),
                operation("set out of range", list -> list.set(list.size(), number("8"))),
                operation("get", list -> list.get(3)),
                operation("get out of range", list -> list.get(list.size())),
                operation("getFirst", List::getFirst),
                operation("getLast", List::getLast),
                operation("indexOf present", list -> list.indexOf(number("1"))),
                operation("indexOf absent", list -> list.indexOf(number("42"))),
                operation("indexOf(null)", list -> list.indexOf(null)),
                operation("lastIndexOf present", list -> list.lastIndexOf(number("1"))),
                operation("lastIndexOf absent", list -> list.lastIndexOf(number("42"))),
                operation("contains present", list -> list.contains(number("2"))),
                operation("contains absent", list -> list.contains(number("42"))),
                operation("contains(null)", list -> list.contains(null)),
                operation("containsAll present", list -> list.containsAll(List.of(number("1"), number("5")))),
                operation("containsAll with an absent element", list -> list.containsAll(List.of(number("1"), number("42")))),
                operation("containsAll(empty)", list -> list.containsAll(List.of())),
                operation("size", List::size),
                operation("isEmpty", List::isEmpty),
                operation("hashCode", List::hashCode),
                operation("toString", List::toString),
                operation("toArray()", list -> Arrays.asList(list.toArray())),
                operation("toArray(array)", list -> Arrays.asList(list.toArray(new BigNumber[0]))),
                operation("toArray(generator)", list -> Arrays.asList(list.toArray(BigNumber[]::new))),
                operation("iterator", list -> drain(list.iterator())),
                operation("listIterator", list -> drain(list.listIterator())),
                operation("listIterator(index)", list -> drain(list.listIterator(2))),
                operation("listIterator(index) out of range", list -> drain(list.listIterator(list.size() + 1))),
                operation("stream", list -> list.stream().map(BigNumber::toString).toList()),
                operation("parallelStream", list -> list.parallelStream().map(BigNumber::toString).toList()),
                operation("spliterator", list -> StreamSupport.stream(list.spliterator(), false).map(BigNumber::toString).toList()),
                operation("forEach", list -> {
                    final List<BigNumber> visited = new ArrayList<>();
                    list.forEach(visited::add);
                    return visited;
                }),
                operation("reversed", list -> new ArrayList<>(list.reversed())),
                operation("reversed view writes through", list -> {
                    list.reversed().add(number("9"));
                    return new ArrayList<>(list);
                }),
                operation("subList", list -> new ArrayList<>(list.subList(1, 3))),
                operation("subList writes through", list -> {
                    list.subList(1, 3).clear();
                    return new ArrayList<>(list);
                }),
                operation("subList out of range", list -> list.subList(1, list.size() + 1)));
    }

    static Stream<Arguments> operationsOnBothStartingLists() {
        return operations().flatMap(operation -> Stream.of(
                Arguments.of(operation, "an empty list", List.<String>of()),
                Arguments.of(operation, "a list with a repeated element", SAMPLE)));
    }

    @ParameterizedTest(name = "{0} on {1}")
    @MethodSource("operationsOnBothStartingLists")
    @DisplayName("every List operation behaves like the one of an ArrayList")
    void behavesLikeAnArrayList(final Operation operation, final String description, final List<String> start) {
        final List<BigNumber> reference = numbers(start);
        final BigNumberList subject = new BigNumberList(numbers(start));

        final Outcome expected = run(operation, reference);
        final Outcome actual = run(operation, subject);

        assertEquals(expected, actual, "result of " + operation + " on " + description);
        assertEquals(reference, subject, "elements after " + operation + " on " + description);
    }
}
