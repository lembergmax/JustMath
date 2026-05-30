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

import static io.github.lembergmax.justmath.bignumber.BigNumbers.ZERO;

import java.math.MathContext;
import java.util.Locale;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import lombok.NonNull;

/**
 * Provides two-dimensional mathematical functions.
 */
public final class TwoDimensionalMath {

	private TwoDimensionalMath() {
		// Utility class — never instantiated.
	}


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
	 *   <li>{@code x} and {@code y} may not both be zero — the angle is undefined at the origin (0, 0).
	 *       All other combinations (including {@code y = 0} or {@code x = 0} on its own) are valid:
	 *       e.g. {@code atan2(5, 0) = π/2}, {@code atan2(0, -5) = π}.</li>
	 * </ul>
	 *
	 * @param y           the y-coordinate
	 * @param x           the x-coordinate
	 * @param mathContext the {@link MathContext} controlling precision and rounding
	 * @param locale      the {@link Locale} used for number formatting
	 * @return the angle θ in radians as a {@link BigNumber}, in the range {@code (-π, π]}
	 * @throws IllegalArgumentException if {@code x} and {@code y} are both zero
	 */
	public static BigNumber atan2(@NonNull final BigNumber y, @NonNull final BigNumber x, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		// Only the origin is undefined; axis points (x=0 or y=0 alone) are well-defined and map
		// to multiples of π/2. The previous check rejected three quarters of the unit circle.
		if (x.isEqualTo(ZERO) && y.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("atan2 is undefined at the origin (0, 0)");
		}

		return new BigNumber(BigDecimalMath.atan2(y.toBigDecimal(), x.toBigDecimal(), mathContext).toPlainString(), locale).trim();
	}

}
