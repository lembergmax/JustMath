package com.mlprograms.justmath.calculator.internal.token.expressionelements.operations;


import com.mlprograms.justmath.bignumber.BigNumber;

import java.util.Locale;

@FunctionalInterface
public interface SimpleTwoArgumentFunctionOperation {

	BigNumber apply(BigNumber a, BigNumber b, Locale locale);

}
