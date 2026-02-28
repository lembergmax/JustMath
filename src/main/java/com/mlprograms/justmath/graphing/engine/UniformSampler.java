package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.graphing.api.*;
import com.mlprograms.justmath.graphing.model.PointBuffer;

import java.util.Map;

/**
 * Uniform x-step sampler for y=f(x).
 */
public final class UniformSampler implements SamplingStrategy {

    @Override
    public PlotSeries sample(final CompiledPlotExpression expression, final PlotRequest request) {
        final Domain domain = request.domain();
        final int samples = request.resolution().targetSamples();
        final double step = (domain.maxX() - domain.minX()) / (samples - 1);
        final PointBuffer buffer = new PointBuffer(samples);
        final Map<String, Double> variables = request.variables();

        for (int i = 0; i < samples; i++) {
            final double x = domain.minX() + (step * i);
            final double y = expression.evaluate(x, variables);
            if (!Double.isFinite(y)) {
                throw new NonFiniteResultException("Non-finite y at x=" + x);
            }
            buffer.add(x, y);
        }

        return new PlotSeries(buffer.xValues(), buffer.yValues());
    }
}
