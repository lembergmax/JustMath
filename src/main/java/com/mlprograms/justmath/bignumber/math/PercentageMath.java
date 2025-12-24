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

import static com.mlprograms.justmath.bignumber.BigNumbers.ONE_HUNDRED;

/**
 * Utility class for percentage calculations using {@link BigNumber}.
 */
public class PercentageMath {

	/**
	 * Calculates n percent of m.
	 * <p>
	 * Mathematically:
	 * <pre>
	 * result = m * (n / 100)
	 * </pre>
	 * Example: 20 percent of 50 is 50 * 0.20 = 10.
	 *
	 * @param n
	 * 	the percentage value (e.g., 20 for 20%)
	 * @param m
	 * 	the base value from which the percentage is taken
	 * @param mathContext
	 * 	the {@link MathContext} to control precision and rounding
	 *
	 * @return n percent of m
	 */
	public static BigNumber nPercentFromM(@NonNull final BigNumber n, @NonNull final BigNumber m, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		return new BigNumber(m.multiply(n.divide(ONE_HUNDRED, mathContext, locale), locale).trim());
	}

	/**
	 * Calculates what percentage m is of n.
	 * <p>
	 * Mathematically:
	 * <pre>
	 * result = (m / n) * 100
	 * </pre>
	 * Example: 25 is 50% of 50, since (25 / 50) * 100 = 50.
	 *
	 * @param n
	 * 	the reference value (100%)
	 * @param m
	 * 	the part value whose percentage of n is calculated
	 * @param mathContext
	 * 	the {@link MathContext} to control precision and rounding
	 *
	 * @return the percentage that m is of n
	 */
	public static BigNumber xIsNPercentOfN(@NonNull final BigNumber n, @NonNull final BigNumber m, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		return new BigNumber(n.divide(m, mathContext, locale).multiply(ONE_HUNDRED, locale).trim());
	}

}
