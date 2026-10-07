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

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArithmeticException;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
import java.util.function.BiFunction;

import static io.github.lembergmax.justmath.bignumber.BigNumbers.ZERO;

/**
 * Utility class providing high‐precision logarithmic functions on {@link BigNumber} values.
 * <p>
 * Wraps the {@link BigDecimalMath} library to compute
 * base‐2 logarithm, base‐10 logarithm, natural logarithm (ln), and
 * logarithm with arbitrary positive base.
 * All methods accept a {@link MathContext} to control precision and rounding,
 * and a {@link Locale} for formatting the resulting {@link BigNumber}.
 */
public final class LogarithmicMath {

    /**
     * Guard digits for the rounding noise of the library logarithm. The adaptive computation adds more digits for
     * an argument near 1, whose logarithm is tiny.
     */
    private static final int LOGARITHM_GUARD_DIGITS = 10;

    /**
     * Guard digits that the two logarithms of {@code logBase} carry, so that their quotient is correct in the
     * requested digits.
     */
    private static final int LOG_BASE_QUOTIENT_GUARD_DIGITS = 15;

    /** Non-instantiable utility class. */
    private LogarithmicMath() {
    }


    /**
     * Computes the base‐2 logarithm of the given argument.
     * <p>
     * Mathematically defined as:
     * <pre>
     * log₂(x) = ln(x) / ln(2)
     * </pre>
     * where ln is the natural logarithm.
     * Domain: x &gt; 0.
     *
     * @param argument
     * 	the positive input value x
     * @param mathContext
     * 	the {@link MathContext} specifying precision and rounding
     * @param locale
     * 	the {@link Locale} used to format the returned {@link BigNumber}
     *
     * @return a {@link BigNumber} representing log₂(argument)
     *
     * @throws ArithmeticException
     * 	if {@code argument <= 0}; the logarithm is undefined for non-positive inputs (an
     * 	{@link ArithmeticException}, consistent with {@code ln} and the JDK convention for
     * 	mathematical-domain errors such as {@code BigDecimal.divide} by zero)
     */
    public static BigNumber log2(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (argument.compareTo(ZERO) <= 0) {
            throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "log2(x) undefined for x <= 0");
        }

        if (argument.isEqualTo(BigNumbers.ONE)) {
            return new BigNumber("0", locale);
        }

        return logarithm(argument, mathContext, locale, BigDecimalMath::log2);
    }

    /**
     * Computes the base‐10 logarithm of the given argument.
     * <p>
     * Mathematically defined as:
     * <pre>
     * log₁₀(x) = ln(x) / ln(10)
     * </pre>
     * Domain: x &gt; 0.
     *
     * @param argument
     * 	the positive input value x
     * @param mathContext
     * 	the {@link MathContext} specifying precision and rounding
     * @param locale
     * 	the {@link Locale} used to format the returned {@link BigNumber}
     *
     * @return a {@link BigNumber} representing log₁₀(argument)
     *
     * @throws ArithmeticException
     * 	if {@code argument <= 0} (logarithm undefined for non-positive inputs)
     */
    public static BigNumber log10(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (argument.compareTo(ZERO) <= 0) {
            throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "log10(x) undefined for x <= 0");
        }

        if (argument.isEqualTo(BigNumbers.ONE)) {
            return new BigNumber("0", locale);
        }

        return logarithm(argument, mathContext, locale, BigDecimalMath::log10);
    }

    /**
     * Computes the natural logarithm (ln) of the given argument.
     * <p>
     * Mathematically defined as the inverse of the exponential function:
     * <pre>
     * ln(x) = the unique y such that eʸ = x
     * </pre>
     * Domain: x &gt; 0.
     *
     * @param argument
     * 	the positive input value x
     * @param mathContext
     * 	the {@link MathContext} specifying precision and rounding
     * @param locale
     * 	the {@link Locale} used to format the returned {@link BigNumber}
     *
     * @return a {@link BigNumber} representing ln(argument)
     *
     * @throws ArithmeticException
     * 	if {@code argument <= 0} (logarithm undefined for non-positive inputs)
     */
    public static BigNumber ln(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (argument.compareTo(ZERO) <= 0) {
            throw new MathArithmeticException(CalculatorErrorCode.MATH_LOG_NON_POSITIVE, "ln(x) undefined for x <= 0");
        }

        if (argument.isEqualTo(BigNumbers.ONE)) {
            return new BigNumber("0", locale);
        }

        return logarithm(argument, mathContext, locale, BigDecimalMath::log);
    }

    /**
     * Computes the logarithm of a number with respect to an arbitrary positive base.
     * <p>
     * Mathematically defined as:
     * <pre>
     * log₍b₎(x) = ln(x) / ln(b)
     * </pre>
     * where ln is the natural logarithm.
     * Domain: x &gt; 0, b &gt; 0 &amp;&amp; b ≠ 1.
     *
     * @param number
     * 	the positive input value x
     * @param base
     * 	the positive base b (not equal to 1)
     * @param mathContext
     * 	the {@link MathContext} specifying precision and rounding
     * @param locale
     * 	the {@link Locale} used to format the returned {@link BigNumber}
     *
     * @return a {@link BigNumber} representing log₍base₎(number)
     *
     * @throws ArithmeticException
     * 	if {@code number ≤ 0}, {@code base ≤ 0}, or {@code base == 1}
     */
    public static BigNumber logBase(@NonNull final BigNumber number, @NonNull final BigNumber base, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (number.compareTo(ZERO) <= 0) {
            throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "logBase(x, b) undefined for x <= 0");
        }
        if (base.compareTo(ZERO) <= 0 || base.isEqualTo(BigNumbers.ONE)) {
            throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "logBase(x, b) undefined for b <= 0 or b == 1");
        }

        if (number.isEqualTo(BigNumbers.ONE)) {
            return new BigNumber("0", locale);
        }

        final MathContext quotientContext = MathUtils.withGuardDigits(mathContext, LOG_BASE_QUOTIENT_GUARD_DIGITS);
        final BigDecimal lnNumber = MathUtils.computeWithGuardDigits(quotientContext, LOGARITHM_GUARD_DIGITS,
                workingContext -> BigDecimalMath.log(number.toBigDecimal(), workingContext));
        final BigDecimal lnBase = MathUtils.computeWithGuardDigits(quotientContext, LOGARITHM_GUARD_DIGITS,
                workingContext -> BigDecimalMath.log(base.toBigDecimal(), workingContext));

        final BigDecimal result = lnNumber.divide(lnBase, quotientContext).round(mathContext);

        return new BigNumber(result.toPlainString(), locale).trim();
    }


    /**
     * Evaluates a library logarithm with guard digits and rounds the result once to the requested precision.
     *
     * @param argument    the positive argument; must not be {@code null}
     * @param mathContext the requested precision and rounding; must not be {@code null}
     * @param locale      the locale of the result; must not be {@code null}
     * @param logarithm   the library function; must not be {@code null}
     * @return the logarithm; never {@code null}
     */
    private static BigNumber logarithm(final BigNumber argument, final MathContext mathContext, final Locale locale, final BiFunction<BigDecimal, MathContext, BigDecimal> logarithm) {
        final BigDecimal value = argument.toBigDecimal();
        final BigDecimal result = MathUtils.computeWithGuardDigits(mathContext, LOGARITHM_GUARD_DIGITS, workingContext -> logarithm.apply(value, workingContext));

        return new BigNumber(result.toPlainString(), locale).trim();
    }
}
