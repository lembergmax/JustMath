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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

/**
 * Compares {@code exp} and the power with a fractional exponent of {@link BasicMath} with
 * {@link BigDecimalMath}. A result must be within one unit in the last place of the requested precision.
 */
class BasicMathTranscendentalPropertyTest {

    private static final int FUNCTION_TRIES = 150;

    private static final int REFERENCE_GUARD_DIGITS = 20;

    private static final Locale LOCALE = Locale.US;

    @Provide
    Arbitrary<BigDecimal> smallDecimals() {
        return DecimalArbitraries.smallDecimals();
    }

    @Provide
    Arbitrary<BigDecimal> positiveSmallDecimals() {
        return DecimalArbitraries.positiveSmallDecimals();
    }

    @Provide
    Arbitrary<BigDecimal> fractionalExponents() {
        return DecimalArbitraries.fractionalExponents();
    }

    private static BigNumber number(final BigDecimal value) {
        return new BigNumber(value.toPlainString(), LOCALE);
    }

    @Property(tries = FUNCTION_TRIES, seed = SEED)
    void expStaysWithinOneUnitInTheLastPlace(
            @ForAll("smallDecimals") final BigDecimal argument,
            @ForAll @IntRange(min = 5, max = 80) final int precision
    ) {
        final MathContext mathContext = new MathContext(precision, RoundingMode.HALF_UP);
        final BigDecimal reference = BigDecimalMath.exp(argument, new MathContext(precision + REFERENCE_GUARD_DIGITS));

        assertWithinOneUnitInTheLastPlace(reference, BasicMath.exp(number(argument), mathContext, LOCALE), precision,
                "exp(" + argument.toPlainString() + ") with " + mathContext);
    }

    @Property(tries = FUNCTION_TRIES, seed = SEED)
    void nonIntegerPowerStaysWithinOneUnitInTheLastPlace(
            @ForAll("positiveSmallDecimals") final BigDecimal base,
            @ForAll("fractionalExponents") final BigDecimal exponent,
            @ForAll @IntRange(min = 5, max = 60) final int precision
    ) {
        final MathContext mathContext = new MathContext(precision, RoundingMode.HALF_UP);
        final BigDecimal reference = BigDecimalMath.pow(base, exponent, new MathContext(precision + REFERENCE_GUARD_DIGITS));

        assertWithinOneUnitInTheLastPlace(reference, BasicMath.power(number(base), number(exponent), mathContext, LOCALE), precision,
                base.toPlainString() + " ^ " + exponent.toPlainString() + " with " + mathContext);
    }

    private static void assertWithinOneUnitInTheLastPlace(
            final BigDecimal reference,
            final BigNumber actual,
            final int precision,
            final String description
    ) {
        final BigDecimal tolerance = reference.abs().movePointLeft(precision - 1);
        final BigDecimal error = reference.subtract(actual.toBigDecimal()).abs();

        assertTrue(error.compareTo(tolerance) <= 0,
                () -> description + ": reference " + reference.round(new MathContext(precision + 5)).toPlainString()
                        + " but was " + actual.toBigDecimal().toPlainString());
    }

}
