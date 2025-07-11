package com.mlprograms.justmath.calculator.internal.token.element;

import lombok.Getter;

@Getter
public class Parenthesis extends ExpressionElement {

	private final Type type;

	public Parenthesis(Parenthesis.Type type) {
		super(type == Type.LEFT ? ExpressionElements.PAR_LEFT : ExpressionElements.PAR_RIGHT, false, 0, 0);
		this.type = type;
	}

	public boolean isLeft() {
		return this.type == Type.LEFT;
	}

	public boolean isRight() {
		return this.type == Type.RIGHT;
	}

	public enum Type {
		LEFT, RIGHT
	}

}
