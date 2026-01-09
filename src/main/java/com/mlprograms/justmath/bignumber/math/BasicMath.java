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

package com.mlprograms.justmath.bignumber.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.bignumber.math.utils.MathUtils;
import lombok.NonNull;

import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Provides core arithmetic and selected transcendental operations for {@link BigNumber} without using
 * {@code BigDecimal}, {@code BigInteger} or external math libraries.
 *
 * <h2>Internal number model</h2>
 * All calculations operate on a minimal tuple representation:
 * <pre>
 *     value = sign * digits * 10^{-scale}
 * </pre>
 * where:
 * <ul>
 *   <li>{@code sign} is {@code +1} or {@code -1}</li>
 *   <li>{@code digits} is an unsigned digit string with no leading zeros (except "0")</li>
 *   <li>{@code scale} is the number of fractional digits (>= 0)</li>
 * </ul>
 *
 * <h2>Locale handling</h2>
 * Parsing tolerates locale grouping separators and locale decimal separators.
 * Internally, plain strings use '.' as decimal separator. Output is adapted back to the provided locale.
 *
 * <h2>Negative base with non-integer exponent</h2>
 * Mathematically, {@code (-a)^b} for non-integer {@code b} is generally complex.
 * This implementation intentionally produces a real-only approximation to support expressions like
 * {@code -1.2^-2.99}:
 * <pre>
 *   a^b ≈ exp(b * ln(|a|)), then apply sign(a) if a is negative
 * </pre>
 */
public final class BasicMath {

    /**
     * Fast-path limit for exp(x) using {@code double}. {@code exp(50)} is finite, {@code exp(1000)} is not.
     * This prevents Infinity/NaN results for the fast path.
     */
    private static final double EXP_FAST_DOUBLE_MAX_ABS_ARGUMENT = 50.0;

    /**
     * Extra working precision used internally for exp Taylor series to reduce rounding noise while staying fast.
     */
    private static final int EXP_WORKING_GUARD_DIGITS = 8;

    /**
     * Hard upper bound for exp Taylor iterations as a safety net.
     */
    private static final int EXP_MAX_ITERATIONS_HARD_LIMIT = 2000;

    /**
     * Adds two {@link BigNumber} values using fast string-based decimal arithmetic.
     *
     * <p>Algorithm overview:
     * <ol>
     *   <li>Parse both inputs to (sign, digits, scale).</li>
     *   <li>Align scales by appending zeros to the smaller scale operand.</li>
     *   <li>Perform unsigned addition if signs match; otherwise perform unsigned subtraction on the larger magnitude.</li>
     *   <li>Normalize (remove redundant zeros) and format the result.</li>
     * </ol>
     *
     * @param augend the first operand; must not be {@code null}
     * @param addend the second operand; must not be {@code null}
     * @param locale the locale used for tolerant parsing and output adaptation; must not be {@code null}
     * @return {@code augend + addend} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber add(@NonNull final BigNumber augend, @NonNull final BigNumber addend, @NonNull final Locale locale) {
        final ParsedDecimalNumber augendParts = normalize(parseToParts(augend.toString(), locale));
        final ParsedDecimalNumber addendParts = normalize(parseToParts(addend.toString(), locale));

        final ParsedDecimalNumber sumParts = normalize(addParsed(augendParts, addendParts));
        return toBigNumber(sumParts, locale);
    }

    /**
     * Subtracts {@code subtrahend} from {@code minuend} using string-based arithmetic.
     *
     * <p>Implemented as:
     * <pre>
     *     minuend - subtrahend = minuend + (-subtrahend)
     * </pre>
     *
     * @param minuend    the value to subtract from; must not be {@code null}
     * @param subtrahend the value to subtract; must not be {@code null}
     * @param locale     the locale used for tolerant parsing and output adaptation; must not be {@code null}
     * @return {@code minuend - subtrahend} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber subtract(@NonNull final BigNumber minuend, @NonNull final BigNumber subtrahend, @NonNull final Locale locale) {
        final ParsedDecimalNumber minuendParts = normalize(parseToParts(minuend.toString(), locale));
        final ParsedDecimalNumber subtrahendParts = normalize(parseToParts(subtrahend.toString(), locale));

        final ParsedDecimalNumber differenceParts = normalize(addParsed(minuendParts, negate(subtrahendParts)));
        return toBigNumber(differenceParts, locale);
    }

    /**
     * Multiplies two {@link BigNumber} values using digit-array multiplication.
     *
     * <p>Algorithm overview:
     * <ul>
     *   <li>Multiply unscaled digit strings as integers (O(n*m)).</li>
     *   <li>Result scale = sum of operand scales.</li>
     *   <li>Result sign = product of operand signs.</li>
     * </ul>
     *
     * @param multiplicand the left operand; must not be {@code null}
     * @param multiplier   the right operand; must not be {@code null}
     * @param locale       the locale used for tolerant parsing and output adaptation; must not be {@code null}
     * @return {@code multiplicand * multiplier} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber multiply(@NonNull final BigNumber multiplicand, @NonNull final BigNumber multiplier, @NonNull final Locale locale) {
        final ParsedDecimalNumber multiplicandParts = normalize(parseToParts(multiplicand.toString(), locale));
        final ParsedDecimalNumber multiplierParts = normalize(parseToParts(multiplier.toString(), locale));

        final ParsedDecimalNumber productParts = normalize(multiplyParsed(multiplicandParts, multiplierParts));
        return toBigNumber(productParts, locale);
    }

    /**
     * Divides {@code dividend} by {@code divisor} and rounds according to {@link MathContext}.
     *
     * <p>This method produces a result rounded to {@code mathContext.getPrecision()} significant digits.
     * It uses string long division and includes an early-termination rule for extremely small quotients:
     * if the first significant digit would appear far beyond the requested precision, the rounded result
     * must be zero.</p>
     *
     * @param dividend    the dividend; must not be {@code null}
     * @param divisor     the divisor; must not be {@code null} and not zero
     * @param mathContext precision and rounding mode; must not be {@code null} and precision must be > 0
     * @param locale      the locale used for tolerant parsing and output adaptation; must not be {@code null}
     * @return {@code dividend / divisor} rounded to {@code mathContext}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws ArithmeticException      if {@code divisor} is zero
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber divide(@NonNull final BigNumber dividend, @NonNull final BigNumber divisor, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        final ParsedDecimalNumber dividendParts = normalize(parseToParts(dividend.toString(), locale));
        final ParsedDecimalNumber divisorParts = normalize(parseToParts(divisor.toString(), locale));

        if (isZero(divisorParts)) {
            throw new ArithmeticException("Division by zero");
        }

        final ParsedDecimalNumber quotientParts = normalize(divideParsed(dividendParts, divisorParts, mathContext));
        return toBigNumber(quotientParts, locale, mathContext);
    }

    /**
     * Computes {@code dividend mod divisor} efficiently without repeated subtraction.
     *
     * <p>Algorithm overview:
     * <ol>
     *   <li>Parse both operands.</li>
     *   <li>Scale both to a common integer scale by appending zeros to their unscaled digits.</li>
     *   <li>Compute integer remainder using unsigned long division.</li>
     *   <li>Apply sign behavior compatible with the earlier implementation:
     *       if dividend is negative and remainder != 0, return {@code |divisor| - remainder}.</li>
     * </ol>
     *
     * @param dividend the dividend; must not be {@code null}
     * @param divisor  the divisor; must not be {@code null} and not zero
     * @param locale   the locale used for tolerant parsing and output adaptation; must not be {@code null}
     * @return {@code dividend mod divisor} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if {@code divisor} is zero or an operand is invalid
     */
    public static BigNumber modulo(@NonNull final BigNumber dividend, @NonNull final BigNumber divisor, @NonNull final Locale locale) {
        final ParsedDecimalNumber dividendParts = normalize(parseToParts(dividend.toString(), locale));
        final ParsedDecimalNumber divisorParts = normalize(parseToParts(divisor.toString(), locale));

        if (isZero(divisorParts)) {
            throw new IllegalArgumentException("Cannot perform modulo operation with divisor zero.");
        }

        final ParsedDecimalNumber remainderParts = computeModulo(dividendParts, divisorParts);
        return toBigNumber(remainderParts, locale);
    }

    /**
     * Computes {@code base ^ exponent}.
     *
     * <p>Behavior:
     * <ul>
     *   <li>Integer exponent: exponentiation by squaring (fast, supports arbitrarily large integer exponents).</li>
     *   <li>Non-integer exponent: prefers fast finite-double approximation (termination + speed) if safe,
     *       otherwise uses {@code exp(exponent * ln(|base|))} fallback.</li>
     *   <li>Negative base + non-integer exponent: returns a real-only approximation by applying the base sign.</li>
     * </ul>
     *
     * @param base        base value; must not be {@code null}
     * @param exponent    exponent value; must not be {@code null}
     * @param mathContext precision and rounding mode; must not be {@code null} and precision must be > 0
     * @param locale      locale used for tolerant parsing and output adaptation; must not be {@code null}
     * @return {@code base ^ exponent} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws ArithmeticException      if {@code base == 0} and {@code exponent < 0}
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber power(@NonNull final BigNumber base, @NonNull final BigNumber exponent, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        final ParsedDecimalNumber baseParts = normalize(parseToParts(base.toString(), locale));
        final ParsedDecimalNumber exponentParts = normalize(parseToParts(exponent.toString(), locale));

        final ParsedDecimalNumber specialCaseResult = tryHandlePowerSpecialCases(baseParts, exponentParts, mathContext);
        if (specialCaseResult != null) {
            return toBigNumber(specialCaseResult, locale, mathContext);
        }

        if (isInteger(exponentParts)) {
            final ParsedDecimalNumber integerPowerResult = powerInteger(baseParts, exponentParts, mathContext);
            return toBigNumber(integerPowerResult, locale, mathContext);
        }

        final String fastDoublePowerPlain = tryComputeNonIntegerPowerUsingDouble(baseParts, exponentParts);
        if (fastDoublePowerPlain != null) {
            return new BigNumber(adaptPlainDecimalToLocale(fastDoublePowerPlain, locale), locale, mathContext).trim();
        }

        final ParsedDecimalNumber fallbackPowerResult = powerNonIntegerFallback(baseParts, exponentParts, mathContext);
        return toBigNumber(fallbackPowerResult, locale, mathContext);
    }

    /**
     * Computes the factorial {@code n!} for a non-negative integer {@code n}.
     *
     * <p>Performance strategy:
     * <ul>
     *   <li>If {@code n} fits into {@code int}, uses a product-tree (divide-and-conquer) multiplication.</li>
     *   <li>Otherwise, falls back to decrementing the full digit string and multiplying iteratively.</li>
     * </ul>
     *
     * @param argument    input value; must not be {@code null}, must be an integer and must be >= 0
     * @param mathContext validated context (factorial is exact but grows extremely large); must not be {@code null}
     * @param locale      locale used for tolerant parsing and output adaptation; must not be {@code null}
     * @return {@code argument!} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if {@code argument} is negative or not an integer
     */
    public static BigNumber factorial(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        final ParsedDecimalNumber argumentParts = normalize(parseToParts(argument.toString(), locale));
        validateFactorialInput(argumentParts);

        final String factorialDigits = computeFactorialDigits(argumentParts);
        return new BigNumber(adaptPlainDecimalToLocale(factorialDigits, locale), locale, mathContext).trim();
    }

    /**
     * Computes the exponential function {@code e^x}.
     *
     * <p>Fast path:
     * if {@code x} can be safely represented as a finite {@code double} and {@code |x| <= 50},
     * this method uses {@link Math#exp(double)} and converts the result to a plain decimal string
     * (no exponent notation). This makes small exp calls extremely fast (your test suite).</p>
     *
     * <p>Fallback path:
     * uses a string-based exp implementation with:
     * <ul>
     *   <li>power-of-two reduction {@code e^x = (e^{x/2^k})^{2^k}}</li>
     *   <li>Taylor series for the reduced exponent</li>
     *   <li>fast division by small integers for series term updates (critical for speed)</li>
     * </ul>
     * </p>
     *
     * @param argument    exponent argument {@code x}; must not be {@code null}
     * @param mathContext precision and rounding mode; must not be {@code null} and precision must be > 0
     * @param locale      locale used for tolerant parsing and output adaptation; must not be {@code null}
     * @return {@code e^x} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if the input is not a plain decimal number
     */
    public static BigNumber exp(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        final ParsedDecimalNumber exponentParts = normalize(parseToParts(argument.toString(), locale));

        final String fastExpPlain = tryComputeExpUsingDouble(exponentParts);
        if (fastExpPlain != null) {
            return new BigNumber(adaptPlainDecimalToLocale(fastExpPlain, locale), locale, mathContext).trim();
        }

        final ParsedDecimalNumber exponentialParts = expParsed(exponentParts, mathContext);
        return toBigNumber(exponentialParts, locale, mathContext);
    }

    /**
     * Minimal internal representation of a decimal value.
     *
     * @param sign   either {@code +1} or {@code -1}
     * @param digits unsigned digits without leading zeros (except "0")
     * @param scale  fractional digit count (>= 0)
     */
    private record ParsedDecimalNumber(int sign, String digits, int scale) {
    }

    /**
     * Creates an internal representation for the constant {@code 0}.
     *
     * @return parsed number for zero
     */
    private static ParsedDecimalNumber zeroParts() {
        return new ParsedDecimalNumber(+1, "0", 0);
    }

    /**
     * Creates an internal representation for the constant {@code 1}.
     *
     * @return parsed number for one
     */
    private static ParsedDecimalNumber oneParts() {
        return new ParsedDecimalNumber(+1, "1", 0);
    }

    /**
     * Checks whether a parsed number is exactly zero.
     *
     * @param parsedDecimalNumber parsed number; must not be {@code null}
     * @return {@code true} if the value equals 0
     */
    private static boolean isZero(final ParsedDecimalNumber parsedDecimalNumber) {
        return parsedDecimalNumber.digits().equals("0");
    }

    /**
     * Checks whether a parsed number is exactly one.
     *
     * @param parsedDecimalNumber parsed number; must not be {@code null}
     * @return {@code true} if the value equals 1
     */
    private static boolean isOne(final ParsedDecimalNumber parsedDecimalNumber) {
        return parsedDecimalNumber.sign() > 0 && parsedDecimalNumber.scale() == 0 && parsedDecimalNumber.digits().equals("1");
    }

    /**
     * Checks whether a parsed number is exactly minus one.
     *
     * @param parsedDecimalNumber parsed number; must not be {@code null}
     * @return {@code true} if the value equals -1
     */
    private static boolean isMinusOne(final ParsedDecimalNumber parsedDecimalNumber) {
        return parsedDecimalNumber.sign() < 0 && parsedDecimalNumber.scale() == 0 && parsedDecimalNumber.digits().equals("1");
    }

    /**
     * Returns the absolute value of the given parsed number.
     *
     * @param parsedDecimalNumber parsed number; must not be {@code null}
     * @return absolute value (sign is positive unless the value is zero)
     */
    private static ParsedDecimalNumber absoluteValue(final ParsedDecimalNumber parsedDecimalNumber) {
        return isZero(parsedDecimalNumber) ? zeroParts() : new ParsedDecimalNumber(+1, parsedDecimalNumber.digits(), parsedDecimalNumber.scale());
    }

    /**
     * Negates the given parsed number.
     *
     * @param parsedDecimalNumber parsed number; must not be {@code null}
     * @return negated value (zero remains positive zero)
     */
    private static ParsedDecimalNumber negate(final ParsedDecimalNumber parsedDecimalNumber) {
        return isZero(parsedDecimalNumber) ? parsedDecimalNumber : new ParsedDecimalNumber(-parsedDecimalNumber.sign(), parsedDecimalNumber.digits(), parsedDecimalNumber.scale());
    }

    /**
     * Normalizes a parsed number by removing redundant zeros and canonicalizing zero.
     *
     * <p>Normalization rules:
     * <ul>
     *   <li>Remove leading zeros from {@code digits}.</li>
     *   <li>Remove trailing zeros that belong to the fractional part by decreasing {@code scale}.</li>
     *   <li>If magnitude becomes zero, return canonical zero representation (+1, "0", 0).</li>
     * </ul>
     *
     * @param parsedDecimalNumber parsed number; must not be {@code null}
     * @return normalized parsed number
     */
    private static ParsedDecimalNumber normalize(final ParsedDecimalNumber parsedDecimalNumber) {
        if (parsedDecimalNumber.digits().equals("0")) {
            return zeroParts();
        }

        String normalizedDigits = stripLeadingZeros(parsedDecimalNumber.digits());
        int normalizedScale = Math.max(0, parsedDecimalNumber.scale());

        while (normalizedScale > 0 && normalizedDigits.length() > 1 && normalizedDigits.charAt(normalizedDigits.length() - 1) == '0') {
            normalizedDigits = normalizedDigits.substring(0, normalizedDigits.length() - 1);
            normalizedScale--;
        }

        normalizedDigits = stripLeadingZeros(normalizedDigits);
        if (normalizedDigits.equals("0")) {
            return zeroParts();
        }

        final int normalizedSign = parsedDecimalNumber.sign() < 0 ? -1 : +1;
        return new ParsedDecimalNumber(normalizedSign, normalizedDigits, normalizedScale);
    }

    /**
     * Determines whether a parsed number represents an integer after normalization.
     *
     * @param parsedDecimalNumber parsed number; must not be {@code null}
     * @return {@code true} if the normalized scale equals 0
     */
    private static boolean isInteger(final ParsedDecimalNumber parsedDecimalNumber) {
        return normalize(parsedDecimalNumber).scale() == 0;
    }

    /**
     * Converts an internal parsed number into a {@link BigNumber} using locale adaptation.
     *
     * @param parsedDecimalNumber internal number; must not be {@code null}
     * @param locale              locale used to adapt the decimal separator; must not be {@code null}
     * @return a new {@link BigNumber} instance
     */
    private static BigNumber toBigNumber(final ParsedDecimalNumber parsedDecimalNumber, final Locale locale) {
        return new BigNumber(adaptPlainDecimalToLocale(formatPlain(parsedDecimalNumber), locale), locale).trim();
    }

    /**
     * Converts an internal parsed number into a {@link BigNumber} using locale adaptation and a {@link MathContext}.
     *
     * @param parsedDecimalNumber internal number; must not be {@code null}
     * @param locale              locale used to adapt the decimal separator; must not be {@code null}
     * @param mathContext         math context attached to the created {@link BigNumber}; must not be {@code null}
     * @return a new {@link BigNumber} instance
     */
    private static BigNumber toBigNumber(final ParsedDecimalNumber parsedDecimalNumber, final Locale locale, final MathContext mathContext) {
        return new BigNumber(adaptPlainDecimalToLocale(formatPlain(parsedDecimalNumber), locale), locale, mathContext).trim();
    }

    /**
     * Formats a parsed number as a plain decimal string using '.' as the decimal separator.
     *
     * @param parsedDecimalNumber parsed number; must not be {@code null}
     * @return plain decimal string (no exponent notation)
     */
    private static String formatPlain(final ParsedDecimalNumber parsedDecimalNumber) {
        final ParsedDecimalNumber normalizedParts = normalize(parsedDecimalNumber);
        if (normalizedParts.digits().equals("0")) {
            return "0";
        }

        final String unsignedPlainString;
        if (normalizedParts.scale() == 0) {
            unsignedPlainString = normalizedParts.digits();
        } else if (normalizedParts.scale() >= normalizedParts.digits().length()) {
            final int leadingZeroCount = normalizedParts.scale() - normalizedParts.digits().length();
            unsignedPlainString = "0." + "0".repeat(leadingZeroCount) + normalizedParts.digits();
        } else {
            final int splitIndex = normalizedParts.digits().length() - normalizedParts.scale();
            unsignedPlainString = normalizedParts.digits().substring(0, splitIndex) + "." + normalizedParts.digits().substring(splitIndex);
        }

        return normalizedParts.sign() < 0 ? "-" + unsignedPlainString : unsignedPlainString;
    }

    /**
     * Adapts a plain decimal string (with '.') to the given locale decimal separator.
     *
     * @param plainDecimalString plain decimal string using '.'
     * @param locale             locale whose decimal separator should be used
     * @return locale-adapted decimal string
     */
    private static String adaptPlainDecimalToLocale(final String plainDecimalString, final Locale locale) {
        final char localeDecimalSeparator = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
        if (localeDecimalSeparator == '.') {
            return plainDecimalString;
        }
        return plainDecimalString.replace('.', localeDecimalSeparator);
    }

    /**
     * Parses a locale-formatted decimal string into internal representation.
     *
     * <p>Supported:
     * <ul>
     *   <li>Optional leading '+' or '-'</li>
     *   <li>Locale grouping separator (removed)</li>
     *   <li>Locale decimal separator (converted to '.')</li>
     * </ul>
     *
     * <p>Not supported:
     * <ul>
     *   <li>Exponent notation (e.g. "1.23E5")</li>
     * </ul>
     *
     * @param rawNumberString input string; must not be {@code null}
     * @param locale          locale describing separators; must not be {@code null}
     * @return parsed number (may not be normalized yet)
     */
    private static ParsedDecimalNumber parseToParts(final String rawNumberString, final Locale locale) {
        final String trimmed = rawNumberString == null ? "" : rawNumberString.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Empty number string");
        }

        final ParsedString parsedString = sanitizeAndExtractSign(trimmed, locale);
        final SplitNumberParts splitParts = splitIntegerAndFraction(parsedString.sanitizedNumberString());

        validateDigitString(splitParts.integerPart());
        validateDigitString(splitParts.fractionalPart());

        final String combinedDigits = stripLeadingZeros(normalizeEmptyIntegerPart(splitParts.integerPart()) + splitParts.fractionalPart());
        if (combinedDigits.equals("0")) {
            return zeroParts();
        }

        return new ParsedDecimalNumber(parsedString.sign(), combinedDigits, splitParts.fractionalPart().length());
    }

    /**
     * Holds a sanitized number string and its sign extracted from the original input.
     *
     * @param sign                  sign (+1 or -1)
     * @param sanitizedNumberString sanitized numeric string using '.' as decimal separator
     */
    private record ParsedString(int sign, String sanitizedNumberString) {
    }

    /**
     * Removes grouping separators and normalizes the decimal separator to '.' while extracting an optional sign.
     *
     * @param input  input string (trimmed); must not be {@code null}
     * @param locale locale describing grouping/decimal separators; must not be {@code null}
     * @return parsed sign and sanitized number string
     */
    private static ParsedString sanitizeAndExtractSign(final String input, final Locale locale) {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
        final char groupingSeparatorCharacter = symbols.getGroupingSeparator();
        final char localeDecimalSeparatorCharacter = symbols.getDecimalSeparator();

        int sign = +1;
        String sanitized = input;

        final char firstCharacter = sanitized.charAt(0);
        if (firstCharacter == '+') {
            sanitized = sanitized.substring(1);
        } else if (firstCharacter == '-') {
            sign = -1;
            sanitized = sanitized.substring(1);
        }

        sanitized = sanitized.replace(String.valueOf(groupingSeparatorCharacter), "");
        sanitized = sanitized.replace(" ", "");

        if (localeDecimalSeparatorCharacter != '.') {
            sanitized = sanitized.replace(localeDecimalSeparatorCharacter, '.');
        }

        if (sanitized.indexOf('e') >= 0 || sanitized.indexOf('E') >= 0) {
            throw new IllegalArgumentException("Exponent notation is not supported: " + input);
        }

        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Invalid number string: " + input);
        }

        return new ParsedString(sign, sanitized);
    }

    /**
     * Holds the integer and fractional part strings for a number, split by '.'.
     *
     * @param integerPart    integer part (may be empty)
     * @param fractionalPart fractional part (may be empty)
     */
    private record SplitNumberParts(String integerPart, String fractionalPart) {
    }

    /**
     * Splits a sanitized decimal string into integer and fractional part.
     *
     * @param sanitizedNumberString sanitized number string using '.' as decimal separator
     * @return split parts (never null strings)
     */
    private static SplitNumberParts splitIntegerAndFraction(final String sanitizedNumberString) {
        final int decimalPointIndex = sanitizedNumberString.indexOf('.');
        if (decimalPointIndex < 0) {
            return new SplitNumberParts(sanitizedNumberString, "");
        }

        final String integerPart = decimalPointIndex == 0 ? "" : sanitizedNumberString.substring(0, decimalPointIndex);
        final String fractionalPart = decimalPointIndex == sanitizedNumberString.length() - 1 ? "" : sanitizedNumberString.substring(decimalPointIndex + 1);
        return new SplitNumberParts(integerPart, fractionalPart);
    }

    /**
     * Converts an empty integer part to "0" to simplify downstream processing.
     *
     * @param integerPart integer part (may be empty)
     * @return "0" if empty, otherwise the original string
     */
    private static String normalizeEmptyIntegerPart(final String integerPart) {
        return integerPart == null || integerPart.isEmpty() ? "0" : integerPart;
    }

    /**
     * Validates that a string consists only of digit characters '0'..'9'.
     *
     * @param digitString digit string (may be empty)
     * @throws IllegalArgumentException if a non-digit character is encountered
     */
    private static void validateDigitString(final String digitString) {
        for (int index = 0; index < digitString.length(); index++) {
            final char character = digitString.charAt(index);
            if (character < '0' || character > '9') {
                throw new IllegalArgumentException("Invalid digit: '" + character + "'");
            }
        }
    }

    /**
     * Adds two parsed numbers with sign handling and scale alignment.
     *
     * @param left  left operand; must not be {@code null}
     * @param right right operand; must not be {@code null}
     * @return non-normalized sum
     */
    private static ParsedDecimalNumber addParsed(final ParsedDecimalNumber left, final ParsedDecimalNumber right) {
        final ParsedDecimalNumber leftNormalized = normalize(left);
        final ParsedDecimalNumber rightNormalized = normalize(right);

        if (isZero(leftNormalized)) {
            return rightNormalized;
        }
        if (isZero(rightNormalized)) {
            return leftNormalized;
        }

        final ScaleAlignedOperands aligned = alignScales(leftNormalized, rightNormalized);
        if (leftNormalized.sign() == rightNormalized.sign()) {
            final String sumDigits = addUnsigned(aligned.leftAlignedDigits(), aligned.rightAlignedDigits());
            return new ParsedDecimalNumber(leftNormalized.sign(), sumDigits, aligned.alignedScale());
        }

        final int magnitudeComparison = compareUnsigned(aligned.leftAlignedDigits(), aligned.rightAlignedDigits());
        if (magnitudeComparison == 0) {
            return zeroParts();
        }

        if (magnitudeComparison > 0) {
            final String diffDigits = subtractUnsigned(aligned.leftAlignedDigits(), aligned.rightAlignedDigits());
            return new ParsedDecimalNumber(leftNormalized.sign(), diffDigits, aligned.alignedScale());
        }

        final String diffDigits = subtractUnsigned(aligned.rightAlignedDigits(), aligned.leftAlignedDigits());
        return new ParsedDecimalNumber(rightNormalized.sign(), diffDigits, aligned.alignedScale());
    }

    /**
     * Multiplies two parsed numbers.
     *
     * @param left  left operand; must not be {@code null}
     * @param right right operand; must not be {@code null}
     * @return non-normalized product
     */
    private static ParsedDecimalNumber multiplyParsed(final ParsedDecimalNumber left, final ParsedDecimalNumber right) {
        final ParsedDecimalNumber leftNormalized = normalize(left);
        final ParsedDecimalNumber rightNormalized = normalize(right);

        if (isZero(leftNormalized) || isZero(rightNormalized)) {
            return zeroParts();
        }

        final int productSign = leftNormalized.sign() * rightNormalized.sign();
        final int productScale = leftNormalized.scale() + rightNormalized.scale();
        final String productDigits = multiplyUnsigned(leftNormalized.digits(), rightNormalized.digits());

        return new ParsedDecimalNumber(productSign, productDigits, productScale);
    }

    /**
     * Holds scale-aligned digit strings for two parsed operands.
     *
     * @param alignedScale       the aligned scale value
     * @param leftAlignedDigits  left operand digits with appended zeros
     * @param rightAlignedDigits right operand digits with appended zeros
     */
    private record ScaleAlignedOperands(int alignedScale, String leftAlignedDigits, String rightAlignedDigits) {
    }

    /**
     * Aligns the scales of two operands by appending trailing zeros to the unscaled digits of the smaller-scale operand.
     *
     * @param leftNormalized  normalized left operand
     * @param rightNormalized normalized right operand
     * @return aligned digits and the common scale
     */
    private static ScaleAlignedOperands alignScales(final ParsedDecimalNumber leftNormalized, final ParsedDecimalNumber rightNormalized) {
        final int alignedScale = Math.max(leftNormalized.scale(), rightNormalized.scale());
        final String leftAlignedDigits = appendZerosRight(leftNormalized.digits(), alignedScale - leftNormalized.scale());
        final String rightAlignedDigits = appendZerosRight(rightNormalized.digits(), alignedScale - rightNormalized.scale());
        return new ScaleAlignedOperands(alignedScale, leftAlignedDigits, rightAlignedDigits);
    }

    /**
     * Divides two parsed numbers and rounds the result to the requested {@link MathContext}.
     *
     * <p>This method implements long division on integer digit strings. To support decimal division:
     * the dividend and divisor are scaled to remove fractional parts before division.</p>
     *
     * <p><b>Termination fix:</b> If the quotient has no significant digit yet (still in leading fractional zeros)
     * and the number of leading fractional zeros exceeds {@code precision + 2}, the rounded result is guaranteed
     * to be zero. The method returns zero immediately to prevent pathological stalls in exp/ln computations.</p>
     *
     * @param dividend    dividend; must not be {@code null}
     * @param divisor     divisor; must not be {@code null} and not zero
     * @param mathContext rounding context; must not be {@code null} and precision must be > 0
     * @return rounded quotient
     */
    private static ParsedDecimalNumber divideParsed(final ParsedDecimalNumber dividend, final ParsedDecimalNumber divisor, final MathContext mathContext) {
        final ParsedDecimalNumber dividendNormalized = normalize(dividend);
        final ParsedDecimalNumber divisorNormalized = normalize(divisor);

        if (isZero(divisorNormalized)) {
            throw new ArithmeticException("Division by zero");
        }
        if (isZero(dividendNormalized)) {
            return zeroParts();
        }

        final int precision = requirePositivePrecision(mathContext);

        final DivisionSetup divisionSetup = prepareIntegerDivision(dividendNormalized, divisorNormalized);
        final UnsignedDivisionResult integerDivision = divideUnsigned(divisionSetup.dividendDigits(), divisionSetup.divisorDigits());

        final QuotientDigits quotientDigits = generateQuotientDigits(integerDivision.quotient(), integerDivision.remainder(), divisionSetup.divisorDigits(), precision);

        final ParsedDecimalNumber unrounded = new ParsedDecimalNumber(divisionSetup.quotientSign(), quotientDigits.digits(), quotientDigits.scale());

        return normalize(roundToMathContext(unrounded, mathContext));
    }

    /**
     * Ensures {@link MathContext#getPrecision()} is strictly positive.
     *
     * @param mathContext math context; must not be {@code null}
     * @return precision value
     */
    private static int requirePositivePrecision(final MathContext mathContext) {
        final int precision = mathContext.getPrecision();
        if (precision <= 0) {
            throw new IllegalArgumentException("MathContext precision must be > 0");
        }
        return precision;
    }

    /**
     * Holds prepared integer-division inputs: scaled digit strings and sign/scale metadata.
     *
     * @param quotientSign   resulting sign of the quotient
     * @param dividendDigits scaled dividend digits (unsigned integer string)
     * @param divisorDigits  scaled divisor digits (unsigned integer string)
     */
    private record DivisionSetup(int quotientSign, String dividendDigits, String divisorDigits) {
    }

    /**
     * Converts decimal division into integer division by scaling dividend or divisor digits to cancel fractional scales.
     *
     * @param dividendNormalized normalized dividend
     * @param divisorNormalized  normalized divisor
     * @return prepared division setup
     */
    private static DivisionSetup prepareIntegerDivision(final ParsedDecimalNumber dividendNormalized, final ParsedDecimalNumber divisorNormalized) {
        final int quotientSign = dividendNormalized.sign() * divisorNormalized.sign();
        final ParsedDecimalNumber dividendAbsolute = absoluteValue(dividendNormalized);
        final ParsedDecimalNumber divisorAbsolute = absoluteValue(divisorNormalized);

        String dividendDigits = dividendAbsolute.digits();
        String divisorDigits = divisorAbsolute.digits();

        final int scaleShift = divisorAbsolute.scale() - dividendAbsolute.scale();
        if (scaleShift > 0) {
            dividendDigits = appendZerosRight(dividendDigits, scaleShift);
        } else if (scaleShift < 0) {
            divisorDigits = appendZerosRight(divisorDigits, -scaleShift);
        }

        return new DivisionSetup(quotientSign, dividendDigits, divisorDigits);
    }

    /**
     * Holds a quotient digit string and its decimal scale.
     *
     * @param digits quotient digits (unsigned)
     * @param scale  number of fractional digits
     */
    private record QuotientDigits(String digits, int scale) {
    }

    /**
     * Generates quotient digits with enough significant digits for rounding.
     *
     * <p>The method:
     * <ul>
     *   <li>Starts with the integer quotient digits</li>
     *   <li>Appends fractional digits by repeatedly dividing (remainder * 10) by divisor</li>
     *   <li>Stops when (precision + 1) significant digits are reached or remainder becomes 0</li>
     * </ul>
     *
     * <p>Includes an early-zero rule for extreme cases (see class-level description).</p>
     *
     * @param integerQuotientDigits  initial integer quotient digits
     * @param initialRemainderDigits initial remainder digits
     * @param divisorDigits          divisor digits used for remainder steps
     * @param precision              target significant digits (precision + 1 will be generated when possible)
     * @return quotient digits and resulting scale
     */
    private static QuotientDigits generateQuotientDigits(final String integerQuotientDigits, final String initialRemainderDigits, final String divisorDigits, final int precision) {
        StringBuilder remainderDigits = new StringBuilder(initialRemainderDigits);

        final StringBuilder digitsBuilder = new StringBuilder(integerQuotientDigits);
        int scale = 0;

        boolean significantStarted = !integerQuotientDigits.equals("0");
        int significantCount = significantStarted ? digitsBuilder.length() : 0;

        int leadingZeroFractionDigits = 0;
        final int targetSignificantDigits = precision + 1;
        final int iterationLimit = Math.max(10_000, precision * 50);
        int iterationCount = 0;

        while (significantCount < targetSignificantDigits && !remainderDigits.toString().equals("0")) {
            iterationCount++;
            if (iterationCount > iterationLimit) {
                break;
            }

            remainderDigits.append("0");
            final UnsignedDivisionResult fractionalStep = divideUnsigned(remainderDigits.toString(), divisorDigits);

            final char nextDigit = fractionalStep.quotient().charAt(0);
            remainderDigits = new StringBuilder(fractionalStep.remainder());

            digitsBuilder.append(nextDigit);
            scale++;

            if (!significantStarted) {
                if (nextDigit == '0') {
                    leadingZeroFractionDigits++;
                    if (leadingZeroFractionDigits > precision + 2) {
                        return new QuotientDigits("0", 0);
                    }
                } else {
                    significantStarted = true;
                    significantCount = 1;
                }
            } else {
                significantCount++;
            }
        }

        final String digits = stripLeadingZeros(digitsBuilder.toString());
        return new QuotientDigits(digits, scale);
    }

    /**
     * Rounds a parsed number to the given {@link MathContext} using significant-digit rounding.
     *
     * @param parsedDecimalNumber value to round; must not be {@code null}
     * @param mathContext         rounding context; must not be {@code null}
     * @return rounded value
     */
    private static ParsedDecimalNumber roundToMathContext(final ParsedDecimalNumber parsedDecimalNumber, final MathContext mathContext) {
        final ParsedDecimalNumber normalizedValue = normalize(parsedDecimalNumber);
        if (isZero(normalizedValue)) {
            return normalizedValue;
        }

        final int precision = requirePositivePrecision(mathContext);
        final RoundingDecision roundingDecision = computeRoundingDecision(normalizedValue, precision);

        if (roundingDecision.noRoundingNeeded()) {
            return normalizedValue;
        }

        final StringBuilder keptDigits = new StringBuilder(roundingDecision.keptDigits());
        int adjustedScale = adjustScaleAfterTruncation(normalizedValue.scale(), roundingDecision.removedDigitCount());

        if (roundingDecision.incrementRequired()) {
            incrementUnsignedDecimalDigits(keptDigits);
        }

        return normalize(new ParsedDecimalNumber(normalizedValue.sign(), stripLeadingZeros(keptDigits.toString()), adjustedScale));
    }

    /**
     * Encapsulates a rounding decision computed from digits and a target precision.
     *
     * @param noRoundingNeeded  true if no rounding is required
     * @param keptDigits        digits that remain after truncation (before increment)
     * @param removedDigitCount how many digits were removed
     * @param incrementRequired whether rounding requires incrementing the kept digits
     */
    private record RoundingDecision(boolean noRoundingNeeded, String keptDigits, int removedDigitCount,
                                    boolean incrementRequired) {
    }

    /**
     * Computes how the digit string should be truncated and rounded to match a target significant-digit precision.
     *
     * @param normalizedValue normalized value to round
     * @param precision       target significant digits
     * @return rounding decision
     */
    private static RoundingDecision computeRoundingDecision(final ParsedDecimalNumber normalizedValue, final int precision) {
        final String digits = normalizedValue.digits();

        final int firstNonZeroIndex = findFirstNonZeroIndex(digits);
        if (firstNonZeroIndex < 0) {
            return new RoundingDecision(true, digits, 0, false);
        }

        final int cutIndexExclusive = firstNonZeroIndex + precision;
        if (cutIndexExclusive >= digits.length()) {
            return new RoundingDecision(true, digits, 0, false);
        }

        final char roundingDigit = digits.charAt(cutIndexExclusive);
        final boolean anyFollowingNonZeroDigit = hasNonZeroDigitAfterIndex(digits, cutIndexExclusive);

        final char lastKeptDigit = digits.charAt(cutIndexExclusive - 1);
        final boolean incrementRequired = shouldIncrementAccordingToRoundingMode(BigNumbers.DEFAULT_MATH_CONTEXT.getRoundingMode(), normalizedValue.sign(), lastKeptDigit, roundingDigit, anyFollowingNonZeroDigit);

        final String keptDigits = digits.substring(0, cutIndexExclusive);
        final int removedCount = digits.length() - cutIndexExclusive;

        return new RoundingDecision(false, keptDigits, removedCount, incrementRequired);
    }

    /**
     * Adjusts the decimal scale after truncating unscaled digits.
     *
     * <p>If digits are removed from the end of the unscaled representation, scale is reduced if possible.
     * If more digits are removed than the current scale, the "extra removal" corresponds to removing integer digits,
     * which is represented by appending zeros and setting scale to 0.</p>
     *
     * @param originalScale     original scale
     * @param removedDigitCount number of removed unscaled digits
     * @return adjusted scale
     */
    private static int adjustScaleAfterTruncation(final int originalScale, final int removedDigitCount) {
        if (removedDigitCount <= originalScale) {
            return originalScale - removedDigitCount;
        }
        // Removing more digits than fractional digits means we truncated integer digits; represent with scale=0.
        return 0;
    }

    /**
     * Checks whether any digit after a given index is non-zero.
     *
     * @param digits digit string
     * @param index  index of the rounding digit
     * @return {@code true} if any subsequent digit is non-zero
     */
    private static boolean hasNonZeroDigitAfterIndex(final String digits, final int index) {
        for (int i = index + 1; i < digits.length(); i++) {
            if (digits.charAt(i) != '0') {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether rounding should increment the kept digits based on the rounding mode.
     *
     * @param roundingMode            rounding mode to apply
     * @param resultSign              sign of the rounded number (+1 or -1)
     * @param lastKeptDigit           last kept digit character ('0'..'9')
     * @param roundingDigit           first removed digit used for rounding decision
     * @param anyFurtherNonZeroDigits whether any later removed digits are non-zero
     * @return {@code true} if increment is required
     */
    private static boolean shouldIncrementAccordingToRoundingMode(final RoundingMode roundingMode, final int resultSign, final char lastKeptDigit, final char roundingDigit, final boolean anyFurtherNonZeroDigits) {
        return switch (roundingMode) {
            case DOWN -> false;
            case UP -> roundingDigit != '0' || anyFurtherNonZeroDigits;
            case CEILING -> resultSign > 0 && (roundingDigit != '0' || anyFurtherNonZeroDigits);
            case FLOOR -> resultSign < 0 && (roundingDigit != '0' || anyFurtherNonZeroDigits);
            case UNNECESSARY -> {
                if (roundingDigit != '0' || anyFurtherNonZeroDigits) {
                    throw new ArithmeticException("Rounding necessary (RoundingMode.UNNECESSARY)");
                }
                yield false;
            }
            case HALF_UP -> roundingDigit >= '5';
            case HALF_DOWN -> roundingDigit > '5' || (roundingDigit == '5' && anyFurtherNonZeroDigits);
            case HALF_EVEN -> {
                if (roundingDigit > '5') yield true;
                if (roundingDigit < '5') yield false;
                if (anyFurtherNonZeroDigits) yield true;
                yield ((lastKeptDigit - '0') % 2) != 0;
            }
        };
    }

    /**
     * Increments an unsigned digit buffer by 1 with carry propagation.
     *
     * @param digitsBuilder digit buffer containing only digits '0'..'9'
     */
    private static void incrementUnsignedDecimalDigits(final StringBuilder digitsBuilder) {
        int index = digitsBuilder.length() - 1;
        while (index >= 0) {
            final char digit = digitsBuilder.charAt(index);
            if (digit != '9') {
                digitsBuilder.setCharAt(index, (char) (digit + 1));
                return;
            }
            digitsBuilder.setCharAt(index, '0');
            index--;
        }
        digitsBuilder.insert(0, '1');
    }

    /**
     * Finds the index of the first non-zero digit in a digit string.
     *
     * @param digits digit string
     * @return index of first non-zero digit or -1 if all digits are zero
     */
    private static int findFirstNonZeroIndex(final String digits) {
        for (int i = 0; i < digits.length(); i++) {
            if (digits.charAt(i) != '0') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Divides a parsed number by a small positive integer and rounds according to the given {@link MathContext}.
     *
     * <p>This method is optimized for repeated use in exp Taylor series where the divisor is {@code n} (1..N)
     * and for exp reduction where the divisor is {@code 2}. It avoids the full general long-division engine and
     * runs in O(L) per step, where L is the length of the digit string.</p>
     *
     * @param dividend        parsed dividend; must not be {@code null}
     * @param positiveDivisor divisor value; must be > 0
     * @param mathContext     rounding context; must not be {@code null}
     * @return {@code dividend / positiveDivisor} rounded to {@code mathContext}
     */
    private static ParsedDecimalNumber divideByPositiveIntWithRounding(final ParsedDecimalNumber dividend, final int positiveDivisor, final MathContext mathContext) {
        if (positiveDivisor <= 0) {
            throw new IllegalArgumentException("Divisor must be positive.");
        }

        final ParsedDecimalNumber normalizedDividend = normalize(dividend);
        if (isZero(normalizedDividend)) {
            return zeroParts();
        }

        final int precision = requirePositivePrecision(mathContext);

        final IntDivisionIntegerPart integerPart = divideIntegerDigitsByInt(normalizedDividend.digits(), positiveDivisor);
        final FractionDigits fractionDigits = generateFractionDigitsForIntDivision(integerPart.remainder(), positiveDivisor, precision);

        final String combinedDigits = stripLeadingZeros(integerPart.quotientDigits() + fractionDigits.fractionalDigits());
        final int combinedScale = normalizedDividend.scale() + fractionDigits.fractionalScale();

        final ParsedDecimalNumber unrounded = new ParsedDecimalNumber(normalizedDividend.sign(), combinedDigits, combinedScale);
        return normalize(roundToMathContext(unrounded, mathContext));
    }

    /**
     * Holds the quotient digits and remainder of dividing an unsigned integer digit string by a small int.
     *
     * @param quotientDigits quotient digits
     * @param remainder      remainder as int
     */
    private record IntDivisionIntegerPart(String quotientDigits, int remainder) {
    }

    /**
     * Divides an unsigned integer digit string by a small positive integer.
     *
     * @param unsignedDigits  digits to divide (no sign)
     * @param positiveDivisor divisor (>0)
     * @return quotient digits and remainder
     */
    private static IntDivisionIntegerPart divideIntegerDigitsByInt(final String unsignedDigits, final int positiveDivisor) {
        int remainder = 0;
        final StringBuilder quotientBuilder = new StringBuilder(unsignedDigits.length());

        for (int i = 0; i < unsignedDigits.length(); i++) {
            final int currentValue = remainder * 10 + (unsignedDigits.charAt(i) - '0');
            final int quotientDigit = currentValue / positiveDivisor;
            remainder = currentValue % positiveDivisor;
            quotientBuilder.append((char) ('0' + quotientDigit));
        }

        return new IntDivisionIntegerPart(stripLeadingZeros(quotientBuilder.toString()), remainder);
    }

    /**
     * Holds generated fractional digits and their scale (digit count).
     *
     * @param fractionalDigits digits generated for the fractional part
     * @param fractionalScale  number of digits generated
     */
    private record FractionDigits(String fractionalDigits, int fractionalScale) {
    }

    /**
     * Generates fractional digits for an int division until enough significant digits are available.
     *
     * <p>This method also applies an early-zero rule: if the quotient has not reached any significant digit and
     * the number of leading fractional zeros exceeds {@code precision + 2}, the rounded value is guaranteed to be zero.</p>
     *
     * @param initialRemainder initial remainder from integer division
     * @param positiveDivisor  divisor (>0)
     * @param precision        target significant digits (precision+1 digits are typically generated)
     * @return fractional digits and digit count
     */
    private static FractionDigits generateFractionDigitsForIntDivision(final int initialRemainder, final int positiveDivisor, final int precision) {
        int remainder = initialRemainder;

        final StringBuilder fractionalDigitsBuilder = new StringBuilder();
        int scale = 0;

        int significantDigitsProduced = 0;
        boolean significantStarted = false;

        int leadingZeroFractionCount = 0;
        final int targetSignificantDigits = precision + 1;

        while (significantDigitsProduced < targetSignificantDigits && remainder != 0) {
            final int expanded = remainder * 10;
            final int digit = expanded / positiveDivisor;
            remainder = expanded % positiveDivisor;

            fractionalDigitsBuilder.append((char) ('0' + digit));
            scale++;

            if (!significantStarted) {
                if (digit == 0) {
                    leadingZeroFractionCount++;
                    if (leadingZeroFractionCount > precision + 2) {
                        return new FractionDigits("", 0);
                    }
                } else {
                    significantStarted = true;
                    significantDigitsProduced = 1;
                }
            } else {
                significantDigitsProduced++;
            }

            if (scale > 10_000) {
                break;
            }
        }

        return new FractionDigits(fractionalDigitsBuilder.toString(), scale);
    }

    /**
     * Compares two unsigned digit strings as integers.
     *
     * @param leftUnsignedDigits  left digits
     * @param rightUnsignedDigits right digits
     * @return -1 if left < right, 0 if equal, +1 if left > right
     */
    private static int compareUnsigned(final String leftUnsignedDigits, final String rightUnsignedDigits) {
        final String leftNormalized = stripLeadingZeros(leftUnsignedDigits);
        final String rightNormalized = stripLeadingZeros(rightUnsignedDigits);

        if (leftNormalized.length() != rightNormalized.length()) {
            return Integer.compare(leftNormalized.length(), rightNormalized.length());
        }

        return leftNormalized.compareTo(rightNormalized);
    }

    /**
     * Adds two unsigned digit strings.
     *
     * @param leftUnsignedDigits  left digits
     * @param rightUnsignedDigits right digits
     * @return unsigned sum digits
     */
    private static String addUnsigned(final String leftUnsignedDigits, final String rightUnsignedDigits) {
        int leftIndex = leftUnsignedDigits.length() - 1;
        int rightIndex = rightUnsignedDigits.length() - 1;
        int carry = 0;

        final StringBuilder result = new StringBuilder(Math.max(leftUnsignedDigits.length(), rightUnsignedDigits.length()) + 1);

        while (leftIndex >= 0 || rightIndex >= 0 || carry != 0) {
            int sum = carry;
            if (leftIndex >= 0) sum += leftUnsignedDigits.charAt(leftIndex--) - '0';
            if (rightIndex >= 0) sum += rightUnsignedDigits.charAt(rightIndex--) - '0';

            result.append((char) ('0' + (sum % 10)));
            carry = sum / 10;
        }

        return stripLeadingZeros(result.reverse().toString());
    }

    /**
     * Subtracts {@code right} from {@code left} for unsigned digit strings, assuming {@code left >= right}.
     *
     * @param leftUnsignedDigits  minuend digits
     * @param rightUnsignedDigits subtrahend digits
     * @return unsigned difference digits
     */
    private static String subtractUnsigned(final String leftUnsignedDigits, final String rightUnsignedDigits) {
        int leftIndex = leftUnsignedDigits.length() - 1;
        int rightIndex = rightUnsignedDigits.length() - 1;
        int borrow = 0;

        final StringBuilder result = new StringBuilder(leftUnsignedDigits.length());

        while (leftIndex >= 0) {
            int diff = (leftUnsignedDigits.charAt(leftIndex--) - '0') - borrow;
            if (rightIndex >= 0) diff -= (rightUnsignedDigits.charAt(rightIndex--) - '0');

            if (diff < 0) {
                diff += 10;
                borrow = 1;
            } else {
                borrow = 0;
            }

            result.append((char) ('0' + diff));
        }

        return stripLeadingZeros(result.reverse().toString());
    }

    /**
     * Multiplies two unsigned digit strings using an accumulator array.
     *
     * @param leftUnsignedDigits  left digits
     * @param rightUnsignedDigits right digits
     * @return unsigned product digits
     */
    private static String multiplyUnsigned(final String leftUnsignedDigits, final String rightUnsignedDigits) {
        final String leftNormalized = stripLeadingZeros(leftUnsignedDigits);
        final String rightNormalized = stripLeadingZeros(rightUnsignedDigits);

        if (leftNormalized.equals("0") || rightNormalized.equals("0")) {
            return "0";
        }

        final int leftLength = leftNormalized.length();
        final int rightLength = rightNormalized.length();
        final int[] accumulator = new int[leftLength + rightLength];

        for (int i = leftLength - 1; i >= 0; i--) {
            final int leftDigit = leftNormalized.charAt(i) - '0';
            int carry = 0;

            for (int j = rightLength - 1; j >= 0; j--) {
                final int rightDigit = rightNormalized.charAt(j) - '0';
                final int index = i + j + 1;

                final int sum = accumulator[index] + leftDigit * rightDigit + carry;
                accumulator[index] = sum % 10;
                carry = sum / 10;
            }

            accumulator[i] += carry;
        }

        final StringBuilder product = new StringBuilder(accumulator.length);
        int start = 0;
        while (start < accumulator.length - 1 && accumulator[start] == 0) {
            start++;
        }
        for (int k = start; k < accumulator.length; k++) {
            product.append((char) ('0' + accumulator[k]));
        }

        return product.toString();
    }

    /**
     * Holds quotient and remainder for unsigned division.
     *
     * @param quotient  quotient digits
     * @param remainder remainder digits
     */
    private record UnsignedDivisionResult(String quotient, String remainder) {
    }

    /**
     * Performs unsigned long division {@code dividend / divisor} and returns quotient and remainder.
     *
     * @param dividendUnsignedDigits dividend digits (unsigned)
     * @param divisorUnsignedDigits  divisor digits (unsigned, not "0")
     * @return quotient and remainder
     */
    private static UnsignedDivisionResult divideUnsigned(final String dividendUnsignedDigits, final String divisorUnsignedDigits) {
        final String dividend = stripLeadingZeros(dividendUnsignedDigits);
        final String divisor = stripLeadingZeros(divisorUnsignedDigits);

        if (divisor.equals("0")) {
            throw new ArithmeticException("Division by zero");
        }
        if (dividend.equals("0")) {
            return new UnsignedDivisionResult("0", "0");
        }
        if (compareUnsigned(dividend, divisor) < 0) {
            return new UnsignedDivisionResult("0", dividend);
        }
        if (divisor.equals("1")) {
            return new UnsignedDivisionResult(dividend, "0");
        }

        final StringBuilder quotientBuilder = new StringBuilder(dividend.length());
        String remainder = "0";

        for (int i = 0; i < dividend.length(); i++) {
            remainder = remainder.equals("0") ? String.valueOf(dividend.charAt(i)) : (remainder + dividend.charAt(i));
            remainder = stripLeadingZeros(remainder);

            final int quotientDigit = estimateQuotientDigit(remainder, divisor);
            quotientBuilder.append((char) ('0' + quotientDigit));

            if (quotientDigit != 0) {
                final String product = multiplyBySingleDigit(divisor, quotientDigit);
                remainder = subtractUnsigned(remainder, product);
            }

            remainder = stripLeadingZeros(remainder);
        }

        return new UnsignedDivisionResult(stripLeadingZeros(quotientBuilder.toString()), stripLeadingZeros(remainder));
    }

    /**
     * Estimates a single quotient digit in the range 0..9 for long division using binary search.
     *
     * @param remainderUnsignedDigits current remainder
     * @param divisorUnsignedDigits   divisor
     * @return maximal digit {@code d} such that {@code divisor * d <= remainder}
     */
    private static int estimateQuotientDigit(final String remainderUnsignedDigits, final String divisorUnsignedDigits) {
        int low = 0;
        int high = 9;
        int best = 0;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final String product = multiplyBySingleDigit(divisorUnsignedDigits, mid);
            final int comparison = compareUnsigned(product, remainderUnsignedDigits);

            if (comparison <= 0) {
                best = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return best;
    }

    /**
     * Multiplies an unsigned digit string by a single digit in 0..9.
     *
     * @param unsignedDigits digits to multiply
     * @param digitValue     digit multiplier
     * @return product digits
     */
    private static String multiplyBySingleDigit(final String unsignedDigits, final int digitValue) {
        if (digitValue == 0) {
            return "0";
        }
        if (digitValue == 1) {
            return stripLeadingZeros(unsignedDigits);
        }

        int carry = 0;
        final StringBuilder product = new StringBuilder(unsignedDigits.length() + 1);

        for (int i = unsignedDigits.length() - 1; i >= 0; i--) {
            final int value = (unsignedDigits.charAt(i) - '0') * digitValue + carry;
            product.append((char) ('0' + (value % 10)));
            carry = value / 10;
        }

        while (carry != 0) {
            product.append((char) ('0' + (carry % 10)));
            carry /= 10;
        }

        return stripLeadingZeros(product.reverse().toString());
    }

    /**
     * Computes the modulo using scale-to-integer and unsigned remainder.
     *
     * @param dividendParts parsed dividend
     * @param divisorParts  parsed divisor
     * @return remainder parts (normalized)
     */
    private static ParsedDecimalNumber computeModulo(final ParsedDecimalNumber dividendParts, final ParsedDecimalNumber divisorParts) {
        final ParsedDecimalNumber dividendAbsolute = absoluteValue(dividendParts);
        final ParsedDecimalNumber divisorAbsolute = absoluteValue(divisorParts);

        final int commonScale = Math.max(dividendAbsolute.scale(), divisorAbsolute.scale());
        final String dividendScaledDigits = appendZerosRight(dividendAbsolute.digits(), commonScale - dividendAbsolute.scale());
        final String divisorScaledDigits = appendZerosRight(divisorAbsolute.digits(), commonScale - divisorAbsolute.scale());

        final UnsignedDivisionResult result = divideUnsigned(dividendScaledDigits, divisorScaledDigits);

        ParsedDecimalNumber remainder = normalize(new ParsedDecimalNumber(+1, result.remainder(), commonScale));
        if (dividendParts.sign() < 0 && !isZero(remainder)) {
            remainder = normalize(addParsed(divisorAbsolute, negate(remainder)));
        }

        return remainder;
    }

    /**
     * Handles power edge cases (exponent 0/1/-1, base 0 with negative exponent, etc.).
     *
     * @param baseParts     parsed base
     * @param exponentParts parsed exponent
     * @param mathContext   rounding context
     * @return parsed result if handled, otherwise {@code null}
     */
    private static ParsedDecimalNumber tryHandlePowerSpecialCases(final ParsedDecimalNumber baseParts, final ParsedDecimalNumber exponentParts, final MathContext mathContext) {
        if (isZero(exponentParts)) {
            return oneParts();
        }
        if (isOne(exponentParts)) {
            return baseParts;
        }
        if (isMinusOne(exponentParts)) {
            if (isZero(baseParts)) {
                throw new ArithmeticException("Division by zero");
            }
            return divideParsed(oneParts(), baseParts, mathContext);
        }

        if (isZero(baseParts) && exponentParts.sign() < 0) {
            throw new ArithmeticException("Cannot compute 0^negative (log undefined)");
        }
        if (isZero(baseParts) && exponentParts.sign() > 0) {
            return zeroParts();
        }

        return null;
    }

    /**
     * Computes integer powers using exponentiation by squaring.
     *
     * @param baseParts     base value
     * @param exponentParts integer exponent (scale must be 0)
     * @param mathContext   rounding context (used for negative exponents)
     * @return {@code base^exponent}
     */
    private static ParsedDecimalNumber powerInteger(final ParsedDecimalNumber baseParts, final ParsedDecimalNumber exponentParts, final MathContext mathContext) {
        final ParsedDecimalNumber baseNormalized = normalize(baseParts);
        final ParsedDecimalNumber exponentNormalized = normalize(exponentParts);

        if (!isInteger(exponentNormalized)) {
            throw new IllegalArgumentException("Exponent must be an integer for integer power.");
        }

        final boolean exponentIsNegative = exponentNormalized.sign() < 0;
        final String exponentAbsoluteDigits = exponentNormalized.digits();

        final ParsedDecimalNumber baseAbsolute = absoluteValue(baseNormalized);
        final boolean baseIsNegative = baseNormalized.sign() < 0;
        final boolean exponentIsOdd = isOddUnsigned(exponentAbsoluteDigits);
        final int resultSign = (baseIsNegative && exponentIsOdd) ? -1 : +1;

        ParsedDecimalNumber result = oneParts();
        ParsedDecimalNumber basePower = baseAbsolute;

        String remainingExponent = stripLeadingZeros(exponentAbsoluteDigits);
        while (!remainingExponent.equals("0")) {
            if (isOddUnsigned(remainingExponent)) {
                result = normalize(multiplyParsed(result, basePower));
            }
            basePower = normalize(multiplyParsed(basePower, basePower));
            remainingExponent = divideUnsignedByTwo(remainingExponent);
        }

        result = normalize(new ParsedDecimalNumber(resultSign, result.digits(), result.scale()));
        if (!exponentIsNegative) {
            return result;
        }

        if (isZero(result)) {
            throw new ArithmeticException("Division by zero");
        }

        return divideParsed(oneParts(), result, mathContext);
    }

    /**
     * Fallback implementation for non-integer powers using {@code exp(exponent * ln(|base|))}.
     *
     * @param baseParts     base value
     * @param exponentParts exponent value (non-integer)
     * @param mathContext   rounding context
     * @return power result (real-only)
     */
    private static ParsedDecimalNumber powerNonIntegerFallback(final ParsedDecimalNumber baseParts, final ParsedDecimalNumber exponentParts, final MathContext mathContext) {
        final ParsedDecimalNumber lnAbsBase = lnParsed(absoluteValue(baseParts), mathContext);
        final ParsedDecimalNumber exponentTimesLn = normalize(multiplyParsed(exponentParts, lnAbsBase));
        final ParsedDecimalNumber absoluteResult = expParsed(exponentTimesLn, mathContext);

        return baseParts.sign() < 0 ? negate(absoluteResult) : absoluteResult;
    }

    /**
     * Attempts to compute a non-integer power using fast double math:
     * <pre>
     *   a^b ≈ exp(b * ln(|a|)), then apply sign(a) if a is negative
     * </pre>
     *
     * @param baseParts     parsed base
     * @param exponentParts parsed exponent (non-integer)
     * @return plain decimal result string or {@code null} if double conversion is not safe/finite
     */
    private static String tryComputeNonIntegerPowerUsingDouble(final ParsedDecimalNumber baseParts, final ParsedDecimalNumber exponentParts) {
        final Double absoluteBase = tryConvertToFiniteDouble(absoluteValue(baseParts));
        final Double exponent = tryConvertToFiniteDouble(exponentParts);

        if (absoluteBase == null || exponent == null) {
            return null;
        }
        if (!(absoluteBase > 0.0) || !Double.isFinite(absoluteBase) || !Double.isFinite(exponent)) {
            return null;
        }

        final double absoluteResult = Math.exp(exponent * Math.log(absoluteBase));
        if (!Double.isFinite(absoluteResult)) {
            return null;
        }

        final double signedResult = baseParts.sign() < 0 ? -absoluteResult : absoluteResult;
        return toPlainDecimalStringFromDouble(signedResult);
    }

    /**
     * Checks whether an unsigned integer digit string represents an odd number.
     *
     * @param unsignedDigits unsigned digits
     * @return {@code true} if odd
     */
    private static boolean isOddUnsigned(final String unsignedDigits) {
        final char last = unsignedDigits.charAt(unsignedDigits.length() - 1);
        return ((last - '0') % 2) != 0;
    }

    /**
     * Divides an unsigned integer digit string by two.
     *
     * @param unsignedDigits unsigned digits
     * @return quotient digits
     */
    private static String divideUnsignedByTwo(final String unsignedDigits) {
        final String normalized = stripLeadingZeros(unsignedDigits);
        if (normalized.equals("0")) {
            return "0";
        }

        final StringBuilder quotient = new StringBuilder(normalized.length());
        int carry = 0;

        for (int i = 0; i < normalized.length(); i++) {
            final int value = carry * 10 + (normalized.charAt(i) - '0');
            quotient.append((char) ('0' + (value / 2)));
            carry = value % 2;
        }

        return stripLeadingZeros(quotient.toString());
    }

    /**
     * Validates that the factorial input is a non-negative integer.
     *
     * @param argumentParts parsed argument
     */
    private static void validateFactorialInput(final ParsedDecimalNumber argumentParts) {
        if (!isInteger(argumentParts)) {
            throw new IllegalArgumentException("Factorial is only defined for integers.");
        }
        if (argumentParts.sign() < 0) {
            throw new IllegalArgumentException("Factorial is only defined for non-negative integers.");
        }
    }

    /**
     * Computes factorial digits for a validated non-negative integer input.
     *
     * @param argumentParts integer input (parsed)
     * @return factorial digits as unsigned string
     */
    private static String computeFactorialDigits(final ParsedDecimalNumber argumentParts) {
        if (isZero(argumentParts)) {
            return "1";
        }

        final Integer asInt = tryParseUnsignedInt(argumentParts.digits());
        if (asInt != null) {
            return factorialUnsignedInt(asInt);
        }

        return factorialUnsignedString(argumentParts.digits());
    }

    /**
     * Computes factorial for an int using a product-tree strategy.
     *
     * @param n integer n >= 0
     * @return n! as unsigned digits
     */
    private static String factorialUnsignedInt(final int n) {
        if (n < 2) {
            return "1";
        }
        return multiplyRangeUnsigned(2, n);
    }

    /**
     * Multiplies the integer range [start, end] using divide-and-conquer (product tree).
     *
     * @param startInclusive range start
     * @param endInclusive   range end
     * @return product as unsigned digits
     */
    private static String multiplyRangeUnsigned(final int startInclusive, final int endInclusive) {
        if (startInclusive > endInclusive) {
            return "1";
        }
        if (startInclusive == endInclusive) {
            return Integer.toString(startInclusive);
        }
        if (endInclusive - startInclusive == 1) {
            return multiplyUnsigned(Integer.toString(startInclusive), Integer.toString(endInclusive));
        }

        final int mid = (startInclusive + endInclusive) >>> 1;
        final String left = multiplyRangeUnsigned(startInclusive, mid);
        final String right = multiplyRangeUnsigned(mid + 1, endInclusive);
        return multiplyUnsigned(left, right);
    }

    /**
     * Computes factorial for an arbitrarily large integer digit string by decrementing and multiplying.
     *
     * @param unsignedIntegerDigits unsigned digits of n
     * @return n! as unsigned digits
     */
    private static String factorialUnsignedString(final String unsignedIntegerDigits) {
        String counter = stripLeadingZeros(unsignedIntegerDigits);
        if (counter.equals("0") || counter.equals("1")) {
            return "1";
        }

        String result = "1";
        while (!counter.equals("1")) {
            result = multiplyUnsigned(result, counter);
            counter = decrementUnsigned(counter);
        }

        return result;
    }

    /**
     * Decrements an unsigned digit string by one (assuming value >= 1).
     *
     * @param unsignedDigits unsigned digits
     * @return decremented digits
     */
    private static String decrementUnsigned(final String unsignedDigits) {
        final char[] digits = stripLeadingZeros(unsignedDigits).toCharArray();
        int index = digits.length - 1;

        while (index >= 0 && digits[index] == '0') {
            digits[index] = '9';
            index--;
        }
        if (index >= 0) {
            digits[index] = (char) (digits[index] - 1);
        }

        return stripLeadingZeros(new String(digits));
    }

    /**
     * Attempts to parse an unsigned digit string into {@code int}. Returns null if it does not fit.
     *
     * @param unsignedDigits unsigned digits
     * @return int value or {@code null} if overflow would occur
     */
    private static Integer tryParseUnsignedInt(final String unsignedDigits) {
        final String normalized = stripLeadingZeros(unsignedDigits);
        if (normalized.length() > 10) {
            return null;
        }

        long value = 0L;
        for (int i = 0; i < normalized.length(); i++) {
            value = value * 10L + (normalized.charAt(i) - '0');
            if (value > Integer.MAX_VALUE) {
                return null;
            }
        }

        return (int) value;
    }

    /**
     * Attempts to compute {@code exp(x)} using {@code double} for maximum speed.
     *
     * @param exponentParts parsed exponent
     * @return plain decimal string result, or {@code null} if not safe/finite
     */
    private static String tryComputeExpUsingDouble(final ParsedDecimalNumber exponentParts) {
        final Double x = tryConvertToFiniteDouble(exponentParts);
        if (x == null) {
            return null;
        }
        if (Math.abs(x) > EXP_FAST_DOUBLE_MAX_ABS_ARGUMENT) {
            return null;
        }

        final double value = Math.exp(x);
        if (!Double.isFinite(value)) {
            return null;
        }

        return toPlainDecimalStringFromDouble(value);
    }

    /**
     * Computes {@code exp(x)} using string arithmetic.
     *
     * <p>Implementation:
     * <ol>
     *   <li>If x is negative: compute 1/exp(|x|).</li>
     *   <li>Choose a small reduction power k and compute x' = x / 2^k using fast int division.</li>
     *   <li>Compute exp(x') by Taylor series with fast division term/n.</li>
     *   <li>Undo reduction by repeated squaring (k times).</li>
     * </ol>
     *
     * @param exponentParts exponent x
     * @param mathContext   precision and rounding mode
     * @return exp(x) as parsed number
     */
    private static ParsedDecimalNumber expParsed(final ParsedDecimalNumber exponentParts, final MathContext mathContext) {
        final ParsedDecimalNumber normalizedExponent = normalize(exponentParts);

        if (isZero(normalizedExponent)) {
            return oneParts();
        }

        if (normalizedExponent.sign() < 0) {
            final ParsedDecimalNumber positiveExponent = negate(normalizedExponent);
            final ParsedDecimalNumber positiveExp = expParsed(positiveExponent, mathContext);
            return normalize(divideParsed(oneParts(), positiveExp, mathContext));
        }

        final MathContext workingContext = createWorkingMathContext(mathContext);
        final int reductionPower = chooseReductionPowerForExp(normalizedExponent);

        final ParsedDecimalNumber reducedExponent = reduceExponentByPowerOfTwo(normalizedExponent, reductionPower, workingContext);
        final ParsedDecimalNumber reducedExpValue = expTaylorSeries(reducedExponent, workingContext, mathContext.getPrecision());

        final ParsedDecimalNumber scaled = undoReductionBySquaring(reducedExpValue, reductionPower, workingContext);
        return normalize(roundToMathContext(scaled, mathContext));
    }

    /**
     * Creates a working {@link MathContext} with slightly increased precision for intermediate exp computations.
     *
     * @param requestedContext requested context
     * @return working context with guard digits
     */
    private static MathContext createWorkingMathContext(final MathContext requestedContext) {
        final int workingPrecision = Math.max(10, requestedContext.getPrecision() + EXP_WORKING_GUARD_DIGITS);
        return new MathContext(workingPrecision, requestedContext.getRoundingMode());
    }

    /**
     * Chooses a small reduction power k for exp reduction {@code x -> x / 2^k}.
     *
     * <p>This is a performance heuristic:
     * smaller |x| -> smaller k to avoid overhead; larger |x| -> slightly larger k to improve Taylor convergence.</p>
     *
     * @param nonNegativeNormalizedExponent normalized exponent with non-negative sign
     * @return reduction power k >= 0
     */
    private static int chooseReductionPowerForExp(final ParsedDecimalNumber nonNegativeNormalizedExponent) {
        final Double asDouble = tryConvertToFiniteDouble(nonNegativeNormalizedExponent);
        if (asDouble != null) {
            final double absolute = Math.abs(asDouble);
            if (absolute <= 1.0) {
                return 0;
            }
            if (absolute <= 4.0) {
                return 2;
            }
            if (absolute <= 10.0) {
                return 3;
            }

            return 4;
        }

        final int integerDigits = Math.max(0, nonNegativeNormalizedExponent.digits().length() - nonNegativeNormalizedExponent.scale());
        if (integerDigits <= 1) {
            return 0;
        }
        if (integerDigits == 2) {
            return 2;
        }

        return 4;
    }

    /**
     * Reduces an exponent by dividing it by 2 repeatedly {@code reductionPower} times.
     *
     * @param exponent       normalized exponent
     * @param reductionPower number of divisions by 2
     * @param workingContext working rounding context
     * @return reduced exponent
     */
    private static ParsedDecimalNumber reduceExponentByPowerOfTwo(final ParsedDecimalNumber exponent, final int reductionPower, final MathContext workingContext) {
        ParsedDecimalNumber reduced = exponent;
        for (int i = 0; i < reductionPower; i++) {
            reduced = divideByPositiveIntWithRounding(reduced, 2, workingContext);
        }

        return reduced;
    }

    /**
     * Computes exp(x) using a Taylor series for the given reduced exponent.
     *
     * <p>Series:
     * <pre>
     *   exp(x) = sum_{n=0..∞} x^n / n!
     * </pre>
     * Iteration:
     * <pre>
     *   term_{n} = term_{n-1} * x / n
     * </pre>
     *
     * <p>Division by {@code n} is performed using the fast int division routine to maximize speed.</p>
     *
     * @param reducedExponent    reduced exponent x
     * @param workingContext     working rounding context for intermediate steps
     * @param requestedPrecision requested precision used to build epsilon threshold
     * @return exp(x) approximation
     */
    private static ParsedDecimalNumber expTaylorSeries(final ParsedDecimalNumber reducedExponent, final MathContext workingContext, final int requestedPrecision) {
        ParsedDecimalNumber sum = oneParts();
        ParsedDecimalNumber term = oneParts();

        final ParsedDecimalNumber epsilon = normalize(new ParsedDecimalNumber(+1, "1", requestedPrecision));
        final int maxIterations = Math.min(EXP_MAX_ITERATIONS_HARD_LIMIT, Math.max(200, workingContext.getPrecision() * 6));

        for (int n = 1; n <= maxIterations; n++) {
            term = normalize(multiplyParsed(term, reducedExponent));
            term = divideByPositiveIntWithRounding(term, n, workingContext);

            sum = normalize(addParsed(sum, term));

            final ParsedDecimalNumber absTerm = term.sign() < 0 ? negate(term) : term;
            if (compareAbsolute(absTerm, epsilon) < 0) {
                break;
            }
        }

        return sum;
    }

    /**
     * Undoes exp reduction by repeated squaring: {@code (exp(x/2^k))^(2^k)}.
     *
     * @param reducedExp     exp(x/2^k)
     * @param reductionPower k
     * @param workingContext working rounding context for intermediate squarings
     * @return exp(x)
     */
    private static ParsedDecimalNumber undoReductionBySquaring(final ParsedDecimalNumber reducedExp, final int reductionPower, final MathContext workingContext) {
        ParsedDecimalNumber value = reducedExp;
        for (int i = 0; i < reductionPower; i++) {
            value = normalize(multiplyParsed(value, value));
            value = normalize(roundToMathContext(value, workingContext));
        }

        return value;
    }

    /**
     * Computes {@code ln(x)} for {@code x > 0} using Newton iteration on {@code exp(y) - x = 0}.
     *
     * <p>Iteration:
     * <pre>
     *   y_{n+1} = y_n + (x - exp(y_n)) / exp(y_n)
     * </pre>
     * A double-based initial guess is used to speed up convergence.</p>
     *
     * @param positiveParts x > 0
     * @param mathContext   precision and rounding mode
     * @return ln(x)
     */
    private static ParsedDecimalNumber lnParsed(final ParsedDecimalNumber positiveParts, final MathContext mathContext) {
        final ParsedDecimalNumber x = normalize(positiveParts);
        if (x.sign() < 0 || isZero(x)) {
            throw new ArithmeticException("ln(x) is only defined for x > 0");
        }
        if (isOne(x)) {
            return zeroParts();
        }

        ParsedDecimalNumber y = initialGuessForLn(x);
        final ParsedDecimalNumber epsilon = normalize(new ParsedDecimalNumber(+1, "1", mathContext.getPrecision()));

        final int maxIterations = Math.max(20, mathContext.getPrecision() * 6);
        for (int i = 0; i < maxIterations; i++) {
            final ParsedDecimalNumber expY = expParsed(y, mathContext);
            final ParsedDecimalNumber numerator = normalize(addParsed(x, negate(expY)));
            final ParsedDecimalNumber delta = divideParsed(numerator, expY, mathContext);

            y = normalize(roundToMathContext(addParsed(y, delta), mathContext));

            final ParsedDecimalNumber absDelta = delta.sign() < 0 ? negate(delta) : delta;
            if (compareAbsolute(absDelta, epsilon) < 0) {
                break;
            }
        }

        return y;
    }

    /**
     * Produces an initial guess for ln(x) using magnitude estimates derived from digits and a double approximation.
     *
     * @param normalizedPositiveX normalized positive x
     * @return initial y ~ ln(x)
     */
    private static ParsedDecimalNumber initialGuessForLn(final ParsedDecimalNumber normalizedPositiveX) {
        final int exponentBase10 = estimateBase10Exponent(normalizedPositiveX);
        final double mantissa = estimateMantissaAsDouble(normalizedPositiveX);

        double guess = Math.log(mantissa) + exponentBase10 * Math.log(10.0);
        if (!Double.isFinite(guess)) {
            guess = 0.0;
        }

        return normalize(parseToParts(toPlainDecimalStringFromDouble(guess), Locale.US));
    }

    /**
     * Estimates base-10 exponent roughly equal to {@code floor(log10(x))} for x > 0.
     *
     * @param positiveNormalizedValue normalized positive value
     * @return base-10 exponent estimate
     */
    private static int estimateBase10Exponent(final ParsedDecimalNumber positiveNormalizedValue) {
        final int digitCount = positiveNormalizedValue.digits().length();
        if (digitCount > positiveNormalizedValue.scale()) {
            final int integerDigits = digitCount - positiveNormalizedValue.scale();
            return integerDigits - 1;
        }

        final int leadingFractionalZeros = positiveNormalizedValue.scale() - digitCount;
        return -(leadingFractionalZeros + 1);
    }

    /**
     * Estimates a mantissa in approximately [1, 10) using the first up to 16 digits.
     *
     * @param positiveNormalizedValue normalized positive value
     * @return mantissa as double
     */
    private static double estimateMantissaAsDouble(final ParsedDecimalNumber positiveNormalizedValue) {
        final String digits = positiveNormalizedValue.digits();
        final int take = Math.min(16, digits.length());

        long leading = 0L;
        for (int i = 0; i < take; i++) {
            leading = leading * 10L + (digits.charAt(i) - '0');
        }

        return leading / Math.pow(10.0, take - 1);
    }

    /**
     * Compares absolute values of two parsed numbers.
     *
     * @param left  left value
     * @param right right value
     * @return -1, 0, +1 according to absolute comparison
     */
    private static int compareAbsolute(final ParsedDecimalNumber left, final ParsedDecimalNumber right) {
        final ParsedDecimalNumber leftAbs = normalize(absoluteValue(left));
        final ParsedDecimalNumber rightAbs = normalize(absoluteValue(right));

        final int maxScale = Math.max(leftAbs.scale(), rightAbs.scale());
        final int leftVirtualLength = leftAbs.digits().length() + (maxScale - leftAbs.scale());
        final int rightVirtualLength = rightAbs.digits().length() + (maxScale - rightAbs.scale());

        if (leftVirtualLength != rightVirtualLength) {
            return Integer.compare(leftVirtualLength, rightVirtualLength);
        }

        for (int i = 0; i < leftVirtualLength; i++) {
            final char leftDigit = digitAtWithVirtualZeros(leftAbs, i);
            final char rightDigit = digitAtWithVirtualZeros(rightAbs, i);
            if (leftDigit != rightDigit) {
                return Character.compare(leftDigit, rightDigit);
            }
        }

        return 0;
    }

    /**
     * Reads a digit from a virtual representation that appends trailing zeros to match a target scale.
     *
     * @param normalizedValue normalized value
     * @param index           index in the virtual digit stream
     * @return digit character
     */
    private static char digitAtWithVirtualZeros(final ParsedDecimalNumber normalizedValue, final int index) {
        if (index < normalizedValue.digits().length()) {
            return normalizedValue.digits().charAt(index);
        }

        return '0';
    }

    /**
     * Converts a parsed number to a finite {@code double} approximation if possible.
     *
     * <p>This uses the first up to 17 digits as mantissa and a base-10 exponent estimate.
     * Returns {@code null} if the result would not be finite.</p>
     *
     * @param parsedDecimalNumber parsed number
     * @return finite double approximation or {@code null}
     */
    private static Double tryConvertToFiniteDouble(final ParsedDecimalNumber parsedDecimalNumber) {
        final ParsedDecimalNumber normalized = normalize(parsedDecimalNumber);
        if (isZero(normalized)) {
            return 0.0;
        }

        final String digits = normalized.digits();
        final int take = Math.min(17, digits.length());

        long leading = 0L;
        for (int i = 0; i < take; i++) {
            leading = leading * 10L + (digits.charAt(i) - '0');
        }

        final double mantissa = leading / Math.pow(10.0, take - 1);
        final int exponentBase10 = (digits.length() - normalized.scale()) - 1;

        final double magnitude = mantissa * Math.pow(10.0, exponentBase10);
        if (!Double.isFinite(magnitude)) {
            return null;
        }

        final double signed = normalized.sign() < 0 ? -magnitude : magnitude;
        return Double.isFinite(signed) ? signed : null;
    }

    /**
     * Converts a {@code double} to a plain decimal string without exponent notation.
     *
     * @param value double value
     * @return plain decimal string using '.' as separator
     */
    private static String toPlainDecimalStringFromDouble(final double value) {
        if (value == 0.0d) {
            return "0";
        }

        final String raw = Double.toString(value);
        if (raw.indexOf('e') < 0 && raw.indexOf('E') < 0) {
            return raw;
        }

        return expandScientificNotationToPlain(raw);
    }

    /**
     * Expands a scientific notation string (e.g. "1.2E-3") into plain decimal form.
     *
     * @param scientificString string in scientific notation
     * @return plain decimal string
     */
    private static String expandScientificNotationToPlain(final String scientificString) {
        String normalized = scientificString.trim();
        int sign = +1;

        if (normalized.startsWith("-")) {
            sign = -1;
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        }

        final int exponentIndex = Math.max(normalized.indexOf('e'), normalized.indexOf('E'));
        final String mantissaString = normalized.substring(0, exponentIndex);
        final int exponentValue = Integer.parseInt(normalized.substring(exponentIndex + 1));

        final int dotIndex = mantissaString.indexOf('.');
        final String mantissaDigits = dotIndex < 0 ? mantissaString : mantissaString.substring(0, dotIndex) + mantissaString.substring(dotIndex + 1);

        final int fractionalDigits = dotIndex < 0 ? 0 : (mantissaString.length() - dotIndex - 1);
        final int shift = exponentValue - fractionalDigits;

        final String plainUnsigned;
        if (shift >= 0) {
            plainUnsigned = mantissaDigits + "0".repeat(shift);
        } else {
            final int split = mantissaDigits.length() + shift;
            if (split > 0) {
                plainUnsigned = mantissaDigits.substring(0, split) + "." + mantissaDigits.substring(split);
            } else {
                plainUnsigned = "0." + "0".repeat(-split) + mantissaDigits;
            }
        }

        return sign < 0 ? "-" + plainUnsigned : plainUnsigned;
    }

    /**
     * Removes leading zeros from an unsigned digit string.
     *
     * @param unsignedDigits digit string
     * @return digit string without leading zeros (except "0")
     */
    private static String stripLeadingZeros(final String unsignedDigits) {
        int index = 0;
        while (index < unsignedDigits.length() - 1 && unsignedDigits.charAt(index) == '0') {
            index++;
        }

        return unsignedDigits.substring(index);
    }

    /**
     * Appends zeros to the right side of an unsigned digit string.
     *
     * @param unsignedDigits digits
     * @param zeroCount      number of zeros to append (>= 0)
     * @return digits with appended zeros
     */
    private static String appendZerosRight(final String unsignedDigits, final int zeroCount) {
        if (zeroCount <= 0 || unsignedDigits.equals("0")) {
            return unsignedDigits;
        }
        return unsignedDigits + "0".repeat(zeroCount);
    }

}
