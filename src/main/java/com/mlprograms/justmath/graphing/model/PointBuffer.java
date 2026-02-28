package com.mlprograms.justmath.graphing.model;

import java.util.Arrays;

/**
 * Primitive buffer optimized for point sampling.
 */
public final class PointBuffer {

    private double[] xValues;
    private double[] yValues;
    private int size;

    public PointBuffer(final int expectedCapacity) {
        final int capacity = Math.max(2, expectedCapacity);
        this.xValues = new double[capacity];
        this.yValues = new double[capacity];
    }

    public void add(final double x, final double y) {
        ensureCapacity(size + 1);
        xValues[size] = x;
        yValues[size] = y;
        size++;
    }

    private void ensureCapacity(final int minCapacity) {
        if (minCapacity <= xValues.length) {
            return;
        }

        int newCapacity = xValues.length + (xValues.length >> 1);
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }

        xValues = Arrays.copyOf(xValues, newCapacity);
        yValues = Arrays.copyOf(yValues, newCapacity);
    }

    public double[] xValues() {
        return Arrays.copyOf(xValues, size);
    }

    public double[] yValues() {
        return Arrays.copyOf(yValues, size);
    }

    public int size() {
        return size;
    }
}
