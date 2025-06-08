package com.mlprograms.justmath.bignumber.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum ArithmeticOperator {

	// name description: NAME_<CONFIGURATION>_<OPERAND_COUNT(>=2)>
	// CONFIGURATION: A = Advanced (not only basic letters, numbers or symbols), F = Function, O = Operator (can consist of multiple letters)

	LEFT_PARENTHESIS("(", false, 0, 0),
	RIGHT_PARENTHESIS(")", false, 0, 0),
	SEMICOLON(";", false, 0, 0),

	ADD_O("+", false, 2, 0),
	SUBTRACT_O("-", false, 2, 0),
	MULTIPLY_O("*", false, 3, 0),
	DIVIDE_O("/", false, 3, 0),
	MODULO_O("%", false, 3, 0),
	POWER_O("^", false, 4, 0),
	FACTORIAL_F("!", true, 5, 1),

	ROOT_A("√", true, 4, 1),
	ROOT_F("sqrt", true, 4, 1),

	NTH_ROOT_F2("rootn", true, 4, 2),
	CUBIC_ROOT_AF("³√", true, 4, 1),
	CUBIC_ROOT_F("cbrt", true, 4, 1),

	SIN_F("sin", true, 6, 1),
	COS_F("cos", true, 6, 1),
	TAN_F("tan", true, 6, 1),
	COT_F("cot", true, 6, 1),

	SINH_F("sinh", true, 6, 1),
	COSH_F("cosh", true, 6, 1),
	TANH_F("tanh", true, 6, 1),
	COTH_F("coth", true, 6, 1),

	ASIN_AF("sin⁻¹", true, 6, 1),
	ASIN_F("asin", true, 6, 1),
	ACOS_AF("cos⁻¹", true, 6, 1),
	ACOS_F("acos", true, 6, 1),
	ATAN_AF("tan⁻¹", true, 6, 1),
	ATAN_F("atan", true, 6, 1),
	ACOT_AF("cot⁻¹", true, 6, 1),
	ACOT_F("acot", true, 6, 1),

	ASINH_AF("sinh⁻¹", true, 6, 1),
	ASINH_F("asinh", true, 6, 1),
	ACOSH_AF("cosh⁻¹", true, 6, 1),
	ACOSH_F("acosh", true, 6, 1),
	ATANH_AF("tanh⁻¹", true, 6, 1),
	ATANH_F("atanh", true, 6, 1),
	ACOTH_AF("coth⁻¹", true, 6, 1),
	ACOTH_F("acoth", true, 6, 1),

	LOG2_F("log2", true, 6, 1),
	LOG10_F("log", true, 6, 1),
	LN_F("ln", true, 6, 1),
	LOG_BASE_F2("logbase", true, 6, 2),

	ATAN2_F2("atan2", true, 6, 2),

	PERMUTATION_O("nPr", false, 6, 1),
	PERMUTATION_F2("perm", true, 6, 2),
	COMBINATION_O("nCr", false, 6, 1),
	COMBINATION_F2("comb", true, 6, 2),

	POLARTOCARTESIAN_F2("Pol", true, 6, 2),
	CARTESIANTOPOLAR_F2("Rec", true, 6, 2),

	GCD_F2("GCD", true, 6, 2),
	LCM_F2("LCM", true, 6, 2);

	private final String operator;
	private final boolean isFunction;
	private final int precedence;
	private final int requiredOperandsCount;

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
