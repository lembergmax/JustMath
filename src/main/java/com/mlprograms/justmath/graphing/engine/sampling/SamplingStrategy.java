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

/**
 * Strategy interface for sampling plot points.
 * <p>
 * Strategies may implement adaptive sampling, discontinuity detection, or any other heuristics.
 * </p>
 */
public interface SamplingStrategy {

    /**
     * Samples an expression over a domain using a given resolution and evaluation context.
     *
     * @param expression compiled expression (non-null)
     * @param domain     domain (non-null)
     * @param resolution resolution (non-null)
     * @param context    evaluation context (non-null)
     * @return sampled points buffer (never {@code null})
     */
    PointBuffer sample(CompiledPlotExpression expression, Domain domain, Resolution resolution, PlotEvaluationContext context);
}
