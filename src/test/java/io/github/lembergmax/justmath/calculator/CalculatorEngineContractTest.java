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

package io.github.lembergmax.justmath.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Map;

import io.github.lembergmax.justmath.calculator.errors.CalculatorError;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.errors.CalculatorResult;
import io.github.lembergmax.justmath.calculator.errors.ErrorMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The parts of the {@link CalculatorEngine} contract that the behavioural tests do not reach: builder-style setters,
 * the copy returned for the active variables, the language registry, the length limit and the name of an undefined
 * variable.
 */
class CalculatorEngineContractTest {

    private static final int MAX_EXPRESSION_LENGTH = 100_000;

    @Test
    @DisplayName("every setter returns the engine it was called on")
    void settersReturnTheEngine() {
        final CalculatorEngine engine = new CalculatorEngine();

        assertSame(engine, engine.setLocale(Locale.GERMANY));
        assertSame(engine, engine.setErrorMode(ErrorMode.USER_FRIENDLY));
        assertSame(engine, engine.setInputLocale(Locale.FRANCE));
        assertSame(engine, engine.setExpressionCacheEnabled(true));
        assertSame(engine, engine.setExpressionCacheSize(16));
    }

    @Test
    @DisplayName("getCurrentVariables() returns a modifiable copy that is empty outside an evaluation")
    void currentVariablesAreAModifiableCopy() {
        final Map<String, String> first = CalculatorEngine.getCurrentVariables();
        first.put("leaked", "1");

        assertTrue(CalculatorEngine.getCurrentVariables().isEmpty(), "a change to the copy must not reach the thread");
    }

    @Test
    @DisplayName("isLanguageSupported() accepts a supported language and rejects an unknown one")
    void languageSupportIsReported() {
        assertTrue(CalculatorEngine.isLanguageSupported(Locale.GERMANY));
        assertTrue(CalculatorEngine.isLanguageSupported(Locale.forLanguageTag("fr-CA")));
        assertFalse(CalculatorEngine.isLanguageSupported(Locale.forLanguageTag("tlh")));
        assertFalse(CalculatorEngine.isLanguageSupported(Locale.ROOT));
    }

    @Test
    @DisplayName("an expression of exactly the maximum length is evaluated, one character more is rejected")
    void expressionLengthLimitIsInclusive() {
        final CalculatorEngine engine = new CalculatorEngine();
        final String atTheLimit = "1" + " ".repeat(MAX_EXPRESSION_LENGTH - 1);
        final String overTheLimit = atTheLimit + " ";

        assertEquals("1", engine.evaluateSafeToString(atTheLimit));
        final CalculatorResult<?> rejected = engine.evaluateSafe(overTheLimit);
        assertTrue(rejected.isFailure());
        assertEquals(CalculatorErrorCode.MATH_OVERFLOW, rejected.error().orElseThrow().code());
    }

    @Test
    @DisplayName("an undefined variable is reported with its name")
    void undefinedVariableCarriesItsName() {
        final CalculatorError error = new CalculatorEngine().evaluateSafe("2*unknownValue").error().orElseThrow();

        assertEquals(CalculatorErrorCode.SYNTAX_UNKNOWN_VARIABLE, error.code());
        assertEquals("unknownValue", error.params().get("variable"));
    }
}
