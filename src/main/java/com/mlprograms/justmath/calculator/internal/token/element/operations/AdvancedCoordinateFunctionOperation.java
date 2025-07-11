package com.mlprograms.justmath.calculator.internal.token.element.operations;


import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.MathContext;
import java.util.Locale;

@FunctionalInterface
public interface AdvancedCoordinateFunctionOperation {

	BigNumberCoordinate apply(BigNumber a, BigNumber b, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale);

}
