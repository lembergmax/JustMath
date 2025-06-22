package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.calculator.CalculatorEngine;

import java.math.MathContext;

public class BigNumberValues {

	public static final int DEFAULT_DIVISION_PRECISION = 100;
	public static final MathContext DEFAULT_MATH_CONTEXT = CalculatorEngine.getDefaultMathContext(DEFAULT_DIVISION_PRECISION);

	public static final BigNumber NEGATIVE_ONE = new BigNumber("-1", DEFAULT_MATH_CONTEXT);
	public static final BigNumber ZERO = new BigNumber("0", DEFAULT_MATH_CONTEXT);
	public static final BigNumber ONE = new BigNumber("1", DEFAULT_MATH_CONTEXT);
	public static final BigNumber TWO = new BigNumber("2", DEFAULT_MATH_CONTEXT);
	public static final BigNumber THREE = new BigNumber("3", DEFAULT_MATH_CONTEXT);
	public static final BigNumber ONE_HUNDRED = new BigNumber("100", DEFAULT_MATH_CONTEXT);
	public static final BigNumber ONE_HUNDRED_EIGHTY = new BigNumber("180", DEFAULT_MATH_CONTEXT);

}
