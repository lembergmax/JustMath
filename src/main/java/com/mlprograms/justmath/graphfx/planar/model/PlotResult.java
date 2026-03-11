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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record PlotResult(List<PlotPoint> plotPoints, List<PlotLine> plotLines) {

    public PlotResult() {
        this(List.of(), List.of());
    }

    public PlotResult {
        Objects.requireNonNull(plotPoints, "plotPoints must not be null");
        Objects.requireNonNull(plotLines, "plotLines must not be null");
        plotPoints = List.copyOf(plotPoints);
        plotLines = List.copyOf(plotLines);
    }

    public static PlotResult ofLine(final PlotLine plotLine) {
        return new PlotResult(List.of(), List.of(plotLine));
    }

    public static PlotResult ofPoint(final PlotPoint plotPoint) {
        return new PlotResult(List.of(plotPoint), List.of());
    }

    public PlotResult withLine(final PlotLine plotLine) {
        final List<PlotLine> lines = new ArrayList<>(plotLines);
        lines.add(plotLine);
        return new PlotResult(plotPoints, lines);
    }

    public PlotResult withPoint(final PlotPoint plotPoint) {
        final List<PlotPoint> points = new ArrayList<>(plotPoints);
        points.add(plotPoint);
        return new PlotResult(points, plotLines);
    }

}
