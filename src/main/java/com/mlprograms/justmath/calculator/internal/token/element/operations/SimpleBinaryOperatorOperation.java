package com.mlprograms.justmath.calculator.internal.token.element.operations;


import com.mlprograms.justmath.bignumber.BigNumber;

import java.util.Locale;

@FunctionalInterface
public interface SimpleBinaryOperatorOperation {

	BigNumber apply(BigNumber a, BigNumber b, Locale locale);

}
