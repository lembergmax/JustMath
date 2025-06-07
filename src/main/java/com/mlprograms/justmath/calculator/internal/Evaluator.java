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
	private void applyOperand(ArithmeticOperator op, Deque<BigNumber> stack) {
		BigNumber b = stack.pop();
		BigNumber a = stack.pop();

		switch (op) {
			case ADD_O -> stack.push(a.add(b));
			case SUBTRACT_O -> stack.push(a.subtract(b));
			case MULTIPLY_O -> stack.push(a.multiply(b));
			case DIVIDE_O -> stack.push(a.divide(b, mathContext));
			case POWER_O -> stack.push(a.power(b, mathContext, CALCULATION_LOCALE));
			case PERMUTATION_O -> stack.push(a.permutation(b, mathContext, CALCULATION_LOCALE));
			case COMBINATION_O -> stack.push(a.combination(b, mathContext));
			case MODULO_O -> stack.push(a.modulo(b));
			default -> throw new IllegalArgumentException("Unknown operator: " + op);
		}
	}

	/**
	 * Executes a mathematical operation represented by {@link ArithmeticOperator} on one or two {@link BigNumber}
	 * operands
	 * taken from the provided stack.
	 * <p>
	 * For single-operand operations, one argument is popped from the stack.
	 * For two-operand operations, two arguments are popped in reverse order (second argument first, then first
	 * argument).
	 * The result is pushed back onto the stack.
	 * <p>
	 * Supports trigonometric, hyperbolic, logarithmic, root, factorial, combinatorial and arithmetic operations.
	 *
	 * @param operator
	 * 	the {@link ArithmeticOperator} representing the mathematical function to apply; must be supported
	 * @param operandStack
	 * 	a {@link Deque} of {@link BigNumber} instances serving as the operand stack; must contain sufficient operands
	 *
	 * @throws IllegalArgumentException
	 * 	if the operator is unknown or unsupported
	 * @throws UnsupportedOperationException
	 * 	if the operator is recognized but its implementation is not yet available
	 */
	private void applyFunction(@NonNull ArithmeticOperator operator, Deque<BigNumber> operandStack) {
		if (requiresTwoOperands(operator)) {
			BigNumber second = operandStack.pop();
			BigNumber first = operandStack.pop();
			BigNumber result = applyTwoOperandFunction(operator, first, second);
			operandStack.push(result);
		} else {
			BigNumber operand = operandStack.pop();
			BigNumber result = applySingleOperandFunction(operator, operand);
			operandStack.push(result);
		}
	}

	/**
	 * Determines whether the given {@link ArithmeticOperator} requires two operands.
	 *
	 * @param operator
	 * 	the arithmetic operator to check
	 *
	 * @return {@code true} if the operator requires two operands; {@code false} otherwise
	 */
	private boolean requiresTwoOperands(ArithmeticOperator operator) {
		return operator.getRequiredOperandsCount() == 2;
	}


	/**
	 * Applies a two-operand arithmetic function specified by {@link ArithmeticOperator}
	 * to the provided operands.
	 *
	 * @param operator
	 * 	the two-operand arithmetic operator to apply
	 * @param first
	 * 	the first operand (typically the base or left operand)
	 * @param second
	 * 	the second operand (typically the exponent or right operand)
	 *
	 * @return the result of the operation as a {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if the operator is unsupported for two operands
	 */
	private BigNumber applyTwoOperandFunction(ArithmeticOperator operator, BigNumber first, BigNumber second) {
		return switch (operator) {
			case LOG_BASE_F2 -> first.logBase(second, mathContext, CALCULATION_LOCALE);
			case NTH_ROOT_F2 -> first.nthRoot(second, mathContext, CALCULATION_LOCALE);
			case ATAN2_F2 -> first.atan2(second, mathContext, CALCULATION_LOCALE);
			case PERMUTATION_F2 -> first.permutation(second, mathContext, CALCULATION_LOCALE);
			case COMBINATION_F2 -> first.combination(second, mathContext);
			case GCD_F2 -> first.gcd(second);
			case LCM_F2 -> first.lcm(second, mathContext);
			case POLARTOCARTESIAN_F2 ->
				throw new UnsupportedOperationException("POLAR_TO_CARTESIAN operation is not implemented.");
			case CARTESIANTOPOLAR_F2 ->
				throw new UnsupportedOperationException("CARTESIAN_TO_POLAR operation is not implemented.");
			default -> throw new IllegalArgumentException("Unsupported two-operand function: " + operator);
		};
	}

	/**
	 * Applies a single-operand arithmetic function specified by {@link ArithmeticOperator}
	 * to the given operand.
	 *
	 * @param operator
	 * 	the single-operand arithmetic operator to apply
	 * @param operand
	 * 	the operand on which to apply the function
	 *
	 * @return the result of the operation as a {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if the operator is unsupported for a single operand
	 * @throws UnsupportedOperationException
	 * 	if the operator is recognized but not implemented yet
	 */
	private BigNumber applySingleOperandFunction(ArithmeticOperator operator, BigNumber operand) {
		return switch (operator) {
			case SIN_F -> operand.sin(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case COS_F -> operand.cos(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case TAN_F -> operand.tan(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case COT_F -> operand.cot(mathContext, CALCULATION_LOCALE);
			case SINH_F -> operand.sinh(mathContext, CALCULATION_LOCALE);
			case COSH_F -> operand.cosh(mathContext, CALCULATION_LOCALE);
			case TANH_F -> operand.tanh(mathContext, CALCULATION_LOCALE);
			case COTH_F -> operand.coth(mathContext, CALCULATION_LOCALE);
			case ASIN_AF, ASIN_F -> operand.asin(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case ACOS_AF, ACOS_F -> operand.acos(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case ATAN_AF, ATAN_F -> operand.atan(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case ACOT_AF, ACOT_F -> operand.acot(mathContext, CALCULATION_LOCALE);
			case ASINH_AF, ASINH_F -> operand.asinh(mathContext, CALCULATION_LOCALE);
			case ACOSH_AF, ACOSH_F -> operand.acosh(mathContext, CALCULATION_LOCALE);
			case ATANH_AF, ATANH_F -> operand.atanh(mathContext, CALCULATION_LOCALE);
			case ACOTH_AF, ACOTH_F -> operand.acoth(mathContext, CALCULATION_LOCALE);
			case LOG10_F -> operand.log10(mathContext, CALCULATION_LOCALE);
			case LOG2_F -> operand.log2(mathContext, CALCULATION_LOCALE);
			case LN_F -> operand.ln(mathContext, CALCULATION_LOCALE);
			case ROOT_A, ROOT_F -> operand.squareRoot(mathContext, CALCULATION_LOCALE);
			case CUBIC_ROOT_AF, CUBIC_ROOT_F -> operand.cubicRoot(mathContext, CALCULATION_LOCALE);
			case FACTORIAL_O -> operand.factorial(mathContext, CALCULATION_LOCALE);
			default -> throw new IllegalArgumentException("Unsupported single-operand function: " + operator);
		};
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
			applyOperand(arithmeticOperator, stack);
		}
	}

}
