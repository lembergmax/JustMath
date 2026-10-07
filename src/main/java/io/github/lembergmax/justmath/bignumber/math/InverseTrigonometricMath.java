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

import static io.github.lembergmax.justmath.bignumber.math.utils.MathUtils.bigDecimalRadiansToDegrees;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArithmeticException;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

/**
 * Provides high-precision implementations of inverse trigonometric functions
 * using {@link BigNumber} for arbitrary precision arithmetic.
 * <p>
 * This class supports calculation of the arcsine, arccosine, arctangent,
 * and arccotangent functions with output in radians or degrees.
 */
public final class InverseTrigonometricMath {

    /** Non-instantiable utility class. */
    private InverseTrigonometricMath() {
    }


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

        if (argument.isEqualTo(BigNumbers.ZERO)) {
            return new BigNumber("0", locale, mathContext);
        }

        final BigDecimal angle = MathUtils.computeWithGuardDigits(mathContext, ANGLE_GUARD_DIGITS,
                workingContext -> inMode(BigDecimalMath.asin(argument.toBigDecimal(), workingContext), workingContext, trigonometricMode, locale));

        return new BigNumber(angle.toPlainString(), locale, mathContext).trim();
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

        if (argument.isEqualTo(BigNumbers.ONE)) {
            return new BigNumber("0", locale, mathContext);
        }

        final BigDecimal angle = MathUtils.computeWithGuardDigits(mathContext, ANGLE_GUARD_DIGITS,
                workingContext -> inMode(BigDecimalMath.acos(argument.toBigDecimal(), workingContext), workingContext, trigonometricMode, locale));

        return new BigNumber(angle.toPlainString(), locale, mathContext).trim();
    }

    /**
     * Converts a radian value computed at the working precision to the requested trigonometric mode, without rounding
     * to the caller's precision.
     *
     * @param radians           the angle in radians at the working precision; must not be {@code null}
     * @param workingContext    the working precision; must not be {@code null}
     * @param trigonometricMode RAD or DEG; must not be {@code null}
     * @param locale            the locale for the intermediate {@link BigNumber}; must not be {@code null}
     * @return the angle in the requested mode at the working precision; never {@code null}
     */
    private static BigDecimal inMode(final BigDecimal radians, final MathContext workingContext, final TrigonometricMode trigonometricMode, final Locale locale) {
        return trigonometricMode == TrigonometricMode.DEG
                ? bigDecimalRadiansToDegrees(radians, workingContext, locale)
                : radians;
    }

    /**
     * Computes the arc tangent (inverse tangent) of the given {@code argument}, returning the angle whose tangent is
     * {@code argument}. The result can be returned either in radians or degrees, depending on the specified
     * {@link TrigonometricMode}.
     *
     * <p>The radian value is computed with {@link BigDecimalMath#atan(BigDecimal, MathContext)} for an argument of
     * at most 1 in absolute value, and for a larger one with {@code sign(x) * π/2 - atan(1/x)}, so the distance from
     * {@code π/2} is not lost. Intermediate work uses {@link #ANGLE_GUARD_DIGITS} extra digits and the result is
     * rounded once to the caller's {@link MathContext}.</p>
     *
     * <p>If {@code trigonometricMode == TrigonometricMode.DEG}, the radian value is converted to degrees via
     * {@code degrees = radians * (180 / π)} (with its own guard digits) before the final rounding.</p>
     *
     * @param argument          the input value for which the arctangent is to be computed (any real value)
     * @param mathContext       the precision and rounding mode for the final result
     * @param trigonometricMode whether the result should be returned in radians or degrees
     * @param locale            the locale used when formatting the final {@code BigNumber} result
     * @return the arctangent of {@code argument}, in the selected trigonometric mode and rounded to the requested precision
     * @see TrigonometricMode
     */
    public static BigNumber atan(@NonNull final BigNumber argument, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        final MathContext guardContext = withGuardDigits(mathContext);
        final BigDecimal radians = arcTangent(argument.toBigDecimal(), guardContext);

        return toAngleResult(radians, mathContext, guardContext, trigonometricMode, locale);
    }

    /**
     * Computes the arc tangent in radians for an argument of any size.
     *
     * <p>{@link BigDecimalMath#atan(BigDecimal, MathContext)} returns exactly {@code ±π/2} for an argument larger
     * than about {@code 10^(precision/2)}, which drops the distance {@code 1/|x|} from {@code π/2} that the last
     * digits of the result depend on. The library therefore only sees an argument with {@code |x| <= 1}; for a
     * larger one the identity {@code atan(x) = sign(x) * π/2 - atan(1/x)} gives the result.</p>
     *
     * @param argument       the argument; must not be {@code null}
     * @param workingContext the working precision; must not be {@code null}
     * @return the arc tangent of {@code argument} in radians, correct at the working precision
     */
    static BigDecimal arcTangent(final BigDecimal argument, final MathContext workingContext) {
        if (argument.abs().compareTo(BigDecimal.ONE) <= 0) {
            return BigDecimalMath.atan(argument, workingContext);
        }

        final BigDecimal reciprocalArcTangent = BigDecimalMath.atan(BigDecimal.ONE.divide(argument, workingContext), workingContext);
        return signedHalfPi(argument.signum(), workingContext).subtract(reciprocalArcTangent, workingContext);
    }

    /**
     * Computes the arc cotangent {@code acot(x) = atan(1/x)} in radians for a non-zero argument of any size.
     *
     * <p>For {@code |x| < 1} the reciprocal is large, so the identity
     * {@code atan(1/x) = sign(x) * π/2 - atan(x)} is used, which needs no reciprocal at all.</p>
     *
     * @param argument       the argument; must not be {@code null} and not zero
     * @param workingContext the working precision; must not be {@code null}
     * @return the arc cotangent of {@code argument} in radians, correct at the working precision
     */
    static BigDecimal arcCotangent(final BigDecimal argument, final MathContext workingContext) {
        if (argument.abs().compareTo(BigDecimal.ONE) > 0) {
            return BigDecimalMath.atan(BigDecimal.ONE.divide(argument, workingContext), workingContext);
        }

        return signedHalfPi(argument.signum(), workingContext).subtract(BigDecimalMath.atan(argument, workingContext), workingContext);
    }

    /**
     * Computes the angle of the point {@code (x, y)} in radians, in {@code (-π, π]}, for any point except the origin.
     *
     * <p>The library arc tangent is only used for a ratio of at most 1 in absolute value:
     * {@code atan2(y, x) = sign(y) * π/2 - atan(x/y)} for {@code |y| > |x|}, and otherwise
     * {@code atan(y/x)} for {@code x > 0} and {@code atan(y/x) ± π} for {@code x < 0}.</p>
     *
     * @param y              the y-coordinate; must not be {@code null}
     * @param x              the x-coordinate; must not be {@code null}; {@code x} and {@code y} are not both zero
     * @param workingContext the working precision; must not be {@code null}
     * @return the angle in radians, correct at the working precision
     */
    static BigDecimal arcTangent2(final BigDecimal y, final BigDecimal x, final MathContext workingContext) {
        if (y.signum() == 0) {
            return x.signum() > 0 ? BigDecimal.ZERO : BigDecimalMath.pi(workingContext);
        }
        if (x.signum() == 0) {
            return signedHalfPi(y.signum(), workingContext);
        }
        if (y.abs().compareTo(x.abs()) > 0) {
            final BigDecimal complementaryAngle = BigDecimalMath.atan(x.divide(y, workingContext), workingContext);
            return signedHalfPi(y.signum(), workingContext).subtract(complementaryAngle, workingContext);
        }

        final BigDecimal angle = BigDecimalMath.atan(y.divide(x, workingContext), workingContext);
        if (x.signum() > 0) {
            return angle;
        }
        final BigDecimal pi = BigDecimalMath.pi(workingContext);
        return y.signum() > 0 ? angle.add(pi, workingContext) : angle.subtract(pi, workingContext);
    }

    private static BigDecimal signedHalfPi(final int sign, final MathContext workingContext) {
        final BigDecimal halfPi = BigDecimalMath.pi(workingContext).divide(BigDecimal.TWO, workingContext);
        return sign < 0 ? halfPi.negate() : halfPi;
    }

    /**
     * Extra working digits added on top of the requested precision so the single final rounding to the caller's
     * {@link MathContext} is correct in its last place (avoids the double-rounding the hand-rolled implementations
     * suffered from).
     */
    private static final int ANGLE_GUARD_DIGITS = 10;

    /**
     * Builds a working {@link MathContext} with {@link #ANGLE_GUARD_DIGITS} extra precision and the caller's rounding mode.
     *
     * @param mathContext the caller-provided context; must not be {@code null}
     * @return a context with increased precision for intermediate computation
     */
    private static MathContext withGuardDigits(final MathContext mathContext) {
        return new MathContext(mathContext.getPrecision() + ANGLE_GUARD_DIGITS, mathContext.getRoundingMode());
    }

    /**
     * Converts a radian value (computed at {@code guardContext} precision) to the requested trigonometric mode and
     * rounds it once to the caller's {@link MathContext}.
     *
     * @param radians           the angle in radians at guard precision; must not be {@code null}
     * @param mathContext       the caller-requested precision/rounding; must not be {@code null}
     * @param guardContext      the working context used for the degree conversion; must not be {@code null}
     * @param trigonometricMode RAD or DEG; must not be {@code null}
     * @param locale            the locale for the resulting {@link BigNumber}; must not be {@code null}
     * @return the angle rounded to {@code mathContext}; never {@code null}
     */
    private static BigNumber toAngleResult(final BigDecimal radians, final MathContext mathContext, final MathContext guardContext, final TrigonometricMode trigonometricMode, final Locale locale) {
        final BigDecimal value = trigonometricMode == TrigonometricMode.DEG
                ? bigDecimalRadiansToDegrees(radians, guardContext, locale)
                : radians;
        return new BigNumber(value, locale).round(mathContext).trim();
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
     *
     * <p><b>Branch / range</b></p>
     * <p>This implementation uses the identity {@code acot(x) = atan(1/x)} directly, so the result shares
     * {@code atan}'s range:
     * <ul>
     *     <li><b>Radians:</b> {@code (-π/2, π/2) \ {0}} — negative for {@code x < 0}, positive for {@code x > 0}.</li>
     *     <li><b>Degrees:</b> {@code (-90°, 90°) \ {0}}.</li>
     * </ul>
     * (This is the {@code atan(1/x)} convention rather than the alternative continuous {@code (0, π)} convention.)
     *
     * <p><b>Domain</b></p>
     * <ul>
     *     <li>All real numbers except {@code 0}, since {@code acot(0)} is undefined.</li>
     * </ul>
     *
     * <p><b>Special cases</b></p>
     * <ul>
     *     <li>{@code acot(0)} → throws {@link ArithmeticException}, since {@code 1/0} is undefined.</li>
     *     <li>{@code acot(+∞)} → approaches {@code 0⁺}.</li>
     *     <li>{@code acot(-∞)} → approaches {@code 0⁻}.</li>
     * </ul>
     *
     * <p><b>Implementation notes</b></p>
     * <p>The reciprocal {@code 1/x} and the arctangent are computed with {@link #ANGLE_GUARD_DIGITS} extra digits
     * via {@link BigDecimalMath#atan(BigDecimal, MathContext)}; the result is rounded once to the caller's
     * {@link MathContext}. (Replaces the previous apfloat-based path, which computed at exactly the requested
     * precision and could be wrong in its last place.)
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
            throw new MathArithmeticException(CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO, "acot(x) is undefined for x = 0");
        }

        final MathContext guardContext = withGuardDigits(mathContext);
        final BigDecimal radians = arcCotangent(argument.toBigDecimal(), guardContext);

        return toAngleResult(radians, mathContext, guardContext, trigonometricMode, locale);
    }

}
