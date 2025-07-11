package com.mlprograms.justmath.calculator.internal.token.element.operations;


import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;

import java.math.MathContext;
import java.util.Locale;

@FunctionalInterface
public interface CoordinateFunctionOperation {

	BigNumberCoordinate apply(BigNumber a, BigNumber b, MathContext mathContext, Locale locale);

}
