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

package com.mlprograms.justmath;

import com.mlprograms.justmath.graphfx.DisplayTheme;
import com.mlprograms.justmath.graphfx.GraphFxDisplayOnlyApp;
import javafx.geometry.Point2D;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        final GraphFxDisplayOnlyApp light = new GraphFxDisplayOnlyApp(DisplayTheme.LIGHT);
        light.setPoints(List.of(
                new Point2D(0, 0),
                new Point2D(2, 3),
                new Point2D(-4, 1)
        ));
        light.setPolyline(List.of(
                new Point2D(-6, -2),
                new Point2D(-2, 2),
                new Point2D(2, -1),
                new Point2D(6, 3)
        ));
        light.show("GraphFx Light (Overlay)", 1000, 700);
        light.centerOrigin();

//        final GraphFxDisplayOnlyApp dark = new GraphFxDisplayOnlyApp(DisplayTheme.DARK);
//        dark.setPoints(List.of(
//                new Point2D(-3, 2),
//                new Point2D(0, 4),
//                new Point2D(3, 2)
//        ));
//        dark.setPolyline(List.of(
//                new Point2D(-5, -5),
//                new Point2D(0, 0),
//                new Point2D(5, 5)
//        ));
//        dark.show("GraphFx Dark (Overlay)", 1000, 700);
//        dark.centerOrigin();

    }

}
