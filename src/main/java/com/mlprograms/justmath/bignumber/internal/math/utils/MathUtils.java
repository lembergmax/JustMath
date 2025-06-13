package com.mlprograms.justmath.bignumber.internal.math.utils;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.util.Values;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ONE;
import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ONE_HUNDRED_EIGHTY;

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
	 * @param context
	 * 	the {@link MathContext} to apply during internal calculations to control precision and rounding
	 * @param trigonometricMode
	 * 	the trigonometric mode indicating whether the input angle is in degrees or radians
	 * @param locale
	 * 	the {@link Locale} used to preserve regional number formatting or symbols in the {@code BigNumber}
	 *
	 * @return the angle in radians as a {@link BigDecimal}, computed with the specified precision and locale
	 */
	public static BigDecimal convertAngle(BigNumber angle, MathContext context, TrigonometricMode trigonometricMode, Locale locale) {
		return (trigonometricMode == TrigonometricMode.DEG)
			       ? bigDecimalNumberToRadians(angle.toBigDecimal(), context, locale)
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
	public static BigDecimal bigDecimalRadiansToDegrees(BigDecimal radians, MathContext mathContext, Locale locale) {
		return new BigNumber(radians, locale).toDegrees(mathContext, locale).toBigDecimal();
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
	public static BigDecimal bigDecimalNumberToRadians(BigDecimal degrees, MathContext mathContext, Locale locale) {
		return new BigNumber(degrees.multiply(Values.PI.toBigDecimal()).divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext), locale).toBigDecimal();
	}

	/**
	 * Generates a uniformly distributed random integer {@link BigNumber} within the range [min, max).
	 * <p>
	 * Mathematically: returns a value x such that min ≤ x < max, where x is an integer.
	 * <p>
	 * Both {@code min} and {@code max} must be exact integers (no decimal part), and {@code min < max}.
	 *
	 * @param min
	 * 	the inclusive lower bound (must be an integer)
	 * @param max
	 * 	the exclusive upper bound (must be an integer)
	 *
	 * @return a random {@link BigNumber} representing an integer in the range [min, max)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code min} ≥ {@code max}, or if either value has decimal places
	 */

	public static BigNumber randomIntegerBigNumberInRange(BigNumber min, BigNumber max) {
		BigInteger minInt = min.toBigDecimal().toBigIntegerExact();
		BigInteger maxInt = max.add(ONE).toBigDecimal().toBigIntegerExact();

		if (minInt.compareTo(maxInt) >= 0) {
			throw new IllegalArgumentException("min must be less than max");
		}

		BigInteger range = maxInt.subtract(minInt);
		BigInteger randomInRange = new BigInteger(range.bitLength(), ThreadLocalRandom.current()).mod(range).add(minInt);

		return new BigNumber(randomInRange.toString(), min.getLocale());
	}

}
