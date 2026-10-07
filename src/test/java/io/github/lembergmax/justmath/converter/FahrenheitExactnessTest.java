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

package io.github.lembergmax.justmath.converter;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test: Fahrenheit is now modelled with an exact rational affine formula (scale {@code 5/9},
 * offset {@code -160/9}), so canonical points are exact instead of carrying the ~1e-26 error baked in by the
 * previous pre-rounded decimal constants.
 */
class FahrenheitExactnessTest {

    private static final UnitConverter CONVERTER = new UnitConverter(60);

    @Test
    @DisplayName("32 °F converts to exactly 0 °C")
    void freezingPointIsExact() {
        final BigNumber celsius = CONVERTER.convertToBigNumber(
                new BigNumber("32", Locale.US), Unit.Temperature.FAHRENHEIT, Unit.Temperature.CELSIUS);
        assertEquals(0, celsius.toBigDecimal().compareTo(BigDecimal.ZERO),
                "32 °F must be exactly 0 °C, was " + celsius);
    }

    @Test
    @DisplayName("100 °C converts to exactly 212 °F")
    void boilingPointIsExact() {
        final BigNumber fahrenheit = CONVERTER.convertToBigNumber(
                new BigNumber("100", Locale.US), Unit.Temperature.CELSIUS, Unit.Temperature.FAHRENHEIT);
        assertEquals(0, fahrenheit.toBigDecimal().compareTo(new BigDecimal("212")),
                "100 °C must be exactly 212 °F, was " + fahrenheit);
    }

    @Test
    @DisplayName("-40 °F equals exactly -40 °C (the crossover point)")
    void crossoverIsExact() {
        final BigNumber celsius = CONVERTER.convertToBigNumber(
                new BigNumber("-40", Locale.US), Unit.Temperature.FAHRENHEIT, Unit.Temperature.CELSIUS);
        assertEquals(0, celsius.toBigDecimal().compareTo(new BigDecimal("-40")),
                "-40 °F must be exactly -40 °C, was " + celsius);
    }
}
