package com.mlprograms.justmath.number;

import com.mlprograms.justmath.calculator.CalculatorEngine;

/**
 * Utility class that performs arithmetic operations on BigNumber instances.
 * <p>
 * All operations assume non-null inputs. This class handles normalization of numbers,
 * alignment of decimal places, and sign management where necessary.
 */
public final class BigNumberArithmetic {

	private final CalculatorEngine calculatorEngine = new CalculatorEngine();

	/**
	 * Adds this BigNumber to another BigNumber using CalculatorEngine.
	 *
	 * @param other
	 * 	the number to add
	 *
	 * @return the sum as a new BigNumber
	 */
	public BigNumber add(BigNumber other) {
		String expression = this + "+" + other.toString();
		BigNumber result = calculatorEngine.calculate(expression);
		return new BigNumber(result);
	}

	/**
	 * Subtracts another BigNumber from this BigNumber using CalculatorEngine.
	 *
	 * @param other
	 * 	the number to subtract
	 *
	 * @return the difference as a new BigNumber
	 */
	public BigNumber subtract(BigNumber other) {
		String expression = this + "-" + other.toString();
		BigNumber result = calculatorEngine.calculate(expression);
		return new BigNumber(result);
	}

	/**
	 * Multiplies this BigNumber with another BigNumber using CalculatorEngine.
	 *
	 * @param other
	 * 	the number to multiply with
	 *
	 * @return the product as a new BigNumber
	 */
	public BigNumber multiply(BigNumber other) {
		String expression = this + "*" + other.toString();
		BigNumber result = calculatorEngine.calculate(expression);
		return new BigNumber(result);
	}

	/**
	 * Divides this BigNumber by another BigNumber using CalculatorEngine.
	 *
	 * @param other
	 * 	the number to divide by
	 *
	 * @return the quotient as a new BigNumber
	 *
	 * @throws ArithmeticException
	 * 	if division by zero occurs
	 */
	public BigNumber divide(BigNumber other) {
		String expression = this + "/" + other.toString();
		BigNumber result = calculatorEngine.calculate(expression);
		return new BigNumber(result);
	}


	/**
	 * Raises a BigNumber to the power of another BigNumber.
	 *
	 * @param base
	 * 	the base BigNumber
	 * @param exponent
	 * 	the exponent BigNumber
	 *
	 * @return result of base ^ exponent
	 */
	BigNumber pow(BigNumber base, BigNumber exponent) {
		BigNumber result = calculatorEngine.calculate(base.toString() + "^" + exponent.toString());
		return new BigNumber(result, base.getLocale());
	}

	/**
	 * Computes the square root of a BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return square root of number
	 */
	BigNumber root(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("√(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the cube root of a BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return cube root of number
	 */
	BigNumber thirdRoot(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("³√(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the factorial of a BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return factorial of number
	 */
	BigNumber factorial(BigNumber number) {
		BigNumber result = calculatorEngine.calculate(number.toString() + "!");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the sine of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return sine of number
	 */
	BigNumber sin(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("sin(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the cosine of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return cosine of number
	 */
	BigNumber cos(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("cos(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the tangent of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return tangent of number
	 */
	BigNumber tan(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("tan(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the hyperbolic sine of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return sinh(number)
	 */
	BigNumber sinh(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("sinh(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the hyperbolic cosine of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return cosh(number)
	 */
	BigNumber cosh(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("cosh(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the hyperbolic tangent of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return tanh(number)
	 */
	BigNumber tanh(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("tanh(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the inverse sine (arcsin) of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return asin(number)
	 */
	BigNumber asin(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("sin⁻¹(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the inverse cosine (arccos) of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return acos(number)
	 */
	BigNumber acos(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("cos⁻¹(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the inverse tangent (arctan) of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return atan(number)
	 */
	BigNumber atan(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("tan⁻¹(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the inverse hyperbolic sine of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return asinh(number)
	 */
	BigNumber asinh(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("sinh⁻¹(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the inverse hyperbolic cosine of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return acosh(number)
	 */
	BigNumber acosh(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("cosh⁻¹(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the inverse hyperbolic tangent of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return atanh(number)
	 */
	BigNumber atanh(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("tanh⁻¹(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the base-10 logarithm of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return log10(number)
	 */
	BigNumber log10(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("log(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the natural logarithm (base e) of the given BigNumber.
	 *
	 * @param number
	 * 	the input BigNumber
	 *
	 * @return ln(number)
	 */
	BigNumber ln(BigNumber number) {
		BigNumber result = calculatorEngine.calculate("ln(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}

	/**
	 * Computes the logarithm of the given number to a specified base.
	 *
	 * @param number
	 * 	the input BigNumber
	 * @param base
	 * 	the base of the logarithm
	 *
	 * @return log_base(number)
	 */
	BigNumber logBase(BigNumber number, int base) {
		BigNumber result = calculatorEngine.calculate("log" + base + "(" + number.toString() + ")");
		return new BigNumber(result, number.getLocale());
	}


}