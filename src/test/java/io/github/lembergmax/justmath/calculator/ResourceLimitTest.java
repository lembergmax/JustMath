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

import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.errors.CalculatorResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Security/abuse-resistance regression tests: crafted expressions that would otherwise hang or OOM the
 * process must fail fast with a typed error instead. Each test has a tight {@link Timeout} so an
 * accidental removal of a cap (which would make the computation run for minutes) fails the build.
 */
class ResourceLimitTest {

    private final CalculatorEngine engine = new CalculatorEngine();

    @Test
    @Timeout(20)
    @DisplayName("a huge factorial is rejected, not computed")
    void hugeFactorialIsRejected() {
        final CalculatorResult<?> result = engine.evaluateSafe("1000000!");
        assertTrue(result.isFailure(), "1000000! must be rejected as too large");
    }

    @Test
    @Timeout(20)
    @DisplayName("a huge integer power is rejected, not computed")
    void hugePowerIsRejected() {
        final CalculatorResult<?> result = engine.evaluateSafe("9^9999999999");
        assertTrue(result.isFailure(), "9^9999999999 must be rejected as too large");
    }

    @Test
    @Timeout(20)
    @DisplayName("a huge summation range is rejected, not iterated")
    void hugeSummationIsRejected() {
        final CalculatorResult<?> result = engine.evaluateSafe("summation(1;100000000000;k)");
        assertTrue(result.isFailure(), "summation over 1e11 terms must be rejected as too large");
    }

    @Test
    @Timeout(20)
    @DisplayName("a huge permutation is rejected, not multiplied out")
    void hugePermutationIsRejected() {
        final CalculatorResult<?> result = engine.evaluateSafe("perm(999999999;999999999)");
        assertTrue(result.isFailure(), "P(1e9, 1e9) must be rejected as too large");
    }

    @Test
    @Timeout(20)
    @DisplayName("a huge combination is rejected, not multiplied out")
    void hugeCombinationIsRejected() {
        final CalculatorResult<?> result = engine.evaluateSafe("comb(999999999;400000000)");
        assertTrue(result.isFailure(), "C(1e9, 4e8) must be rejected as too large");
    }

    @Test
    @Timeout(20)
    @DisplayName("1^n is computed (not rejected) even for an exponent beyond the power-size limit")
    void identityPowerIsNotRejected() {
        final CalculatorResult<?> result = engine.evaluateSafe("1^999999999999");
        assertTrue(result.isSuccess(), "1^n is exactly 1 and must never be rejected as too large");
    }

    @Test
    @Timeout(20)
    @DisplayName("an over-long expression is rejected before tokenization")
    void overLongExpressionIsRejected() {
        final String huge = "1+".repeat(200_000) + "1";
        final CalculatorResult<?> result = engine.evaluateSafe(huge);
        assertTrue(result.isFailure(), "an expression longer than the limit must be rejected");
        assertEquals(CalculatorErrorCode.MATH_OVERFLOW, result.error().orElseThrow().code(),
                "an over-long expression is a range/overflow limit, not an incomplete expression");
    }

    @Test
    @Timeout(20)
    @DisplayName("legitimate factorial / power / summation still work")
    void legitimateInputsStillWork() {
        assertTrue(engine.evaluateSafe("10!").isSuccess(), "10! must still compute");
        assertTrue(engine.evaluateSafe("2^64").isSuccess(), "2^64 must still compute");
        assertTrue(engine.evaluateSafe("summation(1;100;k)").isSuccess(), "small summation must still compute");
        assertTrue(engine.evaluateSafe("perm(20;10)").isSuccess(), "small permutation must still compute");
        assertTrue(engine.evaluateSafe("comb(20;10)").isSuccess(), "small combination must still compute");
    }
}
