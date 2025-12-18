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

import com.mlprograms.justmath.graphfx.model.GraphFxPalette;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxPalette")
final class GraphFxPaletteTest {

    private static final double EPS = 1e-12;

    @Test
    void colorForIndex_isDeterministic() {
        final Color first = GraphFxPalette.colorForIndex(7);
        final Color second = GraphFxPalette.colorForIndex(7);

        assertEquals(first.getRed(), second.getRed(), EPS);
        assertEquals(first.getGreen(), second.getGreen(), EPS);
        assertEquals(first.getBlue(), second.getBlue(), EPS);
        assertEquals(first.getOpacity(), second.getOpacity(), EPS);
    }

    @ParameterizedTest(name = "different indices should differ: a={0}, b={1}")
    @CsvSource({
            "0, 1",
            "1, 2",
            "2, 10",
            "-1, 0",
            "-5, 5"
    })
    void colorForIndex_producesDifferentColorsForDifferentIndices(final int a, final int b) {
        final Color left = GraphFxPalette.colorForIndex(a);
        final Color right = GraphFxPalette.colorForIndex(b);

        final boolean allChannelsEqual =
                nearlyEqual(left.getRed(), right.getRed()) &&
                        nearlyEqual(left.getGreen(), right.getGreen()) &&
                        nearlyEqual(left.getBlue(), right.getBlue()) &&
                        nearlyEqual(left.getOpacity(), right.getOpacity());

        assertFalse(allChannelsEqual, "Different indices should not produce identical colors.");
    }

    @Test
    void colorForIndex_alwaysReturnsValidOpacity() {
        final Color color = GraphFxPalette.colorForIndex(123);

        assertTrue(color.getOpacity() >= 0.0 && color.getOpacity() <= 1.0);
    }

    private static boolean nearlyEqual(final double a, final double b) {
        return Math.abs(a - b) <= EPS;
    }

}

