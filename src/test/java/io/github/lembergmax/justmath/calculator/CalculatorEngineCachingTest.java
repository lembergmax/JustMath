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

import org.junit.jupiter.api.Test;

import java.util.Map;

import io.github.lembergmax.justmath.bignumber.BigNumber;

class CalculatorEngineCachingTest {

    @Test
    void differentVariableValuesProduceDifferentResultsWithCacheEnabled() {
        CalculatorEngine engine = new CalculatorEngine().setExpressionCacheEnabled(true);
        BigNumber a = engine.evaluate("x+1", Map.of("x", "1"));
        BigNumber b = engine.evaluate("x+1", Map.of("x", "2"));
        assertEquals("2", a.toString());
        assertEquals("3", b.toString());
    }

    @Test
    void cacheIsTransparentForSameExpressionRepeats() {
        CalculatorEngine engine = new CalculatorEngine().setExpressionCacheEnabled(true);
        for (int i = 0; i < 50; i++) {
            assertEquals("4", engine.evaluate("2+2").toString());
        }
    }

    @Test
    void threadLocalRestoredAfterEvaluate() {
        CalculatorEngine engine = new CalculatorEngine();
        engine.evaluate("1+1", Map.of("x", "5"));
        assertTrue(CalculatorEngine.getCurrentVariables().isEmpty(),
                "ThreadLocal currentVariables not restored after evaluate");
    }

    @Test
    void threadLocalRestoredAfterException() {
        CalculatorEngine engine = new CalculatorEngine();
        try {
            engine.evaluate("2#3", Map.of("x", "5"));
            fail("expected exception");
        } catch (Exception ignored) {
        }
        assertTrue(CalculatorEngine.getCurrentVariables().isEmpty(),
                "ThreadLocal currentVariables not restored after exception");
    }

    @Test
    void cacheSizeSetterRejectsZero() {
        CalculatorEngine engine = new CalculatorEngine();
        assertThrows(IllegalArgumentException.class, () -> engine.setExpressionCacheSize(0));
    }

    @Test
    void cacheCanBeDisabledAfterEnabling() {
        CalculatorEngine engine = new CalculatorEngine().setExpressionCacheEnabled(true);
        engine.evaluate("2+2");
        engine.setExpressionCacheEnabled(false);
        assertNull(engine.getExpressionCache());
    }
}
