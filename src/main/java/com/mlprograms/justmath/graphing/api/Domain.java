package com.mlprograms.justmath.graphing.api;

/**
 * Inclusive plotting domain for x-values.
 */
public record Domain(double minX, double maxX) {

    public Domain {
        if (Double.isNaN(minX) || Double.isNaN(maxX) || minX >= maxX) {
            throw new IllegalArgumentException("Invalid domain");
        }
    }
}
