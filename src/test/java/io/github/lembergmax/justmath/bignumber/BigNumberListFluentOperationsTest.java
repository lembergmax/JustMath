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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The operations of {@link BigNumberList} that change the list in place return the list itself, and the operations
 * that create a list leave the receiver and the argument as they are.
 */
class BigNumberListFluentOperationsTest {

    private static final long SHUFFLE_SEED = 20_261_006L;

    private static final int SHUFFLED_SIZE = 30;

    private static BigNumberList listOf(final String... texts) {
        return BigNumberList.fromStrings(List.of(texts));
    }

    private static BigNumber number(final String text) {
        return new BigNumber(text);
    }

    private static BigNumberList sequence(final int size) {
        final List<BigNumber> numbers = new ArrayList<>(size);
        IntStream.range(0, size).forEach(index -> numbers.add(BigNumber.valueOf(index)));
        return new BigNumberList(numbers);
    }

    @Test
    @DisplayName("absAll() takes the absolute value of every element and returns the list")
    void absAllReturnsTheList() {
        final BigNumberList list = listOf("-3", "1", "-2", "4");

        assertSame(list, list.absAll());
        assertEquals(listOf("3", "1", "2", "4"), list);
    }

    @Test
    @DisplayName("negateAll() negates every element and returns the list")
    void negateAllReturnsTheList() {
        final BigNumberList list = listOf("3", "-1", "2");

        assertSame(list, list.negateAll());
        assertEquals(listOf("-3", "1", "-2"), list);
    }

    @Test
    @DisplayName("scale() multiplies every element and returns the list")
    void scaleReturnsTheList() {
        final BigNumberList list = listOf("1", "2", "3");

        assertSame(list, list.scale(number("2")));
        assertEquals(listOf("2", "4", "6"), list);
    }

    @Test
    @DisplayName("translate() adds the offset to every element and returns the list")
    void translateReturnsTheList() {
        final BigNumberList list = listOf("1", "2", "3");

        assertSame(list, list.translate(number("10")));
        assertEquals(listOf("11", "12", "13"), list);
    }

    @Test
    @DisplayName("powEach() raises every element to the exponent and returns the list")
    void powEachReturnsTheList() {
        final BigNumberList list = listOf("1", "2", "3");

        assertSame(list, list.powEach(number("2")));
        assertEquals(listOf("1", "4", "9"), list);
    }

    @Test
    @DisplayName("clampAll() limits every element to the range and returns the list")
    void clampAllReturnsTheList() {
        final BigNumberList list = listOf("1", "2", "3", "5");

        assertSame(list, list.clampAll(number("2"), number("4")));
        assertEquals(listOf("2", "2", "3", "4"), list);
    }

    @Test
    @DisplayName("normalizeToSum() scales the elements to the target sum and returns the list")
    void normalizeToSumReturnsTheList() {
        final BigNumberList list = listOf("1", "2", "2");

        assertSame(list, list.normalizeToSum(number("10")));
        assertEquals(listOf("2", "4", "4"), list);
    }

    @Test
    @DisplayName("reverse() reverses the order and returns the list")
    void reverseReturnsTheList() {
        final BigNumberList list = listOf("1", "2", "3");

        assertSame(list, list.reverse());
        assertEquals(listOf("3", "2", "1"), list);
    }

    @Test
    @DisplayName("rotate() moves elements from the end to the front for a positive distance")
    void rotateReturnsTheList() {
        final BigNumberList list = listOf("1", "2", "3", "4");

        assertSame(list, list.rotate(1));
        assertEquals(listOf("4", "1", "2", "3"), list);
        assertSame(list, list.rotate(-2));
        assertEquals(listOf("2", "3", "4", "1"), list);
    }

    @Test
    @DisplayName("shuffle(Random) gives the order of Collections.shuffle with the same seed and returns the list")
    void shuffleWithRandomFollowsCollectionsShuffle() {
        final BigNumberList list = sequence(SHUFFLED_SIZE);
        final List<BigNumber> reference = new ArrayList<>(list);
        Collections.shuffle(reference, new Random(SHUFFLE_SEED));

        assertSame(list, list.shuffle(new Random(SHUFFLE_SEED)));
        assertEquals(reference, list);
        assertNotEquals(sequence(SHUFFLED_SIZE), list, "the seed must change the order of 30 elements");
    }

    @Test
    @DisplayName("shuffle() keeps the elements and returns the list")
    void shuffleKeepsTheElements() {
        final BigNumberList list = sequence(SHUFFLED_SIZE);

        assertSame(list, list.shuffle());
        assertEquals(SHUFFLED_SIZE, list.size());
        assertTrue(list.containsAll(sequence(SHUFFLED_SIZE)));
    }

    @Test
    @DisplayName("append() concatenates into a new list and leaves both lists as they are")
    void appendConcatenatesIntoANewList() {
        final BigNumberList first = listOf("1");
        final BigNumberList second = listOf("2", "3", "4");

        final BigNumberList combined = first.append(second);

        assertNotSame(first, combined);
        assertEquals(listOf("1", "2", "3", "4"), combined);
        assertEquals(listOf("1"), first);
        assertEquals(listOf("2", "3", "4"), second);
    }

    @Test
    @DisplayName("toListBigNumber() returns a modifiable list that does not write through")
    void toListBigNumberReturnsACopy() {
        final BigNumberList list = listOf("1", "2");

        final List<BigNumber> copy = list.toListBigNumber();
        copy.add(number("3"));

        assertEquals(List.of(number("1"), number("2"), number("3")), copy);
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("isMonotonicIncreasing() accepts equal neighbours and rejects a decrease")
    void monotonicIncreasing() {
        assertTrue(listOf().isMonotonicIncreasing());
        assertTrue(listOf("7").isMonotonicIncreasing());
        assertTrue(listOf("1", "1", "1").isMonotonicIncreasing());
        assertTrue(listOf("1", "2", "2", "3").isMonotonicIncreasing());
        assertFalse(listOf("2", "1").isMonotonicIncreasing());
        assertFalse(listOf("1", "3", "2").isMonotonicIncreasing());
    }

    @Test
    @DisplayName("isMonotonicDecreasing() accepts equal neighbours and rejects an increase")
    void monotonicDecreasing() {
        assertTrue(listOf().isMonotonicDecreasing());
        assertTrue(listOf("7").isMonotonicDecreasing());
        assertTrue(listOf("1", "1", "1").isMonotonicDecreasing());
        assertTrue(listOf("3", "2", "2", "1").isMonotonicDecreasing());
        assertFalse(listOf("1", "2").isMonotonicDecreasing());
        assertFalse(listOf("3", "1", "2").isMonotonicDecreasing());
    }
}
