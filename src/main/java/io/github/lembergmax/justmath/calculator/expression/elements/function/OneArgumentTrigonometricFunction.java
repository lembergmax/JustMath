/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package io.github.lembergmax.justmath.calculator.expression.elements.function;

import static io.github.lembergmax.justmath.bignumber.math.utils.MathUtils.ensureScalar;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.calculator.expression.operations.function.OneArgumentTrigonometricFunctionOperation;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;

public class OneArgumentTrigonometricFunction extends Function {

	private final OneArgumentTrigonometricFunctionOperation operation;

	public OneArgumentTrigonometricFunction(String symbol, int precedence, OneArgumentTrigonometricFunctionOperation operation) {
		super(symbol, precedence);
		this.operation = operation;
	}

	@Override
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber a = ensureScalar(stack.pop());
		stack.push(operation.apply(a, mathContext, trigonometricMode, locale));
	}

}
