package com.mlprograms.justmath.bignumber.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.*;

/**
 * Provides high-precision implementations of inverse hyperbolic trigonometric functions
 * using {@link BigNumber} for arbitrary precision arithmetic.
 * <p>
 * This class serves as a wrapper around the {@link BigDecimalMath} library,
 * converting input and output values between {@link BigNumber} and {@link java.math.BigDecimal}
 * to maintain precision and consistent numeric representation.
 * <p>
 * Supported functions include:
 * <ul>
 *   <li>{@link #asinh(BigNumber, MathContext, Locale)} - Inverse hyperbolic sine</li>
 *   <li>{@link #acosh(BigNumber, MathContext, Locale)} - Inverse hyperbolic cosine</li>
 *   <li>{@link #atanh(BigNumber, MathContext, Locale)} - Inverse hyperbolic tangent</li>
 *   <li>{@link #acoth(BigNumber, MathContext, Locale)} - Inverse hyperbolic cotangent</li>
 * </ul>
 * <p>
 * Note that these functions may have domain restrictions, for example:
 * <ul>
 *   <li>{@code acosh(x)} requires {@code x >= 1}</li>
 *   <li>{@code atanh(x)} requires {@code |x| < 1}</li>
 *   <li>{@code acoth(x)} requires {@code |x| > 1}</li>
 * </ul>
 * Calling these methods with out-of-domain inputs may result in exceptions or NaN values depending
 * on the underlying library behavior.
 */
public class InverseHyperbolicTrigonometricMath {

	/**
	 * Calculates the inverse hyperbolic sine (area hyperbolic sine) of the given argument.
	 * <p>
	 * The inverse hyperbolic sine is defined as:
	 * <pre>
	 * asinh(x) = ln(x + sqrt(x^2 + 1))
	 * </pre>
	 *
	 * @param argument
	 * 	the input value for which to compute asinh
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 * 	the {@link Locale} used for formatting the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing asinh(argument) calculated with the specified precision
	 */
	public static BigNumber asinh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.asinh(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Calculates the inverse hyperbolic cosine (area hyperbolic cosine) of the given argument.
	 * <p>
	 * The inverse hyperbolic cosine is defined as:
	 * <pre>
	 * acosh(x) = ln(x + sqrt(x^2 - 1))
	 * </pre>
	 * <p>
	 * Domain restriction: {@code x >= 1}.
	 *
	 * @param argument
	 * 	the input value for which to compute acosh
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 * 	the {@link Locale} used for formatting the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing acosh(argument) calculated with the specified precision
	 *
	 * @throws ArithmeticException
	 * 	if the argument is outside the domain (absolute value >= 1)
	 */
	public static BigNumber acosh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (argument.isLessThan(ONE)) {
			throw new IllegalArgumentException("argument must be greater than 1");
		}

		return new BigNumber(BigDecimalMath.acosh(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Calculates the inverse hyperbolic tangent (area hyperbolic tangent) of the given argument.
	 * <p>
	 * The inverse hyperbolic tangent is defined as:
	 * <pre>
	 * atanh(x) = 0.5 * ln((1 + x) / (1 - x))
	 * </pre>
	 * <p>
	 * Domain restriction: {@code |x| < 1} and {@code |x| > 1}.
	 *
	 * @param argument
	 * 	the input value for which to compute atanh
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 * 	the {@link Locale} used for formatting the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing atanh(argument) calculated with the specified precision
	 *
	 * @throws ArithmeticException
	 * 	if the argument is outside the domain (absolute value >= 1)
	 */
	public static BigNumber atanh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (argument.isGreaterThanOrEqualTo(ONE) || argument.isLessThanOrEqualTo(NEGATIVE_ONE)) {
			throw new IllegalArgumentException("argument must be between 1 and -1");
		}

		return new BigNumber(BigDecimalMath.atanh(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Calculates the inverse hyperbolic cotangent (area hyperbolic cotangent) of the given argument.
	 * <p>
	 * The inverse hyperbolic cotangent is mathematically defined as:
	 * <pre>
	 * acoth(x) = 0.5 * ln((x + 1) / (x - 1))
	 * </pre>
	 * <p>
	 * <strong>Domain restriction:</strong><br>
	 * The function is only defined for real values where {@code |x| > 1}. Values with absolute value
	 * less than or equal to 1 (including 0) are outside the domain and will throw an exception.
	 *
	 * <p>
	 * Example valid inputs: -2, -1.5, 1.5, 2<br>
	 * Example invalid inputs: -1, 0, 1
	 *
	 * @param argument
	 *     the input value for which to compute acoth; must satisfy {@code |argument| > 1}
	 * @param mathContext
	 *     the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 *     the {@link Locale} used for formatting the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing acoth(argument) calculated with the specified precision
	 *
	 * @throws ArithmeticException
	 *     if the argument is outside the domain (i.e., {@code |argument| <= 1})
	 */
	public static BigNumber acoth(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if(argument.isEqualTo(ZERO)  || argument.isEqualTo(ONE) || argument.isEqualTo(NEGATIVE_ONE)) {
			throw new IllegalArgumentException("argument cannot be equal to zero, one or negative one");
		}

		return new BigNumber(BigDecimalMath.acoth(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

}
