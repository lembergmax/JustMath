/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.graphing.api;

/**
 * Sampling resolution for plotting.
 * <p>
 * The engine aims to generate {@link #targetSamples()} points. The {@link #pixelWidthHint()} allows strategies
 * to adapt resolution to display size while still being usable headless.
 * </p>
 *
 * @param targetSamples  desired number of samples (must be >= 2)
 * @param pixelWidthHint positive hint for display width (must be > 0)
 */
public record Resolution(int targetSamples, double pixelWidthHint) {

    /**
     * Validates resolution invariants.
     *
     * @throws IllegalArgumentException if {@code targetSamples < 2} or {@code pixelWidthHint <= 0}
     */
    public Resolution {
        if (targetSamples < 2) {
            throw new IllegalArgumentException("targetSamples must be >= 2");
        }
        if (Double.isNaN(pixelWidthHint) || pixelWidthHint <= 0.0d) {
            throw new IllegalArgumentException("pixelWidthHint must be > 0");
        }
    }

    /**
     * Creates a resolution that matches the given pixel width.
     *
     * @param widthPx pixel width (must be >= 2)
     * @return resolution instance
     */
    public static Resolution forPixels(final int widthPx) {
        if (widthPx < 2) {
            throw new IllegalArgumentException("widthPx must be >= 2");
        }
        return new Resolution(Math.max(2, widthPx), widthPx);
    }

    /**
     * Creates a fixed resolution with an explicit sample count.
     *
     * @param samples sample count (must be >= 2)
     * @return resolution instance
     */
    public static Resolution fixed(final int samples) {
        if (samples < 2) {
            throw new IllegalArgumentException("samples must be >= 2");
        }
        return new Resolution(samples, samples);
    }
}
