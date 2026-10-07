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

package io.github.lembergmax.justmath.bignumber.math.utils;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumberCoordinate;
import io.github.lembergmax.justmath.bignumber.MultiValueResult;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArgumentException;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArithmeticException;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
import java.util.function.Function;

import static io.github.lembergmax.justmath.bignumber.BigNumbers.ONE_HUNDRED_EIGHTY;
import static io.github.lembergmax.justmath.bignumber.BigNumbers.pi;

/**
 * Utility class for internal mathematical operations involving angle conversions.
 * <p>
 * This class provides helper methods for converting angles between degrees and radians
 * in a locale-sensitive and precision-aware manner using {@link BigNumber} and {@link BigDecimal}.
 * It is primarily used internally to support trigonometric computations that require
 * consistent and accurate unit conversions.
 */
public class MathUtils {

    /**
     * Upper bound on {@link MathContext} precision accepted by the library. Requesting an astronomically
     * large precision (e.g. via a crafted untrusted input that reaches a precision-taking operation) would
     * make a single computation allocate gigabytes and run for minutes, a denial-of-service vector. The
     * bound is generous — far above any realistic need (the default division precision is 100) — so it never
     * rejects legitimate use, only absurd requests.
     */
    public static final int MAX_MATH_CONTEXT_PRECISION = 1_000_000;

    /**
     * Converts the given angle to radians depending on the specified {@link TrigonometricMode}.
     * <p>
     * If the {@code trigonometricMode} is {@link TrigonometricMode#DEG}, the angle is treated as
     * being in degrees and converted to radians using the formula:
     * <pre>
     *     radians = degrees × (π / 180)
     * </pre>
     * If the mode is {@link TrigonometricMode#RAD}, the angle is assumed to already be in radians
     * and returned directly as a {@link BigDecimal}.
     *
     * @param angle             the angle to convert, represented as a {@link BigNumber}
     * @param mathContext       the {@link MathContext} to apply during internal calculations to control precision and rounding
     * @param trigonometricMode the trigonometric mode indicating whether the input angle is in degrees or radians
     * @param locale            the {@link Locale} used to preserve regional number formatting or symbols in the {@code BigNumber}
     * @return the angle in radians as a {@link BigDecimal}, computed with the specified precision and locale
     */
    public static BigDecimal convertAngle(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        return (trigonometricMode == TrigonometricMode.DEG)
                ? bigDecimalNumberToRadians(angle.toBigDecimal(), mathContext, locale)
                : angle.toBigDecimal();
    }

    /**
     * Converts an angle from radians to degrees.
     * <p>
     * The conversion uses the formula:
     * <pre>
     *     degrees = radians × (180 / π)
     * </pre>
     * This method wraps the input in a {@link BigNumber}, ensuring precision and locale awareness during conversion.
     *
     * @param radians     the angle in radians as a {@link BigDecimal}
     * @param mathContext the {@link MathContext} defining the precision and rounding used in the conversion
     * @param locale      the {@link Locale} to apply when instantiating the intermediate {@link BigNumber}
     * @return the corresponding angle in degrees as a {@link BigDecimal}
     */
    public static BigDecimal bigDecimalRadiansToDegrees(@NonNull final BigDecimal radians, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        return new BigNumber(radians, locale).toDegrees(mathContext).toBigDecimal();
    }

    /**
     * Converts an angle from degrees to radians.
     * <p>
     * The conversion uses the formula:
     * <pre>
     *     radians = degrees × (π / 180)
     * </pre>
     * This method uses a {@link BigNumber} internally to perform the calculation precisely
     * while respecting the given locale and math context.
     *
     * @param degrees     the angle in degrees as a {@link BigDecimal}
     * @param mathContext the {@link MathContext} that specifies the precision and rounding to use
     * @param locale      the {@link Locale} used for instantiating the {@link BigNumber}, ensuring consistent formatting and behavior
     * @return the corresponding angle in radians as a {@link BigDecimal}
     */
    public static BigDecimal bigDecimalNumberToRadians(@NonNull final BigDecimal degrees, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        return new BigNumber(degrees.multiply(pi(mathContext, locale).toBigDecimal()).divide(ONE_HUNDRED_EIGHTY.toBigDecimal(), mathContext), locale).toBigDecimal();
    }

    /**
     * Digits of relative accuracy that {@link #computeWithGuardDigits} lets a result lose to cancellation before it
     * repeats the computation with more digits.
     */
    private static final int ADAPTIVE_SAFETY_DIGITS = 5;

    /**
     * Largest number of times {@link #computeWithGuardDigits} repeats a computation with more digits.
     */
    private static final int MAX_ADAPTIVE_ATTEMPTS = 8;

    /**
     * Largest number of times {@link #computeWithGuardDigits} evaluates a function again because its result was zero.
     */
    private static final int MAX_ZERO_RESULT_RETRIES = 3;

    /**
     * Factor and offset by which {@link #computeWithGuardDigits} grows the guard digits after a result of zero.
     */
    private static final int ZERO_RESULT_GUARD_FACTOR = 4;

    private static final int ZERO_RESULT_GUARD_OFFSET = 50;

    /**
     * Returns a {@link MathContext} with {@code extraDigits} more digits and the rounding mode of the caller.
     *
     * @param mathContext the requested context; must not be {@code null}
     * @param extraDigits the number of guard digits; must not be negative
     * @return the working context; never {@code null}
     */
    public static MathContext withGuardDigits(@NonNull final MathContext mathContext, final int extraDigits) {
        return new MathContext(mathContext.getPrecision() + extraDigits, mathContext.getRoundingMode());
    }

    /**
     * Counts the digits of a value that lie left of the decimal point.
     *
     * @param value the value; must not be {@code null}
     * @return the number of integer digits; 0 if {@code |value| < 1}
     */
    public static int integerDigitCount(@NonNull final BigDecimal value) {
        return Math.max(0, value.precision() - value.scale());
    }

    /**
     * Computes a function value so that the requested number of digits is correct, whatever the size of the result,
     * and rounds once to the requested precision and rounding mode.
     *
     * <p>Libraries such as {@code BigDecimalMath} guarantee the <em>absolute</em> precision of a result near 1.
     * A result of {@code 1E-20}, for example {@code sin(x)} for an {@code x} near a multiple of pi, {@code acos(x)}
     * for an {@code x} near 1 or {@code ln(x)} for an {@code x} near 1, has lost 20 of its digits to cancellation.
     * A result of {@code 1E+20}, such as {@code tan(x)} near a pole, has the same problem. This method evaluates
     * {@code function} with {@code baseGuardDigits} extra digits and then compares the size of the result with the
     * guard digits it has. If the result has lost more digits than the guard covers, it evaluates the function again
     * with enough digits, until the size of the result is stable.</p>
     *
     * <p>A result of zero is not trusted: a library returns 0 for a result that is smaller than its absolute
     * precision, such as {@code atanh(1E-21)} at 5 digits. The function is then evaluated again with more guard
     * digits, up to {@value #MAX_ZERO_RESULT_RETRIES} times, and zero is returned only if it stays zero. A caller
     * whose function is exactly zero for some arguments, such as {@code sin(0)} or {@code ln(1)}, returns that
     * zero itself before it calls this method.</p>
     *
     * @param requestedMathContext the precision and rounding mode the caller asked for; must not be {@code null}
     * @param baseGuardDigits      the guard digits that every evaluation gets, for the rounding noise of the
     *                             function itself; must not be negative
     * @param function             evaluates the function at the given working precision; must not be {@code null}
     * @return the value rounded to {@code requestedMathContext}; never {@code null}
     * @throws MathArithmeticException with {@code MATH_OVERFLOW} if the working precision would exceed
     *                                 {@link #MAX_MATH_CONTEXT_PRECISION}, or the result does not stabilize
     */
    public static BigDecimal computeWithGuardDigits(
            @NonNull final MathContext requestedMathContext,
            final int baseGuardDigits,
            @NonNull final Function<MathContext, BigDecimal> function
    ) {
        int guardDigits = baseGuardDigits;
        int zeroResultRetries = 0;
        for (int attempt = 0; attempt < MAX_ADAPTIVE_ATTEMPTS; attempt++) {
            final MathContext workingContext = withGuardDigits(requestedMathContext, guardDigits);
            checkMathContext(workingContext);

            final BigDecimal result = function.apply(workingContext);
            if (result.signum() == 0) {
                if (zeroResultRetries >= MAX_ZERO_RESULT_RETRIES) {
                    return result;
                }
                zeroResultRetries++;
                guardDigits = guardDigits * ZERO_RESULT_GUARD_FACTOR + ZERO_RESULT_GUARD_OFFSET;
                continue;
            }

            final int requiredGuardDigits = Math.abs(decimalExponent(result)) + ADAPTIVE_SAFETY_DIGITS;
            if (guardDigits >= requiredGuardDigits) {
                return result.round(requestedMathContext);
            }
            guardDigits = requiredGuardDigits + baseGuardDigits;
        }

        throw new MathArithmeticException(CalculatorErrorCode.MATH_OVERFLOW, "The result does not stabilize within " + MAX_ADAPTIVE_ATTEMPTS + " attempts");
    }

    private static int decimalExponent(final BigDecimal value) {
        return value.precision() - value.scale() - 1;
    }

    /**
     * Checks that the provided {@link MathContext} has a precision greater than zero.
     * <p>
     * Throws an {@link IllegalArgumentException} if the precision is zero or negative,
     * ensuring that mathematical operations using this context are valid.
     *
     * @param mathContext the {@link MathContext} to validate
     * @throws IllegalArgumentException if the precision is less than or equal to zero
     */
    public static void checkMathContext(@NonNull final MathContext mathContext) {
        if (mathContext.getPrecision() <= 0) {
            throw new MathArgumentException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "MathContext precision must be greater than zero");
        }
        if (mathContext.getPrecision() > MAX_MATH_CONTEXT_PRECISION) {
            throw new MathArithmeticException(CalculatorErrorCode.MATH_OVERFLOW, "MathContext precision " + mathContext.getPrecision()
                    + " is too large (maximum " + MAX_MATH_CONTEXT_PRECISION + ")");
        }
    }

    /**
     * Ensures that the provided object can be used as a scalar {@link BigNumber}.
     * <p>
     * Operators and functions that participate in a normal scalar expression
     * (addition, subtraction, multiplication, division, power, single-argument
     * functions, ...) call this helper to support transparent use of multi-value results.
     * </p>
     *
     * <ul>
     *   <li>If the object implements {@link MultiValueResult} (for example a
     *       {@link BigNumberCoordinate} returned by {@code Pol(...)} or
     *       {@code Rec(...)}), its {@link MultiValueResult#firstValue() first
     *       value} is returned as a plain {@code BigNumber}. This is what allows
     *       expressions like {@code Pol(1;2)+3} or {@code Pol(1;2)+Rec(2;1)} to
     *       evaluate correctly.</li>
     *   <li>If the object is already a plain {@link BigNumber}, it is returned
     *       unchanged.</li>
     *   <li>Any other type triggers an {@link IllegalArgumentException}.</li>
     * </ul>
     *
     * @param object the value popped from the evaluation stack
     * @return a scalar {@link BigNumber} that can be fed into arithmetic operations
     * @throws IllegalArgumentException if the value cannot be reduced to a scalar
     */
    public static BigNumber ensureScalar(Object object) {
        if (object instanceof MultiValueResult multiValueResult) {
            return multiValueResult.firstValue();
        }
        if (object instanceof BigNumber bigNumber) {
            return bigNumber;
        }

        throw new IllegalArgumentException("Expected scalar BigNumber but got: " + object);
    }

}
