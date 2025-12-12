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

package com.mlprograms.justmath.graph;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

/**
 * Numerical analysis helpers (derivative, roots, intersections, integrals).
 */
public final class GraphAnalysisMath {

    private GraphAnalysisMath() {
    }

    /**
     * Evaluates f(x) as double for rendering/analysis, using CalculatorEngine and variables.
     *
     * @param engine     calculator engine
     * @param expression function expression
     * @param variables  variables map
     * @param x          x value
     * @return y as Double or null if not finite/evaluable
     */
    public static Double evalY(
            @NonNull final CalculatorEngine engine,
            @NonNull final String expression,
            @NonNull final Map<String, String> variables,
            @NonNull final BigDecimal x
    ) {
        try {
            final Map<String, String> vars = new HashMap<>(variables);
            vars.put("x", x.stripTrailingZeros().toPlainString());
            final BigNumber y = engine.evaluate(expression, vars);
            final double yd = y.toBigDecimal().doubleValue();
            if (!Double.isFinite(yd)) {
                return null;
            }
            return yd;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Approximates derivative f'(x) with central differences.
     *
     * @param engine     calculator engine
     * @param expression expression
     * @param variables  variables map
     * @param x          x
     * @return slope as BigDecimal or null
     */
    public static BigDecimal derivative(
            @NonNull final CalculatorEngine engine,
            @NonNull final String expression,
            @NonNull final Map<String, String> variables,
            @NonNull final BigDecimal x
    ) {
        final MathContext mc = MathContext.DECIMAL128;
        final BigDecimal h = chooseStep(x);
        final Double y1 = evalY(engine, expression, variables, x.add(h, mc));
        final Double y0 = evalY(engine, expression, variables, x.subtract(h, mc));
        if (y1 == null || y0 == null) {
            return null;
        }
        final BigDecimal num = BigDecimal.valueOf(y1).subtract(BigDecimal.valueOf(y0), mc);
        final BigDecimal den = h.multiply(new BigDecimal("2"), mc);
        if (den.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return num.divide(den, mc);
    }

    /**
     * Finds a root near x0 by scanning for a sign change and using bisection.
     *
     * @param engine     calculator engine
     * @param expression expression
     * @param variables  variables map
     * @param xMin       scan min
     * @param xMax       scan max
     * @param steps      scan steps
     * @return roots (distinct) as BigDecimal
     */
    @NonNull
    public static List<BigDecimal> rootsInRange(
            @NonNull final CalculatorEngine engine,
            @NonNull final String expression,
            @NonNull final Map<String, String> variables,
            @NonNull final BigDecimal xMin,
            @NonNull final BigDecimal xMax,
            final int steps
    ) {
        final MathContext mc = MathContext.DECIMAL128;
        final BigDecimal step = xMax.subtract(xMin, mc).divide(BigDecimal.valueOf(Math.max(2, steps)), mc);

        final List<BigDecimal> roots = new ArrayList<>();
        BigDecimal prevX = xMin;
        Double prevY = evalY(engine, expression, variables, prevX);

        for (int i = 1; i <= steps; i++) {
            final BigDecimal x = (i == steps) ? xMax : xMin.add(step.multiply(BigDecimal.valueOf(i), mc), mc);
            final Double y = evalY(engine, expression, variables, x);

            if (prevY != null && y != null) {
                if (isZero(prevY) && addDistinct(roots, prevX)) {
                    prevX = x;
                    prevY = y;
                    continue;
                }
                if (prevY * y < 0) {
                    final BigDecimal r = bisection(engine, expression, variables, prevX, x, mc);
                    if (r != null) {
                        addDistinct(roots, r);
                    }
                }
            }

            prevX = x;
            prevY = y;
        }

        roots.sort(Comparator.naturalOrder());
        return roots;
    }

    /**
     * Finds intersections of f and g by solving f(x)-g(x)=0 in range.
     *
     * @param engine    engine
     * @param fExpr     f
     * @param gExpr     g
     * @param variables variables
     * @param xMin      min
     * @param xMax      max
     * @param steps     scan steps
     * @return x values of intersections
     */
    @NonNull
    public static List<BigDecimal> intersectionsInRange(
            @NonNull final CalculatorEngine engine,
            @NonNull final String fExpr,
            @NonNull final String gExpr,
            @NonNull final Map<String, String> variables,
            @NonNull final BigDecimal xMin,
            @NonNull final BigDecimal xMax,
            final int steps
    ) {
        final String diff = "(" + fExpr + ")-(" + gExpr + ")";
        return rootsInRange(engine, diff, variables, xMin, xMax, steps);
    }

    /**
     * Computes integral \int_a^b f(x) dx using Simpson's rule.
     *
     * @param engine     engine
     * @param expression expression
     * @param variables  variables
     * @param a          a
     * @param b          b
     * @param n          subdivisions (even)
     * @return integral value or null
     */
    public static BigDecimal integralSimpson(
            @NonNull final CalculatorEngine engine,
            @NonNull final String expression,
            @NonNull final Map<String, String> variables,
            @NonNull final BigDecimal a,
            @NonNull final BigDecimal b,
            final int n
    ) {
        final MathContext mc = MathContext.DECIMAL128;
        int nn = Math.max(2, n);
        if (nn % 2 == 1) {
            nn++;
        }

        final BigDecimal h = b.subtract(a, mc).divide(BigDecimal.valueOf(nn), mc);
        if (h.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;

        for (int i = 0; i <= nn; i++) {
            final BigDecimal x = a.add(h.multiply(BigDecimal.valueOf(i), mc), mc);
            final Double y = evalY(engine, expression, variables, x);
            if (y == null) {
                return null;
            }

            final BigDecimal yd = BigDecimal.valueOf(y);
            if (i == 0 || i == nn) {
                sum = sum.add(yd, mc);
            } else if (i % 2 == 0) {
                sum = sum.add(yd.multiply(new BigDecimal("2"), mc), mc);
            } else {
                sum = sum.add(yd.multiply(new BigDecimal("4"), mc), mc);
            }
        }

        return sum.multiply(h, mc).divide(new BigDecimal("3"), mc);
    }

    private static BigDecimal bisection(
            @NonNull final CalculatorEngine engine,
            @NonNull final String expression,
            @NonNull final Map<String, String> variables,
            @NonNull final BigDecimal left,
            @NonNull final BigDecimal right,
            @NonNull final MathContext mc
    ) {
        BigDecimal a = left;
        BigDecimal b = right;

        Double fa = evalY(engine, expression, variables, a);
        Double fb = evalY(engine, expression, variables, b);

        if (fa == null || fb == null) {
            return null;
        }

        if (isZero(fa)) {
            return a;
        }
        if (isZero(fb)) {
            return b;
        }
        if (fa * fb > 0) {
            return null;
        }

        for (int i = 0; i < 80; i++) {
            final BigDecimal mid = a.add(b, mc).divide(new BigDecimal("2"), mc);
            final Double fm = evalY(engine, expression, variables, mid);
            if (fm == null) {
                return null;
            }
            if (isZero(fm)) {
                return mid;
            }

            if (fa * fm < 0) {
                b = mid;
                fb = fm;
            } else {
                a = mid;
                fa = fm;
            }

            if (b.subtract(a, mc).abs().compareTo(new BigDecimal("1e-12")) < 0) {
                return a.add(b, mc).divide(new BigDecimal("2"), mc);
            }
        }

        return a.add(b, mc).divide(new BigDecimal("2"), mc);
    }

    private static BigDecimal chooseStep(@NonNull final BigDecimal x) {
        final BigDecimal ax = x.abs();
        final BigDecimal base = new BigDecimal("1e-6");
        final BigDecimal scaled = ax.multiply(new BigDecimal("1e-6"));
        return scaled.compareTo(base) > 0 ? scaled : base;
    }

    private static boolean isZero(final double v) {
        return Math.abs(v) < 1e-12;
    }

    private static boolean addDistinct(@NonNull final List<BigDecimal> roots, @NonNull final BigDecimal candidate) {
        for (final BigDecimal r : roots) {
            if (r.subtract(candidate).abs().compareTo(new BigDecimal("1e-9")) < 0) {
                return false;
            }
        }
        roots.add(candidate);
        return true;
    }
}