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
import com.mlprograms.justmath.graphfx.core.GraphFxCalculator;
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
 * A lightweight JavaFX viewer for GraphFx that is intended to be used by library consumers.
 *
 * <p>This class owns a {@link Stage} optionally (when {@link #show()} is used), and always exposes an embeddable
 * JavaFX {@link Parent} via {@link #asNode()}.</p>
 *
 * <p><b>Threading:</b> All public methods are safe to call from any thread. UI work is dispatched to the JavaFX
 * thread via {@link FxBootstrap#runLater(Runnable)}.</p>
 */
public final class GraphFxPlotViewer implements AutoCloseable {

    private static final long PLOT_DEBOUNCE_MS = 80L;

    private static final Color DEFAULT_POINT_COLOR = Color.RED;
    private static final double DEFAULT_POINT_RADIUS_PX = 4.0;

    private static final Color DEFAULT_POLYLINE_COLOR = Color.DODGERBLUE;
    private static final double DEFAULT_POLYLINE_WIDTH_PX = 2.0;

    private static final double DEFAULT_PLOT_WIDTH_PX = 2.0;

    @Getter
    private final GraphFxDisplayPane pane;

    private final Canvas overlayCanvas;
    private final StackPane root;

    private final List<Point2D> pointsWorld;
    private final List<Point2D> manualPolylineWorld;
    private final List<ExpressionPlot> expressionPlots;

    private Color pointColor;
    private double pointRadiusPx;

    private Color manualPolylineColor;
    private double manualPolylineWidthPx;

    private final GraphFxCalculator calculator;

    private final Object plotLock;
    private final ScheduledExecutorService plotScheduler;
    private final ExecutorService plotExecutor;

    private final AtomicLong plotGeneration;
    private final AtomicLong plotIdSequence;

    private ScheduledFuture<?> scheduledPlotStart;
    private Future<?> runningPlotTask;

    private boolean overlayRedrawScheduled;

    private Stage stage;
    private Scene scene;

    /**
     * Creates a new viewer window using {@link DisplayTheme#LIGHT}.
     */
    public GraphFxPlotViewer() {
        this(DisplayTheme.LIGHT);
    }

    /**
     * Creates a new viewer window using the given theme.
     *
     * @param theme the initial theme (must not be {@code null})
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public GraphFxPlotViewer(@NonNull final DisplayTheme theme) {
        this(new GraphFxDisplayPane(theme));
    }

    /**
     * Creates a new viewer by wrapping an existing {@link GraphFxDisplayPane}.
     *
     * @param pane the pane to wrap (must not be {@code null})
     * @throws NullPointerException if {@code pane} is {@code null}
     */
    public GraphFxPlotViewer(@NonNull final GraphFxDisplayPane pane) {
        this.pane = pane;

        this.overlayCanvas = new Canvas();
        this.root = new StackPane(this.pane, this.overlayCanvas);

        this.pointsWorld = new ArrayList<>();
        this.manualPolylineWorld = new ArrayList<>();
        this.expressionPlots = new ArrayList<>();

        this.pointColor = DEFAULT_POINT_COLOR;
        this.pointRadiusPx = DEFAULT_POINT_RADIUS_PX;

        this.manualPolylineColor = DEFAULT_POLYLINE_COLOR;
        this.manualPolylineWidthPx = DEFAULT_POLYLINE_WIDTH_PX;

        this.calculator = new GraphFxCalculator();

        this.plotLock = new Object();
        this.plotGeneration = new AtomicLong(0L);
        this.plotIdSequence = new AtomicLong(1L);

        this.plotScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r, "GraphFx-PlotScheduler");
            thread.setDaemon(true);
            return thread;
        });

        this.plotExecutor = Executors.newSingleThreadExecutor(r -> {
            final Thread thread = new Thread(r, "GraphFx-PlotWorker");
            thread.setDaemon(true);
            return thread;
        });

        FxBootstrap.runLater(() -> {
            configureOverlayCanvasFx();
            registerViewportListenersFx();
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Returns an embeddable JavaFX node containing the display pane and the overlay canvas.
     *
     * @return the root node (never {@code null})
     */
    public Parent asNode() {
        return root;
    }

    /**
     * Shows this viewer in its own window using {@link WindowConfig} defaults.
     */
    public void show() {
        show(WindowConfig.DEFAULT_WINDOW_TITLE, WindowConfig.DEFAULT_WINDOW_WIDTH, WindowConfig.DEFAULT_WINDOW_HEIGHT);
    }

    /**
     * Shows this viewer in its own window. If the window already exists, it is brought to front and resized.
     *
     * @param title  window title (must not be {@code null})
     * @param width  window width in pixels
     * @param height window height in pixels
     * @throws NullPointerException if {@code title} is {@code null}
     */
    public void show(@NonNull final String title, final double width, final double height) {
        final double safeWidth = Math.max(1.0, width);
        final double safeHeight = Math.max(1.0, height);

        FxBootstrap.runLater(() -> {
            ensureStageFx(safeWidth, safeHeight);
            stage.setTitle(title);
            stage.setWidth(safeWidth);
            stage.setHeight(safeHeight);
            stage.show();
            stage.toFront();
            scheduleOverlayRedrawFx();
            schedulePlotUpdateFx(0L);
        });
    }

    /**
     * Hides the window if it is currently shown.
     */
    public void hide() {
        FxBootstrap.runLater(() -> {
            if (stage != null) {
                stage.hide();
            }
        });
    }

    /**
     * Replaces all overlay points.
     *
     * @param worldPoints points in world coordinates (must not be {@code null} and must not contain {@code null})
     * @throws NullPointerException if {@code worldPoints} is {@code null} or contains {@code null}
     */
    public void setPoints(@NonNull final List<Point2D> worldPoints) {
        final List<Point2D> safeCopy = copyPoints(worldPoints);
        FxBootstrap.runLater(() -> {
            pointsWorld.clear();
            pointsWorld.addAll(safeCopy);
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Replaces the manual overlay polyline.
     *
     * @param worldPolyline polyline in world coordinates (must not be {@code null} and must not contain {@code null})
     * @throws NullPointerException if {@code worldPolyline} is {@code null} or contains {@code null}
     */
    public void setPolyline(@NonNull final List<Point2D> worldPolyline) {
        final List<Point2D> safeCopy = copyPoints(worldPolyline);
        FxBootstrap.runLater(() -> {
            manualPolylineWorld.clear();
            manualPolylineWorld.addAll(safeCopy);
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Clears points, manual polyline, and all expression plots.
     */
    public void clearOverlay() {
        FxBootstrap.runLater(() -> {
            pointsWorld.clear();
            manualPolylineWorld.clear();
            expressionPlots.clear();
            cancelPlotWork();
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Clears only expression plots.
     */
    public void clearExpressionPlots() {
        FxBootstrap.runLater(() -> {
            expressionPlots.clear();
            cancelPlotWork();
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Adds a new expression plot without variables.
     *
     * @param expression expression to plot (must not be {@code null})
     * @param hexColor   plot color as hex string (must not be {@code null})
     * @return the plot id that can be used for removal
     * @throws NullPointerException     if {@code expression} or {@code hexColor} is {@code null}
     * @throws IllegalArgumentException if {@code hexColor} is not a valid hex color
     */
    public long plotExpression(@NonNull final String expression, @NonNull final String hexColor) {
        return plotExpression(expression, Map.of(), hexColor);
    }

    /**
     * Adds a new expression plot.
     *
     * @param expression expression to plot (must not be {@code null})
     * @param variables  variables as {@link BigNumber} (must not be {@code null}; keys and values must not be {@code null})
     * @param hexColor   plot color as hex string (must not be {@code null})
     * @return the plot id that can be used for removal
     * @throws NullPointerException     if any argument is {@code null}, or if {@code variables} contains {@code null} key/value
     * @throws IllegalArgumentException if {@code hexColor} is not a valid hex color
     */
    public long plotExpression(@NonNull final String expression, @NonNull final Map<String, BigNumber> variables, @NonNull final String hexColor) {
        final Color strokeColor = parseHexColor(hexColor);
        final Map<String, String> engineVariables = convertBigNumberVariables(variables);
        final long plotId = plotIdSequence.getAndIncrement();

        FxBootstrap.runLater(() -> {
            expressionPlots.add(new ExpressionPlot(plotId, expression, engineVariables, strokeColor, DEFAULT_PLOT_WIDTH_PX));
            schedulePlotUpdateFx(0L);
            scheduleOverlayRedrawFx();
        });

        return plotId;
    }

    /**
     * Removes a plot by id. If no plot exists for the given id, this method does nothing.
     *
     * @param plotId plot id
     */
    public void removeExpressionPlot(final long plotId) {
        FxBootstrap.runLater(() -> {
            final boolean removed = expressionPlots.removeIf(p -> p.id == plotId);
            if (removed) {
                schedulePlotUpdateFx(0L);
                scheduleOverlayRedrawFx();
            }
        });
    }

    /**
     * Sets the style of manual overlay points.
     *
     * @param color    point color (must not be {@code null})
     * @param radiusPx radius in pixels; values &lt;= 0 are clamped to 1
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setPointStyle(@NonNull final Color color, final double radiusPx) {
        final double safeRadius = radiusPx > 0 ? radiusPx : 1.0;
        FxBootstrap.runLater(() -> {
            pointColor = color;
            pointRadiusPx = safeRadius;
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Sets the style of the manual overlay polyline.
     *
     * @param color   line color (must not be {@code null})
     * @param widthPx stroke width in pixels; values &lt;= 0 are clamped to 1
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setPolylineStyle(@NonNull final Color color, final double widthPx) {
        final double safeWidth = widthPx > 0 ? widthPx : 1.0;
        FxBootstrap.runLater(() -> {
            manualPolylineColor = color;
            manualPolylineWidthPx = safeWidth;
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Centers the origin of the underlying display pane.
     */
    public void centerOrigin() {
        FxBootstrap.runLater(pane::centerOrigin);
    }

    /**
     * Applies a theme to the underlying pane and refreshes the overlay and expression plots.
     *
     * @param theme theme to apply (must not be {@code null})
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public void setTheme(@NonNull final DisplayTheme theme) {
        FxBootstrap.runLater(() -> {
            pane.setTheme(theme);
            scheduleOverlayRedrawFx();
            schedulePlotUpdateFx(PLOT_DEBOUNCE_MS);
        });
    }

    /**
     * Disposes background executors and closes the window.
     *
     * <p>This method is safe to call multiple times.</p>
     */
    public void dispose() {
        cancelPlotWork();
        plotScheduler.shutdownNow();
        plotExecutor.shutdownNow();

        FxBootstrap.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }

    /**
     * Equivalent to {@link #dispose()}.
     */
    @Override
    public void close() {
        dispose();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ExpressionPlot {
        private final long id;
        private final @NonNull String expression;
        private final @NonNull Map<String, String> variables;
        private final @NonNull Color strokeColor;
        private final double strokeWidthPx;

        private List<GraphFxPoint> polyline = List.of();
        private List<GraphFxCalculator.LineSegment> segments = List.of();
    }

    /**
     * Immutable snapshot of a plot configuration used to compute geometry off the UI thread.
     *
     * @param id            plot id
     * @param expression    expression string
     * @param variables     variables as engine strings
     * @param strokeColor   stroke color
     * @param strokeWidthPx stroke width in pixels
     */
    private record PlotSnapshot(long id, String expression, Map<String, String> variables, Color strokeColor,
                                double strokeWidthPx) {
    }

    /**
     * Computation result for a single plot.
     *
     * @param id       plot id
     * @param geometry computed geometry
     */
    private record PlotResult(long id, GraphFxCalculator.PlotGeometry geometry) {
    }

    /**
     * Creates an immutable defensive copy of a list of points and validates that it contains no {@code null} elements.
     *
     * @param points list of points to copy (must not be {@code null})
     * @return an immutable copy of the input list
     * @throws NullPointerException if {@code points} is {@code null} or contains {@code null}
     */
    private static List<Point2D> copyPoints(@NonNull final List<Point2D> points) {
        for (int i = 0; i < points.size(); i++) {
            Objects.requireNonNull(points.get(i), "points[" + i + "] must not be null");
        }
        return List.copyOf(points);
    }

    /**
     * Converts user variables from {@link BigNumber} to string form for the expression engine.
     *
     * @param variables variables map (must not be {@code null}; keys and values must not be {@code null})
     * @return an immutable map suitable for the calculation engine
     * @throws NullPointerException if {@code variables} is {@code null} or contains {@code null} key/value
     */
    private static Map<String, String> convertBigNumberVariables(@NonNull final Map<String, BigNumber> variables) {
        final Map<String, String> out = new HashMap<>(Math.max(8, variables.size()));
        for (final Map.Entry<String, BigNumber> entry : variables.entrySet()) {
            final String key = Objects.requireNonNull(entry.getKey(), "variable name must not be null");
            final BigNumber value = Objects.requireNonNull(entry.getValue(), "variable '" + key + "' must not be null");
            out.put(key, value.toString());
        }
        return Map.copyOf(out);
    }

    /**
     * Parses a hex color string in {@code RRGGBB} or {@code AARRGGBB} format with an optional leading {@code #}.
     *
     * @param hexColor hex color (must not be {@code null})
     * @return a JavaFX {@link Color}
     * @throws NullPointerException     if {@code hexColor} is {@code null}
     * @throws IllegalArgumentException if the string is empty or not a valid hex color
     */
    private static Color parseHexColor(@NonNull final String hexColor) {
        final String raw = hexColor.trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("hexColor must not be empty");
        }

        final String digits = raw.startsWith("#") ? raw.substring(1) : raw;
        if (!(digits.length() == 6 || digits.length() == 8)) {
            throw new IllegalArgumentException("hexColor must be RRGGBB or AARRGGBB (with optional leading '#'): " + hexColor);
        }

        for (int i = 0; i < digits.length(); i++) {
            final char c = digits.charAt(i);
            final boolean isHex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!isHex) {
                throw new IllegalArgumentException("hexColor contains non-hex character '" + c + "': " + hexColor);
            }
        }

        final String css;
        if (digits.length() == 6) {
            css = "#" + digits;
        } else {
            final String aa = digits.substring(0, 2);
            final String rrggbb = digits.substring(2);
            css = "#" + rrggbb + aa;
        }

        return Color.web(css);
    }

    /**
     * Ensures a {@link Stage} and {@link Scene} exist for this viewer.
     *
     * @param width  initial width
     * @param height initial height
     */
    private void ensureStageFx(final double width, final double height) {
        if (stage != null) {
            return;
        }
        stage = new Stage();
        stage.setOnHidden(e -> cancelPlotWork());
        scene = new Scene(root, width, height);
        stage.setScene(scene);
    }

    /**
     * Configures the overlay canvas to match the pane size and to ignore mouse events.
     */
    private void configureOverlayCanvasFx() {
        overlayCanvas.setMouseTransparent(true);
        overlayCanvas.widthProperty().bind(pane.widthProperty());
        overlayCanvas.heightProperty().bind(pane.heightProperty());
    }

    /**
     * Registers listeners that trigger redraws and plot recomputation when the viewport changes.
     */
    private void registerViewportListenersFx() {
        final InvalidationListener listener = obs -> {
            schedulePlotUpdateFx(PLOT_DEBOUNCE_MS);
            scheduleOverlayRedrawFx();
        };

        overlayCanvas.widthProperty().addListener(listener);
        overlayCanvas.heightProperty().addListener(listener);

        pane.getScalePxPerUnit().addListener(listener);
        pane.getOriginOffsetX().addListener(listener);
        pane.getOriginOffsetY().addListener(listener);
    }

    /**
     * Schedules a debounced plot recomputation for all expression plots.
     *
     * @param debounceMs debounce time in milliseconds
     */
    private void schedulePlotUpdateFx(final long debounceMs) {
        if (expressionPlots.isEmpty()) {
            return;
        }

        final int pixelWidth = (int) Math.max(1.0, overlayCanvas.getWidth());
        final int pixelHeight = (int) Math.max(1.0, overlayCanvas.getHeight());

        final GraphFxCalculator.WorldBounds boundsSnapshot = currentViewportWorldBoundsFx();
        final List<PlotSnapshot> plotsSnapshot = snapshotPlotsFx();
        if (plotsSnapshot.isEmpty()) {
            return;
        }

        final long generation = plotGeneration.incrementAndGet();

        synchronized (plotLock) {
            cancelScheduledPlotStartLocked();
            cancelRunningPlotTaskLocked();

            scheduledPlotStart = plotScheduler.schedule(() -> submitPlotComputation(plotsSnapshot, boundsSnapshot, pixelWidth, pixelHeight, generation), Math.max(0L, debounceMs), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Creates an immutable snapshot of all current plots for background computation.
     *
     * @return a snapshot list
     */
    private List<PlotSnapshot> snapshotPlotsFx() {
        final List<PlotSnapshot> snapshot = new ArrayList<>(expressionPlots.size());
        for (final ExpressionPlot plot : expressionPlots) {
            snapshot.add(new PlotSnapshot(plot.id, plot.expression, plot.variables, plot.strokeColor, plot.strokeWidthPx));
        }
        return snapshot;
    }

    /**
     * Submits plot computation work to the plot worker executor if the generation is still current.
     *
     * @param plotsSnapshot  plots snapshot
     * @param boundsSnapshot world bounds snapshot
     * @param pixelWidth     pixel width
     * @param pixelHeight    pixel height
     * @param generation     generation id
     */
    private void submitPlotComputation(@NonNull final List<PlotSnapshot> plotsSnapshot, @NonNull final GraphFxCalculator.WorldBounds boundsSnapshot, final int pixelWidth, final int pixelHeight, final long generation) {
        synchronized (plotLock) {
            if (plotGeneration.get() != generation) {
                return;
            }
            runningPlotTask = plotExecutor.submit(() -> computePlots(plotsSnapshot, boundsSnapshot, pixelWidth, pixelHeight, generation));
        }
    }

    /**
     * Computes plot geometries for all plots in the snapshot and applies results on the JavaFX thread.
     *
     * @param plotsSnapshot  plots snapshot
     * @param boundsSnapshot world bounds snapshot
     * @param pixelWidth     pixel width
     * @param pixelHeight    pixel height
     * @param generation     generation id
     */
    private void computePlots(@NonNull final List<PlotSnapshot> plotsSnapshot, @NonNull final GraphFxCalculator.WorldBounds boundsSnapshot, final int pixelWidth, final int pixelHeight, final long generation) {
        final GraphFxCalculator.PlotCancellation cancellation = () -> Thread.currentThread().isInterrupted() || plotGeneration.get() != generation;
        final List<PlotResult> results = new ArrayList<>(plotsSnapshot.size());

        for (final PlotSnapshot plot : plotsSnapshot) {
            if (cancellation.isCancelled()) {
                return;
            }

            final GraphFxCalculator.PlotGeometry geometry = calculator.plot(plot.expression(), plot.variables(), boundsSnapshot, pixelWidth, pixelHeight, cancellation);

            if (cancellation.isCancelled()) {
                return;
            }

            results.add(new PlotResult(plot.id(), geometry));
        }

        FxBootstrap.runLater(() -> {
            if (plotGeneration.get() != generation) {
                return;
            }
            applyPlotResultsFx(results);
            scheduleOverlayRedrawFx();
        });
    }

    /**
     * Applies computed results to the corresponding live plots.
     *
     * @param results computed results
     */
    private void applyPlotResultsFx(@NonNull final List<PlotResult> results) {
        for (final PlotResult result : results) {
            for (final ExpressionPlot plot : expressionPlots) {
                if (plot.id == result.id()) {
                    plot.polyline = result.geometry().polyline();
                    plot.segments = result.geometry().segments();
                    break;
                }
            }
        }
    }

    /**
     * Cancels any scheduled and running plot computation work.
     */
    private void cancelPlotWork() {
        plotGeneration.incrementAndGet();
        synchronized (plotLock) {
            cancelScheduledPlotStartLocked();
            cancelRunningPlotTaskLocked();
        }
    }

    /**
     * Cancels a pending scheduled plot start if present.
     */
    private void cancelScheduledPlotStartLocked() {
        final ScheduledFuture<?> scheduled = scheduledPlotStart;
        if (scheduled != null) {
            scheduled.cancel(true);
            scheduledPlotStart = null;
        }
    }

    /**
     * Cancels the running plot computation task if present.
     */
    private void cancelRunningPlotTaskLocked() {
        final Future<?> running = runningPlotTask;
        if (running != null) {
            running.cancel(true);
            runningPlotTask = null;
        }
    }

    /**
     * Computes the current viewport bounds in world units based on pane size, scale, and origin.
     *
     * @return world bounds
     */
    private GraphFxCalculator.WorldBounds currentViewportWorldBoundsFx() {
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
     * Schedules an overlay redraw on the JavaFX thread, coalescing multiple calls into a single frame.
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
     * Redraws the overlay canvas immediately.
     */
    private void redrawOverlayNowFx() {
        final double width = overlayCanvas.getWidth();
        final double height = overlayCanvas.getHeight();

        final GraphicsContext graphicsContext = overlayCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, width, height);

        drawExpressionPlotsFx(graphicsContext);
        drawManualPolylineFx(graphicsContext);
        drawPointsFx(graphicsContext);
    }

    /**
     * Draws all expression plots to the overlay canvas.
     *
     * @param graphicsContext target graphics context (must not be {@code null})
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawExpressionPlotsFx(@NonNull final GraphicsContext graphicsContext) {
        if (expressionPlots.isEmpty()) {
            return;
        }

        for (final ExpressionPlot plot : expressionPlots) {
            drawPlotSegmentsFx(graphicsContext, plot);
            drawPlotPolylineFx(graphicsContext, plot);
        }
    }

    /**
     * Draws the implicit contour segments of a single plot.
     *
     * @param graphicsContext target graphics context (must not be {@code null})
     * @param plot            plot to draw (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    private void drawPlotSegmentsFx(@NonNull final GraphicsContext graphicsContext, @NonNull final ExpressionPlot plot) {
        if (plot.segments.isEmpty()) {
            return;
        }

        graphicsContext.setStroke(plot.strokeColor);
        graphicsContext.setLineWidth(plot.strokeWidthPx);

        for (final GraphFxCalculator.LineSegment segment : plot.segments) {
            final Point2D a = worldToScreenFx(segment.a());
            final Point2D b = worldToScreenFx(segment.b());
            graphicsContext.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
    }

    /**
     * Draws the polyline of a single plot.
     *
     * @param graphicsContext target graphics context (must not be {@code null})
     * @param plot            plot to draw (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    private void drawPlotPolylineFx(@NonNull final GraphicsContext graphicsContext, @NonNull final ExpressionPlot plot) {
        if (plot.polyline.size() < 2) {
            return;
        }

        graphicsContext.setStroke(plot.strokeColor);
        graphicsContext.setLineWidth(plot.strokeWidthPx);

        final Point2D first = worldToScreenFx(plot.polyline.get(0));
        graphicsContext.beginPath();
        graphicsContext.moveTo(first.getX(), first.getY());

        for (int i = 1; i < plot.polyline.size(); i++) {
            final Point2D p = worldToScreenFx(plot.polyline.get(i));
            graphicsContext.lineTo(p.getX(), p.getY());
        }

        graphicsContext.stroke();
    }

    /**
     * Draws the manual polyline overlay.
     *
     * @param graphicsContext target graphics context (must not be {@code null})
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawManualPolylineFx(@NonNull final GraphicsContext graphicsContext) {
        if (manualPolylineWorld.size() < 2) {
            return;
        }

        graphicsContext.setStroke(manualPolylineColor);
        graphicsContext.setLineWidth(manualPolylineWidthPx);

        final Point2D first = worldToScreenFx(manualPolylineWorld.getFirst());
        graphicsContext.beginPath();
        graphicsContext.moveTo(first.getX(), first.getY());

        for (int i = 1; i < manualPolylineWorld.size(); i++) {
            final Point2D p = worldToScreenFx(manualPolylineWorld.get(i));
            graphicsContext.lineTo(p.getX(), p.getY());
        }

        graphicsContext.stroke();
    }

    /**
     * Draws the manual point overlay.
     *
     * @param graphicsContext target graphics context (must not be {@code null})
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawPointsFx(@NonNull final GraphicsContext graphicsContext) {
        if (pointsWorld.isEmpty()) {
            return;
        }

        graphicsContext.setFill(pointColor);

        for (final Point2D worldPoint : pointsWorld) {
            final Point2D screenPoint = worldToScreenFx(worldPoint);
            final double x = screenPoint.getX() - pointRadiusPx;
            final double y = screenPoint.getY() - pointRadiusPx;
            graphicsContext.fillOval(x, y, pointRadiusPx * 2.0, pointRadiusPx * 2.0);
        }
    }

    /**
     * Converts a world coordinate point to screen pixels using the current pane transform.
     *
     * @param worldPoint world point (must not be {@code null})
     * @return screen point in pixels
     * @throws NullPointerException if {@code worldPoint} is {@code null}
     */
    private Point2D worldToScreenFx(@NonNull final Point2D worldPoint) {
        final double scalePixelsPerUnit = pane.getScalePxPerUnit().get();
        final double originPixelsX = pane.getOriginOffsetX().get();
        final double originPixelsY = pane.getOriginOffsetY().get();

        final double screenPixelsX = originPixelsX + worldPoint.getX() * scalePixelsPerUnit;
        final double screenPixelsY = originPixelsY - worldPoint.getY() * scalePixelsPerUnit;

        return new Point2D(screenPixelsX, screenPixelsY);
    }

    /**
     * Converts a {@link GraphFxPoint} in world coordinates to a screen pixel position.
     *
     * @param worldPoint world point (must not be {@code null})
     * @return screen point in pixels
     * @throws NullPointerException if {@code worldPoint} is {@code null}
     */
    private Point2D worldToScreenFx(@NonNull final GraphFxPoint worldPoint) {
        final double scalePxPerUnit = pane.getScalePxPerUnit().get();
        final double originX = pane.getOriginOffsetX().get();
        final double originY = pane.getOriginOffsetY().get();

        final double screenX = originX + worldPoint.x() * scalePxPerUnit;
        final double screenY = originY - worldPoint.y() * scalePxPerUnit;

        return new Point2D(screenX, screenY);
    }

}
