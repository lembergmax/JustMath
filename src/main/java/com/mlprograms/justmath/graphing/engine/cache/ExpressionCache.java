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

package com.mlprograms.justmath.graphing.engine.cache;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small synchronized LRU cache for compiled expressions.
 * <p>
 * This cache is optimized for simplicity and determinism. Compilation is usually significantly more
 * expensive than a cache lookup, so a small LRU cache provides a good tradeoff.
 * </p>
 */
public final class ExpressionCache {

    /**
     * Maximum number of entries retained by the cache.
     */
    private final int maximumSize;

    /**
     * Access-ordered map used to implement LRU eviction.
     */
    private final Map<String, CompiledPlotExpression> cache;

    /**
     * Creates a cache with a maximum size.
     *
     * @param maximumSize maximum number of cached expressions (must be {@code >= 1})
     * @throws IllegalArgumentException if {@code maximumSize < 1}
     */
    public ExpressionCache(final int maximumSize) {
        if (maximumSize < 1) {
            throw new IllegalArgumentException("maximumSize must be >= 1");
        }
        this.maximumSize = maximumSize;
        this.cache = new LinkedHashMap<>(32, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<String, CompiledPlotExpression> eldest) {
                return size() > ExpressionCache.this.maximumSize;
            }
        };
    }

    /**
     * Returns the cached compiled expression for the given key.
     *
     * @param expression expression key (may be {@code null})
     * @return cached compiled expression or {@code null} if absent
     */
    public synchronized CompiledPlotExpression get(final String expression) {
        return cache.get(expression);
    }

    /**
     * Stores a compiled expression in the cache.
     *
     * @param expression         expression key (must not be {@code null})
     * @param compiledExpression compiled expression (must not be {@code null})
     */
    public synchronized void put(@NonNull final String expression,
                                 @NonNull final CompiledPlotExpression compiledExpression) {
        cache.put(expression, compiledExpression);
    }

    /**
     * Returns the configured maximum size.
     *
     * @return maximum cache size (always {@code >= 1})
     */
    public int maximumSize() {
        return maximumSize;
    }
}
