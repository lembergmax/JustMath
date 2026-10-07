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

import static io.github.lembergmax.justmath.bignumber.math.utils.MathUtils.bigDecimalNumberToRadians;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArithmeticException;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

/**
 * Provides trigonometric functions operating on BigNumber values.
 * Supports angle inputs in degrees or radians, controlled by {@link TrigonometricMode}.
 */
public final class TrigonometricMath {

    /**
     * Guard digits for the conversion of the angle and for the rounding noise of the library function, on top of the
     * integer digits of the angle. The adaptive computation adds more digits for an angle near a multiple of pi.
     */
    private static final int ANGLE_GUARD_DIGITS = 10;

    /**
     * Integer digits of an angle in degrees after the reduction to a full turn, which is below 360.
     */
    private static final int REDUCED_DEGREE_INTEGER_DIGITS = 3;

    private static final BigDecimal QUARTER_TURN_DEGREES = BigDecimal.valueOf(90);

    private static final BigDecimal HALF_TURN_DEGREES = BigDecimal.valueOf(180);

    private static final BigDecimal THREE_QUARTER_TURN_DEGREES = BigDecimal.valueOf(270);

    private static final BigDecimal FULL_TURN_DEGREES = BigDecimal.valueOf(360);

    /** Non-instantiable utility class. */
    private TrigonometricMath() {
    }


    /**
     * Calculates the sine of the given angle.
     * <p>
     * Mathematically, the sine function is defined as:
     * <pre>
     * sin(θ) = opposite / hypotenuse
     * </pre>
     * where θ is the angle in radians or degrees. This method converts the angle to radians if necessary.
     *
     * @param angle
     * 	the angle for which to compute sine
     * @param mathContext
     * 	the {@link MathContext} controlling precision and rounding
     * @param trigonometricMode
     * 	the angle measurement mode (DEG for degrees, RAD for radians)
     * @param locale
     * 	the locale used for number formatting
     *
     * @return the sine of the angle as a {@link BigNumber}
     */
    public static BigNumber sin(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (isSineZero(angle, trigonometricMode)) {
            return new BigNumber("0", locale);
        }

        final BigDecimal sine = MathUtils.computeWithGuardDigits(mathContext, angleGuardDigits(angle, trigonometricMode),
                workingContext -> BigDecimalMath.sin(radians(angle, workingContext, trigonometricMode, locale), workingContext));

        return new BigNumber(sine.toPlainString(), locale).trim();
    }

    /**
     * Calculates the cosine of the given angle.
     * <p>
     * Mathematically, the cosine function is defined as:
     * <pre>
     * cos(θ) = adjacent / hypotenuse
     * </pre>
     * where θ is the angle in radians or degrees. This method converts the angle to radians if necessary.
     *
     * @param angle
     * 	the angle for which to compute cosine
     * @param mathContext
     * 	the {@link MathContext} controlling precision and rounding
     * @param trigonometricMode
     * 	the angle measurement mode (DEG for degrees, RAD for radians)
     * @param locale
     * 	the locale used for number formatting
     *
     * @return the cosine of the angle as a {@link BigNumber}
     */
    public static BigNumber cos(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (isCosineZero(angle, trigonometricMode)) {
            return new BigNumber("0", locale);
        }

        final BigDecimal cosine = MathUtils.computeWithGuardDigits(mathContext, angleGuardDigits(angle, trigonometricMode),
                workingContext -> BigDecimalMath.cos(radians(angle, workingContext, trigonometricMode, locale), workingContext));

        return new BigNumber(cosine.toPlainString(), locale).trim();
    }

    /**
     * Calculates the tangent of the given angle.
     * <p>
     * Mathematically, the tangent function is defined as:
     * <pre>
     * tan(θ) = sin(θ) / cos(θ)
     * </pre>
     * where θ is the angle in radians or degrees. This method converts the angle to radians if necessary.
     * Note that tangent is undefined where cosine is zero.
     *
     * @param angle
     * 	the angle for which to compute tangent
     * @param mathContext
     * 	the {@link MathContext} controlling precision and rounding
     * @param trigonometricMode
     * 	the angle measurement mode (DEG for degrees, RAD for radians)
     * @param locale
     * 	the locale used for number formatting
     *
     * @return the tangent of the angle as a {@link BigNumber}
     */
    public static BigNumber tan(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (isTangentSingularity(angle, trigonometricMode)) {
            throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "tan is undefined at " + angle.toString() + " (cosine is zero at this point)");
        }
        if (isSineZero(angle, trigonometricMode)) {
            return new BigNumber("0", locale);
        }

        final BigDecimal tangent = MathUtils.computeWithGuardDigits(mathContext, angleGuardDigits(angle, trigonometricMode),
                workingContext -> BigDecimalMath.tan(radians(angle, workingContext, trigonometricMode, locale), workingContext));

        return new BigNumber(tangent.toPlainString(), locale).trim();
    }

    /**
     * Returns the guard digits for an angle: a constant for the rounding noise, plus one digit per integer digit of
     * the angle, because the relative error of the conversion to radians is multiplied by the size of the angle. An
     * angle in degrees is first reduced to a full turn, so it never has more than three integer digits.
     *
     * @param angle             the angle; must not be {@code null}
     * @param trigonometricMode the angle measurement mode; must not be {@code null}
     * @return the number of guard digits
     */
    private static int angleGuardDigits(final BigNumber angle, final TrigonometricMode trigonometricMode) {
        final int angleIntegerDigits = trigonometricMode == TrigonometricMode.DEG
                ? REDUCED_DEGREE_INTEGER_DIGITS
                : MathUtils.integerDigitCount(angle.toBigDecimal());
        return ANGLE_GUARD_DIGITS + angleIntegerDigits;
    }

    /**
     * Converts an angle to radians at the working precision. An angle in degrees is reduced to a full turn exactly
     * first, so that a large angle keeps its accuracy and an exact multiple of a quarter turn stays exact.
     *
     * @param angle             the angle; must not be {@code null}
     * @param workingContext    the working precision; must not be {@code null}
     * @param trigonometricMode the angle measurement mode; must not be {@code null}
     * @param locale            the locale of the intermediate value; must not be {@code null}
     * @return the angle in radians
     */
    private static BigDecimal radians(final BigNumber angle, final MathContext workingContext, final TrigonometricMode trigonometricMode, final Locale locale) {
        return trigonometricMode == TrigonometricMode.DEG
                ? bigDecimalNumberToRadians(reducedDegrees(angle), workingContext, locale)
                : angle.toBigDecimal();
    }

    /**
     * Reduces an angle in degrees to the interval {@code [0, 360)}. The reduction is exact.
     *
     * @param angle the angle in degrees; must not be {@code null}
     * @return the angle in {@code [0, 360)}
     */
    private static BigDecimal reducedDegrees(final BigNumber angle) {
        final BigDecimal remainder = angle.toBigDecimal().remainder(FULL_TURN_DEGREES);
        return remainder.signum() < 0 ? remainder.add(FULL_TURN_DEGREES) : remainder;
    }

    /**
     * Tells whether the sine of an angle is exactly zero: {@code 0} in radian mode, a multiple of 180 degrees in
     * degree mode. The sine of any other radian value is not zero, because pi is irrational.
     *
     * @param angle             the angle; must not be {@code null}
     * @param trigonometricMode the angle measurement mode; must not be {@code null}
     * @return {@code true} if {@code sin(angle) == 0}
     */
    private static boolean isSineZero(final BigNumber angle, final TrigonometricMode trigonometricMode) {
        if (trigonometricMode == TrigonometricMode.DEG) {
            final BigDecimal reduced = reducedDegrees(angle);
            return reduced.signum() == 0 || reduced.compareTo(HALF_TURN_DEGREES) == 0;
        }
        return angle.toBigDecimal().signum() == 0;
    }

    /**
     * Tells whether the cosine of an angle is exactly zero: an odd multiple of 90 degrees in degree mode, never in
     * radian mode.
     *
     * @param angle             the angle; must not be {@code null}
     * @param trigonometricMode the angle measurement mode; must not be {@code null}
     * @return {@code true} if {@code cos(angle) == 0}
     */
    private static boolean isCosineZero(final BigNumber angle, final TrigonometricMode trigonometricMode) {
        if (trigonometricMode != TrigonometricMode.DEG) {
            return false;
        }
        final BigDecimal reduced = reducedDegrees(angle);
        return reduced.compareTo(QUARTER_TURN_DEGREES) == 0 || reduced.compareTo(THREE_QUARTER_TURN_DEGREES) == 0;
    }

    /**
     * Detects exact tangent singularities for the {@link TrigonometricMode#DEG} mode where the input
     * can be checked symbolically. In radian mode the input is transcendental in general, so a
     * reliable exact check is not possible; callers in radian mode receive whatever the
     * underlying {@link BigDecimalMath#tan(BigDecimal, MathContext)} produces.
     *
     * @param angle             the input angle
     * @param trigonometricMode the angle measurement mode
     * @return {@code true} when {@code angle} is an exact {@code 90° + k·180°} in degree mode
     */
    private static boolean isTangentSingularity(final BigNumber angle, final TrigonometricMode trigonometricMode) {
        if (trigonometricMode != TrigonometricMode.DEG) {
            return false;
        }
        final BigDecimal degrees = angle.toBigDecimal();
        if (hasFractionalPart(degrees)) {
            return false;
        }
        final java.math.BigInteger integerDegrees = degrees.toBigIntegerExact();
        final java.math.BigInteger modulo = integerDegrees.mod(java.math.BigInteger.valueOf(180));
        return modulo.equals(java.math.BigInteger.valueOf(90));
    }

    /**
     * Calculates the cotangent of the given angle.
     * <p>
     * Mathematically, the cotangent function is defined as:
     * <pre>
     * cot(θ) = 1 / tan(θ) = cos(θ) / sin(θ)
     * </pre>
     * where θ is the angle in radians or degrees. This method converts the angle to radians if necessary.
     * Cotangent is undefined where sine is zero.
     *
     * @param angle
     * 	the angle for which to compute cotangent
     * @param mathContext
     * 	the {@link MathContext} controlling precision and rounding
     * @param trigonometricMode
     * 	the angle measurement mode (DEG for degrees, RAD for radians)
     * @param locale
     * 	the locale used for number formatting
     *
     * @return the cotangent of the angle as a {@link BigNumber}
     */
    public static BigNumber cot(@NonNull final BigNumber angle, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (isCotangentSingularity(angle, trigonometricMode)) {
            throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "cot is undefined at " + angle.toString() + " (sine is zero at this point)");
        }
        if (isCosineZero(angle, trigonometricMode)) {
            return new BigNumber("0", locale);
        }

        final BigDecimal cotangent = MathUtils.computeWithGuardDigits(mathContext, angleGuardDigits(angle, trigonometricMode),
                workingContext -> BigDecimalMath.cot(radians(angle, workingContext, trigonometricMode, locale), workingContext));

        return new BigNumber(cotangent.toPlainString(), locale).trim();
    }

    /**
     * Detects exact cotangent singularities for the {@link TrigonometricMode#DEG} mode (where the
     * input can be checked symbolically). In radian mode no exact check is performed.
     *
     * @param angle             the input angle
     * @param trigonometricMode the angle measurement mode
     * @return {@code true} when {@code angle} is an exact {@code k·180°} in degree mode
     */
    private static boolean isCotangentSingularity(final BigNumber angle, final TrigonometricMode trigonometricMode) {
        if (trigonometricMode != TrigonometricMode.DEG) {
            return false;
        }
        final BigDecimal degrees = angle.toBigDecimal();
        if (hasFractionalPart(degrees)) {
            return false;
        }
        final java.math.BigInteger integerDegrees = degrees.toBigIntegerExact();
        return integerDegrees.mod(java.math.BigInteger.valueOf(180)).signum() == 0;
    }

    /**
     * Returns whether the given degree value has a non-zero fractional part and therefore cannot be an
     * exact integer multiple of a degree such as {@code 90° + k·180°}.
     *
     * @param degrees the angle expressed in degrees
     * @return {@code true} if {@code degrees} is not a whole number of degrees
     */
    private static boolean hasFractionalPart(final BigDecimal degrees) {
        return degrees.stripTrailingZeros().scale() > 0;
    }

}
