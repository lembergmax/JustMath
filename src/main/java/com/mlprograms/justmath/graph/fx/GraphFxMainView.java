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

package com.mlprograms.justmath.graph.fx;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.Locale;

public class GraphFxMainView extends BorderPane {

    private final GraphFxModel model;
    private final CalculatorEngine engine;

    private final GraphFxGraphView graphView;
    private final Label statusLabel = new Label("x=0   y=0");

    private final TableView<GraphFxFunction> functionsTable = new TableView<>();
    private final TableView<GraphFxVariable> variablesTable = new TableView<>();
    private final VBox slidersBox = new VBox(10);

    public GraphFxMainView(final GraphFxModel model, final CalculatorEngine engine) {
        this.model = model;
        this.engine = engine;
        this.graphView = new GraphFxGraphView(model, engine);

        setPadding(new Insets(10));
        setTop(buildToolbar());
        setCenter(buildSplit());

        graphView.setStatusListener((x, y) -> statusLabel.setText(
                "x=" + String.format(Locale.ROOT, "%.6f", x) + "   y=" + String.format(Locale.ROOT, "%.6f", y)
        ));

        model.getVariables().addListener((ListChangeListener<GraphFxVariable>) c -> rebuildSliders());
        model.revisionProperty().addListener((obs, o, n) -> rebuildSliders());
        rebuildSliders();
    }

    private SplitPane buildSplit() {
        final SplitPane split = new SplitPane();
        split.setDividerPositions(0.34);

        final VBox sidebar = new VBox(12);
        sidebar.getChildren().addAll(buildFunctionsCard(), buildVariablesCard(), buildSlidersCard());
        VBox.setVgrow(buildFunctionsCard(), Priority.ALWAYS);

        split.getItems().addAll(sidebar, graphView);
        return split;
    }

    private ToolBar buildToolbar() {
        final ToggleGroup tools = new ToggleGroup();

        final ToggleButton move = toolToggle("Move", GraphFxGraphView.ToolMode.MOVE, tools, true);
        final ToggleButton zoomBox = toolToggle("Zoom Box", GraphFxGraphView.ToolMode.ZOOM_BOX, tools, false);
        final ToggleButton point = toolToggle("Point", GraphFxGraphView.ToolMode.POINT_ON_FUNCTION, tools, false);
        final ToggleButton tan = toolToggle("Tangent", GraphFxGraphView.ToolMode.TANGENT, tools, false);
        final ToggleButton normal = toolToggle("Normal", GraphFxGraphView.ToolMode.NORMAL, tools, false);
        final ToggleButton root = toolToggle("Root", GraphFxGraphView.ToolMode.ROOT, tools, false);
        final ToggleButton inter = toolToggle("Intersect", GraphFxGraphView.ToolMode.INTERSECTION, tools, false);
        final ToggleButton integral = toolToggle("Integral", GraphFxGraphView.ToolMode.INTEGRAL, tools, false);

        final Button fit = new Button("Fit");
        fit.setOnAction(e -> graphView.fitToData());

        final CheckBox grid = new CheckBox("Grid");
        grid.selectedProperty().bindBidirectional(model.getSettings().showGridProperty());

        final CheckBox axes = new CheckBox("Axes");
        axes.selectedProperty().bindBidirectional(model.getSettings().showAxesProperty());

        final Button exportPng = new Button("PNG");
        exportPng.disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        exportPng.setOnAction(e -> GraphFxExportService.exportPng(graphView));

        final Button exportCsv = new Button("CSV");
        exportCsv.disableProperty().bind(model.selectedFunctionProperty().isNull());
        exportCsv.setOnAction(e -> GraphFxExportService.exportCsv(graphView, model));

        final Button exportJson = new Button("JSON");
        exportJson.disableProperty().bind(model.selectedFunctionProperty().isNull());
        exportJson.setOnAction(e -> GraphFxExportService.exportJson(graphView, model));

        final Button exportSvg = new Button("SVG");
        exportSvg.disableProperty().bind(model.selectedFunctionProperty().isNull());
        exportSvg.setOnAction(e -> GraphFxExportService.exportSvg(graphView, model));

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final ToolBar tb = new ToolBar(
                move, zoomBox,
                new Separator(),
                point, tan, normal, root, inter, integral,
                new Separator(),
                fit,
                new Separator(),
                grid, axes,
                new Separator(),
                exportPng, exportSvg, exportCsv, exportJson,
                spacer,
                statusLabel
        );

        tb.setPadding(new Insets(6));
        return tb;
    }

    private ToggleButton toolToggle(final String text, final GraphFxGraphView.ToolMode mode, final ToggleGroup group, final boolean selected) {
        final ToggleButton b = new ToggleButton(text);
        b.setToggleGroup(group);
        b.setSelected(selected);
        b.setOnAction(e -> {
            graphView.setToolMode(mode);
            graphView.requestFocus();
        });
        return b;
    }

    private VBox buildFunctionsCard() {
        final VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: -fx-control-inner-background; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(0,0,0,0.08);");

        final Label title = new Label("Functions");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");

        functionsTable.setItems(model.getFunctions());
        functionsTable.setEditable(true);
        functionsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> model.setSelectedFunction(n));

        final TableColumn<GraphFxFunction, Boolean> visibleCol = new TableColumn<>("On");
        visibleCol.setCellValueFactory(c -> c.getValue().visibleProperty());
        visibleCol.setCellFactory(CheckBoxTableCell.forTableColumn(visibleCol));
        visibleCol.setPrefWidth(50);

        final TableColumn<GraphFxFunction, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setPrefWidth(90);

        final TableColumn<GraphFxFunction, String> exprCol = new TableColumn<>("Expression");
        exprCol.setCellValueFactory(c -> c.getValue().expressionProperty());
        exprCol.setCellFactory(TextFieldTableCell.forTableColumn());
        exprCol.setPrefWidth(230);

        final TableColumn<GraphFxFunction, String> colorCol = new TableColumn<>("Color");
        colorCol.setCellValueFactory(c -> c.getValue().colorHexProperty());
        colorCol.setPrefWidth(80);

        functionsTable.getColumns().setAll(visibleCol, nameCol, exprCol, colorCol);
        VBox.setVgrow(functionsTable, Priority.ALWAYS);

        final HBox buttons = new HBox(8);
        final Button add = new Button("Add");
        add.setOnAction(e -> model.addFunction("f" + (model.getFunctions().size() + 1), "x"));

        final Button remove = new Button("Remove");
        remove.disableProperty().bind(functionsTable.getSelectionModel().selectedItemProperty().isNull());
        remove.setOnAction(e -> {
            final GraphFxFunction sel = functionsTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                model.removeFunction(sel);
            }
        });

        buttons.getChildren().addAll(add, remove);

        card.getChildren().addAll(title, functionsTable, buttons);
        return card;
    }

    private VBox buildVariablesCard() {
        final VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: -fx-control-inner-background; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(0,0,0,0.08);");

        final Label title = new Label("Variables");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");

        variablesTable.setItems(model.getVariables());
        variablesTable.setEditable(true);

        final StringConverter<String> str = new StringConverter<>() {
            @Override public String toString(final String object) { return object; }
            @Override public String fromString(final String string) { return string; }
        };

        final TableColumn<GraphFxVariable, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn(str));
        nameCol.setOnEditCommit(e -> model.renameVariable(e.getRowValue(), e.getNewValue()));
        nameCol.setPrefWidth(90);

        final TableColumn<GraphFxVariable, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(c -> c.getValue().valueStringProperty());
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(e -> model.setVariableValue(e.getRowValue(), e.getNewValue()));
        valueCol.setPrefWidth(110);

        final TableColumn<GraphFxVariable, Boolean> sliderCol = new TableColumn<>("Slider");
        sliderCol.setCellValueFactory(c -> c.getValue().sliderEnabledProperty());
        sliderCol.setCellFactory(CheckBoxTableCell.forTableColumn(sliderCol));
        sliderCol.setPrefWidth(70);

        final TableColumn<GraphFxVariable, String> minCol = new TableColumn<>("Min");
        minCol.setCellValueFactory(c -> c.getValue().sliderMinStringProperty());
        minCol.setCellFactory(TextFieldTableCell.forTableColumn());
        minCol.setOnEditCommit(e -> model.setSliderMin(e.getRowValue(), e.getNewValue()));
        minCol.setPrefWidth(90);

        final TableColumn<GraphFxVariable, String> maxCol = new TableColumn<>("Max");
        maxCol.setCellValueFactory(c -> c.getValue().sliderMaxStringProperty());
        maxCol.setCellFactory(TextFieldTableCell.forTableColumn());
        maxCol.setOnEditCommit(e -> model.setSliderMax(e.getRowValue(), e.getNewValue()));
        maxCol.setPrefWidth(90);

        final TableColumn<GraphFxVariable, String> stepCol = new TableColumn<>("Step");
        stepCol.setCellValueFactory(c -> c.getValue().sliderStepStringProperty());
        stepCol.setCellFactory(TextFieldTableCell.forTableColumn());
        stepCol.setOnEditCommit(e -> model.setSliderStep(e.getRowValue(), e.getNewValue()));
        stepCol.setPrefWidth(90);

        variablesTable.getColumns().setAll(nameCol, valueCol, sliderCol, minCol, maxCol, stepCol);
        variablesTable.setPrefHeight(180);

        final HBox buttons = new HBox(8);
        final Button add = new Button("Add");
        add.setOnAction(e -> model.addVariable("k", BigDecimal.ZERO));

        final Button remove = new Button("Remove");
        remove.disableProperty().bind(variablesTable.getSelectionModel().selectedItemProperty().isNull());
        remove.setOnAction(e -> {
            final GraphFxVariable sel = variablesTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                model.removeVariable(sel);
            }
        });

        buttons.getChildren().addAll(add, remove);

        card.getChildren().addAll(title, variablesTable, buttons);
        return card;
    }

    private VBox buildSlidersCard() {
        final VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: -fx-control-inner-background; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(0,0,0,0.08);");

        final Label title = new Label("Sliders");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");

        slidersBox.setPadding(new Insets(6));
        final ScrollPane sc = new ScrollPane(slidersBox);
        sc.setFitToWidth(true);
        sc.setPrefViewportHeight(200);
        sc.setStyle("-fx-background-color:transparent;");

        card.getChildren().addAll(title, sc);
        return card;
    }

    private void rebuildSliders() {
        slidersBox.getChildren().clear();

        for (final GraphFxVariable v : model.getVariables()) {
            if (!v.isSliderEnabled()) {
                continue;
            }

            final HBox row = new HBox(10);
            row.setPadding(new Insets(4, 0, 4, 0));

            final Label name = new Label(v.getName());
            name.setPrefWidth(30);

            final Slider slider = new Slider(0, 1, 0);
            slider.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(slider, Priority.ALWAYS);

            final Label value = new Label(v.getValueString());
            value.setPrefWidth(90);

            final GraphFxSliderAdapter adapter = GraphFxSliderAdapter.of(
                    v.getSliderMin(), v.getSliderMax(), v.getSliderStep(), v.getValue()
            );

            slider.setMin(0);
            slider.setMax(adapter.maxIndex());
            slider.setValue(adapter.toIndex(v.getValue()));

            slider.valueChangingProperty().addListener((obs, o, changing) -> graphView.setInteractiveMode(changing));
            slider.valueProperty().addListener((obs, o, n) -> {
                final BigDecimal newVal = adapter.fromIndex(n.intValue());
                value.setText(newVal.stripTrailingZeros().toPlainString());
                model.setVariableValue(v, newVal);
            });

            v.valueStringProperty().addListener((obs, o, n) -> value.setText(n));

            row.getChildren().addAll(name, slider, value);
            slidersBox.getChildren().add(row);
        }
    }
}
