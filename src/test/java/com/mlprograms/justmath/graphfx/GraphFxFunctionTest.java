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

import com.mlprograms.justmath.graphfx.model.GraphFxFunction;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxFunction")
final class GraphFxFunctionTest {

    @Test
    void create_setsValues_andUpdatesHexFromColor() {
        final GraphFxFunction function = GraphFxFunction.create("f", "x", Color.BLACK);

        assertEquals("f", function.getName());
        assertEquals("x", function.getExpression());
        assertEquals(Color.BLACK, function.getColor());
        assertEquals("#000000", function.colorHexProperty().get());

        function.setColor(Color.rgb(255, 128, 0));
        assertEquals("#FF8000", function.colorHexProperty().get());
    }

    @Test
    void toString_containsNameAndExpression() {
        final GraphFxFunction function = GraphFxFunction.create("f", "x^2", Color.RED);
        final String text = function.toString();

        assertTrue(text.contains("f"));
        assertTrue(text.contains("x^2"));
    }

}

