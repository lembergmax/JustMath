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
 * Internal parsing utility for {@link UnitValue} inputs.
 *
 * <p>
 * This class is intentionally package-private and serves as the implementation detail behind the public
 * {@link UnitValue} constructors. The design goal is to keep {@code UnitValue} small and focused, while
 * encapsulating all parsing rules (number parsing + unit extraction) in this dedicated helper.
 * </p>
 *
 * <h2>Supported input notations</h2>
 * <p>
 * The parser accepts both of the following notations:
 * </p>
 * <ul>
 *   <li><b>Whitespace-separated</b>: {@code "12.5 km"} (value and unit separated by whitespace)</li>
 *   <li><b>Suffix-based</b>: {@code "12.5km"} (unit is a suffix immediately following the number)</li>
 *   <li><b>Non-letter suffixes</b>: {@code "12°C"} (unit symbols may include non-letter characters)</li>
 * </ul>
 *
 * <h2>Locale handling</h2>
 * <p>
 * In the auto-locale parsing path, the numeric {@link BigNumber} is constructed via the auto-locale
 * {@link BigNumber} constructor, which resolves grouping and decimal separators automatically based on the
 * input string.
 * </p>
 *
 * <p>
 * If the caller explicitly provides a {@link Locale}, the numeric string is normalized according to that locale
 * (grouping removed and decimal separator converted to {@code '.'}) and then parsed.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class UnitValueParser {

    /**
     * Default {@link MathContext} used for numeric parsing when callers do not provide one explicitly.
     *
     * <p>
     * The value is derived from the calculator module's default math context creation, using
     * {@link com.mlprograms.justmath.bignumber.BigNumbers#DEFAULT_DIVISION_PRECISION} so that calculator and converter behavior remains consistent.
     * </p>
     */
    private static final MathContext DEFAULT_MATH_CONTEXT =
            CalculatorEngineUtils.getDefaultMathContext(DEFAULT_DIVISION_PRECISION);

    /**
     * A list of all known unit symbols sorted by descending length.
     *
     * <p>
     * This ordering is critical for correct suffix parsing when unit symbols overlap.
     * Example: If both {@code "m"} and {@code "mm"} exist, the input {@code "10mm"} must be split as
     * {@code "10"} + {@code "mm"} and not incorrectly as {@code "10m"} + {@code "m"}.
     * </p>
     *
     * <p>
     * The list is created from {@link UnitElements#getRegistry()} once at class initialization time.
     * </p>
     */
    private static final List<String> UNIT_SYMBOLS_SORTED_BY_LENGTH_DESCENDING =
            createUnitSymbolsSortedByLengthDescending(UnitElements.getRegistry());

    /**
     * Parses a raw {@link UnitValue} input string using auto locale detection for the numeric part.
     *
     * <p>
     * This method delegates to {@link #parseComponents(String, MathContext)} using
     * {@link #DEFAULT_MATH_CONTEXT}.
     * </p>
     *
     * @param rawInputText the raw input string; must not be {@code null} and must not be blank
     * @return parsed components consisting of a {@link BigNumber} value and a {@link Unit}; never {@code null}
     * @throws UnitConversionException if the input is blank, the unit symbol is missing/unknown, or the structure is malformed
     * @throws ConversionException     if the numeric part cannot be parsed into a {@link BigNumber}
     * @throws NullPointerException    if {@code rawInputText} is {@code null}
     */
    static ParsedComponents parseComponents(@NonNull final String rawInputText) {
        return parseComponents(rawInputText, DEFAULT_MATH_CONTEXT);
    }

    /**
     * Parses a raw {@link UnitValue} input string using auto locale detection for the numeric part
     * and a caller-provided {@link MathContext}.
     *
     * <p>
     * The parsing steps are:
     * </p>
     * <ol>
     *   <li>Trim and validate the input (must not be blank).</li>
     *   <li>Split the input into numeric text and unit symbol using {@link #splitIntoNumberTextAndUnitSymbol(String)}.</li>
     *   <li>Parse the unit symbol via {@link UnitElements#parseUnit(String)}.</li>
     *   <li>Create the {@link BigNumber} using an auto-locale constructor.</li>
     * </ol>
     *
     * @param rawInputText                 the raw input string; must not be {@code null} and must not be blank
     * @param mathContextForNumericParsing the math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed components consisting of a {@link BigNumber} value and a {@link Unit}; never {@code null}
     * @throws UnitConversionException if the input is blank, the unit symbol is missing/unknown, or the structure is malformed
     * @throws ConversionException     if the numeric part cannot be parsed into a {@link BigNumber}
     * @throws NullPointerException    if any argument is {@code null}
     */
    static ParsedComponents parseComponents(
            @NonNull final String rawInputText,
            @NonNull final MathContext mathContextForNumericParsing
    ) {
        final String trimmedInputText = rawInputText.trim();
        if (trimmedInputText.isEmpty()) {
            throw new UnitConversionException("Input must not be blank. Expected format: '<number> <unitSymbol>'.");
        }

        final ParsedParts parsedParts = splitIntoNumberTextAndUnitSymbol(trimmedInputText);
        return parseComponents(parsedParts, mathContextForNumericParsing);
    }

    /**
     * Parses a raw {@link UnitValue} input string using a caller-provided {@link Locale} for the numeric part.
     *
     * <p>
     * This method delegates to {@link #parseComponents(String, Locale, MathContext)} using
     * {@link #DEFAULT_MATH_CONTEXT}.
     * </p>
     *
     * @param rawInputText            the raw input string; must not be {@code null} and must not be blank
     * @param localeForNumericParsing the locale used for numeric parsing; must not be {@code null}
     * @return parsed components consisting of a {@link BigNumber} value and a {@link Unit}; never {@code null}
     * @throws UnitConversionException if the input is blank, the unit symbol is missing/unknown, or the structure is malformed
     * @throws ConversionException     if the numeric part cannot be parsed into a {@link BigNumber}
     * @throws NullPointerException    if any argument is {@code null}
     */
    static ParsedComponents parseComponents(
            @NonNull final String rawInputText,
            @NonNull final Locale localeForNumericParsing
    ) {
        return parseComponents(rawInputText, localeForNumericParsing, DEFAULT_MATH_CONTEXT);
    }

    /**
     * Parses a raw {@link UnitValue} input string using a caller-provided {@link Locale} and {@link MathContext}.
     *
     * <p>
     * This parsing mode is strict: it assumes the numeric part is formatted according to the provided locale,
     * normalizes it into a canonical representation, and then parses it.
     * </p>
     *
     * @param rawInputText                 the raw input string; must not be {@code null} and must not be blank
     * @param localeForNumericParsing      the locale used for numeric parsing; must not be {@code null}
     * @param mathContextForNumericParsing the math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed components consisting of a {@link BigNumber} value and a {@link Unit}; never {@code null}
     * @throws UnitConversionException if the input is blank, the unit symbol is missing/unknown, or the structure is malformed
     * @throws ConversionException     if the numeric part cannot be parsed into a {@link BigNumber}
     * @throws NullPointerException    if any argument is {@code null}
     */
    static ParsedComponents parseComponents(
            @NonNull final String rawInputText,
            @NonNull final Locale localeForNumericParsing,
            @NonNull final MathContext mathContextForNumericParsing
    ) {
        final String trimmedInputText = rawInputText.trim();
        if (trimmedInputText.isEmpty()) {
            throw new UnitConversionException("Input must not be blank. Expected format: '<number> <unitSymbol>'.");
        }

        final ParsedParts parsedParts = splitIntoNumberTextAndUnitSymbol(trimmedInputText);
        return parseComponents(parsedParts, localeForNumericParsing, mathContextForNumericParsing);
    }

    /**
     * Parses already split parts (numeric text + unit symbol) into a strongly typed result using auto locale detection.
     *
     * <p>
     * The numeric part is parsed via {@code new BigNumber(numberText, mathContext)} which resolves the locale
     * based on the number text itself.
     * </p>
     *
     * @param parsedParts                 the already split parts; must not be {@code null}
     * @param mathContextForNumericParsing the math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed components; never {@code null}
     * @throws UnitConversionException if the unit symbol is unknown
     * @throws ConversionException     if numeric parsing fails
     * @throws NullPointerException    if any argument is {@code null}
     */
    private static ParsedComponents parseComponents(
            @NonNull final ParsedParts parsedParts,
            @NonNull final MathContext mathContextForNumericParsing
    ) {
        final Unit parsedUnit = UnitElements.parseUnit(parsedParts.symbol());

        final BigNumber parsedBigNumberValue;
        try {
            parsedBigNumberValue = new BigNumber(parsedParts.numberText(), mathContextForNumericParsing);
        } catch (final RuntimeException runtimeException) {
            throw new ConversionException(
                    "Invalid numeric value '" + parsedParts.numberText() + "'.",
                    runtimeException
            );
        }

        return new ParsedComponents(parsedBigNumberValue, parsedUnit);
    }

    /**
     * Parses already split parts (numeric text + unit symbol) into a strongly typed result using a caller-provided locale.
     *
     * <p>
     * The numeric text is normalized according to the provided locale by:
     * </p>
     * <ol>
     *   <li>Removing grouping separators (e.g. {@code '.'} in {@code de_DE}, {@code ','} in {@code en_US}).</li>
     *   <li>Replacing the locale-specific decimal separator with {@code '.'}.</li>
     *   <li>Removing common non-breaking space characters used as grouping separators in some locales.</li>
     * </ol>
     *
     * <p>
     * After normalization, the resulting canonical string is passed into the locale-aware
     * {@link BigNumber} constructor.
     * </p>
     *
     * @param parsedParts                 the already split parts; must not be {@code null}
     * @param localeForNumericParsing     the locale used for numeric normalization and parsing; must not be {@code null}
     * @param mathContextForNumericParsing the math context used to create the {@link BigNumber}; must not be {@code null}
     * @return parsed components; never {@code null}
     * @throws UnitConversionException if the unit symbol is unknown
     * @throws ConversionException     if numeric parsing fails
     * @throws NullPointerException    if any argument is {@code null}
     */
    private static ParsedComponents parseComponents(
            @NonNull final ParsedParts parsedParts,
            @NonNull final Locale localeForNumericParsing,
            @NonNull final MathContext mathContextForNumericParsing
    ) {
        final Unit parsedUnit = UnitElements.parseUnit(parsedParts.symbol());

        final BigNumber parsedBigNumberValue;
        try {
            final String normalizedNumberText = normalizeNumberText(parsedParts.numberText(), localeForNumericParsing);
            parsedBigNumberValue = new BigNumber(normalizedNumberText, localeForNumericParsing, mathContextForNumericParsing);
        } catch (final RuntimeException runtimeException) {
            throw new ConversionException(
                    "Invalid numeric value '" + parsedParts.numberText() + "' for locale " + localeForNumericParsing + ".",
                    runtimeException
            );
        }

        return new ParsedComponents(parsedBigNumberValue, parsedUnit);
    }

    /**
     * Splits an already-trimmed input string into numeric text and unit symbol.
     *
     * <p>
     * The method prefers the whitespace-separated format. If the input contains whitespace and at least two tokens,
     * the last token is treated as the unit symbol and the preceding tokens are re-joined as the numeric part.
     * </p>
     *
     * <p>
     * If the input is a single token (no whitespace), the method falls back to suffix matching against the known unit
     * symbols list {@link #UNIT_SYMBOLS_SORTED_BY_LENGTH_DESCENDING}.
     * </p>
     *
     * @param trimmedInputText the input string that has already been trimmed; must not be {@code null}
     * @return parsed parts consisting of numeric text and unit symbol; never {@code null}
     * @throws UnitConversionException if no unit symbol can be extracted or the numeric part is missing
     * @throws NullPointerException    if {@code trimmedInputText} is {@code null}
     */
    private static ParsedParts splitIntoNumberTextAndUnitSymbol(@NonNull final String trimmedInputText) {
        final String[] tokenArray = trimmedInputText.split("\\s+");

        if (tokenArray.length >= 2) {
            final String unitSymbolText = tokenArray[tokenArray.length - 1].trim();
            final String numericText = joinTokensWithSingleSpaces(tokenArray, 0, tokenArray.length - 1).trim();

            if (numericText.isEmpty()) {
                throw new UnitConversionException("Missing numeric value. Expected format: '<number> <unitSymbol>'.");
            }
            if (unitSymbolText.isEmpty()) {
                throw new UnitConversionException("Missing unit symbol. Expected format: '<number> <unitSymbol>'.");
            }

            return new ParsedParts(numericText, unitSymbolText);
        }

        return splitSingleTokenByUnitSymbolSuffix(tokenArray[0]);
    }

    /**
     * Splits a single-token input (no whitespace) by locating the longest known unit symbol suffix.
     *
     * <p>
     * The algorithm iterates over {@link #UNIT_SYMBOLS_SORTED_BY_LENGTH_DESCENDING} to prefer the longest match.
     * If a match is found, the prefix is returned as the numeric text and the suffix as the unit symbol.
     * </p>
     *
     * @param singleTokenInput the single token input, e.g. {@code "12.5km"}; must not be {@code null}
     * @return parsed parts consisting of numeric text and unit symbol; never {@code null}
     * @throws UnitConversionException if no known unit symbol is a suffix of the token or if the numeric prefix is empty
     * @throws NullPointerException    if {@code singleTokenInput} is {@code null}
     */
    private static ParsedParts splitSingleTokenByUnitSymbolSuffix(@NonNull final String singleTokenInput) {
        for (final String unitSymbolText : UNIT_SYMBOLS_SORTED_BY_LENGTH_DESCENDING) {
            if (singleTokenInput.endsWith(unitSymbolText)) {
                final String numericText = singleTokenInput.substring(0, singleTokenInput.length() - unitSymbolText.length()).trim();
                if (numericText.isEmpty()) {
                    break;
                }
                return new ParsedParts(numericText, unitSymbolText);
            }
        }

        throw new UnitConversionException(
                "Could not extract unit symbol from '" + singleTokenInput + "'. Expected format: '<number> <unitSymbol>'."
        );
    }

    /**
     * Normalizes a locale-specific numeric string into a canonical representation using dot-decimal.
     *
     * <p>
     * Normalization performs:
     * </p>
     * <ol>
     *   <li>Removal of common non-breaking space characters used as grouping separators.</li>
     *   <li>Removal of the locale's grouping separator.</li>
     *   <li>Replacement of the locale's decimal separator with {@code '.'}.</li>
     * </ol>
     *
     * <p>
     * The returned value is intended to be accepted by numeric parsers that expect {@code '.'} as a decimal separator.
     * </p>
     *
     * @param rawNumberText          the raw numeric text (e.g. {@code "1.234,56"}); must not be {@code null}
     * @param localeForNormalization the locale defining decimal and grouping separators; must not be {@code null}
     * @return a canonical numeric text (e.g. {@code "1234.56"}); never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    private static String normalizeNumberText(
            @NonNull final String rawNumberText,
            @NonNull final Locale localeForNormalization
    ) {
        final DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(localeForNormalization);
        final char decimalSeparatorCharacter = decimalFormatSymbols.getDecimalSeparator();
        final char groupingSeparatorCharacter = decimalFormatSymbols.getGroupingSeparator();

        final String numberTextWithoutNonBreakingSpaces = rawNumberText.replace("\u00A0", "").replace("\u202F", "");

        final String numberTextWithoutGroupingSeparators = (groupingSeparatorCharacter == '\0')
                ? numberTextWithoutNonBreakingSpaces
                : numberTextWithoutNonBreakingSpaces.replace(String.valueOf(groupingSeparatorCharacter), "");

        if (decimalSeparatorCharacter == '.') {
            return numberTextWithoutGroupingSeparators;
        }

        return numberTextWithoutGroupingSeparators.replace(decimalSeparatorCharacter, '.');
    }

    /**
     * Creates an immutable list of unit symbols sorted by descending length from a registry map.
     *
     * <p>
     * The registry keys are unit symbols and the corresponding values are units. Sorting by descending length
     * ensures that suffix matching prefers longer symbols.
     * </p>
     *
     * @param unitRegistryBySymbolText a registry map from symbol to unit; must not be {@code null}
     * @return a list of unit symbols sorted by descending length; never {@code null}
     * @throws NullPointerException if {@code unitRegistryBySymbolText} is {@code null}
     */
    private static List<String> createUnitSymbolsSortedByLengthDescending(
            @NonNull final Map<String, Unit> unitRegistryBySymbolText
    ) {
        return unitRegistryBySymbolText.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
    }

    /**
     * Joins a sub-range of a token array using a single space character.
     *
     * <p>
     * This is used to reconstruct numeric text that may have been split by whitespace while still treating
     * the last token as the unit symbol.
     * </p>
     *
     * @param tokenArray        the array of tokens; must not be {@code null}
     * @param startIndexInclusive start index (inclusive) for the join
     * @param endIndexExclusive end index (exclusive) for the join
     * @return the joined string separated by single spaces; never {@code null}
     * @throws NullPointerException if {@code tokenArray} is {@code null}
     */
    private static String joinTokensWithSingleSpaces(
            @NonNull final String[] tokenArray,
            final int startIndexInclusive,
            final int endIndexExclusive
    ) {
        final StringBuilder joinedTokenStringBuilder = new StringBuilder();
        for (int currentTokenIndex = startIndexInclusive; currentTokenIndex < endIndexExclusive; currentTokenIndex++) {
            if (currentTokenIndex > startIndexInclusive) {
                joinedTokenStringBuilder.append(' ');
            }
            joinedTokenStringBuilder.append(tokenArray[currentTokenIndex]);
        }
        return joinedTokenStringBuilder.toString();
    }

    /**
     * Immutable carrier for parsed components consisting of a numeric value and a unit.
     *
     * <p>
     * This record is used as an internal return type to keep parsing logic composable and to avoid exposing
     * parsing APIs on {@link UnitValue} itself.
     * </p>
     *
     * @param value the parsed numeric value; never {@code null}
     * @param unit  the parsed unit; never {@code null}
     */
    record ParsedComponents(BigNumber value, Unit unit) {

        /**
         * Canonical constructor that validates record invariants.
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
     * Internal intermediate parsing result holding a numeric part and a unit symbol part as plain text.
     *
     * <p>
     * This record represents a split-only state prior to:
     * </p>
     * <ul>
     *   <li>resolving the unit symbol into a {@link Unit}</li>
     *   <li>parsing the number text into a {@link BigNumber}</li>
     * </ul>
     *
     * @param numberText the numeric part extracted from the input; never {@code null}
     * @param symbol     the unit symbol extracted from the input; never {@code null}
     */
    private record ParsedParts(String numberText, String symbol) {

        /**
         * Canonical constructor that validates record invariants.
         *
         * @param numberText the numeric part; must not be {@code null}
         * @param symbol     the unit symbol; must not be {@code null}
         * @throws NullPointerException if any argument is {@code null}
         */
        private ParsedParts {
            Objects.requireNonNull(numberText, "numberText must not be null");
            Objects.requireNonNull(symbol, "symbol must not be null");
        }

    }

}