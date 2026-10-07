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

package io.github.lembergmax.justmath.bignumber.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every parameter of a public method of {@link BasicMath} is marked {@code @NonNull}. A {@code null} argument is a
 * {@link NullPointerException} before anything is computed, whatever the position of the argument.
 */
class BasicMathNullContractTest {

    private static final int MIN_PUBLIC_METHODS = 9;

    private static Object validArgument(final Class<?> type) {
        if (type == BigNumber.class) {
            return new BigNumber("2");
        }
        if (type == MathContext.class) {
            return MathContext.DECIMAL64;
        }
        if (type == Locale.class) {
            return Locale.US;
        }
        throw new IllegalArgumentException("No valid argument for " + type);
    }

    static Stream<Arguments> publicMethodsAndNullPositions() {
        return Arrays.stream(BasicMath.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers()))
                .flatMap(method -> java.util.stream.IntStream.range(0, method.getParameterCount()).mapToObj(position -> Arguments.of(method, position)));
    }

    @ParameterizedTest(name = "{0}, argument {1}")
    @MethodSource("publicMethodsAndNullPositions")
    @DisplayName("a null argument is rejected with a NullPointerException")
    void nullArgumentIsRejected(final Method method, final int nullPosition) {
        final Object[] arguments = Arrays.stream(method.getParameterTypes()).map(BasicMathNullContractTest::validArgument).toArray();
        arguments[nullPosition] = null;

        final InvocationTargetException failure = assertThrows(InvocationTargetException.class, () -> method.invoke(null, arguments));

        assertEquals(NullPointerException.class, failure.getCause().getClass());
    }

    @Test
    @DisplayName("the test finds the public methods of BasicMath")
    void findsThePublicMethods() {
        final List<Method> methods = publicMethodsAndNullPositions().map(arguments -> (Method) arguments.get()[0]).distinct().toList();

        assertTrue(methods.size() >= MIN_PUBLIC_METHODS, "BasicMath has at least " + MIN_PUBLIC_METHODS + " public methods: " + methods);
    }
}
