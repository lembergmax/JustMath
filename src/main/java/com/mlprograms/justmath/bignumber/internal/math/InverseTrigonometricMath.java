package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.math.MathHelper.bigDecimalRadiansToDegrees;

public class InverseTrigonometricMath {

	public static BigNumber asin(BigNumber argument, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal result = BigDecimalMath.asin(argument.toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	public static BigNumber acos(BigNumber argument, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal result = BigDecimalMath.acos(argument.toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	public static BigNumber atan(BigNumber argument, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal result = BigDecimalMath.atan(argument.toBigDecimal(), mathContext);
		if (trigonometricMode == TrigonometricMode.DEG) {
			result = bigDecimalRadiansToDegrees(result, mathContext, locale);
		}
		return new BigNumber(result.toPlainString(), locale);
	}

	public static BigNumber acot(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.acot(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

}
