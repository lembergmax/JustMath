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
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.math.MathContext;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
class UnitDefinitionRegistry {

    private static final BigNumber ZERO = new BigNumber("0");

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class UnitDefinition {

        @NonNull
        private final UnitCategory category;
        @NonNull
        private final String displayName;
        @NonNull
        private final String symbol;
        @NonNull
        private final BigNumber factorToBase;
        @NonNull
        private final BigNumber offsetToBase;

        BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
            return value.multiply(factorToBase).add(offsetToBase);
        }

        BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
            return baseValue.subtract(offsetToBase).divide(factorToBase, mathContext);
        }
    }

    private static UnitDefinition linear(final UnitCategory category,
                                         final String displayName,
                                         final String symbol,
                                         final String factorToBase) {
        return new UnitDefinition(category, displayName, symbol, new BigNumber(factorToBase), ZERO);
    }

    private static UnitDefinition affine(final UnitCategory category,
                                         final String displayName,
                                         final String symbol,
                                         final String factorToBase,
                                         final String offsetToBase) {
        return new UnitDefinition(category, displayName, symbol, new BigNumber(factorToBase), new BigNumber(offsetToBase));
    }

    private static final Map<Unit, UnitDefinition> BY_UNIT = Map.ofEntries(
            Map.entry(Unit.KILOMETER, linear(UnitCategory.LENGTH, "Kilometer", "km", "1000")),
            Map.entry(Unit.HECTOMETER, linear(UnitCategory.LENGTH, "Hectometer", "hm", "100")),
            Map.entry(Unit.METER, linear(UnitCategory.LENGTH, "Meter", "m", "1")),
            Map.entry(Unit.DECIMETER, linear(UnitCategory.LENGTH, "Decimeter", "dm", "0.1")),
            Map.entry(Unit.CENTIMETER, linear(UnitCategory.LENGTH, "Centimeter", "cm", "0.01")),
            Map.entry(Unit.MILLIMETER, linear(UnitCategory.LENGTH, "Millimeter", "mm", "0.001")),
            Map.entry(Unit.MICROMETER, linear(UnitCategory.LENGTH, "Micrometer", "um", "0.000001")),
            Map.entry(Unit.NANOMETER, linear(UnitCategory.LENGTH, "Nanometer", "nm", "0.000000001")),
            Map.entry(Unit.ANGSTROM, linear(UnitCategory.LENGTH, "Angstrom", "A", "0.0000000001")),
            Map.entry(Unit.PICOMETER, linear(UnitCategory.LENGTH, "Picometer", "pm", "0.000000000001")),
            Map.entry(Unit.FEMTOMETER, linear(UnitCategory.LENGTH, "Femtometer", "fm", "0.000000000000001")),
            Map.entry(Unit.INCH, linear(UnitCategory.LENGTH, "Inch", "in", "0.0254")),
            Map.entry(Unit.FEET, linear(UnitCategory.LENGTH, "Foot", "ft", "0.3048")),
            Map.entry(Unit.YARD, linear(UnitCategory.LENGTH, "Yard", "yd", "0.9144")),
            Map.entry(Unit.MILE, linear(UnitCategory.LENGTH, "Mile", "mi", "1609.344")),
            Map.entry(Unit.NAUTICAL_MILE, linear(UnitCategory.LENGTH, "Nautical Mile", "nmi", "1852")),
            Map.entry(Unit.LIGHT_YEAR, linear(UnitCategory.LENGTH, "Light Year", "ly", "9460730472580800")),
            Map.entry(Unit.PARSEC, linear(UnitCategory.LENGTH, "Parsec", "pc", "30856775814913673")),
            Map.entry(Unit.PIXEL, linear(UnitCategory.LENGTH, "Pixel", "px", "0.0002645833333333")),
            Map.entry(Unit.POINT, linear(UnitCategory.LENGTH, "Point", "pt", "0.0003527777777778")),
            Map.entry(Unit.PICA, linear(UnitCategory.LENGTH, "Pica", "pc_typ", "0.0042333333333333")),
            Map.entry(Unit.EM, linear(UnitCategory.LENGTH, "Em", "em", "0.0042333333333333")),
            Map.entry(Unit.KILOGRAM, linear(UnitCategory.MASS, "Kilogram", "kg", "1")),
            Map.entry(Unit.GRAM, linear(UnitCategory.MASS, "Gram", "g", "0.001")),
            Map.entry(Unit.MILLIGRAM, linear(UnitCategory.MASS, "Milligram", "mg", "0.000001")),
            Map.entry(Unit.POUND, linear(UnitCategory.MASS, "Pound", "lb", "0.45359237")),
            Map.entry(Unit.OUNCE, linear(UnitCategory.MASS, "Ounce", "oz", "0.028349523125")),
            Map.entry(Unit.KELVIN, linear(UnitCategory.TEMPERATURE, "Kelvin", "K", "1")),
            Map.entry(Unit.CELSIUS, affine(UnitCategory.TEMPERATURE, "Celsius", "°C", "1", "273.15")),
            Map.entry(Unit.FAHRENHEIT, affine(UnitCategory.TEMPERATURE, "Fahrenheit", "°F", "0.5555555555555556", "255.3722222222222"))
    );

    private static final Map<String, Unit> BY_SYMBOL;
    private static final Map<UnitCategory, List<Unit>> BY_CATEGORY;

    static {
        Map<String, Unit> symbols = new java.util.HashMap<>();
        Map<UnitCategory, List<Unit>> unitsByCategory = new EnumMap<>(UnitCategory.class);

        for (Unit unit : Unit.values()) {
            UnitDefinition unitDefinition = requireDefinition(unit);
            symbols.put(unitDefinition.symbol, unit);
            unitsByCategory.computeIfAbsent(unitDefinition.category, ignored -> new java.util.ArrayList<>()).add(unit);
        }

        BY_SYMBOL = Map.copyOf(symbols);

        Map<UnitCategory, List<Unit>> immutableByCategory = new EnumMap<>(UnitCategory.class);
        unitsByCategory.forEach((key, value) -> immutableByCategory.put(key, List.copyOf(value)));
        BY_CATEGORY = Map.copyOf(immutableByCategory);
    }

    static UnitDefinition requireDefinition(@NonNull final Unit unit) {
        UnitDefinition definition = BY_UNIT.get(unit);
        if (definition == null) {
            throw new IllegalStateException("No conversion definition found for unit " + unit);
        }
        return definition;
    }

    static Optional<Unit> findBySymbol(final String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_SYMBOL.get(symbol));
    }

    static List<Unit> byCategory(@NonNull final UnitCategory category) {
        return BY_CATEGORY.getOrDefault(category, List.of());
    }

    static List<Unit> allUnits() {
        return List.of(Unit.values());
    }

    static String displayName(@NonNull final Unit unit) {
        return requireDefinition(unit).displayName;
    }

    static String symbol(@NonNull final Unit unit) {
        return requireDefinition(unit).symbol;
    }

    static UnitCategory category(@NonNull final Unit unit) {
        return requireDefinition(unit).category;
    }

}
