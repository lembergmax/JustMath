package com.mlprograms.justmath.bignumber.calculator;

import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.internal.token.Token;
import com.mlprograms.justmath.calculator.internal.token.Tokenizer;
import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

	private final MathContext mathContext = BigNumbers.DEFAULT_MATH_CONTEXT;
	private final Tokenizer tokenizer = new Tokenizer(mathContext);

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

		assertEquals(List.of(
			new Token(Token.Type.NUMBER, "-3")
		), tokens);
	}

	@Test
	void testSignedNumberAfterOperator() {
		List<Token> tokens = tokenizer.tokenize("5*-2");

		assertEquals(List.of(
			new Token(Token.Type.NUMBER, "5"),
			new Token(Token.Type.OPERATOR, "*"),
			new Token(Token.Type.NUMBER, "-2")
		), tokens);
	}

	@Test
	void testSignedNumberAfterLeftParenthesis() {
		List<Token> tokens = tokenizer.tokenize("(-3)");

		assertEquals(List.of(
			new Token(Token.Type.LEFT_PAREN, "("),
			new Token(Token.Type.NUMBER, "-3"),
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

		assertEquals(List.of(
			new Token(Token.Type.NUMBER, "5"),
			new Token(Token.Type.OPERATOR, "-"),
			new Token(Token.Type.NUMBER, "-3")
		), tokens);
	}

	@Test
	void testMultipleSignsOdd() {
		List<Token> tokens = tokenizer.tokenize("5---3");

		assertEquals(List.of(
			new Token(Token.Type.NUMBER, "5"),
			new Token(Token.Type.OPERATOR, "+"),
			new Token(Token.Type.NUMBER, "-3")
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
			new Token(Token.Type.NUMBER, BigNumbers.pi(mathContext).toString())
		), tokens);
	}

	@Test
	void testConstantE() {
		List<Token> tokens = tokenizer.tokenize("e");

		assertEquals(List.of(
			new Token(Token.Type.NUMBER, BigNumbers.e(mathContext).toString())
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
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
			() -> tokenizer.tokenize("2#3"));

		assertTrue(ex.getMessage().contains("Invalid character"));
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
			new Token(Token.Type.OPERATOR, "-"),
			new Token(Token.Type.NUMBER, BigNumbers.pi(mathContext).toString()),
			new Token(Token.Type.RIGHT_PAREN, ")"),
			new Token(Token.Type.OPERATOR, "-"),
			new Token(Token.Type.FUNCTION, "√"),
			new Token(Token.Type.LEFT_PAREN, "("),
			new Token(Token.Type.NUMBER, "9"),
			new Token(Token.Type.RIGHT_PAREN, ")"),
			new Token(Token.Type.SEMICOLON, ";"),
			new Token(Token.Type.NUMBER, BigNumbers.e(mathContext).toString())
		), tokens);
	}

	@Test
	void testExpressionWithLeadingPlus() {
		List<Token> tokens = tokenizer.tokenize("+2");

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
			new Token(Token.Type.NUMBER, "-3.5")
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
			new Token(Token.Type.NUMBER, BigNumbers.pi(mathContext).toString())
		), piTokens);

		List<Token> eTokens = tokenizer.tokenize("E");
		assertEquals(List.of(
			new Token(Token.Type.NUMBER, BigNumbers.e(mathContext).toString())
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
		String expr = "(-.5)2+PI*e--√(16);(3)4";
		List<Token> tokens = tokenizer.tokenize(expr);
		assertEquals(List.of(
			new Token(Token.Type.LEFT_PAREN, "("),
			new Token(Token.Type.NUMBER, "-.5"),
			new Token(Token.Type.RIGHT_PAREN, ")"),
			new Token(Token.Type.OPERATOR, "*"),
			new Token(Token.Type.NUMBER, "2"),
			new Token(Token.Type.OPERATOR, "+"),
			new Token(Token.Type.NUMBER, BigNumbers.pi(mathContext).toString()),
			new Token(Token.Type.OPERATOR, "*"),
			new Token(Token.Type.NUMBER, BigNumbers.e(mathContext).toString()),
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

	@Test
	void testValidFactorial() {
		var tokens = new Tokenizer(MathContext.DECIMAL64).tokenize("5!");
		assertEquals(List.of(
			new Token(Token.Type.NUMBER, "5"),
			new Token(Token.Type.OPERATOR, "!")
		), tokens);
	}

	@Test
	void testInvalidPrefixFactorial() {
		var tokenizer = new Tokenizer(MathContext.DECIMAL64);
		assertThrows(Exception.class, () -> tokenizer.tokenize("!5"));
	}

	@Test
	void testFactorialAfterParenthesis() {
		var tokens = new Tokenizer(MathContext.DECIMAL64).tokenize("(3+2)!");
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
		assertThrows(IllegalArgumentException.class, () -> tokenizer.tokenize("2−3"));
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
			new Token(Token.Type.NUMBER, BigNumbers.pi(mathContext).toString()),
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

}
