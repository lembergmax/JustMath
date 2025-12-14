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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.NonNull;

import java.util.Locale;
import java.util.function.Function;

public abstract class GraphFxController {

    protected final GraphFxModel model;
    protected final GraphFxGraphView graphView;
    protected final GraphToolbarView toolbar;

    protected GraphFxController(@NonNull final GraphFxModel model, @NonNull final GraphToolbarView toolbar, @NonNull final Function<GraphFxModel, GraphFxGraphView> graphViewFactory) {
        this.model = model;
        this.toolbar = toolbar;
        this.graphView = graphViewFactory.apply(model);

        wireToolbarCommon();
        wireStatusBar();
    }

    protected final void wireToolbarCommon() {
        toolbar.moveButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.MOVE));
        toolbar.zoomBoxButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.ZOOM_BOX));
        toolbar.pointButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.POINT_ON_FUNCTION));
        toolbar.tangentButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.TANGENT));
        toolbar.normalButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.NORMAL));
        toolbar.rootButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.ROOT));
        toolbar.intersectButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.INTERSECTION));
        toolbar.integralButton().setOnAction(e -> selectTool(GraphFxGraphView.ToolMode.INTEGRAL));

        toolbar.resetViewButton().setOnAction(e -> graphView.resetView());

        toolbar.clearMarksButton().disableProperty().bind(Bindings.isEmpty(model.getObjects()));
        toolbar.clearMarksButton().setOnAction(e -> model.clearObjects());

        toolbar.gridCheckBox().selectedProperty().bindBidirectional(model.getSettings().showGridProperty());
        toolbar.axesCheckBox().selectedProperty().bindBidirectional(model.getSettings().showAxesProperty());

        toolbar.exportPngButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportPngButton().setOnAction(e -> safe(() -> GraphFxExportService.exportPng(graphView, model)));

        toolbar.exportSvgButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportSvgButton().setOnAction(e -> safe(() -> GraphFxExportService.exportSvg(graphView, model)));

        toolbar.exportCsvButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportCsvButton().setOnAction(e -> safe(() -> GraphFxExportService.exportCsv(graphView, model)));

        toolbar.exportJsonButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportJsonButton().setOnAction(e -> safe(() -> GraphFxExportService.exportJson(graphView, model)));
    }

    protected final void wireStatusBar() {
        graphView.setStatusListener((x, y) -> toolbar.statusLabel().setText("x=" + String.format(Locale.ROOT, "%.6f", x) + "   y=" + String.format(Locale.ROOT, "%.6f", y)));
    }

    protected final void selectTool(@NonNull final GraphFxGraphView.ToolMode toolMode) {
        graphView.setToolMode(toolMode);
        graphView.requestFocus();
    }

    protected final void safe(@NonNull final Runnable operation) {
        try {
            operation.run();
        } catch (Exception exception) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage() == null ? "Unknown error." : exception.getMessage(), ButtonType.OK);
            alert.setHeaderText("Operation failed");
            alert.showAndWait();
        }
    }

}