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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Shared toolbar for both windows. Only builds controls; wiring is done in controllers.
 */
public final class GraphToolbarView extends ToolBar {

    private final ToggleGroup tools = new ToggleGroup();

    private final ToggleButton move = new ToggleButton("Move");
    private final ToggleButton zoomBox = new ToggleButton("Zoom Box");

    private final ToggleButton point = new ToggleButton("Point");
    private final ToggleButton tangent = new ToggleButton("Tangent");
    private final ToggleButton normal = new ToggleButton("Normal");
    private final ToggleButton root = new ToggleButton("Root");
    private final ToggleButton intersect = new ToggleButton("Intersect");
    private final ToggleButton integral = new ToggleButton("Integral");

    private final Button resetView = new Button("Reset view");
    private final Button clearMarks = new Button("Clear marks");

    private final CheckBox grid = new CheckBox("Grid");
    private final CheckBox axes = new CheckBox("Axes");

    private final Button exportPng = new Button("PNG");
    private final Button exportSvg = new Button("SVG");
    private final Button exportCsv = new Button("CSV");
    private final Button exportJson = new Button("JSON");

    private final Label statusLabel = new Label("x=0   y=0");

    public GraphToolbarView() {
        getStyleClass().add("graphfx-toolbar");
        setPadding(new Insets(6));

        move.setToggleGroup(tools);
        zoomBox.setToggleGroup(tools);
        point.setToggleGroup(tools);
        tangent.setToggleGroup(tools);
        normal.setToggleGroup(tools);
        root.setToggleGroup(tools);
        intersect.setToggleGroup(tools);
        integral.setToggleGroup(tools);

        move.setSelected(true);

        move.setTooltip(tooltip(
                "Move tool",
                "Drag with the left mouse button to pan.\nMouse wheel: Zoom in/out.\nCtrl+Z / Ctrl+Y: Undo/Redo view."
        ));
        zoomBox.setTooltip(tooltip(
                "Zoom box tool",
                "Drag a rectangle to zoom into that region.\nThe view keeps equal scaling on x/y."
        ));
        point.setTooltip(tooltip(
                "Point tool",
                "Click near a function to create a point.\nThe point uses the clicked x-position."
        ));
        tangent.setTooltip(tooltip(
                "Tangent tool",
                "Click near a function to create the tangent line at that x-position."
        ));
        normal.setTooltip(tooltip(
                "Normal tool",
                "Click near a function to create the normal line at that x-position."
        ));
        root.setTooltip(tooltip(
                "Root tool",
                "Click near a function to find a nearby root (x-intercept).\nA point at y=0 will be added."
        ));
        intersect.setTooltip(tooltip(
                "Intersection tool",
                "Step 1: Click the first function.\nStep 2: Click the second function.\nIntersection points in the visible x-range will be added."
        ));
        integral.setTooltip(tooltip(
                "Integral tool",
                "Step 1: Click near a function to select it.\nStep 2: Drag and release to choose the interval.\nAn area + numeric integral value will be added."
        ));

        resetView.setTooltip(tooltip("Reset view", "Reset to a clean default position and zoom.\nShortcut: R"));
        clearMarks.setTooltip(tooltip("Clear marks", "Remove all created points, lines and integrals.\nFunctions and variables remain unchanged."));

        grid.setTooltip(tooltip("Grid", "Toggle the background grid."));
        axes.setTooltip(tooltip("Axes", "Toggle the x- and y-axis lines and tick labels."));

        exportPng.setTooltip(tooltip("Export PNG", "Export the current canvas as a PNG image."));
        exportSvg.setTooltip(tooltip("Export SVG", "Export a function as SVG paths (screen space).\nIf no function is selected, you can choose one on export."));
        exportCsv.setTooltip(tooltip("Export CSV", "Export sampled points of a function as CSV.\nIf no function is selected, you can choose one on export."));
        exportJson.setTooltip(tooltip("Export JSON", "Export sampled points of a function as JSON.\nIf no function is selected, you can choose one on export."));

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusLabel.getStyleClass().add("graphfx-status");

        getItems().setAll(
                move, zoomBox,
                new Separator(),
                point, tangent, normal, root, intersect, integral,
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

    public ToggleGroup toolsGroup() { return tools; }

    public ToggleButton moveButton() { return move; }
    public ToggleButton zoomBoxButton() { return zoomBox; }
    public ToggleButton pointButton() { return point; }
    public ToggleButton tangentButton() { return tangent; }
    public ToggleButton normalButton() { return normal; }
    public ToggleButton rootButton() { return root; }
    public ToggleButton intersectButton() { return intersect; }
    public ToggleButton integralButton() { return integral; }

    public Button resetViewButton() { return resetView; }
    public Button clearMarksButton() { return clearMarks; }

    public CheckBox gridCheckBox() { return grid; }
    public CheckBox axesCheckBox() { return axes; }

    public Button exportPngButton() { return exportPng; }
    public Button exportSvgButton() { return exportSvg; }
    public Button exportCsvButton() { return exportCsv; }
    public Button exportJsonButton() { return exportJson; }

    public Label statusLabel() { return statusLabel; }

    private static Tooltip tooltip(final String title, final String body) {
        final Tooltip t = new Tooltip(title + "\n" + body);
        t.setWrapText(true);
        t.setMaxWidth(520);
        return t;
    }
}
