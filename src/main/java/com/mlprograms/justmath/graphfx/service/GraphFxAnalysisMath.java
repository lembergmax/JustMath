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

package com.mlprograms.justmath.graphfx.service;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

/**
 * Provides numerical analysis utilities (evaluation, derivative, root finding, intersections and integration)
 * for GraphFX based features.
 *
 * <p>All internal arithmetic (differences, step sizes, bisection, Simpson summation) is performed using
 * {@link BigNumber} to benefit from the project's consistent high-precision calculation model.
 * Public APIs return {@link BigDecimal} (or {@link Double} for plotting convenience) to integrate
 * easily with JavaFX and rendering code.</p>
 *
 * <p>All methods are fail-safe: if an expression cannot be evaluated in a specific point or if numeric
 * constraints are violated (e.g., division by zero), the corresponding method returns {@code null}.</p>
 */
public final class GraphFxAnalysisMath {

    private static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL128;

    private static final BigNumber DERIVATIVE_ZERO_TOLERANCE = new BigNumber("1e-12", BigNumbers.CALCULATION_LOCALE, DEFAULT_MATH_CONTEXT);
    private static final BigNumber ROOT_DISTINCT_TOLERANCE = new BigNumber("1e-9", BigNumbers.CALCULATION_LOCALE, DEFAULT_MATH_CONTEXT);
    private static final BigNumber BISECTION_INTERVAL_TOLERANCE = new BigNumber("1e-12", BigNumbers.CALCULATION_LOCALE, DEFAULT_MATH_CONTEXT);

    private static final BigNumber STEP_BASE = new BigNumber("1e-6", BigNumbers.CALCULATION_LOCALE, DEFAULT_MATH_CONTEXT);
    private static final BigNumber STEP_SCALE = new BigNumber("1e-6", BigNumbers.CALCULATION_LOCALE, DEFAULT_MATH_CONTEXT);

    private GraphFxAnalysisMath() {
    }

    /**
     * Evaluates an expression for a given x value and returns a finite {@link Double} suitable for plotting.
     *
     * <p>This method is intended for fast sampling during graph rendering. The expression is evaluated using
     * {@link CalculatorEngine} and the resulting {@link BigNumber} is converted to {@link BigDecimal} and then
     * to {@code double}. If the computed value is not finite (NaN/Infinity) or if evaluation fails, {@code null}
     * is returned.</p>
     *
     * @param engine     the calculator engine used to evaluate the expression
     * @param expression the expression to evaluate
     * @param variables  additional variables used by the expression (will be copied and not mutated)
     * @param x          the x-value to inject as variable {@code "x"}
     * @return the evaluated y-value as finite {@link Double}, or {@code null} if evaluation fails or is non-finite
     */
    public static Double evalY(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigDecimal x) {
        final BigNumber yValue = evaluateYAsBigNumber(engine, expression, variables, x);
        if (yValue == null) {
            return null;
        }

        final double yAsDouble = yValue.toBigDecimal().doubleValue();
        return Double.isFinite(yAsDouble) ? yAsDouble : null;
    }

    /**
     * Computes the numerical derivative at a given point using the symmetric difference quotient:
     * {@code f'(x) ≈ (f(x+h) - f(x-h)) / (2h)}.
     *
     * <p>The step size {@code h} is chosen relative to {@code x} to achieve a stable approximation:
     * {@code h = max(1e-6, |x| * 1e-6)}.</p>
     *
     * @param engine     the calculator engine used to evaluate the expression
     * @param expression the expression to differentiate
     * @param variables  additional variables used by the expression (will be copied and not mutated)
     * @param x          the point at which the derivative is approximated
     * @return the derivative as {@link BigDecimal}, or {@code null} if evaluation fails or division becomes invalid
     */
    public static BigDecimal derivative(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigDecimal x) {
        final BigNumber xValue = toBigNumber(x, DEFAULT_MATH_CONTEXT);
        final BigNumber stepSize = chooseStep(xValue);

        final BigNumber yAtRight = evaluateYAsBigNumber(engine, expression, variables, xValue.add(stepSize).toBigDecimal());
        final BigNumber yAtLeft = evaluateYAsBigNumber(engine, expression, variables, xValue.subtract(stepSize).toBigDecimal());

        if (yAtRight == null || yAtLeft == null) {
            return null;
        }

        final BigNumber numerator = yAtRight.subtract(yAtLeft);
        final BigNumber denominator = stepSize.multiply(BigNumbers.TWO);

        if (isApproximatelyZero(denominator, DERIVATIVE_ZERO_TOLERANCE)) {
            return null;
        }

        final BigNumber derivative = numerator.divide(denominator, DEFAULT_MATH_CONTEXT, BigNumbers.CALCULATION_LOCALE);
        return derivative.toBigDecimal();
    }

    /**
     * Searches for real roots (x-values where {@code f(x) = 0}) in the interval {@code [xMin, xMax]} by sampling
     * and applying a bisection refinement on sign changes.
     *
     * <p>The method splits the interval into {@code steps} segments (at least 2) and checks consecutive samples
     * for sign changes. If a sign change is detected, a bisection is performed to approximate the root.</p>
     *
     * @param engine     the calculator engine used to evaluate the expression
     * @param expression the expression whose roots should be found
     * @param variables  additional variables used by the expression (will be copied and not mutated)
     * @param xMin       the lower bound of the search interval (inclusive)
     * @param xMax       the upper bound of the search interval (inclusive)
     * @param steps      the number of sampling steps (minimum 2)
     * @return a sorted list of distinct roots as {@link BigDecimal}; empty if none are found; {@code null} is never returned
     */
    public static List<BigDecimal> rootsInRange(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigDecimal xMin, @NonNull final BigDecimal xMax, final int steps) {
        final int effectiveSteps = Math.max(2, steps);

        final BigNumber leftBound = toBigNumber(xMin, DEFAULT_MATH_CONTEXT);
        final BigNumber rightBound = toBigNumber(xMax, DEFAULT_MATH_CONTEXT);

        final BigNumber stepSize = rightBound.subtract(leftBound).divide(toBigNumber(BigDecimal.valueOf(effectiveSteps), DEFAULT_MATH_CONTEXT), DEFAULT_MATH_CONTEXT, BigNumbers.CALCULATION_LOCALE);

        final List<BigDecimal> roots = new ArrayList<>();

        BigNumber previousX = leftBound;
        BigNumber previousY = evaluateYAsBigNumber(engine, expression, variables, previousX.toBigDecimal());

        for (int index = 1; index <= effectiveSteps; index++) {
            final BigNumber currentX = (index == effectiveSteps) ? rightBound : leftBound.add(stepSize.multiply(toBigNumber(BigDecimal.valueOf(index), DEFAULT_MATH_CONTEXT)));

            final BigNumber currentY = evaluateYAsBigNumber(engine, expression, variables, currentX.toBigDecimal());

            if (previousY != null && currentY != null) {
                if (isApproximatelyZero(previousY, DERIVATIVE_ZERO_TOLERANCE)) {
                    addDistinctRoot(roots, previousX.toBigDecimal());
                } else if (hasOppositeSigns(previousY, currentY)) {
                    final BigNumber refinedRoot = bisection(engine, expression, variables, previousX, currentX, DEFAULT_MATH_CONTEXT);
                    if (refinedRoot != null) {
                        addDistinctRoot(roots, refinedRoot.toBigDecimal());
                    }
                }
            }

            previousX = currentX;
            previousY = currentY;
        }

        roots.sort(Comparator.naturalOrder());
        return roots;
    }

    /**
     * Computes the x-values in {@code [xMin, xMax]} where two expressions intersect, i.e. {@code f(x) = g(x)}.
     *
     * <p>This method transforms the intersection problem into a root problem by solving
     * {@code (f(x) - g(x)) = 0} and delegates to {@link #rootsInRange(CalculatorEngine, String, Map, BigDecimal, BigDecimal, int)}.</p>
     *
     * @param engine    the calculator engine used to evaluate the expressions
     * @param fExpr     the first expression f(x)
     * @param gExpr     the second expression g(x)
     * @param variables additional variables used by the expressions (will be copied and not mutated)
     * @param xMin      the lower bound of the search interval (inclusive)
     * @param xMax      the upper bound of the search interval (inclusive)
     * @param steps     the number of sampling steps (minimum 2)
     * @return a sorted list of intersection x-values as {@link BigDecimal}
     */
    public static List<BigDecimal> intersectionsInRange(@NonNull final CalculatorEngine engine, @NonNull final String fExpr, @NonNull final String gExpr, @NonNull final Map<String, String> variables, @NonNull final BigDecimal xMin, @NonNull final BigDecimal xMax, final int steps) {
        final String differenceExpression = "(" + fExpr + ")-(" + gExpr + ")";
        return rootsInRange(engine, differenceExpression, variables, xMin, xMax, steps);
    }

    /**
     * Approximates the definite integral {@code ∫[a..b] f(x) dx} using Simpson's rule.
     *
     * <p>The number of sub-intervals {@code n} is forced to be even and at least 2.
     * If any sample point cannot be evaluated, the method returns {@code null}.</p>
     *
     * @param engine     the calculator engine used to evaluate the expression
     * @param expression the integrand expression f(x)
     * @param variables  additional variables used by the expression (will be copied and not mutated)
     * @param a          lower integration bound
     * @param b          upper integration bound
     * @param n          number of sub-intervals (will be normalized to an even number ≥ 2)
     * @return the Simpson approximation as {@link BigDecimal}, or {@code null} if evaluation fails
     */
    public static BigDecimal integralSimpson(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigDecimal a, @NonNull final BigDecimal b, final int n) {
        final int normalizedIntervals = normalizeSimpsonIntervals(n);

        final BigNumber leftBound = toBigNumber(a, DEFAULT_MATH_CONTEXT);
        final BigNumber rightBound = toBigNumber(b, DEFAULT_MATH_CONTEXT);

        final BigNumber intervalWidth = rightBound.subtract(leftBound).divide(toBigNumber(BigDecimal.valueOf(normalizedIntervals), DEFAULT_MATH_CONTEXT), DEFAULT_MATH_CONTEXT, BigNumbers.CALCULATION_LOCALE);

        if (isApproximatelyZero(intervalWidth, DERIVATIVE_ZERO_TOLERANCE)) {
            return BigDecimal.ZERO;
        }

        BigNumber weightedSum = toBigNumber(BigDecimal.ZERO, DEFAULT_MATH_CONTEXT);

        for (int index = 0; index <= normalizedIntervals; index++) {
            final BigNumber xValue = leftBound.add(intervalWidth.multiply(toBigNumber(BigDecimal.valueOf(index), DEFAULT_MATH_CONTEXT)));
            final BigNumber yValue = evaluateYAsBigNumber(engine, expression, variables, xValue.toBigDecimal());

            if (yValue == null) {
                return null;
            }

            final BigNumber weight = simpsonWeight(index, normalizedIntervals);
            weightedSum = weightedSum.add(yValue.multiply(weight));
        }

        final BigNumber integral = weightedSum.multiply(intervalWidth).divide(BigNumbers.THREE, DEFAULT_MATH_CONTEXT, BigNumbers.CALCULATION_LOCALE);

        return integral.toBigDecimal();
    }

    private static BigNumber evaluateYAsBigNumber(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigDecimal x) {
        try {
            final Map<String, String> resolvedVariables = new HashMap<>(variables);
            resolvedVariables.put("x", x.stripTrailingZeros().toPlainString());
            return engine.evaluate(expression, resolvedVariables);
        } catch (final Exception ignored) {
            return null;
        }
    }

    private static BigNumber bisection(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigNumber left, @NonNull final BigNumber right, @NonNull final MathContext mathContext) {
        BigNumber lowerBound = left;
        BigNumber upperBound = right;

        BigNumber yAtLower = evaluateYAsBigNumber(engine, expression, variables, lowerBound.toBigDecimal());
        BigNumber yAtUpper = evaluateYAsBigNumber(engine, expression, variables, upperBound.toBigDecimal());

        if (yAtLower == null || yAtUpper == null) {
            return null;
        }

        if (isApproximatelyZero(yAtLower, DERIVATIVE_ZERO_TOLERANCE)) {
            return lowerBound;
        }
        if (isApproximatelyZero(yAtUpper, DERIVATIVE_ZERO_TOLERANCE)) {
            return upperBound;
        }
        if (!hasOppositeSigns(yAtLower, yAtUpper)) {
            return null;
        }

        for (int iteration = 0; iteration < 80; iteration++) {
            final BigNumber midpoint = lowerBound.add(upperBound).divide(BigNumbers.TWO, mathContext, BigNumbers.CALCULATION_LOCALE);

            final BigNumber yAtMidpoint = evaluateYAsBigNumber(engine, expression, variables, midpoint.toBigDecimal());
            if (yAtMidpoint == null) {
                return null;
            }

            if (isApproximatelyZero(yAtMidpoint, DERIVATIVE_ZERO_TOLERANCE)) {
                return midpoint;
            }

            if (hasOppositeSigns(yAtLower, yAtMidpoint)) {
                upperBound = midpoint;
                yAtUpper = yAtMidpoint;
            } else {
                lowerBound = midpoint;
                yAtLower = yAtMidpoint;
            }

            final BigNumber intervalSize = upperBound.subtract(lowerBound);
            if (isApproximatelyZero(intervalSize, BISECTION_INTERVAL_TOLERANCE)) {
                return lowerBound.add(upperBound).divide(BigNumbers.TWO, mathContext, BigNumbers.CALCULATION_LOCALE);
            }
        }

        return lowerBound.add(upperBound).divide(BigNumbers.TWO, mathContext, BigNumbers.CALCULATION_LOCALE);
    }

    private static BigNumber chooseStep(@NonNull final BigNumber xValue) {
        final BigNumber absoluteX = new BigNumber(xValue, BigNumbers.CALCULATION_LOCALE, DEFAULT_MATH_CONTEXT).abs();
        final BigNumber scaledStep = absoluteX.multiply(STEP_SCALE);

        return (scaledStep.compareTo(STEP_BASE) > 0) ? scaledStep : STEP_BASE;
    }

    private static boolean isApproximatelyZero(@NonNull final BigNumber value, @NonNull final BigNumber tolerance) {
        final BigDecimal absoluteValue = value.toBigDecimal().abs();
        return absoluteValue.compareTo(tolerance.toBigDecimal()) < 0;
    }

    private static boolean hasOppositeSigns(@NonNull final BigNumber leftValue, @NonNull final BigNumber rightValue) {
        final int leftSignum = leftValue.toBigDecimal().signum();
        final int rightSignum = rightValue.toBigDecimal().signum();
        return leftSignum != 0 && rightSignum != 0 && (leftSignum != rightSignum);
    }

    private static boolean addDistinctRoot(@NonNull final List<BigDecimal> roots, @NonNull final BigDecimal candidate) {
        for (final BigDecimal existingRoot : roots) {
            if (existingRoot.subtract(candidate).abs().compareTo(ROOT_DISTINCT_TOLERANCE.toBigDecimal()) < 0) {
                return false;
            }
        }
        roots.add(candidate);
        return true;
    }

    private static int normalizeSimpsonIntervals(final int n) {
        int normalized = Math.max(2, n);
        if (normalized % 2 != 0) {
            normalized++;
        }
        return normalized;
    }

    private static BigNumber simpsonWeight(final int index, final int maxIndex) {
        if (index == 0 || index == maxIndex) {
            return BigNumbers.ONE;
        }
        return (index % 2 == 0) ? BigNumbers.TWO : BigNumbers.FOUR;
    }

    private static BigNumber toBigNumber(@NonNull final BigDecimal value, @NonNull final MathContext mathContext) {
        return new BigNumber(value.toPlainString(), BigNumbers.CALCULATION_LOCALE, mathContext);
    }

}
