package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.exceptions.CyclicVariableReferenceException;
import com.mlprograms.justmath.calculator.exceptions.SyntaxErrorException;
import com.mlprograms.justmath.graphing.api.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Default graphing facade for y=f(x) expressions.
 */
public final class DefaultGraphingCalculator implements GraphingCalculator {

    private static final String X_VARIABLE_NAME = "x";

    private final CalculatorEngine calculatorEngine;
    private final ExpressionCache expressionCache;

    public DefaultGraphingCalculator() {
        this(new CalculatorEngine(), new ExpressionCache(512));
    }

    DefaultGraphingCalculator(final CalculatorEngine calculatorEngine, final ExpressionCache expressionCache) {
        this.calculatorEngine = Objects.requireNonNull(calculatorEngine, "calculatorEngine must not be null");
        this.expressionCache = Objects.requireNonNull(expressionCache, "expressionCache must not be null");
    }

    @Override
    public PlotResponse plot(final String expression, final Domain domain, final Resolution resolution) {
        return plot(new PlotRequest.Builder(expression).domain(domain).resolution(resolution).build());
    }

    @Override
    public PlotResponse plot(final PlotRequest request) {
        final CompiledPlotExpression compiled = compile(request.expression());
        return new PlotResponse(List.of(sample(compiled, request)));
    }

    @Override
    public List<PlotSeries> plotAll(final List<PlotRequest> requests) {
        return requests.stream().map(this::plot).flatMap(response -> response.series().stream()).collect(Collectors.toList());
    }

    @Override
    public CompiledPlotExpression compile(final String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("expression must not be blank");
        }

        final CompiledPlotExpression cached = expressionCache.get(expression);
        if (cached != null) {
            return cached;
        }

        final CompiledPlotExpression compiled = new CalculatorCompiledPlotExpression(expression, calculatorEngine);
        expressionCache.put(expression, compiled);
        return compiled;
    }

    @Override
    public PlotSeries sample(final CompiledPlotExpression expression, final PlotRequest request) {
        return request.samplingStrategy().sample(expression, request);
    }

    private static final class CalculatorCompiledPlotExpression implements CompiledPlotExpression {

        private final String expression;
        private final CalculatorEngine calculatorEngine;

        private CalculatorCompiledPlotExpression(final String expression, final CalculatorEngine calculatorEngine) {
            this.expression = expression;
            this.calculatorEngine = calculatorEngine;

            try {
                evaluate(0.0, Map.of());
            } catch (GraphingEvaluationException ignored) {
                // Runtime domain issues (e.g. log(0)) are acceptable during compile validation.
            }
        }

        @Override
        public String expression() {
            return expression;
        }

        @Override
        public double evaluate(final double x, final Map<String, Double> variables) {
            final Map<String, String> evaluationVariables = variables.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> Double.toString(entry.getValue())));
            evaluationVariables.put(X_VARIABLE_NAME, Double.toString(x));

            try {
                return calculatorEngine.evaluate(expression, evaluationVariables).doubleValue();
            } catch (SyntaxErrorException exception) {
                throw new GraphingParseException("Invalid expression: " + expression, exception);
            } catch (CyclicVariableReferenceException exception) {
                throw new MissingVariableException("Variable resolution failed", exception);
            } catch (GraphingException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new GraphingEvaluationException("Failed to evaluate expression: " + expression, exception);
            }
        }
    }
}
