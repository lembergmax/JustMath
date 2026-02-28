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

package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;
import com.mlprograms.justmath.graphing.api.PlotEvaluationContext;
import com.mlprograms.justmath.graphing.api.PlotRequest;
import com.mlprograms.justmath.graphing.api.PlotSeries;
import com.mlprograms.justmath.graphing.model.PointBuffer;
import lombok.NonNull;

/**
 * Default sampling engine delegating to request-level strategies.
 * <p>
 * This class is intentionally small and focused: it builds a stable evaluation context and delegates to
 * {@link com.mlprograms.justmath.graphing.engine.sampling.SamplingStrategy}.
 * </p>
 */
public final class DefaultSamplingEngine implements SamplingEngine {

    /**
     * Samples a compiled expression according to a plot request.
     *
     * @param expression compiled expression (must not be {@code null})
     * @param request    request (must not be {@code null})
     * @return sampled plot series
     */
    @Override
    public PlotSeries sample(@NonNull final CompiledPlotExpression expression, @NonNull final PlotRequest request) {
        final PlotEvaluationContext context = PlotEvaluationContext.of(request.variables());

        final PointBuffer points = request.samplingStrategy()
                .sample(expression, request.domain(), request.resolution(), context);

        return new PlotSeries(request.expression(), points.xValues(), points.yValues());
    }
}
