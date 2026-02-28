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
import com.mlprograms.justmath.graphing.api.Domain;
import com.mlprograms.justmath.graphing.api.GraphingCalculator;
import com.mlprograms.justmath.graphing.api.GraphingCalculators;
import com.mlprograms.justmath.graphing.api.PlotRequest;
import com.mlprograms.justmath.graphing.api.PlotSeries;
import com.mlprograms.justmath.graphing.api.Resolution;
import com.mlprograms.justmath.graphing.fx.JavaFxRuntime;
import com.mlprograms.justmath.graphing.fx.WindowConfig;
import com.mlprograms.justmath.graphing.fx.planar.calculator.GraphFxCalculatorEngine;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotLine;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotPoint;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotResult;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class GraphFxViewer {

    @Getter
    private final WindowConfig windowConfig;

    private final GridPane gridPane;
    private final GraphFxCalculatorEngine plotEngine;
    private final GraphingCalculator graphingCalculator;
    private final List<QueuedPlot> queuedPlots;

    private static final int MAX_LAYOUT_RETRY_COUNT = 8;

    private Stage stage;

    private boolean closeWasRequested;
    private boolean trackedByExitPolicy;

    public GraphFxViewer() {
        this(WindowConfig.defaultConfig());
    }

    public GraphFxViewer(final String title) {
        this(new WindowConfig(title, WindowConfig.DEFAULT_WIDTH, WindowConfig.DEFAULT_HEIGHT, true));
    }

    public GraphFxViewer(final WindowConfig windowConfig) {
        this.windowConfig = Objects.requireNonNull(windowConfig, "windowConfig must not be null");
        this.gridPane = new GridPane();
        this.plotEngine = new GraphFxCalculatorEngine();
        this.graphingCalculator = GraphingCalculators.createDefault();
        this.queuedPlots = new ArrayList<>();
    }

    public void show() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::showOnFxThread);
    }

    public void hide() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::hideOnFxThread);
    }

    public void close() {
        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(this::closeOnFxThread);
    }

    public void plotExpression(final String expression) {
        plotExpression(expression, Map.of());
    }

    public void plotExpression(final String expression, final Map<String, String> variables) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(variables, "variables must not be null");

        JavaFxRuntime.ensureStarted();
        JavaFxRuntime.runOnFxThread(() -> plotOnFxThread(expression, variables));
    }

    private void showOnFxThread() {
        if (stage == null) {
            stage = createStage();
        }

        if (!stage.isShowing()) {
            stage.show();
        }

        flushQueuedPlots();

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

        final BorderPane root = new BorderPane(gridPane);
        final Scene scene = new Scene(root, windowConfig.width(), windowConfig.height());
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

    private void plotOnFxThread(final String expression, final Map<String, String> variables) {
        queuedPlots.add(new QueuedPlot(expression, variables));
        if (stage != null && stage.isShowing()) {
            flushQueuedPlots();
        }
    }

    private void flushQueuedPlots() {
        final List<QueuedPlot> pending = new ArrayList<>(queuedPlots);
        queuedPlots.clear();

        for (QueuedPlot queuedPlot : pending) {
            renderPlotWithRetry(queuedPlot, MAX_LAYOUT_RETRY_COUNT);
        }
    }

    private void renderPlotWithRetry(final QueuedPlot queuedPlot, final int retriesLeft) {
        try {
            final ViewportSnapshot viewportSnapshot = gridPane.tryCreateViewportSnapshot()
                    .orElseThrow(() -> new IllegalStateException("ViewportSnapshot cannot be created (view not laid out yet)."));

            final PlotResult plotResult = evaluatePlot(queuedPlot.expression(), queuedPlot.variables(), viewportSnapshot);
            gridPane.addPlotResult(plotResult);
        } catch (final RuntimeException ex) {
            if (retriesLeft > 0) {
                Platform.runLater(() -> renderPlotWithRetry(queuedPlot, retriesLeft - 1));
            }
        }
    }

    private PlotResult evaluatePlot(final String expression, final Map<String, String> variables,
                                    final ViewportSnapshot viewportSnapshot) {
        try {
            return evaluateWithGraphingApi(expression, variables, viewportSnapshot);
        } catch (RuntimeException exception) {
            return plotEngine.evaluate(expression, variables, viewportSnapshot);
        }
    }

    private PlotResult evaluateWithGraphingApi(final String expression, final Map<String, String> variables,
                                               final ViewportSnapshot viewportSnapshot) {
        final Domain domain = new Domain(viewportSnapshot.minX().doubleValue(), viewportSnapshot.maxX().doubleValue());
        final int sampleCount = Math.max(2, (int) Math.ceil((domain.maxX() - domain.minX()) / viewportSnapshot.cellSize().doubleValue()));
        final Resolution resolution = Resolution.fixed(sampleCount);

        final PlotRequest request = new PlotRequest.Builder(expression)
                .domain(domain)
                .resolution(resolution)
                .variables(parseDoubleVariables(variables))
                .build();

        final PlotSeries plotSeries = graphingCalculator.plot(request).series().getFirst();
        final List<PlotLine> lines = convertSeriesToLines(plotSeries);
        return new PlotResult(new ArrayList<>(), lines);
    }

    private Map<String, Double> parseDoubleVariables(final Map<String, String> variables) {
        final Map<String, Double> parsed = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            parsed.put(entry.getKey(), Double.parseDouble(entry.getValue()));
        }
        return parsed;
    }

    private List<PlotLine> convertSeriesToLines(final PlotSeries plotSeries) {
        final List<PlotLine> lines = new ArrayList<>();
        final List<PlotPoint> activeSegment = new ArrayList<>();

        for (int index = 0; index < plotSeries.xValues().length; index++) {
            final double x = plotSeries.xValues()[index];
            final double y = plotSeries.yValues()[index];

            if (!Double.isFinite(x) || !Double.isFinite(y)) {
                appendSegment(lines, activeSegment);
                continue;
            }

            activeSegment.add(new PlotPoint(
                    new BigNumber(Double.toString(x), Locale.ROOT),
                    new BigNumber(Double.toString(y), Locale.ROOT)
            ));
        }

        appendSegment(lines, activeSegment);
        return lines;
    }

    private void appendSegment(final List<PlotLine> lines, final List<PlotPoint> activeSegment) {
        if (activeSegment.size() >= 2) {
            lines.add(new PlotLine(new ArrayList<>(activeSegment)));
        }
        activeSegment.clear();
    }

    private record QueuedPlot(String expression, Map<String, String> variables) {
    }

}
