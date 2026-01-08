package com.mlprograms.justmath.graphfx.planar.model;

import java.util.List;
import java.util.Objects;

public record PlotLine(List<PlotPoint> plotPoints) {

    public PlotLine {
        Objects.requireNonNull(plotPoints, "plotPoints must not be null");
    }
    
}
