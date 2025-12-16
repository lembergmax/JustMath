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

import javafx.scene.paint.Color;

/**
 * Provides deterministic, visually distinct colors for graph elements (e.g., functions).
 * <p>
 * The palette uses the golden ratio conjugate to distribute hues evenly around the color wheel.
 * This approach yields a sequence of colors where consecutive indices tend to be perceptually distinct,
 * which is useful when adding multiple functions to a plot.
 * </p>
 *
 * <p>
 * Colors are generated in the HSB color space:
 * </p>
 * <ul>
 *     <li>Hue: derived from the index using the golden ratio conjugate</li>
 *     <li>Saturation: fixed (currently {@code 0.70})</li>
 *     <li>Brightness: fixed (currently {@code 0.95})</li>
 *     <li>Alpha: fixed (currently {@code 1.0})</li>
 * </ul>
 */
public final class GraphFxPalette {

    /**
     * The golden ratio conjugate (1 - φ⁻¹) used to generate well-distributed hue steps.
     * <p>
     * Multiplying an integer index by this value and taking the fractional part yields a low-discrepancy
     * sequence over {@code [0, 1)}, which helps avoid clustering of hues.
     * </p>
     */
    private static final double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;

    private static final double HUE_OFFSET = 0.12;
    private static final double SATURATION = 0.70;
    private static final double BRIGHTNESS = 0.95;
    private static final double ALPHA = 1.0;

    private GraphFxPalette() {
    }

    /**
     * Returns a deterministic color for a given index.
     * <p>
     * The hue is computed as:
     * </p>
     * <pre>
     * hue = (HUE_OFFSET + index * GOLDEN_RATIO_CONJUGATE) mod 1
     * </pre>
     * <p>
     * The result is then mapped to degrees {@code [0..360)} and used to create a JavaFX {@link Color}
     * via {@link Color#hsb(double, double, double, double)}.
     * </p>
     *
     * @param index the index of the element (e.g., function number). Negative indices are allowed and still
     *              produce a valid color due to the modulo operation.
     * @return a generated {@link Color} with fixed saturation, brightness, and alpha
     */
    public static Color colorForIndex(final int index) {
        final double normalizedHue = (HUE_OFFSET + (index * GOLDEN_RATIO_CONJUGATE)) % 1.0;
        return Color.hsb(normalizedHue * 360.0, SATURATION, BRIGHTNESS, ALPHA);
    }

}
