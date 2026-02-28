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

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphing.fx.planar.engine.PlotData;
import com.mlprograms.justmath.graphing.fx.planar.engine.expression.PlotExpression;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotPoint;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Marching Squares implementation extracting contour segments for {@code f(x,y)=0}.
 * <p>
 * This algorithm samples a scalar field on a grid, then, for each cell, determines whether a contour line crosses it.
 * The output is a set of {@link Segment} instances which are later stitched into polylines.
 * </p>
 */
public final class MarchingSquares {

    /**
     * Extracts contour segments for the given plot data.
     *
     * @param plotData plot data (non-null)
     * @return list of contour segments
     */
    public List<Segment> extractSegments(@NonNull final PlotData plotData) {
        final PlotExpression expression = plotData.getPlotExpression();

        final double minX = plotData.getMinX().doubleValue();
        final double maxX = plotData.getMaxX().doubleValue();
        final double minY = plotData.getMinY().doubleValue();
        final double maxY = plotData.getMaxY().doubleValue();
        final double cellSize = plotData.getCellSize().doubleValue();

        final int gridWidth = Math.max(2, (int) Math.ceil((maxX - minX) / cellSize) + 1);
        final int gridHeight = Math.max(2, (int) Math.ceil((maxY - minY) / cellSize) + 1);

        final double[][] values = new double[gridWidth][gridHeight];

        for (int gx = 0; gx < gridWidth; gx++) {
            for (int gy = 0; gy < gridHeight; gy++) {
                final double x = minX + gx * cellSize;
                final double y = minY + gy * cellSize;
                values[gx][gy] = expression.evaluate(x, y);
            }
        }

        final List<Segment> segments = new ArrayList<>();

        for (int gx = 0; gx < gridWidth - 1; gx++) {
            for (int gy = 0; gy < gridHeight - 1; gy++) {
                final double x0 = minX + gx * cellSize;
                final double y0 = minY + gy * cellSize;

                final double v00 = values[gx][gy];
                final double v10 = values[gx + 1][gy];
                final double v11 = values[gx + 1][gy + 1];
                final double v01 = values[gx][gy + 1];

                segments.addAll(extractCellSegments(x0, y0, cellSize, v00, v10, v11, v01));
            }
        }

        return segments;
    }

    /**
     * Extracts contour segments for a single cell using a simplified marching squares case table.
     */
    private List<Segment> extractCellSegments(final double x0,
                                              final double y0,
                                              final double cellSize,
                                              final double v00,
                                              final double v10,
                                              final double v11,
                                              final double v01) {
        final int caseIndex = (v00 >= 0 ? 1 : 0)
                | (v10 >= 0 ? 2 : 0)
                | (v11 >= 0 ? 4 : 0)
                | (v01 >= 0 ? 8 : 0);

        if (caseIndex == 0 || caseIndex == 15) {
            return List.of();
        }

        final BigNumber bx0 = new BigNumber(Double.toString(x0));
        final BigNumber by0 = new BigNumber(Double.toString(y0));
        final BigNumber bx1 = new BigNumber(Double.toString(x0 + cellSize));
        final BigNumber by1 = new BigNumber(Double.toString(y0 + cellSize));

        final BigNumber bv00 = new BigNumber(Double.toString(v00));
        final BigNumber bv10 = new BigNumber(Double.toString(v10));
        final BigNumber bv11 = new BigNumber(Double.toString(v11));
        final BigNumber bv01 = new BigNumber(Double.toString(v01));

        final PlotPoint p00 = new PlotPoint(bx0, by0);
        final PlotPoint p10 = new PlotPoint(bx1, by0);
        final PlotPoint p11 = new PlotPoint(bx1, by1);
        final PlotPoint p01 = new PlotPoint(bx0, by1);

        final List<PlotPoint> intersections = new ArrayList<>(2);

        if (signDiff(v00, v10)) {
            intersections.add(new PlotPoint(
                    SegmentUtils.interpolate(p00.x(), p10.x(), bv00, bv10),
                    p00.y()
            ));
        }
        if (signDiff(v10, v11)) {
            intersections.add(new PlotPoint(
                    p10.x(),
                    SegmentUtils.interpolate(p10.y(), p11.y(), bv10, bv11)
            ));
        }
        if (signDiff(v11, v01)) {
            intersections.add(new PlotPoint(
                    SegmentUtils.interpolate(p11.x(), p01.x(), bv11, bv01),
                    p11.y()
            ));
        }
        if (signDiff(v01, v00)) {
            intersections.add(new PlotPoint(
                    p01.x(),
                    SegmentUtils.interpolate(p01.y(), p00.y(), bv01, bv00)
            ));
        }

        if (intersections.size() < 2) {
            return List.of();
        }

        if (intersections.size() == 2) {
            return List.of(new Segment(intersections.get(0), intersections.get(1)));
        }

        final List<Segment> segments = new ArrayList<>();
        for (int i = 0; i + 1 < intersections.size(); i += 2) {
            segments.add(new Segment(intersections.get(i), intersections.get(i + 1)));
        }
        return segments;
    }

    /**
     * Determines whether two scalar values have different signs around zero.
     */
    private static boolean signDiff(final double a, final double b) {
        return (a >= 0 && b < 0) || (a < 0 && b >= 0);
    }
}
