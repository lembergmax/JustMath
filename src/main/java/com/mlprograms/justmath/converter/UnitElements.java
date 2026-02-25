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
import com.mlprograms.justmath.converter.exception.UnitConversionException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Public lookup and conversion facade for built-in unit definitions.
 *
 * <p>
 * This class exposes a stable API for library users while the actual unit metadata
 * and validation logic live in the internal {@link UnitRegistry}.
 * </p>
 *
 * <p>
 * Design goals:
 * </p>
 * <ul>
 *   <li>Keep {@link Unit} as a pure identifier enum (no metadata)</li>
 *   <li>Offer a small, discoverable API surface for consumers</li>
 *   <li>Hide internal registry details and validation logic</li>
 * </ul>
 */
@UtilityClass
public class UnitElements {

    /**
     * Attempts to find a {@link Unit} by its symbol.
     *
     * <p>
     * This method is lenient: it does not throw if the symbol is unknown.
     * Use {@link #parseUnit(String)} for strict parsing.
     * </p>
     *
     * @param symbol the unit symbol (e.g., {@code "km"}); may be {@code null}
     * @return an {@link Optional} containing the unit if found; otherwise empty
     */
    public static Optional<Unit> findBySymbol(final String symbol) {
        return UnitRegistry.findBySymbol(symbol);
    }

    /**
     * Parses the given unit symbol into a {@link Unit}.
     *
     * <p>
     * This method is strict and will throw if the symbol is blank or unknown.
     * Parsing is case-sensitive because unit symbols may be case-sensitive.
     * </p>
     *
     * @param symbol the unit symbol (e.g., {@code "km"}); must not be {@code null} or blank
     * @return the parsed unit; never {@code null}
     * @throws UnitConversionException if the symbol is blank or not known by the registry
     */
    public static Unit parseUnit(@NonNull final String symbol) {
        final String trimmed = symbol.trim();
        if (trimmed.isEmpty()) {
            throw new UnitConversionException("Unit symbol must not be blank.");
        }
        return findBySymbol(trimmed)
                .orElseThrow(() -> new UnitConversionException("Unknown unit symbol: '" + trimmed + "'."));
    }

    /**
     * Returns the category of the given unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return category of the unit; never {@code null}
     */
    public static UnitCategory getCategory(@NonNull final Unit unit) {
        return UnitRegistry.category(unit);
    }

    /**
     * Returns the human-readable display name of the given unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return display name; never {@code null}
     */
    public static String getDisplayName(@NonNull final Unit unit) {
        return UnitRegistry.displayName(unit);
    }

    /**
     * Returns the symbol of the given unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return unit symbol; never {@code null}
     */
    public static String getSymbol(@NonNull final Unit unit) {
        return UnitRegistry.symbol(unit);
    }

    /**
     * Returns all units belonging to the specified category.
     *
     * <p>
     * The returned list is immutable.
     * </p>
     *
     * @param category the unit category; must not be {@code null}
     * @return immutable list of units in that category; never {@code null}
     */
    public static List<Unit> byCategory(@NonNull final UnitCategory category) {
        return UnitRegistry.byCategory(category);
    }

    /**
     * Returns all built-in units in enum declaration order.
     *
     * @return immutable list of all built-in units; never {@code null}
     */
    public static List<Unit> all() {
        return UnitRegistry.allUnits();
    }

    /**
     * Converts a value expressed in {@code unit} to the category base unit.
     *
     * @param unit the concrete unit identifier; must not be {@code null}
     * @param value the value expressed in {@code unit}; must not be {@code null}
     * @param mathContext math context controlling precision and rounding; must not be {@code null}
     * @return value expressed in the category base unit; never {@code null}
     */
    public static BigNumber toBase(
            @NonNull final Unit unit,
            @NonNull final BigNumber value,
            @NonNull final MathContext mathContext
    ) {
        return UnitRegistry.requireDefinition(unit).toBase(value, mathContext);
    }

    /**
     * Converts a value expressed in the category base unit to the specified concrete unit.
     *
     * @param unit the target unit identifier; must not be {@code null}
     * @param baseValue the value expressed in the category base unit; must not be {@code null}
     * @param mathContext math context controlling precision and rounding; must not be {@code null}
     * @return value expressed in {@code unit}; never {@code null}
     */
    public static BigNumber fromBase(
            @NonNull final Unit unit,
            @NonNull final BigNumber baseValue,
            @NonNull final MathContext mathContext
    ) {
        return UnitRegistry.requireDefinition(unit).fromBase(baseValue, mathContext);
    }

    /**
     * Returns the immutable symbol registry map.
     *
     * <p>
     * The map keys are unit symbols and the values are the corresponding {@link Unit} identifiers.
     * The registry order follows the enum declaration order to keep iteration deterministic.
     * </p>
     *
     * @return immutable mapping of {@code symbol -> unit}; never {@code null}
     */
    public static Map<String, Unit> getRegistry() {
        return UnitRegistry.symbolRegistry();
    }

}