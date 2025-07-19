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

package com.mlprograms.justmath.bignumber;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import lombok.NonNull;

import java.math.BigInteger;
import java.math.MathContext;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class BigNumbers {

	/**
	 * Locale used for all calculations to ensure consistent number formatting and parsing.
	 */
	public static final Locale CALCULATION_LOCALE = Locale.US;

	public static final int DEFAULT_DIVISION_PRECISION = 100;
	public static final MathContext DEFAULT_MATH_CONTEXT = CalculatorEngine.getDefaultMathContext(DEFAULT_DIVISION_PRECISION);

	public static final BigNumber NEGATIVE_ONE = new BigNumber("-1", DEFAULT_MATH_CONTEXT);
	public static final BigNumber ZERO = new BigNumber("0", DEFAULT_MATH_CONTEXT);
	public static final BigNumber ONE = new BigNumber("1", DEFAULT_MATH_CONTEXT);
	public static final BigNumber TWO = new BigNumber("2", DEFAULT_MATH_CONTEXT);
	public static final BigNumber THREE = new BigNumber("3", DEFAULT_MATH_CONTEXT);
	public static final BigNumber ONE_HUNDRED = new BigNumber("100", DEFAULT_MATH_CONTEXT);
	public static final BigNumber ONE_HUNDRED_EIGHTY = new BigNumber("180", DEFAULT_MATH_CONTEXT);

	/**
	 * Generates a uniformly distributed random integer {@link BigNumber} within the range [min, max).
	 * <p>
	 * Mathematically: returns a value x such that {@code min ≤ x < max}, where x is an integer.
	 * <p>
	 * Both {@code min} and {@code max} must be exact integers (no decimal part), and {@code min < max}.
	 *
	 * @param min
	 * 	the inclusive lower bound (must be an integer)
	 * @param max
	 * 	the exclusive upper bound (must be an integer)
	 * @param locale
	 * 	* 	The {@link Locale} to use for the returned {@link BigNumber}, ensuring locale-specific formatting.
	 *
	 * @return a random {@link BigNumber} representing an integer in the range [min, max)
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code min} ≥ {@code max}, or if either value has decimal places
	 */
	public static BigNumber randomIntegerBigNumberInRange(@NonNull final BigNumber min, @NonNull final BigNumber max, @NonNull final Locale locale) {
		BigInteger minInt = min.toBigDecimal().toBigIntegerExact();
		BigInteger maxInt = max.add(ONE).toBigDecimal().toBigIntegerExact();

		if (minInt.compareTo(maxInt) >= 0) {
			throw new IllegalArgumentException("min must be less than max");
		}

		BigInteger range = maxInt.subtract(minInt);
		BigInteger randomInRange = new BigInteger(range.bitLength(), ThreadLocalRandom.current()).mod(range).add(minInt);

		return new BigNumber(randomInRange.toString(), locale);
	}

	/**
	 * Returns the mathematical constant e (Euler's number) with the specified precision,
	 * using the default calculation locale.
	 *
	 * @param mathContext
	 * 	the {@link MathContext} specifying the precision and rounding mode
	 *
	 * @return a {@link BigNumber} representing the value of e
	 */
	public static BigNumber e(@NonNull final MathContext mathContext) {
		return e(mathContext, CALCULATION_LOCALE);
	}

	/**
	 * Returns the mathematical constant e (Euler's number) with the specified precision.
	 * <p>
	 * Uses {@link BigDecimalMath#e(MathContext)} to compute the value of e to the desired precision.
	 *
	 * @param mathContext
	 * 	the {@link MathContext} specifying the precision and rounding mode
	 * @param locale
	 * 	The {@link Locale} to use for the returned {@link BigNumber}, ensuring locale-specific formatting.
	 *
	 * @return a {@link BigNumber} representing the value of e
	 */
	public static BigNumber e(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.e(mathContext).toPlainString(), locale, mathContext);
	}

	/**
	 * Returns the mathematical constant π (pi) with the specified precision,
	 * using the default calculation locale.
	 *
	 * @param mathContext
	 * 	the {@link MathContext} specifying the precision and rounding mode
	 *
	 * @return a {@link BigNumber} representing the value of pi
	 */
	public static BigNumber pi(@NonNull final MathContext mathContext) {
		return pi(mathContext, CALCULATION_LOCALE);
	}

	/**
	 * Returns the mathematical constant π (pi) with the specified precision.
	 * <p>
	 * Uses {@link BigDecimalMath#pi(MathContext)} to compute the value of pi to the desired precision.
	 *
	 * @param mathContext
	 * 	the {@link MathContext} specifying the precision and rounding mode
	 * @param locale
	 * 	The {@link Locale} to use for the returned {@link BigNumber}, ensuring locale-specific formatting.
	 *
	 * @return a {@link BigNumber} representing the value of pi
	 */
	public static BigNumber pi(@NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.pi(mathContext).toPlainString(), locale, mathContext);
	}

}
