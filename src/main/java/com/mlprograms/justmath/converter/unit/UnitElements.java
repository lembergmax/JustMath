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

package com.mlprograms.justmath.converter.unit;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class UnitElements {

    @Getter
    private static final Map<String, Unit> registry = new HashMap<>();

    static {
        List<Unit> units = Unit.LENGTH.all();

        for (Unit unit : units) {
            register(unit);
        }
    }

    /**
     * Finds a {@link Unit} by its symbol.
     *
     * @param symbol the symbol to look up
     * @return an {@link Optional} containing the found {@code Unit}, or empty if not found
     */
    public static Optional<Unit> findBySymbol(String symbol) {
        return Optional.ofNullable(registry.get(symbol));
    }

    /**
     * Registers an {@link Unit} in the registry.
     * The unit is mapped by its symbol for a later lookup.
     *
     * @param unit the {@code Unit} to register
     */
    public static void register(Unit unit) {
        registry.put(unit.getSymbol(), unit);
    }

}
