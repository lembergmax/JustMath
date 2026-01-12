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

package com.mlprograms.justmath.graphfx.planar.calculator;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.graphfx.ReservedVariables;
import com.mlprograms.justmath.graphfx.planar.model.PlotLine;
import com.mlprograms.justmath.graphfx.planar.model.PlotPoint;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import com.mlprograms.justmath.graphfx.planar.view.ViewportSnapshot;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static com.mlprograms.justmath.bignumber.BigNumbers.TWO;
import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;

/**
 * Evaluates plot expressions for GraphFx and converts them into drawable primitives.
 * <p>
 * This engine implements an "implicit contour" plotter based on Marching Squares:
 * it samples a scalar field {@code f(x,y)} on a grid and extracts the contour line(s)
 * where {@code f(x,y)=0}.
 * <p>
 * Performance-relevant implementation notes:
 * <ul>
 *     <li><b>Row-wise sampling</b>: values are evaluated row-by-row, reusing the previously computed row.</li>
 *     <li><b>Early cell skipping</b>: cells whose corner signs are strictly equal are skipped.</li>
 *     <li><b>Ambiguity resolution</b> (masks 5 and 10): uses a center sample to disambiguate saddle cases.</li>
 *     <li><b>Segment stitching</b>: per-cell segments are merged into longer polylines to reduce draw calls.</li>
 *     <li><b>Reduced hot-path allocations</b>: the stitching stage avoids boxing (primitive adjacency lists)
 *     and avoids iterator creation for "previous point" tracking.</li>
 * </ul>
 */
public final class GraphFxCalculatorEngine {

    /**
     * Hard limit for grid points to prevent UI freezes with extremely small cell sizes.
     * <p>
     * Note: this limit is based on the number of sampled points (axis length product),
     * not on the number of cells or generated segments.
     */
    private static final int MAXIMUM_GRID_POINT_COUNT = 2_000_000;

    /**
     * Factor that defines the stitching tolerance relative to the grid cell size.
     * <p>
     * Endpoints that are closer than {@code cellSize * STITCH_TOLERANCE_FACTOR} are considered identical
     * for the purpose of stitching segments into polylines.
     */
    private static final double STITCH_TOLERANCE_FACTOR = 1e-2;

    /**
     * Absolute minimum tolerance for stitching in world units.
     * <p>
     * This prevents degenerate quantization or division-by-zero if the viewport cell size
     * is extremely small.
     */
    private static final double MINIMUM_STITCH_TOLERANCE = 1e-12;

    /**
     * Cached reserved variable name for {@code x}.
     * <p>
     * This avoids repeated enum access and string retrieval in hot loops.
     */
    private static final String X_VARIABLE_NAME = ReservedVariables.X.getValue();

    /**
     * Cached reserved variable name for {@code y}.
     * <p>
     * This avoids repeated enum access and string retrieval in hot loops.
     */
    private static final String Y_VARIABLE_NAME = ReservedVariables.Y.getValue();

    /**
     * Underlying evaluator for mathematical expressions.
     * <p>
     * It must support variables {@code x} and {@code y} via the provided variable map.
     * <p>
     * Important: the dominant runtime cost of plotting is typically inside this engine,
     * because it is invoked for each sampled grid point.
     */
    private final CalculatorEngine calculatorEngine;

    /**
     * Creates a plot engine with a default {@link CalculatorEngine} configuration.
     * <p>
     * The defaults are intentionally conservative and deterministic:
     * {@link MathContext} precision 32 with {@link RoundingMode#HALF_UP} and radians mode.
     */
    public GraphFxCalculatorEngine() {
        this(new CalculatorEngine(new MathContext(32, RoundingMode.HALF_UP), TrigonometricMode.RAD));
    }

    /**
     * Creates a plot engine using a caller-provided {@link CalculatorEngine}.
     *
     * @param calculatorEngine the calculator engine used for evaluating expressions (must not be null)
     */
    public GraphFxCalculatorEngine(final CalculatorEngine calculatorEngine) {
        this.calculatorEngine = Objects.requireNonNull(calculatorEngine, "calculatorEngine must not be null");
    }

    /**
     * Evaluates an expression for the given viewport without additional user variables.
     *
     * @param expression       the expression to evaluate (must not be null/blank)
     * @param viewportSnapshot viewport bounds and grid step (must not be null)
     * @return a {@link PlotResult} containing lines for drawing
     */
    public PlotResult evaluate(final String expression, final ViewportSnapshot viewportSnapshot) {
        return evaluate(expression, Map.of(), viewportSnapshot);
    }

    /**
     * Evaluates an expression for the given viewport.
     * <p>
     * The expression is treated as an implicit contour {@code f(x,y)=0}.
     *
     * @param expression       the expression to evaluate (must not be null/blank)
     * @param variables        custom variable bindings (must not contain reserved {@code x}/{@code y})
     * @param viewportSnapshot viewport bounds and grid step (must not be null)
     * @return a {@link PlotResult} containing stitched polylines
     */
    public PlotResult evaluate(final String expression, final Map<String, String> variables, final ViewportSnapshot viewportSnapshot) {
        validateRequest(expression, variables, viewportSnapshot);

        final String normalizedExpression = GraphFxCalculatorEngineUtils.normalizeExpression(expression);

        final BigNumber minX = viewportSnapshot.minX();
        final BigNumber maxX = viewportSnapshot.maxX();
        final BigNumber minY = viewportSnapshot.minY();
        final BigNumber maxY = viewportSnapshot.maxY();
        final BigNumber cellSize = viewportSnapshot.cellSize();

        final Axis xAxis = createAxis(minX, maxX, cellSize);
        final Axis yAxis = createAxis(minY, maxY, cellSize);

        if (xAxis.values.length < 2 || yAxis.values.length < 2) {
            return new PlotResult();
        }

        final long gridPointCount = (long) xAxis.values.length * (long) yAxis.values.length;
        if (gridPointCount > MAXIMUM_GRID_POINT_COUNT) {
            throw new IllegalArgumentException("Viewport grid is too large: " + gridPointCount + " points (max " + MAXIMUM_GRID_POINT_COUNT + ")");
        }

        final Map<String, String> combinedVariables = new HashMap<>(variables);

        BigNumber[] lowerRowValues = evaluateGridRow(normalizedExpression, combinedVariables, xAxis.strings, yAxis.strings[0]);
        BigNumber[] upperRowValues = evaluateGridRow(normalizedExpression, combinedVariables, xAxis.strings, yAxis.strings[1]);

        final int xCellCount = xAxis.values.length - 1;
        final int yCellCount = yAxis.values.length - 1;

        /**
         * Heuristic initial capacity:
         * - Include both dimensions (x and y) because the number of processed cells scales with their product.
         * - Cap aggressively to avoid excessive upfront memory usage.
         */
        final long cellCount = (long) xCellCount * (long) yCellCount;
        final int initialSegmentCapacity = (int) Math.min(1_000_000L, Math.max(65_536L, cellCount / 2L));

        final List<Segment> rawSegments = new ArrayList<>(initialSegmentCapacity);

        for (int yCellIndex = 0; yCellIndex < yCellCount; yCellIndex++) {
            final BigNumber lowerY = yAxis.values[yCellIndex];
            final BigNumber upperY = yAxis.values[yCellIndex + 1];

            for (int xCellIndex = 0; xCellIndex < xCellCount; xCellIndex++) {
                final BigNumber leftX = xAxis.values[xCellIndex];
                final BigNumber rightX = xAxis.values[xCellIndex + 1];

                final BigNumber bottomLeft = lowerRowValues[xCellIndex];
                final BigNumber bottomRight = lowerRowValues[xCellIndex + 1];
                final BigNumber topRight = upperRowValues[xCellIndex + 1];
                final BigNumber topLeft = upperRowValues[xCellIndex];

                if (bottomLeft == null || bottomRight == null || topRight == null || topLeft == null) {
                    continue;
                }

                if (canSkipCellBecauseNoContourCanExist(bottomLeft, bottomRight, topRight, topLeft)) {
                    continue;
                }

                appendSegmentsForCell(rawSegments, normalizedExpression, combinedVariables, leftX, lowerY, rightX, upperY, bottomLeft, bottomRight, topRight, topLeft);
            }

            final int nextUpperRowIndex = yCellIndex + 2;
            if (nextUpperRowIndex < yAxis.values.length) {
                lowerRowValues = upperRowValues;
                upperRowValues = evaluateGridRow(normalizedExpression, combinedVariables, xAxis.strings, yAxis.strings[nextUpperRowIndex]);
            }
        }

        final double cellSizeDouble = Math.max(0.0, cellSize.doubleValue());
        final double stitchTolerance = Math.max(MINIMUM_STITCH_TOLERANCE, cellSizeDouble * STITCH_TOLERANCE_FACTOR);

        final List<PlotLine> stitchedLines = stitchSegmentsIntoPolylines(rawSegments, stitchTolerance);

        return new PlotResult(new ArrayList<>(), stitchedLines);
    }

    /**
     * Creates a uniformly spaced axis between {@code minimumValue} and {@code maximumValue} (inclusive) with the given step.
     * <p>
     * This method also caches {@link BigNumber#toString()} results to reduce repeated conversions while sampling.
     *
     * @param minimumValue inclusive minimum
     * @param maximumValue inclusive maximum
     * @param stepSize     positive step size
     * @return an {@link Axis} containing BigNumber values and cached string representations
     */
    private Axis createAxis(final BigNumber minimumValue, final BigNumber maximumValue, final BigNumber stepSize) {
        final List<BigNumber> values = new ArrayList<>();
        final List<String> strings = new ArrayList<>();

        for (BigNumber current = minimumValue; !current.isGreaterThan(maximumValue); current = current.add(stepSize)) {
            values.add(current);
            strings.add(current.toString());
        }

        return new Axis(values.toArray(new BigNumber[0]), strings.toArray(new String[0]));
    }

    /**
     * Evaluates a full grid row for a fixed y value.
     * <p>
     * The provided {@code combinedVars} map is mutated in-place to avoid per-sample allocations.
     *
     * @param expression      normalized expression
     * @param combinedVars    mutable variable map used for evaluation
     * @param xAxisStrings    cached x-axis values as strings (same order as x-axis values)
     * @param yAxisValueAsStr cached y value as string
     * @return evaluated row values (same length as x-axis); entries can be null if evaluation fails
     */
    private BigNumber[] evaluateGridRow(final String expression, final Map<String, String> combinedVars, final String[] xAxisStrings, final String yAxisValueAsStr) {
        final BigNumber[] rowValues = new BigNumber[xAxisStrings.length];

        combinedVars.put(Y_VARIABLE_NAME, yAxisValueAsStr);

        for (int xIndex = 0; xIndex < xAxisStrings.length; xIndex++) {
            combinedVars.put(X_VARIABLE_NAME, xAxisStrings[xIndex]);

            try {
                rowValues[xIndex] = calculatorEngine.evaluate(expression, combinedVars);
            } catch (final RuntimeException ignored) {
                rowValues[xIndex] = null;
            }
        }

        return rowValues;
    }

    /**
     * Determines whether a cell can be skipped because all corner values are strictly on the same side of zero.
     *
     * @param bottomLeft  f(x0,y0)
     * @param bottomRight f(x1,y0)
     * @param topRight    f(x1,y1)
     * @param topLeft     f(x0,y1)
     * @return {@code true} if there cannot be a zero crossing inside the cell
     */
    private boolean canSkipCellBecauseNoContourCanExist(final BigNumber bottomLeft, final BigNumber bottomRight, final BigNumber topRight, final BigNumber topLeft) {
        final int s0 = bottomLeft.signum();
        final int s1 = bottomRight.signum();
        final int s2 = topRight.signum();
        final int s3 = topLeft.signum();

        // If any value is exactly zero, do NOT skip – the contour might pass through a corner.
        if (s0 == 0 || s1 == 0 || s2 == 0 || s3 == 0) {
            return false;
        }

        final boolean allPositive = s0 > 0 && s1 > 0 && s2 > 0 && s3 > 0;
        final boolean allNegative = s0 < 0 && s1 < 0 && s2 < 0 && s3 < 0;

        return allPositive || allNegative;
    }

    /**
     * Adds line segments for a single grid cell using Marching Squares.
     * <p>
     * This method generates raw segments; a later stitching step merges them into polylines.
     *
     * @param segments    output list of raw segments
     * @param expression  normalized expression
     * @param variables   mutable variable map
     * @param leftX       cell left x
     * @param lowerY      cell bottom y
     * @param rightX      cell right x
     * @param upperY      cell top y
     * @param bottomLeft  f(left,bottom)
     * @param bottomRight f(right,bottom)
     * @param topRight    f(right,top)
     * @param topLeft     f(left,top)
     */
    private void appendSegmentsForCell(final List<Segment> segments, final String expression, final Map<String, String> variables, final BigNumber leftX, final BigNumber lowerY, final BigNumber rightX, final BigNumber upperY, final BigNumber bottomLeft, final BigNumber bottomRight, final BigNumber topRight, final BigNumber topLeft) {
        final PlotPoint bottom = computeIntersectionPointIfContourCrossesEdge(leftX, lowerY, bottomLeft, rightX, lowerY, bottomRight);
        final PlotPoint right = computeIntersectionPointIfContourCrossesEdge(rightX, lowerY, bottomRight, rightX, upperY, topRight);
        final PlotPoint top = computeIntersectionPointIfContourCrossesEdge(rightX, upperY, topRight, leftX, upperY, topLeft);
        final PlotPoint left = computeIntersectionPointIfContourCrossesEdge(leftX, upperY, topLeft, leftX, lowerY, bottomLeft);

        final PlotPoint[] points = new PlotPoint[]{bottom, right, top, left};

        int count = 0;
        for (final PlotPoint point : points) {
            if (point != null) {
                count++;
            }
        }

        if (count == 0) {
            return;
        }

        // Typical non-ambiguous case: exactly two edge intersections.
        if (count == 2) {
            PlotPoint first = null;
            PlotPoint second = null;

            for (final PlotPoint point : points) {
                if (point == null) {
                    continue;
                }
                if (first == null) {
                    first = point;
                } else {
                    second = point;
                    break;
                }
            }

            if (first != null && second != null) {
                segments.add(new Segment(first, second));
            }
            return;
        }

        // Ambiguous Marching Squares cases can produce 4 intersections.
        // This happens primarily for mask 5 and 10 (saddle).
        if (count == 4) {
            final int mask = computeMarchingSquaresMask(bottomLeft, bottomRight, topRight, topLeft);

            if (mask == 5 || mask == 10) {
                final BigNumber centerX = leftX.add(rightX).divide(TWO);
                final BigNumber centerY = lowerY.add(upperY).divide(TWO);

                final BigNumber centerValue = tryEvaluateAt(expression, variables, centerX, centerY);
                final boolean centerPositive = centerValue != null && centerValue.isGreaterThan(ZERO);

                if (mask == 5) {
                    addSegmentsForAmbiguousCell(segments, centerPositive, bottom, right, top, left);
                    return;
                }

                // mask == 10
                addSegmentsForAmbiguousCell(segments, centerPositive, bottom, left, right, top);
                return;
            }

            // Defensive fallback (should rarely be reached): connect opposite edges.
            segments.add(new Segment(bottom, right));
            segments.add(new Segment(top, left));
        }
    }

    /**
     * Adds the two segments for an ambiguous (saddle) Marching Squares cell.
     * <p>
     * The chosen connectivity depends on the sign of the center sample:
     * <ul>
     *     <li>If the center is positive, connect one diagonal pairing.</li>
     *     <li>If the center is negative, connect the opposite diagonal pairing.</li>
     * </ul>
     *
     * @param segments       output segment list
     * @param centerPositive whether the center sample is positive
     * @param bottom         bottom edge intersection
     * @param right          right edge intersection
     * @param top            top edge intersection
     * @param left           left edge intersection
     */
    private void addSegmentsForAmbiguousCell(final List<Segment> segments, final boolean centerPositive, final PlotPoint bottom, final PlotPoint right, final PlotPoint top, final PlotPoint left) {
        if (centerPositive) {
            segments.add(new Segment(bottom, right));
            segments.add(new Segment(top, left));
        } else {
            segments.add(new Segment(bottom, left));
            segments.add(new Segment(top, right));
        }
    }

    /**
     * Attempts to evaluate the expression at a given point.
     * <p>
     * This method mutates the provided variable map to bind {@code x} and {@code y}.
     *
     * @param expression normalized expression
     * @param variables  mutable variable map
     * @param xValue     x value
     * @param yValue     y value
     * @return the computed value or {@code null} if evaluation fails
     */
    private BigNumber tryEvaluateAt(final String expression, final Map<String, String> variables, final BigNumber xValue, final BigNumber yValue) {
        try {
            variables.put(X_VARIABLE_NAME, xValue.toString());
            variables.put(Y_VARIABLE_NAME, yValue.toString());
            return calculatorEngine.evaluate(expression, variables);
        } catch (final RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Computes the Marching Squares mask from corner signs.
     * <p>
     * Bits are set for corners with value {@code > 0}:
     * <ul>
     *     <li>bit 0: bottom-left</li>
     *     <li>bit 1: bottom-right</li>
     *     <li>bit 2: top-right</li>
     *     <li>bit 3: top-left</li>
     * </ul>
     * <p>
     * Performance note: uses {@link BigNumber#signum()} instead of repeated comparisons,
     * which tends to be cheaper for most BigNumber implementations.
     *
     * @param bottomLeft  bottom-left value
     * @param bottomRight bottom-right value
     * @param topRight    top-right value
     * @param topLeft     top-left value
     * @return mask in range [0..15]
     */
    private int computeMarchingSquaresMask(final BigNumber bottomLeft, final BigNumber bottomRight, final BigNumber topRight, final BigNumber topLeft) {
        int mask = 0;

        if (bottomLeft.signum() > 0) {
            mask |= 1;
        }
        if (bottomRight.signum() > 0) {
            mask |= 2;
        }
        if (topRight.signum() > 0) {
            mask |= 4;
        }
        if (topLeft.signum() > 0) {
            mask |= 8;
        }

        return mask;
    }

    /**
     * Computes an edge intersection point by linear interpolation if the contour crosses the edge.
     * <p>
     * This function checks signs first and only performs the interpolation math if the edge is crossed.
     * <p>
     * Interpolation uses:
     * <pre>
     * t = f1 / (f1 - f2)
     * p = p1 + (p2 - p1) * t
     * </pre>
     *
     * @param x1 first point x
     * @param y1 first point y
     * @param f1 first point value
     * @param x2 second point x
     * @param y2 second point y
     * @param f2 second point value
     * @return intersection point or {@code null} if no crossing exists on this edge
     */
    private PlotPoint computeIntersectionPointIfContourCrossesEdge(final BigNumber x1, final BigNumber y1, final BigNumber f1, final BigNumber x2, final BigNumber y2, final BigNumber f2) {
        final int s1 = f1.signum();
        final int s2 = f2.signum();

        // Both exactly zero -> infinite solutions along the edge; skip to avoid degenerate segments.
        if (s1 == 0 && s2 == 0) {
            return null;
        }

        // Exact hit on a vertex -> use that vertex.
        if (s1 == 0) {
            return new PlotPoint(x1, y1);
        }
        if (s2 == 0) {
            return new PlotPoint(x2, y2);
        }

        // Same sign -> no crossing.
        if (s1 == s2) {
            return null;
        }

        final BigNumber denominator = f1.subtract(f2);
        if (denominator.signum() == 0) {
            return null;
        }

        // t = f1 / (f1 - f2)
        final BigNumber t = f1.divide(denominator);

        final BigNumber x = x1.add(x2.subtract(x1).multiply(t));
        final BigNumber y = y1.add(y2.subtract(y1).multiply(t));

        return new PlotPoint(x, y);
    }

    /**
     * Stitches raw segments into longer polylines by connecting endpoints within a tolerance.
     * <p>
     * Performance improvements compared to a naive implementation:
     * <ul>
     *     <li>Precomputes quantized endpoint keys for each segment once (no repeated quantization in the hot loop).</li>
     *     <li>Uses a primitive int adjacency container to avoid boxing {@code Integer} for segment indices.</li>
     *     <li>Tracks the previous endpoint key without creating iterators on each extension step.</li>
     * </ul>
     *
     * @param segments  raw segments
     * @param tolerance world-unit tolerance for endpoint matching
     * @return stitched polylines as {@link PlotLine}
     */
    private List<PlotLine> stitchSegmentsIntoPolylines(final List<Segment> segments, final double tolerance) {
        if (segments.isEmpty()) {
            return new ArrayList<>();
        }

        final int segmentCount = segments.size();

        final PointKey[] startKeys = new PointKey[segmentCount];
        final PointKey[] endKeys = new PointKey[segmentCount];

        final int expectedAdjacencySize = (int) Math.min(Integer.MAX_VALUE, (long) segmentCount * 2L);
        final Map<PointKey, IntBag> adjacency = new HashMap<>((int) (expectedAdjacencySize / 0.75f) + 1);

        for (int index = 0; index < segmentCount; index++) {
            final Segment segment = segments.get(index);

            final PointKey a = PointKey.from(segment.start, tolerance);
            final PointKey b = PointKey.from(segment.end, tolerance);

            startKeys[index] = a;
            endKeys[index] = b;

            adjacency.computeIfAbsent(a, ignored -> new IntBag(2)).add(index);
            adjacency.computeIfAbsent(b, ignored -> new IntBag(2)).add(index);
        }

        final boolean[] used = new boolean[segmentCount];
        final List<PlotLine> polylines = new ArrayList<>();

        for (int seedIndex = 0; seedIndex < segmentCount; seedIndex++) {
            if (used[seedIndex]) {
                continue;
            }

            final Segment seed = segments.get(seedIndex);
            used[seedIndex] = true;

            final Deque<PlotPoint> points = new ArrayDeque<>();
            points.addLast(seed.start);
            points.addLast(seed.end);

            final PointKey seedHeadKey = startKeys[seedIndex];
            final PointKey seedTailKey = endKeys[seedIndex];

            // Extend forward from the tail (end).
            extendPolyline(points, segments, adjacency, used, startKeys, endKeys, seedHeadKey, seedTailKey, true);

            // Extend backward from the head (start). The "previous" key here is the second point's key.
            extendPolyline(points, segments, adjacency, used, startKeys, endKeys, seedTailKey, seedHeadKey, false);

            if (points.size() >= 2) {
                polylines.add(new PlotLine(new ArrayList<>(points)));
            }
        }

        return polylines;
    }

    /**
     * Extends a polyline by greedily consuming connected unused segments.
     * <p>
     * The method uses quantized endpoint keys (precomputed) to identify connectivity and to avoid
     * immediate backtracking when possible.
     *
     * @param points      current polyline points (deque)
     * @param segments    all segments
     * @param adjacency   endpoint-to-segment adjacency map (primitive index bags)
     * @param used        used flags
     * @param startKeys   precomputed quantized keys for segment start endpoints
     * @param endKeys     precomputed quantized keys for segment end endpoints
     * @param previousKey quantized key of the previous point (used to avoid immediate backtracking)
     * @param currentKey  quantized key of the current endpoint to extend from
     * @param forward     if {@code true}, extend at tail; otherwise extend at head
     */
    private void extendPolyline(final Deque<PlotPoint> points, final List<Segment> segments, final Map<PointKey, IntBag> adjacency, final boolean[] used, final PointKey[] startKeys, final PointKey[] endKeys, PointKey previousKey, PointKey currentKey, final boolean forward) {
        while (true) {
            final IntBag candidates = adjacency.get(currentKey);
            if (candidates == null || candidates.isEmpty()) {
                return;
            }

            int nextSegmentIndex = -1;
            PlotPoint nextPoint = null;
            PointKey nextKey = null;

            // First pass: avoid immediate backtracking if possible.
            for (int i = 0; i < candidates.size(); i++) {
                final int candidateIndex = candidates.get(i);
                if (used[candidateIndex]) {
                    continue;
                }

                final PointKey a = startKeys[candidateIndex];
                final PointKey b = endKeys[candidateIndex];

                final boolean matchesA = a.equals(currentKey);
                final boolean matchesB = b.equals(currentKey);

                if (!matchesA && !matchesB) {
                    continue;
                }

                final PointKey candidateNextKey = matchesA ? b : a;
                if (candidateNextKey.equals(previousKey)) {
                    continue;
                }

                final Segment segment = segments.get(candidateIndex);
                nextPoint = matchesA ? segment.end : segment.start;
                nextKey = candidateNextKey;
                nextSegmentIndex = candidateIndex;
                break;
            }

            // Second pass: allow backtracking as a last resort.
            if (nextSegmentIndex < 0) {
                for (int i = 0; i < candidates.size(); i++) {
                    final int candidateIndex = candidates.get(i);
                    if (used[candidateIndex]) {
                        continue;
                    }

                    final PointKey a = startKeys[candidateIndex];
                    final PointKey b = endKeys[candidateIndex];

                    final boolean matchesA = a.equals(currentKey);
                    final boolean matchesB = b.equals(currentKey);

                    if (!matchesA && !matchesB) {
                        continue;
                    }

                    final Segment segment = segments.get(candidateIndex);
                    nextPoint = matchesA ? segment.end : segment.start;
                    nextKey = matchesA ? b : a;
                    nextSegmentIndex = candidateIndex;
                    break;
                }
            }

            if (nextSegmentIndex < 0 || nextPoint == null || nextKey == null) {
                return;
            }

            // Degenerate after quantization: extending would not move.
            if (nextKey.equals(currentKey)) {
                return;
            }

            used[nextSegmentIndex] = true;

            if (forward) {
                points.addLast(nextPoint);
            } else {
                points.addFirst(nextPoint);
            }

            previousKey = currentKey;
            currentKey = nextKey;
        }
    }

    /**
     * Validates an evaluation request.
     *
     * @param expression       expression to evaluate
     * @param variables        variable map
     * @param viewportSnapshot viewport snapshot
     */
    private void validateRequest(final String expression, final Map<String, String> variables, final ViewportSnapshot viewportSnapshot) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("expression must not be null/blank");
        }
        Objects.requireNonNull(variables, "variables must not be null");
        Objects.requireNonNull(viewportSnapshot, "viewportSnapshot must not be null");

        if (viewportSnapshot.cellSize().isLessThanOrEqualTo(ZERO)) {
            throw new IllegalArgumentException("cellSize must be > 0");
        }
        if (variablesContainReservedVariables(variables.keySet())) {
            throw new IllegalArgumentException("variables must not contain reserved names 'x' or 'y'");
        }

        final BigNumber minX = viewportSnapshot.minX();
        final BigNumber maxX = viewportSnapshot.maxX();
        final BigNumber minY = viewportSnapshot.minY();
        final BigNumber maxY = viewportSnapshot.maxY();

        if (!minX.isLessThan(maxX)) {
            throw new IllegalArgumentException("minX must be < maxX");
        }
        if (!minY.isLessThan(maxY)) {
            throw new IllegalArgumentException("minY must be < maxY");
        }
    }

    /**
     * Checks whether a variable set contains reserved plotting variables.
     *
     * @param variableNames variable names
     * @return {@code true} if reserved variables are present
     */
    private boolean variablesContainReservedVariables(final Set<String> variableNames) {
        for (final String variableName : variableNames) {
            if (variableName == null) {
                continue;
            }
            if (variableName.equals(X_VARIABLE_NAME) || variableName.equals(Y_VARIABLE_NAME)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Immutable container for axis values and their cached string representations.
     *
     * @param values  numeric axis values
     * @param strings cached {@code toString()} results aligned with {@code values}
     */
    private record Axis(BigNumber[] values, String[] strings) {
        /**
         * Creates an axis container.
         *
         * @param values  axis values (non-null)
         * @param strings cached string values (non-null, same order as {@code values})
         */
        private Axis {
            Objects.requireNonNull(values, "values must not be null");
            Objects.requireNonNull(strings, "strings must not be null");
        }
    }

    /**
     * Immutable raw contour segment consisting of two endpoints.
     *
     * @param start start point
     * @param end   end point
     */
    private record Segment(PlotPoint start, PlotPoint end) {
        /**
         * Creates a segment.
         *
         * @param start start point (non-null)
         * @param end   end point (non-null)
         */
        private Segment {
            Objects.requireNonNull(start, "start must not be null");
            Objects.requireNonNull(end, "end must not be null");
        }
    }

    /**
     * Quantized point key used for stitching segments reliably despite minor numeric differences.
     * <p>
     * The key is computed by dividing by a tolerance and rounding to the nearest long.
     * Points that fall into the same quantized bucket are considered identical for stitching.
     *
     * @param qx quantized x coordinate
     * @param qy quantized y coordinate
     */
    private record PointKey(long qx, long qy) {

        /**
         * Creates a point key from a {@link PlotPoint} using the given tolerance.
         * <p>
         * The tolerance must be positive. The caller ensures this by clamping tolerance
         * to at least {@link #MINIMUM_STITCH_TOLERANCE}.
         *
         * @param point     plot point (non-null)
         * @param tolerance matching tolerance (must be > 0)
         * @return quantized key
         */
        static PointKey from(final PlotPoint point, final double tolerance) {
            final double x = point.x().doubleValue();
            final double y = point.y().doubleValue();

            final long qx = Math.round(x / tolerance);
            final long qy = Math.round(y / tolerance);

            return new PointKey(qx, qy);
        }
    }

    /**
     * A minimal primitive int collection specialized for adjacency lists.
     * <p>
     * Why this exists:
     * <ul>
     *     <li>{@code List<Integer>} would allocate and box an {@code Integer} for every stored index.</li>
     *     <li>Stitching can touch many indices, so boxing/GC overhead becomes measurable.</li>
     * </ul>
     * <p>
     * This type provides:
     * <ul>
     *     <li>Amortized O(1) {@link #add(int)}</li>
     *     <li>O(1) {@link #get(int)}</li>
     *     <li>Fast indexed iteration without allocations</li>
     * </ul>
     */
    private static final class IntBag {

        /**
         * Backing array holding the stored indices.
         * <p>
         * Only the first {@link #size} entries are valid.
         */
        private int[] elements;

        /**
         * Number of valid stored indices.
         */
        private int size;

        /**
         * Creates an {@link IntBag} with a given initial capacity.
         *
         * @param initialCapacity initial backing array size (values <= 0 are treated as a small default)
         */
        private IntBag(final int initialCapacity) {
            final int safeCapacity = Math.max(4, initialCapacity);
            this.elements = new int[safeCapacity];
            this.size = 0;
        }

        /**
         * Adds a value to the bag.
         *
         * @param value the integer value to store
         */
        private void add(final int value) {
            if (size == elements.length) {
                grow();
            }
            elements[size++] = value;
        }

        /**
         * Returns the number of stored values.
         *
         * @return current size
         */
        private int size() {
            return size;
        }

        /**
         * Returns whether the bag contains no values.
         *
         * @return {@code true} if empty
         */
        private boolean isEmpty() {
            return size == 0;
        }

        /**
         * Returns the value at the given index.
         *
         * @param index index in range {@code [0..size-1]}
         * @return stored value
         * @throws ArrayIndexOutOfBoundsException if index is out of bounds
         */
        private int get(final int index) {
            return elements[index];
        }

        /**
         * Grows the internal array to accommodate more elements.
         * <p>
         * Uses a 1.5x growth factor to balance memory usage and reallocation frequency.
         */
        private void grow() {
            final int newCapacity = elements.length + (elements.length >> 1) + 1;
            final int[] newArray = new int[newCapacity];
            System.arraycopy(elements, 0, newArray, 0, elements.length);
            elements = newArray;
        }
    }

}
