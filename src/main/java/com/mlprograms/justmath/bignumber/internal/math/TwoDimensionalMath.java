package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;
import java.util.Locale;

public class TwoDimensionalMath {

	public static BigNumber atan2(BigNumber y, BigNumber x, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.atan2(y.toBigDecimal(), x.toBigDecimal(), mathContext).toPlainString(), locale);
	}

}
