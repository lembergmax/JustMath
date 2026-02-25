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
import lombok.NonNull;
import lombok.Value;

import java.math.MathContext;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;

/**
 * Immutable pair of a numeric {@link BigNumber} value and a {@link Unit} identifier.
 * <p>
 * This type provides a developer-friendly representation of inputs like {@code "12.5 km"} by parsing
 * them into a strongly typed value-object.
 * </p>
 *
 * <h2>Parsing rules</h2>
 * <ul>
 *   <li>Preferred format: {@code "<number><whitespace><unitSymbol>"} (e.g., {@code "12.5 km"})</li>
 *   <li>Also supported: suffix format without whitespace (e.g., {@code "12.5km"}, {@code "12°C"})</li>
 *   <li>Unit symbols are parsed via {@link UnitElements#parseUnit(String)}</li>
 *   <li>Numeric parsing is locale-aware via {@link BigNumber}</li>
 *   <li>Auto-locale parsing is supported via {@link #parse(String)} and uses heuristics</li>
 * </ul>
 */
@Value
public class UnitValue {

    /**
     * Default math context used by {@link #parse(String)} and {@link #parse(String, Locale)}.
     * <p>
     * This context is only used to construct the {@link BigNumber} representation.
     * Conversions should use {@link UnitConverter} with the desired precision.
     * </p>
     */
    private static final MathContext DEFAULT_PARSE_MATH_CONTEXT =
            CalculatorEngineUtils.getDefaultMathContext(DEFAULT_DIVISION_PRECISION);

    /**
     * Unit symbols sorted by descending length, used to prefer the longest suffix match.
     * <p>
     * This prevents ambiguous suffix parsing such as interpreting {@code "10mm"} as {@code "10m"} + {@code "m"}.
     * </p>
     */
    private static final List<String> SYMBOLS_BY_LENGTH_DESC = symbolsByLengthDesc(UnitElements.getRegistry());

    /**
     * The numeric value.
     */
    @NonNull
    BigNumber value;

    /**
     * The unit identifier for {@link #value}.
     */
    @NonNull
    Unit unit;

    /**
     * Parses a textual representation like {@code "12.5 km"} into a {@link UnitValue}
     * and automatically determines a suitable {@link Locale} for the numeric part.
     * <p>
     * Locale detection is heuristic:
     * </p>
     * <ul>
     *   <li>If only {@code ','} occurs as decimal separator: assumes comma-decimal (e.g., {@link Locale#GERMANY})</li>
     *   <li>If only {@code '.'} occurs as decimal separator: assumes dot-decimal (e.g., {@link Locale#US})</li>
     *   <li>If both occur: the last occurrence decides the decimal separator</li>
     *   <li>If unclear: falls back to {@link Locale#getDefault()}</li>
     * </ul>
     *
     * @param text input text (e.g., {@code "12.5 km"}); must not be {@code null} or blank
     * @return parsed {@link UnitValue}; never {@code null}
     * @throws ConversionException     if the numeric part cannot be parsed
     * @throws UnitConversionException if the unit symbol is missing/unknown or the text is malformed
     */
    public static UnitValue parse(@NonNull final String text) {
        return parse(text, DEFAULT_PARSE_MATH_CONTEXT);
    }

    /**
     * Parses a textual representation like {@code "12.5 km"} into a {@link UnitValue}
     * and automatically determines a suitable {@link Locale} for the numeric part.
     *
     * @param text        input text (e.g., {@code "12.5 km"}); must not be {@code null} or blank
     * @param mathContext math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed {@link UnitValue}; never {@code null}
     * @throws ConversionException     if the numeric part cannot be parsed
     * @throws UnitConversionException if the unit symbol is missing/unknown or the text is malformed
     */
    public static UnitValue parse(@NonNull final String text, @NonNull final MathContext mathContext) {
        final String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new UnitConversionException("Input must not be blank. Expected format: '<number> <unitSymbol>'.");
        }

        final ParsedParts parts = splitIntoNumberAndSymbol(trimmed);
        final Locale detectedLocale = detectLocaleForNumber(parts.numberText());
        return parse(parts, detectedLocale, mathContext);
    }

    /**
     * Parses a textual representation like {@code "12.5 km"} into a {@link UnitValue}.
     * <p>
     * This overload uses an internal default {@link MathContext} for parsing the numeric value.
     * </p>
     *
     * @param text   input text (e.g., {@code "12.5 km"}); must not be {@code null} or blank
     * @param locale locale used for parsing the numeric part; must not be {@code null}
     * @return parsed {@link UnitValue}; never {@code null}
     * @throws ConversionException     if the numeric part cannot be parsed
     * @throws UnitConversionException if the unit symbol is missing/unknown or the text is malformed
     */
    public static UnitValue parse(@NonNull final String text, @NonNull final Locale locale) {
        return parse(text, locale, DEFAULT_PARSE_MATH_CONTEXT);
    }

    /**
     * Parses a textual representation like {@code "12.5 km"} into a {@link UnitValue}.
     * <p>
     * Use this overload if you want explicit control over the {@link MathContext} that is used while parsing
     * the numeric part into a {@link BigNumber}.
     * </p>
     *
     * @param text        input text (e.g., {@code "12.5 km"}); must not be {@code null} or blank
     * @param locale      locale used for parsing the numeric part; must not be {@code null}
     * @param mathContext math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed {@link UnitValue}; never {@code null}
     * @throws ConversionException     if the numeric part cannot be parsed
     * @throws UnitConversionException if the unit symbol is missing/unknown or the text is malformed
     */
    public static UnitValue parse(
            @NonNull final String text,
            @NonNull final Locale locale,
            @NonNull final MathContext mathContext
    ) {
        final String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new UnitConversionException("Input must not be blank. Expected format: '<number> <unitSymbol>'.");
        }

        final ParsedParts parts = splitIntoNumberAndSymbol(trimmed);
        return parse(parts, locale, mathContext);
    }

    /**
     * Parses already split parts into a {@link UnitValue}.
     *
     * @param parts       split number/symbol parts; must not be {@code null}
     * @param locale      locale used for numeric parsing; must not be {@code null}
     * @param mathContext math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed {@link UnitValue}; never {@code null}
     */
    private static UnitValue parse(
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

        return new UnitValue(parsedNumber, parsedUnit);
    }

    /**
     * Attempts to detect a suitable {@link Locale} for parsing a numeric string.
     * <p>
     * This method only uses lightweight heuristics based on separators present in the text.
     * If no reliable decision can be made, it falls back to {@link Locale#getDefault()}.
     * </p>
     *
     * @param numberText numeric part as text; must not be {@code null}
     * @return detected locale; never {@code null}
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
     * <p>
     * First tries the whitespace form ({@code "<number> <symbol>"}). If no whitespace is present,
     * it tries a suffix match against the known unit symbols and prefers the longest match.
     * </p>
     *
     * @param trimmedInput input string, already {@link String#trim()}ed; must not be {@code null}
     * @return parsed parts; never {@code null}
     * @throws UnitConversionException if no unit symbol can be extracted
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
     * Attempts to parse an input without whitespace by matching a known unit symbol as suffix.
     *
     * @param token single token input (e.g., {@code "12.5km"}); must not be {@code null}
     * @return parsed parts; never {@code null}
     * @throws UnitConversionException if no known symbol is a suffix of the token
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
     * Normalizes a locale-specific numeric text into a canonical representation that is safe to parse.
     *
     * <p>
     * The normalization performs two steps:
     * </p>
     * <ul>
     *   <li>Removes grouping separators (e.g., '.' in Germany, ',' in US).</li>
     *   <li>Converts the locale decimal separator into '.' (dot) for canonical decimal representation.</li>
     * </ul>
     *
     * <p>
     * This makes parsing deterministic even if the underlying {@link com.mlprograms.justmath.bignumber.BigNumber}
     * implementation does not fully honor locale-specific decimal separators.
     * </p>
     *
     * @param rawNumberText the raw numeric text extracted from the input (e.g., {@code "1.234,56"}); must not be {@code null}
     * @param locale the locale that defines decimal and grouping separators; must not be {@code null}
     * @return canonical numeric text (e.g., {@code "1234.56"}); never {@code null}
     */
    private static String normalizeNumberText(@NonNull final String rawNumberText, @NonNull final Locale locale) {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
        final char decimalSeparator = symbols.getDecimalSeparator();
        final char groupingSeparator = symbols.getGroupingSeparator();

        // Remove common non-breaking space group separators too (some locales use NBSP).
        final String withoutNbsp = rawNumberText.replace("\u00A0", "").replace("\u202F", "");

        // 1) remove grouping separator
        final String withoutGrouping = (groupingSeparator == '\0')
                ? withoutNbsp
                : withoutNbsp.replace(String.valueOf(groupingSeparator), "");

        // 2) replace decimal separator with '.'
        if (decimalSeparator == '.') {
            return withoutGrouping;
        }
        return withoutGrouping.replace(decimalSeparator, '.');
    }

    /**
     * Creates a list of symbols sorted by descending length.
     *
     * @param registry symbol registry (symbol -> unit); must not be {@code null}
     * @return immutable list of symbols sorted by descending length
     */
    private static List<String> symbolsByLengthDesc(@NonNull final Map<String, Unit> registry) {
        return registry.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
    }

    /**
     * Joins a subrange of a string array with a single space character.
     *
     * @param tokens array of tokens; must not be {@code null}
     * @param start  start index (inclusive)
     * @param end    end index (exclusive)
     * @return joined string; never {@code null}
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
     * Internal parsing result containing the numeric part and the unit symbol.
     *
     * @param numberText numeric part as text (locale-dependent)
     * @param symbol     unit symbol
     */
    private record ParsedParts(String numberText, String symbol) { }

}