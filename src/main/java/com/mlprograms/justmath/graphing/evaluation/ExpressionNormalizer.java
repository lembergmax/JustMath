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

package com.mlprograms.justmath.graphing.evaluation;

import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * Utility responsible for normalizing expression input strings.
 * <p>
 * Normalization is deliberately conservative:
 * <ul>
 *     <li>Trims leading/trailing whitespace.</li>
 *     <li>Rejects blank expressions.</li>
 * </ul>
 * <p>
 * Future graphing layers may extend normalization (e.g. transforming {@code y = f(x)} into an implicit form),
 * but the low-level evaluation engine stays as a minimal and predictable building block.
 */
@UtilityClass
public class ExpressionNormalizer {

    /**
     * Normalizes a raw expression string.
     *
     * @param expression raw expression input (must not be {@code null})
     * @return normalized expression (never {@code null} or blank)
     * @throws ExpressionEngineException if the expression is {@code null} or blank
     */
    public static String normalize(final String expression) {
        Objects.requireNonNull(expression, "expression must not be null");
        final String normalized = expression.trim();
        if (normalized.isEmpty()) {
            throw new ExpressionEngineException("Expression must not be blank");
        }
        return normalized;
    }
}
