package com.mlprograms.justmath.calculator.internal.expressionelements;

import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.Getter;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

@Getter
public abstract class ExpressionElement {

	private final String symbol;
	private final boolean isFunction;
	private final int precedence;
	private final int numberOfOperands;

	public ExpressionElement(String symbol, boolean isFunction, int precedence, int numberOfOperands) {
		this.symbol = symbol;
		this.isFunction = isFunction;
		this.precedence = precedence;
		this.numberOfOperands = numberOfOperands;
	}

	/**
	 * Applies this expression element to the given stack using the specified math context, trigonometric mode, and
	 * locale.
	 *
	 * @param stack
	 * 	the stack to operate on
	 * @param mathContext
	 * 	the math context for calculations
	 * @param trigonometricMode
	 * 	the trigonometric mode to use
	 * @param locale
	 * 	the locale for formatting or parsing
	 *
	 * @throws UnsupportedOperationException
	 * 	if not implemented by subclass
	 */
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		throw new UnsupportedOperationException("apply(stack, mathContext, trigonometricMode, locale) not supported for: " + symbol);
	}

}
