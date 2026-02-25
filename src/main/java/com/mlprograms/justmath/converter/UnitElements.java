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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Public lookup and conversion facade for built-in unit definitions.
 *
 * <p>
 * This class provides a stable API for library consumers while keeping the catalog metadata in the
 * internal registry ({@link UnitRegistry}).
 * </p>
 */
@UtilityClass
public class UnitElements {

    /**
     * Finds a unit identifier by its symbol.
     *
     * @param symbol unit symbol (e.g., {@code "km"}, {@code "kg"}); may be {@code null}
     * @return optional unit identifier; empty if not found
     */
    public static Optional<Unit> findBySymbol(final String symbol) {
        return UnitRegistry.findBySymbol(symbol);
    }

    /**
     * Parses a unit symbol into a {@link Unit}.
     *
     * <p>
     * This method provides strict parse-or-fail behavior and is intended for API consumers.
     * Symbols are treated as case-sensitive.
     * </p>
     *
     * @param symbol unit symbol (e.g., {@code "km"}, {@code "kg"}); must not be {@code null} or blank
     * @return parsed unit identifier; never {@code null}
     * @throws UnitConversionException if the symbol is blank or unknown
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
     * Returns the category of a unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return category; never {@code null}
     */
    public static UnitCategory getCategory(@NonNull final Unit unit) {
        return UnitRegistry.category(unit);
    }

    /**
     * Returns the human-readable display name of a unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return display name; never {@code null}
     */
    public static String getDisplayName(@NonNull final Unit unit) {
        return UnitRegistry.displayName(unit);
    }

    /**
     * Returns the symbol of a unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return symbol; never {@code null}
     */
    public static String getSymbol(@NonNull final Unit unit) {
        return UnitRegistry.symbol(unit);
    }

    /**
     * Returns all units in a category.
     *
     * @param category category; must not be {@code null}
     * @return immutable list of unit identifiers; never {@code null}
     */
    public static List<Unit> byCategory(@NonNull final UnitCategory category) {
        return UnitRegistry.byCategory(category);
    }

    /**
     * Returns all built-in units in deterministic order.
     *
     * @return immutable list of all unit identifiers; never {@code null}
     */
    public static List<Unit> all() {
        return UnitRegistry.allUnits();
    }

    /**
     * Converts a value expressed in {@code unit} to the category base unit.
     *
     * @param unit unit identifier; must not be {@code null}
     * @param value value expressed in {@code unit}; must not be {@code null}
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return value converted to the base unit; never {@code null}
     */
    public static BigNumber toBase(
            @NonNull final Unit unit,
            @NonNull final BigNumber value,
            @NonNull final MathContext mathContext
    ) {
        return UnitRegistry.requireDefinition(unit).toBase(value, mathContext);
    }

    /**
     * Converts a value expressed in the category base unit to {@code unit}.
     *
     * @param unit target unit identifier; must not be {@code null}
     * @param value value expressed in the base unit; must not be {@code null}
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return value converted to {@code unit}; never {@code null}
     */
    public static BigNumber fromBase(
            @NonNull final Unit unit,
            @NonNull final BigNumber value,
            @NonNull final MathContext mathContext
    ) {
        return UnitRegistry.requireDefinition(unit).fromBase(value, mathContext);
    }

    /**
     * Returns an immutable symbol registry map.
     *
     * <p>
     * The returned map is deterministic: it follows the registry's unit iteration order.
     * </p>
     *
     * @return immutable mapping of {@code symbol -> unit}; never {@code null}
     */
    public static Map<String, Unit> getRegistry() {
        final Map<String, Unit> registry = new LinkedHashMap<>();
        for (final Unit unit : UnitRegistry.allUnits()) {
            registry.put(UnitRegistry.symbol(unit), unit);
        }
        return Map.copyOf(registry);
    }

}