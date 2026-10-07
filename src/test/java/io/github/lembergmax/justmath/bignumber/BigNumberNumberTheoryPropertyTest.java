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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigInteger;
import java.time.Duration;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tuple;
import net.jqwik.api.constraints.IntRange;

/**
 * Compares the integer functions of {@link BigNumber} with {@link BigInteger}: gcd, lcm, modulo, remainder,
 * factorial, combinations and permutations. The results are exact integers, so the comparison is exact, and an
 * operand of any length must give the same result as the reference.
 */
class BigNumberNumberTheoryPropertyTest {

    private static final int TRIES = 200;

    private static final int SHORT_DIGITS = 18;

    private static final int LONG_DIGITS = 120;

    private static final int MAX_FACTORIAL_ARGUMENT = 300;

    private static final int MAX_COMBINATION_N = 400;

    private static final Duration CALL_TIMEOUT = Duration.ofSeconds(10);

    @Provide
    Arbitrary<BigInteger> integers() {
        return Arbitraries.frequencyOf(
                Tuple.of(3, digits(SHORT_DIGITS)),
                Tuple.of(2, digits(LONG_DIGITS)),
                Tuple.of(1, Arbitraries.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.TWO, BigInteger.TEN)));
    }

    @Provide
    Arbitrary<BigInteger> positiveIntegers() {
        return integers().filter(value -> value.signum() > 0);
    }

    @Provide
    Arbitrary<BigInteger> signedIntegers() {
        return Combinators.combine(integers(), Arbitraries.of(true, false))
                .as((value, negative) -> negative ? value.negate() : value);
    }

    private static Arbitrary<BigInteger> digits(final int maxDigits) {
        return Arbitraries.strings().withCharRange('0', '9').ofMinLength(1).ofMaxLength(maxDigits).map(BigInteger::new);
    }

    private static BigNumber number(final BigInteger value) {
        return new BigNumber(value.toString());
    }

    private static <T> T withinTimeout(final java.util.function.Supplier<T> call, final String description) {
        return assertTimeoutPreemptively(CALL_TIMEOUT, call::get, () -> description + " took longer than " + CALL_TIMEOUT);
    }

    @Property(tries = TRIES, seed = SEED)
    void gcdMatchesBigInteger(@ForAll("signedIntegers") final BigInteger a, @ForAll("signedIntegers") final BigInteger b) {
        final BigNumber actual = withinTimeout(() -> number(a).gcd(number(b)), "gcd(" + a + ", " + b + ")");

        assertEquals(a.gcd(b).toString(), actual.toBigDecimal().toBigIntegerExact().toString(), "gcd(" + a + ", " + b + ")");
    }

    @Property(tries = TRIES, seed = SEED)
    void lcmMatchesBigInteger(@ForAll("signedIntegers") final BigInteger a, @ForAll("signedIntegers") final BigInteger b) {
        final BigNumber actual = withinTimeout(() -> number(a).lcm(number(b)), "lcm(" + a + ", " + b + ")");
        final BigInteger expected = a.signum() == 0 || b.signum() == 0 ? BigInteger.ZERO : a.multiply(b).abs().divide(a.gcd(b));

        assertEquals(expected.toString(), actual.toBigDecimal().toBigIntegerExact().toString(), "lcm(" + a + ", " + b + ")");
    }

    @Property(tries = TRIES, seed = SEED)
    void moduloMatchesBigInteger(@ForAll("integers") final BigInteger dividend, @ForAll("positiveIntegers") final BigInteger divisor) {
        final BigNumber actual = withinTimeout(() -> number(dividend).modulo(number(divisor)), dividend + " mod " + divisor);

        assertEquals(dividend.mod(divisor).toString(), actual.toBigDecimal().toBigIntegerExact().toString(), dividend + " mod " + divisor);
    }

    @Property(tries = TRIES, seed = SEED)
    void remainderMatchesBigInteger(@ForAll("signedIntegers") final BigInteger dividend, @ForAll("positiveIntegers") final BigInteger divisor) {
        final BigNumber actual = withinTimeout(() -> number(dividend).remainder(number(divisor)), dividend + " rem " + divisor);

        assertEquals(dividend.remainder(divisor).toString(), actual.toBigDecimal().toBigIntegerExact().toString(), dividend + " rem " + divisor);
    }

    @Property(tries = TRIES, seed = SEED)
    void factorialMatchesTheProduct(@ForAll @IntRange(min = 0, max = MAX_FACTORIAL_ARGUMENT) final int argument) {
        BigInteger expected = BigInteger.ONE;
        for (int factor = 2; factor <= argument; factor++) {
            expected = expected.multiply(BigInteger.valueOf(factor));
        }

        final BigNumber actual = BigNumber.valueOf(argument).factorial();

        assertEquals(expected.toString(), actual.toBigDecimal().toBigIntegerExact().toString(), argument + "!");
    }

    @Property(tries = TRIES, seed = SEED)
    void combinationMatchesTheBinomialCoefficient(
            @ForAll @IntRange(min = 0, max = MAX_COMBINATION_N) final int n,
            @ForAll @IntRange(min = 0, max = MAX_COMBINATION_N) final int rawK
    ) {
        final int k = rawK % (n + 1);
        final BigNumber actual = BigNumber.valueOf(n).combination(BigNumber.valueOf(k));

        assertEquals(binomial(n, k).toString(), actual.toBigDecimal().toBigIntegerExact().toString(), "C(" + n + ", " + k + ")");
    }

    @Property(tries = TRIES, seed = SEED)
    void permutationMatchesTheFallingFactorial(
            @ForAll @IntRange(min = 0, max = MAX_COMBINATION_N) final int n,
            @ForAll @IntRange(min = 0, max = MAX_COMBINATION_N) final int rawK
    ) {
        final int k = rawK % (n + 1);
        final BigNumber actual = BigNumber.valueOf(n).permutation(BigNumber.valueOf(k));

        assertEquals(fallingFactorial(n, k).toString(), actual.toBigDecimal().toBigIntegerExact().toString(), "P(" + n + ", " + k + ")");
    }

    private static BigInteger binomial(final int n, final int k) {
        BigInteger result = BigInteger.ONE;
        for (int index = 1; index <= k; index++) {
            result = result.multiply(BigInteger.valueOf(n - k + index)).divide(BigInteger.valueOf(index));
        }
        return result;
    }

    private static BigInteger fallingFactorial(final int n, final int k) {
        BigInteger result = BigInteger.ONE;
        for (int index = 0; index < k; index++) {
            result = result.multiply(BigInteger.valueOf(n - index));
        }
        return result;
    }

}
