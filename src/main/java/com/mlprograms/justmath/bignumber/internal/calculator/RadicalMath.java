package com.mlprograms.justmath.bignumber.internal.calculator;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.THREE;
import static com.mlprograms.justmath.bignumber.internal.BigNumbers.TWO;

public class RadicalMath {

	public static BigNumber squareRoot(BigNumber radicand, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.root(radicand.toBigDecimal(), TWO.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber cubicRoot(BigNumber radicand, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.root(radicand.toBigDecimal(), THREE.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber nthRoot(BigNumber radicand, BigNumber index, MathContext mathContext, Locale locale) {
		if (radicand.isNegative() || index.isNegative()) {
			throw new IllegalArgumentException("Cannot calculate nth root with negative index.");
		}
		return new BigNumber(BigDecimalMath.root(radicand.toBigDecimal(), index.toBigDecimal(), mathContext).toPlainString(), locale);
	}

}
