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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A method of {@link BigNumber} that exists in several overloads (with and without a {@link MathContext}, a
 * {@link TrigonometricMode}, a {@link Locale} and external variables) must give the same result in every overload when
 * the omitted values are the ones of the receiver. Overloads belong together when they take the same operands. The
 * test finds them by reflection, calls each of them on the first receiver that is in the domain of the function and
 * compares the results. A result is never {@code null}.
 */
class BigNumberOverloadConsistencyTest {

    private static final List<String> RECEIVERS = List.of("0.5", "2", "5", "12", "-3");

    private static final BigNumber FIRST_ARGUMENT = new BigNumber("3");

    private static final String SERIES_EXPRESSION = "k";

    private static final String RANDOM_PREFIX = "random";

    private static final int MIN_METHOD_GROUPS = 40;

    private static boolean isSupportedParameter(final Class<?> type) {
        return type == BigNumber.class || type == MathContext.class || type == TrigonometricMode.class || type == Locale.class
                || type == String.class || type == Map.class;
    }

    private static boolean isCandidate(final Method method) {
        final int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && !method.isSynthetic()
                && !method.getName().startsWith(RANDOM_PREFIX)
                && BigNumber.class.isAssignableFrom(method.getReturnType())
                && Arrays.stream(method.getParameterTypes()).allMatch(BigNumberOverloadConsistencyTest::isSupportedParameter);
    }

    private static Object argumentFor(final Class<?> type, final BigNumber receiver) {
        if (type == BigNumber.class) {
            return FIRST_ARGUMENT;
        }
        if (type == MathContext.class) {
            return receiver.getMathContext();
        }
        if (type == TrigonometricMode.class) {
            return receiver.getTrigonometricMode();
        }
        if (type == Locale.class) {
            return receiver.getLocale();
        }
        if (type == String.class) {
            return SERIES_EXPRESSION;
        }
        return Map.of();
    }

    private static boolean isDefaultedParameter(final Class<?> type) {
        return type == MathContext.class || type == TrigonometricMode.class || type == Locale.class || type == Map.class;
    }

    private static String coreSignature(final Method method) {
        final String parameters = Arrays.stream(method.getParameterTypes())
                .filter(type -> !isDefaultedParameter(type))
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
        return method.getName() + "(" + parameters + ")";
    }

    static Stream<Arguments> methodGroups() {
        final Map<String, List<Method>> groups = new TreeMap<>();
        for (final Method method : BigNumber.class.getDeclaredMethods()) {
            if (isCandidate(method)) {
                groups.computeIfAbsent(coreSignature(method), signature -> new ArrayList<>()).add(method);
            }
        }
        return groups.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    private record Outcome(String value, String failure) {
    }

    private static Outcome invoke(final Method method, final BigNumber receiver) {
        final Object[] arguments = Arrays.stream(method.getParameterTypes()).map(type -> argumentFor(type, receiver)).toArray();
        try {
            final Object result = method.invoke(receiver, arguments);
            assertNotNull(result, method + " returned null");
            return new Outcome(result.toString(), null);
        } catch (final InvocationTargetException failure) {
            return new Outcome(null, failure.getCause().getClass().getName());
        } catch (final IllegalAccessException failure) {
            throw new IllegalStateException(failure);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("methodGroups")
    @DisplayName("every overload of a method gives the result of the others")
    void everyOverloadGivesTheSameResult(final String name, final List<Method> overloads) {
        boolean compared = false;

        for (final String text : RECEIVERS) {
            final BigNumber receiver = new BigNumber(text);
            final List<Outcome> outcomes = overloads.stream().map(method -> invoke(method, receiver)).toList();

            if (outcomes.stream().allMatch(outcome -> outcome.failure() != null)) {
                continue;
            }
            compared = true;
            final Outcome first = outcomes.getFirst();
            for (int index = 1; index < outcomes.size(); index++) {
                assertEquals(first, outcomes.get(index), name + " on " + text + ": " + overloads.getFirst() + " and " + overloads.get(index));
            }
            break;
        }

        assertTrue(compared, name + " has no receiver in its domain among " + RECEIVERS);
    }

    @Test
    @DisplayName("the reflection finds the overloads of the library")
    void reflectionFindsTheOverloads() {
        assertTrue(methodGroups().count() >= MIN_METHOD_GROUPS, "BigNumber has at least " + MIN_METHOD_GROUPS + " methods with overloads");
    }
}
