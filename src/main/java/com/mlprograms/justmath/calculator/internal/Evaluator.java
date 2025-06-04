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
import java.util.Optional;

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
				stack.push(a.divide(b, mathContext));
			}
			case "^" -> {
				BigDecimal exponent = stack.pop();
				BigDecimal base = stack.pop();
				stack.push(mathFunctions.pow(base, exponent));
			}
			case "!" -> {
				BigDecimal value = stack.pop();
				stack.push(mathFunctions.factorial(value));
			}
			case "√" -> {
				BigDecimal value = stack.pop();
				stack.push(mathFunctions.sqrt(value));
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
			case ROOT -> stack.push(mathFunctions.sqrt(arg));
			case CUBIC_ROOT -> stack.push(mathFunctions.cbrt(arg));
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
				case FUNCTION -> {
					Optional<ArithmeticOperator> arithmeticOperator = ArithmeticOperator.findByOperator(token.value());
					if (arithmeticOperator.isEmpty()) {
						throw new IllegalArgumentException("Unknown function: " + token.value());
					}
					applyFunction(arithmeticOperator.get(), stack);
				}
				default -> throw new IllegalArgumentException("Unexpected token: " + token);
			}
		}

		if (stack.size() != 1) {
			throw new IllegalStateException("Invalid expression: stack size != 1");
		}

		return stack.pop();
	}

}
