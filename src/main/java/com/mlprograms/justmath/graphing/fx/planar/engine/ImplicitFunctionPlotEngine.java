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
import com.mlprograms.justmath.graphing.fx.planar.engine.marching.MarchingSquares;
import com.mlprograms.justmath.graphing.fx.planar.engine.marching.Segment;
import com.mlprograms.justmath.graphing.fx.planar.engine.marching.SegmentJoiner;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotLine;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotPoint;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotResult;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Plot engine for implicit equations based on a Marching Squares contour extraction.
 * <p>
 * The engine samples an implicit function {@code f(x,y)=0} over a regular grid and extracts a polyline contour
 * approximating the curve. It is intended for interactive viewing with pan/zoom.
 * </p>
 */
public final class ImplicitFunctionPlotEngine {

    /**
     * Marching squares algorithm implementation.
     */
    private final MarchingSquares marchingSquares;

    /**
     * Segment joiner that merges raw segments into polylines.
     */
    private final SegmentJoiner segmentJoiner;

    /**
     * Creates the engine with default components.
     */
    public ImplicitFunctionPlotEngine() {
        this(new MarchingSquares(), new SegmentJoiner());
    }

    /**
     * Creates the engine.
     *
     * @param marchingSquares marching squares (non-null)
     * @param segmentJoiner   segment joiner (non-null)
     */
    public ImplicitFunctionPlotEngine(@NonNull final MarchingSquares marchingSquares,
                                      @NonNull final SegmentJoiner segmentJoiner) {
        this.marchingSquares = marchingSquares;
        this.segmentJoiner = segmentJoiner;
    }

    /**
     * Plots the implicit function defined in {@link PlotData}.
     *
     * @param plotData plot input (non-null)
     * @return plot result containing polylines
     */
    public PlotResult plot(@NonNull final PlotData plotData) {
        final List<Segment> segments = marchingSquares.extractSegments(plotData);

        final List<List<PlotPoint>> polylines = segmentJoiner.join(segments);

        final List<PlotLine> lines = new ArrayList<>(polylines.size());
        for (final List<PlotPoint> polyline : polylines) {
            lines.add(new PlotLine(polyline));
        }

        return new PlotResult(List.of(), lines);
    }

    /**
     * Helper for creating a BigNumber from a double.
     *
     * @param value numeric value
     * @return BigNumber representing the value
     */
    static BigNumber bn(final double value) {
        return new BigNumber(Double.toString(value));
    }
}
