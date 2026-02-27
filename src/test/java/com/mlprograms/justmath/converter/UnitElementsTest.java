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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UnitElements}.
 *
 * <p>
 * These tests focus on the public facade behavior: parsing, lookups, metadata, and conversions.
 * </p>
 */
final class UnitElementsTest {

    /**
     * Math context used for conversion tests.
     */
    private static final MathContext MC = new MathContext(34);

    /**
     * Ensures {@link UnitElements#parseUnit(String)} parses known symbols.
     */
    @Test
    void parseUnitParsesKnownSymbols() {
        assertEquals(Unit.Length.KILOMETER, UnitElements.parseUnit("km"));
        assertEquals(Unit.Mass.KILOGRAM, UnitElements.parseUnit("kg"));
        assertEquals(Unit.Length.MILLIMETER, UnitElements.parseUnit("mm"));
    }

    /**
     * Ensures {@link UnitElements#parseUnit(String)} rejects blank symbols.
     */
    @Test
    void parseUnitRejectsBlank() {
        assertThrows(UnitConversionException.class, () -> UnitElements.parseUnit(" "));
    }

    /**
     * Ensures {@link UnitElements#parseUnit(String)} rejects unknown symbols.
     */
    @Test
    void parseUnitRejectsUnknown() {
        assertThrows(UnitConversionException.class, () -> UnitElements.parseUnit("nope"));
    }

    /**
     * Ensures the registry map returned by {@link UnitElements#getRegistry()} contains expected entries.
     */
    @Test
    void registryContainsKnownSymbols() {
        final Map<String, Unit> registry = UnitElements.getRegistry();
        assertEquals(Unit.Length.METER, registry.get("m"));
        assertEquals(Unit.Mass.GRAM, registry.get("g"));
    }

    /**
     * Ensures compatibility is type-based and rejects cross-group conversions.
     */
    @Test
    void areCompatibleIsTypeBased() {
        assertTrue(UnitElements.areCompatible(Unit.Length.METER, Unit.Length.INCH));
        assertTrue(UnitElements.areCompatible(Unit.Mass.GRAM, Unit.Mass.OUNCE));
        assertFalse(UnitElements.areCompatible(Unit.Length.METER, Unit.Mass.KILOGRAM));
    }

    /**
     * Ensures toBase/fromBase conversion delegates to the internal registry definitions.
     */
    @Test
    void toBaseAndFromBaseWork() {
        final BigNumber base = UnitElements.toBase(Unit.Length.KILOMETER, new BigNumber("1"), MC);
        assertEquals(0, base.toBigDecimal().compareTo(new java.math.BigDecimal("1000")));

        final BigNumber back = UnitElements.fromBase(Unit.Length.KILOMETER, base, MC);
        assertEquals(0, back.toBigDecimal().compareTo(new java.math.BigDecimal("1")));
    }

}