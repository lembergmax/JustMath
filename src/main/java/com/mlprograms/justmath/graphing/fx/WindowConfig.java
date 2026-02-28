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

package com.mlprograms.justmath.graphing.fx;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Configuration for {@link com.mlprograms.justmath.graphing.fx.planar.view.GraphFxViewer}.
 * <p>
 * This type is immutable and library-friendly. Prefer {@link #defaultConfig()} as a sensible starting point.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class WindowConfig {

    /**
     * Default window title.
     */
    public static final String DEFAULT_TITLE = "GraphFxViewer";

    /**
     * Default window width (pixels).
     */
    public static final int DEFAULT_WIDTH = 1200;

    /**
     * Default window height (pixels).
     */
    public static final int DEFAULT_HEIGHT = 800;

    /**
     * Window title shown in the OS window chrome.
     */
    @NonNull
    String title;

    /**
     * Initial window width in pixels.
     */
    int width;

    /**
     * Initial window height in pixels.
     */
    int height;

    /**
     * If {@code true}, the JavaFX runtime exits when the last viewer window is closed.
     */
    boolean exitApplicationOnLastViewerClose;

    /**
     * Creates a default configuration.
     *
     * @return default configuration instance
     */
    public static WindowConfig defaultConfig() {
        return WindowConfig.builder()
                .title("GraphFx – Pan & Zoom")
                .width(DEFAULT_WIDTH)
                .height(DEFAULT_HEIGHT)
                .exitApplicationOnLastViewerClose(true)
                .build();
    }
}
