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
