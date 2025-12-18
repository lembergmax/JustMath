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

import com.mlprograms.justmath.graphfx.util.FxBootstrap;

import java.util.function.Supplier;

import java.util.concurrent.atomic.AtomicBoolean;

public final class FxTestSupport {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private FxTestSupport() {
    }

    public static void initFx() {
        if (INITIALIZED.compareAndSet(false, true)) {
            FxBootstrap.init();
        }
    }

    public static <T> T onFxThread(final Supplier<T> supplier) {
        initFx();
        return FxBootstrap.callAndWait(supplier);
    }

    public static void onFxThread(final Runnable runnable) {
        onFxThread(() -> {
            runnable.run();
            return null;
        });
    }

    public static void flushFxEvents() {
        onFxThread(() -> null);
    }

}


