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
package com.mlprograms.justmath.graphfx.planar.view;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.util.Objects;

/**
 * Immutable snapshot describing a rectangular visible region in world coordinates.
 *
 * <p>
 * A snapshot is intended to:
 * </p>
 * <ul>
 *     <li>Expose the current viewer state to callers.</li>
 *     <li>Serve as input to calculation engines (sampling, marching squares, etc.).</li>
 *     <li>Allow restoring or fitting a viewport to a known region.</li>
 * </ul>
 *
 * @param minX minimum visible x value (must not be null)
 * @param maxX maximum visible x value (must not be null)
 * @param minY minimum visible y value (must not be null)
 * @param maxY maximum visible y value (must not be null)
 */
public record ViewportSnapshot(
        /** Minimum visible x value. */
        BigNumber minX,
        /** Maximum visible x value. */
        BigNumber maxX,
        /** Minimum visible y value. */
        BigNumber minY,
        /** Maximum visible y value. */
        BigNumber maxY
) {

    /**
     * Validates that all values are present.
     */
    public ViewportSnapshot {
        Objects.requireNonNull(minX, "minX must not be null");
        Objects.requireNonNull(maxX, "maxX must not be null");
        Objects.requireNonNull(minY, "minY must not be null");
        Objects.requireNonNull(maxY, "maxY must not be null");
    }
}
