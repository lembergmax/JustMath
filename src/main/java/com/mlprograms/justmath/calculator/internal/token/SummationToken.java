package com.mlprograms.justmath.calculator.internal.token;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.expressionelements.ExpressionElements;
import lombok.Getter;

@Getter
public class SummationToken extends Token {

	private final BigNumber start;
	private final BigNumber end;
	private final String expression;

	public SummationToken(BigNumber start, BigNumber end, String expression) {
		super(Type.SUMMATION, ExpressionElements.FUNC_SUMM_S);
		this.start = start;
		this.end = end;
		this.expression = expression;
	}

}
