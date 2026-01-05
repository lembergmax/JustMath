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

package com.mlprograms.justmath.graphfx;

import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Internal utility that ensures the JavaFX runtime is initialized exactly once and
 * provides safe execution on the JavaFX Application Thread.
 *
 * <p>JavaFX is a JVM-global singleton. Calling {@link Platform#startup(Runnable)} multiple times
 * throws an {@link IllegalStateException}. This helper hides those constraints and offers
 * a small, predictable API for library-style usage.</p>
 */
final class JavaFxRuntime {

    private static final Object START_LOCK = new Object();
    private static volatile boolean started = false;

    /**
     * Ensures that the JavaFX runtime is started.
     *
     * <p>If JavaFX was already started elsewhere (e.g., by another library), this method
     * completes successfully without side effects.</p>
     *
     * <p>This method blocks the calling thread until the JavaFX runtime is ready to accept
     * {@link Platform#runLater(Runnable)} calls.</p>
     */
    static void ensureStarted() {
        if (started) {
            return;
        }

        synchronized (START_LOCK) {
            if (started) {
                return;
            }

            final CompletableFuture<Void> readySignal = new CompletableFuture<>();

            try {
                Platform.startup(() -> readySignal.complete(null));
                readySignal.join();
            } catch (final IllegalStateException alreadyRunning) {
                // The JavaFX toolkit is already running; nothing to do.
            } finally {
                started = true;
            }
        }
    }

    /**
     * Executes the given runnable on the JavaFX Application Thread.
     *
     * <p>If the caller is already on the JavaFX Application Thread, the runnable is executed immediately.</p>
     *
     * @param runnable the work to execute (must not be {@code null})
     */
    static void runOnFxThread(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

}
