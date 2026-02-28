package com.mlprograms.justmath.graphing.api;

import java.util.Arrays;

/**
 * Immutable sampled point series.
 */
public record PlotSeries(String expression, double[] xValues, double[] yValues) {

    public PlotSeries {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("expression must not be blank");
        }
        if (xValues == null || yValues == null || xValues.length != yValues.length) {
            throw new IllegalArgumentException("xValues/yValues must be non-null and same length");
        }
        xValues = Arrays.copyOf(xValues, xValues.length);
        yValues = Arrays.copyOf(yValues, yValues.length);
    }
}
