package com.mlprograms.justmath.graphing.api;

/**
 * Sampling resolution for the plotting engine.
 *
 * @param sampleCount number of target samples
 */
public record Resolution(int sampleCount) {

    public Resolution {
        if (sampleCount < 2) {
            throw new IllegalArgumentException("sampleCount must be >= 2");
        }
    }

    public static Resolution fixed(int sampleCount) {
        return new Resolution(sampleCount);
    }

    public static Resolution forPixelWidth(int width) {
        return new Resolution(Math.max(2, width));
    }
}
