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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main application controller for the GraphFX window.
 * <p>
 * This controller connects the {@link GraphFxModel} to the {@link MainWindowView} by:
 * </p>
 * <ul>
 *     <li>wiring tables for functions and variables</li>
 *     <li>building slider rows for variables with sliders enabled</li>
 *     <li>installing small keyboard shortcuts for faster navigation</li>
 * </ul>
 *
 * <p>
 * All user-triggered model mutations are executed via {@link #safe(Runnable)} (inherited from
 * {@link GraphFxController}) to ensure that validation errors are handled consistently (e.g., by showing an alert).
 * </p>
 */
public class GraphFxAppController extends GraphFxController {

    private final MainWindowView view;

    /**
     * Immutable dialog result representing a draft of a function to be added.
     *
     * @param name       function name as entered by the user
     * @param expression expression as entered by the user
     */
    private record FunctionDraft(String name, String expression) {
    }

    /**
     * Immutable dialog result representing a draft of a variable to be added.
     *
     * @param name          variable name as entered by the user
     * @param value         variable value as entered by the user
     * @param sliderEnabled whether the slider option was enabled
     * @param min           slider minimum as entered by the user
     * @param max           slider maximum as entered by the user
     * @param step          slider step as entered by the user
     */
    private record VariableDraft(String name, String value, boolean sliderEnabled, String min, String max,
                                 String step) {
    }

    /**
     * Creates the controller and wires the model to the UI.
     *
     * @param model            the shared GraphFX model (must not be {@code null})
     * @param calculatorEngine engine used by {@link GraphFxGraphView} to evaluate expressions (must not be {@code null})
     */
    public GraphFxAppController(@NonNull final GraphFxModel model, @NonNull final CalculatorEngine calculatorEngine) {
        super(model, new GraphToolbarView(), m -> new GraphFxGraphView(m, calculatorEngine));
        this.view = new MainWindowView(toolbar, graphView);

        wireTables();
        wireSliders();
        wireHotkeys();
    }

    /**
     * Installs controller-level hotkeys on the graph view.
     * <p>
     * Current bindings:
     * </p>
     * <ul>
     *     <li><b>R</b> resets the view to the default bounds.</li>
     * </ul>
     */
    private void wireHotkeys() {
        graphView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.R) {
                graphView.resetView();
                event.consume();
            }
        });
    }

    /**
     * Wires function and variable tables to the model, sets up add/remove buttons,
     * and installs edit handlers for variable columns.
     * <p>
     * This method also ensures a sensible initial selection:
     * if no function is selected and at least one exists, the first function becomes selected.
     * </p>
     */
    private void wireTables() {
        wireFunctionTable();
        wireVariableTable();
        ensureInitialFunctionSelection();
        installAutoSelectFirstFunction();
    }

    /**
     * Configures the functions table, selection synchronization, and add/remove actions.
     */
    private void wireFunctionTable() {
        view.functionsTable().setItems(model.getFunctions());

        view.functionsTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> model.setSelectedFunction(newValue));

        view.removeFunctionButton()
                .disableProperty()
                .bind(view.functionsTable().getSelectionModel().selectedItemProperty().isNull());

        view.addFunctionButton().setOnAction(e -> openAddFunctionDialog());

        view.removeFunctionButton().setOnAction(e -> {
            final GraphFxFunction selectedFunction = view.functionsTable().getSelectionModel().getSelectedItem();
            if (selectedFunction != null) {
                safe(() -> model.removeFunction(selectedFunction));
            }
        });
    }

    /**
     * Configures the variables table, add/remove actions, and edit commit handlers for editable columns.
     */
    private void wireVariableTable() {
        view.variablesTable().setItems(model.getVariables());

        view.addVariableButton().setOnAction(e -> openAddVariableDialog());

        view.removeVariableButton()
                .disableProperty()
                .bind(view.variablesTable().getSelectionModel().selectedItemProperty().isNull());

        view.removeVariableButton().setOnAction(e -> {
            final GraphFxVariable selectedVariable = view.variablesTable().getSelectionModel().getSelectedItem();
            if (selectedVariable != null) {
                safe(() -> model.removeVariable(selectedVariable));
            }
        });

        final List<TableColumn<GraphFxVariable, ?>> columns = unsafeVariableColumns(view.variablesTable().getColumns());

        columns.get(0).setOnEditCommit(e -> safe(() -> model.renameVariable(e.getRowValue(), (String) e.getNewValue())));
        columns.get(1).setOnEditCommit(e -> safe(() -> model.setVariableValue(e.getRowValue(), (String) e.getNewValue())));
        columns.get(3).setOnEditCommit(e -> safe(() -> model.setSliderMin(e.getRowValue(), (String) e.getNewValue())));
        columns.get(4).setOnEditCommit(e -> safe(() -> model.setSliderMax(e.getRowValue(), (String) e.getNewValue())));
        columns.get(5).setOnEditCommit(e -> safe(() -> model.setSliderStep(e.getRowValue(), (String) e.getNewValue())));
    }

    /**
     * Ensures there is a selected function if the model contains at least one function and none is selected yet.
     * <p>
     * The selection is synchronized with the functions table selection model.
     * </p>
     */
    private void ensureInitialFunctionSelection() {
        if (model.getSelectedFunction() == null && !model.getFunctions().isEmpty()) {
            model.setSelectedFunction(model.getFunctions().getFirst());
            view.functionsTable().getSelectionModel().selectFirst();
        }
    }

    /**
     * Installs a listener that automatically selects the first function when functions are added
     * and no selection exists.
     */
    private void installAutoSelectFirstFunction() {
        model.getFunctions().addListener((ListChangeListener<GraphFxFunction>) change -> {
            if (model.getSelectedFunction() == null && !model.getFunctions().isEmpty()) {
                model.setSelectedFunction(model.getFunctions().getFirst());
                view.functionsTable().getSelectionModel().selectFirst();
            }
        });
    }

    /**
     * Wires slider rebuilding to variable list and revision changes.
     * <p>
     * The slider panel is rebuilt when:
     * </p>
     * <ul>
     *     <li>variables are added/removed</li>
     *     <li>model revision changes (e.g., slider settings updated)</li>
     * </ul>
     */
    private void wireSliders() {
        model.getVariables().addListener((ListChangeListener<GraphFxVariable>) c -> rebuildSliders());
        model.getRevision().addListener((obs, o, n) -> rebuildSliders());
        rebuildSliders();
    }

    /**
     * Rebuilds the variable slider panel based on the current model state.
     * <p>
     * For each variable with slider enabled, a row is created consisting of:
     * </p>
     * <ul>
     *     <li>a name label</li>
     *     <li>a discrete slider mapped via {@link GraphFxSliderAdapter}</li>
     *     <li>a value label (kept in sync with the variable's value)</li>
     * </ul>
     *
     * <p>
     * While the slider thumb is dragged, interactive mode is enabled in the graph view to reduce sampling cost.
     * </p>
     */
    private void rebuildSliders() {
        view.slidersBox().getChildren().clear();

        for (final GraphFxVariable variable : model.getVariables()) {
            if (!variable.isSliderEnabled()) {
                continue;
            }

            final Label variableNameLabel = new Label(variable.getName());
            final Slider variableSlider = new Slider(0, 1, 0);
            final Label variableValueLabel = new Label(variable.getValueString());

            final GraphFxSliderAdapter adapter = GraphFxSliderAdapter.of(
                    variable.getSliderMin(),
                    variable.getSliderMax(),
                    variable.getSliderStep(),
                    variable.getValue()
            );

            configureSlider(variableSlider, adapter, variable);
            wireSliderToVariable(variableSlider, variableValueLabel, adapter, variable);
            wireVariableToValueLabel(variableValueLabel, variable);

            view.addSliderRow(variable, variableSlider, variableNameLabel, variableValueLabel);
        }
    }

    /**
     * Configures slider bounds and initial position using the {@link GraphFxSliderAdapter}.
     *
     * @param slider   the slider to configure (must not be {@code null})
     * @param adapter  adapter that defines the discrete index range (must not be {@code null})
     * @param variable variable used to compute the initial slider position (must not be {@code null})
     */
    private void configureSlider(
            @NonNull final Slider slider,
            @NonNull final GraphFxSliderAdapter adapter,
            @NonNull final GraphFxVariable variable
    ) {
        slider.setMin(0);
        slider.setMax(adapter.maxIndex());
        slider.setValue(adapter.toIndex(variable.getValue()));
    }

    /**
     * Wires slider interaction to update the model variable value.
     * <p>
     * The slider value is interpreted as an integer index which is converted to a {@link BigDecimal} via
     * {@link GraphFxSliderAdapter#fromIndex(int)}.
     * </p>
     *
     * @param slider         the slider to wire (must not be {@code null})
     * @param valueLabel     the UI label that shows the current value (must not be {@code null})
     * @param adapter        adapter used to map slider indices to decimal values (must not be {@code null})
     * @param targetVariable the variable to update (must not be {@code null})
     */
    private void wireSliderToVariable(
            @NonNull final Slider slider,
            @NonNull final Label valueLabel,
            @NonNull final GraphFxSliderAdapter adapter,
            @NonNull final GraphFxVariable targetVariable
    ) {
        slider.valueChangingProperty()
                .addListener((obs, oldValue, changing) -> graphView.setInteractiveMode(changing));

        slider.valueProperty().addListener((obs, oldValue, newValue) -> safe(() -> {
            final BigDecimal mappedValue = adapter.fromIndex(newValue.intValue());
            final String mappedText = mappedValue.stripTrailingZeros().toPlainString();

            valueLabel.setText(mappedText);
            model.setVariableValue(targetVariable, mappedValue);
        }));
    }

    /**
     * Wires the variable to update the label whenever the variable's value changes (e.g., due to table edits).
     *
     * @param valueLabel the label displaying the variable value (must not be {@code null})
     * @param variable   the variable whose value should be observed (must not be {@code null})
     */
    private void wireVariableToValueLabel(@NonNull final Label valueLabel, @NonNull final GraphFxVariable variable) {
        variable.valueStringProperty().addListener((obs, oldValue, newValue) -> valueLabel.setText(newValue));
    }

    /**
     * Opens a dialog to create a new function and adds it to the model if the user confirms.
     * <p>
     * The dialog validates inputs indirectly via {@link GraphFxModel#addFunction(String, String)} which
     * may throw; such failures are handled through {@link #safe(Runnable)}.
     * </p>
     */
    private void openAddFunctionDialog() {
        final Dialog<FunctionDraft> dialog = new Dialog<>();
        dialog.setTitle("Add function");
        dialog.setHeaderText(null);

        final ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        final GridPane grid = createDialogGrid();

        final String defaultFunctionName = "f" + (model.getFunctions().size() + 1);
        final TextField nameField = new TextField(defaultFunctionName);
        final TextField expressionField = new TextField("x");

        grid.addRow(0, new Label("Name"), nameField);
        grid.addRow(1, new Label("Expression"), expressionField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == addButtonType
                ? new FunctionDraft(nameField.getText(), expressionField.getText())
                : null);

        dialog.showAndWait()
                .ifPresent(draft -> safe(() -> model.addFunction(draft.name().trim(), draft.expression().trim())));
    }

    /**
     * Opens a dialog to create a new variable and adds it to the model if the user confirms.
     * <p>
     * If slider support is enabled by the user, the slider bounds and step are applied and the variable's
     * slider flag is turned on.
     * </p>
     */
    private void openAddVariableDialog() {
        final Dialog<VariableDraft> dialog = new Dialog<>();
        dialog.setTitle("Add variable");
        dialog.setHeaderText(null);

        final ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        final GridPane grid = createDialogGrid();

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
        dialog.setResultConverter(button -> button == addButtonType
                ? new VariableDraft(
                nameField.getText(),
                valueField.getText(),
                sliderEnabledCheckBox.isSelected(),
                minField.getText(),
                maxField.getText(),
                stepField.getText()
        )
                : null);

        dialog.showAndWait().ifPresent(draft -> safe(() -> {
            final GraphFxVariable createdVariable = model.addVariable(
                    draft.name().trim(),
                    new BigDecimal(draft.value().trim())
            );

            if (draft.sliderEnabled()) {
                model.setSliderMin(createdVariable, draft.min());
                model.setSliderMax(createdVariable, draft.max());
                model.setSliderStep(createdVariable, draft.step());
                createdVariable.sliderEnabledProperty().set(true);
            }
        }));
    }

    /**
     * Creates a standard grid layout used by add/edit dialogs.
     *
     * @return a configured {@link GridPane} with spacing and padding
     */
    private GridPane createDialogGrid() {
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(14));
        return grid;
    }

    /**
     * Returns the root node of this controller's view.
     *
     * @return the main window view as a {@link Parent}
     */
    @Override
    public Parent getRoot() {
        return view;
    }

    /**
     * Performs a safe cast of table columns to the expected generic type.
     * <p>
     * JavaFX table APIs expose raw/erased types for columns, so controllers often need a single, well-contained
     * cast. This method isolates the unchecked cast to one place.
     * </p>
     *
     * @param rawColumns raw column list from a {@code TableView}
     * @return the same list typed as {@code List<TableColumn<GraphFxVariable, ?>>}
     */
    @SuppressWarnings("unchecked")
    private static List<TableColumn<GraphFxVariable, ?>> unsafeVariableColumns(final List<?> rawColumns) {
        return (List<TableColumn<GraphFxVariable, ?>>) rawColumns;
    }

}
