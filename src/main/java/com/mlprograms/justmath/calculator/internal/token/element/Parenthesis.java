package com.mlprograms.justmath.calculator.internal.token.element;

public class Parenthesis extends ExpressionElement {

	public Parenthesis(Parenthesis.Type type) {
		super(type == Type.LEFT ? "(" : ")", false, 0, 0);
	}

	public enum Type {
		LEFT, RIGHT
	}

}
