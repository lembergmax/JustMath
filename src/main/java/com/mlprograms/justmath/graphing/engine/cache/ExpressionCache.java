package com.mlprograms.justmath.graphing.engine.cache;

import com.mlprograms.justmath.graphing.api.CompiledPlotExpression;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small LRU cache for compiled expressions.
 */
public final class ExpressionCache {

    private final Map<String, CompiledPlotExpression> cache;

    public ExpressionCache(final int maxSize) {
        this.cache = new LinkedHashMap<>(32, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<String, CompiledPlotExpression> eldest) {
                return size() > maxSize;
            }
        };
    }

    public synchronized CompiledPlotExpression get(final String expression) {
        return cache.get(expression);
    }

    public synchronized void put(final String expression, final CompiledPlotExpression compiledExpression) {
        cache.put(expression, compiledExpression);
    }
}
