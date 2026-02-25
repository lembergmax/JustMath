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
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exhaustive conversion matrix tests for the LENGTH unit catalog.
 *
 * <p>
 * This test verifies conversions for every pair of units:
 * </p>
 * <ul>
 *   <li>from each unit</li>
 *   <li>to each unit</li>
 * </ul>
 *
 * <p>
 * Expected values are computed independently using the known scale-to-base factors:
 * </p>
 * <pre>
 * expected = value * fromScaleToBase / toScaleToBase
 * </pre>
 *
 * <p>
 * This ensures that:
 * </p>
 * <ul>
 *   <li>registry scales are correct</li>
 *   <li>the converter uses correct arithmetic</li>
 *   <li>the {@link MathContext} is applied consistently for divisions</li>
 * </ul>
 */
final class UnitConversionMatrixTest {

    /**
     * High precision context to minimize rounding artifacts and make comparisons stable.
     */
    private static final MathContext MC = new MathContext(34);

    /**
     * Known scale-to-base factors for the LENGTH catalog.
     * <p>
     * Base unit is meter.
     * </p>
     */
    private static final Map<Unit, java.math.BigDecimal> SCALE_TO_BASE = buildScaleToBase();

    /**
     * Ensures that every unit can be converted to every other unit with correct results.
     */
    @Test
    void convertEveryUnitToEveryUnit() {
        final UnitConverter converter = new UnitConverter(MC);

        // Multiple representative values, including negative values.
        final java.math.BigDecimal[] testValues = {
                new java.math.BigDecimal("0"),
                new java.math.BigDecimal("1"),
                new java.math.BigDecimal("12.5"),
                new java.math.BigDecimal("-3.75"),
                new java.math.BigDecimal("123456.789")
        };

        for (final Unit from : Unit.values()) {
            for (final Unit to : Unit.values()) {
                for (final java.math.BigDecimal input : testValues) {
                    assertPairConversion(converter, from, to, input);
                }
            }
        }
    }

    /**
     * Verifies a single conversion pair (from -> to) for one numeric value.
     *
     * @param converter converter instance; must not be null
     * @param from source unit
     * @param to target unit
     * @param input numeric value in {@code from}
     */
    private static void assertPairConversion(
            final UnitConverter converter,
            final Unit from,
            final Unit to,
            final java.math.BigDecimal input
    ) {
        final BigNumber value = new BigNumber(input.toPlainString());

        final BigNumber actual = converter.convert(value, from, to);

        final java.math.BigDecimal expected = expectedLinearConversion(input, from, to);

        assertEquals(
                0,
                actual.toBigDecimal().compareTo(expected),
                () -> "Mismatch converting " + input.toPlainString() + " " + from + " -> " + to
                        + " (expected " + expected.toPlainString() + ", got " + actual.toBigDecimal().toPlainString() + ")"
        );
    }

    /**
     * Computes the expected conversion result for linear (offset=0) unit conversions.
     *
     * @param value value expressed in {@code from}
     * @param from source unit
     * @param to target unit
     * @return expected value expressed in {@code to}
     */
    private static java.math.BigDecimal expectedLinearConversion(
            final java.math.BigDecimal value,
            final Unit from,
            final Unit to
    ) {
        final java.math.BigDecimal fromScale = SCALE_TO_BASE.get(from);
        final java.math.BigDecimal toScale = SCALE_TO_BASE.get(to);

        // expected = value * fromScale / toScale
        return value.multiply(fromScale, MC).divide(toScale, MC);
    }

    /**
     * Builds the immutable scale map for all units.
     *
     * @return map of scale-to-base factors (meter base)
     */
    private static Map<Unit, java.math.BigDecimal> buildScaleToBase() {
        final Map<Unit, java.math.BigDecimal> map = new EnumMap<>(Unit.class);

        map.put(Unit.KILOMETER, new java.math.BigDecimal("1000"));
        map.put(Unit.HECTOMETER, new java.math.BigDecimal("100"));
        map.put(Unit.METER, new java.math.BigDecimal("1"));
        map.put(Unit.DECIMETER, new java.math.BigDecimal("0.1"));
        map.put(Unit.CENTIMETER, new java.math.BigDecimal("0.01"));
        map.put(Unit.MILLIMETER, new java.math.BigDecimal("0.001"));
        map.put(Unit.MICROMETER, new java.math.BigDecimal("0.000001"));
        map.put(Unit.NANOMETER, new java.math.BigDecimal("0.000000001"));
        map.put(Unit.ANGSTROM, new java.math.BigDecimal("0.0000000001"));
        map.put(Unit.PICOMETER, new java.math.BigDecimal("0.000000000001"));
        map.put(Unit.FEMTOMETER, new java.math.BigDecimal("0.000000000000001"));
        map.put(Unit.INCH, new java.math.BigDecimal("0.0254"));
        map.put(Unit.FEET, new java.math.BigDecimal("0.3048"));
        map.put(Unit.YARD, new java.math.BigDecimal("0.9144"));
        map.put(Unit.MILE, new java.math.BigDecimal("1609.344"));
        map.put(Unit.NAUTICAL_MILE, new java.math.BigDecimal("1852"));
        map.put(Unit.LIGHT_YEAR, new java.math.BigDecimal("9460730472580800"));
        map.put(Unit.PARSEC, new java.math.BigDecimal("30856775814913673"));
        map.put(Unit.PIXEL, new java.math.BigDecimal("0.0002645833333333"));
        map.put(Unit.POINT, new java.math.BigDecimal("0.0003527777777778"));
        map.put(Unit.PICA, new java.math.BigDecimal("0.0042333333333333"));
        map.put(Unit.EM, new java.math.BigDecimal("0.0042333333333333"));

        // Sanity check: ensure no missing entries.
        assertEquals(Unit.values().length, map.size(), "Scale map must contain one entry per Unit enum constant.");

        return Map.copyOf(map);
    }

}