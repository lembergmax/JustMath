package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphing.api.*;
import com.mlprograms.justmath.graphing.engine.cache.ExpressionCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Default graphing core implementation.
 */
public final class DefaultGraphingCalculator implements GraphingCalculator {

    private static final int PARALLEL_THRESHOLD = 100_000;

    private final CalculatorEngine calculatorEngine;
    private final ExpressionCache expressionCache;

    public DefaultGraphingCalculator() {
        this(new CalculatorEngine(), new ExpressionCache(512));
    }

    DefaultGraphingCalculator(CalculatorEngine calculatorEngine, ExpressionCache expressionCache) {
        this.calculatorEngine = calculatorEngine;
        this.expressionCache = expressionCache;
    }

    @Override
    public PlotResponse plot(String expression, Domain domain, Resolution resolution) {
        return plot(new PlotRequest.Builder(expression).domain(domain).resolution(resolution).build());
    }

    @Override
    public PlotResponse plot(PlotRequest request) {
        long start = System.currentTimeMillis();
        CompiledPlotExpression compiled = compile(request.expression());
        PlotSeries series = sample(compiled, request);
        return new PlotResponse(List.of(series), System.currentTimeMillis() - start, request.samplingMode());
    }

    @Override
    public CompiledPlotExpression compile(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("expression must not be blank");
        }
        CompiledPlotExpression cached = expressionCache.get(expression);
        if (cached != null) {
            return cached;
        }
        CompiledPlotExpression compiled = new SimpleCompiledPlotExpression(expression.trim());
        expressionCache.put(expression, compiled);
        return compiled;
    }

    @Override
    public PlotSeries sample(CompiledPlotExpression compiledExpression, PlotRequest request) {
        if (!(compiledExpression instanceof SimpleCompiledPlotExpression simpleCompiledPlotExpression)) {
            throw new IllegalArgumentException("Unsupported compiled expression implementation");
        }

        return request.samplingMode() == SamplingMode.ADAPTIVE
                ? sampleAdaptive(simpleCompiledPlotExpression, request)
                : sampleUniform(simpleCompiledPlotExpression, request);
    }

    private PlotSeries sampleUniform(SimpleCompiledPlotExpression compiledExpression, PlotRequest request) {
        int count = request.resolution().sampleCount();
        double[] xs = new double[count];
        double[] ys = new double[count];
        List<Integer> breaks = new ArrayList<>();
        double step = (request.domain().maxX() - request.domain().minX()) / (count - 1.0);

        if (request.parallelEnabled() && count >= PARALLEL_THRESHOLD) {
            Map<Integer, Double> yMap = new ConcurrentHashMap<>(count);
            IntStream.range(0, count).parallel().forEach(i -> {
                double x = request.domain().minX() + i * step;
                yMap.put(i, evaluateAt(compiledExpression.expression(), x, request.variables()));
            });
            for (int i = 0; i < count; i++) {
                xs[i] = request.domain().minX() + i * step;
                ys[i] = yMap.get(i);
                if (!Double.isFinite(ys[i])) {
                    breaks.add(i);
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                double x = request.domain().minX() + i * step;
                xs[i] = x;
                ys[i] = evaluateAt(compiledExpression.expression(), x, request.variables());
                if (!Double.isFinite(ys[i])) {
                    breaks.add(i);
                }
            }
        }

        return new PlotSeries(xs, ys, breaks.stream().mapToInt(Integer::intValue).toArray());
    }

    private PlotSeries sampleAdaptive(SimpleCompiledPlotExpression compiledExpression, PlotRequest request) {
        List<Double> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();

        int initialSegments = Math.max(8, request.resolution().sampleCount() / 8);
        double minX = request.domain().minX();
        double maxX = request.domain().maxX();
        double segmentWidth = (maxX - minX) / initialSegments;

        for (int i = 0; i < initialSegments; i++) {
            double left = minX + i * segmentWidth;
            double right = left + segmentWidth;
            sampleAdaptiveSegment(compiledExpression.expression(), request.variables(), left, right, 0, xValues, yValues);
        }

        double[] xs = xValues.stream().mapToDouble(Double::doubleValue).toArray();
        double[] ys = yValues.stream().mapToDouble(Double::doubleValue).toArray();
        List<Integer> breaks = new ArrayList<>();
        for (int i = 0; i < ys.length; i++) {
            if (!Double.isFinite(ys[i])) {
                breaks.add(i);
            }
        }
        return new PlotSeries(xs, ys, breaks.stream().mapToInt(Integer::intValue).toArray());
    }

    private void sampleAdaptiveSegment(String expression,
                                       Map<String, String> variables,
                                       double left,
                                       double right,
                                       int depth,
                                       List<Double> xValues,
                                       List<Double> yValues) {
        double mid = (left + right) * 0.5;
        double yLeft = evaluateAt(expression, left, variables);
        double yMid = evaluateAt(expression, mid, variables);
        double yRight = evaluateAt(expression, right, variables);

        double interpolationMid = yLeft + (yRight - yLeft) * 0.5;
        double error = Math.abs(yMid - interpolationMid);

        if (depth >= 10 || error < 1e-3) {
            xValues.add(left);
            yValues.add(yLeft);
            xValues.add(mid);
            yValues.add(yMid);
            xValues.add(right);
            yValues.add(yRight);
            return;
        }

        sampleAdaptiveSegment(expression, variables, left, mid, depth + 1, xValues, yValues);
        sampleAdaptiveSegment(expression, variables, mid, right, depth + 1, xValues, yValues);
    }

    private double evaluateAt(String expression, double x, Map<String, String> variables) {
        Map<String, String> scopedVariables = new HashMap<>(variables);
        scopedVariables.put("x", Double.toString(x));

        try {
            BigNumber result = calculatorEngine.evaluate(expression, scopedVariables);
            return result.doubleValue();
        } catch (Exception e) {
            throw new GraphingEvaluationException("Failed evaluating expression at x=" + x, e);
        }
    }
}
