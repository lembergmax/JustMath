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

package com.mlprograms.justmath.graphing.fx;

import javafx.application.Platform;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal JavaFX runtime bootstrap and thread utilities.
 * <p>
 * This helper ensures JavaFX is started exactly once and provides safe helpers for executing tasks on the
 * JavaFX application thread.
 * </p>
 */
@UtilityClass
public class JavaFxRuntime {

    /**
     * Lock object used to guard JavaFX startup.
     */
    private static final Object START_LOCK = new Object();

    /**
     * Indicates whether JavaFX startup has already been performed.
     */
    private static volatile boolean started;

    /**
     * Counter for open viewer windows tracked for the implicit-exit policy.
     */
    private static final AtomicInteger OPEN_TRACKED_WINDOWS = new AtomicInteger(0);

    /**
     * Ensures that JavaFX is started.
     * <p>
     * This method is safe to call multiple times and from multiple threads.
     * </p>
     */
    public static void ensureStarted() {
        if (started) {
            return;
        }

        synchronized (START_LOCK) {
            if (started) {
                return;
            }

            Platform.setImplicitExit(false);

            final CompletableFuture<Void> readySignal = new CompletableFuture<>();
            try {
                Platform.startup(() -> readySignal.complete(null));
                readySignal.join();
            } catch (final IllegalStateException alreadyRunning) {
                // JavaFX already started; safe to continue.
            } finally {
                started = true;
            }
        }
    }

    /**
     * Runs a task on the JavaFX application thread.
     * <p>
     * If the caller is already on the FX thread, the task is executed immediately.
     * </p>
     *
     * @param runnable task to execute (must not be {@code null})
     */
    public static void runOnFxThread(@NonNull final Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }
        Platform.runLater(runnable);
    }

    /**
     * Enqueues a task on the JavaFX application thread.
     *
     * @param runnable task to enqueue (must not be {@code null})
     */
    public static void enqueueOnFxThread(@NonNull final Runnable runnable) {
        Platform.runLater(runnable);
    }

    /**
     * Registers that a tracked viewer window has been opened.
     *
     * @return updated number of tracked windows
     */
    public static int registerTrackedViewerOpened() {
        return OPEN_TRACKED_WINDOWS.incrementAndGet();
    }

    /**
     * Registers that a tracked viewer window has been closed and exits JavaFX if this was the last one.
     *
     * @return remaining number of tracked windows (never negative)
     */
    public static int registerTrackedViewerClosedAndExitIfLast() {
        final int remaining = OPEN_TRACKED_WINDOWS.decrementAndGet();
        if (remaining <= 0) {
            OPEN_TRACKED_WINDOWS.set(0);
            enqueueOnFxThread(Platform::exit);
        }
        return Math.max(0, remaining);
    }
}
