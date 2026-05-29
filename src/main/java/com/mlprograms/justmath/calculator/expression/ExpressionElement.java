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

package com.mlprograms.justmath.calculator.expression;

import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.calculator.exceptions.ProcessingErrorException;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.Getter;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;

@Getter
public abstract class ExpressionElement {

	private final String symbol;
	private final boolean isFunction;
	private final int precedence;

	public ExpressionElement(String symbol, boolean isFunction, int precedence) {
		this.symbol = symbol;
		this.isFunction = isFunction;
		this.precedence = precedence;
	}

	/**
	 * Applies this expression element to the given stack using the specified math context,
	 * trigonometric mode, and locale.
	 *
	 * <p>The base implementation throws a typed {@link ProcessingErrorException} with code
	 * {@link CalculatorErrorCode#PROCESSING_INTERNAL} so that any subclass which is reachable
	 * by the {@code Evaluator} but does not override this method surfaces as a structured,
	 * localizable engine error rather than an untyped {@code UnsupportedOperationException}.
	 * Structural-only subclasses such as {@code Parenthesis} and {@code Separator} are never
	 * dispatched through this method by the {@code Evaluator}; they may safely inherit the
	 * default behaviour.</p>
	 *
	 * @param stack             the stack to operate on
	 * @param mathContext       the math context for calculations
	 * @param trigonometricMode the trigonometric mode to use
	 * @param locale            the locale for formatting or parsing
	 *
	 * @throws ProcessingErrorException with code {@link CalculatorErrorCode#PROCESSING_INTERNAL}
	 *                                  when the concrete subclass does not provide an
	 *                                  implementation
	 */
	public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		throw new ProcessingErrorException(
				CalculatorErrorCode.PROCESSING_INTERNAL,
				Map.of("symbol", symbol),
				"apply(stack, mathContext, trigonometricMode, locale) not implemented for element: " + symbol);
	}

}
