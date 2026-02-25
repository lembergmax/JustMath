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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UnitRegistry}.
 *
 * <p>
 * These tests focus on registry integrity and lookup behavior.
 * </p>
 */
final class UnitRegistryTest {

    /**
     * Ensures every enum constant has a definition.
     */
    @Test
    void everyUnitHasDefinition() {
        for (final Unit unit : Unit.values()) {
            assertNotNull(UnitRegistry.requireDefinition(unit), "Missing definition for " + unit);
        }
    }

    /**
     * Ensures symbolRegistry covers all units and symbols are unique.
     */
    @Test
    void symbolRegistryCoversAllUnitsAndIsUnique() {
        final Map<String, Unit> registry = UnitRegistry.symbolRegistry();
        assertNotNull(registry);
        assertFalse(registry.isEmpty());

        // Must contain exactly one entry per enum unit (because every unit has a symbol).
        assertEquals(Unit.values().length, registry.size(), "Expected 1 symbol per unit.");

        // Ensure each symbol points to a non-null unit.
        for (final Map.Entry<String, Unit> entry : registry.entrySet()) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
        }
    }

    /**
     * Ensures category grouping contains all units.
     */
    @Test
    void byCategoryContainsAllUnitsForLength() {
        final List<Unit> units = UnitRegistry.byCategory(UnitCategory.LENGTH);
        assertEquals(Unit.values().length, units.size(), "All units should be LENGTH in the current catalog.");
    }

}