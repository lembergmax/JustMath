package com.mlprograms.justmath.bignumber.calculator;

import com.mlprograms.justmath.calculator.internal.token.Token;
import com.mlprograms.justmath.calculator.internal.token.Tokenizer;
import com.mlprograms.justmath.util.Values;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
			new Token(Token.Type.NUMBER, Values.PI.toString())
		), tokens);
	}

	@Test
	void testConstantE() {
		List<Token> tokens = tokenizer.tokenize("e");

		assertEquals(List.of(
			new Token(Token.Type.NUMBER, Values.E.toString())
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
			new Token(Token.Type.NUMBER, Values.PI.toString()),
			new Token(Token.Type.RIGHT_PAREN, ")"),
			new Token(Token.Type.OPERATOR, "-"),
			new Token(Token.Type.FUNCTION, "√"),
			new Token(Token.Type.LEFT_PAREN, "("),
			new Token(Token.Type.NUMBER, "9"),
			new Token(Token.Type.RIGHT_PAREN, ")"),
			new Token(Token.Type.SEMICOLON, ";"),
			new Token(Token.Type.NUMBER, Values.E.toString())
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

}
