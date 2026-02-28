package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;
import com.mlprograms.justmath.graphing.api.PlotRequest;
import com.mlprograms.justmath.graphing.api.PlotSeries;
import com.mlprograms.justmath.graphing.model.PointBuffer;

/**
 * Sampling engine delegating to request-level strategies.
 */
public final class DefaultSamplingEngine implements SamplingEngine {

    @Override
    public PlotSeries sample(final CompiledPlotExpression expression, final PlotRequest request) {
        final EvaluationContext context = new EvaluationContext();
        context.putAll(request.variables());
        final PointBuffer points = request.samplingStrategy()
                .sample(expression, request.domain(), request.resolution(), context);

        return new PlotSeries(request.expression(), points.xValues(), points.yValues());
    }
}
