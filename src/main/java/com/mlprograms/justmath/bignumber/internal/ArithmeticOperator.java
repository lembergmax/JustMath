package com.mlprograms.justmath.bignumber.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum ArithmeticOperator {

	LEFT_PARENTHESIS("("),
	RIGHT_PARENTHESIS(")"),

	ADD("+"),
	SUBTRACT("-"),
	MULTIPLY("*"),
	DIVIDE("/"),
	POWER("^"),
	ROOT("√"),
	CUBIC_ROOT("³√"),
	FACTORIAL("!"),
	SIN("sin"),
	COS("cos"),
	TAN("tan"),
	SINH("sinh"),
	COSH("cosh"),
	TANH("tanh"),
	ASIN("sin⁻¹"),
	ACOS("cos⁻¹"),
	ATAN("tan⁻¹"),
	ASINH("sinh⁻¹"),
	ACOSH("cosh⁻¹"),
	ATANH("tanh⁻¹"),
	LOG10("log"),
	LN("ln"),
	LOG_BASE("log");

	private final String operator;

	public static Optional<ArithmeticOperator> findByOperator(String operator) {
		for (ArithmeticOperator op : values()) {
			if (op.getOperator().equals(operator)) {
				return Optional.of(op);
			}
		}
		return Optional.empty();
	}


}
