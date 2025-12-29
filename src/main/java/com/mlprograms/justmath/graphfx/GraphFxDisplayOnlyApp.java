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

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphfx.config.WindowConfig;
import javafx.beans.InvalidationListener;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A small, embeddable "display-only" GraphFx component that renders a {@link GraphFxDisplayPane} (grid + axes + labels)
 * and provides an overlay layer for drawing:
 * <ul>
 *     <li>Points</li>
 *     <li>A polyline</li>
 *     <li>Plots derived from JustMath expressions</li>
 * </ul>
 *
 * <h2>Plotting and BigNumber</h2>
 * <p>
 * Expression evaluation and curve extraction is delegated to {@link GraphFxCalculator}. Internally, the calculator uses
 * JustMath's {@link BigNumber} and a {@code CalculatorEngine} to evaluate expressions with high precision.
 * </p>
 *
 * <p>
 * This class intentionally keeps the viewport representation in {@code double} world coordinates because the scene graph,
 * mouse input and rasterization all operate in doubles/pixels. However, expression variables can be provided as
 * {@link BigNumber} (see {@link #plotExpression(String, Map)}) and are converted losslessly to string form for evaluation
 * by the calculator/engine.
 * </p>
 *
 * <h2>Performance design</h2>
 * <p>
 * Plot computation can be expensive (BigNumber evaluation, marching squares, etc.). For responsive pan/zoom:
 * </p>
 * <ul>
 *     <li>All plot computations run on a dedicated background worker thread (never on the JavaFX UI thread).</li>
 *     <li>Pan/zoom/resize triggers are <strong>debounced</strong> to avoid continuous recomputation during dragging.</li>
 *     <li>Outdated computations are cancelled aggressively (interrupt + generation counter).</li>
 *     <li>Overlay drawing is coalesced to at most one redraw per JavaFX pulse.</li>
 * </ul>
 *
 * <h2>Threading contract</h2>
 * <p>
 * Public API methods may be called from any thread. UI mutations are marshalled onto the JavaFX Application Thread via
 * {@link FxBootstrap#runLater(Runnable)}.
 * </p>
 */
@Getter
public final class GraphFxDisplayOnlyApp implements AutoCloseable {

    private static final long PLOT_DEBOUNCE_MS = 80L;

    private final GraphFxDisplayPane pane;

    private final Canvas overlayCanvas;
    private final StackPane root;

    private final List<Point2D> pointsWorld;
    private final List<Point2D> polylineWorld;
    private final List<GraphFxCalculator.LineSegment> contourSegmentsWorld;

    private Color pointColor;
    private Color lineColor;
    private double pointRadiusPx;
    private double lineWidthPx;

    private final GraphFxCalculator calculator;

    private PlotRequest plotRequest;

    private final Object plotLock;
    private final ScheduledExecutorService plotScheduler;
    private final ExecutorService plotExecutor;
    private final AtomicLong plotGeneration;

    private ScheduledFuture<?> scheduledPlotStart;
    private Future<?> runningPlotTask;

    private boolean overlayRedrawScheduled;

    /**
     * Creates a new instance using {@link DisplayTheme#LIGHT}.
     * <p>
     * This constructor is intended for convenience. Internally, it delegates to
     * {@link #GraphFxDisplayOnlyApp(DisplayTheme)}.
     * </p>
     */
    public GraphFxDisplayOnlyApp() {
        this(DisplayTheme.LIGHT);
    }

    /**
     * Creates a new instance with a newly constructed {@link GraphFxDisplayPane} using the given theme.
     *
     * <p>
     * The pane is wrapped into an internal {@link StackPane} together with a transparent overlay {@link Canvas}.
     * The overlay is used to render points, polylines, and expression plots without interfering with the pane's own
     * rendering logic (grid/axes/labels).
     * </p>
     *
     * @param theme the initial theme; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final DisplayTheme theme) {
        this(new GraphFxDisplayPane(theme));
    }

    /**
     * Creates a new instance by wrapping an existing {@link GraphFxDisplayPane}.
     *
     * <p>
     * Use this constructor if you want to configure the pane externally (e.g., binding properties, customizing theme
     * behavior, installing additional listeners) before handing it to this wrapper.
     * </p>
     *
     * <p>
     * This wrapper adds:
     * </p>
     * <ul>
     *     <li>an overlay canvas bound to the pane size</li>
     *     <li>debounced plot recomputation on viewport changes</li>
     *     <li>coalesced overlay redraw</li>
     * </ul>
     *
     * @param pane the pane to wrap; must not be {@code null}
     * @throws NullPointerException if {@code pane} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final GraphFxDisplayPane pane) {
        this.pane = pane;

        this.overlayCanvas = new Canvas();
        this.root = new StackPane(this.pane, this.overlayCanvas);

        this.pointsWorld = new ArrayList<>();
        this.polylineWorld = new ArrayList<>();
        this.contourSegmentsWorld = new ArrayList<>();

        this.pointColor = Color.RED;
        this.lineColor = Color.DODGERBLUE;
        this.pointRadiusPx = 4.0;
        this.lineWidthPx = 2.0;

        this.calculator = new GraphFxCalculator();

        this.plotLock = new Object();
        this.plotGeneration = new AtomicLong(0L);

        this.plotScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "GraphFx-PlotScheduler");
            t.setDaemon(true);
            return t;
        });

        this.plotExecutor = Executors.newSingleThreadExecutor(r -> {
            final Thread t = new Thread(r, "GraphFx-PlotWorker");
            t.setDaemon(true);
            return t;
        });

        FxBootstrap.runLater(() -> {
            configureOverlayCanvas();
            registerOverlayRedrawTriggers();
            scheduleOverlayRedraw();
        });
    }

    /**
     * Returns the embeddable JavaFX node that represents this component.
     *
     * <p>
     * The returned node is a {@link StackPane} containing:
     * </p>
     * <ul>
     *     <li>the underlying {@link GraphFxDisplayPane}</li>
     *     <li>a transparent overlay {@link Canvas} used for custom drawing</li>
     * </ul>
     *
     * <p>
     * You can add the returned node to any JavaFX scene graph, e.g. as the root of a {@code Scene} or inside a layout.
     * </p>
     *
     * @return the root node (pane + overlay)
     */
    public Parent asNode() {
        return root;
    }

    /**
     * Replaces the currently drawn overlay points.
     *
     * <p>
     * Points are defined in <strong>world coordinates</strong>. They are transformed into screen coordinates based on the
     * pane's current origin offset and scale (pixels per unit). Points are drawn as filled circles on the overlay canvas.
     * </p>
     *
     * <p>
     * This method is safe to call from any thread. The internal state mutation and redraw will run on the JavaFX thread.
     * </p>
     *
     * @param worldPoints points in world coordinates; must not be {@code null} and must not contain {@code null} entries
     * @throws NullPointerException if {@code worldPoints} is {@code null} or contains {@code null}
     */
    public void setPoints(@NonNull final List<Point2D> worldPoints) {
        final List<Point2D> safeCopy = copyPoints(worldPoints);

        FxBootstrap.runLater(() -> {
            pointsWorld.clear();
            pointsWorld.addAll(safeCopy);
            scheduleOverlayRedraw();
        });
    }

    /**
     * Replaces the currently drawn overlay polyline.
     *
     * <p>
     * The polyline is rendered as a single stroked path connecting the points in the order provided.
     * If fewer than two points are provided, no polyline is drawn.
     * </p>
     *
     * <p>
     * Setting a polyline clears any previously plotted expression geometry to avoid mixing multiple plot sources
     * unintentionally.
     * </p>
     *
     * @param worldPolyline polyline vertices in world coordinates; must not be {@code null} and must not contain {@code null} entries
     * @throws NullPointerException if {@code worldPolyline} is {@code null} or contains {@code null}
     */
    public void setPolyline(@NonNull final List<Point2D> worldPolyline) {
        final List<Point2D> safeCopy = copyPoints(worldPolyline);

        FxBootstrap.runLater(() -> {
            polylineWorld.clear();
            polylineWorld.addAll(safeCopy);

            contourSegmentsWorld.clear();
            plotRequest = null;

            cancelPlotWork();
            scheduleOverlayRedraw();
        });
    }

    /**
     * Clears all overlay drawings (points, polyline, and expression plot).
     *
     * <p>
     * This does <strong>not</strong> alter the underlying {@link GraphFxDisplayPane} (grid/axes/labels remain visible).
     * It only clears the overlay layer.
     * </p>
     */
    public void clearOverlay() {
        FxBootstrap.runLater(() -> {
            pointsWorld.clear();
            polylineWorld.clear();
            contourSegmentsWorld.clear();
            plotRequest = null;

            cancelPlotWork();
            scheduleOverlayRedraw();
        });
    }

    /**
     * Plots a JustMath expression using {@link BigNumber}-based variables.
     *
     * <p>
     * This overload is the recommended API when you already work with JustMath's {@link BigNumber} and want to avoid
     * converting numeric values to {@code double} or locale-dependent strings yourself.
     * The values are converted via {@link BigNumber#toString()} and then fed into the calculator/engine.
     * </p>
     *
     * <p>
     * Note: Viewport bounds and sampling are still derived from the JavaFX coordinate system and therefore use
     * {@code double}. The high-precision part is the expression evaluation performed by the JustMath engine.
     * </p>
     *
     * @param expression the expression to plot; must not be {@code null}
     * @param variables  external variables as {@link BigNumber}; must not be {@code null} and must not contain {@code null} keys/values
     * @throws NullPointerException if {@code expression} or {@code variables} is {@code null}, or if any key/value is {@code null}
     */
    public void plotExpression(@NonNull final String expression, @NonNull final Map<String, BigNumber> variables) {
        final Map<String, String> converted = convertBigNumberVariables(variables);

        FxBootstrap.runLater(() -> {
            plotRequest = new PlotRequest(expression, converted);

            polylineWorld.clear();
            contourSegmentsWorld.clear();

            schedulePlotUpdate(0L);
            scheduleOverlayRedraw();
        });
    }

    /**
     * Updates the visual style used for drawing points on the overlay.
     *
     * <p>
     * The radius is interpreted in <strong>screen pixels</strong>. If the provided radius is not positive, a defensive
     * minimum of {@code 1.0} is used.
     * </p>
     *
     * @param color    point fill color; must not be {@code null}
     * @param radiusPx radius in pixels; values {@code <= 0} will be clamped to {@code 1.0}
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setPointStyle(@NonNull final Color color, final double radiusPx) {
        final double safeRadius = radiusPx > 0 ? radiusPx : 1.0;

        FxBootstrap.runLater(() -> {
            this.pointColor = color;
            this.pointRadiusPx = safeRadius;
            scheduleOverlayRedraw();
        });
    }

    /**
     * Updates the visual style used for drawing lines on the overlay (polyline and expression plots).
     *
     * <p>
     * The width is interpreted in <strong>screen pixels</strong>. If the provided width is not positive, a defensive
     * minimum of {@code 1.0} is used.
     * </p>
     *
     * @param color   line stroke color; must not be {@code null}
     * @param widthPx stroke width in pixels; values {@code <= 0} will be clamped to {@code 1.0}
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setLineStyle(@NonNull final Color color, final double widthPx) {
        final double safeWidth = widthPx > 0 ? widthPx : 1.0;

        FxBootstrap.runLater(() -> {
            this.lineColor = color;
            this.lineWidthPx = safeWidth;
            scheduleOverlayRedraw();
        });
    }

    /**
     * Centers the world origin (0,0) within the pane.
     *
     * <p>
     * This affects the transformation from world coordinates to screen coordinates, which in turn changes where overlay
     * geometry is rendered. After centering, a plot update will be scheduled automatically by the viewport listeners.
     * </p>
     */
    public void centerOrigin() {
        FxBootstrap.runLater(pane::centerOrigin);
    }

    /**
     * Updates the theme of the underlying {@link GraphFxDisplayPane}.
     *
     * <p>
     * This affects the pane's own rendering (grid, axes, labels) but also influences the perceived readability of
     * overlay drawings. The overlay will be redrawn after applying the theme.
     * </p>
     *
     * @param theme the new theme; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public void setTheme(@NonNull final DisplayTheme theme) {
        FxBootstrap.runLater(() -> {
            pane.setTheme(theme);
            scheduleOverlayRedraw();
        });
    }

    /**
     * Shows this component in a standalone window using defaults from {@link WindowConfig}.
     *
     * <p>
     * This is a convenience method for quick visual testing or for using the display component in a minimal app.
     * </p>
     */
    public void show() {
        show(WindowConfig.DEFAULT_WINDOW_TITLE, WindowConfig.DEFAULT_WINDOW_WIDTH, WindowConfig.DEFAULT_WINDOW_HEIGHT);
    }

    /**
     * Shows this component in a standalone window with a custom title and initial dimensions.
     *
     * <p>
     * The returned stage lifecycle is managed by {@link FxBootstrap}. This method does not block the calling thread.
     * </p>
     *
     * @param title  stage title; must not be {@code null}
     * @param width  preferred width in pixels
     * @param height preferred height in pixels
     * @throws NullPointerException  if {@code title} is {@code null}
     * @throws IllegalStateException if JavaFX startup is interrupted (propagated by {@link FxBootstrap})
     */
    public void show(@NonNull final String title, final double width, final double height) {
        FxBootstrap.showInWindow(title, root, width, height);
    }

    /**
     * Disposes this instance and shuts down internal executors.
     *
     * <p>
     * Call this method when the component is no longer needed to avoid thread leaks in long-running applications.
     * It is safe to call multiple times.
     * </p>
     *
     * <p>
     * Disposing will:
     * </p>
     * <ul>
     *     <li>cancel any scheduled plot starts</li>
     *     <li>interrupt any running plot computation</li>
     *     <li>shutdown the scheduler and worker executors</li>
     * </ul>
     */
    public void dispose() {
        cancelPlotWork();
        plotScheduler.shutdownNow();
        plotExecutor.shutdownNow();
    }

    /**
     * Equivalent to {@link #dispose()} to support try-with-resources usage.
     */
    @Override
    public void close() {
        dispose();
    }

    /**
     * Internal immutable plot request that captures the expression and its external variables.
     *
     * @param expression expression string (not normalized here; normalization is handled by {@link GraphFxCalculator})
     * @param variables  external variables map (copied defensively before being stored here)
     */
    private record PlotRequest(String expression, Map<String, String> variables) {
    }

    /**
     * Creates an immutable defensive copy of a list of points while validating that no entry is {@code null}.
     *
     * <p>
     * This method is intentionally strict: null points are not tolerated because they would lead to failures during
     * world-to-screen transformation and drawing.
     * </p>
     *
     * @param points input list; must not be {@code null}
     * @return immutable copy of the list
     * @throws NullPointerException if the list or any element is {@code null}
     */
    private static List<Point2D> copyPoints(@NonNull final List<Point2D> points) {
        for (int i = 0; i < points.size(); i++) {
            Objects.requireNonNull(points.get(i), "points[" + i + "] must not be null");
        }

        return List.copyOf(points);
    }

    /**
     * Converts a variable map of {@link BigNumber} values into the string format expected by the underlying
     * JustMath engine.
     *
     * <p>
     * Keys and values are validated. The returned map is immutable.
     * </p>
     *
     * @param variables BigNumber-based variables; must not be {@code null} and must not contain {@code null} keys/values
     * @return immutable string-based variables map
     * @throws NullPointerException if the map or any key/value is {@code null}
     */
    private static Map<String, String> convertBigNumberVariables(@NonNull final Map<String, BigNumber> variables) {
        final Map<String, String> out = new HashMap<>(Math.max(8, variables.size()));
        for (final Map.Entry<String, BigNumber> entry : variables.entrySet()) {
            final String key = Objects.requireNonNull(entry.getKey(), "variable key must not be null");
            final BigNumber value = Objects.requireNonNull(entry.getValue(), "variable '" + key + "' must not be null");
            out.put(key, value.toString());
        }

        return Map.copyOf(out);
    }

    /**
     * Configures the overlay canvas so it always matches the size of the underlying pane and does not intercept input.
     *
     * <p>
     * The canvas is made mouse-transparent so the user can still interact with the {@link GraphFxDisplayPane}
     * (panning/zooming) normally.
     * </p>
     */
    private void configureOverlayCanvas() {
        overlayCanvas.setMouseTransparent(true);
        overlayCanvas.widthProperty().bind(pane.widthProperty());
        overlayCanvas.heightProperty().bind(pane.heightProperty());
    }

    /**
     * Registers listeners that react to viewport changes and trigger:
     * <ul>
     *     <li>a debounced background recomputation of the plotted geometry (if an expression plot is active)</li>
     *     <li>a coalesced overlay redraw</li>
     * </ul>
     *
     * <p>
     * The listeners observe:
     * </p>
     * <ul>
     *     <li>overlay canvas size changes (resize)</li>
     *     <li>pane scale changes (zoom)</li>
     *     <li>pane origin offsets changes (pan)</li>
     * </ul>
     */
    private void registerOverlayRedrawTriggers() {
        final InvalidationListener listener = obs -> {
            schedulePlotUpdate(PLOT_DEBOUNCE_MS);
            scheduleOverlayRedraw();
        };

        overlayCanvas.widthProperty().addListener(listener);
        overlayCanvas.heightProperty().addListener(listener);

        pane.getScalePxPerUnit().addListener(listener);
        pane.getOriginOffsetX().addListener(listener);
        pane.getOriginOffsetY().addListener(listener);
    }

    /**
     * Schedules a debounced plot recomputation for the current viewport.
     *
     * <p>
     * This method must be invoked on the JavaFX thread because it reads JavaFX properties (canvas size, pane transforms).
     * It captures immutable snapshots (request, bounds, pixel sizes) and schedules the actual computation on the
     * background executor after the given debounce delay.
     * </p>
     *
     * <p>
     * Cancellation strategy:
     * </p>
     * <ul>
     *     <li>A monotonically increasing generation counter marks all previously scheduled/running jobs as obsolete.</li>
     *     <li>Any scheduled start is cancelled and replaced.</li>
     *     <li>Any running worker task is interrupted.</li>
     * </ul>
     *
     * @param debounceMs debounce time in milliseconds (0 for immediate scheduling)
     */
    private void schedulePlotUpdate(final long debounceMs) {
        if (plotRequest == null) {
            return;
        }

        final PlotRequest requestSnapshot = plotRequest;
        final GraphFxCalculator.WorldBounds boundsSnapshot = currentViewportWorldBounds();

        final int pixelWidth = (int) Math.max(1.0, overlayCanvas.getWidth());
        final int pixelHeight = (int) Math.max(1.0, overlayCanvas.getHeight());

        final long generation = plotGeneration.incrementAndGet();

        synchronized (plotLock) {
            cancelScheduledPlotStartLocked();
            cancelRunningPlotTaskLocked();

            scheduledPlotStart = plotScheduler.schedule(() -> submitPlotComputation(requestSnapshot, boundsSnapshot, pixelWidth, pixelHeight, generation), Math.max(0L, debounceMs), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Submits the plot computation to the single-thread worker executor.
     *
     * <p>
     * This method runs on the scheduler thread. It double-checks that the generation is still current before enqueuing
     * work, preventing unnecessary computations when the user keeps panning/zooming.
     * </p>
     *
     * @param request     plot request snapshot
     * @param bounds      viewport bounds snapshot
     * @param pixelWidth  width of the viewport in pixels
     * @param pixelHeight height of the viewport in pixels
     * @param generation  generation id that must still match {@link #plotGeneration}
     */
    private void submitPlotComputation(@NonNull final PlotRequest request, @NonNull final GraphFxCalculator.WorldBounds bounds, final int pixelWidth, final int pixelHeight, final long generation) {
        synchronized (plotLock) {
            if (plotGeneration.get() != generation) {
                return;
            }

            runningPlotTask = plotExecutor.submit(() -> computePlotGeometry(request, bounds, pixelWidth, pixelHeight, generation));
        }
    }

    /**
     * Computes the plot geometry on the worker thread and publishes results back to the JavaFX thread.
     *
     * <p>
     * The actual plotting work is delegated to {@link GraphFxCalculator#plot(String, Map, GraphFxCalculator.WorldBounds, int, int, GraphFxCalculator.PlotCancellation)}.
     * That method evaluates expressions using JustMath's {@link BigNumber} and returns either:
     * </p>
     * <ul>
     *     <li>a polyline for explicit functions {@code y=f(x)}</li>
     *     <li>line segments for implicit curves {@code F(x,y)=0}</li>
     * </ul>
     *
     * <p>
     * Cancellation is checked frequently by providing a {@link GraphFxCalculator.PlotCancellation} implementation that
     * reacts to interruption and generation changes.
     * </p>
     *
     * @param request     plot request snapshot
     * @param bounds      viewport bounds snapshot
     * @param pixelWidth  width of the viewport in pixels
     * @param pixelHeight height of the viewport in pixels
     * @param generation  generation id that must still match {@link #plotGeneration}
     */
    private void computePlotGeometry(@NonNull final PlotRequest request, @NonNull final GraphFxCalculator.WorldBounds bounds, final int pixelWidth, final int pixelHeight, final long generation) {
        final GraphFxCalculator.PlotCancellation cancellation = () -> Thread.currentThread().isInterrupted() || plotGeneration.get() != generation;

        final GraphFxCalculator.PlotGeometry geometry = calculator.plot(request.expression(), request.variables(), bounds, pixelWidth, pixelHeight, cancellation);

        if (cancellation.isCancelled()) {
            return;
        }

        FxBootstrap.runLater(() -> {
            if (plotGeneration.get() != generation) {
                return;
            }

            contourSegmentsWorld.clear();
            polylineWorld.clear();

            contourSegmentsWorld.addAll(geometry.segments());
            polylineWorld.addAll(geometry.polyline());

            scheduleOverlayRedraw();
        });
    }

    /**
     * Cancels all scheduled and running plot work.
     *
     * <p>
     * This method is used when:
     * </p>
     * <ul>
     *     <li>clearing the overlay</li>
     *     <li>switching from expression plotting to a manually defined polyline</li>
     *     <li>disposing the instance</li>
     * </ul>
     *
     * <p>
     * It increments the generation counter (invalidating previous work), cancels any scheduled start, and interrupts
     * the running worker task.
     * </p>
     */
    private void cancelPlotWork() {
        plotGeneration.incrementAndGet();

        synchronized (plotLock) {
            cancelScheduledPlotStartLocked();
            cancelRunningPlotTaskLocked();
        }
    }

    /**
     * Cancels the currently scheduled plot start, if present.
     *
     * <p>
     * Must only be called while holding {@link #plotLock}.
     * </p>
     */
    private void cancelScheduledPlotStartLocked() {
        final ScheduledFuture<?> scheduled = scheduledPlotStart;
        if (scheduled != null) {
            scheduled.cancel(true);
            scheduledPlotStart = null;
        }
    }

    /**
     * Cancels the currently running plot worker task, if present.
     *
     * <p>
     * Must only be called while holding {@link #plotLock}.
     * The worker is interrupted to shorten cancellation latency.
     * </p>
     */
    private void cancelRunningPlotTaskLocked() {
        final Future<?> running = runningPlotTask;
        if (running != null) {
            running.cancel(true);
            runningPlotTask = null;
        }
    }

    /**
     * Computes the currently visible world bounds based on the pane's size, origin offsets and scale.
     *
     * <p>
     * The bounds are expressed in world coordinates (units), not in pixels.
     * These bounds are used to decide:
     * </p>
     * <ul>
     *     <li>the sampling range for explicit plots</li>
     *     <li>the grid range for implicit contour extraction</li>
     * </ul>
     *
     * @return the current viewport bounds in world coordinates
     */
    private GraphFxCalculator.WorldBounds currentViewportWorldBounds() {
        final double width = Math.max(1.0, pane.getWidth());
        final double height = Math.max(1.0, pane.getHeight());

        final double scale = pane.getScalePxPerUnit().get();
        final double originX = pane.getOriginOffsetX().get();
        final double originY = pane.getOriginOffsetY().get();

        final double minX = (0.0 - originX) / scale;
        final double maxX = (width - originX) / scale;

        final double minY = (originY - height) / scale;
        final double maxY = (originY - 0.0) / scale;

        return new GraphFxCalculator.WorldBounds(minX, maxX, minY, maxY);
    }

    /**
     * Schedules a single overlay redraw on the next JavaFX pulse.
     *
     * <p>
     * Overlay redraws can be triggered rapidly (mouse drag events, property invalidations). This method coalesces
     * redraw requests so the overlay is painted at most once per pulse, reducing the time spent in canvas drawing.
     * </p>
     */
    private void scheduleOverlayRedraw() {
        if (overlayRedrawScheduled) {
            return;
        }

        overlayRedrawScheduled = true;
        FxBootstrap.runLater(() -> {
            overlayRedrawScheduled = false;
            redrawOverlayNow();
        });
    }

    /**
     * Immediately redraws the overlay canvas.
     *
     * <p>
     * This method clears the overlay and then draws:
     * </p>
     * <ol>
     *     <li>implicit contour segments (if present)</li>
     *     <li>explicit polyline (if present)</li>
     *     <li>points (if present)</li>
     * </ol>
     *
     * <p>
     * The drawing order ensures plots are visible underneath points.
     * </p>
     */
    private void redrawOverlayNow() {
        final double width = overlayCanvas.getWidth();
        final double height = overlayCanvas.getHeight();

        final GraphicsContext graphicsContext = overlayCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, width, height);

        drawContourSegments(graphicsContext);
        drawPolyline(graphicsContext);
        drawPoints(graphicsContext);
    }

    /**
     * Draws the cached implicit plot segments (if any) onto the overlay canvas.
     *
     * <p>
     * Each segment is defined in world coordinates and is transformed to screen coordinates on-the-fly.
     * </p>
     *
     * @param graphicsContext target graphics context; must not be {@code null}
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawContourSegments(@NonNull final GraphicsContext graphicsContext) {
        if (contourSegmentsWorld.isEmpty()) {
            return;
        }

        graphicsContext.setStroke(lineColor);
        graphicsContext.setLineWidth(lineWidthPx);

        for (final GraphFxCalculator.LineSegment segment : contourSegmentsWorld) {
            final Point2D a = worldToScreen(segment.a());
            final Point2D b = worldToScreen(segment.b());
            graphicsContext.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
    }

    /**
     * Draws the cached polyline (if any) onto the overlay canvas.
     *
     * <p>
     * The polyline is drawn as a single path for performance reasons (one stroke call).
     * </p>
     *
     * @param graphicsContext target graphics context; must not be {@code null}
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawPolyline(@NonNull final GraphicsContext graphicsContext) {
        if (polylineWorld.size() < 2) {
            return;
        }

        graphicsContext.setStroke(lineColor);
        graphicsContext.setLineWidth(lineWidthPx);

        final Point2D first = worldToScreen(polylineWorld.get(0));
        graphicsContext.beginPath();
        graphicsContext.moveTo(first.getX(), first.getY());

        for (int i = 1; i < polylineWorld.size(); i++) {
            final Point2D screenPoint = worldToScreen(polylineWorld.get(i));
            graphicsContext.lineTo(screenPoint.getX(), screenPoint.getY());
        }

        graphicsContext.stroke();
    }

    /**
     * Draws all configured points (if any) onto the overlay canvas.
     *
     * <p>
     * Each point is rendered as a filled circle with the configured radius and color.
     * </p>
     *
     * @param graphicsContext target graphics context; must not be {@code null}
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawPoints(@NonNull final GraphicsContext graphicsContext) {
        if (pointsWorld.isEmpty()) {
            return;
        }

        graphicsContext.setFill(pointColor);

        for (final Point2D worldPoint : pointsWorld) {
            final Point2D screenPoint = worldToScreen(worldPoint);

            final double x = screenPoint.getX() - pointRadiusPx;
            final double y = screenPoint.getY() - pointRadiusPx;

            graphicsContext.fillOval(x, y, pointRadiusPx * 2.0, pointRadiusPx * 2.0);
        }
    }

    /**
     * Converts a world coordinate point to screen (pixel) coordinates.
     *
     * <p>
     * The transformation is defined by:
     * </p>
     * <ul>
     *     <li>{@code screenX = originX + worldX * scale}</li>
     *     <li>{@code screenY = originY - worldY * scale}</li>
     * </ul>
     *
     * <p>
     * The Y axis is inverted because JavaFX screen coordinates increase downward.
     * </p>
     *
     * @param worldPoint point in world coordinates; must not be {@code null}
     * @return transformed point in screen coordinates
     * @throws NullPointerException if {@code worldPoint} is {@code null}
     */
    private Point2D worldToScreen(@NonNull final Point2D worldPoint) {
        final double scale = pane.getScalePxPerUnit().get();
        final double originX = pane.getOriginOffsetX().get();
        final double originY = pane.getOriginOffsetY().get();

        final double screenX = originX + worldPoint.getX() * scale;
        final double screenY = originY - worldPoint.getY() * scale;

        return new Point2D(screenX, screenY);
    }

}
