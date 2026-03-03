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

package com.mlprograms.justmath.graphfx.viewer;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.MathContext;
import java.util.Locale;

/**
 * Immutable configuration for {@link GraphFxViewer}.
 *
 * <p>
 * This configuration is part of the public library API. It focuses on:
 * </p>
 * <ul>
 *     <li>Visual styling (colors, fonts, stroke widths).</li>
 *     <li>Layout defaults (dynamic gutters, padding).</li>
 *     <li>Rendering stability (MathContext for internal ratios).</li>
 *     <li>Performance guards (maximum grid line count).</li>
 * </ul>
 *
 * <p>
 * The viewer itself does not perform domain calculations. The {@link MathContext} is used only for intermediate
 * divisions during coordinate mapping (screen ↔ world) to keep interactions stable with arbitrary precision inputs.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class GraphFxViewerConfiguration {

    /**
     * Default configuration used when callers do not provide custom settings.
     */
    public static final GraphFxViewerConfiguration DEFAULT = GraphFxViewerConfiguration.builder().build();

    /**
     * Background color of the entire viewer (including gutters).
     */
    @Builder.Default
    @NonNull
    Color backgroundColor = Color.WHITE;

    /**
     * Background color of the plot area.
     *
     * <p>
     * By default this matches {@link #backgroundColor}. You can set it to a different color if you want a
     * subtle contrast between plot area and gutters.
     * </p>
     */
    @Builder.Default
    @NonNull
    Color plotBackgroundColor = Color.WHITE;

    /**
     * Color used for grid lines.
     */
    @Builder.Default
    @NonNull
    Color gridColor = Color.rgb(220, 220, 220);

    /**
     * Stroke width (pixels) used for grid lines.
     */
    @Builder.Default
    double gridStrokeWidthInPixels = 1.0;

    /**
     * Color used for coordinate axes.
     */
    @Builder.Default
    @NonNull
    Color axesColor = Color.rgb(70, 70, 70);

    /**
     * Stroke width (pixels) used for axis lines.
     */
    @Builder.Default
    double axesStrokeWidthInPixels = 2.0;

    /**
     * Color used for tick labels and element labels.
     */
    @Builder.Default
    @NonNull
    Color labelColor = Color.rgb(25, 25, 25);

    /**
     * Background color used behind labels to keep them readable on top of grid lines.
     */
    @Builder.Default
    @NonNull
    Color labelBackgroundColor = Color.rgb(255, 255, 255, 0.85);

    /**
     * Font used for tick labels and element labels.
     */
    @Builder.Default
    @NonNull
    Font labelFont = Font.font("System", 12.0);

    /**
     * Whether axis names ("x"/"y" by default) are rendered.
     */
    @Builder.Default
    boolean isAxisNameVisible = true;

    /**
     * Font used for axis names.
     */
    @Builder.Default
    @NonNull
    Font axisNameFont = Font.font("System", 12.0);

    /**
     * Name rendered next to the x-axis.
     */
    @Builder.Default
    @NonNull
    String xAxisName = "x";

    /**
     * Name rendered next to the y-axis.
     */
    @Builder.Default
    @NonNull
    String yAxisName = "y";

    /**
 * Left plot padding (pixels).
 *
 * <p>
 * The plot rectangle remains layout-stable while panning/zooming. Tick labels are rendered inside the plot
 * area (grid cells), so no dynamic gutters are required. This value defines the left padding between the
 * plot area and the viewer border.
 * </p>
 */
@Builder.Default
double minimumLeftGutterInPixels = 12.0;

    /**
 * Bottom plot padding (pixels).
 *
 * <p>
 * The plot rectangle remains layout-stable while panning/zooming. Tick labels are rendered inside the plot
 * area (grid cells), so no dynamic gutters are required. This value defines the bottom padding between the
 * plot area and the viewer border.
 * </p>
 */
@Builder.Default
double minimumBottomGutterInPixels = 12.0;

    /**
 * Optional inner padding (pixels) used for label placement and small UI offsets.
 *
 * <p>
 * This value is kept as part of the public API for styling consistency. The current viewer uses a dedicated
 * inset for grid coordinate labels, but callers may still reuse this value in their own UI integration.
 * </p>
 */
@Builder.Default
double gutterInnerPaddingInPixels = 10.0;

    /**
     * Top padding (pixels) used above the plot area.
     */
    @Builder.Default
    double topPaddingInPixels = 10.0;

    /**
     * Right padding (pixels) used to the right of the plot area.
     *
     * <p>
     * This prevents element labels from touching the stage border when near the right edge.
     * </p>
     */
    @Builder.Default
    double rightPaddingInPixels = 10.0;

    /**
     * Locale used for tick label formatting (decimal separator).
     *
     * <p>
     * The viewer preserves the {@link com.mlprograms.justmath.bignumber.BigNumber} string representation and only
     * adapts the decimal separator.
     * </p>
     */
    @Builder.Default
    @NonNull
    Locale labelLocale = Locale.US;

    /**
     * MathContext used for BigNumber division during rendering.
     */
    @Builder.Default
    @NonNull
    MathContext renderingMathContext = MathContext.DECIMAL128;

    /**
     * Maximum number of grid lines/ticks per axis.
     *
     * <p>
     * This is a safety guard against rendering freezes when callers choose extremely small grid spacings.
     * </p>
     */
    @Builder.Default
    int maximumGridLineCountPerAxis = 750;

    /**
     * Zoom factor applied when scrolling up.
     *
     * <p>
     * Values smaller than {@code 1.0} zoom in (reduce viewport size).
     * </p>
     */
    @Builder.Default
    double zoomInFactor = 0.9;

    /**
     * Zoom factor applied when scrolling down.
     *
     * <p>
     * Values larger than {@code 1.0} zoom out (increase viewport size).
     * </p>
     */
    @Builder.Default
    double zoomOutFactor = 1.1;

    /**
     * Validates the configuration and returns a safe configuration instance.
     *
     * <p>
     * This method is intentionally permissive: it clamps unsafe values rather than throwing. The purpose is to make
     * the library robust against accidental misconfiguration in consuming applications.
     * </p>
     *
     * @return a validated configuration instance (never null)
     */
    public GraphFxViewerConfiguration validate() {
        double safeGridStroke = clamp(gridStrokeWidthInPixels, 0.5, 10.0);
        double safeAxesStroke = clamp(axesStrokeWidthInPixels, 0.5, 10.0);

        double safeMinLeftGutter = clamp(minimumLeftGutterInPixels, 0.0, 1000.0);
        double safeMinBottomGutter = clamp(minimumBottomGutterInPixels, 0.0, 1000.0);

        double safeGutterPadding = clamp(gutterInnerPaddingInPixels, 0.0, 250.0);
        double safeTopPadding = clamp(topPaddingInPixels, 0.0, 250.0);
        double safeRightPadding = clamp(rightPaddingInPixels, 0.0, 250.0);

        int safeMaxLines = Math.max(10, maximumGridLineCountPerAxis);

        double safeZoomIn = clamp(zoomInFactor, 0.05, 0.999);
        double safeZoomOut = clamp(zoomOutFactor, 1.001, 20.0);

        return toBuilder()
                .gridStrokeWidthInPixels(safeGridStroke)
                .axesStrokeWidthInPixels(safeAxesStroke)
                .minimumLeftGutterInPixels(safeMinLeftGutter)
                .minimumBottomGutterInPixels(safeMinBottomGutter)
                .gutterInnerPaddingInPixels(safeGutterPadding)
                .topPaddingInPixels(safeTopPadding)
                .rightPaddingInPixels(safeRightPadding)
                .maximumGridLineCountPerAxis(safeMaxLines)
                .zoomInFactor(safeZoomIn)
                .zoomOutFactor(safeZoomOut)
                .build();
    }

    /**
     * Clamps a value to an inclusive range.
     *
     * @param value current value
     * @param min minimum allowed value
     * @param max maximum allowed value
     * @return clamped value
     */
    private static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) {
            return min;
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
