/*
 * Copyright (c) 2025 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.graphfx.api;

import com.mlprograms.justmath.graphfx.api.plot.GraphFxPlotCancellation;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxPlotEngine;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxPlotEngines;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxPlotGeometry;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxPlotRequest;
import com.mlprograms.justmath.graphfx.api.plot.GraphFxWorldBounds;
import com.mlprograms.justmath.graphfx.config.WindowConfig;
import com.mlprograms.justmath.graphfx.core.GraphFxPoint;
import com.mlprograms.justmath.graphfx.internal.FxBootstrap;
import com.mlprograms.justmath.graphfx.view.GraphFxDisplayPane;
import com.mlprograms.justmath.graphfx.view.internal.GraphFxSeriesOverlay;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JavaFX facade that provides a simple, stable API to plot expressions using GraphFx.
 *
 * <p>This class is the recommended entry point for JavaFX-based applications. It encapsulates:</p>
 * <ul>
 *   <li>an interactive coordinate system ({@link GraphFxDisplayPane})</li>
 *   <li>a background plot computation pipeline (core engine, no JavaFX operations)</li>
 *   <li>a multi-series overlay renderer</li>
 * </ul>
 *
 * <h2>Core / UI separation</h2>
 * <p>
 * Expression evaluation and geometry generation are delegated to a {@link GraphFxPlotEngine} (core layer).
 * The viewer only performs JavaFX rendering on the JavaFX Application Thread.
 * </p>
 *
 * <h2>Threading</h2>
 * <ul>
 *   <li>All public methods are expected to be called on the JavaFX Application Thread.</li>
 *   <li>Computations run on a dedicated background executor and are applied via {@link Platform#runLater(Runnable)}.</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <p>
 * When using {@link #show(String, double, double)}, the viewer automatically disposes itself when the window is closed.
 * If you embed the node using {@link #asNode()}, call {@link #dispose()} when you no longer need the viewer.
 * </p>
 *
 * <h2>Input validation</h2>
 * <p>
 * Public API methods validate inputs and throw clear exceptions. Expression evaluation errors are handled
 * robustly (invalid sample points are skipped rather than crashing the UI).
 * </p>
 */
public final class GraphFxPlotViewer {

    private static final long REDRAW_DEBOUNCE_MILLIS = 40;

    private final GraphFxDisplayPane displayPane;
    private final GraphFxSeriesOverlay overlay;
    private final Parent root;

    private final GraphFxPlotEngine plotEngine;

    private final ScheduledExecutorService computeExecutor;
    private final AtomicLong generation;

    private final Map<String, SeriesConfig> seriesByExpression;

    private Map<String, String> globalVariables;

    private volatile ScheduledFuture<?> scheduledRefresh;
    private volatile boolean disposed;

    private volatile Stage stage;

    /**
     * Creates a new viewer using the default plot engine and {@link DisplayTheme#LIGHT}.
     */
    public GraphFxPlotViewer() {
        this(DisplayTheme.LIGHT);
    }

    /**
     * Creates a new viewer using the default plot engine and the provided theme.
     *
     * @param theme initial theme (nullable -> treated as {@link DisplayTheme#LIGHT})
     */
    public GraphFxPlotViewer(final DisplayTheme theme) {
        this(GraphFxPlotEngines.defaultEngine(), theme);
    }

    /**
     * Creates a new viewer using a custom plot engine and the provided theme.
     *
     * @param plotEngine core plot engine to use (must not be {@code null})
     * @param theme      initial theme (nullable -> treated as {@link DisplayTheme#LIGHT})
     * @throws NullPointerException if {@code plotEngine} is {@code null}
     */
    public GraphFxPlotViewer(@NonNull final GraphFxPlotEngine plotEngine, final DisplayTheme theme) {
        this.plotEngine = plotEngine;

        this.displayPane = new GraphFxDisplayPane(theme);
        this.overlay = new GraphFxSeriesOverlay(displayPane);

        this.root = new javafx.scene.layout.StackPane(displayPane, overlay);

        this.computeExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r, "GraphFxPlotViewer-Compute");
            thread.setDaemon(true);
            return thread;
        });

        this.generation = new AtomicLong(0);
        this.seriesByExpression = new LinkedHashMap<>();
        this.globalVariables = Map.of();

        registerViewportAutoReplot();
    }

    /**
     * Returns the JavaFX node representing the viewer content.
     *
     * <p>Use this for embedding into your own JavaFX layouts.</p>
     *
     * @return viewer root node
     */
    public Parent asNode() {
        return root;
    }

    /**
     * Plots an expression using the provided stroke color.
     *
     * <p>If the expression was already plotted, its style is updated and the plot is recomputed.</p>
     *
     * @param expression     expression to plot (must not be blank)
     * @param strokeColorHex stroke color in CSS hex format (e.g. {@code "#FF5500"}); must not be {@code null}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if expression is blank or color is invalid
     * @throws IllegalStateException    if this viewer has been disposed
     */
    public void plotExpression(@NonNull final String expression, @NonNull final String strokeColorHex) {
        plotExpression(expression, strokeColorHex, 2.0);
    }

    /**
     * Plots an expression using the provided stroke color and stroke width.
     *
     * @param expression     expression to plot (must not be blank)
     * @param strokeColorHex stroke color in CSS hex format (e.g. {@code "#FF5500"}); must not be {@code null}
     * @param strokeWidth    stroke width in pixels; values {@code <= 0} are rejected
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if expression is blank, color is invalid, or strokeWidth is not positive
     * @throws IllegalStateException    if this viewer has been disposed
     */
    public void plotExpression(@NonNull final String expression, @NonNull final String strokeColorHex, final double strokeWidth) {
        ensureUsable();

        final String trimmed = expression.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("expression must not be blank.");
        }

        if (!(strokeWidth > 0.0) || Double.isNaN(strokeWidth) || Double.isInfinite(strokeWidth)) {
            throw new IllegalArgumentException("strokeWidth must be a positive finite number.");
        }

        final Color color = parseColor(strokeColorHex);

        seriesByExpression.put(trimmed, new SeriesConfig(trimmed, color, strokeWidth));
        scheduleReplot();
    }

    /**
     * Removes a previously plotted expression.
     *
     * @param expression expression key (must not be {@code null})
     * @return {@code true} if a series was removed; {@code false} otherwise
     * @throws NullPointerException  if {@code expression} is {@code null}
     * @throws IllegalStateException if disposed
     */
    public boolean removeExpression(@NonNull final String expression) {
        ensureUsable();

        final boolean removed = seriesByExpression.remove(expression.trim()) != null;
        if (removed) {
            scheduleReplot();
        }
        return removed;
    }

    /**
     * Clears all plotted expressions.
     *
     * @throws IllegalStateException if disposed
     */
    public void clear() {
        ensureUsable();
        seriesByExpression.clear();
        overlay.clear();
    }

    /**
     * Sets global variables applied to all plotted expressions (e.g. {@code a=2}).
     *
     * <p>The provided map is defensively copied and validated to not contain null keys/values.</p>
     *
     * @param variables variables map (must not be {@code null})
     * @throws NullPointerException  if {@code variables} is {@code null} or contains null keys/values
     * @throws IllegalStateException if disposed
     */
    public void setVariables(@NonNull final Map<String, String> variables) {
        ensureUsable();
        this.globalVariables = Map.copyOf(validateVariables(variables));
        scheduleReplot();
    }

    /**
     * Convenience method to set a single global variable.
     *
     * @param name  variable name (must not be blank)
     * @param value variable value (must not be {@code null})
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code name} is blank
     * @throws IllegalStateException    if disposed
     */
    public void setVariable(@NonNull final String name, @NonNull final String value) {
        ensureUsable();

        final String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Variable name must not be blank.");
        }

        final Map<String, String> updated = new LinkedHashMap<>(globalVariables);
        updated.put(trimmedName, value);
        setVariables(updated);
    }

    /**
     * Shows the viewer in a standalone window.
     *
     * <p>This method ensures the JavaFX runtime is started. The created window will dispose the viewer when closed.</p>
     *
     * @param title  window title (must not be blank)
     * @param width  initial window width (pixels). If {@code <= 0}, defaults are used.
     * @param height initial window height (pixels). If {@code <= 0}, defaults are used.
     * @throws NullPointerException     if {@code title} is {@code null}
     * @throws IllegalArgumentException if {@code title} is blank
     * @throws IllegalStateException    if disposed
     */
    public void show(@NonNull final String title, final double width, final double height) {
        ensureUsable();

        final String trimmedTitle = title.trim();
        if (trimmedTitle.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank.");
        }

        final double safeWidth = width > 0 ? width : WindowConfig.DEFAULT_WINDOW_WIDTH;
        final double safeHeight = height > 0 ? height : WindowConfig.DEFAULT_WINDOW_HEIGHT;

        this.stage = FxBootstrap.createAndShowStage(trimmedTitle, asNode(), safeWidth, safeHeight);
        this.stage.setOnHidden(e -> dispose());
    }

    /**
     * Disposes this viewer and releases background resources.
     *
     * <p>After disposal, all public methods (except {@link #dispose()} itself) will throw {@link IllegalStateException}.</p>
     */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;

        final ScheduledFuture<?> future = scheduledRefresh;
        if (future != null) {
            future.cancel(false);
        }

        computeExecutor.shutdownNow();
        overlay.clear();
        seriesByExpression.clear();
    }

    private void registerViewportAutoReplot() {
        displayPane.widthProperty().addListener(obs -> scheduleReplot());
        displayPane.heightProperty().addListener(obs -> scheduleReplot());

        displayPane.getScalePxPerUnit().addListener(obs -> scheduleReplot());
        displayPane.getOriginOffsetX().addListener(obs -> scheduleReplot());
        displayPane.getOriginOffsetY().addListener(obs -> scheduleReplot());
    }

    private void scheduleReplot() {
        if (disposed) {
            return;
        }

        final long gen = generation.incrementAndGet();

        final ScheduledFuture<?> previous = scheduledRefresh;
        if (previous != null) {
            previous.cancel(false);
        }

        scheduledRefresh = computeExecutor.schedule(() -> computeAndApply(gen), REDRAW_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void computeAndApply(final long gen) {
        if (disposed || gen != generation.get()) {
            return;
        }

        final double width = displayPane.getWidth();
        final double height = displayPane.getHeight();

        if (!(width > 1.0) || !(height > 1.0)) {
            return;
        }

        final GraphFxWorldBounds bounds = computeBounds(width, height);

        final GraphFxPlotCancellation cancellation = () -> disposed || gen != generation.get() || Thread.currentThread().isInterrupted();

        final List<GraphFxSeriesOverlay.Series> rendered = new ArrayList<>(seriesByExpression.size());

        for (final SeriesConfig config : seriesByExpression.values()) {
            if (cancellation.isCancelled()) {
                return;
            }

            final GraphFxPlotRequest request = new GraphFxPlotRequest(
                    config.expression(),
                    globalVariables,
                    bounds,
                    (int) Math.round(width),
                    (int) Math.round(height)
            );

            final GraphFxPlotGeometry geometry = plotEngine.plot(request, cancellation);
            rendered.add(new GraphFxSeriesOverlay.Series(config.color(), config.strokeWidth(), geometry.polyline(), geometry.segments()));
        }

        Platform.runLater(() -> {
            if (disposed || gen != generation.get()) {
                return;
            }
            overlay.setSeries(rendered);
        });
    }

    private GraphFxWorldBounds computeBounds(final double width, final double height) {
        final GraphFxPoint topLeft = displayPane.screenToWorld(new Point2D(0, 0));
        final GraphFxPoint bottomRight = displayPane.screenToWorld(new Point2D(width, height));

        return new GraphFxWorldBounds(topLeft.x(), bottomRight.x(), bottomRight.y(), topLeft.y()).normalized();
    }

    private static Map<String, String> validateVariables(@NonNull final Map<String, String> variables) {
        final Map<String, String> out = new LinkedHashMap<>(Math.max(8, variables.size()));
        for (final Map.Entry<String, String> e : variables.entrySet()) {
            final String key = Objects.requireNonNull(e.getKey(), "variable name must not be null");
            final String value = Objects.requireNonNull(e.getValue(), "variable '" + key + "' value must not be null");
            out.put(key, value);
        }
        return out;
    }

    private static Color parseColor(@NonNull final String colorHex) {
        final String trimmed = colorHex.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("strokeColorHex must not be blank.");
        }

        try {
            return Color.web(trimmed);
        } catch (final IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid color: '" + colorHex + "'. Expected CSS color (e.g. #RRGGBB).", exception);
        }
    }

    private void ensureUsable() {
        if (disposed) {
            throw new IllegalStateException("GraphFxPlotViewer is disposed and can no longer be used.");
        }
    }

    private record SeriesConfig(String expression, Color color, double strokeWidth) {
    }
}
