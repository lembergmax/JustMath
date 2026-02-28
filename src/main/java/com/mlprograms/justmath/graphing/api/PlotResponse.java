package com.mlprograms.justmath.graphing.api;

import java.util.List;

/**
 * Result object for one plotting call.
 */
public record PlotResponse(List<PlotSeries> series, long elapsedMillis, SamplingMode samplingMode) {

    public PlotResponse {
        if (series == null || samplingMode == null) {
            throw new IllegalArgumentException("series and samplingMode must not be null");
        }
    }
}
