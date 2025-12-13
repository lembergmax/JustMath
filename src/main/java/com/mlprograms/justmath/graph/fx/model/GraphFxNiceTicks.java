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
package com.mlprograms.justmath.graph.fx.model;
public final class GraphFxNiceTicks {

    private GraphFxNiceTicks() {
    }

    public static double niceStep(final double min, final double max, final int targetLines) {
        final double range = Math.abs(max - min);
        if (range == 0d || !Double.isFinite(range)) {
            return 1d;
        }

        final double rough = range / Math.max(2, targetLines);
        final double pow10 = Math.pow(10, Math.floor(Math.log10(rough)));
        final double norm = rough / pow10;

        final double nice;
        if (norm < 1.5) nice = 1;
        else if (norm < 3) nice = 2;
        else if (norm < 7) nice = 5;
        else nice = 10;

        return nice * pow10;
    }
}

