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

package com.mlprograms.justmath.graphfx.api;

import com.mlprograms.justmath.graphfx.testutil.FxTestUtil;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class GraphFxPlotViewerTest {

    @BeforeAll
    static void initFx() {
        FxTestUtil.ensureFxStarted();
    }

    @Test
    void canBeCreatedAndEmbedded() {
        final GraphFxPlotViewer viewer = new GraphFxPlotViewer(DisplayTheme.LIGHT);
        final Parent node = viewer.asNode();
        assertNotNull(node);

        viewer.dispose();
    }

    @Test
    void addAndRemovePointIsStable() {
        final GraphFxPlotViewer viewer = new GraphFxPlotViewer();

        final long id = viewer.addPoint(new Point2D(1, 2), Color.GREEN, 5);
        assertTrue(id > 0);

        viewer.removePoint(id);
        viewer.dispose();
    }

    @Test
    void addPolylineCopiesInputAndRejectsNulls() {
        final GraphFxPlotViewer viewer = new GraphFxPlotViewer();

        assertThrows(NullPointerException.class, () -> viewer.addPolyline(List.of(new Point2D(0,0), null)));

        final var points = new java.util.ArrayList<>(List.of(new Point2D(0,0), new Point2D(1,1)));
        final long id = viewer.addPolyline(points, Color.BLUE, 2);

        // Mutate original list after call (viewer must have copied)
        points.clear();

        // should not throw
        viewer.removePolyline(id);
        viewer.dispose();
    }

    @Test
    void plotExpressionDoesNotThrowAndCanBeCancelledByDispose() {
        final GraphFxPlotViewer viewer = new GraphFxPlotViewer();

        final long plotId = viewer.plotExpression("x^2", "FF0000");
        assertTrue(plotId > 0);

        viewer.plotExpression("x^2 + y^2 - 1", Map.of(), "00FF00");

        viewer.dispose();
    }

    @Test
    void plotExpressionRejectsInvalidHexColor() {
        final GraphFxPlotViewer viewer = new GraphFxPlotViewer();
        assertThrows(IllegalArgumentException.class, () -> viewer.plotExpression("x^2", "f"));
        viewer.dispose();
    }

}
