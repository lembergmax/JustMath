package com.mlprograms.justmath.graphing.engine.sampling;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;
import com.mlprograms.justmath.graphing.api.Domain;
import com.mlprograms.justmath.graphing.api.Resolution;
import com.mlprograms.justmath.graphing.engine.EvaluationContext;
import com.mlprograms.justmath.graphing.model.PointBuffer;

/**
 * Uniform x-axis sampler with fixed step width.
 */
public final class UniformSampler implements SamplingStrategy {

    @Override
    public PointBuffer sample(final CompiledPlotExpression expression, final Domain domain,
                              final Resolution resolution, final EvaluationContext context) {
        final int samples = resolution.targetSamples();
        final PointBuffer buffer = new PointBuffer(samples);

        final double span = domain.maxX() - domain.minX();
        final double step = span / (samples - 1.0d);

        for (int index = 0; index < samples; index++) {
            final double x = domain.minX() + index * step;
            final double y = expression.evaluate(x, context);
            buffer.add(x, y);
        }

        return buffer;
    }
}
