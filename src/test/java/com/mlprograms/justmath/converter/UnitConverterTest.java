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
import com.mlprograms.justmath.converter.exception.UnitConversionException;
import org.junit.jupiter.api.Test;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link UnitConverter}.
 *
 * <p>
 * These tests validate:
 * </p>
 * <ul>
 *   <li>cross-group conversions are rejected</li>
 *   <li>simple known conversions are correct</li>
 *   <li>identity conversions preserve values</li>
 * </ul>
 */
final class UnitConverterTest {

    /**
     * Math context used for deterministic conversion outputs.
     */
    private static final MathContext MC = new MathContext(34);

    /**
     * Ensures converting between different unit groups throws {@link UnitConversionException}.
     */
    @Test
    void rejectsCrossGroupConversions() {
        final UnitConverter converter = new UnitConverter(MC);

        assertThrows(UnitConversionException.class, () ->
                converter.convert(new BigNumber("1"), Unit.Length.METER, Unit.Mass.KILOGRAM)
        );
    }

    /**
     * Ensures a known LENGTH conversion is computed correctly.
     */
    @Test
    void convertsKilometerToMeter() {
        final UnitConverter converter = new UnitConverter(MC);

        final BigNumber result = converter.convert(new BigNumber("1"), Unit.Length.KILOMETER, Unit.Length.METER);
        assertEquals(0, result.toBigDecimal().compareTo(new java.math.BigDecimal("1000")));
    }

    /**
     * Ensures a known MASS conversion is computed correctly.
     */
    @Test
    void convertsGramToKilogram() {
        final UnitConverter converter = new UnitConverter(MC);

        final BigNumber result = converter.convert(new BigNumber("1000"), Unit.Mass.GRAM, Unit.Mass.KILOGRAM);
        assertEquals(0, result.toBigDecimal().compareTo(new java.math.BigDecimal("1")));
    }

    /**
     * Ensures converting a value to the same unit returns an equal numeric value.
     */
    @Test
    void identityConversionPreservesValue() {
        final UnitConverter converter = new UnitConverter(MC);

        final BigNumber result = converter.convert(new BigNumber("123.456"), Unit.Length.METER, Unit.Length.METER);
        assertEquals(0, result.toBigDecimal().compareTo(new java.math.BigDecimal("123.456")));
    }

}