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

import com.mlprograms.justmath.graphfx.core.GraphFxPoint;
import com.mlprograms.justmath.graphfx.testutil.FxTestUtil;
import com.mlprograms.justmath.graphfx.view.GraphFxDisplayPane;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class GraphFxUtilTest {

    @BeforeAll
    static void initFx() {
        FxTestUtil.ensureFxStarted();
    }

    @Test
    void worldToScreenUsesScaleAndOrigin() {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            final GraphFxDisplayPane pane = new GraphFxDisplayPane();
            pane.getScalePxPerUnit().set(100.0);
            pane.getOriginOffsetX().set(400.0);
            pane.getOriginOffsetY().set(300.0);

            final Point2D screen = GraphFxUtil.worldToScreen(pane, new GraphFxPoint(2.0, 1.5));

            // x: originX + worldX*scale
            assertEquals(400.0 + 2.0 * 100.0, screen.getX(), 1e-12);
            // y: originY - worldY*scale (inverted)
            assertEquals(300.0 - 1.5 * 100.0, screen.getY(), 1e-12);
        });
    }

    @Test
    void worldToScreenRejectsNull() {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            final GraphFxDisplayPane pane = new GraphFxDisplayPane();
            assertThrows(NullPointerException.class, () -> GraphFxUtil.worldToScreen(pane, null));
        });
    }

}
