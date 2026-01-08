package com.mlprograms.justmath.graphfx.planar.view;

import com.mlprograms.justmath.graphfx.JavaFxRuntime;
import com.mlprograms.justmath.graphfx.WindowConfig;
import com.mlprograms.justmath.graphfx.planar.calculator.GraphFxCalculatorEngine;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;

public final class GraphFxViewer {

    @Getter
    private final WindowConfig windowConfig;

    private final GridPane gridPane;
    private final GraphFxCalculatorEngine plotEngine;

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

    public void plot(final String expression) {
        plot(expression, Map.of());
    }

    public void plot(final String expression, final Map<String, String> variables) {
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
        try {
            final ViewportSnapshot viewportSnapshot = gridPane.tryCreateViewportSnapshot()
                    .orElseThrow(() -> new IllegalStateException("ViewportSnapshot cannot be created (view not laid out yet)."));

            final PlotResult plotResult = plotEngine.evaluate(expression, variables, viewportSnapshot);
            gridPane.setPlotResult(plotResult);
        } catch (final RuntimeException ex) {
            gridPane.clearPlot();
        }
    }

}
