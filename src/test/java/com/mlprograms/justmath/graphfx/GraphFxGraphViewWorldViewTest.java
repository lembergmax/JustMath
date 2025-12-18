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

import com.mlprograms.justmath.graphfx.view.GraphFxGraphView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxGraphView.WorldView")
final class GraphFxGraphViewWorldViewTest {

    @Test
    void constructor_rejectsInvalidRanges() {
        assertThrows(IllegalArgumentException.class, () -> new GraphFxGraphView.WorldView(1, 1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new GraphFxGraphView.WorldView(0, 1, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> new GraphFxGraphView.WorldView(2, 1, 0, 1));
    }

    @Test
    void containsX_containsY_workAsExpected() {
        final GraphFxGraphView.WorldView view = new GraphFxGraphView.WorldView(-1, 1, -2, 2);

        assertTrue(view.containsX(0));
        assertTrue(view.containsY(0));
        assertFalse(view.containsX(2));
        assertFalse(view.containsY(3));
    }

    @Test
    void pan_shiftsAllBounds() {
        final GraphFxGraphView.WorldView view = new GraphFxGraphView.WorldView(0, 10, 0, 20);
        final GraphFxGraphView.WorldView panned = view.pan(2, -3);

        assertEquals(2, panned.xMin(), 1e-12);
        assertEquals(12, panned.xMax(), 1e-12);
        assertEquals(-3, panned.yMin(), 1e-12);
        assertEquals(17, panned.yMax(), 1e-12);
    }

    @Test
    void zoom_keepsAnchorPositionRelative() {
        final GraphFxGraphView.WorldView view = new GraphFxGraphView.WorldView(0, 10, 0, 10);
        final GraphFxGraphView.WorldView zoomed = view.zoom(5, 5, 0.5); // zoom in

        assertTrue(zoomed.xMax() - zoomed.xMin() < 10);
        assertTrue(zoomed.yMax() - zoomed.yMin() < 10);
        assertTrue(zoomed.containsX(5));
        assertTrue(zoomed.containsY(5));
    }

    @Test
    void lockAspectExact_returnsSameIfCanvasInvalid() {
        final GraphFxGraphView.WorldView view = new GraphFxGraphView.WorldView(0, 10, 0, 10);

        assertSame(view, view.lockAspectExact(0, 100));
        assertSame(view, view.lockAspectExact(100, 0));
        assertSame(view, view.lockAspectExact(-1, 100));
    }

    @Test
    void lockAspectExpandOnly_neverShrinks() {
        final GraphFxGraphView.WorldView view = new GraphFxGraphView.WorldView(0, 10, 0, 1);
        final GraphFxGraphView.WorldView expanded = view.lockAspectExpandOnly(1000, 100);

        assertTrue(expanded.yMax() - expanded.yMin() >= 1);
        assertTrue(expanded.xMax() - expanded.xMin() >= 10);
    }

}
