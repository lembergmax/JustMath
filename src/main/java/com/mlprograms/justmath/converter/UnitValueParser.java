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
     * The locale for numeric parsing is detected using simple heuristics based on the presence and position of
     * {@code '.'} and {@code ','}. If detection is unclear, {@link Locale#getDefault()} is used.
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
     * Parses the given text using heuristic locale detection and a provided {@link MathContext}.
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
        final Locale detectedLocale = detectLocaleForNumber(parts.numberText());
        return parseComponents(parts, detectedLocale, mathContext);
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
     * Parses already split parts (numeric text + unit symbol) into a strongly typed result.
     *
     * <p>
     * This method is responsible for:
     * </p>
     * <ul>
     *   <li>resolving the unit symbol via {@link UnitElements#parseUnit(String)}</li>
     *   <li>normalizing the number text based on the provided locale</li>
     *   <li>creating the {@link BigNumber} instance</li>
     * </ul>
     *
     * @param parts       numeric and unit symbol parts; must not be {@code null}
     * @param locale      locale used for numeric normalization; must not be {@code null}
     * @param mathContext math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed components; never {@code null}
     * @throws UnitConversionException if the unit symbol is unknown
     * @throws ConversionException     if numeric parsing fails
     * @throws NullPointerException    if any argument is {@code null}
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

    /**
     * Attempts to detect a suitable {@link Locale} for parsing the numeric part.
     *
     * <p>
     * Heuristic rules:
     * </p>
     * <ul>
     *   <li>If only {@code ','} occurs: assume comma-decimal (e.g. {@link Locale#GERMANY})</li>
     *   <li>If only {@code '.'} occurs: assume dot-decimal (e.g. {@link Locale#US})</li>
     *   <li>If both occur: whichever appears last is assumed to be the decimal separator</li>
     *   <li>If none occur: fall back to {@link Locale#getDefault()}</li>
     * </ul>
     *
     * @param numberText numeric part as text; must not be {@code null}
     * @return detected locale; never {@code null}
     * @throws NullPointerException if {@code numberText} is {@code null}
     */
    private static Locale detectLocaleForNumber(@NonNull final String numberText) {
        final int lastComma = numberText.lastIndexOf(',');
        final int lastDot = numberText.lastIndexOf('.');

        final boolean hasComma = lastComma >= 0;
        final boolean hasDot = lastDot >= 0;

        if (hasComma && !hasDot) {
            return Locale.GERMANY;
        }
        if (hasDot && !hasComma) {
            return Locale.US;
        }
        if (hasComma) {
            return (lastComma > lastDot) ? Locale.GERMANY : Locale.US;
        }

        return Locale.getDefault();
    }

    /**
     * Splits the input into numeric text and a unit symbol.
     *
     * <p>
     * This method prefers the whitespace-separated format. If whitespace does not exist,
     * it falls back to suffix matching against the known unit symbols.
     * </p>
     *
     * @param trimmedInput input string that has already been {@link String#trim() trimmed}; must not be {@code null}
     * @return parsed parts; never {@code null}
     * @throws UnitConversionException if no unit symbol can be extracted
     * @throws NullPointerException    if {@code trimmedInput} is {@code null}
     */
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

    /**
     * Splits a single-token input by locating a known unit symbol suffix.
     *
     * <p>
     * The lookup iterates over {@link #SYMBOLS_BY_LENGTH_DESC} and thus prefers the longest suffix match.
     * </p>
     *
     * @param token single token input, e.g. {@code "12.5km"}; must not be {@code null}
     * @return parsed parts; never {@code null}
     * @throws UnitConversionException if no known symbol is a suffix of the token
     * @throws NullPointerException    if {@code token} is {@code null}
     */
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

    /**
     * Normalizes a locale-specific numeric string into a canonical representation.
     *
     * <p>
     * The normalization performs two operations:
     * </p>
     * <ol>
     *   <li>Remove grouping separators (e.g. '.' in Germany, ',' in US).</li>
     *   <li>Replace the locale decimal separator with '.' (dot).</li>
     * </ol>
     *
     * <p>
     * It also removes common non-breaking space characters used as grouping separators in some locales.
     * </p>
     *
     * @param rawNumberText the raw numeric text (e.g. {@code "1.234,56"}); must not be {@code null}
     * @param locale        the locale defining decimal and grouping separators; must not be {@code null}
     * @return canonical numeric text (e.g. {@code "1234.56"}); never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
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

    /**
     * Builds an immutable list of known unit symbols sorted by descending length.
     *
     * <p>
     * This ordering is required for correct suffix parsing with ambiguous symbols.
     * </p>
     *
     * @param registry the symbol registry (symbol -&gt; unit); must not be {@code null}
     * @return symbols sorted by descending length; never {@code null}
     * @throws NullPointerException if {@code registry} is {@code null}
     */
    private static List<String> symbolsByLengthDesc(@NonNull final Map<String, Unit> registry) {
        return registry.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
    }

    /**
     * Joins a sub-range of a token array using a single space character.
     *
     * <p>
     * This is used to reconstruct numeric text that may itself contain spaces (e.g. if inputs were
     * already tokenized by whitespace and the unit is the last token).
     * </p>
     *
     * @param tokens array of tokens; must not be {@code null}
     * @param start  start index (inclusive)
     * @param end    end index (exclusive)
     * @return joined string; never {@code null}
     * @throws NullPointerException if {@code tokens} is {@code null}
     */
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

    /**
     * Lightweight internal carrier for parsed components (value + unit).
     *
     * <p>
     * This type is deliberately small and immutable. It exists to keep {@link UnitValue} constructors simple
     * and to avoid exposing parsing APIs on {@link UnitValue} directly.
     * </p>
     *
     * @param value the parsed numeric value; never {@code null}
     * @param unit  the parsed unit; never {@code null}
     */
    record ParsedComponents(BigNumber value, Unit unit) {

        /**
         * Creates a new parsed component carrier and validates its invariants.
         *
         * @param value the parsed numeric value; must not be {@code null}
         * @param unit  the parsed unit; must not be {@code null}
         * @throws NullPointerException if any argument is {@code null}
         */
        ParsedComponents {
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(unit, "unit must not be null");
        }

    }

    /**
     * Internal parsing result holding the textual numeric part and the textual unit symbol.
     *
     * <p>
     * This type represents an intermediate state, before:
     * </p>
     * <ul>
     *   <li>the unit symbol is resolved to a {@link Unit}</li>
     *   <li>the number text is normalized and converted to {@link BigNumber}</li>
     * </ul>
     *
     * @param numberText numeric part as extracted from input; never {@code null}
     * @param symbol     unit symbol as extracted from input; never {@code null}
     */
    private record ParsedParts(String numberText, String symbol) {

        /**
         * Creates a new textual parsing result.
         *
         * @param numberText numeric part; must not be {@code null}
         * @param symbol     unit symbol; must not be {@code null}
         * @throws NullPointerException if any argument is {@code null}
         */
        private ParsedParts {
            Objects.requireNonNull(numberText, "numberText must not be null");
            Objects.requireNonNull(symbol, "symbol must not be null");
        }

    }

}