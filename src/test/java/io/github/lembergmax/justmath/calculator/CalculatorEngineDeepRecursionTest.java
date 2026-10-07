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

package io.github.lembergmax.justmath.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.errors.CalculatorResult;
import io.github.lembergmax.justmath.calculator.exceptions.ProcessingErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Regression for H6: a deep, acyclic variable-reference chain recurses through both the cycle-detection DFS and the
 * substitution re-entry into {@link CalculatorEngine#evaluate(String, Map)}, eventually raising a
 * {@link StackOverflowError}.
 *
 * <p>Because {@code StackOverflowError} is an {@link Error} (not an {@link Exception}), it used to slip past the
 * {@code catch (Exception)} clauses of the Safe / Text Output APIs and escape their documented "never throws"
 * contract. The boundaries now catch it explicitly.</p>
 *
 * <p>A variable name consists of ASCII letters only, so {@code x1} is the product of {@code x} and {@code 1} and not a
 * name. The chain therefore uses names made of the letters {@code x} and {@code y}, which no function or constant
 * starts with. The evaluation runs on a thread with a small stack, so that the overflow does not depend on the
 * default stack size of the JVM.</p>
 */
class CalculatorEngineDeepRecursionTest {

    private static final int CHAIN_LENGTH = 6_000;

    private static final int NAME_WIDTH = 13;

    private static final int SMALL_STACK_BYTES = 160 * 1024;

    private static final String DEEPLY_NESTED_DETAIL = "Expression is nested too deeply to evaluate";

    private static final Map<String, String> DEEP_CHAIN = acyclicChain(CHAIN_LENGTH);

    private static String variableName(final int index) {
        final StringBuilder name = new StringBuilder();
        for (int bit = NAME_WIDTH - 1; bit >= 0; bit--) {
            name.append(((index >> bit) & 1) == 0 ? 'x' : 'y');
        }
        return name.toString();
    }

    private static Map<String, String> acyclicChain(final int length) {
        final Map<String, String> variables = new HashMap<>();
        for (int index = 1; index < length; index++) {
            variables.put(variableName(index), variableName(index + 1) + "+1");
        }
        variables.put(variableName(length), "1");
        return variables;
    }

    private static <T> T callOnSmallStack(final Supplier<T> action) throws InterruptedException {
        final AtomicReference<T> result = new AtomicReference<>();
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        final Thread thread = new Thread(null, () -> {
            try {
                result.set(action.get());
            } catch (final Throwable escaped) {
                failure.set(escaped);
            }
        }, "small-stack-evaluation", SMALL_STACK_BYTES);
        thread.start();
        thread.join();
        assertNull(failure.get(), "the API must not let anything escape, but threw " + failure.get());
        return result.get();
    }

    private static String expectedRawMessage() {
        return new ProcessingErrorException(CalculatorErrorCode.PROCESSING_INTERNAL, DEEPLY_NESTED_DETAIL).getMessage();
    }

    static Stream<Arguments> safeTextApis() {
        final String start = variableName(1);
        return Stream.of(
                Arguments.of("evaluateSafeToString", (Function<CalculatorEngine, String>) engine -> engine.evaluateSafeToString(start, DEEP_CHAIN)),
                Arguments.of("evaluateSafeToPrettyString", (Function<CalculatorEngine, String>) engine -> engine.evaluateSafeToPrettyString(start, DEEP_CHAIN)));
    }

    static Stream<Arguments> textApis() {
        final String start = variableName(1);
        return Stream.of(
                Arguments.of("evaluateToString", (Function<CalculatorEngine, String>) engine -> engine.evaluateToString(start, DEEP_CHAIN)),
                Arguments.of("evaluateToPrettyString", (Function<CalculatorEngine, String>) engine -> engine.evaluateToPrettyString(start, DEEP_CHAIN)));
    }

    static Stream<Arguments> typedResultApis() {
        final String start = variableName(1);
        return Stream.of(
                Arguments.of("evaluateSafe", (Function<CalculatorEngine, CalculatorResult<?>>) engine -> engine.evaluateSafe(start, DEEP_CHAIN)),
                Arguments.of("evaluateToStringResult", (Function<CalculatorEngine, CalculatorResult<?>>) engine -> engine.evaluateToStringResult(start, DEEP_CHAIN)),
                Arguments.of("evaluateToPrettyStringResult", (Function<CalculatorEngine, CalculatorResult<?>>) engine -> engine.evaluateToPrettyStringResult(start, DEEP_CHAIN)));
    }

    @Test
    @DisplayName("a chain of letter-only variable names resolves, so the deep chain really recurses")
    void shortChainResolves() {
        final int length = 5;

        assertEquals("5", new CalculatorEngine().evaluateToString(variableName(1), acyclicChain(length)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("typedResultApis")
    @DisplayName("the typed result APIs report a deep variable chain as PROCESSING_INTERNAL")
    void typedResultApisNeverEscapeOnDeepChain(final String name, final Function<CalculatorEngine, CalculatorResult<?>> api)
            throws InterruptedException {
        final CalculatorEngine engine = new CalculatorEngine();

        final CalculatorResult<?> result = callOnSmallStack(() -> api.apply(engine));

        assertTrue(result.isFailure(), name);
        assertEquals(CalculatorErrorCode.PROCESSING_INTERNAL, result.error().orElseThrow().code(), name);
        assertEquals(DEEPLY_NESTED_DETAIL, result.error().orElseThrow().technicalDetail(), name);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("safeTextApis")
    @DisplayName("the safe text APIs report a deep variable chain as a prefixed error text")
    void safeStringApisNeverEscapeOnDeepChain(final String name, final Function<CalculatorEngine, String> api)
            throws InterruptedException {
        final CalculatorEngine engine = new CalculatorEngine();

        assertEquals("Error: " + expectedRawMessage(), callOnSmallStack(() -> api.apply(engine)), name);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("textApis")
    @DisplayName("the text APIs report a deep variable chain as an error text")
    void textApisNeverEscapeOnDeepChain(final String name, final Function<CalculatorEngine, String> api)
            throws InterruptedException {
        final CalculatorEngine engine = new CalculatorEngine();

        assertEquals(expectedRawMessage(), callOnSmallStack(() -> api.apply(engine)), name);
    }
}
