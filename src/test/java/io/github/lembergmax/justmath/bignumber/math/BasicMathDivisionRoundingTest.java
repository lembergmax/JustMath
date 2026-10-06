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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * The rounding of a quotient must see the remainder that is left after the last generated digit (#204).
 * A tail that is not zero lifts a value above a tie and makes a directed rounding mode round away from zero.
 */
class BasicMathDivisionRoundingTest {

    private static final Locale LOCALE = Locale.US;

    private static BigNumber number(final String value) {
        return new BigNumber(value, LOCALE);
    }

    @ParameterizedTest(name = "{0} / {1} with {2} digits")
    @DisplayName("a quotient matches BigDecimal for every rounding mode")
    @CsvSource(textBlock = """
            -956101643.1033184343275862, 43843995913765418.10891955170421794454651, 19
            100000000000000000001, 8, 2
            -100000000000000000001, 8, 2
            10000000000000000000001, 1000000000000, 3
            -10000000000000000000001, 1000000000000, 3
            1, 3, 1
            -1, 3, 1
            2, 3, 1
            5, 3, 1
            1, 8, 2
            1.0000000000000000000000001, 1, 1
            -1.0000000000000000000000001, 1, 1
            0.5000000000000000000000001, 1, 1
            7, 7000000000000000000000001, 3
            """)
    void quotientMatchesBigDecimalForEveryRoundingMode(final String dividend, final String divisor, final int precision) {
        for (final RoundingMode roundingMode : RoundingMode.values()) {
            if (roundingMode == RoundingMode.UNNECESSARY) {
                continue;
            }
            final MathContext mathContext = new MathContext(precision, roundingMode);
            final BigDecimal expected = new BigDecimal(dividend).divide(new BigDecimal(divisor), mathContext);

            final BigDecimal actual = BasicMath.divide(number(dividend), number(divisor), mathContext, LOCALE).toBigDecimal();

            assertEquals(0, expected.compareTo(actual),
                    () -> dividend + " / " + divisor + " with " + mathContext + ": expected " + expected.toPlainString()
                            + " but was " + actual.toPlainString());
        }
    }

}
