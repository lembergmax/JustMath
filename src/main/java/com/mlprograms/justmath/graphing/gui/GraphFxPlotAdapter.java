package com.mlprograms.justmath.graphing.gui;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphfx.planar.model.PlotLine;
import com.mlprograms.justmath.graphfx.planar.model.PlotPoint;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import com.mlprograms.justmath.graphing.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that converts headless {@link GraphingCalculator} output into legacy GraphFx model objects.
 */
public final class GraphFxPlotAdapter {

    private final GraphingCalculator graphingCalculator;

    public GraphFxPlotAdapter(GraphingCalculator graphingCalculator) {
        this.graphingCalculator = graphingCalculator;
    }

    public PlotResult plotFunction(String expression, Domain domain, Resolution resolution) {
        PlotResponse response = graphingCalculator.plot(expression, domain, resolution);
        if (response.series().isEmpty()) {
            return new PlotResult();
        }

        PlotSeries plotSeries = response.series().getFirst();
        List<PlotPoint> points = new ArrayList<>(plotSeries.size());
        for (int i = 0; i < plotSeries.size(); i++) {
            points.add(new PlotPoint(
                    new BigNumber(Double.toString(plotSeries.xValues()[i])),
                    new BigNumber(Double.toString(plotSeries.yValues()[i]))
            ));
        }

        return new PlotResult(List.of(), List.of(new PlotLine(points)));
    }
}
