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

package com.mlprograms.justmath.graph.fx.view;

import com.mlprograms.justmath.graph.fx.model.GraphFxFunction;
import com.mlprograms.justmath.graph.fx.model.GraphFxVariable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/**
 * Main window (editable) view.
 * Pure UI: builds layout and exposes controls to controllers.
 */
public class MainWindowView extends BorderPane {

    private static final double SIDEBAR_WIDTH = 440;

    private final TableView<GraphFxFunction> functionsTable = new TableView<>();
    private final Button addFunctionButton = new Button("Add");
    private final Button removeFunctionButton = new Button("Remove");

    private final TableView<GraphFxVariable> variablesTable = new TableView<>();
    private final Button addVariableButton = new Button("Add");
    private final Button removeVariableButton = new Button("Remove");

    private final VBox slidersBox = new VBox(10);

    private final ScrollPane sidebarScroll;

    public MainWindowView(final GraphToolbarView toolbar, final GraphFxGraphView graphView) {
        getStyleClass().add("graphfx-root");

        setPadding(new Insets(10));
        setTop(toolbar);
        setCenter(buildContent(graphView));

        sidebarScroll = (ScrollPane) ((HBox) getCenter()).getChildren().getFirst();
    }

    public TableView<GraphFxFunction> functionsTable() { return functionsTable; }
    public Button addFunctionButton() { return addFunctionButton; }
    public Button removeFunctionButton() { return removeFunctionButton; }

    public TableView<GraphFxVariable> variablesTable() { return variablesTable; }
    public Button addVariableButton() { return addVariableButton; }
    public Button removeVariableButton() { return removeVariableButton; }

    public VBox slidersBox() { return slidersBox; }

    public ScrollPane sidebarScroll() { return sidebarScroll; }

    private Node buildContent(final GraphFxGraphView graphView) {
        final VBox sidebarContent = new VBox(12);
        sidebarContent.getChildren().addAll(buildFunctionsCard(), buildVariablesCard(), buildSlidersCard());
        sidebarContent.setFillWidth(true);

        final ScrollPane sidebar = new ScrollPane(sidebarContent);
        sidebar.getStyleClass().add("graphfx-sidebar");
        sidebar.setFitToWidth(true);
        sidebar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebar.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMinWidth(SIDEBAR_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_WIDTH);

        final HBox content = new HBox(12, sidebar, graphView);
        HBox.setHgrow(graphView, Priority.ALWAYS);
        return content;
    }

    private VBox buildFunctionsCard() {
        final VBox card = cardContainer();

        final Label title = new Label("Functions");
        title.getStyleClass().add("graphfx-card-title");

        final Label hint = new Label(
                "Create and manage functions that will be drawn on the coordinate plane.\n" +
                        "Use x as the input variable. Other variables (a, b, c, …) come from the Variables tab.\n"
        );
        hint.setWrapText(true);
        hint.getStyleClass().add("graphfx-hint");

        functionsTable.setEditable(true);
        functionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        functionsTable.setFixedCellSize(30);
        functionsTable.setPlaceholder(new Label(""));

        final TableColumn<GraphFxFunction, Boolean> visibleCol = new TableColumn<>();
        setHeader(visibleCol, "On", "Toggle function visibility.");
        visibleCol.setCellValueFactory(c -> c.getValue().visibleProperty());
        visibleCol.setCellFactory(CheckBoxTableCell.forTableColumn(visibleCol));
        visibleCol.setPrefWidth(62);

        final TableColumn<GraphFxFunction, String> nameCol = new TableColumn<>();
        setHeader(nameCol, "Name", "Short function name (e.g., f, g, h).");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setPrefWidth(95);

        final TableColumn<GraphFxFunction, String> exprCol = new TableColumn<>();
        setHeader(exprCol, "Expression", "Expression to evaluate.\nUse x as input.\nOther variables are defined in the Variables tab.");
        exprCol.setCellValueFactory(c -> c.getValue().expressionProperty());
        exprCol.setCellFactory(TextFieldTableCell.forTableColumn());
        exprCol.setPrefWidth(320);

        final TableColumn<GraphFxFunction, Color> colorCol = new TableColumn<>();
        setHeader(colorCol, "Color", "Pick a color for this function.");
        colorCol.setCellValueFactory(c -> c.getValue().colorProperty());
        colorCol.setCellFactory(col -> new ColorSwatchCell());
        colorCol.setPrefWidth(90);

        functionsTable.getColumns().setAll(visibleCol, nameCol, exprCol, colorCol);
        VBox.setVgrow(functionsTable, Priority.ALWAYS);

        final HBox buttons = new HBox(8, addFunctionButton, removeFunctionButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        addFunctionButton.setTooltip(tooltip("Add function", "Create a new function.\nA unique color is assigned automatically."));
        removeFunctionButton.setTooltip(tooltip("Remove function", "Remove the selected function."));

        card.getChildren().addAll(title, hint, functionsTable, buttons);
        return card;
    }

    private VBox buildVariablesCard() {
        final VBox card = cardContainer();

        final Label title = new Label("Variables");
        title.getStyleClass().add("graphfx-card-title");

        final Label hint = new Label(
                "Define variables used inside your function expressions\n" +
                        "If “Slider” is enabled, the variable appears in the Sliders tab for interactive control.\n"
        );
        hint.setWrapText(true);
        hint.getStyleClass().add("graphfx-hint");

        variablesTable.setEditable(true);
        variablesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        variablesTable.setFixedCellSize(30);
        variablesTable.setPlaceholder(new Label(""));

        final StringConverter<String> str = new StringConverter<>() {
            @Override public String toString(final String object) { return object; }
            @Override public String fromString(final String string) { return string; }
        };

        final TableColumn<GraphFxVariable, String> nameCol = new TableColumn<>();
        setHeader(nameCol, "Name", "Variable name (letters, digits, underscore).\nThe name 'x' is reserved.");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn(str));
        nameCol.setPrefWidth(95);

        final TableColumn<GraphFxVariable, String> valueCol = new TableColumn<>();
        setHeader(valueCol, "Value", "Current numeric value.");
        valueCol.setCellValueFactory(c -> c.getValue().valueStringProperty());
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setPrefWidth(120);

        final TableColumn<GraphFxVariable, Boolean> sliderCol = new TableColumn<>();
        setHeader(sliderCol, "Slider", "Enable a slider for this variable.");
        sliderCol.setCellValueFactory(c -> c.getValue().sliderEnabledProperty());
        sliderCol.setCellFactory(CheckBoxTableCell.forTableColumn(sliderCol));
        sliderCol.setPrefWidth(85);

        final TableColumn<GraphFxVariable, String> minCol = new TableColumn<>();
        setHeader(minCol, "Min", "Minimum slider value.");
        minCol.setCellValueFactory(c -> c.getValue().sliderMinStringProperty());
        minCol.setCellFactory(TextFieldTableCell.forTableColumn());
        minCol.setPrefWidth(95);

        final TableColumn<GraphFxVariable, String> maxCol = new TableColumn<>();
        setHeader(maxCol, "Max", "Maximum slider value.");
        maxCol.setCellValueFactory(c -> c.getValue().sliderMaxStringProperty());
        maxCol.setCellFactory(TextFieldTableCell.forTableColumn());
        maxCol.setPrefWidth(95);

        final TableColumn<GraphFxVariable, String> stepCol = new TableColumn<>();
        setHeader(stepCol, "Step", "Slider step size (must be > 0).");
        stepCol.setCellValueFactory(c -> c.getValue().sliderStepStringProperty());
        stepCol.setCellFactory(TextFieldTableCell.forTableColumn());
        stepCol.setPrefWidth(95);

        variablesTable.getColumns().setAll(nameCol, valueCol, sliderCol, minCol, maxCol, stepCol);
        variablesTable.setPrefHeight(240);

        final HBox buttons = new HBox(8, addVariableButton, removeVariableButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        addVariableButton.setTooltip(tooltip("Add variable", "Create a new variable.\nOptionally enable a slider and configure min/max/step."));
        removeVariableButton.setTooltip(tooltip("Remove variable", "Remove the selected variable."));

        card.getChildren().addAll(title, hint, variablesTable, buttons);
        return card;
    }

    private VBox buildSlidersCard() {
        final VBox card = cardContainer();

        final Label title = new Label("Sliders");
        title.getStyleClass().add("graphfx-card-title");

        final Label hint = new Label(
                "Sliders are an easy way to explore how variables affect your functions.\n" +
                        "Enable a slider in the Variables tab, then drag it here to update the graph in real time."
        );
        hint.setWrapText(true);
        hint.getStyleClass().add("graphfx-hint");

        // Etwas mehr Luft innerhalb der Sliders-Liste
        slidersBox.setSpacing(8);
        slidersBox.setPadding(new Insets(8));

        final ScrollPane sc = new ScrollPane(slidersBox);
        sc.setFitToWidth(true);
        sc.setPrefViewportHeight(210);
        sc.getStyleClass().add("graphfx-sliders-scroll");
        sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Leichter Abstand zwischen Titel, Hint und Inhalt
        card.setSpacing(10);
        card.getChildren().addAll(title, hint, sc);
        return card;
    }

    public void addSliderRow(final GraphFxVariable v, final Slider slider, final Label nameLabel, final Label valueLabel) {
        final HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        nameLabel.getStyleClass().add("graphfx-slider-name");
        valueLabel.getStyleClass().add("graphfx-slider-value");

        nameLabel.setMinWidth(40);
        nameLabel.setPrefWidth(40);
        nameLabel.setMaxWidth(40);

        valueLabel.setMinWidth(90);
        valueLabel.setPrefWidth(90);
        valueLabel.setMaxWidth(90);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);

        HBox.setHgrow(slider, Priority.ALWAYS);

        row.getChildren().addAll(nameLabel, slider, valueLabel);
        slidersBox.getChildren().add(row);
    }

    private static VBox cardContainer() {
        final VBox card = new VBox(8);
        // kleines, konsistentes Padding rund um den Card-Inhalt
        card.setPadding(new Insets(12));
        card.getStyleClass().add("graphfx-card");
        return card;
    }

    private static Tooltip tooltip(final String title, final String body) {
        final Tooltip t = new Tooltip(title + "\n" + body);
        t.setWrapText(true);
        t.setMaxWidth(520);
        return t;
    }

    private static void setHeader(final TableColumn<?, ?> col, final String text, final String tooltipText) {
        final Label lbl = new Label(text);
        lbl.getStyleClass().add("graphfx-header");
        lbl.setTooltip(tooltip(text, tooltipText));
        col.setText(null);
        col.setGraphic(lbl);
    }

    private static final class ColorSwatchCell extends TableCell<GraphFxFunction, Color> {
        private final ColorPicker picker = new ColorPicker();

        private ColorSwatchCell() {
            picker.getStyleClass().add("graphfx-color-swatch");
            picker.setMinWidth(44);
            picker.setPrefWidth(44);
            picker.setMaxWidth(44);

            picker.setOnAction(e -> {
                final Object rowItem = getTableRow() == null ? null : getTableRow().getItem();
                if (rowItem instanceof GraphFxFunction fn) {
                    fn.setColor(picker.getValue());
                }
            });

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(final Color color, final boolean empty) {
            super.updateItem(color, empty);
            if (empty) {
                setGraphic(null);
                return;
            }
            picker.setValue(color == null ? Color.DODGERBLUE : color);
            setGraphic(picker);
        }
    }
}
