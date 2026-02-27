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
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link UnitDefinition}.
 *
 * <p>
 * These tests verify that {@link UnitDefinition} properly delegates conversions to the contained
 * {@link ConversionFormula} and preserves metadata such as symbol and display name.
 * </p>
 */
final class UnitDefinitionTest {

    /**
     * Math context used for deterministic delegation tests.
     */
    private static final MathContext MC = new MathContext(34);

    /**
     * Verifies that metadata fields are stored as provided by the constructor.
     */
    @Test
    void storesMetadata() {
        final UnitDefinition def = new UnitDefinition("Meter", "m", ConversionFormulas.linear(new BigNumber("1")));

        assertEquals("Meter", def.displayName());
        assertEquals("m", def.symbol());
        assertNotNull(def.formula());
    }

    /**
     * Verifies that {@link UnitDefinition#toBase(BigNumber, MathContext)} delegates to the formula.
     */
    @Test
    void toBaseDelegatesToFormula() {
        final UnitDefinition def = new UnitDefinition("X", "x", ConversionFormulas.linear(new BigNumber("2")));
        final BigNumber base = def.toBase(new BigNumber("3"), MC);

        assertEquals(0, base.toBigDecimal().compareTo(new java.math.BigDecimal("6")));
    }

    /**
     * Verifies that {@link UnitDefinition#fromBase(BigNumber, MathContext)} delegates to the formula.
     */
    @Test
    void fromBaseDelegatesToFormula() {
        final UnitDefinition def = new UnitDefinition("X", "x", ConversionFormulas.linear(new BigNumber("2")));
        final BigNumber value = def.fromBase(new BigNumber("6"), MC);

        assertEquals(0, value.toBigDecimal().compareTo(new java.math.BigDecimal("3")));
    }

}