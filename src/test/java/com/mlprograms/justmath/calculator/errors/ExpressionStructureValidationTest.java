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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Behavioural coverage for the structural pre-evaluation validation introduced to make
 * malformed input fail fast with a precise, Casio-style error instead of computing an
 * expensive sub-expression or returning a misclassified "Processing Error".
 *
 * <p>
 * This class is intentionally <em>complementary</em> to
 * {@link CalculatorErrorLocalizationTest}: it does not re-assert the basic code mappings
 * already covered there. Instead it focuses on advanced valid expressions (false-positive
 * guard), additional malformed shapes, the localized messages of the newly added codes,
 * the performance guarantee, and the guarantee that a structural error never leaks as a
 * processing/math error.
 * </p>
 */
@DisplayName("Structural expression validation")
class ExpressionStructureValidationTest {

    private CalculatorEngine raw() {
        return new CalculatorEngine();
    }

    private CalculatorEngine ui(final Locale locale) {
        return new CalculatorEngine().setLocale(locale).setErrorMode(ErrorMode.USER_FRIENDLY);
    }

    @Nested
    @DisplayName("Valid expressions are never rejected (false-positive guard)")
    class ValidExpressions {

        @Test
        @DisplayName("advanced valid expressions still evaluate successfully")
        void advancedValidExpressionsRemainValid() {
            CalculatorEngine engine = raw();
            String[] valid = {
                    "Pol(3;4)", "Rec(5;53.13)", "logbase(8;2)", "product(1;3;k)",
                    "∏(1;3;k)", "gamma(5)", "beta(2;3)", "atan2(1;1)",
                    "rootn(27;3)", "summation(1;5;k)", "avg(1;2;3)", "sum(1;2;3)",
                    "median(3;1;2)", "((1+2))", "2sin(30)", "(2+3)!", "abs(0-9)",
            };
            for (String expr : valid) {
                assertTrue(engine.evaluateSafe(expr).isSuccess(),
                        "valid expression was rejected: " + expr);
            }
        }

        @Test
        @DisplayName("integer-stable valid expressions yield the exact expected result")
        void exactValueExpressions() {
            CalculatorEngine engine = raw();
            assertEquals("6", engine.evaluateToString("product(1;3;k)"));
            assertEquals("15", engine.evaluateToString("summation(1;5;k)"));
            assertEquals("120", engine.evaluateToString("(2+3)!"));
            assertEquals("3628800", engine.evaluateToString("((2+3)*2)!"));
            assertEquals("512", engine.evaluateToString("2^3^2"));
            assertEquals("6", engine.evaluateToString("gcd(12;18)"));
            assertEquals("12", engine.evaluateToString("lcm(4;6)"));
            assertEquals("2", engine.evaluateToString("median(3;1;2)"));
            assertEquals("24", engine.evaluateToString("gamma(5)"));
            assertEquals("1", engine.evaluateToString("sin(0)+cos(0)"));
            assertEquals("2", engine.evaluateToString("avg(1;2;3)"));
        }
    }

    @Nested
    @DisplayName("Additional malformed shapes map to a specific code and fail fast")
    class MalformedExpressions {

        @Test
        @DisplayName("each malformed shape resolves to its specific code in < 2 s")
        void additionalMalformedExpressionsHaveSpecificCodes() {
            CalculatorEngine engine = raw();
            Object[][] cases = {
                    {"^2", CalculatorErrorCode.SYNTAX_LEADING_OPERATOR},
                    {"nPr 2", CalculatorErrorCode.SYNTAX_LEADING_OPERATOR},
                    {"%5", CalculatorErrorCode.SYNTAX_LEADING_OPERATOR},
                    {"5!!", CalculatorErrorCode.SYNTAX_INVALID_FACTORIAL},
                    {"sin()", CalculatorErrorCode.SYNTAX_EMPTY_FUNCTION_ARGUMENT},
                    {"(   )", CalculatorErrorCode.SYNTAX_EMPTY_PARENTHESES},
                    {"atan2(1)", CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT},
                    {"rootn(27)", CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT},
                    {"summation(1;5)", CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT},
                    {"sqrt(1;2)", CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT},
                    {"2!3", CalculatorErrorCode.SYNTAX_MISSING_OPERATOR},
                    {"sin", CalculatorErrorCode.SYNTAX_MISSING_OPERAND},
                    {"3 4", CalculatorErrorCode.SYNTAX_MISSING_OPERATOR},
                    {"5!sqrt(4)", CalculatorErrorCode.SYNTAX_MISSING_OPERATOR},
            };
            for (Object[] c : cases) {
                String expr = (String) c[0];
                long start = System.nanoTime();
                var result = engine.evaluateSafe(expr);
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;

                assertTrue(result.isFailure(), "expected failure: " + expr);
                assertEquals(c[1], result.error().orElseThrow().code(), "wrong code: " + expr);
                assertTrue(elapsedMs < 2_000, "slow fail (" + elapsedMs + " ms): " + expr);
            }
        }

        @Test
        @DisplayName("expensive left operand never computed before the structural error")
        void expensiveLeftOperandFailsFast() {
            CalculatorEngine engine = raw();
            for (String expr : new String[]{
                    "50000!/", "50000!*", "50000!^", "50000!nPr", "50000!sqrt()"}) {
                long start = System.nanoTime();
                var result = engine.evaluateSafe(expr);
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                assertTrue(result.isFailure(), expr);
                assertTrue(elapsedMs < 2_000,
                        "factorial was computed before failing (" + elapsedMs + " ms): " + expr);
            }
        }
    }

    @Nested
    @DisplayName("Error classification & localization")
    class Classification {

        @Test
        @DisplayName("a structural error never surfaces as Processing/Math Error")
        void structuralErrorNeverReportsProcessingOrMathError() {
            CalculatorEngine engine = raw(); // default RAW -> category label
            for (String expr : new String[]{
                    "*5", "5+", "()", "sqrt()", "5000!sqrt()", "!5", "2!3",
                    "(2+3", "2+3)", "5;3", "atan2(1)", "5!!", "^2"}) {
                String message = engine.evaluateToString(expr);
                assertNotEquals("Processing Error", message, "misclassified: " + expr);
                assertNotEquals("Math Error", message, "misclassified: " + expr);
            }
        }

        @Test
        @DisplayName("RAW mode keeps the legacy \"Syntax Error\" category for the new codes")
        void rawModeKeepsLegacySyntaxCategory() {
            CalculatorEngine engine = raw();
            for (String expr : new String[]{"*5", "()", "sin()", "5!!", "^2"}) {
                assertEquals("Syntax Error", engine.evaluateToString(expr), expr);
            }
        }

        @Test
        @DisplayName("new codes carry precise localized messages in DE and EN")
        void newCodeMessagesLocalized() {
            CalculatorEngine de = ui(Locale.GERMAN);
            CalculatorEngine en = ui(Locale.ENGLISH);

            assertEquals("Der Ausdruck beginnt mit dem Operator '*', der linke Operand fehlt.",
                    de.evaluateToString("*5"));
            assertEquals("The expression starts with the operator '*' but is missing its left-hand operand.",
                    en.evaluateToString("*5"));

            assertEquals("Leeres Klammerpaar '()' ohne Inhalt.",
                    de.evaluateToString("()"));
            assertEquals("Empty pair of parentheses '()' with no content.",
                    en.evaluateToString("()"));

            assertEquals("Dem Operator bzw. der Funktion 'sin' fehlt ein Operand.",
                    de.evaluateToString("sin"));
            assertEquals("Operator or function 'sin' is missing an operand.",
                    en.evaluateToString("sin"));

            assertEquals("Zwischen zwei Operanden fehlt ein Operator.",
                    de.evaluateToString("3 4"));
            assertEquals("Two operands are not connected by an operator.",
                    en.evaluateToString("3 4"));
        }

        @Test
        @DisplayName("variadic argument-count mismatch is an Argument Error category")
        void variadicArgumentCountMismatchCategory() {
            CalculatorEngine engine = raw();
            assertEquals("Argument Error", engine.evaluateToString("summation(1;5)"));
        }
    }

    @Nested
    @DisplayName("Safety net")
    class SafetyNet {

        @Test
        @DisplayName("a tokenizer-originated failure is reported as a syntax error")
        void tokenizerFailureBecomesSyntaxError() {
            // Leading factorial drives the tokenizer down a path that historically threw
            // an unchecked exception misclassified as a processing error.
            CalculatorEngine engine = raw();
            var result = engine.evaluateSafe("!5");
            assertTrue(result.isFailure());
            assertEquals(CalculatorErrorCode.SYNTAX_INVALID_FACTORIAL,
                    result.error().orElseThrow().code());
            assertEquals("Syntax Error", engine.evaluateToString("!5"));
        }

        @Test
        @DisplayName("empty input is the neutral element, not an error")
        void blankInputIsZero() {
            CalculatorEngine engine = raw();
            assertEquals("0", engine.evaluateToString(""));
            assertEquals("0", engine.evaluateToString("   "));
        }
    }

    @Nested
    @DisplayName("Unary, whitespace, charset & argument-count rules")
    class SyntaxRules {

        @Test
        @DisplayName("prefix unary +/- applies to groups, functions, constants, variables")
        void prefixUnaryOperator() {
            CalculatorEngine e = raw();
            assertEquals("-7", e.evaluateToString("-(3+4)"));
            assertEquals("0", e.evaluateToString("-sin(0)"));
            assertEquals("-5", e.evaluateToString("-(-(-5))"));
            assertEquals("-4", e.evaluateToString("2*-(1+1)"));
            assertEquals("0.125", e.evaluateToString("2^-3"));
            assertEquals("5", e.evaluateToString("--5"));
            assertEquals("-4", e.evaluateToString("-abs(0-4)"));
            assertEquals("-4", e.evaluateToString("-(2)^2"));
        }

        @Test
        @DisplayName("whitespace separates operands; '3 4' is not '34'")
        void whitespaceIsSeparator() {
            CalculatorEngine e = raw();
            assertEquals("3", e.evaluateToString("1 + 2"));
            assertEquals("6", e.evaluateToString("2 * 3"));
            assertEquals(CalculatorErrorCode.SYNTAX_MISSING_OPERATOR,
                    e.evaluateSafe("3 4").error().orElseThrow().code());
            assertEquals(CalculatorErrorCode.SYNTAX_MISSING_OPERATOR,
                    e.evaluateSafe("12 34").error().orElseThrow().code());
        }

        @Test
        @DisplayName("no implicit multiplication after a postfix factorial")
        void noImplicitMultiplicationAfterFactorial() {
            CalculatorEngine e = raw();
            assertEquals(CalculatorErrorCode.SYNTAX_MISSING_OPERATOR,
                    e.evaluateSafe("5!sqrt(4)").error().orElseThrow().code());
            assertEquals("240", e.evaluateToString("5!*2")); // explicit operator works
        }

        @Test
        @DisplayName("non-ASCII characters are invalid, not unknown variables")
        void nonAsciiIsInvalidCharacter() {
            CalculatorEngine e = raw();
            assertEquals(CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                    e.evaluateSafe("5000ß!sqrt(4)").error().orElseThrow().code());
            assertEquals(CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                    e.evaluateSafe("2±3").error().orElseThrow().code());
            // registered non-ASCII symbols still work
            assertEquals("4", e.evaluateToString("√16"));
            assertEquals("12", e.evaluateToString("3×4"));
        }

        @Test
        @DisplayName("fixed-arity function argument counts are checked precisely")
        void functionArgumentCountIsPrecise() {
            CalculatorEngine e = raw();
            for (String tooMany : new String[]{"sqrt(1;2)", "ln(1;2)"}) {
                assertEquals(CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT,
                        e.evaluateSafe(tooMany).error().orElseThrow().code(), tooMany);
            }
            for (String tooFew : new String[]{"atan2(1)", "rootn(27)", "logbase(8)"}) {
                assertEquals(CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT,
                        e.evaluateSafe(tooFew).error().orElseThrow().code(), tooFew);
            }
            // correct counts still evaluate
            assertEquals("4", e.evaluateToString("sqrt(16)"));
            assertTrue(e.evaluateSafe("atan2(1;1)").isSuccess());
        }
    }
}
