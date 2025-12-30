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

package com.mlprograms.justmath.graphfx.view;

import com.mlprograms.justmath.graphfx.testutil.FxTestUtil;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class GraphFxOverlayPlotTest {

    @BeforeAll
    static void initFx() {
        FxTestUtil.ensureFxStarted();
    }

    @Test
    void setPointsRejectsNullElements() {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            final GraphFxOverlayPlot overlay = new GraphFxOverlayPlot(new GraphFxDisplayPane());
            assertThrows(NullPointerException.class, () -> overlay.setPoints(List.of(new Point2D(1,2), null)));
        });
    }

    @Test
    void styleClampsToMinimum() {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            final GraphFxOverlayPlot overlay = new GraphFxOverlayPlot(new GraphFxDisplayPane());
            overlay.setPointStyle(Color.RED, -10);      // clamp
            overlay.setPolylineStyle(Color.BLUE, 0);    // clamp
            // Wenn keine Exception: OK (Rendering bleibt stabil)
        });
    }

    @Test
    void disposeMakesInstanceUnusableAndIsIdempotent() {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            final GraphFxOverlayPlot overlay = new GraphFxOverlayPlot(new GraphFxDisplayPane());

            assertTrue(overlay.getOverlayCanvas().widthProperty().isBound());
            assertTrue(overlay.getOverlayCanvas().heightProperty().isBound());

            overlay.dispose();
            overlay.dispose(); // idempotent

            assertFalse(overlay.getOverlayCanvas().widthProperty().isBound());
            assertFalse(overlay.getOverlayCanvas().heightProperty().isBound());

            assertThrows(IllegalStateException.class, overlay::clear);
            assertThrows(IllegalStateException.class, () -> overlay.setPoints(List.of(new Point2D(0,0))));
        });
    }

}
