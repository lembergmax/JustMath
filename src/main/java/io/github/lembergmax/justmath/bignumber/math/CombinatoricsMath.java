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

import static io.github.lembergmax.justmath.bignumber.BigNumbers.ZERO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.math.utils.MathUtils;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Provides combinatorial mathematical operations on {@link BigNumber} instances,
 * specifically computing combinations and permutations with high precision.
 * <p>
 * This class enforces integer inputs and validates arguments to ensure
 * mathematically correct results for combinatorics.
 */
@UtilityClass
public final class CombinatoricsMath {

    /**
     * Upper bound on the number of descending factors {@code P(n, k)} / {@code C(n, k)} may multiply.
     * The multiplicative loop is a bounded partial factorial, so it is capped in the same range a raw
     * factorial is (mirrors {@code BasicMath}'s factorial-argument cap); without it an expression such as
     * {@code 999999999 nPr 999999999} reachable from untrusted input would run for minutes and exhaust
     * the heap.
     */
    private static final int MAX_COMBINATORIAL_FACTORS = 100_000;

    /**
     * Upper bound on the number of decimal digits a {@code P(n, k)} / {@code C(n, k)} result may have.
     * The digit count grows like {@code factorCount · log10(n)}; the estimate is checked cheaply before
     * the expensive product and rejected if it exceeds this bound.
     */
    private static final long MAX_COMBINATORIAL_RESULT_DIGITS = 1_000_000L;

    /**
     * Calculates the number of combinations (n choose k), denoted as C(n, k),
     * which is the count of ways to choose {@code k} items from {@code n} items without regard to order.
     * <p>
     * The formula used is:
     * <pre>
     *     C(n, k) = n! / (k! * (n-k)!)
     * </pre>
     * but computed efficiently via a multiplicative approach to avoid intermediate factorial computation.
     *
     * <p>The binomial coefficient is an exact integer; this method computes it with exact {@link BigInteger}
     * arithmetic (each running product is divisible by the next index), so the result is never rounded to
     * {@code mathContext} precision. {@code mathContext} is still validated for API consistency but does not
     * affect the (exact) result.
     *
     * @param n
     * 	the total number of items (must be a non-negative integer)
     * @param k
     * 	the number of items to choose (must be a non-negative integer, k ≤ n)
     * @param mathContext
     * 	the {@link MathContext}; validated for positive precision but not applied (the result is exact)
     *
     * @return the number of combinations C(n, k) as an exact {@link BigNumber}
     *
     * @throws IllegalArgumentException
     * 	if {@code n} or {@code k} are not integers, if {@code k > n}, or if the effective term count exceeds
     * 	{@link Integer#MAX_VALUE}
     * @throws ArithmeticException
     * 	if the computation would be prohibitively large (very large effective {@code k}, or a very large {@code n})
     */
    public static BigNumber combination(@NonNull final BigNumber n, @NonNull final BigNumber k, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (!n.isInteger() || !k.isInteger()) {
            throw new IllegalArgumentException("Combination requires integer values for both n and k.");
        }

        if (n.isNegative() || k.isNegative()) {
            throw new IllegalArgumentException("Combination requires non-negative integer values for both n and k.");
        }

        if (k.compareTo(n) > 0) {
            throw new IllegalArgumentException("Cannot calculate combinations: k cannot be greater than n.");
        }

        if (k.isEqualTo(ZERO) || k.isEqualTo(n)) {
            return freshOne(locale);
        }

        final BigNumber effectiveK = k.min(n.subtract(k));
        if (effectiveK.isGreaterThan(BigNumber.valueOf(Integer.MAX_VALUE))) {
            throw new IllegalArgumentException("combination is not supported for k > Integer.MAX_VALUE");
        }
        final int iterationCount = effectiveK.intValue();

        final BigInteger nInteger = n.toBigDecimal().toBigIntegerExact();
        rejectIfResultTooLarge(nInteger, iterationCount);

        BigInteger product = BigInteger.ONE;
        for (int i = 0; i < iterationCount; i++) {
            product = product.multiply(nInteger.subtract(BigInteger.valueOf(i)))
                    .divide(BigInteger.valueOf(i + 1L));
        }

        return new BigNumber(new BigDecimal(product), locale);
    }

    /**
     * Calculates the number of permutations of {@code k} items selected from {@code n} items,
     * denoted as P(n, k), which counts the number of ordered arrangements.
     * <p>
     * The formula used is:
     * <pre>
     *     P(n, k) = n! / (n - k)! = n · (n-1) · … · (n-k+1)
     * </pre>
     *
     * <p>Computed directly as the bounded product of {@code k} descending factors with exact
     * {@link BigInteger} arithmetic. This avoids materializing the full {@code n!} (which is unbounded in
     * time/memory for large {@code n}) and never rounds the exact-integer result. {@code mathContext} is
     * validated for API consistency but does not affect the (exact) result.
     *
     * @param n
     * 	the total number of items (must be a non-negative integer)
     * @param k
     * 	the number of items to arrange (must be a non-negative integer, k ≤ n)
     * @param mathContext
     * 	the {@link MathContext}; validated for positive precision but not applied (the result is exact)
     * @param locale
     * 	the {@link Locale} to apply when constructing the resulting {@link BigNumber} (for formatting)
     *
     * @return the number of permutations P(n, k) as an exact {@link BigNumber}
     *
     * @throws IllegalArgumentException
     * 	if {@code n} or {@code k} are not integers, if {@code k > n}, or if {@code k > Integer.MAX_VALUE}
     * @throws ArithmeticException
     * 	if the computation would be prohibitively large (very large {@code k}, or a very large {@code n})
     */
    public static BigNumber permutation(@NonNull final BigNumber n, @NonNull final BigNumber k, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
        MathUtils.checkMathContext(mathContext);

        if (!n.isInteger() || !k.isInteger()) {
            throw new IllegalArgumentException("Permutations requires integer values for both n and k.");
        }

        if (n.isNegative() || k.isNegative()) {
            throw new IllegalArgumentException("Permutations requires non-negative integer values for both n and k.");
        }

        if (k.compareTo(n) > 0) {
            throw new IllegalArgumentException("Cannot calculate permutations: k cannot be greater than n.");
        }

        if (k.isGreaterThan(BigNumber.valueOf(Integer.MAX_VALUE))) {
            throw new IllegalArgumentException("permutation is not supported for k > Integer.MAX_VALUE");
        }

        final int iterationCount = k.intValue();
        final BigInteger nInteger = n.toBigDecimal().toBigIntegerExact();
        rejectIfResultTooLarge(nInteger, iterationCount);

        BigInteger product = BigInteger.ONE;
        for (int i = 0; i < iterationCount; i++) {
            product = product.multiply(nInteger.subtract(BigInteger.valueOf(i)));
        }

        return new BigNumber(new BigDecimal(product), locale);
    }

    /**
     * Rejects a combinatorial computation whose multiplicative product would be impractically large,
     * before the {@code factorCount}-iteration loop runs. Both the loop count and the projected number of
     * result digits ({@code factorCount · digitsOf(n)}) are bounded.
     *
     * @param n           the (non-negative, integer) upper value of the descending product
     * @param factorCount the number of descending factors that would be multiplied
     * @throws ArithmeticException if the loop count or the projected result size exceeds the limit
     */
    private static void rejectIfResultTooLarge(final BigInteger n, final int factorCount) {
        if (factorCount > MAX_COMBINATORIAL_FACTORS) {
            throw new ArithmeticException("Combinatorial argument is too large (maximum " + MAX_COMBINATORIAL_FACTORS + " terms)");
        }

        final long digitsOfN = Math.max(1L, n.abs().toString().length());
        if (factorCount > MAX_COMBINATORIAL_RESULT_DIGITS / digitsOfN) {
            throw new ArithmeticException("Combinatorial result is too large (would exceed " + MAX_COMBINATORIAL_RESULT_DIGITS + " digits)");
        }
    }

    /**
     * Returns a fresh {@link BigNumber} equal to one.
     *
     * <p>Always a new instance rather than the shared {@code BigNumbers.ONE} constant, because the
     * returned value flows back to callers that may mutate it (audit fixes K2/H10).
     *
     * @param locale the locale used for formatting; must not be {@code null}
     * @return a new {@link BigNumber} equal to {@code 1}; never {@code null}
     */
    private static BigNumber freshOne(final Locale locale) {
        return new BigNumber("1", locale);
    }

}
