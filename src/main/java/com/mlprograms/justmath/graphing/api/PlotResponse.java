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

package com.mlprograms.justmath.graphing.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result container for one or more plotted expressions.
 * <p>
 * The response is immutable and safe to return from library APIs.
 * </p>
 *
 * @param series plotted series (unmodifiable defensive copy)
 */
public record PlotResponse(List<PlotSeries> series) {

    /**
     * Validates and defensively copies the provided series list.
     *
     * @throws IllegalArgumentException if {@code series} is null
     */
    public PlotResponse {
        if (series == null) {
            throw new IllegalArgumentException("series must not be null");
        }
        series = Collections.unmodifiableList(new ArrayList<>(series));
    }
}
