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

package com.mlprograms.justmath.graphfx;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import javafx.geometry.Point2D;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Double.isFinite;
import static java.lang.Math.*;

/**
 * High-performance plot calculator for GraphFx that evaluates expressions with JustMath ({@link BigNumber})
 * and creates drawable geometry for GraphFx overlays.
 *
 * <h2>Supported plot modes</h2>
 * <ul>
 *   <li><strong>Explicit</strong>: if the expression does not contain {@code y}, it is interpreted as {@code y = f(x)}.</li>
 *   <li><strong>Implicit</strong>: if the expression contains {@code y}, it is interpreted as {@code F(x,y) = 0} and
 *       the 0-contour is extracted.</li>
 * </ul>
 *
 * <h2>Performance strategy</h2>
 * <ul>
 *   <li><strong>Fast-path for linear implicit equations</strong> such as {@code 2x-3y+4,3a}:
 *       the line is derived analytically using a few BigNumber evaluations and drawn as a single segment.</li>
 *   <li><strong>Implicit non-linear curves</strong> are computed with marching squares, but the scalar field is
 *       evaluated only at grid vertices: {@code (nx+1)*(ny+1)} evaluations (instead of {@code 4*nx*ny}).</li>
 *   <li>A {@link PlotCancellation} is checked frequently so outdated computations stop early.</li>
 * </ul>
 *
 * <h2>Expression normalization</h2>
 * <p>
 * For user convenience, normalization applies:
 * </p>
 * <ul>
 *   <li>Decimal comma: {@code 4,3} → {@code 4.3}</li>
 *   <li>Implicit multiplication between number and variable: {@code 2x} → {@code 2*x}</li>
 *   <li>Implicit multiplication between number and {@code (}: {@code 2(x)} → {@code 2*(x)}</li>
 *   <li>Implicit multiplication between {@code )} and variable/number: {@code )(} is not modified, but {@code )x} → {@code )*x}</li>
 * </ul>
 *
 * <p>
 * The normalization is intentionally conservative and does not attempt to parse function tokens.
 * </p>
 */
public final class GraphFxCalculator {

    /**
     * Cancellation hook used to abort long-running plot computations when they become obsolete.
     */
    @FunctionalInterface
    public interface PlotCancellation {

        /**
         * Returns {@code true} if the current computation should stop as soon as possible.
         *
         * @return {@code true} if cancelled; {@code false} otherwise
         */
        boolean isCancelled();
    }

    /**
     * Immutable bounds in world coordinates.
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
    public record LineSegment(Point2D a, Point2D b) {
    }

    /**
     * Result container for plotting.
     * <p>
     * Exactly one of {@link #polyline} or {@link #segments} is typically used, depending on plot mode.
     * </p>
     *
     * @param polyline polyline points (explicit plots)
     * @param segments segments (implicit plots)
     */
    public record PlotGeometry(List<Point2D> polyline, List<LineSegment> segments) {

        /**
         * Creates an empty geometry result.
         *
         * @return empty geometry
         */
        public static PlotGeometry empty() {
            return new PlotGeometry(List.of(), List.of());
        }
    }

    private static final BigNumber BN_ZERO = new BigNumber("0");
    private static final BigNumber BN_ONE = new BigNumber("1");
    private static final BigNumber BN_TWO = new BigNumber("2");

    private static final BigNumber LINEAR_TOLERANCE = new BigNumber("0.000000000000000000000000000001"); // 1e-30
    private static final double ZERO_EPSILON_DOUBLE = 1e-12;

    private final CalculatorEngine engine;

    /**
     * Creates a new plot calculator using a fresh {@link CalculatorEngine} instance.
     */
    public GraphFxCalculator() {
        this(new CalculatorEngine());
    }

    /**
     * Creates a new plot calculator using the given {@link CalculatorEngine}.
     *
     * @param engine engine to use; must not be {@code null}
     * @throws NullPointerException if {@code engine} is {@code null}
     */
    public GraphFxCalculator(@NonNull final CalculatorEngine engine) {
        this.engine = engine;
    }

    /**
     * Returns {@code true} if the expression contains a standalone {@code y} variable token.
     *
     * @param expression input expression; must not be {@code null}
     * @return {@code true} if {@code y} is present as a variable token; {@code false} otherwise
     * @throws NullPointerException if {@code expression} is {@code null}
     */
    public boolean containsYVariable(@NonNull final String expression) {
        final String normalized = normalizeExpression(expression);
        return normalized.matches("(?s).*\\b[yY]\\b.*");
    }

    /**
     * Computes plot geometry for the provided expression and viewport.
     * <p>
     * If the expression contains {@code y}, the implicit curve {@code F(x,y)=0} is plotted.
     * Otherwise, it is plotted as {@code y=f(x)}.
     * </p>
     *
     * <p>
     * This method is intended to be called from a background thread. It performs no JavaFX UI operations.
     * </p>
     *
     * @param expression   expression to plot; must not be {@code null}
     * @param variables    external variables (e.g. {@code a}); must not be {@code null}
     * @param bounds       current viewport bounds; must not be {@code null}
     * @param pixelWidth   viewport width in pixels (used for adaptive resolution)
     * @param pixelHeight  viewport height in pixels (used for adaptive resolution)
     * @param cancellation cancellation hook; must not be {@code null}
     * @return plot geometry (polyline or segments)
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public PlotGeometry plot(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final WorldBounds bounds, final int pixelWidth, final int pixelHeight, @NonNull final PlotCancellation cancellation) {
        final String normalizedExpression = normalizeExpression(expression);

        final boolean implicit = normalizedExpression.matches("(?s).*\\b[yY]\\b.*");
        if (!implicit) {
            final int samples = clampInt(pixelWidth, 250, 1400);
            final List<Point2D> polyline = createExplicitPolyline(normalizedExpression, variables, bounds, samples, cancellation);
            return cancellation.isCancelled() ? PlotGeometry.empty() : new PlotGeometry(polyline, List.of());
        }

        final LineSegment fastLinear = tryCreateLinearImplicitSegment(normalizedExpression, variables, bounds, cancellation);
        if (fastLinear != null) {
            return cancellation.isCancelled() ? PlotGeometry.empty() : new PlotGeometry(List.of(), List.of(fastLinear));
        }

        final int cellSizePx = 18;
        final int nx = clampInt(max(8, pixelWidth / cellSizePx), 40, 140);
        final int ny = clampInt(max(8, pixelHeight / cellSizePx), 30, 120);

        final List<LineSegment> segments = createImplicitZeroContourSegments(normalizedExpression, variables, bounds, nx, ny, cancellation);
        return cancellation.isCancelled() ? PlotGeometry.empty() : new PlotGeometry(List.of(), segments);
    }

    List<Point2D> createExplicitPolyline(@NonNull final String normalizedExpression, @NonNull final Map<String, String> baseVariables, @NonNull final WorldBounds bounds, final int samples, @NonNull final PlotCancellation cancellation) {
        final double minX = min(bounds.minX(), bounds.maxX());
        final double maxX = max(bounds.minX(), bounds.maxX());

        final double step = (maxX - minX) / max(1, samples - 1);

        final Map<String, String> evaluationVariables = new HashMap<>(baseVariables);
        evaluationVariables.put("y", BN_ZERO.toString());

        final List<Point2D> result = new ArrayList<>(samples);

        for (int i = 0; i < samples; i++) {
            if (cancellation.isCancelled()) {
                return List.of();
            }

            final double x = minX + i * step;
            evaluationVariables.put("x", String.valueOf(x));

            final BigNumber yValue;
            try {
                yValue = engine.evaluate(normalizedExpression, evaluationVariables);
            } catch (final Exception ignored) {
                continue;
            }

            final double y = toDouble(yValue);
            if (!isFinite(y)) {
                continue;
            }

            result.add(new Point2D(x, y));
        }

        return result;
    }

    /**
     * Detects whether {@code F(x,y)} is affine-linear in {@code x} and {@code y} and if so returns a single
     * viewport-clipped segment for {@code F(x,y)=0}.
     *
     * <p>
     * Coefficients are derived using finite differences:
     * </p>
     * <ul>
     *   <li>{@code c = F(0,0)}</li>
     *   <li>{@code a = F(1,0) - c}</li>
     *   <li>{@code b = F(0,1) - c}</li>
     * </ul>
     *
     * <p>
     * Linearity is verified at a few additional points. If verification fails, {@code null} is returned.
     * </p>
     */
    private LineSegment tryCreateLinearImplicitSegment(@NonNull final String normalizedExpression, @NonNull final Map<String, String> baseVariables, @NonNull final WorldBounds bounds, @NonNull final PlotCancellation cancellation) {
        final Map<String, String> vars = new HashMap<>(baseVariables);

        final BigNumber f00 = evaluateBigNumber(normalizedExpression, vars, BN_ZERO, BN_ZERO);
        if (f00 == null || cancellation.isCancelled()) {
            return null;
        }

        final BigNumber f10 = evaluateBigNumber(normalizedExpression, vars, BN_ONE, BN_ZERO);
        final BigNumber f01 = evaluateBigNumber(normalizedExpression, vars, BN_ZERO, BN_ONE);
        if (f10 == null || f01 == null || cancellation.isCancelled()) {
            return null;
        }

        final BigNumber c = f00;
        final BigNumber a = f10.subtract(c);
        final BigNumber b = f01.subtract(c);

        if (!verifyAffine(normalizedExpression, vars, a, b, c, cancellation)) {
            return null;
        }

        final double minX = min(bounds.minX(), bounds.maxX());
        final double maxX = max(bounds.minX(), bounds.maxX());
        final double minY = min(bounds.minY(), bounds.maxY());
        final double maxY = max(bounds.minY(), bounds.maxY());

        final BigNumber bnMinX = new BigNumber(minX);
        final BigNumber bnMaxX = new BigNumber(maxX);
        final BigNumber bnMinY = new BigNumber(minY);
        final BigNumber bnMaxY = new BigNumber(maxY);

        final List<Point2D> intersections = new ArrayList<>(4);

        if (!isEffectivelyZero(b)) {
            addIntersectionIfInside(intersections, bnMinX, solveY(a, b, c, bnMinX), minX, maxX, minY, maxY);

            addIntersectionIfInside(intersections, bnMaxX, solveY(a, b, c, bnMaxX), minX, maxX, minY, maxY);
        }

        if (!isEffectivelyZero(a)) {
            addIntersectionIfInside(intersections, solveX(a, b, c, bnMinY), bnMinY, minX, maxX, minY, maxY);

            addIntersectionIfInside(intersections, solveX(a, b, c, bnMaxY), bnMaxY, minX, maxX, minY, maxY);
        }

        if (intersections.size() < 2) {
            return null;
        }

        final Point2D aPoint = intersections.getFirst();
        Point2D bPoint = intersections.get(1);
        double bestDist = distanceSq(aPoint, bPoint);

        for (int i = 0; i < intersections.size(); i++) {
            for (int j = i + 1; j < intersections.size(); j++) {
                final double dist = distanceSq(intersections.get(i), intersections.get(j));
                if (dist > bestDist) {
                    bestDist = dist;
                    bPoint = intersections.get(j);
                }
            }
        }

        return new LineSegment(aPoint, bPoint);
    }

    private boolean verifyAffine(@NonNull final String normalizedExpression, @NonNull final Map<String, String> vars, @NonNull final BigNumber a, @NonNull final BigNumber b, @NonNull final BigNumber c, @NonNull final PlotCancellation cancellation) {
        final BigNumber f20 = evaluateBigNumber(normalizedExpression, vars, BN_TWO, BN_ZERO);
        final BigNumber f02 = evaluateBigNumber(normalizedExpression, vars, BN_ZERO, BN_TWO);
        final BigNumber f11 = evaluateBigNumber(normalizedExpression, vars, BN_ONE, BN_ONE);

        if (f20 == null || f02 == null || f11 == null || cancellation.isCancelled()) {
            return false;
        }

        final BigNumber expected20 = c.add(a.multiply(BN_TWO));
        final BigNumber expected02 = c.add(b.multiply(BN_TWO));
        final BigNumber expected11 = c.add(a).add(b);

        return approxEquals(f20, expected20) && approxEquals(f02, expected02) && approxEquals(f11, expected11);
    }

    List<LineSegment> createImplicitZeroContourSegments(@NonNull final String normalizedExpression, @NonNull final Map<String, String> baseVariables, @NonNull final WorldBounds bounds, final int nx, final int ny, @NonNull final PlotCancellation cancellation) {
        final double minX = min(bounds.minX(), bounds.maxX());
        final double maxX = max(bounds.minX(), bounds.maxX());
        final double minY = min(bounds.minY(), bounds.maxY());
        final double maxY = max(bounds.minY(), bounds.maxY());

        final double dx = (maxX - minX) / nx;
        final double dy = (maxY - minY) / ny;

        final double[][] field = new double[ny + 1][nx + 1];
        final Map<String, String> vars = new HashMap<>(baseVariables);

        for (int iy = 0; iy <= ny; iy++) {
            if (cancellation.isCancelled()) {
                return List.of();
            }

            final double y = minY + iy * dy;
            vars.put("y", String.valueOf(y));

            for (int ix = 0; ix <= nx; ix++) {
                final double x = minX + ix * dx;
                vars.put("x", String.valueOf(x));

                final BigNumber value;
                try {
                    value = engine.evaluate(normalizedExpression, vars);
                } catch (final Exception ignored) {
                    field[iy][ix] = Double.NaN;
                    continue;
                }

                field[iy][ix] = toDouble(value);
            }
        }

        final List<LineSegment> segments = new ArrayList<>(nx * ny);

        for (int iy = 0; iy < ny; iy++) {
            if (cancellation.isCancelled()) {
                return List.of();
            }

            final double y0 = minY + iy * dy;
            final double y1 = y0 + dy;

            for (int ix = 0; ix < nx; ix++) {
                final double x0 = minX + ix * dx;
                final double x1 = x0 + dx;

                final double f00 = field[iy][ix];
                final double f10 = field[iy][ix + 1];
                final double f01 = field[iy + 1][ix];
                final double f11 = field[iy + 1][ix + 1];

                if (!isFinite(f00) || !isFinite(f10) || !isFinite(f01) || !isFinite(f11)) {
                    continue;
                }

                final int code = contourCode(f00, f10, f11, f01);
                if (code == 0 || code == 15) {
                    continue;
                }

                final Point2D bottom = intersect(x0, y0, f00, x1, y0, f10);
                final Point2D right = intersect(x1, y0, f10, x1, y1, f11);
                final Point2D top = intersect(x0, y1, f01, x1, y1, f11);
                final Point2D left = intersect(x0, y0, f00, x0, y1, f01);

                addSegmentsForCase(segments, code, f00, f10, f11, f01, bottom, right, top, left);
            }
        }

        return segments;
    }

    private static int contourCode(final double f00, final double f10, final double f11, final double f01) {
        final int c00 = signBit(f00);
        final int c10 = signBit(f10);
        final int c11 = signBit(f11);
        final int c01 = signBit(f01);
        return (c00) | (c10 << 1) | (c11 << 2) | (c01 << 3);
    }

    private static int signBit(final double value) {
        if (abs(value) <= ZERO_EPSILON_DOUBLE) {
            return 0;
        }
        return value < 0 ? 1 : 0;
    }

    private static Point2D intersect(final double x0, final double y0, final double f0, final double x1, final double y1, final double f1) {
        final double denom = (f0 - f1);
        final double t = abs(denom) <= ZERO_EPSILON_DOUBLE ? 0.5 : (f0 / denom);
        final double clamped = max(0.0, min(1.0, t));
        return new Point2D(x0 + (x1 - x0) * clamped, y0 + (y1 - y0) * clamped);
    }

    private static void addSegmentsForCase(@NonNull final List<LineSegment> out, final int code, final double f00, final double f10, final double f11, final double f01, final Point2D bottom, final Point2D right, final Point2D top, final Point2D left) {
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

    private BigNumber evaluateBigNumber(@NonNull final String normalizedExpression, @NonNull final Map<String, String> vars, @NonNull final BigNumber x, @NonNull final BigNumber y) {
        try {
            vars.put("x", x.toString());
            vars.put("y", y.toString());
            return engine.evaluate(normalizedExpression, vars);
        } catch (final Exception ignored) {
            return null;
        }
    }

    private static boolean approxEquals(@NonNull final BigNumber a, @NonNull final BigNumber b) {
        return a.subtract(b).abs().compareTo(LINEAR_TOLERANCE) <= 0;
    }

    private static boolean isEffectivelyZero(@NonNull final BigNumber value) {
        return value.abs().compareTo(LINEAR_TOLERANCE) <= 0;
    }

    private static BigNumber solveY(@NonNull final BigNumber a, @NonNull final BigNumber b, @NonNull final BigNumber c, @NonNull final BigNumber x) {
        // y = -(a*x + c) / b
        final BigNumber numerator = a.multiply(x).add(c).multiply(new BigNumber("-1"));
        return numerator.divide(b);
    }

    private static BigNumber solveX(@NonNull final BigNumber a, @NonNull final BigNumber b, @NonNull final BigNumber c, @NonNull final BigNumber y) {
        // x = -(b*y + c) / a
        final BigNumber numerator = b.multiply(y).add(c).multiply(new BigNumber("-1"));
        return numerator.divide(a);
    }

    private static void addIntersectionIfInside(@NonNull final List<Point2D> out, @NonNull final BigNumber x, @NonNull final BigNumber y, final double minX, final double maxX, final double minY, final double maxY) {
        final double xd = toDouble(x);
        final double yd = toDouble(y);

        if (!isFinite(xd) || !isFinite(yd)) {
            return;
        }

        if (xd < minX - 1e-9 || xd > maxX + 1e-9) {
            return;
        }
        if (yd < minY - 1e-9 || yd > maxY + 1e-9) {
            return;
        }

        out.add(new Point2D(xd, yd));
    }

    private static double distanceSq(@NonNull final Point2D a, @NonNull final Point2D b) {
        final double dx = a.getX() - b.getX();
        final double dy = a.getY() - b.getY();
        return dx * dx + dy * dy;
    }

    private static double toDouble(@NonNull final BigNumber value) {
        return value.toBigDecimal().doubleValue();
    }

    private static int clampInt(final int value, final int min, final int max) {
        return max(min, min(max, value));
    }

    private static String normalizeExpression(@NonNull final String expression) {
        String value = expression.trim();
        value = value.replaceAll("\\s+", "");

        // decimal comma -> decimal point (only number parts)
        value = value.replaceAll("(\\d),(\\d)", "$1.$2");

        // implicit multiplication: 2x -> 2*x
        value = value.replaceAll("(?<=\\d)(?=[A-Za-z])", "*");

        // implicit multiplication: 2( -> 2*(
        value = value.replaceAll("(?<=\\d)(?=\\()", "*");

        // implicit multiplication: )x or )2 -> )*x / )*2
        value = value.replaceAll("(?<=\\))(?=[A-Za-z0-9])", "*");

        return value;
    }

}
