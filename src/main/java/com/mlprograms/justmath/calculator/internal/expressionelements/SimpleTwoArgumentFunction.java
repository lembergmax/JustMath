/*
 * Copyright (c) 2025 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.calculator.internal.expressionelements;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.expressionelements.operations.SimpleTwoArgumentFunctionOperation;
import com.mlprograms.justmath.calculator.internal.expressionelements.operations.TwoArgumentFunctionOperation;

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