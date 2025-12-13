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
import com.mlprograms.justmath.graph.fx.controller.DisplayWindowController;
import com.mlprograms.justmath.graph.fx.controller.GraphFxFunctionSpec;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Convenience API for opening a read-only ("display-only") GraphFx window.
 * <p>
 * This class is meant for scenarios where you want to quickly show one or more functions
 * inside a separate window that only contains:
 * <ul>
 *     <li>The graph canvas</li>
 *     <li>The same tool buttons as in the main window (move/zoom/point/tangent/…)</li>
 *     <li>Export buttons (PNG/SVG/CSV/JSON)</li>
 * </ul>
 * The window intentionally does <b>not</b> contain any function management UI
 * (no add/remove, no function table, no variable table, no sliders).
 * <p>
 * <b>JavaFX Toolkit / Threading</b><br>
 * JavaFX UI objects must be created and modified on the JavaFX Application Thread.
 * When you call this utility from a plain {@code public static void main(String[] args)} method
 * (i.e. without calling {@code Application.launch(...)}), the JavaFX toolkit is not initialized yet.
 * In that case, calling {@link Platform#runLater(Runnable)} would fail with:
 * {@code IllegalStateException: Toolkit not initialized}.
 * <p>
 * To make this class usable both:
 * <ul>
 *     <li>from a normal {@code main()} method (non-JavaFX application)</li>
 *     <li>and from an already running JavaFX {@code Application}</li>
 * </ul>
 * it automatically initializes JavaFX (once) using {@link Platform#startup(Runnable)}
 * and then safely creates the {@link Stage} on the JavaFX thread.
 * <p>
 */
public final class GraphFxDisplayWindow {

    /**
     * Default window width used when not provided explicitly.
     */
    private static final double DEFAULT_WIDTH = 1100;

    /**
     * Default window height used when not provided explicitly.
     */
    private static final double DEFAULT_HEIGHT = 720;

    /**
     * Default window title used when the provided title is blank.
     */
    private static final String DEFAULT_WINDOW_TITLE = "JustMath - Graph Display";

    /**
     * Ensures JavaFX toolkit initialization happens only once.
     * <p>
     * Note: {@link Platform#startup(Runnable)} may throw {@link IllegalStateException}
     * when JavaFX is already running; this is safe to ignore.
     */
    private static final AtomicBoolean FX_INITIALIZED = new AtomicBoolean(false);

    /**
     * Calculator engine used to evaluate expressions (e.g., {@code sin(x)+x^2}).
     */
    private final CalculatorEngine calculatorEngine;

    /**
     * Title displayed in the window title bar.
     */
    private final String windowTitle;

    /**
     * Desired stage width.
     */
    private final double width;

    /**
     * Desired stage height.
     */
    private final double height;

    /**
     * Creates a display window using:
     * <ul>
     *     <li>a new {@link CalculatorEngine}</li>
     *     <li>the default title</li>
     *     <li>the default window size</li>
     * </ul>
     */
    public GraphFxDisplayWindow() {
        this(new CalculatorEngine(), DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a display window with a custom calculator engine and default size/title.
     *
     * @param calculatorEngine the engine used to evaluate expressions
     */
    public GraphFxDisplayWindow(@NonNull final CalculatorEngine calculatorEngine) {
        this(calculatorEngine, DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a display window with a custom title and default size/engine.
     *
     * @param windowTitle title shown in the window title bar
     */
    public GraphFxDisplayWindow(@NonNull final String windowTitle) {
        this(new CalculatorEngine(), windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a display window with custom calculator engine + title and default size.
     *
     * @param calculatorEngine the engine used to evaluate expressions
     * @param windowTitle      title shown in the window title bar
     */
    public GraphFxDisplayWindow(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle) {
        this(calculatorEngine, windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a display window with full customization.
     *
     * @param calculatorEngine the engine used to evaluate expressions
     * @param windowTitle      title shown in the window title bar
     * @param width            desired stage width in pixels
     * @param height           desired stage height in pixels
     */
    public GraphFxDisplayWindow(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle, final double width, final double height) {
        this.calculatorEngine = Objects.requireNonNull(calculatorEngine, "calculatorEngine");
        this.windowTitle = Objects.requireNonNull(windowTitle, "windowTitle");
        this.width = width;
        this.height = height;
    }

    /**
     * Opens (and shows) the read-only display window.
     * <p>
     * This method is safe to call:
     * <ul>
     *     <li>from the JavaFX Application Thread</li>
     *     <li>from any background thread</li>
     *     <li>from a plain {@code main()} method without {@code Application.launch(...)}</li>
     * </ul>
     * If JavaFX is not initialized yet, the toolkit will be started automatically.
     * <p>
     * If called from a non-JavaFX thread, the method blocks until the window is created and shown.
     *
     * @param functions list of functions to display; each entry defines name + expression (+ optional color)
     * @return the created and shown {@link Stage}
     * @throws NullPointerException if {@code functions} is {@code null}
     */
    public Stage show(@NonNull final List<GraphFxFunctionSpec> functions) {
        Objects.requireNonNull(functions, "functions cannot be null");
        initJavaFxIfNeeded();

        if (Platform.isFxApplicationThread()) {
            return createAndShowStage(functions);
        }

        final CompletableFuture<Stage> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                future.complete(createAndShowStage(functions));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future.join();
    }

    /**
     * Initializes the JavaFX runtime if it has not been initialized yet.
     * <p>
     * JavaFX applications normally initialize the toolkit via {@code Application.launch(...)}.
     * When this class is used from a plain {@code main()} method, JavaFX is not started automatically,
     * so we must ensure it is started before calling {@link Platform#runLater(Runnable)}.
     * <p>
     * This method is idempotent and safe to call multiple times.
     */
    private void initJavaFxIfNeeded() {
        if (FX_INITIALIZED.get()) {
            return;
        }

        synchronized (FX_INITIALIZED) {
            if (FX_INITIALIZED.get()) {
                return;
            }
            try {
                Platform.startup(() -> {
                });
            } catch (IllegalStateException ignored) {
            }
            FX_INITIALIZED.set(true);
        }
    }

    /**
     * Creates and shows the stage on the JavaFX Application Thread.
     * <p>
     * This method must only be called on the FX thread.
     *
     * @param functions functions to show
     * @return shown stage
     */
    private Stage createAndShowStage(final List<GraphFxFunctionSpec> functions) {
        final DisplayWindowController controller = new DisplayWindowController(calculatorEngine, functions);
        final Scene scene = new Scene(controller.getRoot(), width, height);
        final Stage stage = new Stage();

        stage.setTitle(windowTitle.isBlank() ? DEFAULT_WINDOW_TITLE : windowTitle);
        stage.setScene(scene);
        stage.show();

        return stage;
    }

}