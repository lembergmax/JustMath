package com.mlprograms.justmath.bignumber.internal.calculator;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;
import java.util.Locale;

public class HyperbolicTrigonometricMath {

	public static BigNumber sinh(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.sinh(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber cosh(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.cosh(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber tanh(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.tanh(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber coth(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.coth(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

}
