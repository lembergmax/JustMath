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

package com.mlprograms.justmath.graphing.api;

import java.util.Arrays;

/**
 * Immutable sampled point series.
 * <p>
 * A series represents one expression sampled into aligned {@code xValues[i], yValues[i]} pairs.
 * Arrays are defensively copied to keep the type immutable and safe to expose.
 * </p>
 *
 * @param expression the source expression (non-blank)
 * @param xValues    sampled x values (defensively copied)
 * @param yValues    sampled y values (defensively copied)
 */
public record PlotSeries(String expression, double[] xValues, double[] yValues) {

    /**
     * Validates and defensively copies array inputs.
     *
     * @throws IllegalArgumentException if expression is blank, arrays are null, or lengths differ
     */
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
