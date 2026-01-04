/*
 * Copyright (c) 2025 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.graphfx.api.plot;

import lombok.NonNull;

import java.util.Objects;

/**
 * Strategy interface for computing plot geometry from a {@link PlotRequest}.
 * <p>
 * A plot engine takes a request (expression, variables, bounds and pixel size) and returns a {@link PlotGeometry}
 * that can be rendered by a viewer.
 *
 * <h2>Cancellation</h2>
 * Engines should periodically check {@link PlotCancellation#isCancelled()} and stop early if cancellation is
 * requested.
 *
 * <h2>Thread-safety</h2>
 * Implementations should document whether they are thread-safe. Unless otherwise stated, callers should assume that
 * engines are safe to use from multiple threads as long as they do not share mutable state between plot calls.
 *
 * <h2>Error handling</h2>
 * Public inputs must be validated. Evaluation errors (e.g., division by zero) should be handled gracefully by producing
 * discontinuities instead of throwing, unless throwing is explicitly documented.
 */
@FunctionalInterface
public interface PlotEngine {

    /**
     * Calculates plot geometry for the given request.
     *
     * @param request      the plot request
     * @param cancellation cancellation hook (best-effort)
     * @return the computed plot geometry (never {@code null})
     * @throws NullPointerException     if {@code request} or {@code cancellation} is {@code null}
     * @throws IllegalArgumentException if {@code request} violates constraints
     */
    PlotGeometry plot(@NonNull final PlotRequest request, @NonNull final PlotCancellation cancellation);

    /**
     * Calculates plot geometry for the given request without cancellation.
     *
     * @param request the plot request
     * @return the computed plot geometry (never {@code null})
     * @throws NullPointerException     if {@code request} is {@code null}
     * @throws IllegalArgumentException if {@code request} violates constraints
     */
    default PlotGeometry plot(@NonNull final PlotRequest request) {
        Objects.requireNonNull(request, "request must not be null.");
        return plot(request, PlotCancellation.none());
    }

}
