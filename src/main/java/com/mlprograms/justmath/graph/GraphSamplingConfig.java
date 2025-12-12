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


import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.MathContext;

/**
 * Configuration for graph sampling and discontinuity handling.
 */
@Value
@Builder(toBuilder = true)
public class GraphSamplingConfig {

    /**
     * MathContext used for step size computation.
     */
    @NonNull
    @Builder.Default
    MathContext stepMathContext = MathContext.DECIMAL128;

    /**
     * If |y(i) - y(i-1)| exceeds this value, the polyline is split into a new segment.
     */
    @Builder.Default
    double discontinuityJumpThreshold = 1_000_000d;

    /**
     * Points with |y| > maxAbsY are treated as out-of-range and split the segment.
     */
    @Builder.Default
    double maxAbsY = 1_000_000d;

    /**
     * Padding factor added to computed y bounds for nicer initial view fitting.
     */
    @Builder.Default
    double yPaddingFactor = 0.08d;

    /**
     * Minimal segment size to keep (prevents tiny fragments).
     */
    @Builder.Default
    int minSegmentPoints = 2;

}
