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
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.NonNull;

/**
 * Convenience API for opening the <b>editable</b> GraphFx main window as a library consumer.
 * <p>
 * This class exists to make GraphFx easy to use from <i>any</i> Java program, including applications
 * that are <b>not</b> structured as a JavaFX {@link javafx.application.Application}.
 * Instead of requiring callers to:
 * <ul>
 *     <li>create an {@code Application} subclass,</li>
 *     <li>call {@code Application.launch(...)},</li>
 *     <li>or configure IDE run settings for JavaFX module-path / runtime components,</li>
 * </ul>
 * a user can simply instantiate this class and call {@link #show()}.
 *
 * <h2>What window does this open?</h2>
 * The window created by this class is the "main window" of GraphFx, i.e. the editable UI that includes:
 * <ul>
 *     <li>Graph canvas (rendered coordinate system and drawn functions)</li>
 *     <li>Function management UI (add/edit/remove functions)</li>
 *     <li>Variable management UI (add/edit/remove variables)</li>
 *     <li>Slider panel (interactive variable sliders)</li>
 *     <li>Toolbar with tools and export options</li>
 * </ul>
 *
 * <h2>JavaFX lifecycle and threading</h2>
 * JavaFX enforces that all UI objects are created and modified on the <b>JavaFX Application Thread</b>.
 * If this library is used from a plain {@code public static void main(String[] args)} method,
 * then the JavaFX toolkit is typically not initialized yet.
 * <p>
 * This class relies on {@link FxBootstrap} to:
 * <ul>
 *     <li>initialize the JavaFX toolkit exactly once, if necessary</li>
 *     <li>run the window creation code on the JavaFX Application Thread</li>
 *     <li>optionally block the caller until the {@link Stage} is created and shown</li>
 * </ul>
 *
 * <h2>Blocking behavior</h2>
 * {@link #show()} returns the created {@link Stage}. If {@code show()} is invoked from a non-JavaFX thread,
 * it will block until the stage has been constructed and shown (internally using
 * {@code FxBootstrap.callAndWait(...)}). If called from the JavaFX Application Thread, it runs immediately.
 *
 * <h2>Model ownership</h2>
 * The {@link GraphFxModel} provided to the constructors is used as the data model for the UI.
 * Changes performed through the UI will directly modify that model instance.
 * <p>
 * This allows library users to:
 * <ul>
 *     <li>pre-populate the model before opening the window</li>
 *     <li>inspect and reuse the model after the window has been shown</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * public class Main {
 *     public static void main(String[] args) {
 *         GraphFxModel model = new GraphFxModel();
 *         model.addFunction("f(x)", "x^2");
 *
 *         new GraphFxMainWindow(model).show();
 *     }
 * }
 * }</pre>
 *
 * <p><b>Note:</b> This class only creates and shows a {@link Stage}. If you want to control application shutdown
 * (e.g. exit when all windows close), configure that via {@link javafx.application.Platform#setImplicitExit(boolean)}
 * in your bootstrap logic, or add explicit shutdown handling in your host application.</p>
 */
public final class GraphFxApp {

    /**
     * Default window width used when not provided explicitly.
     */
    private static final double DEFAULT_WIDTH = 1280;

    /**
     * Default window height used when not provided explicitly.
     */
    private static final double DEFAULT_HEIGHT = 820;

    /**
     * Default stage title used when the provided title is blank.
     */
    private static final String DEFAULT_TITLE = "JustMath - Graph Display";

    /**
     * The calculator engine used to evaluate expressions in the graph (e.g. {@code sin(x) + x^2}).
     * <p>
     * This engine is passed to the {@link MainWindowController} and is responsible for parsing/evaluating
     * the expression language supported by JustMath.
     */
    private final CalculatorEngine calculatorEngine;

    /**
     * The model used as the data source for functions, variables, settings, and objects.
     * <p>
     * This model instance is directly modified by UI interactions in the main window.
     */
    private final GraphFxModel model;

    /**
     * Title displayed in the window title bar.
     */
    private final String windowTitle;

    /**
     * Desired stage width in pixels.
     */
    private final double width;

    /**
     * Desired stage height in pixels.
     */
    private final double height;

    /**
     * Creates a {@code GraphFxMainWindow} with:
     * <ul>
     *     <li>a new {@link CalculatorEngine}</li>
     *     <li>a new {@link GraphFxModel}</li>
     *     <li>default title and size</li>
     * </ul>
     * <p>
     * This is the simplest entry point for opening the editable main window.
     */
    public GraphFxApp() {
        this(new CalculatorEngine(), new GraphFxModel(), DEFAULT_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a {@code GraphFxMainWindow} with a custom title and default engine/model/size.
     *
     * @param windowTitle the stage title; if blank, {@link #DEFAULT_TITLE} is used
     * @throws NullPointerException if {@code windowTitle} is {@code null}
     */
    public GraphFxApp(@NonNull final String windowTitle) {
        this(new CalculatorEngine(), new GraphFxModel(), windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a {@code GraphFxMainWindow} that uses the provided model and default engine/title/size.
     * <p>
     * This constructor is useful if you want to pre-configure the model (e.g. add functions)
     * before showing the UI.
     *
     * @param model the model instance to use as data source for the window
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public GraphFxApp(@NonNull final GraphFxModel model) {
        this(new CalculatorEngine(), model, DEFAULT_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a {@code GraphFxMainWindow} that uses the provided engine and model with default title/size.
     *
     * @param calculatorEngine expression evaluation engine
     * @param model            model instance used by the UI
     * @throws NullPointerException if any parameter is {@code null}
     */
    public GraphFxApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model) {
        this(calculatorEngine, model, DEFAULT_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a fully customized {@code GraphFxMainWindow}.
     *
     * @param calculatorEngine expression evaluation engine
     * @param model            model instance used by the UI
     * @param windowTitle      stage title; if blank, {@link #DEFAULT_TITLE} is used
     * @param width            desired window width in pixels
     * @param height           desired window height in pixels
     * @throws NullPointerException if {@code calculatorEngine}, {@code model}, or {@code windowTitle} is {@code null}
     */
    public GraphFxApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model, @NonNull final String windowTitle, final double width, final double height) {
        this.calculatorEngine = calculatorEngine;
        this.model = model;
        this.windowTitle = windowTitle;
        this.width = width;
        this.height = height;
    }

    /**
     * Creates and shows the editable GraphFx main window.
     * <p>
     * This method is safe to call from:
     * <ul>
     *     <li>a plain {@code main()} method (no {@code Application.launch(...)})</li>
     *     <li>any background thread</li>
     *     <li>an already running JavaFX application</li>
     * </ul>
     * The JavaFX toolkit is initialized if required, and the window creation runs on the JavaFX thread.
     * <p>
     * If invoked from a non-JavaFX thread, this call blocks until the window is created and shown.
     *
     * @return the created and shown {@link Stage}
     */
    public Stage show() {
        return FxBootstrap.callAndWait(() -> {
            final MainWindowController controller = new MainWindowController(model, calculatorEngine);

            final Scene scene = new Scene(controller.getRoot(), width, height);
            final Stage stage = new Stage();

            stage.setTitle(windowTitle.isBlank() ? DEFAULT_TITLE : windowTitle);
            stage.setScene(scene);
            stage.show();
            return stage;
        });
    }

}
