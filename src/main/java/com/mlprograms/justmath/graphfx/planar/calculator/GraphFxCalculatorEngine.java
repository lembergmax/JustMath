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
 * This engine implements a classic "implicit contour" plotter (Marching Squares):
 * it samples a scalar field {@code f(x,y)} on a grid and draws the contour line where {@code f(x,y)=0}.
 * <p>
 * Two important quality/performance improvements are implemented here:
 * <ol>
 *     <li><b>Correct ambiguous Marching Squares cases (mask 5 and 10)</b> using a center sample to disambiguate.</li>
 *     <li><b>Segment stitching</b>: all per-cell line segments are merged into longer polylines. This produces visually
 *     continuous curves and drastically reduces draw calls in the JavaFX view.</li>
 * </ol>
 */
public final class GraphFxCalculatorEngine {

    /**
     * Hard limit for grid points to prevent UI freezes with extremely small cell sizes.
     */
    private static final int MAXIMUM_GRID_POINT_COUNT = 2_000_000;

    /**
     * Factor that defines the stitching tolerance relative to the grid cell size.
     * <p>
     * Endpoints that are closer than {@code cellSize * STITCH_TOLERANCE_FACTOR} are considered identical
     * and will be connected into the same polyline.
     */
    private static final double STITCH_TOLERANCE_FACTOR = 1e-2;

    /**
     * Absolute minimum tolerance for stitching in world units to avoid division-by-zero / ultra-small tolerance.
     */
    private static final double MINIMUM_STITCH_TOLERANCE = 1e-12;

    /**
     * Underlying evaluator for mathematical expressions.
     * <p>
     * It must support variables {@code x} and {@code y} via the provided variable map.
     */
    private final CalculatorEngine calculatorEngine;

    /**
     * Creates a plot engine with a default {@link CalculatorEngine} configuration.
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

        final List<Segment> rawSegments = new ArrayList<>(Math.min(65_536, xCellCount * 8));

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
     *
     * @param expression      normalized expression
     * @param combinedVars    mutable variable map used for evaluation
     * @param xAxisStrings    cached x-axis values as strings (same order as x-axis values)
     * @param yAxisValueAsStr cached y value as string
     * @return evaluated row values (same length as x-axis)
     */
    private BigNumber[] evaluateGridRow(final String expression, final Map<String, String> combinedVars, final String[] xAxisStrings, final String yAxisValueAsStr) {

        final BigNumber[] rowValues = new BigNumber[xAxisStrings.length];

        combinedVars.put(ReservedVariables.Y.getValue(), yAxisValueAsStr);

        for (int xIndex = 0; xIndex < xAxisStrings.length; xIndex++) {
            combinedVars.put(ReservedVariables.X.getValue(), xAxisStrings[xIndex]);

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

            for (final PlotPoint p : points) {
                if (p == null) {
                    continue;
                }
                if (first == null) {
                    first = p;
                } else {
                    second = p;
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

                // Naming:
                // bottom = intersection on bottom edge
                // right  = intersection on right edge
                // top    = intersection on top edge
                // left   = intersection on left edge
                //
                // Disambiguation rule:
                // - If center is positive, connect segments around negative corners.
                // - If center is negative, connect segments around positive corners.
                //
                // Mask 5: bl(+), br(-), tr(+), tl(-)
                //   center positive => (bottom-right) + (top-left)
                //   center negative => (bottom-left) + (top-right)
                //
                // Mask 10: bl(-), br(+), tr(-), tl(+)
                //   center positive => (bottom-left) + (right-top)
                //   center negative => (bottom-right) + (top-left)
                if (mask == 5) {
                    if (centerPositive) {
                        segments.add(new Segment(bottom, right));
                        segments.add(new Segment(top, left));
                    } else {
                        segments.add(new Segment(bottom, left));
                        segments.add(new Segment(top, right));
                    }
                    return;
                }

                // mask == 10
                if (centerPositive) {
                    segments.add(new Segment(bottom, left));
                    segments.add(new Segment(right, top));
                } else {
                    segments.add(new Segment(bottom, right));
                    segments.add(new Segment(top, left));
                }
                return;
            }

            // Defensive fallback (should rarely be reached): connect opposite edges.
            segments.add(new Segment(bottom, right));
            segments.add(new Segment(top, left));
        }

        // If we get 1 or 3 intersections, we skip the cell.
        // This usually indicates an exact-zero corner degeneracy, which is rare and unstable in practice.
    }

    /**
     * Attempts to evaluate the expression at a given point.
     *
     * @param expression normalized expression
     * @param variables  mutable variable map
     * @param xValue     x value
     * @param yValue     y value
     * @return the computed value or {@code null} if evaluation fails
     */
    private BigNumber tryEvaluateAt(final String expression, final Map<String, String> variables, final BigNumber xValue, final BigNumber yValue) {
        try {
            variables.put(ReservedVariables.X.getValue(), xValue.toString());
            variables.put(ReservedVariables.Y.getValue(), yValue.toString());
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
     *
     * @param bottomLeft  bottom-left value
     * @param bottomRight bottom-right value
     * @param topRight    top-right value
     * @param topLeft     top-left value
     * @return mask in range [0..15]
     */
    private int computeMarchingSquaresMask(final BigNumber bottomLeft, final BigNumber bottomRight, final BigNumber topRight, final BigNumber topLeft) {
        int mask = 0;

        if (bottomLeft.isGreaterThan(ZERO)) {
            mask |= 1;
        }
        if (bottomRight.isGreaterThan(ZERO)) {
            mask |= 2;
        }
        if (topRight.isGreaterThan(ZERO)) {
            mask |= 4;
        }
        if (topLeft.isGreaterThan(ZERO)) {
            mask |= 8;
        }

        return mask;
    }

    /**
     * Computes an edge intersection point by linear interpolation if the contour crosses the edge.
     *
     * @param x1 first point x
     * @param y1 first point y
     * @param f1 first point value
     * @param x2 second point x
     * @param y2 second point y
     * @param f2 second point value
     * @return intersection point or {@code null}
     */
    private PlotPoint computeIntersectionPointIfContourCrossesEdge(final BigNumber x1, final BigNumber y1, final BigNumber f1, final BigNumber x2, final BigNumber y2, final BigNumber f2) {
        if (f1.isEqualTo(ZERO) && f2.isEqualTo(ZERO)) {
            return null;
        }

        if (f1.isEqualTo(ZERO)) {
            return new PlotPoint(x1, y1);
        }
        if (f2.isEqualTo(ZERO)) {
            return new PlotPoint(x2, y2);
        }

        final int s1 = f1.signum();
        final int s2 = f2.signum();

        if (s1 == 0 || s2 == 0 || s1 == s2) {
            return null;
        }

        final BigNumber denominator = f1.subtract(f2);
        if (denominator.isEqualTo(ZERO)) {
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
     *
     * @param segments  raw segments
     * @param tolerance world-unit tolerance for endpoint matching
     * @return stitched polylines as {@link PlotLine}
     */
    private List<PlotLine> stitchSegmentsIntoPolylines(final List<Segment> segments, final double tolerance) {
        if (segments.isEmpty()) {
            return new ArrayList<>();
        }

        final Map<PointKey, List<Integer>> adjacency = new HashMap<>(segments.size() * 2);
        for (int index = 0; index < segments.size(); index++) {
            final Segment segment = segments.get(index);

            final PointKey a = PointKey.from(segment.start, tolerance);
            final PointKey b = PointKey.from(segment.end, tolerance);

            adjacency.computeIfAbsent(a, ignored -> new ArrayList<>()).add(index);
            adjacency.computeIfAbsent(b, ignored -> new ArrayList<>()).add(index);
        }

        final boolean[] used = new boolean[segments.size()];
        final List<PlotLine> polylines = new ArrayList<>();

        for (int startIndex = 0; startIndex < segments.size(); startIndex++) {
            if (used[startIndex]) {
                continue;
            }

            final Segment seed = segments.get(startIndex);
            used[startIndex] = true;

            final Deque<PlotPoint> points = new ArrayDeque<>();
            points.addLast(seed.start);
            points.addLast(seed.end);

            // Extend forward from the end.
            extendPolyline(points, segments, adjacency, used, tolerance, true);

            // Extend backward from the start.
            extendPolyline(points, segments, adjacency, used, tolerance, false);

            if (points.size() >= 2) {
                polylines.add(new PlotLine(new ArrayList<>(points)));
            }
        }

        return polylines;
    }

    /**
     * Extends a polyline by greedily consuming connected unused segments.
     *
     * @param points    current polyline points (deque)
     * @param segments  all segments
     * @param adjacency endpoint-to-segment adjacency map
     * @param used      used flags
     * @param tolerance matching tolerance
     * @param forward   if {@code true}, extend at tail; otherwise extend at head
     */
    private void extendPolyline(final Deque<PlotPoint> points, final List<Segment> segments, final Map<PointKey, List<Integer>> adjacency, final boolean[] used, final double tolerance, final boolean forward) {

        while (true) {
            final PlotPoint current = forward ? points.peekLast() : points.peekFirst();
            final PlotPoint previous = forward ? getSecondLast(points) : getSecondFirst(points);

            if (current == null) {
                return;
            }

            final PointKey currentKey = PointKey.from(current, tolerance);
            final PointKey previousKey = previous == null ? null : PointKey.from(previous, tolerance);

            final List<Integer> candidates = adjacency.get(currentKey);
            if (candidates == null || candidates.isEmpty()) {
                return;
            }

            Integer nextSegmentIndex = null;
            PlotPoint nextPoint = null;

            for (final Integer index : candidates) {
                if (used[index]) {
                    continue;
                }

                final Segment segment = segments.get(index);

                final PointKey a = PointKey.from(segment.start, tolerance);
                final PointKey b = PointKey.from(segment.end, tolerance);

                final boolean matchesA = a.equals(currentKey);
                final boolean matchesB = b.equals(currentKey);

                if (!matchesA && !matchesB) {
                    continue;
                }

                final PlotPoint candidateNext = matchesA ? segment.end : segment.start;
                final PointKey candidateNextKey = matchesA ? b : a;

                // Avoid immediate backtracking if possible.
                if (candidateNextKey.equals(previousKey)) {
                    continue;
                }

                nextSegmentIndex = index;
                nextPoint = candidateNext;
                break;
            }

            // If we only found a backtracking option, allow it as a last resort.
            if (nextSegmentIndex == null) {
                for (final Integer index : candidates) {
                    if (used[index]) {
                        continue;
                    }

                    final Segment segment = segments.get(index);
                    final PointKey a = PointKey.from(segment.start, tolerance);
                    final PointKey b = PointKey.from(segment.end, tolerance);

                    final boolean matchesA = a.equals(currentKey);
                    final boolean matchesB = b.equals(currentKey);

                    if (!matchesA && !matchesB) {
                        continue;
                    }

                    nextSegmentIndex = index;
                    nextPoint = matchesA ? segment.end : segment.start;
                    break;
                }
            }

            if (nextSegmentIndex == null || nextPoint == null) {
                return;
            }

            used[nextSegmentIndex] = true;

            if (forward) {
                if (!samePoint(points.peekLast(), nextPoint, tolerance)) {
                    points.addLast(nextPoint);
                } else {
                    return;
                }
            } else {
                if (!samePoint(points.peekFirst(), nextPoint, tolerance)) {
                    points.addFirst(nextPoint);
                } else {
                    return;
                }
            }
        }
    }

    /**
     * Gets the second last point in a deque, or {@code null} if not available.
     *
     * @param points deque of points
     * @return second last point or null
     */
    private PlotPoint getSecondLast(final Deque<PlotPoint> points) {
        if (points.size() < 2) {
            return null;
        }

        final Iterator<PlotPoint> iterator = points.descendingIterator();
        iterator.next(); // last
        return iterator.next(); // second last
    }

    /**
     * Gets the second first point in a deque, or {@code null} if not available.
     *
     * @param points deque of points
     * @return second first point or null
     */
    private PlotPoint getSecondFirst(final Deque<PlotPoint> points) {
        if (points.size() < 2) {
            return null;
        }

        final Iterator<PlotPoint> iterator = points.iterator();
        iterator.next(); // first
        return iterator.next(); // second
    }

    /**
     * Compares two plot points with tolerance in world units.
     *
     * @param a         first point
     * @param b         second point
     * @param tolerance tolerance in world units
     * @return {@code true} if points are sufficiently equal
     */
    private boolean samePoint(final PlotPoint a, final PlotPoint b, final double tolerance) {
        if (a == null || b == null) {
            return false;
        }

        final double ax = a.x().doubleValue();
        final double ay = a.y().doubleValue();
        final double bx = b.x().doubleValue();
        final double by = b.y().doubleValue();

        final double dx = ax - bx;
        final double dy = ay - by;

        return (dx * dx + dy * dy) <= (tolerance * tolerance);
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
            if (variableName.equals(ReservedVariables.X.getValue()) || variableName.equals(ReservedVariables.Y.getValue())) {
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
         * Creates an axis.
         *
         * @param values  axis values
         * @param strings cached string values
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
     *
     * @param qx quantized x coordinate
     * @param qy quantized y coordinate
     */
    private record PointKey(long qx, long qy) {

        /**
         * Creates a point key from a {@link PlotPoint} using the given tolerance.
         *
         * @param point     plot point
         * @param tolerance matching tolerance
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
}
