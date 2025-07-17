package com.mlprograms.justmath.calculator.internal.token;

import com.mlprograms.justmath.calculator.internal.expressionelements.ExpressionElements;
import lombok.Getter;

import java.util.List;

/**
 * Represents a summation token in a mathematical expression.
 * A summation token contains a start value, an end value, and a list of tokens representing the expression to be
 * evaluated.
 */
@Getter
public class SummationToken extends Token {

	private final String start;
	private final String end;
	private final List<Token> expressionTokens;

	/**
	 * Creates a new summation token with the specified start value, end value, and expression tokens.
	 *
	 * @param functionSymbol
	 * 	the function symbol (e.g., "sum" or "∑")
	 * @param start
	 * 	the start value of the summation
	 * @param end
	 * 	the end value of the summation
	 * @param expressionTokens
	 * 	the list of tokens representing the expression to be evaluated
	 */
	public SummationToken(String functionSymbol, String start, String end, List<Token> expressionTokens) {
		super(Type.SUMMATION, functionSymbol);
		this.start = start;
		this.end = end;
		this.expressionTokens = expressionTokens;
	}

	/**
	 * Returns a string representation of this summation token.
	 *
	 * @return the string representation
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getValue())
			.append(ExpressionElements.PAR_LEFT)
			.append(start)
			.append(ExpressionElements.SEP_SEMICOLON)
			.append(end)
			.append(ExpressionElements.SEP_SEMICOLON);

		for (Token token : expressionTokens) {
			sb.append(token.toString());
		}

		sb.append(ExpressionElements.PAR_RIGHT);
		return sb.toString();
	}

}