package com.mlprograms.justmath.calculator.internal;

import com.mlprograms.justmath.bignumber.internal.ArithmeticOperator;
import com.mlprograms.justmath.calculator.MathFunctions;
import com.mlprograms.justmath.calculator.internal.token.Token;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Evaluates a mathematical expression represented as a list of tokens in Reverse Polish Notation.
 * Supports full precision using BigDecimal.
 */
@NoArgsConstructor
public class Evaluator {

	/**
	 * Math context specifying the precision and rounding mode for calculations.
	 */
	private MathContext mathContext;

	/**
	 * Provides mathematical functions (e.g., trigonometric, logarithmic) used in evaluation.
	 */
	private MathFunctions mathFunctions;

	/**
	 * Constructs an Evaluator with the specified math context and trigonometric mode.
	 *
	 * @param mathContext
	 * 	the MathContext specifying precision and rounding mode
	 * @param trigonometricMode
	 * 	the mode for trigonometric calculations (e.g., degrees or radians)
	 */
	public Evaluator(MathContext mathContext, TrigonometricMode trigonometricMode) {
		this.mathContext = mathContext;
		this.mathFunctions = new MathFunctions(mathContext, trigonometricMode);
	}

	/**
	 * Applies the specified operator to the operands on the stack.
	 *
	 * @param op
	 * 	the operator as a string (e.g., "+", "-", "*", "/", "^", "!", "√")
	 * @param stack
	 * 	the stack containing operands as BigDecimal values; operands are popped as needed
	 *
	 * @throws IllegalArgumentException
	 * 	if the operator is unknown
	 * @throws ArithmeticException
	 * 	if division by zero occurs
	 */
	private void applyOperator(ArithmeticOperator op, Deque<BigDecimal> stack) {
		switch (op) {
			case ADD -> stack.push(stack.pop().add(stack.pop()));
			case SUBTRACT -> {
				BigDecimal b = stack.pop();
				BigDecimal a = stack.pop();
				stack.push(a.subtract(b));
			}
			case MULTIPLY -> stack.push(stack.pop().multiply(stack.pop()));
			case DIVIDE -> {
				BigDecimal b = stack.pop();
				BigDecimal a = stack.pop();
				if (b.compareTo(BigDecimal.ZERO) == 0) {
					throw new ArithmeticException("Division by zero");
				}
				stack.push(a.divide(b, mathContext));
			}
			case POWER -> {
				BigDecimal exponent = stack.pop();
				BigDecimal base = stack.pop();
				stack.push(mathFunctions.pow(base, exponent));
			}
			case FACTORIAL -> {
				BigDecimal value = stack.pop();
				stack.push(mathFunctions.factorial(value));
			}
			default -> throw new IllegalArgumentException("Unknown operator: " + op);
		}
	}

	/**
	 * Applies a mathematical function to the top value on the stack.
	 *
	 * @param func
	 * 	the function name as a string (e.g., "sin", "cos", "tan", "log", "sqrt")
	 * @param stack
	 * 	the stack containing operands as BigDecimal values; the argument is popped from the stack
	 *
	 * @throws IllegalArgumentException
	 * 	if the function is unknown
	 */
	private void applyFunction(@NonNull ArithmeticOperator func, Deque<BigDecimal> stack) {
		BigDecimal arg = stack.pop();
		switch (func) {
			case SIN -> stack.push(mathFunctions.sin(arg));
			case COS -> stack.push(mathFunctions.cos(arg));
			case TAN -> stack.push(mathFunctions.tan(arg));
			case ASIN -> stack.push(mathFunctions.asin(arg));
			case ACOS -> stack.push(mathFunctions.acos(arg));
			case ATAN -> stack.push(mathFunctions.atan(arg));
			case SINH -> stack.push(mathFunctions.sinh(arg));
			case COSH -> stack.push(mathFunctions.cosh(arg));
			case TANH -> stack.push(mathFunctions.tanh(arg));
			case ASINH -> stack.push(mathFunctions.asinh(arg));
			case ACOSH -> stack.push(mathFunctions.acosh(arg));
			case ATANH -> stack.push(mathFunctions.atanh(arg));
			case LOG10 -> stack.push(mathFunctions.log10(arg));
			case LN -> stack.push(mathFunctions.ln(arg));
			case ROOT_S, ROOT_T -> stack.push(mathFunctions.sqrt(arg));
			case CUBIC_ROOT_S, CUBIC_ROOT_T -> stack.push(mathFunctions.cbrt(arg));
			default -> throw new IllegalArgumentException("Unknown function: " + func);
		}
	}

	/**
	 * Evaluates a postfix (Reverse Polish Notation) token list.
	 *
	 * @param rpnTokens
	 * 	the list of tokens in postfix order
	 *
	 * @return the final result as a BigDecimal
	 *
	 * @throws IllegalArgumentException
	 * 	if an unknown token or operator is encountered
	 * @throws IllegalStateException
	 * 	if the final stack size is not 1
	 */
	public BigDecimal evaluate(List<Token> rpnTokens) {
		Deque<BigDecimal> stack = new ArrayDeque<>();

		for (Token token : rpnTokens) {
			switch (token.type()) {
				case NUMBER -> stack.push(new BigDecimal(token.value()));
				case OPERATOR, FUNCTION -> applyArithmetic(token, stack);
				default -> throw new IllegalArgumentException("Unexpected token: " + token);
			}
		}

		if (stack.size() != 1) {
			throw new IllegalStateException("Invalid expression: expected a single result, but found " + stack.size());
		}

		return stack.pop();
	}

	/**
	 * Applies an arithmetic operator or function to the stack based on the given token.
	 *
	 * @param token
	 * 	the token representing an operator or function
	 * @param stack
	 * 	the stack containing operands as BigDecimal values
	 *
	 * @throws IllegalArgumentException
	 * 	if the operator or function is unknown
	 */
	private void applyArithmetic(Token token, Deque<BigDecimal> stack) {
		String symbol = token.value();
		ArithmeticOperator op = ArithmeticOperator.findByOperator(symbol)
			                        .orElseThrow(() -> new IllegalArgumentException("Unknown operator or function: " + symbol));

		if (op.isFunction()) {
			applyFunction(op, stack);
		} else {
			applyOperator(op, stack);
		}
	}

}
