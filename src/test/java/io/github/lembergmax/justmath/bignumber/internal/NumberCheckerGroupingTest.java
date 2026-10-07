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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test: {@link NumberChecker#isNumber} must reject malformed grouping placement (a grouping
 * separator not followed by a digit) instead of accepting it and letting {@code normalize} silently strip it.
 */
class NumberCheckerGroupingTest {

    @ParameterizedTest
    @DisplayName("malformed US grouping is rejected")
    @ValueSource(strings = {"1,", "1,,234", "1,.5", "1,234,"})
    void rejectsMalformedGrouping(final String input) {
        assertFalse(NumberChecker.isNumber(input, Locale.US),
                "expected '" + input + "' to be rejected as malformed");
    }

    @ParameterizedTest
    @DisplayName("well-formed US numbers are still accepted")
    @ValueSource(strings = {"1", "1234", "1,234", "1,234,567", "1,234.56", "0.5", "-3.14"})
    void acceptsWellFormedNumbers(final String input) {
        assertTrue(NumberChecker.isNumber(input, Locale.US),
                "expected '" + input + "' to be accepted");
    }
}
