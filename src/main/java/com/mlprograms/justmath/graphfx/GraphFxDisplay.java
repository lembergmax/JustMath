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
import com.mlprograms.justmath.graphfx.controller.GraphFxController;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Base class used by all GraphFX application wrappers to create and show JavaFX stages.
 * <p>
 * This class is intentionally lightweight: it holds the common window configuration
 * (title, size) and core dependencies ({@link CalculatorEngine} and {@link GraphFxModel})
 * and provides a single {@link #buildStage(GraphFxController)} method that turns a
 * {@link GraphFxController} into a visible {@link Stage}.
 * </p>
 * <p>
 * Subclasses are responsible for instantiating concrete controllers and invoking
 * {@link #buildStage(GraphFxController)} from the JavaFX Application Thread. In this
 * project, that responsibility is handled via {@code FxBootstrap} helpers.
 * </p>
 */
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
     * The calculator engine used to evaluate expressions in the graph (for example {@code sin(x) + x^2}).
     * <p>
     * Subclasses pass this engine to their controllers so that expression evaluation is consistent
     * across all GraphFX windows.
     * </p>
     */
    protected final CalculatorEngine calculatorEngine;

    /**
     * The model used as the data source for functions, variables, settings and graphical objects.
     */
    protected final GraphFxModel model;

    /**
     * Title text displayed in the window title bar.
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

    /**
     * Creates a new display configuration with the given engine, model and window properties.
     * <p>
     * The provided {@code windowTitle} is normalized so that a blank or {@code null} title
     * is replaced with {@link #DEFAULT_WINDOW_TITLE}.
     * </p>
     *
     * @param calculatorEngine the calculator engine to be used by controllers
     * @param model            the model backing the graph window
     * @param windowTitle      the requested window title; may be blank to use the default
     * @param width            the initial stage width in pixels
     * @param height           the initial stage height in pixels
     */
    public GraphFxDisplay(final CalculatorEngine calculatorEngine, final GraphFxModel model, final String windowTitle, final double width, final double height) {
        this.calculatorEngine = calculatorEngine;
        this.model = model;
        this.windowTitle = windowTitle == null || windowTitle.isBlank() ? DEFAULT_WINDOW_TITLE : windowTitle;
        this.width = width;
        this.height = height;
    }

    /**
     * Creates, configures and shows a new {@link Stage} for the given controller.
     * <p>
     * The controller must provide a concrete JavaFX root node via
     * {@link GraphFxController#getRoot()}. That root node is wrapped into a {@link Scene}
     * with the configured width and height. The stage's title is derived from
     * {@link #windowTitle} falling back to {@link #DEFAULT_WINDOW_TITLE} when blank.
     * </p>
     * <p>
     * This method must be called on the JavaFX Application Thread.
     * </p>
     *
     * @param controller the controller whose root node should be displayed
     * @param <T>        the concrete controller type
     * @return the created and already shown {@link Stage}
     * @throws NullPointerException if {@code controller} or its root node is {@code null}
     */
    protected <T extends GraphFxController> Stage buildStage(final T controller) {
        final Region root = (Region) controller.getRoot();
        final Scene scene = new Scene(root, width, height);
        final Stage stage = new Stage();

        stage.setTitle(windowTitle == null || windowTitle.isBlank() ? DEFAULT_WINDOW_TITLE : windowTitle);
        stage.setScene(scene);
        stage.show();

        return stage;
    }

}
