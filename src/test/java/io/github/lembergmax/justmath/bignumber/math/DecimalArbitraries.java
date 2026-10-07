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
import java.math.BigInteger;
import java.math.RoundingMode;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Tuple;

/**
 * Generators for the property tests of the math layer. They favour the inputs that break hand-written
 * decimal arithmetic: long operands, runs of nines that carry through every digit, powers of ten, zero
 * and operands with very different scales.
 */
public final class DecimalArbitraries {

    /**
     * Seed of every property in this package. The build stays deterministic with a fixed seed; change it
     * temporarily to search for new failures. A failing property prints its shrunk sample and the seed.
     */
    public static final String SEED = "20261006";

    private static final int SMALL_DIGITS = 4;

    private static final int SMALL_SCALE = 3;

    private static final int SHORT_DIGITS = 40;

    private static final int SHORT_SCALE = 25;

    private static final int LONG_DIGITS = 250;

    private static final int LONG_SCALE = 60;

    private static final int RUN_OF_NINES = 120;

    private static final int MAX_POWER_OF_TEN = 80;

    private DecimalArbitraries() {
    }

    public static Arbitrary<BigDecimal> decimals() {
        return Arbitraries.frequencyOf(
                Tuple.of(6, randomDecimals(SHORT_DIGITS, SHORT_SCALE)),
                Tuple.of(2, randomDecimals(LONG_DIGITS, LONG_SCALE)),
                Tuple.of(1, runsOfNines()),
                Tuple.of(1, powersOfTen()));
    }

    public static Arbitrary<BigDecimal> nonZeroDecimals() {
        return decimals().filter(value -> value.signum() != 0);
    }

    public static Arbitrary<BigDecimal> smallDecimals() {
        return randomDecimals(SMALL_DIGITS, SMALL_SCALE);
    }

    public static Arbitrary<BigDecimal> nonZeroSmallDecimals() {
        return smallDecimals().filter(value -> value.signum() != 0);
    }

    public static Arbitrary<BigDecimal> positiveSmallDecimals() {
        return randomDecimals(8, 4).filter(value -> value.signum() > 0);
    }

    public static Arbitrary<BigDecimal> fractionalExponents() {
        return Combinators.combine(
                        Arbitraries.integers().between(-5, 5),
                        Arbitraries.integers().between(1, 99))
                .as((whole, hundredths) -> BigDecimal.valueOf(whole).add(BigDecimal.valueOf(hundredths, 2)));
    }

    public static Arbitrary<RoundingMode> roundingModes() {
        return Arbitraries.of(RoundingMode.HALF_UP, RoundingMode.HALF_EVEN, RoundingMode.HALF_DOWN,
                RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR);
    }

    private static Arbitrary<BigDecimal> randomDecimals(final int maxDigits, final int maxScale) {
        final Arbitrary<BigInteger> magnitude = Arbitraries.strings().withCharRange('0', '9')
                .ofMinLength(1).ofMaxLength(maxDigits).map(BigInteger::new);
        return Combinators.combine(magnitude, Arbitraries.of(true, false), Arbitraries.integers().between(0, maxScale))
                .as((digits, negative, scale) -> new BigDecimal(negative ? digits.negate() : digits, scale));
    }

    private static Arbitrary<BigDecimal> runsOfNines() {
        return Combinators.combine(
                        Arbitraries.integers().between(1, RUN_OF_NINES),
                        Arbitraries.integers().between(0, RUN_OF_NINES),
                        Arbitraries.of(true, false))
                .as((length, scale, negative) -> {
                    final BigInteger nines = new BigInteger("9".repeat(length));
                    return new BigDecimal(negative ? nines.negate() : nines, Math.min(scale, length + 5));
                });
    }

    private static Arbitrary<BigDecimal> powersOfTen() {
        return Combinators.combine(
                        Arbitraries.integers().between(0, MAX_POWER_OF_TEN),
                        Arbitraries.integers().between(0, MAX_POWER_OF_TEN),
                        Arbitraries.of(true, false))
                .as((exponent, scale, negative) -> {
                    final BigInteger power = BigInteger.TEN.pow(exponent);
                    return new BigDecimal(negative ? power.negate() : power, scale);
                });
    }

}
