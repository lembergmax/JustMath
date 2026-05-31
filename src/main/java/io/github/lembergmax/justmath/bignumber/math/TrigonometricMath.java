/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package io.github.lembergmax.justmath.bignumber.math;

import static io.github.lembergmax.justmath.bignumber.math.utils.MathUtils.convertAngle;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

/**
 * Provides trigonometric functions operating on BigNumber values.
 * Supports angle inputs in degrees or radians, controlled by {@link TrigonometricMode}.
 */
public final class TrigonometricMath {

	/** Non-instantiable utility class. */
	private TrigonometricMath() {
	}


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
		MathUtils.checkMathContext(mathContext);

		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);

		return new BigNumber(BigDecimalMath.sin(radians, mathContext).toPlainString(), locale).trim();
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
		MathUtils.checkMathContext(mathContext);

		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);

		return new BigNumber(BigDecimalMath.cos(radians, mathContext).toPlainString(), locale).trim();
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
		MathUtils.checkMathContext(mathContext);

		if (isTangentSingularity(angle, trigonometricMode)) {
			throw new ArithmeticException(
					"tan is undefined at " + angle.toString() + " (cosine is zero at this point)");
		}

		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);

		return new BigNumber(BigDecimalMath.tan(radians, mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Detects exact tangent singularities for the {@link TrigonometricMode#DEG} mode where the input
	 * can be checked symbolically. In radian mode the input is transcendental in general, so a
	 * reliable exact check is not possible; callers in radian mode receive whatever the
	 * underlying {@link BigDecimalMath#tan(BigDecimal, MathContext)} produces.
	 *
	 * @param angle             the input angle
	 * @param trigonometricMode the angle measurement mode
	 * @return {@code true} when {@code angle} is an exact {@code 90° + k·180°} in degree mode
	 */
	private static boolean isTangentSingularity(final BigNumber angle, final TrigonometricMode trigonometricMode) {
		if (trigonometricMode != TrigonometricMode.DEG) {
			return false;
		}
		final BigDecimal degrees = angle.toBigDecimal();
		if (hasFractionalPart(degrees)) {
			return false;
		}
		final java.math.BigInteger integerDegrees = degrees.toBigIntegerExact();
		final java.math.BigInteger modulo = integerDegrees.mod(java.math.BigInteger.valueOf(180));
		return modulo.equals(java.math.BigInteger.valueOf(90));
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
		MathUtils.checkMathContext(mathContext);

		if (isCotangentSingularity(angle, trigonometricMode)) {
			throw new ArithmeticException(
					"cot is undefined at " + angle.toString() + " (sine is zero at this point)");
		}

		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);

		return new BigNumber(BigDecimalMath.cot(radians, mathContext).toPlainString(), locale).trim();
	}

	/**
	 * Detects exact cotangent singularities for the {@link TrigonometricMode#DEG} mode (where the
	 * input can be checked symbolically). In radian mode no exact check is performed.
	 *
	 * @param angle             the input angle
	 * @param trigonometricMode the angle measurement mode
	 * @return {@code true} when {@code angle} is an exact {@code k·180°} in degree mode
	 */
	private static boolean isCotangentSingularity(final BigNumber angle, final TrigonometricMode trigonometricMode) {
		if (trigonometricMode != TrigonometricMode.DEG) {
			return false;
		}
		final BigDecimal degrees = angle.toBigDecimal();
		if (hasFractionalPart(degrees)) {
			return false;
		}
		final java.math.BigInteger integerDegrees = degrees.toBigIntegerExact();
		return integerDegrees.mod(java.math.BigInteger.valueOf(180)).signum() == 0;
	}

	/**
	 * Returns whether the given degree value has a non-zero fractional part and therefore cannot be an
	 * exact integer multiple of a degree such as {@code 90° + k·180°}.
	 *
	 * @param degrees the angle expressed in degrees
	 * @return {@code true} if {@code degrees} is not a whole number of degrees
	 */
	private static boolean hasFractionalPart(final BigDecimal degrees) {
		return degrees.stripTrailingZeros().scale() > 0;
	}

}
