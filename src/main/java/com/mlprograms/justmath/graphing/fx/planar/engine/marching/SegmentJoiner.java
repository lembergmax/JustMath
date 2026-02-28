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

package com.mlprograms.justmath.graphing.fx.planar.engine.marching;

import com.mlprograms.justmath.graphing.fx.planar.model.PlotPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Joins unordered line segments into polylines.
 * <p>
 * This is a heuristic joiner that tries to stitch segment endpoints with a small epsilon tolerance.
 * It is designed for interactive plotting where speed matters more than perfectly optimal joins.
 * </p>
 */
public final class SegmentJoiner {

    /**
     * Epsilon tolerance used for joining endpoints.
     */
    private static final double EPSILON = 1e-6;

    /**
     * Joins raw segments into polylines.
     *
     * @param segments segments to join
     * @return list of polylines as ordered point lists
     */
    public List<List<PlotPoint>> join(final List<Segment> segments) {
        final List<Segment> remaining = new ArrayList<>(segments);
        final List<List<PlotPoint>> polylines = new ArrayList<>();

        while (!remaining.isEmpty()) {
            final Segment seed = remaining.remove(remaining.size() - 1);
            final List<PlotPoint> polyline = new ArrayList<>();
            polyline.add(seed.from());
            polyline.add(seed.to());

            boolean extended;
            do {
                extended = tryExtend(polyline, remaining);
            } while (extended);

            polylines.add(polyline);
        }

        return polylines;
    }

    /**
     * Attempts to extend the polyline by attaching any remaining segment to either end.
     */
    private boolean tryExtend(final List<PlotPoint> polyline, final List<Segment> remaining) {
        final PlotPoint head = polyline.get(0);
        final PlotPoint tail = polyline.get(polyline.size() - 1);

        for (int i = 0; i < remaining.size(); i++) {
            final Segment segment = remaining.get(i);

            if (SegmentUtils.equalsEpsilon(segment.from(), tail, EPSILON)) {
                polyline.add(segment.to());
                remaining.remove(i);
                return true;
            }
            if (SegmentUtils.equalsEpsilon(segment.to(), tail, EPSILON)) {
                polyline.add(segment.from());
                remaining.remove(i);
                return true;
            }
            if (SegmentUtils.equalsEpsilon(segment.to(), head, EPSILON)) {
                polyline.add(0, segment.from());
                remaining.remove(i);
                return true;
            }
            if (SegmentUtils.equalsEpsilon(segment.from(), head, EPSILON)) {
                polyline.add(0, segment.to());
                remaining.remove(i);
                return true;
            }
        }

        return false;
    }
}
