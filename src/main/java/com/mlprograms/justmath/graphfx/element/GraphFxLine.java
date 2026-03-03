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

package com.mlprograms.justmath.graphfx.element;

import com.mlprograms.justmath.bignumber.BigNumber;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable line segment element represented by two high-precision endpoints.
 *
 * <p>
 * The viewer does not compute slopes, intercepts, or equations. If you want to show an equation/value,
 * provide it explicitly via {@link #lineLabelText}.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class GraphFxLine implements GraphFxElement {

    /**
     * Human-readable name for the line.
     */
    @Builder.Default
    @NonNull
    String lineName = "";

    /**
     * Start X coordinate in world units.
     */
    @NonNull
    BigNumber startWorldXValue;

    /**
     * Start Y coordinate in world units.
     */
    @NonNull
    BigNumber startWorldYValue;

    /**
     * End X coordinate in world units.
     */
    @NonNull
    BigNumber endWorldXValue;

    /**
     * End Y coordinate in world units.
     */
    @NonNull
    BigNumber endWorldYValue;

    /**
     * Label text displayed near the line midpoint.
     *
     * <p>
     * This should contain the "corresponding values" you want to show (e.g. length, equation, parameter name),
     * computed elsewhere. The viewer will not compute it.
     * </p>
     */
    @Builder.Default
    String lineLabelText = null;

    /**
     * Line stroke color.
     */
    @Builder.Default
    @NonNull
    Color lineColor = Color.DARKORANGE;

    /**
     * Line stroke width in pixels.
     */
    @Builder.Default
    double lineStrokeWidthInPixels = 2.5;
}
