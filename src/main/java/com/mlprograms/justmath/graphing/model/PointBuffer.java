package com.mlprograms.justmath.graphing.model;

import java.util.Arrays;

/**
 * Primitive storage for sampled points.
 */
public final class PointBuffer {

    private final double[] xValues;
    private final double[] yValues;
    private int size;

    public PointBuffer(final int capacity) {
        this.xValues = new double[capacity];
        this.yValues = new double[capacity];
    }

    public void add(final double x, final double y) {
        xValues[size] = x;
        yValues[size] = y;
        size++;
    }

    public double[] xValues() {
        return Arrays.copyOf(xValues, size);
    }

    public double[] yValues() {
        return Arrays.copyOf(yValues, size);
    }
}
