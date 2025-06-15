package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ZERO;

/**
 * Provides two-dimensional mathematical functions.
 */
public class TwoDimensionalMath {

	/**
	 * Computes the angle θ between the positive x-axis and the point (x, y).
	 * <p>
	 * This method calculates the arctangent of y/x considering the signs of both arguments to determine
	 * the correct quadrant of the angle. The result is in radians, ranging from -π to π.
	 * <p>
	 * Mathematically, atan2(y, x) is defined as:
	 * <pre>
	 * θ = atan2(y, x)
	 * </pre>
	 * where θ is the angle between the positive x-axis and the point (x, y) in the Cartesian plane.
	 *
	 * @param y
	 * 	the y-coordinate
	 * @param x
	 * 	the x-coordinate
	 * @param mathContext
	 * 	the {@link MathContext} controlling precision and rounding
	 * @param locale
	 * 	the locale used for number formatting
	 *
	 * @return the angle θ in radians as a {@link BigNumber}, within the range [-π, π]
	 */
	public static BigNumber atan2(@NonNull final BigNumber y, @NonNull final BigNumber x, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (x.isEqualTo(ZERO) || y.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("x or y cannot be zero");
		}

		return new BigNumber(BigDecimalMath.atan2(y.toBigDecimal(), x.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

}
