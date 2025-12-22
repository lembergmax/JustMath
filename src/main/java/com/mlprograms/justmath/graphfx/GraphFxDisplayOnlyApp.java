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

/**
 * Small convenience wrapper that exposes a {@link GraphFxDisplayPane} as a reusable "display-only" component.
 * <p>
 * The intent of this class is to provide a minimal, embeddable API for showing an interactive coordinate system
 * (grid + axes + tick labels) without any higher-level editor or function management UI.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <ul>
 *   <li>Embed the display pane into an existing JavaFX layout using {@link #asNode()}.</li>
 *   <li>Show the pane in its own window using {@link #show()} or {@link #show(String, double, double)}.</li>
 *   <li>Adjust runtime behavior (theme, origin centering) through the provided convenience methods.</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * <p>
 * JavaFX UI changes must occur on the JavaFX Application Thread. This class ensures safe execution by
 * delegating UI work to {@link FxBootstrap}. Callers may invoke these methods from any thread; operations
 * that mutate JavaFX state are executed on the correct thread via {@link FxBootstrap#runLater(Runnable)}.
 * </p>
 *
 * <p>
 * The wrapped {@link GraphFxDisplayPane} remains accessible via {@link #getPane()} for advanced customization
 * (e.g., binding properties, changing zoom bounds, toggling panning behavior).
 * </p>
 */
@Getter
public final class GraphFxDisplayOnlyApp {

    /**
     * The underlying display pane that renders the coordinate system and handles input gestures.
     */
    private final GraphFxDisplayPane pane;

    /**
     * Creates a new display-only app using {@link DisplayTheme#LIGHT}.
     * <p>
     * This constructor is primarily intended for quick demos and default usage.
     * </p>
     */
    public GraphFxDisplayOnlyApp() {
        this(DisplayTheme.LIGHT);
    }

    /**
     * Creates a new display-only app with a newly constructed {@link GraphFxDisplayPane} using the given theme.
     * <p>
     * The theme is passed to the pane constructor and normalized there (if applicable).
     * </p>
     *
     * @param theme the initial theme to use; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final DisplayTheme theme) {
        this(new GraphFxDisplayPane(theme));
    }

    /**
     * Creates a new display-only app wrapping an existing {@link GraphFxDisplayPane}.
     * <p>
     * This constructor is useful when the pane is configured elsewhere (custom scale limits, bindings,
     * input behavior, or test setup) and should then be exposed through this simplified API.
     * </p>
     *
     * @param pane the pane to wrap; must not be {@code null}
     * @throws NullPointerException if {@code pane} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final GraphFxDisplayPane pane) {
        this.pane = pane;
    }

    /**
     * Returns the wrapped pane as a {@link Parent} node for embedding into any JavaFX scene graph.
     * <p>
     * This method intentionally returns {@link Parent} (instead of the concrete type) to keep the public
     * surface minimal while still allowing direct use in layouts.
     * </p>
     *
     * @return the pane instance as a JavaFX node
     */
    public Parent asNode() {
        return pane;
    }

    /**
     * Centers the world origin (0,0) within the pane.
     * <p>
     * The operation is executed on the JavaFX Application Thread via {@link FxBootstrap#runLater(Runnable)}.
     * </p>
     */
    public void centerOrigin() {
        FxBootstrap.runLater(pane::centerOrigin);
    }

    /**
     * Updates the pane theme.
     * <p>
     * The update is executed on the JavaFX Application Thread to comply with JavaFX threading rules.
     * </p>
     *
     * @param theme the new theme; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public void setTheme(@NonNull final DisplayTheme theme) {
        FxBootstrap.runLater(() -> pane.setTheme(theme));
    }

    /**
     * Shows the pane in a standalone window using defaults from {@link WindowConfig}.
     * <p>
     * The window is created with:
     * </p>
     * <ul>
     *   <li>Title: {@link WindowConfig#DEFAULT_WINDOW_TITLE}</li>
     *   <li>Width: {@link WindowConfig#DEFAULT_WINDOW_WIDTH}</li>
     *   <li>Height: {@link WindowConfig#DEFAULT_WINDOW_HEIGHT}</li>
     * </ul>
     *
     * <p>
     * This method is intended for quick preview and "just show it" scenarios.
     * </p>
     */
    public void show() {
        show(WindowConfig.DEFAULT_WINDOW_TITLE, WindowConfig.DEFAULT_WINDOW_WIDTH, WindowConfig.DEFAULT_WINDOW_HEIGHT);
    }

    /**
     * Shows the pane in a standalone window with a custom title and initial dimensions.
     * <p>
     * JavaFX platform bootstrapping and stage creation are handled by {@link FxBootstrap#showInWindow(String, javafx.scene.Parent, double, double)}.
     * Width/height values that are non-positive are handled defensively by the bootstrap utility (fallback sizing).
     * </p>
     *
     * @param title  the stage title; must not be {@code null}
     * @param width  the preferred initial width in pixels; non-positive values are treated as invalid and may be replaced
     * @param height the preferred initial height in pixels; non-positive values are treated as invalid and may be replaced
     * @throws NullPointerException  if {@code title} is {@code null}
     * @throws IllegalStateException if JavaFX startup is interrupted (propagated from {@link FxBootstrap})
     */
    public void show(@NonNull final String title, final double width, final double height) {
        FxBootstrap.showInWindow(title, pane, width, height);
    }

}
