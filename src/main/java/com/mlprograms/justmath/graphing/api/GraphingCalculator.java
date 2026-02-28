package com.mlprograms.justmath.graphing.api;

import java.util.List;

public interface GraphingCalculator {

    PlotResponse plot(String expression, Domain domain, Resolution resolution);

    PlotResponse plot(PlotRequest request);

    List<PlotSeries> plotAll(List<PlotRequest> requests);

    CompiledPlotExpression compile(String expression);

    PlotSeries sample(CompiledPlotExpression expression, PlotRequest request);
}
