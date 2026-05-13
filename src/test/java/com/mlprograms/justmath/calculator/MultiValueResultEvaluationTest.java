/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package com.mlprograms.justmath.calculator;

import static org.junit.jupiter.api.Assertions.*;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import org.junit.jupiter.api.Test;

/**
 * Verifies that multi-value function results (e.g. from {@code Pol} and {@code Rec})
 * collapse to their first scalar component when used inside a larger scalar
 * expression, while still rendering as full multi-value output when the
 * expression consists of only the multi-value call.
 */
class MultiValueResultEvaluationTest {

    private final CalculatorEngine calculator = new CalculatorEngine(TrigonometricMode.DEG);

    @Test
    void polAlone_keepsFullMultiValueOutput() {
        BigNumber raw = calculator.evaluate("Pol(1;2)");

        assertInstanceOf(BigNumberCoordinate.class, raw,
                "Pol(1;2) by itself must remain a coordinate");
        String rendered = raw.toString();
        assertTrue(rendered.startsWith("r="),
                "Pol(1;2) by itself should render the full polar coordinate, was: " + rendered);
        assertTrue(rendered.contains("θ="),
                "Pol(1;2) by itself should keep the θ component, was: " + rendered);
    }

    @Test
    void recAlone_keepsFullMultiValueOutput() {
        BigNumber raw = calculator.evaluate("Rec(2;1)");

        assertInstanceOf(BigNumberCoordinate.class, raw,
                "Rec(2;1) by itself must remain a coordinate");
        String rendered = raw.toString();
        assertTrue(rendered.startsWith("x="),
                "Rec(2;1) by itself should render the full cartesian coordinate, was: " + rendered);
        assertTrue(rendered.contains("y="),
                "Rec(2;1) by itself should keep the y component, was: " + rendered);
    }

    @Test
    void polPlusScalar_matchesScalarSqrtFiveAddition() {
        // r(Pol(1;2)) = sqrt(1²+2²) = sqrt(5)
        assertEqualResults("sqrt(5)+3", "Pol(1;2)+3");
    }

    @Test
    void scalarPlusPol_matchesScalarSqrtFiveAddition() {
        assertEqualResults("3+sqrt(5)", "3+Pol(1;2)");
    }

    @Test
    void polTimesScalar_matchesScalarSqrtFiveMultiplication() {
        assertEqualResults("sqrt(5)*3", "Pol(1;2)*3");
    }

    @Test
    void polPowerTwo_matchesScalarSqrtFiveSquared() {
        assertEqualResults("sqrt(5)^2", "Pol(1;2)^2");
    }

    @Test
    void polPlusRec_collapsesBothToFirstComponent() {
        // r(Pol(1;2)) = sqrt(5); x(Rec(2;1)) = 2*cos(1°)
        assertEqualResults("sqrt(5)+2*cos(1)", "Pol(1;2)+Rec(2;1)");
    }

    @Test
    void recPlusPol_collapsesBothToFirstComponent() {
        assertEqualResults("2*cos(1)+sqrt(5)", "Rec(2;1)+Pol(1;2)");
    }

    @Test
    void polMinusScalar_collapsesToFirstComponent() {
        assertEqualResults("sqrt(5)-1", "Pol(1;2)-1");
    }

    @Test
    void polDivScalar_collapsesToFirstComponent() {
        assertEqualResults("sqrt(5)/2", "Pol(1;2)/2");
    }

    @Test
    void scalarFunctionApplied_toMultiValueResult_usesFirstComponent() {
        // sqrt of Pol(1;2) collapses to sqrt(sqrt(5))
        assertEqualResults("sqrt(sqrt(5))", "sqrt(Pol(1;2))");
    }

    @Test
    void firstValue_isReturnedAsPlainBigNumber_notAsCoordinate() {
        BigNumber raw = calculator.evaluate("Pol(1;2)");
        BigNumberCoordinate coord = assertInstanceOf(BigNumberCoordinate.class, raw);
        BigNumber first = coord.firstValue();

        assertTrue(first.getClass().equals(BigNumber.class),
                "firstValue() must return a plain BigNumber, not a coordinate; was: " + first.getClass());
    }

    /**
     * Evaluates {@code expected} and {@code actual} with the same engine and
     * asserts that both produce the same string representation. Using the
     * engine for both sides keeps precision and rounding aligned.
     */
    private void assertEqualResults(String expected, String actual) {
        BigNumber expectedValue = calculator.evaluate(expected);
        BigNumber actualValue = calculator.evaluate(actual);
        assertEquals(expectedValue.toString(), actualValue.toString(),
                "expected `" + expected + "` and `" + actual + "` to produce the same scalar result");
    }

}
