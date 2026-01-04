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

package com.mlprograms.justmath.graphfx.internal;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Small bootstrap utility that ensures the JavaFX runtime is initialized exactly once and offers
 * safe helpers to execute code on the JavaFX Application Thread.
 *
 * <h2>Why this exists</h2>
 * <p>
 * JavaFX requires a running toolkit to create stages/scenes and to interact with UI components.
 * In "library" contexts (unit tests, CLI tools, background services, or modular applications),
 * there may be no {@code Application.launch(...)} entry point. In such scenarios, this helper
 * provides a predictable way to start the JavaFX platform on demand.
 * </p>
 *
 * <h2>Threading model</h2>
 * <ul>
 *   <li>{@link #ensureStarted()} can be called from any thread.</li>
 *   <li>{@link #runLater(Runnable)} runs the task on the JavaFX Application Thread.</li>
 *   <li>{@link #createAndShowStage(String, Parent, double, double)} creates and shows a {@link Stage} on the JavaFX thread.</li>
 * </ul>
 *
 * <h2>Idempotence and safety</h2>
 * <p>
 * Startup is guarded by an {@link AtomicBoolean} and a synchronization block to avoid double initialization.
 * If the JavaFX platform was already started elsewhere, {@link Platform#startup(Runnable)} may throw
 * an {@link IllegalStateException}; this is treated as a successful "already started" condition.
 * If the calling thread is interrupted while waiting for startup, the interrupt flag is restored and an
 * {@link IllegalStateException} is thrown.
 * </p>
 *
 * <p><strong>Note:</strong> This class is located in an {@code internal} package because it is not intended to be
 * a stable public API surface. It may change between versions.</p>
 */
@UtilityClass
public class FxBootstrap {

    /**
     * Tracks whether the JavaFX platform has been started (or is known to be started).
     */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * Ensures that the JavaFX platform is initialized.
     *
     * <p>This method is safe to call multiple times and from any thread.</p>
     *
     * @throws IllegalStateException if the thread is interrupted while waiting for JavaFX startup to complete
     */
    public static void ensureStarted() {
        if (STARTED.get()) {
            return;
        }

        synchronized (FxBootstrap.class) {
            if (STARTED.get()) {
                return;
            }

            if (Platform.isFxApplicationThread()) {
                STARTED.set(true);
                return;
            }

            final CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(() -> {
                    STARTED.set(true);
                    latch.countDown();
                });
                latch.await();
            } catch (final IllegalStateException alreadyStarted) {
                STARTED.set(true);
            } catch (final InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while starting JavaFX.", interruptedException);
            }
        }
    }

    /**
     * Executes the given runnable on the JavaFX Application Thread.
     *
     * <p>If the caller already runs on the JavaFX Application Thread, the runnable is executed immediately.
     * Otherwise, it is scheduled via {@link Platform#runLater(Runnable)}.</p>
     *
     * @param runnable the code to execute on the JavaFX Application Thread; must not be {@code null}
     * @throws NullPointerException if {@code runnable} is {@code null}
     * @throws IllegalStateException if JavaFX startup is interrupted (see {@link #ensureStarted()})
     */
    public static void runLater(@NonNull final Runnable runnable) {
        ensureStarted();

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

    /**
     * Creates and shows a {@link Stage} for the given content.
     *
     * <p>This overload returns the created stage which enables callers to register lifecycle hooks (e.g. close handlers).</p>
     *
     * @param title window title; must not be {@code null}
     * @param content root node to display; must not be {@code null}
     * @param width initial width in pixels; values {@code <= 0} use a fallback
     * @param height initial height in pixels; values {@code <= 0} use a fallback
     * @return the created stage instance
     * @throws NullPointerException if {@code title} or {@code content} is {@code null}
     * @throws IllegalStateException if JavaFX startup is interrupted
     */
    public static Stage createAndShowStage(@NonNull final String title, @NonNull final Parent content, final double width, final double height) {
        ensureStarted();

        final double safeWidth = width > 0 ? width : 800.0;
        final double safeHeight = height > 0 ? height : 600.0;

        final Stage[] stageHolder = new Stage[1];
        final CountDownLatch latch = new CountDownLatch(1);

        runLater(() -> {
            final Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(content, safeWidth, safeHeight));
            stage.show();

            stageHolder[0] = stage;
            latch.countDown();
        });

        try {
            latch.await();
        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while creating JavaFX window.", interruptedException);
        }

        return stageHolder[0];
    }

    /**
     * Convenience method that creates and shows a window for the given content.
     *
     * <p>Prefer {@link #createAndShowStage(String, Parent, double, double)} when you need to access the {@link Stage}.</p>
     *
     * @param title window title; must not be {@code null}
     * @param content root node to display; must not be {@code null}
     * @param width initial width in pixels; values {@code <= 0} use a fallback
     * @param height initial height in pixels; values {@code <= 0} use a fallback
     * @throws NullPointerException if {@code title} or {@code content} is {@code null}
     * @throws IllegalStateException if JavaFX startup is interrupted
     */
    public static void showInWindow(@NonNull final String title, @NonNull final Parent content, final double width, final double height) {
        createAndShowStage(title, content, width, height);
    }

}
