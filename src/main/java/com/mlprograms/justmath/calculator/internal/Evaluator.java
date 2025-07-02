package com.mlprograms.justmath.calculator.internal;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;
import com.mlprograms.justmath.bignumber.internal.ArithmeticOperator;
import com.mlprograms.justmath.bignumber.internal.BigNumberWrapper;
import com.mlprograms.justmath.bignumber.math.CoordinateConversionMath;
import com.mlprograms.justmath.calculator.internal.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumberValues.CALCULATION_LOCALE;

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
	 * Applies a binary arithmetic operation to the top two elements of the stack.
	 * <p>
	 * This method assumes that the top two elements of the provided stack are either
	 * {@link BigNumber} instances or {@link BigNumberCoordinate}, and applies the specified
	 * arithmetic operation to them. The result is then pushed back onto the stack.
	 * </p>
	 *
	 * @param op
	 * 	the {@link ArithmeticOperator} to apply (e.g., addition, subtraction)
	 * @param stack
	 * 	the operand stack containing objects (expected to be BigNumbers or coordinates)
	 *
	 * @throws IllegalArgumentException
	 * 	if the operator is unknown or the operands are invalid types
	 */
	private void applyOperand(ArithmeticOperator op, Deque<Object> stack) {
		BigNumber b = ensureBigNumber(stack.pop());
		BigNumber a = ensureBigNumber(stack.pop());

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
	 * Ensures that the given object is a {@link BigNumber}.
	 * <p>
	 * If the object is a {@link BigNumber}, it is returned as-is.
	 * If the object is a {@link BigNumberCoordinate}, the x-component is used as a {@link BigNumber}.
	 * </p>
	 *
	 * @param object
	 * 	the object to convert or validate
	 *
	 * @return the extracted or verified {@link BigNumber} instance
	 *
	 * @throws IllegalArgumentException
	 * 	if the object is neither a BigNumber nor a BigNumberCoordinateRecord
	 */
	private BigNumber ensureBigNumber(Object object) {
		if (object instanceof BigNumber bn) {
			return bn;
		}
		if (object instanceof BigNumberCoordinate coordinate) {
			return coordinate.getX(); // Use X coordinate for calculations
		}

		throw new IllegalArgumentException("Expected BigNumber or coordinate, got: " + object);
	}

	/**
	 * Applies an arithmetic function (unary or binary) to operands from the stack.
	 * <p>
	 * This method checks the {@link ArithmeticOperator} to determine whether it requires
	 * one or two operands. It then pops the required number of operands from the stack,
	 * evaluates the function, and pushes the result back.
	 * </p>
	 *
	 * @param operator
	 * 	the function/operator to apply (e.g., SIN, MAX, LOG)
	 * @param operandStack
	 * 	the stack holding operand values, expected to be BigNumbers or coordinates
	 *
	 * @throws IllegalArgumentException
	 * 	if operand types are invalid or the operator is unsupported
	 */
	private void applyFunction(@NonNull ArithmeticOperator operator, Deque<Object> operandStack) {
		Object result;
		if (requiresTwoOperands(operator)) {
			BigNumber second = ensureBigNumber(operandStack.pop());
			BigNumber first = ensureBigNumber(operandStack.pop());
			result = applyTwoOperandFunction(operator, first, second);
		} else {
			BigNumber operand = ensureBigNumber(operandStack.pop());
			result = applySingleOperandFunction(operator, operand);
		}

		operandStack.push(result);
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
	 * Applies a two-operand arithmetic function represented by the specified {@link ArithmeticOperator}.
	 * <p>
	 * This method performs advanced binary operations on two {@link BigNumber} instances such as logarithms with
	 * arbitrary bases, nth roots, trigonometric functions like atan2, combinatorics (permutation, combination),
	 * number theory functions (GCD, LCM), and coordinate transformations (polar to Cartesian and vice versa).
	 * </p>
	 * <p>
	 * For coordinate transformations, the method returns a {@link BigNumberCoordinate} containing both x and y
	 * components along with the coordinate type. All mathematical computations respect the specified
	 * {@code mathContext},
	 * {@code trigonometricMode}, and {@code CALCULATION_LOCALE}.
	 * </p>
	 *
	 * @param operator
	 * 	the {@link ArithmeticOperator} representing the two-operand function to apply
	 * @param first
	 * 	the first operand (e.g., base, angle, radius, x)
	 * @param second
	 * 	the second operand (e.g., exponent, degree, y)
	 *
	 * @return the result of the operation, either as a {@link BigNumber} or a {@link BigNumberCoordinate}
	 *
	 * @throws IllegalArgumentException
	 * 	if the given operator is not supported for two operands
	 */
	private Object applyTwoOperandFunction(ArithmeticOperator operator, BigNumber first, BigNumber second) {
		return switch (operator) {
			case LOG_BASE_F2 -> first.logBase(second, mathContext, CALCULATION_LOCALE);
			case NTH_ROOT_F2 -> first.nthRoot(second, mathContext, CALCULATION_LOCALE);
			case ATAN2_F2 -> first.atan2(second, mathContext, CALCULATION_LOCALE);
			case PERMUTATION_F2 -> first.permutation(second, mathContext, CALCULATION_LOCALE);
			case COMBINATION_F2 -> first.combination(second, mathContext);
			case GCD_F2 -> first.gcd(second);
			case LCM_F2 -> first.lcm(second, mathContext);
			case RANDINT_F2 -> first.randomIntegerForRange(second);
			case POLARTOCARTESIAN_F2 -> {
				BigNumberCoordinate coordinates = CoordinateConversionMath.polarToCartesianCoordinates(first, second, mathContext, trigonometricMode, CALCULATION_LOCALE);
				yield new BigNumberCoordinate(coordinates.getX(), coordinates.getY(), CoordinateType.CARTESIAN);
			}
			case CARTESIANTOPOLAR_F2 -> {
				BigNumberCoordinate coordinates = CoordinateConversionMath.cartesianToPolarCoordinates(first, second, mathContext, CALCULATION_LOCALE);
				yield new BigNumberCoordinate(coordinates.getX(), coordinates.getY(), CoordinateType.POLAR);
			}
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
			case COT_F -> operand.cot(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case SINH_F -> operand.sinh(mathContext, CALCULATION_LOCALE);
			case COSH_F -> operand.cosh(mathContext, CALCULATION_LOCALE);
			case TANH_F -> operand.tanh(mathContext, CALCULATION_LOCALE);
			case COTH_F -> operand.coth(mathContext, CALCULATION_LOCALE);
			case ASIN_AF, ASIN_F -> operand.asin(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case ACOS_AF, ACOS_F -> operand.acos(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case ATAN_AF, ATAN_F -> operand.atan(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case ACOT_AF, ACOT_F -> operand.acot(mathContext, trigonometricMode, CALCULATION_LOCALE);
			case ASINH_AF, ASINH_F -> operand.asinh(mathContext, CALCULATION_LOCALE);
			case ACOSH_AF, ACOSH_F -> operand.acosh(mathContext, CALCULATION_LOCALE);
			case ATANH_AF, ATANH_F -> operand.atanh(mathContext, CALCULATION_LOCALE);
			case ACOTH_AF, ACOTH_F -> operand.acoth(mathContext, CALCULATION_LOCALE);
			case LOG10_F -> operand.log10(mathContext, CALCULATION_LOCALE);
			case LOG2_F -> operand.log2(mathContext, CALCULATION_LOCALE);
			case LN_F -> operand.ln(mathContext, CALCULATION_LOCALE);
			case ROOT_A, ROOT_F -> operand.squareRoot(mathContext, CALCULATION_LOCALE);
			case CUBIC_ROOT_AF, CUBIC_ROOT_F -> operand.cubicRoot(mathContext, CALCULATION_LOCALE);
			case FACTORIAL_F -> operand.factorial(mathContext, CALCULATION_LOCALE);
			default -> throw new IllegalArgumentException("Unsupported single-operand function: " + operator);
		};
	}

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
	 * @param rpnTokens
	 * 	a list of {@link Token} objects in Reverse Polish Notation
	 *
	 * @return the result of evaluating the expression as a {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if an unexpected token type is encountered
	 * @throws IllegalStateException
	 * 	if the expression does not reduce to a single result or has an unsupported result type
	 */
	public BigNumber evaluate(List<Token> rpnTokens) {
		Deque<Object> stack = new ArrayDeque<>();

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

		Object result = stack.pop();
		BigNumber finalResult;

		if (result instanceof BigNumber bn) {
			finalResult = bn;
		} else if (result instanceof BigNumberCoordinate coordinate) {
			finalResult = new BigNumberWrapper(coordinate.toString());
		} else {
			throw new IllegalStateException("Unsupported result type: " + result);
		}

		return finalResult;
	}

	/**
	 * Applies an arithmetic operator or function to the operands on the stack
	 * based on the parsed {@link Token}.
	 *
	 * @param token
	 * 	the token representing an operator or function
	 * @param stack
	 * 	the operand stack containing values as {@link BigNumber} or {@link BigNumberCoordinate}
	 *
	 * @throws IllegalArgumentException
	 * 	if the token value is not recognized as a valid operator or function
	 */
	private void applyArithmetic(Token token, Deque<Object> stack) {
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
