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

import com.mlprograms.justmath.calculator.CalculatorEngineUtils;
import com.mlprograms.justmath.calculator.Tokenizer;
import com.mlprograms.justmath.calculator.internal.Token;
import com.mlprograms.justmath.graphfx.ReservedVariables;

import java.util.List;
import java.util.Objects;

/**
 * Internal helper utilities for analyzing user plot expressions.
 * <p>
 * The plot engine supports two families of inputs:
 * <ul>
 *     <li><b>Explicit functions</b> (fast): {@code sin(x)}, {@code y = sin(x)}, {@code sin(x) = y}</li>
 *     <li><b>Implicit equations</b> (2D contour): {@code x^2 + y^2 - 9}, {@code x^2 + y^2 = 9}</li>
 * </ul>
 * This class detects the family and returns a normalized implicit form where needed.
 */
final class GraphFxCalculatorEngineUtils {

    /**
     * Immutable analysis result for a user expression.
     *
     * @param normalizedImplicitExpression expression normalized to an implicit zero contour form {@code f(x,y)=0}
     * @param explicitFunction             whether the expression can be treated as {@code y = f(x)}
     * @param explicitFunctionExpression   if {@code explicitFunction=true}, this contains {@code f(x)} (never references {@code y});
     *                                     otherwise {@code null}
     */
    record ExpressionAnalysis(String normalizedImplicitExpression, boolean explicitFunction,
                                     String explicitFunctionExpression) {

        /**
         * Compact validation for the analysis record.
         *
         * @param normalizedImplicitExpression normalized implicit expression (never null/blank)
         * @param explicitFunction             whether the expression is explicit
         * @param explicitFunctionExpression   explicit expression {@code f(x)} if explicit
         */
        ExpressionAnalysis {
            Objects.requireNonNull(normalizedImplicitExpression, "normalizedImplicitExpression must not be null");
            if (normalizedImplicitExpression.isBlank()) {
                throw new IllegalArgumentException("normalizedImplicitExpression must not be blank");
            }
            if (explicitFunction && (explicitFunctionExpression == null || explicitFunctionExpression.isBlank())) {
                throw new IllegalArgumentException("explicitFunctionExpression must not be null/blank when explicitFunction is true");
            }
        }
    }

    /**
     * Utility class constructor.
     */
    private GraphFxCalculatorEngineUtils() {
        throw new AssertionError("No instances");
    }

    /**
     * Normalizes a raw user expression into an implicit form where the contour is at value {@code 0}.
     * <p>
     * This method exists for backwards compatibility. Prefer {@link #analyzeExpression(String)}.
     *
     * @param rawExpression raw user input
     * @return normalized implicit expression in the form {@code f(x,y)=0}
     */
    static String normalizeExpression(final String rawExpression) {
        return analyzeExpression(rawExpression).normalizedImplicitExpression();
    }

    /**
     * Analyzes a raw user expression and determines whether it can be plotted as an explicit function {@code y=f(x)}.
     * <p>
     * Explicit detection rules:
     * <ul>
     *     <li>{@code y = f(x)} where the right side does not reference {@code y}</li>
     *     <li>{@code f(x) = y} where the left side does not reference {@code y}</li>
     *     <li>Pure {@code f(x)} that does not reference {@code y} is treated as {@code y=f(x)}</li>
     * </ul>
     *
     * @param rawExpression raw user input (non-null, non-blank)
     * @return a fully populated {@link ExpressionAnalysis}
     */
    static ExpressionAnalysis analyzeExpression(final String rawExpression) {
        final String expression = Objects.requireNonNull(rawExpression, "rawExpression must not be null").trim();
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("expression must not be blank");
        }

        final String[] equationParts = splitTopLevelEquation(expression);
        if (equationParts != null) {
            final String left = equationParts[0];
            final String right = equationParts[1];

            final String normalizedImplicit = "(" + left + ")-(" + right + ")";

            final boolean leftIsY = isExactlyReservedVariable(left, ReservedVariables.Y.getValue());
            final boolean rightIsY = isExactlyReservedVariable(right, ReservedVariables.Y.getValue());

            if (leftIsY && !referencesReservedVariable(right, ReservedVariables.Y.getValue())) {
                return new ExpressionAnalysis(normalizedImplicit, true, right);
            }
            if (rightIsY && !referencesReservedVariable(left, ReservedVariables.Y.getValue())) {
                return new ExpressionAnalysis(normalizedImplicit, true, left);
            }

            return new ExpressionAnalysis(normalizedImplicit, false, null);
        }

        final boolean referencesY = referencesReservedVariable(expression, ReservedVariables.Y.getValue());
        if (referencesY) {
            return new ExpressionAnalysis(expression, false, null);
        }

        // Treat "f(x)" as "y = f(x)" for fast plotting while keeping implicit normalization for compatibility.
        final String normalizedImplicit = "(" + expression + ")-" + ReservedVariables.Y.getValue();
        return new ExpressionAnalysis(normalizedImplicit, true, expression);
    }

    /**
     * Checks whether the given expression references a specific reserved variable (e.g. {@code y}).
     *
     * @param expression  the expression to inspect
     * @param variableKey the variable name to look for (e.g. {@code "y"})
     * @return {@code true} if referenced, otherwise {@code false}
     */
    private static boolean referencesReservedVariable(final String expression, final String variableKey) {
        final String normalized = CalculatorEngineUtils.replaceAbsSigns(expression);
        final List<Token> tokens = new Tokenizer().tokenize(normalized);

        for (final Token token : tokens) {
            if (token.getType() == Token.Type.VARIABLE && variableKey.equals(token.getValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the expression is exactly a single variable token (ignoring whitespace),
     * e.g. {@code "y"}.
     *
     * @param expression  expression to inspect
     * @param variableKey variable key to match
     * @return {@code true} if expression is exactly that variable
     */
    private static boolean isExactlyReservedVariable(final String expression, final String variableKey) {
        final String normalized = CalculatorEngineUtils.replaceAbsSigns(expression);
        final List<Token> tokens = new Tokenizer().tokenize(normalized);

        return tokens.size() == 1 && tokens.get(0).getType() == Token.Type.VARIABLE && variableKey.equals(tokens.get(0).getValue());
    }

    /**
     * Splits an equation of the form {@code left = right} only if {@code =} occurs on top-level (depth 0),
     * i.e. not inside parentheses.
     *
     * @param expression full expression
     * @return a {@code [left, right]} pair, or {@code null} if there is no top-level {@code =}
     */
    private static String[] splitTopLevelEquation(final String expression) {
        int depth = 0;
        int equalsIndex = -1;

        for (int index = 0; index < expression.length(); index++) {
            final char character = expression.charAt(index);

            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth = Math.max(0, depth - 1);
            } else if (character == '=' && depth == 0) {
                if (equalsIndex >= 0) {
                    throw new IllegalArgumentException("Expression must contain at most one '=' on top level.");
                }
                equalsIndex = index;
            }
        }

        if (equalsIndex < 0) {
            return null;
        }

        final String left = expression.substring(0, equalsIndex).trim();
        final String right = expression.substring(equalsIndex + 1).trim();

        if (left.isEmpty() || right.isEmpty()) {
            throw new IllegalArgumentException("Invalid equation syntax. Use 'left = right'.");
        }

        return new String[]{left, right};
    }

}
