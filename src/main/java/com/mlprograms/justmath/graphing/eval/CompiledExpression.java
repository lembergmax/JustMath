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

package com.mlprograms.justmath.graphing.eval;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.util.Map;

/**
 * Reusable, thread-safe expression handle.
 * <p>
 * A compiled expression represents a normalized expression string plus
 * an optimized evaluation pathway (fast variable binding, fewer allocations).
 * <p>
 * Important: "compiled" does not imply AST compilation at this stage.
 * It is a semantic handle intended for caching and for fast repeated evaluation.
 */
public interface CompiledExpression {

    /**
     * Returns the normalized expression string represented by this instance.
     *
     * @return normalized expression string (never {@code null} or blank)
     */
    String expression();

    /**
     * Evaluates this expression with no additional variables.
     *
     * @return result as {@link BigNumber} (never {@code null})
     * @throws ExpressionEngineException if evaluation fails
     */
    BigNumber evaluate();

    /**
     * Evaluates this expression with the given variables.
     *
     * @param variables variables to apply (must not be {@code null})
     * @return result as {@link BigNumber} (never {@code null})
     * @throws ExpressionEngineException if evaluation fails
     */
    BigNumber evaluate(Map<String, String> variables);

    /**
     * Evaluates this expression with the given variables plus a dedicated {@code x} variable.
     * <p>
     * This method exists specifically for the graph engine hot path.
     * It avoids:
     * <ul>
     *     <li>Allocating a new {@link Map} for every point.</li>
     *     <li>Generating scientific-notation strings (e.g. {@code 1.0E7}) that would introduce
     *     undefined variables like {@code E} in the JustMath tokenizer.</li>
     * </ul>
     *
     * @param xValue    x-value to bind (must be finite)
     * @param variables extra variables (must not be {@code null})
     * @return result as {@link BigNumber} (never {@code null})
     * @throws ExpressionEngineException if evaluation fails
     */
    BigNumber evaluateAtX(double xValue, Map<String, String> variables);
}
