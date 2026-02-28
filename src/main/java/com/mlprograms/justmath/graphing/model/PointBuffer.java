/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.graphing.model;

import java.util.Arrays;

/**
 * Primitive storage for sampled points.
 * <p>
 * This buffer is optimized for sampling performance and minimal allocations.
 * It assumes the caller knows the target capacity and fills it monotonically.
 * </p>
 */
public final class PointBuffer {

    /**
     * Stored x values.
     */
    private final double[] xValues;

    /**
     * Stored y values.
     */
    private final double[] yValues;

    /**
     * Current number of stored points.
     */
    private int size;

    /**
     * Creates a buffer with a fixed capacity.
     *
     * @param capacity number of points to store (must be >= 1)
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public PointBuffer(final int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.xValues = new double[capacity];
        this.yValues = new double[capacity];
        this.size = 0;
    }

    /**
     * Adds a point to the buffer.
     *
     * @param x x-value
     * @param y y-value
     */
    public void add(final double x, final double y) {
        xValues[size] = x;
        yValues[size] = y;
        size++;
    }

    /**
     * Returns a copy of the stored x values.
     *
     * @return x values
     */
    public double[] xValues() {
        return Arrays.copyOf(xValues, size);
    }

    /**
     * Returns a copy of the stored y values.
     *
     * @return y values
     */
    public double[] yValues() {
        return Arrays.copyOf(yValues, size);
    }
}
