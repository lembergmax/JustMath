package com.mlprograms.justmath.bignumber.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;

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
 * <br>
 * <strong>Restrictions:</strong> None. This function is defined for all real input values.
 * <p>However, be aware that extremely large arguments may cause performance or overflow issues due to exponential growth.</p>
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
	 * The hyperbolic cotangent function is defined as:
	 * <pre>
	 *     coth(x) = 1 / tanh(x)
	 * </pre>
	 * <p>
	 * <strong>Restrictions:</strong>
	 * <ul>
	 *   <li>This function is <strong>undefined for x = 0</strong>. Division by zero will occur.</li>
	 *   <li>Very small values close to zero may lead to extreme outputs (positive or negative infinity-like behavior).</li>
	 * </ul>
	 *
	 * @param argument
	 * 	the input value for which to compute coth (must not be zero)
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding behavior
	 * @param locale
	 * 	the {@link Locale} used for formatting the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing coth(argument) calculated with the specified precision
	 *
	 * @throws ArithmeticException
	 * 	if {@code argument} is zero (undefined result due to division by zero)
	 */
	public static BigNumber coth(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (argument.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("argument cannot be zero");
		}

		return new BigNumber(BigDecimalMath.coth(argument.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

}
