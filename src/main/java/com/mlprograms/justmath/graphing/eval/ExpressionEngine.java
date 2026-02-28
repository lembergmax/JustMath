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
 * High-level facade for evaluating mathematical expressions using the underlying JustMath {@code CalculatorEngine}.
 * <p>
 * This interface is intentionally small and stable to serve as the foundation for a highly optimized graph engine.
 * The graph engine will call this API extremely frequently (thousands to millions of evaluations per frame),
 * therefore implementations are expected to:
 * <ul>
 *     <li>Minimize allocations per evaluation.</li>
 *     <li>Cache any reusable state (for example compiled expressions).</li>
 *     <li>Avoid slow or lossy numeric conversions (e.g., scientific notation strings like {@code 1.0E7}).</li>
 * </ul>
 * <p>
 * Expressions and variables follow the semantics of JustMath:
 * variables are passed as {@code Map<String, String>} and can themselves be expressions.
 */
public interface ExpressionEngine {

    /**
     * Evaluates the given expression without custom variables.
     *
     * @param expression the mathematical expression to evaluate (must not be {@code null} or blank)
     * @return the computed result as a {@link BigNumber} (never {@code null})
     * @throws ExpressionEngineException if evaluation fails for any reason
     */
    BigNumber evaluate(String expression);

    /**
     * Evaluates the given expression using the supplied variables.
     * <p>
     * Variables are substituted according to JustMath rules. If an undefined variable is referenced,
     * the underlying calculator throws an exception which is wrapped into {@link ExpressionEngineException}.
     *
     * @param expression the mathematical expression to evaluate (must not be {@code null} or blank)
     * @param variables  variables available to the expression (must not be {@code null})
     * @return the computed result as a {@link BigNumber} (never {@code null})
     * @throws ExpressionEngineException if evaluation fails for any reason
     */
    BigNumber evaluate(String expression, Map<String, String> variables);

    /**
     * Creates a reusable, cached representation of the expression.
     * <p>
     * The default implementation uses caching and cheap validation only.
     * It does not parse the expression into an AST because JustMath currently exposes evaluation only.
     * <p>
     * A {@link CompiledExpression} is still valuable because:
     * <ul>
     *     <li>It avoids repeated normalization/validation.</li>
     *     <li>It enables fast variable binding paths used by the future graph engine.</li>
     *     <li>It allows LRU caching keyed by the normalized expression string.</li>
     * </ul>
     *
     * @param expression the expression to compile (must not be {@code null} or blank)
     * @return a reusable {@link CompiledExpression} (never {@code null})
     * @throws ExpressionEngineException if compilation input is invalid
     */
    CompiledExpression compile(String expression);
}
