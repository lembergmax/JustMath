package com.mlprograms.justmath.calculator.internal.token.element;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.token.element.operations.InverseHyperbolicTrigonometricFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class InverseHyperbolicTrigonometricFunction extends Function {

	private final InverseHyperbolicTrigonometricFunctionOperation operation;

	public InverseHyperbolicTrigonometricFunction(String symbol, InverseHyperbolicTrigonometricFunctionOperation operation) {
		super(symbol, 6, 1);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber a = ensureBigNumber(stack.pop());
		stack.push(operation.apply(a, mathContext, locale));
	}

}
