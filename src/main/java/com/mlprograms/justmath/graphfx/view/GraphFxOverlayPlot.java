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

package com.mlprograms.justmath.graphfx.view;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX-only overlay layer that draws simple helpers (points and a polyline) on top of a {@link GraphFxDisplayPane}.
 * <p>
 * The overlay is implemented as an additional {@link Canvas} stacked above the display pane. It automatically redraws
 * when either:
 * <ul>
 *   <li>the canvas size changes (because the {@code displayPane} resized), or</li>
 *   <li>the display viewport changes (scale and origin offsets).</li>
 * </ul>
 * <p>
 * This class intentionally contains <strong>no</strong> mathematical logic. It only transforms world coordinates into
 * screen coordinates using the current viewport configuration of the {@link GraphFxDisplayPane} and renders primitives.
 *
 * <h3>Thread-safety</h3>
 * This type is <strong>not</strong> thread-safe. All methods must be called on the JavaFX Application Thread.
 *
 * <h3>Lifecycle</h3>
 * If you create instances dynamically, call {@link #dispose()} when the overlay is no longer used to detach listeners
 * and unbind properties. This avoids memory leaks caused by listener references.
 */
public final class GraphFxOverlayPlot {

    /**
     * Minimum visual size to keep points/lines visible even with invalid input values.
     */
    private static final double MIN_PIXEL_SIZE = 0.5;

    /**
     * The display pane this overlay belongs to.
     */
    @Getter
    private final GraphFxDisplayPane displayPane;

    /**
     * The underlying canvas used for rendering the overlay.
     * <p>
     * This is package-private intentionally to keep the public API small and avoid leaking
     * rendering details to library users.
     */
    @Getter(AccessLevel.PACKAGE)
    private final Canvas overlayCanvas;

    /**
     * Root node holding the display pane and the overlay canvas.
     */
    private final StackPane root;

    /**
     * Points in world coordinates that should be rendered as filled circles.
     */
    private final List<Point2D> worldPoints;

    /**
     * Polyline vertices in world coordinates that should be rendered as a continuous line.
     */
    private final List<Point2D> worldPolyline;

    /**
     * Listener that triggers a redraw when viewport-related properties change.
     */
    private final InvalidationListener redrawListener;

    /**
     * Current fill color used for point rendering.
     */
    private Color pointColor;

    /**
     * Current stroke color used for polyline rendering.
     */
    private Color polylineColor;

    /**
     * Radius of point circles in pixels.
     */
    private double pointRadiusPixels;

    /**
     * Stroke width of the polyline in pixels.
     */
    private double polylineWidthPixels;

    /**
     * Whether this instance has been disposed and must no longer be used.
     */
    private boolean disposed;

    /**
     * Creates an overlay plot stacked above the given {@link GraphFxDisplayPane}.
     *
     * @param displayPane the display pane this overlay belongs to
     * @throws NullPointerException if {@code displayPane} is {@code null}
     */
    public GraphFxOverlayPlot(@NonNull final GraphFxDisplayPane displayPane) {
        this.displayPane = displayPane;

        this.overlayCanvas = new Canvas();
        this.overlayCanvas.setMouseTransparent(true);

        this.root = new StackPane(displayPane, overlayCanvas);

        this.worldPoints = new ArrayList<>();
        this.worldPolyline = new ArrayList<>();

        this.pointColor = Color.RED;
        this.polylineColor = Color.DODGERBLUE;
        this.pointRadiusPixels = 4.0;
        this.polylineWidthPixels = 2.0;

        this.redrawListener = ignored -> redraw();

        bindCanvasSizeToDisplayPane();
        registerViewportListeners();

        redraw();
    }

    /**
     * Returns the JavaFX node containing the display pane and the overlay canvas.
     * <p>
     * Add this node to your scene graph instead of adding the {@link GraphFxDisplayPane} directly.
     *
     * @return the overlay root node
     */
    public Parent asNode() {
        return root;
    }

    /**
     * Replaces the currently rendered points.
     *
     * @param worldPoints points in world coordinates (must not contain {@code null})
     * @throws NullPointerException     if {@code worldPoints} is {@code null}
     * @throws IllegalArgumentException if {@code worldPoints} contains {@code null} elements
     * @throws IllegalStateException    if this instance has been disposed
     */
    public void setPoints(@NonNull final List<Point2D> worldPoints) {
        ensureUsable();
        validateNoNullElements(worldPoints, "worldPoints");

        this.worldPoints.clear();
        this.worldPoints.addAll(worldPoints);
        redraw();
    }

    /**
     * Replaces the currently rendered polyline.
     *
     * @param worldPolyline polyline vertices in world coordinates (must not contain {@code null})
     * @throws NullPointerException     if {@code worldPolyline} is {@code null}
     * @throws IllegalArgumentException if {@code worldPolyline} contains {@code null} elements
     * @throws IllegalStateException    if this instance has been disposed
     */
    public void setPolyline(@NonNull final List<Point2D> worldPolyline) {
        ensureUsable();
        validateNoNullElements(worldPolyline, "worldPolyline");

        this.worldPolyline.clear();
        this.worldPolyline.addAll(worldPolyline);
        redraw();
    }

    /**
     * Removes all overlay primitives (points and polyline).
     *
     * @throws IllegalStateException if this instance has been disposed
     */
    public void clear() {
        ensureUsable();
        worldPoints.clear();
        worldPolyline.clear();
        redraw();
    }

    /**
     * Configures the point style.
     *
     * @param color          fill color for points
     * @param radiusInPixels radius in pixels; values smaller than {@value #MIN_PIXEL_SIZE} are clamped
     * @throws NullPointerException  if {@code color} is {@code null}
     * @throws IllegalStateException if this instance has been disposed
     */
    public void setPointStyle(@NonNull final Color color, final double radiusInPixels) {
        ensureUsable();
        this.pointColor = color;
        this.pointRadiusPixels = clampPixelSize(radiusInPixels);
        redraw();
    }

    /**
     * Configures the polyline style.
     *
     * @param color         stroke color for the polyline
     * @param widthInPixels stroke width in pixels; values smaller than {@value #MIN_PIXEL_SIZE} are clamped
     * @throws NullPointerException  if {@code color} is {@code null}
     * @throws IllegalStateException if this instance has been disposed
     */
    public void setPolylineStyle(@NonNull final Color color, final double widthInPixels) {
        ensureUsable();
        this.polylineColor = color;
        this.polylineWidthPixels = clampPixelSize(widthInPixels);
        redraw();
    }

    /**
     * Detaches all listeners and unbinds canvas properties.
     * <p>
     * After calling this method, this overlay must not be used anymore. Calling any mutating method will throw
     * an {@link IllegalStateException}. Calling {@code dispose()} multiple times has no further effect.
     */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;

        unbindCanvasSizeFromDisplayPane();
        unregisterViewportListeners();

        worldPoints.clear();
        worldPolyline.clear();
    }

    /**
     * Binds the overlay canvas size to the display pane size and registers size-change redraw listeners.
     * <p>
     * This ensures the overlay always matches the display pane dimensions and redraws on resize.
     *
     * @throws IllegalStateException if this instance has been disposed
     */
    private void bindCanvasSizeToDisplayPane() {
        ensureNotDisposedForInternalOperation("bindCanvasSizeToDisplayPane");

        overlayCanvas.widthProperty().bind(displayPane.widthProperty());
        overlayCanvas.heightProperty().bind(displayPane.heightProperty());

        overlayCanvas.widthProperty().addListener(redrawListener);
        overlayCanvas.heightProperty().addListener(redrawListener);
    }

    /**
     * Removes resize listeners and unbinds the overlay canvas size properties from the display pane.
     * <p>
     * This is part of {@link #dispose()} to break listener chains and prevent memory leaks.
     */
    private void unbindCanvasSizeFromDisplayPane() {
        overlayCanvas.widthProperty().removeListener(redrawListener);
        overlayCanvas.heightProperty().removeListener(redrawListener);

        overlayCanvas.widthProperty().unbind();
        overlayCanvas.heightProperty().unbind();
    }

    /**
     * Registers listeners for viewport-relevant properties of the display pane.
     * <p>
     * Any change to scale or origin offsets will trigger a redraw so that the overlay stays aligned with the plot.
     *
     * @throws IllegalStateException if this instance has been disposed
     */
    private void registerViewportListeners() {
        ensureNotDisposedForInternalOperation("registerViewportListeners");

        displayPane.getScalePxPerUnit().addListener(redrawListener);
        displayPane.getOriginOffsetX().addListener(redrawListener);
        displayPane.getOriginOffsetY().addListener(redrawListener);
    }

    /**
     * Unregisters previously registered viewport listeners from the display pane.
     * <p>
     * This is part of {@link #dispose()} to avoid strong references from the display pane to this overlay.
     */
    private void unregisterViewportListeners() {
        displayPane.getScalePxPerUnit().removeListener(redrawListener);
        displayPane.getOriginOffsetX().removeListener(redrawListener);
        displayPane.getOriginOffsetY().removeListener(redrawListener);
    }

    /**
     * Clears the overlay canvas and redraws all currently configured primitives (polyline first, then points).
     * <p>
     * If this overlay has been disposed, this method returns immediately without doing any work.
     * <p>
     * <strong>Side effects:</strong> clears and repaints the overlay canvas.
     */
    private void redraw() {
        if (disposed) {
            return;
        }

        final double canvasWidth = overlayCanvas.getWidth();
        final double canvasHeight = overlayCanvas.getHeight();

        final GraphicsContext graphicsContext = overlayCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);

        drawPolyline(graphicsContext);
        drawPoints(graphicsContext);
    }

    /**
     * Draws all currently configured world points as filled circles.
     *
     * @param graphicsContext the graphics context to draw to
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawPoints(@NonNull final GraphicsContext graphicsContext) {
        if (worldPoints.isEmpty()) {
            return;
        }

        graphicsContext.setFill(pointColor);

        for (final Point2D worldPoint : worldPoints) {
            final Point2D screenPoint = toScreenCoordinates(worldPoint);

            final double topLeftX = screenPoint.getX() - pointRadiusPixels;
            final double topLeftY = screenPoint.getY() - pointRadiusPixels;

            final double diameter = pointRadiusPixels * 2.0;
            graphicsContext.fillOval(topLeftX, topLeftY, diameter, diameter);
        }
    }

    /**
     * Draws the currently configured polyline as a continuous stroked path.
     *
     * @param graphicsContext the graphics context to draw to
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawPolyline(@NonNull final GraphicsContext graphicsContext) {
        if (worldPolyline.size() < 2) {
            return;
        }

        graphicsContext.setStroke(polylineColor);
        graphicsContext.setLineWidth(polylineWidthPixels);

        final Point2D firstScreenPoint = toScreenCoordinates(worldPolyline.getFirst());

        graphicsContext.beginPath();
        graphicsContext.moveTo(firstScreenPoint.getX(), firstScreenPoint.getY());

        for (int index = 1; index < worldPolyline.size(); index++) {
            final Point2D screenPoint = toScreenCoordinates(worldPolyline.get(index));
            graphicsContext.lineTo(screenPoint.getX(), screenPoint.getY());
        }

        graphicsContext.stroke();
    }

    /**
     * Converts a world coordinate into a screen coordinate using the current viewport configuration of the display pane.
     * <p>
     * X grows to the right; Y grows upwards in world space. Screen space uses the typical JavaFX coordinate system
     * where Y grows downwards, therefore Y is inverted.
     *
     * @param worldPoint the point in world coordinates
     * @return the converted point in screen coordinates (pixels)
     * @throws NullPointerException if {@code worldPoint} is {@code null}
     */
    private Point2D toScreenCoordinates(@NonNull final Point2D worldPoint) {
        final ObservableValue<Number> scaleProperty = displayPane.getScalePxPerUnit();
        final ObservableValue<Number> originOffsetXProperty = displayPane.getOriginOffsetX();
        final ObservableValue<Number> originOffsetYProperty = displayPane.getOriginOffsetY();

        final double scalePixelsPerUnit = scaleProperty.getValue().doubleValue();
        final double originPixelsX = originOffsetXProperty.getValue().doubleValue();
        final double originPixelsY = originOffsetYProperty.getValue().doubleValue();

        final double screenPixelsX = originPixelsX + worldPoint.getX() * scalePixelsPerUnit;
        final double screenPixelsY = originPixelsY - worldPoint.getY() * scalePixelsPerUnit;

        return new Point2D(screenPixelsX, screenPixelsY);
    }

    /**
     * Clamps a pixel-sized value to a minimal visible size and sanitizes invalid floating point input.
     * <p>
     * If {@code value} is {@code NaN} or infinite, {@value #MIN_PIXEL_SIZE} is returned.
     *
     * @param value a pixel value (radius/width)
     * @return a safe pixel value that is always at least {@value #MIN_PIXEL_SIZE}
     */
    private static double clampPixelSize(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return MIN_PIXEL_SIZE;
        }
        return Math.max(MIN_PIXEL_SIZE, value);
    }

    /**
     * Validates that a list contains no {@code null} elements.
     *
     * @param values        the list to validate
     * @param parameterName name used in exception messages for clarity
     * @throws NullPointerException     if {@code values} or {@code parameterName} is {@code null}
     * @throws IllegalArgumentException if {@code values} contains a {@code null} element
     */
    private static void validateNoNullElements(@NonNull final List<?> values, @NonNull final String parameterName) {
        for (int index = 0; index < values.size(); index++) {
            if (values.get(index) == null) {
                throw new IllegalArgumentException(
                        parameterName + " must not contain null elements (null at index " + index + ")."
                );
            }
        }
    }

    /**
     * Ensures this overlay is not disposed and therefore still usable for public operations.
     *
     * @throws IllegalStateException if this overlay has been disposed
     */
    private void ensureUsable() {
        if (disposed) {
            throw new IllegalStateException("GraphFxOverlayPlot is disposed and can no longer be used.");
        }
    }

    /**
     * Ensures this overlay is not disposed for internal setup/teardown operations.
     * <p>
     * This method exists to keep internal precondition checks expressive and searchable.
     *
     * @param operationName the internal operation name for debugging purposes
     * @throws NullPointerException  if {@code operationName} is {@code null}
     * @throws IllegalStateException if this overlay has been disposed
     */
    private void ensureNotDisposedForInternalOperation(@NonNull final String operationName) {
        if (disposed) {
            throw new IllegalStateException("GraphFxOverlayPlot is disposed; cannot run internal operation: " + operationName);
        }
    }

}
