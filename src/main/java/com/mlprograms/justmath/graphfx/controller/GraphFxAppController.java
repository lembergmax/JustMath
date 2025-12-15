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
import com.mlprograms.justmath.graphfx.model.GraphFxSliderAdapter;
import com.mlprograms.justmath.graphfx.model.GraphFxVariable;
import com.mlprograms.justmath.graphfx.view.GraphFxGraphView;
import com.mlprograms.justmath.graphfx.view.GraphToolbarView;
import com.mlprograms.justmath.graphfx.view.MainWindowView;
import javafx.collections.ListChangeListener;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import lombok.NonNull;

import java.math.BigDecimal;

public class GraphFxAppController extends GraphFxController {

    private final MainWindowView view;

    private record FunctionDraft(String name, String expression) {
    }

    private record VariableDraft(String name, String value, boolean sliderEnabled, String min, String max,
                                 String step) {
    }

    public GraphFxAppController(@NonNull final GraphFxModel model, @NonNull final CalculatorEngine calculatorEngine) {
        super(model, new GraphToolbarView(), m -> new GraphFxGraphView(m, calculatorEngine));
        this.view = new MainWindowView(toolbar, graphView);

        wireTables();
        wireSliders();
        wireHotkeys();
    }

    private void wireHotkeys() {
        graphView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.R) {
                graphView.resetView();
                event.consume();
            }
        });
    }

    private void wireTables() {
        view.functionsTable().setItems(model.getFunctions());
        view.functionsTable().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> model.setSelectedFunction(newValue));

        view.removeFunctionButton().disableProperty().bind(view.functionsTable().getSelectionModel().selectedItemProperty().isNull());

        view.addFunctionButton().setOnAction(e -> openAddFunctionDialog());
        view.removeFunctionButton().setOnAction(e -> {
            final GraphFxFunction selectedFunction = view.functionsTable().getSelectionModel().getSelectedItem();
            if (selectedFunction != null) {
                safe(() -> model.removeFunction(selectedFunction));
            }
        });

        view.variablesTable().setItems(model.getVariables());

        view.addVariableButton().setOnAction(e -> openAddVariableDialog());
        view.removeVariableButton().disableProperty().bind(view.variablesTable().getSelectionModel().selectedItemProperty().isNull());
        view.removeVariableButton().setOnAction(e -> {
            final GraphFxVariable selectedVariable = view.variablesTable().getSelectionModel().getSelectedItem();
            if (selectedVariable != null) {
                safe(() -> model.removeVariable(selectedVariable));
            }
        });

        @SuppressWarnings("unchecked") final var columns = (java.util.List<TableColumn<GraphFxVariable, ?>>) (java.util.List<?>) view.variablesTable().getColumns();

        columns.get(0).setOnEditCommit(e -> safe(() -> model.renameVariable(e.getRowValue(), (String) e.getNewValue())));
        columns.get(1).setOnEditCommit(e -> safe(() -> model.setVariableValue(e.getRowValue(), (String) e.getNewValue())));
        columns.get(3).setOnEditCommit(e -> safe(() -> model.setSliderMin(e.getRowValue(), (String) e.getNewValue())));
        columns.get(4).setOnEditCommit(e -> safe(() -> model.setSliderMax(e.getRowValue(), (String) e.getNewValue())));
        columns.get(5).setOnEditCommit(e -> safe(() -> model.setSliderStep(e.getRowValue(), (String) e.getNewValue())));

        model.getFunctions().addListener((ListChangeListener<GraphFxFunction>) change -> {
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

        for (final GraphFxVariable variable : model.getVariables()) {
            if (!variable.isSliderEnabled()) {
                continue;
            }

            final Label nameLabel = new Label(variable.getName());
            final Slider slider = new Slider(0, 1, 0);
            final Label valueLabel = new Label(variable.getValueString());

            final GraphFxSliderAdapter adapter = GraphFxSliderAdapter.of(variable.getSliderMin(), variable.getSliderMax(), variable.getSliderStep(), variable.getValue());

            slider.setMin(0);
            slider.setMax(adapter.maxIndex());
            slider.setValue(adapter.toIndex(variable.getValue()));

            slider.valueChangingProperty().addListener((obs, o, changing) -> graphView.setInteractiveMode(changing));
            slider.valueProperty().addListener((obs, o, n) -> safe(() -> {
                final BigDecimal newValue = adapter.fromIndex(n.intValue());
                final String newText = newValue.stripTrailingZeros().toPlainString();
                valueLabel.setText(newText);
                model.setVariableValue(variable, newValue);
            }));

            variable.valueStringProperty().addListener((obs, o, n) -> valueLabel.setText(n));

            view.addSliderRow(variable, slider, nameLabel, valueLabel);
        }
    }

    private void openAddFunctionDialog() {
        final Dialog<FunctionDraft> dialog = new Dialog<>();
        dialog.setTitle("Add function");
        dialog.setHeaderText(null);

        final ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(14));

        final String defaultName = "f" + (model.getFunctions().size() + 1);

        final TextField nameField = new TextField(defaultName);
        final TextField expressionField = new TextField("x");

        grid.addRow(0, new Label("Name"), nameField);
        grid.addRow(1, new Label("Expression"), expressionField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == addButtonType ? new FunctionDraft(nameField.getText(), expressionField.getText()) : null);

        dialog.showAndWait().ifPresent(draft -> safe(() -> model.addFunction(draft.name().trim(), draft.expression().trim())));
    }

    private void openAddVariableDialog() {
        final Dialog<VariableDraft> dialog = new Dialog<>();
        dialog.setTitle("Add variable");
        dialog.setHeaderText(null);

        final ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(14));

        final TextField nameField = new TextField(model.nextSuggestedVariableName());
        final TextField valueField = new TextField("0");

        final CheckBox sliderEnabledCheckBox = new CheckBox("Slider");

        final TextField minField = new TextField("-10");
        final TextField maxField = new TextField("10");
        final TextField stepField = new TextField("0.1");

        minField.disableProperty().bind(sliderEnabledCheckBox.selectedProperty().not());
        maxField.disableProperty().bind(sliderEnabledCheckBox.selectedProperty().not());
        stepField.disableProperty().bind(sliderEnabledCheckBox.selectedProperty().not());

        grid.addRow(0, new Label("Name"), nameField);
        grid.addRow(1, new Label("Value"), valueField);
        grid.addRow(2, new Label("Slider"), sliderEnabledCheckBox);
        grid.addRow(3, new Label("Min"), minField);
        grid.addRow(4, new Label("Max"), maxField);
        grid.addRow(5, new Label("Step"), stepField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == addButtonType ? new VariableDraft(nameField.getText(), valueField.getText(), sliderEnabledCheckBox.isSelected(), minField.getText(), maxField.getText(), stepField.getText()) : null);

        dialog.showAndWait().ifPresent(draft -> safe(() -> {
            final GraphFxVariable createdVariable = model.addVariable(draft.name().trim(), new BigDecimal(draft.value().trim()));
            if (draft.sliderEnabled()) {
                model.setSliderMin(createdVariable, draft.min());
                model.setSliderMax(createdVariable, draft.max());
                model.setSliderStep(createdVariable, draft.step());
                createdVariable.sliderEnabledProperty().set(true);
            }
        }));
    }

    @Override
    public Parent getRoot() {
        return view;
    }

}