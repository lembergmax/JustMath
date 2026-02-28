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

package com.mlprograms.justmath.graphing.fx.planar.engine.expression;

import lombok.experimental.UtilityClass;

/**
 * Parses an equation string into left and right expressions.
 * <p>
 * Supported forms:
 * <ul>
 *     <li>{@code left=right}</li>
 *     <li>{@code left==right}</li>
 * </ul>
 * If no equals sign exists, the whole string is interpreted as {@code left} and {@code right} becomes {@code 0}.
 * </p>
 */
@UtilityClass
public class GraphExpressionParser {

    /**
     * Parses an equation into two sides.
     *
     * @param equation equation string
     * @return parsed pair
     */
    public static ParsedEquation parse(final String equation) {
        final String trimmed = equation == null ? "" : equation.trim();

        final int doubleEqualsIndex = trimmed.indexOf("==");
        if (doubleEqualsIndex >= 0) {
            return new ParsedEquation(
                    trimmed.substring(0, doubleEqualsIndex).trim(),
                    trimmed.substring(doubleEqualsIndex + 2).trim()
            );
        }

        final int equalsIndex = trimmed.indexOf('=');
        if (equalsIndex >= 0) {
            return new ParsedEquation(
                    trimmed.substring(0, equalsIndex).trim(),
                    trimmed.substring(equalsIndex + 1).trim()
            );
        }

        return new ParsedEquation(trimmed, "0");
    }

    /**
     * Immutable parsed equation.
     *
     * @param left  left expression
     * @param right right expression
     */
    public record ParsedEquation(String left, String right) {
    }
}
