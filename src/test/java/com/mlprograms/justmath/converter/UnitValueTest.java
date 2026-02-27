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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link UnitValue}.
 *
 * <p>
 * These tests focus on the public API behavior of {@link UnitValue}:
 * </p>
 * <ul>
 *   <li>Parsing via constructors (whitespace form and suffix form)</li>
 *   <li>Locale heuristics for decimal/grouping separators</li>
 *   <li>Error handling (blank input, missing unit, unknown unit, etc.)</li>
 *   <li>Utility methods (symbol resolution, formatting helpers, unit checks)</li>
 *   <li>{@link Comparable} contract and unit mismatch behavior</li>
 * </ul>
 *
 * <p>
 * The tests intentionally avoid depending on internal implementation details (like {@code UnitValueParser})
 * and instead validate observable behavior only.
 * </p>
 */
@DisplayName("UnitValue")
class UnitValueTest {

    /**
     * Asserts that a {@link BigNumber} is numerically equal to the expected canonical decimal string.
     *
     * <p>
     * The expected string should use '.' as decimal separator, without grouping separators.
     * </p>
     *
     * @param expectedCanonical expected numeric value in canonical form (e.g. {@code "1234.56"})
     * @param actual            actual big number; must not be {@code null}
     */
    private static void assertBigNumberEquals(final String expectedCanonical, final BigNumber actual) {
        assertNotNull(actual, "actual BigNumber must not be null");
        final BigNumber expected = new BigNumber(expectedCanonical);
        assertEquals(
                0,
                actual.compareTo(expected),
                () -> "Expected numeric value " + expectedCanonical + " but was " + actual
        );
    }

    /**
     * Asserts that a parsed {@link UnitValue} matches an expected unit and numeric value.
     *
     * @param unitValue         parsed instance; must not be {@code null}
     * @param expectedUnit      expected unit; must not be {@code null}
     * @param expectedCanonical expected numeric value in canonical form
     */
    private static void assertUnitValue(
            final UnitValue unitValue,
            final Unit expectedUnit,
            final String expectedCanonical
    ) {
        assertNotNull(unitValue, "unitValue must not be null");
        assertEquals(expectedUnit, unitValue.getUnit(), "Unexpected unit");
        assertBigNumberEquals(expectedCanonical, unitValue.getValue());
    }

    /**
     * Creates a unit value for meters with a canonical numeric string.
     *
     * @param canonical canonical decimal representation ('.' decimal separator)
     * @return unit value in meters
     */
    private static UnitValue meters(final String canonical) {
        return new UnitValue(new BigNumber(canonical), Unit.Length.METER);
    }

    @Nested
    @DisplayName("Parsing via constructors")
    class ParsingViaConstructors {

        @Test
        @DisplayName("parses whitespace-separated format '<number> <symbol>'")
        void parsesWhitespaceSeparatedFormat() {
            final UnitValue unitValue = new UnitValue("12.5 km");
            assertUnitValue(unitValue, Unit.Length.KILOMETER, "12.5");
        }

        @Test
        @DisplayName("parses suffix format '<number><symbol>' (no whitespace)")
        void parsesSuffixFormatWithoutWhitespace() {
            final UnitValue unitValue = new UnitValue("12.5km");
            assertUnitValue(unitValue, Unit.Length.KILOMETER, "12.5");
        }

        @Test
        @DisplayName("trims input and tolerates multiple spaces")
        void trimsAndToleratesMultipleSpaces() {
            final UnitValue unitValue = new UnitValue("   12.5     km   ");
            assertUnitValue(unitValue, Unit.Length.KILOMETER, "12.5");
        }

        @Test
        @DisplayName("parses symbols containing non-letters (e.g. '°C')")
        void parsesNonLetterSymbols() {
            final UnitValue unitValue = new UnitValue("12°C");
            assertUnitValue(unitValue, Unit.Temperature.CELSIUS, "12");
        }

        @Test
        @DisplayName("parses negative numbers")
        void parsesNegativeNumbers() {
            final UnitValue unitValue = new UnitValue("-12.5 km");
            assertUnitValue(unitValue, Unit.Length.KILOMETER, "-12.5");
        }

        @Test
        @DisplayName("prefers longest suffix match (e.g. '10mm' must not be split into '10m' + 'm')")
        void prefersLongestSuffixMatch() {
            final UnitValue unitValue = new UnitValue("10mm");
            assertUnitValue(unitValue, Unit.Length.MILLIMETER, "10");
        }
    }

    @Nested
    @DisplayName("Locale heuristics and normalization")
    class LocaleHeuristicsAndNormalization {

        @Test
        @DisplayName("interprets '1,5' as decimal comma (Germany heuristic) => 1.5")
        void commaOnlyBecomesDecimalComma() {
            final UnitValue unitValue = new UnitValue("1,5 m");
            assertUnitValue(unitValue, Unit.Length.METER, "1.5");
        }

        @Test
        @DisplayName("interprets '1.5' as decimal dot (US heuristic) => 1.5")
        void dotOnlyBecomesDecimalDot() {
            final UnitValue unitValue = new UnitValue("1.5 m");
            assertUnitValue(unitValue, Unit.Length.METER, "1.5");
        }

        @Test
        @DisplayName("when both separators exist, the last one wins (Germany-style '1.234,56' => 1234.56)")
        void bothSeparatorsGermanyStyle() {
            final UnitValue unitValue = new UnitValue("1.234,56 m");
            assertUnitValue(unitValue, Unit.Length.METER, "1234.56");
        }

        @Test
        @DisplayName("when both separators exist, the last one wins (US-style '1,234.56' => 1234.56)")
        void bothSeparatorsUsStyle() {
            final UnitValue unitValue = new UnitValue("1,234.56 m");
            assertUnitValue(unitValue, Unit.Length.METER, "1234.56");
        }

        @Test
        @DisplayName("removes non-breaking spaces used as grouping separators (NBSP)")
        void removesNbspGroupingSeparators() {
            final String nbsp = "\u00A0";
            final UnitValue unitValue = new UnitValue("1" + nbsp + "234,56 m");
            assertUnitValue(unitValue, Unit.Length.METER, "1234.56");
        }

        @Test
        @DisplayName("explicit locale parsing: Germany for '1.234,56' => 1234.56")
        void explicitLocaleGermany() {
            final UnitValue unitValue = new UnitValue("1.234,56 m", Locale.GERMANY);
            assertUnitValue(unitValue, Unit.Length.METER, "1234.56");
        }

        @Test
        @DisplayName("explicit locale parsing: US for '1,234.56' => 1234.56")
        void explicitLocaleUs() {
            final UnitValue unitValue = new UnitValue("1,234.56 m", Locale.US);
            assertUnitValue(unitValue, Unit.Length.METER, "1234.56");
        }

        @Test
        @DisplayName("accepts a custom MathContext constructor (sanity check: does not throw and produces a value)")
        void acceptsCustomMathContextConstructor() {
            final MathContext mathContext = new MathContext(10);
            final UnitValue unitValue = new UnitValue("1234567890.12345 m", Locale.US, mathContext);

            assertNotNull(unitValue.getValue(), "value must not be null");
            assertEquals(Unit.Length.METER, unitValue.getUnit(), "unit must match input symbol");
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("throws UnitConversionException for blank input")
        void blankInputThrows() {
            assertThrows(UnitConversionException.class, () -> new UnitValue("   "));
        }

        @Test
        @DisplayName("throws NullPointerException for null input")
        void nullInputThrows() {
            assertThrows(NullPointerException.class, () -> new UnitValue((String) null));
        }

        @Test
        @DisplayName("throws UnitConversionException for missing unit symbol")
        void missingUnitSymbolThrows() {
            assertThrows(UnitConversionException.class, () -> new UnitValue("12.5"));
        }

        @Test
        @DisplayName("throws UnitConversionException for missing numeric value")
        void missingNumericValueThrows() {
            assertThrows(UnitConversionException.class, () -> new UnitValue("km"));
        }

        @Test
        @DisplayName("throws UnitConversionException for unknown unit symbol")
        void unknownUnitSymbolThrows() {
            assertThrows(UnitConversionException.class, () -> new UnitValue("12.5 xyz"));
        }

        @Test
        @DisplayName("isUnitSymbol throws UnitConversionException for blank symbol")
        void isUnitSymbolBlankThrows() {
            final UnitValue unitValue = new UnitValue("1 m");
            assertThrows(UnitConversionException.class, () -> unitValue.isUnitSymbol("   "));
        }

        @Test
        @DisplayName("isUnitSymbol throws UnitConversionException for unknown symbol")
        void isUnitSymbolUnknownThrows() {
            final UnitValue unitValue = new UnitValue("1 m");
            assertThrows(UnitConversionException.class, () -> unitValue.isUnitSymbol("???"));
        }
    }

    @Nested
    @DisplayName("Utility methods")
    class UtilityMethods {

        @Test
        @DisplayName("toUnitSymbol resolves the registry symbol for the unit")
        void toUnitSymbolResolvesRegistrySymbol() {
            final UnitValue unitValue = new UnitValue("12.5 km");
            assertEquals(UnitElements.getSymbol(Unit.Length.KILOMETER), unitValue.toUnitSymbol());
        }

        @Test
        @DisplayName("isUnit returns true for matching unit and false otherwise")
        void isUnitWorks() {
            final UnitValue unitValue = new UnitValue("12.5 km");

            assertTrue(unitValue.isUnit(Unit.Length.KILOMETER));
            assertFalse(unitValue.isUnit(Unit.Length.METER));
        }

        @Test
        @DisplayName("isUnitSymbol resolves the symbol via UnitElements and matches accordingly")
        void isUnitSymbolWorks() {
            final UnitValue unitValue = new UnitValue("12.5 km");

            assertTrue(unitValue.isUnitSymbol("km"));
            assertFalse(unitValue.isUnitSymbol("m"));
        }

        @Test
        @DisplayName("toDisplayString returns '<value> <symbol>'")
        void toDisplayStringUsesSpace() {
            final UnitValue unitValue = new UnitValue("12.5 km");
            final String expected = unitValue.getValue().toString() + " " + UnitElements.getSymbol(Unit.Length.KILOMETER);

            assertEquals(expected, unitValue.toDisplayString());
        }

        @Test
        @DisplayName("toCompactString returns '<value><symbol>'")
        void toCompactStringOmitsSpace() {
            final UnitValue unitValue = new UnitValue("12.5 km");
            final String expected = unitValue.getValue().toString() + UnitElements.getSymbol(Unit.Length.KILOMETER);

            assertEquals(expected, unitValue.toCompactString());
        }
    }

    @Nested
    @DisplayName("Comparable behavior")
    class ComparableBehavior {

        @ParameterizedTest(name = "compare {0} m to {1} m => sign {2}")
        @CsvSource({
                "1, 2, -1",
                "2, 1,  1",
                "2, 2,  0",
                "-1, 1, -1"
        })
        @DisplayName("compareTo compares numeric values when units match")
        void compareToSameUnit(final String left, final String right, final int expectedSign) {
            final UnitValue a = meters(left);
            final UnitValue b = meters(right);

            final int result = Integer.signum(a.compareTo(b));
            assertEquals(expectedSign, result);
        }

        @Test
        @DisplayName("compareTo throws IllegalArgumentException when units differ")
        void compareToDifferentUnitsThrows() {
            final UnitValue meters = new UnitValue("1 m");
            final UnitValue kilometers = new UnitValue("1 km");

            final IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> meters.compareTo(kilometers)
            );

            assertTrue(
                    exception.getMessage().contains("Unit mismatch"),
                    "Exception message should indicate a unit mismatch"
            );
        }

        @Test
        @DisplayName("sorting a list of same-unit values uses compareTo deterministically")
        void sortingUsesCompareTo() {
            final List<UnitValue> values = new ArrayList<>();
            values.add(meters("3"));
            values.add(meters("1"));
            values.add(meters("2"));

            Collections.sort(values);

            assertBigNumberEquals("1", values.get(0).getValue());
            assertBigNumberEquals("2", values.get(1).getValue());
            assertBigNumberEquals("3", values.get(2).getValue());
        }
    }

    @Nested
    @DisplayName("equals / hashCode basics")
    class EqualsHashCodeBasics {

        @Test
        @DisplayName("two instances with the exact same field instances are equal and share the same hashCode")
        void equalsAndHashCodeWithSameFieldInstances() {
            final BigNumber number = new BigNumber("12.5");
            final Unit unit = Unit.Length.KILOMETER;

            final UnitValue a = new UnitValue(number, unit);
            final UnitValue b = new UnitValue(number, unit);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("different unit => not equal")
        void differentUnitNotEqual() {
            final BigNumber number = new BigNumber("12.5");

            final UnitValue a = new UnitValue(number, Unit.Length.KILOMETER);
            final UnitValue b = new UnitValue(number, Unit.Length.METER);

            assertNotEquals(a, b);
        }
    }

}