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

package com.mlprograms.justmath.graphfx;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphfx.controller.GraphFxDisplayOnlyController;
import com.mlprograms.justmath.graphfx.controller.GraphFxFunction;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.util.FxBootstrap;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.List;

public class GraphFxDisplayOnlyApp extends GraphFxDisplay {

    public GraphFxDisplayOnlyApp(final CalculatorEngine calculatorEngine, final GraphFxModel model, final String windowTitle, final double width, final double height) {
        super(calculatorEngine, model, windowTitle, width, height);
    }

    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model, @NonNull final String windowTitle) {
        this(calculatorEngine, model, windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model, final double width, final double height) {
        this(calculatorEngine, model, DEFAULT_WINDOW_TITLE, width, height);
    }

    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final GraphFxModel model) {
        this(calculatorEngine, model, DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle, final double width, final double height) {
        this(calculatorEngine, new GraphFxModel(), windowTitle, width, height);
    }

    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String windowTitle) {
        this(calculatorEngine, new GraphFxModel(), windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine, final double width, final double height) {
        this(calculatorEngine, new GraphFxModel(), DEFAULT_WINDOW_TITLE, width, height);
    }

    public GraphFxDisplayOnlyApp(@NonNull final CalculatorEngine calculatorEngine) {
        this(calculatorEngine, new GraphFxModel(), DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GraphFxDisplayOnlyApp(@NonNull final GraphFxModel model, @NonNull final String windowTitle, final double width, final double height) {
        this(new CalculatorEngine(), model, windowTitle, width, height);
    }

    public GraphFxDisplayOnlyApp(@NonNull final GraphFxModel model, @NonNull final String windowTitle) {
        this(new CalculatorEngine(), model, windowTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GraphFxDisplayOnlyApp(@NonNull final GraphFxModel model, final double width, final double height) {
        this(new CalculatorEngine(), model, DEFAULT_WINDOW_TITLE, width, height);
    }

    public GraphFxDisplayOnlyApp(@NonNull final GraphFxModel model) {
        this(new CalculatorEngine(), model, DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GraphFxDisplayOnlyApp() {
        this(new CalculatorEngine(), new GraphFxModel(), DEFAULT_WINDOW_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Stage show() {
        return show(List.of());
    }

    public Stage show(@NonNull final List<GraphFxFunction> functions) {
        return FxBootstrap.callAndWait(() -> {
            final GraphFxDisplayOnlyController controller = new GraphFxDisplayOnlyController(calculatorEngine, functions);
            return buildStage(controller);
        });
    }

}
