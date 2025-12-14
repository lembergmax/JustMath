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
import javafx.scene.Parent;
import lombok.NonNull;

import java.util.List;

public class GraphFxDisplayOnlyController extends GraphFxController {

    private final DisplayWindowView view;

    public GraphFxDisplayOnlyController(@NonNull final CalculatorEngine engine, @NonNull final List<GraphFxFunctionSpec> functions) {
        super(createModelFromSpecs(functions), new GraphToolbarView(), model -> new GraphFxGraphView(model, engine));

        this.view = new DisplayWindowView(toolbar, graphView);
        selectFirstFunctionIfPresent();
    }

    private void selectFirstFunctionIfPresent() {
        if (model.getSelectedFunction() == null && !model.getFunctions().isEmpty()) {
            model.setSelectedFunction(model.getFunctions().getFirst());
        }
    }

    private static GraphFxModel createModelFromSpecs(@NonNull final List<GraphFxFunctionSpec> functions) {
        final GraphFxModel model = new GraphFxModel();

        int index = 0;
        for (final GraphFxFunctionSpec spec : functions) {
            final var color = spec.color() != null ? spec.color() : GraphFxPalette.colorForIndex(index);
            model.addFunction(spec.name(), spec.expression()).setColor(color);
            index++;
        }

        if (!model.getFunctions().isEmpty()) {
            model.setSelectedFunction(model.getFunctions().getFirst());
        }

        return model;
    }

    @Override
    public Parent getRoot() {
        return view;
    }

}