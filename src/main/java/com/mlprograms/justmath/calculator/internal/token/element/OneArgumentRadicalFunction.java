package com.mlprograms.justmath.calculator.internal.token.element;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.token.element.operations.OneArgumentFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class OneArgumentRadicalFunction extends OneArgumentFunction {

	private final OneArgumentFunctionOperation operation;

	public OneArgumentRadicalFunction(String symbol, OneArgumentFunctionOperation operation) {
		super(symbol, operation);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber a = ensureBigNumber(stack.pop());
		stack.push(operation.apply(a, mathContext, locale));
	}

}
