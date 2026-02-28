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

package com.mlprograms.justmath.graphing.fx.planar.view;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphing.fx.JavaFxRuntime;
import com.mlprograms.justmath.graphing.fx.WindowConfig;
import com.mlprograms.justmath.graphing.fx.planar.engine.ImplicitFunctionPlotEngine;
import com.mlprograms.justmath.graphing.fx.planar.engine.PlotData;
import com.mlprograms.justmath.graphing.fx.planar.engine.expression.PlotExpression;
import com.mlprograms.justmath.graphing.fx.planar.engine.expression.PlotExpressionFactory;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotResult;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Minimal JavaFX viewer that supports panning/zooming and re-plotting on demand.
 * <p>
 * This viewer implements the implicit function renderer from your existing GraphFx approach:
 * it samples a grid for an equation like {@code y^2 = x^3 - x} and draws the contour line.
 * </p>
 */
public final class GraphFxViewer {

    /**
     * Plot engine used to compute plot primitives.
     */
    private final ImplicitFunctionPlotEngine plotEngine;

    /**
     * Window configuration.
     */
    private final WindowConfig windowConfig;

    /**
     * Creates a viewer with default config and engine.
     */
    public GraphFxViewer() {
        this(WindowConfig.defaultConfig(), new ImplicitFunctionPlotEngine());
    }

    /**
     * Creates a viewer.
     *
     * @param windowConfig window configuration (must not be {@code null})
     * @param plotEngine   plot engine (must not be {@code null})
     */
    public GraphFxViewer(@NonNull final WindowConfig windowConfig,
                         @NonNull final ImplicitFunctionPlotEngine plotEngine) {
        this.windowConfig = windowConfig;
        this.plotEngine = plotEngine;
    }

    /**
     * Shows the viewer window and renders an initial equation.
     * <p>
     * This method is safe to call from a non-FX thread; it will bootstrap JavaFX and show the window.
     * </p>
     */
    public void show() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::showInternal);
    }

    /**
     * Internal FX-thread window creation.
     */
    private void showInternal() {
        final Stage stage = new Stage();
        stage.setTitle(windowConfig.getTitle());

        final CartesianPlanePane planePane = new CartesianPlanePane();
        final BorderPane root = new BorderPane();
        root.setCenter(planePane);

        final TextField equationField = new TextField("y^2=x^3-x");
        equationField.setPrefColumnCount(30);

        final Label hint = new Label("Equation:");
        final Button plotButton = new Button("Plot");

        final HBox top = new HBox(10, hint, equationField, plotButton);
        top.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.03);");
        root.setTop(top);

        final AtomicReference<String> currentEquation = new AtomicReference<>(equationField.getText());

        plotButton.setOnAction(event -> {
            currentEquation.set(equationField.getText());
            updatePlot(planePane, currentEquation.get());
        });

        planePane.setOnMouseReleased(event -> updatePlot(planePane, currentEquation.get()));
        planePane.widthProperty().addListener((obs, oldVal, newVal) -> updatePlot(planePane, currentEquation.get()));
        planePane.heightProperty().addListener((obs, oldVal, newVal) -> updatePlot(planePane, currentEquation.get()));

        updatePlot(planePane, currentEquation.get());

        final Scene scene = new Scene(root, windowConfig.getWidth(), windowConfig.getHeight());
        stage.setScene(scene);

        JavaFxRuntime.registerTrackedViewerOpened();
        stage.setOnHidden(event -> {
            if (windowConfig.isExitApplicationOnLastViewerClose()) {
                JavaFxRuntime.registerTrackedViewerClosedAndExitIfLast();
            } else {
                JavaFxRuntime.registerTrackedViewerClosedAndExitIfLast();
            }
        });

        stage.show();
    }

    /**
     * Computes a new plot for the current viewport and equation and updates the pane.
     *
     * @param pane     pane to update
     * @param equation equation string
     */
    private void updatePlot(final CartesianPlanePane pane, final String equation) {
        final VisibleWorldBounds bounds = pane.visibleWorldBounds();

        final BigNumber minX = new BigNumber(Double.toString(bounds.minX()));
        final BigNumber maxX = new BigNumber(Double.toString(bounds.maxX()));
        final BigNumber minY = new BigNumber(Double.toString(bounds.minY()));
        final BigNumber maxY = new BigNumber(Double.toString(bounds.maxY()));

        final BigNumber cellSize = new BigNumber(Double.toString(Math.max(0.01, 3.0 / pane.getPixelsPerUnit())));

        final ViewportSnapshot viewport = new ViewportSnapshot(minX, maxX, minY, maxY, cellSize);

        final PlotExpressionFactory factory = new PlotExpressionFactory();
        final PlotExpression expression = factory.create(equation);

        final PlotData plotData = PlotData.builder()
                .minX(viewport.minX())
                .maxX(viewport.maxX())
                .minY(viewport.minY())
                .maxY(viewport.maxY())
                .cellSize(viewport.cellSize())
                .plotExpression(expression)
                .build();

        final PlotResult result = plotEngine.plot(plotData);
        pane.setPlotResult(result);
    }
}
