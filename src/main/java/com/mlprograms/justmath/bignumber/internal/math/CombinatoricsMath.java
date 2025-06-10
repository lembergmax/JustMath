package com.mlprograms.justmath.bignumber.internal.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;

import java.math.MathContext;
import java.util.Locale;

/**
 * Provides combinatorial mathematical operations on {@link BigNumber} instances,
 * specifically computing combinations and permutations with high precision.
 * <p>
 * This class enforces integer inputs and validates arguments to ensure
 * mathematically correct results for combinatorics.
 */
public class CombinatoricsMath {

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
	public static BigNumber combination(BigNumber n, BigNumber k, MathContext mathContext) {
		if (n.hasDecimals() || k.hasDecimals()) {
			throw new IllegalArgumentException("Combination requires integer values for both n and k.");
		}

		if (k.compareTo(n) > 0) {
			throw new IllegalArgumentException("Cannot calculate combinations: k cannot be greater than n.");
		}

		if (k.isEqualTo(BigNumbers.ZERO) || k.isEqualTo(n)) {
			return BigNumbers.ONE;
		}

		// Use symmetry property: C(n, k) = C(n, n-k)
		k = k.min(n.subtract(k));
		BigNumber c = BigNumbers.ONE;
		for (BigNumber i = BigNumbers.ZERO; i.isLessThan(k); i = i.add(BigNumbers.ONE)) {
			c = c.multiply(n.subtract(i)).divide(i.add(BigNumbers.ONE), mathContext);
		}

		return c;
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
	public static BigNumber permutation(BigNumber n, BigNumber k, MathContext mathContext, Locale locale) {
		if (n.hasDecimals() || k.hasDecimals()) {
			throw new IllegalArgumentException("Permutations requires integer values for both n and k.");
		}

		if (k.compareTo(n) > 0) {
			throw new IllegalArgumentException("Cannot calculate permutations: k cannot be greater than n.");
		}

		BigNumber nFactorial = n.factorial(mathContext, locale);
		BigNumber nMinusKFactorial = n.subtract(k).factorial(mathContext, locale);
		return nFactorial.divide(nMinusKFactorial, mathContext);
	}

}
