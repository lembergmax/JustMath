package com.mlprograms.justmath.calculator.internal.expressionelements;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.expressionelements.operations.OneArgumentTrigonometricFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class OneArgumentTrigonometricFunction extends Function {

	private final OneArgumentTrigonometricFunctionOperation operation;

	public OneArgumentTrigonometricFunction(String symbol, int precedence, OneArgumentTrigonometricFunctionOperation operation) {
		super(symbol, precedence, 1);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber a = ensureBigNumber(stack.pop());
		stack.push(operation.apply(a, mathContext, trigonometricMode, locale));
	}

}
