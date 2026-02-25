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

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Internal single source of truth for all built-in unit definitions.
 * <p>
 * This registry is package-private by design. Public access is provided via {@link UnitElements}.
 * </p>
 *
 * <p>
 * The registry validates:
 * </p>
 * <ul>
 *   <li>each {@link Unit} has exactly one definition</li>
 *   <li>unit symbols are unique</li>
 *   <li>category mappings are consistent</li>
 * </ul>
 */
@UtilityClass
class UnitRegistry {

    /**
     * Map from {@link Unit} identifiers to their immutable {@link UnitDefinition}.
     */
    private static final Map<Unit, UnitDefinition> BY_UNIT = Map.ofEntries(
            Map.entry(Unit.KILOMETER, linear(UnitCategory.LENGTH, "Kilometer", "km", new BigNumber("1000"))),
            Map.entry(Unit.HECTOMETER, linear(UnitCategory.LENGTH, "Hectometer", "hm", new BigNumber("100"))),
            Map.entry(Unit.METER, linear(UnitCategory.LENGTH, "Meter", "m", new BigNumber("1"))),
            Map.entry(Unit.DECIMETER, linear(UnitCategory.LENGTH, "Decimeter", "dm", new BigNumber("0.1"))),
            Map.entry(Unit.CENTIMETER, linear(UnitCategory.LENGTH, "Centimeter", "cm", new BigNumber("0.01"))),
            Map.entry(Unit.MILLIMETER, linear(UnitCategory.LENGTH, "Millimeter", "mm", new BigNumber("0.001"))),
            Map.entry(Unit.MICROMETER, linear(UnitCategory.LENGTH, "Micrometer", "um", new BigNumber("0.000001"))),
            Map.entry(Unit.NANOMETER, linear(UnitCategory.LENGTH, "Nanometer", "nm", new BigNumber("0.000000001"))),
            Map.entry(Unit.ANGSTROM, linear(UnitCategory.LENGTH, "Angstrom", "A", new BigNumber("0.0000000001"))),
            Map.entry(Unit.PICOMETER, linear(UnitCategory.LENGTH, "Picometer", "pm", new BigNumber("0.000000000001"))),
            Map.entry(Unit.FEMTOMETER, linear(UnitCategory.LENGTH, "Femtometer", "fm", new BigNumber("0.000000000000001"))),
            Map.entry(Unit.INCH, linear(UnitCategory.LENGTH, "Inch", "in", new BigNumber("0.0254"))),
            Map.entry(Unit.FEET, linear(UnitCategory.LENGTH, "Foot", "ft", new BigNumber("0.3048"))),
            Map.entry(Unit.YARD, linear(UnitCategory.LENGTH, "Yard", "yd", new BigNumber("0.9144"))),
            Map.entry(Unit.MILE, linear(UnitCategory.LENGTH, "Mile", "mi", new BigNumber("1609.344"))),
            Map.entry(Unit.NAUTICAL_MILE, linear(UnitCategory.LENGTH, "Nautical Mile", "nmi", new BigNumber("1852"))),
            Map.entry(Unit.LIGHT_YEAR, linear(UnitCategory.LENGTH, "Light Year", "ly", new BigNumber("9460730472580800"))),
            Map.entry(Unit.PARSEC, linear(UnitCategory.LENGTH, "Parsec", "pc", new BigNumber("30856775814913673"))),
            Map.entry(Unit.PIXEL, linear(UnitCategory.LENGTH, "Pixel", "px", new BigNumber("0.0002645833333333"))),
            Map.entry(Unit.POINT, linear(UnitCategory.LENGTH, "Point", "pt", new BigNumber("0.0003527777777778"))),
            Map.entry(Unit.PICA, linear(UnitCategory.LENGTH, "Pica", "pica", new BigNumber("0.0042333333333333"))),
            Map.entry(Unit.EM, linear(UnitCategory.LENGTH, "Em", "em", new BigNumber("0.0042333333333333")))
    );

    /**
     * Map from unit symbol to unit identifier.
     */
    private static final Map<String, Unit> BY_SYMBOL;

    /**
     * Map from category to immutable list of unit identifiers.
     */
    private static final Map<UnitCategory, List<Unit>> BY_CATEGORY;

    static {
        final Map<String, Unit> symbols = new HashMap<>();
        final Map<UnitCategory, List<Unit>> byCategory = new EnumMap<>(UnitCategory.class);

        for (final Unit unit : Unit.values()) {
            final UnitDefinition definition = requireDefinition(unit);

            final Unit previous = symbols.put(definition.symbol(), unit);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate unit symbol detected: '" + definition.symbol() + "' used by " + previous + " and " + unit
                );
            }

            byCategory.computeIfAbsent(definition.category(), ignored -> new ArrayList<>()).add(unit);
        }

        BY_SYMBOL = Map.copyOf(symbols);

        final Map<UnitCategory, List<Unit>> immutableByCategory = new EnumMap<>(UnitCategory.class);
        for (final Map.Entry<UnitCategory, List<Unit>> entry : byCategory.entrySet()) {
            immutableByCategory.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        BY_CATEGORY = Map.copyOf(immutableByCategory);
    }

    /**
     * Returns the definition for the given unit or throws.
     *
     * @param unit the unit identifier; must not be {@code null}
     * @return unit definition; never {@code null}
     * @throws IllegalStateException if no definition exists for the unit
     */
    static UnitDefinition requireDefinition(@NonNull final Unit unit) {
        final UnitDefinition definition = BY_UNIT.get(unit);
        if (definition == null) {
            throw new IllegalStateException("No unit definition found for unit: " + unit);
        }
        return definition;
    }

    /**
     * Finds a unit by its symbol.
     *
     * @param symbol symbol to look up; may be {@code null}
     * @return optional unit identifier; empty if {@code symbol} is {@code null} or unknown
     */
    static Optional<Unit> findBySymbol(final String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_SYMBOL.get(symbol));
    }

    /**
     * Returns all units belonging to the given category.
     *
     * @param category category; must not be {@code null}
     * @return immutable list of units; never {@code null}
     */
    static List<Unit> byCategory(@NonNull final UnitCategory category) {
        return BY_CATEGORY.getOrDefault(category, List.of());
    }

    /**
     * Returns all units in enum declaration order.
     *
     * @return immutable list of all units
     */
    static List<Unit> allUnits() {
        return List.of(Unit.values());
    }

    /**
     * Returns the display name of a unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return display name; never {@code null}
     */
    static String displayName(@NonNull final Unit unit) {
        return requireDefinition(unit).displayName();
    }

    /**
     * Returns the symbol of a unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return symbol; never {@code null}
     */
    static String symbol(@NonNull final Unit unit) {
        return requireDefinition(unit).symbol();
    }

    /**
     * Returns the category of a unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return category; never {@code null}
     */
    static UnitCategory category(@NonNull final Unit unit) {
        return requireDefinition(unit).category();
    }

    /**
     * Builds a linear unit definition using {@link ConversionFormulas#linear(BigNumber)}.
     *
     * @param category     category; must not be {@code null}
     * @param displayName  display name; must not be {@code null}
     * @param symbol       unit symbol; must not be {@code null}
     * @param factorToBase scale factor into the category base unit; must not be {@code null}
     * @return immutable unit definition
     */
    private static UnitDefinition linear(
            @NonNull final UnitCategory category,
            @NonNull final String displayName,
            @NonNull final String symbol,
            @NonNull final BigNumber factorToBase
    ) {
        return new UnitDefinition(category, displayName, symbol, ConversionFormulas.linear(factorToBase));
    }

    /**
     * Builds an affine unit definition using {@link ConversionFormulas#affine(BigNumber, BigNumber)}.
     *
     * @param category    category; must not be {@code null}
     * @param displayName display name; must not be {@code null}
     * @param symbol      unit symbol; must not be {@code null}
     * @param scale       multiplicative factor into base unit; must not be {@code null}
     * @param offset      additive offset into base unit; must not be {@code null}
     * @return immutable unit definition
     */
    private static UnitDefinition affine(
            @NonNull final UnitCategory category,
            @NonNull final String displayName,
            @NonNull final String symbol,
            @NonNull final BigNumber scale,
            @NonNull final BigNumber offset
    ) {
        return new UnitDefinition(category, displayName, symbol, ConversionFormulas.affine(scale, offset));
    }

}