/*
 * Copyright (c) 2026 Max Lemberg
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
package io.github.lembergmax.justmath.calculator.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link CalculatorError} is documented as an immutable value object. A record only makes the reference to its
 * parameter map final, so the map has to be copied at construction time.
 */
class CalculatorErrorImmutabilityTest {

    @Test
    @DisplayName("changing the map after construction does not change the error")
    void parametersAreCopiedAtConstruction() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("function", "sqrt");
        final CalculatorError error = new CalculatorError(CalculatorErrorCode.SYNTAX_EMPTY_FUNCTION_ARGUMENT, parameters, "Function 'sqrt' was called without an argument");

        parameters.put("function", "sin");
        parameters.put("extra", "value");

        assertEquals(Map.of("function", "sqrt"), error.params());
        assertEquals(
                error.format(Locale.ENGLISH, ErrorMode.USER_FRIENDLY),
                new CalculatorError(CalculatorErrorCode.SYNTAX_EMPTY_FUNCTION_ARGUMENT, Map.of("function", "sqrt"), "Function 'sqrt' was called without an argument").format(Locale.ENGLISH, ErrorMode.USER_FRIENDLY)
        );
    }

    @Test
    @DisplayName("the parameter map returned by the error cannot be modified")
    void returnedParametersAreUnmodifiable() {
        final CalculatorError error = new CalculatorError(CalculatorErrorCode.SYNTAX_UNKNOWN_VARIABLE, Map.of("variable", "x"), "Variable 'x' is not defined.");

        assertThrows(UnsupportedOperationException.class, () -> error.params().put("variable", "y"));
    }
}
