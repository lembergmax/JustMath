package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ZERO;

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
	 *
	 * @return a new {@link BigNumber} representing the sum of {@code augend} and {@code addend}
	 */
	public static BigNumber add(@NonNull final BigNumber augend, @NonNull final BigNumber addend) {
		return new BigNumber(augend.toBigDecimal().add(addend.toBigDecimal()).toPlainString()).trim();
	}

	/**
	 * Subtracts one {@link BigNumber} from another.
	 *
	 * @param minuend
	 * 	the number from which the {@code subtrahend} is subtracted
	 * @param subtrahend
	 * 	the number to subtract from the {@code minuend}
	 *
	 * @return a new {@link BigNumber} representing the result of the subtraction
	 */
	public static BigNumber subtract(@NonNull final BigNumber minuend, @NonNull final BigNumber subtrahend) {
		return new BigNumber(minuend.toBigDecimal().subtract(subtrahend.toBigDecimal()).toPlainString()).trim();
	}

	/**
	 * Multiplies two {@link BigNumber} values.
	 *
	 * @param multiplicand
	 * 	the number to be multiplied
	 * @param multiplier
	 * 	the number by which to multiply the {@code multiplicand}
	 *
	 * @return a new {@link BigNumber} representing the product
	 */
	public static BigNumber multiply(@NonNull final BigNumber multiplicand, @NonNull final BigNumber multiplier) {
		return new BigNumber(multiplicand.toBigDecimal().multiply(multiplier.toBigDecimal()).toPlainString()).trim();
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
	 *
	 * @return a new {@link BigNumber} representing the quotient
	 *
	 * @throws ArithmeticException
	 * 	if the divisor is zero
	 */
	public static BigNumber divide(@NonNull final BigNumber dividend, @NonNull final BigNumber divisor, @NonNull final MathContext mathContext) {
		if (divisor.compareTo(BigNumbers.ZERO) == 0) {
			throw new ArithmeticException("Division by zero");
		}

		return new BigNumber(dividend.toBigDecimal().divide(divisor.toBigDecimal(), mathContext).toPlainString()).trim();
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
	 *
	 * @return the remainder of the division (dividend mod divisor)
	 *
	 * @throws IllegalArgumentException
	 * 	if either argument is negative or if the divisor is zero
	 */
	public static BigNumber modulo(@NonNull final BigNumber dividend, @NonNull final BigNumber divisor) {
		if (divisor.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("Cannot perform modulo operation with divisor zero.");
		}

		if (dividend.isNegative() || divisor.isNegative()) {
			throw new IllegalArgumentException("Modulo operation requires both numbers to be non-negative.");
		}

		BigNumber remainder = dividend.clone();

		while (remainder.isGreaterThanOrEqualTo(divisor)) {
			remainder = remainder.subtract(divisor);
		}

		return remainder.trim();
	}

	/**
	 * Computes the power of a {@link BigNumber} base raised to an arbitrary real exponent.
	 * <p>
	 * Mathematically, the power function with real base and real exponent is defined as:
	 * <pre>
	 *     base^exponent = e^(exponent * ln(base))
	 * </pre>
	 * where {@code e} is Euler's number and {@code ln} is the natural logarithm.
	 * <p>
	 * This method supports negative, zero, and fractional exponents, as well as negative and fractional bases
	 * where mathematically defined (currently, only positive bases are supported due to logarithm domain).
	 * <p>
	 * Special cases:
	 * <ul>
	 *   <li>If {@code base} is zero and {@code exponent} &gt; 0, returns zero.</li>
	 *   <li>Negative or zero {@code base} values are not supported because natural logarithm is undefined there.</li>
	 *   <li>{@code exponent} can be any real number (negative, zero, fractional).</li>
	 * </ul>
	 *
	 * @param base
	 * 	the base value of the power operation, must be &gt; 0 (domain restriction due to ln)
	 * @param exponent
	 * 	the real exponent value
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior for intermediate calculations
	 * @param locale
	 * 	the {@link Locale} used to format the resulting {@link BigNumber}
	 *
	 * @return a new {@link BigNumber} representing {@code base^exponent}
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code base} is less than or equal to zero (since natural logarithm is undefined there)
	 */
	public static BigNumber power(@NonNull final BigNumber base, @NonNull final BigNumber exponent, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		// TODO: replace with own and more functional logic
		return new BigNumber(BigDecimalMath.pow(base.toBigDecimal(), exponent.toBigDecimal(), mathContext).toPlainString(), locale, mathContext).trim();
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
		if (!argument.isInteger()) {
			throw new IllegalArgumentException("Factorial is only defined for integers.");
		}
		if (argument.isNegative()) {
			throw new IllegalArgumentException("Factorial is only defined for non-negative integers.");
		}

		BigNumber result = BigNumbers.ONE;
		BigNumber counter = argument.clone();

		// 0! = 1, so if argument is zero, returns 1 immediately
		if (counter.isEqualTo(BigNumbers.ZERO)) {
			return new BigNumber("1", locale, mathContext);
		}

		// multiply result by each integer from argument down to 1
		while (counter.isGreaterThan(BigNumbers.ONE)) {
			result = result.multiply(counter);
			counter = counter.subtract(BigNumbers.ONE);
		}

		return new BigNumber(result.toString(), locale, mathContext).trim();
	}

	/**
	 * Computes the exponential function <code>e<sup>x</sup></code> for a given {@link BigNumber} argument using
	 * its Maclaurin (Taylor) series expansion.
	 * <p>
	 * The exponential function is defined mathematically as:
	 * <pre>
	 *     exp(x) = e^x = Σ (x^n / n!) from n = 0 to ∞
	 * </pre>
	 * where:
	 * <ul>
	 *   <li><code>x</code> is the real number input (in this case represented by a {@link BigNumber})</li>
	 *   <li><code>n!</code> is the factorial of n</li>
	 * </ul>
	 * This implementation uses a loop to iteratively compute and sum terms of the Maclaurin series until the
	 * absolute value of the current term is smaller than the numerical precision defined by the provided
	 * {@link MathContext}.
	 * <p>
	 * The computation proceeds as follows:
	 * <ol>
	 *   <li>Initialize the result with the first term of the series (1)</li>
	 *   <li>Iteratively compute each term using the recurrence relation:
	 *       <code>term = term * x / n</code> to avoid recomputing powers and factorials from scratch</li>
	 *   <li>Stop the iteration once the absolute value of the current term is less than
	 *       <code>10<sup>-precision</sup></code>, as defined by the {@link MathContext}</li>
	 * </ol>
	 * <p>
	 * This method ensures correct handling of precision and rounding through the specified {@link MathContext}.
	 * The result is returned as a new {@link BigNumber} instance using the specified {@link Locale}, which may
	 * influence formatting or parsing behavior elsewhere in the application.
	 * <p>
	 * <b>Note:</b> This method computes <code>e^x</code> only for real numbers. For complex exponents, a different
	 * implementation involving Euler's formula would be required.
	 *
	 * @param argument
	 * 	the exponent x in the expression <code>e^x</code>, must not be null
	 * @param mathContext
	 * 	the precision and rounding context to be used during computation must not be null
	 * @param locale
	 * 	the locale to be associated with the resulting {@link BigNumber}, must not be null
	 *
	 * @return the computed value of <code>e^x</code> as a {@link BigNumber}
	 *
	 * @throws NullPointerException
	 * 	if any of the parameters is null
	 * @see java.math.BigDecimal
	 * @see java.math.MathContext
	 * @see java.util.Locale
	 */
	public static BigNumber exp(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
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