package com.mlprograms.justmath.calculator.internal.expressionelements;

import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.expressionelements.operations.ZeroArgumentFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

public class ZeroArgumentFunction extends Function {

	private final ZeroArgumentFunctionOperation operation;

	public ZeroArgumentFunction(String symbol, ZeroArgumentFunctionOperation operation) {
		super(symbol, 1, 0);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		stack.push(operation.apply(mathContext, locale));
	}

}
