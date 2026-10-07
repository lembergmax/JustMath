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

package io.github.lembergmax.justmath.bignumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.math.AccuracyAssertions;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Pins the elementary functions of {@link BigNumber} at arguments where the result is much smaller than the
 * intermediate values, so that a computation with a fixed number of digits loses the result to cancellation, and
 * where a computation took minutes or never finished. Every case runs with several precisions and rounding modes and
 * must be within one unit in the last place of the requested precision, measured relative to the result itself.
 */
class BigNumberIllConditionedArgumentsTest {

    private static final int REFERENCE_GUARD_DIGITS = 200;

    private static final double MAX_ULPS = 1.0;

    private static final Duration CALL_TIMEOUT = Duration.ofSeconds(10);

    private static final List<Integer> PRECISIONS = List.of(20, 50, 100);

    private static final List<RoundingMode> ROUNDING_MODES = List.of(RoundingMode.HALF_UP, RoundingMode.HALF_EVEN, RoundingMode.DOWN, RoundingMode.CEILING);

    private static final BigDecimal DEGREES_PER_HALF_TURN = BigDecimal.valueOf(180);

    private static final String PI_TO_35_DECIMALS = "3.14159265358979323846264338327950288";

    private static final String HALF_PI_TO_35_DECIMALS = "1.57079632679489661923132169163975144";

    private static final String ONE_PLUS_1E_MINUS_28 = "1.0000000000000000000000000001";

    private static final String ONE_PLUS_1E_MINUS_19 = "1.0000000000000000001";

    private static final String ONE_MINUS_1E_MINUS_20 = "0.99999999999999999999";

    private static final String ONE_E_MINUS_10 = "0.0000000001";

    private static final String ONE_E_MINUS_30 = "0.000000000000000000000000000001";

    private static final String ONE_E_MINUS_40 = "0.0000000000000000000000000000000000000001";

    private static final String ONE_E_MINUS_50 = "0.00000000000000000000000000000000000000000000000001";

    private static final String ONE_E_40 = "10000000000000000000000000000000000000000";

    private static final String ONE_E_30 = "1000000000000000000000000000000";

    private record Case(
            String name,
            String argument,
            BiFunction<BigNumber, MathContext, BigNumber> function,
            BiFunction<BigDecimal, MathContext, BigDecimal> reference
    ) {
    }

    static Stream<Arguments> accuracyCases() {
        final List<Case> cases = List.of(
                new Case("sin(pi to 35 decimals)", PI_TO_35_DECIMALS, (value, context) -> value.sin(context, TrigonometricMode.RAD), BigDecimalMath::sin),
                new Case("cos(pi/2 to 35 decimals)", HALF_PI_TO_35_DECIMALS, (value, context) -> value.cos(context, TrigonometricMode.RAD), BigDecimalMath::cos),
                new Case("tan(pi to 35 decimals)", PI_TO_35_DECIMALS, (value, context) -> value.tan(context, TrigonometricMode.RAD), BigDecimalMath::tan),
                new Case("sin(180.0000000000000000000000000001 degrees)", "180.0000000000000000000000000001",
                        (value, context) -> value.sin(context, TrigonometricMode.DEG), (value, context) -> BigDecimalMath.sin(degreesToRadians(value, context), context)),
                new Case("cos(90.0000000000000000000000000001 degrees)", "90.0000000000000000000000000001",
                        (value, context) -> value.cos(context, TrigonometricMode.DEG), (value, context) -> BigDecimalMath.cos(degreesToRadians(value, context), context)),
                new Case("asin(1e-50)", ONE_E_MINUS_50, (value, context) -> value.asin(context, TrigonometricMode.RAD), BigDecimalMath::asin),
                new Case("acos(1 - 1e-20)", ONE_MINUS_1E_MINUS_20, (value, context) -> value.acos(context, TrigonometricMode.RAD), BigDecimalMath::acos),
                new Case("ln(1 + 1e-28)", ONE_PLUS_1E_MINUS_28, BigNumber::ln, BigDecimalMath::log),
                new Case("log2(1 + 1e-28)", ONE_PLUS_1E_MINUS_28, BigNumber::log2, BigDecimalMath::log2),
                new Case("log10(1 + 1e-28)", ONE_PLUS_1E_MINUS_28, BigNumber::log10, BigDecimalMath::log10),
                new Case("sinh(1e-10)", ONE_E_MINUS_10, BigNumber::sinh, BigDecimalMath::sinh),
                new Case("sinh(1e-40)", ONE_E_MINUS_40, BigNumber::sinh, BigDecimalMath::sinh),
                new Case("tanh(1e-10)", ONE_E_MINUS_10, BigNumber::tanh, BigDecimalMath::tanh),
                new Case("tanh(1e-40)", ONE_E_MINUS_40, BigNumber::tanh, BigDecimalMath::tanh),
                new Case("asinh(1e-50)", ONE_E_MINUS_50, BigNumber::asinh, BigDecimalMath::asinh),
                new Case("asinh(1e40)", ONE_E_40, BigNumber::asinh, BigDecimalMath::asinh),
                new Case("acosh(1 + 1e-19)", ONE_PLUS_1E_MINUS_19, BigNumber::acosh, BigDecimalMath::acosh),
                new Case("atanh(1e-30)", ONE_E_MINUS_30, BigNumber::atanh, BigDecimalMath::atanh),
                new Case("atanh(1 - 1e-20)", ONE_MINUS_1E_MINUS_20, BigNumber::atanh, BigDecimalMath::atanh),
                new Case("acoth(1e30)", ONE_E_30, BigNumber::acoth, BigDecimalMath::acoth),
                new Case("acoth(1 + 1e-19)", ONE_PLUS_1E_MINUS_19, BigNumber::acoth, BigDecimalMath::acoth));

        return cases.stream().flatMap(testCase -> PRECISIONS.stream().flatMap(precision -> ROUNDING_MODES.stream()
                .map(mode -> Arguments.of(testCase.name(), testCase, new MathContext(precision, mode)))));
    }

    @ParameterizedTest(name = "{0} with {2}")
    @MethodSource("accuracyCases")
    @DisplayName("an ill-conditioned argument keeps every requested digit of the result")
    void illConditionedArgumentKeepsEveryRequestedDigit(final String name, final Case testCase, final MathContext mathContext) {
        final BigNumber actual = assertTimeoutPreemptively(CALL_TIMEOUT,
                () -> testCase.function().apply(new BigNumber(testCase.argument()), mathContext),
                () -> name + " with " + mathContext + " took longer than " + CALL_TIMEOUT);

        final MathContext referenceContext = new MathContext(mathContext.getPrecision() + REFERENCE_GUARD_DIGITS, RoundingMode.HALF_EVEN);
        final BigDecimal expected = testCase.reference().apply(new BigDecimal(testCase.argument()), referenceContext).round(mathContext);

        AccuracyAssertions.assertWithinUlps(expected, actual.toBigDecimal(), mathContext.getPrecision(), MAX_ULPS, name + " with " + mathContext);
    }

    static Stream<Arguments> exactZeroCases() {
        final List<Arguments> cases = List.of(
                Arguments.of("sin(180 degrees)", "180", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.sin(context, TrigonometricMode.DEG)),
                Arguments.of("sin(360 degrees)", "360", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.sin(context, TrigonometricMode.DEG)),
                Arguments.of("sin(-540 degrees)", "-540", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.sin(context, TrigonometricMode.DEG)),
                Arguments.of("cos(90 degrees)", "90", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.cos(context, TrigonometricMode.DEG)),
                Arguments.of("cos(270 degrees)", "270", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.cos(context, TrigonometricMode.DEG)),
                Arguments.of("cos(-450 degrees)", "-450", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.cos(context, TrigonometricMode.DEG)),
                Arguments.of("tan(180 degrees)", "180", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.tan(context, TrigonometricMode.DEG)),
                Arguments.of("cot(90 degrees)", "90", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.cot(context, TrigonometricMode.DEG)),
                Arguments.of("sin(0 radians)", "0", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.sin(context, TrigonometricMode.RAD)),
                Arguments.of("asin(0)", "0", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.asin(context, TrigonometricMode.RAD)),
                Arguments.of("acos(1)", "1", (BiFunction<BigNumber, MathContext, BigNumber>) (value, context) -> value.acos(context, TrigonometricMode.RAD)),
                Arguments.of("ln(1)", "1", (BiFunction<BigNumber, MathContext, BigNumber>) BigNumber::ln),
                Arguments.of("log10(1)", "1", (BiFunction<BigNumber, MathContext, BigNumber>) BigNumber::log10),
                Arguments.of("asinh(0)", "0", (BiFunction<BigNumber, MathContext, BigNumber>) BigNumber::asinh),
                Arguments.of("acosh(1)", "1", (BiFunction<BigNumber, MathContext, BigNumber>) BigNumber::acosh),
                Arguments.of("atanh(0)", "0", (BiFunction<BigNumber, MathContext, BigNumber>) BigNumber::atanh));

        return cases.stream().flatMap(entry -> ROUNDING_MODES.stream().map(mode -> Arguments.of(entry.get()[0], entry.get()[1], entry.get()[2], mode)));
    }

    @ParameterizedTest(name = "{0} is exactly zero with {3}")
    @MethodSource("exactZeroCases")
    @DisplayName("a function value that is exactly zero is returned as zero, not as rounding noise")
    void exactZeroIsReturnedAsZero(
            final String name,
            final String argument,
            final BiFunction<BigNumber, MathContext, BigNumber> function,
            final RoundingMode mode
    ) {
        final BigNumber actual = function.apply(new BigNumber(argument), new MathContext(50, mode));

        assertEquals(0, actual.toBigDecimal().signum(), () -> name + " was " + actual.toBigDecimal().toPlainString());
    }

    @ParameterizedTest(name = "atan2(1e-30, 1) with {0}")
    @MethodSource("atan2Contexts")
    @DisplayName("atan2 of a point close to the x-axis keeps every requested digit")
    void atan2NearTheXAxisKeepsEveryRequestedDigit(final MathContext mathContext) {
        final BigNumber actual = assertTimeoutPreemptively(CALL_TIMEOUT,
                () -> new BigNumber(ONE_E_MINUS_30).atan2(new BigNumber("1"), mathContext));

        final MathContext referenceContext = new MathContext(mathContext.getPrecision() + REFERENCE_GUARD_DIGITS, RoundingMode.HALF_EVEN);
        final BigDecimal expected = BigDecimalMath.atan2(new BigDecimal(ONE_E_MINUS_30), BigDecimal.ONE, referenceContext).round(mathContext);

        AccuracyAssertions.assertWithinUlps(expected, actual.toBigDecimal(), mathContext.getPrecision(), MAX_ULPS, "atan2(1e-30, 1) with " + mathContext);
    }

    static Stream<MathContext> atan2Contexts() {
        return PRECISIONS.stream().flatMap(precision -> ROUNDING_MODES.stream().map(mode -> new MathContext(precision, mode)));
    }

    private static BigDecimal degreesToRadians(final BigDecimal degrees, final MathContext mathContext) {
        return degrees.multiply(BigDecimalMath.pi(mathContext), mathContext).divide(DEGREES_PER_HALF_TURN, mathContext);
    }

}
