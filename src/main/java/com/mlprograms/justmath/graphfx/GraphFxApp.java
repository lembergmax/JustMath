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
import com.mlprograms.justmath.graphfx.controller.GraphFxFunctionSpec;
import com.mlprograms.justmath.graphfx.model.GraphFxFunction;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.util.FxBootstrap;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class GraphFxApp extends GraphFxDisplay {

    /**
     * Creates a new editable GraphFX application instance with fully customized dependencies and window settings.
     * <p>
     * This constructor is the most flexible entry point: callers can supply an existing
     * {@link CalculatorEngine} and a preconfigured {@link GraphFxModel} instance, as well as explicitly
     * control the window title and initial dimensions.
     * </p>
     *
     * @param calculatorEngine the calculator engine used to parse and evaluate expressions
     * @param model            the model backing the graph window (functions, variables, settings, objects)
     * @param windowTitle      the title to display in the window decoration
     * @param width            the initial window width in pixels
     * @param height           the initial window height in pixels
     */
    public GraphFxApp(final CalculatorEngine calculatorEngine, final GraphFxModel model, final String windowTitle, final double width, final double height) {
        super(calculatorEngine, model, windowTitle, width, height);
    }

    /**
     * Creates a new editable GraphFX application instance using default dimensions.
     *
     * @param calculatorEngine the calculator engine used to parse and evaluate expressions
     * @param model            the model backing the graph window
     * @param windowTitle      the title to display in the window decoration
     * @throws NullPointerException if {@code calculatorEngine}, {@code model} or {@code windowTitle} is {@code null}
     */
    public GraphFxApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model, @NonNull final String windowTitle) {
        this(calculatorEngine, model, windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new editable GraphFX application instance using the default window title.
     *
     * @param calculatorEngine the calculator engine used to parse and evaluate expressions
     * @param model            the model backing the graph window
     * @param width            the initial window width in pixels
     * @param height           the initial window height in pixels
     * @throws NullPointerException if {@code calculatorEngine} or {@code model} is {@code null}
     */
    public GraphFxApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model, final double width, final double height) {
        this(calculatorEngine, model, DEFAULT_WINDOW_TITLE, width, height);
    }

    /**
     * Creates a new editable GraphFX application instance using default window title and dimensions.
     *
     * @param calculatorEngine the calculator engine used to parse and evaluate expressions
     * @param model            the model backing the graph window
     * @throws NullPointerException if {@code calculatorEngine} or {@code model} is {@code null}
     */
    public GraphFxApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model) {
        this(calculatorEngine, model, DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new editable GraphFX application instance with a new model and fully customized window settings.
     *
     * @param calculatorEngine the calculator engine used to parse and evaluate expressions
     * @param windowTitle      the title to display in the window decoration
     * @param width            the initial window width in pixels
     * @param height           the initial window height in pixels
     * @throws NullPointerException if {@code calculatorEngine} or {@code windowTitle} is {@code null}
     */
    public GraphFxApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle, final double width, final double height) {
        this(calculatorEngine, new GraphFxModel(), windowTitle, width, height);
    }

    /**
     * Creates a new editable GraphFX application instance with a new model and default dimensions.
     *
     * @param calculatorEngine the calculator engine used to parse and evaluate expressions
     * @param windowTitle      the title to display in the window decoration
     * @throws NullPointerException if {@code calculatorEngine} or {@code windowTitle} is {@code null}
     */
    public GraphFxApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle) {
        this(calculatorEngine, new GraphFxModel(), windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new editable GraphFX application instance with a new model and the default window title.
     *
     * @param calculatorEngine the calculator engine used to parse and evaluate expressions
     * @param width            the initial window width in pixels
     * @param height           the initial window height in pixels
     * @throws NullPointerException if {@code calculatorEngine} is {@code null}
     */
    public GraphFxApp(@NonNull final CalculatorEngine calculatorEngine, final double width, final double height) {
        this(calculatorEngine, new GraphFxModel(), DEFAULT_WINDOW_TITLE, width, height);
    }

    /**
     * Creates a new editable GraphFX application instance with a new model and default window settings.
     *
     * @param calculatorEngine the calculator engine used to parse and evaluate expressions
     * @throws NullPointerException if {@code calculatorEngine} is {@code null}
     */
    public GraphFxApp(@NonNull final CalculatorEngine calculatorEngine) {
        this(calculatorEngine, new GraphFxModel(), DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new editable GraphFX application instance using a newly created calculator engine.
     *
     * @param model       the model backing the graph window
     * @param windowTitle the title to display in the window decoration
     * @param width       the initial window width in pixels
     * @param height      the initial window height in pixels
     * @throws NullPointerException if {@code model} or {@code windowTitle} is {@code null}
     */
    public GraphFxApp(@NonNull final GraphFxModel model, @NonNull final String windowTitle, final double width, final double height) {
        this(new CalculatorEngine(), model, windowTitle, width, height);
    }

    /**
     * Creates a new editable GraphFX application instance using a newly created calculator engine
     * and default dimensions.
     *
     * @param model       the model backing the graph window
     * @param windowTitle the title to display in the window decoration
     * @throws NullPointerException if {@code model} or {@code windowTitle} is {@code null}
     */
    public GraphFxApp(@NonNull final GraphFxModel model, @NonNull final String windowTitle) {
        this(new CalculatorEngine(), model, windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new editable GraphFX application instance using a newly created calculator engine
     * and the default window title.
     *
     * @param model  the model backing the graph window
     * @param width  the initial window width in pixels
     * @param height the initial window height in pixels
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public GraphFxApp(@NonNull final GraphFxModel model, final double width, final double height) {
        this(new CalculatorEngine(), model, DEFAULT_WINDOW_TITLE, width, height);
    }

    /**
     * Creates a new editable GraphFX application instance using default dependencies and window settings.
     * <p>
     * This constructor creates a new {@link CalculatorEngine} and a new {@link GraphFxModel}.
     * </p>
     */
    public GraphFxApp() {
        this(new CalculatorEngine(), new GraphFxModel(), DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Shows the main editable graph window and pre-populates it with the given functions.
     * <p>
     * UI creation and controller construction are performed on the JavaFX Application Thread
     * using {@link FxBootstrap#callAndWait(java.util.function.Supplier)}. For each
     * {@link GraphFxFunctionSpec}, a corresponding {@code GraphFxFunction} is added to the model
     * prior to creating the {@link GraphFxAppController}.
     * </p>
     *
     * @return the created and shown {@link Stage}
     * @throws NullPointerException if {@code functions} is {@code null}
     */
    public Stage show() {
        return FxBootstrap.callAndWait(() -> {
            final GraphFxAppController controller = new GraphFxAppController(model, calculatorEngine);
            return buildStage(controller);
        });
    }

}
