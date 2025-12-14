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
import com.mlprograms.justmath.graphfx.controller.GraphFxDisplayOnlyController;
import com.mlprograms.justmath.graphfx.controller.GraphFxFunctionSpec;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.util.FxBootstrap;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * A display-only GraphFX application wrapper that shows a graph window without interactive editing features.
 * <p>
 * This class is a convenience entry point for embedding the GraphFX UI into other applications or
 * launching it with a predefined set of functions. It relies on the {@link GraphFxDisplay} base class
 * for stage creation and window configuration (title, width, height), but uses a
 * {@link GraphFxDisplayOnlyController} to render the graph in a read-only manner.
 * </p>
 * <p>
 * The many constructor overloads are designed to support different initialization scenarios:
 * supplying an external {@link CalculatorEngine}, providing a preconfigured {@link GraphFxModel},
 * customizing window properties, or using defaults for quick usage.
 * </p>
 * <p>
 * UI creation is performed via {@link FxBootstrap#callAndWait(Supplier)} to ensure
 * that stage construction happens on the JavaFX Application Thread and that the calling thread receives
 * the created {@link Stage} as a result.
 * </p>
 */
public class GraphFxDisplayOnlyApp extends GraphFxDisplay {

    /**
     * Creates a new display-only GraphFX application instance with fully customized dependencies and window settings.
     *
     * @param calculatorEngine the calculator engine used for parsing/evaluating expressions
     * @param model            the model backing the graph display
     * @param windowTitle      the window title to show in the stage decoration
     * @param width            the initial window width in pixels
     * @param height           the initial window height in pixels
     */
    public GraphFxDisplayOnlyApp(final CalculatorEngine calculatorEngine, final GraphFxModel model, final String windowTitle, final double width, final double height) {
        super(calculatorEngine, model, windowTitle, width, height);
    }

    /**
     * Creates a new display-only GraphFX application instance using default dimensions.
     *
     * @param calculatorEngine the calculator engine used for parsing/evaluating expressions
     * @param model            the model backing the graph display
     * @param windowTitle      the window title to show in the stage decoration
     * @throws NullPointerException if {@code calculatorEngine}, {@code model}, or {@code windowTitle} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model, @NonNull final String windowTitle) {
        this(calculatorEngine, model, windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new display-only GraphFX application instance using the default window title.
     *
     * @param calculatorEngine the calculator engine used for parsing/evaluating expressions
     * @param model            the model backing the graph display
     * @param width            the initial window width in pixels
     * @param height           the initial window height in pixels
     * @throws NullPointerException if {@code calculatorEngine} or {@code model} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model, final double width, final double height) {
        this(calculatorEngine, model, DEFAULT_WINDOW_TITLE, width, height);
    }

    /**
     * Creates a new display-only GraphFX application instance using default window title and dimensions.
     *
     * @param calculatorEngine the calculator engine used for parsing/evaluating expressions
     * @param model            the model backing the graph display
     * @throws NullPointerException if {@code calculatorEngine} or {@code model} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model) {
        this(calculatorEngine, model, DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new display-only GraphFX application instance with a new model and fully customized window settings.
     *
     * @param calculatorEngine the calculator engine used for parsing/evaluating expressions
     * @param windowTitle      the window title to show in the stage decoration
     * @param width            the initial window width in pixels
     * @param height           the initial window height in pixels
     * @throws NullPointerException if {@code calculatorEngine} or {@code windowTitle} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle, final double width, final double height) {
        this(calculatorEngine, new GraphFxModel(), windowTitle, width, height);
    }

    /**
     * Creates a new display-only GraphFX application instance with a new model and default dimensions.
     *
     * @param calculatorEngine the calculator engine used for parsing/evaluating expressions
     * @param windowTitle      the window title to show in the stage decoration
     * @throws NullPointerException if {@code calculatorEngine} or {@code windowTitle} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle) {
        this(calculatorEngine, new GraphFxModel(), windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new display-only GraphFX application instance with a new model and the default window title.
     *
     * @param calculatorEngine the calculator engine used for parsing/evaluating expressions
     * @param width            the initial window width in pixels
     * @param height           the initial window height in pixels
     * @throws NullPointerException if {@code calculatorEngine} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, final double width, final double height) {
        this(calculatorEngine, new GraphFxModel(), DEFAULT_WINDOW_TITLE, width, height);
    }

    /**
     * Creates a new display-only GraphFX application instance with a new model and default window settings.
     *
     * @param calculatorEngine the calculator engine used for parsing/evaluating expressions
     * @throws NullPointerException if {@code calculatorEngine} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine) {
        this(calculatorEngine, new GraphFxModel(), DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new display-only GraphFX application instance using a newly created calculator engine.
     *
     * @param model       the model backing the graph display
     * @param windowTitle the window title to show in the stage decoration
     * @param width       the initial window width in pixels
     * @param height      the initial window height in pixels
     * @throws NullPointerException if {@code model} or {@code windowTitle} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final GraphFxModel model, @NonNull final String windowTitle, final double width, final double height) {
        this(new CalculatorEngine(), model, windowTitle, width, height);
    }

    /**
     * Creates a new display-only GraphFX application instance using a newly created calculator engine
     * and default dimensions.
     *
     * @param model       the model backing the graph display
     * @param windowTitle the window title to show in the stage decoration
     * @throws NullPointerException if {@code model} or {@code windowTitle} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final GraphFxModel model, @NonNull final String windowTitle) {
        this(new CalculatorEngine(), model, windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new display-only GraphFX application instance using a newly created calculator engine
     * and the default window title.
     *
     * @param model  the model backing the graph display
     * @param width  the initial window width in pixels
     * @param height the initial window height in pixels
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final GraphFxModel model, final double width, final double height) {
        this(new CalculatorEngine(), model, DEFAULT_WINDOW_TITLE, width, height);
    }

    /**
     * Creates a new display-only GraphFX application instance using a newly created calculator engine
     * and default window settings.
     *
     * @param model the model backing the graph display
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final GraphFxModel model) {
        this(new CalculatorEngine(), model, DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new display-only GraphFX application instance using default dependencies and window settings.
     * <p>
     * This constructor creates a new {@link CalculatorEngine} and a new {@link GraphFxModel}.
     * </p>
     */
    public GraphFxDisplayOnlyApp() {
        this(new CalculatorEngine(), new GraphFxModel(), DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Shows the graph window without any predefined functions.
     * <p>
     * This method delegates to {@link #show(List)} with an empty function list.
     * </p>
     *
     * @return the created and shown {@link Stage}
     */
    public Stage show() {
        return show(List.of());
    }

    /**
     * Shows the graph window with the given list of functions.
     * <p>
     * The window and its controller are constructed on the JavaFX Application Thread using
     * {@link FxBootstrap#callAndWait(Supplier)}. The returned stage is
     * created via the base class stage builder.
     * </p>
     *
     * @param functions the functions to display initially
     * @return the created and shown {@link Stage}
     * @throws NullPointerException if {@code functions} is {@code null}
     */
    public Stage show(@NonNull final List<GraphFxFunctionSpec> functions) {
        return FxBootstrap.callAndWait(() -> {
            final GraphFxDisplayOnlyController controller = new GraphFxDisplayOnlyController(calculatorEngine, functions);
            return buildStage(controller);
        });
    }

}
