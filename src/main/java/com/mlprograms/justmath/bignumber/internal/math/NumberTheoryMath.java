package com.mlprograms.justmath.bignumber.internal.math;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ZERO;

public class NumberTheoryMath {

	public static BigNumber gcd(BigNumber a, BigNumber b) {
		if (a.hasDecimals() || b.hasDecimals()) {
			throw new IllegalArgumentException("GCD requires integer values.");
		}

		a = a.abs();
		b = b.abs();

		while (b.isGreaterThan(ZERO)) {
			BigNumber temp = b;
			b = a.modulo(b);
			a = temp;
		}
		return a;
	}

	public static BigNumber lcm(BigNumber a, BigNumber b, MathContext mathContext) {
		if (a.hasDecimals() || b.hasDecimals()) {
			throw new IllegalArgumentException("LCM requires integer values.");
		}

		BigNumber product = a.multiply(b).abs();
		BigNumber divisor = gcd(a, b);

		return product.divide(divisor, mathContext);
	}

}
