/*
 * Copyright (c) 2025-2026 Max Lemberg
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal helper that initializes the JavaFX toolkit exactly once and provides safe execution
 * on the JavaFX Application Thread.
 *
 * <p><b>Important:</b> JavaFX is JVM-global. You cannot run multiple independent toolkits per JVM,
 * but you can create multiple independent windows (stages) inside one toolkit.</p>
 *
 * <h2>Exit handling</h2>
 * <p>This helper keeps {@code implicitExit=false} so that temporary window hiding does not shut down
 * the toolkit. If the last tracked viewer window is closed, this helper schedules a deferred
 * {@link Platform#exit()} to terminate JavaFX cleanly.</p>
 */
public final class JavaFxRuntime {

    private static final Object START_LOCK = new Object();
    private static volatile boolean started;

    private static final AtomicInteger OPEN_TRACKED_WINDOWS = new AtomicInteger(0);

    /**
     * Ensures the JavaFX runtime is started exactly once.
     *
     * <p>This method is thread-safe and blocks until the JavaFX toolkit is ready to accept
     * {@link Platform#runLater(Runnable)} calls.</p>
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
                // JavaFX already started by someone else; safe to continue.
            } finally {
                started = true;
            }
        }
    }

    /**
     * Executes the given runnable on the JavaFX Application Thread.
     *
     * <p>If called from the JavaFX Application Thread, the runnable is executed immediately.</p>
     *
     * @param runnable the work to run (must not be {@code null})
     */
    public static void runOnFxThread(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

    /**
     * Enqueues a runnable on the JavaFX Application Thread, even if the caller is already
     * on the JavaFX Application Thread.
     *
     * <p>This is important when you must avoid re-entrancy during close/hide event processing.</p>
     *
     * @param runnable the work to enqueue (must not be {@code null})
     */
    public static void enqueueOnFxThread(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        Platform.runLater(runnable);
    }

    /**
     * Registers that a tracked viewer window became "open" for the "exit on last viewer close" policy.
     *
     * @return the current number of tracked open windows
     */
    public static int registerTrackedViewerOpened() {
        return OPEN_TRACKED_WINDOWS.incrementAndGet();
    }

    /**
     * Registers that a tracked viewer window was closed. If it was the last tracked window,
     * the JavaFX runtime is terminated via {@link Platform#exit()}.
     *
     * <p><b>Implementation detail:</b> The exit is executed deferred (queued with
     * {@link Platform#runLater(Runnable)}) to avoid issues during the close/hide call stack.</p>
     *
     * @return the remaining number of tracked open windows
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
