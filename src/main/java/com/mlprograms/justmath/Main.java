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

package com.mlprograms.justmath;

import com.mlprograms.justmath.graphfx.api.DisplayTheme;
import com.mlprograms.justmath.graphfx.api.PlotViewer;
import com.mlprograms.justmath.graphfx.api.plot.LineSegment;
import com.mlprograms.justmath.graphfx.api.plot.PlotEngine;
import com.mlprograms.justmath.graphfx.api.plot.PlotEngines;
import com.mlprograms.justmath.graphfx.api.plot.PlotGeometry;
import com.mlprograms.justmath.graphfx.api.plot.PlotRequest;
import com.mlprograms.justmath.graphfx.api.plot.WorldBounds;
import com.mlprograms.justmath.graphfx.core.Point;

import java.util.List;
import java.util.Map;

public final class Main {

    public static void main(final String[] args) {

        final PlotViewer plotViewer = new PlotViewer();
        plotViewer.plotExpression("x^2+y^2-9", "#D4D4D4");
        plotViewer.show("GraphFx PlotViewer Demo", 1200, 800);

    }

}
