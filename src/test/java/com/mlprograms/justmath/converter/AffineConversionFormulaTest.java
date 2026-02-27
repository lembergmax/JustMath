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
import org.junit.jupiter.api.Test;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AffineConversionFormula}.
 *
 * <p>
 * The formula is package-private by design; tests reside in the same package to validate behavior.
 * </p>
 */
final class AffineConversionFormulaTest {

    /**
     * Math context used to validate deterministic division results.
     */
    private static final MathContext MC = new MathContext(34);

    /**
     * Verifies that a zero scale is rejected because the inverse conversion would be undefined.
     */
    @Test
    void rejectsZeroScale() {
        assertThrows(IllegalArgumentException.class, () ->
                new AffineConversionFormula(new BigNumber("0"), new BigNumber("0"))
        );
    }

    /**
     * Verifies correct forward conversion: base = value * scale + offset.
     */
    @Test
    void toBaseAppliesScaleAndOffset() {
        final ConversionFormula formula = new AffineConversionFormula(new BigNumber("3"), new BigNumber("5"));
        final BigNumber base = formula.toBase(new BigNumber("7"), MC);

        assertEquals(0, base.toBigDecimal().compareTo(new java.math.BigDecimal("26")));
    }

    /**
     * Verifies correct inverse conversion: value = (base - offset) / scale.
     */
    @Test
    void fromBaseAppliesInverseFormula() {
        final ConversionFormula formula = new AffineConversionFormula(new BigNumber("3"), new BigNumber("5"));
        final BigNumber value = formula.fromBase(new BigNumber("26"), MC);

        assertEquals(0, value.toBigDecimal().compareTo(new java.math.BigDecimal("7")));
    }

}