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

package com.mlprograms.justmath.graphfx.model;

import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Adapts a continuous {@link BigDecimal} value range to the discrete integer index model of a UI slider.
 * <p>
 * JavaFX sliders operate on a numeric range, but many UIs effectively treat the slider as a discrete control
 * (e.g., step-wise increments). This adapter provides a stable mapping between:
 * </p>
 * <ul>
 *     <li>a decimal domain value {@code v} in {@code [min .. max]}</li>
 *     <li>an integer slider index {@code i} in {@code [0 .. maxIndex]}</li>
 * </ul>
 *
 * <h2>Rounding behavior</h2>
 * <p>
 * When converting a value to an index, this adapter uses {@link RoundingMode#HALF_UP}. This means the value is mapped
 * to the nearest step index with ties rounding upward.
 * </p>
 *
 * <p><b>Note:</b> This adapter assumes {@code step > 0}. Validation should occur before creating an instance.</p>
 *
 * @param min      the minimum domain value represented by index {@code 0}
 * @param step     the step size between two adjacent indices (must be &gt; 0)
 * @param maxIndex the maximum allowed index (must be &gt;= 1)
 */
public record GraphFxSliderAdapter(@NonNull BigDecimal min, @NonNull BigDecimal step, int maxIndex) {

    /**
     * Creates a slider adapter for the given range and step size.
     * <p>
     * The {@code maxIndex} is computed as:
     * </p>
     * <pre>
     * maxIndex = max(1, roundHalfUp((max - min) / step))
     * </pre>
     * <p>
     * The {@code current} parameter is accepted to fit common call sites (range + current value),
     * but it is not needed for computing the mapping and therefore not used.
     * </p>
     *
     * @param min     minimum domain value represented by index {@code 0} (must not be {@code null})
     * @param max     maximum domain value represented by {@code maxIndex} (must not be {@code null})
     * @param step    step size per index increment (must not be {@code null} and should be &gt; 0)
     * @param current current value (must not be {@code null}); not used for computation
     * @return a new {@link GraphFxSliderAdapter} with a computed {@code maxIndex}
     */
    public static GraphFxSliderAdapter of(
            @NonNull final BigDecimal min,
            @NonNull final BigDecimal max,
            @NonNull final BigDecimal step,
            @NonNull final BigDecimal current
    ) {
        final BigDecimal rangeWidth = max.subtract(min);
        final int computedIndex = rangeWidth
                .divide(step, 0, RoundingMode.HALF_UP)
                .max(BigDecimal.ONE)
                .intValue();

        return new GraphFxSliderAdapter(min, step, Math.max(1, computedIndex));
    }

    /**
     * Converts a domain value into the nearest slider index using {@link RoundingMode#HALF_UP}.
     *
     * <p>Formula:</p>
     * <pre>
     * index = roundHalfUp((value - min) / step)
     * </pre>
     *
     * @param value the domain value to map (must not be {@code null})
     * @return the discrete slider index for the given value
     */
    public int toIndex(@NonNull final BigDecimal value) {
        return value
                .subtract(min)
                .divide(step, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /**
     * Converts a slider index back into its represented domain value.
     *
     * <p>Formula:</p>
     * <pre>
     * value = min + step * index
     * </pre>
     *
     * @param index the slider index
     * @return the corresponding domain value as {@link BigDecimal}
     */
    public BigDecimal fromIndex(final int index) {
        return min.add(step.multiply(BigDecimal.valueOf(index)));
    }
    
}
