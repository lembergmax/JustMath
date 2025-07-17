package com.mlprograms.justmath.calculator.internal.expressionelements.operations;


import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.MathContext;
import java.util.Locale;

@FunctionalInterface
public interface ThreeArgumentSummationFunctionOperation {

	BigNumber apply(BigNumber a, BigNumber b, String c, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale);

}
