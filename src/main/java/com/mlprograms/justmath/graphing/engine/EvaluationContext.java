package com.mlprograms.justmath.graphing.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutable evaluation context reused while sampling points.
 */
public final class EvaluationContext {

    private final Map<String, Double> variables = new HashMap<>();

    public void clear() {
        variables.clear();
    }

    public void putAll(final Map<String, Double> values) {
        variables.putAll(values);
    }

    public Map<String, Double> variables() {
        return variables;
    }
}
