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

package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphfx.planar.model.PlotLine;
import com.mlprograms.justmath.graphfx.planar.model.PlotPoint;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import com.mlprograms.justmath.graphfx.planar.view.GraphFxViewer;

import java.util.List;

public final class Main {

    public static void main(final String[] args) {
        GraphFxViewer viewer = new GraphFxViewer();
        viewer.show();

        PlotResult plot = new PlotResult(
                List.of(
                        new PlotPoint(new BigNumber("-2"), new BigNumber("1")),
                        new PlotPoint(new BigNumber("0"), new BigNumber("0")),
                        new PlotPoint(new BigNumber("2"), new BigNumber("1"))
                ),
                List.of(
                        new PlotLine(List.of(
                                new PlotPoint(new BigNumber("-4"), new BigNumber("-2")),
                                new PlotPoint(new BigNumber("0"), new BigNumber("2")),
                                new PlotPoint(new BigNumber("4"), new BigNumber("-2"))
                        ))
                )
        );

        viewer.setPlotResult(plot);
        viewer.fitViewport(new com.mlprograms.justmath.graphfx.planar.view.ViewportSnapshot(
                new BigNumber("-5"), new BigNumber("5"), new BigNumber("-4"), new BigNumber("4")
        ));

    }

}