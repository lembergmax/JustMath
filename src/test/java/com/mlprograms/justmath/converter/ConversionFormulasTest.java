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

/**
 * Unit tests for {@link ConversionFormulas}.
 *
 * <p>
 * These tests validate that factory methods create functional and correct {@link ConversionFormula}
 * implementations.
 * </p>
 */
final class ConversionFormulasTest {

    /**
     * Math context used in these tests to ensure deterministic divisions.
     */
    private static final MathContext MC = new MathContext(34);

    /**
     * Verifies that {@link ConversionFormulas#linear(BigNumber)} maps values using only scale.
     */
    @Test
    void linearFormulaConvertsToAndFromBase() {
        final ConversionFormula formula = ConversionFormulas.linear(new BigNumber("2"));

        final BigNumber base = formula.toBase(new BigNumber("3"), MC);
        assertEquals(0, base.toBigDecimal().compareTo(new java.math.BigDecimal("6")));

        final BigNumber value = formula.fromBase(new BigNumber("6"), MC);
        assertEquals(0, value.toBigDecimal().compareTo(new java.math.BigDecimal("3")));
    }

    /**
     * Verifies that {@link ConversionFormulas#affine(BigNumber, BigNumber)} applies scale and offset correctly.
     */
    @Test
    void affineFormulaConvertsToAndFromBase() {
        final ConversionFormula formula = ConversionFormulas.affine(new BigNumber("2"), new BigNumber("10"));

        final BigNumber base = formula.toBase(new BigNumber("3"), MC);
        assertEquals(0, base.toBigDecimal().compareTo(new java.math.BigDecimal("16")));

        final BigNumber value = formula.fromBase(new BigNumber("16"), MC);
        assertEquals(0, value.toBigDecimal().compareTo(new java.math.BigDecimal("3")));
    }

}