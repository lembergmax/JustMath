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

package com.mlprograms.justmath.graphfx.core;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * High-performance plot calculator for GraphFx that evaluates JustMath expressions using {@link BigNumber}
 * and generates drawable geometry for overlay rendering.
 *
 * <h2>Plot modes</h2>
 * <ul>
 *   <li><strong>Explicit plot</strong> ({@code y = f(x)}): used when the expression does not contain a standalone {@code y} token.</li>
 *   <li><strong>Implicit plot</strong> ({@code F(x,y) = 0}): used when the expression contains a standalone {@code y} token.</li>
 * </ul>
 *
 * <h2>Performance strategy</h2>
 * <ul>
 *   <li><strong>Analytic fast-path</strong> for affine-linear implicit equations (e.g. {@code 2x-3y+4,3a}):
 *       the line {@code a*x + b*y + c = 0} is recovered using a few BigNumber evaluations and then clipped
 *       to the viewport as a single segment.</li>
 *   <li><strong>Marching squares</strong> for non-linear implicit plots:
 *       the scalar field is evaluated only at grid vertices ({@code (nx+1)*(ny+1)} evaluations).</li>
 *   <li><strong>Cancellation hook</strong> is checked frequently to stop obsolete work quickly.</li>
 * </ul>
 *
 * <h2>Input normalization</h2>
 * <p>
 * For user convenience this class applies a small, conservative normalization:
 * </p>
 * <ul>
 *   <li>Whitespace is removed.</li>
 *   <li>Decimal comma between digits is converted to a dot: {@code 4,3 -> 4.3}.</li>
 *   <li>Implicit multiplication is inserted for common safe cases:
 *     <ul>
 *       <li>number → identifier/parenthesis: {@code 2x -> 2*x}, {@code 2(x) -> 2*(x)}</li>
 *       <li>close-paren → identifier/number/open-paren: {@code )x -> )*x}, {@code )( -> )*(}</li>
 *       <li>single-letter identifier → open-paren: {@code x(y) -> x*(y)} (avoids breaking function names like {@code sin(x)})</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>
 * The normalization is deliberately not a full parser. It is designed to handle typical math input without trying
 * to understand user-defined function names.
 * </p>
 */
public final class GraphFxCalculatorEngine {

    /**
     * Cancellation hook used to abort long-running plot computations when they become obsolete.
     */
    @FunctionalInterface
    public interface PlotCancellation {

        /**
         * Indicates whether the current computation should stop as soon as possible.
         *
         * <p>
         * Implementations should be fast and side-effect free because this method may be called very frequently.
         * Typical implementations check thread interruption, generation counters, or other lightweight flags.
         * </p>
         *
         * @return {@code true} if cancelled; {@code false} otherwise
         */
        boolean isCancelled();
    }

    /**
     * Immutable viewport bounds in world coordinates.
     *
     * @param minX minimum x
     * @param maxX maximum x
     * @param minY minimum y
     * @param maxY maximum y
     */
    public record WorldBounds(double minX, double maxX, double minY, double maxY) {
    }

    /**
     * Immutable line segment in world coordinates.
     *
     * @param a start point
     * @param b end point
     */
    public record LineSegment(GraphFxPoint a, GraphFxPoint b) {
    }

    /**
     * Plot result container.
     *
     * <p>
     * For explicit plots, {@link #polyline()} is populated and {@link #segments()} is empty.
     * For implicit plots, {@link #segments()} is populated and {@link #polyline()} is empty.
     * </p>
     *
     * @param polyline polyline points for explicit plots
     * @param segments line segments for implicit plots
     */
    public record PlotGeometry(List<GraphFxPoint> polyline, List<LineSegment> segments) {

        /**
         * Creates an empty geometry result.
         *
         * @return an empty geometry result
         */
        public static PlotGeometry empty() {
            return new PlotGeometry(List.of(), List.of());
        }
    }

    /**
     * Tolerance used when verifying that an expression behaves like an affine function in x/y.
     * A small value is required because the engine can introduce rounding effects depending on internal contexts.
     */
    private static final BigNumber LINEAR_TOLERANCE = new BigNumber("0.000000000000000000000000000001"); // 1e-30

    /**
     * Epsilon used for double-domain contouring operations (intersections/sign decisions).
     */
    private static final double CONTOUR_EPSILON = 1e-12;

    private final CalculatorEngine calculatorEngine;

    /**
     * Creates a new plot calculator using a fresh {@link CalculatorEngine} instance.
     *
     * <p>
     * This constructor is convenient for typical usage. If you want to reuse an engine instance across multiple
     * calculators (e.g., to share configuration or caches), use {@link #GraphFxCalculatorEngine(CalculatorEngine)}.
     * </p>
     */
    public GraphFxCalculatorEngine() {
        this(new CalculatorEngine(TrigonometricMode.RAD));
    }

    /**
     * Creates a new plot calculator using the provided {@link CalculatorEngine}.
     *
     * <p>
     * The engine is responsible for parsing and evaluating expressions using JustMath types, particularly {@link BigNumber}.
     * This class never performs numeric evaluation itself; it delegates all evaluation to the engine.
     * </p>
     *
     * @param calculatorEngine the engine to use; must not be {@code null}
     * @throws NullPointerException if {@code engine} is {@code null}
     */
    public GraphFxCalculatorEngine(@NonNull final CalculatorEngine calculatorEngine) {
        this.calculatorEngine = calculatorEngine;
    }

    /**
     * Computes plot geometry for the given expression and viewport.
     *
     * <p>
     * The method automatically selects plot mode:
     * </p>
     * <ul>
     *   <li>If a standalone {@code y} token is present: implicit plot {@code F(x,y)=0}.</li>
     *   <li>Otherwise: explicit plot {@code y=f(x)}.</li>
     * </ul>
     *
     * <p>
     * This method performs no JavaFX operations and can safely be called from a background thread.
     * </p>
     *
     * @param expression   expression to plot; must not be {@code null}
     * @param variables    external variables (e.g. {@code a}); must not be {@code null} and must not contain {@code null} keys/values
     * @param bounds       viewport bounds in world coordinates; must not be {@code null}
     * @param pixelWidth   viewport width in pixels (used for adaptive resolution)
     * @param pixelHeight  viewport height in pixels (used for adaptive resolution)
     * @param cancellation cancellation hook; must not be {@code null}
     * @return plot geometry (polyline or segments)
     * @throws NullPointerException if any required argument is {@code null}
     */
    public PlotGeometry plot(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final WorldBounds bounds, final int pixelWidth, final int pixelHeight, @NonNull final PlotCancellation cancellation) {
        final String normalizedExpression = normalizeExpression(expression);
        final Map<String, String> safeVariables = copyVariables(variables);

        if (!containsYVariable(normalizedExpression)) {
            final int samples = clampInt(pixelWidth, 250, 1400);
            final List<GraphFxPoint> polyline = createExplicitPolyline(normalizedExpression, safeVariables, bounds, samples, cancellation);
            return cancellation.isCancelled() ? PlotGeometry.empty() : new PlotGeometry(polyline, List.of());
        }

        final LineSegment linearSegment = tryCreateLinearImplicitSegment(normalizedExpression, safeVariables, bounds, cancellation);
        if (linearSegment != null) {
            return cancellation.isCancelled() ? PlotGeometry.empty() : new PlotGeometry(List.of(), List.of(linearSegment));
        }

        final int cellSizePx = 18;
        final int nx = clampInt(Math.max(8, pixelWidth / cellSizePx), 40, 140);
        final int ny = clampInt(Math.max(8, pixelHeight / cellSizePx), 30, 120);

        final List<LineSegment> segments = createImplicitZeroContourSegments(normalizedExpression, safeVariables, bounds, nx, ny, cancellation);
        return cancellation.isCancelled() ? PlotGeometry.empty() : new PlotGeometry(List.of(), segments);
    }

    /**
     * Checks whether the given expression contains a standalone {@code y} variable token.
     *
     * <p>
     * This method performs a conservative token-boundary check:
     * {@code y} (or {@code Y}) is considered a variable only if it is not surrounded by identifier characters
     * ({@code [A-Za-z0-9_]}). This prevents false positives such as {@code y1} or {@code myVar}.
     * </p>
     *
     * @param expression expression to check; must not be {@code null}
     * @return {@code true} if a standalone {@code y}/{@code Y} token is present; {@code false} otherwise
     * @throws NullPointerException if {@code expression} is {@code null}
     */
    public boolean containsYVariable(@NonNull final String expression) {
        return containsStandaloneIdentifier(expression, 'y', 'Y');
    }

    /**
     * Creates a polyline for an explicit function {@code y=f(x)}.
     *
     * <p>
     * The expression is evaluated at evenly spaced x-values across the viewport.
     * Invalid evaluations (exceptions, NaN/Infinite results) are skipped.
     * </p>
     *
     * <p>
     * The engine is evaluated using {@link BigNumber} internally; this method returns {@code double} points because
     * JavaFX rendering uses double precision coordinates.
     * </p>
     *
     * @param expression   expression to evaluate as {@code y=f(x)}; must not be {@code null}
     * @param variables    external variables; must not be {@code null} and must not contain {@code null} keys/values
     * @param bounds       viewport bounds; must not be {@code null}
     * @param samples      number of samples across the x-range (must be {@code >= 2} to be meaningful)
     * @param cancellation cancellation hook; must not be {@code null}
     * @return polyline points in world coordinates (possibly empty)
     * @throws NullPointerException if any required argument is {@code null}
     */
    private List<GraphFxPoint> createExplicitPolyline(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final WorldBounds bounds, final int samples, @NonNull final PlotCancellation cancellation) {
        final double minX = Math.min(bounds.minX(), bounds.maxX());
        final double maxX = Math.max(bounds.minX(), bounds.maxX());

        final double minY = Math.min(bounds.minY(), bounds.maxY());
        final double maxY = Math.max(bounds.minY(), bounds.maxY());
        final double yRange = Math.max(1e-12, maxY - minY);

        final double jumpThreshold = yRange * 3.0;
        final double hardAbsThreshold = yRange * 20.0;

        final int safeSamples = Math.max(2, samples);
        final double step = (maxX - minX) / (safeSamples - 1);

        final Map<String, String> evalVariables = new HashMap<>(variables);
        evalVariables.put("y", "0");

        final List<GraphFxPoint> polyline = new ArrayList<>(safeSamples + 16);

        boolean previousValid = false;
        double previousY = 0.0;

        for (int i = 0; i < safeSamples; i++) {
            if (cancellation.isCancelled()) {
                return List.of();
            }

            final double x = minX + i * step;
            evalVariables.put("x", Double.toString(x));

            final BigNumber yBig = safeEvaluate(expression, evalVariables);
            if (yBig == null) {
                if (previousValid) {
                    polyline.add(new GraphFxPoint(Double.NaN, Double.NaN));
                    previousValid = false;
                }

                continue;
            }

            final double y = safeToDouble(yBig);
            if (!Double.isFinite(y) || Math.abs(y) > hardAbsThreshold) {
                if (previousValid) {
                    polyline.add(new GraphFxPoint(Double.NaN, Double.NaN));
                    previousValid = false;
                }

                continue;
            }

            if (previousValid && Math.abs(y - previousY) > jumpThreshold) {
                polyline.add(new GraphFxPoint(Double.NaN, Double.NaN));
            }

            polyline.add(new GraphFxPoint(x, y));
            previousValid = true;
            previousY = y;
        }

        return polyline;
    }

    /**
     * Safely evaluates the given expression using the configured {@link CalculatorEngine}.
     *
     * <p>
     * Any {@link RuntimeException} thrown by the engine is considered an invalid evaluation and
     * is swallowed; the method returns {@code null} in that case. This defensive wrapper allows
     * higher-level plotting logic to treat failed evaluations (e.g. due to domain errors,
     * parsing issues, or intermediate numeric problems) as missing points rather than aborting
     * the entire plotting operation.
     * </p>
     *
     * @param expression non-null expression to evaluate
     * @param variables  non-null map of variable bindings; this map may be mutated by the caller
     * @return evaluated {@link BigNumber} result, or {@code null} if evaluation failed
     */
    private BigNumber safeEvaluate(@NonNull final String expression, @NonNull final Map<String, String> variables) {
        try {
            return calculatorEngine.evaluate(expression, variables);
        } catch (final RuntimeException runtimeException) {
            return null;
        }
    }

    /**
     * Converts a {@link BigNumber} to a primitive {@code double} in a safe manner.
     *
     * <p>
     * The conversion uses {@link BigNumber#toString()} followed by {@link Double#parseDouble(String)}.
     * Any runtime error during conversion (for example unexpected formatting) is caught and
     * results in {@link Double#NaN} being returned. This method is intended for final geometry
     * creation where a best-effort double representation is sufficient; numeric evaluation
     * remains in {@link BigNumber} throughout plotting.
     * </p>
     *
     * @param value non-null {@link BigNumber} to convert
     * @return the {@code double} representation, or {@link Double#NaN} if conversion fails
     */
    private double safeToDouble(@NonNull final BigNumber value) {
        try {
            return Double.parseDouble(value.toString());
        } catch (final RuntimeException runtimeException) {
            return Double.NaN;
        }
    }

    /**
     * Creates line segments for the implicit curve {@code F(x,y)=0} using marching squares.
     *
     * <p>
     * The function is sampled on a regular grid of {@code nx * ny} cells. Field values are evaluated at grid vertices
     * ({@code (nx+1)*(ny+1)} evaluations). Each cell is then converted into 0-contour segments according to the standard
     * marching squares case table.
     * </p>
     *
     * <p>
     * The returned geometry is in world coordinates.
     * </p>
     *
     * @param expression   expression defining {@code F(x,y)}; must not be {@code null}
     * @param variables    external variables; must not be {@code null} and must not contain {@code null} keys/values
     * @param bounds       viewport bounds; must not be {@code null}
     * @param nx           number of cells along x (must be {@code >= 1})
     * @param ny           number of cells along y (must be {@code >= 1})
     * @param cancellation cancellation hook; must not be {@code null}
     * @return list of line segments approximating the 0-contour (possibly empty)
     * @throws NullPointerException if any required argument is {@code null}
     */
    public List<LineSegment> createImplicitZeroContourSegments(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final WorldBounds bounds, final int nx, final int ny, @NonNull final PlotCancellation cancellation) {
        final String normalizedExpression = normalizeExpression(expression);
        final Map<String, String> baseVariables = copyVariables(variables);

        final double minX = Math.min(bounds.minX(), bounds.maxX());
        final double maxX = Math.max(bounds.minX(), bounds.maxX());
        final double minY = Math.min(bounds.minY(), bounds.maxY());
        final double maxY = Math.max(bounds.minY(), bounds.maxY());

        final int cellsX = Math.max(1, nx);
        final int cellsY = Math.max(1, ny);

        final double dx = (maxX - minX) / cellsX;
        final double dy = (maxY - minY) / cellsY;

        final double[][] field = new double[cellsY + 1][cellsX + 1];
        final Map<String, String> evalVars = new HashMap<>(baseVariables);

        for (int iy = 0; iy <= cellsY; iy++) {
            if (cancellation.isCancelled()) {
                return List.of();
            }

            final double y = minY + iy * dy;
            evalVars.put("y", Double.toString(y));

            for (int ix = 0; ix <= cellsX; ix++) {
                final double x = minX + ix * dx;
                evalVars.put("x", Double.toString(x));

                final BigNumber value = evaluate(normalizedExpression, evalVars);
                field[iy][ix] = value == null ? Double.NaN : toDouble(value);
            }
        }

        final List<LineSegment> segments = new ArrayList<>(cellsX * cellsY);

        for (int iy = 0; iy < cellsY; iy++) {
            if (cancellation.isCancelled()) {
                return List.of();
            }

            final double y0 = minY + iy * dy;
            final double y1 = y0 + dy;

            for (int ix = 0; ix < cellsX; ix++) {
                final double x0 = minX + ix * dx;
                final double x1 = x0 + dx;

                final double f00 = field[iy][ix];
                final double f10 = field[iy][ix + 1];
                final double f01 = field[iy + 1][ix];
                final double f11 = field[iy + 1][ix + 1];

                if (!Double.isFinite(f00) || !Double.isFinite(f10) || !Double.isFinite(f01) || !Double.isFinite(f11)) {
                    continue;
                }

                final int code = contourCode(f00, f10, f11, f01);
                if (code == 0 || code == 15) {
                    continue;
                }

                final GraphFxPoint bottom = interpolateZeroCrossing(x0, y0, f00, x1, y0, f10);
                final GraphFxPoint right = interpolateZeroCrossing(x1, y0, f10, x1, y1, f11);
                final GraphFxPoint top = interpolateZeroCrossing(x0, y1, f01, x1, y1, f11);
                final GraphFxPoint left = interpolateZeroCrossing(x0, y0, f00, x0, y1, f01);

                appendSegmentsForCase(segments, code, f00, f10, f11, f01, bottom, right, top, left);
            }
        }

        return segments;
    }

    /**
     * Attempts to detect whether {@code F(x,y)} is affine-linear in {@code x} and {@code y} and, if so,
     * returns a viewport-clipped line segment for {@code F(x,y)=0}.
     *
     * <p>
     * Coefficients are derived from evaluations:
     * </p>
     * <ul>
     *   <li>{@code c = F(0,0)}</li>
     *   <li>{@code a = F(1,0) - c}</li>
     *   <li>{@code b = F(0,1) - c}</li>
     * </ul>
     *
     * <p>
     * Linearity is validated at three additional points: {@code (2,0)}, {@code (0,2)}, {@code (1,1)}.
     * If validation fails, {@code null} is returned and the caller should fall back to marching squares.
     * </p>
     *
     * @param expression   normalized expression
     * @param variables    external variables
     * @param bounds       viewport bounds
     * @param cancellation cancellation hook
     * @return a clipped segment if affine-linear; otherwise {@code null}
     */
    private LineSegment tryCreateLinearImplicitSegment(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final WorldBounds bounds, @NonNull final PlotCancellation cancellation) {
        final Map<String, String> evalVars = new HashMap<>(variables);

        final BigNumber f00 = evaluateAt(expression, evalVars, BigNumbers.ZERO, BigNumbers.ZERO);
        if (f00 == null || cancellation.isCancelled()) {
            return null;
        }

        final BigNumber f10 = evaluateAt(expression, evalVars, BigNumbers.ONE, BigNumbers.ZERO);
        final BigNumber f01 = evaluateAt(expression, evalVars, BigNumbers.ZERO, BigNumbers.ONE);
        if (f10 == null || f01 == null || cancellation.isCancelled()) {
            return null;
        }

        final BigNumber c = f00;
        final BigNumber a = f10.subtract(c);
        final BigNumber b = f01.subtract(c);

        if (!isAffineVerified(expression, evalVars, a, b, c, cancellation)) {
            return null;
        }

        final double minX = Math.min(bounds.minX(), bounds.maxX());
        final double maxX = Math.max(bounds.minX(), bounds.maxX());
        final double minY = Math.min(bounds.minY(), bounds.maxY());
        final double maxY = Math.max(bounds.minY(), bounds.maxY());

        final BigNumber bnMinX = new BigNumber(minX);
        final BigNumber bnMaxX = new BigNumber(maxX);
        final BigNumber bnMinY = new BigNumber(minY);
        final BigNumber bnMaxY = new BigNumber(maxY);

        final List<GraphFxPoint> intersections = new ArrayList<>(4);

        if (!isEffectivelyZero(b)) {
            addIntersectionIfVisible(intersections, bnMinX, solveY(a, b, c, bnMinX), minX, maxX, minY, maxY);
            addIntersectionIfVisible(intersections, bnMaxX, solveY(a, b, c, bnMaxX), minX, maxX, minY, maxY);
        }

        if (!isEffectivelyZero(a)) {
            addIntersectionIfVisible(intersections, solveX(a, b, c, bnMinY), bnMinY, minX, maxX, minY, maxY);
            addIntersectionIfVisible(intersections, solveX(a, b, c, bnMaxY), bnMaxY, minX, maxX, minY, maxY);
        }

        if (intersections.size() < 2) {
            return null;
        }

        final GraphFxPoint p0 = intersections.get(0);
        GraphFxPoint bestP1 = intersections.get(1);
        double bestDist = distanceSq(p0, bestP1);

        for (int i = 0; i < intersections.size(); i++) {
            for (int j = i + 1; j < intersections.size(); j++) {
                final double d = distanceSq(intersections.get(i), intersections.get(j));
                if (d > bestDist) {
                    bestDist = d;
                    bestP1 = intersections.get(j);
                }
            }
        }

        return new LineSegment(p0, bestP1);
    }

    /**
     * Verifies that the expression behaves like {@code a*x + b*y + c} by checking a small set of reference points.
     *
     * @param expression   normalized expression
     * @param evalVars     mutable evaluation variable map
     * @param a            affine coefficient for x
     * @param b            affine coefficient for y
     * @param c            constant term
     * @param cancellation cancellation hook
     * @return {@code true} if the function matches the affine model within tolerance; {@code false} otherwise
     */
    private boolean isAffineVerified(@NonNull final String expression, @NonNull final Map<String, String> evalVars, @NonNull final BigNumber a, @NonNull final BigNumber b, @NonNull final BigNumber c, @NonNull final PlotCancellation cancellation) {
        final BigNumber f20 = evaluateAt(expression, evalVars, BigNumbers.TWO, BigNumbers.ZERO);
        final BigNumber f02 = evaluateAt(expression, evalVars, BigNumbers.ZERO, BigNumbers.TWO);
        final BigNumber f11 = evaluateAt(expression, evalVars, BigNumbers.ONE, BigNumbers.ONE);

        if (f20 == null || f02 == null || f11 == null || cancellation.isCancelled()) {
            return false;
        }

        final BigNumber expected20 = c.add(a.multiply(BigNumbers.TWO));
        final BigNumber expected02 = c.add(b.multiply(BigNumbers.TWO));
        final BigNumber expected11 = c.add(a).add(b);

        return approxEquals(f20, expected20) && approxEquals(f02, expected02) && approxEquals(f11, expected11);
    }

    /**
     * Evaluates the expression using the engine with the provided variables map.
     *
     * <p>
     * Any exception thrown by the engine is treated as an invalid evaluation and results in {@code null}.
     * This keeps plotting robust against discontinuities and invalid intermediate states.
     * </p>
     *
     * @param expression normalized expression
     * @param variables  evaluation variable map (will be read by the engine)
     * @return evaluated value as {@link BigNumber}, or {@code null} if evaluation failed
     */
    private BigNumber evaluate(@NonNull final String expression, @NonNull final Map<String, String> variables) {
        try {
            return calculatorEngine.evaluate(expression, variables);
        } catch (final Exception ignored) {
            return null;
        }
    }

    /**
     * Evaluates {@code F(x,y)} at BigNumber coordinates by updating the provided variable map.
     *
     * <p>
     * This method exists primarily for the affine-linear fast path where evaluations should be performed in
     * {@link BigNumber} space to minimize error.
     * </p>
     *
     * @param expression normalized expression
     * @param variables  mutable variable map to be updated with {@code x} and {@code y}
     * @param x          x coordinate as {@link BigNumber}
     * @param y          y coordinate as {@link BigNumber}
     * @return evaluation result or {@code null} if evaluation failed
     */
    private BigNumber evaluateAt(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigNumber x, @NonNull final BigNumber y) {
        variables.put("x", x.toString());
        variables.put("y", y.toString());
        return evaluate(expression, variables);
    }

    /**
     * Determines the marching squares case code for a cell.
     *
     * <p>
     * Bits are assigned as:
     * </p>
     * <ul>
     *   <li>bit 0: f00 (bottom-left)</li>
     *   <li>bit 1: f10 (bottom-right)</li>
     *   <li>bit 2: f11 (top-right)</li>
     *   <li>bit 3: f01 (top-left)</li>
     * </ul>
     *
     * @param f00 field value at (x0,y0)
     * @param f10 field value at (x1,y0)
     * @param f11 field value at (x1,y1)
     * @param f01 field value at (x0,y1)
     * @return case code in range [0..15]
     */
    private static int contourCode(final double f00, final double f10, final double f11, final double f01) {
        final int c00 = signBit(f00);
        final int c10 = signBit(f10);
        final int c11 = signBit(f11);
        final int c01 = signBit(f01);
        return (c00) | (c10 << 1) | (c11 << 2) | (c01 << 3);
    }

    /**
     * Converts a field value into a marching squares sign bit.
     *
     * <p>
     * Values within {@link #CONTOUR_EPSILON} of zero are treated as non-negative to reduce noise around the contour.
     * </p>
     *
     * @param value field value
     * @return 1 if negative, 0 otherwise
     */
    private static int signBit(final double value) {
        if (Math.abs(value) <= CONTOUR_EPSILON) {
            return 0;
        }
        return value < 0 ? 1 : 0;
    }

    /**
     * Computes an approximate zero-crossing point on an edge by linear interpolation.
     *
     * <p>
     * If the denominator is near zero, this method falls back to {@code t=0.5} to avoid unstable division.
     * </p>
     *
     * @param x0 x of first endpoint
     * @param y0 y of first endpoint
     * @param f0 field value at first endpoint
     * @param x1 x of second endpoint
     * @param y1 y of second endpoint
     * @param f1 field value at second endpoint
     * @return interpolated point on the edge
     */
    private static GraphFxPoint interpolateZeroCrossing(final double x0, final double y0, final double f0, final double x1, final double y1, final double f1) {
        final double denom = (f0 - f1);
        final double t = Math.abs(denom) <= CONTOUR_EPSILON ? 0.5 : (f0 / denom);
        final double clamped = Math.max(0.0, Math.min(1.0, t));
        return new GraphFxPoint(x0 + (x1 - x0) * clamped, y0 + (y1 - y0) * clamped);
    }

    /**
     * Appends one or two segments for a given marching squares case.
     *
     * <p>
     * Ambiguous saddle cases (5 and 10) are resolved using the average field value at the cell center.
     * This heuristic improves visual continuity without requiring additional evaluations.
     * </p>
     *
     * @param out    output segment list
     * @param code   marching squares case code
     * @param f00    field value bottom-left
     * @param f10    field value bottom-right
     * @param f11    field value top-right
     * @param f01    field value top-left
     * @param bottom interpolated point on bottom edge
     * @param right  interpolated point on right edge
     * @param top    interpolated point on top edge
     * @param left   interpolated point on left edge
     */
    private static void appendSegmentsForCase(@NonNull final List<LineSegment> out, final int code, final double f00, final double f10, final double f11, final double f01, final GraphFxPoint bottom, final GraphFxPoint right, final GraphFxPoint top, final GraphFxPoint left) {
        switch (code) {
            case 1, 14 -> out.add(new LineSegment(left, bottom));
            case 2, 13 -> out.add(new LineSegment(bottom, right));
            case 3, 12 -> out.add(new LineSegment(left, right));
            case 4, 11 -> out.add(new LineSegment(right, top));
            case 6, 9 -> out.add(new LineSegment(bottom, top));
            case 7, 8 -> out.add(new LineSegment(left, top));
            case 5, 10 -> {
                final double center = (f00 + f10 + f11 + f01) / 4.0;
                final boolean centerNegative = center < 0;

                if (code == 5) {
                    if (centerNegative) {
                        out.add(new LineSegment(left, bottom));
                        out.add(new LineSegment(right, top));
                    } else {
                        out.add(new LineSegment(bottom, right));
                        out.add(new LineSegment(left, top));
                    }
                } else {
                    if (centerNegative) {
                        out.add(new LineSegment(bottom, right));
                        out.add(new LineSegment(left, top));
                    } else {
                        out.add(new LineSegment(left, bottom));
                        out.add(new LineSegment(right, top));
                    }
                }
            }
            default -> {
                // no-op
            }
        }
    }

    /**
     * Compares two BigNumber values for approximate equality using {@link #LINEAR_TOLERANCE}.
     *
     * @param a first value
     * @param b second value
     * @return {@code true} if {@code |a-b| <= tolerance}; {@code false} otherwise
     */
    private static boolean approxEquals(@NonNull final BigNumber a, @NonNull final BigNumber b) {
        return a.subtract(b).abs().compareTo(LINEAR_TOLERANCE) <= 0;
    }

    /**
     * Checks whether a BigNumber is effectively zero using {@link #LINEAR_TOLERANCE}.
     *
     * <p>
     * This method is used when deciding whether the affine line is parallel to one axis, in which case certain
     * intersection computations would require division by near-zero coefficients.
     * </p>
     *
     * @param value value to test
     * @return {@code true} if {@code |value| <= tolerance}; {@code false} otherwise
     */
    private static boolean isEffectivelyZero(@NonNull final BigNumber value) {
        return value.abs().compareTo(LINEAR_TOLERANCE) <= 0;
    }

    /**
     * Solves {@code a*x + b*y + c = 0} for {@code y} given {@code x}.
     *
     * @param a coefficient a
     * @param b coefficient b (must not be zero)
     * @param c constant term
     * @param x x value
     * @return y value as {@link BigNumber}
     */
    private static BigNumber solveY(@NonNull final BigNumber a, @NonNull final BigNumber b, @NonNull final BigNumber c, @NonNull final BigNumber x) {
        return a.multiply(x).add(c).multiply(BigNumbers.NEGATIVE_ONE).divide(b);
    }

    /**
     * Solves {@code a*x + b*y + c = 0} for {@code x} given {@code y}.
     *
     * @param a coefficient a (must not be zero)
     * @param b coefficient b
     * @param c constant term
     * @param y y value
     * @return x value as {@link BigNumber}
     */
    private static BigNumber solveX(@NonNull final BigNumber a, @NonNull final BigNumber b, @NonNull final BigNumber c, @NonNull final BigNumber y) {
        return b.multiply(y).add(c).multiply(BigNumbers.NEGATIVE_ONE).divide(a);
    }

    /**
     * Adds an intersection point to the output list if it lies within the viewport bounds.
     *
     * <p>
     * The intersection is computed in {@link BigNumber} and converted to {@code double} only for geometric output.
     * </p>
     *
     * @param out  output list
     * @param x    x coordinate as {@link BigNumber}
     * @param y    y coordinate as {@link BigNumber}
     * @param minX viewport min x
     * @param maxX viewport max x
     * @param minY viewport min y
     * @param maxY viewport max y
     */
    private static void addIntersectionIfVisible(@NonNull final List<GraphFxPoint> out, @NonNull final BigNumber x, @NonNull final BigNumber y, final double minX, final double maxX, final double minY, final double maxY) {

        final double xd = toDouble(x);
        final double yd = toDouble(y);

        if (!Double.isFinite(xd) || !Double.isFinite(yd)) {
            return;
        }

        final double margin = 1e-9;
        if (xd < minX - margin || xd > maxX + margin) {
            return;
        }
        if (yd < minY - margin || yd > maxY + margin) {
            return;
        }

        out.add(new GraphFxPoint(xd, yd));
    }

    /**
     * Returns squared distance between two points.
     *
     * <p>
     * Squared distance avoids computing a square root and is sufficient for comparisons.
     * </p>
     *
     * @param a first point
     * @param b second point
     * @return squared distance
     */
    private static double distanceSq(@NonNull final GraphFxPoint a, @NonNull final GraphFxPoint b) {
        final double dx = a.x() - b.x();
        final double dy = a.y() - b.y();
        return dx * dx + dy * dy;
    }

    /**
     * Converts a {@link BigNumber} to a {@code double}.
     *
     * <p>
     * This is used only at the final stage to create JavaFX-friendly geometry.
     * The evaluation itself remains in BigNumber inside the engine.
     * </p>
     *
     * @param value BigNumber value
     * @return double value
     */
    private static double toDouble(@NonNull final BigNumber value) {
        return value.toBigDecimal().doubleValue();
    }

    /**
     * Clamps an integer value into the inclusive range {@code [min, max]}.
     *
     * @param value input value
     * @param min   minimum allowed value
     * @param max   maximum allowed value
     * @return clamped value
     */
    private static int clampInt(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Creates a defensive copy of the variables map and validates that no key/value is {@code null}.
     *
     * <p>
     * Plotting mutates variable maps internally (injecting {@code x} and {@code y}), so callers should not
     * pass mutable maps that are shared with other code. This method guarantees a safe copy.
     * </p>
     *
     * @param variables input variables map
     * @return immutable safe copy
     * @throws NullPointerException if any key/value is {@code null}
     */
    private static Map<String, String> copyVariables(@NonNull final Map<String, String> variables) {
        final Map<String, String> out = new HashMap<>(Math.max(8, variables.size()));

        for (final Map.Entry<String, String> entry : variables.entrySet()) {
            final String key = Objects.requireNonNull(entry.getKey(), "variable name must not be null");
            final String value = Objects.requireNonNull(entry.getValue(), "variable '" + key + "' value must not be null");
            out.put(key, value);
        }

        return Map.copyOf(out);
    }

    /**
     * Normalizes expression input to improve user ergonomics.
     *
     * <p>
     * This method is designed to be fast (single pass, minimal allocations) because it may be called frequently.
     * It is intentionally conservative and avoids transformations that could break common function notation.
     * </p>
     *
     * @param expression raw expression
     * @return normalized expression (never {@code null})
     */
    private static String normalizeExpression(@NonNull final String expression) {
        final String trimmed = expression.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        final StringBuilder out = new StringBuilder(trimmed.length() + 16);

        char prevOut = '\0';
        int i = 0;
        while (i < trimmed.length()) {
            final char ch = trimmed.charAt(i);

            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            // decimal comma between digits -> dot
            if (ch == ',' && i > 0 && i + 1 < trimmed.length()) {
                final char prev = lastNonWhitespace(trimmed, i - 1);
                final char next = nextNonWhitespace(trimmed, i + 1);
                if (Character.isDigit(prev) && Character.isDigit(next)) {
                    maybeInsertImplicitMultiply(out, prevOut, '.'); // treat '.' like a digit continuation
                    out.append('.');
                    prevOut = '.';
                    i++;
                    continue;
                }
            }

            // implicit multiplication insertion (safe cases)
            maybeInsertImplicitMultiply(out, prevOut, ch);

            out.append(ch);
            prevOut = ch;
            i++;
        }

        return out.toString();
    }

    /**
     * Inserts {@code *} into the output buffer for common implicit multiplication patterns.
     *
     * <p>
     * This method is intentionally conservative to avoid breaking function tokens.
     * It inserts {@code *} for:
     * </p>
     * <ul>
     *   <li>digit / '.' followed by identifier or '('</li>
     *   <li>')' followed by digit / identifier / '('</li>
     *   <li>single-letter identifier followed by '(' (e.g., x(y)), but not multi-letter tokens (e.g., sin(x))</li>
     * </ul>
     *
     * @param out     output buffer
     * @param prevOut last written non-whitespace character (or {@code '\0'} if none)
     * @param next    current character to be appended
     */
    private static void maybeInsertImplicitMultiply(final StringBuilder out, final char prevOut, final char next) {
        if (out.isEmpty()) {
            return;
        }

        final boolean prevIsDigitish = Character.isDigit(prevOut) || prevOut == '.';
        final boolean nextStartsValue = Character.isLetter(next) || next == '(';

        // number -> identifier/paren
        if (prevIsDigitish && nextStartsValue) {
            out.append('*');
            return;
        }

        // close-paren -> (identifier/number/paren)
        if (prevOut == ')' && (Character.isLetter(next) || Character.isDigit(next) || next == '(')) {
            out.append('*');
            return;
        }

        // single-letter identifier -> '(' (avoid breaking sin(x), cos(x), ...)
        if (next == '(' && Character.isLetter(prevOut)) {
            final int len = out.length();
            final char beforePrev = len >= 2 ? out.charAt(len - 2) : '\0';
            final boolean prevIsSingleLetterToken = !isIdentifierChar(beforePrev) && isIdentifierChar(prevOut);
            if (prevIsSingleLetterToken) {
                out.append('*');
            }
        }
    }

    /**
     * Returns the previous non-whitespace character in a string or {@code '\0'} if none exists.
     *
     * @param string    string
     * @param fromIndex starting index (inclusive) to search backward
     * @return previous non-whitespace character or {@code '\0'}
     */
    private static char lastNonWhitespace(final String string, final int fromIndex) {
        for (int i = fromIndex; i >= 0; i--) {
            final char c = string.charAt(i);
            if (!Character.isWhitespace(c)) {
                return c;
            }
        }
        return '\0';
    }

    /**
     * Returns the next non-whitespace character in a string or {@code '\0'} if none exists.
     *
     * @param s         string
     * @param fromIndex starting index (inclusive) to search forward
     * @return next non-whitespace character or {@code '\0'}
     */
    private static char nextNonWhitespace(final String s, final int fromIndex) {
        for (int i = fromIndex; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (!Character.isWhitespace(c)) {
                return c;
            }
        }
        return '\0';
    }

    /**
     * Checks whether the given string contains a standalone identifier consisting of a single character.
     *
     * @param s     string to scan
     * @param lower lowercase variant of the identifier
     * @param upper uppercase variant of the identifier
     * @return {@code true} if present as standalone token; {@code false} otherwise
     */
    private static boolean containsStandaloneIdentifier(final String s, final char lower, final char upper) {
        final int n = s.length();

        for (int i = 0; i < n; i++) {
            final char c = s.charAt(i);
            if (c != lower && c != upper) {
                continue;
            }

            final char before = i > 0 ? s.charAt(i - 1) : '\0';
            final char after = i + 1 < n ? s.charAt(i + 1) : '\0';

            if (!isIdentifierChar(before) && !isIdentifierChar(after)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if a character is considered part of an identifier token.
     *
     * <p>
     * This matches common parser behavior and is used for conservative token boundary detection.
     * </p>
     *
     * @param c character
     * @return {@code true} if identifier character; {@code false} otherwise
     */
    private static boolean isIdentifierChar(final char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

}
