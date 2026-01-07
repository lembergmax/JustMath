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

import com.mlprograms.justmath.graphfx.planar.view.GraphFxViewer;

import java.util.Objects;

/**
 * Immutable configuration for {@link GraphFxViewer}.
 *
 * @param title                            window title (must not be {@code null} or blank)
 * @param width                            initial width in pixels (must be {@code > 0})
 * @param height                           initial height in pixels (must be {@code > 0})
 * @param drawDemoCurve                    reserved for future demo overlays
 * @param exitApplicationOnLastViewerClose whether to exit when the last viewer window is closed
 */
public record WindowConfig(String title, int width, int height, boolean drawDemoCurve,
                           boolean exitApplicationOnLastViewerClose) {

    /**
     * Default width used by {@link #defaultConfig()}.
     */
    public static final int DEFAULT_WIDTH = 1200;

    /**
     * Default height used by {@link #defaultConfig()}.
     */
    public static final int DEFAULT_HEIGHT = 800;

    /**
     * Validates and creates a configuration instance.
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
     * Returns a default configuration.
     *
     * <p>By default, the application exits when the last viewer window is closed.</p>
     *
     * @return default configuration (never {@code null})
     */
    public static WindowConfig defaultConfig() {
        return new WindowConfig("GraphFx – Pan & Zoom", DEFAULT_WIDTH, DEFAULT_HEIGHT, true, true);
    }
}
