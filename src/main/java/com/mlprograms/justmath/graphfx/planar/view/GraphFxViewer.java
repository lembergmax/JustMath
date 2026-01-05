/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package com.mlprograms.justmath.graphfx.planar.view;

import com.mlprograms.justmath.graphfx.JavaFxRuntime;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;

/**
 * A minimal JavaFX viewer window that can be used like a library component:
 *
 * <pre>{@code
 * GraphFxPanZoomViewer viewer = new GraphFxPanZoomViewer();
 * viewer.show();
 * }</pre>
 *
 * <h2>Multiple windows</h2>
 * <p>Each instance manages its own {@link Stage} and its own {@link GridPane}. Pan/zoom state is independent.
 * All windows share the same JavaFX toolkit (JVM-global), which is started lazily.</p>
 *
 * <h2>Program exit</h2>
 * <p>By default, the program exits when the <i>last</i> viewer window is closed (via {@code X} button or
 * {@link #close()}). The exit is executed deferred to avoid re-entrancy issues during JavaFX close processing.</p>
 */
public final class GraphFxViewer {

    @Getter
    private final WindowConfig windowConfig;
    @Getter
    private final GridPane content;

    private Stage stage;

    private boolean closeWasRequested;
    private boolean trackedByExitPolicy;

    /**
     * Creates a viewer with a default configuration.
     */
    public GraphFxViewer() {
        this(WindowConfig.defaultConfig());
    }

    /**
     * Creates a viewer with a custom title and default size.
     *
     * @param title the window title (must not be {@code null} or blank)
     */
    public GraphFxViewer(final String title) {
        this(new WindowConfig(title, WindowConfig.DEFAULT_WIDTH, WindowConfig.DEFAULT_HEIGHT, true, true));
    }

    /**
     * Creates a viewer with a given configuration.
     *
     * @param windowConfig the immutable window configuration (must not be {@code null})
     */
    public GraphFxViewer(final WindowConfig windowConfig) {
        this.windowConfig = Objects.requireNonNull(windowConfig, "windowConfig must not be null");
        this.content = new GridPane();
    }

    /**
     * Shows this viewer window.
     *
     * <p>The JavaFX runtime is started lazily on the first call. If the window does not exist yet,
     * it is created and shown.</p>
     */
    public void show() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::showOnFxThread);
    }

    /**
     * Hides this viewer window if it exists.
     *
     * <p>Hiding does not count as "closed" for the exit policy, so the program will not exit.</p>
     */
    public void hide() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::hideOnFxThread);
    }

    /**
     * Closes this viewer window if it exists.
     *
     * <p>If this is the last tracked viewer window and the exit policy is enabled, the program exits.</p>
     */
    public void close() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::closeOnFxThread);
    }

    /**
     * Shows the stage on the JavaFX Application Thread.
     */
    private void showOnFxThread() {
        if (stage == null) {
            stage = createStage();
        }

        if (!stage.isShowing()) {
            stage.show();
        }

        stage.toFront();
        stage.requestFocus();
    }

    /**
     * Hides the stage on the JavaFX Application Thread.
     */
    private void hideOnFxThread() {
        if (stage == null) {
            return;
        }

        stage.hide();
    }

    /**
     * Closes the stage on the JavaFX Application Thread.
     */
    private void closeOnFxThread() {
        if (stage == null) {
            return;
        }

        closeWasRequested = true;
        stage.close();
    }

    /**
     * Creates and configures the stage for this viewer instance.
     *
     * @return a fully configured stage (never {@code null})
     */
    private Stage createStage() {
        final Stage newStage = new Stage();
        newStage.setTitle(windowConfig.title());

        final BorderPane root = new BorderPane(content);
        final Scene scene = new Scene(root, windowConfig.width(), windowConfig.height());
        newStage.setScene(scene);

        installExitPolicyHooksIfEnabled(newStage);

        return newStage;
    }

    /**
     * Installs hooks that implement the "exit application on last viewer close" behavior.
     *
     * <p>Important: The actual {@code Platform.exit()} call is performed deferred (queued) by {@link JavaFxRuntime}
     * to avoid re-entrancy during JavaFX close/hide processing.</p>
     *
     * @param stage the stage to configure (must not be {@code null})
     */
    private void installExitPolicyHooksIfEnabled(final Stage stage) {
        Objects.requireNonNull(stage, "stage must not be null");

        if (!windowConfig.exitApplicationOnLastViewerClose()) {
            return;
        }

        if (!trackedByExitPolicy) {
            JavaFxRuntime.registerTrackedViewerOpened();
            trackedByExitPolicy = true;
        }

        stage.setOnCloseRequest(event -> closeWasRequested = true);

        stage.setOnHidden(event -> {
            final boolean treatAsClose = closeWasRequested;
            closeWasRequested = false;

            if (!treatAsClose) {
                return; // hidden via hide() -> do not exit
            }

            this.stage = null;

            if (trackedByExitPolicy) {
                trackedByExitPolicy = false;
                JavaFxRuntime.registerTrackedViewerClosedAndExitIfLast();
            }
        });
    }

    /**
     * Immutable configuration for {@link GraphFxViewer}.
     *
     * @param title                            window title (must not be {@code null} or blank)
     * @param width                            initial width in pixels (must be {@code > 0})
     * @param height                           initial height in pixels (must be {@code > 0})
     * @param drawDemoCurve                    reserved for future demo overlays
     * @param exitApplicationOnLastViewerClose whether to exit when the last viewer window is closed
     */
    public record WindowConfig(String title, int width, int height, boolean drawDemoCurve,
                               boolean exitApplicationOnLastViewerClose) {

        /**
         * Default width used by {@link #defaultConfig()}.
         */
        public static final int DEFAULT_WIDTH = 1200;

        /**
         * Default height used by {@link #defaultConfig()}.
         */
        public static final int DEFAULT_HEIGHT = 800;

        /**
         * Validates and creates a configuration instance.
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
         * Returns a default configuration.
         *
         * <p>By default, the application exits when the last viewer window is closed.</p>
         *
         * @return default configuration (never {@code null})
         */
        public static WindowConfig defaultConfig() {
            return new WindowConfig("GraphFx – Pan & Zoom", DEFAULT_WIDTH, DEFAULT_HEIGHT, true, true);
        }
    }

}
