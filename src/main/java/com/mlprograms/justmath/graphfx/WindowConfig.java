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

import java.util.Objects;

/**
 * Immutable configuration for a {@code GraphFxViewer} window.
 *
 * <p>
 * This type is intentionally small and stable because it is part of the public API.
 * More rendering and interaction settings belong to view-specific configuration objects.
 * </p>
 *
 * @param title window title (must not be blank)
 * @param width initial window width in pixels (must be > 0)
 * @param height initial window height in pixels (must be > 0)
 * @param exitApplicationOnLastViewerClose whether JavaFX should exit once the last tracked viewer closes
 */
public record WindowConfig(
        /** Window title displayed in the native window chrome. */
        String title,
        /** Initial window width in pixels. */
        int width,
        /** Initial window height in pixels. */
        int height,
        /** Exit policy flag used by {@link com.mlprograms.justmath.graphfx.JavaFxRuntime}. */
        boolean exitApplicationOnLastViewerClose
) {

    /**
     * Default width for a viewer window.
     */
    public static final int DEFAULT_WIDTH = 1200;

    /**
     * Default height for a viewer window.
     */
    public static final int DEFAULT_HEIGHT = 800;

    /**
     * Validates and creates a new config record.
     */
    public WindowConfig {
        Objects.requireNonNull(title, "title must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }
    }

    /**
     * Creates a default configuration suitable for typical desktop usage.
     *
     * @return default window configuration
     */
    public static WindowConfig defaultConfig() {
        return new WindowConfig("GraphFx – Pan & Zoom", DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }
}
