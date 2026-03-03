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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized JavaFX runtime bootstrap and thread utilities for library usage.
 *
 * <p>
 * JavaFX can only be started once per JVM. A library should therefore not force users
 * to extend {@code javafx.application.Application}. This helper provides a safe,
 * idempotent bootstrap and a few convenience methods for scheduling work on the JavaFX thread.
 * </p>
 *
 * <p>
 * In addition, this runtime supports a simple "exit policy" counter: if a viewer window is
 * tracked and the last tracked window closes, JavaFX will be exited.
 * </p>
 */
public final class JavaFxRuntime {

    /**
     * Global lock for {@link #ensureStarted()} to avoid racing JavaFX startup.
     */
    private static final Object START_LOCK = new Object();

    /**
     * Flag indicating whether the JavaFX platform has been started.
     */
    private static volatile boolean started;

    /**
     * Counter for viewer windows that are tracked for "exit when last closes".
     */
    private static final AtomicInteger OPEN_TRACKED_WINDOWS = new AtomicInteger(0);

    private JavaFxRuntime() {
        // Utility class.
    }

    /**
     * Ensures that JavaFX is started.
     *
     * <p>
     * This method is safe to call multiple times and from any thread.
     * </p>
     *
     * <p>
     * It also configures JavaFX to not implicitly exit when the last window is closed,
     * because libraries often open/close windows dynamically.
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
     * Executes the given runnable on the JavaFX application thread.
     *
     * <p>
     * If the caller is already on the JavaFX thread, the runnable is executed immediately.
     * Otherwise, it is scheduled via {@link Platform#runLater(Runnable)}.
     * </p>
     *
     * @param runnable task to execute (must not be null)
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
     * Schedules the given runnable to run later on the JavaFX thread.
     *
     * <p>
     * This method never executes inline, even if called on the JavaFX thread.
     * Use {@link #runOnFxThread(Runnable)} if inline execution is desired.
     * </p>
     *
     * @param runnable task to schedule (must not be null)
     */
    public static void enqueueOnFxThread(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        Platform.runLater(runnable);
    }

    /**
     * Executes a task on the JavaFX thread and blocks the calling thread until completion.
     *
     * <p>
     * This is useful for synchronous snapshot APIs. It should be used sparingly because
     * it blocks the calling thread.
     * </p>
     *
     * @param runnable task to execute (must not be null)
     */
    public static void runOnFxThreadAndWait(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        final CompletableFuture<Void> finished = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                runnable.run();
                finished.complete(null);
            } catch (final RuntimeException ex) {
                finished.completeExceptionally(ex);
            }
        });
        finished.join();
    }

    /**
     * Increments the tracked viewer window counter.
     *
     * @return the current number of tracked windows after incrementing
     */
    public static int registerTrackedViewerOpened() {
        return OPEN_TRACKED_WINDOWS.incrementAndGet();
    }

    /**
     * Decrements the tracked viewer window counter and exits JavaFX if the last window closed.
     *
     * <p>
     * When the remaining count reaches zero, JavaFX is exited via {@link Platform#exit()}.
     * </p>
     *
     * @return the remaining number of tracked windows (never negative)
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
