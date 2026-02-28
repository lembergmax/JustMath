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

package com.mlprograms.justmath.graphing.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * High-level facade for compiling and sampling plot expressions.
 * <p>
 * Implementations should be thread-safe and may internally cache compiled expressions.
 * </p>
 */
public interface GraphingCalculator {

    /**
     * Plots a single expression using explicit parameters.
     *
     * @param expression expression string (non-blank)
     * @param domain     x-domain (non-null)
     * @param resolution sampling resolution (non-null)
     * @return plot response containing one series
     */
    default PlotResponse plot(final String expression, final Domain domain, final Resolution resolution) {
        Objects.requireNonNull(domain, "domain must not be null");
        Objects.requireNonNull(resolution, "resolution must not be null");
        return plot(PlotRequest.builder(expression).domain(domain).resolution(resolution).build());
    }

    /**
     * Plots a single request.
     *
     * @param request plot request (non-null)
     * @return plot response
     */
    PlotResponse plot(PlotRequest request);

    /**
     * Plots multiple requests.
     *
     * @param requests requests (non-null)
     * @return list of plot series (one per request)
     */
    default List<PlotSeries> plotAll(final List<PlotRequest> requests) {
        Objects.requireNonNull(requests, "requests must not be null");
        final List<PlotSeries> result = new ArrayList<>(requests.size());
        for (final PlotRequest request : requests) {
            result.add(plot(request).series().get(0));
        }
        return result;
    }

    /**
     * Compiles an expression into a reusable evaluator.
     *
     * @param expression expression string (non-blank)
     * @return compiled expression
     */
    CompiledPlotExpression compile(String expression);

    /**
     * Samples a compiled expression according to a request.
     *
     * @param expression compiled expression (non-null)
     * @param request    plot request (non-null)
     * @return sampled series
     */
    PlotSeries sample(CompiledPlotExpression expression, PlotRequest request);
}
