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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exhaustive conversion matrix tests for built-in unit catalogs.
 *
 * <p>
 * This test converts every unit of a group into every other unit of the same group.
 * It generates test rows in the desired semicolon-separated format:
 * </p>
 *
 * <pre>
 * value;Unit.Length.FROM;Unit.Length.TO;expected
 * value;Unit.Mass.FROM;Unit.Mass.TO;expected
 * </pre>
 *
 * <p>
 * Expected values are computed independently via BigDecimal using scale-to-base factors:
 * </p>
 *
 * <pre>
 * expected = value * fromScaleToBase / toScaleToBase
 * </pre>
 */
final class UnitConversionExhaustiveTest {

    /**
     * High precision math context used to compute expected values and to drive converter rounding.
     */
    private static final MathContext MC = new MathContext(34);

    /**
     * Representative numeric inputs used for the exhaustive matrix.
     *
     * <p>
     * Using multiple values increases confidence that scaling works for zeros, positives and negatives.
     * </p>
     */
    private static final List<java.math.BigDecimal> TEST_VALUES = List.of(
            new java.math.BigDecimal("0"),
            new java.math.BigDecimal("1"),
            new java.math.BigDecimal("24.68"),
            new java.math.BigDecimal("-3.75")
    );

    /**
     * Scale-to-base factors for LENGTH units (base: meter).
     */
    private static final Map<Unit, java.math.BigDecimal> LENGTH_SCALE_TO_BASE = lengthScaleToBase();

    /**
     * Scale-to-base factors for MASS units (base: kilogram).
     */
    private static final Map<Unit, java.math.BigDecimal> MASS_SCALE_TO_BASE = massScaleToBase();

    /**
     * Exhaustive matrix test for LENGTH units: each length unit converted into each other length unit.
     *
     * @param valueText numeric input
     * @param fromText qualified "from" unit name
     * @param toText qualified "to" unit name
     * @param expectedText expected numeric output
     */
    @ParameterizedTest(name = "[LENGTH] {0};{1};{2};{3}")
    @MethodSource("lengthCsvRows")
    void convertsEveryLengthUnitToEveryOtherLengthUnit(
            final String valueText,
            final String fromText,
            final String toText,
            final String expectedText
    ) {
        assertCsvRow(valueText, fromText, toText, expectedText);
    }

    /**
     * Exhaustive matrix test for MASS units: each mass unit converted into each other mass unit.
     *
     * @param valueText numeric input
     * @param fromText qualified "from" unit name
     * @param toText qualified "to" unit name
     * @param expectedText expected numeric output
     */
    @ParameterizedTest(name = "[MASS] {0};{1};{2};{3}")
    @MethodSource("massCsvRows")
    void convertsEveryMassUnitToEveryOtherMassUnit(
            final String valueText,
            final String fromText,
            final String toText,
            final String expectedText
    ) {
        assertCsvRow(valueText, fromText, toText, expectedText);
    }

    /**
     * Creates semicolon-based rows for the LENGTH matrix in the format:
     * {@code value;Unit.Length.FROM;Unit.Length.TO;expected}.
     *
     * @return stream of arguments in CSV-column form
     */
    static Stream<org.junit.jupiter.params.provider.Arguments> lengthCsvRows() {
        return buildRows(UnitRegistry.unitsOfGroup(Unit.Length.class), LENGTH_SCALE_TO_BASE);
    }

    /**
     * Creates semicolon-based rows for the MASS matrix in the format:
     * {@code value;Unit.Mass.FROM;Unit.Mass.TO;expected}.
     *
     * @return stream of arguments in CSV-column form
     */
    static Stream<org.junit.jupiter.params.provider.Arguments> massCsvRows() {
        return buildRows(UnitRegistry.unitsOfGroup(Unit.Mass.class), MASS_SCALE_TO_BASE);
    }

    /**
     * Executes one row in the CSV row format by parsing units and numbers, running the converter,
     * and comparing actual output to expected output using BigDecimal compare semantics.
     *
     * @param valueText numeric input text
     * @param fromText qualified "from" unit name
     * @param toText qualified "to" unit name
     * @param expectedText expected numeric output text
     */
    private static void assertCsvRow(
            final String valueText,
            final String fromText,
            final String toText,
            final String expectedText
    ) {
        final Unit from = UnitTestSupport.parseQualifiedUnitName(fromText);
        final Unit to = UnitTestSupport.parseQualifiedUnitName(toText);

        final UnitConverter converter = new UnitConverter(MC);

        final BigNumber actual = converter.convert(new BigNumber(valueText), from, to);
        final java.math.BigDecimal expected = new java.math.BigDecimal(expectedText, MC);

        assertEquals(
                0,
                actual.toBigDecimal().compareTo(expected),
                () -> "Mismatch converting " + valueText + " " + fromText + " -> " + toText
                        + " (expected " + expected.toPlainString() + ", got " + actual.toBigDecimal().toPlainString() + ")"
        );
    }

    /**
     * Builds CSV-style argument rows for a given group list and scale map.
     *
     * @param units all units of a single group
     * @param scaleToBase scale-to-base factors for the group
     * @return stream of arguments, each containing 4 string columns
     */
    private static Stream<org.junit.jupiter.params.provider.Arguments> buildRows(
            final List<Unit> units,
            final Map<Unit, java.math.BigDecimal> scaleToBase
    ) {
        final List<org.junit.jupiter.params.provider.Arguments> rows = new ArrayList<>();

        for (final java.math.BigDecimal input : TEST_VALUES) {
            for (final Unit from : units) {
                for (final Unit to : units) {
                    final java.math.BigDecimal expected = expectedLinearConversion(input, from, to, scaleToBase);
                    rows.add(org.junit.jupiter.params.provider.Arguments.of(
                            input.toPlainString(),
                            UnitTestSupport.qualifiedName(from),
                            UnitTestSupport.qualifiedName(to),
                            expected.toPlainString()
                    ));
                }
            }
        }

        return rows.stream();
    }

    /**
     * Computes the expected conversion for linear units within one group.
     *
     * @param value numeric input in {@code from} unit
     * @param from source unit
     * @param to target unit
     * @param scaleToBase scale-to-base factors
     * @return expected numeric output in {@code to} unit
     */
    private static java.math.BigDecimal expectedLinearConversion(
            final java.math.BigDecimal value,
            final Unit from,
            final Unit to,
            final Map<Unit, java.math.BigDecimal> scaleToBase
    ) {
        final java.math.BigDecimal fromScale = scaleToBase.get(from);
        final java.math.BigDecimal toScale = scaleToBase.get(to);

        if (fromScale == null || toScale == null) {
            throw new AssertionError("Missing scale factor for unit(s): " + from + ", " + to);
        }

        return value.multiply(fromScale, MC).divide(toScale, MC);
    }

    /**
     * Builds the scale-to-base factor map for LENGTH (base: meter).
     *
     * @return immutable mapping of length unit to meter scale factor
     */
    private static Map<Unit, java.math.BigDecimal> lengthScaleToBase() {
        final Map<Unit, java.math.BigDecimal> map = new HashMap<>();

        map.put(Unit.Length.KILOMETER, new java.math.BigDecimal("1000"));
        map.put(Unit.Length.HECTOMETER, new java.math.BigDecimal("100"));
        map.put(Unit.Length.METER, new java.math.BigDecimal("1"));
        map.put(Unit.Length.DECIMETER, new java.math.BigDecimal("0.1"));
        map.put(Unit.Length.CENTIMETER, new java.math.BigDecimal("0.01"));
        map.put(Unit.Length.MILLIMETER, new java.math.BigDecimal("0.001"));
        map.put(Unit.Length.MICROMETER, new java.math.BigDecimal("0.000001"));
        map.put(Unit.Length.NANOMETER, new java.math.BigDecimal("0.000000001"));
        map.put(Unit.Length.ANGSTROM, new java.math.BigDecimal("0.0000000001"));
        map.put(Unit.Length.PICOMETER, new java.math.BigDecimal("0.000000000001"));
        map.put(Unit.Length.FEMTOMETER, new java.math.BigDecimal("0.000000000000001"));
        map.put(Unit.Length.INCH, new java.math.BigDecimal("0.0254"));
        map.put(Unit.Length.FEET, new java.math.BigDecimal("0.3048"));
        map.put(Unit.Length.YARD, new java.math.BigDecimal("0.9144"));
        map.put(Unit.Length.MILE, new java.math.BigDecimal("1609.344"));
        map.put(Unit.Length.NAUTICAL_MILE, new java.math.BigDecimal("1852"));
        map.put(Unit.Length.LIGHT_YEAR, new java.math.BigDecimal("9460730472580800"));
        map.put(Unit.Length.PARSEC, new java.math.BigDecimal("30856775814913673"));
        map.put(Unit.Length.PIXEL, new java.math.BigDecimal("0.0002645833333333"));
        map.put(Unit.Length.POINT, new java.math.BigDecimal("0.0003527777777778"));
        map.put(Unit.Length.PICA, new java.math.BigDecimal("0.0042333333333333"));
        map.put(Unit.Length.EM, new java.math.BigDecimal("0.0042333333333333"));

        return Map.copyOf(map);
    }

    /**
     * Builds the scale-to-base factor map for MASS (base: kilogram).
     *
     * @return immutable mapping of mass unit to kilogram scale factor
     */
    private static Map<Unit, java.math.BigDecimal> massScaleToBase() {
        final Map<Unit, java.math.BigDecimal> map = new HashMap<>();

        map.put(Unit.Mass.TONNE, new java.math.BigDecimal("1000"));
        map.put(Unit.Mass.KILOGRAM, new java.math.BigDecimal("1"));
        map.put(Unit.Mass.GRAM, new java.math.BigDecimal("0.001"));
        map.put(Unit.Mass.MILLIGRAM, new java.math.BigDecimal("0.000001"));
        map.put(Unit.Mass.POUND, new java.math.BigDecimal("0.45359237"));
        map.put(Unit.Mass.OUNCE, new java.math.BigDecimal("0.028349523125"));

        return Map.copyOf(map);
    }

}