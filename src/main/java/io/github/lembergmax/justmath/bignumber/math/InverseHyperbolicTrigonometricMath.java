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

package io.github.lembergmax.justmath.bignumber.math;

import static io.github.lembergmax.justmath.bignumber.BigNumbers.NEGATIVE_ONE;
import static io.github.lembergmax.justmath.bignumber.BigNumbers.ONE;
import static io.github.lembergmax.justmath.bignumber.BigNumbers.ZERO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArgumentException;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import lombok.NonNull;

/**
 * Provides inverse hyperbolic trigonometric functions for {@link BigNumber}.
 *
 * <h2>Definitions</h2>
 * <pre>
 * asinh(x) = ln(x + sqrt(x^2 + 1))
 * acosh(x) = ln(x + sqrt(x^2 - 1))           domain: x &gt;= 1
 * atanh(x) = 0.5 * ln((1 + x) / (1 - x))     domain: |x| &lt; 1
 * acoth(x) = 0.5 * ln((x + 1) / (x - 1))     domain: |x| &gt; 1
 * </pre>
 *
 * <h2>Implementation</h2>
 * The values come from {@link BigDecimalMath}. {@code asinh}, {@code atanh} and {@code acoth} are odd functions
 * whose result is tiny for a tiny or a huge argument, and the library resolves such a result only to its absolute
 * precision. {@link MathUtils#computeWithGuardDigits(MathContext, int, java.util.function.Function)} repeats the
 * computation with more digits until the digits of the result that are requested are correct, and rounds once to
 * the caller's {@link MathContext} and rounding mode.
 */
public final class InverseHyperbolicTrigonometricMath {

    /**
     * Extra working digits on top of the requested precision.
     */
    private static final int GUARD_DIGITS = 10;

    /** Non-instantiable utility class. */
    private InverseHyperbolicTrigonometricMath() {
    }

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
            return freshZero(locale);
        }

        return compute(argument, mathContext, locale, BigDecimalMath::asinh);
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
            return freshZero(locale);
        }

        return compute(argument, mathContext, locale, BigDecimalMath::acosh);
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
            return freshZero(locale);
        }

        return compute(argument, mathContext, locale, BigDecimalMath::atanh);
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
     * @param argument    the input value {@code x}; must not be {@code null} and must satisfy {@code |x| > 1}
     * @param mathContext the precision and rounding configuration; must not be {@code null} and must have positive precision
     * @param locale      the locale used for parsing/formatting; must not be {@code null}
     * @return {@code acoth(argument)} computed with the requested precision
     * @throws IllegalArgumentException if {@code |argument| <= 1}
     */
    public static BigNumber acoth(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);
        ensureAbsoluteGreaterThanOne(argument);

        return compute(argument, mathContext, locale, BigDecimalMath::acoth);
    }

    /**
     * Evaluates a {@link BigDecimalMath} function with guard digits and rounds the result once.
     *
     * @param argument    the argument; must not be {@code null}
     * @param mathContext the requested precision and rounding; must not be {@code null}
     * @param locale      the locale of the result; must not be {@code null}
     * @param function    the library function; must not be {@code null}
     * @return the result rounded to {@code mathContext}; never {@code null}
     */
    private static BigNumber compute(final BigNumber argument, final MathContext mathContext, final Locale locale, final LibraryFunction function) {
        final BigDecimal value = argument.toBigDecimal();
        final BigDecimal result = MathUtils.computeWithGuardDigits(mathContext, GUARD_DIGITS, workingContext -> function.apply(value, workingContext));

        return new BigNumber(result.toPlainString(), locale).trim();
    }

    /**
     * A function of {@link BigDecimalMath} that takes one argument and a {@link MathContext}.
     */
    @FunctionalInterface
    private interface LibraryFunction {

        /**
         * Applies the function.
         *
         * @param argument    the argument
         * @param mathContext the working precision
         * @return the function value
         */
        BigDecimal apply(BigDecimal argument, MathContext mathContext);
    }

    /**
     * Ensures {@code argument >= 1}.
     *
     * @param argument input argument; must not be {@code null}
     * @throws IllegalArgumentException if {@code argument < 1}
     */
    private static void ensureGreaterOrEqualToOne(final BigNumber argument) {
        if (argument.isLessThan(ONE)) {
            throw new MathArgumentException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "argument must be greater than or equal to 1");
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
            throw new MathArgumentException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "argument must satisfy |argument| < 1");
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
            throw new MathArgumentException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "argument must satisfy |argument| > 1");
        }
    }

    /**
     * Returns a fresh {@link BigNumber} equal to zero.
     *
     * <p>Always a new instance rather than the shared {@code BigNumbers.ZERO} constant, because the
     * returned value flows back to callers that may mutate it (audit fixes K2/H10).
     *
     * @param locale the locale used for formatting; must not be {@code null}
     * @return a new {@link BigNumber} equal to {@code 0}; never {@code null}
     */
    private static BigNumber freshZero(final Locale locale) {
        return new BigNumber("0", locale);
    }

}
