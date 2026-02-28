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

package com.mlprograms.justmath.graphing.fx.planar.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Container for plot primitives produced by a plotting engine.
 * <p>
 * The viewer currently uses {@link #plotLines()} for drawing. {@link #plotPoints()} is provided for
 * potential extensions (e.g. scatter plots).
 * </p>
 *
 * @param plotPoints point primitives (non-null)
 * @param plotLines  polyline primitives (non-null)
 */
public record PlotResult(List<PlotPoint> plotPoints, List<PlotLine> plotLines) {

    /**
     * Creates an empty plot result.
     */
    public PlotResult() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Validates lists are non-null.
     */
    public PlotResult {
        Objects.requireNonNull(plotPoints, "plotPoints must not be null");
        Objects.requireNonNull(plotLines, "plotLines must not be null");
    }
}
