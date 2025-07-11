package com.mlprograms.justmath.calculator.internal.token.element;

public class Separator extends ExpressionElement {

	public Separator(String type) {
		super(type, false, 0, 0);
	}

	public static class Type {

		public static final String SEMICOLON = ";";

	}

}
