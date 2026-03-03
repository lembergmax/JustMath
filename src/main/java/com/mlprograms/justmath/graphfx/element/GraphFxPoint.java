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
 * Immutable point element represented by high-precision world coordinates.
 *
 * <p>
 * The viewer does not modify or compute coordinates. Coordinates are treated as authoritative input data.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class GraphFxPoint implements GraphFxElement {

    /**
     * Display name shown next to the point.
     *
     * <p>
     * This is purely a label. The viewer does not derive any values from it.
     * </p>
     */
    @Builder.Default
    @NonNull
    String pointName = "";

    /**
     * X coordinate in world units.
     */
    @NonNull
    BigNumber worldXValue;

    /**
     * Y coordinate in world units.
     */
    @NonNull
    BigNumber worldYValue;

    /**
     * Point fill color.
     */
    @Builder.Default
    @NonNull
    Color pointColor = Color.BLACK;

    /**
     * Point radius in pixels.
     */
    @Builder.Default
    double pointRadiusInPixels = 4.0;
}
