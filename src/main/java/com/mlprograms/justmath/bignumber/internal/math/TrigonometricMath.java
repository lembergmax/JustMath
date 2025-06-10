package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.math.utils.MathHelper.convertAngle;

/**
 * Provides trigonometric functions operating on BigNumber values.
 * Supports angle inputs in degrees or radians, controlled by {@link TrigonometricMode}.
 */
public class TrigonometricMath {

	/**
	 * Computes the sine of a given angle using the Taylor series expansion.
	 * <p>
	 * This method does <b>not</b> rely on any external libraries. It uses
	 * the following Taylor series expansion for sin(x):
	 * <pre>
	 *     sin(x) = x - x^3/3! + x^5/5! - x^7/7! + ...
	 *            = ∑ (n=0 to ∞) [(-1)^n * x^(2n+1)] / (2n+1)!
	 * </pre>
	 * The angle is first converted to radians depending on the given
	 * {@link TrigonometricMode}. The series is evaluated until a fixed
	 * number of terms (maxIterations) is reached.
	 *
	 * @param angle
	 * 	the angle for which to compute the sine, in degrees or radians
	 * @param mathContext
	 * 	the {@link MathContext} controlling the precision of intermediate and final results
	 * @param trigonometricMode
	 * 	the angle mode: degrees (DEG) or radians (RAD)
	 * @param locale
	 * 	the locale for formatting and parsing {@link BigNumber} values
	 *
	 * @return the sine of the angle as a {@link BigNumber}
	 */
	public static BigNumber sin(BigNumber angle, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		// Convert angle to radians if necessary (ensures x is in radian)
		BigNumber x = new BigNumber(convertAngle(angle, mathContext, trigonometricMode, locale), locale);

		// Initialize variables for Taylor series
		BigNumber term = x.clone();      // First term: x^1 / 1! = x
		BigNumber result = x.clone();    // Start accumulating the result with the first term
		BigNumber xSquared = x.multiply(x); // x^2 (used repeatedly)
		int maxIterations = 100;        // Number of terms to compute; affects precision
		int sign = -1;                  // Alternating sign for the series: +, -, +, -, ...

		for (int i = 1; i < maxIterations; i++) {
			// Calculate the denominator of the term: (2n)(2n+1) (this builds factorial incrementally)
			BigNumber denominator = new BigNumber(String.valueOf((2 * i) * (2 * i + 1)), locale);

			// Update the current term: term = term * x^2 / denominator
			term = term.multiply(xSquared).divide(denominator, mathContext);

			// Apply the alternating sign: (-1)^n
			BigNumber signedTerm = (sign == -1) ? term.negate() : term;

			// Accumulate result
			result = result.add(signedTerm);

			// Flip the sign for the next term
			sign *= -1;
		}

		// Round the result to the requested precision
		return result.round(mathContext);
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
	public static BigNumber cos(BigNumber angle, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
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
	public static BigNumber tan(BigNumber angle, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
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
	public static BigNumber cot(BigNumber angle, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);
		return new BigNumber(BigDecimalMath.cot(radians, mathContext).toPlainString(), locale);
	}

}
