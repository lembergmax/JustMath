package com.mlprograms.justmath.graphing.api;

import com.mlprograms.justmath.graphing.engine.EvaluationContext;

/**
 * Compiled, reusable plotting expression.
 */
public interface CompiledPlotExpression {

    double evaluate(double x, EvaluationContext context);
}
