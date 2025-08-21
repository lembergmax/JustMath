/*
 * Copyright (c) 2025 Max Lemberg
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

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.bignumber.math.utils.MathUtils;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
import java.util.Map;

import static com.mlprograms.justmath.bignumber.math.utils.MathUtils.bigDecimalRadiansToDegrees;

/**
 * Provides high-precision implementations of inverse trigonometric functions
 * using {@link BigNumber} for arbitrary precision arithmetic.
 * <p>
 * This class supports calculation of the arcsine, arccosine, arctangent,
 * and arccotangent functions with output in radians or degrees.
 */
public class InverseTrigonometricMath {

    /**
     * Computes the arcsine (inverse sine) of a given BigNumber.
     * <p>
     * The arcsine is defined as the angle θ such that:
     * <pre>
     *   sin(θ) = x, where θ ∈ [-π/2, π/2]
     * </pre>
     * and x ∈ [-1, 1]. This implementation uses the identity:
     * <pre>
     *   arcsin(x) = arctan( x / sqrt(1 - x²) )
     * </pre>
     * which provides improved numerical stability and precision over direct series expansion.
     *
     * <p>
     * If the trigonometric mode is DEG (degrees), the result is converted accordingly.
     *
     * @param argument          the input value x for which to compute arcsine; must be in [-1, 1]
     * @param mathContext       the context to control precision and rounding
     * @param trigonometricMode the output mode: RAD or DEG
     * @param locale            the locale used for formatting output
     * @return the arcsine of x as a BigNumber
     * @throws ArithmeticException if argument is outside \[-1, 1\]
     */
    public static BigNumber asin(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        BigDecimal result = BigDecimalMath.asin(argument.toBigDecimal(), mathContext);
        if (trigonometricMode == TrigonometricMode.DEG) {
            result = bigDecimalRadiansToDegrees(result, mathContext, locale);
        }

        return new BigNumber(result.toPlainString(), locale, mathContext).roundAfterDecimals(mathContext).trim();
    }

    /**
     * Calculates the arccosine (inverse cosine) of the given argument.
     * <p>
     * Mathematically, acos(x) returns the angle θ such that cos(θ) = x, where θ ∈ [0, π].
     * The function is defined for input values x ∈ [-1, 1].
     * <p>
     * Formula:
     * <pre>
     * acos(x) = θ, where cos(θ) = x
     * </pre>
     * <p>
     * If {@code trigonometricMode} is DEG, the result is converted from radians to degrees.
     *
     * @param argument          the input value x for which to compute arccosine
     * @param mathContext       the precision and rounding context
     * @param trigonometricMode indicates whether the result is returned in radians or degrees
     * @param locale            locale used for formatting the output
     * @return a {@link BigNumber} representing the arccosine of the argument
     * @throws ArithmeticException if argument is outside [-1, 1]
     */
    public static BigNumber acos(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        BigDecimal result = BigDecimalMath.acos(argument.toBigDecimal(), mathContext);
        if (trigonometricMode == TrigonometricMode.DEG) {
            result = bigDecimalRadiansToDegrees(result, mathContext, locale);
        }

        return new BigNumber(result.toPlainString(), locale, mathContext).trim();
    }

    /**
     * Computes the arc tangent (inverse tangent) of the given {@code argument}, returning the angle whose tangent is
     * {@code argument}.
     * The result can be returned either in radians or degrees, depending on the specified {@link TrigonometricMode}.
     *
     * <p>
     * Mathematically, this method evaluates:
     * <ul>
     *   <li>{@code atan(x)} for |x| ≤ 1 using the Taylor (Maclaurin) series:
     *     <pre>{@code
     *     atan(x) = sum_{k=0}^∞ [ (-1)^k * x^(2k + 1) / (2k + 1) ]
     *     }</pre>
     *     This series converges for all real x with |x| ≤ 1 and is centered at 0. It provides a numerically stable and accurate
     *     way to approximate arctangent for small values of {@code x}.
     *   </li>
     *   <li>{@code atan(x)} for |x| > 1 using the identity:
     *     <pre>{@code
     *     atan(x) = sign(x) * π/2 - atan(1 / x)
     *     }</pre>
     *     This identity ensures fast convergence by transforming the input into the domain |x| ≤ 1, where the Taylor series is applied.
     *     The subtraction compensates for the shift due to transformation, and sign(x) determines the correct quadrant.
     *   </li>
     * </ul>
     *
     * <p>
     * If {@code trigonometricMode == TrigonometricMode.DEG}, the result in radians is converted to degrees using:
     * <pre>{@code
     * degrees = radians * (180 / π)
     * }</pre>
     *
     * <p>
     * Internally, the result is computed by symbolically evaluating a summation expression via {@link CalculatorEngine}.
     * The number of summation terms is chosen based on {@link MathContext#getPrecision()}, with an upper limit to ensure performance.
     *
     * @param argument          the input value for which the arctangent is to be computed (may be negative or greater than 1)
     * @param mathContext       the precision and rounding mode used for all intermediate and final calculations
     * @param trigonometricMode whether the result should be returned in radians or degrees
     * @param locale            the locale to be used when formatting the final {@code BigNumber} result (e.g. decimal separator)
     * @return the arctangent of {@code argument}, expressed in the selected trigonometric mode and with the specified precision
     * @see <a href="https://en.wikipedia.org/wiki/Inverse_trigonometric_functions#Infinite_series">Wikipedia: Inverse
     * trigonometric functions – Infinite series</a>
     * @see BigNumber
     * @see CalculatorEngine
     * @see TrigonometricMode
     */
    public static BigNumber atan(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        BigNumber result;

        if (argument.isLessThanOrEqualTo(BigNumbers.ONE)) {
            result = computeAtanSeries(argument, mathContext);
        } else {
            BigNumber oneOverX = BigNumbers.ONE.divide(argument, mathContext);
            BigNumber atanReciprocal = computeAtanSeries(oneOverX, mathContext);
            BigNumber piOver2 = BigNumbers.pi(mathContext).divide(BigNumbers.TWO, mathContext);

            result = argument.isPositive()
                    ? piOver2.subtract(atanReciprocal)
                    : piOver2.negate().subtract(atanReciprocal);
        }

        if (trigonometricMode == TrigonometricMode.DEG) {
            BigDecimal degrees = bigDecimalRadiansToDegrees(result.toBigDecimal(), mathContext, locale);
            result = new BigNumber(degrees.toPlainString(), locale, mathContext);
        }

        return result;
    }

    /**
     * Computes the arctangent of the given argument using the Taylor (Maclaurin) series expansion.
     * <p>
     * The series used is:
     * <pre>
     *   atan(x) = sum_{k=0}^∞ [ (-1)^k * x^(2k + 1) / (2k + 1) ]
     * </pre>
     * This method sums terms until the absolute value of the current term is less than a small epsilon,
     * determined by the desired precision, or until a maximum of 10,000 terms is reached to prevent infinite loops.
     *
     * @param argument    the input value for which to compute arctangent (|x| ≤ 1 recommended for best convergence)
     * @param mathContext the precision and rounding context for calculations
     * @return the arctangent of the argument as a BigNumber, with the specified precision and locale
     */
    private static BigNumber computeAtanSeries(@NonNull final BigNumber argument, @NonNull final MathContext mathContext) {
        final BigDecimal xBD = argument.toBigDecimal();
        final int scale = mathContext.getPrecision() + 5;
        final BigDecimal epsilon = BigDecimal.ONE.movePointLeft(scale);

        BigDecimal term = xBD;
        BigDecimal sum = term;
        int k = 1;

        while (term.abs().compareTo(epsilon) > 0 && k < 10000) {
            BigDecimal numerator = xBD.pow(2 * k + 1, mathContext).negate();
            BigDecimal denominator = BigDecimal.valueOf(2L * k + 1L);

            if (k % 2 == 0) {
                numerator = numerator.negate();
            }

            term = numerator.divide(denominator, mathContext);
            sum = sum.add(term, mathContext);
            k++;
        }
        return new BigNumber(sum.toPlainString(), argument.getLocale(), mathContext);
    }

    /**
     * Computes the inverse cotangent (arccotangent, <i>acot</i>) of a given {@link BigNumber} argument with
     * arbitrary precision, according to the specified {@link MathContext}, {@link TrigonometricMode},
     * and {@link Locale}.
     * <p>
     * The acot function is mathematically defined as:
     * <pre>
     *     acot(x) = arccot(x) = atan(1 / x),    for x ≠ 0
     * </pre>
     * where {@code atan} denotes the inverse tangent function.
     * <p>
     * The principal branch of the inverse cotangent is chosen, with values constrained to:
     * <ul>
     *     <li><b>Radians:</b> (0, π)</li>
     *     <li><b>Degrees:</b> (0°, 180°)</li>
     * </ul>
     * This corresponds to the standard mathematical convention for the real-valued inverse cotangent.
     *
     * <h3>Domain</h3>
     * <ul>
     *     <li>All real numbers except {@code 0}, since {@code acot(0)} is undefined.</li>
     * </ul>
     *
     * <h3>Range</h3>
     * <ul>
     *     <li>In <b>radian mode</b>: values lie strictly between {@code 0} and {@code π}.</li>
     *     <li>In <b>degree mode</b>: values lie strictly between {@code 0°} and {@code 180°}.</li>
     * </ul>
     *
     * <h3>Special Cases</h3>
     * <ul>
     *     <li>{@code acot(0)} → throws {@link ArithmeticException}, since division by zero occurs.</li>
     *     <li>{@code acot(+∞)} → approaches {@code 0}.</li>
     *     <li>{@code acot(-∞)} → approaches {@code π} (or {@code 180°} in degree mode).</li>
     * </ul>
     *
     * <h3>Implementation Notes</h3>
     * <ol>
     *     <li>The input {@code argument} is first converted to an {@link Apfloat} with the given precision.</li>
     *     <li>The reciprocal {@code 1 / x} is computed using {@link ApfloatMath#inverseRoot} with exponent {@code 1}.</li>
     *     <li>The inverse tangent {@code atan(1 / x)} is then computed via {@link ApfloatMath#atan}.</li>
     *     <li>If {@link TrigonometricMode#DEG} is specified, the result is converted from radians to degrees using:
     *         <pre>acot_deg(x) = acot_rad(x) × (180 / π)</pre></li>
     *     <li>The result is wrapped in a {@link BigNumber}, respecting the provided {@link Locale},
     *         and finally rounded according to the given {@link MathContext}.</li>
     * </ol>
     *
     * @param argument          the input value {@code x}, must not be {@code null}
     * @param mathContext       the {@link MathContext} specifying precision and rounding, must not be {@code null}
     * @param trigonometricMode the {@link TrigonometricMode} to determine whether the result is expressed
     *                          in radians or degrees, must not be {@code null}
     * @param locale            the {@link Locale} used for number formatting and parsing, must not be {@code null}
     * @return the inverse cotangent of the given {@code argument}, expressed as a {@link BigNumber}
     * in the specified trigonometric mode and locale
     * @throws ArithmeticException if {@code argument} is equal to zero, since {@code acot(0)} is undefined
     */
    public static BigNumber acot(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (argument.isEqualTo(BigNumbers.ZERO)) {
            throw new ArithmeticException("acot(x) is undefined for x = 0");
        }

        Apfloat apfloatArgument = new Apfloat(argument.toBigDecimal(), mathContext.getPrecision());
        Apfloat reciprocal = ApfloatMath.inverseRoot(apfloatArgument, 1); // = 1/x

        Apfloat acotValue = ApfloatMath.atan(reciprocal);

        if (trigonometricMode == TrigonometricMode.DEG) {
            Apfloat pi = ApfloatMath.pi(mathContext.getPrecision());
            acotValue = acotValue.multiply(new Apfloat(BigNumbers.ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext.getPrecision()))
                    .divide(pi);
        }

        return new BigNumber(acotValue.toString(true), locale).round(mathContext);
    }

}
