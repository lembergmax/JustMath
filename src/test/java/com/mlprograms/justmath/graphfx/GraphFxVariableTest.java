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

import com.mlprograms.justmath.graphfx.model.GraphFxVariable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxVariable")
final class GraphFxVariableTest {

    @Test
    void value_and_valueString_areSynchronized_bothDirections() {
        final GraphFxVariable variable = GraphFxVariable.create("a", new BigDecimal("10.0"));

        assertEquals(new BigDecimal("10").toString(), variable.getValue().toString());
        assertEquals("10", variable.getValueString());

        variable.setValue(new BigDecimal("123.4500"));
        assertEquals("123.45", variable.getValueString());

        variable.setValue(new BigDecimal("7.500"));
        assertEquals(new BigDecimal("7.5"), variable.getValue());
        assertEquals("7.5", variable.getValueString());
    }

    @Test
    void sliderValues_and_strings_areSynchronized_bothDirections() {
        final GraphFxVariable variable = GraphFxVariable.create("a", BigDecimal.ZERO);

        variable.setSliderMin(new BigDecimal("-2.0"));
        variable.setSliderMax(new BigDecimal("2.00"));
        variable.setSliderStep(new BigDecimal("0.5000"));

        assertEquals("-2", variable.getSliderMin().toString());
        assertEquals("2", variable.getSliderMax().toString());
        assertEquals("0.5", variable.getSliderStep().toString());

        variable.setSliderMin(new BigDecimal("-10"));
        variable.setSliderMax(new BigDecimal("10"));
        variable.setSliderStep(new BigDecimal("0.1"));

        assertEquals(new BigDecimal("-10"), variable.getSliderMin());
        assertEquals(new BigDecimal("10"), variable.getSliderMax());
        assertEquals(new BigDecimal("0.1"), variable.getSliderStep());
    }

}

