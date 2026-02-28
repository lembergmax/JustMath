package com.mlprograms.justmath.graphing.api;

/**
 * Sampling resolution for plotting.
 */
public record Resolution(int targetSamples, double pixelWidthHint) {

    public Resolution {
        if (targetSamples < 2) {
            throw new IllegalArgumentException("targetSamples must be >= 2");
        }
        if (Double.isNaN(pixelWidthHint) || pixelWidthHint <= 0.0d) {
            throw new IllegalArgumentException("pixelWidthHint must be > 0");
        }
    }

    public static Resolution forPixels(final int widthPx) {
        if (widthPx < 2) {
            throw new IllegalArgumentException("widthPx must be >= 2");
        }
        return new Resolution(Math.max(2, widthPx), widthPx);
    }

    public static Resolution fixed(final int samples) {
        if (samples < 2) {
            throw new IllegalArgumentException("samples must be >= 2");
        }
        return new Resolution(samples, samples);
    }
}
