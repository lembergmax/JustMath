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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Unit constants that were renamed in 1.7.0 keep working: the old constant is a deprecated alias that
 * resolves to the correctly named unit. {@code Unit.DataStorage.KILOBYTE} and its siblings were binary
 * units (1024 bytes) with decimal-sounding names.
 */
@SuppressWarnings("deprecation")
final class UnitAliasTest {

    /**
     * Deprecated constants that never had a registry definition. A poundal is a unit of force, so
     * {@code Mass.POUNDAL} was misfiled; {@code Force.POUNDAL} is the defined unit.
     */
    private static final Set<String> DEPRECATED_WITHOUT_DEFINITION = Set.of("Mass.POUNDAL");

    private final UnitConverter converter = new UnitConverter(new MathContext(50));

    static Stream<Arguments> deprecatedUnits() {
        return Stream.of(
                Arguments.of(Unit.Length.NAIL_COTH, Unit.Length.NAIL_CLOTH),
                Arguments.of(Unit.Mass.CARRAT, Unit.Mass.CARAT),
                Arguments.of(Unit.DataStorage.KILOBIT, Unit.DataStorage.KIBIBIT),
                Arguments.of(Unit.DataStorage.KILOBYTE, Unit.DataStorage.KIBIBYTE),
                Arguments.of(Unit.DataStorage.MEGABIT, Unit.DataStorage.MEBIBIT),
                Arguments.of(Unit.DataStorage.MEGABYTE, Unit.DataStorage.MEBIBYTE),
                Arguments.of(Unit.DataStorage.GIGABIT, Unit.DataStorage.GIBIBIT),
                Arguments.of(Unit.DataStorage.GIGABYTE, Unit.DataStorage.GIBIBYTE),
                Arguments.of(Unit.DataStorage.TERABIT, Unit.DataStorage.TEBIBIT),
                Arguments.of(Unit.DataStorage.TERABYTE, Unit.DataStorage.TEBIBYTE),
                Arguments.of(Unit.DataStorage.PETABIT, Unit.DataStorage.PEBIBIT),
                Arguments.of(Unit.DataStorage.PETABYTE, Unit.DataStorage.PEBIBYTE),
                Arguments.of(Unit.DataStorage.EXABIT, Unit.DataStorage.EXBIBIT),
                Arguments.of(Unit.DataStorage.EXABYTE, Unit.DataStorage.EXBIBYTE)
        );
    }

    @ParameterizedTest(name = "{0} resolves to {1}")
    @MethodSource("deprecatedUnits")
    @DisplayName("a deprecated constant has the metadata and the conversion result of its canonical unit")
    void deprecatedConstantResolvesToTheCanonicalUnit(final Unit deprecated, final Unit canonical) {
        final Unit base = UnitElements.getBaseUnit(canonical);

        assertEquals(UnitElements.getSymbol(canonical), UnitElements.getSymbol(deprecated));
        assertEquals(UnitElements.getDisplayName(canonical), UnitElements.getDisplayName(deprecated));
        assertEquals(base, UnitElements.getBaseUnit(deprecated));
        assertTrue(UnitElements.areCompatible(deprecated, canonical));
        assertEquals(canonical, UnitElements.parseUnit(UnitElements.getSymbol(deprecated)));
        assertEquals(
                converter.convertToBigNumber("1", canonical, base),
                converter.convertToBigNumber("1", deprecated, base)
        );
    }

    @ParameterizedTest(name = "{0} is listed neither in all() nor in the symbol registry")
    @MethodSource("deprecatedUnits")
    @DisplayName("an alias does not appear as a second entry in the registry")
    void aliasIsNotListedAsAUnitOfItsOwn(final Unit deprecated, final Unit canonical) {
        assertFalse(UnitElements.all().contains(deprecated));
        assertTrue(UnitElements.all().contains(canonical));
        assertFalse(UnitElements.getRegistry().containsValue(deprecated));
    }

    @ParameterizedTest(name = "{0} is annotated @Deprecated")
    @MethodSource("deprecatedUnits")
    @DisplayName("every alias carries the Deprecated annotation")
    void aliasIsMarkedDeprecated(final Unit deprecated, final Unit canonical) throws NoSuchFieldException {
        final Enum<?> constant = (Enum<?>) deprecated;
        final Field field = constant.getDeclaringClass().getField(constant.name());

        assertTrue(field.isAnnotationPresent(Deprecated.class), constant + " must be @Deprecated");
    }

    @Test
    @DisplayName("every enum constant is a listed unit, a deprecated alias or a documented deprecated constant without definition")
    void everyEnumConstantHasAKnownRole() throws NoSuchFieldException {
        final Set<Unit> listed = new HashSet<>(UnitElements.all());
        final Set<Unit> aliases = deprecatedUnits().map(arguments -> (Unit) arguments.get()[0]).collect(Collectors.toSet());
        final List<String> offenders = new ArrayList<>();

        for (final Class<?> group : Unit.class.getClasses()) {
            if (!group.isEnum()) {
                continue;
            }
            for (final Object constant : group.getEnumConstants()) {
                final Enum<?> unit = (Enum<?>) constant;
                final String name = group.getSimpleName() + "." + unit.name();
                final boolean deprecated = group.getField(unit.name()).isAnnotationPresent(Deprecated.class);
                final boolean isListedUnit = listed.contains((Unit) unit) && !deprecated;
                final boolean isAlias = !listed.contains((Unit) unit) && deprecated && aliases.contains((Unit) unit);
                final boolean isDocumentedWithoutDefinition = !listed.contains((Unit) unit) && deprecated && DEPRECATED_WITHOUT_DEFINITION.contains(name);

                if (!(isListedUnit || isAlias || isDocumentedWithoutDefinition)) {
                    offenders.add(name);
                }
            }
        }

        assertEquals(List.of(), offenders, "constants without a registry definition that are not documented as deprecated");
    }

    @Test
    @DisplayName("the binary and the decimal kilobyte are different units")
    void binaryAndDecimalKilobyteDiffer() {
        assertEquals(new BigNumber("1024"), converter.convertToBigNumber("1", Unit.DataStorage.KIBIBYTE, Unit.DataStorage.BYTE));
        assertEquals(new BigNumber("1000"), converter.convertToBigNumber("1", Unit.DataStorage.KILOBYTE_DECIMAL, Unit.DataStorage.BYTE));
    }
}
