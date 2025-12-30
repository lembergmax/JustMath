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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.*;

/**
 * Provides mathematical operations for calculating roots of numbers (radicals).
 */
public class RadicalMath {

	/**
	 * Calculates the square root of the given radicand.
	 * <p>
	 * Mathematically:
	 * <pre>
	 * result = √radicand = radicand^(1/2)
	 * </pre>
	 * The square root is the value which, when multiplied by itself, gives the radicand.
	 *
	 * @param radicand
	 * 	the number to find the square root of, must be non-negative
	 * @param mathContext
	 * 	the {@link MathContext} to control precision and rounding
	 * @param locale
	 * 	the {@link Locale} used for number formatting
	 *
	 * @return the square root of the radicand
	 */
	public static BigNumber squareRoot(@NonNull final BigNumber radicand, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		return new BigNumber(nthRoot(radicand, TWO, mathContext, locale));
	}

	/**
	 * Calculates the cubic root (third root) of the given radicand.
	 * <p>
	 * Mathematically:
	 * <pre>
	 * result = ∛radicand = radicand^(1/3)
	 * </pre>
	 * The cubic root is the number which, when raised to the power 3, equals the radicand.
	 *
	 * @param radicand
	 * 	the number to find the cubic root of, can be negative or positive
	 * @param mathContext
	 * 	the {@link MathContext} to control precision and rounding
	 * @param locale
	 * 	the {@link Locale} used for number formatting
	 *
	 * @return the cubic root of the radicand
	 */
	public static BigNumber cubicRoot(@NonNull final BigNumber radicand, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		return new BigNumber(nthRoot(radicand, THREE, mathContext, locale));
	}

	/**
	 * Calculates the n-th root of a given radicand.
	 * <p>
	 * Handles negative indices (returns reciprocal of positive root),
	 * checks for invalid cases (zero index, even root of negative number),
	 * and uses BigDecimalMath for root calculation.
	 * <ul>
	 *   <li>If the index is zero, throws IllegalArgumentException.</li>
	 *   <li>If the index is negative, computes the positive root and returns its reciprocal.</li>
	 *   <li>If the radicand is negative and the root is even, throws IllegalArgumentException.</li>
	 *   <li>If the radicand is negative and the root is odd, returns the negative root.</li>
	 *   <li>Otherwise, returns the n-th root of the radicand.</li>
	 * </ul>
	 *
	 * @param radicand
	 * 	the number to find the root of
	 * @param index
	 * 	the degree of the root (n)
	 * @param mathContext
	 * 	the MathContext to control precision and rounding
	 * @param locale
	 * 	the Locale used for number formatting
	 *
	 * @return the n-th root of the radicand as a BigNumber
	 *
	 * @throws IllegalArgumentException
	 * 	if the index is zero or if even root of a negative number is requested
	 */
	public static BigNumber nthRoot(@NonNull final BigNumber radicand, @NonNull final BigNumber index, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		if (index.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("Index must not be zero");
		}

		if (index.isNegative()) {
			BigNumber positiveIndex = index.negate();
			BigNumber positiveRoot = nthRoot(radicand, positiveIndex, mathContext, locale);
			return ONE.divide(positiveRoot, mathContext, locale).trim();
		}

		boolean isEvenRoot = index.isInteger() && index.toBigDecimal().remainder(BigDecimal.valueOf(2)).compareTo(BigDecimal.ZERO) == 0;
		boolean radicandIsNegative = radicand.isNegative();

		if (radicandIsNegative && isEvenRoot) {
			throw new IllegalArgumentException("Even root of a negative number is not a real number");
		}

		if (radicandIsNegative) {
			BigDecimal absValue = radicand.toBigDecimal().negate();  // |-x|
			BigDecimal root = BigDecimalMath.root(absValue, index.toBigDecimal(), mathContext);
			return new BigNumber(root.negate().toPlainString(), locale, mathContext).trim();
		}

		BigDecimal result = BigDecimalMath.root(radicand.toBigDecimal(), index.toBigDecimal(), mathContext);
		return new BigNumber(result.toPlainString(), locale, mathContext).trim();
	}

}
