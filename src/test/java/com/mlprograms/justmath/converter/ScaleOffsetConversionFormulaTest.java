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

package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import org.junit.jupiter.api.Test;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link ScaleOffsetConversionFormula}.
 */
final class ScaleOffsetConversionFormulaTest {

    /**
     * Ensures that creating a formula with scale=0 is rejected,
     * because inversion would require division by zero.
     */
    @Test
    void ofRejectsZeroScale() {
        assertThrows(IllegalArgumentException.class, () ->
                ScaleOffsetConversionFormula.of(BigNumbers.ZERO, BigNumbers.ZERO)
        );
    }

    /**
     * Validates the forward and inverse transformation with a simple scale+offset.
     */
    @Test
    void toBaseAndFromBaseAreInverseOperations() {
        final ConversionFormula formula = ScaleOffsetConversionFormula.of(new BigNumber("2"), new BigNumber("10"));
        final MathContext mc = new MathContext(34);

        // base = 7 * 2 + 10 = 24
        final BigNumber base = formula.toBase(new BigNumber("7"), mc);
        assertEquals(0, base.toBigDecimal().compareTo(new java.math.BigDecimal("24")));

        // value = (24 - 10) / 2 = 7
        final BigNumber value = formula.fromBase(base, mc);
        assertEquals(0, value.toBigDecimal().compareTo(new java.math.BigDecimal("7")));
    }

}