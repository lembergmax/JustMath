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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate plot data to be rendered by the viewer.
 *
 * <p>
 * This type is intentionally data-only. Calculation engines should produce a {@code PlotResult} and
 * pass it into the GUI module.
 * </p>
 *
 * <p>
 * The viewer can render:
 * </p>
 * <ul>
 *     <li>Points (markers) via {@link #plotPoints()}</li>
 *     <li>Lines (polylines) via {@link #plotLines()}</li>
 * </ul>
 *
 * @param plotPoints points to render (must not be null)
 * @param plotLines lines to render (must not be null)
 */
public record PlotResult(
        /** Plot markers in world coordinates. */
        List<PlotPoint> plotPoints,
        /** Plot polylines in world coordinates. */
        List<PlotLine> plotLines
) {

    /**
     * Creates an empty plot result (no points, no lines).
     */
    public PlotResult() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Validates that both collections are present.
     */
    public PlotResult {
        Objects.requireNonNull(plotPoints, "plotPoints must not be null");
        Objects.requireNonNull(plotLines, "plotLines must not be null");
    }

    /**
     * Creates a fluent builder for plot results.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder that reduces boilerplate when adding points and lines.
     */
    public static final class Builder {
        private final List<PlotPoint> plotPoints = new ArrayList<>();
        private final List<PlotLine> plotLines = new ArrayList<>();

        public Builder addPoint(final PlotPoint plotPoint) {
            plotPoints.add(Objects.requireNonNull(plotPoint, "plotPoint must not be null"));
            return this;
        }

        public Builder addPoint(final double x, final double y) {
            return addPoint(PlotPoint.of(x, y));
        }

        public Builder addLine(final PlotLine plotLine) {
            plotLines.add(Objects.requireNonNull(plotLine, "plotLine must not be null"));
            return this;
        }

        public Builder addLine(final PlotPoint... points) {
            return addLine(new PlotLine(Arrays.asList(points)));
        }

        public PlotResult build() {
            return new PlotResult(new ArrayList<>(plotPoints), new ArrayList<>(plotLines));
        }
    }
}
