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
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;

/**
 * Provides high-precision implementations of hyperbolic trigonometric functions for {@link BigNumber}
 * without using {@code BigDecimal}, {@code BigInteger}, or external math libraries.
 *
 * <h2>Definitions</h2>
 * The functions are implemented via their exponential identities:
 * <pre>
 * sinh(x) = (e^x - e^{-x}) / 2
 * cosh(x) = (e^x + e^{-x}) / 2
 * tanh(x) = (e^x - e^{-x}) / (e^x + e^{-x})
 * coth(x) = (e^x + e^{-x}) / (e^x - e^{-x})
 * </pre>
 *
 * <h2>Performance</h2>
 * For maximum speed, this class computes {@code e^x} and {@code e^{-x}} from a single expensive exponential:
 * <ul>
 *   <li>Compute {@code e^{|x|}} once</li>
 *   <li>Compute {@code e^{-|x|}} as {@code 1 / e^{|x|}}</li>
 *   <li>Swap depending on the sign of {@code x}</li>
 * </ul>
 * This reduces the number of exponential evaluations from 2 to 1 per hyperbolic call.
 *
 * <h2>Precision strategy</h2>
 * Intermediate operations may lose a few digits due to cancellation (especially around x≈0). Therefore a small
 * number of guard digits is added to the provided {@link MathContext} for internal computation, while the final
 * result is returned with the caller-provided {@link MathContext}.
 */
public final class HyperbolicTrigonometricMath {

    /**
     * Number of extra precision digits used for intermediate computations.
     * <p>
     * This improves stability for expressions like {@code sinh(0.0001)} where cancellation occurs in
     * {@code e^x - e^{-x}}.
     */
    private static final int INTERNAL_GUARD_DIGITS = 8;

    /**
     * Computes the hyperbolic sine {@code sinh(x)} with the given precision.
     *
     * <p>The implementation uses:
     * <pre>
     * sinh(x) = (e^x - e^{-x}) / 2
     * </pre>
     *
     * <p>Performance note:
     * {@code e^x} and {@code e^{-x}} are derived from a single {@code e^{|x|}} evaluation to reduce runtime.</p>
     *
     * @param argument    the input value {@code x}; must not be {@code null}
     * @param mathContext the precision and rounding configuration; must not be {@code null} and must have positive precision
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return a {@link BigNumber} representing {@code sinh(argument)} computed with the provided precision
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if {@code mathContext} is invalid
     */
    public static BigNumber sinh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (argument.isEqualTo(ZERO)) {
            return ZERO;
        }

        final MathContext internalMathContext = createInternalMathContext(mathContext);
        final ExponentialPair exponentialPair = computeExponentialPair(argument, internalMathContext, locale);

        final BigNumber numerator = BasicMath.subtract(exponentialPair.expX(), exponentialPair.expNegativeX(), locale);
        final BigNumber result = divideByTwo(numerator, internalMathContext, locale);

        return rewrapWithRequestedMathContext(result, locale, mathContext);
    }

    /**
     * Computes the hyperbolic cosine {@code cosh(x)} with the given precision.
     *
     * <p>The implementation uses:
     * <pre>
     * cosh(x) = (e^x + e^{-x}) / 2
     * </pre>
     *
     * <p>Performance note:
     * {@code e^x} and {@code e^{-x}} are derived from a single {@code e^{|x|}} evaluation to reduce runtime.</p>
     *
     * @param argument    the input value {@code x}; must not be {@code null}
     * @param mathContext the precision and rounding configuration; must not be {@code null} and must have positive precision
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return a {@link BigNumber} representing {@code cosh(argument)} computed with the provided precision
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if {@code mathContext} is invalid
     */
    public static BigNumber cosh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (argument.isEqualTo(ZERO)) {
            return BigNumbers.ONE;
        }

        final MathContext internalMathContext = createInternalMathContext(mathContext);
        final ExponentialPair exponentialPair = computeExponentialPair(argument, internalMathContext, locale);

        final BigNumber numerator = BasicMath.add(exponentialPair.expX(), exponentialPair.expNegativeX(), locale);
        final BigNumber result = divideByTwo(numerator, internalMathContext, locale);

        return rewrapWithRequestedMathContext(result, locale, mathContext);
    }

    /**
     * Computes the hyperbolic tangent {@code tanh(x)} with the given precision.
     *
     * <p>The implementation uses:
     * <pre>
     * tanh(x) = (e^x - e^{-x}) / (e^x + e^{-x})
     * </pre>
     *
     * <p>Performance note:
     * {@code e^x} and {@code e^{-x}} are derived from a single {@code e^{|x|}} evaluation to reduce runtime.</p>
     *
     * @param argument    the input value {@code x}; must not be {@code null}
     * @param mathContext the precision and rounding configuration; must not be {@code null} and must have positive precision
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return a {@link BigNumber} representing {@code tanh(argument)} computed with the provided precision
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if {@code mathContext} is invalid
     */
    public static BigNumber tanh(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (argument.isEqualTo(ZERO)) {
            return ZERO;
        }

        final MathContext internalMathContext = createInternalMathContext(mathContext);
        final ExponentialPair exponentialPair = computeExponentialPair(argument, internalMathContext, locale);

        final BigNumber numerator = BasicMath.subtract(exponentialPair.expX(), exponentialPair.expNegativeX(), locale);
        final BigNumber denominator = BasicMath.add(exponentialPair.expX(), exponentialPair.expNegativeX(), locale);

        final BigNumber result = BasicMath.divide(numerator, denominator, internalMathContext, locale);
        return rewrapWithRequestedMathContext(result, locale, mathContext);
    }

    /**
     * Computes the hyperbolic cotangent {@code coth(x)} with the given precision.
     *
     * <p>The implementation uses:
     * <pre>
     * coth(x) = (e^x + e^{-x}) / (e^x - e^{-x})
     * </pre>
     *
     * <p><strong>Domain restriction:</strong> coth(x) is undefined for {@code x = 0}.</p>
     *
     * <p>Performance note:
     * {@code e^x} and {@code e^{-x}} are derived from a single {@code e^{|x|}} evaluation to reduce runtime.</p>
     *
     * @param argument    the input value {@code x}; must not be {@code null} and must not be zero
     * @param mathContext the precision and rounding configuration; must not be {@code null} and must have positive precision
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return a {@link BigNumber} representing {@code coth(argument)} computed with the provided precision
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if {@code argument} is zero or {@code mathContext} is invalid
     */
    public static BigNumber coth(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (argument.isEqualTo(ZERO)) {
            throw new IllegalArgumentException("argument cannot be zero");
        }

        final MathContext internalMathContext = createInternalMathContext(mathContext);
        final ExponentialPair exponentialPair = computeExponentialPair(argument, internalMathContext, locale);

        final BigNumber numerator = BasicMath.add(exponentialPair.expX(), exponentialPair.expNegativeX(), locale);
        final BigNumber denominator = BasicMath.subtract(exponentialPair.expX(), exponentialPair.expNegativeX(), locale);

        final BigNumber result = BasicMath.divide(numerator, denominator, internalMathContext, locale);
        return rewrapWithRequestedMathContext(result, locale, mathContext);
    }

    // =================================================================================================================
    // Internal helpers
    // =================================================================================================================

    /**
     * Holds {@code e^x} and {@code e^{-x}} for a given input {@code x}.
     *
     * @param expX         computed {@code e^x}
     * @param expNegativeX computed {@code e^{-x}}
     */
    private record ExponentialPair(BigNumber expX, BigNumber expNegativeX) {
    }

    /**
     * Creates an internal {@link MathContext} that adds guard digits on top of the requested precision.
     *
     * <p>This reduces the impact of intermediate rounding when subtracting nearly equal numbers
     * such as {@code e^x - e^{-x}} for small |x|.</p>
     *
     * @param requestedMathContext the caller-provided context; must not be {@code null}
     * @return internal context with increased precision
     */
    private static MathContext createInternalMathContext(final MathContext requestedMathContext) {
        final int requestedPrecision = requestedMathContext.getPrecision();
        final int internalPrecision = Math.max(10, requestedPrecision + INTERNAL_GUARD_DIGITS);
        return new MathContext(internalPrecision, requestedMathContext.getRoundingMode());
    }

    /**
     * Computes {@code e^x} and {@code e^{-x}} using only one exponential evaluation:
     * <ol>
     *   <li>Compute {@code a = |x|}</li>
     *   <li>Compute {@code expAbs = e^{a}}</li>
     *   <li>Compute {@code expNegAbs = 1 / expAbs}</li>
     *   <li>Return (expAbs, expNegAbs) if x >= 0, else swapped</li>
     * </ol>
     *
     * @param argument    the input x; must not be {@code null}
     * @param mathContext internal precision context; must not be {@code null}
     * @param locale      locale for parsing/formatting; must not be {@code null}
     * @return exponential pair (e^x, e^{-x})
     */
    private static ExponentialPair computeExponentialPair(final BigNumber argument, final MathContext mathContext, final Locale locale) {
        final BigNumber absoluteArgument = absoluteValue(argument, locale);
        final BigNumber expAbsolute = BasicMath.exp(absoluteArgument, mathContext, locale);

        final BigNumber expNegativeAbsolute = computeReciprocal(expAbsolute, mathContext, locale);

        if (argument.isNegative()) {
            return new ExponentialPair(expNegativeAbsolute, expAbsolute);
        }
        return new ExponentialPair(expAbsolute, expNegativeAbsolute);
    }

    /**
     * Computes the absolute value of a {@link BigNumber} without using {@code BigDecimal}.
     *
     * <p>This implementation uses arithmetic negation via {@link BasicMath} to remain independent from
     * any BigDecimal-based conversions.</p>
     *
     * @param value  input value; must not be {@code null}
     * @param locale locale for parsing/formatting; must not be {@code null}
     * @return |value|
     */
    private static BigNumber absoluteValue(final BigNumber value, final Locale locale) {
        if (!value.isNegative()) {
            return value;
        }
        return BasicMath.subtract(ZERO, value, locale);
    }

    /**
     * Computes the reciprocal {@code 1/value} with the requested rounding behavior.
     *
     * @param value       divisor value; must not be {@code null} and must not be zero
     * @param mathContext precision and rounding; must not be {@code null}
     * @param locale      locale used for parsing/formatting; must not be {@code null}
     * @return 1/value
     * @throws ArithmeticException if {@code value} is zero
     */
    private static BigNumber computeReciprocal(final BigNumber value, final MathContext mathContext, final Locale locale) {
        if (value.isEqualTo(ZERO)) {
            throw new ArithmeticException("Division by zero");
        }
        return BasicMath.divide(BigNumbers.ONE, value, mathContext, locale);
    }

    /**
     * Divides a number by 2 using the general division routine.
     *
     * <p>This method exists to keep public methods short and consistent. The divisor is a constant "2".</p>
     *
     * @param value       dividend; must not be {@code null}
     * @param mathContext precision and rounding; must not be {@code null}
     * @param locale      locale used for parsing/formatting; must not be {@code null}
     * @return value / 2
     */
    private static BigNumber divideByTwo(final BigNumber value, final MathContext mathContext, final Locale locale) {
        return BasicMath.divide(value, BigNumbers.TWO, mathContext, locale);
    }

    /**
     * Re-wraps an intermediate {@link BigNumber} result with the caller-provided {@link MathContext}.
     *
     * <p>This method does not change the numeric value; it only ensures the returned {@link BigNumber}
     * carries the requested context if your {@link BigNumber} type stores/uses it.</p>
     *
     * @param intermediateResult   intermediate computed result; must not be {@code null}
     * @param locale               locale used for parsing/formatting; must not be {@code null}
     * @param requestedMathContext context to attach; must not be {@code null}
     * @return a result BigNumber associated with the requested math context
     */
    private static BigNumber rewrapWithRequestedMathContext(final BigNumber intermediateResult, final Locale locale, final MathContext requestedMathContext) {
        return new BigNumber(intermediateResult.toString(), locale, requestedMathContext).trim();
    }

}
