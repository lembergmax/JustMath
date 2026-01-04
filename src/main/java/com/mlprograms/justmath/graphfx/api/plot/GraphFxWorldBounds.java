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

package com.mlprograms.justmath.graphfx.api.plot;

import com.mlprograms.justmath.graphfx.core.GraphFxPoint;
import lombok.NonNull;

import java.util.Objects;

/**
 * Immutable axis-aligned bounds in world coordinates.
 * <p>
 * World space is the mathematical coordinate system used for plotting:
 * X increases to the right and Y increases upwards.
 *
 * <h2>Normalization</h2>
 * The constructor accepts any order of coordinates. Internally, values are normalized so that:
 * <ul>
 *   <li>{@code minX <= maxX}</li>
 *   <li>{@code minY <= maxY}</li>
 * </ul>
 *
 * <h2>Constraints</h2>
 * All values must be finite (not {@code NaN} and not infinite).
 *
 * @param minX the minimum x value (inclusive)
 * @param maxX the maximum x value (inclusive)
 * @param minY the minimum y value (inclusive)
 * @param maxY the maximum y value (inclusive)
 *
 * @since 1.0
 */
public record GraphFxWorldBounds(double minX, double maxX, double minY, double maxY) {

    /**
     * Canonical constructor that validates and normalizes the bounds.
     *
     * @throws IllegalArgumentException if any coordinate is {@code NaN} or infinite
     */
    public GraphFxWorldBounds {
        if (!isFinite(minX) || !isFinite(maxX) || !isFinite(minY) || !isFinite(maxY)) {
            throw new IllegalArgumentException("World bounds must be finite (no NaN/Infinity).");
        }

        final double normalizedMinX = Math.min(minX, maxX);
        final double normalizedMaxX = Math.max(minX, maxX);

        final double normalizedMinY = Math.min(minY, maxY);
        final double normalizedMaxY = Math.max(minY, maxY);

        minX = normalizedMinX;
        maxX = normalizedMaxX;
        minY = normalizedMinY;
        maxY = normalizedMaxY;
    }

    /**
     * Returns a normalized version of this bounds.
     * <p>
     * Because {@link GraphFxWorldBounds} normalizes values in the canonical constructor, instances are always
     * normalized already. This method therefore exists mainly for API compatibility and fluent usage.
     *
     * @return this instance (already normalized)
     */
    public GraphFxWorldBounds normalized() {
        return this;
    }

    /**
     * Returns the width of this bounds ({@code maxX - minX}).
     *
     * @return width in world units (can be {@code 0})
     */
    public double width() {
        return maxX - minX;
    }

    /**
     * Returns the height of this bounds ({@code maxY - minY}).
     *
     * @return height in world units (can be {@code 0})
     */
    public double height() {
        return maxY - minY;
    }

    /**
     * Returns the center point of this bounds.
     *
     * @return the bounds center (finite)
     */
    public GraphFxPoint center() {
        return new GraphFxPoint((minX + maxX) / 2.0, (minY + maxY) / 2.0);
    }

    /**
     * Checks whether a point lies inside this bounds (inclusive).
     *
     * @param point the point to test
     * @return {@code true} if the point is inside; {@code false} otherwise
     * @throws NullPointerException if {@code point} is {@code null}
     */
    public boolean contains(@NonNull final GraphFxPoint point) {
        Objects.requireNonNull(point, "point must not be null.");
        return point.x() >= minX && point.x() <= maxX && point.y() >= minY && point.y() <= maxY;
    }

    private static boolean isFinite(final double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
