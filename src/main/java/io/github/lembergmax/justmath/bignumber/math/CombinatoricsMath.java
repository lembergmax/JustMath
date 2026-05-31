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

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import lombok.NonNull;

/**
 * Provides combinatorial mathematical operations on {@link BigNumber} instances,
 * specifically computing combinations and permutations with high precision.
 * <p>
 * This class enforces integer inputs and validates arguments to ensure
 * mathematically correct results for combinatorics.
 */
public final class CombinatoricsMath {

	private CombinatoricsMath() {
		// Utility class — never instantiated.
	}


	/**
	 * Calculates the number of combinations (n choose k), denoted as C(n, k),
	 * which is the count of ways to choose {@code k} items from {@code n} items without regard to order.
	 * <p>
	 * The formula used is:
	 * <pre>
	 *     C(n, k) = n! / (k! * (n-k)!)
	 * </pre>
	 * but computed efficiently via a multiplicative approach to avoid intermediate factorial computation.
	 *
	 * @param n
	 * 	the total number of items (must be a non-negative integer)
	 * @param k
	 * 	the number of items to choose (must be a non-negative integer, k ≤ n)
	 * @param mathContext
	 * 	the {@link MathContext} to control precision and rounding during division operations
	 *
	 * @return the number of combinations C(n, k) as a {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code n} or {@code k} are not integers, or if {@code k > n}
	 */
	public static BigNumber combination(@NonNull final BigNumber n, @NonNull final BigNumber k, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		if (n.hasDecimals() || k.hasDecimals()) {
			throw new IllegalArgumentException("Combination requires integer values for both n and k.");
		}

		if (n.isNegative() || k.isNegative()) {
			// Without this guard a negative k slips past the symmetry logic with a negative iteration
			// count, the loop never runs and C(5, -3) wrongly returns 1 instead of a domain error.
			throw new IllegalArgumentException("Combination requires non-negative integer values for both n and k.");
		}

		if (k.compareTo(n) > 0) {
			throw new IllegalArgumentException("Cannot calculate combinations: k cannot be greater than n.");
		}

		if (k.isEqualTo(ZERO) || k.isEqualTo(n)) {
			// Fresh instance, never the shared constant: the caller may mutate the result.
			return new BigNumber("1", locale);
		}

		// Use symmetry property: C(n, k) = C(n, n-k). Choosing the smaller of the two values
		// halves the iteration count and keeps the divisor sequence in the int range, which
		// makes the primitive index loop below safe.
		final BigNumber effectiveK = k.min(n.subtract(k));
		if (effectiveK.isGreaterThan(BigNumber.valueOf(Integer.MAX_VALUE))) {
			// Practically unreachable — a binomial with k > 2 billion would produce a result with
			// hundreds of millions of digits — but guard against silent {@link BigDecimal#intValue}
			// wrap-around just in case a caller hands us pathological inputs.
			throw new IllegalArgumentException("combination is not supported for k > Integer.MAX_VALUE");
		}
		final int iterationCount = effectiveK.intValue();

		// Previously this loop ran on {@link BigNumber} counters and allocated several throwaway
		// instances per step (i.add(ONE) twice, n.subtract(i) once). Replacing the counter with a
		// primitive {@code int} eliminates ~3·k allocations and the corresponding string-based
		// arithmetic — the only BigNumber math that remains is the actual product update.
		// Start from a fresh instance, never the shared ONE constant: the trailing product.trim()
		// would otherwise trim the global constant in place.
		BigNumber product = new BigNumber("1", locale);
		for (int i = 0; i < iterationCount; i++) {
			final BigNumber iAsBigNumber = BigNumber.valueOf(i);
			final BigNumber divisor = BigNumber.valueOf(i + 1L);
			product = product.multiply(n.subtract(iAsBigNumber), locale).divide(divisor, mathContext);
		}

		return new BigNumber(product.trim());
	}

	/**
	 * Calculates the number of permutations of {@code k} items selected from {@code n} items,
	 * denoted as P(n, k), which counts the number of ordered arrangements.
	 * <p>
	 * The formula used is:
	 * <pre>
	 *     P(n, k) = n! / (n - k)!
	 * </pre>
	 *
	 * @param n
	 * 	the total number of items (must be a non-negative integer)
	 * @param k
	 * 	the number of items to arrange (must be a non-negative integer, k ≤ n)
	 * @param mathContext
	 * 	the {@link MathContext} defining precision and rounding for internal factorial divisions
	 * @param locale
	 * 	the {@link Locale} to apply when constructing intermediate {@link BigNumber} results (for formatting)
	 *
	 * @return the number of permutations P(n, k) as a {@link BigNumber}
	 *
	 * @throws IllegalArgumentException
	 * 	if {@code n} or {@code k} are not integers, or if {@code k > n}
	 */
	public static BigNumber permutation(@NonNull final BigNumber n, @NonNull final BigNumber k, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		MathUtils.checkMathContext(mathContext);

		if (n.hasDecimals() || k.hasDecimals()) {
			throw new IllegalArgumentException("Permutations requires integer values for both n and k.");
		}

		if (n.isNegative() || k.isNegative()) {
			// A negative k yields P(n, -1) = n!/(n+1)! = 1/(n+1) — a finite but meaningless value;
			// reject it as a domain error instead.
			throw new IllegalArgumentException("Permutations requires non-negative integer values for both n and k.");
		}

		if (k.compareTo(n) > 0) {
			throw new IllegalArgumentException("Cannot calculate permutations: k cannot be greater than n.");
		}

		BigNumber nFactorial = n.factorial(mathContext, locale);
		BigNumber nMinusKFactorial = n.subtract(k).factorial(mathContext, locale);
		return new BigNumber(nFactorial.divide(nMinusKFactorial, mathContext, locale).trim());
	}

}
