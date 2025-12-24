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
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Small convenience wrapper that exposes a {@link GraphFxDisplayPane} as a reusable "display-only" component.
 * <p>
 * The intent of this class is to provide a minimal, embeddable API for showing an interactive coordinate system
 * (grid + axes + tick labels) without any higher-level editor or function management UI.
 * </p>
 *
 * <h2>Overlay drawing</h2>
 * <p>
 * This wrapper can draw:
 * </p>
 * <ul>
 *   <li><strong>Points</strong> via {@link #setPoints(List)}</li>
 *   <li><strong>A polyline</strong> via {@link #setPolyline(List)}</li>
 *   <li><strong>Implicit curve segments</strong> (used by {@link #plotExpression(String, Map)})</li>
 * </ul>
 *
 * <h2>Expression plotting</h2>
 * <p>
 * Use {@link #plotExpression(String, Map)} to plot a JustMath expression:
 * </p>
 * <ul>
 *   <li>If the expression contains {@code y}: it is interpreted as {@code F(x,y)=0} and the 0-contour is drawn.</li>
 *   <li>If the expression does not contain {@code y}: it is interpreted as {@code y=f(x)} and a polyline is drawn.</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * <p>
 * JavaFX UI changes must occur on the JavaFX Application Thread. This class ensures safe execution by
 * delegating UI work to {@link FxBootstrap}. Callers may invoke these methods from any thread; operations
 * that mutate JavaFX state are executed on the correct thread via {@link FxBootstrap#runLater(Runnable)}.
 * </p>
 */
@Getter
public final class GraphFxDisplayOnlyApp {

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
    private boolean plotUpdateScheduled;

    /**
     * Creates a new display-only app using {@link DisplayTheme#LIGHT}.
     */
    public GraphFxDisplayOnlyApp() {
        this(DisplayTheme.LIGHT);
    }

    /**
     * Creates a new display-only app with a newly constructed {@link GraphFxDisplayPane} using the given theme.
     *
     * @param theme the initial theme to use; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final DisplayTheme theme) {
        this(new GraphFxDisplayPane(theme));
    }

    /**
     * Creates a new display-only app wrapping an existing {@link GraphFxDisplayPane}.
     *
     * @param pane the pane to wrap; must not be {@code null}
     * @throws NullPointerException if {@code pane} is {@code null}
     */
    public GraphFxDisplayOnlyApp(@NonNull final GraphFxDisplayPane pane) {
        this.pane = pane;

        this.overlayCanvas = new Canvas();
        this.root = new StackPane(pane, overlayCanvas);

        this.pointsWorld = new ArrayList<>();
        this.polylineWorld = new ArrayList<>();
        this.contourSegmentsWorld = new ArrayList<>();

        this.pointColor = Color.RED;
        this.lineColor = Color.DODGERBLUE;
        this.pointRadiusPx = 4.0;
        this.lineWidthPx = 2.0;

        this.calculator = new GraphFxCalculator();

        FxBootstrap.runLater(() -> {
            configureOverlayCanvas();
            registerOverlayRedrawTriggers();
            redrawOverlay();
        });
    }

    /**
     * Returns the wrapped pane (with overlay) as a {@link Parent} node for embedding into any JavaFX scene graph.
     *
     * @return the root node (pane + overlay)
     */
    public Parent asNode() {
        return root;
    }

    /**
     * Replaces the currently drawn overlay points.
     *
     * @param worldPoints points in world coordinates; must not be {@code null} and must not contain {@code null} entries
     * @throws NullPointerException if {@code worldPoints} is {@code null} or contains {@code null}
     */
    public void setPoints(@NonNull final List<Point2D> worldPoints) {
        final List<Point2D> safeCopy = copyPoints(worldPoints);

        FxBootstrap.runLater(() -> {
            pointsWorld.clear();
            pointsWorld.addAll(safeCopy);
            redrawOverlay();
        });
    }

    /**
     * Replaces the currently drawn overlay polyline.
     * <p>
     * If fewer than two points are provided, nothing will be drawn as a polyline.
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

            redrawOverlay();
        });
    }

    /**
     * Clears all overlay drawings (points, polyline, and expression plot).
     */
    public void clearOverlay() {
        FxBootstrap.runLater(() -> {
            pointsWorld.clear();
            polylineWorld.clear();
            contourSegmentsWorld.clear();
            plotRequest = null;
            redrawOverlay();
        });
    }

    /**
     * Plots a JustMath expression as a graph.
     * <p>
     * Interpretation:
     * </p>
     * <ul>
     *   <li>If the expression contains {@code y}: plot the implicit curve {@code F(x,y)=0}.</li>
     *   <li>Otherwise: plot the explicit function {@code y=f(x)}.</li>
     * </ul>
     *
     * <p>
     * The plotted geometry is recomputed automatically when pan/zoom/resize changes (debounced per JavaFX pulse),
     * so the graph stays visually consistent with the current viewport.
     * </p>
     *
     * @param expression the expression to plot; must not be {@code null}
     * @param variables  external variables for the expression (e.g. {@code a}); must not be {@code null}
     * @throws NullPointerException if {@code expression} or {@code variables} is {@code null}
     */
    public void plotExpression(@NonNull final String expression, @NonNull final Map<String, String> variables) {
        final Map<String, String> safeVariables = Map.copyOf(variables);

        FxBootstrap.runLater(() -> {
            final boolean implicit = calculator.containsYVariable(expression);
            plotRequest = new PlotRequest(expression, safeVariables, implicit);
            recomputePlotForViewport();
            redrawOverlay();
        });
    }

    /**
     * Updates the visual style used for drawing points.
     *
     * @param color    point fill color; must not be {@code null}
     * @param radiusPx radius in pixels; values {@code <= 0} are clamped defensively
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setPointStyle(@NonNull final Color color, final double radiusPx) {
        final double safeRadius = radiusPx > 0 ? radiusPx : 1.0;

        FxBootstrap.runLater(() -> {
            this.pointColor = color;
            this.pointRadiusPx = safeRadius;
            redrawOverlay();
        });
    }

    /**
     * Updates the visual style used for drawing polylines and expression plots.
     *
     * @param color   line stroke color; must not be {@code null}
     * @param widthPx stroke width in pixels; values {@code <= 0} are clamped defensively
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public void setLineStyle(@NonNull final Color color, final double widthPx) {
        final double safeWidth = widthPx > 0 ? widthPx : 1.0;

        FxBootstrap.runLater(() -> {
            this.lineColor = color;
            this.lineWidthPx = safeWidth;
            redrawOverlay();
        });
    }

    /**
     * Centers the world origin (0,0) within the pane.
     */
    public void centerOrigin() {
        FxBootstrap.runLater(() -> {
            pane.centerOrigin();
            schedulePlotUpdate();
            redrawOverlay();
        });
    }

    /**
     * Updates the pane theme.
     *
     * @param theme the new theme; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public void setTheme(@NonNull final DisplayTheme theme) {
        FxBootstrap.runLater(() -> {
            pane.setTheme(theme);
            redrawOverlay();
        });
    }

    /**
     * Shows the pane in a standalone window using defaults from {@link WindowConfig}.
     */
    public void show() {
        show(WindowConfig.DEFAULT_WINDOW_TITLE, WindowConfig.DEFAULT_WINDOW_WIDTH, WindowConfig.DEFAULT_WINDOW_HEIGHT);
    }

    /**
     * Shows the pane in a standalone window with a custom title and initial dimensions.
     *
     * @param title  the stage title; must not be {@code null}
     * @param width  the preferred initial width in pixels; non-positive values are treated as invalid and may be replaced
     * @param height the preferred initial height in pixels; non-positive values are treated as invalid and may be replaced
     * @throws NullPointerException  if {@code title} is {@code null}
     * @throws IllegalStateException if JavaFX startup is interrupted (propagated from {@link FxBootstrap})
     */
    public void show(@NonNull final String title, final double width, final double height) {
        FxBootstrap.showInWindow(title, root, width, height);
    }

    private record PlotRequest(String expression, Map<String, String> variables, boolean implicitZeroContour) {
    }

    private static List<Point2D> copyPoints(@NonNull final List<Point2D> points) {
        for (int i = 0; i < points.size(); i++) {
            Objects.requireNonNull(points.get(i), "points[" + i + "] must not be null");
        }
        return List.copyOf(points);
    }

    private void configureOverlayCanvas() {
        overlayCanvas.setMouseTransparent(true);
        overlayCanvas.widthProperty().bind(pane.widthProperty());
        overlayCanvas.heightProperty().bind(pane.heightProperty());
    }

    private void registerOverlayRedrawTriggers() {
        final InvalidationListener redrawListener = obs -> {
            schedulePlotUpdate();
            redrawOverlay();
        };

        overlayCanvas.widthProperty().addListener(redrawListener);
        overlayCanvas.heightProperty().addListener(redrawListener);

        pane.getScalePxPerUnit().addListener(redrawListener);
        pane.getOriginOffsetX().addListener(redrawListener);
        pane.getOriginOffsetY().addListener(redrawListener);
    }

    private void schedulePlotUpdate() {
        if (plotRequest == null) {
            return;
        }
        if (plotUpdateScheduled) {
            return;
        }

        plotUpdateScheduled = true;
        FxBootstrap.runLater(() -> {
            plotUpdateScheduled = false;
            recomputePlotForViewport();
            redrawOverlay();
        });
    }

    private void recomputePlotForViewport() {
        if (plotRequest == null) {
            return;
        }

        final GraphFxCalculator.WorldBounds bounds = currentViewportWorldBounds();
        final double widthPx = Math.max(1.0, overlayCanvas.getWidth());
        final double heightPx = Math.max(1.0, overlayCanvas.getHeight());

        final int cellsX = clampInt((int) (widthPx / 6.0), 80, 260);
        final int cellsY = clampInt((int) (heightPx / 6.0), 60, 220);

        contourSegmentsWorld.clear();
        polylineWorld.clear();

        if (plotRequest.implicitZeroContour) {
            final List<GraphFxCalculator.LineSegment> segments = calculator.createImplicitZeroContourSegments(
                    plotRequest.expression,
                    plotRequest.variables,
                    bounds,
                    cellsX,
                    cellsY,
                    () -> false
            );
            contourSegmentsWorld.addAll(segments);
        } else {
            final int samples = clampInt((int) (widthPx * 1.5), 200, 4000);
            final List<Point2D> polyline = calculator.createExplicitPolyline(
                    plotRequest.expression,
                    plotRequest.variables,
                    bounds,
                    samples,
                    () -> false
            );
            polylineWorld.addAll(polyline);
        }
    }

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

    private void redrawOverlay() {
        final double width = overlayCanvas.getWidth();
        final double height = overlayCanvas.getHeight();

        final GraphicsContext graphicsContext = overlayCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, width, height);

        drawContourSegments(graphicsContext);
        drawPolyline(graphicsContext);
        drawPoints(graphicsContext);
    }

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

    private void drawPolyline(@NonNull final GraphicsContext graphicsContext) {
        if (polylineWorld.size() < 2) {
            return;
        }

        graphicsContext.setStroke(lineColor);
        graphicsContext.setLineWidth(lineWidthPx);

        final Point2D first = worldToScreen(polylineWorld.getFirst());
        graphicsContext.beginPath();
        graphicsContext.moveTo(first.getX(), first.getY());

        for (int i = 1; i < polylineWorld.size(); i++) {
            final Point2D screenPoint = worldToScreen(polylineWorld.get(i));
            graphicsContext.lineTo(screenPoint.getX(), screenPoint.getY());
        }

        graphicsContext.stroke();
    }

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

    private Point2D worldToScreen(@NonNull final Point2D worldPoint) {
        final double scale = pane.getScalePxPerUnit().get();
        final double originX = pane.getOriginOffsetX().get();
        final double originY = pane.getOriginOffsetY().get();

        final double screenX = originX + worldPoint.getX() * scale;
        final double screenY = originY - worldPoint.getY() * scale;

        return new Point2D(screenX, screenY);
    }

    private static int clampInt(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

}
