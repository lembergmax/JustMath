package com.mlprograms.justmath.calculator.token;

/**
 * Represents a lexical token extracted from a mathematical expression.
 * Tokens can be numbers, operators, functions, parentheses, or special symbols.
 *
 * @param type
 * 	-- GETTER --
 * 	Returns the type of this token.
 * @param value
 * 	-- GETTER --
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


