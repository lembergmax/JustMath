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

package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;
import com.mlprograms.justmath.graphing.api.GraphingEvaluationException;
import com.mlprograms.justmath.graphing.api.GraphingParseException;
import com.mlprograms.justmath.graphing.api.PlotEvaluationContext;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Default compiler facade that validates expression syntax and creates a reusable evaluator.
 * <p>
 * This implementation delegates evaluation to {@link CalculatorEngine}. It validates syntax by performing
 * a lightweight trial evaluation using {@code x=0}.
 * </p>
 */
public final class DefaultExpressionCompiler implements ExpressionCompiler {

    /**
     * Reserved variable name for the independent x-axis variable.
     */
    private static final String X_VARIABLE = "x";

    /**
     * Thread-local variable map to avoid allocating a new map for every evaluation call.
     * <p>
     * The map is cleared and repopulated on each evaluation.
     * </p>
     */
    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL_VARIABLE_MAP =
            ThreadLocal.withInitial(HashMap::new);

    /**
     * Underlying calculator engine used for parsing and evaluation.
     */
    private final CalculatorEngine calculatorEngine;

    /**
     * Creates a compiler using the provided calculator engine.
     *
     * @param calculatorEngine calculator engine (must not be {@code null})
     */
    public DefaultExpressionCompiler(@NonNull final CalculatorEngine calculatorEngine) {
        this.calculatorEngine = calculatorEngine;
    }

    /**
     * Compiles an expression string into a reusable {@link CompiledPlotExpression}.
     *
     * @param expression expression string (must not be {@code null} or blank)
     * @return compiled expression (never {@code null})
     * @throws GraphingParseException if the expression is blank or cannot be parsed
     */
    @Override
    public CompiledPlotExpression compile(final String expression) {
        if (expression == null || expression.isBlank()) {
            throw new GraphingParseException("Expression must not be blank", null);
        }

        validateSyntax(expression);
        return (x, context) -> evaluate(expression, x, context);
    }

    /**
     * Validates expression syntax by performing a trial evaluation with {@code x=0}.
     *
     * @param expression expression to validate (non-blank)
     * @throws GraphingParseException if parsing fails
     */
    private void validateSyntax(final String expression) {
        try {
            calculatorEngine.evaluate(expression, Map.of(X_VARIABLE, "0"));
        } catch (final Exception exception) {
            throw new GraphingParseException("Failed to parse expression: " + expression, exception);
        }
    }

    /**
     * Evaluates the expression at a given x-value.
     *
     * @param expression expression (non-blank)
     * @param x          x-value
     * @param context    evaluation context (must not be {@code null})
     * @return evaluated y-value
     * @throws GraphingEvaluationException if evaluation fails
     */
    private double evaluate(final String expression, final double x, @NonNull final PlotEvaluationContext context) {
        final Map<String, String> mappedVariables = THREAD_LOCAL_VARIABLE_MAP.get();
        mappedVariables.clear();

        for (final Map.Entry<String, Double> entry : context.variables().entrySet()) {
            mappedVariables.put(entry.getKey(), Double.toString(entry.getValue()));
        }
        mappedVariables.put(X_VARIABLE, Double.toString(x));

        try {
            return calculatorEngine.evaluate(expression, mappedVariables).doubleValue();
        } catch (final Exception exception) {
            throw new GraphingEvaluationException("Failed evaluating expression at x=" + x, exception);
        }
    }
}
