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

package com.mlprograms.justmath.graph.fx.controller;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graph.fx.view.GraphFxCss;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Convenience helpers to open GraphFx windows.
 */
public final class GraphFxWindows {

    private GraphFxWindows() {}

    public static Stage openDisplayWindow(final Stage owner,
                                         final CalculatorEngine engine,
                                         final List<GraphFxFunctionSpec> functions,
                                         final String title) {

        final DisplayWindowController controller = new DisplayWindowController(engine, functions);

        final Scene scene = new Scene(controller.getRoot(), 1100, 720);
        final String css = GraphFxCss.stylesheet();
        if (css != null) scene.getStylesheets().add(css);

        final Stage stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.NONE);
        }
        stage.setTitle(title == null || title.isBlank() ? "JustMath - Graph (Display)" : title);
        stage.setScene(scene);
        stage.show();
        return stage;
    }
}
