/*
 * Copyright (c) 2025 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.graphfx.core;

/**
 * Immutable 2D point used by GraphFx's UI-agnostic plotting API.
 * <p>
 * This record intentionally does <strong>not</strong> depend on JavaFX types like {@code Point2D}. It can therefore be
 * used in headless contexts (server-side calculations, testing, exporting) and by non-JavaFX renderers.
 *
 * <h2>Discontinuities</h2>
 * Plot engines may emit non-finite points (NaN / infinity) as "break markers" to indicate discontinuities in an
 * otherwise continuous polyline. Renderers should treat non-finite points as a "pen lift".
 *
 * @param x x-coordinate (world units)
 * @param y y-coordinate (world units)
 */
public record Point(double x, double y) {

    /**
     * Returns whether both coordinates are finite (not {@code NaN} and not infinite).
     *
     * @return {@code true} if this point is finite
     */
    public boolean isFinite() {
        return isFinite(x) && isFinite(y);
    }

    private static boolean isFinite(final double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

}
