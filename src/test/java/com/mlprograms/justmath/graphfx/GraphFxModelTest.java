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

import com.mlprograms.justmath.graphfx.model.GraphFxFunction;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.model.GraphFxPointObject;
import com.mlprograms.justmath.graphfx.model.GraphFxVariable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxModel")
final class GraphFxModelTest {

    @Nested
    @DisplayName("Functions")
    final class FunctionTests {

        @Test
        void addFunction_createsAndRegistersFunction() {
            final GraphFxModel model = new GraphFxModel();

            final GraphFxFunction function = model.addFunction("f", "x^2");

            assertNotNull(function);
            assertEquals(1, model.getFunctions().size());
            assertSame(function, model.getFunctions().getFirst());
            assertEquals("f", function.getName());
            assertEquals("x^2", function.getExpression());
        }

        @ParameterizedTest
        @CsvSource({
                "'',x",
                "'   ',x",
                "f,''",
                "f,'   '"
        })
        void addFunction_rejectsBlankInputs(final String name, final String expression) {
            final GraphFxModel model = new GraphFxModel();

            assertThrows(IllegalArgumentException.class, () -> model.addFunction(name, expression));
        }

        @Test
        void removeFunction_removesObjectsReferencingFunctionId_andClearsSelection() {
            final GraphFxModel model = new GraphFxModel();
            final GraphFxFunction function = model.addFunction("f", "x");
            model.setSelectedFunction(function);

            final GraphFxPointObject point = GraphFxPointObject.of("P", BigDecimal.ONE, BigDecimal.ONE, function.getId());
            final GraphFxPointObject otherPoint = GraphFxPointObject.of("Q", BigDecimal.ZERO, BigDecimal.ZERO, UUID.randomUUID());
            model.addObject(point);
            model.addObject(otherPoint);

            model.removeFunction(function);

            assertTrue(model.getFunctions().isEmpty());
            assertNull(model.getSelectedFunction());
            assertEquals(1, model.getObjects().size());
            assertSame(otherPoint.id(), model.getObjects().getFirst().id());
        }
    }

    @Nested
    @DisplayName("Variables")
    final class VariableTests {

        @Test
        void addVariable_rejectsReservedNameX() {
            final GraphFxModel model = new GraphFxModel();

            assertThrows(IllegalArgumentException.class, () -> model.addVariable("x", BigDecimal.ONE));
            assertThrows(IllegalArgumentException.class, () -> model.addVariable("X", BigDecimal.ONE));
        }

        @Test
        void addVariable_rejectsDuplicateNames_caseInsensitive() {
            final GraphFxModel model = new GraphFxModel();
            model.addVariable("a", BigDecimal.ONE);

            assertThrows(IllegalArgumentException.class, () -> model.addVariable("A", BigDecimal.TEN));
        }

        @Test
        void renameVariable_rejectsReservedAndDuplicates() {
            final GraphFxModel model = new GraphFxModel();
            final GraphFxVariable a = model.addVariable("a", BigDecimal.ONE);
            model.addVariable("b", BigDecimal.ONE);

            assertThrows(IllegalArgumentException.class, () -> model.renameVariable(a, "x"));
            assertThrows(IllegalArgumentException.class, () -> model.renameVariable(a, "B"));
        }

        @Test
        void setVariableValue_parsesStringAndUpdatesMap() {
            final GraphFxModel model = new GraphFxModel();
            final GraphFxVariable a = model.addVariable("a", BigDecimal.ZERO);

            model.setVariableValue(a, "  123.5000 ");

            assertEquals(new BigDecimal("123.5"), a.getValue());
            final Map<String, String> variables = model.variablesAsStringMap();
            assertEquals("123.5", variables.get("a")); // stripTrailingZeros().toPlainString()
        }

        @Test
        void sliderValidation_rejectsInvalidConfigurations() {
            final GraphFxModel model = new GraphFxModel();
            final GraphFxVariable a = model.addVariable("a", BigDecimal.ZERO);

            model.setSliderMin(a, "0");
            model.setSliderMax(a, "10");
            model.setSliderStep(a, "1");

            assertThrows(IllegalArgumentException.class, () -> model.setSliderMax(a, "0")); // max <= min
            assertThrows(IllegalArgumentException.class, () -> model.setSliderStep(a, "0")); // step <= 0
            assertThrows(IllegalArgumentException.class, () -> model.setSliderStep(a, "-1")); // step <= 0
        }

        @Test
        void nextSuggestedVariableName_skipsExistingNames() {
            final GraphFxModel model = new GraphFxModel();
            model.addVariable("a", BigDecimal.ZERO);
            model.addVariable("b", BigDecimal.ZERO);

            final String suggested = model.nextSuggestedVariableName();

            assertNotNull(suggested);
            assertNotEquals("a", suggested.toLowerCase());
            assertNotEquals("b", suggested.toLowerCase());
        }
    }

    @Nested
    @DisplayName("Revision")
    final class RevisionTests {

        @Test
        void revision_bumpsOnSettingsAndSelectionChanges() {
            final GraphFxModel model = new GraphFxModel();
            final long r0 = model.getRevision().get();

            model.getSettings().showGridProperty().set(!model.getSettings().isShowGrid());
            final long r1 = model.getRevision().get();

            final GraphFxFunction f = model.addFunction("f", "x");
            model.setSelectedFunction(f);
            final long r2 = model.getRevision().get();

            assertTrue(r1 > r0);
            assertTrue(r2 > r1);
        }
    }

}
