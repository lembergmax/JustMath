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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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

        Tooltip.install(graphView, tooltip(
                "Graph canvas",
                "Mouse wheel: Zoom\n" +
                        "Drag: Pan (Move tool)\n" +
                        "Ctrl+Z / Ctrl+Y: Undo/Redo view\n" +
                        "Double-click: Not used"
        ));

        statusLabel.setTooltip(tooltip(
                "Cursor coordinates",
                "Shows the current cursor position in world coordinates."
        ));

        model.getVariables().addListener((ListChangeListener<GraphFxVariable>) c -> rebuildSliders());
        model.revisionProperty().addListener((obs, o, n) -> rebuildSliders());
        rebuildSliders();
    }

    private SplitPane buildSplit() {
        final SplitPane split = new SplitPane();
        split.setDividerPositions(0.36);

        final VBox sidebar = new VBox(12);

        final Node functionsCard = buildFunctionsCard();
        final Node variablesCard = buildVariablesCard();
        final Node slidersCard = buildSlidersCard();

        sidebar.getChildren().addAll(functionsCard, variablesCard, slidersCard);
        VBox.setVgrow(functionsCard, Priority.ALWAYS);

        split.getItems().addAll(sidebar, graphView);
        return split;
    }

    private ToolBar buildToolbar() {
        final ToggleGroup tools = new ToggleGroup();

        final ToggleButton move = toolToggle("Move", GraphFxGraphView.ToolMode.MOVE, tools, true,
                "Move tool",
                "Drag to pan the view.\nMouse wheel zoom works in every tool."
        );

        final ToggleButton zoomBox = toolToggle("Zoom Box", GraphFxGraphView.ToolMode.ZOOM_BOX, tools, false,
                "Zoom box tool",
                "Drag a rectangle to zoom into that region."
        );

        final ToggleButton point = toolToggle("Point", GraphFxGraphView.ToolMode.POINT_ON_FUNCTION, tools, false,
                "Point tool",
                "Click near a function to create a point on that function at the current x-position."
        );

        final ToggleButton tan = toolToggle("Tangent", GraphFxGraphView.ToolMode.TANGENT, tools, false,
                "Tangent tool",
                "Click near a function to create the tangent line at that x-position."
        );

        final ToggleButton normal = toolToggle("Normal", GraphFxGraphView.ToolMode.NORMAL, tools, false,
                "Normal tool",
                "Click near a function to create the normal line at that x-position."
        );

        final ToggleButton root = toolToggle("Root", GraphFxGraphView.ToolMode.ROOT, tools, false,
                "Root tool",
                "Click near a function to search for a root close to the click position."
        );

        final ToggleButton inter = toolToggle("Intersect", GraphFxGraphView.ToolMode.INTERSECTION, tools, false,
                "Intersection tool",
                "Click the first function, then click a second function.\nIntersection points in the visible range will be added."
        );

        final ToggleButton integral = toolToggle("Integral", GraphFxGraphView.ToolMode.INTEGRAL, tools, false,
                "Integral tool",
                "Click near a function to pick it.\nThen drag/release to select an interval.\nThe integral area and value will be added."
        );

        final Button fit = new Button("Fit");
        fit.setTooltip(tooltip(
                "Fit view",
                "Adjust the y-range to fit all visible functions in the current x-range."
        ));
        fit.setOnAction(e -> graphView.fitToData());

        final Button clearMarks = new Button("Clear marks");
        clearMarks.setTooltip(tooltip(
                "Clear marks",
                "Remove all created points, lines and integral objects.\nFunctions and variables stay unchanged."
        ));
        clearMarks.disableProperty().bind(Bindings.isEmpty(model.getObjects()));
        clearMarks.setOnAction(e -> model.clearObjects());

        final CheckBox grid = new CheckBox("Grid");
        grid.setTooltip(tooltip(
                "Grid",
                "Toggle the background grid."
        ));
        grid.selectedProperty().bindBidirectional(model.getSettings().showGridProperty());

        final CheckBox axes = new CheckBox("Axes");
        axes.setTooltip(tooltip(
                "Axes",
                "Toggle x- and y-axes."
        ));
        axes.selectedProperty().bindBidirectional(model.getSettings().showAxesProperty());

        final Button exportPng = new Button("PNG");
        exportPng.setTooltip(tooltip(
                "Export PNG",
                "Export the current view as a PNG image."
        ));
        exportPng.disableProperty().bind(Bindings.isEmpty(model.getFunctions()));
        exportPng.setOnAction(e -> GraphFxExportService.exportPng(graphView));

        final Button exportSvg = new Button("SVG");
        exportSvg.setTooltip(tooltip(
                "Export SVG",
                "Export the selected function as SVG paths (screen space).\nRequires a selected function."
        ));
        exportSvg.disableProperty().bind(model.selectedFunctionProperty().isNull());
        exportSvg.setOnAction(e -> GraphFxExportService.exportSvg(graphView, model));

        final Button exportCsv = new Button("CSV");
        exportCsv.setTooltip(tooltip(
                "Export CSV",
                "Export sampled points of the selected function as CSV.\nRequires a selected function."
        ));
        exportCsv.disableProperty().bind(model.selectedFunctionProperty().isNull());
        exportCsv.setOnAction(e -> GraphFxExportService.exportCsv(graphView, model));

        final Button exportJson = new Button("JSON");
        exportJson.setTooltip(tooltip(
                "Export JSON",
                "Export sampled points of the selected function as JSON.\nRequires a selected function."
        ));
        exportJson.disableProperty().bind(model.selectedFunctionProperty().isNull());
        exportJson.setOnAction(e -> GraphFxExportService.exportJson(graphView, model));

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final ToolBar tb = new ToolBar(
                move, zoomBox,
                new Separator(),
                point, tan, normal, root, inter, integral,
                new Separator(),
                fit, clearMarks,
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

    private ToggleButton toolToggle(
            final String text,
            final GraphFxGraphView.ToolMode mode,
            final ToggleGroup group,
            final boolean selected,
            final String tipTitle,
            final String tipBody
    ) {
        final ToggleButton b = new ToggleButton(text);
        b.setToggleGroup(group);
        b.setSelected(selected);
        b.setTooltip(tooltip(tipTitle, tipBody));
        b.setOnAction(e -> {
            graphView.setToolMode(mode);
            graphView.requestFocus();
        });
        return b;
    }

    private VBox buildFunctionsCard() {
        final VBox card = cardContainer();

        final Label title = new Label("Functions");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");
        title.setTooltip(tooltip(
                "Functions",
                "Define and toggle functions.\nEach function is drawn with its own color."
        ));

        functionsTable.setItems(model.getFunctions());
        functionsTable.setEditable(true);
        functionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        functionsTable.setFixedCellSize(28);
        functionsTable.setPlaceholder(new Label(""));

        Tooltip.install(functionsTable, tooltip(
                "Functions table",
                "Edit cells directly.\nSelect a function to enable exports (SVG/CSV/JSON)."
        ));

        functionsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> model.setSelectedFunction(n));

        final TableColumn<GraphFxFunction, Boolean> visibleCol = new TableColumn<>();
        setHeader(visibleCol, "On", "Toggle visibility of the function.");
        visibleCol.setCellValueFactory(c -> c.getValue().visibleProperty());
        visibleCol.setCellFactory(CheckBoxTableCell.forTableColumn(visibleCol));
        visibleCol.setPrefWidth(60);

        final TableColumn<GraphFxFunction, String> nameCol = new TableColumn<>();
        setHeader(nameCol, "Name", "A short name for the function (e.g., f, g, h).");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> safe(() -> e.getRowValue().setName(e.getNewValue())));
        nameCol.setPrefWidth(90);

        final TableColumn<GraphFxFunction, String> exprCol = new TableColumn<>();
        setHeader(exprCol, "Expression", "The expression to evaluate.\nUse x as the input variable.\nOther variables come from the Variables table.");
        exprCol.setCellValueFactory(c -> c.getValue().expressionProperty());
        exprCol.setCellFactory(TextFieldTableCell.forTableColumn());
        exprCol.setOnEditCommit(e -> safe(() -> e.getRowValue().setExpression(e.getNewValue())));
        exprCol.setPrefWidth(280);

        final TableColumn<GraphFxFunction, Color> colorCol = new TableColumn<>();
        setHeader(colorCol, "Color", "Change the drawing color of the function.");
        colorCol.setCellValueFactory(c -> c.getValue().colorProperty());
        colorCol.setCellFactory(col -> new ColorPickerCell());
        colorCol.setPrefWidth(110);

        functionsTable.getColumns().setAll(visibleCol, nameCol, exprCol, colorCol);
        VBox.setVgrow(functionsTable, Priority.ALWAYS);

        final HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_LEFT);

        final Button add = new Button("Add");
        add.setTooltip(tooltip(
                "Add function",
                "Create a new function.\nA unique color is assigned automatically."
        ));
        add.setOnAction(e -> openAddFunctionDialog());

        final Button remove = new Button("Remove");
        remove.setTooltip(tooltip(
                "Remove function",
                "Remove the selected function."
        ));
        remove.disableProperty().bind(functionsTable.getSelectionModel().selectedItemProperty().isNull());
        remove.setOnAction(e -> {
            final GraphFxFunction sel = functionsTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                safe(() -> model.removeFunction(sel));
            }
        });

        buttons.getChildren().addAll(add, remove);
        card.getChildren().addAll(title, functionsTable, buttons);
        return card;
    }

    private VBox buildVariablesCard() {
        final VBox card = cardContainer();

        final Label title = new Label("Variables");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");
        title.setTooltip(tooltip(
                "Variables",
                "Define numeric variables used by your functions.\nYou can optionally enable a slider for interactive exploration."
        ));

        variablesTable.setItems(model.getVariables());
        variablesTable.setEditable(true);
        variablesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        variablesTable.setFixedCellSize(28);
        variablesTable.setPlaceholder(new Label(""));

        Tooltip.install(variablesTable, tooltip(
                "Variables table",
                "Edit cells directly.\nEnable “Slider” to show the variable in the slider panel."
        ));

        final StringConverter<String> str = new StringConverter<>() {
            @Override public String toString(final String object) { return object; }
            @Override public String fromString(final String string) { return string; }
        };

        final TableColumn<GraphFxVariable, String> nameCol = new TableColumn<>();
        setHeader(nameCol, "Name", "Variable name (letters, digits, underscore).\nThe name 'x' is reserved for function input.");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn(str));
        nameCol.setOnEditCommit(e -> safe(() -> model.renameVariable(e.getRowValue(), e.getNewValue())));
        nameCol.setPrefWidth(90);

        final TableColumn<GraphFxVariable, String> valueCol = new TableColumn<>();
        setHeader(valueCol, "Value", "Current value of the variable.\nMust be a valid number.");
        valueCol.setCellValueFactory(c -> c.getValue().valueStringProperty());
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(e -> safe(() -> model.setVariableValue(e.getRowValue(), e.getNewValue())));
        valueCol.setPrefWidth(120);

        final TableColumn<GraphFxVariable, Boolean> sliderCol = new TableColumn<>();
        setHeader(sliderCol, "Slider", "Enable a slider for this variable.");
        sliderCol.setCellValueFactory(c -> c.getValue().sliderEnabledProperty());
        sliderCol.setCellFactory(CheckBoxTableCell.forTableColumn(sliderCol));
        sliderCol.setPrefWidth(80);

        final TableColumn<GraphFxVariable, String> minCol = new TableColumn<>();
        setHeader(minCol, "Min", "Minimum slider value.");
        minCol.setCellValueFactory(c -> c.getValue().sliderMinStringProperty());
        minCol.setCellFactory(TextFieldTableCell.forTableColumn());
        minCol.setOnEditCommit(e -> safe(() -> model.setSliderMin(e.getRowValue(), e.getNewValue())));
        minCol.setPrefWidth(95);

        final TableColumn<GraphFxVariable, String> maxCol = new TableColumn<>();
        setHeader(maxCol, "Max", "Maximum slider value.");
        maxCol.setCellValueFactory(c -> c.getValue().sliderMaxStringProperty());
        maxCol.setCellFactory(TextFieldTableCell.forTableColumn());
        maxCol.setOnEditCommit(e -> safe(() -> model.setSliderMax(e.getRowValue(), e.getNewValue())));
        maxCol.setPrefWidth(95);

        final TableColumn<GraphFxVariable, String> stepCol = new TableColumn<>();
        setHeader(stepCol, "Step", "Slider step size.\nMust be > 0.");
        stepCol.setCellValueFactory(c -> c.getValue().sliderStepStringProperty());
        stepCol.setCellFactory(TextFieldTableCell.forTableColumn());
        stepCol.setOnEditCommit(e -> safe(() -> model.setSliderStep(e.getRowValue(), e.getNewValue())));
        stepCol.setPrefWidth(95);

        variablesTable.getColumns().setAll(nameCol, valueCol, sliderCol, minCol, maxCol, stepCol);
        variablesTable.setPrefHeight(220);

        final HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_LEFT);

        final Button add = new Button("Add");
        add.setTooltip(tooltip(
                "Add variable",
                "Create a new variable.\nYou can enable a slider and configure min/max/step."
        ));
        add.setOnAction(e -> openAddVariableDialog());

        final Button remove = new Button("Remove");
        remove.setTooltip(tooltip(
                "Remove variable",
                "Remove the selected variable."
        ));
        remove.disableProperty().bind(variablesTable.getSelectionModel().selectedItemProperty().isNull());
        remove.setOnAction(e -> {
            final GraphFxVariable sel = variablesTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                safe(() -> model.removeVariable(sel));
            }
        });

        buttons.getChildren().addAll(add, remove);

        card.getChildren().addAll(title, variablesTable, buttons);
        return card;
    }

    private VBox buildSlidersCard() {
        final VBox card = cardContainer();

        final Label title = new Label("Sliders");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");
        title.setTooltip(tooltip(
                "Sliders",
                "Only variables with “Slider = true” appear here.\nDragging a slider uses fast preview rendering."
        ));

        slidersBox.setPadding(new Insets(6));

        final ScrollPane sc = new ScrollPane(slidersBox);
        sc.setFitToWidth(true);
        sc.setPrefViewportHeight(210);
        sc.setStyle("-fx-background-color:transparent;");
        sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Tooltip.install(sc, tooltip(
                "Slider panel",
                "Drag to change values.\nThe graph updates in real time."
        ));

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
            row.setAlignment(Pos.CENTER_LEFT);

            final Label name = new Label(v.getName());
            name.setPrefWidth(40);
            name.setTooltip(tooltip(
                    "Variable: " + v.getName(),
                    "Value updates the graph.\nConfigure min/max/step in the Variables table."
            ));

            final Slider slider = new Slider(0, 1, 0);
            slider.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(slider, Priority.ALWAYS);

            final Label value = new Label(v.getValueString());
            value.setPrefWidth(100);
            value.setAlignment(Pos.CENTER_RIGHT);
            value.setTooltip(tooltip(
                    "Current value",
                    "This is the current numeric value of the variable."
            ));

            final GraphFxSliderAdapter adapter = GraphFxSliderAdapter.of(
                    v.getSliderMin(), v.getSliderMax(), v.getSliderStep(), v.getValue()
            );

            slider.setMin(0);
            slider.setMax(adapter.maxIndex());
            slider.setValue(adapter.toIndex(v.getValue()));

            slider.setTooltip(tooltip(
                    "Slider: " + v.getName(),
                    "Min: " + v.getSliderMin().stripTrailingZeros().toPlainString() + "\n" +
                            "Max: " + v.getSliderMax().stripTrailingZeros().toPlainString() + "\n" +
                            "Step: " + v.getSliderStep().stripTrailingZeros().toPlainString()
            ));

            slider.valueChangingProperty().addListener((obs, o, changing) -> graphView.setInteractiveMode(changing));
            slider.valueProperty().addListener((obs, o, n) -> safe(() -> {
                final BigDecimal newVal = adapter.fromIndex(n.intValue());
                value.setText(newVal.stripTrailingZeros().toPlainString());
                model.setVariableValue(v, newVal);
            }));

            v.valueStringProperty().addListener((obs, o, n) -> value.setText(n));

            row.getChildren().addAll(name, slider, value);
            slidersBox.getChildren().add(row);
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
        grid.setPadding(new Insets(14));

        final String defaultName = "f" + (model.getFunctions().size() + 1);

        final TextField nameField = new TextField(defaultName);
        nameField.setTooltip(tooltip("Name", "A short name for the function (e.g., f, g, h)."));

        final TextField exprField = new TextField("x");
        exprField.setTooltip(tooltip(
                "Expression",
                "Use x as the input variable.\nExample: sin(x) + x^2\nVariables come from the Variables table."
        ));

        final Label nameLbl = new Label("Name");
        nameLbl.setTooltip(nameField.getTooltip());

        final Label exprLbl = new Label("Expression");
        exprLbl.setTooltip(exprField.getTooltip());

        grid.addRow(0, nameLbl, nameField);
        grid.addRow(1, exprLbl, exprField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != add) return null;
            return new FunctionDraft(nameField.getText(), exprField.getText());
        });

        dialog.showAndWait().ifPresent(draft -> safe(() -> {
            model.addFunction(draft.name().trim(), draft.expression().trim());
        }));
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
        grid.setPadding(new Insets(14));

        final TextField nameField = new TextField(model.nextSuggestedVariableName());
        nameField.setTooltip(tooltip("Name", "Variable name (letters, digits, underscore). The name 'x' is reserved."));

        final TextField valueField = new TextField("0");
        valueField.setTooltip(tooltip("Value", "Initial numeric value."));

        final CheckBox sliderEnabled = new CheckBox("Slider");
        sliderEnabled.setTooltip(tooltip("Slider", "Enable a slider for this variable."));

        final TextField minField = new TextField("-10");
        minField.setTooltip(tooltip("Min", "Minimum slider value."));

        final TextField maxField = new TextField("10");
        maxField.setTooltip(tooltip("Max", "Maximum slider value."));

        final TextField stepField = new TextField("0.1");
        stepField.setTooltip(tooltip("Step", "Slider step size. Must be > 0."));

        final Label nameLbl = new Label("Name");
        nameLbl.setTooltip(nameField.getTooltip());
        final Label valueLbl = new Label("Value");
        valueLbl.setTooltip(valueField.getTooltip());
        final Label sliderLbl = new Label("Slider");
        sliderLbl.setTooltip(sliderEnabled.getTooltip());
        final Label minLbl = new Label("Min");
        minLbl.setTooltip(minField.getTooltip());
        final Label maxLbl = new Label("Max");
        maxLbl.setTooltip(maxField.getTooltip());
        final Label stepLbl = new Label("Step");
        stepLbl.setTooltip(stepField.getTooltip());

        minField.disableProperty().bind(sliderEnabled.selectedProperty().not());
        maxField.disableProperty().bind(sliderEnabled.selectedProperty().not());
        stepField.disableProperty().bind(sliderEnabled.selectedProperty().not());

        grid.addRow(0, nameLbl, nameField);
        grid.addRow(1, valueLbl, valueField);
        grid.addRow(2, sliderLbl, sliderEnabled);
        grid.addRow(3, minLbl, minField);
        grid.addRow(4, maxLbl, maxField);
        grid.addRow(5, stepLbl, stepField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != add) return null;
            return new VariableDraft(
                    nameField.getText(),
                    valueField.getText(),
                    sliderEnabled.isSelected(),
                    minField.getText(),
                    maxField.getText(),
                    stepField.getText()
            );
        });

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

    private static VBox cardContainer() {
        final VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: -fx-control-inner-background;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: rgba(0,0,0,0.08);"
        );
        return card;
    }

    private static Tooltip tooltip(final String title, final String body) {
        final Tooltip t = new Tooltip(title + "\n" + body);
        t.setWrapText(true);
        t.setMaxWidth(420);
        return t;
    }

    private static void setHeader(final TableColumn<?, ?> col, final String text, final String tooltipText) {
        final Label lbl = new Label(text);
        lbl.setTooltip(tooltip(text, tooltipText));
        col.setText(null);
        col.setGraphic(lbl);
    }

    private void safe(final Runnable r) {
        try {
            r.run();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(final String message) {
        final Alert a = new Alert(Alert.AlertType.ERROR, message == null ? "Unknown error." : message, ButtonType.OK);
        a.setHeaderText("Operation failed");
        a.showAndWait();
    }

    private record FunctionDraft(String name, String expression) {
    }

    private record VariableDraft(String name, String value, boolean sliderEnabled, String min, String max, String step) {
    }

    private static final class ColorPickerCell extends TableCell<GraphFxFunction, Color> {
        private final ColorPicker picker = new ColorPicker();

        private ColorPickerCell() {
            picker.setTooltip(tooltip("Function color", "Pick a color for this function."));
            picker.setOnAction(e -> {
                final GraphFxFunction item = getTableRow() == null ? null : (GraphFxFunction) getTableRow().getItem();
                if (item != null) {
                    item.setColor(picker.getValue());
                }
            });
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
