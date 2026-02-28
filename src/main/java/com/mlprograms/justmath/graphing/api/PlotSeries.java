package com.mlprograms.justmath.graphing.api;

import java.util.Arrays;

/**
 * Plot data represented by primitive arrays for low-allocation rendering.
 */
public record PlotSeries(double[] xValues, double[] yValues, int[] segmentBreakIndices) {

    public PlotSeries {
        if (xValues == null || yValues == null || segmentBreakIndices == null) {
            throw new IllegalArgumentException("series arrays must not be null");
        }
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("x/y arrays must have the same length");
        }
    }

    public int size() {
        return xValues.length;
    }

    public PlotSeries copy() {
        return new PlotSeries(
                Arrays.copyOf(xValues, xValues.length),
                Arrays.copyOf(yValues, yValues.length),
                Arrays.copyOf(segmentBreakIndices, segmentBreakIndices.length)
        );
    }
}
