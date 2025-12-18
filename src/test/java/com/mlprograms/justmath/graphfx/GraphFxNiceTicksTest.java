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

import com.mlprograms.justmath.graphfx.model.GraphFxNiceTicks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraphFxNiceTicks")
final class GraphFxNiceTicksTest {

    private static final double EPS = 1e-12;

    @Nested
    @DisplayName("niceStep(min, max, targetLines)")
    final class NiceStepTests {

        @ParameterizedTest(name = "range edge case: min={0}, max={1} -> 1.0")
        @CsvSource({
                "0, 0",
                "1, 1",
                "-5, -5",
                "42.123, 42.123"
        })
        void returnsOneForZeroRange(final double min, final double max) {
            final double step = GraphFxNiceTicks.niceStep(min, max, 10);

            assertEquals(1.0, step, EPS);
        }

        @ParameterizedTest(name = "non-finite range: min={0}, max={1} -> 1.0")
        @CsvSource({
                "NaN, 1",
                "1, NaN",
                "Infinity, 1",
                "1, Infinity",
                "-Infinity, 1",
                "1, -Infinity"
        })
        void returnsOneForNonFiniteRange(final double min, final double max) {
            final double step = GraphFxNiceTicks.niceStep(min, max, 10);

            assertEquals(1.0, step, EPS);
        }

        @ParameterizedTest(name = "targetLines < 2 coerced: min={0}, max={1}, targetLines={2} -> expected={3}")
        @CsvSource({
                "0, 10, 0, 5",
                "0, 10, 1, 5",
                "0, 10, -3, 5"
        })
        void treatsTargetLinesLessThanTwoAsTwo(
                final double min,
                final double max,
                final int targetLines,
                final double expectedStep
        ) {
            final double step = GraphFxNiceTicks.niceStep(min, max, targetLines);

            assertEquals(expectedStep, step, EPS);
        }

        @ParameterizedTest(name = "basic rounding: min={0}, max={1}, targetLines={2} -> expected={3}")
        @CsvSource({
                // range=10
                "0, 10, 3, 5",   // rough=3.333 -> normalized=3.333 -> 5
                "0, 10, 9, 1",   // rough=1.111 -> normalized=1.111 -> 1
                // range=1
                "0, 1, 3, 0.5",  // rough=0.333 -> power=0.1, normalized=3.333 -> 0.5
                "0, 1, 9, 0.1"   // rough=0.111 -> power=0.1, normalized=1.111 -> 0.1
        })
        void roundsToNiceStepsAcrossTypicalRanges(
                final double min,
                final double max,
                final int targetLines,
                final double expectedStep
        ) {
            final double step = GraphFxNiceTicks.niceStep(min, max, targetLines);

            assertEquals(expectedStep, step, EPS);
        }

        @ParameterizedTest(name = "boundary rounding: range={0}, targetLines={1} -> expected={2}")
        @CsvSource({
                // These are crafted so roughStep hits the threshold exactly:
                // normalizedStep < 1.5 => 1 ; else < 3 => 2 ; else < 7 => 5 ; else => 10
                "15, 10, 2",   // rough=1.5 -> should become 2
                "30, 10, 5",   // rough=3.0 -> should become 5
                "70, 10, 10"   // rough=7.0 -> should become 10
        })
        void usesCorrectBucketsOnThresholdBoundaries(
                final double range,
                final int targetLines,
                final double expectedStep
        ) {
            final double min = 0;
            final double max = range;

            final double step = GraphFxNiceTicks.niceStep(min, max, targetLines);

            assertEquals(expectedStep, step, EPS);
        }

        @ParameterizedTest(name = "scale invariance: min={0}, max={1}, targetLines={2} -> expected={3}")
        @CsvSource({
                // range=1000, target=10 => rough=100 => normalized=1 => 100
                "0, 1000, 10, 100",
                // range=2000, target=10 => rough=200 => normalized=2 => 200
                "0, 2000, 10, 200",
                // range=5000, target=10 => rough=500 => normalized=5 => 500
                "0, 5000, 10, 500",
                // reversed min/max should behave the same
                "1000, 0, 10, 100"
        })
        void supportsLargeMagnitudesAndReversedBounds(
                final double min,
                final double max,
                final int targetLines,
                final double expectedStep
        ) {
            final double step = GraphFxNiceTicks.niceStep(min, max, targetLines);

            assertEquals(expectedStep, step, EPS);
        }

        @Test
        void alwaysReturnsPositiveFiniteStepForNormalInputs() {
            final double step = GraphFxNiceTicks.niceStep(-123.45, 987.65, 12);

            assertTrue(Double.isFinite(step), "Step must be finite.");
            assertTrue(step > 0, "Step must be positive.");
        }
    }

}

