package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphing.api.*;
import com.mlprograms.justmath.graphing.engine.cache.ExpressionCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default graphing calculator implementation for headless and GUI adapters.
 */
public final class DefaultGraphingCalculator implements GraphingCalculator {

    private final ExpressionCompiler compiler;
    private final SamplingEngine samplingEngine;
    private final ExpressionCache expressionCache;

    public DefaultGraphingCalculator() {
        this(new DefaultExpressionCompiler(new CalculatorEngine()), new DefaultSamplingEngine(), new ExpressionCache(512));
    }

    public DefaultGraphingCalculator(final ExpressionCompiler compiler,
                                     final SamplingEngine samplingEngine,
                                     final ExpressionCache expressionCache) {
        this.compiler = Objects.requireNonNull(compiler, "compiler");
        this.samplingEngine = Objects.requireNonNull(samplingEngine, "samplingEngine");
        this.expressionCache = Objects.requireNonNull(expressionCache, "expressionCache");
    }

    @Override
    public PlotResponse plot(final String expression, final Domain domain, final Resolution resolution) {
        return plot(new PlotRequest.Builder(expression).domain(domain).resolution(resolution).build());
    }

    @Override
    public PlotResponse plot(final PlotRequest request) {
        final CompiledPlotExpression expression = compile(request.expression());
        return new PlotResponse(List.of(sample(expression, request)));
    }

    @Override
    public List<PlotSeries> plotAll(final List<PlotRequest> requests) {
        final List<PlotSeries> result = new ArrayList<>(requests.size());
        for (PlotRequest request : requests) {
            result.add(plot(request).series().getFirst());
        }
        return result;
    }

    @Override
    public CompiledPlotExpression compile(final String expression) {
        final CompiledPlotExpression cached = expressionCache.get(expression);
        if (cached != null) {
            return cached;
        }

        final CompiledPlotExpression compiledExpression = compiler.compile(expression);
        expressionCache.put(expression, compiledExpression);
        return compiledExpression;
    }

    @Override
    public PlotSeries sample(final CompiledPlotExpression expression, final PlotRequest request) {
        return samplingEngine.sample(expression, request);
    }
}
