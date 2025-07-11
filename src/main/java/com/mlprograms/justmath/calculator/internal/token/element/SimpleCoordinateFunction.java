package com.mlprograms.justmath.calculator.internal.token.element;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.token.element.operations.CoordinateFunctionOperation;
import com.mlprograms.justmath.calculator.internal.token.element.operations.SimpleCoordinateFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class SimpleCoordinateFunction extends CoordinateFunction {

	private final SimpleCoordinateFunctionOperation operation;

	public SimpleCoordinateFunction(String symbol, int precedence, SimpleCoordinateFunctionOperation operation) {
		super(symbol, precedence, wrap(operation));
		this.operation = operation;
	}

	/**
	 * Wraps a SimpleCoordinateFunctionOperation into a CoordinateFunctionOperation.
	 * Ignores the trigonometricMode parameter, delegating to the underlying operation.
	 *
	 * @param operation
	 * 	the SimpleCoordinateFunctionOperation to wrap
	 *
	 * @return a CoordinateFunctionOperation that delegates to the given operation
	 */
	private static CoordinateFunctionOperation wrap(SimpleCoordinateFunctionOperation operation) {
		return (a, b, mathContext, trigonometricMode, locale) -> operation.apply(a, b, mathContext, locale);
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber b = ensureBigNumber(stack.pop());
		BigNumber a = ensureBigNumber(stack.pop());
		stack.push(operation.apply(a, b, mathContext, locale));
	}

}
