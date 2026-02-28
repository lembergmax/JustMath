package com.mlprograms.justmath.graphing.api;

import com.mlprograms.justmath.graphing.engine.DefaultGraphingCalculator;

public final class GraphingCalculators {

    private GraphingCalculators() {
    }

    public static GraphingCalculator createDefault() {
        return new DefaultGraphingCalculator();
    }
}
