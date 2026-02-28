package com.mlprograms.justmath.graphing.engine;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small synchronized LRU cache for compiled expressions.
 */
public final class ExpressionCache {

    private final Map<String, CompiledPlotExpression> cache;

    public ExpressionCache(final int capacity) {
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<String, CompiledPlotExpression> eldest) {
                return size() > capacity;
            }
        };
    }

    public synchronized CompiledPlotExpression get(final String expression) {
        return cache.get(expression);
    }

    public synchronized void put(final String expression, final CompiledPlotExpression compiled) {
        cache.put(expression, compiled);
    }
}
