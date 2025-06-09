package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

public class LogarithmicMath {

	public static BigNumber log2(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.log2(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber log10(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.log10(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber ln(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.log(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber logBase(BigNumber number, BigNumber base, MathContext mathContext, Locale locale) {
		if (number.isNegative() || number.isEqualTo(BigNumbers.ZERO)) {
			throw new IllegalArgumentException("Number must be positive and non-zero.");
		}

		if (base.isNegative() || base.isEqualTo(BigNumbers.ZERO) || base.isEqualTo(BigNumbers.ONE)) {
			throw new IllegalArgumentException("Base must be positive and not equal to 1.");
		}

		BigDecimal lnNumber = BigDecimalMath.log(number.toBigDecimal(), mathContext);
		BigDecimal lnBase = BigDecimalMath.log(base.toBigDecimal(), mathContext);
		return new BigNumber(lnNumber.divide(lnBase, mathContext).toPlainString(), locale);
	}

}
