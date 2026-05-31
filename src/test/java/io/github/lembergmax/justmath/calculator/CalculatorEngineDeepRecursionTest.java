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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regression for H6: a deep, acyclic variable-reference chain ({@code x1=x2+1, x2=x3+1, ...})
 * recurses through both the cycle-detection DFS and the substitution re-entry into
 * {@link CalculatorEngine#evaluate(String, Map)}, eventually raising a {@link StackOverflowError}.
 *
 * <p>Because {@code StackOverflowError} is an {@link Error} (not an {@link Exception}), it used to
 * slip past the {@code catch (Exception)} clauses of the Safe / Text Output APIs and escape their
 * documented "never throws" contract. The boundaries now catch it explicitly.</p>
 */
class CalculatorEngineDeepRecursionTest {

    /** Deep enough to overflow the recursive cycle-check / substitution on a default JVM stack. */
    private static final int CHAIN_LENGTH = 15_000;

    private static Map<String, String> deepAcyclicChain() {
        final Map<String, String> variables = new HashMap<>();
        for (int i = 1; i < CHAIN_LENGTH; i++) {
            variables.put("x" + i, "x" + (i + 1) + "+1");
        }
        variables.put("x" + CHAIN_LENGTH, "1");
        return variables;
    }

    @Test
    @DisplayName("evaluateSafe() never escapes with a StackOverflowError on a deep variable chain")
    void evaluateSafeNeverEscapesOnDeepChain() {
        final CalculatorEngine engine = new CalculatorEngine();
        final Map<String, String> variables = deepAcyclicChain();

        // The Typed Result API must never throw — neither an Exception nor a StackOverflowError.
        assertDoesNotThrow(() -> {
            engine.evaluateSafe("x1", variables);
        }, "evaluateSafe must surface deep recursion as a failure result, not throw");
    }

    @Test
    @DisplayName("Safe / Text string APIs never escape with a StackOverflowError on a deep variable chain")
    void safeStringApisNeverEscapeOnDeepChain() {
        final CalculatorEngine engine = new CalculatorEngine();
        final Map<String, String> variables = deepAcyclicChain();

        assertDoesNotThrow(() -> engine.evaluateSafeToString("x1", variables),
                "evaluateSafeToString must never throw");
        assertDoesNotThrow(() -> engine.evaluateSafeToPrettyString("x1", variables),
                "evaluateSafeToPrettyString must never throw");
        assertDoesNotThrow(() -> engine.evaluateToString("x1", variables),
                "evaluateToString must never throw");
    }
}
