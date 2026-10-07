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

package io.github.lembergmax.justmath.bignumber.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.stream.Stream;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests of {@link NumberTheoryMath} for operands longer than the precision of the {@link MathContext}. The least
 * common multiple is an exact integer, so its digits must not depend on the precision or on the rounding mode.
 */
final class NumberTheoryMathTest {

    private static final int OPERAND_DIGITS = 120;

    static Stream<MathContext> contexts() {
        return Stream.of(10, 50, 100)
                .flatMap(precision -> Stream.of(RoundingMode.HALF_UP, RoundingMode.HALF_EVEN, RoundingMode.DOWN)
                        .map(mode -> new MathContext(precision, mode)));
    }

    private static BigNumber lcm(final BigInteger first, final BigInteger second, final MathContext mathContext) {
        return NumberTheoryMath.lcm(new BigNumber(first.toString()), new BigNumber(second.toString()), mathContext, Locale.US);
    }

    private static String digitsOf(final BigNumber value) {
        return value.toBigDecimal().toBigIntegerExact().toString();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("contexts")
    @DisplayName("lcm keeps every digit of a result that is longer than the precision")
    void lcmKeepsEveryDigitOfALongResult(final MathContext mathContext) {
        final BigInteger first = BigInteger.TEN.pow(OPERAND_DIGITS - 1).add(BigInteger.valueOf(7));
        final BigInteger second = BigInteger.valueOf(3);

        assertEquals(first.multiply(second).toString(), digitsOf(lcm(first, second, mathContext)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("contexts")
    @DisplayName("lcm of a long operand and a negative operand is exact")
    void lcmOfALongOperandAndANegativeOperandIsExact(final MathContext mathContext) {
        final BigInteger first = new BigInteger("684783608612095138514116590999429787021792482842505776804411177837848894779790094845470272314992807990127602604576225115");

        assertEquals(first.toString(), digitsOf(lcm(first, BigInteger.valueOf(-5), mathContext)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("contexts")
    @DisplayName("lcm of operands that share a long common factor is exact")
    void lcmOfOperandsWithACommonFactorIsExact(final MathContext mathContext) {
        final BigInteger first = BigInteger.TWO.pow(200);
        final BigInteger second = BigInteger.valueOf(6).pow(100);
        final BigInteger expected = first.multiply(second).divide(first.gcd(second));

        assertEquals(expected.toString(), digitsOf(lcm(first, second, mathContext)));
    }

    @Test
    @DisplayName("lcm with a zero operand is zero")
    void lcmWithAZeroOperandIsZero() {
        assertEquals("0", digitsOf(lcm(BigInteger.ZERO, BigInteger.valueOf(12), MathContext.DECIMAL64)));
        assertEquals("0", digitsOf(lcm(BigInteger.valueOf(12), BigInteger.ZERO, MathContext.DECIMAL64)));
    }
}
