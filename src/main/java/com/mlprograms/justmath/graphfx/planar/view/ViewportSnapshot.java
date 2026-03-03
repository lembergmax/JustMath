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
 * Immutable snapshot of the currently visible world bounds.
 *
 * <p>
 * This is primarily useful if another module wants to compute plot data for exactly the visible region.
 * The GUI module itself does not compute plot data.
 * </p>
 *
 * @param minX minimum visible x in world units (must not be null)
 * @param maxX maximum visible x in world units (must not be null)
 * @param minY minimum visible y in world units (must not be null)
 * @param maxY maximum visible y in world units (must not be null)
 */
public record ViewportSnapshot(BigNumber minX, BigNumber maxX, BigNumber minY, BigNumber maxY) {

    public ViewportSnapshot {
        Objects.requireNonNull(minX, "minX must not be null");
        Objects.requireNonNull(maxX, "maxX must not be null");
        Objects.requireNonNull(minY, "minY must not be null");
        Objects.requireNonNull(maxY, "maxY must not be null");
    }

}