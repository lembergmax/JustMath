package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.MathContext;
import java.util.Locale;

/**
 * Provides high-precision implementations of hyperbolic trigonometric functions
 * using {@link BigNumber} for arbitrary precision arithmetic.
 * <p>
 * This class acts as a wrapper around the {@link BigDecimalMath} library,
 * converting input and output values to and from {@link BigNumber} to maintain
 * consistent numeric representation throughout calculations.
 * <p>
 * Supported functions include:
 * <ul>
 *   <li>{@link #sinh(BigNumber, MathContext, Locale)} - Hyperbolic sine</li>
 *   <li>{@link #cosh(BigNumber, MathContext, Locale)} - Hyperbolic cosine</li>
 *   <li>{@link #tanh(BigNumber, MathContext, Locale)} - Hyperbolic tangent</li>
 *   <li>{@link #coth(BigNumber, MathContext, Locale)} - Hyperbolic cotangent</li>
 * </ul>
 */
public class HyperbolicTrigonometricMath {

	/**
	 * Calculates the hyperbolic sine of the given argument.
	 * <p>
	 * The hyperbolic sine function is defined as:
	 * <pre>
	 * sinh(x) = (e^x - e^(-x)) / 2
	 * </pre>
	 *
	 * @param argument
	 * 	the input value for which to compute sinh
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 * 	the {@link Locale} used for formatting the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing sinh(argument) calculated with the specified precision
	 */
	public static BigNumber sinh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.sinh(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Calculates the hyperbolic cosine of the given argument.
	 * <p>
	 * The hyperbolic cosine function is defined as:
	 * <pre>
	 * cosh(x) = (e^x + e^(-x)) / 2
	 * </pre>
	 *
	 * @param argument
	 * 	the input value for which to compute cosh
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 * 	the {@link Locale} used for formatting the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing cosh(argument) calculated with the specified precision
	 */
	public static BigNumber cosh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.cosh(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Calculates the hyperbolic tangent of the given argument.
	 * <p>
	 * The hyperbolic tangent function is defined as:
	 * <pre>
	 * tanh(x) = sinh(x) / cosh(x) = (e^x - e^(-x)) / (e^x + e^(-x))
	 * </pre>
	 *
	 * @param argument
	 * 	the input value for which to compute tanh
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 * 	the {@link Locale} used for formatting the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing tanh(argument) calculated with the specified precision
	 */
	public static BigNumber tanh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.tanh(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Calculates the hyperbolic cotangent of the given argument.
	 * <p>
	 * The hyperbolic cotangent function is defined as the reciprocal of the hyperbolic tangent:
	 * <pre>
	 * coth(x) = 1 / tanh(x)
	 * </pre>
	 * <p>
	 * Note that coth(x) is undefined for x = 0, and the caller should handle such cases appropriately.
	 *
	 * @param argument
	 * 	the input value for which to compute coth
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 * 	the {@link Locale} used for formatting the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing coth(argument) calculated with the specified precision
	 *
	 * @throws ArithmeticException
	 * 	if the calculation results in division by zero or undefined values
	 */
	public static BigNumber coth(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.coth(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

}
