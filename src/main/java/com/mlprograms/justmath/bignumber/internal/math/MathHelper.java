package com.mlprograms.justmath.bignumber.internal.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.util.Values;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ONE_HUNDRED_EIGHTY;

public class MathHelper {

	public static BigDecimal convertAngle(BigNumber angle, MathContext context, TrigonometricMode trigonometricMode, Locale locale) {
		return (trigonometricMode == TrigonometricMode.DEG)
			       ? bigDecimalNumberToRadians(angle.toBigDecimal(), context, locale)
			       : angle.toBigDecimal();
	}

	public static BigDecimal bigDecimalRadiansToDegrees(BigDecimal radians, MathContext mathContext, Locale locale) {
		return new BigNumber(radians, locale).toDegrees(mathContext, locale).toBigDecimal();
	}

	public static BigDecimal bigDecimalNumberToRadians(BigDecimal degrees, MathContext mathContext, Locale locale) {
		return new BigNumber(degrees.multiply(Values.PI.toBigDecimal()).divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext), locale).toBigDecimal();
	}

}
