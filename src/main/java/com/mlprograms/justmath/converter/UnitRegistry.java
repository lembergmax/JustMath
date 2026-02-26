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
     * <a href="https://www.unitconverters.net/length-converter.html">Get the scaleToBase from this website</a>
     */
    private static final List<UnitSpec> BUILT_IN = List.of(
            // =========================
            // LENGTH (base: meter)
            // =========================
            define(Unit.Length.EXAMETER, "Exameter", "Em", "1000000000000000000"),
            define(Unit.Length.PETAMETER, "Petameter", "Pm", "1000000000000000"),
            define(Unit.Length.TERAMETER, "Terameter", "Tm", "1000000000000"),
            define(Unit.Length.GIGAMETER, "Gigameter", "Gm", "1000000000"),
            define(Unit.Length.MEGAMETER, "Megameter", "Mm", "1000000"),
            define(Unit.Length.KILOMETER, "Kilometer", "km", "1000"),
            define(Unit.Length.HECTOMETER, "Hectometer", "hm", "100"),
            define(Unit.Length.DEKAMETER, "Dekameter", "dam", "10"),
            define(Unit.Length.METER, "Meter", "m"),
            define(Unit.Length.DECIMETER, "Decimeter", "dm", "0.1"),
            define(Unit.Length.CENTIMETER, "Centimeter", "cm", "0.01"),
            define(Unit.Length.MILLIMETER, "Millimeter", "mm", "0.001"),
            define(Unit.Length.MICROMETER, "Micrometer", "um", "0.000001"),
            define(Unit.Length.MICRON, "Micron", "µm", "0.000001"), // alias for micrometer
            define(Unit.Length.NANOMETER, "Nanometer", "nm", "9.999999999E-10"),
            define(Unit.Length.ANGSTROM, "Angstrom", "A", "9.999999999E-11"),
            define(Unit.Length.PICOMETER, "Picometer", "pm", "1.E-12"),
            define(Unit.Length.FEMTOMETER, "Femtometer", "fm", "9.999999999E-16"),
            define(Unit.Length.ATTOMETER, "Attometer", "am", "1.E-18"),

            define(Unit.Length.PLANCK_LENGTH, "Planck Length", "lP", "1.616049999E-35"),
            define(Unit.Length.ELECTRON_RADIUS, "Electron Radius", "re", "2.81794092E-15"),
            define(Unit.Length.BOHR_RADIUS, "Bohr Radius", "a0", "5.29177249E-11"),
            define(Unit.Length.X_UNIT, "X Unit", "xu", "1.002079999E-13"),
            define(Unit.Length.FERMI, "Fermi", "frm", "9.999999999E-16"),

            define(Unit.Length.SUN_RADIUS, "Sun Radius", "Rsun", "696000000"),
            define(Unit.Length.EARTH_EQUATORIAL_RADIUS, "Earth Equatorial Radius", "a", "6378160"),
            define(Unit.Length.EARTH_POLAR_RADIUS, "Earth Polar Radius", "b", "6356777"),
            define(Unit.Length.ASTRONOMICAL_UNIT, "Astronomical Unit", "au", "149597870691"),
            define(Unit.Length.EARTH_DISTANCE_FROM_SUN, "Earth Distance from Sun", "AU", "149600000000"),
            define(Unit.Length.KILOPARSEC, "Kiloparsec", "kpc", "30856775812799586000"),
            define(Unit.Length.MEGAPARSEC, "Megaparsec", "Mpc", "3.085677581E+22"),
            define(Unit.Length.PARSEC, "Parsec", "pc", "30856775812799588"),
            define(Unit.Length.LIGHT_YEAR, "Light Year", "ly", "9460730472580044"),

            define(Unit.Length.LEAGUE, "League", "lea", "4828.032"),
            define(Unit.Length.NAUTICAL_LEAGUE_INTERNATIONAL, "Nautical League", "NL", "55565556"),
            define(Unit.Length.NAUTICAL_LEAGUE_UK, "Nautical League (UK)", "NL (UK)", "5559.552"),
            define(Unit.Length.NAUTICAL_MILE, "Nautical Mile", "nmi", "1852"),
            define(Unit.Length.NAUTICAL_MILE_UK, "Nautical Mile (UK)", "nmi (UK)", "1853.184"),

            define(Unit.Length.MILE, "Mile", "mi", "1609.344"),
            define(Unit.Length.MILE_ROMAN, "Roman Mile", "m.p.", "1479.804"),
            define(Unit.Length.KILOYARD, "Kiloyard", "kyd", "914.4"),
            define(Unit.Length.FURLONG, "Furlong", "fur", "201.168"),
            define(Unit.Length.CHAIN, "Chain", "ch", "20.1168"),
            define(Unit.Length.ROPE, "Rope", "rope", "6.096"),
            define(Unit.Length.ROD, "Rod", "rod", "5.0292"),
            define(Unit.Length.FATHOM, "Fathom", "ftm", "1.8288"),
            define(Unit.Length.FAMN, "Famn", "famn", "1.7813333333"),
            define(Unit.Length.ELL, "Ell", "ell", "1.143"),
            define(Unit.Length.ALN, "Aln", "aln", "0.5937777778"),
            define(Unit.Length.CUBIT_UK, "Cubit (UK)", "cubit", "0.4572"),
            define(Unit.Length.SPAN_CLOTH, "Span (cloth)", "span", "0.2286"),
            define(Unit.Length.LINK, "Link", "li", "0.201168"),
            define(Unit.Length.FINGER_CLOTH, "Finger (cloth)", "finger", "0.1143"),
            define(Unit.Length.HAND, "Hand", "hand", "0.1016"),
            define(Unit.Length.HANDBREADTH, "Handbreadth", "hb", "0.0762"),
            define(Unit.Length.NAIL_COTH, "Nail (cloth)", "nail", "0.05715"),
            define(Unit.Length.FINGERBREADTH, "Fingerbreadth", "fb", "0.01905"),
            define(Unit.Length.BARLEYCORN, "Barleycorn", "barleycorn", "0.0084666667"),
            define(Unit.Length.YARD, "Yard", "yd", "0.9144"),
            define(Unit.Length.FEET, "Foot", "ft", "0.3048"),
            define(Unit.Length.INCH, "Inch", "in", "0.0254"),
            define(Unit.Length.CENTIINCH, "Centiinch", "cin", "0.000254"),
            define(Unit.Length.CALIBER, "Caliber", "cl", "0.000254"),
            define(Unit.Length.MIL, "Mil", "mil", "0.0000254"),
            define(Unit.Length.MICROINCH, "Microinch", "µin", "2.54E-8"),

            define(Unit.Length.ARPENT, "Arpent", "arp", "58.5216"),
            define(Unit.Length.KEN, "Ken", "ken", "2.11836"),

            define(Unit.Length.PIXEL, "Pixel", "px", "0.0002645833"),
            define(Unit.Length.POINT, "Point", "pt", "0.0003527778"),
            define(Unit.Length.PICA, "Pica", "pica", "0.0042333333"),
            define(Unit.Length.EM, "Em", "em", "0.0042175176"),
            define(Unit.Length.TWIP, "Twip", "twp", "0.0000176389"),

            // =========================
            // MASS (base: kilogram)
            // =========================
            define(Unit.Mass.TON, "Tonne", "t", "1000"),
            define(Unit.Mass.KILOGRAM, "Kilogram", "kg"),
            define(Unit.Mass.GRAM, "Gram", "g", "0.001"),
            define(Unit.Mass.MILLIGRAM, "Milligram", "mg", "0.000001"),

            define(Unit.Mass.LONG_TON, "Long Ton", "lt", "1016.04608"),
            define(Unit.Mass.SHORT_TON, "Short Ton", "st", "907.184"),
            define(Unit.Mass.POUND, "Pound", "lb", "0.453592"),
            define(Unit.Mass.OUNCE, "Ounce", "oz", "0.0283495"),

            define(Unit.Mass.CARRAT, "Carat", "ct", "0.0002"),
            define(Unit.Mass.ATOMIC_MASS_UNIT, "Atomic Mass Unit", "u", "1.660540199E-27"),

            // =========================
            // TEMPERATURE (base: celsius)
            // =========================
            define(Unit.Temperature.KELVIN, "Kelvin", "K", "1", "-273.15"),
            define(Unit.Temperature.CELSIUS, "Celsius", "°C"),
            define(Unit.Temperature.FAHRENHEIT, "Fahrenheit", "°F", "1", "17.777777778"),

            // =========================
            // AREA (base: square meter)
            // =========================
            define(Unit.Area.SQUARE_KILOMETER, "Square Kilometer", "km^2", "1000000"),
            define(Unit.Area.SQUARE_HECTOMETER, "Square Hectometer", "hm^2", "10000"),
            define(Unit.Area.SQUARE_DEKAMETER, "Square Dekameter", "dam^2", "100"),
            define(Unit.Area.SQUARE_METER, "Square Meter", "m^2"),
            define(Unit.Area.SQUARE_DECIMETER, "Square Decimeter", "dm^2", "0.01"),
            define(Unit.Area.SQUARE_CENTIMETER, "Square Centimeter", "cm^2", "0.0001"),
            define(Unit.Area.SQUARE_MILLIMETER, "Square Millimeter", "mm^2", "0.000001"),
            define(Unit.Area.SQUARE_MICROMETER, "Square Micrometer", "µm^2", "1.E-12"),
            define(Unit.Area.SQUARE_NANOMETER, "Square Nanometer", "nm^2", "1.E-18"),

            define(Unit.Area.HECTARE, "Hectare", "ha", "10000"),
            define(Unit.Area.ARE, "Are", "a", "100"),

            define(Unit.Area.BARN, "Barn", "b", "1.E-28"),
            define(Unit.Area.ELECTRON_CROSS_SECTION, "Thomson Cross Section", "σT", "6.652461599E-29"),

            define(Unit.Area.TOWNSHIP, "Township", "twp", "93239571.972"),
            define(Unit.Area.SECTION, "Section", "sec", "2589988.1103"),
            define(Unit.Area.HOMESTEAD, "Homestead", "hstd", "647497.02758"),

            define(Unit.Area.SQUARE_MILE, "Square Mile", "mi^2", "6.4516E-10"),
            define(Unit.Area.ACRE, "Acre", "ac", "4046.8564224"),
            define(Unit.Area.ROOD, "Rood", "rood", "1011.7141056"),

            define(Unit.Area.SQUARE_CHAIN, "Square Chain", "ch^2", "404.68564224"),
            define(Unit.Area.SQUARE_ROD, "Square Rod", "rd^2", "25.29285264"),
            define(Unit.Area.SQUARE_POLE, "Square Pole", "pole^2", "25.29285264"),
            define(Unit.Area.SQUARE_ROPE, "Square Rope", "rope^2", "37.161216"),

            define(Unit.Area.SQUARE_YARD, "Square Yard", "yd^2", "0.83612736"),
            define(Unit.Area.SQUARE_FOOT, "Square Foot", "ft^2", "0.09290304"),
            define(Unit.Area.SQUARE_INCH, "Square Inch", "in^2", "0.00064516"),

            define(Unit.Area.ARPENT, "Arpent", "arp", "3418.8929237"),
            define(Unit.Area.CUERDA, "Cuerda", "cda", "3930.395625"),
            define(Unit.Area.PLAZA, "Plaza", "plz", "6400")
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
     * @param left  the left unit; must not be {@code null}
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
     * @param unit        the unit identifier; must not be {@code null}
     * @param displayName human-readable display name; must not be {@code null}
     * @param symbol      unit symbol; must not be {@code null}
     * @return immutable unit spec entry; never {@code null}
     */
    private static UnitSpec define(
            @NonNull final Unit unit,
            @NonNull final String displayName,
            @NonNull final String symbol
    ) {
        return define(unit, displayName, symbol, "1", "0");
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
     * @param unit        the unit identifier; must not be {@code null}
     * @param displayName human-readable display name; must not be {@code null}
     * @param symbol      unit symbol; must not be {@code null}
     * @param scaleToBase multiplicative factor into base unit; must not be {@code null}
     * @return immutable unit spec entry; never {@code null}
     */
    private static UnitSpec define(
            @NonNull final Unit unit,
            @NonNull final String displayName,
            @NonNull final String symbol,
            @NonNull final String scaleToBase
    ) {
        return define(unit, displayName, symbol, scaleToBase, "0");
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
     * @param unit         the unit identifier; must not be {@code null}
     * @param displayName  human-readable display name; must not be {@code null}
     * @param symbol       unit symbol; must not be {@code null}
     * @param scaleToBase  multiplicative factor into base unit; must not be {@code null}
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
     * @param unit       the unit identifier
     * @param definition the unit definition
     */
    private record UnitSpec(Unit unit, UnitDefinition definition) {
    }

}