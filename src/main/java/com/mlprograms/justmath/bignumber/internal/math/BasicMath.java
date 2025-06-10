package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;

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
	public static BigNumber add(BigNumber augend, BigNumber addend) {
		return new BigNumber(augend.toBigDecimal().add(addend.toBigDecimal()).toPlainString());
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
	public static BigNumber subtract(BigNumber minuend, BigNumber subtrahend) {
		return new BigNumber(minuend.toBigDecimal().subtract(subtrahend.toBigDecimal()).toPlainString());
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
	public static BigNumber multiply(BigNumber multiplicand, BigNumber multiplier) {
		return new BigNumber(multiplicand.toBigDecimal().multiply(multiplier.toBigDecimal()).toPlainString());
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
	public static BigNumber divide(BigNumber dividend, BigNumber divisor, MathContext mathContext) {
		if (divisor.compareTo(BigNumbers.ZERO) == 0) {
			throw new ArithmeticException("Division by zero");
		}

		return new BigNumber(dividend.toBigDecimal().divide(divisor.toBigDecimal(), mathContext).toPlainString());
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
	public static BigNumber modulo(BigNumber dividend, BigNumber divisor) {
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

		return remainder;
	}

	/**
	 * Calculates the power of a {@link BigNumber} base raised to a non-negative integer exponent,
	 * i.e., repeated multiplication of the base by itself.
	 * <p>
	 * Mathematically, this corresponds to:
	 * <pre>
	 *     base^exponent = base × base × ... × base  (exponent times)
	 * </pre>
	 * For {@code exponent = 0}, by definition:
	 * <pre>
	 *     base^0 = 1  (for base ≠ 0)
	 * </pre>
	 * This implementation supports only non-negative integer exponents and does not perform roots or
	 * exponential/logarithmic operations.
	 * The method is based on repeated multiplication:
	 * <pre>
	 *     result := 1
	 *     repeat exponent times:
	 *         result := result × base
	 * </pre>
	 *
	 * @param base
	 * 	the base of the power operation ({@code base ∈ ℝ})
	 * @param exponent
	 * 	the exponent, a non-negative integer ({@code exponent ∈ ℕ₀})
	 * @param mathContext
	 * 	the {@link MathContext} defining precision and rounding (used for intermediate calculations, currently unused)
	 * @param locale
	 * 	the {@link Locale} used for formatting the result
	 *
	 * @return the result of {@code base^exponent} as a {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if the exponent is negative or not an integer
	 * @implNote This method currently only supports {@code exponent ∈ ℕ₀} (non-negative integers).
	 * 	To support rational or real exponents, implementations of logarithms and roots would be required.
	 */
	public static BigNumber power(BigNumber base, BigNumber exponent, MathContext mathContext, Locale locale) {
		if (!exponent.isInteger() || exponent.isNegative()) {
			throw new IllegalArgumentException("Only non-negative integer exponents are supported in this implementation.");
		}

		BigNumber result = BigNumbers.ONE;
		BigNumber count = exponent.clone();

		while (!count.isEqualTo(BigNumbers.ZERO)) {
			result = multiply(result, base);
			count = subtract(count, BigNumbers.ONE);
		}

		return result;
	}

	/**
	 * Computes the factorial of a {@link BigNumber} argument using high-precision math.
	 * <p>
	 * The argument must be a non-negative integer. If the input is not an integer, the factorial
	 * is computed using the gamma function approximation.
	 *
	 * @param argument
	 * 	the number for which the factorial is to be computed
	 * @param mathContext
	 * 	the {@link MathContext} defining precision and rounding
	 * @param locale
	 * 	the {@link Locale} used to construct the result {@link BigNumber}
	 *
	 * @return the factorial of {@code argument} as a new {@link BigNumber}
	 */
	public static BigNumber factorial(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.factorial(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

}