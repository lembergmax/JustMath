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

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;

/**
 * A minimal JavaFX window that can be opened in a "library-like" way:
 *
 * <pre>{@code
 * GraphFxPanZoomViewer viewer = new GraphFxPanZoomViewer();
 * viewer.show();
 * }</pre>
 *
 * <p>This class does not require an {@code Application} subclass. It starts the JavaFX runtime lazily
 * on the first call to {@link #show()} and then opens a {@link Stage} with a pan/zoom capable
 * coordinate grid.</p>
 *
 * <h2>Threading</h2>
 * <p>All public methods are safe to call from any thread. UI work is executed on the JavaFX Application Thread.</p>
 *
 * <h2>Lifecycle</h2>
 * <p>JavaFX can only be initialized once per JVM. This viewer handles that internally via
 * {@link JavaFxRuntime#ensureStarted()}.</p>
 */
public final class GraphFxPanZoomViewer {

    @Getter
    private final WindowConfig windowConfig;
    @Getter
    private final PanZoomGridPane content;

    private volatile Stage stage;

    /**
     * Creates a viewer with default configuration (title, size, demo curve enabled).
     */
    public GraphFxPanZoomViewer() {
        this(WindowConfig.defaultConfig());
    }

    /**
     * Creates a viewer with the given configuration.
     *
     * @param windowConfig immutable window configuration (must not be {@code null})
     */
    public GraphFxPanZoomViewer(final WindowConfig windowConfig) {
        this.windowConfig = Objects.requireNonNull(windowConfig, "windowConfig must not be null");

        this.content = new PanZoomGridPane();
    }

    /**
     * Shows the viewer window.
     *
     * <p>If the window does not exist yet, it is created lazily. If it already exists,
     * it is brought to the foreground.</p>
     */
    public void show() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::showOnFxThread);
    }

    /**
     * Hides the viewer window if it exists.
     */
    public void hide() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::hideOnFxThread);
    }

    /**
     * Closes the viewer window and releases the internal {@link Stage} reference.
     *
     * <p>This does not shut down the JavaFX runtime because it is JVM-global and might be used
     * by other components within the same process.</p>
     */
    public void close() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::closeOnFxThread);
    }

    /**
     * Creates the stage (if required) and shows it.
     *
     * <p>This method must only be called on the JavaFX Application Thread.</p>
     */
    private void showOnFxThread() {
        if (stage == null) {
            stage = createStage();
        }

        if (stage.isShowing()) {
            stage.toFront();
            stage.requestFocus();
            return;
        }

        stage.show();
        stage.toFront();
        stage.requestFocus();
    }

    /**
     * Hides the stage if it exists.
     *
     * <p>This method must only be called on the JavaFX Application Thread.</p>
     */
    private void hideOnFxThread() {
        final Stage currentStage = stage;
        if (currentStage == null) {
            return;
        }

        currentStage.hide();
    }

    /**
     * Closes the stage if it exists and clears the reference.
     *
     * <p>This method must only be called on the JavaFX Application Thread.</p>
     */
    private void closeOnFxThread() {
        final Stage currentStage = stage;
        if (currentStage == null) {
            return;
        }

        currentStage.close();
        stage = null;
    }

    /**
     * Creates a fully configured JavaFX stage for this viewer.
     *
     * <p>This method must only be called on the JavaFX Application Thread.</p>
     *
     * @return a new stage instance (never {@code null})
     */
    private Stage createStage() {
        final Stage newStage = new Stage();
        newStage.setTitle(windowConfig.title());

        final BorderPane root = new BorderPane(content);
        final Scene scene = new Scene(root, windowConfig.width(), windowConfig.height());

        newStage.setScene(scene);
        return newStage;
    }

    /**
     * Immutable configuration for {@link GraphFxPanZoomViewer}.
     *
     * @param title         window title (must not be {@code null} or blank)
     * @param width         initial window width in pixels (must be {@code > 0})
     * @param height        initial window height in pixels (must be {@code > 0})
     * @param drawDemoCurve whether a demo curve (sin(x)) is drawn on top of the grid
     */
    public record WindowConfig(String title, int width, int height, boolean drawDemoCurve) {

        /**
         * Default width used by {@link #defaultConfig()}.
         */
        public static final int DEFAULT_WIDTH = 1200;

        /**
         * Default height used by {@link #defaultConfig()}.
         */
        public static final int DEFAULT_HEIGHT = 800;

        /**
         * Creates a validated configuration record.
         *
         * @param title         window title (must not be {@code null} or blank)
         * @param width         initial window width in pixels (must be {@code > 0})
         * @param height        initial window height in pixels (must be {@code > 0})
         * @param drawDemoCurve whether a demo curve is drawn
         */
        public WindowConfig {
            Objects.requireNonNull(title, "title must not be null");

            if (title.isBlank()) {
                throw new IllegalArgumentException("title must not be blank");
            }
            if (width <= 0) {
                throw new IllegalArgumentException("width must be > 0");
            }
            if (height <= 0) {
                throw new IllegalArgumentException("height must be > 0");
            }
        }

        /**
         * Returns a sensible default configuration.
         *
         * @return default configuration (never {@code null})
         */
        public static WindowConfig defaultConfig() {
            return new WindowConfig("GraphFx – Pan & Zoom", DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
        }
    }

}
