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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.Function;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.lembergmax.justmath.bignumber.internal.BoundedCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * The caches behind {@link BigNumbers#pi(MathContext)} and {@link BigNumbers#e(MathContext)} must not
 * grow with the number of distinct precisions a caller asks for, and caching must not change a result.
 */
class BigNumbersConstantCacheTest {

    private static final int DOCUMENTED_MAXIMUM_ENTRIES = 32;

    private static final int PRECISIONS_BEYOND_THE_BOUND = 200;

    private static final int PRECISIONS_COMPARED = 300;

    @SuppressWarnings("unchecked")
    private static BoundedCache<MathContext, BigDecimal> cache(final String fieldName) throws ReflectiveOperationException {
        final Field field = BigNumbers.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (BoundedCache<MathContext, BigDecimal>) field.get(null);
    }

    private static void requestDistinctPrecisions(final Function<MathContext, BigNumber> constant) {
        for (int precision = 10; precision < 10 + PRECISIONS_BEYOND_THE_BOUND; precision++) {
            constant.apply(new MathContext(precision));
        }
    }

    @Test
    @DisplayName("the pi cache holds at most the documented number of precisions")
    void piCacheIsBounded() throws ReflectiveOperationException {
        requestDistinctPrecisions(BigNumbers::pi);

        final BoundedCache<MathContext, BigDecimal> cache = cache("PI_BD_CACHE");
        assertEquals(DOCUMENTED_MAXIMUM_ENTRIES, cache.getMaximumEntries());
        assertTrue(cache.size() <= DOCUMENTED_MAXIMUM_ENTRIES, "cache size was " + cache.size());
    }

    @Test
    @DisplayName("the e cache holds at most the documented number of precisions")
    void eCacheIsBounded() throws ReflectiveOperationException {
        requestDistinctPrecisions(BigNumbers::e);

        final BoundedCache<MathContext, BigDecimal> cache = cache("E_BD_CACHE");
        assertEquals(DOCUMENTED_MAXIMUM_ENTRIES, cache.getMaximumEntries());
        assertTrue(cache.size() <= DOCUMENTED_MAXIMUM_ENTRIES, "cache size was " + cache.size());
    }

    @Test
    @DisplayName("a precision that was evicted is computed again with the same result")
    void evictedPrecisionGivesTheSameResult() {
        final MathContext early = new MathContext(50);
        final BigNumber before = BigNumbers.pi(early);

        requestDistinctPrecisions(BigNumbers::pi);

        assertEquals(before, BigNumbers.pi(early));
        assertEquals(0, BigDecimalMath.pi(early).compareTo(BigNumbers.pi(early).toBigDecimal()));
    }

    @ParameterizedTest
    @EnumSource(value = RoundingMode.class, names = {"HALF_UP", "HALF_EVEN", "DOWN"})
    @DisplayName("pi equals BigDecimalMath.pi for every precision from 1 to 300")
    void piMatchesBigDecimalMath(final RoundingMode roundingMode) {
        for (int precision = 1; precision <= PRECISIONS_COMPARED; precision++) {
            final MathContext mathContext = new MathContext(precision, roundingMode);

            assertEquals(0, BigDecimalMath.pi(mathContext).compareTo(BigNumbers.pi(mathContext).toBigDecimal()),
                    "pi with " + mathContext);
        }
    }

    @ParameterizedTest
    @EnumSource(value = RoundingMode.class, names = {"HALF_UP", "HALF_EVEN", "DOWN"})
    @DisplayName("e equals BigDecimalMath.e for every precision from 1 to 300")
    void eMatchesBigDecimalMath(final RoundingMode roundingMode) {
        for (int precision = 1; precision <= PRECISIONS_COMPARED; precision++) {
            final MathContext mathContext = new MathContext(precision, roundingMode);

            assertEquals(0, BigDecimalMath.e(mathContext).compareTo(BigNumbers.e(mathContext).toBigDecimal()),
                    "e with " + mathContext);
        }
    }

}
