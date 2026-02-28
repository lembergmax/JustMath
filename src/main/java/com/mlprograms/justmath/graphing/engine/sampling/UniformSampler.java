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

package com.mlprograms.justmath.graphing.engine.sampling;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;
import com.mlprograms.justmath.graphing.api.Domain;
import com.mlprograms.justmath.graphing.api.PlotEvaluationContext;
import com.mlprograms.justmath.graphing.api.Resolution;
import com.mlprograms.justmath.graphing.model.PointBuffer;
import lombok.NonNull;

/**
 * Uniform x-axis sampler with fixed step width.
 * <p>
 * This is the simplest and fastest strategy: it samples exactly {@code targetSamples} points
 * equally spaced across the domain.
 * </p>
 */
public final class UniformSampler implements SamplingStrategy {

    /**
     * Samples points uniformly spaced across the domain.
     *
     * @param expression compiled expression (non-null)
     * @param domain     domain (non-null)
     * @param resolution resolution (non-null)
     * @param context    evaluation context (non-null)
     * @return sampled points buffer
     */
    @Override
    public PointBuffer sample(@NonNull final CompiledPlotExpression expression,
                              @NonNull final Domain domain,
                              @NonNull final Resolution resolution,
                              @NonNull final PlotEvaluationContext context) {
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
