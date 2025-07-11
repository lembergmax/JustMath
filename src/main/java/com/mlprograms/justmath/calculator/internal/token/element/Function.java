package com.mlprograms.justmath.calculator.internal.token.element;

public class Function extends ExpressionElement {

	public Function(String symbol, int precedence, int numberOfOperands) {
		super(symbol, false, precedence, numberOfOperands);
	}

}
