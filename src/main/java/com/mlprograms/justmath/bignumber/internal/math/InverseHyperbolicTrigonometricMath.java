package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;
import java.util.Locale;

public class InverseHyperbolicTrigonometricMath {

	public static BigNumber asinh(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.asinh(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber acosh(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.acosh(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber atanh(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.atanh(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber acoth(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.acoth(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

}
