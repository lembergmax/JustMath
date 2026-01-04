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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable plot output produced by a {@link GraphFxPlotEngine}.
 * <p>
 * A plot can be represented either as:
 * <ul>
 *   <li>a <strong>polyline</strong> ({@link #polyline()}) for explicit functions such as {@code y = f(x)}</li>
 *   <li>a set of <strong>line segments</strong> ({@link #segments()}) for implicit plots such as {@code f(x,y)=0}</li>
 * </ul>
 *
 * <h2>Discontinuities</h2>
 * For polylines, discontinuities can be represented by inserting a non-finite point (e.g. {@code NaN}) into the
 * polyline list. Renderers should interpret this as a "pen lift" (break in the line).
 *
 * <h2>Immutability</h2>
 * The lists returned by this record are unmodifiable snapshots.
 *
 * @param polyline polyline vertices in world coordinates (may contain non-finite points as discontinuity markers)
 * @param segments explicit line segments in world coordinates (typically for implicit plots)
 *
 * @since 1.0
 */
public record GraphFxPlotGeometry(@NonNull List<GraphFxPoint> polyline, @NonNull List<GraphFxLineSegment> segments) {

    /**
     * Canonical constructor that validates and defensively copies list inputs.
     *
     * @throws NullPointerException     if any list is {@code null}
     * @throws IllegalArgumentException if any list contains {@code null} elements
     */
    public GraphFxPlotGeometry {
        Objects.requireNonNull(polyline, "polyline must not be null.");
        Objects.requireNonNull(segments, "segments must not be null.");

        validateNoNullElements(polyline, "polyline");
        validateNoNullElements(segments, "segments");

        polyline = Collections.unmodifiableList(new ArrayList<>(polyline));
        segments = Collections.unmodifiableList(new ArrayList<>(segments));
    }

    /**
     * Returns whether this geometry contains no drawable primitives.
     *
     * @return {@code true} if both polyline and segments are empty
     */
    public boolean isEmpty() {
        return polyline.isEmpty() && segments.isEmpty();
    }

    /**
     * Creates an empty geometry.
     *
     * @return an empty geometry (polyline and segments are empty)
     */
    public static GraphFxPlotGeometry empty() {
        return new GraphFxPlotGeometry(List.of(), List.of());
    }

    private static void validateNoNullElements(@NonNull final List<?> values, @NonNull final String parameterName) {
        for (int index = 0; index < values.size(); index++) {
            if (values.get(index) == null) {
                throw new IllegalArgumentException(
                        parameterName + " must not contain null elements (null at index " + index + ")."
                );
            }
        }
    }
}
