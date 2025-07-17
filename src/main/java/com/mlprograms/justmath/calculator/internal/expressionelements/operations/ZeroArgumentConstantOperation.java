package com.mlprograms.justmath.calculator.internal.expressionelements.operations;


import com.mlprograms.justmath.bignumber.BigNumber;

import java.math.MathContext;
import java.util.Locale;

@FunctionalInterface
public interface ZeroArgumentConstantOperation {

	BigNumber apply(MathContext mathContext, Locale locale);

}
