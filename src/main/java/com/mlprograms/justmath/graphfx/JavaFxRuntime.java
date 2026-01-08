package com.mlprograms.justmath.graphfx;

import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class JavaFxRuntime {

    private static final Object START_LOCK = new Object();
    private static volatile boolean started;

    private static final AtomicInteger OPEN_TRACKED_WINDOWS = new AtomicInteger(0);

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

    public static void runOnFxThread(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

    public static void enqueueOnFxThread(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        Platform.runLater(runnable);
    }

    public static int registerTrackedViewerOpened() {
        return OPEN_TRACKED_WINDOWS.incrementAndGet();
    }

    public static int registerTrackedViewerClosedAndExitIfLast() {
        final int remaining = OPEN_TRACKED_WINDOWS.decrementAndGet();
        if (remaining <= 0) {
            OPEN_TRACKED_WINDOWS.set(0);
            enqueueOnFxThread(Platform::exit);
        }
        return Math.max(0, remaining);
    }

}
