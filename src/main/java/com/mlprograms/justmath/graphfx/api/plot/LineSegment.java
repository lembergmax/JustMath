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

import com.mlprograms.justmath.graphfx.core.Point;
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
 */
public record LineSegment(@NonNull Point start, @NonNull Point end) {

    /**
     * Canonical constructor that validates endpoints.
     *
     * @throws NullPointerException     if any endpoint is {@code null}
     * @throws IllegalArgumentException if any coordinate is {@code NaN} or infinite
     */
    public LineSegment {
        Objects.requireNonNull(start, "start must not be null.");
        Objects.requireNonNull(end, "end must not be null.");

        if (!start.isFinite() || !end.isFinite()) {
            throw new IllegalArgumentException("Line segment endpoints must be finite (no NaN/Infinity).");
        }
    }

}
