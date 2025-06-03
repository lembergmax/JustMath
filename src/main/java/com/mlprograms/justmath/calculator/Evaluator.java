package com.mlprograms.justmath.calculator;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
	private void applyOperator(String op, Deque<BigDecimal> stack) {
		switch (op) {
			case "+" -> stack.push(stack.pop().add(stack.pop()));
			case "-" -> {
				BigDecimal b = stack.pop();
				BigDecimal a = stack.pop();
				stack.push(a.subtract(b));
			}
			case "*" -> stack.push(stack.pop().multiply(stack.pop()));
			case "/" -> {
				BigDecimal b = stack.pop();
				BigDecimal a = stack.pop();
				if (b.compareTo(BigDecimal.ZERO) == 0) {
					throw new ArithmeticException("Division by zero");
				}
				stack.push(a.divide(b, Values.MATH_CONTEXT));
			}
			case "^" -> {
				BigDecimal exponent = stack.pop();
				BigDecimal base = stack.pop();
				stack.push(MathFunctions.pow(base, exponent));
			}
			case "!" -> {
				BigDecimal value = stack.pop();
				stack.push(MathFunctions.factorial(value));
			}
			case "√" -> {
				BigDecimal value = stack.pop();
				stack.push(MathFunctions.sqrt(value));
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
	private void applyFunction(String func, Deque<BigDecimal> stack) {
		BigDecimal arg = stack.pop();
		switch (func) {
			case "sin" -> stack.push(MathFunctions.sin(arg));
			case "cos" -> stack.push(MathFunctions.cos(arg));
			case "tan" -> stack.push(MathFunctions.tan(arg));
			case "asin" -> stack.push(MathFunctions.asin(arg));
			case "acos" -> stack.push(MathFunctions.acos(arg));
			case "atan" -> stack.push(MathFunctions.atan(arg));
			case "sinh" -> stack.push(MathFunctions.sinh(arg));
			case "cosh" -> stack.push(MathFunctions.cosh(arg));
			case "tanh" -> stack.push(MathFunctions.tanh(arg));
			case "asinh" -> stack.push(MathFunctions.asinh(arg));
			case "acosh" -> stack.push(MathFunctions.acosh(arg));
			case "atanh" -> stack.push(MathFunctions.atanh(arg));
			case "log" -> stack.push(MathFunctions.log10(arg));
			case "ln" -> stack.push(MathFunctions.ln(arg));
			case "sqrt" -> stack.push(MathFunctions.sqrt(arg));
			case "cbrt" -> stack.push(MathFunctions.cbrt(arg));
			default -> throw new IllegalArgumentException("Unknown function: " + func);
		}
	}

	/**
	 * Evaluates a postfix token list.
	 *
	 * @param rpnTokens
	 * 	the list of tokens in postfix order
	 *
	 * @return final result as BigDecimal
	 */
	public BigDecimal evaluate(List<Token> rpnTokens) {
		Deque<BigDecimal> stack = new ArrayDeque<>();

		for (Token token : rpnTokens) {
			switch (token.type()) {
				case NUMBER -> stack.push(new BigDecimal(token.value()));

				case OPERATOR -> applyOperator(token.value(), stack);

				case FUNCTION -> applyFunction(token.value(), stack);

				default -> throw new IllegalArgumentException("Unexpected token: " + token);
			}
		}

		if (stack.size() != 1) {
			throw new IllegalStateException("Invalid expression: stack size != 1");
		}

		return stack.pop();
	}

}
