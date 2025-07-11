package com.mlprograms.justmath.calculator.internal.token.element;

public class Operator extends ExpressionElement {

	public Operator(String symbol, int precedence, int numberOfOperands) {
		super(symbol, false, precedence, numberOfOperands);
	}

}
