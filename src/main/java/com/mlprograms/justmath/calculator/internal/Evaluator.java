package com.mlprograms.justmath.calculator.internal;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.internal.ArithmeticOperator;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;
import com.mlprograms.justmath.calculator.internal.token.Token;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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
	 * The mode used for trigonometric calculations (e.g., degrees or radians).
	 */
	private TrigonometricMode trigonometricMode;

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
		this.trigonometricMode = trigonometricMode;
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
	private void applyOperator(ArithmeticOperator op, Deque<BigNumber> stack) {
		switch (op) {
			case ADD -> stack.push(stack.pop().add(stack.pop()));
			case SUBTRACT -> {
				BigNumber b = stack.pop();
				BigNumber a = stack.pop();
				stack.push(a.subtract(b));
			}
			case MULTIPLY -> stack.push(stack.pop().multiply(stack.pop()));
			case DIVIDE -> {
				BigNumber b = stack.pop();
				BigNumber a = stack.pop();
				if (b.compareTo(BigNumbers.ZERO) == 0) {
					throw new ArithmeticException("Division by zero");
				}
				stack.push(a.divide(b));
			}
			case POWER -> {
				BigNumber exponent = stack.pop();
				BigNumber base = stack.pop();
				stack.push(base.pow(exponent));
			}
			case PERMUTATION_S -> {
				BigNumber k = stack.pop();
				BigNumber n = stack.pop();
				stack.push(n.permutation(k));
			}
			case COMBINATION_S -> {
				BigNumber k = stack.pop();
				BigNumber n = stack.pop();
				stack.push(n.combination(k));
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
	private void applyFunction(@NonNull ArithmeticOperator func, Deque<BigNumber> stack) {
		BigNumber arg = stack.pop();
		switch (func) {
			case SIN -> stack.push(arg.sin());
			case COS -> stack.push(arg.cos());
			case TAN -> stack.push(arg.tan());
			case COT -> stack.push(arg.cot());
			case SINH -> stack.push(arg.sinh());
			case COSH -> stack.push(arg.cosh());
			case TANH -> stack.push(arg.tanh());
			case COTH -> stack.push(arg.coth());
			case ASIN_S, ASIN_T -> stack.push(arg.asin());
			case ACOS_S, ACOS_T -> stack.push(arg.acos());
			case ATAN_S, ATAN_T -> stack.push(arg.atan());
			case ACOT_S, ACOT_T -> stack.push(arg.acot());
			case ASINH_S, ASINH_T -> stack.push(arg.asinh());
			case ACOSH_S, ACOSH_T -> stack.push(arg.acosh());
			case ATANH_S, ATANH_T -> stack.push(arg.atanh());
			case ACOTH_S, ACOTH_T -> stack.push(arg.acoth());
			case LOG10 -> stack.push(arg.log10());
			case LN -> stack.push(arg.ln());
			case LOG_BASE -> {
				BigNumber base = stack.pop(); // logBase(x, base)
				stack.push(arg.logBase(base));
			}
			case ROOT_S, ROOT_T -> stack.push(arg.squareRoot());
			case CUBIC_ROOT_S, CUBIC_ROOT_T -> stack.push(arg.cubicRoot());
			case NTH_ROOT -> {
				BigNumber n = stack.pop();
				stack.push(arg.nthRoot(n));
			}
			case FACTORIAL -> stack.push(arg.factorial());
			case ATAN2 -> {
				BigNumber y = stack.pop();
				stack.push(arg.atan2(y));
			}
			case PERMUTATION_T -> {
				BigNumber n = stack.pop();
				stack.push(n.permutation(arg));
			}
			case COMBINATION_T -> {
				BigNumber n = stack.pop();
				stack.push(n.combination(arg));
			}
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
	public BigNumber evaluate(List<Token> rpnTokens) {
		Deque<BigNumber> stack = new ArrayDeque<>();

		for (Token token : rpnTokens) {
			switch (token.type()) {
				case NUMBER -> stack.push(new BigNumber(token.value()));
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
	private void applyArithmetic(Token token, Deque<BigNumber> stack) {
		String tokenValue = token.value();
		ArithmeticOperator arithmeticOperator = ArithmeticOperator.findByOperator(tokenValue)
			                                        .orElseThrow(() -> new IllegalArgumentException("Unknown operator or function: " + tokenValue));

		if (arithmeticOperator.isFunction()) {
			applyFunction(arithmeticOperator, stack);
		} else {
			applyOperator(arithmeticOperator, stack);
		}
	}

}
