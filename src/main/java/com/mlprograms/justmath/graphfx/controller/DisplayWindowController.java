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

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.model.GraphFxPalette;
import com.mlprograms.justmath.graphfx.service.GraphFxExportService;
import com.mlprograms.justmath.graphfx.view.DisplayWindowView;
import com.mlprograms.justmath.graphfx.view.GraphFxGraphView;
import com.mlprograms.justmath.graphfx.view.GraphToolbarView;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;

import java.util.List;
import java.util.Locale;

/**
 * Controller for the "Nur-Anzeige-Fenster" (read-only display window).
 */
public final class DisplayWindowController {

    private final GraphFxModel model;
    private final GraphFxGraphView graphView;
    private final GraphToolbarView toolbar;
    private final DisplayWindowView view;

    public DisplayWindowController(final CalculatorEngine engine, final List<GraphFxFunctionSpec> functions) {
        this.model = new GraphFxModel();

        int idx = 0;
        for (final GraphFxFunctionSpec spec : functions) {
            final var color = spec.color() != null ? spec.color() : GraphFxPalette.colorForIndex(idx);
            model.addFunction(spec.name(), spec.expression()).setColor(color);
            idx++;
        }

        if (!model.getFunctions().isEmpty()) {
            model.setSelectedFunction(model.getFunctions().getFirst());
        }

        this.graphView = new GraphFxGraphView(model, engine);
        this.toolbar = new GraphToolbarView();
        this.view = new DisplayWindowView(toolbar, graphView);

        wireToolbar();

        graphView.setStatusListener((x, y) -> toolbar.statusLabel().setText(
                "x=" + String.format(Locale.ROOT, "%.6f", x) + "   y=" + String.format(Locale.ROOT, "%.6f", y)
        ));
    }

    public Parent getRoot() {
        return view;
    }

    public GraphFxModel getModel() {
        return model;
    }

    public GraphFxGraphView getGraphView() {
        return graphView;
    }

    private void wireToolbar() {
        toolbar.moveButton().setOnAction(e -> { graphView.setToolMode(GraphFxGraphView.ToolMode.MOVE); graphView.requestFocus(); });
        toolbar.zoomBoxButton().setOnAction(e -> { graphView.setToolMode(GraphFxGraphView.ToolMode.ZOOM_BOX); graphView.requestFocus(); });
        toolbar.pointButton().setOnAction(e -> { graphView.setToolMode(GraphFxGraphView.ToolMode.POINT_ON_FUNCTION); graphView.requestFocus(); });
        toolbar.tangentButton().setOnAction(e -> { graphView.setToolMode(GraphFxGraphView.ToolMode.TANGENT); graphView.requestFocus(); });
        toolbar.normalButton().setOnAction(e -> { graphView.setToolMode(GraphFxGraphView.ToolMode.NORMAL); graphView.requestFocus(); });
        toolbar.rootButton().setOnAction(e -> { graphView.setToolMode(GraphFxGraphView.ToolMode.ROOT); graphView.requestFocus(); });
        toolbar.intersectButton().setOnAction(e -> { graphView.setToolMode(GraphFxGraphView.ToolMode.INTERSECTION); graphView.requestFocus(); });
        toolbar.integralButton().setOnAction(e -> { graphView.setToolMode(GraphFxGraphView.ToolMode.INTEGRAL); graphView.requestFocus(); });

        toolbar.resetViewButton().setOnAction(e -> graphView.resetView());

        toolbar.clearMarksButton().disableProperty().bind(Bindings.isEmpty(model.getObjects()));
        toolbar.clearMarksButton().setOnAction(e -> model.clearObjects());

        toolbar.gridCheckBox().selectedProperty().bindBidirectional(model.getSettings().showGridProperty());
        toolbar.axesCheckBox().selectedProperty().bindBidirectional(model.getSettings().showAxesProperty());

        toolbar.exportPngButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportPngButton().setOnAction(e -> GraphFxExportService.exportPng(graphView, model));

        toolbar.exportSvgButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportSvgButton().setOnAction(e -> GraphFxExportService.exportSvg(graphView, model));

        toolbar.exportCsvButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportCsvButton().setOnAction(e -> GraphFxExportService.exportCsv(graphView, model));

        toolbar.exportJsonButton().disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        toolbar.exportJsonButton().setOnAction(e -> GraphFxExportService.exportJson(graphView, model));
    }
}
