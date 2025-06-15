package com.mlprograms.justmath.bignumber.internal.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;
import com.mlprograms.justmath.calculator.internal.CoordinateType;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.MathContext;
import java.util.Locale;

/**
 * Provides mathematical utilities to convert coordinates between
 * polar and Cartesian coordinate systems using {@link BigNumber} for high precision.
 * <p>
 * Supports angle interpretation in degrees or radians via {@link TrigonometricMode}.
 * All calculations honor the specified {@link MathContext} for precision and rounding,
 * and {@link Locale} for number formatting and parsing.
 */
public class CoordinateConversionMath {

	/**
	 * Converts polar coordinates (r, θ) to Cartesian coordinates (x, y).
	 * <p>
	 * Uses the standard conversion formulas:
	 * <pre>
	 *     x = r * cos(θ)
	 *     y = r * sin(θ)
	 * </pre>
	 * where θ can be given in degrees or radians depending on {@code trigonometricMode}.
	 *
	 * @param r
	 * 	the radius (distance from origin), must be non-negative
	 * @param theta
	 * 	the angle component (θ), interpreted according to {@code trigonometricMode}
	 * @param mathContext
	 * 	the {@link MathContext} controlling calculation precision and rounding
	 * @param trigonometricMode
	 * 	the mode specifying whether the angle {@code theta} is in degrees or radians
	 * @param locale
	 * 	the {@link Locale} for number formatting during intermediate calculations
	 *
	 * @return a {@link BigNumberCoordinate} representing the Cartesian coordinates (x, y)
	 */
	public static BigNumberCoordinate polarToCartesianCoordinates(@NonNull final BigNumber r, @NonNull final BigNumber theta, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		BigNumber x = r.multiply(theta.cos(mathContext, trigonometricMode, locale));
		BigNumber y = r.multiply(theta.sin(mathContext, trigonometricMode, locale));
		return new BigNumberCoordinate(x, y, CoordinateType.CARTESIAN).trim();
	}

	/**
	 * Converts Cartesian coordinates (x, y) to polar coordinates (r, θ).
	 * <p>
	 * Calculates the radius r and angle θ (in degrees):
	 * <pre>
	 *     r = √(x² + y²)
	 *     θ = atan2(y, x) (converted to degrees)
	 * </pre>
	 * The angle θ is returned in degrees for consistency and ease of use.
	 *
	 * @param x
	 * 	the x-coordinate in a Cartesian system
	 * @param y
	 * 	the y-coordinate in a Cartesian system
	 * @param mathContext
	 * 	the {@link MathContext} controlling precision and rounding for calculations
	 * @param locale
	 * 	the {@link Locale} for formatting intermediate {@link BigNumber} values
	 *
	 * @return a {@link BigNumberCoordinate} representing the polar coordinates (r, θ in degrees)
	 */
	public static BigNumberCoordinate cartesianToPolarCoordinates(@NonNull final BigNumber x, @NonNull final BigNumber y, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		BigNumber r = x.power(BigNumbers.TWO, mathContext, locale)
			              .add(y.power(BigNumbers.TWO, mathContext, locale))
			              .squareRoot(mathContext, locale);
		BigNumber theta = y.atan2(x, mathContext, locale);
		BigNumber thetaDeg = theta.toDegrees();

		return new BigNumberCoordinate(r, thetaDeg, CoordinateType.POLAR).trim();
	}

}
