package com.mlprograms.justmath.calculator.internal.token.expressionelements;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.token.expressionelements.operations.CoordinateFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class CoordinateFunction extends Function {

	private final CoordinateFunctionOperation operation;

	public CoordinateFunction(String symbol, int precedence, CoordinateFunctionOperation operation) {
		super(symbol, precedence, 2);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber b = ensureBigNumber(stack.pop());
		BigNumber a = ensureBigNumber(stack.pop());
		stack.push(operation.apply(a, b, mathContext, trigonometricMode, locale));
	}

}
