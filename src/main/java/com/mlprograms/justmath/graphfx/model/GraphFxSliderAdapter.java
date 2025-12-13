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
import java.math.BigDecimal;
import java.math.RoundingMode;

public record GraphFxSliderAdapter(BigDecimal min, BigDecimal step, int maxIndex) {

    public static GraphFxSliderAdapter of(final BigDecimal min, final BigDecimal max, final BigDecimal step, final BigDecimal current) {
        final BigDecimal range = max.subtract(min);
        final int idx = range.divide(step, 0, RoundingMode.HALF_UP).max(BigDecimal.ONE).intValue();
        return new GraphFxSliderAdapter(min, step, Math.max(1, idx));
    }

    public int toIndex(final BigDecimal value) {
        return value.subtract(min).divide(step, 0, RoundingMode.HALF_UP).intValue();
    }

    public BigDecimal fromIndex(final int index) {
        return min.add(step.multiply(BigDecimal.valueOf(index)));
    }
}

