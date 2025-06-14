package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.math.utils.MathUtils.convertAngle;

/**
 * Provides trigonometric functions operating on BigNumber values.
 * Supports angle inputs in degrees or radians, controlled by {@link TrigonometricMode}.
 */
public class TrigonometricMath {

	/**
	 * Calculates the sine of the given angle.
	 * <p>
	 * Mathematically, the sine function is defined as:
	 * <pre>
	 * sin(θ) = opposite / hypotenuse
	 * </pre>
	 * where θ is the angle in radians or degrees. This method converts the angle to radians if necessary.
	 *
	 * @param angle
	 * 	the angle for which to compute sine
	 * @param mathContext
	 * 	the {@link MathContext} controlling precision and rounding
	 * @param trigonometricMode
	 * 	the angle measurement mode (DEG for degrees, RAD for radians)
	 * @param locale
	 * 	the locale used for number formatting
	 *
	 * @return the sine of the angle as a {@link BigNumber}
	 */
	public static BigNumber sin(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);
		return new BigNumber(BigDecimalMath.sin(radians, mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the cosine of the given angle.
	 * <p>
	 * Mathematically, the cosine function is defined as:
	 * <pre>
	 * cos(θ) = adjacent / hypotenuse
	 * </pre>
	 * where θ is the angle in radians or degrees. This method converts the angle to radians if necessary.
	 *
	 * @param angle
	 * 	the angle for which to compute cosine
	 * @param mathContext
	 * 	the {@link MathContext} controlling precision and rounding
	 * @param trigonometricMode
	 * 	the angle measurement mode (DEG for degrees, RAD for radians)
	 * @param locale
	 * 	the locale used for number formatting
	 *
	 * @return the cosine of the angle as a {@link BigNumber}
	 */
	public static BigNumber cos(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);
		return new BigNumber(BigDecimalMath.cos(radians, mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the tangent of the given angle.
	 * <p>
	 * Mathematically, the tangent function is defined as:
	 * <pre>
	 * tan(θ) = sin(θ) / cos(θ)
	 * </pre>
	 * where θ is the angle in radians or degrees. This method converts the angle to radians if necessary.
	 * Note that tangent is undefined where cosine is zero.
	 *
	 * @param angle
	 * 	the angle for which to compute tangent
	 * @param mathContext
	 * 	the {@link MathContext} controlling precision and rounding
	 * @param trigonometricMode
	 * 	the angle measurement mode (DEG for degrees, RAD for radians)
	 * @param locale
	 * 	the locale used for number formatting
	 *
	 * @return the tangent of the angle as a {@link BigNumber}
	 */
	public static BigNumber tan(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);
		return new BigNumber(BigDecimalMath.tan(radians, mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the cotangent of the given angle.
	 * <p>
	 * Mathematically, the cotangent function is defined as:
	 * <pre>
	 * cot(θ) = 1 / tan(θ) = cos(θ) / sin(θ)
	 * </pre>
	 * where θ is the angle in radians or degrees. This method converts the angle to radians if necessary.
	 * Cotangent is undefined where sine is zero.
	 *
	 * @param angle
	 * 	the angle for which to compute cotangent
	 * @param mathContext
	 * 	the {@link MathContext} controlling precision and rounding
	 * @param trigonometricMode
	 * 	the angle measurement mode (DEG for degrees, RAD for radians)
	 * @param locale
	 * 	the locale used for number formatting
	 *
	 * @return the cotangent of the angle as a {@link BigNumber}
	 */
	public static BigNumber cot(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);
		return new BigNumber(BigDecimalMath.cot(radians, mathContext).toPlainString(), locale);
	}

}
