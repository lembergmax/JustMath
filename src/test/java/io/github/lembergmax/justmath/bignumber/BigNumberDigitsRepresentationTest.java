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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A {@link BigNumber} that is built from a {@link BigDecimal}, and so is every result of a calculation, writes its
 * digits down only when somebody asks for them. The digits, the sign, the value and its scale must be the same as for
 * a number that was built from the text, whatever the order of the calls.
 */
class BigNumberDigitsRepresentationTest {

    private static final int READER_THREADS = 8;

    private static final int RACE_ROUNDS = 200;

    private static BigNumber fromDecimal(final String value) {
        return new BigNumber(new BigDecimal(value), Locale.US);
    }

    private static void assertDigits(final BigNumber number, final String integerDigits, final String fractionDigits, final boolean negative) {
        assertEquals(integerDigits, number.getValueBeforeDecimalPoint(), "digits before the decimal point");
        assertEquals(fractionDigits, number.getValueAfterDecimalPoint(), "digits after the decimal point");
        assertEquals(negative, number.isNegative(), "sign flag");
    }

    static Stream<Arguments> numbersBuiltFromADecimal() {
        return Stream.of(
                Arguments.of("1.50", "1", "50", false, 2, "1.5"),
                Arguments.of("1E+3", "1000", "0", false, 0, "1000"),
                Arguments.of("-0.25", "0", "25", true, 2, "-0.25"),
                Arguments.of("0.000", "0", "000", false, 3, "0"),
                Arguments.of("-7.250", "7", "250", true, 3, "-7.25"),
                Arguments.of("12345678901234567890.0001", "12345678901234567890", "0001", false, 4, "12345678901234567890.0001"));
    }

    static Stream<Arguments> trimmedNumbers() {
        return Stream.of(
                Arguments.of("1.50", "1", "5", 1, "1.5"),
                Arguments.of("3.00", "3", "", 0, "3"),
                Arguments.of("0.000", "0", "", 0, "0"),
                Arguments.of("100", "100", "", 0, "100"),
                Arguments.of("100.100", "100", "1", 1, "100.1"),
                Arguments.of("-2.50", "2", "5", 1, "-2.5"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("numbersBuiltFromADecimal")
    @DisplayName("a number built from a BigDecimal has the digits, the value and the scale of its plain string")
    void numberBuiltFromADecimal(final String value, final String integerDigits, final String fractionDigits, final boolean negative, final int scale, final String text) {
        final BigNumber fromDecimal = fromDecimal(value);
        final BigNumber fromText = new BigNumber(new BigDecimal(value).toPlainString(), Locale.US);

        assertEquals(scale, fromDecimal.toBigDecimal().scale());
        assertDigits(fromDecimal, integerDigits, fractionDigits, negative);
        assertEquals(text, fromDecimal.toString());
        assertEquals(fromText.toBigDecimal().scale(), fromDecimal.toBigDecimal().scale());
        assertEquals(fromText, fromDecimal);
        assertEquals(fromText.hashCode(), fromDecimal.hashCode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("numbersBuiltFromADecimal")
    @DisplayName("asking for the value first and for the digits afterwards gives the same digits")
    void valueBeforeDigits(final String value, final String integerDigits, final String fractionDigits, final boolean negative, final int scale, final String text) {
        final BigNumber number = fromDecimal(value);

        assertEquals(0, new BigDecimal(value).compareTo(number.toBigDecimal()));
        assertDigits(number, integerDigits, fractionDigits, negative);
        assertEquals(scale, number.toBigDecimal().scale());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("trimmedNumbers")
    @DisplayName("trim() leaves the digits and the scale of the trimmed text, whether the digits were written down or not")
    void trimLeavesTheDigitsOfTheTrimmedText(final String value, final String integerDigits, final String fractionDigits, final int scale, final String text) {
        final boolean negative = value.startsWith("-");
        final BigNumber lazy = fromDecimal(value).trim();
        final BigNumber written = fromDecimal(value);
        written.getValueBeforeDecimalPoint();
        written.trim();
        final BigNumber fromText = new BigNumber(new BigDecimal(value).toPlainString(), Locale.US).trim();

        for (final BigNumber number : List.of(lazy, written, fromText)) {
            assertDigits(number, integerDigits, fractionDigits, negative);
            assertEquals(scale, number.toBigDecimal().scale());
            assertEquals(text, number.toString());
        }
    }

    @Test
    @DisplayName("negateThis() flips the sign flag of zero as it always did, and twice gives the original back")
    void negateThisOfZero() {
        final BigNumber zero = fromDecimal("0");

        assertSame(zero, zero.negateThis());
        assertDigits(zero, "0", "0", true);
        assertEquals("-0", zero.toString());
        zero.negateThis();
        assertDigits(zero, "0", "0", false);
        assertEquals("0", zero.toString());
    }

    @Test
    @DisplayName("negateThis(), negate() and abs() keep the digits and the scale")
    void negationKeepsTheDigits() {
        final BigNumber negated = fromDecimal("2.50").negateThis();
        final BigNumber negatedCopy = fromDecimal("2.50").negate();
        final BigNumber absolute = fromDecimal("-2.50").abs();

        assertDigits(negated, "2", "50", true);
        assertDigits(negatedCopy, "2", "50", true);
        assertDigits(absolute, "2", "50", false);
        assertEquals(2, negated.toBigDecimal().scale());
        assertEquals(2, negatedCopy.toBigDecimal().scale());
        assertEquals(2, absolute.toBigDecimal().scale());
        assertEquals("-2.5", negated.toString());
        assertEquals("2.5", absolute.toString());
    }

    @Test
    @DisplayName("abs() of a negative zero is a plain zero")
    void absOfNegativeZero() {
        final BigNumber absolute = new BigNumber("-0").abs();

        assertDigits(absolute, "0", "0", false);
        assertEquals("0", absolute.toString());
    }

    @Test
    @DisplayName("a clone does not see a change of the original and the other way round")
    void cloneIsIndependent() {
        final BigNumber original = fromDecimal("12.50");
        final BigNumber copy = original.clone();

        copy.negateThis();
        original.trim();

        assertNotSame(original, copy);
        assertDigits(original, "12", "5", false);
        assertDigits(copy, "12", "50", true);
        assertEquals("12.5", original.toString());
        assertEquals("-12.5", copy.toString());
    }

    @Test
    @DisplayName("changing the digits discards the value that was calculated from them")
    void changeOfTheDigitsDiscardsTheValue() {
        final BigNumber number = new BigNumber("1.05");
        assertEquals(new BigDecimal("1.05"), number.toBigDecimal());

        number.trimLeadingZerosAfterDecimalPoint();

        assertEquals(new BigDecimal("1.5"), number.toBigDecimal());
        assertDigits(number, "1", "5", false);
    }

    @Test
    @DisplayName("trimming the digits discards the cached value and keeps the number")
    void trimDiscardsTheValue() {
        final BigNumber number = new BigNumber("1.50");
        assertEquals(2, number.toBigDecimal().scale());

        number.trim();

        assertEquals(1, number.toBigDecimal().scale());
        assertDigits(number, "1", "5", false);
    }

    @Test
    @DisplayName("a copy of a number whose digits were not written down yet has the same digits")
    void copyOfALazyNumber() {
        final BigNumber lazy = fromDecimal("-3.140");

        final BigNumber copy = new BigNumber(lazy);
        final BigNumber withContext = new BigNumber(lazy, Locale.GERMANY, new MathContext(5, RoundingMode.DOWN));

        assertDigits(copy, "3", "140", true);
        assertDigits(withContext, "3", "140", true);
        assertEquals("-3,14", withContext.toString());
        assertEquals(new MathContext(5, RoundingMode.DOWN), withContext.getMathContext());
        assertEquals(Locale.GERMANY, withContext.getLocale());
    }

    @Test
    @DisplayName("a serialized number keeps its digits, also when they had not been written down")
    void serializationKeepsTheDigits() throws IOException, ClassNotFoundException {
        final BigNumber lazy = fromDecimal("-7.250");
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream stream = new ObjectOutputStream(bytes)) {
            stream.writeObject(lazy);
        }

        final BigNumber restored;
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (BigNumber) stream.readObject();
        }

        assertDigits(restored, "7", "250", true);
        assertEquals(3, restored.toBigDecimal().scale());
        assertEquals(lazy, restored);
        assertEquals("-7.25", restored.toString());
    }

    @Test
    @DisplayName("the results of the arithmetic have trimmed digits and the context that the operation gives them")
    void resultsOfTheArithmetic() {
        final MathContext tenDigits = new MathContext(10, RoundingMode.HALF_UP);

        final BigNumber sum = new BigNumber("1.5").add(new BigNumber("1.5"));
        final BigNumber difference = new BigNumber("2.5").subtract(new BigNumber("2.5"));
        final BigNumber product = new BigNumber("-0").multiply(new BigNumber("5"));
        final BigNumber quotient = new BigNumber("1").divide(new BigNumber("3"), tenDigits);
        final BigNumber power = new BigNumber("2").power(new BigNumber("10"), tenDigits);
        final BigNumber factorial = new BigNumber("5").factorial(new MathContext(20));

        assertDigits(sum, "3", "", false);
        assertDigits(difference, "0", "", false);
        assertDigits(product, "0", "", false);
        assertDigits(quotient, "0", "3333333333", false);
        assertDigits(power, "1024", "", false);
        assertDigits(factorial, "120", "", false);
        assertEquals(BigNumbers.DEFAULT_MATH_CONTEXT, sum.getMathContext());
        assertEquals(tenDigits, quotient.getMathContext());
        assertEquals(tenDigits, power.getMathContext());
        assertEquals(new MathContext(20), factorial.getMathContext());
    }

    @Test
    @DisplayName("a result of the arithmetic has the locale that was asked for")
    void resultHasTheLocale() {
        final BigNumber german = new BigNumber("1,5", Locale.GERMANY).add(new BigNumber("1,25", Locale.GERMANY));

        assertEquals(Locale.GERMANY, german.getLocale());
        assertEquals("2,75", german.toString());
        assertEquals("2.75", german.toString(Locale.US));
    }

    @Test
    @DisplayName("signum() follows the value")
    void signumFollowsTheValue() {
        assertEquals(-1, fromDecimal("-0.001").signum());
        assertEquals(0, fromDecimal("0.000").signum());
        assertEquals(1, fromDecimal("1E+5").signum());
        assertTrue(fromDecimal("3").isPositive());
        assertFalse(fromDecimal("-3").isPositive());
    }

    @Test
    @DisplayName("threads that read the digits of a shared number at the same time all see the same digits")
    void concurrentReadersSeeConsistentDigits() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(READER_THREADS);
        try {
            for (int round = 0; round < RACE_ROUNDS; round++) {
                final BigNumber shared = new BigNumber(new BigDecimal("-12345.678900"), Locale.US);
                final CountDownLatch start = new CountDownLatch(1);
                final List<Future<String>> readings = new ArrayList<>();
                for (int reader = 0; reader < READER_THREADS; reader++) {
                    readings.add(executor.submit(() -> {
                        start.await();
                        return shared.isNegative() + "|" + shared.getValueBeforeDecimalPoint() + "|" + shared.getValueAfterDecimalPoint() + "|" + shared;
                    }));
                }
                start.countDown();
                final String first = readings.getFirst().get();
                for (final Future<String> reading : readings) {
                    assertEquals(first, reading.get());
                }
                assertEquals("true|12345|678900|-12345.6789", first);
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
