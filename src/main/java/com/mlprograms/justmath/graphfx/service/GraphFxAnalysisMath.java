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
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides numerical analysis utilities (evaluation, derivative, root finding, intersections and integration)
 * for GraphFX based features.
 *
 * <p>All internal arithmetic (differences, step sizes, bisection, Simpson summation) is performed using
 * {@link BigNumber} to benefit from the project's consistent high-precision calculation model.
 * Public APIs return {@link BigDecimal} (or {@link Double} for plotting convenience) to integrate
 * easily with JavaFX and rendering code.</p>
 *
 * <p>All methods are fail-safe: if an expression cannot be evaluated at a given point or if numeric
 * constraints are violated (e.g., division by zero), the corresponding method returns {@code null}.</p>
 */
@NoArgsConstructor
public final class GraphFxAnalysisMath {

    private final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL128;
    private final BigNumber DERIVATIVE_ZERO_TOLERANCE = BigNumbers.ONE.power(new BigNumber("-12"), DEFAULT_MATH_CONTEXT);
    private final BigNumber ROOT_DISTINCT_TOLERANCE = BigNumbers.ONE.power(new BigNumber("-9"), DEFAULT_MATH_CONTEXT);
    private final BigNumber BISECTION_INTERVAL_TOLERANCE = BigNumbers.ONE.power(new BigNumber("-12"), DEFAULT_MATH_CONTEXT);
    private final BigNumber STEP_BASE = BigNumbers.ONE.power(new BigNumber("-6"), DEFAULT_MATH_CONTEXT);
    private final BigNumber STEP_SCALE = BigNumbers.ONE.power(new BigNumber("-6"), DEFAULT_MATH_CONTEXT);

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
    public Double evalY(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigDecimal x) {
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
     * <p>The step size {@code h} is chosen relative to {@code x} to reduce cancellation effects:
     * {@code h = max(1e-6, |x| * 1e-6)} (performed in {@link BigNumber} arithmetic).</p>
     *
     * <p>If either sample value cannot be evaluated or if the denominator becomes too close to zero,
     * this method returns {@code null}.</p>
     *
     * @param engine     the calculator engine used to evaluate the expression
     * @param expression the expression to differentiate
     * @param variables  additional variables used by the expression (will be copied and not mutated)
     * @param x          the point at which the derivative is approximated
     * @return the derivative as {@link BigDecimal}, or {@code null} if evaluation fails or division becomes invalid
     */
    public BigNumber derivative(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigDecimal x) {
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

        return numerator.divide(denominator, DEFAULT_MATH_CONTEXT, BigNumbers.CALCULATION_LOCALE);
    }

    /**
     * Searches for real roots (x-values where {@code f(x) = 0}) in the interval {@code [xMin, xMax]} by sampling
     * and refining sign changes with bisection.
     *
     * <p>The method splits the interval into {@code steps} sub-intervals (at least 2). For each consecutive sample
     * pair {@code (x[i-1], x[i])}, it checks:
     * <ul>
     *     <li>if {@code f(x[i-1])} is approximately zero → a root at {@code x[i-1]}</li>
     *     <li>if {@code f(x[i-1])} and {@code f(x[i])} have opposite signs → a root in between, refined by bisection</li>
     * </ul>
     * Candidate roots are merged if they are closer than {@link #ROOT_DISTINCT_TOLERANCE} to prevent duplicates.</p>
     *
     * <p>This method never returns {@code null}. If no roots can be determined, an empty list is returned.</p>
     *
     * @param engine     the calculator engine used to evaluate the expression
     * @param expression the expression whose roots should be found
     * @param variables  additional variables used by the expression (will be copied and not mutated)
     * @param xMin       the lower bound of the search interval (inclusive)
     * @param xMax       the upper bound of the search interval (inclusive)
     * @param steps      the number of sampling steps (minimum 2; values &lt; 2 are coerced to 2)
     * @return a sorted list of distinct roots as {@link BigDecimal}; empty if none are found
     */
    public List<BigDecimal> rootsInRange(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigDecimal xMin, @NonNull final BigDecimal xMax, final int steps) {
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
     * Computes intersection x-values within {@code [xMin, xMax]} for two expressions {@code f(x)} and {@code g(x)}.
     *
     * <p>This method transforms the intersection problem into a root finding problem by solving
     * {@code (f(x) - g(x)) = 0} and delegates to {@link #rootsInRange(CalculatorEngine, String, Map, BigDecimal, BigDecimal, int)}.</p>
     *
     * @param engine    the calculator engine used to evaluate the expressions
     * @param fExpr     the first expression f(x)
     * @param gExpr     the second expression g(x)
     * @param variables additional variables used by the expressions (will be copied and not mutated)
     * @param xMin      the lower bound of the search interval (inclusive)
     * @param xMax      the upper bound of the search interval (inclusive)
     * @param steps     the number of sampling steps (minimum 2; values &lt; 2 are coerced to 2)
     * @return a sorted list of intersection x-values as {@link BigDecimal}; empty if none are found
     */
    public List<BigDecimal> intersectionsInRange(@NonNull final CalculatorEngine engine, @NonNull final String fExpr, @NonNull final String gExpr, @NonNull final Map<String, String> variables, @NonNull final BigDecimal xMin, @NonNull final BigDecimal xMax, final int steps) {
        final String differenceExpression = "(" + fExpr + ")-(" + gExpr + ")";
        return rootsInRange(engine, differenceExpression, variables, xMin, xMax, steps);
    }

    /**
     * Evaluates {@code expression} at the given x-value using the provided calculator engine.
     *
     * <p>This helper creates a defensive copy of {@code variables}, injects {@code "x"} with a plain string
     * representation of the input, and delegates to {@link CalculatorEngine#evaluate(String, Map)}.</p>
     *
     * <p>If the engine throws an exception (syntax error, domain error, etc.), {@code null} is returned to keep
     * callers fail-safe.</p>
     *
     * @param engine     calculator engine used for evaluation
     * @param expression expression to evaluate
     * @param variables  variable map (copied; never mutated)
     * @param x          x-value for variable {@code "x"}
     * @return evaluated y-value as {@link BigNumber}, or {@code null} if evaluation fails
     */
    private BigNumber evaluateYAsBigNumber(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigDecimal x) {
        try {
            final Map<String, String> resolvedVariables = new HashMap<>(variables);
            resolvedVariables.put("x", x.stripTrailingZeros().toPlainString());
            return engine.evaluate(expression, resolvedVariables);
        } catch (final Exception ignored) {
            return null;
        }
    }

    /**
     * Refines a single root within an interval using the bisection method.
     *
     * <p>Preconditions for bisection:
     * <ul>
     *     <li>{@code f(left)} and {@code f(right)} must be evaluable</li>
     *     <li>The values must have opposite signs, or one endpoint must already be approximately zero</li>
     * </ul>
     * If these constraints are not met, {@code null} is returned.</p>
     *
     * <p>The loop runs a fixed maximum number of iterations and also stops early if the interval length
     * drops below {@link #BISECTION_INTERVAL_TOLERANCE}.</p>
     *
     * @param engine      calculator engine used for evaluation
     * @param expression  expression to evaluate
     * @param variables   variable map (copied by the evaluation helper)
     * @param left        left interval endpoint
     * @param right       right interval endpoint
     * @param mathContext math context used for divisions during refinement
     * @return refined root as {@link BigNumber}, or {@code null} if bisection cannot be applied
     */
    private BigNumber bisection(@NonNull final CalculatorEngine engine, @NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final BigNumber left, @NonNull final BigNumber right, @NonNull final MathContext mathContext) {
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

    /**
     * Chooses a numeric step size for finite differences based on the magnitude of {@code x}.
     *
     * <p>The step is computed as {@code max(STEP_BASE, |x| * STEP_SCALE)}. This keeps the step sufficiently
     * large near zero (to avoid catastrophic cancellation) while scaling appropriately for larger magnitudes.</p>
     *
     * @param xValue x-value as {@link BigNumber}
     * @return step size as {@link BigNumber}
     */
    private BigNumber chooseStep(@NonNull final BigNumber xValue) {
        final BigNumber absoluteX = new BigNumber(xValue, BigNumbers.CALCULATION_LOCALE, DEFAULT_MATH_CONTEXT).abs();
        final BigNumber scaledStep = absoluteX.multiply(STEP_SCALE);
        return (scaledStep.compareTo(STEP_BASE) > 0) ? scaledStep : STEP_BASE;
    }

    /**
     * Checks whether a value is approximately zero according to a given tolerance.
     *
     * @param value     the value to check
     * @param tolerance absolute tolerance threshold
     * @return {@code true} if {@code |value| < tolerance}, otherwise {@code false}
     */
    private boolean isApproximatelyZero(@NonNull final BigNumber value, @NonNull final BigNumber tolerance) {
        final BigNumber absoluteValue = value.abs();
        return absoluteValue.compareTo(tolerance) < 0;
    }

    /**
     * Checks whether two values have opposite signs (both non-zero).
     *
     * <p>This method uses {@link BigDecimal#signum()} on the underlying {@link BigDecimal} representation.
     * If either value is zero, the method returns {@code false}.</p>
     *
     * @param leftValue  left value
     * @param rightValue right value
     * @return {@code true} if signs differ and neither is zero
     */
    private boolean hasOppositeSigns(@NonNull final BigNumber leftValue, @NonNull final BigNumber rightValue) {
        final int leftSignum = leftValue.toBigDecimal().signum();
        final int rightSignum = rightValue.toBigDecimal().signum();
        return leftSignum != 0 && rightSignum != 0 && (leftSignum != rightSignum);
    }

    /**
     * Adds a root candidate to the list if it is not too close to an existing root.
     *
     * <p>Two roots are considered identical if their absolute distance is smaller than
     * {@link #ROOT_DISTINCT_TOLERANCE}.</p>
     *
     * @param roots     current list of roots (modified in-place)
     * @param candidate candidate root to add
     * @return {@code true} if the candidate was added, {@code false} if it was considered a duplicate
     */
    private boolean addDistinctRoot(@NonNull final List<BigDecimal> roots, @NonNull final BigDecimal candidate) {
        for (final BigDecimal existingRoot : roots) {
            if (existingRoot.subtract(candidate).abs().compareTo(ROOT_DISTINCT_TOLERANCE.toBigDecimal()) < 0) {
                return false;
            }
        }
        roots.add(candidate);
        return true;
    }

    /**
     * Normalizes a requested number of Simpson sub-intervals.
     *
     * <p>Simpson's rule requires an even number of intervals. This method enforces:
     * <ul>
     *     <li>minimum of 2</li>
     *     <li>even parity (odd values are incremented)</li>
     * </ul>
     *
     * @param n requested interval count
     * @return normalized even interval count (≥ 2)
     */
    private int normalizeSimpsonIntervals(final int n) {
        int normalized = Math.max(2, n);
        if (normalized % 2 != 0) {
            normalized++;
        }
        return normalized;
    }

    /**
     * Returns the Simpson weight for a sample index in {@code [0..maxIndex]}.
     *
     * <p>Weights:
     * <ul>
     *     <li>index 0 and maxIndex: 1</li>
     *     <li>odd indices: 4</li>
     *     <li>even indices (excluding endpoints): 2</li>
     * </ul>
     *
     * @param index    sample index
     * @param maxIndex last index (equals the number of intervals)
     * @return Simpson weight as {@link BigNumber}
     */
    private BigNumber simpsonWeight(final int index, final int maxIndex) {
        if (index == 0 || index == maxIndex) {
            return BigNumbers.ONE;
        }
        return (index % 2 == 0) ? BigNumbers.TWO : BigNumbers.FOUR;
    }

    /**
     * Converts a {@link BigDecimal} to a {@link BigNumber} using the given math context and the global calculation locale.
     *
     * <p>The conversion uses {@link BigDecimal#toPlainString()} to avoid scientific notation and maintain a stable
     * textual representation for {@link BigNumber} parsing.</p>
     *
     * @param value       decimal value to convert
     * @param mathContext math context to associate with the created {@link BigNumber}
     * @return created {@link BigNumber}
     */
    private BigNumber toBigNumber(@NonNull final BigDecimal value, @NonNull final MathContext mathContext) {
        return new BigNumber(value.toPlainString(), BigNumbers.CALCULATION_LOCALE, mathContext);
    }

}
