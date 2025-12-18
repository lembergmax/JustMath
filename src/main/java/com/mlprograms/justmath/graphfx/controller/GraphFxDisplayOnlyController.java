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
import com.mlprograms.justmath.graphfx.view.DisplayWindowView;
import com.mlprograms.justmath.graphfx.view.GraphFxGraphView;
import com.mlprograms.justmath.graphfx.view.GraphToolbarView;
import com.mlprograms.justmath.graphfx.view.MainWindowView;
import javafx.scene.Parent;
import lombok.NonNull;

import java.util.List;

/**
 * Controller for a display-only GraphFX window.
 * <p>
 * This controller creates a {@link GraphFxModel} from a list of {@link com.mlprograms.justmath.graphfx.model.GraphFxFunction} specifications and
 * wires it to a {@link GraphFxGraphView} and a {@link GraphToolbarView}. Unlike an editor-style controller,
 * this controller focuses on presenting functions for visualization and interaction (e.g., move/zoom tools)
 * while not providing an explicit authoring workflow.
 * </p>
 * <p>
 * The {@link CalculatorEngine} is injected and used by the graph view for expression evaluation and any
 * computation required for rendering or tool interaction.
 * </p>
 */
public class GraphFxDisplayOnlyController extends GraphFxController {

    /**
     * The root view for the display window, composed of the toolbar and the graph view.
     */
    private final DisplayWindowView view;

    /**
     * Creates a new display-only controller from the given calculator calculatorEngine and function specifications.
     * <p>
     * The provided {@code functions} are transformed into entries in a new {@link GraphFxModel}. If a function
     * specification does not provide a color, a palette color is selected based on its index.
     * After initialization, the first function (if present) is selected to establish a consistent default state.
     * </p>
     *
     * @param calculatorEngine the calculator calculatorEngine used by the graph view for expression evaluation
     * @param model            the model containing the functions to display
     * @throws NullPointerException if {@code calculatorEngine} or {@code functions} is {@code null}
     */
    public GraphFxDisplayOnlyController(@NonNull final GraphFxModel model, @NonNull final CalculatorEngine calculatorEngine) {
        super(model, new GraphToolbarView(), m -> new GraphFxGraphView(m, calculatorEngine));
        this.view = new DisplayWindowView(toolbar, graphView);

        selectFirstFunctionIfPresent();
    }

    /**
     * Ensures that a function is selected if the model currently has none selected.
     * <p>
     * If the model contains at least one function and {@link GraphFxModel#getSelectedFunction()} is {@code null},
     * this method selects the first function in the model list.
     * </p>
     */
    private void selectFirstFunctionIfPresent() {
        if (model.getSelectedFunction() == null && !model.getFunctions().isEmpty()) {
            model.setSelectedFunction(model.getFunctions().getFirst());
        }
    }

    /**
     * Returns the root node for this controller, to be embedded into a JavaFX scene.
     *
     * @return the root {@link Parent} node of the display window
     */
    @Override
    public Parent getRoot() {
        return view;
    }

}
