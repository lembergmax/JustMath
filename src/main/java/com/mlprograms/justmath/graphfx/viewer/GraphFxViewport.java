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

package com.mlprograms.justmath.graphfx.viewer;

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable world viewport definition for {@link GraphFxViewer}.
 *
 * <p>
 * A viewport defines the visible world rectangle. All element coordinates are interpreted in this world coordinate
 * system.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class GraphFxViewport {

    /**
     * Default viewport used by {@link GraphFxViewer}.
     */
    public static final GraphFxViewport DEFAULT = GraphFxViewport.builder().build();

    /**
     * Minimum X bound (world units).
     */
    @Builder.Default
    @NonNull
    BigNumber worldMinimumXValue = new BigNumber("-10");

    /**
     * Maximum X bound (world units).
     */
    @Builder.Default
    @NonNull
    BigNumber worldMaximumXValue = new BigNumber("10");

    /**
     * Minimum Y bound (world units).
     */
    @Builder.Default
    @NonNull
    BigNumber worldMinimumYValue = new BigNumber("-10");

    /**
     * Maximum Y bound (world units).
     */
    @Builder.Default
    @NonNull
    BigNumber worldMaximumYValue = new BigNumber("10");
}
