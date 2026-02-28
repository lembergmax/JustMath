package com.mlprograms.justmath.graphing.engine.cache;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small synchronized LRU cache for compiled expressions.
 */
public final class ExpressionCache {

    private final Map<String, CompiledPlotExpression> cache;

    public ExpressionCache(int maxEntries) {
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CompiledPlotExpression> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public synchronized CompiledPlotExpression get(String expression) {
        return cache.get(expression);
    }

    public synchronized void put(String expression, CompiledPlotExpression compiledExpression) {
        cache.put(expression, compiledExpression);
    }
}
