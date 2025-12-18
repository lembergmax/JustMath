/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.graphfx;

import com.mlprograms.justmath.graphfx.model.GraphFxSliderAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxSliderAdapter")
final class GraphFxSliderAdapterTest {

    @ParameterizedTest(name = "min={0}, step={1}, value={2} -> index={3}")
    @CsvSource({
            "0, 1, 0, 0",
            "0, 1, 1, 1",
            "0, 1, 1.49, 1",
            "0, 1, 1.50, 2",
            "-10, 0.5, -10, 0",
            "-10, 0.5, -9.75, 1"
    })
    void toIndex_usesHalfUpRounding(
            final String min,
            final String step,
            final String value,
            final int expectedIndex
    ) {
        final GraphFxSliderAdapter adapter = new GraphFxSliderAdapter(
                new BigDecimal(min),
                new BigDecimal(step),
                100
        );

        assertEquals(expectedIndex, adapter.toIndex(new BigDecimal(value)));
    }

    @ParameterizedTest(name = "min={0}, step={1}, index={2} -> value={3}")
    @CsvSource({
            "0, 1, 0, 0",
            "0, 1, 2, 2",
            "-10, 0.5, 4, -8.0"
    })
    void fromIndex_mapsBackLinearly(
            final String min,
            final String step,
            final int index,
            final String expectedValue
    ) {
        final GraphFxSliderAdapter adapter = new GraphFxSliderAdapter(
                new BigDecimal(min),
                new BigDecimal(step),
                100
        );

        assertEquals(new BigDecimal(expectedValue), adapter.fromIndex(index));
    }

    @ParameterizedTest(name = "of() ensures maxIndex >= 1: min={0}, max={1}, step={2}")
    @CsvSource({
            "0, 0, 1",
            "0, 0.1, 1",
            "0, 0.5, 10"
    })
    void of_computesAtLeastOneIndex(final String min, final String max, final String step) {
        final GraphFxSliderAdapter adapter = GraphFxSliderAdapter.of(
                new BigDecimal(min),
                new BigDecimal(max),
                new BigDecimal(step),
                BigDecimal.ZERO
        );

        assertTrue(adapter.maxIndex() >= 1);
    }

}

