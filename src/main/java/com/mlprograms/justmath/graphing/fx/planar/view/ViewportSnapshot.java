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

package com.mlprograms.justmath.graphing.fx.planar.view;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.util.Objects;

/**
 * Immutable snapshot of the current world viewport and sampling parameters.
 * <p>
 * This type captures the minimum/maximum world coordinates and a world-unit cell size that defines the
 * sampling grid for the implicit contour plotter.
 * </p>
 *
 * @param minX     inclusive minimum x in world units (non-null)
 * @param maxX     inclusive maximum x in world units (non-null)
 * @param minY     inclusive minimum y in world units (non-null)
 * @param maxY     inclusive maximum y in world units (non-null)
 * @param cellSize positive sampling step size in world units (non-null)
 */
public record ViewportSnapshot(BigNumber minX, BigNumber maxX, BigNumber minY, BigNumber maxY, BigNumber cellSize) {

    /**
     * Validates that all values are non-null.
     */
    public ViewportSnapshot {
        Objects.requireNonNull(minX, "minX must not be null");
        Objects.requireNonNull(maxX, "maxX must not be null");
        Objects.requireNonNull(minY, "minY must not be null");
        Objects.requireNonNull(maxY, "maxY must not be null");
        Objects.requireNonNull(cellSize, "cellSize must not be null");
    }
}
