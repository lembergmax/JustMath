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

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.CalculatorEngineUtils;
import com.mlprograms.justmath.calculator.Tokenizer;
import com.mlprograms.justmath.calculator.internal.Token;
import com.mlprograms.justmath.graphfx.ReservedVariables;

import java.util.List;
import java.util.Objects;

final class GraphFxCalculatorEngineUtils {

    static String normalizeExpression(final String rawExpression) {
        final String expression = Objects.requireNonNull(rawExpression, "expression must not be null").trim();
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("expression must not be blank");
        }

        final String[] equation = splitTopLevelEquation(expression);
        if (equation != null) {
            return "(" + equation[0] + ")-(" + equation[1] + ")";
        }

        if (referencesReservedY(expression)) {
            return expression;
        }

        return "(" + expression + ")-" + ReservedVariables.Y.getValue();
    }

    private static boolean referencesReservedY(final String expression) {
        final String normalized = CalculatorEngineUtils.replaceAbsSigns(expression);
        final List<Token> tokens = new Tokenizer().tokenize(normalized);

        for (final Token token : tokens) {
            if (token.getType() == Token.Type.VARIABLE && ReservedVariables.Y.getValue().equals(token.getValue())) {
                return true;
            }
        }

        return false;
    }

    private static String[] splitTopLevelEquation(final String expression) {
        int depth = 0;
        int equalsIndex = -1;

        for (int i = 0; i < expression.length(); i++) {
            final char character = expression.charAt(i);
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth = Math.max(0, depth - 1);
            } else if (character == '=' && depth == 0) {
                if (equalsIndex >= 0) {
                    throw new IllegalArgumentException("Expression must contain at most one '=' on top level.");
                }

                equalsIndex = i;
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
