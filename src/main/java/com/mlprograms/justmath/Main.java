/*
 * Copyright (c) 2026 Max Lemberg
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

import com.mlprograms.justmath.graphing.api.*;
import com.mlprograms.justmath.graphing.fx.WindowConfig;
import com.mlprograms.justmath.graphing.fx.planar.engine.ImplicitFunctionPlotEngine;
import com.mlprograms.justmath.graphing.fx.planar.view.GraphFxViewer;

import java.util.Map;

/**
 * Example entry point that demonstrates both:
 * <ul>
 *     <li>Headless sampling via {@link GraphingCalculator} (core module)</li>
 *     <li>Launching the JavaFX viewer via {@link GraphFxViewer} (fx module)</li>
 * </ul>
 */
public final class Main {

    /**
     * Starts the demo.
     *
     * @param args CLI args (unused)
     */
    public static void main(final String[] args) {
        runHeadlessSamplingExample();
        launchFxViewerExample();
    }

    /**
     * Demonstrates how to sample a function without any JavaFX UI.
     * <p>
     * This is the library-style API: compile + sample/plot.
     * </p>
     */
    private static void runHeadlessSamplingExample() {
        final GraphingCalculator calculator = GraphingCalculators.createDefault();

        final PlotRequest request = PlotRequest.builder("sin(x)")
                .domain(new Domain(-10.0, 10.0))
                .resolution(Resolution.fixed(200))
                .variables(Map.of(
                        // Optional constants for the expression, e.g. "a*sin(x)" with a=2
                        // "a", 2.0
                ))
                .build();

        final PlotSeries series = calculator.plot(request).series().get(0);

        System.out.println("Headless sampling: " + request.expression());
        System.out.println("Samples: " + series.xValues().length);

        // Print the first few points
        for (int i = 0; i < Math.min(5, series.xValues().length); i++) {
            System.out.printf("  x=%f  y=%f%n", series.xValues()[i], series.yValues()[i]);
        }
    }

    /**
     * Demonstrates how to launch the JavaFX viewer contained in the fx module.
     * <p>
     * In the refactored viewer, the equation is entered interactively in the text field.
     * </p>
     */
    private static void launchFxViewerExample() {
        final WindowConfig windowConfig = WindowConfig.defaultConfig()
                .toBuilder()
                .title("JustMath GraphFx Demo")
                .width(1200)
                .height(800)
                .exitApplicationOnLastViewerClose(true)
                .build();

        final GraphFxViewer viewer = new GraphFxViewer(windowConfig, new ImplicitFunctionPlotEngine());
        viewer.show();

        System.out.println("FX Viewer started. Enter an equation in the text field and press 'Plot'.");
        System.out.println("Examples: y^2=x^3-x  |  x^2+y^2=9  |  y=sin(x)");
    }

}