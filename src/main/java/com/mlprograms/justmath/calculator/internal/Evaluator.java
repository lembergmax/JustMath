package com.mlprograms.justmath.calculator.internal;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;
import com.mlprograms.justmath.bignumber.internal.BigNumberWrapper;
import com.mlprograms.justmath.calculator.internal.expressionelements.ExpressionElement;
import com.mlprograms.justmath.calculator.internal.expressionelements.ExpressionElements;
import com.mlprograms.justmath.calculator.internal.token.Token;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.mlprograms.justmath.bignumber.BigNumbers.CALCULATION_LOCALE;

/**
 * Evaluates a mathematical expression represented as a list of tokens in Reverse Polish Notation.
 * Supports full precision using BigDecimal.
 */
@NoArgsConstructor
@AllArgsConstructor
public class Evaluator {


	/**
	 * Math context specifying the precision and rounding mode for calculations.
	 */
	private MathContext mathContext;

	/**
	 * The mode used for trigonometric calculations (e.g., degrees or radians).
	 */
	private TrigonometricMode trigonometricMode;

	/**
	 * Evaluates a list of tokens in Reverse Polish Notation (RPN) and returns the final result as a {@link BigNumber}.
	 * <p>
	 * This method processes the given RPN token list by using a stack-based evaluation strategy. It supports numeric
	 * values as well as arithmetic operators and functions. Operands and intermediate results can be either
	 * {@link BigNumber}
	 * or {@link BigNumberCoordinate} objects.
	 * </p>
	 * <p>
	 * If the final result is a {@link BigNumberCoordinate}, it is converted into a string representation and
	 * wrapped in a {@link BigNumber}. This preserves compatibility with downstream code expecting BigNumber results,
	 * while still supporting polar and Cartesian coordinates.
	 * </p>
	 *
	 * @param reversePolishNotationTokens
	 * 	a list of {@link Token} objects in Reverse Polish Notation
	 *
	 * @return the result of evaluating the expression as a {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if an unexpected token type is encountered
	 * @throws IllegalStateException
	 * 	if the expression does not reduce to a single result or has an unsupported result type
	 */
	public BigNumber evaluate(List<Token> reversePolishNotationTokens) {
		Deque<Object> stack = new ArrayDeque<>();

		for (Token token : reversePolishNotationTokens) {
			switch (token.getType()) {
				case NUMBER -> stack.push(new BigNumber(token.getValue()));
				case STRING -> stack.push(token.getValue());
				case OPERATOR, FUNCTION -> {
					ExpressionElement expressionElement = ExpressionElements.findBySymbol(token.getValue())
						                                      .orElseThrow(() -> new IllegalArgumentException("Unknown operator or function: " + token.getValue()));

					expressionElement.apply(stack, mathContext, trigonometricMode, CALCULATION_LOCALE);
				}

				default -> throw new IllegalArgumentException("Unexpected token: " + token);
			}
		}

		if (stack.size() != 1) {
			throw new IllegalStateException("Invalid expression: expected a single result, but found " + stack.size());
		}

		Object result = stack.pop();
		BigNumber finalResult;

		if (result instanceof BigNumber bigNumber) {
			finalResult = bigNumber;
		} else if (result instanceof BigNumberCoordinate coordinate) {
			finalResult = new BigNumberWrapper(coordinate.toString());
		} else {
			throw new IllegalStateException("Unsupported result type: " + result);
		}

		return finalResult;
	}

}
