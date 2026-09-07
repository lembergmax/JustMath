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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for the v1.6.0 {@link CalculatorEngine#setInputLocale(Locale)} feature, which had no
 * tests. It exercises: the comma-decimal input switch, the strict rejection of the previous separator, the
 * cache discard on switch, and independence from the output {@link CalculatorEngine#setLocale(Locale)}.
 */
class CalculatorEngineInputLocaleTest {

    @Test
    @DisplayName("default input locale is US: '.' is the decimal separator and ',' is invalid")
    void defaultInputIsUnitedStates() {
        final CalculatorEngine engine = new CalculatorEngine();
        assertEquals(Locale.US, engine.getInputLocale());
        assertEquals("4.14", engine.evaluateToString("3.14+1"));
        assertTrue(engine.evaluateSafe("3,14").isFailure(), "',' must be an invalid input character under US input");
    }

    @Test
    @DisplayName("setInputLocale(GERMANY) accepts comma decimals and rejects '.'")
    void germanInputAcceptsCommaDecimals() {
        final CalculatorEngine engine = new CalculatorEngine();
        engine.setInputLocale(Locale.GERMANY);
        assertEquals(Locale.GERMANY, engine.getInputLocale());
        // Output locale is unchanged (English) so the result still prints with a dot.
        assertEquals("4.14", engine.evaluateToString("3,14+1"));
        assertTrue(engine.evaluateSafe("3.14").isFailure(), "'.' must be invalid under German input");
    }

    @Test
    @DisplayName("switching the input locale discards previously cached tokens")
    void switchingInputLocaleDiscardsCache() {
        final CalculatorEngine engine = new CalculatorEngine();
        engine.setExpressionCacheEnabled(true);
        assertEquals("1.5", engine.evaluateToString("1.5"));

        engine.setInputLocale(Locale.GERMANY);
        assertTrue(engine.evaluateSafe("1.5").isFailure(),
                "'1.5' must be rejected after switching to German input (stale cache must not let it through)");
        assertEquals("1.5", engine.evaluateToString("1,5"));
    }

    @Test
    @DisplayName("output setLocale does not change input parsing")
    void outputLocaleIsIndependentOfInputLocale() {
        final CalculatorEngine engine = new CalculatorEngine();
        engine.setLocale(Locale.GERMANY);
        assertEquals(Locale.US, engine.getInputLocale(), "setLocale must not change the input locale");
        assertFalse(engine.evaluateSafe("1.5").isFailure(), "US input '.' must still be valid after output setLocale");
        assertTrue(engine.evaluateSafe("1,5").isFailure(), "',' must still be invalid input after output setLocale");
    }
}
