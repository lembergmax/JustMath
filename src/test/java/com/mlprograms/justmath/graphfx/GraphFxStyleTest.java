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

import com.mlprograms.justmath.graphfx.model.GraphFxStyle;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxStyle")
final class GraphFxStyleTest {

    private static final double EPS = 1e-12;

    @ParameterizedTest(name = "alpha clamp: input={0} -> expectedAlpha={1}")
    @CsvSource({
            "-1.0, 0.0",
            "-0.01, 0.0",
            "0.0, 0.0",
            "0.25, 0.25",
            "0.5, 0.5",
            "1.0, 1.0",
            "1.01, 1.0",
            "2.0, 1.0"
    })
    void colorWithAlpha_clampsAlphaToUnitInterval(final double inputAlpha, final double expectedAlpha) {
        final Color base = new Color(0.1, 0.2, 0.3, 0.9);
        final GraphFxStyle style = new GraphFxStyle(base, 2.0, inputAlpha);

        final Color effective = style.colorWithAlpha();

        assertEquals(expectedAlpha, effective.getOpacity(), EPS);
    }

    @Test
    void colorWithAlpha_preservesRgbChannels() {
        final Color base = new Color(0.25, 0.5, 0.75, 1.0);
        final GraphFxStyle style = new GraphFxStyle(base, 1.5, 0.4);

        final Color effective = style.colorWithAlpha();

        assertEquals(base.getRed(), effective.getRed(), EPS);
        assertEquals(base.getGreen(), effective.getGreen(), EPS);
        assertEquals(base.getBlue(), effective.getBlue(), EPS);
    }

    @Test
    void constructor_throwsForNullColor() {
        assertThrows(NullPointerException.class, () -> new GraphFxStyle(null, 1.0, 0.5));
    }

}

