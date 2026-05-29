/*
 * Copyright (c) 2025-2026 Max Lemberg
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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import io.github.lembergmax.justmath.calculator.exceptions.SyntaxErrorException;
import io.github.lembergmax.justmath.calculator.internal.Token;

class TokenizerTest {

    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    void testSimpleExpression() {
        List<Token> tokens = tokenizer.tokenize("3+4");

        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "4")
        ), tokens);
    }

    @Test
    void testExpressionWithWhitespace() {
        List<Token> tokens = tokenizer.tokenize("  3 +   4 ");

        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "4")
        ), tokens);
    }

    @Test
    void testNegativeNumberAtStart() {
        List<Token> tokens = tokenizer.tokenize("-3");

        // Sign is emitted as a separate UNARY_OPERATOR token rather than
        // folded into the number, so that '-3^2' parses as '-(3^2)' = -9.
        assertEquals(List.of(
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3")
        ), tokens);
    }

    @Test
    void testSignedNumberAfterOperator() {
        List<Token> tokens = tokenizer.tokenize("5*-2");

        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "2")
        ), tokens);
    }

    @Test
    void testSignedNumberAfterLeftParenthesis() {
        List<Token> tokens = tokenizer.tokenize("(-3)");

        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testFixAfterRightParenthesis() {
        List<Token> tokens = tokenizer.tokenize("(2)-3");

        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3")
        ), tokens);
    }

    @Test
    void testMultipleConsecutiveSigns() {
        List<Token> tokens = tokenizer.tokenize("5--3");

        // Aggressive sign-merge: '--' after the operand '5' is a binary
        // context with two minuses (even) → net '+'. Eval unchanged: 5+3 == 8.
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "3")
        ), tokens);
    }

    @Test
    void testMultipleSignsOdd() {
        List<Token> tokens = tokenizer.tokenize("5---3");

        // Aggressive sign-merge: '---' after the operand '5' is a binary
        // context with three minuses (odd) → net '-'. Eval unchanged: 5-3 == 2.
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3")
        ), tokens);
    }

    @Test
    void testParentheses() {
        List<Token> tokens = tokenizer.tokenize("(1+2)");

        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testSemicolon() {
        List<Token> tokens = tokenizer.tokenize("1;2");

        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.SEMICOLON, ";"),
                new Token(Token.Type.NUMBER, "2")
        ), tokens);
    }

    @Test
    void testConstantPi() {
        List<Token> tokens = tokenizer.tokenize("pi");

        assertEquals(List.of(
                new Token(Token.Type.CONSTANT, "pi")
        ), tokens);
    }

    @Test
    void testConstantE() {
        List<Token> tokens = tokenizer.tokenize("e");

        assertEquals(List.of(
                new Token(Token.Type.CONSTANT, "e")
        ), tokens);
    }

    @Test
    void testFunctionSqrt() {
        List<Token> tokens = tokenizer.tokenize("√(4)");

        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "√"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "4"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testInvalidCharacterThrowsException() {
        assertThrows(Exception.class, () -> tokenizer.tokenize("2#3"));
    }

    @Test
    void testDecimalNumber() {
        List<Token> tokens = tokenizer.tokenize("3.14 + 2.0");

        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "3.14"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "2.0")
        ), tokens);
    }

    @Test
    void testComplexExpression() {
        List<Token> tokens = tokenizer.tokenize("3 + (4*-pi) - √(9) ; e");

        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "4"),
                new Token(Token.Type.OPERATOR, "*"),
                // Prefix '-' before the constant 'pi' is a unary-minus operator.
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.CONSTANT, "pi"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "-"),
                new Token(Token.Type.FUNCTION, "√"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "9"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.SEMICOLON, ";"),
                new Token(Token.Type.CONSTANT, "e")
        ), tokens);
    }

    @Test
    void testExpressionWithLeadingPlus() {
        List<Token> tokens = tokenizer.tokenize("+2");

        // Leading '+' in unary context is a no-op; aggressive sign-merging
        // drops it entirely.
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "2")
        ), tokens);
    }

    @Test
    void testExpressionWithPlusAfterParenthesis() {
        List<Token> tokens = tokenizer.tokenize("(2)+3");

        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "3")
        ), tokens);
    }

    @Test
    void testSignedNumberWithDecimal() {
        List<Token> tokens = tokenizer.tokenize("-3.5");

        assertEquals(List.of(
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3.5")
        ), tokens);
    }

    @Test
    void testEmptyInputReturnsEmptyList() {
        List<Token> tokens = tokenizer.tokenize("");
        assertTrue(tokens.isEmpty(), "Expected no tokens for empty input");
    }

    @Test
    void testOnlyWhitespaceReturnsEmptyList() {
        List<Token> tokens = tokenizer.tokenize("   \t \n ");
        assertTrue(tokens.isEmpty(), "Expected no tokens for whitespace-only input");
    }

    @Test
    void testImplicitMultiplicationParenthesisNumber() {
        // (2)3 → (2)*3
        List<Token> tokens = tokenizer.tokenize("(2)3");
        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.NUMBER, "3")
        ), tokens);
    }

    @Test
    void testImplicitMultiplicationNumberParenthesis() {
        // 2(3) → 2*(3)
        List<Token> tokens = tokenizer.tokenize("2(3)");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testImplicitMultiplicationParenthesisParenthesis() {
        // (2)(3) → (2)*(3)
        List<Token> tokens = tokenizer.tokenize("(2)(3)");
        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testImplicitMultiplicationNumberFunction() {
        // 2√4 → 2*√4
        List<Token> tokens = tokenizer.tokenize("2√(4)");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.FUNCTION, "√"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "4"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testDecimalLeadingDot() {
        // .5 → NUMBER(".5")
        List<Token> tokens = tokenizer.tokenize(".5");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, ".5")
        ), tokens);
    }

    @Test
    void testDecimalTrailingDot() {
        // 5. → NUMBER("5.")
        List<Token> tokens = tokenizer.tokenize("5.");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5.")
        ), tokens);
    }

    @Test
    void testMultiDigitNumber() {
        List<Token> tokens = tokenizer.tokenize("12345");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "12345")
        ), tokens);
    }

    @Test
    void testUpperCaseConstants() {
        List<Token> piTokens = tokenizer.tokenize("PI");
        assertEquals(List.of(
                new Token(Token.Type.VARIABLE, "PI")
        ), piTokens);

        List<Token> eTokens = tokenizer.tokenize("E");
        assertEquals(List.of(
                new Token(Token.Type.VARIABLE, "E")
        ), eTokens);
    }

    @Test
    void testNestedParentheses() {
        List<Token> tokens = tokenizer.tokenize("((3))");
        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testTrailingOperatorIsTokenized() {
        // Parser will complain later, but tokenizer must emit the '+'
        List<Token> tokens = tokenizer.tokenize("3+");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "+")
        ), tokens);
    }

    @Test
    void testMultipleSemicolons() {
        List<Token> tokens = tokenizer.tokenize("1;;2");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.SEMICOLON, ";"),
                new Token(Token.Type.SEMICOLON, ";"),
                new Token(Token.Type.NUMBER, "2")
        ), tokens);
    }

    @Test
    void testLongExpressionWithAllFeatures() {
        String expr = "(-.5)2+pi*e--√(16);(3)4";
        List<Token> tokens = tokenizer.tokenize(expr);
        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, ".5"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.CONSTANT, "pi"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.CONSTANT, "e"),
                // "e--√(16)": '--' after operand 'e' is a binary context
                // with two minuses (even) → merged to net '+'. Eval unchanged:
                // e + √16.
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.FUNCTION, "√"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "16"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.SEMICOLON, ";"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.NUMBER, "4")
        ), tokens);
    }

    /**
     * Regression: a three-argument function is pre-expanded by the tokenizer to
     * {@code NUMBER NUMBER STRING FUNCTION}, so the trailing FUNCTION token is a
     * completed operand. A following '+' or '-' must therefore be a binary
     * operator, NOT a prefix unary-sign sentinel (which previously broke
     * arity validation for "summation(1;3;k)+product(1;3;k)").
     */
    @Test
    void testBinarySignAfterThreeArgumentFunction() {
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.STRING, "k"),
                new Token(Token.Type.FUNCTION, "summation"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.STRING, "k"),
                new Token(Token.Type.FUNCTION, "product")
        ), tokenizer.tokenize("summation(1;3;k)+product(1;3;k)"));

        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.NUMBER, "4"),
                new Token(Token.Type.STRING, "k^2"),
                new Token(Token.Type.FUNCTION, "summation"),
                new Token(Token.Type.OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.STRING, "k"),
                new Token(Token.Type.FUNCTION, "product")
        ), tokenizer.tokenize("summation(1;4;k^2)-product(1;3;k)"));
    }

    @Test
    void testValidFactorial() {
        var tokens = new Tokenizer().tokenize("5!");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.OPERATOR, "!")
        ), tokens);
    }

    @Test
    void testInvalidPrefixFactorial() {
        var tokenizer = new Tokenizer();
        assertThrows(Exception.class, () -> tokenizer.tokenize("!5"));
    }

    @Test
    void testFactorialAfterParenthesis() {
        var tokens = new Tokenizer().tokenize("(3+2)!");
        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "!")
        ), tokens);
    }

    @Test
    void testUnicodeMinusShouldFail() {
        assertThrows(SyntaxErrorException.class, () -> tokenizer.tokenize("2−3"));
    }

    @Test
    void testChainedFunctions() {
        List<Token> tokens = tokenizer.tokenize("√(√(16))");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "√"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.FUNCTION, "√"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "16"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testImplicitMultiplicationPiFunction() {
        List<Token> tokens = tokenizer.tokenize("pi√(4)");
        assertEquals(List.of(
                new Token(Token.Type.CONSTANT, "pi"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.FUNCTION, "√"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "4"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testDeeplyNestedImplicitMultiplication() {
        List<Token> tokens = tokenizer.tokenize("2(3)(4)√(9)");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "4"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.FUNCTION, "√"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "9"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testEmptyParenthesesAreTokenized() {
        List<Token> tokens = tokenizer.tokenize("()");
        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testInvalidConsecutiveOperators() {
        List<Token> tokens = tokenizer.tokenize("3+*/2");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.OPERATOR, "/"),
                new Token(Token.Type.NUMBER, "2")
        ), tokens);
    }

    @Test
    void testNumberWithTrailingDotFollowedByOperator() {
        List<Token> tokens = tokenizer.tokenize("5.+2");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5."),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "2")
        ), tokens);
    }

    @Test
    void testVeryLongNumber() {
        String longNumber = "123456789012345678901234567890.123456789";
        List<Token> tokens = tokenizer.tokenize(longNumber);
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, longNumber)
        ), tokens);
    }

    @Test
    void testUnclosedParenthesis() {
        List<Token> tokens = tokenizer.tokenize("(2+3");
        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "3")
        ), tokens);
    }

    @Test
    void testConsecutiveOperatorsAreTokenized() {
        List<Token> tokens = tokenizer.tokenize("3+*/2");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.OPERATOR, "/"),
                new Token(Token.Type.NUMBER, "2")
        ), tokens);
    }

    @Test
    void testFunctionSin() {
        List<Token> tokens = tokenizer.tokenize("sin(30)");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "sin"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "30"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testFunctionAcos() {
        List<Token> tokens = tokenizer.tokenize("acos(0.5)");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "acos"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "0.5"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testFunctionAcot() {
        List<Token> tokens = tokenizer.tokenize("acot(1)");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "acot"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testFunctionCot() {
        List<Token> tokens = tokenizer.tokenize("cot(pi/4)");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "cot"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.CONSTANT, "pi"),
                new Token(Token.Type.OPERATOR, "/"),
                new Token(Token.Type.NUMBER, "4"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testFunctionSinh() {
        List<Token> tokens = tokenizer.tokenize("sinh(2)");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "sinh"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testFunctionAcosh() {
        List<Token> tokens = tokenizer.tokenize("acosh(1.5)");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "acosh"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "1.5"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testNestedFunctions() {
        List<Token> tokens = tokenizer.tokenize("sin(acot(1))");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "sin"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.FUNCTION, "acot"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testAbsFunctionAndPipes() {
        // abs(x) syntax
        List<Token> tokens1 = tokenizer.tokenize("abs(-5)");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "abs"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens1);

        // pipe syntax for absolute value
        List<Token> tokens2 = tokenizer.tokenize("|-5|");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "abs"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens2);

        // nested expression inside pipes
        List<Token> tokens3 = tokenizer.tokenize("|3-7|");
        assertEquals(List.of(
                new Token(Token.Type.FUNCTION, "abs"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "7"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens3);
    }

    @Test
    void testVariableAndVariableImplicitMultiplication() {
        List<Token> tokens = tokenizer.tokenize("xy");
        assertEquals(List.of(
                new Token(Token.Type.VARIABLE, "xy")
        ), tokens);
    }

    @Test
    void testNumberAndParenthesisImplicitMultiplication() {
        List<Token> tokens = tokenizer.tokenize("2(x+1)");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.VARIABLE, "x"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testParenthesisAndVariableImplicitMultiplication() {
        List<Token> tokens = tokenizer.tokenize("(x+1)y");
        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.VARIABLE, "x"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.VARIABLE, "y")
        ), tokens);
    }

    @Test
    void testVariableAndParenthesisImplicitMultiplication() {
        List<Token> tokens = tokenizer.tokenize("x(y+1)");
        assertEquals(List.of(
                new Token(Token.Type.VARIABLE, "x"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.VARIABLE, "y"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokens);
    }

    @Test
    void testDecimalCommaAndVariableImplicitMultiplication() {
        List<Token> tokens = tokenizer.tokenize("4.3a");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "4.3"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.VARIABLE, "a")
        ), tokens);
    }

    @Test
    void testWhitespaceStillCreatesImplicitMultiplication() {
        List<Token> tokens = tokenizer.tokenize("2 x");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.VARIABLE, "x")
        ), tokens);
    }

    @Test
    void testPowerOperator() {
        List<Token> tokens = tokenizer.tokenize("x^2");
        assertEquals(List.of(
                new Token(Token.Type.VARIABLE, "x"),
                new Token(Token.Type.OPERATOR, "^"),
                new Token(Token.Type.NUMBER, "2")
        ), tokens);
    }

    @Test
    void testMixedExpressionWithMultipleImplicitMultiplications() {
        List<Token> tokens = tokenizer.tokenize("2x-3y+4.3a");
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.VARIABLE, "x"),
                new Token(Token.Type.OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.VARIABLE, "y"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "4.3"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.VARIABLE, "a")
        ), tokens);
    }

    // ------------------------------------------------------------------
    // Sign-merge token-sequence tests (Phase 2).
    // ------------------------------------------------------------------

    @Test
    void testDoubleMinusAtStart() {
        // '--' in unary context: 2 minuses → even → no-op, no token emitted.
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5")
        ), tokenizer.tokenize("--5"));
    }

    @Test
    void testDoublePlusAtStart() {
        // '++' in unary context: no minuses → no-op.
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5")
        ), tokenizer.tokenize("++5"));
    }

    @Test
    void testPlusMinusAtStart() {
        // '+-' in unary context: 1 minus → odd → UNARY '-'.
        assertEquals(List.of(
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "5")
        ), tokenizer.tokenize("+-5"));
    }

    @Test
    void testMinusPlusAtStart() {
        // '-+' in unary context: 1 minus → odd → UNARY '-'.
        assertEquals(List.of(
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "5")
        ), tokenizer.tokenize("-+5"));
    }

    @Test
    void testBinaryPlusMinus() {
        // '+-' after operand 5 is binary context with 1 minus → OP '-'.
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3")
        ), tokenizer.tokenize("5+-3"));
    }

    @Test
    void testBinaryMinusPlus() {
        // '-+' after operand 5: 1 minus → OP '-'.
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3")
        ), tokenizer.tokenize("5-+3"));
    }

    @Test
    void testWhitespaceSeparatesSignRuns() {
        // "5 - -3": whitespace inserts a boundary between the two '-' signs
        // so the merger does NOT collapse them. The first '-' classifies as
        // binary (after operand), the second as unary (after the OPERATOR).
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.OPERATOR, "-"),
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3")
        ), tokenizer.tokenize("5 - -3"));
    }

    @Test
    void testUnaryMinusBeforeParenthesisGroup() {
        assertEquals(List.of(
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "4"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokenizer.tokenize("-(3+4)"));
    }

    @Test
    void testDoubleMinusBeforeParenthesisGroup() {
        // '--' in unary context → 2 minuses → no-op, group untouched.
        assertEquals(List.of(
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "4"),
                new Token(Token.Type.RIGHT_PAREN, ")")
        ), tokenizer.tokenize("--(3+4)"));
    }

    @Test
    void testUnaryMinusBeforePowerLiteral() {
        // '2*-3^2': '-' after '*' is unary, '3^2' is a separate subexpression.
        // The UNARY_OPERATOR binds looser than '^' (same precedence 4, but
        // right-associative), so eval is 2 * (-(3^2)) = -18.
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.OPERATOR, "^"),
                new Token(Token.Type.NUMBER, "2")
        ), tokenizer.tokenize("2*-3^2"));
    }

    @Test
    void testParenthesisedNegativeBaseInPower() {
        // '2*(-3)^2' = 2 * ((-3)^2) = 18. The unary minus is inside the
        // parens, so the '^' sees a pre-evaluated -3 on the stack.
        assertEquals(List.of(
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.OPERATOR, "*"),
                new Token(Token.Type.LEFT_PAREN, "("),
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "3"),
                new Token(Token.Type.RIGHT_PAREN, ")"),
                new Token(Token.Type.OPERATOR, "^"),
                new Token(Token.Type.NUMBER, "2")
        ), tokenizer.tokenize("2*(-3)^2"));
    }

    @Test
    void testLongUnaryRun() {
        // '-------5+3': 7 leading minuses in unary context → odd → UNARY '-';
        // then '+' is binary, '3' a number. Eval: -5 + 3 = -2.
        assertEquals(List.of(
                new Token(Token.Type.UNARY_OPERATOR, "-"),
                new Token(Token.Type.NUMBER, "5"),
                new Token(Token.Type.OPERATOR, "+"),
                new Token(Token.Type.NUMBER, "3")
        ), tokenizer.tokenize("-------5+3"));
    }

}
