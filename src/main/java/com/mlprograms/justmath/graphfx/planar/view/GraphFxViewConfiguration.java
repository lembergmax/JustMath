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
package com.mlprograms.justmath.graphfx.planar.view;

import lombok.Builder;
import lombok.Value;

/**
 * Grid and interaction configuration for the planar viewer.
 *
 * <p>
 * The grid step is chosen automatically from the current zoom (pixels-per-world-unit) such that the
 * on-screen pixel distance stays close to {@link #targetMinorGridSpacingInPixels}.
 * </p>
 *
 * <p>
 * A key design goal is stability while panning and zooming: grid lines and axis labels are aligned
 * to world-space multiples of the computed step size.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class GraphFxViewConfiguration {

    /**
     * Whether the grid is rendered.
     */
    @Builder.Default
    boolean gridVisible = true;

    /**
     * Whether axes (x=0, y=0) are rendered if they are within the viewport.
     */
    @Builder.Default
    boolean axesVisible = true;

    /**
     * Whether tick labels are rendered on the axes.
     */
    @Builder.Default
    boolean axisLabelsVisible = true;

    /**
     * Target spacing for minor grid lines in pixels (auto-step selection).
     */
    @Builder.Default
    double targetMinorGridSpacingInPixels = 90.0;

    /**
     * Number of minor grid lines per major grid line.
     *
     * <p>
     * Example: 5 → every 5th minor line is rendered as a major line.
     * </p>
     */
    @Builder.Default
    int minorLinesPerMajorLine = 5;

    /**
     * Minimum pixel spacing between axis labels to avoid overlaps (auto-skip).
     *
     * <p>
     * Smaller values increase label density. For typical fonts, 40–60 pixels works well.
     * </p>
     */
    @Builder.Default
    double minimumAxisLabelSpacingInPixels = 45.0;

    /**
     * Tick mark length in pixels (drawn on the axes at label positions).
     */
    @Builder.Default
    double axisTickLengthInPixels = 8.0;

    /**
     * Label offset from the axis in pixels (distance between tick and text).
     */
    @Builder.Default
    double axisLabelOffsetInPixels = 12.0;

    /**
     * Minimum allowed zoom (pixels per world unit).
     */
    @Builder.Default
    double minimumPixelsPerWorldUnit = 10.0;

    /**
     * Maximum allowed zoom (pixels per world unit).
     */
    @Builder.Default
    double maximumPixelsPerWorldUnit = 5000.0;

    /**
     * Mouse wheel zoom sensitivity exponent used in {@code pow(exponent, wheelDelta)}.
     */
    @Builder.Default
    double mouseWheelZoomExponent = 1.0016;

    /**
     * Whether pan is enabled via left mouse drag.
     */
    @Builder.Default
    boolean panEnabled = true;

    /**
     * Whether zoom is enabled via mouse wheel.
     */
    @Builder.Default
    boolean zoomEnabled = true;
}
