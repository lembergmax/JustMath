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

import java.util.*;

/**
 * Internal single source of truth for all built-in unit definitions.
 *
 * <p>
 * This registry is intentionally package-private to keep the public API surface minimal and stable.
 * Public access is provided via {@link UnitElements}.
 * </p>
 *
 * <h2>Design goals</h2>
 * <ul>
 *   <li><strong>Enums are identifiers only</strong>: {@link Unit.Length} and {@link Unit.Mass} do not carry metadata.</li>
 *   <li><strong>Single place to edit</strong>: add/remove units by editing one list ({@link #BUILT_IN}).</li>
 *   <li><strong>Deterministic and validated</strong>: unit symbols are unique and group mappings are consistent.</li>
 *   <li><strong>Thread-safe</strong>: all registries are immutable after class initialization.</li>
 * </ul>
 *
 * <h2>How to add a new unit</h2>
 * <p>
 * Add exactly one entry to {@link #BUILT_IN}. That is the only place you should need to touch.
 * </p>
 *
 * <pre>
 * define(Unit.Length.MEGAMETER, "Megameter", "Mm", "1000000", "0")
 * </pre>
 *
 * <p>
 * The {@code scaleToBase} and {@code offsetToBase} parameters define the mapping into the group base unit:
 * </p>
 *
 * <pre>
 * base = value * scaleToBase + offsetToBase
 * </pre>
 *
 * <p>
 * For purely linear conversions, use {@code offsetToBase = "0"}.
 * </p>
 */
@UtilityClass
class UnitRegistry {

    /**
     * Declarative list of all built-in units and their definitions.
     *
     * <p>
     * This list is the <strong>only</strong> place you need to edit to add or modify units.
     * The rest of the registry is derived from this list and validated at startup.
     * </p>
     */
    private static final List<UnitSpec> BUILT_IN = List.of(
            // =========================
            // LENGTH (base: meter)
            // =========================
            define(Unit.Length.KILOMETER, "Kilometer", "km", "1000", "0"),
            define(Unit.Length.HECTOMETER, "Hectometer", "hm", "100", "0"),
            define(Unit.Length.METER, "Meter", "m", "1", "0"),
            define(Unit.Length.DECIMETER, "Decimeter", "dm", "0.1", "0"),
            define(Unit.Length.CENTIMETER, "Centimeter", "cm", "0.01", "0"),
            define(Unit.Length.MILLIMETER, "Millimeter", "mm", "0.001", "0"),
            define(Unit.Length.MICROMETER, "Micrometer", "um", "0.000001", "0"),
            define(Unit.Length.NANOMETER, "Nanometer", "nm", "0.000000001", "0"),
            define(Unit.Length.ANGSTROM, "Angstrom", "A", "0.0000000001", "0"),
            define(Unit.Length.PICOMETER, "Picometer", "pm", "0.000000000001", "0"),
            define(Unit.Length.FEMTOMETER, "Femtometer", "fm", "0.000000000000001", "0"),
            define(Unit.Length.INCH, "Inch", "in", "0.0254", "0"),
            define(Unit.Length.FEET, "Foot", "ft", "0.3048", "0"),
            define(Unit.Length.YARD, "Yard", "yd", "0.9144", "0"),
            define(Unit.Length.MILE, "Mile", "mi", "1609.344", "0"),
            define(Unit.Length.NAUTICAL_MILE, "Nautical Mile", "nmi", "1852", "0"),
            define(Unit.Length.LIGHT_YEAR, "Light Year", "ly", "9460730472580800", "0"),
            define(Unit.Length.PARSEC, "Parsec", "pc", "30856775814913673", "0"),
            define(Unit.Length.PIXEL, "Pixel", "px", "0.0002645833333333", "0"),
            define(Unit.Length.POINT, "Point", "pt", "0.0003527777777778", "0"),
            define(Unit.Length.PICA, "Pica", "pica", "0.0042333333333333", "0"),
            define(Unit.Length.EM, "Em", "em", "0.0042333333333333", "0"),

            // =========================
            // MASS (base: kilogram)
            // =========================
            define(Unit.Mass.TONNE, "Tonne", "t", "1000", "0"),
            define(Unit.Mass.KILOGRAM, "Kilogram", "kg", "1", "0"),
            define(Unit.Mass.GRAM, "Gram", "g", "0.001", "0"),
            define(Unit.Mass.MILLIGRAM, "Milligram", "mg", "0.000001", "0"),
            define(Unit.Mass.POUND, "Pound", "lb", "0.45359237", "0"),
            define(Unit.Mass.OUNCE, "Ounce", "oz", "0.028349523125", "0")
    );

    /**
     * Map from unit identifier to its immutable {@link UnitDefinition}.
     *
     * <p>
     * This is the canonical lookup structure used by the public facade.
     * </p>
     */
    private static final Map<Unit, UnitDefinition> BY_UNIT;

    /**
     * Map from unit symbol to unit identifier.
     *
     * <p>
     * Symbols are treated as case-sensitive because some real-world symbols are case-sensitive
     * (e.g., {@code "K"} vs {@code "k"}). Validation ensures every symbol is unique.
     * </p>
     */
    private static final Map<String, Unit> BY_SYMBOL;

    /**
     * Map from unit group type (e.g., {@code Unit.Length.class}) to an immutable list of units
     * belonging to that group.
     *
     * <p>
     * This mapping enables compatibility checks and group-wise listing without a separate
     * {@code UnitCategory} enum.
     * </p>
     */
    private static final Map<Class<? extends Unit>, List<Unit>> BY_GROUP;

    static {
        final Map<Unit, UnitDefinition> byUnit = new LinkedHashMap<>();
        final Map<String, Unit> bySymbol = new HashMap<>();
        final Map<Class<? extends Unit>, List<Unit>> byGroup = new LinkedHashMap<>();

        for (final UnitSpec spec : BUILT_IN) {
            final Unit unit = spec.unit();
            final UnitDefinition definition = spec.definition();

            final UnitDefinition previousDef = byUnit.put(unit, definition);
            if (previousDef != null) {
                throw new IllegalStateException("Duplicate unit definition detected for: " + unit);
            }

            final Unit previousUnit = bySymbol.put(definition.symbol(), unit);
            if (previousUnit != null) {
                throw new IllegalStateException(
                        "Duplicate unit symbol detected: '" + definition.symbol() + "' used by " + previousUnit + " and " + unit
                );
            }

            final Class<? extends Unit> groupType = groupTypeOf(unit);
            byGroup.computeIfAbsent(groupType, ignored -> new ArrayList<>()).add(unit);
        }

        BY_UNIT = Map.copyOf(byUnit);
        BY_SYMBOL = Map.copyOf(bySymbol);

        final Map<Class<? extends Unit>, List<Unit>> immutableGroupMap = new LinkedHashMap<>();
        for (final Map.Entry<Class<? extends Unit>, List<Unit>> entry : byGroup.entrySet()) {
            immutableGroupMap.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        BY_GROUP = Map.copyOf(immutableGroupMap);
    }

    /**
     * Returns the immutable {@link UnitDefinition} for a given unit identifier or throws
     * if no definition exists.
     *
     * @param unit the unit identifier; must not be {@code null}
     * @return immutable unit definition; never {@code null}
     * @throws IllegalStateException if no definition exists for {@code unit}
     */
    static UnitDefinition requireDefinition(@NonNull final Unit unit) {
        final UnitDefinition definition = BY_UNIT.get(unit);
        if (definition == null) {
            throw new IllegalStateException("No unit definition found for unit: " + unit);
        }
        return definition;
    }

    /**
     * Finds a unit identifier by its symbol.
     *
     * @param symbol the symbol to look up (e.g., {@code "km"}, {@code "kg"}); may be {@code null}
     * @return optional unit identifier; empty if {@code symbol} is {@code null} or unknown
     */
    static Optional<Unit> findBySymbol(final String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_SYMBOL.get(symbol));
    }

    /**
     * Returns all units of the given group (e.g., {@code Unit.Length.class}).
     *
     * @param groupType the group type; must not be {@code null}
     * @return immutable list of units; never {@code null}
     */
    static List<Unit> unitsOfGroup(@NonNull final Class<? extends Unit> groupType) {
        return BY_GROUP.getOrDefault(groupType, List.of());
    }

    /**
     * Returns all built-in units in deterministic registry order.
     *
     * <p>
     * The order is the order of {@link #BUILT_IN}, which is intended to be stable and human-controlled.
     * </p>
     *
     * @return immutable list of all units; never {@code null}
     */
    static List<Unit> allUnits() {
        return List.copyOf(BY_UNIT.keySet());
    }

    /**
     * Returns the display name of a unit.
     *
     * @param unit the unit identifier; must not be {@code null}
     * @return display name; never {@code null}
     */
    static String displayName(@NonNull final Unit unit) {
        return requireDefinition(unit).displayName();
    }

    /**
     * Returns the symbol of a unit.
     *
     * @param unit the unit identifier; must not be {@code null}
     * @return symbol; never {@code null}
     */
    static String symbol(@NonNull final Unit unit) {
        return requireDefinition(unit).symbol();
    }

    /**
     * Checks whether two units belong to the same group type.
     *
     * <p>
     * This replaces the need for a separate {@code UnitCategory} enum. Group identity is derived
     * from the unit's declaring enum type (e.g., {@code Unit.Length} or {@code Unit.Mass}).
     * </p>
     *
     * @param left the left unit; must not be {@code null}
     * @param right the right unit; must not be {@code null}
     * @return {@code true} if both units are in the same group; otherwise {@code false}
     */
    static boolean areCompatible(@NonNull final Unit left, @NonNull final Unit right) {
        return groupTypeOf(left).equals(groupTypeOf(right));
    }

    /**
     * Creates one declarative built-in definition entry.
     *
     * <p>
     * The conversion is defined by the affine mapping into the group base unit:
     * </p>
     *
     * <pre>
     * base = value * scaleToBase + offsetToBase
     * </pre>
     *
     * @param unit the unit identifier; must not be {@code null}
     * @param displayName human-readable display name; must not be {@code null}
     * @param symbol unit symbol; must not be {@code null}
     * @param scaleToBase multiplicative factor into base unit; must not be {@code null}
     * @param offsetToBase additive offset into base unit; must not be {@code null}
     * @return immutable unit spec entry; never {@code null}
     */
    private static UnitSpec define(
            @NonNull final Unit unit,
            @NonNull final String displayName,
            @NonNull final String symbol,
            @NonNull final String scaleToBase,
            @NonNull final String offsetToBase
    ) {
        final BigNumber scale = new BigNumber(scaleToBase);
        final BigNumber offset = new BigNumber(offsetToBase);

        final ConversionFormula formula = ConversionFormulas.affine(scale, offset);
        final UnitDefinition definition = new UnitDefinition(displayName, symbol, formula);

        return new UnitSpec(unit, definition);
    }

    /**
     * Determines the logical group type of a unit.
     *
     * <p>
     * For enum-based units, the group type is the declaring enum type (e.g., {@code Unit.Length.class}).
     * This makes grouping stable and independent of enum constant-specific classes.
     * </p>
     *
     * @param unit unit identifier; must not be {@code null}
     * @return the group type; never {@code null}
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Unit> groupTypeOf(@NonNull final Unit unit) {
        if (unit instanceof Enum<?> enumValue) {
            final Class<?> declaring = enumValue.getDeclaringClass();
            return (Class<? extends Unit>) declaring;
        }
        return unit.getClass();
    }

    /**
     * Internal immutable pair of a unit identifier and its {@link UnitDefinition}.
     *
     * <p>
     * This is purely a registry construction artifact to keep {@link #BUILT_IN} readable.
     * </p>
     *
     * @param unit the unit identifier
     * @param definition the unit definition
     */
    private record UnitSpec(Unit unit, UnitDefinition definition) { }

}