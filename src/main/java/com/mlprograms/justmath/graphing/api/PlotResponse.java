package com.mlprograms.justmath.graphing.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result container for one or more plotted expressions.
 */
public record PlotResponse(List<PlotSeries> series) {

    public PlotResponse {
        if (series == null) {
            throw new IllegalArgumentException("series must not be null");
        }
        series = Collections.unmodifiableList(new ArrayList<>(series));
    }
}
