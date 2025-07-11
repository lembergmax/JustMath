package com.mlprograms.justmath.calculator.internal.token.element.operations;


import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;
import java.util.Locale;

@FunctionalInterface
public interface TwoArgumentFunctionOperation {

	BigNumber apply(BigNumber a, BigNumber b, MathContext mathContext, Locale locale);

}
