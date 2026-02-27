/*
 * Copyright (c) 2025-2026 Max Lemberg
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
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.*;

/**
 * Provides inverse hyperbolic trigonometric functions for {@link BigNumber} without using
 * {@code BigDecimal}, {@code BigInteger}, or external math libraries.
 *
 * <h2>Definitions</h2>
 * <pre>
 * asinh(x) = ln(x + sqrt(x^2 + 1))
 * acosh(x) = ln(x + sqrt(x^2 - 1))           domain: x >= 1
 * atanh(x) = 0.5 * ln((1 + x) / (1 - x))     domain: |x| < 1
 * acoth(x) = 0.5 * ln((x + 1) / (x - 1))     domain: |x| > 1
 * </pre>
 *
 * <h2>Robust sign handling</h2>
 * The functions {@code asinh}, {@code atanh}, and {@code acoth} are odd:
 * <pre>
 * f(-x) = -f(x)
 * </pre>
 * This implementation computes the magnitude using {@code |x|} and applies the sign at the end.
 * The sign is derived from the input string representation to avoid any dependency on potentially
 * unfinished sign logic in other components during refactors.
 *
 * <h2>Performance</h2>
 * The logarithm is computed via Newton iteration using {@link BasicMath#exp(BigNumber, MathContext, Locale)}
 * and fast double initial guesses when safe.
 */
public final class InverseHyperbolicTrigonometricMath {

    /**
     * Additional digits used internally to reduce cancellation issues in intermediate operations.
     */
    private static final int INTERNAL_GUARD_DIGITS = 100;

    /**
     * Maximum number of Newton iterations for ln(x). This prevents pathological non-termination.
     */
    private static final int LN_MAX_ITERATIONS = 2500;

    /**
     * Conservative magnitude bound for using {@code double} fast paths.
     */
    private static final double DOUBLE_SAFE_MAGNITUDE = 10e300;

    /**
     * Computes the inverse hyperbolic sine {@code asinh(x)} for any real {@code x}.
     *
     * <p>Definition:
     * <pre>
     * asinh(x) = ln(x + sqrt(x^2 + 1))
     * </pre>
     *
     * <p>Odd symmetry:
     * <pre>
     * asinh(x) = sign(x) * asinh(|x|)
     * </pre>
     *
     * @param argument    the input value {@code x}; must not be {@code null}
     * @param mathContext the precision and rounding configuration; must not be {@code null} and must have positive precision
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return {@code asinh(argument)} computed with the requested precision
     */
    public static BigNumber asinh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (argument.isEqualTo(ZERO)) {
            return BigNumbers.ZERO;
        }

        final boolean argumentIsNegative = isNegativeLiteral(argument);
        final BigNumber absoluteArgument = absoluteValue(argument, locale);

        final MathContext internalMathContext = createInternalMathContext(mathContext);
        final BigNumber magnitude = asinhMagnitude(absoluteArgument, internalMathContext, locale);

        return applyOddSign(magnitude, argumentIsNegative, locale, mathContext);
    }

    /**
     * Computes the inverse hyperbolic cosine {@code acosh(x)} for {@code x >= 1}.
     *
     * <p>Definition:
     * <pre>
     * acosh(x) = ln(x + sqrt(x^2 - 1))
     * </pre>
     *
     * <p>Domain restriction:
     * Real-valued results require {@code x >= 1}.</p>
     *
     * @param argument    the input value {@code x}; must not be {@code null} and must be >= 1
     * @param mathContext the precision and rounding configuration; must not be {@code null} and must have positive precision
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return {@code acosh(argument)} computed with the requested precision
     * @throws IllegalArgumentException if {@code argument < 1}
     */
    public static BigNumber acosh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);
        ensureGreaterOrEqualToOne(argument);

        if (argument.isEqualTo(ONE)) {
            return BigNumbers.ZERO;
        }

        final MathContext internalMathContext = createInternalMathContext(mathContext);
        final BigNumber result = acoshValue(argument, internalMathContext, locale);

        return rewrapWithRequestedMathContext(result, locale, mathContext);
    }

    /**
     * Computes the inverse hyperbolic tangent {@code atanh(x)} for {@code |x| < 1}.
     *
     * <p>Definition:
     * <pre>
     * atanh(x) = 0.5 * ln((1 + x) / (1 - x))
     * </pre>
     *
     * <p>Domain restriction:
     * Real-valued results require {@code |x| < 1}.</p>
     *
     * <p>Odd symmetry:
     * <pre>
     * atanh(x) = sign(x) * atanh(|x|)
     * </pre>
     *
     * @param argument    the input value {@code x}; must not be {@code null} and must satisfy {@code |x| < 1}
     * @param mathContext the precision and rounding configuration; must not be {@code null} and must have positive precision
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return {@code atanh(argument)} computed with the requested precision
     * @throws IllegalArgumentException if {@code |argument| >= 1}
     */
    public static BigNumber atanh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);
        ensureAbsoluteLessThanOne(argument);

        if (argument.isEqualTo(ZERO)) {
            return BigNumbers.ZERO;
        }

        final boolean argumentIsNegative = isNegativeLiteral(argument);
        final BigNumber absoluteArgument = absoluteValue(argument, locale);

        final MathContext internalMathContext = createInternalMathContext(mathContext);
        final BigNumber magnitude = atanhMagnitude(absoluteArgument, internalMathContext, locale);

        return applyOddSign(magnitude, argumentIsNegative, locale, mathContext);
    }

    /**
     * Computes the inverse hyperbolic cotangent {@code acoth(x)} for {@code |x| > 1}.
     *
     * <p>Definition:
     * <pre>
     * acoth(x) = 0.5 * ln((x + 1) / (x - 1))
     * </pre>
     *
     * <p>Domain restriction:
     * Real-valued results require {@code |x| > 1}.</p>
     *
     * <p>Odd symmetry:
     * <pre>
     * acoth(x) = sign(x) * acoth(|x|)
     * </pre>
     *
     * <p>Robustness note:
     * For {@code |x| > 1}, the ratio {@code (|x|+1)/(|x|-1)} is strictly > 1, so the logarithm is strictly positive.
     * The final sign therefore depends only on the original sign of {@code x}.</p>
     *
     * @param argument    the input value {@code x}; must not be {@code null} and must satisfy {@code |x| > 1}
     * @param mathContext the precision and rounding configuration; must not be {@code null} and must have positive precision
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return {@code acoth(argument)} computed with the requested precision
     * @throws IllegalArgumentException if {@code |argument| <= 1}
     */
    public static BigNumber acoth(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);
        ensureAbsoluteGreaterThanOne(argument);

        final boolean argumentIsNegative = isNegativeLiteral(argument);
        final BigNumber absoluteArgument = absoluteValue(argument, locale);

        final MathContext internalMathContext = createInternalMathContext(mathContext);
        final BigNumber magnitude = acothMagnitude(absoluteArgument, internalMathContext, locale);

        return applyOddSign(magnitude, argumentIsNegative, locale, mathContext);
    }

    /**
     * Computes {@code asinh(x)} for {@code x >= 0}.
     *
     * @param nonNegativeArgument a value {@code x} with {@code x >= 0}; must not be {@code null}
     * @param mathContext         the internal precision and rounding configuration; must not be {@code null}
     * @param locale              the locale used for parsing/formatting; must not be {@code null}
     * @return {@code asinh(nonNegativeArgument)} which is non-negative
     */
    private static BigNumber asinhMagnitude(final BigNumber nonNegativeArgument, final MathContext mathContext, final Locale locale) {
        final BigNumber argumentSquared = square(nonNegativeArgument, locale);
        final BigNumber insideSquareRoot = BasicMath.add(argumentSquared, BigNumbers.ONE, locale);
        final BigNumber squareRoot = sqrtNonNegative(insideSquareRoot, mathContext, locale);

        final BigNumber logarithmArgument = BasicMath.add(nonNegativeArgument, squareRoot, locale);
        return lnPositive(logarithmArgument, mathContext, locale);
    }

    /**
     * Computes {@code acosh(x)} for {@code x >= 1}.
     *
     * @param argument    a value {@code x} with {@code x >= 1}; must not be {@code null}
     * @param mathContext internal precision and rounding configuration; must not be {@code null}
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return {@code acosh(argument)} which is non-negative
     */
    private static BigNumber acoshValue(final BigNumber argument, final MathContext mathContext, final Locale locale) {
        final BigNumber argumentSquared = square(argument, locale);
        final BigNumber insideSquareRoot = BasicMath.subtract(argumentSquared, BigNumbers.ONE, locale);
        final BigNumber squareRoot = sqrtNonNegative(insideSquareRoot, mathContext, locale);

        final BigNumber logarithmArgument = BasicMath.add(argument, squareRoot, locale);
        return lnPositive(logarithmArgument, mathContext, locale);
    }

    /**
     * Computes {@code atanh(x)} for {@code 0 <= x < 1}.
     *
     * <p>For {@code 0 <= x < 1} the ratio {@code (1+x)/(1-x)} is strictly > 1, so {@code ln(ratio)} is positive.</p>
     *
     * @param nonNegativeArgument a value {@code x} with {@code 0 <= x < 1}; must not be {@code null}
     * @param mathContext         internal precision and rounding configuration; must not be {@code null}
     * @param locale              the locale used for parsing/formatting; must not be {@code null}
     * @return {@code atanh(nonNegativeArgument)} which is non-negative
     */
    private static BigNumber atanhMagnitude(final BigNumber nonNegativeArgument, final MathContext mathContext, final Locale locale) {
        final BigNumber numerator = BasicMath.add(BigNumbers.ONE, nonNegativeArgument, locale);
        final BigNumber denominator = BasicMath.subtract(BigNumbers.ONE, nonNegativeArgument, locale);

        final BigNumber ratio = BasicMath.divide(numerator, denominator, mathContext, locale);
        final BigNumber logarithm = lnPositive(ratio, mathContext, locale);

        return divideByTwo(logarithm, mathContext, locale);
    }

    /**
     * Computes {@code acoth(x)} for {@code x > 1}.
     *
     * <p>For {@code x > 1} the ratio {@code (x+1)/(x-1)} is strictly > 1, so {@code ln(ratio)} is positive.</p>
     *
     * @param greaterThanOneArgument a value {@code x} with {@code x > 1}; must not be {@code null}
     * @param mathContext            internal precision and rounding configuration; must not be {@code null}
     * @param locale                 the locale used for parsing/formatting; must not be {@code null}
     * @return {@code acoth(greaterThanOneArgument)} which is positive
     */
    private static BigNumber acothMagnitude(final BigNumber greaterThanOneArgument, final MathContext mathContext, final Locale locale) {
        final BigNumber numerator = BasicMath.add(greaterThanOneArgument, BigNumbers.ONE, locale);
        final BigNumber denominator = BasicMath.subtract(greaterThanOneArgument, BigNumbers.ONE, locale);

        final BigNumber ratio = BasicMath.divide(numerator, denominator, mathContext, locale);
        final BigNumber logarithm = lnPositive(ratio, mathContext, locale);

        return divideByTwo(logarithm, mathContext, locale);
    }

    /**
     * Applies the sign for an odd function to a non-negative magnitude result.
     *
     * <p>This method applies the sign via string prefixing to avoid relying on any arithmetic negation
     * that might be under refactor elsewhere.</p>
     *
     * @param positiveMagnitude    magnitude computed for {@code |x|}; must not be {@code null}
     * @param shouldBeNegative     whether the final value should be negative
     * @param locale               the locale used for parsing/formatting; must not be {@code null}
     * @param requestedMathContext the caller requested MathContext; must not be {@code null}
     * @return signed result wrapped with {@code requestedMathContext}
     */
    private static BigNumber applyOddSign(final BigNumber positiveMagnitude, final boolean shouldBeNegative, final Locale locale, final MathContext requestedMathContext) {
        if (!shouldBeNegative || positiveMagnitude.isEqualTo(ZERO)) {
            return rewrapWithRequestedMathContext(positiveMagnitude, locale, requestedMathContext);
        }

        final String magnitudeString = positiveMagnitude.toString().trim();
        final String signedString = magnitudeString.startsWith("-") ? magnitudeString : "-" + magnitudeString;

        return new BigNumber(signedString, locale, requestedMathContext).trim();
    }

    /**
     * Determines whether the given number is negative by checking its string representation.
     *
     * @param value value to check; must not be {@code null}
     * @return {@code true} if the string form starts with '-'
     */
    private static boolean isNegativeLiteral(final BigNumber value) {
        return value.toString().trim().startsWith("-");
    }

    /**
     * Returns the absolute value of a number without depending on other math operations.
     *
     * @param value  input value; must not be {@code null}
     * @param locale locale used for constructing the returned number; must not be {@code null}
     * @return absolute value of {@code value}
     */
    private static BigNumber absoluteValue(final BigNumber value, final Locale locale) {
        final String raw = value.toString().trim();
        if (!raw.startsWith("-")) {
            return value;
        }
        final String withoutMinus = raw.substring(1);
        return new BigNumber(withoutMinus, locale).trim();
    }

    /**
     * Ensures {@code argument >= 1}.
     *
     * @param argument input argument; must not be {@code null}
     * @throws IllegalArgumentException if {@code argument < 1}
     */
    private static void ensureGreaterOrEqualToOne(final BigNumber argument) {
        if (argument.isLessThan(ONE)) {
            throw new IllegalArgumentException("argument must be greater than or equal to 1");
        }
    }

    /**
     * Ensures {@code |argument| < 1}.
     *
     * @param argument input argument; must not be {@code null}
     * @throws IllegalArgumentException if {@code |argument| >= 1}
     */
    private static void ensureAbsoluteLessThanOne(final BigNumber argument) {
        if (argument.isGreaterThanOrEqualTo(ONE) || argument.isLessThanOrEqualTo(NEGATIVE_ONE)) {
            throw new IllegalArgumentException("argument must satisfy |argument| < 1");
        }
    }

    /**
     * Ensures {@code |argument| > 1}.
     *
     * @param argument input argument; must not be {@code null}
     * @throws IllegalArgumentException if {@code |argument| <= 1}
     */
    private static void ensureAbsoluteGreaterThanOne(final BigNumber argument) {
        if (argument.isGreaterThanOrEqualTo(NEGATIVE_ONE) && argument.isLessThanOrEqualTo(ONE)) {
            throw new IllegalArgumentException("argument must satisfy |argument| > 1");
        }
    }

    /**
     * Creates an internal MathContext with additional guard digits to improve stability.
     *
     * @param requestedMathContext caller provided MathContext; must not be {@code null}
     * @return internal MathContext with increased precision
     */
    private static MathContext createInternalMathContext(final MathContext requestedMathContext) {
        final int internalPrecision = Math.max(10, requestedMathContext.getPrecision() + INTERNAL_GUARD_DIGITS);
        return new MathContext(internalPrecision, requestedMathContext.getRoundingMode());
    }

    /**
     * Re-wraps an intermediate result using the requested MathContext.
     *
     * @param intermediateResult   intermediate computed value; must not be {@code null}
     * @param locale               locale; must not be {@code null}
     * @param requestedMathContext requested MathContext; must not be {@code null}
     * @return BigNumber carrying the requested MathContext
     */
    private static BigNumber rewrapWithRequestedMathContext(final BigNumber intermediateResult, final Locale locale, final MathContext requestedMathContext) {
        return new BigNumber(intermediateResult.toString(), locale, requestedMathContext).trim();
    }

    /**
     * Computes x^2.
     *
     * @param value  input value; must not be {@code null}
     * @param locale locale; must not be {@code null}
     * @return value * value
     */
    private static BigNumber square(final BigNumber value, final Locale locale) {
        return BasicMath.multiply(value, value, locale);
    }

    /**
     * Divides a value by 2 using {@link BasicMath#divide(BigNumber, BigNumber, MathContext, Locale)}.
     *
     * @param value       dividend; must not be {@code null}
     * @param mathContext precision and rounding; must not be {@code null}
     * @param locale      locale; must not be {@code null}
     * @return value / 2
     */
    private static BigNumber divideByTwo(final BigNumber value, final MathContext mathContext, final Locale locale) {
        return BasicMath.divide(value, TWO, mathContext, locale);
    }

    /**
     * Computes sqrt(x) for x >= 0 using Newton iteration:
     * <pre>
     * y_{n+1} = (y_n + x / y_n) / 2
     * </pre>
     *
     * @param nonNegativeValue input x; must not be {@code null} and must be >= 0
     * @param mathContext      precision and rounding; must not be {@code null}
     * @param locale           locale; must not be {@code null}
     * @return sqrt(x)
     */
    private static BigNumber sqrtNonNegative(final BigNumber nonNegativeValue, final MathContext mathContext, final Locale locale) {
        if (nonNegativeValue.isNegative()) {
            throw new IllegalArgumentException("sqrt is only defined for non-negative values in real arithmetic");
        }
        if (nonNegativeValue.isEqualTo(ZERO)) {
            return BigNumbers.ZERO;
        }

        BigNumber currentEstimate = initialSqrtGuess(nonNegativeValue, locale);
        final BigNumber epsilon = epsilon(mathContext.getPrecision(), locale);

        for (int iterationIndex = 0; iterationIndex < LN_MAX_ITERATIONS; iterationIndex++) {
            final BigNumber valueOverEstimate = BasicMath.divide(nonNegativeValue, currentEstimate, mathContext, locale);
            final BigNumber nextEstimate = divideByTwo(BasicMath.add(currentEstimate, valueOverEstimate, locale), mathContext, locale);

            final BigNumber delta = BasicMath.subtract(nextEstimate, currentEstimate, locale).abs();
            currentEstimate = nextEstimate;

            if (delta.isLessThan(epsilon)) {
                break;
            }
        }

        return new BigNumber(currentEstimate.toString(), locale, mathContext).trim();
    }

    /**
     * Provides an initial guess for sqrt(x).
     *
     * <p>Fast path:
     * Uses {@link Math#sqrt(double)} when the input can be safely represented as finite double.</p>
     *
     * <p>Fallback:
     * Uses a base-10 exponent estimate to build a magnitude-only guess.</p>
     *
     * @param positiveValue input x; must be > 0
     * @param locale        locale; must not be {@code null}
     * @return initial guess for sqrt(x)
     */
    private static BigNumber initialSqrtGuess(final BigNumber positiveValue, final Locale locale) {
        final Double asDouble = tryParseFiniteDouble(positiveValue.toString(), locale);
        if (asDouble != null && asDouble > 0.0 && asDouble < DOUBLE_SAFE_MAGNITUDE) {
            final double sqrtValue = Math.sqrt(asDouble);
            if (Double.isFinite(sqrtValue) && sqrtValue > 0.0) {
                return new BigNumber(toPlainDecimalStringFromDouble(sqrtValue), locale).trim();
            }
        }

        final int exponentBase10 = estimateBase10Exponent(positiveValue.toString(), locale);
        return powerOfTen(exponentBase10 / 2, locale);
    }

    /**
     * Computes ln(x) for x > 0.
     *
     * <p>Fast path:
     * Uses {@link Math#log(double)} when x can be parsed as a safe finite double.</p>
     *
     * <p>Fallback:
     * Uses Newton iteration to solve exp(y) = x:
     * <pre>
     * y_{n+1} = y_n + (x / exp(y_n)) - 1
     * </pre>
     *
     * @param positiveValue input x; must be {@code > 0}
     * @param mathContext   precision and rounding; must not be {@code null}
     * @param locale        locale; must not be {@code null}
     * @return ln(x)
     */
    private static BigNumber lnPositive(final BigNumber positiveValue, final MathContext mathContext, final Locale locale) {
        if (positiveValue.isLessThanOrEqualTo(ZERO)) {
            throw new IllegalArgumentException("ln(x) is only defined for x > 0");
        }
        if (positiveValue.isEqualTo(ONE)) {
            return BigNumbers.ZERO;
        }

        final String fastLnPlain = tryLnUsingDouble(positiveValue, locale);
        if (fastLnPlain != null) {
            return new BigNumber(fastLnPlain, locale, mathContext).trim();
        }

        return lnNewton(positiveValue, mathContext, locale);
    }

    /**
     * Computes ln(x) via Newton iteration.
     *
     * @param positiveValue input x; must be > 0
     * @param mathContext   precision and rounding
     * @param locale        locale
     * @return ln(x)
     */
    private static BigNumber lnNewton(final BigNumber positiveValue, final MathContext mathContext, final Locale locale) {
        BigNumber currentEstimate = initialLnGuess(positiveValue, locale);
        final BigNumber epsilon = epsilon(mathContext.getPrecision(), locale);

        for (int iterationIndex = 0; iterationIndex < LN_MAX_ITERATIONS; iterationIndex++) {
            final BigNumber expEstimate = BasicMath.exp(currentEstimate, mathContext, locale);
            final BigNumber ratio = BasicMath.divide(positiveValue, expEstimate, mathContext, locale);
            final BigNumber delta = BasicMath.subtract(ratio, BigNumbers.ONE, locale);

            currentEstimate = BasicMath.add(currentEstimate, delta, locale);

            if (delta.abs().isLessThan(epsilon)) {
                break;
            }
        }

        return new BigNumber(currentEstimate.toString(), locale, mathContext).trim();
    }

    /**
     * Builds an initial guess for ln(x).
     *
     * @param positiveValue input x > 0
     * @param locale        locale
     * @return initial guess for ln(x)
     */
    private static BigNumber initialLnGuess(final BigNumber positiveValue, final Locale locale) {
        final Double asDouble = tryParseFiniteDouble(positiveValue.toString(), locale);
        if (asDouble != null && asDouble > 0.0 && asDouble < DOUBLE_SAFE_MAGNITUDE) {
            final double ln = Math.log(asDouble);
            if (Double.isFinite(ln)) {
                return new BigNumber(toPlainDecimalStringFromDouble(ln), locale).trim();
            }
        }

        final int exponentBase10 = estimateBase10Exponent(positiveValue.toString(), locale);
        final double guess = exponentBase10 * Math.log(10.0);

        return new BigNumber(toPlainDecimalStringFromDouble(guess), locale).trim();
    }

    /**
     * Attempts to compute ln(x) using {@code double} for speed.
     *
     * @param positiveValue input x > 0
     * @param locale        locale
     * @return plain decimal ln(x) string or null if not safe
     */
    private static String tryLnUsingDouble(final BigNumber positiveValue, final Locale locale) {
        final Double valueAsDouble = tryParseFiniteDouble(positiveValue.toString(), locale);
        if (valueAsDouble == null || !(valueAsDouble > 0.0) || !Double.isFinite(valueAsDouble)) {
            return null;
        }
        if (valueAsDouble > DOUBLE_SAFE_MAGNITUDE || valueAsDouble < 1.0 / DOUBLE_SAFE_MAGNITUDE) {
            return null;
        }

        final double ln = Math.log(valueAsDouble);
        return Double.isFinite(ln) ? toPlainDecimalStringFromDouble(ln) : null;
    }

    /**
     * Builds epsilon = 10^{-precision}.
     *
     * @param precision requested precision (>0)
     * @param locale    locale
     * @return epsilon value
     */
    private static BigNumber epsilon(final int precision, final Locale locale) {
        if (precision <= 0) {
            return new BigNumber("0.1", locale).trim();
        }

        final String plain = "0." + "0".repeat(Math.max(0, precision - 1)) + "1";
        return new BigNumber(plain, locale).trim();
    }

    /**
     * Creates 10^exponent as a plain decimal string wrapped as {@link BigNumber}.
     *
     * @param exponent base-10 exponent (may be negative)
     * @param locale   locale
     * @return 10^exponent
     */
    private static BigNumber powerOfTen(final int exponent, final Locale locale) {
        if (exponent == 0) {
            return BigNumbers.ONE;
        }
        if (exponent > 0) {
            return new BigNumber("1" + "0".repeat(exponent), locale).trim();
        }

        final int zeros = -exponent - 1;
        return new BigNumber("0." + "0".repeat(Math.max(0, zeros)) + "1", locale).trim();
    }

    /**
     * Parses a locale-formatted decimal string into a finite {@code double} if possible.
     *
     * @param rawNumberString input string
     * @param locale          locale describing separators
     * @return finite double value or null if parsing fails or value is non-finite
     */
    private static Double tryParseFiniteDouble(final String rawNumberString, final Locale locale) {
        final String sanitized = sanitizeToDotDecimal(rawNumberString, locale);
        if (sanitized == null) {
            return null;
        }
        try {
            final double parsed = Double.parseDouble(sanitized);
            return Double.isFinite(parsed) ? parsed : null;
        } catch (final NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * Sanitizes a locale-formatted decimal string by:
     * <ul>
     *   <li>removing grouping separators</li>
     *   <li>removing spaces</li>
     *   <li>converting the decimal separator to '.'</li>
     * </ul>
     *
     * @param rawNumberString input string
     * @param locale          locale
     * @return sanitized string or null if empty
     */
    private static String sanitizeToDotDecimal(final String rawNumberString, final Locale locale) {
        final String trimmed = rawNumberString == null ? "" : rawNumberString.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
        final char groupingSeparator = symbols.getGroupingSeparator();
        final char decimalSeparator = symbols.getDecimalSeparator();

        String sanitized = trimmed.replace(String.valueOf(groupingSeparator), "");
        sanitized = sanitized.replace(" ", "");
        if (decimalSeparator != '.') {
            sanitized = sanitized.replace(decimalSeparator, '.');
        }

        return sanitized;
    }

    /**
     * Estimates the base-10 exponent for a locale-formatted decimal string.
     *
     * @param rawNumberString input string
     * @param locale          locale
     * @return approximate floor(log10(x)) for positive x
     */
    private static int estimateBase10Exponent(final String rawNumberString, final Locale locale) {
        final String sanitized = sanitizeToDotDecimal(rawNumberString, locale);
        if (sanitized == null) {
            return 0;
        }

        String working = sanitized;
        if (working.startsWith("+") || working.startsWith("-")) {
            working = working.substring(1);
        }

        final int dotIndex = working.indexOf('.');
        final String integerPart = dotIndex < 0 ? working : working.substring(0, dotIndex);
        final String fractionalPart = dotIndex < 0 ? "" : working.substring(dotIndex + 1);

        final String intStripped = stripLeadingZerosToZero(integerPart);
        if (!intStripped.equals("0")) {
            return intStripped.length() - 1;
        }

        int leadingFractionalZeros = 0;
        while (leadingFractionalZeros < fractionalPart.length() && fractionalPart.charAt(leadingFractionalZeros) == '0') {
            leadingFractionalZeros++;
        }

        return -(leadingFractionalZeros + 1);
    }

    /**
     * Removes leading zeros and returns "0" for empty/all-zero strings.
     *
     * @param digits digit string
     * @return stripped digit string
     */
    private static String stripLeadingZerosToZero(final String digits) {
        if (digits == null || digits.isEmpty()) {
            return "0";
        }
        int index = 0;
        while (index < digits.length() - 1 && digits.charAt(index) == '0') {
            index++;
        }

        return digits.substring(index);
    }

    /**
     * Converts a double into a plain decimal string without exponent notation.
     *
     * @param value double value
     * @return plain decimal string using '.'
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
     * Expands scientific notation into plain decimal form.
     *
     * @param scientificString scientific notation string (e.g. "1.2E-3")
     * @return plain decimal string using '.'
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

}
