package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.math.utils.MathUtils.bigDecimalRadiansToDegrees;

/**
 * Provides high-precision implementations of inverse trigonometric functions
 * using {@link BigNumber} for arbitrary precision arithmetic.
 * <p>
 * This class supports calculation of the arcsine, arccosine, arctangent,
 * and arccotangent functions with output in radians or degrees.
 */
public class InverseTrigonometricMath {

	/**
	 * Calculates the arcsine (inverse sine) of the given argument.
	 * <p>
	 * Mathematically, asin(x) returns the angle θ such that sin(θ) = x, where θ ∈ [-π/2, π/2].
	 * The function is defined for input values x ∈ [-1, 1].
	 * <p>
	 * Formula:
	 * <pre>
	 * asin(x) = θ, where sin(θ) = x
	 * </pre>
	 * <p>
	 * If {@code trigonometricMode} is DEG, the result is converted from radians to degrees.
	 *
	 * @param argument
	 * 	the input value x for which to compute arcsine
	 * @param mathContext
	 * 	the precision and rounding context
	 * @param trigonometricMode
	 * 	indicates whether the result is returned in radians or degrees
	 * @param locale
	 * 	locale used for formatting the output
	 *
	 * @return a {@link BigNumber} representing the arcsine of the argument
	 *
	 * @throws ArithmeticException
	 * 	if argument is outside [-1, 1]
	 */
	public static BigNumber asin(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		BigDecimal result = BigDecimalMath.asin(argument.toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}
		return new BigNumber(result.toPlainString(), locale);
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
		BigDecimal result = BigDecimalMath.acos(argument.toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}
		return new BigNumber(result.toPlainString(), locale);
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
		BigDecimal result = BigDecimalMath.atan(argument.toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	/**
	 * Calculates the arccotangent (inverse cotangent) of the given argument.
	 * <p>
	 * Mathematically, acot(x) returns the angle θ such that cot(θ) = x.
	 * The range of θ is typically (0, π).
	 * The cotangent is defined as cot(θ) = 1 / tan(θ).
	 * <p>
	 * Formula:
	 * <pre>
	 * acot(x) = atan(1/x), for x ≠ 0
	 * </pre>
	 * <p>
	 * This function does not support degree output mode; result is in radians.
	 *
	 * @param argument
	 * 	the input value x for which to compute arccotangent
	 * @param mathContext
	 * 	the precision and rounding context
	 * @param locale
	 * 	locale used for formatting the output
	 *
	 * @return a {@link BigNumber} representing the arccotangent of the argument
	 */
	public static BigNumber acot(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.acot(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

}
