package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;
import com.mlprograms.justmath.graphing.api.GraphingEvaluationException;
import com.mlprograms.justmath.graphing.api.GraphingParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Compiler facade that validates expression syntax and creates a reusable evaluator.
 */
public final class DefaultExpressionCompiler implements ExpressionCompiler {

    private final CalculatorEngine calculatorEngine;

    public DefaultExpressionCompiler(final CalculatorEngine calculatorEngine) {
        this.calculatorEngine = calculatorEngine;
    }

    @Override
    public CompiledPlotExpression compile(final String expression) {
        if (expression == null || expression.isBlank()) {
            throw new GraphingParseException("Expression must not be blank", null);
        }

        try {
            calculatorEngine.evaluate(expression, Map.of("x", "0"));
        } catch (Exception exception) {
            throw new GraphingParseException("Failed to parse expression: " + expression, exception);
        }

        return (x, context) -> {
            final Map<String, String> mappedVariables = new HashMap<>();
            for (Map.Entry<String, Double> entry : context.variables().entrySet()) {
                mappedVariables.put(entry.getKey(), Double.toString(entry.getValue()));
            }
            mappedVariables.put("x", Double.toString(x));

            try {
                return calculatorEngine.evaluate(expression, mappedVariables).doubleValue();
            } catch (Exception exception) {
                throw new GraphingEvaluationException("Failed evaluating expression at x=" + x, exception);
            }
        };
    }
}
