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
 * Immutable grid configuration for {@link GraphFxViewer}.
 *
 * <p>
 * Grid spacing is explicit. The viewer does not compute "nice" tick spacing.
 * Library consumers define spacing in world units using {@link BigNumber}.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class GraphFxGridConfiguration {

    /**
     * Default grid configuration: grid and tick labels enabled, axes enabled, spacing set to 1.
     */
    public static final GraphFxGridConfiguration DEFAULT = GraphFxGridConfiguration.builder().build();

    /**
     * Whether the grid is visible.
     */
    @Builder.Default
    boolean isGridVisible = true;

    /**
     * Whether tick labels are visible.
     */
    @Builder.Default
    boolean areTickLabelsVisible = true;

    /**
     * Whether axes (x=0 and y=0) are visible when within bounds.
     */
    @Builder.Default
    boolean areAxesVisible = true;

    /**
     * Grid spacing on the X axis in world units.
     */
    @Builder.Default
    @NonNull
    BigNumber gridSpacingXValue = new BigNumber("1");

    /**
     * Grid spacing on the Y axis in world units.
     */
    @Builder.Default
    @NonNull
    BigNumber gridSpacingYValue = new BigNumber("1");
}
