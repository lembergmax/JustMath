package com.mlprograms.justmath.bignumber.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.math.utils.MathUtils;
import lombok.NonNull;

import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;

/**
 * Provides two-dimensional mathematical functions.
 */
public class TwoDimensionalMath {

	/**
	 * Computes the angle θ between the positive x-axis and the point (x, y).
	 * <p>
	 * This method calculates the arctangent of y/x while taking into account the signs of both arguments
	 * to determine the correct quadrant of the angle. The result is returned in radians and lies within
	 * the interval [-π, π].
	 * <p>
	 * Mathematically, this function represents:
	 * <pre>
	 *     θ = atan2(y, x)
	 * </pre>
	 * where θ is the angle between the vector (x, y) and the positive x-axis in the Cartesian plane.
	 * <p>
	 * <strong>Restrictions:</strong>
	 * <ul>
	 *   <li>Neither {@code x} nor {@code y} may be zero. The angle is undefined at the origin (0,0).</li>
	 * </ul>
	 *
	 * @param y
	 * 	the y-coordinate (must not be zero)
	 * @param x
	 * 	the x-coordinate (must not be zero)
	 * @param mathContext
	 * 	the {@link MathContext} controlling precision and rounding
	 * @param locale
	 * 	the {@link Locale} used for number formatting
	 *
	 * @return the angle θ in radians as a {@link BigNumber}, in the range [-π, π]
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code x} or {@code y} is zero (undefined result at origin)
	 */
	public static BigNumber atan2(@NonNull final BigNumber y, @NonNull final BigNumber x, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		if (x.isEqualTo(ZERO) || y.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("x or y cannot be zero");
		}

		return new BigNumber(BigDecimalMath.atan2(y.toBigDecimal(), x.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

}
