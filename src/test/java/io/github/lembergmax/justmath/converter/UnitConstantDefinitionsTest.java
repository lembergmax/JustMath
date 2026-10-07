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
package io.github.lembergmax.justmath.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Checks the length constants that are defined exactly by an international agreement or by a
 * physical definition against that definition. Before 1.7.0 the international nautical league was
 * wrong by a factor of 10,000 and the light year, the astronomical unit and the parsec family
 * differed from their definitions between the 11th and the 14th significant digit.
 */
final class UnitConstantDefinitionsTest {

    private static final MathContext MC = new MathContext(60);
    private static final BigDecimal PARSEC_TOLERANCE = new BigDecimal("1E-40");

    private final UnitConverter converter = new UnitConverter(MC);

    private BigNumber convert(final Unit.Length from, final Unit.Length to) {
        return converter.convertToBigNumber("1", from, to);
    }

    private static void assertRelativeError(final BigDecimal expected, final BigNumber actual, final BigDecimal tolerance, final String message) {
        final BigDecimal difference = expected.subtract(actual.toBigDecimal()).abs();
        final BigDecimal allowed = expected.abs().multiply(tolerance);

        assertTrue(difference.compareTo(allowed) <= 0, message + ": expected " + expected + " but was " + actual.toBigDecimal());
    }

    @Test
    @DisplayName("the international nautical league is exactly three nautical miles, 5556 m")
    void internationalNauticalLeague() {
        assertEquals("5556", convert(Unit.Length.NAUTICAL_LEAGUE_INTERNATIONAL, Unit.Length.METER).toString());
        assertEquals("3", convert(Unit.Length.NAUTICAL_LEAGUE_INTERNATIONAL, Unit.Length.NAUTICAL_MILE).toString());
    }

    @Test
    @DisplayName("the light year is exactly 299792458 m/s times a Julian year of 31557600 s")
    void lightYear() {
        assertEquals("9460730472580800", convert(Unit.Length.LIGHT_YEAR, Unit.Length.METER).toString());
    }

    @Test
    @DisplayName("the astronomical unit is exactly 149597870700 m (IAU 2012)")
    void astronomicalUnit() {
        assertEquals("149597870700", convert(Unit.Length.ASTRONOMICAL_UNIT, Unit.Length.METER).toString());
    }

    @Test
    @DisplayName("the parsec equals 648000 / pi astronomical units to at least 40 significant digits")
    void parsec() {
        final BigDecimal expected = new BigDecimal(648000)
                .divide(BigDecimalMath.pi(MC), MC)
                .multiply(new BigDecimal("149597870700"), MC);

        assertRelativeError(expected, convert(Unit.Length.PARSEC, Unit.Length.METER), PARSEC_TOLERANCE, "1 pc in m");
        assertRelativeError(expected.multiply(new BigDecimal(1000)), convert(Unit.Length.KILOPARSEC, Unit.Length.METER), PARSEC_TOLERANCE, "1 kpc in m");
        assertRelativeError(expected.multiply(new BigDecimal(1000000)), convert(Unit.Length.MEGAPARSEC, Unit.Length.METER), PARSEC_TOLERANCE, "1 Mpc in m");
    }

    @Test
    @DisplayName("kiloparsec and megaparsec are exact multiples of the parsec")
    void parsecMultiplesAreExact() {
        assertEquals("1000", convert(Unit.Length.KILOPARSEC, Unit.Length.PARSEC).toString());
        assertEquals("1000", convert(Unit.Length.MEGAPARSEC, Unit.Length.KILOPARSEC).toString());
        assertEquals("1000000", convert(Unit.Length.MEGAPARSEC, Unit.Length.PARSEC).toString());
    }
}
