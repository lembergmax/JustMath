package com.mlprograms.justmath.calculator.internal.token.expressionelements;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.token.expressionelements.operations.SimpleTwoArgumentFunctionOperation;
import com.mlprograms.justmath.calculator.internal.token.expressionelements.operations.TwoArgumentFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class SimpleTwoArgumentFunction extends TwoArgumentFunction {

	private final SimpleTwoArgumentFunctionOperation operation;

	public SimpleTwoArgumentFunction(String symbol, int precedence, SimpleTwoArgumentFunctionOperation operation) {
		super(symbol, precedence, wrap(operation));
		this.operation = operation;
	}

	/**
	 * Wraps a {@link SimpleTwoArgumentFunctionOperation} into a {@link TwoArgumentFunctionOperation}.
	 * The resulting operation ignores the context parameter and delegates to the simple operation.
	 *
	 * @param operation
	 * 	the simple two-argument function operation to wrap
	 *
	 * @return a {@link TwoArgumentFunctionOperation} that calls the given simple operation
	 */
	private static TwoArgumentFunctionOperation wrap(SimpleTwoArgumentFunctionOperation operation) {
		return (a, b, context, locale) -> operation.apply(a, b, locale);
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber b = ensureBigNumber(stack.pop());
		BigNumber a = ensureBigNumber(stack.pop());
		stack.push(operation.apply(a, b, locale));
	}
}