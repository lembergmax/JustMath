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

import static io.github.lembergmax.justmath.bignumber.math.DecimalArbitraries.SEED;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.math.AccuracyAssertions;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tuple;
import net.jqwik.api.constraints.IntRange;

/**
 * Compares the elementary functions of {@link BigNumber} with {@link BigDecimalMath} on generated arguments,
 * including the arguments that are hard: values near zero, near a multiple of pi, and near the edge of a domain.
 * A result must be within one unit in the last place of the requested precision, measured relative to the size
 * of the result itself. The reference is computed with 200 more digits than requested, which is enough to
 * resolve a result that has lost up to 45 digits to cancellation.
 */
class BigNumberFunctionPropertyTest {

    private static final int TRIES = 150;

    private static final int REFERENCE_GUARD_DIGITS = 200;

    private static final int MIN_PRECISION = 5;

    private static final int MAX_PRECISION = 60;

    private static final double MAX_ULPS = 1.0;

    private static final Duration CALL_TIMEOUT = Duration.ofSeconds(10);

    private static final BigDecimal DEGREES_PER_HALF_TURN = BigDecimal.valueOf(180);

    private static final BigDecimal QUARTER_TURN_DEGREES = BigDecimal.valueOf(90);

    private static final int MAX_TINY_EXPONENT = 60;

    private static final int MAX_MULTIPLE_OF_PI = 300;

    private static final int MIN_DECIMALS_OF_PI_MULTIPLE = 3;

    private static final int MAX_DECIMALS_OF_PI_MULTIPLE = 45;

    @Provide
    Arbitrary<BigDecimal> angles() {
        return Arbitraries.frequencyOf(
                Tuple.of(4, decimalsBetween(-1000, 1000, 6)),
                Tuple.of(2, nearMultiplesOfPi()),
                Tuple.of(1, tinyValues()));
    }

    @Provide
    Arbitrary<BigDecimal> tangentArguments() {
        return Arbitraries.frequencyOf(
                Tuple.of(4, decimalsBetween(-1.5, 1.5, 12)),
                Tuple.of(1, tinyValues()),
                Tuple.of(1, nearMultiplesOfPi()));
    }

    @Provide
    Arbitrary<BigDecimal> unitIntervalValues() {
        return Arbitraries.frequencyOf(
                Tuple.of(3, decimalsBetween(-1, 1, 15)),
                Tuple.of(2, nearOne(true)),
                Tuple.of(1, tinyValues()));
    }

    @Provide
    Arbitrary<BigDecimal> hyperbolicArguments() {
        return Arbitraries.frequencyOf(
                Tuple.of(3, decimalsBetween(-50, 50, 10)),
                Tuple.of(1, tinyValues()));
    }

    @Provide
    Arbitrary<BigDecimal> realValues() {
        return Arbitraries.frequencyOf(
                Tuple.of(3, decimalsBetween(-100000, 100000, 8)),
                Tuple.of(1, tinyValues()));
    }

    @Provide
    Arbitrary<BigDecimal> positiveValues() {
        return Arbitraries.frequencyOf(
                Tuple.of(3, decimalsBetween(0.000001, 1000000, 10)),
                Tuple.of(2, nearOneOnEitherSide()),
                Tuple.of(1, Arbitraries.integers().between(-MAX_TINY_EXPONENT, MAX_TINY_EXPONENT)
                        .map(exponent -> BigDecimal.ONE.scaleByPowerOfTen(exponent))));
    }

    @Provide
    Arbitrary<Integer> rootIndexes() {
        return Arbitraries.integers().between(2, 12);
    }

    private static Arbitrary<BigDecimal> decimalsBetween(final double min, final double max, final int scale) {
        return Arbitraries.bigDecimals().between(BigDecimal.valueOf(min), BigDecimal.valueOf(max)).ofScale(scale);
    }

    private static Arbitrary<BigDecimal> tinyValues() {
        return Combinators.combine(
                        Arbitraries.integers().between(1, 999_999),
                        Arbitraries.integers().between(1, MAX_TINY_EXPONENT),
                        Arbitraries.of(true, false))
                .as((mantissa, exponent, negative) -> new BigDecimal(BigInteger.valueOf(negative ? -mantissa : mantissa), exponent + 6));
    }

    private static Arbitrary<BigDecimal> nearMultiplesOfPi() {
        final MathContext piContext = new MathContext(MAX_DECIMALS_OF_PI_MULTIPLE + 10);
        return Combinators.combine(
                        Arbitraries.integers().between(-MAX_MULTIPLE_OF_PI, MAX_MULTIPLE_OF_PI),
                        Arbitraries.integers().between(MIN_DECIMALS_OF_PI_MULTIPLE, MAX_DECIMALS_OF_PI_MULTIPLE))
                .as((multiple, decimals) -> BigDecimalMath.pi(piContext).multiply(BigDecimal.valueOf(multiple)).setScale(decimals, RoundingMode.HALF_UP));
    }

    private static Arbitrary<BigDecimal> offsetsFromOne() {
        return Combinators.combine(
                        Arbitraries.integers().between(1, 9_999),
                        Arbitraries.integers().between(5, 40))
                .as((mantissa, exponent) -> new BigDecimal(BigInteger.valueOf(mantissa), exponent));
    }

    private static Arbitrary<BigDecimal> withRandomSign(final Arbitrary<BigDecimal> values) {
        return Combinators.combine(values, Arbitraries.of(true, false))
                .as((value, negative) -> negative ? value.negate() : value);
    }

    private static Arbitrary<BigDecimal> nearOne(final boolean belowOneWithRandomSign) {
        if (belowOneWithRandomSign) {
            return withRandomSign(offsetsFromOne().map(offset -> BigDecimal.ONE.subtract(offset)));
        }
        return offsetsFromOne().map(offset -> BigDecimal.ONE.add(offset));
    }

    private static Arbitrary<BigDecimal> nearOneOnEitherSide() {
        return Combinators.combine(offsetsFromOne(), Arbitraries.of(true, false))
                .as((offset, below) -> below ? BigDecimal.ONE.subtract(offset) : BigDecimal.ONE.add(offset));
    }

    private static BigNumber number(final BigDecimal value) {
        return new BigNumber(value.toPlainString());
    }

    private static MathContext context(final int precision) {
        return new MathContext(precision, RoundingMode.HALF_UP);
    }

    private static MathContext referenceContext(final int precision) {
        return new MathContext(precision + REFERENCE_GUARD_DIGITS);
    }

    private static BigDecimal degreesToRadians(final BigDecimal degrees, final int precision) {
        final MathContext reference = referenceContext(precision);
        return degrees.multiply(BigDecimalMath.pi(reference), reference).divide(DEGREES_PER_HALF_TURN, reference);
    }

    private static void check(
            final BigDecimal argument,
            final int precision,
            final BiFunction<BigNumber, MathContext, BigNumber> function,
            final UnaryOperator<BigDecimal> reference,
            final String name
    ) {
        check(argument, precision, function, reference, name, value -> false);
    }

    private static void check(
            final BigDecimal argument,
            final int precision,
            final BiFunction<BigNumber, MathContext, BigNumber> function,
            final UnaryOperator<BigDecimal> reference,
            final String name,
            final Predicate<BigDecimal> resultIsExactlyZero
    ) {
        final BigNumber actual = assertTimeoutPreemptively(CALL_TIMEOUT, () -> function.apply(number(argument), context(precision)),
                () -> name + "(" + argument.toPlainString() + ") with " + precision + " digits took longer than " + CALL_TIMEOUT);

        final BigDecimal expected = resultIsExactlyZero.test(argument) ? BigDecimal.ZERO : reference.apply(argument).round(context(precision));
        AccuracyAssertions.assertWithinUlps(expected, actual.toBigDecimal(), precision, MAX_ULPS,
                name + "(" + argument.toPlainString() + ") with " + precision + " digits");
    }

    @Property(tries = TRIES, seed = SEED)
    void sinInRadians(@ForAll("angles") final BigDecimal angle, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(angle, precision, (value, mathContext) -> value.sin(mathContext, TrigonometricMode.RAD),
                argument -> BigDecimalMath.sin(argument, referenceContext(precision)), "sin");
    }

    @Property(tries = TRIES, seed = SEED)
    void cosInRadians(@ForAll("angles") final BigDecimal angle, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(angle, precision, (value, mathContext) -> value.cos(mathContext, TrigonometricMode.RAD),
                argument -> BigDecimalMath.cos(argument, referenceContext(precision)), "cos");
    }

    @Property(tries = TRIES, seed = SEED)
    void tanInRadians(@ForAll("tangentArguments") final BigDecimal angle, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(angle, precision, (value, mathContext) -> value.tan(mathContext, TrigonometricMode.RAD),
                argument -> BigDecimalMath.tan(argument, referenceContext(precision)), "tan");
    }

    @Property(tries = TRIES, seed = SEED)
    void sinInDegrees(@ForAll("angles") final BigDecimal angle, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(angle, precision, (value, mathContext) -> value.sin(mathContext, TrigonometricMode.DEG),
                argument -> BigDecimalMath.sin(degreesToRadians(argument, precision), referenceContext(precision)), "sin degrees",
                argument -> argument.remainder(DEGREES_PER_HALF_TURN).signum() == 0);
    }

    @Property(tries = TRIES, seed = SEED)
    void cosInDegrees(@ForAll("angles") final BigDecimal angle, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(angle, precision, (value, mathContext) -> value.cos(mathContext, TrigonometricMode.DEG),
                argument -> BigDecimalMath.cos(degreesToRadians(argument, precision), referenceContext(precision)), "cos degrees",
                argument -> argument.subtract(QUARTER_TURN_DEGREES).remainder(DEGREES_PER_HALF_TURN).signum() == 0);
    }

    @Property(tries = TRIES, seed = SEED)
    void asinInRadians(@ForAll("unitIntervalValues") final BigDecimal value, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(value, precision, (number, mathContext) -> number.asin(mathContext, TrigonometricMode.RAD),
                argument -> BigDecimalMath.asin(argument, referenceContext(precision)), "asin");
    }

    @Property(tries = TRIES, seed = SEED)
    void acosInRadians(@ForAll("unitIntervalValues") final BigDecimal value, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(value, precision, (number, mathContext) -> number.acos(mathContext, TrigonometricMode.RAD),
                argument -> BigDecimalMath.acos(argument, referenceContext(precision)), "acos",
                argument -> argument.compareTo(BigDecimal.ONE) == 0);
    }

    @Property(tries = TRIES, seed = SEED)
    void atanInRadians(@ForAll("realValues") final BigDecimal value, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(value, precision, (number, mathContext) -> number.atan(mathContext, TrigonometricMode.RAD),
                argument -> BigDecimalMath.atan(argument, referenceContext(precision)), "atan");
    }

    @Property(tries = TRIES, seed = SEED)
    void sinh(@ForAll("hyperbolicArguments") final BigDecimal value, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(value, precision, BigNumber::sinh, argument -> BigDecimalMath.sinh(argument, referenceContext(precision)), "sinh");
    }

    @Property(tries = TRIES, seed = SEED)
    void cosh(@ForAll("hyperbolicArguments") final BigDecimal value, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(value, precision, BigNumber::cosh, argument -> BigDecimalMath.cosh(argument, referenceContext(precision)), "cosh");
    }

    @Property(tries = TRIES, seed = SEED)
    void tanh(@ForAll("hyperbolicArguments") final BigDecimal value, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(value, precision, BigNumber::tanh, argument -> BigDecimalMath.tanh(argument, referenceContext(precision)), "tanh");
    }

    @Property(tries = TRIES, seed = SEED)
    void naturalLogarithm(@ForAll("positiveValues") final BigDecimal value, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(value, precision, BigNumber::ln, argument -> BigDecimalMath.log(argument, referenceContext(precision)), "ln");
    }

    @Property(tries = TRIES, seed = SEED)
    void logarithmToBaseTwo(@ForAll("positiveValues") final BigDecimal value, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(value, precision, BigNumber::log2, argument -> BigDecimalMath.log2(argument, referenceContext(precision)), "log2");
    }

    @Property(tries = TRIES, seed = SEED)
    void logarithmToBaseTen(@ForAll("positiveValues") final BigDecimal value, @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision) {
        check(value, precision, BigNumber::log10, argument -> BigDecimalMath.log10(argument, referenceContext(precision)), "log10");
    }

    @Property(tries = TRIES, seed = SEED)
    void nthRoot(
            @ForAll("positiveValues") final BigDecimal value,
            @ForAll("rootIndexes") final int index,
            @ForAll @IntRange(min = MIN_PRECISION, max = MAX_PRECISION) final int precision
    ) {
        check(value, precision, (number, mathContext) -> number.nthRoot(BigNumber.valueOf(index), mathContext),
                argument -> BigDecimalMath.root(argument, BigDecimal.valueOf(index), referenceContext(precision)), index + "th root");
    }

}
