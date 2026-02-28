package com.mlprograms.justmath.graphing.api;

import java.util.Map;

/**
 * Compiled representation of an expression that can be sampled for x-values.
 */
public interface CompiledPlotExpression {

    String expression();

    double evaluate(double x, Map<String, Double> variables);
}
