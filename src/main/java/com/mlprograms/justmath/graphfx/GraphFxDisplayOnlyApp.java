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


import com.mlprograms.justmath.graphfx.config.WindowConfig;
import javafx.scene.Parent;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GraphFxDisplayOnlyApp {

    private final GraphFxDisplayPane pane;

    public GraphFxDisplayOnlyApp() {
        this(DisplayTheme.LIGHT);
    }

    public GraphFxDisplayOnlyApp(@NonNull final DisplayTheme theme) {
        this(new GraphFxDisplayPane(theme));
    }

    public GraphFxDisplayOnlyApp(@NonNull final GraphFxDisplayPane pane) {
        this.pane = pane;
    }

    public Parent asNode() {
        return pane;
    }

    public void centerOrigin() {
        FxBootstrap.runLater(pane::centerOrigin);
    }

    public void setTheme(@NonNull final DisplayTheme theme) {
        FxBootstrap.runLater(() -> pane.setTheme(theme));
    }

    public void show() {
        show(WindowConfig.DEFAULT_WINDOW_TITLE, WindowConfig.DEFAULT_WINDOW_WIDTH, WindowConfig.DEFAULT_WINDOW_HEIGHT);
    }

    public void show(@NonNull final String title, final double width, final double height) {
        FxBootstrap.showInWindow(title, pane, width, height);
    }

}