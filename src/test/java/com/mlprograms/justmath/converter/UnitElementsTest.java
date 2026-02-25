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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.MathContext;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link UnitElements}.
 */
final class UnitElementsTest {

    /**
     * A high precision context used for deterministic conversion comparisons.
     */
    private static final MathContext MC = new MathContext(34);

    /**
     * Verifies strict parsing rules:
     * blank symbols are rejected.
     */
    @Test
    void parseUnitRejectsBlank() {
        assertThrows(UnitConversionException.class, () -> UnitElements.parseUnit("  "));
    }

    /**
     * Verifies strict parsing rules:
     * unknown symbols are rejected.
     */
    @Test
    void parseUnitRejectsUnknownSymbol() {
        assertThrows(UnitConversionException.class, () -> UnitElements.parseUnit("does-not-exist"));
    }

    /**
     * Verifies that the registry map is immutable.
     */
    @Test
    void registryMapIsImmutable() {
        final Map<String, Unit> registry = UnitElements.getRegistry();
        assertThrows(UnsupportedOperationException.class, () -> registry.put("x", Unit.METER));
    }

    /**
     * Validates symbol lookup and basic base conversion for each unit using CSV-driven expectations.
     *
     * <p>
     * This test asserts:
     * </p>
     * <ul>
     *   <li>{@link UnitElements#getSymbol(Unit)} matches the expected symbol</li>
     *   <li>{@link UnitElements#parseUnit(String)} returns the expected unit</li>
     *   <li>Converting {@code 1 <unit>} to base yields the expected scaleToBase</li>
     * </ul>
     */
    @ParameterizedTest(name = "Unit metadata: {0} symbol={1} scaleToBase={2}")
    @CsvSource({
            "KILOMETER, km, 1000",
            "HECTOMETER, hm, 100",
            "METER, m, 1",
            "DECIMETER, dm, 0.1",
            "CENTIMETER, cm, 0.01",
            "MILLIMETER, mm, 0.001",
            "MICROMETER, um, 0.000001",
            "NANOMETER, nm, 0.000000001",
            "ANGSTROM, A, 0.0000000001",
            "PICOMETER, pm, 0.000000000001",
            "FEMTOMETER, fm, 0.000000000000001",
            "INCH, in, 0.0254",
            "FEET, ft, 0.3048",
            "YARD, yd, 0.9144",
            "MILE, mi, 1609.344",
            "NAUTICAL_MILE, nmi, 1852",
            "LIGHT_YEAR, ly, 9460730472580800",
            "PARSEC, pc, 30856775814913673",
            "PIXEL, px, 0.0002645833333333",
            "POINT, pt, 0.0003527777777778",
            "PICA, pica, 0.0042333333333333",
            "EM, em, 0.0042333333333333"
    })
    void metadataAndBaseConversionAreCorrect(final Unit unit, final String symbol, final String scaleToBase) {
        assertEquals(UnitCategory.LENGTH, UnitElements.getCategory(unit));
        assertEquals(symbol, UnitElements.getSymbol(unit));
        assertEquals(unit, UnitElements.parseUnit(symbol));

        final BigNumber one = new BigNumber("1");
        final BigNumber base = UnitElements.toBase(unit, one, MC);

        assertEquals(
                0,
                base.toBigDecimal().compareTo(new java.math.BigDecimal(scaleToBase)),
                () -> "Expected 1 " + unit + " to equal " + scaleToBase + " in base units"
        );
    }

}