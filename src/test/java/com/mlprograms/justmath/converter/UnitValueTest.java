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

import com.mlprograms.justmath.converter.exception.UnitConversionException;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link UnitValue}.
 */
final class UnitValueTest {

    /**
     * Parses using whitespace format with dot decimal.
     */
    @Test
    void parseWhitespaceDotDecimal() {
        final UnitValue value = UnitValue.parse("12.5 km", Locale.US);
        assertEquals(Unit.KILOMETER, value.getUnit());
        assertEquals(0, value.getValue().toBigDecimal().compareTo(new java.math.BigDecimal("12.5")));
    }

    /**
     * Parses using whitespace format with comma decimal.
     */
    @Test
    void parseWhitespaceCommaDecimal() {
        final UnitValue value = UnitValue.parse("12,5 km", Locale.GERMANY);
        assertEquals(Unit.KILOMETER, value.getUnit());
        assertEquals(0, value.getValue().toBigDecimal().compareTo(new java.math.BigDecimal("12.5")));
    }

    /**
     * Parses using suffix format without whitespace.
     */
    @Test
    void parseSuffixFormat() {
        final UnitValue value = UnitValue.parse("12.5km", Locale.US);
        assertEquals(Unit.KILOMETER, value.getUnit());
        assertEquals(0, value.getValue().toBigDecimal().compareTo(new java.math.BigDecimal("12.5")));
    }

    /**
     * Parses using auto-locale detection:
     * comma implies comma-decimal locale.
     */
    @Test
    void parseAutoLocaleComma() {
        final UnitValue value = UnitValue.parse("12,5 km");
        assertEquals(Unit.KILOMETER, value.getUnit());
        assertEquals(0, value.getValue().toBigDecimal().compareTo(new java.math.BigDecimal("12.5")));
    }

    /**
     * Parses using auto-locale detection:
     * dot implies dot-decimal locale.
     */
    @Test
    void parseAutoLocaleDot() {
        final UnitValue value = UnitValue.parse("12.5 km");
        assertEquals(Unit.KILOMETER, value.getUnit());
        assertEquals(0, value.getValue().toBigDecimal().compareTo(new java.math.BigDecimal("12.5")));
    }

    /**
     * Rejects blank input.
     */
    @Test
    void parseRejectsBlank() {
        assertThrows(UnitConversionException.class, () -> UnitValue.parse("   "));
    }

    /**
     * Rejects missing unit symbol.
     */
    @Test
    void parseRejectsMissingUnit() {
        assertThrows(UnitConversionException.class, () -> UnitValue.parse("12.5"));
    }

    /**
     * Rejects missing numeric value.
     */
    @Test
    void parseRejectsMissingNumber() {
        assertThrows(UnitConversionException.class, () -> UnitValue.parse("km"));
    }

    /**
     * Rejects unknown unit symbol.
     */
    @Test
    void parseRejectsUnknownUnitSymbol() {
        assertThrows(UnitConversionException.class, () -> UnitValue.parse("12.5 unknown"));
    }

}