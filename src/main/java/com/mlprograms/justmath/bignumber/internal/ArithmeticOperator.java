package com.mlprograms.justmath.bignumber.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum ArithmeticOperator {

	LEFT_PARENTHESIS("(", false, 0, false),
	RIGHT_PARENTHESIS(")", false, 0, false),

	ADD("+", false, 2, false),
	SUBTRACT("-", false, 2, false),
	MULTIPLY("*", false, 3, false),
	DIVIDE("/", false, 3, false),
	POWER("^", false, 4, true),

	ROOT_S("√", true, 4, false),
	ROOT_T("sqrt", true, 4, false),
	CUBIC_ROOT_S("³√", true, 4, false),
	CUBIC_ROOT_T("cbrt", true, 4, false),

	FACTORIAL("!", true, 5, false),

	SIN("sin", true, 6, false),
	COS("cos", true, 6, false),
	TAN("tan", true, 6, false),
	SINH("sinh", true, 6, false),
	COSH("cosh", true, 6, false),
	TANH("tanh", true, 6, false),

	ASIN("sin⁻¹", true, 6, false),
	ACOS("cos⁻¹", true, 6, false),
	ATAN("tan⁻¹", true, 6, false),
	ASINH("sinh⁻¹", true, 6, false),
	ACOSH("cosh⁻¹", true, 6, false),
	ATANH("tanh⁻¹", true, 6, false),

	LOG10("log", true, 6, false),
	LN("ln", true, 6, false),
	LOG_BASE("log", true, 6, false);

	private final String operator;
	private final boolean isFunction;
	private final int precedence;
	private final boolean rightAssociative;

	/**
	 * Finds an {@link ArithmeticOperator} by its operator string.
	 *
	 * @param operator
	 * 	the operator string to search for
	 *
	 * @return an {@link Optional} containing the matching {@link ArithmeticOperator}, or empty if not found
	 */
	public static Optional<ArithmeticOperator> findByOperator(String operator) {
		for (ArithmeticOperator op : values()) {
			if (op.getOperator().equals(operator)) {
				return Optional.of(op);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns the precedence of the operator represented by the given symbol.
	 *
	 * @param symbol
	 * 	the operator symbol to look up
	 *
	 * @return the precedence value, or 0 if the symbol is not found
	 */
	public static int getPrecedence(String symbol) {
		return findByOperator(symbol).map(ArithmeticOperator::getPrecedence).orElse(0);
	}

	/**
	 * Determines if the operator represented by the given symbol is right-associative.
	 *
	 * @param symbol
	 * 	the operator symbol to look up
	 *
	 * @return true if the operator is right-associative, false otherwise
	 */
	public static boolean isRightAssociative(String symbol) {
		return findByOperator(symbol).map(ArithmeticOperator::isRightAssociative).orElse(false);
	}

}
