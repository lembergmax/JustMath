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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cooperative cancellation hook for long-running plot calculations.
 * <p>
 * Plot engines are expected to periodically call {@link #isCancelled()} and abort the calculation quickly when it
 * returns {@code true}. Cancellation is <strong>best-effort</strong>: it is not an interrupt mechanism and does not
 * force-stop threads. Instead, it is a lightweight signal that allows plotting code to stop early.
 *
 * <h2>Thread-safety</h2>
 * Implementations must be safe to call from any thread. This is typically achieved by reading a volatile/atomic flag.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * AtomicBoolean cancelled = new AtomicBoolean(false);
 * GraphFxPlotCancellation cancellation = GraphFxPlotCancellation.from(cancelled);
 *
 * // Somewhere else:
 * cancelled.set(true);
 * }</pre>
 */
@FunctionalInterface
public interface PlotCancellation {

    /**
     * Returns whether the current plot operation should be cancelled.
     *
     * @return {@code true} if the plot engine should stop as soon as possible; {@code false} otherwise
     */
    boolean isCancelled();

    /**
     * Returns a cancellation instance that is never cancelled.
     * <p>
     * Useful as a default when the caller does not need cancellation.
     *
     * @return a cancellation that always returns {@code false}
     */
    static PlotCancellation none() {
        return () -> false;
    }

    /**
     * Creates a cancellation hook backed by an {@link AtomicBoolean}.
     *
     * @param cancelledFlag an atomic flag; when it becomes {@code true}, {@link #isCancelled()} will return {@code true}
     * @return a cancellation instance
     * @throws NullPointerException if {@code cancelledFlag} is {@code null}
     */
    static PlotCancellation from(@NonNull final AtomicBoolean cancelledFlag) {
        Objects.requireNonNull(cancelledFlag, "cancelledFlag must not be null.");
        return cancelledFlag::get;
    }

}
