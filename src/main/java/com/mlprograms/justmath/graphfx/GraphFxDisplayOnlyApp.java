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
import com.mlprograms.justmath.graphfx.controller.DisplayWindowController;
import com.mlprograms.justmath.graphfx.controller.GraphFxFunctionSpec;
import com.mlprograms.justmath.graphfx.util.FxBootstrap;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * Convenience API to open the read-only ("Nur-Anzeige") graph window.
 * <p>
 * This class is designed to be usable as a library entry point:
 * it can be called from a plain {@code public static void main(...)} method
 * without requiring {@code Application.launch(...)} or a JavaFX {@code Application} subclass.
 * <p>
 * Internally, JavaFX initialization and threading is handled by {@link FxBootstrap}.
 */
public final class GraphFxDisplayOnlyApp {

    private static final double DEFAULT_WIDTH = 1100;
    private static final double DEFAULT_HEIGHT = 720;
    private static final String DEFAULT_WINDOW_TITLE = "JustMath - Graph Display";

    private final CalculatorEngine calculatorEngine;
    private final String windowTitle;
    private final double width;
    private final double height;

    /**
     * Creates a display window using a new {@link CalculatorEngine}, default title and default size.
     */
    public GraphFxDisplayOnlyApp() {
        this(new CalculatorEngine(), DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a display window using the provided {@link CalculatorEngine}, default title and default size.
     *
     * @param calculatorEngine the engine used to evaluate expressions
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine) {
        this(calculatorEngine, DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a display window using a new {@link CalculatorEngine}, a custom title and default size.
     *
     * @param windowTitle the stage title; if blank, a default title will be used
     */
    public GraphFxDisplayOnlyApp(@NonNull final String windowTitle) {
        this(new CalculatorEngine(), windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a display window using the provided {@link CalculatorEngine}, a custom title and default size.
     *
     * @param calculatorEngine the engine used to evaluate expressions
     * @param windowTitle      the stage title; if blank, a default title will be used
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle) {
        this(calculatorEngine, windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a fully customized display window.
     *
     * @param calculatorEngine the engine used to evaluate expressions
     * @param windowTitle      the stage title; if blank, a default title will be used
     * @param width            desired stage width in pixels
     * @param height           desired stage height in pixels
     */
    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle, final double width, final double height) {
        this.calculatorEngine = Objects.requireNonNull(calculatorEngine, "calculatorEngine");
        this.windowTitle = Objects.requireNonNull(windowTitle, "windowTitle");
        this.width = width;
        this.height = height;
    }

    /**
     * Shows the read-only window and returns the created {@link Stage}.
     * <p>
     * This method works from:
     * <ul>
     *     <li>a plain {@code main()} method</li>
     *     <li>any background thread</li>
     *     <li>an already running JavaFX application</li>
     * </ul>
     * The JavaFX toolkit is initialized if required, and stage creation is executed on the FX thread.
     * <p>
     * If called from a non-FX thread, this call blocks until the window has been created and shown.
     *
     * @param functions functions to display
     * @return the created and shown stage
     * @throws NullPointerException if {@code functions} is {@code null}
     */
    public Stage show(@NonNull final List<GraphFxFunctionSpec> functions) {
        return FxBootstrap.callAndWait(() -> {
            final DisplayWindowController controller = new DisplayWindowController(calculatorEngine, functions);

            final Scene scene = new Scene(controller.getRoot(), width, height);
            final Stage stage = new Stage();

            stage.setTitle(windowTitle.isBlank() ? DEFAULT_WINDOW_TITLE : windowTitle);
            stage.setScene(scene);
            stage.show();

            return stage;
        });
    }

}
