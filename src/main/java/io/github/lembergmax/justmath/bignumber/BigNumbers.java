package io.github.lembergmax.justmath.bignumber;

import java.math.MathContext;
import java.util.Locale;

public class BigNumbers {

    public static final BigNumber ZERO = new BigNumber("0", Locale.US);

    public static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL64;

}
