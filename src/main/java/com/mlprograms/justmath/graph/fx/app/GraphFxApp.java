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

package com.mlprograms.justmath.graph.fx.app;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graph.fx.controller.MainWindowController;
import com.mlprograms.justmath.graph.fx.model.GraphFxModel;
import com.mlprograms.justmath.graph.fx.view.GraphFxCss;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GraphFxApp extends Application {

    @Override
    public void start(final Stage stage) {
        final CalculatorEngine engine = new CalculatorEngine();

        final GraphFxModel model = new GraphFxModel();
        model.addFunction("f", "sin(x)+x^2");
        model.addFunction("g", "a*sin(b*x)+c");

        final MainWindowController controller = new MainWindowController(model, engine);

        final Scene scene = new Scene(controller.getRoot(), 1280, 820);
        final String css = GraphFxCss.stylesheet();
        if (css != null) scene.getStylesheets().add(css);

        stage.setTitle("JustMath - Graph (JavaFX)");
        stage.setScene(scene);
        stage.show();

         // Example (programmatic): open read-only window
         com.mlprograms.justmath.graph.fx.controller.GraphFxWindows.openDisplayWindow(
                 stage, engine,
                 java.util.List.of(
                         com.mlprograms.justmath.graph.fx.controller.GraphFxFunctionSpec.of("h", "cos(x)"),
                         com.mlprograms.justmath.graph.fx.controller.GraphFxFunctionSpec.of("p", "x^3-4*x")
                 ),
                 "Read-only graph"
         );
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
