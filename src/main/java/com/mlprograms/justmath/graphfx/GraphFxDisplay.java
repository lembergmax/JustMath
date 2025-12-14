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

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphfx.controller.GraphFxAppController;
import com.mlprograms.justmath.graphfx.controller.GraphFxController;
import com.mlprograms.justmath.graphfx.controller.GraphFxDisplayOnlyController;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import javafx.scene.Scene;
import javafx.stage.Stage;

class GraphFxDisplay {

    /**
     * Default window width used when not provided explicitly.
     */
    protected static final double DEFAULT_WIDTH = 1280;

    /**
     * Default window height used when not provided explicitly.
     */
    protected static final double DEFAULT_HEIGHT = 820;

    /**
     * Default stage title used when the provided title is blank.
     */
    protected static final String DEFAULT_WINDOW_TITLE = "JustMath - Graph Display";

    /**
     * The calculator engine used to evaluate expressions in the graph (e.g. {@code sin(x) + x^2}).
     * <p>
     * This engine is passed to the {@link GraphFxAppController} and is responsible for parsing/evaluating
     * the expression language supported by JustMath.
     */
    protected final CalculatorEngine calculatorEngine;

    /**
     * The model used as the data source for functions, variables, settings, and objects.
     * <p>
     * This model instance is directly modified by UI interactions in the main window.
     */
    protected final GraphFxModel model;

    /**
     * Title displayed in the window title bar.
     */
    protected final String windowTitle;

    /**
     * Desired stage width in pixels.
     */
    protected final double width;

    /**
     * Desired stage height in pixels.
     */
    protected final double height;

    public GraphFxDisplay(CalculatorEngine calculatorEngine, GraphFxModel model, String windowTitle, double width, double height) {
        this.calculatorEngine = calculatorEngine;
        this.model = model;
        this.windowTitle = windowTitle == null || windowTitle.isBlank() ? DEFAULT_WINDOW_TITLE : windowTitle;
        this.width = width;
        this.height = height;
    }

    protected <T extends GraphFxController> Stage buildStage(final T controller) {
        final Scene scene = new Scene(controller.getRoot(), width, height);
        final Stage stage = new Stage();

        stage.setTitle(windowTitle.isBlank() ? DEFAULT_WINDOW_TITLE : windowTitle);
        stage.setScene(scene);
        stage.show();

        return stage;
    }

}
