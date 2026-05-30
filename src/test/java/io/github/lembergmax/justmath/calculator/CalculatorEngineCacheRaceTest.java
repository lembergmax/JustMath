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

package io.github.lembergmax.justmath.calculator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Regression tests for the M12 cache-visibility fix.
 *
 * <p>The previous implementation read {@code expressionCacheEnabled} outside the engine
 * monitor on the fast path inside {@code evaluate(String, Map)} and then dispatched into a
 * synchronized {@code storeCache(...)}. Between these two steps another thread could call
 * {@link CalculatorEngine#setExpressionCacheEnabled(boolean)} with {@code false}, discard the
 * cache reference, and the original thread would still execute the store — leaking a token
 * list into a cache that the user thought was off. The fix declares the enabled-flag and the
 * cache reference {@code volatile} and moves the enabled-check inside the synchronized
 * lookup/store helpers so disabling is observed atomically by all readers.</p>
 */
class CalculatorEngineCacheRaceTest {

    /**
     * Reads the private {@code expressionCache} field via reflection so the tests can assert
     * the post-disable state without exposing the field through the public API.
     */
    private static Map<?, ?> readCacheReflectively(final CalculatorEngine engine) throws Exception {
        final Field cacheField = CalculatorEngine.class.getDeclaredField("expressionCache");
        cacheField.setAccessible(true);
        return (Map<?, ?>) cacheField.get(engine);
    }

    @Test
    @DisplayName("setExpressionCacheEnabled(false) discards the cache and blocks subsequent stores")
    void disableClearsCacheAndBlocksStores() throws Exception {
        final CalculatorEngine engine = new CalculatorEngine();
        engine.setExpressionCacheEnabled(true);

        engine.evaluate("1+2");
        final Map<?, ?> populated = readCacheReflectively(engine);
        assertNotNull(populated, "cache must be created on first store while enabled");
        assertTrue(populated.containsKey("1+2"), "first evaluation must populate the cache");

        engine.setExpressionCacheEnabled(false);
        assertNull(readCacheReflectively(engine), "disabling the cache must discard the underlying map");

        engine.evaluate("3+4");
        assertNull(readCacheReflectively(engine),
                "evaluations after a disable must not lazily recreate the cache");
    }

    @Test
    @DisplayName("concurrent setExpressionCacheEnabled toggling and evaluate must not corrupt the cache")
    void concurrentDisableAndEvaluate() throws Exception {
        final CalculatorEngine engine = new CalculatorEngine();
        engine.setExpressionCacheEnabled(true);

        final int workerThreads = 4;
        final int iterations = 200;
        final CountDownLatch start = new CountDownLatch(1);
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        final ExecutorService pool = Executors.newFixedThreadPool(workerThreads * 2);
        final List<Future<?>> futures = new ArrayList<>(workerThreads * 2);

        try {
            for (int worker = 0; worker < workerThreads; worker++) {
                futures.add(pool.submit(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < iterations; i++) {
                            engine.evaluate("1+2");
                            engine.evaluate("3*4");
                        }
                    } catch (Throwable thrown) {
                        failure.compareAndSet(null, thrown);
                    }
                }));
                futures.add(pool.submit(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < iterations; i++) {
                            engine.setExpressionCacheEnabled(false);
                            engine.setExpressionCacheEnabled(true);
                        }
                    } catch (Throwable thrown) {
                        failure.compareAndSet(null, thrown);
                    }
                }));
            }

            start.countDown();
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertNull(failure.get(), "no thread must throw during concurrent cache toggling");

        // After the contention has settled, a final explicit disable must leave the cache
        // reference cleared. With the M12 fix this is observable via the volatile field
        // without needing to acquire the monitor on the reader side.
        engine.setExpressionCacheEnabled(false);
        assertNull(readCacheReflectively(engine),
                "after a quiescent disable, no token list may remain in the cache");
    }
}
