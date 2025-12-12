/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.graph;


import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Samples a GraphFunction into polyline segments for plotting.
 */
public class GraphSampler {

    private GraphSampler() {
    }

    /**
     * Samples the given function in the interval [xMin, xMax] into segments.
     *
     * @param function the function to sample
     * @param xMin     min x
     * @param xMax     max x
     * @param samples  number of samples (>= 2)
     * @param config   sampling configuration
     * @return sampled graph data with bounds
     */
    @NonNull
    public static GraphData sample(
            @NonNull final GraphFunction function,
            @NonNull final BigDecimal xMin,
            @NonNull final BigDecimal xMax,
            final int samples,
            @NonNull final GraphSamplingConfig config
    ) {
        if (samples < 2) {
            throw new IllegalArgumentException("samples must be >= 2");
        }

        if (xMin.compareTo(xMax) >= 0) {
            throw new IllegalArgumentException("xMin must be < xMax");
        }

        final BigDecimal range = xMax.subtract(xMin);
        final BigDecimal step = range.divide(BigDecimal.valueOf(samples), config.getStepMathContext());
        if (step.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Step size is zero. Increase range, reduce samples, or use a higher-precision step MathContext.");
        }

        final List<GraphSegment> segments = new ArrayList<>();
        final List<GraphPoint> current = new ArrayList<>();

        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;

        Double lastY = null;

        for (int i = 0; i <= samples; i++) {
            final BigDecimal x = (i == samples) ? xMax : xMin.add(step.multiply(BigDecimal.valueOf(i), config.getStepMathContext()));

            final Double y = evaluateY(function, x, config);
            if (y == null) {
                lastY = null;
                flushSegment(segments, current, config);
                continue;
            }

            if (lastY != null && Math.abs(y - lastY) > config.getDiscontinuityJumpThreshold()) {
                flushSegment(segments, current, config);
            }

            current.add(new GraphPoint(x.doubleValue(), y));
            lastY = y;

            yMin = Math.min(yMin, y);
            yMax = Math.max(yMax, y);
        }

        flushSegment(segments, current, config);

        final GraphData.GraphWorldBounds bounds = createBounds(xMin.doubleValue(), xMax.doubleValue(), yMin, yMax, config);
        return new GraphData(List.copyOf(segments), bounds);
    }

    private static Double evaluateY(@NonNull final GraphFunction function, @NonNull final BigDecimal x, @NonNull final GraphSamplingConfig config) {
        try {
            final BigNumber yBN = function.evaluate(x);
            final double y = yBN.toBigDecimal().doubleValue();
            if (!Double.isFinite(y)) {
                return null;
            }
            if (Math.abs(y) > config.getMaxAbsY()) {
                return null;
            }
            return y;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void flushSegment(@NonNull final List<GraphSegment> segments, @NonNull final List<GraphPoint> current, @NonNull final GraphSamplingConfig config) {
        if (current.size() >= config.getMinSegmentPoints()) {
            segments.add(new GraphSegment(List.copyOf(current)));
        }
        current.clear();
    }

    private static GraphData.GraphWorldBounds createBounds(final double xMin, final double xMax, final double yMin, final double yMax, @NonNull final GraphSamplingConfig config) {
        final double safeYMin = Double.isFinite(yMin) ? yMin : -10d;
        final double safeYMax = Double.isFinite(yMax) ? yMax : 10d;

        double height = safeYMax - safeYMin;
        if (height == 0d) {
            height = 1d;
        }

        final double pad = Math.max(1e-9, height * config.getYPaddingFactor());
        return new GraphData.GraphWorldBounds(xMin, xMax, safeYMin - pad, safeYMax + pad);
    }

}
