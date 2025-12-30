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
 *   <li>{@link #runLater(Runnable)} guarantees that the provided {@link Runnable} is executed
 *       on the JavaFX Application Thread (immediately if already on that thread, otherwise enqueued).</li>
 *   <li>{@link #showInWindow(String, Parent, double, double)} creates and shows a {@link Stage}
 *       on the JavaFX Application Thread.</li>
 * </ul>
 *
 * <h2>Idempotence and safety</h2>
 * <p>
 * Startup is guarded by an {@link AtomicBoolean} and a synchronization block to avoid double initialization.
 * If the JavaFX platform was already started elsewhere, {@link Platform#startup(Runnable)} may throw
 * an {@link IllegalStateException}; this is treated as a successful "already started" condition.
 * If the calling thread is interrupted while waiting for startup, the interrupt flag is restored and an
 * {@link IllegalStateException} is thrown to signal that initialization did not complete normally.
 * </p>
 *
 * <p><strong>Note:</strong> This class is package-private on purpose to keep the public API surface of the
 * graphfx module small while still allowing internal components to bootstrap JavaFX when needed.</p>
 */
@UtilityClass
public class FxBootstrap {

    /**
     * Tracks whether the JavaFX platform has been started (or is known to be started).
     * <p>
     * This flag is used as a fast path to avoid synchronization and repeated startup attempts
     * once initialization has completed.
     * </p>
     */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * Ensures that the JavaFX platform is initialized.
     * <p>
     * This method is safe to call multiple times and from any thread. It will start the JavaFX runtime
     * only once. If called on the JavaFX Application Thread, it marks the platform as started and returns
     * without invoking {@link Platform#startup(Runnable)}.
     * </p>
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>If the platform is already started (according to {@link #STARTED}), the method returns immediately.</li>
     *   <li>Otherwise, it synchronizes to ensure only a single thread attempts startup.</li>
     *   <li>If the platform is already started by another component, an {@link IllegalStateException} from
     *       {@link Platform#startup(Runnable)} is interpreted as "already started".</li>
     *   <li>If the current thread is interrupted while waiting for startup to complete, the interrupt status is
     *       restored and an {@link IllegalStateException} is thrown.</li>
     * </ul>
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
     * <p>
     * This method first ensures that the JavaFX platform is started. If the caller already runs on the
     * JavaFX Application Thread, the runnable is executed immediately in the current call stack.
     * Otherwise, it is scheduled via {@link Platform#runLater(Runnable)}.
     * </p>
     *
     * <p>
     * This helper is useful to centralize thread handling and avoid scattered {@code Platform.runLater(...)}
     * calls throughout the codebase.
     * </p>
     *
     * @param runnable the code to execute on the JavaFX Application Thread; must not be {@code null}
     * @throws NullPointerException  if {@code runnable} is {@code null}
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
     * Creates a new {@link Stage} and shows the given content inside a {@link Scene}.
     * <p>
     * This method is intended for simple "show this UI quickly" scenarios such as demos, diagnostics,
     * development tools, or test harnesses.
     * </p>
     *
     * <h3>Size handling</h3>
     * <p>
     * The provided {@code width} and {@code height} are treated as preferred initial window dimensions.
     * If either value is less than or equal to {@code 0}, a conservative fallback is used:
     * </p>
     * <ul>
     *   <li>Width fallback: {@code 800.0}</li>
     *   <li>Height fallback: {@code 600.0}</li>
     * </ul>
     *
     * <h3>Threading</h3>
     * <p>
     * The stage is created and shown on the JavaFX Application Thread via {@link #runLater(Runnable)}.
     * </p>
     *
     * @param title   the window title; must not be {@code null}
     * @param content the root node to display; must not be {@code null}
     * @param width   initial window width in pixels; values {@code <= 0} will be replaced by a fallback
     * @param height  initial window height in pixels; values {@code <= 0} will be replaced by a fallback
     * @throws NullPointerException  if {@code title} or {@code content} is {@code null}
     * @throws IllegalStateException if JavaFX startup is interrupted (see {@link #ensureStarted()})
     */
    public static void showInWindow(@NonNull final String title, @NonNull final Parent content, final double width, final double height) {
        ensureStarted();

        final double safeWidth = width > 0 ? width : 800.0;
        final double safeHeight = height > 0 ? height : 600.0;

        runLater(() -> {
            final Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(content, safeWidth, safeHeight));
            stage.show();
        });
    }

}
