package com.mlprograms.justmath.bignumber.internal.math;

import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ONE_HUNDRED;

public class PercentageMath {

	public static BigNumber nPercentFromM(BigNumber n, BigNumber m, MathContext mathContext) {
		return m.multiply(n.divide(ONE_HUNDRED, mathContext));
	}

	public static BigNumber mIsXPercentOfN(BigNumber n, BigNumber m, MathContext mathContext) {
		return m.divide(n, mathContext).multiply(ONE_HUNDRED);
	}

}
