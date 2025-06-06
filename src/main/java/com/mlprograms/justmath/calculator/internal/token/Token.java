package com.mlprograms.justmath.calculator.internal.token;

import com.mlprograms.justmath.bignumber.internal.ArithmeticOperator;

import java.util.Optional;

/**
 * Represents a lexical token extracted from a mathematical expression.
 * Tokens can be numbers, operators, functions, parentheses, or special symbols.
 *
 * @param type
 * 	Returns the type of this token.
 * @param value
 * 	Returns the string value of this token.
 */
public record Token(Token.Type type, String value) {

	/**
	 * Returns a string representation of this token.
	 *
	 * @return token as a string
	 */
	@Override
	public String toString() {
		return type + "(" + value + ")";
	}

	/**
	 * Returns the matching ArithmeticOperator if available.
	 *
	 * @return Optional of ArithmeticOperator
	 */
	public Optional<ArithmeticOperator> asArithmeticOperator() {
		return ArithmeticOperator.findByOperator(value);
	}

	/**
	 * Enumeration of token types.
	 */
	public enum Type {
		NUMBER,
		OPERATOR,
		FUNCTION,
		LEFT_PAREN,
		RIGHT_PAREN,
		SEMICOLON
	}

}
