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

package com.mlprograms.justmath.graphfx.core;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxLineSegment;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxPlotCancellation;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxPlotEngine;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxPlotGeometry;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxPlotRequest;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxWorldBounds;
import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High-performance plot engine for GraphFx that evaluates JustMath expressions using {@link BigNumber}
 * and generates drawable geometry in world coordinates.
 *
 * <h2>Plot modes</h2>
 * <ul>
 *   <li><strong>Explicit plot</strong> ({@code y = f(x)}): used when the expression does not contain a standalone {@code y} token.</li>
 *   <li><strong>Implicit plot</strong> ({@code F(x,y) = 0}): used when the expression contains a standalone {@code y} token.</li>
 * </ul>
 *
 * <h2>Robustness</h2>
 * <p>
 * Plotting must not crash on invalid evaluations. Therefore, engine evaluation failures are treated as missing values.
 * For explicit plots, missing values create path breaks (NaN sentinel points). For implicit plots, cells that contain
 * invalid values are skipped.
 * </p>
 *
 * <h2>Thread-safety</h2>
 * <p>This class is thread-safe if the underlying {@link CalculatorEngine} is thread-safe for concurrent evaluations.</p>
 */
public final class GraphFxCalculatorEngine implements GraphFxPlotEngine {

    /**
     * Tolerance for verifying affine linearity in implicit equations.
     */
    private static final BigNumber LINEAR_TOLERANCE = new BigNumber("0.000000000000000000000000000001");

    /**
     * Epsilon used in marching squares decisions and interpolation.
     */
    private static final double CONTOUR_EPSILON = 1e-12;

    private static final int EXPLICIT_SAMPLES_MIN = 250;
    private static final int EXPLICIT_SAMPLES_MAX = 1400;

    private static final int IMPLICIT_CELL_SIZE_PX = 18;
    private static final int IMPLICIT_NX_MIN = 40;
    private static final int IMPLICIT_NX_MAX = 140;
    private static final int IMPLICIT_NY_MIN = 30;
    private static final int IMPLICIT_NY_MAX = 120;

    private final CalculatorEngine calculatorEngine;

    /**
     * Creates a new plot engine backed by a default {@link CalculatorEngine}.
     * <p>
     * This constructor is a convenience for typical usage where a dedicated calculator configuration is not
     * required. If you need a customized calculator (precision, locale settings, additional functions, ...),
     * use {@link #GraphFxCalculatorEngine(CalculatorEngine)}.
     * </p>
     *
     * <h2>Trigonometric unit</h2>
     * <p>
     * GraphFx treats the world coordinate system as a mathematical plane where {@code x} and {@code y} values are
     * unit-less. For trigonometric expressions (for example {@code sin(x)}), the de-facto expectation in most Java
     * math libraries and plotting tools is that angles are interpreted as <strong>radians</strong>.
     * </p>
     * <p>
     * JustMath's {@link CalculatorEngine} supports different trigonometric modes in some versions (for example
     * degrees vs. radians). To provide stable and predictable plots across applications, this constructor
     * <em>best-effort</em> configures the default calculator instance to use radians.
     * </p>
     * <p>
     * The configuration is performed via reflection to avoid leaking any trigonometric-mode types through the
     * GraphFx public API. If the current {@link CalculatorEngine} implementation does not expose such a mode, the
     * default constructor is used as-is.
     *
     * @throws RuntimeException if the default {@link CalculatorEngine} cannot be constructed
     */
    public GraphFxCalculatorEngine() {
        this(createDefaultCalculatorEngine());
    }

    /**
     * Creates the default calculator engine used by GraphFx and configures it for radians when possible.
     * <p>
     * This method performs a best-effort reflection-based configuration to support multiple JustMath
     * {@link CalculatorEngine} variants without introducing a hard compile-time dependency on an internal
     * trigonometric-mode enum.
     * </p>
     *
     * @return a non-null calculator engine instance
     */
    private static CalculatorEngine createDefaultCalculatorEngine() {
        final CalculatorEngine calculatorEngine = tryConstructCalculatorEngineInRadians();

        if (tryConfigureCalculatorEngineForRadians(calculatorEngine)) {
            return calculatorEngine;
        }

        return calculatorEngine;
    }

    /**
     * Tries to create a {@link CalculatorEngine} instance using a constructor that accepts a trigonometric-mode
     * enum and configures it to radians.
     *
     * @return a calculator engine instance (never {@code null})
     */
    private static CalculatorEngine tryConstructCalculatorEngineInRadians() {
        final Object radianMode = tryResolveRadianModeEnumValue();
        if (radianMode == null) {
            return new CalculatorEngine();
        }

        final Class<?> modeClass = radianMode.getClass();

        // Common signatures used across JustMath versions.
        final Constructor<?>[] candidateConstructors = new Constructor<?>[]{
                tryGetPublicConstructor(CalculatorEngine.class, modeClass),
                tryGetPublicConstructor(CalculatorEngine.class, int.class, modeClass),
                tryGetPublicConstructor(CalculatorEngine.class, int.class, modeClass, Locale.class)
        };

        for (final Constructor<?> candidateConstructor : candidateConstructors) {
            if (candidateConstructor == null) {
                continue;
            }
            try {
                final Object instance;
                final Class<?>[] parameterTypes = candidateConstructor.getParameterTypes();
                if (parameterTypes.length == 1) {
                    instance = candidateConstructor.newInstance(radianMode);
                } else if (parameterTypes.length == 2) {
                    instance = candidateConstructor.newInstance(50, radianMode);
                } else {
                    instance = candidateConstructor.newInstance(50, radianMode, Locale.getDefault());
                }

                return (CalculatorEngine) instance;
            } catch (final ReflectiveOperationException ignored) {
                // Fallback to other constructors.
            }
        }

        return new CalculatorEngine();
    }

    /**
     * Attempts to configure the given calculator instance so that trigonometric functions interpret angles as
     * radians.
     *
     * @param calculatorEngine calculator engine instance to configure
     * @return {@code true} if a matching configuration method was found and invoked successfully
     */
    private static boolean tryConfigureCalculatorEngineForRadians(@NonNull final CalculatorEngine calculatorEngine) {
        final Object radianMode = tryResolveRadianModeEnumValue();
        if (radianMode == null) {
            return false;
        }

        final Class<?> modeClass = radianMode.getClass();

        for (final Method method : CalculatorEngine.class.getMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (method.getReturnType() != void.class) {
                continue;
            }
            if (method.getParameterCount() != 1 || !method.getParameterTypes()[0].equals(modeClass)) {
                continue;
            }

            final String lowerCaseName = method.getName().toLowerCase(Locale.ROOT);
            if (!lowerCaseName.contains("trig") && !lowerCaseName.contains("angle") && !lowerCaseName.contains("mode")) {
                continue;
            }

            try {
                method.invoke(calculatorEngine, radianMode);
                return true;
            } catch (final ReflectiveOperationException ignored) {
                // Continue searching.
            }
        }

        return false;
    }

    /**
     * Resolves an enum constant that represents "radians" in the calculator's trigonometric-mode enum.
     * <p>
     * This method supports multiple historical enum package locations while avoiding a hard dependency.
     * </p>
     *
     * @return the enum constant instance, or {@code null} if no known enum could be resolved
     */
    private static Object tryResolveRadianModeEnumValue() {
        final Class<?> modeEnumClass = tryLoadFirstAvailableClass(
                "com.mlprograms.justmath.calculator.TrigonometricMode",
                "com.mlprograms.justmath.calculator.internal.TrigonometricMode",
                "com.mlprograms.justmath.calculator.enums.TrigonometricMode"
        );
        if (modeEnumClass == null || !modeEnumClass.isEnum()) {
            return null;
        }

        final Object rad = tryGetEnumConstant(modeEnumClass, "RAD");
        if (rad != null) {
            return rad;
        }
        final Object radians = tryGetEnumConstant(modeEnumClass, "RADIANS");
        if (radians != null) {
            return radians;
        }

        return tryGetEnumConstant(modeEnumClass, "RADIAN");
    }

    private static Class<?> tryLoadFirstAvailableClass(@NonNull final String... classNames) {
        for (final String className : classNames) {
            try {
                return Class.forName(className);
            } catch (final ClassNotFoundException ignored) {
                // Try next.
            }
        }
        return null;
    }

    private static Object tryGetEnumConstant(@NonNull final Class<?> enumClass, @NonNull final String constantName) {
        for (final Object enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant instanceof Enum<?> typed && typed.name().equals(constantName)) {
                return enumConstant;
            }
        }
        return null;
    }

    private static Constructor<?> tryGetPublicConstructor(@NonNull final Class<?> type, @NonNull final Class<?>... parameterTypes) {
        try {
            return type.getConstructor(parameterTypes);
        } catch (final NoSuchMethodException ignored) {
            return null;
        }
    }

    /**
     * Creates a new plot engine using the provided {@link CalculatorEngine}.
     *
     * @param calculatorEngine non-null calculator engine
     * @throws NullPointerException if {@code calculatorEngine} is {@code null}
     */
    public GraphFxCalculatorEngine(@NonNull final CalculatorEngine calculatorEngine) {
        this.calculatorEngine = calculatorEngine;
    }

    /**
     * Computes plot geometry for the given request.
     *
     * @param request non-null plot request
     * @param cancellation non-null cancellation hook
     * @return geometry result (never {@code null})
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if request constraints are violated
     */
    @Override
    public GraphFxPlotGeometry plot(@NonNull final GraphFxPlotRequest request, @NonNull final GraphFxPlotCancellation cancellation) {
        final String normalizedExpression = normalizeExpression(request.expression());
        final Map<String, String> safeVariables = request.variables();
        final GraphFxWorldBounds bounds = request.bounds();

        if (!containsYVariable(normalizedExpression)) {
            final int samples = clampInt(request.pixelWidth(), EXPLICIT_SAMPLES_MIN, EXPLICIT_SAMPLES_MAX);
            final List<GraphFxPoint> polyline = createExplicitPolyline(normalizedExpression, safeVariables, bounds, samples, cancellation);
            return cancellation.isCancelled() ? GraphFxPlotGeometry.empty() : new GraphFxPlotGeometry(polyline, List.of());
        }

        final GraphFxLineSegment linearSegment = tryCreateLinearImplicitSegment(normalizedExpression, safeVariables, bounds, cancellation);
        if (linearSegment != null) {
            return cancellation.isCancelled() ? GraphFxPlotGeometry.empty() : new GraphFxPlotGeometry(List.of(), List.of(linearSegment));
        }

        final int nx = clampInt(Math.max(8, request.pixelWidth() / IMPLICIT_CELL_SIZE_PX), IMPLICIT_NX_MIN, IMPLICIT_NX_MAX);
        final int ny = clampInt(Math.max(8, request.pixelHeight() / IMPLICIT_CELL_SIZE_PX), IMPLICIT_NY_MIN, IMPLICIT_NY_MAX);

        final List<GraphFxLineSegment> segments = createImplicitZeroContourSegments(normalizedExpression, safeVariables, bounds, nx, ny, cancellation);
        return cancellation.isCancelled() ? GraphFxPlotGeometry.empty() : new GraphFxPlotGeometry(List.of(), segments);
    }

    /**
     * Checks whether the expression contains a standalone {@code y} token.
     *
     * @param expression expression to scan
     * @return {@code true} if {@code y}/{@code Y} is present as standalone token; {@code false} otherwise
     * @throws NullPointerException if {@code expression} is {@code null}
     */
    private boolean containsYVariable(@NonNull final String expression) {
        return containsStandaloneIdentifier(expression, 'y', 'Y');
    }

    /**
     * Creates an explicit polyline for {@code y=f(x)}.
     *
     * <p>Invalid evaluations are skipped and cause NaN path breaks.</p>
     *
     * @param expression normalized expression
     * @param variables variables (must not be {@code null})
     * @param bounds viewport bounds
     * @param samples number of samples along x
     * @param cancellation cancellation hook
     * @return polyline list (may contain NaN breaks)
     */
    private List<GraphFxPoint> createExplicitPolyline(@NonNull final String expression,
                                                      @NonNull final Map<String, String> variables,
                                                      @NonNull final GraphFxWorldBounds bounds,
                                                      final int samples,
                                                      @NonNull final GraphFxPlotCancellation cancellation) {
        final GraphFxWorldBounds normalized = bounds.normalized();

        final double minX = normalized.minX();
        final double maxX = normalized.maxX();
        final double minY = normalized.minY();
        final double maxY = normalized.maxY();

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

            final BigNumber yBig = evaluateSafe(expression, evalVariables);
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
     * Creates line segments for the implicit curve {@code F(x,y)=0} using marching squares.
     *
     * @param expression normalized expression
     * @param variables variables (must not be {@code null})
     * @param bounds viewport bounds
     * @param nx number of cells along x
     * @param ny number of cells along y
     * @param cancellation cancellation hook
     * @return list of line segments
     */
    private List<GraphFxLineSegment> createImplicitZeroContourSegments(@NonNull final String expression,
                                                                       @NonNull final Map<String, String> variables,
                                                                       @NonNull final GraphFxWorldBounds bounds,
                                                                       final int nx,
                                                                       final int ny,
                                                                       @NonNull final GraphFxPlotCancellation cancellation) {
        final GraphFxWorldBounds normalized = bounds.normalized();

        final double minX = normalized.minX();
        final double maxX = normalized.maxX();
        final double minY = normalized.minY();
        final double maxY = normalized.maxY();

        final int cellsX = Math.max(1, nx);
        final int cellsY = Math.max(1, ny);

        final double dx = (maxX - minX) / cellsX;
        final double dy = (maxY - minY) / cellsY;

        final double[][] field = new double[cellsY + 1][cellsX + 1];
        final Map<String, String> evalVars = new HashMap<>(variables);

        for (int iy = 0; iy <= cellsY; iy++) {
            if (cancellation.isCancelled()) {
                return List.of();
            }

            final double y = minY + iy * dy;
            evalVars.put("y", Double.toString(y));

            for (int ix = 0; ix <= cellsX; ix++) {
                final double x = minX + ix * dx;
                evalVars.put("x", Double.toString(x));

                final BigNumber value = evaluateSafe(expression, evalVars);
                field[iy][ix] = value == null ? Double.NaN : safeToDouble(value);
            }
        }

        final List<GraphFxLineSegment> segments = new ArrayList<>(cellsX * cellsY);

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
     * Attempts to detect an affine-linear implicit equation and returns a viewport-clipped segment.
     *
     * @param expression normalized expression
     * @param variables variables map
     * @param bounds viewport bounds
     * @param cancellation cancellation hook
     * @return segment if affine-linear; otherwise {@code null}
     */
    private GraphFxLineSegment tryCreateLinearImplicitSegment(@NonNull final String expression,
                                                              @NonNull final Map<String, String> variables,
                                                              @NonNull final GraphFxWorldBounds bounds,
                                                              @NonNull final GraphFxPlotCancellation cancellation) {
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

        final GraphFxWorldBounds normalized = bounds.normalized();

        final BigNumber bnMinX = new BigNumber(normalized.minX());
        final BigNumber bnMaxX = new BigNumber(normalized.maxX());
        final BigNumber bnMinY = new BigNumber(normalized.minY());
        final BigNumber bnMaxY = new BigNumber(normalized.maxY());

        final List<GraphFxPoint> intersections = new ArrayList<>(4);

        if (!isEffectivelyZero(b)) {
            addIntersectionIfVisible(intersections, bnMinX, solveY(a, b, c, bnMinX), normalized);
            addIntersectionIfVisible(intersections, bnMaxX, solveY(a, b, c, bnMaxX), normalized);
        }

        if (!isEffectivelyZero(a)) {
            addIntersectionIfVisible(intersections, solveX(a, b, c, bnMinY), bnMinY, normalized);
            addIntersectionIfVisible(intersections, solveX(a, b, c, bnMaxY), bnMaxY, normalized);
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

        return new GraphFxLineSegment(p0, bestP1);
    }

    /**
     * Verifies affine behavior at a small set of reference points.
     *
     * @param expression normalized expression
     * @param evalVars mutable evaluation variables
     * @param a coefficient for x
     * @param b coefficient for y
     * @param c constant term
     * @param cancellation cancellation hook
     * @return {@code true} if affine verified
     */
    private boolean isAffineVerified(@NonNull final String expression,
                                     @NonNull final Map<String, String> evalVars,
                                     @NonNull final BigNumber a,
                                     @NonNull final BigNumber b,
                                     @NonNull final BigNumber c,
                                     @NonNull final GraphFxPlotCancellation cancellation) {
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
     * Evaluates the expression and returns {@code null} on failure.
     *
     * @param expression expression
     * @param variables variables (mutated by caller)
     * @return value or {@code null}
     */
    private BigNumber evaluateSafe(@NonNull final String expression, @NonNull final Map<String, String> variables) {
        try {
            return calculatorEngine.evaluate(expression, variables);
        } catch (final RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Evaluates at BigNumber x/y by setting variables.
     *
     * @param expression expression
     * @param variables mutable variables map
     * @param x x value
     * @param y y value
     * @return evaluated value or {@code null}
     */
    private BigNumber evaluateAt(@NonNull final String expression,
                                 @NonNull final Map<String, String> variables,
                                 @NonNull final BigNumber x,
                                 @NonNull final BigNumber y) {
        variables.put("x", x.toString());
        variables.put("y", y.toString());
        return evaluateSafe(expression, variables);
    }

    /**
     * Computes marching squares case code for a cell.
     *
     * @param f00 bottom-left field
     * @param f10 bottom-right field
     * @param f11 top-right field
     * @param f01 top-left field
     * @return case code [0..15]
     */
    private static int contourCode(final double f00, final double f10, final double f11, final double f01) {
        final int c00 = signBit(f00);
        final int c10 = signBit(f10);
        final int c11 = signBit(f11);
        final int c01 = signBit(f01);
        return (c00) | (c10 << 1) | (c11 << 2) | (c01 << 3);
    }

    /**
     * Computes sign bit for marching squares.
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
     * Interpolates an approximate zero crossing point along an edge.
     *
     * @param x0 first x
     * @param y0 first y
     * @param f0 first field
     * @param x1 second x
     * @param y1 second y
     * @param f1 second field
     * @return interpolated point
     */
    private static GraphFxPoint interpolateZeroCrossing(final double x0,
                                                        final double y0,
                                                        final double f0,
                                                        final double x1,
                                                        final double y1,
                                                        final double f1) {
        final double denom = (f0 - f1);
        final double t = Math.abs(denom) <= CONTOUR_EPSILON ? 0.5 : (f0 / denom);
        final double clamped = Math.max(0.0, Math.min(1.0, t));
        return new GraphFxPoint(x0 + (x1 - x0) * clamped, y0 + (y1 - y0) * clamped);
    }

    /**
     * Appends segments for a marching squares case.
     *
     * @param out output list
     * @param code case code
     * @param f00 f00
     * @param f10 f10
     * @param f11 f11
     * @param f01 f01
     * @param bottom bottom edge point
     * @param right right edge point
     * @param top top edge point
     * @param left left edge point
     */
    private static void appendSegmentsForCase(@NonNull final List<GraphFxLineSegment> out,
                                              final int code,
                                              final double f00,
                                              final double f10,
                                              final double f11,
                                              final double f01,
                                              final GraphFxPoint bottom,
                                              final GraphFxPoint right,
                                              final GraphFxPoint top,
                                              final GraphFxPoint left) {
        switch (code) {
            case 1, 14 -> out.add(new GraphFxLineSegment(left, bottom));
            case 2, 13 -> out.add(new GraphFxLineSegment(bottom, right));
            case 3, 12 -> out.add(new GraphFxLineSegment(left, right));
            case 4, 11 -> out.add(new GraphFxLineSegment(right, top));
            case 6, 9 -> out.add(new GraphFxLineSegment(bottom, top));
            case 7, 8 -> out.add(new GraphFxLineSegment(left, top));
            case 5, 10 -> {
                final double center = (f00 + f10 + f11 + f01) / 4.0;
                final boolean centerNegative = center < 0;

                if (code == 5) {
                    if (centerNegative) {
                        out.add(new GraphFxLineSegment(left, bottom));
                        out.add(new GraphFxLineSegment(right, top));
                    } else {
                        out.add(new GraphFxLineSegment(bottom, right));
                        out.add(new GraphFxLineSegment(left, top));
                    }
                } else {
                    if (centerNegative) {
                        out.add(new GraphFxLineSegment(bottom, right));
                        out.add(new GraphFxLineSegment(left, top));
                    } else {
                        out.add(new GraphFxLineSegment(left, bottom));
                        out.add(new GraphFxLineSegment(right, top));
                    }
                }
            }
            default -> {
            }
        }
    }

    /**
     * Approximate equality check using {@link #LINEAR_TOLERANCE}.
     *
     * @param a value a
     * @param b value b
     * @return {@code true} if |a-b| <= tolerance
     */
    private static boolean approxEquals(@NonNull final BigNumber a, @NonNull final BigNumber b) {
        return a.subtract(b).abs().compareTo(LINEAR_TOLERANCE) <= 0;
    }

    /**
     * Tests whether a value is effectively zero.
     *
     * @param value value to test
     * @return {@code true} if |value| <= tolerance
     */
    private static boolean isEffectivelyZero(@NonNull final BigNumber value) {
        return value.abs().compareTo(LINEAR_TOLERANCE) <= 0;
    }

    /**
     * Solves a*x + b*y + c = 0 for y given x.
     *
     * @param a coefficient a
     * @param b coefficient b
     * @param c constant term
     * @param x x value
     * @return y value
     */
    private static BigNumber solveY(@NonNull final BigNumber a,
                                    @NonNull final BigNumber b,
                                    @NonNull final BigNumber c,
                                    @NonNull final BigNumber x) {
        return a.multiply(x).add(c).multiply(BigNumbers.NEGATIVE_ONE).divide(b);
    }

    /**
     * Solves a*x + b*y + c = 0 for x given y.
     *
     * @param a coefficient a
     * @param b coefficient b
     * @param c constant term
     * @param y y value
     * @return x value
     */
    private static BigNumber solveX(@NonNull final BigNumber a,
                                    @NonNull final BigNumber b,
                                    @NonNull final BigNumber c,
                                    @NonNull final BigNumber y) {
        return b.multiply(y).add(c).multiply(BigNumbers.NEGATIVE_ONE).divide(a);
    }

    /**
     * Adds an intersection point if it lies within bounds.
     *
     * @param out output list
     * @param x x coordinate
     * @param y y coordinate
     * @param bounds viewport bounds
     */
    private static void addIntersectionIfVisible(@NonNull final List<GraphFxPoint> out,
                                                 @NonNull final BigNumber x,
                                                 @NonNull final BigNumber y,
                                                 @NonNull final GraphFxWorldBounds bounds) {
        final double xd = safeBigNumberToDouble(x);
        final double yd = safeBigNumberToDouble(y);

        if (!Double.isFinite(xd) || !Double.isFinite(yd)) {
            return;
        }

        final double margin = 1e-9;
        if (xd < bounds.minX() - margin || xd > bounds.maxX() + margin) {
            return;
        }
        if (yd < bounds.minY() - margin || yd > bounds.maxY() + margin) {
            return;
        }

        out.add(new GraphFxPoint(xd, yd));
    }

    /**
     * Computes squared distance between two points.
     *
     * @param a point a
     * @param b point b
     * @return squared distance
     */
    private static double distanceSq(@NonNull final GraphFxPoint a, @NonNull final GraphFxPoint b) {
        final double dx = a.x() - b.x();
        final double dy = a.y() - b.y();
        return dx * dx + dy * dy;
    }

    /**
     * Converts BigNumber to double safely.
     *
     * @param value big number
     * @return parsed double or NaN
     */
    private double safeToDouble(@NonNull final BigNumber value) {
        return safeBigNumberToDouble(value);
    }

    /**
     * Parses a {@link BigNumber} using {@link BigNumber#toString()}.
     *
     * @param value BigNumber
     * @return parsed double or NaN
     */
    private static double safeBigNumberToDouble(@NonNull final BigNumber value) {
        try {
            return Double.parseDouble(value.toString());
        } catch (final RuntimeException ignored) {
            return Double.NaN;
        }
    }

    /**
     * Clamps an integer value.
     *
     * @param value value
     * @param min min
     * @param max max
     * @return clamped
     */
    private static int clampInt(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Normalizes the input expression for user ergonomics.
     *
     * <p>Applies whitespace removal, decimal comma conversion, and conservative implicit multiplication insertion.</p>
     *
     * @param expression raw expression
     * @return normalized expression
     * @throws NullPointerException if {@code expression} is {@code null}
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

            if (ch == ',' && i > 0 && i + 1 < trimmed.length()) {
                final char prev = lastNonWhitespace(trimmed, i - 1);
                final char next = nextNonWhitespace(trimmed, i + 1);
                if (Character.isDigit(prev) && Character.isDigit(next)) {
                    maybeInsertImplicitMultiply(out, prevOut, '.');
                    out.append('.');
                    prevOut = '.';
                    i++;
                    continue;
                }
            }

            maybeInsertImplicitMultiply(out, prevOut, ch);

            out.append(ch);
            prevOut = ch;
            i++;
        }

        return out.toString();
    }

    /**
     * Inserts implicit multiplication markers for common safe cases.
     *
     * @param out output buffer
     * @param prevOut previous output character
     * @param next next character
     */
    private static void maybeInsertImplicitMultiply(final StringBuilder out, final char prevOut, final char next) {
        if (out.isEmpty()) {
            return;
        }

        final boolean prevIsDigitish = Character.isDigit(prevOut) || prevOut == '.';
        final boolean nextStartsValue = Character.isLetter(next) || next == '(';

        if (prevIsDigitish && nextStartsValue) {
            out.append('*');
            return;
        }

        if (prevOut == ')' && (Character.isLetter(next) || Character.isDigit(next) || next == '(')) {
            out.append('*');
            return;
        }

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
     * Returns the previous non-whitespace character.
     *
     * @param string string
     * @param fromIndex start index
     * @return character or {@code '\0'}
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
     * Returns the next non-whitespace character.
     *
     * @param s string
     * @param fromIndex start index
     * @return character or {@code '\0'}
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
     * Checks whether a string contains a standalone single-character identifier.
     *
     * @param s string
     * @param lower lowercase id
     * @param upper uppercase id
     * @return {@code true} if present as standalone token
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
     * Determines whether a character is considered part of an identifier.
     *
     * @param c character
     * @return {@code true} if identifier character
     */
    private static boolean isIdentifierChar(final char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
