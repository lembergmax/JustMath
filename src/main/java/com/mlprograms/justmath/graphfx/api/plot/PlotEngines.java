/*
 * Copyright (c) 2025 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.graphfx.api.plot;

import com.mlprograms.justmath.graphfx.core.GraphFxCalculatorEngine;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Factory methods for commonly used {@link PlotEngine} implementations.
 * <p>
 * The built-in engines are designed to work well out of the box, while still allowing advanced users to provide
 * custom engines by implementing {@link PlotEngine}.
 *
 * <h2>Defaults</h2>
 * {@link GraphFxCalculatorEngine} uses JustMath's expression engine and supports:
 * <ul>
 *   <li>explicit expressions such as {@code sin(x)} (produces a polyline)</li>
 *   <li>implicit expressions such as {@code x^2 + y^2 - 1} (produces line segments for the zero contour)</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlotEngines {

    /**
     * Returns the library default {@link PlotEngine}.
     * <p>
     * This method exists primarily for backward compatibility with earlier GraphFx versions
     * where a dedicated default engine method was exposed.
     *
     * @return the default plot engine
     */
    public static PlotEngine defaultEngine() {
        return new GraphFxCalculatorEngine();
    }

}
