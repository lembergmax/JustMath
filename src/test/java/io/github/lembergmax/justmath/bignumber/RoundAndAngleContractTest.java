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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for two rounding/angle contract bugs:
 * <ul>
 *   <li>{@code roundAfterDecimals} dropped the receiver's locale and fell back to the JVM-default locale.</li>
 *   <li>{@code toDegrees}/{@code toRadians} silently capped an {@link MathContext#UNLIMITED} request to the
 *       internal guard-digit precision instead of rejecting it.</li>
 * </ul>
 */
class RoundAndAngleContractTest {

    @Test
    @DisplayName("roundAfterDecimals preserves the receiver's locale (deterministic, no JVM-default fallback)")
    void roundAfterDecimalsPreservesLocale() {
        final BigNumber german = new BigNumber("1234.567", Locale.GERMANY).roundAfterDecimals(2);
        assertEquals(Locale.GERMANY, german.getLocale(), "result must keep the receiver's locale");
        assertTrue(german.toString().contains(","),
                "German result must format with a comma decimal separator, was " + german.toString());

        final BigNumber us = new BigNumber("1234.567", Locale.US).roundAfterDecimals(2);
        assertEquals("1234.57", us.toString(), "US result must use a dot decimal separator");
    }

    @Test
    @DisplayName("toDegrees/toRadians reject MathContext.UNLIMITED instead of silently capping precision")
    void angleConversionsRejectUnlimitedPrecision() {
        final BigNumber one = new BigNumber("1", Locale.US);
        assertThrows(IllegalArgumentException.class, () -> one.toDegrees(MathContext.UNLIMITED));
        assertThrows(IllegalArgumentException.class, () -> one.toRadians(MathContext.UNLIMITED));
    }
}
