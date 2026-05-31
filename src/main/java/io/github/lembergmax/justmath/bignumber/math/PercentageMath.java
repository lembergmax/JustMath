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

import static io.github.lembergmax.justmath.bignumber.BigNumbers.ONE_HUNDRED;

import java.math.MathContext;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import lombok.NonNull;

/**
 * Utility class for percentage calculations using {@link BigNumber}.
 */
public final class PercentageMath {

	/** Non-instantiable utility class. */
	private PercentageMath() {
	}


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
	 * Calculates what percentage {@code part} is of {@code total}.
	 * <p>
	 * Mathematically:
	 * <pre>
	 * result = (part / total) * 100
	 * </pre>
	 * Example: 25 is 50% of 50, since {@code (25 / 50) * 100 = 50}.
	 *
	 * <p>Historical note: the parameters are named {@code part} and {@code total} for clarity.
	 * Earlier versions called them {@code n} and {@code m} with a JavaDoc that described the
	 * opposite semantics (n = reference, m = part) — the runtime behaviour, however, has always
	 * computed {@code part / total × 100} as documented above. The rename clarifies the contract
	 * without changing behaviour.
	 *
	 * @param part        the part value whose percentage of {@code total} is being computed
	 * @param total       the reference value (100%)
	 * @param mathContext the {@link MathContext} to control precision and rounding
	 * @param locale      the locale used for {@link BigNumber} formatting
	 * @return the percentage that {@code part} is of {@code total}
	 */
	public static BigNumber xIsNPercentOfN(@NonNull final BigNumber part, @NonNull final BigNumber total, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		return new BigNumber(part.divide(total, mathContext, locale).multiply(ONE_HUNDRED, locale).trim());
	}

}
