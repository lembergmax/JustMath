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

package com.mlprograms.justmath.graphfx;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Safe JavaFX bootstrap helper for library usage outside a JavaFX Application.
 */
@UtilityClass
public class FxBootstrap {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

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

    public static void runLater(@NonNull final Runnable runnable) {
        ensureStarted();

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

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
