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

package io.github.lembergmax.justmath.calculator.expression.elements.operator;

import static io.github.lembergmax.justmath.bignumber.math.utils.MathUtils.ensureScalar;

import java.math.MathContext;
import java.util.Deque;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.calculator.expression.operations.function.OneArgumentFunctionOperation;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import lombok.Getter;

/**
 * Single-operand operator — covers both prefix unary operators (e.g. {@code -x}, {@code +x})
 * and true postfix operators (e.g. factorial {@code !}).
 *
 * <p>The class replaces the historically misnamed {@code PostfixUnaryOperator}, which carried
 * both roles despite its name describing only one of them. The semantic position of an instance
 * (prefix vs. postfix) is now carried explicitly as a {@link Position} value and exposed via
 * {@link #getPosition()} for diagnostics, pretty-printing, and any future tooling that needs to
 * distinguish the two without re-parsing the symbol string.</p>
 *
 * <p>The runtime behaviour does not depend on the {@link Position}: every instance pops exactly
 * one value off the evaluation stack, runs the configured {@link OneArgumentFunctionOperation}
 * and pushes the result back. Disambiguation between prefix and binary uses of the same symbol
 * (for example {@code -}) happens at lookup time in {@link io.github.lembergmax.justmath.calculator.expression.ExpressionElements}
 * through a dedicated unary registry.</p>
 */
@Getter
public class UnaryOperator extends Operator {

	/**
	 * Where the operator appears relative to its operand.
	 */
	public enum Position {
		/**
		 * Operator precedes the operand, e.g. unary {@code -x}, unary {@code +x}.
		 */
		PREFIX,
		/**
		 * Operator follows the operand, e.g. factorial {@code x!}.
		 */
		POSTFIX
	}

	private final Position position;
	private final OneArgumentFunctionOperation operation;

	/**
	 * Creates a new unary operator.
	 *
	 * @param symbol     the token symbol that triggers this operator (e.g. {@code "!"} or {@code "-"})
	 * @param precedence the operator's precedence in the shunting-yard parser
	 * @param position   whether the operator sits before ({@link Position#PREFIX}) or after
	 *                   ({@link Position#POSTFIX}) its operand
	 * @param operation  the 1-argument operation invoked during evaluation
	 */
	public UnaryOperator(final String symbol, final int precedence, final Position position, final OneArgumentFunctionOperation operation) {
		super(symbol, precedence);
		this.position = position;
		this.operation = operation;
	}

	@Override
	public void apply(final Deque<Object> stack, final MathContext mathContext, final TrigonometricMode trigonometricMode, final Locale locale) {
		final BigNumber value = ensureScalar(stack.pop());
		stack.push(operation.apply(value, mathContext, locale));
	}

}
