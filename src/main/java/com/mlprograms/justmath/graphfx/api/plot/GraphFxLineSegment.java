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
 * Immutable line segment in world coordinates.
 * <p>
 * This is primarily used for implicit plots (e.g., level sets) where the geometry is represented as many small
 * independent line segments.
 *
 * <h2>Constraints</h2>
 * Segment endpoints must not be {@code null} and must be finite.
 *
 * @param start the start point in world coordinates
 * @param end   the end point in world coordinates
 *
 * @since 1.0
 */
public record GraphFxLineSegment(@NonNull GraphFxPoint start, @NonNull GraphFxPoint end) {

    /**
     * Canonical constructor that validates endpoints.
     *
     * @throws NullPointerException     if any endpoint is {@code null}
     * @throws IllegalArgumentException if any coordinate is {@code NaN} or infinite
     */
    public GraphFxLineSegment {
        Objects.requireNonNull(start, "start must not be null.");
        Objects.requireNonNull(end, "end must not be null.");

        if (!start.isFinite() || !end.isFinite()) {
            throw new IllegalArgumentException("Line segment endpoints must be finite (no NaN/Infinity).");
        }
    }

    /**
     * Returns the squared length of this segment.
     * <p>
     * This avoids a {@link Math#sqrt(double)} call and is useful for comparisons.
     *
     * @return squared length in world units
     */
    public double lengthSquared() {
        final double dx = end.x() - start.x();
        final double dy = end.y() - start.y();
        return dx * dx + dy * dy;
    }

    /**
     * Backward compatible alias for {@link #start()}.
     * <p>
     * Some earlier revisions of GraphFx represented line segments with endpoints named {@code a} and {@code b}.
     * The public API uses {@link #start()} and {@link #end()} for clarity, but internal code or older examples may
     * still call {@code a()}.
     *
     * @return the start point of this segment
     * @deprecated use {@link #start()} instead
     */
    @Deprecated(forRemoval = false)
    public GraphFxPoint a() {
        return start;
    }

    /**
     * Backward compatible alias for {@link #end()}.
     *
     * @return the end point of this segment
     * @deprecated use {@link #end()} instead
     */
    @Deprecated(forRemoval = false)
    public GraphFxPoint b() {
        return end;
    }
}
