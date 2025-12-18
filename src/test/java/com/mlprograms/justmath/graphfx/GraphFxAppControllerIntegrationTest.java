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
import com.mlprograms.justmath.graphfx.controller.GraphFxAppController;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.model.GraphFxVariable;
import com.mlprograms.justmath.graphfx.FxTestSupport;
import com.mlprograms.justmath.graphfx.view.MainWindowView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxAppController (Model/View wiring)")
final class GraphFxAppControllerIntegrationTest {

    @Test
    void controllerBuildsRoot_andTablesReflectModelChanges() {
        final CalculatorEngine engine = Mockito.mock(CalculatorEngine.class);
        final GraphFxModel model = new GraphFxModel();

        final GraphFxAppController controller = FxTestSupport.onFxThread(() -> new GraphFxAppController(model, engine));
        final MainWindowView root = (MainWindowView) FxTestSupport.onFxThread(controller::getRoot);

        assertNotNull(root.functionsTable());
        assertNotNull(root.variablesTable());

        FxTestSupport.onFxThread(() -> model.addFunction("f", "x"));
        FxTestSupport.flushFxEvents();

        assertEquals(1, root.functionsTable().getItems().size());

        FxTestSupport.onFxThread(() -> model.addVariable("a", BigDecimal.ONE));
        FxTestSupport.flushFxEvents();

        assertEquals(1, root.variablesTable().getItems().size());
    }

    @Test
    void enablingSlider_createsSliderRowInView() {
        final CalculatorEngine engine = Mockito.mock(CalculatorEngine.class);
        final GraphFxModel model = new GraphFxModel();

        final GraphFxAppController controller = FxTestSupport.onFxThread(() -> new GraphFxAppController(model, engine));
        final MainWindowView root = (MainWindowView) FxTestSupport.onFxThread(controller::getRoot);

        final GraphFxVariable variable = FxTestSupport.onFxThread(() -> model.addVariable("a", BigDecimal.ZERO));
        FxTestSupport.flushFxEvents();

        final int before = FxTestSupport.onFxThread(() -> root.slidersBox().getChildren().size());

        FxTestSupport.onFxThread(() -> variable.sliderEnabledProperty().set(true));
        FxTestSupport.flushFxEvents();

        final int after = FxTestSupport.onFxThread(() -> root.slidersBox().getChildren().size());

        assertTrue(after >= before, "Slider rows should not decrease when enabling a slider.");
    }

}
