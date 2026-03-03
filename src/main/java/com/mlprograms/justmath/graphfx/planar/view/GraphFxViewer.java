/*
 * Copyright (c) 2026 Max Lemberg
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
import com.mlprograms.justmath.graphfx.WindowConfig;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;

/**
 * Public entry point for the planar GraphFx GUI.
 *
 * <p>
 * This viewer is intentionally GUI-only: it accepts precomputed {@link PlotResult} instances and renders them.
 * Any calculation (expression evaluation, marching squares, sampling) belongs to another module.
 * </p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 * GraphFxViewer viewer = new GraphFxViewer();
 * viewer.show();
 *
 * PlotResult result = new PlotResult(...);
 * viewer.setPlotResult(result);
 * }</pre>
 */
public final class GraphFxViewer {

    @Getter
    private final WindowConfig windowConfig;

    @Getter
    private GraphFxViewerStyle style;

    @Getter
    private GraphFxViewConfiguration viewConfiguration;

    private final GraphFxPlotSurface plotSurface;

    private Stage stage;

    private boolean closeWasRequested;
    private boolean trackedByExitPolicy;

    public GraphFxViewer() {
        this(WindowConfig.defaultConfig(), GraphFxViewerStyle.builder().build(), GraphFxViewConfiguration.builder().build());
    }

    public GraphFxViewer(final WindowConfig windowConfig) {
        this(windowConfig, GraphFxViewerStyle.builder().build(), GraphFxViewConfiguration.builder().build());
    }

    public GraphFxViewer(
            final WindowConfig windowConfig,
            final GraphFxViewerStyle style,
            final GraphFxViewConfiguration viewConfiguration
    ) {
        this.windowConfig = Objects.requireNonNull(windowConfig, "windowConfig must not be null");
        this.style = Objects.requireNonNull(style, "style must not be null");
        this.viewConfiguration = Objects.requireNonNull(viewConfiguration, "viewConfiguration must not be null");

        this.plotSurface = new GraphFxPlotSurface(this.style, this.viewConfiguration);
    }

    /**
     * Shows the viewer window (creates it lazily).
     */
    public void show() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::showOnFxThread);
    }

    /**
     * Hides the viewer window (if open).
     */
    public void hide() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::hideOnFxThread);
    }

    /**
     * Closes the viewer window (if open).
     */
    public void close() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::closeOnFxThread);
    }

    /**
     * Updates the rendered plot data.
     *
     * @param plotResult plot data (must not be null)
     */
    public void setPlotResult(final PlotResult plotResult) {
        Objects.requireNonNull(plotResult, "plotResult must not be null");
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(() -> plotSurface.setPlotResult(plotResult));
    }

    /**
     * Clears all plot data.
     */
    public void clearPlot() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(plotSurface::clearPlot);
    }

    /**
     * Fits the viewport to the given world bounds.
     *
     * @param viewportSnapshot world bounds (must not be null)
     */
    public void fitViewport(final ViewportSnapshot viewportSnapshot) {
        Objects.requireNonNull(viewportSnapshot, "viewportSnapshot must not be null");
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(() -> plotSurface.fitViewport(viewportSnapshot));
    }

    /**
     * @return current visible world bounds snapshot
     */
    public ViewportSnapshot snapshotViewport() {
        JavaFxRuntime.ensureStarted();
        final ViewportSnapshot[] snapshot = new ViewportSnapshot[1];
        JavaFxRuntime.runOnFxThreadAndWait(() -> snapshot[0] = plotSurface.snapshotViewport());
        return snapshot[0];
    }

    /**
     * Updates viewer style and re-renders.
     *
     * @param style new style (must not be null)
     */
    public void setStyle(final GraphFxViewerStyle style) {
        Objects.requireNonNull(style, "style must not be null");
        this.style = style;

        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(() -> plotSurface.setStyle(style));
    }

    /**
     * Updates view configuration (grid/interaction) and re-renders.
     *
     * @param viewConfiguration new view configuration (must not be null)
     */
    public void setViewConfiguration(final GraphFxViewConfiguration viewConfiguration) {
        Objects.requireNonNull(viewConfiguration, "viewConfiguration must not be null");
        this.viewConfiguration = viewConfiguration;

        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(() -> plotSurface.setViewConfiguration(viewConfiguration));
    }

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

    private void hideOnFxThread() {
        if (stage == null) {
            return;
        }
        stage.hide();
    }

    private void closeOnFxThread() {
        if (stage == null) {
            return;
        }
        closeWasRequested = true;
        stage.close();
    }

    private Stage createStage() {
        final Stage newStage = new Stage();
        newStage.setTitle(windowConfig.title());

        final BorderPane root = new BorderPane(plotSurface);

        final Scene scene = new Scene(root, windowConfig.width(), windowConfig.height());
        scene.setFill(style.getBackgroundColor());

        newStage.setScene(scene);

        installExitPolicyHooksIfEnabled(newStage);

        return newStage;
    }

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
                return;
            }

            this.stage = null;

            if (trackedByExitPolicy) {
                trackedByExitPolicy = false;
                JavaFxRuntime.registerTrackedViewerClosedAndExitIfLast();
            }
        });
    }

}