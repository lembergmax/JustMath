package com.mlprograms.justmath.graphing.engine.sampling;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;
import com.mlprograms.justmath.graphing.api.Domain;
import com.mlprograms.justmath.graphing.api.Resolution;
import com.mlprograms.justmath.graphing.engine.EvaluationContext;
import com.mlprograms.justmath.graphing.model.PointBuffer;

public interface SamplingStrategy {

    PointBuffer sample(CompiledPlotExpression expression, Domain domain, Resolution resolution, EvaluationContext context);
}
