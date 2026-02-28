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

package com.mlprograms.justmath.graphing.fx.planar.engine;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphing.fx.planar.engine.expression.PlotExpression;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable input data for {@link ImplicitFunctionPlotEngine}.
 * <p>
 * Defines world coordinate bounds, a cell size (sampling step), and the {@link PlotExpression} that represents
 * an equation. The engine will sample the equation on a grid and produce a contour line at value {@code 0}.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class PlotData {

    /**
     * Minimum x in world coordinates.
     */
    @NonNull
    BigNumber minX;

    /**
     * Maximum x in world coordinates.
     */
    @NonNull
    BigNumber maxX;

    /**
     * Minimum y in world coordinates.
     */
    @NonNull
    BigNumber minY;

    /**
     * Maximum y in world coordinates.
     */
    @NonNull
    BigNumber maxY;

    /**
     * Cell size in world units; controls grid resolution.
     */
    @NonNull
    BigNumber cellSize;

    /**
     * Expression representing the implicit function f(x,y) = left - right.
     */
    @NonNull
    PlotExpression plotExpression;
}
