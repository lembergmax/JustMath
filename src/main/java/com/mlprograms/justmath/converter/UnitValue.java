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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.math.MathContext;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable value object that couples a numeric {@link BigNumber} with a {@link Unit}.
 *
 * <p>
 * The primary purpose of this type is to represent user-facing inputs like {@code "12.5 km"} in a
 * strongly typed form that is safe and convenient for further processing (e.g. conversion).
 * </p>
 *
 * <h2>Important design note</h2>
 * <p>
 * This class intentionally exposes <b>no public parse methods</b>. Instead, parsing is performed
 * through dedicated constructors (e.g. {@link #UnitValue(String)}).
 * The actual parsing logic is delegated to {@code UnitValueParser} as an internal implementation detail.
 * </p>
 *
 * <h2>Comparable contract</h2>
 * <p>
 * {@link #compareTo(UnitValue)} compares <b>only values with the same {@link Unit}</b>.
 * Comparing different units without conversion is ambiguous and therefore rejected with an exception.
 * If you need cross-unit comparisons, convert first using {@link UnitConverter}.
 * </p>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class UnitValue implements Comparable<UnitValue> {

    /**
     * The parsed numeric value.
     *
     * <p>
     * This value is immutable and always non-null.
     * Its scale/precision depends on the {@link MathContext} used during parsing (if provided).
     * </p>
     */
    @NonNull
    private final BigNumber value;

    /**
     * The parsed unit that semantically belongs to {@link #value}.
     *
     * <p>
     * This unit is immutable and always non-null.
     * Unit symbols are resolved via the unit registry (see {@link UnitElements}).
     * </p>
     */
    @NonNull
    private final Unit unit;

    /**
     * Creates a new {@link UnitValue} from already validated and parsed components.
     *
     * <p>
     * Use this constructor if you already have a numeric {@link BigNumber} and a corresponding {@link Unit},
     * and you want to wrap them into a single immutable value object without any additional parsing.
     * </p>
     *
     * @param value the numeric value; must not be {@code null}
     * @param unit  the unit of the numeric value; must not be {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public UnitValue(@NonNull final BigNumber value, @NonNull final Unit unit) {
        this.value = Objects.requireNonNull(value, "value must not be null");
        this.unit = Objects.requireNonNull(unit, "unit must not be null");
    }

    /**
     * Parses the given text input using heuristic locale detection and creates a new {@link UnitValue}.
     *
     * <p>
     * Locale detection is intentionally lightweight and based on the numeric separators found in the input.
     * If no reliable decision can be made, {@link Locale#getDefault()} is used.
     * </p>
     *
     * <p>
     * Examples:
     * </p>
     * <ul>
     *   <li>{@code new UnitValue("12.5 km")}</li>
     *   <li>{@code new UnitValue("1.234,56 m")}</li>
     *   <li>{@code new UnitValue("12°C")}</li>
     * </ul>
     *
     * @param text the raw user input; must not be {@code null} or blank
     * @throws com.mlprograms.justmath.converter.exception.UnitConversionException
     *         if the unit symbol is missing/unknown or the input format is malformed
     * @throws com.mlprograms.justmath.converter.exception.ConversionException
     *         if the numeric part cannot be parsed
     * @throws NullPointerException if {@code text} is {@code null}
     */
    public UnitValue(@NonNull final String text) {
        this(UnitValueParser.parseComponents(text));
    }

    /**
     * Parses the given text input using heuristic locale detection and a caller-provided {@link MathContext}.
     *
     * <p>
     * Use this constructor if you want explicit control over the precision used when constructing the
     * {@link BigNumber} instance from the numeric text.
     * </p>
     *
     * @param text        the raw user input; must not be {@code null} or blank
     * @param mathContext the math context used to create the {@link BigNumber}; must not be {@code null}
     * @throws com.mlprograms.justmath.converter.exception.UnitConversionException
     *         if the unit symbol is missing/unknown or the input format is malformed
     * @throws com.mlprograms.justmath.converter.exception.ConversionException
     *         if the numeric part cannot be parsed
     * @throws NullPointerException if any argument is {@code null}
     */
    public UnitValue(@NonNull final String text, @NonNull final MathContext mathContext) {
        this(UnitValueParser.parseComponents(text, mathContext));
    }

    /**
     * Parses the given text input using an explicitly provided {@link Locale}.
     *
     * <p>
     * This is useful if auto-locale detection would be ambiguous or undesired.
     * The locale affects decimal and grouping separators during numeric normalization.
     * </p>
     *
     * @param text   the raw user input; must not be {@code null} or blank
     * @param locale the locale to use for numeric parsing; must not be {@code null}
     * @throws com.mlprograms.justmath.converter.exception.UnitConversionException
     *         if the unit symbol is missing/unknown or the input format is malformed
     * @throws com.mlprograms.justmath.converter.exception.ConversionException
     *         if the numeric part cannot be parsed
     * @throws NullPointerException if any argument is {@code null}
     */
    public UnitValue(@NonNull final String text, @NonNull final Locale locale) {
        this(UnitValueParser.parseComponents(text, locale));
    }

    /**
     * Parses the given text input using an explicitly provided {@link Locale} and {@link MathContext}.
     *
     * <p>
     * This constructor provides maximum control over parsing behavior:
     * </p>
     * <ul>
     *   <li>{@link Locale} controls numeric separators (decimal/grouping) during normalization</li>
     *   <li>{@link MathContext} controls precision/rounding used by {@link BigNumber} construction</li>
     * </ul>
     *
     * @param text        the raw user input; must not be {@code null} or blank
     * @param locale      the locale to use for numeric parsing; must not be {@code null}
     * @param mathContext the math context used to create the {@link BigNumber}; must not be {@code null}
     * @throws com.mlprograms.justmath.converter.exception.UnitConversionException
     *         if the unit symbol is missing/unknown or the input format is malformed
     * @throws com.mlprograms.justmath.converter.exception.ConversionException
     *         if the numeric part cannot be parsed
     * @throws NullPointerException if any argument is {@code null}
     */
    public UnitValue(
            @NonNull final String text,
            @NonNull final Locale locale,
            @NonNull final MathContext mathContext
    ) {
        this(UnitValueParser.parseComponents(text, locale, mathContext));
    }

    /**
     * Internal constructor that consumes a parsed result produced by {@code UnitValueParser}.
     *
     * <p>
     * This constructor exists to keep the public API clean while still allowing the parser to return
     * a compact carrier object.
     * </p>
     *
     * @param parsed the parsed value/unit pair; must not be {@code null}
     * @throws NullPointerException if {@code parsed} is {@code null}
     */
    private UnitValue(@NonNull final UnitValueParser.ParsedComponents parsed) {
        this(
                Objects.requireNonNull(parsed, "parsed must not be null").value(),
                parsed.unit()
        );
    }

    /**
     * Resolves the preferred display symbol for the current {@link #unit}.
     *
     * <p>
     * Because {@link Unit} does not necessarily expose a {@code getSymbol()} method, this implementation
     * resolves the symbol by scanning the unit registry returned by {@link UnitElements#getRegistry()}.
     * </p>
     *
     * <p>
     * If multiple symbols map to the same {@link Unit} (aliases), the preferred symbol is chosen deterministically:
     * </p>
     * <ul>
     *   <li>Prefer the shortest symbol (more compact display).</li>
     *   <li>If equal length, choose lexicographically smallest (stable ordering).</li>
     * </ul>
     *
     * <p>
     * If the unit cannot be found in the registry (should not happen for valid units), this method falls back
     * to {@link Object#toString()} of the unit.
     * </p>
     *
     * @return preferred unit symbol for display; never {@code null}
     */
    public String toUnitSymbol() {
        return resolvePreferredSymbol(unit);
    }

    /**
     * Checks whether this {@link UnitValue} uses the given unit.
     *
     * @param expectedUnit the unit to check; must not be {@code null}
     * @return {@code true} if the unit matches; otherwise {@code false}
     * @throws NullPointerException if {@code expectedUnit} is {@code null}
     */
    public boolean isUnit(@NonNull final Unit expectedUnit) {
        return this.unit.equals(expectedUnit);
    }

    /**
     * Checks whether this {@link UnitValue} uses the given unit symbol.
     *
     * <p>
     * The symbol is resolved via {@link UnitElements#parseUnit(String)} which ensures consistent mapping
     * with the converter's registry.
     * </p>
     *
     * @param unitSymbol the unit symbol to resolve and compare; must not be {@code null} or blank
     * @return {@code true} if the resolved unit matches; otherwise {@code false}
     * @throws UnitConversionException if the symbol is unknown or blank
     * @throws NullPointerException    if {@code unitSymbol} is {@code null}
     */
    public boolean isUnitSymbol(@NonNull final String unitSymbol) {
        return isUnit(UnitElements.parseUnit(unitSymbol));
    }

    /**
     * Creates a human-readable representation using a resolved unit symbol.
     *
     * <p>
     * The numeric format depends on {@link BigNumber#toString()}.
     * If you require localized formatting, apply it to {@link #value} externally.
     * </p>
     *
     * @return formatted string such as {@code "12.5 km"}; never {@code null}
     */
    public String toDisplayString() {
        return value + " " + toUnitSymbol();
    }

    /**
     * Creates a compact representation without whitespace between number and unit symbol.
     *
     * <p>
     * Example: {@code "12.5km"}.
     * </p>
     *
     * @return compact string representation; never {@code null}
     */
    public String toCompactString() {
        return value + toUnitSymbol();
    }

    /**
     * Compares this instance to another {@link UnitValue}.
     *
     * <p>
     * The comparison is only defined for values with the same {@link Unit}. If units differ,
     * an {@link IllegalArgumentException} is thrown to prevent incorrect assumptions.
     * </p>
     *
     * @param other the other unit value; must not be {@code null}
     * @return comparison result compatible with {@link Comparable}
     * @throws IllegalArgumentException if units differ
     * @throws NullPointerException     if {@code other} is {@code null}
     */
    @Override
    public int compareTo(@NonNull final UnitValue other) {
        requireSameUnit(other);
        return value.compareTo(other.value);
    }

    /**
     * Validates that the given value uses the same unit as this instance.
     *
     * @param other the other unit value; must not be {@code null}
     * @throws IllegalArgumentException if units differ
     * @throws NullPointerException     if {@code other} is {@code null}
     */
    private void requireSameUnit(@NonNull final UnitValue other) {
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException(
                    "Unit mismatch. Expected '" + this.unit + "' but was '" + other.unit + "'. Convert first using UnitConverter."
            );
        }
    }

    /**
     * Resolves a stable "preferred" symbol for the given unit by scanning the registry.
     *
     * <p>
     * The registry returned by {@link UnitElements#getRegistry()} maps symbols to units. Because the reverse mapping
     * (unit to symbol) is not guaranteed to exist on the {@link Unit} type, this method computes it.
     * </p>
     *
     * <p>
     * Selection strategy is deterministic:
     * </p>
     * <ul>
     *   <li>Prefer the shortest matching symbol.</li>
     *   <li>Then lexicographically smallest symbol.</li>
     * </ul>
     *
     * @param unit the unit to resolve; must not be {@code null}
     * @return preferred symbol or {@code unit.toString()} as fallback; never {@code null}
     * @throws NullPointerException if {@code unit} is {@code null}
     */
    private static String resolvePreferredSymbol(@NonNull final Unit unit) {
        final Map<String, Unit> registry = UnitElements.getRegistry();

        return registry.entrySet().stream()
                .filter(entry -> unit.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .min(Comparator.comparingInt(String::length).thenComparing(String::compareTo))
                .orElseGet(unit::toString);
    }

}