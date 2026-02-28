package com.mlprograms.justmath.graphing.api;

/**
 * Defines sampling density.
 */
public record Resolution(int targetSamples, double pixelWidthHint) {

    private static final int MINIMUM_SAMPLES = 2;

    public Resolution {
        if (targetSamples < MINIMUM_SAMPLES) {
            throw new IllegalArgumentException("targetSamples must be >= " + MINIMUM_SAMPLES);
        }
        if (!Double.isFinite(pixelWidthHint) || pixelWidthHint <= 0) {
            throw new IllegalArgumentException("pixelWidthHint must be positive");
        }
    }

    public static Resolution forPixels(final int widthPx) {
        if (widthPx < MINIMUM_SAMPLES) {
            throw new IllegalArgumentException("widthPx must be >= " + MINIMUM_SAMPLES);
        }
        return new Resolution(widthPx, widthPx);
    }

    public static Resolution fixed(final int samples) {
        if (samples < MINIMUM_SAMPLES) {
            throw new IllegalArgumentException("samples must be >= " + MINIMUM_SAMPLES);
        }
        return new Resolution(samples, samples);
    }
}
