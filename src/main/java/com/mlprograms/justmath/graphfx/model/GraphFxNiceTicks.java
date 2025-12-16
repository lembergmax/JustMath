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

package com.mlprograms.justmath.graphfx.model;

/**
 * Utility for computing "nice" tick/grid step sizes for numeric axes.
 * <p>
 * When rendering axes and grid lines, a purely arithmetic step (range / N) often produces awkward values
 * such as {@code 3.14159}. This utility rounds the step size to human-friendly values of the form:
 * </p>
 * <ul>
 *     <li>{@code 1 * 10^k}</li>
 *     <li>{@code 2 * 10^k}</li>
 *     <li>{@code 5 * 10^k}</li>
 *     <li>{@code 10 * 10^k}</li>
 * </ul>
 * <p>
 * This yields visually pleasing ticks and labels while keeping the number of grid lines close to a target.
 * </p>
 */
public final class GraphFxNiceTicks {

    private GraphFxNiceTicks() {
    }

    /**
     * Computes a "nice" step size for the interval {@code [min, max]} given a desired number of grid lines.
     * <p>
     * The algorithm:
     * </p>
     * <ol>
     *     <li>Computes the absolute range {@code |max - min|}.</li>
     *     <li>Derives a rough step as {@code range / max(2, targetLines)}.</li>
     *     <li>Normalizes the rough step into {@code [1, 10)} by factoring out a power of 10.</li>
     *     <li>Rounds the normalized value to {1, 2, 5, 10} and scales back.</li>
     * </ol>
     *
     * <p>
     * If the input range is zero or non-finite, this method returns {@code 1.0} as a safe default.
     * </p>
     *
     * @param min         minimum axis value
     * @param max         maximum axis value
     * @param targetLines desired number of major grid lines; values &lt; 2 are treated as 2
     * @return a positive, finite step size suitable for ticks/grid lines
     */
    public static double niceStep(final double min, final double max, final int targetLines) {
        final double axisRange = Math.abs(max - min);
        if (axisRange == 0d || !Double.isFinite(axisRange)) {
            return 1d;
        }

        final double roughStep = axisRange / Math.max(2, targetLines);

        final double powerOfTen = Math.pow(10, Math.floor(Math.log10(roughStep)));
        final double normalizedStep = roughStep / powerOfTen;

        final double niceNormalizedStep;
        if (normalizedStep < 1.5) {
            niceNormalizedStep = 1d;
        } else if (normalizedStep < 3d) {
            niceNormalizedStep = 2d;
        } else if (normalizedStep < 7d) {
            niceNormalizedStep = 5d;
        } else {
            niceNormalizedStep = 10d;
        }

        return niceNormalizedStep * powerOfTen;
    }

}
