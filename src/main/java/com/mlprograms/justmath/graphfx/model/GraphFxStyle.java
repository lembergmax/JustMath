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
import lombok.NonNull;

/**
 * Immutable style definition used for rendering GraphFX objects (points, lines, integrals, ...).
 * <p>
 * A style consists of a base {@link Color}, a {@code strokeWidth} for outlines/lines,
 * and an {@code alpha} value that controls transparency.
 * </p>
 *
 * <p>This record is intentionally immutable and therefore safe to share across multiple
 * objects without defensive copies.</p>
 *
 * @param color       base color (without forced alpha modification)
 * @param strokeWidth stroke width used for drawing lines/outlines
 * @param alpha       alpha component in the range {@code [0.0 .. 1.0]} (values outside will be clamped)
 */
public record GraphFxStyle(@NonNull Color color, double strokeWidth, double alpha) {

    /**
     * Returns the configured {@link #color()} with the {@link #alpha()} applied as the effective opacity.
     * <p>
     * The returned color keeps the original RGB components and replaces the alpha channel with
     * the clamped {@link #alpha()} value. Clamping ensures the result is always within the valid
     * JavaFX opacity range {@code [0.0 .. 1.0]}.
     * </p>
     *
     * @return a new {@link Color} instance with the same RGB values as {@link #color()} and an alpha
     * channel derived from {@link #alpha()}
     */
    public Color colorWithAlpha() {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), clampToUnitInterval(alpha));
    }

    /**
     * Clamps the given opacity to the valid JavaFX alpha range.
     *
     * @param opacity desired opacity value
     * @return {@code opacity} clamped into {@code [0.0 .. 1.0]}
     */
    private static double clampToUnitInterval(final double opacity) {
        return Math.max(0.0, Math.min(1.0, opacity));
    }

}
