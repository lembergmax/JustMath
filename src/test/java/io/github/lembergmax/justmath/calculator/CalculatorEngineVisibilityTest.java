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

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.lembergmax.justmath.calculator.errors.ErrorMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Visibility contract of the {@link CalculatorEngine} configuration. The setters may run on a
 * different thread than the evaluations, so every configuration field has to be {@code volatile}:
 * SpotBugs reported {@code UG_SYNC_SET_UNSYNC_GET} for fields that were written under the engine's
 * monitor but read without it.
 */
class CalculatorEngineVisibilityTest {

    private static final int THREAD_COUNT = 8;
    private static final int ITERATIONS = 300;
    private static final Set<String> VALID_RESULTS = Set.of("0.5", "0,5");

    @ParameterizedTest(name = "{0} is volatile")
    @ValueSource(strings = {
            "locale",
            "inputLocale",
            "errorMode",
            "expressionCacheSize",
            "inputDecimalSeparator",
            "expressionCacheEnabled",
            "expressionCache"
    })
    @DisplayName("configuration fields that are written by one thread and read by another are volatile")
    void configurationFieldsAreVolatile(final String fieldName) throws NoSuchFieldException {
        final Field field = CalculatorEngine.class.getDeclaredField(fieldName);

        assertTrue(Modifier.isVolatile(field.getModifiers()), fieldName + " must be volatile");
    }

    @Test
    @Timeout(60)
    @DisplayName("changing the configuration while other threads evaluate never fails and never yields an invalid result")
    void configurationChangesDuringEvaluationAreSafe() throws Exception {
        final CalculatorEngine engine = new CalculatorEngine();
        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        final CountDownLatch start = new CountDownLatch(1);
        final List<Future<Void>> futures = new ArrayList<>();

        try {
            for (int thread = 0; thread < THREAD_COUNT; thread++) {
                final boolean writer = thread % 2 == 0;
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int iteration = 0; iteration < ITERATIONS; iteration++) {
                        if (writer) {
                            reconfigure(engine, iteration);
                        } else {
                            final String result = engine.evaluateToString("1/2");
                            assertTrue(VALID_RESULTS.contains(result), "unexpected result: " + result);
                        }
                    }
                    return null;
                }));
            }

            start.countDown();
            for (final Future<Void> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static void reconfigure(final CalculatorEngine engine, final int iteration) {
        final boolean even = iteration % 2 == 0;

        engine.setLocale(even ? Locale.US : Locale.GERMANY);
        engine.setErrorMode(even ? ErrorMode.RAW : ErrorMode.USER_FRIENDLY);
        engine.setInputLocale(even ? Locale.US : Locale.GERMANY);
        engine.setExpressionCacheEnabled(even);
        engine.setExpressionCacheSize(even ? 16 : 64);
    }
}
