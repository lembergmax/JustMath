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
package com.mlprograms.justmath.calculator.errors;

import static org.junit.jupiter.api.Assertions.*;

import com.mlprograms.justmath.calculator.CalculatorEngine;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

class CalculatorErrorLocalizationTest {

    @Test
    void allCodesHaveEnglishAndGermanBundleEntries() {
        ResourceBundle en = ResourceBundle.getBundle(CalculatorError.BUNDLE_BASENAME, Locale.ENGLISH);
        ResourceBundle de = ResourceBundle.getBundle(CalculatorError.BUNDLE_BASENAME, Locale.GERMAN);

        for (CalculatorErrorCode code : CalculatorErrorCode.values()) {
            assertDoesNotThrow(() -> en.getString(code.getBundleKey()),
                    "Missing EN bundle entry for " + code);
            assertDoesNotThrow(() -> de.getString(code.getBundleKey()),
                    "Missing DE bundle entry for " + code);
        }
    }

    @Test
    void germanInvalidCharacterMessageContainsUmlaut() {
        CalculatorError err = new CalculatorError(
                CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                java.util.Map.of("character", "#"),
                "Invalid character '#' at position 1",
                1);
        String german = err.format(Locale.GERMAN, ErrorMode.USER_FRIENDLY);
        assertTrue(german.startsWith("Ungültiges Zeichen"), "got: " + german);
        assertTrue(german.contains("'#'"));
        assertTrue(german.contains("Position 1"));
    }

    @Test
    void englishDivisionByZeroIsLocalized() {
        CalculatorError err = new CalculatorError(
                CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO,
                "Division by zero");
        assertEquals("Division by zero is not allowed.",
                err.format(Locale.ENGLISH, ErrorMode.USER_FRIENDLY));
        assertEquals("Division durch Null ist nicht erlaubt.",
                err.format(Locale.GERMAN, ErrorMode.USER_FRIENDLY));
    }

    @Test
    void rawModeReturnsTechnicalDetailUnchanged() {
        CalculatorError err = new CalculatorError(
                CalculatorErrorCode.SYNTAX_UNKNOWN_FUNCTION,
                java.util.Map.of("function", "fooBar"),
                "Unknown operator or function: fooBar");
        assertEquals("Unknown operator or function: fooBar",
                err.format(Locale.GERMAN, ErrorMode.RAW));
    }

    @Test
    void engineUserFriendlyModeReturnsLocalizedMessage() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(ErrorMode.USER_FRIENDLY);

        String msg = engine.evaluateToString("2#3");
        assertTrue(msg.toLowerCase().contains("zeichen"), "got: " + msg);
        assertFalse(msg.toLowerCase().contains("exception"), "msg leaked exception keyword: " + msg);
    }

    @Test
    void engineRawModeStillReturnsLegacyMessageForBackCompat() {
        CalculatorEngine engine = new CalculatorEngine(); // default RAW + ENGLISH
        String msg = engine.evaluateToString("2#3");
        assertEquals("Syntax Error", msg);
    }

    @Test
    void evaluateSafeReturnsFailureWithMatchingCode() {
        CalculatorEngine engine = new CalculatorEngine();
        CalculatorResult<?> result = engine.evaluateSafe("2#3");
        assertTrue(result.isFailure());
        assertEquals(CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                result.error().orElseThrow().code());
    }

    @Test
    void evaluateSafeSuccessCarriesValue() {
        CalculatorEngine engine = new CalculatorEngine();
        CalculatorResult<com.mlprograms.justmath.bignumber.BigNumber> result =
                engine.evaluateSafe("2+3");
        assertTrue(result.isSuccess());
        assertEquals("5", result.value().orElseThrow().toString());
    }

    @Test
    void missingBundleEntryFallsBackToTechnicalDetail() {
        // Sanity: with a locale that has no override, default bundle still works
        CalculatorError err = new CalculatorError(
                CalculatorErrorCode.PROCESSING_INTERNAL,
                "raw detail");
        // any unrelated locale -> falls back to default bundle
        String text = err.format(Locale.JAPAN, ErrorMode.USER_FRIENDLY);
        assertNotNull(text);
        assertFalse(text.contains("Exception"));
    }

    @Test
    void bundleLoadsForDefaultLocale() {
        assertDoesNotThrow(() ->
                ResourceBundle.getBundle(CalculatorError.BUNDLE_BASENAME));
    }

    @Test
    void divisionByZeroLocalizedInGerman() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(ErrorMode.USER_FRIENDLY);
        String msg = engine.evaluateToString("5/0");
        assertEquals("Division durch Null ist nicht erlaubt.", msg);
    }

    @Test
    void domainErrorLocalizedInGerman() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(ErrorMode.USER_FRIENDLY);
        String msg = engine.evaluateToString("sqrt(-4)");
        assertEquals("Wert liegt außerhalb des zulässigen Bereichs für diese Operation.", msg);
    }

    @Test
    void prettyStringLocalizesArithmeticErrors() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(ErrorMode.USER_FRIENDLY);
        String msg = engine.evaluateToPrettyString("ln(-1)");
        assertFalse(msg.toLowerCase().contains("exception"));
        assertTrue(msg.startsWith("Wert liegt") || msg.startsWith("Der Ausdruck"),
                "expected localized DE message, got: " + msg);
    }

    @Test
    void evaluateSafeClassifiesDivisionByZero() {
        CalculatorEngine engine = new CalculatorEngine();
        CalculatorResult<?> result = engine.evaluateSafe("5/0");
        assertTrue(result.isFailure());
        assertEquals(CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO,
                result.error().orElseThrow().code());
    }

    @Test
    void unknownVariableLocalizedMessage() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(ErrorMode.USER_FRIENDLY);
        String msg = engine.evaluateToString("x+1", java.util.Map.of());
        assertTrue(msg.toLowerCase().contains("variable"), "got: " + msg);
    }

    @Test
    void trailingOperatorClassifiedAsSyntaxError() {
        CalculatorEngine engine = new CalculatorEngine();
        CalculatorResult<?> result = engine.evaluateSafe("25+3.6*");
        assertTrue(result.isFailure());
        assertEquals(CalculatorErrorCode.SYNTAX_INCOMPLETE_EXPRESSION,
                result.error().orElseThrow().code());
    }

    @Test
    void trailingOperatorLocalizedAsIncompleteExpressionInGerman() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(ErrorMode.USER_FRIENDLY);
        String msg = engine.evaluateToString("25+3.6*");
        assertEquals("Der Ausdruck ist unvollständig oder die Betragsstriche sind unausgeglichen.", msg);
    }

    @Test
    void trailingOperatorRawModeReturnsSyntaxErrorCategory() {
        CalculatorEngine engine = new CalculatorEngine();
        String msg = engine.evaluateToString("25+3.6*");
        assertEquals("Syntax Error", msg);
    }
}
