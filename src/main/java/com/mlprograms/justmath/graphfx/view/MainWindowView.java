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

package com.mlprograms.justmath.graphfx.view;

import com.mlprograms.justmath.graphfx.model.GraphFxFunction;
import com.mlprograms.justmath.graphfx.model.GraphFxVariable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import lombok.NonNull;

/**
 * Builds the editable GraphFx main window user interface.
 * <p>
 * This class follows a "View-only" responsibility: it creates and arranges JavaFX controls,
 * provides accessors to key nodes (tables and buttons), and offers helper methods to render
 * a slider row in a consistent layout.
 * <p>
 * It intentionally does not:
 * <ul>
 *     <li>validate user input (expressions, variable names, numeric ranges)</li>
 *     <li>perform calculations or model updates outside of direct UI binding</li>
 *     <li>decide application behavior (what happens when a button is clicked)</li>
 * </ul>
 * Those responsibilities belong to the controller layer.
 *
 * <h2>Layout</h2>
 * The view consists of:
 * <ul>
 *     <li>a toolbar at the top (provided externally)</li>
 *     <li>a fixed-width sidebar containing cards for functions, variables, and sliders</li>
 *     <li>a graph view area filling the remaining space</li>
 * </ul>
 *
 * <h2>Styling</h2>
 * The UI uses CSS style classes (e.g. {@code graphfx-root}, {@code graphfx-card}, {@code graphfx-hint}).
 * Styling is expected to be provided via an external stylesheet.
 *
 * <h2>Threading</h2>
 * Like all JavaFX UI code, this view must be constructed and accessed on the JavaFX Application Thread.
 */
public final class MainWindowView extends BorderPane {

    private static final double SIDEBAR_WIDTH = 440;

    private static final double FUNCTIONS_VISIBLE_COL_WIDTH = 62;
    private static final double FUNCTIONS_NAME_COL_WIDTH = 95;
    private static final double FUNCTIONS_EXPR_COL_WIDTH = 320;
    private static final double FUNCTIONS_COLOR_COL_WIDTH = 90;

    private static final double VARIABLES_NAME_COL_WIDTH = 95;
    private static final double VARIABLES_VALUE_COL_WIDTH = 120;
    private static final double VARIABLES_SLIDER_COL_WIDTH = 85;
    private static final double VARIABLES_MIN_COL_WIDTH = 95;
    private static final double VARIABLES_MAX_COL_WIDTH = 95;
    private static final double VARIABLES_STEP_COL_WIDTH = 95;

    private static final Insets ROOT_PADDING = new Insets(10);
    private static final Insets CARD_PADDING = new Insets(12);
    private static final Insets SLIDERS_PADDING = new Insets(8);

    private static final double TABLE_FIXED_CELL_SIZE = 30;
    private static final double SLIDERS_VIEWPORT_HEIGHT = 210;

    private static final double SLIDER_NAME_WIDTH = 40;
    private static final double SLIDER_VALUE_WIDTH = 90;
    private static final double COLOR_PICKER_WIDTH = 44;

    private final TableView<GraphFxFunction> functionsTable = new TableView<>();
    private final Button addFunctionButton = new Button("Add");
    private final Button removeFunctionButton = new Button("Remove");

    private final TableView<GraphFxVariable> variablesTable = new TableView<>();
    private final Button addVariableButton = new Button("Add");
    private final Button removeVariableButton = new Button("Remove");

    private final VBox slidersBox = new VBox(8);
    private final ScrollPane sidebarScroll;

    /**
     * Creates the main window view by composing a toolbar, a sidebar, and the graph view.
     * <p>
     * The toolbar and graph view are injected to keep this view class focused on layout composition,
     * not on creating all possible UI parts.
     *
     * @param toolbar   the toolbar displayed at the top of the window
     * @param graphView the graph view displayed to the right of the sidebar
     * @throws NullPointerException if {@code toolbar} or {@code graphView} is {@code null}
     */
    public MainWindowView(@NonNull final GraphToolbarView toolbar, @NonNull final GraphFxGraphView graphView) {
        setPadding(ROOT_PADDING);
        setTop(toolbar);

        this.sidebarScroll = createSidebarScroll();
        setCenter(createContentLayout(sidebarScroll, graphView));
    }

    /**
     * Exposes the functions table to allow controllers to set items, register handlers,
     * and read selection state.
     *
     * @return the functions {@link TableView}
     */
    public TableView<GraphFxFunction> functionsTable() {
        return functionsTable;
    }

    /**
     * Exposes the "Add function" button for controllers to attach behavior.
     *
     * @return the add function {@link Button}
     */
    public Button addFunctionButton() {
        return addFunctionButton;
    }

    /**
     * Exposes the "Remove function" button for controllers to attach behavior.
     *
     * @return the remove function {@link Button}
     */
    public Button removeFunctionButton() {
        return removeFunctionButton;
    }

    /**
     * Exposes the variables table to allow controllers to set items, register handlers,
     * and read selection state.
     *
     * @return the variables {@link TableView}
     */
    public TableView<GraphFxVariable> variablesTable() {
        return variablesTable;
    }

    /**
     * Exposes the "Add variable" button for controllers to attach behavior.
     *
     * @return the add variable {@link Button}
     */
    public Button addVariableButton() {
        return addVariableButton;
    }

    /**
     * Exposes the "Remove variable" button for controllers to attach behavior.
     *
     * @return the remove variable {@link Button}
     */
    public Button removeVariableButton() {
        return removeVariableButton;
    }

    /**
     * Exposes the container that hosts all slider rows.
     * <p>
     * Controllers can either add rows via {@link #addSliderRow(GraphFxVariable, Slider, Label, Label)}
     * or directly manage the children for advanced scenarios.
     *
     * @return the sliders container
     */
    public VBox slidersBox() {
        return slidersBox;
    }

    /**
     * Adds a slider row to the sliders container and associates the row with a model variable.
     * <p>
     * The provided {@code variable} is stored as {@code userData} on the row to enable controllers
     * to later find/remove/update rows based on the associated variable instance.
     * <p>
     * This method expects the controller to construct and bind:
     * <ul>
     *     <li>{@code slider} to the variable value and range</li>
     *     <li>{@code nameLabel} to the variable name (or a derived label)</li>
     *     <li>{@code valueLabel} to the current value display</li>
     * </ul>
     *
     * @param variable   variable represented by the slider row
     * @param slider     the slider control
     * @param nameLabel  label shown on the left side
     * @param valueLabel label shown on the right side
     * @throws NullPointerException if any parameter is {@code null}
     */
    public void addSliderRow(@NonNull final GraphFxVariable variable, @NonNull final Slider slider, @NonNull final Label nameLabel, @NonNull final Label valueLabel) {
        final HBox row = createSliderRow(slider, nameLabel, valueLabel);
        row.setUserData(variable);
        slidersBox.getChildren().add(row);
    }

    /**
     * Creates the overall content layout consisting of sidebar and graph view.
     *
     * @param sidebar   the sidebar node
     * @param graphView the graph view node
     * @return a node suitable for placement as the center of this {@link BorderPane}
     */
    private Node createContentLayout(@NonNull final ScrollPane sidebar, @NonNull final GraphFxGraphView graphView) {
        final HBox content = new HBox(12, sidebar, graphView);
        HBox.setHgrow(graphView, Priority.ALWAYS);
        return content;
    }

    /**
     * Creates and configures the sidebar scroll container with all sidebar cards.
     * <p>
     * The sidebar is a fixed-width {@link ScrollPane} so the graph view can freely grow and shrink
     * while the sidebar remains readable.
     *
     * @return the configured sidebar scroll pane
     */
    private ScrollPane createSidebarScroll() {
        final VBox sidebarContent = new VBox(12);
        sidebarContent.setFillWidth(true);
        sidebarContent.getChildren().addAll(createFunctionsCard(), createVariablesCard(), createSlidersCard());

        final ScrollPane sidebar = new ScrollPane(sidebarContent);
        sidebar.setFitToWidth(true);
        sidebar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebar.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMinWidth(SIDEBAR_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_WIDTH);
        return sidebar;
    }

    /**
     * Creates the "Functions" card containing explanatory text, a table, and action buttons.
     *
     * @return the functions card container
     */
    private VBox createFunctionsCard() {
        final VBox card = createCardContainer();
        card.getChildren().addAll(createCardTitle("Functions"), createHintLabel("Create and manage functions that will be drawn on the coordinate plane.\n" + "Use x as the input variable. Other variables (a, b, c, …) come from the Variables tab.\n"), configureFunctionsTable(), createCardButtons(addFunctionButton, removeFunctionButton, tooltip("Add function", "Create a new function.\nA unique color is assigned automatically."), tooltip("Remove function", "Remove the selected function.")));
        return card;
    }

    /**
     * Creates the "Variables" card containing explanatory text, a table, and action buttons.
     *
     * @return the variables card container
     */
    private VBox createVariablesCard() {
        final VBox card = createCardContainer();
        card.getChildren().addAll(createCardTitle("Variables"), createHintLabel("Define variables used inside your function expressions\n" + "If “Slider” is enabled, the variable appears in the Sliders tab for interactive control.\n"), configureVariablesTable(), createCardButtons(addVariableButton, removeVariableButton, tooltip("Add variable", "Create a new variable.\nOptionally enable a slider and configure min/max/step."), tooltip("Remove variable", "Remove the selected variable.")));
        return card;
    }

    /**
     * Creates the "Sliders" card containing explanatory text and a scrollable slider list.
     *
     * @return the sliders card container
     */
    private VBox createSlidersCard() {
        final VBox card = createCardContainer();
        card.setSpacing(10);

        slidersBox.setPadding(SLIDERS_PADDING);

        final ScrollPane slidersScroll = new ScrollPane(slidersBox);
        slidersScroll.setFitToWidth(true);
        slidersScroll.setPrefViewportHeight(SLIDERS_VIEWPORT_HEIGHT);
        slidersScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        card.getChildren().addAll(createCardTitle("Sliders"), createHintLabel("Sliders are an easy way to explore how variables affect your functions.\n" + "Enable a slider in the Variables tab, then drag it here to update the graph in real time."), slidersScroll);
        return card;
    }

    /**
     * Configures the functions table (columns, sizing, cell factories) and returns it.
     * <p>
     * The table supports inline editing for name and expression, a visibility toggle,
     * and a color picker cell for function color selection.
     *
     * @return the configured functions table
     */
    private TableView<GraphFxFunction> configureFunctionsTable() {
        functionsTable.setEditable(true);
        functionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        functionsTable.setFixedCellSize(TABLE_FIXED_CELL_SIZE);
        functionsTable.setPlaceholder(new Label(""));

        final TableColumn<GraphFxFunction, Boolean> visibleColumn = new TableColumn<>();
        configureHeader(visibleColumn, "On", "Toggle function visibility.");
        visibleColumn.setCellValueFactory(c -> c.getValue().visibleProperty());
        visibleColumn.setCellFactory(CheckBoxTableCell.forTableColumn(visibleColumn));
        visibleColumn.setPrefWidth(FUNCTIONS_VISIBLE_COL_WIDTH);

        final TableColumn<GraphFxFunction, String> nameColumn = new TableColumn<>();
        configureHeader(nameColumn, "Name", "Short function name (e.g., f, g, h).");
        nameColumn.setCellValueFactory(c -> c.getValue().nameProperty());
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setPrefWidth(FUNCTIONS_NAME_COL_WIDTH);

        final TableColumn<GraphFxFunction, String> expressionColumn = new TableColumn<>();
        configureHeader(expressionColumn, "Expression", "Expression to evaluate.\n" + "Use x as input.\n" + "Other variables are defined in the Variables tab.");
        expressionColumn.setCellValueFactory(c -> c.getValue().expressionProperty());
        expressionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        expressionColumn.setPrefWidth(FUNCTIONS_EXPR_COL_WIDTH);

        final TableColumn<GraphFxFunction, Color> colorColumn = new TableColumn<>();
        configureHeader(colorColumn, "Color", "Pick a color for this function.");
        colorColumn.setCellValueFactory(c -> c.getValue().colorProperty());
        colorColumn.setCellFactory(col -> new ColorSwatchCell());
        colorColumn.setPrefWidth(FUNCTIONS_COLOR_COL_WIDTH);

        functionsTable.getColumns().setAll(visibleColumn, nameColumn, expressionColumn, colorColumn);
        VBox.setVgrow(functionsTable, Priority.ALWAYS);

        return functionsTable;
    }

    /**
     * Configures the variables table (columns, sizing, cell factories) and returns it.
     * <p>
     * The table supports inline editing for name/value/range fields and a checkbox toggle
     * indicating whether a slider should be displayed for a variable.
     *
     * @return the configured variables table
     */
    private TableView<GraphFxVariable> configureVariablesTable() {
        variablesTable.setEditable(true);
        variablesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        variablesTable.setFixedCellSize(TABLE_FIXED_CELL_SIZE);
        variablesTable.setPlaceholder(new Label(""));

        final StringConverter<String> identity = identityStringConverter();

        final TableColumn<GraphFxVariable, String> nameColumn = new TableColumn<>();
        configureHeader(nameColumn, "Name", "Variable name (letters, digits, underscore).\n" + "The name 'x' is reserved.");
        nameColumn.setCellValueFactory(c -> c.getValue().nameProperty());
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn(identity));
        nameColumn.setPrefWidth(VARIABLES_NAME_COL_WIDTH);

        final TableColumn<GraphFxVariable, String> valueColumn = new TableColumn<>();
        configureHeader(valueColumn, "Value", "Current numeric value.");
        valueColumn.setCellValueFactory(c -> c.getValue().valueStringProperty());
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setPrefWidth(VARIABLES_VALUE_COL_WIDTH);

        final TableColumn<GraphFxVariable, Boolean> sliderColumn = new TableColumn<>();
        configureHeader(sliderColumn, "Slider", "Enable a slider for this variable.");
        sliderColumn.setCellValueFactory(c -> c.getValue().sliderEnabledProperty());
        sliderColumn.setCellFactory(CheckBoxTableCell.forTableColumn(sliderColumn));
        sliderColumn.setPrefWidth(VARIABLES_SLIDER_COL_WIDTH);

        final TableColumn<GraphFxVariable, String> minColumn = new TableColumn<>();
        configureHeader(minColumn, "Min", "Minimum slider value.");
        minColumn.setCellValueFactory(c -> c.getValue().sliderMinStringProperty());
        minColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        minColumn.setPrefWidth(VARIABLES_MIN_COL_WIDTH);

        final TableColumn<GraphFxVariable, String> maxColumn = new TableColumn<>();
        configureHeader(maxColumn, "Max", "Maximum slider value.");
        maxColumn.setCellValueFactory(c -> c.getValue().sliderMaxStringProperty());
        maxColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        maxColumn.setPrefWidth(VARIABLES_MAX_COL_WIDTH);

        final TableColumn<GraphFxVariable, String> stepColumn = new TableColumn<>();
        configureHeader(stepColumn, "Step", "Slider step size (must be > 0).");
        stepColumn.setCellValueFactory(c -> c.getValue().sliderStepStringProperty());
        stepColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        stepColumn.setPrefWidth(VARIABLES_STEP_COL_WIDTH);

        variablesTable.getColumns().setAll(nameColumn, valueColumn, sliderColumn, minColumn, maxColumn, stepColumn);
        variablesTable.setPrefHeight(240);

        return variablesTable;
    }

    /**
     * Creates a standard "card" container used in the sidebar.
     * <p>
     * Cards provide consistent spacing, padding, and style class assignment
     * for visually grouped UI sections.
     *
     * @return a new card container
     */
    private static VBox createCardContainer() {
        final VBox card = new VBox(8);
        card.setPadding(CARD_PADDING);
        return card;
    }

    /**
     * Creates a standard card title label and applies the appropriate style class.
     *
     * @param text title text
     * @return title label
     */
    private static Label createCardTitle(@NonNull final String text) {
        return new Label(text);
    }

    /**
     * Creates an explanatory hint label for use inside sidebar cards.
     * <p>
     * Hint labels are wrapped and styled for readability.
     *
     * @param text hint content
     * @return hint label
     */
    private static Label createHintLabel(@NonNull final String text) {
        final Label hint = new Label(text);
        hint.setWrapText(true);
        return hint;
    }

    /**
     * Creates a standard "Add/Remove" button row for a card and assigns tooltips.
     *
     * @param addButton     the add button
     * @param removeButton  the remove button
     * @param addTooltip    tooltip for the add button
     * @param removeTooltip tooltip for the remove button
     * @return a button row container aligned to the left
     */
    private static HBox createCardButtons(@NonNull final Button addButton, @NonNull final Button removeButton, @NonNull final Tooltip addTooltip, @NonNull final Tooltip removeTooltip) {
        addButton.setTooltip(addTooltip);
        removeButton.setTooltip(removeTooltip);

        final HBox buttons = new HBox(8, addButton, removeButton);
        buttons.setAlignment(Pos.CENTER_LEFT);
        return buttons;
    }

    /**
     * Configures a table column header to use a styled label with a tooltip instead of plain text.
     * <p>
     * This approach allows multi-line descriptions and consistent header styling via CSS.
     *
     * @param column      the column to configure
     * @param text        header text
     * @param tooltipText tooltip body describing the column purpose
     */
    private static void configureHeader(@NonNull final TableColumn<?, ?> column, @NonNull final String text, @NonNull final String tooltipText) {
        final Label label = new Label(text);
        label.setTooltip(tooltip(text, tooltipText));
        column.setText(null);
        column.setGraphic(label);
    }

    /**
     * Creates a tooltip with a title and a body, wrapped for better readability.
     *
     * @param title tooltip title line
     * @param body  tooltip description
     * @return configured tooltip
     */
    private static Tooltip tooltip(@NonNull final String title, @NonNull final String body) {
        final Tooltip tooltip = new Tooltip(title + "\n" + body);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(520);
        return tooltip;
    }

    /**
     * Provides an identity {@link StringConverter} for editable table cells.
     * <p>
     * This converter avoids unnecessary transformations and ensures that the cell editor
     * returns exactly the string entered by the user.
     *
     * @return an identity string converter
     */
    private static StringConverter<String> identityStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(final String object) {
                return object;
            }

            @Override
            public String fromString(final String string) {
                return string;
            }
        };
    }

    /**
     * Creates a single slider row with a fixed-width name label, a growing slider, and a fixed-width value label.
     * <p>
     * The method assigns style classes to the labels for external theming and ensures consistent sizing
     * across all slider rows.
     *
     * @param slider     the slider control that should expand horizontally
     * @param nameLabel  label on the left side, typically the variable name
     * @param valueLabel label on the right side, typically the current variable value
     * @return a fully configured row container
     */
    private static HBox createSliderRow(@NonNull final Slider slider, @NonNull final Label nameLabel, @NonNull final Label valueLabel) {
        configureFixedWidth(nameLabel, SLIDER_NAME_WIDTH);
        configureFixedWidth(valueLabel, SLIDER_VALUE_WIDTH);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);

        final HBox row = new HBox(10, nameLabel, slider, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(slider, Priority.ALWAYS);
        return row;
    }

    /**
     * Applies a fixed width configuration to a {@link Region} (min/pref/max).
     * <p>
     * This is used to keep labels and compact controls (like the color picker) aligned
     * regardless of window size.
     *
     * @param region the region to configure
     * @param width  the fixed width to apply
     */
    private static void configureFixedWidth(@NonNull final Region region, final double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }

    /**
     * Table cell that renders a compact {@link ColorPicker} for editing a {@link GraphFxFunction}'s color.
     * <p>
     * The cell updates the underlying function instance when the user selects a new color.
     * The {@link ColorPicker} is displayed as the only graphic content of the cell.
     */
    private static final class ColorSwatchCell extends TableCell<GraphFxFunction, Color> {

        private final ColorPicker picker = new ColorPicker();

        /**
         * Creates a color swatch cell with a fixed-width {@link ColorPicker} and the appropriate styling.
         */
        private ColorSwatchCell() {
            configureFixedWidth(picker, COLOR_PICKER_WIDTH);

            picker.setOnAction(e -> {
                final Object rowItem = getTableRow() == null ? null : getTableRow().getItem();
                if (rowItem instanceof GraphFxFunction fn) {
                    fn.setColor(picker.getValue());
                }
            });

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);
        }

        /**
         * Updates the displayed {@link ColorPicker} state when the table cell is refreshed.
         * <p>
         * If the cell is empty, no graphic is shown. Otherwise, the picker is displayed and
         * receives either the function's color or a sensible fallback if the value is {@code null}.
         *
         * @param color the current color value for this cell
         * @param empty whether this cell represents an actual item
         */
        @Override
        protected void updateItem(@NonNull final  Color color, @NonNull final  boolean empty) {
            super.updateItem(color, empty);

            if (empty) {
                setGraphic(null);
                return;
            }

            picker.setValue(color);
            setGraphic(picker);
        }
    }

}
