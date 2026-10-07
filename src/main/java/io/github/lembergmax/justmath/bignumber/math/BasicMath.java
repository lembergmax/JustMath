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

package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.internal.LocaleSeparators;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArgumentException;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArithmeticException;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Provides core arithmetic and selected transcendental operations for {@link BigNumber}: addition, subtraction,
 * multiplication, division, remainder, modulo, integer powers, factorial, {@code exp} and fractional powers.
 *
 * <h2>How the operations are computed</h2>
 * Addition, subtraction, multiplication, division, remainder, modulo, integer powers and the factorial run on
 * {@link BigDecimal} and {@link BigInteger}. A result keeps its value as a {@link BigDecimal} and writes its
 * digits down only when somebody asks for them, so a chain of calculations does not convert to text between the
 * steps. Division rounds once with the {@link MathContext} of the caller, that is with {@link BigDecimal#divide(BigDecimal, MathContext)}.
 *
 * <p>{@code exp} and the fractional power compute with the internal tuple representation
 * {@code value = sign * digits * 10^{-scale}} on digit strings, with guard digits, and round once.</p>
 *
 * <h2>Locale handling</h2>
 * The operands are read through {@link BigNumber#toBigDecimal()}, which does not depend on a locale. The locale
 * only decides how the result formats itself.
 *
 * <h2>Special semantics</h2>
 * <ul>
 *   <li>{@code power}: a non-negative integer exponent gives the exact result, a negative one the reciprocal rounded once.</li>
 *   <li>A negative base with a non-integer exponent gives a real-only approximation, see below.</li>
 * </ul>
 *
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
     * Maximum requested precision (in significant digits) for which a {@code double}-based fast path may be
     * used to produce a <em>final</em> result (for {@code exp} and non-integer {@code power}).
     * <p>
     * A {@code double} carries at most ~15–17 significant decimal digits, so returning a {@code double}
     * result when the caller requested higher precision would silently deliver fewer correct digits than
     * promised by the {@link MathContext}. Above this threshold the exact (Taylor / exp·ln) path runs.
     */
    private static final int DOUBLE_FAST_PATH_MAX_PRECISION = 15;

    /**
     * Upper bound on the argument of {@link #factorial}. {@code n!} has on the order of
     * {@code n·log10(n)} digits, so without a cap an expression like {@code 1000000!} would build a
     * multi-million-digit number — minutes of CPU and large heap — turning a one-line untrusted input
     * into a denial-of-service. {@code 100000!} (~456 500 digits) is comfortably within reach; larger
     * arguments are rejected with a typed "too large" error.
     */
    private static final int MAX_FACTORIAL_ARGUMENT = 100_000;

    /**
     * Upper bound on the number of decimal digits a single integer {@link #power} result may have.
     * {@code base^exponent} has about {@code exponent·log10(base)} digits, so {@code 9^9999999999} or
     * {@code 2^100000000000} would each produce billions of digits and exhaust memory. The projected size
     * is estimated cheaply before the (expensive) computation and rejected if it exceeds this bound.
     */
    private static final long MAX_POWER_RESULT_DIGITS = 1_000_000L;

    /**
     * Extra working precision used internally for exp Taylor series to reduce rounding noise while staying fast.
     */
    private static final int EXP_WORKING_GUARD_DIGITS = 8;

    /**
     * Hard upper bound for exp Taylor iterations as a safety net.
     */
    private static final int EXP_MAX_ITERATIONS_HARD_LIMIT = 2000;

    /**
     * Smallest reduction power used for an exp argument above 10.
     */
    private static final int EXP_MIN_REDUCTION_POWER_FOR_LARGE_ARGUMENT = 4;

    /**
     * Extra working digits for a power with a fractional exponent, on top of the integer digits of the product
     * {@code exponent * ln(|base|)}, whose relative error the final {@code exp} multiplies by its own size.
     */
    private static final int POWER_WORKING_GUARD_DIGITS = 10;

    /**
     * Largest absolute value of {@code exponent * ln(|base|)} for which the double fast path of a fractional power is
     * used. {@code Math.exp} multiplies the relative error of its argument by the argument, so a larger product would
     * cost the digits that the fast path promises.
     */
    private static final double POWER_FAST_DOUBLE_MAX_ABS_PRODUCT = 50.0;

    /**
     * {@code log10(e)}: the number of decimal digits that {@code exp(x)} has per unit of {@code x}.
     */
    private static final double LOG10_OF_E = 0.4342944819032518;

    /**
     * Number of powers of ten that {@link #powerOfTen(int)} keeps.
     */
    private static final int CACHED_POWERS_OF_TEN = 320;

    /**
     * The powers of ten below {@link #CACHED_POWERS_OF_TEN}. The array is filled when the class is initialized and
     * never changed afterwards.
     */
    private static final BigInteger[] POWERS_OF_TEN = buildPowersOfTen();

    /**
     * {@code log10(2)}: the number of decimal digits that a bit has.
     */
    private static final double LOG10_OF_2 = 0.3010299956639812;

    /**
     * Digits that the integer quotient of {@link #divideRounded} has beyond the precision, so that the digits after
     * the kept ones are known even when the digit counts of the operands are estimated.
     */
    private static final int QUOTIENT_GUARD_DIGITS = 4;

    /**
     * {@code log2(10)}: the number of bits that a decimal digit has.
     */
    private static final double LOG2_OF_10 = 3.321928094887362;

    /**
     * The number zero as a string
     */
    private static final String ZERO_AS_STRING = "0";

    /**
     * Non-instantiable utility class.
     */
    private BasicMath() {
    }

    /**
     * Adds two {@link BigNumber} values. The result is exact.
     *
     * @param augend the first operand; must not be {@code null}
     * @param addend the second operand; must not be {@code null}
     * @param locale the locale of the result; must not be {@code null}
     * @return {@code augend + addend} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber add(@NonNull final BigNumber augend, @NonNull final BigNumber addend, @NonNull final Locale locale) {
        return toBigNumber(augend.toBigDecimal().add(addend.toBigDecimal()), locale);
    }

    /**
     * Subtracts {@code subtrahend} from {@code minuend}. The result is exact.
     *
     * @param minuend    the value to subtract from; must not be {@code null}
     * @param subtrahend the value to subtract; must not be {@code null}
     * @param locale     the locale of the result; must not be {@code null}
     * @return {@code minuend - subtrahend} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber subtract(@NonNull final BigNumber minuend, @NonNull final BigNumber subtrahend, @NonNull final Locale locale) {
        return toBigNumber(minuend.toBigDecimal().subtract(subtrahend.toBigDecimal()), locale);
    }

    /**
     * Multiplies two {@link BigNumber} values. The result is exact.
     *
     * @param multiplicand the left operand; must not be {@code null}
     * @param multiplier   the right operand; must not be {@code null}
     * @param locale       the locale of the result; must not be {@code null}
     * @return {@code multiplicand * multiplier} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber multiply(@NonNull final BigNumber multiplicand, @NonNull final BigNumber multiplier, @NonNull final Locale locale) {
        return toBigNumber(multiplicand.toBigDecimal().multiply(multiplier.toBigDecimal()), locale);
    }

    /**
     * Divides {@code dividend} by {@code divisor} and rounds once according to {@link MathContext}.
     *
     * <p>The result is rounded to {@code mathContext.getPrecision()} significant digits with the rounding mode
     * of {@code mathContext}. A quotient far below 1, such as {@code 1 / 10^200}, keeps its magnitude: the
     * precision counts significant digits, not decimal places. With {@link RoundingMode#UNNECESSARY} a quotient
     * that needs rounding is an error.</p>
     *
     * @param dividend    the dividend; must not be {@code null}
     * @param divisor     the divisor; must not be {@code null} and not zero
     * @param mathContext precision and rounding mode; must not be {@code null} and precision must be > 0
     * @param locale      the locale of the result; must not be {@code null}
     * @return {@code dividend / divisor} rounded to {@code mathContext}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws MathArithmeticException  if {@code divisor} is zero, or the rounding mode is {@code UNNECESSARY} and the
     *                                  quotient is not exact
     * @throws MathArgumentException    if the precision of {@code mathContext} is not positive
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber divide(@NonNull final BigNumber dividend, @NonNull final BigNumber divisor, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        final BigDecimal divisorValue = divisor.toBigDecimal();
        if (divisorValue.signum() == 0) {
            throw divisionByZero();
        }

        final BigDecimal dividendValue = dividend.toBigDecimal();
        if (dividendValue.signum() == 0) {
            return toBigNumber(BigDecimal.ZERO, locale, mathContext);
        }

        return toBigNumber(divideRounded(dividendValue, divisorValue, mathContext), locale, mathContext);
    }

    /**
     * Computes {@code dividend mod divisor}. The result is never negative: for a negative dividend and a remainder
     * that is not zero it is {@code |divisor| - remainder}.
     *
     * @param dividend the dividend; must not be {@code null}
     * @param divisor  the divisor; must not be {@code null} and not zero
     * @param locale   the locale of the result; must not be {@code null}
     * @return {@code dividend mod divisor} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws MathArgumentException    if {@code divisor} is zero
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber modulo(@NonNull final BigNumber dividend, @NonNull final BigNumber divisor, @NonNull final Locale locale) {
        final BigDecimal divisorValue = divisor.toBigDecimal();
        if (divisorValue.signum() == 0) {
            throw new MathArgumentException(CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO, "Cannot perform modulo operation with divisor zero.");
        }

        final BigDecimal dividendValue = dividend.toBigDecimal();
        final BigDecimal remainder = absoluteRemainder(dividendValue, divisorValue);
        if (dividendValue.signum() < 0 && remainder.signum() != 0) {
            return toBigNumber(divisorValue.abs().subtract(remainder), locale);
        }

        return toBigNumber(remainder, locale);
    }

    /**
     * Computes {@code base ^ exponent}.
     *
     * <p>Behavior:
     * <ul>
     *   <li>Integer exponent {@code >= 0}: {@link BigDecimal#pow(int)}, which squares repeatedly. The result is exact and is <em>not</em>
     *       rounded to {@code mathContext}; a result of more than {@code 1,000,000} digits is rejected with
     *       {@code MATH_OVERFLOW}.</li>
     *   <li>Integer exponent {@code < 0}: the reciprocal of the exact positive power, rounded once to
     *       {@code mathContext}.</li>
     *   <li>Non-integer exponent: rounded to {@code mathContext}, accurate to within one unit in the last place.
     *       It prefers a fast finite-double approximation if the precision is at most 15 digits and the result is
     *       safely representable, otherwise it uses {@code exp(exponent * ln(|base|))} with guard digits.</li>
     *   <li>Negative base + non-integer exponent: returns a real-only approximation by applying the base sign.</li>
     * </ul>
     *
     * @param base        base value; must not be {@code null}
     * @param exponent    exponent value; must not be {@code null}
     * @param mathContext precision and rounding mode; must not be {@code null} and precision must be > 0
     * @param locale      the locale of the result; must not be {@code null}
     * @return {@code base ^ exponent} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws ArithmeticException      if {@code base == 0} and {@code exponent < 0}
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber power(@NonNull final BigNumber base, @NonNull final BigNumber exponent, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        final ParsedDecimalNumber baseParts = normalize(parseFromBigNumber(base));
        final ParsedDecimalNumber exponentParts = normalize(parseFromBigNumber(exponent));

        final ParsedDecimalNumber specialCaseResult = tryHandlePowerSpecialCases(baseParts, exponentParts, mathContext);
        if (specialCaseResult != null) {
            return toBigNumber(specialCaseResult, locale, mathContext);
        }

        if (isInteger(exponentParts)) {
            return toBigNumber(powerInteger(base.toBigDecimal(), baseParts, exponentParts, mathContext), locale, mathContext);
        }

        if (mathContext.getPrecision() <= DOUBLE_FAST_PATH_MAX_PRECISION) {
            final String fastDoublePowerPlain = tryComputeNonIntegerPowerUsingDouble(baseParts, exponentParts);
            if (fastDoublePowerPlain != null) {
                return new BigNumber(adaptPlainDecimalToLocale(fastDoublePowerPlain, locale), locale, mathContext).trim();
            }
        }

        final ParsedDecimalNumber fallbackPowerResult = powerNonIntegerFallback(baseParts, exponentParts, mathContext);
        return toBigNumber(fallbackPowerResult, locale, mathContext);
    }

    /**
     * Computes the factorial {@code n!} for a non-negative integer {@code n}.
     *
     * <p>The result is exact. It is computed with a product tree: the product of a range is the product of its two
     * halves, so the large factors are multiplied with the fast {@link BigInteger} algorithms. The argument is limited
     * to {@value #MAX_FACTORIAL_ARGUMENT}.</p>
     *
     * @param argument    input value; must not be {@code null}, must be an integer and must be >= 0
     * @param mathContext the context of the result (the factorial itself is exact); must not be {@code null}
     * @param locale      the locale of the result; must not be {@code null}
     * @return {@code argument!} as a new {@link BigNumber}
     * @throws NullPointerException    if any argument is {@code null}
     * @throws MathArgumentException   if {@code argument} is negative or not an integer
     * @throws MathArithmeticException if {@code argument} is above {@value #MAX_FACTORIAL_ARGUMENT}
     */
    public static BigNumber factorial(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        final ParsedDecimalNumber argumentParts = normalize(parseFromBigNumber(argument));
        validateFactorialInput(argumentParts);

        return toBigNumber(new BigDecimal(factorialOf(Integer.parseInt(argumentParts.digits()))), locale, mathContext);
    }

    /**
     * Computes the exponential function {@code e^x}.
     *
     * <p>Fast path:
     * if {@code x} can be safely represented as a finite {@code double} and {@code |x| <= 50},
     * this method uses {@link Math#exp(double)} and converts the result to a plain decimal string
     * (no exponent notation). This is the cheap path for a small argument at a low precision.</p>
     *
     * <p>Fallback path:
     * uses an exp implementation on the internal digit representation with:</p>
     * <ul>
     *   <li>power-of-two reduction {@code e^x = (e^{x/2^k})^{2^k}}</li>
     *   <li>Taylor series for the reduced exponent</li>
     *   <li>fast division by small integers for series term updates (critical for speed)</li>
     * </ul>
     *
     * @param argument    exponent argument {@code x}; must not be {@code null}
     * @param mathContext precision and rounding mode; must not be {@code null} and precision must be > 0
     * @param locale      the locale of the result; must not be {@code null}
     * @return {@code e^x} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if the input is not a plain decimal number
     */
    public static BigNumber exp(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        final ParsedDecimalNumber exponentParts = normalize(parseFromBigNumber(argument));

        if (mathContext.getPrecision() <= DOUBLE_FAST_PATH_MAX_PRECISION) {
            final String fastExpPlain = tryComputeExpUsingDouble(exponentParts);
            if (fastExpPlain != null) {
                return new BigNumber(adaptPlainDecimalToLocale(fastExpPlain, locale), locale, mathContext).trim();
            }
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
        final char localeDecimalSeparator = LocaleSeparators.forLocale(locale).decimalSeparator();
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
    /**
     * Reads the digits of a {@link BigNumber} into the internal representation. The digits are locale independent,
     * so nothing is parsed.
     *
     * @param bigNumber the source number; must not be {@code null}
     * @return the parsed parts (not necessarily normalized yet); never {@code null}
     */
    private static ParsedDecimalNumber parseFromBigNumber(final BigNumber bigNumber) {
        final String integerDigits = bigNumber.getValueBeforeDecimalPoint();
        final String fractionDigits = bigNumber.getValueAfterDecimalPoint().equals(ZERO_AS_STRING) ? "" : bigNumber.getValueAfterDecimalPoint();
        final String combinedDigits = stripLeadingZeros((integerDigits.isEmpty() ? ZERO_AS_STRING : integerDigits) + fractionDigits);
        if (combinedDigits.equals(ZERO_AS_STRING)) {
            return zeroParts();
        }

        return new ParsedDecimalNumber(bigNumber.isNegative() ? -1 : 1, combinedDigits, fractionDigits.length());
    }

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
        final LocaleSeparators localeSeparators = LocaleSeparators.forLocale(locale);
        final char groupingSeparatorCharacter = localeSeparators.groupingSeparator();
        final char localeDecimalSeparatorCharacter = localeSeparators.decimalSeparator();

        int sign = 1;
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
     * Divides two parsed numbers and rounds the result once to the requested {@link MathContext}.
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
            throw divisionByZero();
        }
        if (isZero(dividendNormalized)) {
            return zeroParts();
        }

        requirePositivePrecision(mathContext);

        return toParts(divideRounded(toDecimal(dividendNormalized), toDecimal(divisorNormalized), mathContext));
    }

    /**
     * Divides two values and rounds once with the {@link MathContext}.
     *
     * <p>The unscaled value of the dividend is multiplied by a power of ten so that the integer quotient has at least
     * two digits more than the precision. A remainder that is not zero is kept as one more, non-zero digit, so that
     * {@link BigDecimal#round(MathContext)} sees whether the quotient is above a tie or above the kept digits. The
     * result is the same as {@link BigDecimal#divide(BigDecimal, MathContext)} gives; that method strips the zeros of
     * an exact quotient one division at a time, which is slow for two small numbers at a high precision.</p>
     *
     * @param dividend    the dividend
     * @param divisor     the divisor, not zero
     * @param mathContext the precision and the rounding mode
     * @return the rounded quotient
     * @throws MathArithmeticException if the rounding mode is {@code UNNECESSARY} and the quotient is not exact
     */
    private static BigDecimal divideRounded(final BigDecimal dividend, final BigDecimal divisor, final MathContext mathContext) {
        final BigInteger dividendUnits = dividend.unscaledValue().abs();
        final BigInteger divisorUnits = divisor.unscaledValue().abs();

        final int digitDifference = approximateDigitCount(dividendUnits) - approximateDigitCount(divisorUnits);
        final int shift = Math.max(0, mathContext.getPrecision() + QUOTIENT_GUARD_DIGITS - digitDifference);

        final BigInteger[] quotientAndRemainder = dividendUnits.multiply(powerOfTen(shift)).divideAndRemainder(divisorUnits);
        BigInteger quotient = quotientAndRemainder[0];
        long scale = (long) dividend.scale() - divisor.scale() + shift;
        if (quotientAndRemainder[1].signum() != 0) {
            quotient = quotient.multiply(BigInteger.TEN).add(BigInteger.ONE);
            scale++;
        }

        final BigInteger signedQuotient = dividend.signum() * divisor.signum() < 0 ? quotient.negate() : quotient;
        try {
            return new BigDecimal(signedQuotient, Math.toIntExact(scale)).round(mathContext);
        } catch (final ArithmeticException roundingNecessary) {
            if (mathContext.getRoundingMode() != RoundingMode.UNNECESSARY) {
                throw roundingNecessary;
            }
            throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "Rounding necessary (RoundingMode.UNNECESSARY)");
        }
    }

    /**
     * Returns {@code 10^exponent}. The powers below {@value #CACHED_POWERS_OF_TEN} are computed once, because every
     * division needs one and computing it costs about as much as the division of two small numbers.
     *
     * @param exponent the exponent, not negative
     * @return {@code 10^exponent}
     */
    private static BigInteger powerOfTen(final int exponent) {
        return exponent < CACHED_POWERS_OF_TEN ? POWERS_OF_TEN[exponent] : BigInteger.TEN.pow(exponent);
    }

    /**
     * Computes the powers of ten for {@link #POWERS_OF_TEN}.
     *
     * @return the powers {@code 10^0} to {@code 10^(CACHED_POWERS_OF_TEN - 1)}
     */
    private static BigInteger[] buildPowersOfTen() {
        final BigInteger[] powers = new BigInteger[CACHED_POWERS_OF_TEN];
        powers[0] = BigInteger.ONE;
        for (int exponent = 1; exponent < CACHED_POWERS_OF_TEN; exponent++) {
            powers[exponent] = powers[exponent - 1].multiply(BigInteger.TEN);
        }
        return powers;
    }

    /**
     * Estimates the number of decimal digits of a positive integer from its bit length. The estimate is exact or one
     * too small or too large.
     *
     * @param units the integer; must be positive
     * @return the estimated digit count
     */
    private static int approximateDigitCount(final BigInteger units) {
        return (int) ((units.bitLength() - 1) * LOG10_OF_2) + 1;
    }

    /**
     * Computes {@code |dividend| mod |divisor|} exactly: both values are scaled to a common scale and their
     * unscaled digits are divided as integers.
     *
     * @param dividend the dividend
     * @param divisor  the divisor, not zero
     * @return the remainder, never negative
     */
    private static BigDecimal absoluteRemainder(final BigDecimal dividend, final BigDecimal divisor) {
        final int commonScale = Math.max(dividend.scale(), divisor.scale());
        final BigInteger dividendUnits = dividend.abs().setScale(commonScale).unscaledValue();
        final BigInteger divisorUnits = divisor.abs().setScale(commonScale).unscaledValue();
        return new BigDecimal(dividendUnits.remainder(divisorUnits), commonScale);
    }

    /**
     * Converts a parsed number into a value.
     *
     * @param parts the number; must not be {@code null}
     * @return the value
     */
    private static BigDecimal toDecimal(final ParsedDecimalNumber parts) {
        final BigDecimal magnitude = new BigDecimal(new BigInteger(parts.digits()), parts.scale());
        return parts.sign() < 0 ? magnitude.negate() : magnitude;
    }

    /**
     * Converts a value into a normalized parsed number: no trailing zeros after the decimal point, scale at least 0.
     *
     * @param value the value; must not be {@code null}
     * @return the normalized number
     */
    private static ParsedDecimalNumber toParts(final BigDecimal value) {
        if (value.signum() == 0) {
            return zeroParts();
        }

        final BigDecimal stripped = value.stripTrailingZeros();
        final BigDecimal withNonNegativeScale = stripped.scale() < 0 ? stripped.setScale(0) : stripped;
        return new ParsedDecimalNumber(withNonNegativeScale.signum(), withNonNegativeScale.unscaledValue().abs().toString(), withNonNegativeScale.scale());
    }

    /**
     * Converts a value into a {@link BigNumber} without trailing zeros after the decimal point.
     *
     * @param value  the value; must not be {@code null}
     * @param locale the locale of the result; must not be {@code null}
     * @return a new {@link BigNumber}
     */
    private static BigNumber toBigNumber(final BigDecimal value, final Locale locale) {
        return new BigNumber(value, locale).trim();
    }

    /**
     * Converts a value into a {@link BigNumber} without trailing zeros after the decimal point and with a
     * {@link MathContext}.
     *
     * @param value       the value; must not be {@code null}
     * @param locale      the locale of the result; must not be {@code null}
     * @param mathContext the math context of the result; already validated
     * @return a new {@link BigNumber}
     */
    private static BigNumber toBigNumber(final BigDecimal value, final Locale locale, final MathContext mathContext) {
        final BigNumber result = toBigNumber(value, locale);
        result.setMathContext(mathContext);
        return result;
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
            throw new MathArgumentException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "MathContext precision must be > 0");
        }
        return precision;
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
        final RoundingDecision roundingDecision = computeRoundingDecision(normalizedValue, precision, mathContext.getRoundingMode());

        if (roundingDecision.noRoundingNeeded()) {
            return normalizedValue;
        }

        final StringBuilder keptDigits = new StringBuilder(roundingDecision.keptDigits());

        if (roundingDecision.incrementRequired()) {
            incrementUnsignedDecimalDigits(keptDigits);
        }

        final int removedIntegerDigitCount = removedIntegerDigitCount(normalizedValue.scale(), roundingDecision.removedDigitCount());
        keptDigits.append("0".repeat(removedIntegerDigitCount));
        final int adjustedScale = adjustScaleAfterTruncation(normalizedValue.scale(), roundingDecision.removedDigitCount());

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
     * @param roundingMode    the caller-supplied rounding mode that decides the half-way / directed behaviour
     * @return rounding decision
     */
    private static RoundingDecision computeRoundingDecision(final ParsedDecimalNumber normalizedValue, final int precision, final RoundingMode roundingMode) {
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
        final boolean incrementRequired = shouldIncrementAccordingToRoundingMode(roundingMode, normalizedValue.sign(), lastKeptDigit, roundingDigit, anyFollowingNonZeroDigit);

        final String keptDigits = digits.substring(0, cutIndexExclusive);
        final int removedCount = digits.length() - cutIndexExclusive;

        return new RoundingDecision(false, keptDigits, removedCount, incrementRequired);
    }

    /**
     * Adjusts the decimal scale after truncating unscaled digits.
     *
     * <p>If digits are removed from the end of the unscaled representation, scale is reduced if possible.
     * If more digits are removed than the current scale, the "extra removal" corresponds to removing integer digits.
     * The scale is then 0, and the caller restores the magnitude with {@link #removedIntegerDigitCount(int, int)}
     * zeros.</p>
     *
     * @param originalScale     original scale
     * @param removedDigitCount number of removed unscaled digits
     * @return adjusted scale
     */
    private static int adjustScaleAfterTruncation(final int originalScale, final int removedDigitCount) {
        if (removedDigitCount <= originalScale) {
            return originalScale - removedDigitCount;
        }
        return 0;
    }

    /**
     * Counts the integer digits that rounding removed, which must come back as trailing zeros so that the
     * rounded value keeps its magnitude.
     *
     * @param originalScale     original scale
     * @param removedDigitCount number of removed unscaled digits
     * @return the number of removed digits that lie left of the decimal point; 0 if only fractional digits were removed
     */
    private static int removedIntegerDigitCount(final int originalScale, final int removedDigitCount) {
        return Math.max(0, removedDigitCount - originalScale);
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
                    throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "Rounding necessary (RoundingMode.UNNECESSARY)");
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
     * Multiplies two unsigned digit strings with {@link BigInteger}.
     *
     * @param leftUnsignedDigits  left digits
     * @param rightUnsignedDigits right digits
     * @return unsigned product digits, without leading zeros
     */
    private static String multiplyUnsigned(final String leftUnsignedDigits, final String rightUnsignedDigits) {
        return new BigInteger(leftUnsignedDigits).multiply(new BigInteger(rightUnsignedDigits)).toString();
    }

    /**
     * Computes the truncating remainder (Java {@code %} semantics) of {@code dividend} divided by {@code divisor}.
     *
     * <p><b>Important:</b> This is intentionally different from {@link #modulo(BigNumber, BigNumber, Locale)}.</p>
     * <ul>
     *   <li>{@code remainder}: sign follows the dividend (like Java {@code %}).</li>
     *   <li>{@code modulo}: returns a non-negative result for negative dividends.</li>
     * </ul>
     *
     * <p>The remainder is exact: both operands are scaled to a common scale and divided as integers.</p>
     *
     * @param dividend the dividend; must not be {@code null}
     * @param divisor  the divisor; must not be {@code null} and not zero
     * @param locale   the locale of the result; must not be {@code null}
     * @return {@code dividend % divisor} as a new {@link BigNumber}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws MathArgumentException    if {@code divisor} is zero
     * @throws IllegalArgumentException if an operand is not a plain decimal number
     */
    public static BigNumber remainder(@NonNull final BigNumber dividend, @NonNull final BigNumber divisor, @NonNull final Locale locale) {
        final BigDecimal divisorValue = divisor.toBigDecimal();
        if (divisorValue.signum() == 0) {
            throw new MathArgumentException(CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO, "Cannot perform remainder operation with divisor zero.");
        }

        final BigDecimal dividendValue = dividend.toBigDecimal();
        final BigDecimal remainder = absoluteRemainder(dividendValue, divisorValue);
        if (dividendValue.signum() < 0 && remainder.signum() != 0) {
            return toBigNumber(remainder.negate(), locale);
        }

        return toBigNumber(remainder, locale);
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
                throw divisionByZero();
            }
            return divideParsed(oneParts(), baseParts, mathContext);
        }

        if (isZero(baseParts) && exponentParts.sign() < 0) {
            throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "Cannot compute 0^negative (log undefined)");
        }
        if (isZero(baseParts) && exponentParts.sign() > 0) {
            return zeroParts();
        }

        return null;
    }

    /**
     * Computes an integer power: the exact power for a non-negative exponent, the reciprocal of the exact power
     * rounded once for a negative one.
     *
     * @param base          the base as a value
     * @param baseParts     the base, normalized and not zero
     * @param exponentParts integer exponent (scale must be 0), not zero
     * @param mathContext   rounding context (used for negative exponents)
     * @return {@code base^exponent}
     * @throws MathArithmeticException if the result would have more than {@link #MAX_POWER_RESULT_DIGITS} digits
     */
    private static BigDecimal powerInteger(final BigDecimal base, final ParsedDecimalNumber baseParts, final ParsedDecimalNumber exponentParts, final MathContext mathContext) {
        final ParsedDecimalNumber baseAbsolute = absoluteValue(baseParts);
        final String exponentAbsoluteDigits = exponentParts.digits();

        rejectPowerIfResultTooLarge(baseAbsolute, exponentAbsoluteDigits);

        final BigDecimal magnitude = isOne(baseAbsolute) ? BigDecimal.ONE : base.abs().pow(Integer.parseInt(exponentAbsoluteDigits));
        final boolean resultIsNegative = baseParts.sign() < 0 && isOddUnsigned(exponentAbsoluteDigits);
        final BigDecimal power = resultIsNegative ? magnitude.negate() : magnitude;
        if (exponentParts.sign() > 0) {
            return power;
        }

        return divideRounded(BigDecimal.ONE, power, mathContext);
    }

    /**
     * Rejects an integer power whose plain-decimal result would be impractically large, before the
     * (expensive) squaring loop runs. The size of {@code |base|^exponent} grows like
     * {@code exponent · digitsPerFactor}, where {@code digitsPerFactor} bounds both the growth of the
     * significant digits ({@code log10} of the base significand) and the growth of the fractional part
     * ({@code scale} extra places per factor for a base with {@code scale} fractional digits). A base of
     * magnitude {@code 1} (the result is exactly {@code 1}), a zero base and a zero exponent are never
     * rejected.
     *
     * @param baseAbsolute           the absolute, normalized base
     * @param exponentAbsoluteDigits unsigned integer-exponent digit string
     * @throws ArithmeticException if the projected result size exceeds {@link #MAX_POWER_RESULT_DIGITS}
     */
    private static void rejectPowerIfResultTooLarge(final ParsedDecimalNumber baseAbsolute, final String exponentAbsoluteDigits) {
        final String strippedExponent = stripLeadingZeros(exponentAbsoluteDigits);
        if (strippedExponent.equals("0") || isZero(baseAbsolute) || isOne(baseAbsolute)) {
            return;
        }

        final double digitsPerFactor = Math.max(baseAbsolute.scale(), log10OfSignificand(baseAbsolute));
        if (digitsPerFactor <= 0.0) {
            return;
        }

        final boolean exponentExceedsLong = strippedExponent.length() > 18;
        if (exponentExceedsLong || Long.parseLong(strippedExponent) > (long) (MAX_POWER_RESULT_DIGITS / digitsPerFactor)) {
            throw new MathArithmeticException(CalculatorErrorCode.MATH_OVERFLOW, "Power result is too large (would exceed " + MAX_POWER_RESULT_DIGITS + " digits)");
        }
    }

    /**
     * Base-10 logarithm of the base's significand — its digit string read as an integer, ignoring the
     * scale. Only the leading (up to 15) digits are used, which is ample precision for a size estimate.
     *
     * @param baseAbsolute the absolute, normalized, non-zero base
     * @return {@code log10} of the significand; never negative
     */
    private static double log10OfSignificand(final ParsedDecimalNumber baseAbsolute) {
        final String digits = stripLeadingZeros(baseAbsolute.digits());
        final String head = digits.length() <= 15 ? digits : digits.substring(0, 15);
        final double significand = Double.parseDouble(head.charAt(0) + "." + head.substring(1));
        return Math.log10(significand) + (digits.length() - 1);
    }

    /**
     * Fallback implementation for non-integer powers using {@code exp(exponent * ln(|base|))}.
     *
     * <p>The logarithm, the product and the exponential are computed with guard digits and the result is rounded
     * once. The product {@code exponent * ln(|base|)} is the argument of {@code exp}, which multiplies its relative
     * error by its size, so the guard grows with the integer digits of the product.</p>
     *
     * @param baseParts     base value
     * @param exponentParts exponent value (non-integer)
     * @param mathContext   rounding context
     * @return power result (real-only)
     */
    private static ParsedDecimalNumber powerNonIntegerFallback(final ParsedDecimalNumber baseParts, final ParsedDecimalNumber exponentParts, final MathContext mathContext) {
        final ParsedDecimalNumber absoluteBase = absoluteValue(baseParts);
        final MathContext workingContext = createPowerWorkingMathContext(absoluteBase, exponentParts, mathContext);

        final ParsedDecimalNumber lnAbsBase = lnParsed(absoluteBase, workingContext);
        final ParsedDecimalNumber exponentTimesLn = normalize(multiplyParsed(exponentParts, lnAbsBase));
        final ParsedDecimalNumber absoluteResult = normalize(roundToMathContext(expParsed(exponentTimesLn, workingContext), mathContext));

        return baseParts.sign() < 0 ? negate(absoluteResult) : absoluteResult;
    }

    /**
     * Creates the working {@link MathContext} of a power with a fractional exponent.
     *
     * @param absoluteBase  absolute value of the base, greater than zero
     * @param exponentParts the exponent
     * @param mathContext   requested context
     * @return a context with the precision of the request plus guard digits and the rounding mode of the caller
     */
    private static MathContext createPowerWorkingMathContext(final ParsedDecimalNumber absoluteBase, final ParsedDecimalNumber exponentParts, final MathContext mathContext) {
        final double estimatedLogarithm = Math.abs(estimateNaturalLogarithmAsDouble(normalize(absoluteBase)));
        final int logarithmIntegerDigits = estimatedLogarithm < 1.0 ? 0 : (int) Math.floor(Math.log10(estimatedLogarithm)) + 1;
        final int productIntegerDigits = integerDigitCount(normalize(exponentParts)) + logarithmIntegerDigits;
        final int workingPrecision = mathContext.getPrecision() + POWER_WORKING_GUARD_DIGITS + productIntegerDigits;
        return new MathContext(workingPrecision, mathContext.getRoundingMode());
    }

    /**
     * Attempts to compute a non-integer power using fast double math:
     * <pre>
     *   a^b ≈ exp(b * ln(|a|)), then apply sign(a) if a is negative
     * </pre>
     *
     * <p>The fast path is skipped if {@code |b * ln(|a|)|} exceeds {@link #POWER_FAST_DOUBLE_MAX_ABS_PRODUCT}, because
     * the relative error of the double result grows with that product.</p>
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

        final double product = exponent * Math.log(absoluteBase);
        if (Math.abs(product) > POWER_FAST_DOUBLE_MAX_ABS_PRODUCT) {
            return null;
        }

        final double absoluteResult = Math.exp(product);
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
     * Validates that the factorial input is a non-negative integer.
     *
     * @param argumentParts parsed argument
     */
    private static void validateFactorialInput(final ParsedDecimalNumber argumentParts) {
        if (!isInteger(argumentParts)) {
            throw new MathArgumentException(CalculatorErrorCode.MATH_FACTORIAL_NON_INTEGER, "Factorial is only defined for integers.");
        }
        if (argumentParts.sign() < 0) {
            throw new MathArgumentException(CalculatorErrorCode.MATH_FACTORIAL_NEGATIVE, "Factorial is only defined for non-negative integers.");
        }
        final Integer argumentAsInt = tryParseUnsignedInt(argumentParts.digits());
        if (argumentAsInt == null || argumentAsInt > MAX_FACTORIAL_ARGUMENT) {
            throw new MathArithmeticException(CalculatorErrorCode.MATH_OVERFLOW, "Factorial argument is too large (maximum " + MAX_FACTORIAL_ARGUMENT + ")");
        }
    }

    /**
     * Computes {@code n!} for a validated argument with a product tree: the product of the range is the product
     * of its two halves, so the large factors are multiplied with the fast {@link BigInteger} algorithms.
     *
     * @param n integer {@code 0 <= n <= MAX_FACTORIAL_ARGUMENT}
     * @return {@code n!}
     */
    private static BigInteger factorialOf(final int n) {
        return n < 2 ? BigInteger.ONE : productOfRange(2, n);
    }

    /**
     * Multiplies the integers of the range {@code [startInclusive, endInclusive]} by splitting it in halves.
     *
     * @param startInclusive range start
     * @param endInclusive   range end
     * @return the product; {@code 1} for an empty range
     */
    private static BigInteger productOfRange(final int startInclusive, final int endInclusive) {
        if (startInclusive > endInclusive) {
            return BigInteger.ONE;
        }
        if (startInclusive == endInclusive) {
            return BigInteger.valueOf(startInclusive);
        }
        if (endInclusive - startInclusive == 1) {
            return BigInteger.valueOf((long) startInclusive * endInclusive);
        }

        final int middle = (startInclusive + endInclusive) >>> 1;
        return productOfRange(startInclusive, middle).multiply(productOfRange(middle + 1, endInclusive));
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
     *   <li>Reject an argument whose result would exceed {@link #MAX_POWER_RESULT_DIGITS} digits.</li>
     *   <li>Work with guard digits that grow with the integer digits of x, because every squaring of the range
     *       reduction doubles the relative error.</li>
     *   <li>If x is negative: compute 1/exp(|x|) from the unrounded value, so that the result is rounded once.</li>
     *   <li>Choose a reduction power k so that {@code |x / 2^k| <= 1} and compute x' = x / 2^k using fast int
     *       division.</li>
     *   <li>Compute exp(x') by Taylor series with fast division term/n.</li>
     *   <li>Undo reduction by repeated squaring (k times).</li>
     * </ol>
     *
     * @param exponentParts exponent x
     * @param mathContext   precision and rounding mode
     * @return exp(x) as parsed number, rounded once to {@code mathContext}
     * @throws MathArithmeticException if the result would exceed the size limit, or if the precision is too large for
     *                                 the Taylor series to converge within its iteration limit
     */
    private static ParsedDecimalNumber expParsed(final ParsedDecimalNumber exponentParts, final MathContext mathContext) {
        final ParsedDecimalNumber normalizedExponent = normalize(exponentParts);

        if (isZero(normalizedExponent)) {
            return oneParts();
        }

        requireExpResultWithinSizeLimit(normalizedExponent);

        final MathContext workingContext = createWorkingMathContext(mathContext, normalizedExponent);
        if (normalizedExponent.sign() < 0) {
            final ParsedDecimalNumber positiveExp = expOfPositiveParsed(negate(normalizedExponent), workingContext);
            return normalize(divideParsed(oneParts(), positiveExp, mathContext));
        }

        return normalize(roundToMathContext(expOfPositiveParsed(normalizedExponent, workingContext), mathContext));
    }

    /**
     * Computes {@code exp(x)} for {@code x > 0} at the working precision, without the final rounding.
     *
     * @param positiveExponent exponent x > 0
     * @param workingContext   working precision, including guard digits
     * @return exp(x), rounded to the working precision
     */
    private static ParsedDecimalNumber expOfPositiveParsed(final ParsedDecimalNumber positiveExponent, final MathContext workingContext) {
        final int reductionPower = chooseReductionPowerForExp(positiveExponent);

        final ParsedDecimalNumber reducedExponent = reduceExponentByPowerOfTwo(positiveExponent, reductionPower, workingContext);
        final ParsedDecimalNumber reducedExpValue = expTaylorSeries(reducedExponent, workingContext);

        return undoReductionBySquaring(reducedExpValue, reductionPower, workingContext);
    }

    /**
     * Rejects an exponent whose {@code exp} has more than {@link #MAX_POWER_RESULT_DIGITS} digits before or after the
     * decimal point. A small argument such as {@code 10000000} would otherwise produce a result with millions of
     * digits, which is the same denial of service that the limit on integer powers prevents.
     *
     * @param normalizedExponent normalized exponent x, not zero
     * @throws MathArithmeticException with {@code MATH_OVERFLOW} if {@code |x| * log10(e)} exceeds the limit
     */
    private static void requireExpResultWithinSizeLimit(final ParsedDecimalNumber normalizedExponent) {
        final Double asDouble = tryConvertToFiniteDouble(normalizedExponent);
        final boolean exceedsLimit = asDouble == null || Math.abs(asDouble) * LOG10_OF_E > MAX_POWER_RESULT_DIGITS;
        if (exceedsLimit) {
            throw new MathArithmeticException(CalculatorErrorCode.MATH_OVERFLOW,
                    "exp result is too large (would exceed " + MAX_POWER_RESULT_DIGITS + " digits)");
        }
    }

    /**
     * Counts the digits of a value that lie left of the decimal point.
     *
     * @param value parsed value
     * @return the number of integer digits; 0 if the value is below 1
     */
    private static int integerDigitCount(final ParsedDecimalNumber value) {
        return Math.max(0, value.digits().length() - value.scale());
    }

    /**
     * Creates a working {@link MathContext} with increased precision for intermediate exp computations.
     *
     * <p>The guard digits cover the rounding noise of the Taylor series and the squarings of the range reduction.
     * The squarings multiply the relative error by about {@code |x|}, so the guard grows with the integer digits
     * of the exponent.</p>
     *
     * @param requestedContext requested context
     * @param exponent         exponent x of the exp computation
     * @return working context with guard digits and the rounding mode of the caller
     */
    private static MathContext createWorkingMathContext(final MathContext requestedContext, final ParsedDecimalNumber exponent) {
        final int guardDigits = EXP_WORKING_GUARD_DIGITS + integerDigitCount(exponent);
        final int workingPrecision = Math.max(10, requestedContext.getPrecision() + guardDigits);
        return new MathContext(workingPrecision, requestedContext.getRoundingMode());
    }

    /**
     * Chooses a reduction power k for exp reduction {@code x -> x / 2^k}.
     *
     * <p>Small arguments use a small k to avoid overhead. For {@code |x| > 10} the power grows with
     * {@code log2(|x|)}, so that the reduced exponent stays below 1 and the Taylor series converges within its
     * iteration limit whatever the argument.</p>
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

            return Math.max(EXP_MIN_REDUCTION_POWER_FOR_LARGE_ARGUMENT, (int) Math.ceil(Math.log(absolute) / Math.log(2.0)) + 1);
        }

        return Math.max(EXP_MIN_REDUCTION_POWER_FOR_LARGE_ARGUMENT, (int) Math.ceil(integerDigitCount(nonNegativeNormalizedExponent) * LOG2_OF_10) + 1);
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
     * <p>Division by {@code n} is performed using the fast int division routine to maximize speed. The series stops
     * when a term is below the working precision, so that the truncation error stays below the guard digits.</p>
     *
     * @param reducedExponent reduced exponent x, with {@code |x| <= 1}
     * @param workingContext  working rounding context for intermediate steps
     * @return exp(x) approximation
     * @throws MathArithmeticException with {@code MATH_OVERFLOW} if the series does not converge within
     *                                 {@link #EXP_MAX_ITERATIONS_HARD_LIMIT} terms, which happens for precisions of
     *                                 several thousand digits
     */
    private static ParsedDecimalNumber expTaylorSeries(final ParsedDecimalNumber reducedExponent, final MathContext workingContext) {
        ParsedDecimalNumber sum = oneParts();
        ParsedDecimalNumber term = oneParts();

        final ParsedDecimalNumber epsilon = normalize(new ParsedDecimalNumber(+1, "1", workingContext.getPrecision()));
        final int maxIterations = Math.clamp((long) workingContext.getPrecision() * 6, 200, EXP_MAX_ITERATIONS_HARD_LIMIT);

        for (int n = 1; n <= maxIterations; n++) {
            term = normalize(multiplyParsed(term, reducedExponent));
            term = divideByPositiveIntWithRounding(term, n, workingContext);

            sum = normalize(addParsed(sum, term));

            final ParsedDecimalNumber absTerm = term.sign() < 0 ? negate(term) : term;
            if (compareAbsolute(absTerm, epsilon) < 0) {
                return sum;
            }
        }

        throw new MathArithmeticException(CalculatorErrorCode.MATH_OVERFLOW,
                "exp does not converge for a precision of " + workingContext.getPrecision() + " digits");
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
            throw new MathArithmeticException(CalculatorErrorCode.MATH_LOG_NON_POSITIVE, "ln(x) is only defined for x > 0");
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
        double guess = estimateNaturalLogarithmAsDouble(normalizedPositiveX);
        if (!Double.isFinite(guess)) {
            guess = 0.0;
        }

        return normalize(parseToParts(toPlainDecimalStringFromDouble(guess), Locale.US));
    }

    /**
     * Estimates {@code ln(x)} for {@code x > 0} with a double, from the leading digits and the decimal exponent, so
     * that it works for values beyond the range of a double.
     *
     * @param normalizedPositiveX normalized positive x
     * @return an approximation of ln(x) with about 15 correct digits
     */
    private static double estimateNaturalLogarithmAsDouble(final ParsedDecimalNumber normalizedPositiveX) {
        final int exponentBase10 = estimateBase10Exponent(normalizedPositiveX);
        final double mantissa = estimateMantissaAsDouble(normalizedPositiveX);

        return Math.log(mantissa) + exponentBase10 * Math.log(10.0);
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
        int sign = 1;

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

    private static MathArithmeticException divisionByZero() {
        return new MathArithmeticException(CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO, "Division by zero");
    }

}
