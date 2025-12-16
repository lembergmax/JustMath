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

package com.mlprograms.justmath.graphfx.util;

import javafx.application.Platform;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Small JavaFX bootstrap helper that allows creating/showing JavaFX windows
 * from a plain {@code public static void main(...)} method without extending
 * {@link javafx.application.Application} and without calling {@code Application.launch(...)}.
 * <p>
 * JavaFX requires its toolkit to be initialized before {@link Platform#runLater(Runnable)}
 * can be used. In a standard JavaFX app the toolkit is started by {@code Application.launch}.
 * In a library use-case (your case), callers might want to open a window by simply calling
 * a method from any context (even a non-JavaFX application). This class ensures:
 * <ul>
 *   <li>JavaFX toolkit is initialized exactly once via {@link Platform#startup(Runnable)}</li>
 *   <li>Work is executed on the JavaFX Application Thread</li>
 *   <li>Callers can optionally block until the work completes</li>
 * </ul>
 */
@NoArgsConstructor
public final class FxBootstrap {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * Ensures that the JavaFX toolkit has been started.
     * <p>
     * This method is idempotent and safe to call multiple times.
     * If JavaFX is already running, {@link Platform#startup(Runnable)} throws
     * an {@link IllegalStateException}; this is expected and ignored.
     */
    public static void init() {
        if (STARTED.get()) {
            return;
        }
        synchronized (STARTED) {
            if (STARTED.get()) {
                return;
            }
            try {
                Platform.startup(() -> {
                });
                Platform.setImplicitExit(true);
            } catch (IllegalStateException ignored) {
            }
            STARTED.set(true);
        }
    }

    /**
     * Runs a supplier on the JavaFX Application Thread and blocks until it completes.
     * <p>
     * If called from the JavaFX Application Thread, the supplier is executed immediately.
     *
     * @param supplier code to run on the FX thread
     * @param <T>      return type
     * @return supplier result
     */
    public static <T> T callAndWait(final Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        init();

        if (Platform.isFxApplicationThread()) {
            return supplier.get();
        }

        final CompletableFuture<T> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future.join();
    }

}
