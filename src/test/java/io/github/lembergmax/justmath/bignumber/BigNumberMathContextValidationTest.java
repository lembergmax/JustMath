/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.lembergmax.justmath.bignumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.MathContext;
import java.util.function.Function;
import java.util.stream.Stream;

import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArgumentException;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArithmeticException;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every operation that takes a {@link MathContext} rejects a precision of zero and a precision above the limit with a
 * typed exception and the matching code, before it computes anything. The operations are called with valid arguments, so
 * the context is the only thing that can be wrong.
 */
class BigNumberMathContextValidationTest {

    private static final BigNumber HALF = new BigNumber("0.5");

    private static final BigNumber TWO = new BigNumber("2");

    private static final BigNumber THREE = new BigNumber("3");

    private static final BigNumber FIVE = new BigNumber("5");

    private static Arguments operation(final String name, final Function<MathContext, Object> operation) {
        return Arguments.of(name, operation);
    }

    static Stream<Arguments> operations() {
        return Stream.of(
                operation("divide", mathContext -> TWO.divide(THREE, mathContext)),
                operation("integer power", mathContext -> TWO.power(THREE, mathContext)),
                operation("fractional power", mathContext -> TWO.power(HALF, mathContext)),
                operation("factorial", mathContext -> FIVE.factorial(mathContext)),
                operation("exp", mathContext -> TWO.exp(mathContext)),
                operation("squareRoot", mathContext -> TWO.squareRoot(mathContext)),
                operation("cubicRoot", mathContext -> TWO.cubicRoot(mathContext)),
                operation("nthRoot", mathContext -> FIVE.nthRoot(THREE, mathContext)),
                operation("log2", mathContext -> FIVE.log2(mathContext)),
                operation("log10", mathContext -> FIVE.log10(mathContext)),
                operation("ln", mathContext -> FIVE.ln(mathContext)),
                operation("logBase", mathContext -> FIVE.logBase(TWO, mathContext)),
                operation("sin", mathContext -> HALF.sin(mathContext, TrigonometricMode.RAD)),
                operation("cos", mathContext -> HALF.cos(mathContext, TrigonometricMode.RAD)),
                operation("tan", mathContext -> HALF.tan(mathContext, TrigonometricMode.RAD)),
                operation("cot", mathContext -> HALF.cot(mathContext, TrigonometricMode.RAD)),
                operation("asin", mathContext -> HALF.asin(mathContext, TrigonometricMode.RAD)),
                operation("acos", mathContext -> HALF.acos(mathContext, TrigonometricMode.RAD)),
                operation("atan", mathContext -> HALF.atan(mathContext, TrigonometricMode.RAD)),
                operation("acot", mathContext -> HALF.acot(mathContext, TrigonometricMode.RAD)),
                operation("atan2", mathContext -> HALF.atan2(TWO, mathContext)),
                operation("sinh", mathContext -> HALF.sinh(mathContext)),
                operation("cosh", mathContext -> HALF.cosh(mathContext)),
                operation("tanh", mathContext -> HALF.tanh(mathContext)),
                operation("coth", mathContext -> TWO.coth(mathContext)),
                operation("asinh", mathContext -> HALF.asinh(mathContext)),
                operation("acosh", mathContext -> TWO.acosh(mathContext)),
                operation("atanh", mathContext -> HALF.atanh(mathContext)),
                operation("acoth", mathContext -> TWO.acoth(mathContext)),
                operation("combination", mathContext -> FIVE.combination(TWO, mathContext)),
                operation("permutation", mathContext -> FIVE.permutation(TWO, mathContext)),
                operation("lcm", mathContext -> FIVE.lcm(THREE, mathContext)),
                operation("gamma", mathContext -> THREE.gamma(mathContext)),
                operation("beta", mathContext -> TWO.beta(THREE, mathContext)),
                operation("polarToCartesianCoordinates", mathContext -> TWO.polarToCartesianCoordinates(HALF, mathContext)),
                operation("cartesianToPolarCoordinates", mathContext -> TWO.cartesianToPolarCoordinates(THREE, mathContext)),
                operation("isXPercentOfN", mathContext -> TWO.isXPercentOfN(FIVE, mathContext)),
                operation("toDegrees", mathContext -> HALF.toDegrees(mathContext)),
                operation("toRadians", mathContext -> HALF.toRadians(mathContext)),
                operation("summation", mathContext -> BigNumber.valueOf(1).summation(FIVE, "k", mathContext)),
                operation("product", mathContext -> BigNumber.valueOf(1).product(FIVE, "k", mathContext)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    @DisplayName("an operation rejects a MathContext without a precision")
    void anOperationRejectsAMathContextWithoutAPrecision(final String name, final Function<MathContext, Object> operation) {
        final MathArgumentException failure = assertThrows(MathArgumentException.class, () -> operation.apply(MathContext.UNLIMITED), name);

        assertEquals(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, failure.getErrorCode(), name);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    @DisplayName("an operation rejects a MathContext above the precision limit")
    void anOperationRejectsAMathContextAboveThePrecisionLimit(final String name, final Function<MathContext, Object> operation) {
        final MathContext tooLarge = new MathContext(MathUtils.MAX_MATH_CONTEXT_PRECISION + 1);

        final MathArithmeticException failure = assertThrows(MathArithmeticException.class, () -> operation.apply(tooLarge), name);

        assertEquals(CalculatorErrorCode.MATH_OVERFLOW, failure.getErrorCode(), name);
    }
}
