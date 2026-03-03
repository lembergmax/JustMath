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

package com.mlprograms.justmath.graphfx.runtime;

import javafx.application.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JavaFX runtime bootstrap utility for library-like usage.
 *
 * <p>
 * JavaFX typically expects applications to extend {@code javafx.application.Application} and call
 * {@code Application.launch(...)}. For reusable libraries, that requirement is often undesirable because library
 * consumers want to instantiate UI components directly (e.g. {@code new GraphFxViewer().show()}).
 * </p>
 *
 * <p>
 * This class provides a safe, minimal API to:
 * </p>
 *
 * <ul>
 *     <li>Initialize the JavaFX Toolkit exactly once per JVM.</li>
 *     <li>Execute actions on the JavaFX Application Thread.</li>
 *     <li>Execute actions on the JavaFX Application Thread and wait for completion.</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JavaFxRuntime {

    /**
     * Synchronization lock used to guarantee one-time initialization.
     */
    private static final Object TOOLKIT_INITIALIZATION_LOCK = new Object();

    /**
     * Indicates whether the JavaFX Toolkit has already been initialized.
     *
     * <p>
     * This flag is {@code volatile} to ensure visibility across threads.
     * </p>
     */
    private static volatile boolean isToolkitInitialized = false;

    /**
     * Ensures that the JavaFX Toolkit is initialized.
     *
     * <p>
     * This method is idempotent and thread-safe. Internally it calls {@link Platform#startup(Runnable)} exactly once.
     * If the toolkit is already running (e.g. because the host application called {@code Application.launch(...)}),
     * JavaFX throws an {@link IllegalStateException}, which is intentionally swallowed.
     * </p>
     */
    public static void ensureToolkitInitialized() {
        if (isToolkitInitialized) {
            return;
        }

        synchronized (TOOLKIT_INITIALIZATION_LOCK) {
            if (isToolkitInitialized) {
                return;
            }

            try {
                Platform.startup(() -> {
                    // Intentionally empty: starting the toolkit is the only purpose of this call.
                });
            } catch (IllegalStateException ignored) {
                // Toolkit already initialized.
            }

            isToolkitInitialized = true;
        }
    }

    /**
     * Executes the given action on the JavaFX Application Thread.
     *
     * <p>
     * If the caller is already on the JavaFX Application Thread, the action is executed immediately.
     * Otherwise it is scheduled via {@link Platform#runLater(Runnable)}.
     * </p>
     *
     * @param action the action to execute (must not be null)
     */
    public static void runOnFxThread(@NonNull Runnable action) {
        ensureToolkitInitialized();

        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        Platform.runLater(action);
    }

    /**
     * Executes the given action on the JavaFX Application Thread and blocks until it has completed.
     *
     * <p>
     * This is useful for constructors that must perform JavaFX node wiring on the FX thread before returning.
     * </p>
     *
     * @param action the action to execute (must not be null)
     */
    public static void runOnFxThreadAndWait(@NonNull Runnable action) {
        ensureToolkitInitialized();

        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch completionLatch = new CountDownLatch(1);
        AtomicReference<Throwable> thrownThrowableReference = new AtomicReference<>(null);

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                thrownThrowableReference.set(throwable);
            } finally {
                completionLatch.countDown();
            }
        });

        try {
            completionLatch.await();
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for JavaFX action completion.", interruptedException);
        }

        Throwable thrownThrowable = thrownThrowableReference.get();
        if (thrownThrowable == null) {
            return;
        }

        if (thrownThrowable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }

        throw new IllegalStateException("JavaFX action failed with a checked exception.", thrownThrowable);
    }

    /**
     * Requests a clean JavaFX shutdown.
     *
     * <p>
     * In typical desktop applications this is not required because JavaFX exits automatically once the last window is
     * closed (implicit exit enabled by default). This method exists for CLI tools and tests that want explicit control.
     * </p>
     */
    public static void shutdown() {
        ensureToolkitInitialized();
        Platform.exit();
    }
}
