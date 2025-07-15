package com.mlprograms.justmath.bignumber.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.math.utils.MathUtils;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;
import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.bigDecimalRadiansToDegrees;

/**
 * Provides high-precision implementations of inverse trigonometric functions
 * using {@link BigNumber} for arbitrary precision arithmetic.
 * <p>
 * This class supports calculation of the arcsine, arccosine, arctangent,
 * and arccotangent functions with output in radians or degrees.
 */
public class InverseTrigonometricMath {

	/**
	 * Computes the arcsine (inverse sine) of a given BigNumber.
	 * <p>
	 * The arcsine is defined as the angle θ such that:
	 * <pre>
	 *   sin(θ) = x, where θ ∈ [-π/2, π/2]
	 * </pre>
	 * and x ∈ [-1, 1]. This implementation uses the identity:
	 * <pre>
	 *   arcsin(x) = arctan( x / sqrt(1 - x²) )
	 * </pre>
	 * which provides improved numerical stability and precision over direct series expansion.
	 *
	 * <p>
	 * If the trigonometric mode is DEG (degrees), the result is converted accordingly.
	 *
	 * @param argument
	 * 	the input value x for which to compute arcsine; must be in [-1, 1]
	 * @param mathContext
	 * 	the context to control precision and rounding
	 * @param trigonometricMode
	 * 	the output mode: RAD or DEG
	 * @param locale
	 * 	the locale used for formatting output
	 *
	 * @return the arcsine of x as a BigNumber
	 *
	 * @throws ArithmeticException
	 * 	if argument is outside \[-1, 1\]
	 */
	public static BigNumber asin(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		BigDecimal result = BigDecimalMath.asin(argument.toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}
		return new BigNumber(result.toPlainString(), locale, mathContext).trim();
	}

	/**
	 * Calculates the arccosine (inverse cosine) of the given argument.
	 * <p>
	 * Mathematically, acos(x) returns the angle θ such that cos(θ) = x, where θ ∈ [0, π].
	 * The function is defined for input values x ∈ [-1, 1].
	 * <p>
	 * Formula:
	 * <pre>
	 * acos(x) = θ, where cos(θ) = x
	 * </pre>
	 * <p>
	 * If {@code trigonometricMode} is DEG, the result is converted from radians to degrees.
	 *
	 * @param argument
	 * 	the input value x for which to compute arccosine
	 * @param mathContext
	 * 	the precision and rounding context
	 * @param trigonometricMode
	 * 	indicates whether the result is returned in radians or degrees
	 * @param locale
	 * 	locale used for formatting the output
	 *
	 * @return a {@link BigNumber} representing the arccosine of the argument
	 *
	 * @throws ArithmeticException
	 * 	if argument is outside [-1, 1]
	 */
	public static BigNumber acos(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		BigDecimal result = BigDecimalMath.acos(argument.toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}
		return new BigNumber(result.toPlainString(), locale, mathContext).trim();
	}

	/**
	 * Calculates the arctangent (inverse tangent) of the given argument.
	 * <p>
	 * Mathematically, atan(x) returns the angle θ such that tan(θ) = x, where θ ∈ (-π/2, π/2).
	 * The function is defined for all real x.
	 * <p>
	 * Formula:
	 * <pre>
	 * atan(x) = θ, where tan(θ) = x
	 * </pre>
	 * <p>
	 * If {@code trigonometricMode} is DEG, the result is converted from radians to degrees.
	 *
	 * @param argument
	 * 	the input value x for which to compute arctangent
	 * @param mathContext
	 * 	the precision and rounding context
	 * @param trigonometricMode
	 * 	indicates whether the result is returned in radians or degrees
	 * @param locale
	 * 	locale used for formatting the output
	 *
	 * @return a {@link BigNumber} representing the arctangent of the argument
	 */
	public static BigNumber atan(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		BigDecimal result = BigDecimalMath.atan(argument.toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}
		return new BigNumber(result.toPlainString(), locale, mathContext).trim();
	}

	/**
	 * Calculates the arccotangent (inverse cotangent) of the given argument.
	 * <p>
	 * Mathematically, {@code acot(x)} returns the angle θ such that {@code cot(θ) = x}.
	 * Since {@code cot(θ) = 1 / tan(θ)}, the function is computed using:
	 * <pre>
	 * acot(x) = atan(1 / x), for x ≠ 0
	 * </pre>
	 *
	 * <p>
	 * The function is defined for all real values of {@code x} except 0.
	 * For positive arguments, the result is in (0, π/2),
	 * and for negative arguments, the result is in (−π/2, 0).
	 * The result is returned in radians or degrees, depending on the specified {@link TrigonometricMode}.
	 *
	 * <p><strong>Domain restriction:</strong> x ≠ 0. The function is undefined for zero due to division by zero.
	 *
	 * @param argument
	 * 	the input value {@code x} for which to compute the inverse cotangent; must not be zero
	 * @param mathContext
	 * 	the precision and rounding context to be used during the computation
	 * @param trigonometricMode
	 * 	determines whether the result is returned in radians or degrees
	 * @param locale
	 * 	the locale used to format the resulting {@link BigNumber}
	 *
	 * @return a {@link BigNumber} representing the inverse cotangent of the argument
	 *
	 * @throws ArithmeticException
	 * 	if the argument is zero (undefined operation)
	 * @see #atan(BigNumber, MathContext, TrigonometricMode, Locale)
	 */
	public static BigNumber acot(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		if (argument.isEqualTo(ZERO)) {
			throw new ArithmeticException("acot(x) is undefined for x = 0");
		}

		BigDecimal result = BigDecimalMath.acot(argument.toBigDecimal(), mathContext);

		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}

		return new BigNumber(result.toPlainString(), locale, mathContext, trigonometricMode);
	}

}
