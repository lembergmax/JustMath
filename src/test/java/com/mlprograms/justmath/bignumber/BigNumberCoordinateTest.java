/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.calculator.internal.CoordinateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BigNumberCoordinateTest {

    @ParameterizedTest(name = "toString (CARTESIAN, grouping={2}): {0}, {1}")
    @CsvSource(value = {
            "1234.500|9876.000|true|x=1,234.5; y=9,876",
            "1234.500|9876.000|false|x=1234.5; y=9876",
            "0.00000|0.00000|false|x=0; y=0",
            "1000000|2000000|true|x=1,000,000; y=2,000,000"
    }, delimiter = '|')
    void testToString_Cartesian(String x, String y, boolean useGrouping, String expected) {
        BigNumberCoordinate coordinate = new BigNumberCoordinate(
                new BigNumber(x, Locale.US),
                new BigNumber(y, Locale.US),
                CoordinateType.CARTESIAN
        );
        if (useGrouping) {
            assertEquals(expected, coordinate.toPrettyString());
        } else {
            assertEquals(expected, coordinate.toString());
        }
    }

    @ParameterizedTest(name = "toString (POLAR, grouping={2}): {0}, {1}")
    @CsvSource({
            "3.14159, 1.5708, false, r=3.14159; θ=1.5708",
            "0.000, 0.000, true, r=0; θ=0",
            "10000.000, 0.50000, false, r=10000; θ=0.5"
    })
    void testToString_Polar(String r, String theta, boolean useGrouping, String expected) {
        BigNumberCoordinate coordinate = new BigNumberCoordinate(
                new BigNumber(r, Locale.US),
                new BigNumber(theta, Locale.US),
                CoordinateType.POLAR
        );
        assertEquals(expected, coordinate.toString());
    }

    @ParameterizedTest(name = "Trim removes unnecessary zeros: {0}, {1}")
    @CsvSource({
            "123.45000, 0.000000, 123.45, 0",
            "000.00100, 2.300000, 0.001, 2.3",
            "00000, 0.00000, 0, 0"
    })
    void testTrim(String xIn, String yIn, String expectedX, String expectedY) {
        BigNumberCoordinate coordinate = new BigNumberCoordinate(
                new BigNumber(xIn, Locale.US),
                new BigNumber(yIn, Locale.US),
                CoordinateType.CARTESIAN
        ).trim();

        assertEquals(expectedX, coordinate.getX().toString());
        assertEquals(expectedY, coordinate.getY().toString());
    }

    @ParameterizedTest(name = "Default constructor returns (0, 0)")
    @CsvSource({
            "0, 0"
    })
    void testDefaultConstructor(String expectedX, String expectedY) {
        BigNumberCoordinate coordinate = new BigNumberCoordinate();
        assertEquals(expectedX, coordinate.getX().toString());
        assertEquals(expectedY, coordinate.getY().toString());
        assertEquals(CoordinateType.CARTESIAN, coordinate.getType());
    }

    @ParameterizedTest(name = "Single value constructor: {0}")
    @CsvSource({
            "42.42, 42.42, 42.42"
    })
    void testSingleValueConstructor(String input, String expectedX, String expectedY) {
        BigNumberCoordinate coordinate = new BigNumberCoordinate(new BigNumber(input, Locale.US));
        assertEquals(expectedX, coordinate.getX().toString());
        assertEquals(expectedY, coordinate.getY().toString());
        assertEquals(CoordinateType.CARTESIAN, coordinate.getType());
    }

    @Test
    @DisplayName("Constructor with (BigNumber x, BigNumber y, Locale)")
    void testConstructor_XY_Locale() {
        BigNumber x = new BigNumber("1234.5", Locale.GERMANY);
        BigNumber y = new BigNumber("6789.0", Locale.GERMANY);
        BigNumberCoordinate coordinate = new BigNumberCoordinate(x, y, Locale.GERMANY);

        assertEquals("1234,5", coordinate.getX().toString());
        assertEquals("6789", coordinate.getY().toString());
        assertEquals(CoordinateType.CARTESIAN, coordinate.getType());
        assertEquals(Locale.GERMANY, coordinate.getLocale());
    }

    @Test
    @DisplayName("Constructor with (BigNumber x, BigNumber y, CoordinateType, Locale)")
    void testConstructor_XY_Type_Locale() {
        BigNumber x = new BigNumber("3.14", Locale.US);
        BigNumber y = new BigNumber("1.57", Locale.US);
        BigNumberCoordinate coordinate = new BigNumberCoordinate(x, y, CoordinateType.POLAR, Locale.US);

        assertEquals("r=3.14; θ=1.57", coordinate.toPrettyString(Locale.US));
        assertEquals(CoordinateType.POLAR, coordinate.getType());
        assertEquals(Locale.US, coordinate.getLocale());
    }

    @Test
    @DisplayName("Constructor with (BigNumber x, BigNumber y, CoordinateType)")
    void testConstructor_XY_Type() {
        BigNumber x = new BigNumber("2.5", Locale.US);
        BigNumber y = new BigNumber("4.5", Locale.US);
        BigNumberCoordinate coordinate = new BigNumberCoordinate(x, y, CoordinateType.CARTESIAN);

        assertEquals(CoordinateType.CARTESIAN, coordinate.getType());
        assertEquals("x=2.5; y=4.5", coordinate.toPrettyString(Locale.US));
    }

    @Test
    @DisplayName("Constructor with (BigNumber xy, Locale)")
    void testConstructor_XY_Same_Locale() {
        BigNumber value = new BigNumber("9.99", Locale.FRANCE);
        BigNumberCoordinate coordinate = new BigNumberCoordinate(value, Locale.FRANCE);

        assertEquals("9,99", coordinate.getX().toPrettyString(Locale.FRANCE));
        assertEquals("9,99", coordinate.getY().toPrettyString(Locale.FRANCE));
        assertEquals(CoordinateType.CARTESIAN, coordinate.getType());
        assertEquals(Locale.FRANCE, coordinate.getLocale());
    }

    @Test
    @DisplayName("Constructor with (String xStr, yStr, CoordinateType, Locale)")
    void testConstructor_StringInputs() {
        BigNumberCoordinate coordinate = new BigNumberCoordinate("1.23", "4.56", CoordinateType.CARTESIAN, Locale.US);

        assertEquals("x=1.23; y=4.56", coordinate.toPrettyString(Locale.US));
        assertEquals("1.23", coordinate.getX().toString());
        assertEquals("4.56", coordinate.getY().toString());
        assertEquals(CoordinateType.CARTESIAN, coordinate.getType());
        assertEquals(Locale.US, coordinate.getLocale());
    }

    @Test
    @DisplayName("Constructor with (String xStr, yStr, Locale)")
    void testConstructor_StringInputs_DefaultType() {
        BigNumberCoordinate coordinate = new BigNumberCoordinate("1000", "2000", Locale.GERMANY);

        assertEquals("x=1000; y=2000", coordinate.toString());
        assertEquals(CoordinateType.CARTESIAN, coordinate.getType());
        assertEquals(Locale.GERMANY, coordinate.getLocale());
    }

    @Test
    void defaultConstructor_originCartesian() {
        BigNumberCoordinate c = new BigNumberCoordinate();
        assertEquals(CoordinateType.CARTESIAN, c.getType());
        assertEquals("0", c.getX().toString());
        assertEquals("0", c.getY().toString());
        assertEquals("x=0; y=0", c.toString());
    }

    @Test
    void singleArgConstructor_setsBothAxesSameValue() {
        BigNumber xy = new BigNumber("12.3400", Locale.US);
        BigNumberCoordinate c = new BigNumberCoordinate(xy);
        // Ensure both components are set to the same value (untrimmed until formatting/trim is called)
        assertEquals("12.3400", c.getX().toPrettyString(Locale.US));
        assertEquals("12.3400", c.getY().toPrettyString(Locale.US));
        assertEquals(CoordinateType.CARTESIAN, c.getType());
    }

    @Test
    void twoArgConstructor_defaultTypeCartesian() {
        BigNumber x = new BigNumber("-5.000", Locale.US);
        BigNumber y = new BigNumber("10.2500", Locale.US);
        BigNumberCoordinate c = new BigNumberCoordinate(x, y);
        assertEquals(CoordinateType.CARTESIAN, c.getType());
        assertEquals("-5.000", c.getX().toPrettyString(Locale.US));
        assertEquals("10.2500", c.getY().toPrettyString(Locale.US));
    }

    @Test
    void constructorWithTypeAndLocale_formatsUsingProvidedLocale() {
        BigNumber x = new BigNumber("1234.56", Locale.US);
        BigNumber y = new BigNumber("7890.12", Locale.US);
        BigNumberCoordinate c = new BigNumberCoordinate(x, y, CoordinateType.CARTESIAN, Locale.GERMANY);

        // Default toString() uses the coordinate's internal locale (taken from x inside the coordinate)
        assertEquals("x=1234,56; y=7890,12", c.toString());
        // With grouping enabled (using object's locale)
        assertEquals("x=1.234,56; y=7.890,12", c.toPrettyString());
        // Explicit locale overrides object's locale
        assertEquals("x=1,234.56; y=7,890.12", c.toPrettyString(Locale.US));
    }

    @Test
    void trim_mutatesComponents_andReturnsSameInstance() {
        BigNumber x = new BigNumber("0000123.45000", Locale.US);
        BigNumber y = new BigNumber("-000000.12000", Locale.US);
        BigNumberCoordinate c = new BigNumberCoordinate(x, y, CoordinateType.CARTESIAN, Locale.US);

        BigNumberCoordinate returned = c.trim();
        assertSame(c, returned, "trim() should return the same instance");
        assertEquals("123.45", c.getX().toString());
        assertEquals("-0.12", c.getY().toString());
        assertEquals("x=123.45; y=-0.12", c.toString());
    }

    @Test
    void toString_shouldTrimComponentsAsSideEffect() {
        BigNumber x = new BigNumber("1000.23000", Locale.US);
        BigNumber y = new BigNumber("2000.00000", Locale.US);
        BigNumberCoordinate c = new BigNumberCoordinate(x, y, CoordinateType.CARTESIAN, Locale.US);

        // Calling toString() should trim internal BigNumbers
        assertEquals("x=1000.23; y=2000", c.toString());
        assertEquals("1000.23", c.getX().toString());
        assertEquals("2000", c.getY().toString());
    }

    @Test
    void toStringVariants_USLocale() {
        BigNumber x = new BigNumber("1234567.8900", Locale.US);
        BigNumber y = new BigNumber("-9876543.2100", Locale.US);
        BigNumberCoordinate c = new BigNumberCoordinate(x, y, CoordinateType.CARTESIAN, Locale.US);

        assertEquals("x=1234567.89; y=-9876543.21", c.toString());
        assertEquals("x=1,234,567.89; y=-9,876,543.21", c.toPrettyString());
        assertEquals("x=1,234,567.89; y=-9,876,543.21", c.toPrettyString(Locale.US));
        assertEquals("x=1,234,567.89; y=-9,876,543.21", c.toPrettyString());
    }

    @Test
    void toStringVariants_GermanLocale() {
        BigNumber x = new BigNumber("1234567.8900", Locale.US);
        BigNumber y = new BigNumber("-9876543.2100", Locale.US);
        BigNumberCoordinate c = new BigNumberCoordinate(x, y, CoordinateType.CARTESIAN, Locale.GERMANY);

        // No grouping by default, but decimal comma
        assertEquals("x=1234567,89; y=-9876543,21", c.toString());
        // Grouping with dots and comma as decimal
        assertEquals("x=1.234.567,89; y=-9.876.543,21", c.toPrettyString());
        // Explicit locale with grouping
        assertEquals("x=1.234.567,89; y=-9.876.543,21", c.toPrettyString(Locale.GERMANY));
    }

    @Test
    void polarCoordinate_usesPolarSymbols() {
        BigNumber r = new BigNumber("2", Locale.US);
        BigNumber theta = new BigNumber("3.14159", Locale.US);
        BigNumberCoordinate c = new BigNumberCoordinate(r, theta, CoordinateType.POLAR, Locale.US);

        assertEquals("r=2; θ=3.14159", c.toString());
        assertEquals("r=2; θ=3,14159", c.toPrettyString(Locale.GERMANY));
    }

    @Test
    void constructors_nullArguments_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> new BigNumberCoordinate((BigNumber) null));
        BigNumber one = new BigNumber("1", Locale.US);
        assertThrows(NullPointerException.class, () -> new BigNumberCoordinate(null, one, CoordinateType.CARTESIAN, Locale.US));
        assertThrows(NullPointerException.class, () -> new BigNumberCoordinate(one, null, CoordinateType.CARTESIAN, Locale.US));
        assertThrows(NullPointerException.class, () -> new BigNumberCoordinate(one, one, CoordinateType.CARTESIAN, null));
    }

}
