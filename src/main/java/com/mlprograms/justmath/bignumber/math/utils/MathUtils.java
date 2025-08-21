/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.bignumber.math.utils;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.ONE_HUNDRED_EIGHTY;
import static com.mlprograms.justmath.bignumber.BigNumbers.pi;

/**
 * Utility class for internal mathematical operations involving angle conversions.
 * <p>
 * This class provides helper methods for converting angles between degrees and radians
 * in a locale-sensitive and precision-aware manner using {@link BigNumber} and {@link BigDecimal}.
 * It is primarily used internally to support trigonometric computations that require
 * consistent and accurate unit conversions.
 */
public class MathUtils {

	/**
	 * Converts the given angle to radians depending on the specified {@link TrigonometricMode}.
	 * <p>
	 * If the {@code trigonometricMode} is {@link TrigonometricMode#DEG}, the angle is treated as
	 * being in degrees and converted to radians using the formula:
	 * <pre>
	 *     radians = degrees × (π / 180)
	 * </pre>
	 * If the mode is {@link TrigonometricMode#RAD}, the angle is assumed to already be in radians
	 * and returned directly as a {@link BigDecimal}.
	 *
	 * @param angle
	 * 	the angle to convert, represented as a {@link BigNumber}
	 * @param mathContext
	 * 	the {@link MathContext} to apply during internal calculations to control precision and rounding
	 * @param trigonometricMode
	 * 	the trigonometric mode indicating whether the input angle is in degrees or radians
	 * @param locale
	 * 	the {@link Locale} used to preserve regional number formatting or symbols in the {@code BigNumber}
	 *
	 * @return the angle in radians as a {@link BigDecimal}, computed with the specified precision and locale
	 */
	public static BigDecimal convertAngle(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		return (trigonometricMode == TrigonometricMode.DEG)
			       ? bigDecimalNumberToRadians(angle.toBigDecimal(), mathContext, locale)
			       : angle.toBigDecimal();
	}

	/**
	 * Converts an angle from radians to degrees.
	 * <p>
	 * The conversion uses the formula:
	 * <pre>
	 *     degrees = radians × (180 / π)
	 * </pre>
	 * This method wraps the input in a {@link BigNumber}, ensuring precision and locale awareness during conversion.
	 *
	 * @param radians
	 * 	the angle in radians as a {@link BigDecimal}
	 * @param mathContext
	 * 	the {@link MathContext} defining the precision and rounding used in the conversion
	 * @param locale
	 * 	the {@link Locale} to apply when instantiating the intermediate {@link BigNumber}
	 *
	 * @return the corresponding angle in degrees as a {@link BigDecimal}
	 */
	public static BigDecimal bigDecimalRadiansToDegrees(@NonNull final BigDecimal radians, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(radians, locale).toDegrees(mathContext).toBigDecimal();
	}

	/**
	 * Converts an angle from degrees to radians.
	 * <p>
	 * The conversion uses the formula:
	 * <pre>
	 *     radians = degrees × (π / 180)
	 * </pre>
	 * This method uses a {@link BigNumber} internally to perform the calculation precisely
	 * while respecting the given locale and math context.
	 *
	 * @param degrees
	 * 	the angle in degrees as a {@link BigDecimal}
	 * @param mathContext
	 * 	the {@link MathContext} that specifies the precision and rounding to use
	 * @param locale
	 * 	the {@link Locale} used for instantiating the {@link BigNumber}, ensuring consistent formatting and behavior
	 *
	 * @return the corresponding angle in radians as a {@link BigDecimal}
	 */
	public static BigDecimal bigDecimalNumberToRadians(@NonNull final BigDecimal degrees, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(degrees.multiply(pi(mathContext, locale).toBigDecimal()).divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext), locale).toBigDecimal();
	}

	/**
	 * Checks that the provided {@link MathContext} has a precision greater than zero.
	 * <p>
	 * Throws an {@link IllegalArgumentException} if the precision is zero or negative,
	 * ensuring that mathematical operations using this context are valid.
	 *
	 * @param mathContext
	 * 	the {@link MathContext} to validate
	 *
	 * @throws IllegalArgumentException
	 * 	if the precision is less than or equal to zero
	 */
	public static void checkMathContext(@NonNull final MathContext mathContext) {
		if (mathContext.getPrecision() <= 0) {
			throw new IllegalArgumentException("MathContext precision must be greater than zero");
		}
	}

	/**
	 * Ensures that the provided object is an instance of {@link BigNumber}.
	 * <p>
	 * If the object is a {@code BigNumber}, it is returned as-is.
	 * Otherwise, an {@link IllegalArgumentException} is thrown.
	 *
	 * @param object
	 * 	the object to check and cast
	 *
	 * @return the object cast to {@link BigNumber} if it is an instance
	 *
	 * @throws IllegalArgumentException
	 * 	if the object is not a {@link BigNumber}
	 */
	public static BigNumber ensureBigNumber(Object object) {
		if (object instanceof BigNumber bigNumber) {
			return bigNumber;
		}

		if (object instanceof BigNumberCoordinate bigNumberCoordinate) {
			return bigNumberCoordinate.getX();
		}

		throw new IllegalArgumentException("Expected BigNumber but got: " + object);
	}

}
