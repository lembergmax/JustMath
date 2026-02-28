package com.mlprograms.justmath.graphing.api;

import java.util.List;

/**
 * Response wrapper for one or more plotted series.
 */
public record PlotResponse(List<PlotSeries> series) {
}
