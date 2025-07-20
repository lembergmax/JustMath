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
import com.mlprograms.justmath.bignumber.BigNumbers;
import lombok.NonNull;

import java.math.MathContext;

/**
 * Provides implementations of special functions from advanced mathematics,
 * including the Gamma and Beta functions.
 * <p>
 * These functions extend and generalize factorials and combinatorics,
 * and are widely used in fields such as calculus, number theory,
 * probability theory, and statistics.
 */
public class SpecialFunctionMath {

	/**
	 * Computes the Gamma function Γ(x) using the Lanczos approximation.
	 * <p>
	 * The Gamma function is a generalization of the factorial function to real and complex numbers.
	 * For all positive integers n, the identity holds:
	 * <pre>
	 *     Γ(n) = (n - 1)!
	 * </pre>
	 * More generally, for real and complex values (excluding non-positive integers),
	 * the function is defined via the improper integral:
	 * <pre>
	 *     Γ(x) = ∫₀^∞ t^(x-1) · e^(-t) dt
	 * </pre>
	 * The Gamma function is undefined for non-positive integers due to the presence of simple poles.
	 * <p>
	 * This implementation uses the Lanczos approximation, which is a numerically stable and accurate
	 * approximation suitable for arbitrary-precision arithmetic. For arguments with {@code x < 0},
	 * the reflection formula is applied:
	 * <pre>
	 *     Γ(x) = π / (sin(πx) · Γ(1 - x))
	 * </pre>
	 * which allows evaluation for negative non-integer arguments.
	 *
	 * <h3>Mathematical Properties:</h3>
	 * <ul>
	 *     <li>Γ(x+1) = x · Γ(x)</li>
	 *     <li>Γ(1) = 1, Γ(1/2) = √π</li>
	 *     <li>The function grows rapidly for large x (super-exponential growth).</li>
	 *     <li>The logarithm of Γ(x) is often used for numerical stability in applications.</li>
	 * </ul>
	 *
	 * @param x
	 * 	The input value for which to compute the Gamma function. Must not be a non-positive integer.
	 * @param mathContext
	 * 	The precision and rounding settings to use for the calculation.
	 *
	 * @return The evaluated Gamma function Γ(x) as a {@link BigNumber}.
	 *
	 * @throws ArithmeticException
	 * 	If {@code x} is a non-positive integer, where Γ(x) is undefined.
	 */
	public static BigNumber gamma(@NonNull final BigNumber x, @NonNull final MathContext mathContext) {
		BigNumber xClone = x.clone();

		if (xClone.isInteger() && xClone.isLessThanOrEqualTo(BigNumbers.ZERO)) {
			throw new ArithmeticException("Gamma function is undefined for non-positive integers");
		}

		// return xClone.subtract(BigNumbers.ONE).factorial(mathContext);
		return new BigNumber(BigDecimalMath.gamma(x.toBigDecimal(), mathContext).toPlainString());
	}

	/**
	 * Computes the Beta function B(x, y) using its definition in terms of the Gamma function.
	 * <p>
	 * The Beta function, also known as Euler's integral of the first kind, is defined as:
	 * <pre>
	 *     B(x, y) = ∫₀¹ t^(x-1) · (1 - t)^(y-1) dt
	 * </pre>
	 * for real numbers {@code x > 0} and {@code y > 0}.
	 * <p>
	 * Alternatively, it is related to the Gamma function by the identity:
	 * <pre>
	 *     B(x, y) = Γ(x) · Γ(y) / Γ(x + y)
	 * </pre>
	 * which is used in this implementation for computational accuracy and speed.
	 * <p>
	 * The Beta function appears in:
	 * <ul>
	 *     <li>Combinatorics and binomial integrals</li>
	 *     <li>Probability distributions such as the Beta and F-distributions</li>
	 *     <li>Normalization constants in Bayesian statistics</li>
	 * </ul>
	 *
	 * @param x
	 * 	The first argument of the Beta function (must be > 0).
	 * @param y
	 * 	The second argument of the Beta function (must be > 0).
	 * @param mathContext
	 * 	The precision and rounding mode used for all intermediate computations.
	 *
	 * @return The evaluated Beta function B(x, y) as a {@link BigNumber}.
	 *
	 * @throws ArithmeticException
	 * 	If x or y are ≤ 0 or lead to undefined Γ evaluations.
	 */
	public static BigNumber beta(@NonNull final BigNumber x, @NonNull final BigNumber y, @NonNull final MathContext mathContext) {
		BigNumber xClone = x.clone();
		BigNumber yClone = y.clone();

		BigNumber gammaX = gamma(xClone, mathContext);
		BigNumber gammaY = gamma(yClone, mathContext);
		BigNumber gammaXY = gamma(xClone.add(yClone), mathContext);

		return gammaX.multiply(gammaY).divide(gammaXY, mathContext);
	}

}
