/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.graph.fx.model;

import lombok.NonNull;
import lombok.Value;

/**
 * Immutable 2D point used for graph sampling and rendering.
 * <p>
 * Coordinates are stored as {@code double} for fast rendering and interoperability
 * with JavaFX/Swing drawing APIs.
 */
@Value
public class GraphPoint {

    double x;
    double y;

    /**
     * Creates a new point.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public GraphPoint(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a new point.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return point
     */
    @NonNull
    public static GraphPoint of(final double x, final double y) {
        return new GraphPoint(x, y);
    }

    /**
     * @return {@code true} if both coordinates are finite (not NaN/Infinity)
     */
    public boolean isFinite() {
        return Double.isFinite(x) && Double.isFinite(y);
    }

    /**
     * Computes the euclidean distance to another point.
     *
     * @param other other point
     * @return distance
     */
    public double distanceTo(@NonNull final GraphPoint other) {
        final double dx = x - other.x;
        final double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * @return a JavaFX {@code Point2D} instance with the same coordinates
     */
    @NonNull
    public javafx.geometry.Point2D toFxPoint() {
        return new javafx.geometry.Point2D(x, y);
    }

}
