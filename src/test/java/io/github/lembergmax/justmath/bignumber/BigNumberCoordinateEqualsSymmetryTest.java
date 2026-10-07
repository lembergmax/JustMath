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
package io.github.lembergmax.justmath.bignumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.lembergmax.justmath.calculator.internal.CoordinateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * {@code BigNumberCoordinate} extends {@code BigNumber} and overrides {@code equals} with a stricter rule. SpotBugs
 * reports that pattern as {@code EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC}. It is symmetric here because both sides
 * reject the other type (audit fix K1); this test pins that, together with the matching ordering and hash code.
 */
class BigNumberCoordinateEqualsSymmetryTest {

    private static BigNumberCoordinate coordinate(final String x, final String y, final CoordinateType type) {
        return new BigNumberCoordinate(new BigNumber(x), new BigNumber(y), type, Locale.US);
    }

    @Test
    @DisplayName("two coordinates with the same type and components are equal in both directions and share a hash code")
    void equalCoordinatesAreSymmetric() {
        final BigNumberCoordinate first = coordinate("3", "4", CoordinateType.CARTESIAN);
        final BigNumberCoordinate second = coordinate("3.0", "4.00", CoordinateType.CARTESIAN);

        assertTrue(first.equals(second));
        assertTrue(second.equals(first));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("a coordinate and a plain number with the same first value are unequal in both directions")
    void coordinateAndPlainNumberAreNeverEqual() {
        final BigNumber plain = new BigNumber("3");
        final BigNumberCoordinate coordinate = coordinate("3", "4", CoordinateType.CARTESIAN);

        assertFalse(plain.equals(coordinate));
        assertFalse(coordinate.equals(plain));
        assertTrue(plain.compareTo(coordinate) < 0);
        assertTrue(coordinate.compareTo(plain) > 0);
    }

    @Test
    @DisplayName("coordinates with different types are unequal in both directions")
    void differentTypesAreUnequalInBothDirections() {
        final BigNumberCoordinate cartesian = coordinate("3", "4", CoordinateType.CARTESIAN);
        final BigNumberCoordinate polar = coordinate("3", "4", CoordinateType.POLAR);

        assertFalse(cartesian.equals(polar));
        assertFalse(polar.equals(cartesian));
    }
}
