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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import io.github.lembergmax.justmath.calculator.CalculatorEngine;

/**
 * Audit fix K8: clones and copies must not share their {@link CalculatorEngine} with the source.
 * The engine is now created lazily per instance, so mutating one holder's engine configuration
 * must never be observable through another holder.
 */
class BigNumberEngineIsolationTest {

    @Test
    @DisplayName("getCalculatorEngine() lazily returns a non-null engine")
    void lazyEngineIsCreated() {
        assertNotNull(new BigNumber("5").getCalculatorEngine());
    }

    @Test
    @DisplayName("clone() gets an independent engine")
    void cloneEngineIsIndependent() {
        final BigNumber original = new BigNumber("5");
        final CalculatorEngine originalEngine = original.getCalculatorEngine();

        final BigNumber clone = original.clone();
        final CalculatorEngine cloneEngine = clone.getCalculatorEngine();

        assertNotSame(originalEngine, cloneEngine, "clone must own a separate engine");

        cloneEngine.setLocale(Locale.GERMANY);
        assertNotEquals(Locale.GERMANY, originalEngine.getLocale(),
                "mutating the clone's engine must not affect the original");
    }

    @Test
    @DisplayName("copy constructor gets an independent engine")
    void copyConstructorEngineIsIndependent() {
        final BigNumber original = new BigNumber("5");
        final CalculatorEngine originalEngine = original.getCalculatorEngine();

        final BigNumber copy = new BigNumber(original);
        final CalculatorEngine copyEngine = copy.getCalculatorEngine();

        assertNotSame(originalEngine, copyEngine, "copy must own a separate engine");
    }

    @Test
    @DisplayName("Lazy engine still drives summation correctly")
    void summationStillWorks() {
        // sum_{k=0}^{3} k = 6
        assertEquals("6", new BigNumber("0").summation(new BigNumber("3"), "k").toString());
    }
}
