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

package io.github.lembergmax.justmath.bignumber.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BoundedCacheTest {

    private static final int THREAD_COUNT = 8;

    private static final int LOOKUPS_PER_THREAD = 2_000;

    private static final int DISTINCT_KEYS = 100;

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    @DisplayName("a cache that could not hold a single entry is rejected")
    void rejectsMaximumBelowOne(final int maximumEntries) {
        assertThrows(IllegalArgumentException.class, () -> new BoundedCache<String, String>(maximumEntries));
    }

    @Test
    @DisplayName("the loader runs once per key while the entry stays cached")
    void loadsEachKeyOnce() {
        final BoundedCache<Integer, String> cache = new BoundedCache<>(4);
        final AtomicInteger loads = new AtomicInteger();

        for (int round = 0; round < 3; round++) {
            assertEquals("value-1", cache.computeIfAbsent(1, key -> {
                loads.incrementAndGet();
                return "value-" + key;
            }));
        }

        assertEquals(1, loads.get());
    }

    @Test
    @DisplayName("the least recently used entry is evicted first")
    void evictsLeastRecentlyUsed() {
        final BoundedCache<String, String> cache = new BoundedCache<>(2);
        final AtomicInteger loads = new AtomicInteger();
        final Function<String, String> loader = key -> {
            loads.incrementAndGet();
            return key.toUpperCase();
        };

        cache.computeIfAbsent("a", loader);
        cache.computeIfAbsent("b", loader);
        cache.computeIfAbsent("a", loader);
        cache.computeIfAbsent("c", loader);

        final int loadsBefore = loads.get();
        cache.computeIfAbsent("a", loader);
        assertEquals(loadsBefore, loads.get(), "a was used last before c arrived, so it must still be cached");
        cache.computeIfAbsent("b", loader);
        assertEquals(loadsBefore + 1, loads.get(), "b was the least recently used entry, so it must have been evicted");
    }

    @Test
    @DisplayName("the size never exceeds the maximum however many keys are requested")
    void sizeStaysWithinTheMaximum() {
        final BoundedCache<Integer, Integer> cache = new BoundedCache<>(8);

        for (int key = 0; key < 1_000; key++) {
            cache.computeIfAbsent(key, k -> k * 2);
            assertTrue(cache.size() <= cache.getMaximumEntries(), "size after key " + key);
        }

        assertEquals(8, cache.size());
        assertEquals(8, cache.getMaximumEntries());
    }

    @Test
    @DisplayName("a loader that returns null fails and caches nothing")
    void nullFromLoaderIsRejected() {
        final BoundedCache<String, String> cache = new BoundedCache<>(2);

        assertThrows(NullPointerException.class, () -> cache.computeIfAbsent("a", key -> null));

        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("a loader that throws caches nothing and the exception reaches the caller")
    void loaderFailureCachesNothing() {
        final BoundedCache<String, String> cache = new BoundedCache<>(2);

        assertThrows(ArithmeticException.class, () -> cache.computeIfAbsent("a", key -> {
            throw new ArithmeticException("no value for " + key);
        }));

        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("concurrent lookups return the right values and keep the size within the maximum")
    void concurrentLookupsStayConsistent() throws InterruptedException, ExecutionException {
        final BoundedCache<Integer, Integer> cache = new BoundedCache<>(16);
        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            final List<Future<Boolean>> results = new ArrayList<>();
            for (int thread = 0; thread < THREAD_COUNT; thread++) {
                final Random random = new Random(thread);
                final Callable<Boolean> task = () -> lookUpRandomKeys(cache, random);
                results.add(executor.submit(task));
            }
            for (final Future<Boolean> result : results) {
                assertTrue(result.get(), "every lookup must return the value loaded for its key");
            }
        } finally {
            executor.shutdownNow();
        }

        assertTrue(cache.size() <= cache.getMaximumEntries());
    }

    private static boolean lookUpRandomKeys(final BoundedCache<Integer, Integer> cache, final Random random) {
        boolean allCorrect = true;
        for (int lookup = 0; lookup < LOOKUPS_PER_THREAD; lookup++) {
            final int key = random.nextInt(DISTINCT_KEYS);
            allCorrect &= cache.computeIfAbsent(key, k -> k * 31) == key * 31;
            allCorrect &= cache.size() <= cache.getMaximumEntries();
        }
        return allCorrect;
    }

}
