package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ZERO;
import static com.mlprograms.justmath.bignumber.internal.math.BasicMath.exp;

/**
 * Utility class providing high‐precision logarithmic functions on {@link BigNumber} values.
 * <p>
 * Wraps the {@link BigDecimalMath} library to compute
 * base‐2 logarithm, base‐10 logarithm, natural logarithm (ln), and
 * logarithm with arbitrary positive base.
 * All methods accept a {@link MathContext} to control precision and rounding,
 * and a {@link Locale} for formatting the resulting {@link BigNumber}.
 */
public class LogarithmicMath {

	/**
	 * Computes the base‐2 logarithm of the given argument.
	 * <p>
	 * Mathematically defined as:
	 * <pre>
	 * log₂(x) = ln(x) / ln(2)
	 * </pre>
	 * where ln is the natural logarithm.
	 * Domain: x &gt; 0.
	 *
	 * @param argument
	 * 	the positive input value x
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding
	 * @param locale
	 * 	the {@link Locale} used to format the returned {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing log₂(argument)
	 *
	 * @throws ArithmeticException
	 * 	if the underlying library cannot compute with given context
	 * @throws IllegalArgumentException
	 * 	if argument is non‐positive
	 */
	public static BigNumber log2(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (argument.isNegative() || argument.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("Argument to log2 must be positive and non-zero.");
		}
		return new BigNumber(BigDecimalMath.log2(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Computes the base‐10 logarithm of the given argument.
	 * <p>
	 * Mathematically defined as:
	 * <pre>
	 * log₁₀(x) = ln(x) / ln(10)
	 * </pre>
	 * Domain: x &gt; 0.
	 *
	 * @param argument
	 * 	the positive input value x
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding
	 * @param locale
	 * 	the {@link Locale} used to format the returned {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing log₁₀(argument)
	 *
	 * @throws ArithmeticException
	 * 	if the underlying library cannot compute with given context
	 * @throws IllegalArgumentException
	 * 	if argument is non‐positive
	 */
	public static BigNumber log10(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (argument.isNegative() || argument.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("Argument to log10 must be positive and non-zero.");
		}
		return new BigNumber(BigDecimalMath.log10(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Computes the natural logarithm (ln) of the given argument.
	 * <p>
	 * Mathematically defined as the inverse of the exponential function:
	 * <pre>
	 * ln(x) = the unique y such that eʸ = x
	 * </pre>
	 * Domain: x &gt; 0.
	 *
	 * @param argument
	 * 	the positive input value x
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding
	 * @param locale
	 * 	the {@link Locale} used to format the returned {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing ln(argument)
	 *
	 * @throws ArithmeticException
	 * 	if the underlying library cannot compute with given context
	 * @throws IllegalArgumentException
	 * 	if argument is non‐positive
	 */
	public static BigNumber ln(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (argument.compareTo(ZERO) <= 0)
			throw new ArithmeticException("ln(x) undefined for x <= 0");

		BigDecimal last;
		BigDecimal result = BigDecimal.ZERO;
		int iterations = 0;

		do {
			last = result;
			result = result.subtract(BigDecimal.ONE.subtract(argument.divide(exp(new BigNumber(result.toPlainString(), locale, mathContext), mathContext, locale), mathContext).toBigDecimal()), mathContext);
		} while (result.subtract(last).abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mathContext.getPrecision())) > 0
			         && ++iterations < 50);

		return new BigNumber(result.toPlainString(), locale).trim();
	}

	/**
	 * Computes the logarithm of a number with respect to an arbitrary positive base.
	 * <p>
	 * Mathematically defined as:
	 * <pre>
	 * log₍b₎(x) = ln(x) / ln(b)
	 * </pre>
	 * where ln is the natural logarithm.
	 * Domain: x &gt; 0, b &gt; 0 &amp;&amp; b ≠ 1.
	 *
	 * @param number
	 * 	the positive input value x
	 * @param base
	 * 	the positive base b (not equal to 1)
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding
	 * @param locale
	 * 	the {@link Locale} used to format the returned {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing log₍base₎(number)
	 *
	 * @throws IllegalArgumentException
	 * 	if number ≤ 0, or if base ≤ 0, or base == 1
	 */
	public static BigNumber logBase(@NonNull final BigNumber number, @NonNull final BigNumber base, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (number.isNegative() || number.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("Number must be positive and non-zero.");
		}
		if (base.isNegative() || base.isEqualTo(ZERO) || base.isEqualTo(BigNumbers.ONE)) {
			throw new IllegalArgumentException("Base must be positive and not equal to 1.");
		}

		BigDecimal lnNumber = BigDecimalMath.log(number.toBigDecimal(), mathContext);
		BigDecimal lnBase = BigDecimalMath.log(base.toBigDecimal(), mathContext);
		BigDecimal result = lnNumber.divide(lnBase, mathContext);

		return new BigNumber(result.toPlainString(), locale).trim();
	}

}
