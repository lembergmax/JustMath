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

import static io.github.lembergmax.justmath.bignumber.math.DecimalArbitraries.SEED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

/**
 * Compares the exact operations of {@link BasicMath} with {@link BigDecimal} and {@link BigInteger} on
 * generated operands.
 */
class BasicMathArithmeticPropertyTest {

    private static final int ARITHMETIC_TRIES = 1_000;

    private static final int FUNCTION_TRIES = 150;

    private static final int MAX_FACTORIAL_ARGUMENT = 300;

    private static final Locale LOCALE = Locale.US;

    @Provide
    Arbitrary<BigDecimal> decimals() {
        return DecimalArbitraries.decimals();
    }

    @Provide
    Arbitrary<BigDecimal> nonZeroDecimals() {
        return DecimalArbitraries.nonZeroDecimals();
    }

    private static BigNumber number(final BigDecimal value) {
        return new BigNumber(value.toPlainString(), LOCALE);
    }

    private static void assertSameValue(final BigDecimal expected, final BigNumber actual, final String description) {
        assertEquals(0, expected.compareTo(actual.toBigDecimal()),
                () -> description + ": expected " + expected.toPlainString() + " but was " + actual.toBigDecimal().toPlainString());
    }

    @Property(tries = ARITHMETIC_TRIES, seed = SEED)
    void addMatchesBigDecimal(@ForAll("decimals") final BigDecimal left, @ForAll("decimals") final BigDecimal right) {
        assertSameValue(left.add(right), BasicMath.add(number(left), number(right), LOCALE),
                left.toPlainString() + " + " + right.toPlainString());
    }

    @Property(tries = ARITHMETIC_TRIES, seed = SEED)
    void subtractMatchesBigDecimal(@ForAll("decimals") final BigDecimal left, @ForAll("decimals") final BigDecimal right) {
        assertSameValue(left.subtract(right), BasicMath.subtract(number(left), number(right), LOCALE),
                left.toPlainString() + " - " + right.toPlainString());
    }

    @Property(tries = ARITHMETIC_TRIES, seed = SEED)
    void multiplyMatchesBigDecimal(@ForAll("decimals") final BigDecimal left, @ForAll("decimals") final BigDecimal right) {
        assertSameValue(left.multiply(right), BasicMath.multiply(number(left), number(right), LOCALE),
                left.toPlainString() + " * " + right.toPlainString());
    }

    @Property(tries = ARITHMETIC_TRIES, seed = SEED)
    void remainderTakesTheSignOfTheDividend(
            @ForAll("decimals") final BigDecimal dividend,
            @ForAll("nonZeroDecimals") final BigDecimal divisor
    ) {
        assertSameValue(dividend.remainder(divisor), BasicMath.remainder(number(dividend), number(divisor), LOCALE),
                dividend.toPlainString() + " rem " + divisor.toPlainString());
    }

    @Property(tries = ARITHMETIC_TRIES, seed = SEED)
    void moduloIsNeverNegative(
            @ForAll("decimals") final BigDecimal dividend,
            @ForAll("nonZeroDecimals") final BigDecimal divisor
    ) {
        final BigDecimal magnitude = divisor.abs();
        BigDecimal expected = dividend.remainder(magnitude);
        if (expected.signum() < 0) {
            expected = expected.add(magnitude);
        }

        assertSameValue(expected, BasicMath.modulo(number(dividend), number(divisor), LOCALE),
                dividend.toPlainString() + " mod " + divisor.toPlainString());
    }

    @Property(tries = FUNCTION_TRIES, seed = SEED)
    void factorialIsExact(@ForAll @IntRange(min = 0, max = MAX_FACTORIAL_ARGUMENT) final int argument) {
        BigInteger expected = BigInteger.ONE;
        for (int factor = 2; factor <= argument; factor++) {
            expected = expected.multiply(BigInteger.valueOf(factor));
        }

        assertSameValue(new BigDecimal(expected),
                BasicMath.factorial(number(BigDecimal.valueOf(argument)), MathContext.DECIMAL128, LOCALE),
                argument + "!");
    }

}
