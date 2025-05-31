package com.mlprograms.justmath.calculator;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a lexical token extracted from a mathematical expression.
 * Tokens can be numbers, operators, functions, parentheses, or special symbols.
 */
@AllArgsConstructor
@Getter
public class Token {

	/**
	 * -- GETTER --
	 * Returns the type of this token.
	 */
	private final Type type;
	/**
	 * -- GETTER --
	 * Returns the string value of this token.
	 */
	private final String value;

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
	 * Enumeration of token types.
	 */
	public enum Type {
		NUMBER,
		OPERATOR,
		FUNCTION,
		LEFT_PAREN,
		RIGHT_PAREN
	}

}
