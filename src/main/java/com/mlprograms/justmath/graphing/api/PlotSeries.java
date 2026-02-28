package com.mlprograms.justmath.graphing.api;

/**
 * Immutable sampled function series.
 */
public record PlotSeries(double[] xValues, double[] yValues) {

    public PlotSeries {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("x/y value count mismatch");
        }
    }

    public int size() {
        return xValues.length;
    }
}
