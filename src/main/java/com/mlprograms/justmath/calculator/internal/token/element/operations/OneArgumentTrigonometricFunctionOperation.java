package com.mlprograms.justmath.calculator.internal.token.element.operations;


import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.MathContext;
import java.util.Locale;

@FunctionalInterface
public interface OneArgumentTrigonometricFunctionOperation {

	BigNumber apply(BigNumber a, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale);

}
