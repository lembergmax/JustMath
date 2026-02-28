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
import com.mlprograms.justmath.calculator.CalculatorEngine;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default {@link ExpressionEngine} implementation backed by the JustMath {@link CalculatorEngine}.
 * <p>
 * Design goals:
 * <ul>
 *     <li><b>Fast repeated evaluation</b> via LRU caching of {@link CompiledExpression}.</li>
 *     <li><b>Low allocation hot-path</b> by reusing a {@link HashMap} per thread for variable binding.</li>
 *     <li><b>Correct number literals</b> by formatting doubles without scientific notation
 *     (prevents accidental variables like {@code E} from {@code 1.0E7}).</li>
 * </ul>
 * <p>
 * Thread-safety:
 * <ul>
 *     <li>The engine is thread-safe.</li>
 *     <li>The cache uses synchronization internally.</li>
 *     <li>Variable binding uses a {@link ThreadLocal} map, so concurrent evaluations do not interfere.</li>
 * </ul>
 */
public final class GraphFxCalculatorEngine implements ExpressionEngine {

    /**
     * Default maximum number of cached compiled expressions.
     * <p>
     * This is intentionally moderate: expressions can be large, and the cache should not grow unbounded.
     */
    public static final int DEFAULT_CACHE_SIZE = 512;

    /**
     * Reserved variable name for the graph engine x-axis.
     */
    public static final String X_VARIABLE = "x";

    private final CalculatorEngine calculatorEngine;
    private final ExpressionCache cache;

    /**
     * Per-thread reusable map used to bind variables for an evaluation call.
     * <p>
     * A ThreadLocal is used to keep this optimization safe in concurrent scenarios
     * without needing to allocate a new map for each evaluation.
     */
    private final ThreadLocal<HashMap<String, String>> reusableVariableMap;

    /**
     * Creates an engine with a default {@link CalculatorEngine} and default cache size.
     *
     * @return a new {@link GraphFxCalculatorEngine} (never {@code null})
     */
    public static GraphFxCalculatorEngine createDefault() {
        return new GraphFxCalculatorEngine(new CalculatorEngine(), new ExpressionCache(DEFAULT_CACHE_SIZE));
    }

    /**
     * Creates an engine with the given calculator and cache size.
     *
     * @param calculatorEngine underlying calculator engine (must not be {@code null})
     * @param cacheSize        maximum cache size (must be {@code >= 0}; {@code 0} disables caching)
     */
    public GraphFxCalculatorEngine(final CalculatorEngine calculatorEngine, final int cacheSize) {
        this(calculatorEngine, new ExpressionCache(Math.max(0, cacheSize)));
    }

    /**
     * Creates an engine with the given calculator and cache implementation.
     *
     * @param calculatorEngine underlying calculator engine (must not be {@code null})
     * @param cache            expression cache (must not be {@code null})
     */
    public GraphFxCalculatorEngine(final CalculatorEngine calculatorEngine, final ExpressionCache cache) {
        this.calculatorEngine = Objects.requireNonNull(calculatorEngine, "calculatorEngine must not be null");
        this.cache = Objects.requireNonNull(cache, "cache must not be null");
        this.reusableVariableMap = ThreadLocal.withInitial(() -> new HashMap<>(32));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigNumber evaluate(final String expression) {
        return evaluate(expression, Collections.emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigNumber evaluate(final String expression, final Map<String, String> variables) {
        return compile(expression).evaluate(variables);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompiledExpression compile(final String expression) {
        final String normalized = ExpressionNormalizer.normalize(expression);

        final CompiledExpression cached = cache.get(normalized);
        if (cached != null) {
            return cached;
        }

        final CompiledExpression compiled = new DefaultCompiledExpression(
                normalized,
                calculatorEngine,
                reusableVariableMap
        );

        cache.put(normalized, compiled);
        return compiled;
    }

    /**
     * Default compiled expression implementation.
     * <p>
     * This is a small value object holding the expression string and references to shared services.
     */
    @ToString
    @EqualsAndHashCode(of = "expression")
    @RequiredArgsConstructor
    static final class DefaultCompiledExpression implements CompiledExpression {

        /**
         * Normalized expression string.
         */
        private final String expression;

        /**
         * Underlying evaluation engine (shared).
         */
        private final CalculatorEngine calculatorEngine;

        /**
         * Per-thread variable map provider used to avoid allocations in hot loops.
         */
        private final ThreadLocal<HashMap<String, String>> reusableVariableMap;

        /**
         * {@inheritDoc}
         */
        @Override
        public String expression() {
            return expression;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BigNumber evaluate() {
            return evaluate(Collections.emptyMap());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BigNumber evaluate(final Map<String, String> variables) {
            Objects.requireNonNull(variables, "variables must not be null");

            final Map<String, String> bindings = bindVariables(variables);
            try {
                return calculatorEngine.evaluate(expression, bindings);
            } catch (final RuntimeException exception) {
                throw new ExpressionEngineException("Failed to evaluate expression: " + expression, exception);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BigNumber evaluateAtX(final double xValue, final Map<String, String> variables) {
            if (!Double.isFinite(xValue)) {
                throw new IllegalArgumentException("xValue must be finite");
            }
            Objects.requireNonNull(variables, "variables must not be null");

            final HashMap<String, String> map = reusableVariableMap.get();
            map.clear();

            map.putAll(variables);
            map.put(X_VARIABLE, NumberLiteralFormatter.toCalculatorLiteral(xValue));

            try {
                return calculatorEngine.evaluate(expression, map);
            } catch (final RuntimeException exception) {
                throw new ExpressionEngineException("Failed to evaluate expression at x=" + xValue + ": " + expression, exception);
            }
        }

        /**
         * Binds user variables into the reusable per-thread map.
         *
         * @param variables user variables
         * @return a map instance safe to pass to {@link CalculatorEngine#evaluate(String, Map)}
         */
        private Map<String, String> bindVariables(final Map<String, String> variables) {
            if (variables.isEmpty()) {
                return Collections.emptyMap();
            }

            final HashMap<String, String> map = reusableVariableMap.get();
            map.clear();
            map.putAll(variables);
            return map;
        }
    }
}
