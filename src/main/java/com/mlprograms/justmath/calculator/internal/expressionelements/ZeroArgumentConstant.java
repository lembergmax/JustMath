package com.mlprograms.justmath.calculator.internal.expressionelements;

import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.expressionelements.operations.ZeroArgumentConstantOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

public class ZeroArgumentConstant extends Function {

	private final ZeroArgumentConstantOperation operation;

	public ZeroArgumentConstant(String symbol, ZeroArgumentConstantOperation operation) {
		super(symbol, 1, 0);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		stack.push(operation.apply(mathContext, locale));
	}

}
