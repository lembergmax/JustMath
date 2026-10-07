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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

/**
 * Compares division and integer powers of {@link BasicMath} with {@link BigDecimal} for every rounding
 * mode. These properties found #202, #203 and #204. A power with a non-negative integer exponent is exact and
 * ignores the precision; a power with a negative integer exponent is the reciprocal, rounded once.
 */
class BasicMathDivisionPropertyTest {

    private static final int ROUNDING_TRIES = 600;

    private static final int MAX_INTEGER_POWER = 40;

    private static final int MIN_NEGATIVE_POWER = -12;

    private static final Locale LOCALE = Locale.US;

    @Provide
    Arbitrary<BigDecimal> decimals() {
        return DecimalArbitraries.decimals();
    }

    @Provide
    Arbitrary<BigDecimal> nonZeroDecimals() {
        return DecimalArbitraries.nonZeroDecimals();
    }

    @Provide
    Arbitrary<BigDecimal> nonZeroSmallDecimals() {
        return DecimalArbitraries.nonZeroSmallDecimals();
    }

    @Provide
    Arbitrary<RoundingMode> roundingModes() {
        return DecimalArbitraries.roundingModes();
    }

    private static BigNumber number(final BigDecimal value) {
        return new BigNumber(value.toPlainString(), LOCALE);
    }

    private static void assertSameValue(final BigDecimal expected, final BigNumber actual, final String description) {
        assertEquals(0, expected.compareTo(actual.toBigDecimal()),
                () -> description + ": expected " + expected.toPlainString() + " but was " + actual.toBigDecimal().toPlainString());
    }

    @Property(tries = ROUNDING_TRIES, seed = SEED)
    void divideMatchesBigDecimalForEveryRoundingMode(
            @ForAll("decimals") final BigDecimal dividend,
            @ForAll("nonZeroDecimals") final BigDecimal divisor,
            @ForAll @IntRange(min = 1, max = 60) final int precision,
            @ForAll("roundingModes") final RoundingMode roundingMode
    ) {
        final MathContext mathContext = new MathContext(precision, roundingMode);

        assertSameValue(dividend.divide(divisor, mathContext),
                BasicMath.divide(number(dividend), number(divisor), mathContext, LOCALE),
                dividend.toPlainString() + " / " + divisor.toPlainString() + " with " + mathContext);
    }

    @Property(tries = ROUNDING_TRIES, seed = SEED)
    void nonNegativeIntegerPowerIsExact(
            @ForAll("nonZeroSmallDecimals") final BigDecimal base,
            @ForAll @IntRange(min = 0, max = MAX_INTEGER_POWER) final int exponent,
            @ForAll @IntRange(min = 1, max = 80) final int precision,
            @ForAll("roundingModes") final RoundingMode roundingMode
    ) {
        final MathContext mathContext = new MathContext(precision, roundingMode);

        assertSameValue(base.pow(exponent),
                BasicMath.power(number(base), number(BigDecimal.valueOf(exponent)), mathContext, LOCALE),
                base.toPlainString() + " ^ " + exponent + " with " + mathContext);
    }

    @Property(tries = ROUNDING_TRIES, seed = SEED)
    void negativeIntegerPowerIsTheReciprocalRoundedOnce(
            @ForAll("nonZeroSmallDecimals") final BigDecimal base,
            @ForAll @IntRange(min = MIN_NEGATIVE_POWER, max = -1) final int exponent,
            @ForAll @IntRange(min = 1, max = 80) final int precision,
            @ForAll("roundingModes") final RoundingMode roundingMode
    ) {
        final MathContext mathContext = new MathContext(precision, roundingMode);
        final BigDecimal expected = BigDecimal.ONE.divide(base.pow(-exponent), mathContext);

        assertSameValue(expected,
                BasicMath.power(number(base), number(BigDecimal.valueOf(exponent)), mathContext, LOCALE),
                base.toPlainString() + " ^ " + exponent + " with " + mathContext);
    }

}
