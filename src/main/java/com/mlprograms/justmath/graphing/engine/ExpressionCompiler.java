package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;

public interface ExpressionCompiler {

    CompiledPlotExpression compile(String expression);
}
