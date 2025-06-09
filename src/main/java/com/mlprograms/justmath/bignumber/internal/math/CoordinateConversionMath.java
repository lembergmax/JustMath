package com.mlprograms.justmath.bignumber.internal.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.MathContext;
import java.util.Locale;

public class CoordinateConversionMath {

	public static BigNumberCoordinate polarToCartesianCoordinates(BigNumber r, BigNumber theta, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		BigNumber x = r.multiply(theta.cos(mathContext, trigonometricMode, locale));
		BigNumber y = r.multiply(theta.sin(mathContext, trigonometricMode, locale));
		return new BigNumberCoordinate(x, y);
	}

	public static BigNumberCoordinate cartesianToPolarCoordinates(BigNumber x, BigNumber y, MathContext mathContext, Locale locale) {
		BigNumber r = x.power(BigNumbers.TWO, mathContext, locale).add(y.power(BigNumbers.TWO, mathContext, locale)).squareRoot(mathContext, locale);
		BigNumber theta = y.atan2(x, mathContext, locale);
		BigNumber thetaDeg = theta.toDegrees();

		return new BigNumberCoordinate(r, thetaDeg);
	}

}
