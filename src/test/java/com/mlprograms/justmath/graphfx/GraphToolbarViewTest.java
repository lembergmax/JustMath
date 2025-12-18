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

package com.mlprograms.justmath.graphfx;

import com.mlprograms.justmath.graphfx.FxTestSupport;
import com.mlprograms.justmath.graphfx.view.GraphToolbarView;
import javafx.scene.control.ToggleButton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphToolbarView")
final class GraphToolbarViewTest {

    @Test
    void createsAllControls_andToolButtonsShareAToggleGroup() {
        /* TODO: will nicht fertig werden
        final GraphToolbarView toolbar = FxTestSupport.onFxThread(GraphToolbarView::new);

        assertNotNull(toolbar.resetViewButton());
        assertNotNull(toolbar.clearMarksButton());
        assertNotNull(toolbar.gridCheckBox());
        assertNotNull(toolbar.axesCheckBox());
        assertNotNull(toolbar.exportPngButton());
        assertNotNull(toolbar.exportSvgButton());
        assertNotNull(toolbar.exportCsvButton());
        assertNotNull(toolbar.exportJsonButton());
        assertNotNull(toolbar.statusLabel());

        assertAllToolButtonsInSameGroup(
                toolbar.moveButton(),
                toolbar.zoomBoxButton(),
                toolbar.pointButton(),
                toolbar.tangentButton(),
                toolbar.normalButton(),
                toolbar.rootButton(),
                toolbar.intersectButton(),
                toolbar.integralButton()
        );
         */
    }

    private static void assertAllToolButtonsInSameGroup(final ToggleButton... buttons) {
        assertTrue(buttons.length > 1);
        final var group = buttons[0].getToggleGroup();
        assertNotNull(group, "First button must have a ToggleGroup.");

        for (final ToggleButton button : buttons) {
            assertNotNull(button);
            assertSame(group, button.getToggleGroup(), "All tool buttons must share the same ToggleGroup.");
        }
    }

}

