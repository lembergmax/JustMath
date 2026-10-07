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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The unbiased sample variance divides the sum of the squared deviations by {@code N - 1}, the population variance by
 * {@code N}. Neither is defined for fewer than two elements.
 */
class BigNumberListSampleStatisticsTest {

    private static BigNumberList listOf(final String... texts) {
        return BigNumberList.fromStrings(List.of(texts));
    }

    @Test
    @DisplayName("sampleVariance() divides by N - 1")
    void sampleVarianceDividesByNMinusOne() {
        assertEquals(new BigNumber("2.5"), listOf("1", "2", "3", "4", "5").sampleVariance().trim());
        assertEquals(new BigNumber("2"), listOf("1", "3").sampleVariance().trim());
    }

    @Test
    @DisplayName("sampleVariance() of a spread that does not divide evenly is rounded to the default precision")
    void sampleVarianceRoundsToTheDefaultPrecision() {
        final BigDecimal expected = new BigDecimal(32).divide(new BigDecimal(7), new MathContext(BigNumbers.DEFAULT_DIVISION_PRECISION, RoundingMode.HALF_UP));

        final BigNumber actual = listOf("2", "4", "4", "4", "5", "5", "7", "9").sampleVariance();

        assertEquals(0, expected.compareTo(actual.toBigDecimal()), "32 / 7 but was " + actual);
    }

    @Test
    @DisplayName("sampleVariance() times N - 1 equals the population variance times N")
    void sampleVarianceRelatesToThePopulationVariance() {
        final BigNumberList list = listOf("1", "2", "3", "4", "5");

        final BigNumber populationTimesN = list.variance().multiply(new BigNumber("5"));
        final BigNumber sampleTimesNMinusOne = list.sampleVariance().multiply(new BigNumber("4"));

        assertEquals(0, populationTimesN.compareTo(sampleTimesNMinusOne));
    }

    @Test
    @DisplayName("sampleVariance() needs two elements")
    void sampleVarianceNeedsTwoElements() {
        assertThrows(IllegalStateException.class, () -> listOf().sampleVariance());
        assertThrows(IllegalStateException.class, () -> listOf("5").sampleVariance());
    }

    @Test
    @DisplayName("sampleStandardDeviation() is the square root of the sample variance")
    void sampleStandardDeviationIsTheSquareRoot() {
        assertEquals(new BigNumber("1"), listOf("1", "2", "3").sampleStandardDeviation().trim());
        assertEquals(new BigNumber("2"), listOf("2", "4", "6").sampleStandardDeviation().trim());
    }

    @Test
    @DisplayName("sampleStandardDeviation() needs two elements")
    void sampleStandardDeviationNeedsTwoElements() {
        assertThrows(IllegalStateException.class, () -> listOf("5").sampleStandardDeviation());
    }
}
