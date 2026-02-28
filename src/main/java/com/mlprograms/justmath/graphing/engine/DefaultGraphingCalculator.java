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

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphing.api.*;
import com.mlprograms.justmath.graphing.engine.cache.ExpressionCache;
import lombok.NonNull;

import java.util.List;

/**
 * Default {@link GraphingCalculator} implementation.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Compile expressions (with caching)</li>
 *     <li>Delegate sampling to a {@link SamplingEngine}</li>
 *     <li>Provide a stable, ergonomic façade API</li>
 * </ul>
 * </p>
 */
public final class DefaultGraphingCalculator implements GraphingCalculator {

    /**
     * Compiler used to create {@link CompiledPlotExpression} instances.
     */
    private final ExpressionCompiler compiler;

    /**
     * Engine used to sample points for a compiled expression.
     */
    private final SamplingEngine samplingEngine;

    /**
     * LRU cache for compiled expressions.
     */
    private final ExpressionCache expressionCache;

    /**
     * Creates a default calculator with conservative defaults.
     */
    public DefaultGraphingCalculator() {
        this(new DefaultExpressionCompiler(new CalculatorEngine()),
                new DefaultSamplingEngine(),
                new ExpressionCache(512));
    }

    /**
     * Creates a calculator using the provided internal components.
     *
     * @param compiler        expression compiler (must not be {@code null})
     * @param samplingEngine  sampling engine (must not be {@code null})
     * @param expressionCache cache for compiled expressions (must not be {@code null})
     */
    public DefaultGraphingCalculator(@NonNull final ExpressionCompiler compiler,
                                     @NonNull final SamplingEngine samplingEngine,
                                     @NonNull final ExpressionCache expressionCache) {
        this.compiler = compiler;
        this.samplingEngine = samplingEngine;
        this.expressionCache = expressionCache;
    }

    /**
     * Plots a single request by compiling (or retrieving from cache) and sampling.
     *
     * @param request plot request (must not be {@code null})
     * @return plot response containing exactly one series
     */
    @Override
    public PlotResponse plot(@NonNull final PlotRequest request) {
        final CompiledPlotExpression expression = compile(request.expression());
        final PlotSeries series = sample(expression, request);
        return new PlotResponse(List.of(series));
    }

    /**
     * Compiles an expression into a reusable evaluator, using the cache if possible.
     *
     * @param expression expression string (may be {@code null}; validation happens in compiler)
     * @return compiled expression
     */
    @Override
    public CompiledPlotExpression compile(final String expression) {
        final CompiledPlotExpression cached = expressionCache.get(expression);
        if (cached != null) {
            return cached;
        }

        final CompiledPlotExpression compiled = compiler.compile(expression);
        expressionCache.put(expression, compiled);
        return compiled;
    }

    /**
     * Samples a compiled expression according to a request.
     *
     * @param expression compiled expression (must not be {@code null})
     * @param request    plot request (must not be {@code null})
     * @return sampled series
     */
    @Override
    public PlotSeries sample(@NonNull final CompiledPlotExpression expression, @NonNull final PlotRequest request) {
        return samplingEngine.sample(expression, request);
    }
}
