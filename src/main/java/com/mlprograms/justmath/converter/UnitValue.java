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
import lombok.*;

import java.math.MathContext;
import java.util.Locale;
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
 * This class intentionally exposes <b>no public parse methods</b>. Instead, all parsing is performed
 * through dedicated constructors (e.g. {@link #UnitValue(String)}).
 * The actual parsing logic is delegated to {@code UnitValueParser} as an internal implementation detail.
 * </p>
 *
 * <h2>Supported formats</h2>
 * <ul>
 *   <li>Preferred: {@code "<number><whitespace><unitSymbol>"} (e.g. {@code "12.5 km"})</li>
 *   <li>Also supported: suffix without whitespace (e.g. {@code "12.5km"}, {@code "12°C"})</li>
 * </ul>
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class UnitValue {

    /**
     * The parsed numeric value.
     *
     * <p>
     * This value is immutable and always non-null.
     * Its scale/precision depends on the used {@link MathContext} during parsing (if provided).
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

}