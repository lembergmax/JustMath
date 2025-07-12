package com.mlprograms.justmath.calculator.internal.token.expressionelements;

public class Function extends ExpressionElement {

	public Function(String symbol, int precedence, int numberOfOperands) {
		super(symbol, true, precedence, numberOfOperands);
	}

}
