/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.calculator.expression;

import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.calculator.exceptions.ProcessingErrorException;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Contract tests for {@link ExpressionElement#apply(Deque, MathContext, TrigonometricMode, Locale)}.
 *
 * <p>The base implementation must throw a typed {@link ProcessingErrorException} with code
 * {@link CalculatorErrorCode#PROCESSING_INTERNAL} when a subclass forgets to override
 * {@code apply(...)}. This locks in the M11 fix: previously the base threw a raw
 * {@link UnsupportedOperationException}, which surfaced to callers as an unstructured
 * runtime error and bypassed the localized error pipeline.</p>
 */
class ExpressionElementContractTest {

    /**
     * Concrete element that intentionally does not override {@code apply(...)}, so it inherits
     * the base behaviour and can be used to exercise it directly.
     */
    private static final class UnimplementedElement extends ExpressionElement {
        UnimplementedElement(final String symbol) {
            super(symbol, false, 0);
        }
    }

    @Test
    @DisplayName("base apply() throws ProcessingErrorException with code PROCESSING_INTERNAL when subclass does not override")
    void baseApplyThrowsTypedInternalError() {
        final ExpressionElement element = new UnimplementedElement("?");
        final Deque<Object> stack = new ArrayDeque<>();

        final ProcessingErrorException thrown = assertThrows(
                ProcessingErrorException.class,
                () -> element.apply(stack, MathContext.DECIMAL64, TrigonometricMode.DEG, Locale.US));

        assertEquals(CalculatorErrorCode.PROCESSING_INTERNAL, thrown.getCalculatorError().code(),
                "unimplemented apply() must surface as PROCESSING_INTERNAL, not an untyped runtime error");
    }

    @Test
    @DisplayName("base apply() includes the offending element's symbol in the structured parameters")
    void baseApplyExposesSymbolInErrorParameters() {
        final ExpressionElement element = new UnimplementedElement("Ξ");
        final Deque<Object> stack = new ArrayDeque<>();

        final ProcessingErrorException thrown = assertThrows(
                ProcessingErrorException.class,
                () -> element.apply(stack, MathContext.DECIMAL64, TrigonometricMode.DEG, Locale.US));

        assertEquals("Ξ", thrown.getCalculatorError().params().get("symbol"),
                "the offending symbol must be exposed as a structured parameter so it can be localized");
    }
}
