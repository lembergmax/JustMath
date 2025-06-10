package com.mlprograms.justmath.bignumber.internal.math;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ONE_HUNDRED;

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
	public static BigNumber nPercentFromM(BigNumber n, BigNumber m, MathContext mathContext) {
		return m.multiply(n.divide(ONE_HUNDRED, mathContext));
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
	public static BigNumber mIsXPercentOfN(BigNumber n, BigNumber m, MathContext mathContext) {
		return m.divide(n, mathContext).multiply(ONE_HUNDRED);
	}

}
