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

package com.mlprograms.justmath.graphfx.demo;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphfx.element.GraphFxLine;
import com.mlprograms.justmath.graphfx.element.GraphFxPoint;
import com.mlprograms.justmath.graphfx.element.GraphFxPolyline;
import com.mlprograms.justmath.graphfx.viewer.GraphFxGridConfiguration;
import com.mlprograms.justmath.graphfx.viewer.GraphFxViewer;
import com.mlprograms.justmath.graphfx.viewer.GraphFxViewerConfiguration;
import com.mlprograms.justmath.graphfx.viewer.GraphFxViewport;

/**
 * Small, self-contained demo entry point for {@link GraphFxViewer}.
 *
 * <p>
 * This class is meant as a usage example for library consumers. It does not participate in the rendering pipeline.
 * </p>
 */
public final class GraphFxViewerDemoMain {

    /**
     * Private constructor to prevent instantiation.
     */
    private GraphFxViewerDemoMain() {
    }

    /**
     * Launches a demo window and renders a few example elements.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        GraphFxViewer viewer = new GraphFxViewer(
                GraphFxViewport.builder()
                        .worldMinimumXValue(new BigNumber("-4.5"))
                        .worldMaximumXValue(new BigNumber("4.5"))
                        .worldMinimumYValue(new BigNumber("-3.0"))
                        .worldMaximumYValue(new BigNumber("5.5"))
                        .build(),
                GraphFxGridConfiguration.builder()
                        .gridSpacingXValue(new BigNumber("1"))
                        .gridSpacingYValue(new BigNumber("1"))
                        .areAxesVisible(true)
                        .areTickLabelsVisible(true)
                        .isGridVisible(true)
                        .build(),
                GraphFxViewerConfiguration.DEFAULT.toBuilder()
                        .xAxisName("x")
                        .yAxisName("y")
                        .build()
        );

        // Polyline "graph" - caller provides points; viewer only draws them.
        viewer.addPolyline(GraphFxPolyline.builder()
                .polylineName("Graph A (given points)")
                .polylinePoint(GraphFxPolyline.GraphFxPolylinePoint.builder()
                        .worldXValue(new BigNumber("-4"))
                        .worldYValue(new BigNumber("1.3"))
                        .build())
                .polylinePoint(GraphFxPolyline.GraphFxPolylinePoint.builder()
                        .worldXValue(new BigNumber("-2"))
                        .worldYValue(new BigNumber("2"))
                        .build())
                .polylinePoint(GraphFxPolyline.GraphFxPolylinePoint.builder()
                        .worldXValue(new BigNumber("0"))
                        .worldYValue(new BigNumber("0"))
                        .build())
                .polylinePoint(GraphFxPolyline.GraphFxPolylinePoint.builder()
                        .worldXValue(new BigNumber("3"))
                        .worldYValue(new BigNumber("4"))
                        .build())
                .build());

        // Line with an explicit label (viewer does not compute equation/value).
        viewer.addLine(GraphFxLine.builder()
                .lineName("L1")
                .lineLabelText("L1: caller-provided label")
                .startWorldXValue(new BigNumber("-4"))
                .startWorldYValue(new BigNumber("-1"))
                .endWorldXValue(new BigNumber("4.5"))
                .endWorldYValue(new BigNumber("3.7"))
                .build());

        // A point with a label.
        viewer.addPoint(GraphFxPoint.builder()
                .pointName("P(2.5, 3.75)")
                .worldXValue(new BigNumber("2.5"))
                .worldYValue(new BigNumber("3.75"))
                .build());

        viewer.show("JustMath GraphFxViewer Demo", 1200, 820);
    }
}
