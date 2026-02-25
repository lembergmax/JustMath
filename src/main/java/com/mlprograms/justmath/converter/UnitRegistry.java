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
import com.mlprograms.justmath.bignumber.BigNumbers;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.*;

/**
 * Internal single source of truth for all built-in unit definitions.
 *
 * <p>
 * This registry is package-private by design. Public access is provided via {@link UnitElements}.
 * </p>
 *
 * <p>
 * Responsibilities:
 * </p>
 * <ul>
 *   <li>Define immutable {@link UnitDefinition} instances for each {@link Unit}</li>
 *   <li>Build fast lookup indices (symbol -> unit, category -> units)</li>
 *   <li>Validate uniqueness (e.g., no duplicate symbols)</li>
 * </ul>
 */
@UtilityClass
class UnitRegistry {

    /**
     * Immutable mapping from unit identifiers to their definitions.
     *
     * <p>
     * Each {@link Unit} must have exactly one {@link UnitDefinition}.
     * </p>
     */
    private static final Map<Unit, UnitDefinition> DEFINITIONS = Map.ofEntries(
            Map.entry(Unit.KILOMETER, define(UnitCategory.LENGTH, "Kilometer", "km", new BigNumber("1000"), BigNumbers.ZERO)),
            Map.entry(Unit.HECTOMETER, define(UnitCategory.LENGTH, "Hectometer", "hm", new BigNumber("100"), BigNumbers.ZERO)),
            Map.entry(Unit.METER, define(UnitCategory.LENGTH, "Meter", "m", new BigNumber("1"), BigNumbers.ZERO)),
            Map.entry(Unit.DECIMETER, define(UnitCategory.LENGTH, "Decimeter", "dm", new BigNumber("0.1"), BigNumbers.ZERO)),
            Map.entry(Unit.CENTIMETER, define(UnitCategory.LENGTH, "Centimeter", "cm", new BigNumber("0.01"), BigNumbers.ZERO)),
            Map.entry(Unit.MILLIMETER, define(UnitCategory.LENGTH, "Millimeter", "mm", new BigNumber("0.001"), BigNumbers.ZERO)),
            Map.entry(Unit.MICROMETER, define(UnitCategory.LENGTH, "Micrometer", "um", new BigNumber("0.000001"), BigNumbers.ZERO)),
            Map.entry(Unit.NANOMETER, define(UnitCategory.LENGTH, "Nanometer", "nm", new BigNumber("0.000000001"), BigNumbers.ZERO)),
            Map.entry(Unit.ANGSTROM, define(UnitCategory.LENGTH, "Angstrom", "A", new BigNumber("0.0000000001"), BigNumbers.ZERO)),
            Map.entry(Unit.PICOMETER, define(UnitCategory.LENGTH, "Picometer", "pm", new BigNumber("0.000000000001"), BigNumbers.ZERO)),
            Map.entry(Unit.FEMTOMETER, define(UnitCategory.LENGTH, "Femtometer", "fm", new BigNumber("0.000000000000001"), BigNumbers.ZERO)),
            Map.entry(Unit.INCH, define(UnitCategory.LENGTH, "Inch", "in", new BigNumber("0.0254"), BigNumbers.ZERO)),
            Map.entry(Unit.FEET, define(UnitCategory.LENGTH, "Foot", "ft", new BigNumber("0.3048"), BigNumbers.ZERO)),
            Map.entry(Unit.YARD, define(UnitCategory.LENGTH, "Yard", "yd", new BigNumber("0.9144"), BigNumbers.ZERO)),
            Map.entry(Unit.MILE, define(UnitCategory.LENGTH, "Mile", "mi", new BigNumber("1609.344"), BigNumbers.ZERO)),
            Map.entry(Unit.NAUTICAL_MILE, define(UnitCategory.LENGTH, "Nautical Mile", "nmi", new BigNumber("1852"), BigNumbers.ZERO)),
            Map.entry(Unit.LIGHT_YEAR, define(UnitCategory.LENGTH, "Light Year", "ly", new BigNumber("9460730472580800"), BigNumbers.ZERO)),
            Map.entry(Unit.PARSEC, define(UnitCategory.LENGTH, "Parsec", "pc", new BigNumber("30856775814913673"), BigNumbers.ZERO)),
            Map.entry(Unit.PIXEL, define(UnitCategory.LENGTH, "Pixel", "px", new BigNumber("0.0002645833333333"), BigNumbers.ZERO)),
            Map.entry(Unit.POINT, define(UnitCategory.LENGTH, "Point", "pt", new BigNumber("0.0003527777777778"), BigNumbers.ZERO)),
            Map.entry(Unit.PICA, define(UnitCategory.LENGTH, "Pica", "pica", new BigNumber("0.0042333333333333"), BigNumbers.ZERO)),
            Map.entry(Unit.EM, define(UnitCategory.LENGTH, "Em", "em", new BigNumber("0.0042333333333333"), BigNumbers.ZERO))
    );

    /**
     * Immutable index bundle that contains derived lookup structures.
     *
     * <p>
     * Keeping indices grouped reduces the risk of partially initialized static state.
     * </p>
     */
    private static final Index INDEX = buildIndex();

    /**
     * Returns the definition for the given unit identifier.
     *
     * @param unit the unit identifier; must not be {@code null}
     * @return the immutable unit definition; never {@code null}
     * @throws IllegalStateException if the registry has no definition for the given unit
     */
    static UnitDefinition requireDefinition(@NonNull final Unit unit) {
        final UnitDefinition definition = DEFINITIONS.get(unit);
        if (definition == null) {
            throw new IllegalStateException("No unit definition found for unit: " + unit);
        }
        return definition;
    }

    /**
     * Attempts to find a unit identifier by its symbol.
     *
     * @param symbol unit symbol; may be {@code null}
     * @return optional unit; empty if {@code symbol} is {@code null} or unknown
     */
    static Optional<Unit> findBySymbol(final String symbol) {
        return symbol == null ? Optional.empty() : Optional.ofNullable(INDEX.bySymbol().get(symbol));
    }

    /**
     * Returns all units of a category.
     *
     * @param category category; must not be {@code null}
     * @return immutable list of units; never {@code null}
     */
    static List<Unit> byCategory(@NonNull final UnitCategory category) {
        return INDEX.byCategory().getOrDefault(category, List.of());
    }

    /**
     * Returns all units in enum declaration order.
     *
     * @return immutable list of all units; never {@code null}
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
     * Returns the immutable symbol registry.
     *
     * <p>
     * This mapping is primarily used for parsing and for building developer tooling (e.g., auto-complete).
     * The iteration order follows the {@link Unit} enum declaration order.
     * </p>
     *
     * @return immutable map of {@code symbol -> unit}; never {@code null}
     */
    static Map<String, Unit> symbolRegistry() {
        return INDEX.bySymbol();
    }

    /**
     * Creates a unit definition based on {@code scale} and {@code offset} without exposing
     * different public "formula types".
     *
     * @param category    category of the unit; must not be {@code null}
     * @param displayName human-readable name; must not be {@code null}
     * @param symbol      unit symbol; must not be {@code null}
     * @param scale       multiplicative scale into base unit; must not be {@code null} and must not be zero
     * @param offset      additive offset into base unit; must not be {@code null}
     * @return immutable unit definition; never {@code null}
     */
    private static UnitDefinition define(
            @NonNull final UnitCategory category,
            @NonNull final String displayName,
            @NonNull final String symbol,
            @NonNull final BigNumber scale,
            @NonNull final BigNumber offset
    ) {
        return new UnitDefinition(category, displayName, symbol, ScaleOffsetConversionFormula.of(scale, offset));
    }

    /**
     * Builds all derived indices from {@link #DEFINITIONS}.
     *
     * <p>
     * This method validates:
     * </p>
     * <ul>
     *   <li>Every {@link Unit} has a definition</li>
     *   <li>Every symbol is unique</li>
     *   <li>Category grouping is consistent</li>
     * </ul>
     *
     * @return an immutable index bundle; never {@code null}
     */
    private static Index buildIndex() {
        final Map<String, Unit> bySymbol = new LinkedHashMap<>();
        final Map<UnitCategory, List<Unit>> byCategoryMutable = new EnumMap<>(UnitCategory.class);

        for (final Unit unit : Unit.values()) {
            final UnitDefinition definition = requireDefinition(unit);

            final Unit previous = bySymbol.put(definition.symbol(), unit);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate unit symbol detected: '" + definition.symbol() + "' used by " + previous + " and " + unit
                );
            }

            byCategoryMutable.computeIfAbsent(definition.category(), ignored -> new ArrayList<>()).add(unit);
        }

        final Map<UnitCategory, List<Unit>> byCategoryImmutable = new EnumMap<>(UnitCategory.class);
        for (final Map.Entry<UnitCategory, List<Unit>> entry : byCategoryMutable.entrySet()) {
            byCategoryImmutable.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        return new Index(
                Collections.unmodifiableMap(bySymbol),
                Collections.unmodifiableMap(byCategoryImmutable)
        );
    }

    /**
     * Immutable container holding derived lookup indices for units.
     *
     * @param bySymbol   mapping of {@code symbol -> unit}; never {@code null}
     * @param byCategory mapping of {@code category -> units}; never {@code null}
     */
    private record Index(
            @NonNull Map<String, Unit> bySymbol,
            @NonNull Map<UnitCategory, List<Unit>> byCategory
    ) {
    }
}