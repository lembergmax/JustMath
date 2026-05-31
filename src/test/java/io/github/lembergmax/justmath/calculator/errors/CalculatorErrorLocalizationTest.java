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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import io.github.lembergmax.justmath.calculator.CalculatorEngine;

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
    void parameterValueContainingPlaceholderIsNotReinjected() {
        // A parameter value that literally contains "{position}" must not be turned into the numeric
        // position by a second substitution pass. Template: "Invalid character '{character}' at
        // position {position}." with character = "{position}" and position = 7.
        CalculatorError err = new CalculatorError(
                CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                java.util.Map.of("character", "{position}"),
                "Invalid character '{position}' at position 7",
                7);
        assertEquals("Invalid character '{position}' at position 7.",
                err.format(Locale.ENGLISH, ErrorMode.USER_FRIENDLY));
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
        CalculatorResult<io.github.lembergmax.justmath.bignumber.BigNumber> result =
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
        assertEquals("Die gerade Wurzel einer negativen Zahl ist keine reelle Zahl.", msg);
    }

    @Test
    void prettyStringLocalizesArithmeticErrors() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(ErrorMode.USER_FRIENDLY);
        String msg = engine.evaluateToPrettyString("ln(-1)");
        assertFalse(msg.toLowerCase().contains("exception"));
        assertEquals("Der Logarithmus ist für nicht-positive Argumente nicht definiert.", msg);
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
        assertEquals(CalculatorErrorCode.SYNTAX_TRAILING_OPERATOR,
                result.error().orElseThrow().code());
    }

    @Test
    void trailingOperatorLocalizedAsTrailingOperatorInGerman() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(ErrorMode.USER_FRIENDLY);
        String msg = engine.evaluateToString("25+3.6*");
        assertEquals("Der Ausdruck endet mit dem Operator '*', der rechte Operand fehlt.", msg);
    }

    @Test
    void trailingOperatorRawModeReturnsSyntaxErrorCategory() {
        CalculatorEngine engine = new CalculatorEngine();
        String msg = engine.evaluateToString("25+3.6*");
        assertEquals("Syntax Error", msg);
    }

    @Test
    void trailingOperatorAfterFactorialFailsFastWithoutComputingFactorial() {
        CalculatorEngine engine = new CalculatorEngine();
        long start = System.nanoTime();
        CalculatorResult<?> result = engine.evaluateSafe("50000!/");
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertTrue(result.isFailure());
        assertEquals(CalculatorErrorCode.SYNTAX_TRAILING_OPERATOR,
                result.error().orElseThrow().code());
        // Computing 50000! takes tens of seconds; the pre-check must short-circuit.
        assertTrue(elapsedMs < 2_000,
                "trailing-operator pre-check did not short-circuit; took " + elapsedMs + " ms");
    }

    @Test
    void structuralValidatorAcceptsValidExpressionsUnchanged() {
        CalculatorEngine engine = new CalculatorEngine();
        String[][] cases = {
                {"1+2", "3"}, {"-1234.56", "-1234.56"}, {"1/2", "0.5"},
                {"2sin(30)", "1"}, {"2(3+4)", "14"}, {"(2+3)!", "120"},
                {"sqrt(16)", "4"}, {"summation(1;5;k)", "15"}, {"avg(2;4;6)", "4"},
                {"sum(1;2;3;4)", "10"}, {"abs(0-7)", "7"}, {"3*-2", "-6"},
                {"5--3", "8"}, {"rootn(27;3)", "3"},
        };
        for (String[] c : cases) {
            assertEquals(c[1], engine.evaluateToString(c[0]), "valid expression broke: " + c[0]);
        }
    }

    @Test
    void structuralValidatorRejectsMalformedExpressionsWithSpecificCodes() {
        CalculatorEngine engine = new CalculatorEngine();
        Object[][] cases = {
                {"*5", CalculatorErrorCode.SYNTAX_LEADING_OPERATOR},
                {"/3", CalculatorErrorCode.SYNTAX_LEADING_OPERATOR},
                {"5+", CalculatorErrorCode.SYNTAX_TRAILING_OPERATOR},
                {"50000!/", CalculatorErrorCode.SYNTAX_TRAILING_OPERATOR},
                {"50000!*", CalculatorErrorCode.SYNTAX_TRAILING_OPERATOR},
                {"()", CalculatorErrorCode.SYNTAX_EMPTY_PARENTHESES},
                {"sqrt()", CalculatorErrorCode.SYNTAX_EMPTY_FUNCTION_ARGUMENT},
                {"5000!sqrt()", CalculatorErrorCode.SYNTAX_EMPTY_FUNCTION_ARGUMENT},
                {"!5", CalculatorErrorCode.SYNTAX_INVALID_FACTORIAL},
                {"2!3", CalculatorErrorCode.SYNTAX_MISSING_OPERATOR},
                {"rootn(27)", CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT},
                {"sqrt(1;2)", CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT},
                {"(2+3", CalculatorErrorCode.SYNTAX_UNMATCHED_PAREN},
                {"2+3)", CalculatorErrorCode.SYNTAX_UNMATCHED_PAREN},
                {"5;3", CalculatorErrorCode.SYNTAX_MISPLACED_SEPARATOR},
        };
        for (Object[] c : cases) {
            String expr = (String) c[0];
            long start = System.nanoTime();
            CalculatorResult<?> result = engine.evaluateSafe(expr);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            assertTrue(result.isFailure(), "expected failure for: " + expr);
            assertEquals(c[1], result.error().orElseThrow().code(), "wrong code for: " + expr);
            assertTrue(elapsedMs < 2_000,
                    "did not fail fast (" + elapsedMs + " ms) for: " + expr);
        }
    }

    @Test
    void emptyFunctionArgumentAfterFactorialFailsFastWithSpecificMessage() {
        CalculatorEngine engine = new CalculatorEngine();
        long start = System.nanoTime();
        CalculatorResult<?> result = engine.evaluateSafe("5000!sqrt()");
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertTrue(result.isFailure());
        assertEquals(CalculatorErrorCode.SYNTAX_EMPTY_FUNCTION_ARGUMENT,
                result.error().orElseThrow().code());
        assertTrue(elapsedMs < 2_000,
                "empty-function pre-check did not short-circuit; took " + elapsedMs + " ms");

        String de = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(io.github.lembergmax.justmath.calculator.errors.ErrorMode.USER_FRIENDLY)
                .evaluateToString("5000!sqrt()");
        assertEquals("Funktion 'sqrt' wurde ohne Argument aufgerufen.", de);
    }

    @Test
    void leadingFactorialIsSyntaxErrorNotProcessingError() {
        CalculatorEngine raw = new CalculatorEngine();
        for (String expr : new String[]{"!5", "!", "!sqrt(4)"}) {
            CalculatorResult<?> result = raw.evaluateSafe(expr);
            assertTrue(result.isFailure(), expr);
            assertEquals(CalculatorErrorCode.SYNTAX_INVALID_FACTORIAL,
                    result.error().orElseThrow().code(), "wrong code for " + expr);
            assertEquals("Syntax Error", raw.evaluateToString(expr), "wrong RAW for " + expr);
        }
    }

    @Test
    void everyCodeHasCategoryAndGenericFallbackEntries() {
        for (Locale locale : new Locale[]{Locale.ENGLISH, Locale.GERMAN}) {
            ResourceBundle bundle = ResourceBundle.getBundle(CalculatorError.BUNDLE_BASENAME, locale);
            assertDoesNotThrow(() -> bundle.getString(CalculatorErrorCode.GENERIC_BUNDLE_KEY),
                    "Missing generic fallback for " + locale);
            for (CalculatorErrorCode code : CalculatorErrorCode.values()) {
                assertDoesNotThrow(() -> bundle.getString(code.getCategoryBundleKey()),
                        "Missing category fallback " + code.getCategoryBundleKey() + " for " + locale);
            }
        }
    }

    @Test
    void formatFallsBackToCategoryThenGenericWhenSpecificKeyMissing() {
        // Synthetic code-free probe: a CalculatorError whose specific key is absent from
        // the bundle must resolve to the localized category text, never the English
        // technical detail.
        CalculatorError err = new CalculatorError(
                CalculatorErrorCode.PROCESSING_INTERNAL, "raw technical detail");
        String de = err.format(Locale.GERMAN, io.github.lembergmax.justmath.calculator.errors.ErrorMode.USER_FRIENDLY);
        assertFalse(de.equals("raw technical detail"), "fallback leaked technical detail");
        assertFalse(de.toLowerCase().contains("exception"));
    }

    @Test
    void factorialOfNegativeLocalizedInGerman() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(io.github.lembergmax.justmath.calculator.errors.ErrorMode.USER_FRIENDLY);
        String msg = engine.evaluateToString("(0-5)!");
        assertEquals("Die Fakultät ist für negative Zahlen nicht definiert.", msg);
    }

    @Test
    void logOfZeroLocalizedInGerman() {
        CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(io.github.lembergmax.justmath.calculator.errors.ErrorMode.USER_FRIENDLY);
        String msg = engine.evaluateToString("ln(0)");
        assertEquals("Der Logarithmus ist für nicht-positive Argumente nicht definiert.", msg);
    }

    @Test
    void divisionByZeroRawModeReturnsMathErrorCategory() {
        CalculatorEngine engine = new CalculatorEngine(); // default RAW
        String msg = engine.evaluateToString("5/0");
        assertEquals("Math Error", msg);
    }
}
