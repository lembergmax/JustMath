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

import com.mlprograms.justmath.graphfx.api.DisplayTheme;
import com.mlprograms.justmath.graphfx.config.WindowConfig;
import com.mlprograms.justmath.graphfx.testutil.FxTestUtil;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class GraphFxDisplayPaneTest {

    @BeforeAll
    static void initFx() {
        FxTestUtil.ensureFxStarted();
    }

    @Test
    void themePropertyApplies() {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            final GraphFxDisplayPane pane = new GraphFxDisplayPane(DisplayTheme.DARK);
            assertEquals(DisplayTheme.DARK, pane.getTheme().get());

            pane.getTheme().set(DisplayTheme.LIGHT);
            assertEquals(DisplayTheme.LIGHT, pane.getTheme().get());
        });
    }

    @Test
    void zoomClampsToBounds() {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            final GraphFxDisplayPane pane = new GraphFxDisplayPane();
            pane.resize(800, 600);
            pane.layout();

            // 1) clamp nach oben: setze auf MAX und zoome "rein" -> darf nicht > MAX werden
            pane.getScalePxPerUnit().set(WindowConfig.MAX_SCALE_PX_PER_UNIT);

            pane.fireEvent(new ScrollEvent(
                    ScrollEvent.SCROLL,
                    400, 300, 400, 300,
                    false,false,false,false,
                    false,false,
                    0, +120, 0, +120, // positive deltaY -> zoom in
                    ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                    ScrollEvent.VerticalTextScrollUnits.NONE, 0,
                    0, null
            ));

            assertTrue(pane.getScalePxPerUnit().get() <= WindowConfig.MAX_SCALE_PX_PER_UNIT);

            // 2) clamp nach unten: setze auf MIN und zoome "raus" -> darf nicht < MIN werden
            pane.getScalePxPerUnit().set(WindowConfig.MIN_SCALE_PX_PER_UNIT);

            pane.fireEvent(new ScrollEvent(
                    ScrollEvent.SCROLL,
                    400, 300, 400, 300,
                    false,false,false,false,
                    false,false,
                    0, -120, 0, -120, // negative deltaY -> zoom out
                    ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                    ScrollEvent.VerticalTextScrollUnits.NONE, 0,
                    0, null
            ));

            assertTrue(pane.getScalePxPerUnit().get() >= WindowConfig.MIN_SCALE_PX_PER_UNIT);
        });
    }

    @Test
    void panningChangesOriginOffsets() {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            final GraphFxDisplayPane pane = new GraphFxDisplayPane();
            pane.resize(800, 600);
            pane.layout();

            final double ox = pane.getOriginOffsetX().get();
            final double oy = pane.getOriginOffsetY().get();

            // Simuliere Drag
            pane.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED, 100, 100, 100, 100, MouseButton.MIDDLE, 1,
                    false,false,false,false, true,false,false,true,false,false, null));
            pane.fireEvent(new MouseEvent(MouseEvent.MOUSE_DRAGGED, 130, 120, 130, 120, MouseButton.MIDDLE, 1,
                    false,false,false,false, true,false,false,true,false,false, null));
            pane.fireEvent(new MouseEvent(MouseEvent.MOUSE_RELEASED, 130, 120, 130, 120, MouseButton.MIDDLE, 1,
                    false,false,false,false, true,false,false,true,false,false, null));

            assertNotEquals(ox, pane.getOriginOffsetX().get());
            assertNotEquals(oy, pane.getOriginOffsetY().get());
        });
    }

    @Test
    void scrollZoomChangesScale() {
        FxTestUtil.runOnFxThreadAndWait(() -> {
            final GraphFxDisplayPane pane = new GraphFxDisplayPane();
            pane.resize(800, 600);
            pane.layout();

            final double before = pane.getScalePxPerUnit().get();

            pane.fireEvent(new ScrollEvent(
                    ScrollEvent.SCROLL,
                    400, 300, 400, 300,
                    false,false,false,false,
                    false,false,
                    0, -120, 0, -120, // deltaY -> zoom in/out
                    ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                    ScrollEvent.VerticalTextScrollUnits.NONE, 0,
                    0, null
            ));

            assertNotEquals(before, pane.getScalePxPerUnit().get());
        });
    }

}
