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

package com.mlprograms.justmath.graphing.fx.planar.engine.expression;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import lombok.NonNull;

/**
 * Factory for creating {@link PlotExpression} instances from equation strings.
 * <p>
 * Produces an implicit function {@code f(x,y)=left-right}.
 * </p>
 */
public final class PlotExpressionFactory {

    /**
     * Calculator engine used for numeric evaluation.
     */
    private final CalculatorEngine calculatorEngine;

    /**
     * Creates a factory using a new default calculator engine.
     */
    public PlotExpressionFactory() {
        this(new CalculatorEngine());
    }

    /**
     * Creates a factory.
     *
     * @param calculatorEngine calculator engine (non-null)
     */
    public PlotExpressionFactory(@NonNull final CalculatorEngine calculatorEngine) {
        this.calculatorEngine = calculatorEngine;
    }

    /**
     * Creates a plot expression from an equation string.
     *
     * @param equation equation string (non-null)
     * @return plot expression
     */
    public PlotExpression create(@NonNull final String equation) {
        final GraphExpressionParser.ParsedEquation parsed = GraphExpressionParser.parse(equation);
        final String left = parsed.left();
        final String right = parsed.right();

        return (x, y) -> {
            final String sx = Double.toString(x);
            final String sy = Double.toString(y);

            final double leftVal = calculatorEngine.evaluate(left, ExpressionVariables.xy(sx, sy)).doubleValue();
            final double rightVal = calculatorEngine.evaluate(right, ExpressionVariables.xy(sx, sy)).doubleValue();

            return leftVal - rightVal;
        };
    }
}
