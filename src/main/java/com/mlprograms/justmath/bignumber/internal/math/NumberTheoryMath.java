package com.mlprograms.justmath.bignumber.internal.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.MathContext;

import static com.mlprograms.justmath.bignumber.internal.BigNumberValues.ZERO;

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
	public static BigNumber gcd(@NonNull final BigNumber a, @NonNull final BigNumber b) {
		if (a.hasDecimals() || b.hasDecimals()) {
			throw new IllegalArgumentException("GCD requires integer values.");
		}

		BigNumber aClone = a.clone().abs();
		BigNumber bClone = b.clone().abs();

		while (bClone.isGreaterThan(ZERO)) {
			BigNumber temp = bClone;
			bClone = aClone.modulo(bClone);
			aClone = temp;
		}
		return aClone.trim();
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
	public static BigNumber lcm(@NonNull final BigNumber a, @NonNull final BigNumber b, @NonNull final MathContext mathContext) {
		if (a.hasDecimals() || b.hasDecimals()) {
			throw new IllegalArgumentException("LCM requires integer values.");
		}

		BigNumber product = a.multiply(b).abs();
		BigNumber divisor = gcd(a, b);

		return product.divide(divisor, mathContext).trim();
	}

}
