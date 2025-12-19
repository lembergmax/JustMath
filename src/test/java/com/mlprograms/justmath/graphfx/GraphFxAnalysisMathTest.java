/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.graphfx;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphfx.service.GraphFxAnalysisMath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxAnalysisMath")
final class GraphFxAnalysisMathTest {

    private static final Locale LOCALE = BigNumbers.CALCULATION_LOCALE;
    private static final MathContext MC = MathContext.DECIMAL128;

    @Test
    void evalY_returnsNull_whenEngineThrows() {
        final CalculatorEngine engine = Mockito.mock(CalculatorEngine.class);
        Mockito.when(engine.evaluate(Mockito.anyString(), Mockito.anyMap()))
                .thenThrow(new RuntimeException("nope"));

        final GraphFxAnalysisMath math = new GraphFxAnalysisMath();
        final Double y = math.evalY(engine, "x", Map.of(), BigDecimal.ONE);

        assertNull(y);
    }

    @Test
    void derivative_matchesForXSquared_atX2_isApproximately4() {
        final CalculatorEngine engine = mockEngineForExpressions();
        final GraphFxAnalysisMath math = new GraphFxAnalysisMath();

        final BigNumber derivative = math.derivative(engine, "x^2", Map.of(), new BigDecimal("2"));
        assertNotNull(derivative);

        final BigDecimal asDecimal = derivative.toBigDecimal();
        assertTrue(asDecimal.subtract(new BigDecimal("4")).abs().compareTo(new BigDecimal("0.01")) < 0,
                "Derivative of x^2 at x=2 should be close to 4 (numerical approximation).");
    }

    @Test
    void rootsInRange_findsZeroForX() {
        final CalculatorEngine engine = mockEngineForExpressions();
        final GraphFxAnalysisMath math = new GraphFxAnalysisMath();

        final List<BigDecimal> roots = math.rootsInRange(engine, "x", Map.of(), new BigDecimal("-1"), new BigDecimal("1"), 40);

        assertNotNull(roots);
        assertFalse(roots.isEmpty());

        final boolean containsNearZero = roots.stream()
                .anyMatch(r -> r.abs().compareTo(new BigDecimal("0.01")) < 0);

        assertTrue(containsNearZero, "Expected a root close to 0.");
    }

    @Test
    void intersectionsInRange_findsIntersectionForX_andOneMinusX_atHalf() {
        final CalculatorEngine engine = mockEngineForExpressions();
        final GraphFxAnalysisMath math = new GraphFxAnalysisMath();

        final List<BigDecimal> xs = math.intersectionsInRange(
                engine,
                "x",
                "1-x",
                Map.of(),
                BigDecimal.ZERO,
                BigDecimal.ONE,
                60
        );

        assertNotNull(xs);
        assertTrue(xs.stream().anyMatch(x -> x.subtract(new BigDecimal("0.5")).abs().compareTo(new BigDecimal("0.02")) < 0),
                "Expected intersection near x=0.5.");
    }

    private static CalculatorEngine mockEngineForExpressions() {
        final CalculatorEngine engine = Mockito.mock(CalculatorEngine.class);

        final Answer<BigNumber> answer = invocation -> {
            final String expr = invocation.getArgument(0, String.class);
            @SuppressWarnings("unchecked")
            final Map<String, String> vars = invocation.getArgument(1, Map.class);

            final BigDecimal x = new BigDecimal(vars.getOrDefault("x", "0"));
            final BigDecimal result = switch (expr) {
                case "x" -> x;
                case "x^2", "x*x" -> x.multiply(x);
                case "1-x" -> BigDecimal.ONE.subtract(x);
                default -> {
                    // very small fallback to keep the graph code safe
                    yield BigDecimal.ZERO;
                }
            };

            return new BigNumber(result.toPlainString(), LOCALE, MC);
        };

        Mockito.when(engine.evaluate(Mockito.anyString(), Mockito.anyMap()))
                .thenAnswer(answer);

        return engine;
    }

}
