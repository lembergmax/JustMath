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

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Builder;
import lombok.Value;

import java.util.Locale;

/**
 * Visual style configuration for the GraphFx planar viewer.
 *
 * <p>
 * This configuration is intentionally rendering-focused (colors, fonts, stroke widths, spacing).
 * It does not contain plot data and does not perform any computations.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class GraphFxViewerStyle {

    /**
     * Background color (canvas clear color).
     */
    @Builder.Default
    Color backgroundColor = Color.WHITE;

    /**
     * Minor grid line color.
     */
    @Builder.Default
    Color minorGridColor = Color.rgb(220, 220, 220);

    /**
     * Major grid line color.
     */
    @Builder.Default
    Color majorGridColor = Color.rgb(200, 200, 200);

    /**
     * Minor grid line width in pixels.
     */
    @Builder.Default
    double minorGridStrokeWidthInPixels = 1.0;

    /**
     * Major grid line width in pixels.
     */
    @Builder.Default
    double majorGridStrokeWidthInPixels = 1.4;

    /**
     * Axis color for x=0 and y=0.
     */
    @Builder.Default
    Color axisColor = Color.rgb(110, 110, 110);

    /**
     * Axis stroke width in pixels.
     */
    @Builder.Default
    double axisStrokeWidthInPixels = 2.0;

    /**
     * Tick label font used on axes.
     */
    @Builder.Default
    Font axisLabelFont = Font.font("Consolas", 12);

    /**
     * Tick label color.
     */
    @Builder.Default
    Color axisLabelColor = Color.rgb(40, 40, 40);

    /**
     * Locale for formatting axis numbers (decimal separator etc.).
     */
    @Builder.Default
    Locale axisLabelLocale = Locale.ROOT;

    /**
     * Plot line color (default for {@link com.mlprograms.justmath.graphfx.planar.model.PlotLine}).
     */
    @Builder.Default
    Color plotLineColor = Color.rgb(30, 30, 200);

    /**
     * Plot line width in pixels.
     */
    @Builder.Default
    double plotLineStrokeWidthInPixels = 2.0;

    /**
     * Plot point color (default for {@link com.mlprograms.justmath.graphfx.planar.model.PlotPoint}).
     */
    @Builder.Default
    Color plotPointColor = Color.rgb(200, 40, 40);

    /**
     * Plot point radius in pixels.
     */
    @Builder.Default
    double plotPointRadiusInPixels = 3.0;

}