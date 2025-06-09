package com.mlprograms.justmath.bignumber.internal.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;

import java.math.MathContext;
import java.util.Locale;

public class CombinatoricsMath {

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

		k = k.min(n.subtract(k));
		BigNumber c = BigNumbers.ONE;
		for (BigNumber i = BigNumbers.ZERO; i.isLessThan(k); i = i.add(BigNumbers.ONE)) {
			c = c.multiply(n.subtract(i)).divide(i.add(BigNumbers.ONE), mathContext);
		}

		return c;
	}

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
