/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 */

package com.mlprograms.justmath.converter.unit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitElementsTest {

    @Test
    void shouldFindUnitBySymbol() {
        assertEquals(Unit.LENGTH.METER, UnitElements.findBySymbol("m").orElseThrow());
    }

    @Test
    void shouldProvideUnitsByCategory() {
        assertTrue(UnitElements.unitsByCategory(UnitCategory.LENGTH).contains(Unit.LENGTH.METER));
        assertTrue(UnitElements.unitsByCategory(UnitCategory.MASS).contains(Unit.MASS.KILOGRAM));
        assertTrue(UnitElements.unitsByCategory(UnitCategory.TEMPERATURE).contains(Unit.TEMPERATURE.KELVIN));
    }

    @Test
    void shouldRejectDuplicateSymbolWithDifferentDefinition() {
        Unit duplicate = new Unit(
                UnitCategory.LENGTH,
                "Meter duplicate",
                "m",
                Unit.LENGTH.KILOMETER.getFactorToBase(),
                Unit.LENGTH.KILOMETER.getConversionFormula()
        );

        assertThrows(IllegalArgumentException.class, () -> UnitElements.register(duplicate));
    }

}
