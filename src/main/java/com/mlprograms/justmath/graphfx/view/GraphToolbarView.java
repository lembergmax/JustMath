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

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import lombok.NonNull;

/**
 * Shared toolbar view used by both the editable main window and the read-only display window.
 * <p>
 * This class follows a "view-only" responsibility: it creates and arranges controls,
 * assigns default UI configuration (toggle grouping, default selection, tooltips and styling),
 * and exposes accessors to the created nodes.
 * <p>
 * Controllers are expected to:
 * <ul>
 *     <li>Bind tool selection to the graph interaction mode</li>
 *     <li>Wire button actions (reset, clear marks, exports)</li>
 *     <li>Bind checkbox states (grid/axes) to the graph rendering configuration</li>
 *     <li>Update the status label (e.g. current cursor coordinates)</li>
 * </ul>
 *
 * <h2>Tool selection</h2>
 * The tool buttons are implemented as {@link ToggleButton}s that are part of a single {@link ToggleGroup}.
 * Exactly one tool is intended to be active at a time; the "Move" tool is selected by default.
 *
 * <h2>Exports</h2>
 * The export buttons are provided as plain {@link Button}s. The view does not decide what gets exported;
 * it only presents the actions and provides tooltips describing the expected behavior.
 *
 * <h2>Styling</h2>
 * The toolbar uses the CSS style class {@code graphfx-toolbar}. The status label uses {@code graphfx-status}.
 * External stylesheets should define these classes to keep styling consistent across both windows.
 */
public final class GraphToolbarView extends ToolBar {

    private static final Insets TOOLBAR_PADDING = new Insets(6);
    private static final double TOOLTIP_MAX_WIDTH = 520;

    private final ToggleGroup tools = new ToggleGroup();

    private final ToggleButton move = new ToggleButton("Move");
    private final ToggleButton zoomBox = new ToggleButton("Zoom Box");

    private final ToggleButton point = new ToggleButton("Point");
    private final ToggleButton tangent = new ToggleButton("Tangent");
    private final ToggleButton normal = new ToggleButton("Normal");
    private final ToggleButton root = new ToggleButton("Root");
    private final ToggleButton intersect = new ToggleButton("Intersect");

    private final Button resetView = new Button("Reset view");
    private final Button clearMarks = new Button("Clear marks");

    private final CheckBox grid = new CheckBox("Grid");
    private final CheckBox axes = new CheckBox("Axes");

    private final Button exportPng = new Button("PNG");
    private final Button exportSvg = new Button("SVG");
    private final Button exportCsv = new Button("CSV");
    private final Button exportJson = new Button("JSON");

    private final Label statusLabel = new Label("x=0   y=0");

    /**
     * Creates and initializes the toolbar UI.
     * <p>
     * The constructor:
     * <ul>
     *     <li>sets CSS style classes and padding</li>
     *     <li>registers all tool toggle buttons in a single {@link ToggleGroup}</li>
     *     <li>selects the default tool ("Move")</li>
     *     <li>assigns user-facing tooltips for all controls</li>
     *     <li>builds the final toolbar item list including separators and a right-aligned status label</li>
     * </ul>
     */
    public GraphToolbarView() {
        getStyleClass().add("graphfx-toolbar");
        setPadding(TOOLBAR_PADDING);

        configureToolToggle(move, tooltip(
                "Move tool",
                "Drag with the left mouse button to pan.\nMouse wheel: Zoom in/out.\nCtrl+Z / Ctrl+Y: Undo/Redo view."
        ));
        configureToolToggle(zoomBox, tooltip(
                "Zoom box tool",
                "Drag a rectangle to zoom into that region.\nThe view keeps equal scaling on x/y."
        ));
        configureToolToggle(point, tooltip(
                "Point tool",
                "Click near a function to create a point.\nThe point uses the clicked x-position."
        ));
        configureToolToggle(tangent, tooltip(
                "Tangent tool",
                "Click near a function to create the tangent line at that x-position."
        ));
        configureToolToggle(normal, tooltip(
                "Normal tool",
                "Click near a function to create the normal line at that x-position."
        ));
        configureToolToggle(root, tooltip(
                "Root tool",
                "Click near a function to find a nearby root (x-intercept).\nA point at y=0 will be added."
        ));
        configureToolToggle(intersect, tooltip(
                "Intersection tool",
                "Step 1: Click the first function.\nStep 2: Click the second function.\nIntersection points in the visible x-range will be added."
        ));

        move.setSelected(true);

        resetView.setTooltip(tooltip("Reset view", "Reset to a clean default position and zoom.\nShortcut: R"));
        clearMarks.setTooltip(tooltip("Clear marks", "Remove all created points, lines and integrals.\nFunctions and variables remain unchanged."));

        grid.setTooltip(tooltip("Grid", "Toggle the background grid."));
        axes.setTooltip(tooltip("Axes", "Toggle the x- and y-axis lines and tick labels."));

        exportPng.setTooltip(tooltip("Export PNG", "Export the current canvas as a PNG image."));
        exportSvg.setTooltip(tooltip("Export SVG", "Export a function as SVG paths (screen space).\nIf no function is selected, you can choose one on export."));
        exportCsv.setTooltip(tooltip("Export CSV", "Export sampled points of a function as CSV.\nIf no function is selected, you can choose one on export."));
        exportJson.setTooltip(tooltip("Export JSON", "Export sampled points of a function as JSON.\nIf no function is selected, you can choose one on export."));

        final Region spacer = createGrowingSpacer();

        statusLabel.getStyleClass().add("graphfx-status");

        getItems().setAll(
                move, zoomBox,
                new Separator(),
                point, tangent, normal, root, intersect,
                new Separator(),
                resetView, clearMarks,
                new Separator(),
                grid, axes,
                new Separator(),
                exportPng, exportSvg, exportCsv, exportJson,
                spacer,
                statusLabel
        );
    }

    /**
     * Returns the "Move" tool toggle button.
     *
     * @return the move tool button
     */
    public ToggleButton moveButton() {
        return move;
    }

    /**
     * Returns the "Zoom Box" tool toggle button.
     *
     * @return the zoom box tool button
     */
    public ToggleButton zoomBoxButton() {
        return zoomBox;
    }

    /**
     * Returns the "Point" tool toggle button.
     *
     * @return the point tool button
     */
    public ToggleButton pointButton() {
        return point;
    }

    /**
     * Returns the "Tangent" tool toggle button.
     *
     * @return the tangent tool button
     */
    public ToggleButton tangentButton() {
        return tangent;
    }

    /**
     * Returns the "Normal" tool toggle button.
     *
     * @return the normal tool button
     */
    public ToggleButton normalButton() {
        return normal;
    }

    /**
     * Returns the "Root" tool toggle button.
     *
     * @return the root tool button
     */
    public ToggleButton rootButton() {
        return root;
    }

    /**
     * Returns the "Intersect" tool toggle button.
     *
     * @return the intersect tool button
     */
    public ToggleButton intersectButton() {
        return intersect;
    }

    /**
     * Returns the "Reset view" button.
     * <p>
     * Controllers typically reset the graph viewport (pan/zoom) when this button is pressed.
     *
     * @return the reset view button
     */
    public Button resetViewButton() {
        return resetView;
    }

    /**
     * Returns the "Clear marks" button.
     * <p>
     * Controllers typically clear user-created graph marks (points/lines/areas)
     * when this button is pressed.
     *
     * @return the clear marks button
     */
    public Button clearMarksButton() {
        return clearMarks;
    }

    /**
     * Returns the "Grid" checkbox.
     * <p>
     * Controllers typically bind this checkbox to a graph setting controlling whether the grid is drawn.
     *
     * @return the grid checkbox
     */
    public CheckBox gridCheckBox() {
        return grid;
    }

    /**
     * Returns the "Axes" checkbox.
     * <p>
     * Controllers typically bind this checkbox to a graph setting controlling whether axes and tick labels are drawn.
     *
     * @return the axes checkbox
     */
    public CheckBox axesCheckBox() {
        return axes;
    }

    /**
     * Returns the "Export PNG" button.
     *
     * @return the PNG export button
     */
    public Button exportPngButton() {
        return exportPng;
    }

    /**
     * Returns the "Export SVG" button.
     *
     * @return the SVG export button
     */
    public Button exportSvgButton() {
        return exportSvg;
    }

    /**
     * Returns the "Export CSV" button.
     *
     * @return the CSV export button
     */
    public Button exportCsvButton() {
        return exportCsv;
    }

    /**
     * Returns the "Export JSON" button.
     *
     * @return the JSON export button
     */
    public Button exportJsonButton() {
        return exportJson;
    }

    /**
     * Returns the status label displayed on the right side of the toolbar.
     * <p>
     * Controllers typically update the label text with information such as cursor coordinates,
     * selected tool hints, or evaluation feedback.
     *
     * @return the status label
     */
    public Label statusLabel() {
        return statusLabel;
    }

    /**
     * Configures a tool {@link ToggleButton} to be part of the shared tool {@link ToggleGroup}
     * and assigns its tooltip.
     *
     * @param button  the toggle button to configure
     * @param tooltip the tooltip to assign
     * @throws NullPointerException if any parameter is {@code null}
     */
    private void configureToolToggle(@NonNull final ToggleButton button, @NonNull final Tooltip tooltip) {
        button.setToggleGroup(tools);
        button.setTooltip(tooltip);
    }

    /**
     * Creates a spacer region that grows horizontally so that the status label is pushed
     * to the right edge of the toolbar.
     *
     * @return a region configured to grow horizontally
     */
    private static Region createGrowingSpacer() {
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /**
     * Creates a wrapped tooltip with a title line and a descriptive body.
     *
     * @param title tooltip title line
     * @param body  tooltip body text
     * @return the configured tooltip
     */
    private static Tooltip tooltip(@NonNull final String title, @NonNull final String body) {
        final Tooltip tooltip = new Tooltip(title + "\n" + body);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(TOOLTIP_MAX_WIDTH);
        return tooltip;
    }

}
