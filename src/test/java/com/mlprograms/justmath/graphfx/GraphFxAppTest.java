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
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxApp")
final class GraphFxAppTest {

    @Test
    void show_createsStage_andUsesDefaultTitleWhenBlank() {
        final CalculatorEngine engine = Mockito.mock(CalculatorEngine.class);
        final GraphFxApp app = new GraphFxApp(engine, new GraphFxModel(), "   ", 640, 480);

        final Stage stage = app.show();
        assertNotNull(stage);

        try {
            assertNotNull(stage.getTitle());
            assertFalse(stage.getTitle().isBlank());
            assertEquals(640.0, stage.getScene().getWidth(), 0.0001);
            assertEquals(480.0, stage.getScene().getHeight(), 0.0001);
        } finally {
            FxTestSupport.onFxThread(stage::hide);
        }
    }

}
