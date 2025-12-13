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
import com.mlprograms.justmath.graphfx.model.GraphFxFunction;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.model.GraphFxVariable;
import com.mlprograms.justmath.graphfx.model.GraphFxSliderAdapter;
import com.mlprograms.justmath.graphfx.service.GraphFxExportService;
import com.mlprograms.justmath.graphfx.view.GraphFxGraphView;
import com.mlprograms.justmath.graphfx.view.GraphToolbarView;
import com.mlprograms.justmath.graphfx.view.MainWindowView;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Controller for the editable main window.
 */
public final class MainWindowController {

    private final GraphFxModel model;
    private final CalculatorEngine engine;

    private final GraphFxGraphView graphView;
    private final GraphToolbarView toolbar;
    private final MainWindowView view;

    public MainWindowController(final GraphFxModel model, final CalculatorEngine engine) {
        this.model = model;
        this.engine = engine;

        this.graphView = new GraphFxGraphView(model, engine);
        this.toolbar = new GraphToolbarView();
        this.view = new MainWindowView(toolbar, graphView);

        wireToolbar();
        wireTables();
        wireSliders();

        graphView.setStatusListener((x, y) -> toolbar.statusLabel().setText(
                "x=" + String.format(Locale.ROOT, "%.6f", x) + "   y=" + String.format(Locale.ROOT, "%.6f", y)
        ));

        graphView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.R) {
                graphView.resetView();
                e.consume();
            }
        });
    }

    public Parent getRoot() {
        return view;
    }

    public GraphFxGraphView getGraphView() {
        return graphView;
    }

    public GraphFxModel getModel() {
        return model;
    }

    private void wireToolbar() {
        toolbar.moveButton().setOnAction(e -> {
            graphView.setToolMode(GraphFxGraphView.ToolMode.MOVE);
            graphView.requestFocus();
        });
        toolbar.zoomBoxButton().setOnAction(e -> {
            graphView.setToolMode(GraphFxGraphView.ToolMode.ZOOM_BOX);
            graphView.requestFocus();
        });
        toolbar.pointButton().setOnAction(e -> {
            graphView.setToolMode(GraphFxGraphView.ToolMode.POINT_ON_FUNCTION);
            graphView.requestFocus();
        });
        toolbar.tangentButton().setOnAction(e -> {
            graphView.setToolMode(GraphFxGraphView.ToolMode.TANGENT);
            graphView.requestFocus();
        });
        toolbar.normalButton().setOnAction(e -> {
            graphView.setToolMode(GraphFxGraphView.ToolMode.NORMAL);
            graphView.requestFocus();
        });
        toolbar.rootButton().setOnAction(e -> {
            graphView.setToolMode(GraphFxGraphView.ToolMode.ROOT);
            graphView.requestFocus();
        });
        toolbar.intersectButton().setOnAction(e -> {
            graphView.setToolMode(GraphFxGraphView.ToolMode.INTERSECTION);
            graphView.requestFocus();
        });
        toolbar.integralButton().setOnAction(e -> {
            graphView.setToolMode(GraphFxGraphView.ToolMode.INTEGRAL);
            graphView.requestFocus();
        });

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

    private void wireTables() {
        view.functionsTable().setItems(model.getFunctions());
        view.functionsTable().getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> model.setSelectedFunction(n));

        view.removeFunctionButton().disableProperty().bind(view.functionsTable().getSelectionModel().selectedItemProperty().isNull());

        view.addFunctionButton().setOnAction(e -> openAddFunctionDialog());
        view.removeFunctionButton().setOnAction(e -> {
            final GraphFxFunction sel = view.functionsTable().getSelectionModel().getSelectedItem();
            if (sel != null) safe(() -> model.removeFunction(sel));
        });

        view.variablesTable().setItems(model.getVariables());

        view.addVariableButton().setOnAction(e -> openAddVariableDialog());
        view.removeVariableButton().disableProperty().bind(view.variablesTable().getSelectionModel().selectedItemProperty().isNull());
        view.removeVariableButton().setOnAction(e -> {
            final GraphFxVariable sel = view.variablesTable().getSelectionModel().getSelectedItem();
            if (sel != null) safe(() -> model.removeVariable(sel));
        });

        @SuppressWarnings("unchecked") final var cols = (java.util.List<TableColumn<GraphFxVariable, ?>>) (java.util.List<?>) view.variablesTable().getColumns();

        ((TableColumn<GraphFxVariable, String>) cols.get(0)).setOnEditCommit(e -> safe(() -> model.renameVariable(e.getRowValue(), e.getNewValue())));
        ((TableColumn<GraphFxVariable, String>) cols.get(1)).setOnEditCommit(e -> safe(() -> model.setVariableValue(e.getRowValue(), e.getNewValue())));
        ((TableColumn<GraphFxVariable, String>) cols.get(3)).setOnEditCommit(e -> safe(() -> model.setSliderMin(e.getRowValue(), e.getNewValue())));
        ((TableColumn<GraphFxVariable, String>) cols.get(4)).setOnEditCommit(e -> safe(() -> model.setSliderMax(e.getRowValue(), e.getNewValue())));
        ((TableColumn<GraphFxVariable, String>) cols.get(5)).setOnEditCommit(e -> safe(() -> model.setSliderStep(e.getRowValue(), e.getNewValue())));

        model.getFunctions().addListener((ListChangeListener<GraphFxFunction>) c -> {
            if (model.getSelectedFunction() == null && !model.getFunctions().isEmpty()) {
                model.setSelectedFunction(model.getFunctions().getFirst());
                view.functionsTable().getSelectionModel().selectFirst();
            }
        });

        if (model.getSelectedFunction() == null && !model.getFunctions().isEmpty()) {
            model.setSelectedFunction(model.getFunctions().getFirst());
            view.functionsTable().getSelectionModel().selectFirst();
        }
    }

    private void wireSliders() {
        model.getVariables().addListener((ListChangeListener<GraphFxVariable>) c -> rebuildSliders());
        model.revisionProperty().addListener((obs, o, n) -> rebuildSliders());
        rebuildSliders();
    }

    private void rebuildSliders() {
        view.slidersBox().getChildren().clear();

        for (final GraphFxVariable v : model.getVariables()) {
            if (!v.isSliderEnabled()) continue;

            final Label name = new Label(v.getName());
            final Slider slider = new Slider(0, 1, 0);
            final Label value = new Label(v.getValueString());

            final var adapter = GraphFxSliderAdapter.of(
                    v.getSliderMin(), v.getSliderMax(), v.getSliderStep(), v.getValue()
            );

            slider.setMin(0);
            slider.setMax(adapter.maxIndex());
            slider.setValue(adapter.toIndex(v.getValue()));

            slider.valueChangingProperty().addListener((obs, o, changing) -> graphView.setInteractiveMode(changing));
            slider.valueProperty().addListener((obs, o, n) -> safe(() -> {
                final BigDecimal newVal = adapter.fromIndex(n.intValue());
                final String text = newVal.stripTrailingZeros().toPlainString();
                value.setText(text);
                model.setVariableValue(v, newVal);
            }));

            v.valueStringProperty().addListener((obs, o, n) -> value.setText(n));

            // benutze die View-Hilfsmethode fuer Layout und Styling
            view.addSliderRow(v, slider, name, value);
        }
    }

    private void openAddFunctionDialog() {
        final Dialog<FunctionDraft> dialog = new Dialog<>();
        dialog.setTitle("Add function");
        dialog.setHeaderText(null);

        final ButtonType add = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(add, ButtonType.CANCEL);

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(14));

        final String defaultName = "f" + (model.getFunctions().size() + 1);

        final TextField nameField = new TextField(defaultName);
        final TextField exprField = new TextField("x");

        grid.addRow(0, new Label("Name"), nameField);
        grid.addRow(1, new Label("Expression"), exprField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(bt -> bt == add ? new FunctionDraft(nameField.getText(), exprField.getText()) : null);

        dialog.showAndWait().ifPresent(draft -> safe(() -> model.addFunction(draft.name().trim(), draft.expression().trim())));
    }

    private void openAddVariableDialog() {
        final Dialog<VariableDraft> dialog = new Dialog<>();
        dialog.setTitle("Add variable");
        dialog.setHeaderText(null);

        final ButtonType add = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(add, ButtonType.CANCEL);

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(14));

        final TextField nameField = new TextField(model.nextSuggestedVariableName());
        final TextField valueField = new TextField("0");

        final CheckBox sliderEnabled = new CheckBox("Slider");

        final TextField minField = new TextField("-10");
        final TextField maxField = new TextField("10");
        final TextField stepField = new TextField("0.1");

        minField.disableProperty().bind(sliderEnabled.selectedProperty().not());
        maxField.disableProperty().bind(sliderEnabled.selectedProperty().not());
        stepField.disableProperty().bind(sliderEnabled.selectedProperty().not());

        grid.addRow(0, new Label("Name"), nameField);
        grid.addRow(1, new Label("Value"), valueField);
        grid.addRow(2, new Label("Slider"), sliderEnabled);
        grid.addRow(3, new Label("Min"), minField);
        grid.addRow(4, new Label("Max"), maxField);
        grid.addRow(5, new Label("Step"), stepField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(bt -> bt == add
                ? new VariableDraft(nameField.getText(), valueField.getText(), sliderEnabled.isSelected(), minField.getText(), maxField.getText(), stepField.getText())
                : null);

        dialog.showAndWait().ifPresent(draft -> safe(() -> {
            final GraphFxVariable v = model.addVariable(draft.name().trim(), new BigDecimal(draft.value().trim()));
            if (draft.sliderEnabled()) {
                model.setSliderMin(v, draft.min());
                model.setSliderMax(v, draft.max());
                model.setSliderStep(v, draft.step());
                v.sliderEnabledProperty().set(true);
            }
        }));
    }

    private void safe(final Runnable r) {
        try {
            r.run();
        } catch (Exception ex) {
            final Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage() == null ? "Unknown error." : ex.getMessage(), ButtonType.OK);
            a.setHeaderText("Operation failed");
            a.showAndWait();
        }
    }

    private record FunctionDraft(String name, String expression) {
    }

    private record VariableDraft(String name, String value, boolean sliderEnabled, String min, String max,
                                 String step) {
    }
}
