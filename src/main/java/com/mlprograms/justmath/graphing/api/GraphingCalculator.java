package com.mlprograms.justmath.graphing.api;

/**
 * Public graphing API for headless consumers and GUI adapters.
 */
public interface GraphingCalculator {

    PlotResponse plot(String expression, Domain domain, Resolution resolution);

    PlotResponse plot(PlotRequest request);

    CompiledPlotExpression compile(String expression);

    PlotSeries sample(CompiledPlotExpression compiledExpression, PlotRequest request);
}
