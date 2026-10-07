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

package io.github.lembergmax.justmath.bignumber.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

/**
 * The arc tangent, the arc cotangent and atan2 are close to ±π/2 for a large argument or a large ratio. The distance
 * from π/2 is about {@code 1/|x|}, so a computation that treats a large argument as infinite loses the digits that
 * the distance decides. Every case must be within one unit in the last place, in radians and in degrees.
 */
class ArcTangentNearHalfPiTest {

    private static final int REFERENCE_GUARD_DIGITS = 250;

    private static final double MAX_ULPS = 1.0;

    private static final List<Integer> PRECISIONS = List.of(20, 31, 52);

    private static final List<RoundingMode> ROUNDING_MODES = List.of(RoundingMode.HALF_UP, RoundingMode.HALF_EVEN, RoundingMode.DOWN, RoundingMode.CEILING);

    private static final BigDecimal DEGREES_PER_HALF_TURN = BigDecimal.valueOf(180);

    private record Case(String name, BigDecimal first, BigDecimal second, TrigonometricMode mode, Kind kind) {
    }

    private enum Kind {
        ATAN, ACOT, ATAN2
    }

    static Stream<Arguments> cases() {
        final List<Case> cases = List.of(
                new Case("atan(1E+18)", new BigDecimal("1E+18"), null, TrigonometricMode.RAD, Kind.ATAN),
                new Case("atan(-1E+18)", new BigDecimal("-1E+18"), null, TrigonometricMode.RAD, Kind.ATAN),
                new Case("atan(1E+30)", new BigDecimal("1E+30"), null, TrigonometricMode.RAD, Kind.ATAN),
                new Case("atan(1E+18) in degrees", new BigDecimal("1E+18"), null, TrigonometricMode.DEG, Kind.ATAN),
                new Case("acot(-1E-18)", new BigDecimal("-1E-18"), null, TrigonometricMode.RAD, Kind.ACOT),
                new Case("acot(8.63387E-22)", new BigDecimal("8.63387E-22"), null, TrigonometricMode.RAD, Kind.ACOT),
                new Case("acot(1E-30)", new BigDecimal("1E-30"), null, TrigonometricMode.RAD, Kind.ACOT),
                new Case("acot(-1E-18) in degrees", new BigDecimal("-1E-18"), null, TrigonometricMode.DEG, Kind.ACOT),
                new Case("atan2(1E-8, -1E-28)", new BigDecimal("1E-8"), new BigDecimal("-1E-28"), TrigonometricMode.RAD, Kind.ATAN2),
                new Case("atan2(-11.32730073, -2.28E-37)", new BigDecimal("-11.32730073"), new BigDecimal("-2.28E-37"), TrigonometricMode.RAD, Kind.ATAN2),
                new Case("atan2(5, 1E-25)", new BigDecimal("5"), new BigDecimal("1E-25"), TrigonometricMode.RAD, Kind.ATAN2),
                new Case("atan2(-5, 1E-25)", new BigDecimal("-5"), new BigDecimal("1E-25"), TrigonometricMode.RAD, Kind.ATAN2));

        return cases.stream().flatMap(testCase -> PRECISIONS.stream().flatMap(precision -> ROUNDING_MODES.stream()
                .map(mode -> Arguments.of(testCase.name(), testCase, new MathContext(precision, mode)))));
    }

    private static BigDecimal reference(final Case testCase, final MathContext referenceContext) {
        final BigDecimal radians = switch (testCase.kind()) {
            case ATAN -> BigDecimalMath.atan(testCase.first(), referenceContext);
            case ACOT -> BigDecimalMath.atan(BigDecimal.ONE.divide(testCase.first(), referenceContext), referenceContext);
            case ATAN2 -> BigDecimalMath.atan2(testCase.first(), testCase.second(), referenceContext);
        };
        if (testCase.mode() == TrigonometricMode.DEG) {
            return radians.multiply(DEGREES_PER_HALF_TURN, referenceContext).divide(BigDecimalMath.pi(referenceContext), referenceContext);
        }
        return radians;
    }

    private static BigNumber actual(final Case testCase, final MathContext mathContext) {
        final BigNumber first = new BigNumber(testCase.first().toPlainString());
        final BiFunction<BigNumber, MathContext, BigNumber> function = switch (testCase.kind()) {
            case ATAN -> (value, context) -> value.atan(context, testCase.mode());
            case ACOT -> (value, context) -> value.acot(context, testCase.mode());
            case ATAN2 -> (value, context) -> value.atan2(new BigNumber(testCase.second().toPlainString()), context);
        };
        return function.apply(first, mathContext);
    }

    @ParameterizedTest(name = "{0} with {2}")
    @MethodSource("cases")
    @DisplayName("an arc tangent close to a quarter turn keeps every requested digit")
    void arcTangentCloseToAQuarterTurnKeepsEveryRequestedDigit(final String name, final Case testCase, final MathContext mathContext) {
        final MathContext referenceContext = new MathContext(mathContext.getPrecision() + REFERENCE_GUARD_DIGITS, RoundingMode.HALF_EVEN);
        final BigDecimal expected = reference(testCase, referenceContext).round(mathContext);

        AccuracyAssertions.assertWithinUlps(expected, actual(testCase, mathContext).toBigDecimal(), mathContext.getPrecision(), MAX_ULPS, name + " with " + mathContext);
    }
}
