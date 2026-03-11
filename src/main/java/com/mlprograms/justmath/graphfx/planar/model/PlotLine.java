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

package com.mlprograms.justmath.graphfx.planar.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record PlotLine(List<PlotPoint> plotPoints) {

    public PlotLine {
        Objects.requireNonNull(plotPoints, "plotPoints must not be null");
        plotPoints = List.copyOf(plotPoints);
    }

    public static PlotLine of(final List<PlotPoint> plotPoints) {
        return new PlotLine(plotPoints);
    }

    public static PlotLine of(final PlotPoint... plotPoints) {
        Objects.requireNonNull(plotPoints, "plotPoints must not be null");
        return new PlotLine(Arrays.asList(plotPoints));
    }

    public static PlotLine between(final PlotPoint first, final PlotPoint second) {
        return new PlotLine(List.of(first, second));
    }

    public static PlotLine fromDoubles(final double... coordinates) {
        Objects.requireNonNull(coordinates, "coordinates must not be null");
        if (coordinates.length < 4 || coordinates.length % 2 != 0) {
            throw new IllegalArgumentException("coordinates must contain pairs of x,y values and at least two points");
        }

        final PlotPoint[] points = new PlotPoint[coordinates.length / 2];
        for (int i = 0, j = 0; i < coordinates.length; i += 2, j++) {
            points[j] = PlotPoint.of(coordinates[i], coordinates[i + 1]);
        }

        return PlotLine.of(points);
    }

}
