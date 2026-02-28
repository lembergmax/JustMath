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

package com.mlprograms.justmath.graphing.fx.planar.engine.marching;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotPoint;
import lombok.experimental.UtilityClass;

/**
 * Utility functions for segment handling.
 */
@UtilityClass
public class SegmentUtils {

    /**
     * Determines whether two points are considered equal within an epsilon tolerance.
     * <p>
     * BigNumber does not provide a built-in epsilon equals, so we compare using doubles for the join heuristic.
     * </p>
     *
     * @param a       first point
     * @param b       second point
     * @param epsilon epsilon tolerance (positive)
     * @return true if both coordinates are within epsilon
     */
    public static boolean equalsEpsilon(final PlotPoint a, final PlotPoint b, final double epsilon) {
        final double ax = a.x().doubleValue();
        final double ay = a.y().doubleValue();
        final double bx = b.x().doubleValue();
        final double by = b.y().doubleValue();

        return Math.abs(ax - bx) <= epsilon && Math.abs(ay - by) <= epsilon;
    }

    /**
     * Performs linear interpolation between {@code p0} and {@code p1} based on their function values.
     *
     * @param p0     start coordinate
     * @param p1     end coordinate
     * @param v0     function value at p0
     * @param v1     function value at p1
     * @return interpolated coordinate
     */
    public static BigNumber interpolate(final BigNumber p0, final BigNumber p1, final BigNumber v0, final BigNumber v1) {
        final double dv0 = v0.doubleValue();
        final double dv1 = v1.doubleValue();

        final double t;
        if (Math.abs(dv1 - dv0) < 1e-12) {
            t = 0.5;
        } else {
            t = dv0 / (dv0 - dv1);
        }

        final double result = p0.doubleValue() + t * (p1.doubleValue() - p0.doubleValue());
        return new BigNumber(Double.toString(result));
    }
}
