package com.mlprograms.justmath.graphing.api;

/**
 * Inclusive x-domain used for plotting.
 *
 * @param minX minimum x value (inclusive)
 * @param maxX maximum x value (inclusive)
 */
public record Domain(double minX, double maxX) {

    public Domain {
        if (!Double.isFinite(minX) || !Double.isFinite(maxX) || minX >= maxX) {
            throw new IllegalArgumentException("Invalid domain bounds");
        }
    }

}
