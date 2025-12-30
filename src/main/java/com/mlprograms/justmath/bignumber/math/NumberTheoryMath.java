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

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.math.utils.MathUtils;
import lombok.NonNull;

import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;

/**
 * Utility class providing number theory operations on {@link BigNumber} integers.
 * <p>
 * Implements the calculation of the greatest common divisor (GCD) and least common multiple (LCM).
 */
public class NumberTheoryMath {

	/**
	 * Computes the greatest common divisor (GCD) of two integers a and b using the Euclidean algorithm.
	 * <p>
	 * The GCD of two integers is the largest positive integer that divides both without leaving a remainder.
	 * Formally:
	 * <pre>
	 * gcd(a, b) = max { d ∈ ℕ | d divides a and d divides b }
	 * </pre>
	 * This method requires that both inputs be integers (no decimal part).
	 *
	 * @param a
	 * 	first integer operand
	 * @param b
	 * 	second integer operand
	 *
	 * @return the greatest common divisor of |a| and |b|
	 *
	 * @throws IllegalArgumentException
	 * 	if a or b is not an integer
	 */
	public static BigNumber gcd(@NonNull final BigNumber a, @NonNull final BigNumber b, @NonNull final Locale locale) {
		if (a.hasDecimals() || b.hasDecimals()) {
			throw new IllegalArgumentException("GCD requires integer values.");
		}

		BigNumber aClone = a.clone().abs();
		BigNumber bClone = b.clone().abs();

		while (bClone.isGreaterThan(ZERO)) {
			BigNumber temp = bClone;
			bClone = aClone.modulo(bClone, locale);
			aClone = temp;
		}
		return new BigNumber(aClone.trim());
	}

	/**
	 * Computes the least common multiple (LCM) of two integers a and b.
	 * <p>
	 * The LCM is the smallest positive integer that is a multiple of both a and b.
	 * It can be computed via the formula:
	 * <pre>
	 * lcm(a, b) = |a * b| / gcd(a, b)
	 * </pre>
	 * This method requires that both inputs be integers (no decimal part).
	 *
	 * @param a
	 * 	first integer operand
	 * @param b
	 * 	second integer operand
	 * @param mathContext
	 * 	the {@link MathContext} specifying precision and rounding mode for division
	 *
	 * @return the least common multiple of |a| and |b|
	 *
	 * @throws IllegalArgumentException
	 * 	if a or b is not an integer
	 */
	public static BigNumber lcm(@NonNull final BigNumber a, @NonNull final BigNumber b, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		if (a.hasDecimals() || b.hasDecimals()) {
			throw new IllegalArgumentException("LCM requires integer values.");
		}

		BigNumber product = a.multiply(b, locale).abs();
		BigNumber divisor = gcd(a, b, locale);

		return new BigNumber(product.divide(divisor, mathContext, locale).trim());
	}

}
