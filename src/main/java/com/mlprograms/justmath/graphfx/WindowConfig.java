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

public record WindowConfig(String title, int width, int height, boolean exitApplicationOnLastViewerClose) {

    public static final String DEFAULT_TITLE = "GraphFxViewer";
    public static final int DEFAULT_WIDTH = 1200;
    public static final int DEFAULT_HEIGHT = 800;

    public WindowConfig() {
        this(DEFAULT_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }

    public WindowConfig(final String title) {
        this(title, DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }

    public WindowConfig(final int width, final int height) {
        this(DEFAULT_TITLE, width, height, true);
    }

    public WindowConfig(final boolean exitApplicationOnLastViewerClose) {
        this(DEFAULT_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT, exitApplicationOnLastViewerClose);
    }

    public WindowConfig(final String title, final boolean exitApplicationOnLastViewerClose) {
        this(title, DEFAULT_WIDTH, DEFAULT_HEIGHT, exitApplicationOnLastViewerClose);
    }

    public WindowConfig(final int width, final int height, final boolean exitApplicationOnLastViewerClose) {
        this(DEFAULT_TITLE, width, height, exitApplicationOnLastViewerClose);
    }

    public WindowConfig(final String title, final int width, final int height) {
        this(title, width, height, true);
    }

    public static WindowConfig defaultConfig() {
        return new WindowConfig("GraphFx – Pan & Zoom", DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }

}
