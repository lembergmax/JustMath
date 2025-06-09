package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.math.MathHelper.convertAngle;

public class TrigonometricMath {

	public static BigNumber sin(BigNumber angle, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);
		return new BigNumber(BigDecimalMath.sin(radians, mathContext).toPlainString(), locale);
	}

	public static BigNumber cos(BigNumber angle, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);
		return new BigNumber(BigDecimalMath.cos(radians, mathContext).toPlainString(), locale);
	}

	public static BigNumber tan(BigNumber angle, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);
		return new BigNumber(BigDecimalMath.tan(radians, mathContext).toPlainString(), locale);
	}

	public static BigNumber cot(BigNumber angle, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigDecimal radians = convertAngle(angle, mathContext, trigonometricMode, locale);
		return new BigNumber(BigDecimalMath.cot(radians, mathContext).toPlainString(), locale);
	}

}
