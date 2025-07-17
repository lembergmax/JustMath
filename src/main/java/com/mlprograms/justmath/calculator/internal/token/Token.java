package com.mlprograms.justmath.calculator.internal.token;

import com.mlprograms.justmath.calculator.internal.expressionelements.ExpressionElement;
import com.mlprograms.justmath.calculator.internal.expressionelements.ExpressionElements;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

/**
 * Represents a lexical token extracted from a mathematical expression.
 * Tokens can be numbers, operators, functions, parentheses, or special symbols.
 */
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Token {

	private Type type;
	private String value;

	/**
	 * Returns the matching ArithmeticOperator if available.
	 *
	 * @return Optional of ArithmeticOperator
	 */
	public Optional<ExpressionElement> asArithmeticOperator() {
		return ExpressionElements.findBySymbol(value);
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
		SEMICOLON,
		STRING,
		CONSTANT,
		VARIABLE,
		SUMMATION
	}
}
