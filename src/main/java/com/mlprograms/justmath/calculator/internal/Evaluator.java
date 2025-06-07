package com.mlprograms.justmath.calculator.internal;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.internal.ArithmeticOperator;
import com.mlprograms.justmath.calculator.internal.token.Token;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Evaluates a mathematical expression represented as a list of tokens in Reverse Polish Notation.
 * Supports full precision using BigDecimal.
 */
@NoArgsConstructor
public class Evaluator {

	/**
	 * Locale used for all calculations to ensure consistent number formatting and parsing.
	 */
	private final Locale CALCULATION_LOCALE = Locale.US;
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
		BigNumber b = stack.pop();
		BigNumber a = stack.pop();

		switch (op) {
			case ADD -> stack.push(a.add(b));
			case SUBTRACT -> stack.push(a.subtract(b));
			case MULTIPLY -> stack.push(a.multiply(b));
			case DIVIDE -> stack.push(a.divide(b, mathContext));
			case POWER -> stack.push(a.power(b, mathContext, CALCULATION_LOCALE));
			case PERMUTATION_S -> stack.push(a.permutation(b, mathContext, CALCULATION_LOCALE));
			case COMBINATION_S -> stack.push(a.combination(b, mathContext));
			case MODULO -> stack.push(a.modulo(b));
			default -> throw new IllegalArgumentException("Unknown operator: " + op);
		}
	}

	/**
	 * Applies a mathematical function to one or more {@link BigNumber} arguments on the stack,
	 * depending on the specified {@link ArithmeticOperator}.
	 * <p>
	 * Most functions operate on a single argument popped from the top of the stack.
	 * Some functions (e.g. {@code LOG_BASE}, {@code NTH_ROOT}, {@code ATAN2}, {@code PERMUTATION_T},
	 * {@code COMBINATION_T})
	 * require two arguments, which are popped in reverse order (i.e. second argument first, then first argument).
	 * <p>
	 * The result of the function is pushed back onto the stack.
	 *
	 * @param func
	 * 	the function to apply; must be a valid {@link ArithmeticOperator} representing a function
	 * @param stack
	 * 	the operand stack containing one or more {@link BigNumber} values
	 *
	 * @throws IllegalArgumentException
	 * 	if the function is unknown or unsupported
	 */
	private void applyFunction(@NonNull ArithmeticOperator func, Deque<BigNumber> stack) {
		BigNumber x = stack.pop(); // all functions require at least one argument (x)

		switch (func) {
			case SIN -> stack.push(x.sin(mathContext, trigonometricMode, CALCULATION_LOCALE));
			case COS -> stack.push(x.cos(mathContext, trigonometricMode, CALCULATION_LOCALE));
			case TAN -> stack.push(x.tan(mathContext, trigonometricMode, CALCULATION_LOCALE));
			case COT -> stack.push(x.cot(mathContext, CALCULATION_LOCALE));
			case SINH -> stack.push(x.sinh(mathContext, CALCULATION_LOCALE));
			case COSH -> stack.push(x.cosh(mathContext, CALCULATION_LOCALE));
			case TANH -> stack.push(x.tanh(mathContext, CALCULATION_LOCALE));
			case COTH -> stack.push(x.coth(mathContext, CALCULATION_LOCALE));
			case ASIN_S, ASIN_T -> stack.push(x.asin(mathContext, trigonometricMode, CALCULATION_LOCALE));
			case ACOS_S, ACOS_T -> stack.push(x.acos(mathContext, trigonometricMode, CALCULATION_LOCALE));
			case ATAN_S, ATAN_T -> stack.push(x.atan(mathContext, trigonometricMode, CALCULATION_LOCALE));
			case ACOT_S, ACOT_T -> stack.push(x.acot(mathContext, CALCULATION_LOCALE));
			case ASINH_S, ASINH_T -> stack.push(x.asinh(mathContext, CALCULATION_LOCALE));
			case ACOSH_S, ACOSH_T -> stack.push(x.acosh(mathContext, CALCULATION_LOCALE));
			case ATANH_S, ATANH_T -> stack.push(x.atanh(mathContext, CALCULATION_LOCALE));
			case ACOTH_S, ACOTH_T -> stack.push(x.acoth(mathContext, CALCULATION_LOCALE));
			case LOG10 -> stack.push(x.log10(mathContext, CALCULATION_LOCALE));
			case LOG2 -> stack.push(x.log2(mathContext, CALCULATION_LOCALE));
			case LN -> stack.push(x.ln(mathContext, CALCULATION_LOCALE));
			case LOG_BASE -> {
				BigNumber base = stack.pop();
				stack.push(x.logBase(base, mathContext, CALCULATION_LOCALE));
			}
			case ROOT_S, ROOT_T -> stack.push(x.squareRoot(mathContext, CALCULATION_LOCALE));
			case CUBIC_ROOT_S, CUBIC_ROOT_T -> stack.push(x.cubicRoot(mathContext, CALCULATION_LOCALE));
			case NTH_ROOT -> {
				BigNumber n = stack.pop();
				stack.push(x.nthRoot(n, mathContext, CALCULATION_LOCALE));
			}
			case FACTORIAL -> stack.push(x.factorial(mathContext, CALCULATION_LOCALE));
			case ATAN2 -> {
				BigNumber y = stack.pop();
				stack.push(x.atan2(y, mathContext, CALCULATION_LOCALE));
			}
			case PERMUTATION_T -> {
				BigNumber n = stack.pop();
				stack.push(n.permutation(x, mathContext, CALCULATION_LOCALE));
			}
			case COMBINATION_T -> {
				BigNumber n = stack.pop();
				stack.push(n.combination(x, mathContext));
			}
			case POLAR_TO_CARTESIAN -> {
				// TODO: implement this logic
				throw new RuntimeException("this logic is not implemented yet!");
			}
			case CARTESIAN_TO_POLAR -> {
				// TODO: implement this logic
				throw new RuntimeException("this logic is not implemented yet!");
			}
			case GCD -> {
				// TODO: implement this logic
				throw new RuntimeException("this logic is not implemented yet!");
			}
			case LCM -> {
				// TODO: implement this logic
				throw new RuntimeException("this logic is not implemented yet!");
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
