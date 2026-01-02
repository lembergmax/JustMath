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

package com.mlprograms.justmath.graphfx.api;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphfx.config.WindowConfig;
import com.mlprograms.justmath.graphfx.core.GraphFxCalculatorEngine;
import com.mlprograms.justmath.graphfx.core.GraphFxPoint;
import com.mlprograms.justmath.graphfx.internal.FxBootstrap;
import com.mlprograms.justmath.graphfx.view.GraphFxDisplayPane;
import javafx.beans.InvalidationListener;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
 * A lightweight JavaFX plot viewer that renders GraphFx expressions and user-managed overlay elements.
 *
 * <p>This class is designed as a library-friendly facade around {@link GraphFxDisplayPane}. It provides:</p>
 * <ul>
 *   <li>an embeddable JavaFX {@link Parent} via {@link #asNode()} for integration into existing JavaFX applications,</li>
 *   <li>an optional self-managed window via {@link #show()} / {@link #show(String, double, double)},</li>
 *   <li>expression plotting with cancellation and debounced recomputation,</li>
 *   <li>id-based overlay management for polylines and points (add/remove/style individual elements).</li>
 * </ul>
 *
 * <h2>Threading and safety</h2>
 * <p>
 * All public API methods are safe to call from any thread. Every UI mutation is dispatched onto the JavaFX Application
 * Thread using {@link FxBootstrap#runLater(Runnable)}.
 * </p>
 *
 * <h2>Lifetime</h2>
 * <p>
 * The viewer owns background executors for plot computation. Call {@link #dispose()} (or {@link #close()}) when the
 * instance is no longer required to prevent thread leaks.
 * </p>
 */
public final class GraphFxPlotViewer implements AutoCloseable {

    /**
     * Debounce delay in milliseconds used for plot recomputation to avoid heavy redraws while the viewport is changing.
     */
    private static final long PLOT_DEBOUNCE_MILLISECONDS = 80L;

    /**
     * Default overlay point color used by {@link #addPoint(Point2D)} and {@link #setPoints(List)}.
     */
    private static final Color DEFAULT_POINT_COLOR = Color.RED;

    /**
     * Default overlay point radius in pixels used by {@link #addPoint(Point2D)} and {@link #setPoints(List)}.
     */
    private static final double DEFAULT_POINT_RADIUS_PIXELS = 4.0;

    /**
     * Default overlay polyline stroke color used by {@link #addPolyline(List)} and {@link #setPolyline(List)}.
     */
    private static final Color DEFAULT_POLYLINE_COLOR = Color.DODGERBLUE;

    /**
     * Default overlay polyline stroke width in pixels used by {@link #addPolyline(List)} and {@link #setPolyline(List)}.
     */
    private static final double DEFAULT_POLYLINE_WIDTH_PIXELS = 2.0;

    /**
     * Default stroke width in pixels used for expression plots created through {@link #plotExpression(String, String)}
     * and {@link #plotExpression(String, Map, String)}.
     */
    private static final double DEFAULT_EXPRESSION_PLOT_WIDTH_PIXELS = 2.0;

    /**
     * The underlying display pane that provides coordinate system, scaling, and theme management.
     *
     * <p>This value is never {@code null}.</p>
     */
    @Getter
    private final GraphFxDisplayPane pane;

    /**
     * Canvas used exclusively for drawing overlays and expression polylines/segments above the display pane.
     */
    private final Canvas overlayCanvas;

    /**
     * Root node containing the display pane and overlay canvas.
     */
    private final StackPane root;

    /**
     * Mutable list of user-managed overlay points. All mutations happen on the JavaFX Application Thread.
     */
    private final List<OverlayPoint> overlayPoints;

    /**
     * Mutable list of user-managed overlay polylines. All mutations happen on the JavaFX Application Thread.
     */
    private final List<OverlayPolyline> overlayPolylines;

    /**
     * Mutable list of expression plots. Each plot has an id and stores computed geometry.
     * All mutations happen on the JavaFX Application Thread.
     */
    private final List<ExpressionPlot> expressionPlots;

    /**
     * Default point color applied when the user does not provide an explicit point color.
     */
    private Color defaultPointColor;

    /**
     * Default point radius in pixels applied when the user does not provide an explicit point radius.
     */
    private double defaultPointRadiusPixels;

    /**
     * Default polyline color applied when the user does not provide an explicit polyline color.
     */
    private Color defaultPolylineColor;

    /**
     * Default polyline stroke width in pixels applied when the user does not provide an explicit polyline stroke width.
     */
    private double defaultPolylineWidthPixels;

    /**
     * Calculator used to generate plot geometry from expressions.
     */
    private final GraphFxCalculatorEngine graphFxCalculatorEngine;

    /**
     * Synchronization lock guarding plot scheduling and cancellation fields.
     */
    private final Object plotWorkLock;

    /**
     * Scheduler used to implement debouncing before submitting expensive plot calculations.
     */
    private final ScheduledExecutorService plotSchedulerExecutorService;

    /**
     * Single-thread executor used to compute plots in sequence and allow fast cancellation.
     */
    private final ExecutorService plotComputationExecutorService;

    /**
     * Monotonically increasing generation number that invalidates older plot tasks.
     */
    private final AtomicLong plotGenerationCounter;

    /**
     * Monotonically increasing id sequence used for overlays and expression plots.
     */
    private final AtomicLong elementIdentifierSequence;

    /**
     * Scheduled task handle for the next debounced plot calculation.
     */
    private ScheduledFuture<?> scheduledPlotStartFuture;

    /**
     * Running computation task handle for cancellation.
     */
    private Future<?> runningPlotComputationFuture;

    /**
     * Flag to ensure overlay redraw is scheduled at most once per pulse.
     */
    private boolean overlayRedrawScheduled;

    /**
     * Optional stage owned by this viewer when {@link #show()} or {@link #show(String, double, double)} is used.
     */
    private Stage stage;

    /**
     * Scene owned by {@link #stage} when a window is created.
     */
    private Scene scene;

    /**
     * Creates a new viewer using {@link DisplayTheme#LIGHT}.
     *
     * <p>The viewer can be embedded via {@link #asNode()} or shown as a standalone window via {@link #show()}.</p>
     */
    public GraphFxPlotViewer() {
        this(DisplayTheme.LIGHT);
    }

    /**
     * Creates a new viewer using the given theme.
     *
     * <p>The theme is applied to an internally created {@link GraphFxDisplayPane} instance.</p>
     *
     * @param theme the initial theme to use; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public GraphFxPlotViewer(@NonNull final DisplayTheme theme) {
        this(new GraphFxDisplayPane(theme));
    }

    /**
     * Creates a new viewer by wrapping an existing {@link GraphFxDisplayPane}.
     *
     * <p>This constructor is useful when you want full control over the display pane configuration before
     * instantiating the viewer.</p>
     *
     * @param pane the pane to wrap; must not be {@code null}
     * @throws NullPointerException if {@code pane} is {@code null}
     */
    public GraphFxPlotViewer(@NonNull final GraphFxDisplayPane pane) {
        this.pane = pane;

        this.overlayCanvas = new Canvas();
        this.root = new StackPane(this.pane, this.overlayCanvas);

        this.overlayPoints = new ArrayList<>();
        this.overlayPolylines = new ArrayList<>();
        this.expressionPlots = new ArrayList<>();

        this.defaultPointColor = DEFAULT_POINT_COLOR;
        this.defaultPointRadiusPixels = DEFAULT_POINT_RADIUS_PIXELS;

        this.defaultPolylineColor = DEFAULT_POLYLINE_COLOR;
        this.defaultPolylineWidthPixels = DEFAULT_POLYLINE_WIDTH_PIXELS;

        this.graphFxCalculatorEngine = new GraphFxCalculatorEngine();

        this.plotWorkLock = new Object();
        this.plotGenerationCounter = new AtomicLong(0L);
        this.elementIdentifierSequence = new AtomicLong(1L);

        this.plotSchedulerExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            final Thread schedulerThread = new Thread(runnable, "GraphFx-PlotScheduler");
            schedulerThread.setDaemon(true);
            return schedulerThread;
        });

        this.plotComputationExecutorService = Executors.newSingleThreadExecutor(runnable -> {
            final Thread workerThread = new Thread(runnable, "GraphFx-PlotWorker");
            workerThread.setDaemon(true);
            return workerThread;
        });

        FxBootstrap.runLater(() -> {
            configureOverlayCanvasFx();
            registerViewportListenersFx();
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Returns an embeddable JavaFX node that contains the display pane and the overlay layer.
     *
     * <p>
     * This node can be inserted into any JavaFX layout. The overlay canvas automatically binds its size to the
     * display pane, ensuring overlays always match the visible viewport.
     * </p>
     *
     * @return a non-null JavaFX {@link Parent} node
     */
    public Parent asNode() {
        return root;
    }

    /**
     * Shows this viewer in its own JavaFX window using {@link WindowConfig} defaults.
     *
     * <p>
     * If the window was never created, this method creates it. If the window already exists, it is brought to front.
     * </p>
     */
    public void show() {
        show(WindowConfig.DEFAULT_WINDOW_TITLE, WindowConfig.DEFAULT_WINDOW_WIDTH, WindowConfig.DEFAULT_WINDOW_HEIGHT);
    }

    /**
     * Shows this viewer in its own JavaFX window.
     *
     * <p>
     * If the window was never created, it is created with the provided dimensions. If it already exists, it is resized
     * and brought to the foreground.
     * </p>
     *
     * @param title  the window title; must not be {@code null}
     * @param width  requested window width in pixels; values smaller than 1 are clamped
     * @param height requested window height in pixels; values smaller than 1 are clamped
     * @throws NullPointerException if {@code title} is {@code null}
     */
    public void show(@NonNull final String title, final double width, final double height) {
        final double safeWidthPixels = Math.max(1.0, width);
        final double safeHeightPixels = Math.max(1.0, height);

        FxBootstrap.runLater(() -> {
            ensureStageFx(safeWidthPixels, safeHeightPixels);
            stage.setTitle(title);
            stage.setWidth(safeWidthPixels);
            stage.setHeight(safeHeightPixels);
            stage.show();
            stage.toFront();
            scheduleOverlayRedrawFx();
            schedulePlotUpdateFx(0L);
        });
    }

    /**
     * Hides the window if it is currently displayed.
     *
     * <p>If the viewer is embedded via {@link #asNode()}, this method has no effect on the embedded usage.</p>
     */
    public void hide() {
        FxBootstrap.runLater(() -> {
            if (stage != null) {
                stage.hide();
            }
        });
    }

    /**
     * Adds a single overlay point using the current default point style.
     *
     * <p>
     * The point is stored in world coordinates. It is converted to screen coordinates during rendering based on the
     * current viewport transform of {@link #getPane()}.
     * </p>
     *
     * @param worldPoint the point in world coordinates; must not be {@code null}
     * @return a stable identifier that can be used to remove or re-style the point later
     * @throws NullPointerException if {@code worldPoint} is {@code null}
     */
    public long addPoint(@NonNull final Point2D worldPoint) {
        return addPoint(worldPoint, defaultPointColor, defaultPointRadiusPixels);
    }

    /**
     * Adds a single overlay point with an individual style.
     *
     * <p>
     * The provided radius is interpreted in pixels (screen space). Values less than or equal to zero are clamped to 1
     * pixel to ensure the point is visible and rendering remains stable.
     * </p>
     *
     * @param worldPoint   the point in world coordinates; must not be {@code null}
     * @param color        the fill color for the point; must not be {@code null}
     * @param radiusPixels the point radius in pixels; values &lt;= 0 are clamped to 1
     * @return a stable identifier that can be used to remove or re-style the point later
     * @throws NullPointerException if {@code worldPoint} or {@code color} is {@code null}
     */
    public long addPoint(@NonNull final Point2D worldPoint, @NonNull final Color color, final double radiusPixels) {
        final long pointIdentifier = elementIdentifierSequence.getAndIncrement();
        final double safeRadiusPixels = radiusPixels > 0 ? radiusPixels : 1.0;

        FxBootstrap.runLater(() -> {
            overlayPoints.add(new OverlayPoint(pointIdentifier, worldPoint, color, safeRadiusPixels));
            scheduleOverlayRedrawFx();
        });

        return pointIdentifier;
    }

    /**
     * Removes a single overlay point by identifier.
     *
     * <p>If the identifier is unknown, this method performs no changes and does not throw.</p>
     *
     * @param pointIdentifier the point identifier returned by {@link #addPoint(Point2D)} or {@link #addPoint(Point2D, Color, double)}
     */
    public void removePoint(final long pointIdentifier) {
        FxBootstrap.runLater(() -> {
            final boolean removed = overlayPoints.removeIf(overlayPoint -> overlayPoint.id == pointIdentifier);
            if (removed) {
                scheduleOverlayRedrawFx();
            }
        });
    }

    /**
     * Removes all overlay points.
     *
     * <p>This does not affect polylines or expression plots.</p>
     */
    public void clearPoints() {
        FxBootstrap.runLater(() -> {
            overlayPoints.clear();
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Sets the default style applied by {@link #addPoint(Point2D)} and {@link #setPoints(List)}.
     *
     * <p>
     * Default styles only affect points added afterwards (or via replacement methods). Existing points are not modified.
     * </p>
     *
     * @param color        the new default point color; must not be {@code null}
     * @param radiusPixels the new default radius in pixels; values &lt;= 0 are clamped to 1
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setDefaultPointStyle(@NonNull final Color color, final double radiusPixels) {
        final double safeRadiusPixels = radiusPixels > 0 ? radiusPixels : 1.0;

        FxBootstrap.runLater(() -> {
            defaultPointColor = color;
            defaultPointRadiusPixels = safeRadiusPixels;
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Updates the style of an existing overlay point.
     *
     * <p>If the identifier is unknown, this method performs no changes and does not throw.</p>
     *
     * @param pointIdentifier the point identifier returned by {@link #addPoint(Point2D)} or {@link #addPoint(Point2D, Color, double)}
     * @param color           the new fill color; must not be {@code null}
     * @param radiusPixels    the new radius in pixels; values &lt;= 0 are clamped to 1
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setPointStyle(final long pointIdentifier, @NonNull final Color color, final double radiusPixels) {
        final double safeRadiusPixels = radiusPixels > 0 ? radiusPixels : 1.0;

        FxBootstrap.runLater(() -> {
            for (int overlayPointIndex = 0; overlayPointIndex < overlayPoints.size(); overlayPointIndex++) {
                final OverlayPoint overlayPoint = overlayPoints.get(overlayPointIndex);
                if (overlayPoint.id == pointIdentifier) {
                    overlayPoints.set(
                            overlayPointIndex,
                            new OverlayPoint(pointIdentifier, overlayPoint.worldPoint, color, safeRadiusPixels)
                    );
                    scheduleOverlayRedrawFx();
                    break;
                }
            }
        });
    }

    /**
     * Adds a polyline overlay using the current default polyline style.
     *
     * <p>
     * The polyline is defined by a list of world-coordinate points. During rendering, each point is transformed into
     * screen space using the current viewport settings of the display pane.
     * </p>
     *
     * @param worldPolyline points in world coordinates; must not be {@code null} and must not contain {@code null}
     * @return a stable identifier that can be used to remove or re-style the polyline later
     * @throws NullPointerException if {@code worldPolyline} is {@code null} or contains {@code null}
     */
    public long addPolyline(@NonNull final List<Point2D> worldPolyline) {
        return addPolyline(worldPolyline, defaultPolylineColor, defaultPolylineWidthPixels);
    }

    /**
     * Adds a polyline overlay with an individual style.
     *
     * <p>
     * The provided width is interpreted in pixels (screen space). Values less than or equal to zero are clamped to 1
     * pixel to ensure visibility and stable rendering.
     * </p>
     *
     * @param worldPolyline points in world coordinates; must not be {@code null} and must not contain {@code null}
     * @param color         stroke color; must not be {@code null}
     * @param widthPixels   stroke width in pixels; values &lt;= 0 are clamped to 1
     * @return a stable identifier that can be used to remove or re-style the polyline later
     * @throws NullPointerException if {@code worldPolyline} is {@code null} orl contains {@code null}, or if {@code color} is {@code null}
     */
    public long addPolyline(@NonNull final List<Point2D> worldPolyline, @NonNull final Color color, final double widthPixels) {
        final List<Point2D> safePolylinePoints = copyPoints(worldPolyline);
        final long polylineIdentifier = elementIdentifierSequence.getAndIncrement();
        final double safeWidthPixels = widthPixels > 0 ? widthPixels : 1.0;

        FxBootstrap.runLater(() -> {
            overlayPolylines.add(new OverlayPolyline(polylineIdentifier, safePolylinePoints, color, safeWidthPixels));
            scheduleOverlayRedrawFx();
        });

        return polylineIdentifier;
    }

    /**
     * Removes a single polyline by identifier.
     *
     * <p>If the identifier is unknown, this method performs no changes and does not throw.</p>
     *
     * @param polylineIdentifier the polyline identifier returned by {@link #addPolyline(List)} or {@link #addPolyline(List, Color, double)}
     */
    public void removePolyline(final long polylineIdentifier) {
        FxBootstrap.runLater(() -> {
            final boolean removed = overlayPolylines.removeIf(overlayPolyline -> overlayPolyline.id == polylineIdentifier);
            if (removed) {
                scheduleOverlayRedrawFx();
            }
        });
    }

    /**
     * Removes all overlay polylines.
     *
     * <p>This does not affect points or expression plots.</p>
     */
    public void clearPolylines() {
        FxBootstrap.runLater(() -> {
            overlayPolylines.clear();
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Sets the default style applied by {@link #addPolyline(List)} and {@link #setPolyline(List)}.
     *
     * <p>
     * Default styles only affect polylines added afterwards (or via replacement methods). Existing polylines are not modified.
     * </p>
     *
     * @param color       the new default stroke color; must not be {@code null}
     * @param widthPixels the new default stroke width in pixels; values &lt;= 0 are clamped to 1
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setDefaultPolylineStyle(@NonNull final Color color, final double widthPixels) {
        final double safeWidthPixels = widthPixels > 0 ? widthPixels : 1.0;

        FxBootstrap.runLater(() -> {
            defaultPolylineColor = color;
            defaultPolylineWidthPixels = safeWidthPixels;
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Updates the style of an existing overlay polyline.
     *
     * <p>If the identifier is unknown, this method performs no changes and does not throw.</p>
     *
     * @param polylineIdentifier the polyline identifier returned by {@link #addPolyline(List)} or {@link #addPolyline(List, Color, double)}
     * @param color              the new stroke color; must not be {@code null}
     * @param widthPixels        the new stroke width in pixels; values &lt;= 0 are clamped to 1
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setPolylineStyle(final long polylineIdentifier, @NonNull final Color color, final double widthPixels) {
        final double safeWidthPixels = widthPixels > 0 ? widthPixels : 1.0;

        FxBootstrap.runLater(() -> {
            for (int overlayPolylineIndex = 0; overlayPolylineIndex < overlayPolylines.size(); overlayPolylineIndex++) {
                final OverlayPolyline overlayPolyline = overlayPolylines.get(overlayPolylineIndex);
                if (overlayPolyline.id == polylineIdentifier) {
                    overlayPolylines.set(
                            overlayPolylineIndex,
                            new OverlayPolyline(polylineIdentifier, overlayPolyline.worldPolyline, color, safeWidthPixels)
                    );
                    scheduleOverlayRedrawFx();
                    break;
                }
            }
        });
    }

    /**
     * Replaces all overlay points with the provided list.
     *
     * <p>
     * This is a convenience method for scatter-like overlays. Each created overlay point receives a new identifier and
     * uses the current default point style. For per-point styling, prefer {@link #addPoint(Point2D, Color, double)}.
     * </p>
     *
     * @param worldPoints points in world coordinates; must not be {@code null} and must not contain {@code null}
     * @throws NullPointerException if {@code worldPoints} is {@code null} or contains {@code null}
     */
    public void setPoints(@NonNull final List<Point2D> worldPoints) {
        final List<Point2D> safePointList = copyPoints(worldPoints);

        FxBootstrap.runLater(() -> {
            overlayPoints.clear();
            for (final Point2D worldPoint : safePointList) {
                overlayPoints.add(
                        new OverlayPoint(
                                elementIdentifierSequence.getAndIncrement(),
                                worldPoint,
                                defaultPointColor,
                                defaultPointRadiusPixels
                        )
                );
            }
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Replaces all polylines with a single polyline using the current default polyline style.
     *
     * <p>
     * This method exists mainly as a compatibility convenience. For multiple polylines and individual styling, use
     * {@link #addPolyline(List, Color, double)} repeatedly.
     * </p>
     *
     * @param worldPolyline polyline points in world coordinates; must not be {@code null} and must not contain {@code null}
     * @throws NullPointerException if {@code worldPolyline} is {@code null} or contains {@code null}
     */
    public void setPolyline(@NonNull final List<Point2D> worldPolyline) {
        final List<Point2D> safePolylinePoints = copyPoints(worldPolyline);

        FxBootstrap.runLater(() -> {
            overlayPolylines.clear();
            overlayPolylines.add(
                    new OverlayPolyline(
                            elementIdentifierSequence.getAndIncrement(),
                            safePolylinePoints,
                            defaultPolylineColor,
                            defaultPolylineWidthPixels
                    )
            );
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Clears overlay points, overlay polylines, and all expression plots.
     *
     * <p>This method also cancels any ongoing plot computation work.</p>
     */
    public void clearOverlay() {
        FxBootstrap.runLater(() -> {
            overlayPoints.clear();
            overlayPolylines.clear();
            expressionPlots.clear();
            cancelPlotWork();
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Clears only expression plots.
     *
     * <p>This method also cancels any ongoing plot computation work.</p>
     */
    public void clearExpressionPlots() {
        FxBootstrap.runLater(() -> {
            expressionPlots.clear();
            cancelPlotWork();
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Adds an expression plot without variables.
     *
     * <p>
     * The provided color must be a hexadecimal string in {@code RRGGBB} or {@code AARRGGBB} format. A leading {@code '#'}
     * is optional.
     * </p>
     *
     * @param expression the expression to plot; must not be {@code null}
     * @param hexColor   the plot color as hex string; must not be {@code null}
     * @return a stable plot identifier that can be used to remove the plot later
     * @throws NullPointerException     if {@code expression} or {@code hexColor} is {@code null}
     * @throws IllegalArgumentException if {@code hexColor} is not a valid hex color
     */
    public long plotExpression(@NonNull final String expression, @NonNull final String hexColor) {
        return plotExpression(expression, Map.of(), hexColor);
    }

    /**
     * Adds an expression plot.
     *
     * <p>
     * Variables are provided as string values and forwarded to the underlying evaluation engine. Keys and values must
     * be non-null. The plot is computed asynchronously and will be updated when computation completes.
     * </p>
     *
     * @param expression the expression to plot; must not be {@code null}
     * @param variables  variable mapping (name -&gt; value) as strings; must not be {@code null} and must not contain {@code null} keys/values
     * @param hexColor   the plot color as hex string; must not be {@code null}
     * @return a stable plot identifier that can be used to remove the plot later
     * @throws NullPointerException     if any argument is {@code null}, or if {@code variables} contains {@code null} key/value
     * @throws IllegalArgumentException if {@code hexColor} is not a valid hex color
     */
    public long plotExpression(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final String hexColor) {
        final Color strokeColor = parseHexColor(hexColor);
        final long plotIdentifier = elementIdentifierSequence.getAndIncrement();

        FxBootstrap.runLater(() -> {
            expressionPlots.add(
                    new ExpressionPlot(
                            plotIdentifier,
                            expression,
                            variables,
                            strokeColor,
                            DEFAULT_EXPRESSION_PLOT_WIDTH_PIXELS
                    )
            );
            schedulePlotUpdateFx(0L);
            scheduleOverlayRedrawFx();
        });

        return plotIdentifier;
    }

    /**
     * Removes a plotted expression by identifier.
     *
     * <p>If the identifier is unknown, this method performs no changes and does not throw.</p>
     *
     * @param plotIdentifier the plot identifier returned by {@link #plotExpression(String, String)} or {@link #plotExpression(String, Map, String)}
     */
    public void removeExpressionPlot(final long plotIdentifier) {
        FxBootstrap.runLater(() -> {
            final boolean removed = expressionPlots.removeIf(expressionPlot -> expressionPlot.id == plotIdentifier);
            if (removed) {
                schedulePlotUpdateFx(0L);
                scheduleOverlayRedrawFx();
            }
        });
    }

    /**
     * Centers the origin of the underlying display pane.
     *
     * <p>This affects how world coordinates map to screen coordinates and triggers overlay/plot recalculation.</p>
     */
    public void centerOrigin() {
        FxBootstrap.runLater(pane::centerOrigin);
    }

    /**
     * Applies a new theme to the underlying display pane.
     *
     * <p>
     * The overlay and expression plots are refreshed after applying the theme. Plot recomputation is debounced to avoid
     * heavy work when multiple changes occur quickly.
     * </p>
     *
     * @param theme the theme to apply; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public void setTheme(@NonNull final DisplayTheme theme) {
        FxBootstrap.runLater(() -> {
            pane.setTheme(theme);
            scheduleOverlayRedrawFx();
            schedulePlotUpdateFx(PLOT_DEBOUNCE_MILLISECONDS);
        });
    }

    /**
     * Releases all resources held by this viewer.
     *
     * <p>
     * This method cancels ongoing plot computations, shuts down background executors, and closes the optional owned
     * window if present. The method is idempotent and can be called multiple times safely.
     * </p>
     */
    public void dispose() {
        cancelPlotWork();
        plotSchedulerExecutorService.shutdownNow();
        plotComputationExecutorService.shutdownNow();

        FxBootstrap.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }

    /**
     * Equivalent to {@link #dispose()}.
     *
     * <p>This is provided to integrate cleanly with try-with-resources blocks.</p>
     */
    @Override
    public void close() {
        dispose();
    }

    /**
     * Internal data class representing a plotted expression and its computed geometry.
     *
     * <p>
     * The geometry is stored as either a continuous polyline (for explicit functions) or a list of line segments
     * (for implicit contours). The plotting engine may choose either representation based on the expression type.
     * </p>
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ExpressionPlot {

        /**
         * Stable identifier for this plot, used for removal and lookup.
         */
        private final long id;

        /**
         * Original user-provided expression string.
         */
        private final @NonNull String expression;

        /**
         * Original variable mapping (string values) used by the evaluation engine.
         */
        private final @NonNull Map<String, String> variables;

        /**
         * Plot stroke color.
         */
        private final @NonNull Color strokeColor;

        /**
         * Plot stroke width in pixels.
         */
        private final double strokeWidthPixels;

        /**
         * Computed polyline geometry for explicit plots.
         */
        private List<GraphFxPoint> polyline = List.of();

        /**
         * Computed line segment geometry for implicit contour plots.
         */
        private List<GraphFxCalculatorEngine.LineSegment> segments = List.of();
    }

    /**
     * Immutable snapshot of an {@link ExpressionPlot} that is safe to use from a background thread.
     *
     * <p>This prevents races between UI thread mutations and background plot computation.</p>
     *
     * @param id                stable plot identifier
     * @param expression        expression string
     * @param variables         variable mapping as strings
     * @param strokeColor       stroke color
     * @param strokeWidthPixels stroke width in pixels
     */
    private record PlotSnapshot(
            long id,
            String expression,
            Map<String, String> variables,
            Color strokeColor,
            double strokeWidthPixels
    ) {
    }

    /**
     * Result of a completed plot computation.
     *
     * <p>The geometry is applied back to the matching {@link ExpressionPlot} on the JavaFX thread.</p>
     *
     * @param id       stable plot identifier
     * @param geometry computed plot geometry
     */
    private record PlotResult(long id, GraphFxCalculatorEngine.PlotGeometry geometry) {
    }

    /**
     * Creates an immutable defensive copy of a list of {@link Point2D} and validates that no element is {@code null}.
     *
     * @param points list of points; must not be {@code null} and must not contain {@code null}
     * @return an immutable copy of the provided points
     * @throws NullPointerException if {@code points} is {@code null} or contains {@code null}
     */
    private static List<Point2D> copyPoints(@NonNull final List<Point2D> points) {
        for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
            Objects.requireNonNull(points.get(pointIndex), "points[" + pointIndex + "] must not be null");
        }
        return List.copyOf(points);
    }

    /**
     * Converts a variable map from {@link BigNumber} values to string values.
     *
     * <p>
     * This helper exists to allow API layers to accept high precision variables while still delegating evaluation
     * to the underlying plotting engine that consumes string inputs.
     * </p>
     *
     * @param variables variables with {@link BigNumber} values; must not be {@code null} and must not contain {@code null} keys/values
     * @return an immutable copy of variables converted to string values
     * @throws NullPointerException if {@code variables} is {@code null} or contains {@code null} keys/values
     */
    private static Map<String, String> convertBigNumberVariables(@NonNull final Map<String, BigNumber> variables) {
        final Map<String, String> convertedVariables = new HashMap<>(Math.max(8, variables.size()));

        for (final Map.Entry<String, BigNumber> variableEntry : variables.entrySet()) {
            final String variableName = Objects.requireNonNull(variableEntry.getKey(), "variable name must not be null");
            final BigNumber variableValue = Objects.requireNonNull(
                    variableEntry.getValue(),
                    "variable '" + variableName + "' must not be null"
            );
            convertedVariables.put(variableName, variableValue.toString());
        }

        return Map.copyOf(convertedVariables);
    }

    /**
     * Parses a hex color string and returns a JavaFX {@link Color}.
     *
     * <p>
     * Supported input formats:
     * </p>
     * <ul>
     *   <li>{@code RRGGBB}</li>
     *   <li>{@code #RRGGBB}</li>
     *   <li>{@code AARRGGBB}</li>
     *   <li>{@code #AARRGGBB}</li>
     * </ul>
     *
     * <p>
     * When alpha is supplied, it is provided as the first two bytes (AA). Internally, JavaFX expects alpha at the end
     * for {@link Color#web(String)}, therefore the string is converted accordingly.
     * </p>
     *
     * @param hexColor a hex color string; must not be {@code null}
     * @return parsed JavaFX color
     * @throws NullPointerException     if {@code hexColor} is {@code null}
     * @throws IllegalArgumentException if {@code hexColor} is empty, has invalid length, or contains non-hex characters
     */
    private static Color parseHexColor(@NonNull final String hexColor) {
        final String trimmedHexColor = hexColor.trim();
        if (trimmedHexColor.isEmpty()) {
            throw new IllegalArgumentException("hexColor must not be empty");
        }

        final String hexDigits = trimmedHexColor.startsWith("#") ? trimmedHexColor.substring(1) : trimmedHexColor;
        if (!(hexDigits.length() == 6 || hexDigits.length() == 8)) {
            throw new IllegalArgumentException(
                    "hexColor must be RRGGBB or AARRGGBB (with optional leading '#'): " + hexColor
            );
        }

        for (int digitIndex = 0; digitIndex < hexDigits.length(); digitIndex++) {
            final char currentDigit = hexDigits.charAt(digitIndex);
            final boolean isHexDigit =
                    (currentDigit >= '0' && currentDigit <= '9')
                            || (currentDigit >= 'a' && currentDigit <= 'f')
                            || (currentDigit >= 'A' && currentDigit <= 'F');

            if (!isHexDigit) {
                throw new IllegalArgumentException(
                        "hexColor contains non-hex character '" + currentDigit + "': " + hexColor
                );
            }
        }

        final String cssHexColor;
        if (hexDigits.length() == 6) {
            cssHexColor = "#" + hexDigits;
        } else {
            final String alphaHex = hexDigits.substring(0, 2);
            final String rgbHex = hexDigits.substring(2);
            cssHexColor = "#" + rgbHex + alphaHex;
        }

        return Color.web(cssHexColor);
    }

    /**
     * Ensures the owned JavaFX {@link Stage} exists and is configured.
     *
     * <p>
     * If the stage does not exist, this method creates it, attaches the scene, and wires cancellation behavior for
     * when the window is hidden.
     * </p>
     *
     * @param widthPixels  initial scene width in pixels
     * @param heightPixels initial scene height in pixels
     */
    private void ensureStageFx(final double widthPixels, final double heightPixels) {
        if (stage != null) {
            return;
        }

        stage = new Stage();
        stage.setOnHidden(event -> cancelPlotWork());

        scene = new Scene(root, widthPixels, heightPixels);
        stage.setScene(scene);
    }

    /**
     * Configures the overlay canvas so it behaves like a transparent drawing layer above the display pane.
     *
     * <p>
     * The canvas is made mouse-transparent and its size is bound to the display pane size.
     * </p>
     */
    private void configureOverlayCanvasFx() {
        overlayCanvas.setMouseTransparent(true);
        overlayCanvas.widthProperty().bind(pane.widthProperty());
        overlayCanvas.heightProperty().bind(pane.heightProperty());
    }

    /**
     * Registers listeners that trigger overlay redraw and plot recomputation when the viewport changes.
     *
     * <p>
     * Viewport changes include resizing, scaling (zoom), and origin offset changes (panning).
     * </p>
     */
    private void registerViewportListenersFx() {
        final InvalidationListener viewportInvalidationListener = observable -> {
            schedulePlotUpdateFx(PLOT_DEBOUNCE_MILLISECONDS);
            scheduleOverlayRedrawFx();
        };

        overlayCanvas.widthProperty().addListener(viewportInvalidationListener);
        overlayCanvas.heightProperty().addListener(viewportInvalidationListener);

        pane.getScalePxPerUnit().addListener(viewportInvalidationListener);
        pane.getOriginOffsetX().addListener(viewportInvalidationListener);
        pane.getOriginOffsetY().addListener(viewportInvalidationListener);
    }

    /**
     * Schedules a plot recomputation after a debounce delay.
     *
     * <p>
     * The current viewport bounds and the current set of expression plots are snapshotted on the JavaFX thread. The
     * expensive computation runs on a dedicated worker thread. If a newer generation is scheduled while a computation
     * is pending or running, older work is cancelled.
     * </p>
     *
     * @param debounceMilliseconds debounce delay in milliseconds; negative values are treated as zero
     */
    private void schedulePlotUpdateFx(final long debounceMilliseconds) {
        if (expressionPlots.isEmpty()) {
            return;
        }

        final int viewportPixelWidth = (int) Math.max(1.0, overlayCanvas.getWidth());
        final int viewportPixelHeight = (int) Math.max(1.0, overlayCanvas.getHeight());

        final GraphFxCalculatorEngine.WorldBounds worldBoundsSnapshot = currentViewportWorldBoundsFx();
        final List<PlotSnapshot> plotSnapshots = snapshotPlotsFx();
        if (plotSnapshots.isEmpty()) {
            return;
        }

        final long generation = plotGenerationCounter.incrementAndGet();

        synchronized (plotWorkLock) {
            cancelScheduledPlotStartLocked();
            cancelRunningPlotTaskLocked();

            scheduledPlotStartFuture = plotSchedulerExecutorService.schedule(
                    () -> submitPlotComputation(plotSnapshots, worldBoundsSnapshot, viewportPixelWidth, viewportPixelHeight, generation),
                    Math.max(0L, debounceMilliseconds),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * Creates thread-safe snapshots of the currently registered expression plots.
     *
     * <p>
     * The snapshot contains only immutable state needed for computation and can safely be used from a background thread.
     * </p>
     *
     * @return immutable snapshots for all expression plots
     */
    private List<PlotSnapshot> snapshotPlotsFx() {
        final List<PlotSnapshot> plotSnapshots = new ArrayList<>(expressionPlots.size());
        for (final ExpressionPlot expressionPlot : expressionPlots) {
            plotSnapshots.add(
                    new PlotSnapshot(
                            expressionPlot.id,
                            expressionPlot.expression,
                            expressionPlot.variables,
                            expressionPlot.strokeColor,
                            expressionPlot.strokeWidthPixels
                    )
            );
        }
        return plotSnapshots;
    }

    /**
     * Submits plot computation work to the worker executor if the generation is still current.
     *
     * @param plotSnapshots       immutable plot snapshots
     * @param worldBoundsSnapshot immutable world bounds snapshot
     * @param viewportPixelWidth  viewport width in pixels
     * @param viewportPixelHeight viewport height in pixels
     * @param generation          generation token that must still match {@link #plotGenerationCounter}
     */
    private void submitPlotComputation(
            @NonNull final List<PlotSnapshot> plotSnapshots,
            @NonNull final GraphFxCalculatorEngine.WorldBounds worldBoundsSnapshot,
            final int viewportPixelWidth,
            final int viewportPixelHeight,
            final long generation
    ) {
        synchronized (plotWorkLock) {
            if (plotGenerationCounter.get() != generation) {
                return;
            }

            runningPlotComputationFuture = plotComputationExecutorService.submit(() ->
                    computePlots(plotSnapshots, worldBoundsSnapshot, viewportPixelWidth, viewportPixelHeight, generation)
            );
        }
    }

    /**
     * Computes plot geometry for all snapshots and applies the result on the JavaFX thread.
     *
     * <p>
     * This method runs on a background thread. It checks cancellation frequently using the generation token and
     * interruption status.
     * </p>
     *
     * @param plotSnapshots       snapshots of the plots to compute
     * @param worldBoundsSnapshot viewport bounds
     * @param viewportPixelWidth  viewport width in pixels
     * @param viewportPixelHeight viewport height in pixels
     * @param generation          generation token for cancellation
     */
    private void computePlots(
            @NonNull final List<PlotSnapshot> plotSnapshots,
            @NonNull final GraphFxCalculatorEngine.WorldBounds worldBoundsSnapshot,
            final int viewportPixelWidth,
            final int viewportPixelHeight,
            final long generation
    ) {
        final GraphFxCalculatorEngine.PlotCancellation plotCancellation =
                () -> Thread.currentThread().isInterrupted() || plotGenerationCounter.get() != generation;

        final List<PlotResult> plotResults = new ArrayList<>(plotSnapshots.size());

        for (final PlotSnapshot plotSnapshot : plotSnapshots) {
            if (plotCancellation.isCancelled()) {
                return;
            }

            final GraphFxCalculatorEngine.PlotGeometry computedGeometry = graphFxCalculatorEngine.plot(
                    plotSnapshot.expression(),
                    plotSnapshot.variables(),
                    worldBoundsSnapshot,
                    viewportPixelWidth,
                    viewportPixelHeight,
                    plotCancellation
            );

            if (plotCancellation.isCancelled()) {
                return;
            }

            plotResults.add(new PlotResult(plotSnapshot.id(), computedGeometry));
        }

        FxBootstrap.runLater(() -> {
            if (plotGenerationCounter.get() != generation) {
                return;
            }

            applyPlotResultsFx(plotResults);
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Applies computed plot geometry to the matching {@link ExpressionPlot} instances.
     *
     * <p>This method must be executed on the JavaFX Application Thread.</p>
     *
     * @param plotResults computed plot results to apply
     */
    private void applyPlotResultsFx(@NonNull final List<PlotResult> plotResults) {
        for (final PlotResult plotResult : plotResults) {
            for (final ExpressionPlot expressionPlot : expressionPlots) {
                if (expressionPlot.id == plotResult.id()) {
                    expressionPlot.polyline = plotResult.geometry().polyline();
                    expressionPlot.segments = plotResult.geometry().segments();
                    break;
                }
            }
        }
    }

    /**
     * Cancels any scheduled or running plot work by advancing the generation token.
     *
     * <p>
     * This method does not clear plots; it only prevents current computations from applying their results.
     * </p>
     */
    private void cancelPlotWork() {
        plotGenerationCounter.incrementAndGet();
        synchronized (plotWorkLock) {
            cancelScheduledPlotStartLocked();
            cancelRunningPlotTaskLocked();
        }
    }

    /**
     * Cancels the scheduled debounced plot start task.
     *
     * <p>This method must be called while holding {@link #plotWorkLock}.</p>
     */
    private void cancelScheduledPlotStartLocked() {
        final ScheduledFuture<?> scheduledFuture = scheduledPlotStartFuture;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledPlotStartFuture = null;
        }
    }

    /**
     * Cancels the currently running plot computation task.
     *
     * <p>This method must be called while holding {@link #plotWorkLock}.</p>
     */
    private void cancelRunningPlotTaskLocked() {
        final Future<?> runningFuture = runningPlotComputationFuture;
        if (runningFuture != null) {
            runningFuture.cancel(true);
            runningPlotComputationFuture = null;
        }
    }

    /**
     * Calculates the current world bounds that are visible within the pane.
     *
     * <p>
     * The bounds are derived from the display pane size, the origin offsets, and the scale (pixels per world unit).
     * </p>
     *
     * @return world bounds representing the visible viewport
     */
    private GraphFxCalculatorEngine.WorldBounds currentViewportWorldBoundsFx() {
        final double viewportWidthPixels = Math.max(1.0, pane.getWidth());
        final double viewportHeightPixels = Math.max(1.0, pane.getHeight());

        final double pixelsPerWorldUnit = pane.getScalePxPerUnit().get();
        final double originOffsetXPixels = pane.getOriginOffsetX().get();
        final double originOffsetYPixels = pane.getOriginOffsetY().get();

        final double minimumWorldX = (0.0 - originOffsetXPixels) / pixelsPerWorldUnit;
        final double maximumWorldX = (viewportWidthPixels - originOffsetXPixels) / pixelsPerWorldUnit;

        final double minimumWorldY = (originOffsetYPixels - viewportHeightPixels) / pixelsPerWorldUnit;
        final double maximumWorldY = (originOffsetYPixels - 0.0) / pixelsPerWorldUnit;

        return new GraphFxCalculatorEngine.WorldBounds(minimumWorldX, maximumWorldX, minimumWorldY, maximumWorldY);
    }

    /**
     * Schedules a single overlay redraw on the JavaFX Application Thread.
     *
     * <p>
     * Multiple calls within one JavaFX pulse are coalesced into a single redraw, reducing unnecessary work.
     * </p>
     */
    private void scheduleOverlayRedrawFx() {
        if (overlayRedrawScheduled) {
            return;
        }

        overlayRedrawScheduled = true;

        FxBootstrap.runLater(() -> {
            overlayRedrawScheduled = false;
            redrawOverlayNowFx();
        });
    }

    /**
     * Redraws the entire overlay layer immediately.
     *
     * <p>This method must be executed on the JavaFX Application Thread.</p>
     */
    private void redrawOverlayNowFx() {
        final double canvasWidthPixels = overlayCanvas.getWidth();
        final double canvasHeightPixels = overlayCanvas.getHeight();

        final GraphicsContext graphicsContext = overlayCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvasWidthPixels, canvasHeightPixels);

        drawExpressionPlotsFx(graphicsContext);
        drawPolylinesFx(graphicsContext);
        drawPointsFx(graphicsContext);
    }

    /**
     * Draws all expression plots (segments and polylines).
     *
     * <p>This method must be executed on the JavaFX Application Thread.</p>
     *
     * @param graphicsContext the graphics context used to draw onto the overlay canvas; must not be {@code null}
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawExpressionPlotsFx(@NonNull final GraphicsContext graphicsContext) {
        if (expressionPlots.isEmpty()) {
            return;
        }

        for (final ExpressionPlot expressionPlot : expressionPlots) {
            drawPlotSegments(graphicsContext, expressionPlot);
            drawPlotPolyline(graphicsContext, expressionPlot);
        }
    }

    /**
     * Draws segment-based plot geometry (typically used for implicit contours).
     *
     * @param graphicsContext the graphics context used to draw; must not be {@code null}
     * @param expressionPlot  plot definition containing stroke style and computed segments; must not be {@code null}
     * @throws NullPointerException if {@code graphicsContext} or {@code expressionPlot} is {@code null}
     */
    private void drawPlotSegments(@NonNull final GraphicsContext graphicsContext, @NonNull final ExpressionPlot expressionPlot) {
        if (expressionPlot.segments.isEmpty()) {
            return;
        }

        graphicsContext.setStroke(expressionPlot.strokeColor);
        graphicsContext.setLineWidth(expressionPlot.strokeWidthPixels);

        for (final GraphFxCalculatorEngine.LineSegment lineSegment : expressionPlot.segments) {
            final Point2D screenPointA = GraphFxUtil.worldToScreen(pane, lineSegment.a());
            final Point2D screenPointB = GraphFxUtil.worldToScreen(pane, lineSegment.b());
            graphicsContext.strokeLine(screenPointA.getX(), screenPointA.getY(), screenPointB.getX(), screenPointB.getY());
        }
    }

    /**
     * Draws polyline-based plot geometry (typically used for explicit functions).
     *
     * <p>
     * The polyline may contain non-finite points as separators. When a non-finite point is encountered, the current
     * path is ended and a new path may start afterwards.
     * </p>
     *
     * @param graphicsContext the graphics context used to draw; must not be {@code null}
     * @param expressionPlot  plot definition containing stroke style and computed polyline; must not be {@code null}
     * @throws NullPointerException if {@code graphicsContext} or {@code expressionPlot} is {@code null}
     */
    private void drawPlotPolyline(@NonNull final GraphicsContext graphicsContext, @NonNull final ExpressionPlot expressionPlot) {
        if (expressionPlot.polyline.size() < 2) {
            return;
        }

        graphicsContext.setStroke(expressionPlot.strokeColor);
        graphicsContext.setLineWidth(expressionPlot.strokeWidthPixels);

        boolean pathOpen = false;

        for (final GraphFxPoint worldPoint : expressionPlot.polyline) {
            final double worldX = worldPoint.x();
            final double worldY = worldPoint.y();

            if (!Double.isFinite(worldX) || !Double.isFinite(worldY)) {
                if (pathOpen) {
                    graphicsContext.stroke();
                    pathOpen = false;
                }
                continue;
            }

            final Point2D screenPoint = GraphFxUtil.worldToScreen(pane, worldPoint);

            if (!pathOpen) {
                graphicsContext.beginPath();
                graphicsContext.moveTo(screenPoint.getX(), screenPoint.getY());
                pathOpen = true;
            } else {
                graphicsContext.lineTo(screenPoint.getX(), screenPoint.getY());
            }
        }

        if (pathOpen) {
            graphicsContext.stroke();
        }
    }

    /**
     * Draws all user-managed overlay polylines.
     *
     * @param graphicsContext the graphics context used to draw; must not be {@code null}
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawPolylinesFx(@NonNull final GraphicsContext graphicsContext) {
        if (overlayPolylines.isEmpty()) {
            return;
        }

        for (final OverlayPolyline overlayPolyline : overlayPolylines) {
            if (overlayPolyline.worldPolyline.size() < 2) {
                continue;
            }

            graphicsContext.setStroke(overlayPolyline.color);
            graphicsContext.setLineWidth(overlayPolyline.widthPixels);

            final Point2D firstWorldPoint2D = overlayPolyline.worldPolyline.getFirst();
            final Point2D firstScreenPoint = GraphFxUtil.worldToScreen(
                    pane,
                    new GraphFxPoint(firstWorldPoint2D.getX(), firstWorldPoint2D.getY())
            );

            graphicsContext.beginPath();
            graphicsContext.moveTo(firstScreenPoint.getX(), firstScreenPoint.getY());

            for (int pointIndex = 1; pointIndex < overlayPolyline.worldPolyline.size(); pointIndex++) {
                final Point2D worldPointAtIndex = overlayPolyline.worldPolyline.get(pointIndex);
                final Point2D screenPointAtIndex = GraphFxUtil.worldToScreen(
                        pane,
                        new GraphFxPoint(worldPointAtIndex.getX(), worldPointAtIndex.getY())
                );
                graphicsContext.lineTo(screenPointAtIndex.getX(), screenPointAtIndex.getY());
            }

            graphicsContext.stroke();
        }
    }

    /**
     * Draws all user-managed overlay points.
     *
     * @param graphicsContext the graphics context used to draw; must not be {@code null}
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawPointsFx(@NonNull final GraphicsContext graphicsContext) {
        if (overlayPoints.isEmpty()) {
            return;
        }

        for (final OverlayPoint overlayPoint : overlayPoints) {
            graphicsContext.setFill(overlayPoint.color);

            final Point2D screenPoint = GraphFxUtil.worldToScreen(
                    pane,
                    new GraphFxPoint(overlayPoint.worldPoint.getX(), overlayPoint.worldPoint.getY())
            );

            final double drawX = screenPoint.getX() - overlayPoint.radiusPixels;
            final double drawY = screenPoint.getY() - overlayPoint.radiusPixels;

            graphicsContext.fillOval(drawX, drawY, overlayPoint.radiusPixels * 2.0, overlayPoint.radiusPixels * 2.0);
        }
    }

    /**
     * Internal immutable overlay point.
     *
     * <p>
     * The point is stored in world coordinates and rendered in screen space each time the overlay is redrawn.
     * </p>
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class OverlayPoint {

        /**
         * Stable identifier of the point.
         */
        private final long id;

        /**
         * Point position in world coordinates.
         */
        private final @NonNull Point2D worldPoint;

        /**
         * Fill color of the point.
         */
        private final @NonNull Color color;

        /**
         * Radius in pixels.
         */
        private final double radiusPixels;
    }

    /**
     * Internal immutable overlay polyline.
     *
     * <p>
     * The polyline is stored as a list of world-coordinate points and rendered in screen space during redraw.
     * </p>
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class OverlayPolyline {

        /**
         * Stable identifier of the polyline.
         */
        private final long id;

        /**
         * Polyline points in world coordinates.
         */
        private final @NonNull List<Point2D> worldPolyline;

        /**
         * Stroke color of the polyline.
         */
        private final @NonNull Color color;

        /**
         * Stroke width in pixels.
         */
        private final double widthPixels;
    }

}
