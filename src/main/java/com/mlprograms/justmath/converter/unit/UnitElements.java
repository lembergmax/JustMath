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

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.experimental.UtilityClass;

import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Öffentliche Lookup-API für Einheiten.
 */
@UtilityClass
public class UnitElements {

    public static Optional<Unit> findBySymbol(final String symbol) {
        return UnitDefinitionRegistry.findBySymbol(symbol);
    }

    public static UnitCategory getCategory(final Unit unit) {
        return UnitDefinitionRegistry.category(unit);
    }

    public static String getDisplayName(final Unit unit) {
        return UnitDefinitionRegistry.displayName(unit);
    }

    public static String getSymbol(final Unit unit) {
        return UnitDefinitionRegistry.symbol(unit);
    }

    public static List<Unit> byCategory(final UnitCategory category) {
        return UnitDefinitionRegistry.byCategory(category);
    }

    public static List<Unit> all() {
        return UnitDefinitionRegistry.allUnits();
    }

    public static BigNumber toBase(final Unit unit,
                                   final BigNumber value,
                                   final MathContext mathContext) {
        return UnitDefinitionRegistry.requireDefinition(unit).toBase(value, mathContext);
    }

    public static BigNumber fromBase(final Unit unit,
                                     final BigNumber value,
                                     final MathContext mathContext) {
        return UnitDefinitionRegistry.requireDefinition(unit).fromBase(value, mathContext);
    }

    /**
     * @deprecated Units sind statisch hinterlegt; Registrierung zur Laufzeit wird nicht unterstützt.
     */
    @Deprecated(forRemoval = true)
    public static void register(final Unit unit) {
        throw new UnsupportedOperationException("Runtime registration is not supported. Add the unit to UnitDefinitionRegistry.");
    }

    public static Map<String, Unit> getRegistry() {
        Map<String, Unit> registry = new LinkedHashMap<>();
        for (Unit unit : UnitDefinitionRegistry.allUnits()) {
            registry.put(UnitDefinitionRegistry.symbol(unit), unit);
        }
        return Map.copyOf(registry);
    }

}
