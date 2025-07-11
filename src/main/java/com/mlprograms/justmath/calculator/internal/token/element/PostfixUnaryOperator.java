package com.mlprograms.justmath.calculator.internal.token.element;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.token.element.operations.OneArgumentFunctionOperation;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.ensureBigNumber;

public class PostfixUnaryOperator extends Operator {

	private final OneArgumentFunctionOperation operation;

	public PostfixUnaryOperator(String symbol, int precedence, OneArgumentFunctionOperation operation) {
		super(symbol, precedence, 1);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber value = ensureBigNumber(stack.pop());
		stack.push(operation.apply(value, mathContext, locale));
	}
}
