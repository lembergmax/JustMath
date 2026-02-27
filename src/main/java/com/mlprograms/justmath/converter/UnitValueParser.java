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
import com.mlprograms.justmath.calculator.CalculatorEngineUtils;
import com.mlprograms.justmath.converter.exception.ConversionException;
import com.mlprograms.justmath.converter.exception.UnitConversionException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.MathContext;
import java.text.DecimalFormatSymbols;
import java.util.*;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;

/**
 * Internal parser implementation for {@link UnitValue}.
 *
 * <p>
 * This class contains the full parsing logic but is intentionally designed as an implementation detail.
 * Public parsing entry points are provided through {@link UnitValue} constructors, not through public parse methods
 * on {@code UnitValue} itself.
 * </p>
 *
 * <p>
 * The parser supports both whitespace-separated and suffix-based unit notation:
 * </p>
 * <ul>
 *   <li>{@code "12.5 km"} (whitespace form)</li>
 *   <li>{@code "12.5km"} (suffix form)</li>
 *   <li>{@code "12°C"} (suffix form, symbol may include non-letters)</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class UnitValueParser {

    /**
     * Default {@link MathContext} used for numeric parsing when callers do not provide one explicitly.
     *
     * <p>
     * The default is aligned with the library's default division precision to ensure consistent behavior across
     * the calculator/converter modules.
     * </p>
     */
    private static final MathContext DEFAULT_MATH_CONTEXT =
            CalculatorEngineUtils.getDefaultMathContext(DEFAULT_DIVISION_PRECISION);

    /**
     * Unit symbols sorted by descending length.
     *
     * <p>
     * This is required to prefer the longest possible suffix match when parsing inputs without whitespace,
     * e.g. ensuring {@code "10mm"} is interpreted as {@code "10"} + {@code "mm"} and not {@code "10m"} + {@code "m"}.
     * </p>
     */
    private static final List<String> SYMBOLS_BY_LENGTH_DESC = symbolsByLengthDesc(UnitElements.getRegistry());

    /**
     * Parses the given text and returns the parsed components (value + unit).
     *
     * <p>
     * The locale for numeric parsing is auto-detected by {@link BigNumber} based on the input string.
     * </p>
     *
     * @param text the raw input; must not be {@code null} or blank
     * @return parsed components; never {@code null}
     * @throws UnitConversionException if the unit symbol is missing/unknown or the input format is malformed
     * @throws ConversionException     if the numeric part cannot be parsed
     * @throws NullPointerException    if {@code text} is {@code null}
     */
    static ParsedComponents parseComponents(@NonNull final String text) {
        return parseComponents(text, DEFAULT_MATH_CONTEXT);
    }

    /**
     * Parses the given text using auto locale detection and a provided {@link MathContext}.
     *
     * @param text        the raw input; must not be {@code null} or blank
     * @param mathContext the math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed components; never {@code null}
     * @throws UnitConversionException if the unit symbol is missing/unknown or the input format is malformed
     * @throws ConversionException     if the numeric part cannot be parsed
     * @throws NullPointerException    if any argument is {@code null}
     */
    static ParsedComponents parseComponents(@NonNull final String text, @NonNull final MathContext mathContext) {
        final String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new UnitConversionException("Input must not be blank. Expected format: '<number> <unitSymbol>'.");
        }

        final ParsedParts parts = splitIntoNumberAndSymbol(trimmed);
        return parseComponents(parts, mathContext);
    }

    /**
     * Parses the given text using a caller-provided {@link Locale}.
     *
     * @param text   the raw input; must not be {@code null} or blank
     * @param locale the locale used for numeric parsing; must not be {@code null}
     * @return parsed components; never {@code null}
     * @throws UnitConversionException if the unit symbol is missing/unknown or the input format is malformed
     * @throws ConversionException     if the numeric part cannot be parsed
     * @throws NullPointerException    if any argument is {@code null}
     */
    static ParsedComponents parseComponents(@NonNull final String text, @NonNull final Locale locale) {
        return parseComponents(text, locale, DEFAULT_MATH_CONTEXT);
    }

    /**
     * Parses the given text using a caller-provided {@link Locale} and {@link MathContext}.
     *
     * @param text        the raw input; must not be {@code null} or blank
     * @param locale      the locale used for numeric parsing; must not be {@code null}
     * @param mathContext the math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed components; never {@code null}
     * @throws UnitConversionException if the unit symbol is missing/unknown or the input format is malformed
     * @throws ConversionException     if the numeric part cannot be parsed
     * @throws NullPointerException    if any argument is {@code null}
     */
    static ParsedComponents parseComponents(
            @NonNull final String text,
            @NonNull final Locale locale,
            @NonNull final MathContext mathContext
    ) {
        final String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new UnitConversionException("Input must not be blank. Expected format: '<number> <unitSymbol>'.");
        }

        final ParsedParts parts = splitIntoNumberAndSymbol(trimmed);
        return parseComponents(parts, locale, mathContext);
    }

    /**
     * Parses already split parts (numeric text + unit symbol) into a strongly typed result
     * using auto locale detection for the numeric part.
     */
    private static ParsedComponents parseComponents(
            @NonNull final ParsedParts parts,
            @NonNull final MathContext mathContext
    ) {
        final Unit parsedUnit = UnitElements.parseUnit(parts.symbol());

        final BigNumber parsedNumber;
        try {
            parsedNumber = new BigNumber(parts.numberText(), mathContext); // ✅ new auto-locale constructor
        } catch (final RuntimeException runtimeException) {
            throw new ConversionException(
                    "Invalid numeric value '" + parts.numberText() + "'.",
                    runtimeException
            );
        }

        return new ParsedComponents(parsedNumber, parsedUnit);
    }

    /**
     * Parses already split parts (numeric text + unit symbol) into a strongly typed result
     * using a caller-provided locale (strict normalization based on that locale).
     */
    private static ParsedComponents parseComponents(
            @NonNull final ParsedParts parts,
            @NonNull final Locale locale,
            @NonNull final MathContext mathContext
    ) {
        final Unit parsedUnit = UnitElements.parseUnit(parts.symbol());

        final BigNumber parsedNumber;
        try {
            final String normalized = normalizeNumberText(parts.numberText(), locale);
            parsedNumber = new BigNumber(normalized, locale, mathContext);
        } catch (final RuntimeException runtimeException) {
            throw new ConversionException(
                    "Invalid numeric value '" + parts.numberText() + "' for locale " + locale + ".",
                    runtimeException
            );
        }

        return new ParsedComponents(parsedNumber, parsedUnit);
    }

    private static ParsedParts splitIntoNumberAndSymbol(@NonNull final String trimmedInput) {
        final String[] tokens = trimmedInput.split("\\s+");

        if (tokens.length >= 2) {
            final String symbol = tokens[tokens.length - 1].trim();
            final String numberText = join(tokens, 0, tokens.length - 1).trim();

            if (numberText.isEmpty()) {
                throw new UnitConversionException("Missing numeric value. Expected format: '<number> <unitSymbol>'.");
            }
            if (symbol.isEmpty()) {
                throw new UnitConversionException("Missing unit symbol. Expected format: '<number> <unitSymbol>'.");
            }

            return new ParsedParts(numberText, symbol);
        }

        return splitBySuffixSymbol(tokens[0]);
    }

    private static ParsedParts splitBySuffixSymbol(@NonNull final String token) {
        for (final String symbol : SYMBOLS_BY_LENGTH_DESC) {
            if (token.endsWith(symbol)) {
                final String numberText = token.substring(0, token.length() - symbol.length()).trim();
                if (numberText.isEmpty()) {
                    break;
                }
                return new ParsedParts(numberText, symbol);
            }
        }

        throw new UnitConversionException(
                "Could not extract unit symbol from '" + token + "'. Expected format: '<number> <unitSymbol>'."
        );
    }

    private static String normalizeNumberText(@NonNull final String rawNumberText, @NonNull final Locale locale) {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
        final char decimalSeparator = symbols.getDecimalSeparator();
        final char groupingSeparator = symbols.getGroupingSeparator();

        final String withoutNbsp = rawNumberText.replace("\u00A0", "").replace("\u202F", "");

        final String withoutGrouping = (groupingSeparator == '\0')
                ? withoutNbsp
                : withoutNbsp.replace(String.valueOf(groupingSeparator), "");

        if (decimalSeparator == '.') {
            return withoutGrouping;
        }

        return withoutGrouping.replace(decimalSeparator, '.');
    }

    private static List<String> symbolsByLengthDesc(@NonNull final Map<String, Unit> registry) {
        return registry.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
    }

    private static String join(@NonNull final String[] tokens, final int start, final int end) {
        final StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (i > start) {
                builder.append(' ');
            }
            builder.append(tokens[i]);
        }
        return builder.toString();
    }

    record ParsedComponents(BigNumber value, Unit unit) {
        ParsedComponents {
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(unit, "unit must not be null");
        }
    }

    private record ParsedParts(String numberText, String symbol) {
        private ParsedParts {
            Objects.requireNonNull(numberText, "numberText must not be null");
            Objects.requireNonNull(symbol, "symbol must not be null");
        }
    }

}