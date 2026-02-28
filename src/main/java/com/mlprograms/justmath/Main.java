/*
 * Copyright (c) 2025-2026 Max Lemberg
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

import com.mlprograms.justmath.graphfx.planar.view.GraphFxViewer;

import java.util.List;

/**
 * Quick start entry point for launching GraphFx visually.
 * <p>
 * Usage examples:
 * <ul>
 *     <li>No args: starts with a demo set of expressions.</li>
 *     <li>With args: each argument is interpreted as one expression, e.g. {@code x^2} {@code sin(x)}.</li>
 * </ul>
 */
public class Main {


    public static void main(final String[] args) {
        final List<String> expressions = List.of("x^2", "sin(x)", "x^3-2*x");

        final GraphFxViewer viewer = new GraphFxViewer("JustMath Graphing Calculator");
        viewer.show();

        for (String expression : expressions) {
            viewer.plotExpression(expression);
        }

        viewer.show();
        System.out.println("GraphFx started. Plotted expressions: " + String.join(", ", expressions));
    }


}
