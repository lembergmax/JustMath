/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.bignumber.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.bignumber.math.utils.MathUtils;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.ONE;
import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;

/**
 * A utility class that provides core arithmetic and mathematical operations for {@link BigNumber} instances.
 * <p>
 * This class wraps standard arithmetic operations (addition, subtraction, multiplication, division, modulo),
 * as well as advanced operations such as exponentiation and factorial computation, using high-precision
 * arithmetic via {@code BigDecimal} and external libraries where needed.
 */
public class BasicMath {

	/**
	 * Adds two {@link BigNumber} values.
	 *
	 * @param augend
	 * 	the number to which the addend is added
	 * @param addend
	 * 	the number to add to the augend
	 * @param locale
	 * 	the {@link Locale} used for formatting and parsing the result
	 *
	 * @return a new {@link BigNumber} representing the sum of {@code augend} and {@code addend}
	 */
	public static BigNumber add(@NonNull final BigNumber augend, @NonNull final BigNumber addend, @NonNull final Locale locale) {
		return new BigNumber(augend.toBigDecimal().add(addend.toBigDecimal()).toPlainString(), locale).trim();
	}

	/**
	 * Subtracts one {@link BigNumber} from another.
	 *
	 * @param minuend
	 * 	the number from which the {@code subtrahend} is subtracted
	 * @param subtrahend
	 * 	the number to subtract from the {@code minuend}
	 * @param locale
	 * 	the {@link Locale} used for formatting and parsing the result
	 *
	 * @return a new {@link BigNumber} representing the result of the subtraction
	 */
	public static BigNumber subtract(@NonNull final BigNumber minuend, @NonNull final BigNumber subtrahend, @NonNull final Locale locale) {
		return new BigNumber(minuend.toBigDecimal().subtract(subtrahend.toBigDecimal()).toPlainString(), locale).trim();
	}

	/**
	 * Multiplies two {@link BigNumber} values.
	 *
	 * @param multiplicand
	 * 	the number to be multiplied
	 * @param multiplier
	 * 	the number by which to multiply the {@code multiplicand}
	 * @param locale
	 * 	the {@link Locale} used for formatting and parsing the result
	 *
	 * @return a new {@link BigNumber} representing the product
	 */
	public static BigNumber multiply(@NonNull final BigNumber multiplicand, @NonNull final BigNumber multiplier, @NonNull final Locale locale) {
		return new BigNumber(multiplicand.toBigDecimal().multiply(multiplier.toBigDecimal()).toPlainString(), locale).trim();
	}

	/**
	 * Divides one {@link BigNumber} by another, using the specified {@link MathContext} for precision and rounding.
	 *
	 * @param dividend
	 * 	the number to be divided
	 * @param divisor
	 * 	the number by which the {@code dividend} is divided
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 * 	the {@link Locale} used for formatting and parsing the result
	 *
	 * @return a new {@link BigNumber} representing the quotient
	 *
	 * @throws ArithmeticException
	 * 	if the divisor is zero
	 */
	public static BigNumber divide(@NonNull final BigNumber dividend, @NonNull final BigNumber divisor, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		if (divisor.compareTo(BigNumbers.ZERO) == 0) {
			throw new ArithmeticException("Division by zero");
		}

		return new BigNumber(dividend.toBigDecimal().divide(divisor.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Computes the modulo (remainder) of two non-negative {@link BigNumber} values.
	 * <p>
	 * The modulo is computed using repeated subtraction:
	 * <pre>
	 * while (remainder >= divisor) {
	 *     remainder -= divisor;
	 * }
	 * </pre>
	 *
	 * @param dividend
	 * 	the non-negative number from which the modulo is computed
	 * @param divisor
	 * 	the non-zero, non-negative number used as the divisor
	 * @param locale
	 * 	the {@link Locale} used for formatting and parsing the result
	 *
	 * @return the remainder of the division (dividend mod divisor)
	 *
	 * @throws IllegalArgumentException
	 * 	if either argument is negative or if the divisor is zero
	 */
	public static BigNumber modulo(@NonNull final BigNumber dividend, @NonNull final BigNumber divisor, @NonNull final Locale locale) {
		if (divisor.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("Cannot perform modulo operation with divisor zero.");
		}

		BigNumber dividendAbs = dividend.abs();
		BigNumber divisorAbs = divisor.abs();

		BigNumber remainder = dividendAbs.clone();

		while (remainder.isGreaterThanOrEqualTo(divisorAbs)) {
			remainder = remainder.subtract(divisorAbs);
		}

		if (dividend.isNegative()) {
			return divisorAbs.subtract(remainder).trim();
		} else {
			return new BigNumber(remainder, locale).trim();
		}
	}

	/**
	 * Raises a {@link BigNumber} base to the power of a given {@link BigNumber} exponent using the identity:
	 * <pre>
	 *     a^b = exp(b * ln|a|)
	 * </pre>
	 * and re-applies the sign if the base is negative and the exponent is real.
	 * <p>
	 * Mathematically, exponentiation with real exponents is defined as follows:
	 * <ul>
	 *     <li>If the base <code>a</code> is positive, the power <code>a^b</code> is defined for all real <code>b</code>.</li>
	 *     <li>If <code>a</code> is negative and <code>b</code> is an integer, the result is real and preserves the expected sign.</li>
	 *     <li>If <code>a</code> is negative and <code>b</code> is non-integer, the result is generally complex (involving imaginary parts),
	 *         but this method approximates a real result by applying the sign after computing <code>exp(b * ln|a|)</code>.</li>
	 * </ul>
	 * This method:
	 * <ol>
	 *     <li>Computes the natural logarithm <code>ln|a|</code> of the absolute value of the base.</li>
	 *     <li>Multiplies the logarithm by the exponent <code>b</code>.</li>
	 *     <li>Computes the exponential <code>exp(b * ln|a|)</code> to obtain the absolute value of the result.</li>
	 *     <li>Applies the original sign of the base if it was negative, flipping the result's sign accordingly.</li>
	 * </ol>
	 * <p>
	 * <b>Important:</b> This implementation assumes real-valued results and does not support complex number outputs
	 * (e.g. it does not return <code>i</code> for results like <code>(-1)^0.5</code>). If <code>base &lt; 0</code> and
	 * <code>exponent</code> is not an integer, the returned value will be negative, though mathematically the result is complex.
	 * <p>
	 * This method uses high-precision arithmetic via the {@link BigDecimalMath} library to ensure accuracy and is locale-aware
	 * to support appropriate formatting and parsing.
	 *
	 * @param base
	 * 	the base <code>a</code> of the exponentiation, must not be null
	 * @param exponent
	 * 	the exponent <code>b</code> of the exponentiation, must not be null
	 * @param mathContext
	 * 	the precision and rounding settings for all intermediate and final calculations, must not be null
	 * @param locale
	 * 	the locale used for parsing and formatting the result, must not be null
	 *
	 * @return a new {@link BigNumber} representing the computed power <code>a^b</code>
	 *
	 * @throws ArithmeticException
	 * 	if the logarithm of zero is attempted (i.e., <code>base = 0</code> and <code>exponent &le; 0</code>)
	 * @throws NullPointerException
	 * 	if any argument is {@code null}
	 * @see BigDecimalMath#log(BigDecimal, MathContext)
	 * @see BigDecimalMath#exp(BigDecimal, MathContext)
	 * @see MathContext
	 */
	public static BigNumber power(@NonNull final BigNumber base, @NonNull final BigNumber exponent, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		BigDecimal bigDecimalBase = base.toBigDecimal();
		BigDecimal bigDecimalValue = exponent.toBigDecimal();

		// Exponent == 0 → result = 1
		if (BigDecimal.ZERO.compareTo(bigDecimalValue) == 0) {
			return ONE;
		}

		// Exponent == 1 → result = base
		if (BigDecimal.ONE.compareTo(bigDecimalValue) == 0) {
			return base.trim();
		}

		// Exponent == -1 → result = 1 / base
		if (BigDecimal.ONE.negate().compareTo(bigDecimalValue) == 0) {
			BigDecimal reciprocal = BigDecimal.ONE.divide(bigDecimalBase, mathContext);
			return new BigNumber(reciprocal.toPlainString(), locale, mathContext).trim();
		}

		// Exact integer exponent → use pow(int) to avoid rounding issues
		try {
			int integerExponent = bigDecimalValue.intValueExact();
			BigDecimal powResult = bigDecimalBase.pow(integerExponent, mathContext);
			return new BigNumber(powResult.toPlainString(), locale, mathContext).trim();
		} catch (ArithmeticException ignored) {
			// exponent is not an exact integer → use general formula
		}

		// Check for invalid log(0)
		if (bigDecimalBase.signum() == 0) {
			if (bigDecimalValue.signum() < 0) {
				throw new ArithmeticException("Cannot compute 0^" + bigDecimalValue + " (log undefined)");
			} else if (bigDecimalValue.signum() > 0) {
				return ZERO; // 0^positive = 0
			}
			// 0^0 is handled above (returns 1)
		}

		// General case: a^b = exp(b * ln|a|)
		BigDecimal lnAbsBase = BigDecimalMath.log(bigDecimalBase.abs(), mathContext);
		BigDecimal exponentTimesLn = bigDecimalValue.multiply(lnAbsBase, mathContext);
		BigDecimal absResult = BigDecimalMath.exp(exponentTimesLn, mathContext);

		// Re-apply sign if the base was negative and the exponent is an integer
		BigDecimal signedResult = bigDecimalBase.signum() < 0 ? absResult.negate() : absResult;

		return new BigNumber(signedResult.toPlainString(), locale, mathContext).trim();
	}

	/**
	 * Computes the factorial of a {@link BigNumber} argument.
	 * <p>
	 * Mathematically, factorial for a non-negative integer n is defined as:
	 * <pre>
	 *     n! = n × (n-1) × (n-2) × ... × 1, with 0! = 1
	 * </pre>
	 * This method supports only non-negative integer inputs.
	 *
	 * @param argument
	 * 	the number for which the factorial is to be computed; must be a non-negative integer
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding (currently not used in calculation)
	 * @param locale
	 * 	the {@link Locale} used for formatting the result
	 *
	 * @return the factorial of {@code argument} as a new {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code argument} is negative or not an integer
	 */
	public static BigNumber factorial(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		return new BigNumber(BigDecimalMath.factorial(argument.toBigDecimal(), mathContext).toPlainString());

		/*
		if (!argument.isInteger()) {
			throw new IllegalArgumentException("Factorial is only defined for integers.");
		}
		if (argument.isNegative()) {
			throw new IllegalArgumentException("Factorial is only defined for non-negative integers.");
		}

		BigNumber result = BigNumbers.ONE;
		BigNumber counter = argument.clone();

		// 0! = 1, so if the argument is zero, returns 1 immediately
		if (counter.isEqualTo(BigNumbers.ZERO)) {
			return new BigNumber("1", locale, mathContext);
		}

		// multiply the result by each integer from argument down to 1
		while (counter.isGreaterThan(BigNumbers.ONE)) {
			result = result.multiply(counter);
			counter = counter.subtract(BigNumbers.ONE);
		}

		return new BigNumber(result.toString(), locale, mathContext).trim();
		 */
	}

	/**
	 * Computes the exponential function <code>e<sup>x</sup></code> for the given {@link BigNumber} argument
	 * with high precision, using the specified {@link MathContext} for precision and rounding.
	 * <p>
	 * This method implements the Taylor series expansion of the exponential function:
	 * <pre>
	 *     e<sup>x</sup> = 1 + x/1! + x<sup>2</sup>/2! + x<sup>3</sup>/3! + ...
	 * </pre>
	 * The calculation proceeds by iteratively computing each term of the series until
	 * the absolute value of the next term is smaller than the threshold defined by the
	 * {@link MathContext} precision (i.e., less than 10<sup>-precision</sup>).
	 * <p>
	 * For example:
	 * <ul>
	 *     <li><code>exp(0) → 1</code></li>
	 *     <li><code>exp(1) → e ≈ 2.71828...</code></li>
	 *     <li><code>exp(-1) → 1/e ≈ 0.36788...</code></li>
	 * </ul>
	 * <p>
	 * <b>Algorithm:</b>
	 * <ol>
	 *     <li>Initialize the result to 1 (the first term of the series).</li>
	 *     <li>Initialize the current term to 1.</li>
	 *     <li>For n = 1, 2, 3, ...:
	 *         <ul>
	 *             <li>Multiply the current term by <code>x</code> and divide by <code>n</code>.</li>
	 *             <li>Add the term to the result.</li>
	 *             <li>Stop when the term's absolute value is below the precision threshold.</li>
	 *         </ul>
	 *     </li>
	 * </ol>
	 * <p>
	 * This approach guarantees that the result converges to the desired precision for any real input.
	 * It is particularly effective for small to moderate values of <code>x</code>. For very large
	 * arguments, more numerically stable methods (e.g., scaling and squaring) may be more efficient,
	 * but this implementation prioritizes correctness and simplicity.
	 * <p>
	 * The method is locale-aware, meaning the returned {@link BigNumber} is formatted and parsed
	 * according to the specified {@link Locale}.
	 *
	 * @param argument
	 * 	the exponent <code>x</code> in <code>e<sup>x</sup></code>; must not be {@code null}
	 * @param mathContext
	 * 	the {@link MathContext} specifying the precision and rounding behavior;
	 * 	must not be {@code null} and must have a positive precision
	 * @param locale
	 * 	the {@link Locale} used for parsing and formatting the resulting {@link BigNumber}; must not be {@code null}
	 *
	 * @return a new {@link BigNumber} representing the computed exponential value <code>e<sup>x</sup></code>
	 *
	 * @throws NullPointerException
	 * 	if any of the parameters is {@code null}
	 * @see BigDecimalMath#exp(BigDecimal, MathContext)
	 * @see Math#exp(double)
	 */
	public static BigNumber exp(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		BigDecimal result = BigDecimal.ONE;
		BigDecimal term = BigDecimal.ONE;

		int n = 1;
		while (term.compareTo(BigDecimal.ZERO) != 0) {
			term = term.multiply(argument.toBigDecimal(), mathContext).divide(BigDecimal.valueOf(n), mathContext);
			result = result.add(term, mathContext);

			if (term.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mathContext.getPrecision())) < 0) {
				break;
			}

			n++;
		}

		return new BigNumber(result.toPlainString(), locale, mathContext);
	}

}
