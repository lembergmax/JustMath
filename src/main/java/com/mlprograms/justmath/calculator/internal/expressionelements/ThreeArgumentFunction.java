package com.mlprograms.justmath.calculator.internal.expressionelements;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.expressionelements.operations.ThreeArgumentFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class ThreeArgumentFunction extends Function {

	private final ThreeArgumentFunctionOperation operation;

	public ThreeArgumentFunction(String symbol, int precedence, ThreeArgumentFunctionOperation operation) {
		super(symbol, precedence, 3);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		String c = String.valueOf(stack.pop());
		BigNumber b = ensureBigNumber(stack.pop());
		BigNumber a = ensureBigNumber(stack.pop());
		stack.push(operation.apply(a, b, c, mathContext, trigonometricMode, locale));
	}

}
