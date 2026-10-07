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

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression tests proving that a {@code summation} / {@code product} loop stops on a thread interrupt
 * and surfaces it as a {@link CancellationException} — not as an {@link ArithmeticException}, which a
 * caller's math-domain error handler ({@code catch (ArithmeticException)}) would silently swallow, and
 * which the engine's runtime-exception classifier would mislabel as a <em>domain error</em>.
 */
class SeriesMathInterruptTest {

    private static final BigNumber ONE = new BigNumber("1", Locale.US);
    private static final BigNumber HUNDRED = new BigNumber("100", Locale.US);

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    @DisplayName("summation aborts with CancellationException when the calling thread is interrupted")
    void summationHonorsInterrupt() {
        Thread.currentThread().interrupt();
        assertThrows(CancellationException.class, () -> SeriesMath.summation(
                ONE, HUNDRED, "k", BigNumbers.DEFAULT_MATH_CONTEXT, TrigonometricMode.DEG, Locale.US));
    }

    @Test
    @DisplayName("product aborts with CancellationException when the calling thread is interrupted")
    void productHonorsInterrupt() {
        Thread.currentThread().interrupt();
        assertThrows(CancellationException.class, () -> SeriesMath.product(
                ONE, HUNDRED, "k", BigNumbers.DEFAULT_MATH_CONTEXT, TrigonometricMode.DEG, Locale.US));
    }
}
