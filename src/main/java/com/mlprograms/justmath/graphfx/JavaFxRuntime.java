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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal and robust JavaFX runtime bootstrap for library usage.
 *
 * <p>
 * This class ensures that the JavaFX toolkit is initialized exactly once and provides
 * convenience methods to execute code on the JavaFX Application Thread.
 * </p>
 *
 * <p>
 * The viewer library uses {@link Platform#setImplicitExit(boolean)} with {@code false}
 * so that opening/closing windows does not kill the JavaFX runtime unexpectedly.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JavaFxRuntime {

    private static final Object START_LOCK = new Object();
    private static volatile boolean started;

    private static final AtomicInteger OPEN_TRACKED_WINDOWS = new AtomicInteger(0);

    /**
     * Ensures the JavaFX toolkit is started.
     *
     * <p>
     * Safe to call multiple times. If JavaFX is already running (e.g. started by another
     * UI component), this method will simply mark the runtime as started.
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

            CompletableFuture<Void> ready = new CompletableFuture<>();
            try {
                Platform.startup(() -> ready.complete(null));
                ready.join();
            } catch (IllegalStateException alreadyRunning) {
                // Toolkit already initialized by someone else.
            } finally {
                started = true;
            }
        }
    }

    /**
     * Runs the given task on the JavaFX Application Thread as soon as possible.
     *
     * @param runnable task to execute (must not be null)
     */
    public static void runOnFxThread(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");

        ensureStarted();

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

    /**
     * Runs the given task on the JavaFX Application Thread and blocks until it finished.
     *
     * @param runnable task to execute (must not be null)
     */
    public static void runOnFxThreadAndWait(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");

        ensureStarted();

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        CompletableFuture<Void> done = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                runnable.run();
                done.complete(null);
            } catch (RuntimeException ex) {
                done.completeExceptionally(ex);
            }
        });

        done.join();
    }

    /**
     * Registers a viewer window as opened for "exit-on-last-close" policy.
     *
     * @return current number of tracked open windows after increment
     */
    public static int registerTrackedViewerOpened() {
        return OPEN_TRACKED_WINDOWS.incrementAndGet();
    }

    /**
     * Registers a viewer window as closed. If the last tracked viewer closes, the JavaFX runtime exits.
     *
     * @return remaining number of tracked open windows after decrement
     */
    public static int registerTrackedViewerClosedAndExitIfLast() {
        int remaining = OPEN_TRACKED_WINDOWS.decrementAndGet();
        if (remaining <= 0) {
            OPEN_TRACKED_WINDOWS.set(0);
            Platform.runLater(Platform::exit);
            return 0;
        }
        return remaining;
    }

}