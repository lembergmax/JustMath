package com.mlprograms.justmath.calculator.internal.expressionelements;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.expressionelements.operations.ThreeArgumentSummationFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class ThreeArgumentSummationFunction extends Function {

	private final ThreeArgumentSummationFunctionOperation operation;

	public ThreeArgumentSummationFunction(String symbol, int precedence, ThreeArgumentSummationFunctionOperation operation) {
		super(symbol, precedence, 3);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		System.out.println("stack: " + stack);

		String c = String.valueOf(stack.pop());
		BigNumber b = ensureBigNumber(stack.pop());
		BigNumber a = ensureBigNumber(stack.pop());
		stack.push(operation.apply(a, b, c, mathContext, trigonometricMode, locale));
	}

}
