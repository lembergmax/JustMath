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
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.function.Supplier;

import io.github.lembergmax.justmath.calculator.CalculatorEngine;

/**
 * Audit fix K2: public operations that mathematically reduce to 0 or 1 (e.g. {@code sinh(0)},
 * {@code cosh(0)}, {@code combination(n, 0)}, {@code evaluate("")}) must never hand back the
 * shared {@link BigNumbers} constants by reference. If they did, a caller mutating the result
 * (e.g. via {@code negateThis()}) would corrupt the global constant for every other caller.
 *
 * <p>Each probe takes a result from such an operation, mutates it in place, and then asserts the
 * global constants are still intact.</p>
 */
class BigNumberConstantIntegrityTest {

    @AfterEach
    void constantsRemainIntact() {
        assertEquals("0", BigNumbers.ZERO.toString(), "BigNumbers.ZERO corrupted");
        assertEquals("1", BigNumbers.ONE.toString(), "BigNumbers.ONE corrupted");
        assertEquals("-1", BigNumbers.NEGATIVE_ONE.toString(), "BigNumbers.NEGATIVE_ONE corrupted");
    }

    private static void mutateResultOf(final Supplier<BigNumber> operation) {
        operation.get().negateThis();
    }

    @Test
    @DisplayName("Hyperbolic identities returning 0/1 do not leak the shared constant")
    void hyperbolicReductions() {
        mutateResultOf(() -> new BigNumber("0").sinh());
        mutateResultOf(() -> new BigNumber("0").cosh());
        mutateResultOf(() -> new BigNumber("0").tanh());
    }

    @Test
    @DisplayName("Inverse-hyperbolic identities returning 0 do not leak the shared constant")
    void inverseHyperbolicReductions() {
        mutateResultOf(() -> new BigNumber("0").asinh());
        mutateResultOf(() -> new BigNumber("0").atanh());
        mutateResultOf(() -> new BigNumber("1").acosh());
    }

    @Test
    @DisplayName("combination(n, 0) returning 1 does not leak the shared constant")
    void combinationReduction() {
        mutateResultOf(() -> new BigNumber("5").combination(new BigNumber("0")));
    }

    @Test
    @DisplayName("Evaluating a blank expression returning 0 does not leak the shared constant")
    void blankEvaluationReduction() {
        final CalculatorEngine engine = new CalculatorEngine();
        mutateResultOf(() -> engine.evaluate("   "));
    }

    @Test
    @DisplayName("min()/max() return a fresh instance, never the receiver, argument, or a shared constant")
    void minMaxReturnFreshInstance() {
        final BigNumber five = new BigNumber("5");
        assertNotSame(BigNumbers.ZERO, BigNumbers.ZERO.min(five), "min() must not leak the ZERO constant");
        assertNotSame(five, BigNumbers.ZERO.max(five), "max() must not alias its argument");
        assertNotSame(BigNumbers.ONE, new BigNumber("5").min(BigNumbers.ONE), "min() must not leak the ONE constant");
        assertEquals("0", BigNumbers.ZERO.min(five).toString());
        assertEquals("5", BigNumbers.ZERO.max(five).toString());
    }

    @Test
    @DisplayName("min()/max() returning a shared constant do not leak it under mutation")
    void minMaxReductions() {
        // ONE is the larger / NEGATIVE_ONE the smaller, so the constant itself would be returned
        // and then negated — a detectable corruption if it were leaked by reference.
        mutateResultOf(() -> BigNumbers.ONE.max(new BigNumber("-5")));
        mutateResultOf(() -> BigNumbers.NEGATIVE_ONE.min(new BigNumber("5")));
    }

    @Test
    @DisplayName("BigNumberParser.parse(\"\") returns a fresh zero, not the shared ZERO constant")
    void blankParseDoesNotLeakConstant() {
        final BigNumberParser parser = new BigNumberParser();
        assertNotSame(BigNumbers.ZERO, parser.parse(""), "parse(\"\") must not leak the ZERO constant");
        assertNotSame(BigNumbers.ZERO, parser.parse("  ", Locale.US), "parse(blank, locale) must not leak the ZERO constant");
    }
}
