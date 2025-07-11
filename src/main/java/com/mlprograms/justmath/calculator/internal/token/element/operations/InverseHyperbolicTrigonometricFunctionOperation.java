package com.mlprograms.justmath.calculator.internal.token.element.operations;


import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;
import java.util.Locale;

@FunctionalInterface
public interface InverseHyperbolicTrigonometricFunctionOperation {

	BigNumber apply(BigNumber a, MathContext mathContext, Locale locale);

}
