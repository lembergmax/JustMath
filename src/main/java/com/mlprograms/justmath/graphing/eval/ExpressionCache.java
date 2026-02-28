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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Small, synchronized LRU cache used for caching {@link CompiledExpression} instances.
 * <p>
 * The cache is designed for the graph engine workload:
 * <ul>
 *     <li>Many repeated expressions across frames.</li>
 *     <li>Occasional new expressions when the user edits the input.</li>
 *     <li>Bounded memory usage via LRU eviction.</li>
 * </ul>
 * <p>
 * Thread-safety is implemented via {@code synchronized} methods.
 * This is sufficient here because cache operations are tiny compared to evaluation cost.
 */
public final class ExpressionCache {

    /**
     * Maximum number of entries in the cache.
     * <p>
     * If {@code 0}, the cache behaves as disabled (still stores nothing).
     */
    private final int maxSize;

    /**
     * Backing LRU map.
     */
    private final Map<String, CompiledExpression> cache;

    /**
     * Creates a cache with the given maximum size.
     *
     * @param maxSize maximum number of cached expressions (must be {@code >= 0})
     */
    public ExpressionCache(final int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize must be >= 0");
        }
        this.maxSize = maxSize;

        this.cache = new LinkedHashMap<>(32, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<String, CompiledExpression> eldest) {
                return maxSize > 0 && size() > maxSize;
            }
        };
    }

    /**
     * Returns the maximum size of the cache.
     *
     * @return maximum size, {@code 0} means disabled
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * Retrieves a cached expression by its normalized expression key.
     *
     * @param normalizedExpression normalized expression string (must not be {@code null})
     * @return cached {@link CompiledExpression} or {@code null} if not found
     */
    public synchronized CompiledExpression get(final String normalizedExpression) {
        Objects.requireNonNull(normalizedExpression, "normalizedExpression must not be null");
        return cache.get(normalizedExpression);
    }

    /**
     * Stores a compiled expression under its normalized expression key.
     * <p>
     * If the cache is disabled ({@link #maxSize()} == 0), this method does nothing.
     *
     * @param normalizedExpression normalized expression string (must not be {@code null})
     * @param compiledExpression   compiled expression (must not be {@code null})
     */
    public synchronized void put(final String normalizedExpression, final CompiledExpression compiledExpression) {
        Objects.requireNonNull(normalizedExpression, "normalizedExpression must not be null");
        Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");

        if (maxSize == 0) {
            return;
        }
        cache.put(normalizedExpression, compiledExpression);
    }

    /**
     * Clears all cache entries.
     */
    public synchronized void clear() {
        cache.clear();
    }
}
