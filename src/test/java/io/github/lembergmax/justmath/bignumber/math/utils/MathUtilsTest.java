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

package io.github.lembergmax.justmath.bignumber.math.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumberCoordinate;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArgumentException;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArithmeticException;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MathUtilsTest {

    private static final int REQUESTED_PRECISION = 5;

    private static final int BASE_GUARD_DIGITS = 10;

    private static final int SAFETY_DIGITS = 5;

    private static final int ZERO_RETRY_FACTOR = 4;

    private static final int ZERO_RETRY_OFFSET = 50;

    private static final MathContext REQUESTED = new MathContext(REQUESTED_PRECISION, RoundingMode.HALF_UP);

    private static final class RecordingFunction implements Function<MathContext, BigDecimal> {

        private final List<MathContext> contexts = new ArrayList<>();

        private final Function<MathContext, BigDecimal> delegate;

        private RecordingFunction(final Function<MathContext, BigDecimal> delegate) {
            this.delegate = delegate;
        }

        @Override
        public BigDecimal apply(final MathContext mathContext) {
            contexts.add(mathContext);
            return delegate.apply(mathContext);
        }

        private List<Integer> precisions() {
            return contexts.stream().map(MathContext::getPrecision).toList();
        }
    }

    @Nested
    @DisplayName("withGuardDigits")
    class WithGuardDigits {

        @Test
        @DisplayName("adds the digits to the precision and keeps the rounding mode of the caller")
        void addsDigitsAndKeepsTheRoundingMode() {
            final MathContext working = MathUtils.withGuardDigits(new MathContext(20, RoundingMode.HALF_EVEN), 7);

            assertEquals(27, working.getPrecision());
            assertEquals(RoundingMode.HALF_EVEN, working.getRoundingMode());
        }

        @Test
        @DisplayName("accepts zero guard digits")
        void acceptsZeroGuardDigits() {
            assertEquals(new MathContext(9, RoundingMode.DOWN), MathUtils.withGuardDigits(new MathContext(9, RoundingMode.DOWN), 0));
        }
    }

    @Nested
    @DisplayName("integerDigitCount")
    class IntegerDigitCount {

        @Test
        @DisplayName("counts the digits left of the decimal point")
        void countsTheIntegerDigits() {
            assertEquals(3, MathUtils.integerDigitCount(new BigDecimal("123.45")));
            assertEquals(2, MathUtils.integerDigitCount(new BigDecimal("-45.6")));
            assertEquals(3, MathUtils.integerDigitCount(new BigDecimal("999")));
            assertEquals(6, MathUtils.integerDigitCount(new BigDecimal("1E+5")));
        }

        @Test
        @DisplayName("is zero for a magnitude below one")
        void isZeroBelowOne() {
            assertEquals(0, MathUtils.integerDigitCount(new BigDecimal("0.5")));
            assertEquals(0, MathUtils.integerDigitCount(new BigDecimal("0.00123")));
            assertEquals(0, MathUtils.integerDigitCount(new BigDecimal("-0.999")));
        }
    }

    @Nested
    @DisplayName("checkMathContext")
    class CheckMathContext {

        @Test
        @DisplayName("accepts the precisions from one up to the limit")
        void acceptsTheSupportedRange() {
            assertDoesNotThrow(() -> MathUtils.checkMathContext(new MathContext(1)));
            assertDoesNotThrow(() -> MathUtils.checkMathContext(new MathContext(MathUtils.MAX_MATH_CONTEXT_PRECISION)));
        }

        @Test
        @DisplayName("rejects an unlimited precision as a domain error")
        void rejectsUnlimitedPrecision() {
            final MathArgumentException failure = assertThrows(MathArgumentException.class, () -> MathUtils.checkMathContext(MathContext.UNLIMITED));

            assertEquals(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, failure.getErrorCode());
        }

        @Test
        @DisplayName("rejects a precision one above the limit as an overflow")
        void rejectsPrecisionAboveTheLimit() {
            final MathContext tooLarge = new MathContext(MathUtils.MAX_MATH_CONTEXT_PRECISION + 1);

            final MathArithmeticException failure = assertThrows(MathArithmeticException.class, () -> MathUtils.checkMathContext(tooLarge));

            assertEquals(CalculatorErrorCode.MATH_OVERFLOW, failure.getErrorCode());
        }
    }

    @Nested
    @DisplayName("computeWithGuardDigits")
    class ComputeWithGuardDigits {

        @Test
        @DisplayName("evaluates a result near one once, with the base guard digits, and rounds it to the request")
        void evaluatesOnceForAResultNearOne() {
            final RecordingFunction function = new RecordingFunction(context -> new BigDecimal("1.23456789"));

            final BigDecimal result = MathUtils.computeWithGuardDigits(REQUESTED, BASE_GUARD_DIGITS, function);

            assertEquals(new BigDecimal("1.2346"), result);
            assertEquals(List.of(REQUESTED_PRECISION + BASE_GUARD_DIGITS), function.precisions());
        }

        @Test
        @DisplayName("passes the rounding mode of the caller to the function and rounds with it")
        void passesTheRoundingModeOfTheCaller() {
            final MathContext halfEven = new MathContext(1, RoundingMode.HALF_EVEN);
            final MathContext halfUp = new MathContext(1, RoundingMode.HALF_UP);
            final RecordingFunction function = new RecordingFunction(context -> new BigDecimal("2.5"));

            assertEquals(new BigDecimal("2"), MathUtils.computeWithGuardDigits(halfEven, BASE_GUARD_DIGITS, function));
            assertEquals(new BigDecimal("3"), MathUtils.computeWithGuardDigits(halfUp, BASE_GUARD_DIGITS, function));
            assertEquals(RoundingMode.HALF_EVEN, function.contexts.getFirst().getRoundingMode());
            assertEquals(RoundingMode.HALF_UP, function.contexts.getLast().getRoundingMode());
        }

        @Test
        @DisplayName("repeats with the digits that a result close to zero lost")
        void repeatsForAResultCloseToZero() {
            final int exponent = -30;
            final RecordingFunction function = new RecordingFunction(context -> BigDecimal.ONE.scaleByPowerOfTen(exponent));

            final BigDecimal result = MathUtils.computeWithGuardDigits(REQUESTED, BASE_GUARD_DIGITS, function);

            final int firstPrecision = REQUESTED_PRECISION + BASE_GUARD_DIGITS;
            final int secondPrecision = REQUESTED_PRECISION + Math.abs(exponent) + SAFETY_DIGITS + BASE_GUARD_DIGITS;
            assertEquals(List.of(firstPrecision, secondPrecision), function.precisions());
            assertEquals(0, BigDecimal.ONE.scaleByPowerOfTen(exponent).compareTo(result));
        }

        @Test
        @DisplayName("repeats with the digits that a huge result lost")
        void repeatsForAHugeResult() {
            final int exponent = 30;
            final RecordingFunction function = new RecordingFunction(context -> BigDecimal.ONE.scaleByPowerOfTen(exponent));

            MathUtils.computeWithGuardDigits(REQUESTED, BASE_GUARD_DIGITS, function);

            assertEquals(
                    List.of(REQUESTED_PRECISION + BASE_GUARD_DIGITS, REQUESTED_PRECISION + exponent + SAFETY_DIGITS + BASE_GUARD_DIGITS),
                    function.precisions());
        }

        @Test
        @DisplayName("accepts a result whose lost digits equal the guard digits plus nothing more")
        void acceptsAResultThatLostExactlyTheSafetyMargin() {
            final int exponent = -(BASE_GUARD_DIGITS - SAFETY_DIGITS);
            final RecordingFunction function = new RecordingFunction(context -> BigDecimal.ONE.scaleByPowerOfTen(exponent));

            MathUtils.computeWithGuardDigits(REQUESTED, BASE_GUARD_DIGITS, function);

            assertEquals(1, function.contexts.size());
        }

        @Test
        @DisplayName("evaluates again when the result lost one digit more than the guard covers")
        void repeatsWhenTheGuardIsOneDigitShort() {
            final int exponent = -(BASE_GUARD_DIGITS - SAFETY_DIGITS + 1);
            final RecordingFunction function = new RecordingFunction(context -> BigDecimal.ONE.scaleByPowerOfTen(exponent));

            MathUtils.computeWithGuardDigits(REQUESTED, BASE_GUARD_DIGITS, function);

            assertEquals(2, function.contexts.size());
        }

        @Test
        @DisplayName("trusts a zero only after three more evaluations with growing guard digits")
        void retriesAZeroResult() {
            final RecordingFunction function = new RecordingFunction(context -> BigDecimal.ZERO);
            final int baseGuard = 2;

            final BigDecimal result = MathUtils.computeWithGuardDigits(REQUESTED, baseGuard, function);

            final int secondGuard = baseGuard * ZERO_RETRY_FACTOR + ZERO_RETRY_OFFSET;
            final int thirdGuard = secondGuard * ZERO_RETRY_FACTOR + ZERO_RETRY_OFFSET;
            final int fourthGuard = thirdGuard * ZERO_RETRY_FACTOR + ZERO_RETRY_OFFSET;
            assertEquals(0, result.signum());
            assertEquals(
                    List.of(REQUESTED_PRECISION + baseGuard, REQUESTED_PRECISION + secondGuard, REQUESTED_PRECISION + thirdGuard, REQUESTED_PRECISION + fourthGuard),
                    function.precisions());
        }

        @Test
        @DisplayName("returns a non-zero result that appears after a zero")
        void returnsTheResultAfterAZero() {
            final RecordingFunction function = new RecordingFunction(context -> context.getPrecision() > REQUESTED_PRECISION + BASE_GUARD_DIGITS
                    ? new BigDecimal("0.5")
                    : BigDecimal.ZERO);

            final BigDecimal result = MathUtils.computeWithGuardDigits(REQUESTED, BASE_GUARD_DIGITS, function);

            assertEquals(new BigDecimal("0.5"), result);
            assertEquals(2, function.contexts.size());
        }

        @Test
        @DisplayName("gives up with MATH_OVERFLOW after eight attempts when the result never stabilizes")
        void givesUpWhenTheResultDoesNotStabilize() {
            final RecordingFunction function = new RecordingFunction(context -> BigDecimal.ONE.scaleByPowerOfTen(-2 * context.getPrecision()));

            final MathArithmeticException failure = assertThrows(
                    MathArithmeticException.class,
                    () -> MathUtils.computeWithGuardDigits(REQUESTED, BASE_GUARD_DIGITS, function));

            assertEquals(CalculatorErrorCode.MATH_OVERFLOW, failure.getErrorCode());
            assertEquals(8, function.contexts.size());
        }

        @Test
        @DisplayName("rejects a working precision above the limit before it calls the function")
        void rejectsAWorkingPrecisionAboveTheLimit() {
            final RecordingFunction function = new RecordingFunction(context -> BigDecimal.ONE);
            final MathContext nearTheLimit = new MathContext(MathUtils.MAX_MATH_CONTEXT_PRECISION - 5);

            final MathArithmeticException failure = assertThrows(
                    MathArithmeticException.class,
                    () -> MathUtils.computeWithGuardDigits(nearTheLimit, BASE_GUARD_DIGITS, function));

            assertEquals(CalculatorErrorCode.MATH_OVERFLOW, failure.getErrorCode());
            assertEquals(0, function.contexts.size());
        }
    }

    @Nested
    @DisplayName("angle conversion")
    class AngleConversion {

        private final MathContext thirtyDigits = new MathContext(30, RoundingMode.HALF_UP);

        @Test
        @DisplayName("convertAngle() turns degrees into radians in DEG mode")
        void convertsDegreesToRadians() {
            final BigDecimal radians = MathUtils.convertAngle(new BigNumber("180"), thirtyDigits, TrigonometricMode.DEG, Locale.US);

            assertEquals(0, BigDecimalMath.pi(thirtyDigits).compareTo(radians));
        }

        @Test
        @DisplayName("convertAngle() returns the angle unchanged in RAD mode")
        void keepsRadians() {
            final BigDecimal radians = MathUtils.convertAngle(new BigNumber("1.5"), thirtyDigits, TrigonometricMode.RAD, Locale.US);

            assertEquals(0, new BigDecimal("1.5").compareTo(radians));
        }

        @Test
        @DisplayName("bigDecimalNumberToRadians() and bigDecimalRadiansToDegrees() undo each other")
        void conversionsAreInverse() {
            final BigDecimal degrees = new BigDecimal("45");

            final BigDecimal radians = MathUtils.bigDecimalNumberToRadians(degrees, thirtyDigits, Locale.US);
            final BigDecimal back = MathUtils.bigDecimalRadiansToDegrees(radians, thirtyDigits, Locale.US);

            assertEquals(0, BigDecimalMath.pi(thirtyDigits).divide(new BigDecimal("4"), thirtyDigits).compareTo(radians));
            assertEquals(0, degrees.compareTo(back.round(new MathContext(20))));
        }
    }

    @Nested
    @DisplayName("ensureScalar")
    class EnsureScalar {

        @Test
        @DisplayName("returns a plain BigNumber unchanged")
        void returnsAScalar() {
            final BigNumber scalar = new BigNumber("7");

            assertSame(scalar, MathUtils.ensureScalar(scalar));
        }

        @Test
        @DisplayName("reduces a multi-value result to its first value")
        void reducesACoordinate() {
            final BigNumberCoordinate coordinate = new BigNumberCoordinate(new BigNumber("3"), new BigNumber("4"));

            final BigNumber scalar = MathUtils.ensureScalar(coordinate);

            assertEquals(new BigNumber("3"), scalar);
            assertNotSame(coordinate, scalar);
        }

        @Test
        @DisplayName("rejects anything that is not a number")
        void rejectsOtherTypes() {
            assertThrows(IllegalArgumentException.class, () -> MathUtils.ensureScalar("7"));
            assertThrows(IllegalArgumentException.class, () -> MathUtils.ensureScalar(null));
        }
    }
}
