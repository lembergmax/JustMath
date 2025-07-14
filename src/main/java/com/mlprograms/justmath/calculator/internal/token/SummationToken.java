package com.mlprograms.justmath.calculator.internal.token;

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.Getter;

import java.math.MathContext;

@Getter
public class SummationToken extends Token {

	private final BigNumber start;
	private final BigNumber end;
	private final String expression;

	public SummationToken(BigNumber start, BigNumber end, String expression, MathContext mathContext) {
		super(Token.Type.SUMMATION, "∑");
		this.start = start;
		this.end = end;
		this.expression = expression;
	}

}
