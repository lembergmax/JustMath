package com.mlprograms.justmath.bignumber.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum ArithmeticOperator {

	LEFT_PARENTHESIS("(", false, 0),
	RIGHT_PARENTHESIS(")", false, 0),
	ADD("+", false, 2),
	SUBTRACT("-", false, 2),
	MULTIPLY("*", false, 3),
	DIVIDE("/", false, 3),
	POWER("^", false, 4),
	ROOT_S("√", true, 4),
	ROOT_T("sqrt", true, 4),
	CUBIC_ROOT_S("³√", true, 4),
	CUBIC_ROOT_T("cbrt", true, 4),
	FACTORIAL("!", true, 5),
	SIN("sin", true, 6),
	COS("cos", true, 6),
	TAN("tan", true, 6),
	SINH("sinh", true, 6),
	COSH("cosh", true, 6),
	TANH("tanh", true, 6),
	ASIN("sin⁻¹", true, 6),
	ACOS("cos⁻¹", true, 6),
	ATAN("tan⁻¹", true, 6),
	ASINH("sinh⁻¹", true, 6),
	ACOSH("cosh⁻¹", true, 6),
	ATANH("tanh⁻¹", true, 6),
	LOG10("log", true, 6),
	LN("ln", true, 6),
	LOG_BASE("log", true, 6);

	private final String operator;
	private final boolean isFunction;
	private final int precedence;

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

}
