package com.mlprograms.justmath.graphfx.planar.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record PlotResult(List<PlotPoint> plotPoints, List<PlotLine> plotLines) {

    public PlotResult() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public PlotResult {
        Objects.requireNonNull(plotPoints, "plotPoints must not be null");
        Objects.requireNonNull(plotLines, "plotLines must not be null");
    }

}
