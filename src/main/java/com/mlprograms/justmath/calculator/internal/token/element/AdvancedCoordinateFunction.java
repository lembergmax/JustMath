package com.mlprograms.justmath.calculator.internal.token.element;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.token.element.operations.AdvancedCoordinateFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class AdvancedCoordinateFunction extends Function {

	private final AdvancedCoordinateFunctionOperation operation;

	public AdvancedCoordinateFunction(String symbol, AdvancedCoordinateFunctionOperation operation) {
		super(symbol, 6, 2);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber b = ensureBigNumber(stack.pop());
		BigNumber a = ensureBigNumber(stack.pop());
		stack.push(operation.apply(a, b, mathContext, trigonometricMode, locale));
	}

}
