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
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

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
            throw new IllegalArgumentException("MathContext precision must be greater than zero");
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
