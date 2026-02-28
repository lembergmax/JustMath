package com.mlprograms.justmath.graphing.api;

import com.mlprograms.justmath.graphing.engine.DefaultGraphingCalculator;

/**
 * Factory for creating graphing calculator instances.
 */
public final class GraphingCalculators {

    private GraphingCalculators() {
    }

    public static GraphingCalculator createDefault() {
        return new DefaultGraphingCalculator();
    }
}
