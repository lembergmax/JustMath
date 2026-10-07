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

import static io.github.lembergmax.justmath.bignumber.math.DecimalArbitraries.SEED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.converter.exception.UnitConversionException;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Properties of unit conversion over all 601 units: converting there and back returns the value, converting through
 * a third unit gives the same result as converting directly, and units of different groups are rejected.
 */
class UnitConverterPropertyTest {

    private static final int TRIES = 600;

    private static final MathContext PRECISION = new MathContext(120, RoundingMode.HALF_EVEN);

    private static final BigDecimal RELATIVE_TOLERANCE = new BigDecimal("1E-100");

    private static final int MAX_DIGITS = 12;

    private static final int MAX_SCALE = 6;

    private final UnitConverter converter = new UnitConverter(PRECISION);

    private record Triple(Unit first, Unit second, Unit third) {
    }

    private record Pair(Unit first, Unit second) {
    }

    @Provide
    Arbitrary<Triple> unitsOfOneGroup() {
        final List<Class<? extends Unit>> groups = UnitElements.all().stream().map(UnitElements::getGroup).distinct().toList();
        return Arbitraries.of(groups).flatMap(group -> {
            final List<Unit> units = UnitElements.all().stream().filter(unit -> UnitElements.getGroup(unit) == group).toList();
            final Arbitrary<Unit> any = Arbitraries.of(units);
            return Combinators.combine(any, any, any).as(Triple::new);
        });
    }

    @Provide
    Arbitrary<Pair> unitsOfDifferentGroups() {
        final Arbitrary<Unit> any = Arbitraries.of(UnitElements.all());
        return Combinators.combine(any, any).as(Pair::new).filter(pair -> UnitElements.getGroup(pair.first()) != UnitElements.getGroup(pair.second()));
    }

    @Provide
    Arbitrary<BigDecimal> values() {
        return Combinators.combine(
                        Arbitraries.bigIntegers().between(BigInteger.ONE, BigInteger.TEN.pow(MAX_DIGITS)),
                        Arbitraries.integers().between(0, MAX_SCALE))
                .as((digits, scale) -> new BigDecimal(digits, scale));
    }

    private static void assertCloseTo(final BigDecimal expected, final BigNumber actual, final String description) {
        final BigDecimal allowed = expected.abs().multiply(RELATIVE_TOLERANCE);
        assertTrue(expected.subtract(actual.toBigDecimal()).abs().compareTo(allowed) <= 0,
                description + ": expected " + expected.round(new MathContext(30)) + " but was " + actual.toBigDecimal().round(new MathContext(30)));
    }

    @Property(tries = TRIES, seed = SEED)
    void convertingThereAndBackReturnsTheValue(@ForAll("unitsOfOneGroup") final Triple units, @ForAll("values") final BigDecimal value) {
        final BigNumber there = converter.convertToBigNumber(new BigNumber(value.toPlainString()), units.first(), units.second());
        final BigNumber back = converter.convertToBigNumber(there, units.second(), units.first());

        assertCloseTo(value, back, value.toPlainString() + " " + units.first() + " via " + units.second());
    }

    @Property(tries = TRIES, seed = SEED)
    void convertingThroughAThirdUnitGivesTheDirectResult(@ForAll("unitsOfOneGroup") final Triple units, @ForAll("values") final BigDecimal value) {
        final BigNumber input = new BigNumber(value.toPlainString());

        final BigNumber direct = converter.convertToBigNumber(input, units.first(), units.third());
        final BigNumber viaSecond = converter.convertToBigNumber(converter.convertToBigNumber(input, units.first(), units.second()), units.second(), units.third());

        assertCloseTo(direct.toBigDecimal(), viaSecond, value.toPlainString() + " " + units.first() + " to " + units.third() + " via " + units.second());
    }

    @Property(tries = TRIES, seed = SEED)
    void convertingToTheSameUnitKeepsTheValue(@ForAll("unitsOfOneGroup") final Triple units, @ForAll("values") final BigDecimal value) {
        final BigNumber result = converter.convertToBigNumber(new BigNumber(value.toPlainString()), units.first(), units.first());

        assertCloseTo(value, result, value.toPlainString() + " " + units.first());
    }

    @Property(tries = TRIES, seed = SEED)
    void unitsOfDifferentGroupsCannotBeConverted(@ForAll("unitsOfDifferentGroups") final Pair units, @ForAll("values") final BigDecimal value) {
        assertThrows(UnitConversionException.class,
                () -> converter.convertToBigNumber(new BigNumber(value.toPlainString()), units.first(), units.second()),
                units.first() + " to " + units.second());
    }

}
