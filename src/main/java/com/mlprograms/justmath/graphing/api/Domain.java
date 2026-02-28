package com.mlprograms.justmath.graphing.api;

/**
 * Closed x-domain used for sampling.
 */
public record Domain(double minX, double maxX) {

    public Domain {
        if (!Double.isFinite(minX) || !Double.isFinite(maxX) || minX >= maxX) {
            throw new IllegalArgumentException("Invalid domain");
        }
    }
}
