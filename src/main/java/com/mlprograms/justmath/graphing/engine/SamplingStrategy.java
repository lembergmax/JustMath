package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;
import com.mlprograms.justmath.graphing.api.PlotRequest;
import com.mlprograms.justmath.graphing.api.PlotSeries;

public interface SamplingStrategy {

    PlotSeries sample(CompiledPlotExpression expression, PlotRequest request);
}
