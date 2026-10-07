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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.MathContext;
import java.math.RoundingMode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;

/**
 * Edge-case coverage for the mathematical boundaries the audit flagged as untested: the {@code 0^0}
 * convention, trigonometric/logarithmic singularities, inverse-trig domain limits, very large and
 * very small magnitudes, factorial boundaries, and rounding-mode sensitivity.
 */
class MathematicalEdgeCaseTest {

    private final CalculatorEngine engine = new CalculatorEngine(TrigonometricMode.DEG);

    @Test
    void zeroToThePowerOfZeroIsOne() {
        assertEquals("1", engine.evaluateToString("0^0"));
    }

    @Test
    @DisplayName("trigonometric poles (tan 90°, cot 0°) are rejected")
    void trigonometricPolesAreRejected() {
        assertTrue(engine.evaluateSafe("tan(90)").isFailure(), "tan(90°) is a pole");
        assertTrue(engine.evaluateSafe("cot(0)").isFailure(), "cot(0°) is a pole");
    }

    @Test
    void logarithmDomainViolationsAreRejected() {
        assertTrue(engine.evaluateSafe("ln(0)").isFailure(), "ln(0) is undefined");
        assertTrue(engine.evaluateSafe("ln(-1)").isFailure(), "ln of a negative is undefined");
    }

    @Test
    void rootDomainViolationsAreRejected() {
        assertTrue(engine.evaluateSafe("sqrt(-1)").isFailure(), "sqrt of a negative is not real");
    }

    @Test
    void inverseTrigBoundariesRespectDomain() {
        assertTrue(engine.evaluateSafe("asin(1)").isSuccess(), "asin(1) is the in-domain boundary");
        assertTrue(engine.evaluateSafe("acos(-1)").isSuccess(), "acos(-1) is the in-domain boundary");
        assertTrue(engine.evaluateSafe("asin(2)").isFailure(), "asin(2) is out of domain");
        assertTrue(engine.evaluateSafe("acos(-2)").isFailure(), "acos(-2) is out of domain");
    }

    @Test
    @DisplayName("very large and very small magnitudes stay exact")
    void largeAndSmallMagnitudes() {
        assertEquals("1267650600228229401496703205376", engine.evaluateToString("2^100"));
        assertEquals("1", engine.evaluateToString("10^50/10^50"));
        assertEquals(engine.evaluateToString("10^50"), engine.evaluateToString("10^25*10^25"));
    }

    @Test
    void factorialBoundaries() {
        assertEquals("1", engine.evaluateToString("0!"));
        assertTrue(engine.evaluateSafe("(-1)!").isFailure(), "factorial of a negative is undefined");
    }

    @Test
    @DisplayName("division/rounding honor at least HALF_UP and HALF_EVEN")
    void roundingModesAreHonored() {
        final BigNumber twoAndAHalf = new BigNumber("2.5");
        assertEquals("2", twoAndAHalf.round(new MathContext(1, RoundingMode.HALF_EVEN)).toString());
        assertEquals("3", twoAndAHalf.round(new MathContext(1, RoundingMode.HALF_UP)).toString());

        final BigNumber oneThird = new BigNumber("1").divide(new BigNumber("3"), new MathContext(4, RoundingMode.HALF_EVEN));
        assertEquals("0.3333", oneThird.toString());
    }
}
