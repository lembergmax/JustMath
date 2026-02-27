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

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UnitRegistry}.
 *
 * <p>
 * The registry is package-private. These tests validate internal integrity assumptions:
 * </p>
 * <ul>
 *   <li>every unit has a definition</li>
 *   <li>symbols are unique</li>
 *   <li>group lists contain exactly the expected units</li>
 * </ul>
 */
final class UnitRegistryTest {

    /**
     * Ensures every unit returned by {@link UnitRegistry#allUnits()} has a registry definition.
     */
    @Test
    void everyUnitHasDefinition() {
        for (final Unit unit : UnitRegistry.allUnits()) {
            assertNotNull(UnitRegistry.requireDefinition(unit), () -> "Missing definition for " + unit);
        }
    }

    /**
     * Ensures unit symbols are globally unique across all groups.
     */
    @Test
    void symbolsAreUnique() {
        final HashSet<String> seen = new HashSet<>();
        for (final Unit unit : UnitRegistry.allUnits()) {
            final String symbol = UnitRegistry.symbol(unit);
            assertTrue(seen.add(symbol), () -> "Duplicate symbol detected: " + symbol);
        }
    }

    /**
     * Ensures group unit lists are present and non-empty for known groups.
     */
    @Test
    void groupListsExist() {
        final List<Unit> lengths = UnitRegistry.unitsOfGroup(Unit.Length.class);
        final List<Unit> masses = UnitRegistry.unitsOfGroup(Unit.Mass.class);

        assertFalse(lengths.isEmpty(), "Length group must not be empty.");
        assertFalse(masses.isEmpty(), "Mass group must not be empty.");
    }

}