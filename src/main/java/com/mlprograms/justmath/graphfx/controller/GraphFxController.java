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

package com.mlprograms.justmath.graphfx.controller;

import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.service.GraphFxExportService;
import com.mlprograms.justmath.graphfx.view.GraphFxGraphView;
import com.mlprograms.justmath.graphfx.view.GraphToolbarView;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.NonNull;

import java.util.Locale;
import java.util.function.Function;

/**
 * Base controller class for all GraphFX controllers.
 * <p>
 * This class is responsible for wiring together the {@link GraphFxModel},
 * the {@link GraphFxGraphView}, and the {@link GraphToolbarView}.
 * It provides common toolbar behavior, status bar updates, tool selection logic,
 * and safe execution utilities for operations that may fail at runtime.
 * </p>
 * <p>
 * Subclasses are expected to provide a concrete root node via {@link #getRoot()},
 * which can then be embedded into a JavaFX scene.
 * </p>
 */
public abstract class GraphFxController {

    /**
     * The underlying model containing functions, objects, and settings
     * used by the graph view.
     */
    protected final GraphFxModel model;

    /**
     * The main graphical view responsible for rendering the coordinate system,
     * functions, and interactive graphical elements.
     */
    protected final GraphFxGraphView graphView;

    /**
     * The toolbar view providing user interface controls for interaction,
     * navigation, and export functionality.
     */
    protected final GraphToolbarView toolbar;

    /**
     * Service used for exporting the graph view to various formats.
     */
    private final GraphFxExportService graphFxExportService = new GraphFxExportService();

    /**
     * Creates a new GraphFX controller and initializes all common bindings
     * between model, view, and toolbar.
     * <p>
     * During construction, the graph view is created using the provided factory,
     * common toolbar actions are wired, and the status bar listener is registered.
     * </p>
     *
     * @param model            the data model backing the graph view
     * @param toolbar          the toolbar providing user interaction controls
     * @param graphViewFactory a factory function used to create the graph view
     *                         from the given model
     * @throws NullPointerException if any parameter is {@code null}
     */
    protected GraphFxController(@NonNull final GraphFxModel model, @NonNull final GraphToolbarView toolbar, @NonNull final Function<GraphFxModel, GraphFxGraphView> graphViewFactory) {
        this.model = model;
        this.toolbar = toolbar;
        this.graphView = graphViewFactory.apply(model);

        wireToolbarCommon();
        wireStatusBar();
    }

    /**
     * Wires all toolbar controls that are common to every GraphFX controller.
     * <p>
     * This includes tool selection buttons, view reset functionality,
     * grid and axes visibility toggles, object clearing, and export actions.
     * Button enablement is dynamically bound to the model state where applicable.
     * </p>
     */
    protected final void wireToolbarCommon() {
        toolbar.moveButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.MOVE));
        toolbar.zoomBoxButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.ZOOM_BOX));
        toolbar.pointButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.POINT_ON_FUNCTION));
        toolbar.tangentButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.TANGENT));
        toolbar.normalButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.NORMAL));
        toolbar.rootButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.ROOT));
        toolbar.intersectButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.INTERSECTION));

        toolbar.resetViewButton().setOnAction(e -> graphView.resetView());

        toolbar.clearMarksButton().disableProperty().bind(Bindings.isEmpty(model.getObjects()));
        toolbar.clearMarksButton().setOnAction(e -> model.clearObjects());

        toolbar.gridCheckBox().selectedProperty().bindBidirectional(model.getSettings().showGridProperty());
        toolbar.axesCheckBox().selectedProperty().bindBidirectional(model.getSettings().showAxesProperty());

        toolbar.exportPngButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportPngButton().setOnAction(e -> safe(() -> graphFxExportService.exportPng(graphView, model)));

        toolbar.exportSvgButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportSvgButton().setOnAction(e -> safe(() -> graphFxExportService.exportSvg(graphView, model)));

        toolbar.exportCsvButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportCsvButton().setOnAction(e -> safe(() -> graphFxExportService.exportCsv(graphView, model)));

        toolbar.exportJsonButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportJsonButton().setOnAction(e -> safe(() -> graphFxExportService.exportJson(graphView, model)));
    }

    /**
     * Registers a status listener on the graph view and updates the toolbar
     * status label with the current cursor coordinates.
     * <p>
     * Coordinates are formatted using a fixed locale and six decimal places
     * to ensure consistent numerical representation.
     * </p>
     */
    protected final void wireStatusBar() {
        graphView.setStatusListener((x, y) -> toolbar.statusLabel().setText("x=" + String.format(Locale.ROOT, "%.6f", x) + "   y=" + String.format(Locale.ROOT, "%.6f", y)));
    }

    /**
     * Selects the given tool mode for the graph view and transfers focus
     * to the view to ensure immediate user interaction.
     *
     * @param toolMode the tool mode to activate
     * @throws NullPointerException if {@code toolMode} is {@code null}
     */
    protected final void selectTool(@NonNull final GraphFxGraphView.ToolMode toolMode) {
        graphView.setToolMode(toolMode);
        graphView.requestFocus();
    }

    /**
     * Returns the root JavaFX node managed by this controller.
     * <p>
     * Subclasses must override this method to provide the actual root node
     * used for scene construction.
     * </p>
     *
     * @return the root JavaFX node
     * @throws UnsupportedOperationException if not implemented by a subclass
     */
    public Parent getRoot() {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes the given operation and handles any thrown exception
     * by displaying an error dialog to the user.
     * <p>
     * This method centralizes error handling for user-triggered actions,
     * ensuring that failures are reported in a consistent and user-friendly way.
     * </p>
     *
     * @param operation the operation to execute
     * @throws NullPointerException if {@code operation} is {@code null}
     */
    protected final void safe(@NonNull final Runnable operation) {
        try {
            operation.run();
        } catch (final Exception exception) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage() == null ? "Unknown error." : exception.getMessage(), ButtonType.OK);
            alert.setHeaderText("Operation failed");
            alert.showAndWait();
        }
    }

}
