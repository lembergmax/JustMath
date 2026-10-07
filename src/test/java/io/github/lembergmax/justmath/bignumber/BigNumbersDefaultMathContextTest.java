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
import java.math.RoundingMode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BigNumbersDefaultMathContextTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 34, 100, 1_000})
    @DisplayName("getDefaultMathContext keeps the precision and rounds half up")
    void keepsPrecisionAndRoundsHalfUp(final int precision) {
        final MathContext mathContext = BigNumbers.getDefaultMathContext(precision);

        assertEquals(precision, mathContext.getPrecision());
        assertEquals(RoundingMode.HALF_UP, mathContext.getRoundingMode());
    }

    @Test
    @DisplayName("getDefaultMathContext rejects a negative precision")
    void rejectsNegativePrecision() {
        assertThrows(IllegalArgumentException.class, () -> BigNumbers.getDefaultMathContext(-1));
    }

    @Test
    @DisplayName("DEFAULT_MATH_CONTEXT is built from the default division precision")
    void defaultMathContextUsesTheDefaultDivisionPrecision() {
        assertEquals(BigNumbers.getDefaultMathContext(BigNumbers.DEFAULT_DIVISION_PRECISION), BigNumbers.DEFAULT_MATH_CONTEXT);
    }

}
