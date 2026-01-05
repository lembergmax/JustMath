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

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * A self-contained JavaFX pane that renders a 2D coordinate grid on a {@link Canvas} and supports:
 * <ul>
 *   <li><b>Pan</b> via primary mouse drag</li>
 *   <li><b>Zoom</b> via mouse wheel (zooms towards the cursor)</li>
 * </ul>
 *
 * <h2>Coordinate System</h2>
 * <p>The pane uses an abstract world coordinate space. The view transform is defined by:</p>
 * <ul>
 *   <li>{@code scale}: pixels per world unit</li>
 *   <li>{@code offsetX}/{@code offsetY}: pixel translation (panning)</li>
 * </ul>
 *
 * <p>World-to-screen transform:</p>
 * <pre>
 * screenX = width/2  + worldX * scale + offsetX
 * screenY = height/2 - worldY * scale + offsetY
 * </pre>
 */
public final class PanZoomGridPane extends Pane {

    private static final double TARGET_GRID_PIXEL_STEP = 100.0;
    private static final double MIN_SCALE = 10.0;
    private static final double MAX_SCALE = 5000.0;

    private final Canvas canvas = new Canvas();

    private double scale = 80.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    private Point2D lastDragPoint;

    /**
     * Creates the pane, binds the internal canvas to the pane size, installs input handlers,
     * and performs an initial render.
     */
    public PanZoomGridPane() {
        getChildren().add(canvas);

        bindCanvasSizeToPane();
        installPanHandlers();
        installZoomHandlers();

        redraw();
    }

    /**
     * Computes a smooth zoom factor for a given scroll delta.
     *
     * @param deltaY the scroll delta
     * @return a zoom factor greater than 0
     */
    private static double computeZoomFactor(final double deltaY) {
        return Math.pow(1.0015, deltaY);
    }

    /**
     * Fills the background with a solid color.
     *
     * @param graphics the graphics context
     * @param width    the canvas width
     * @param height   the canvas height
     */
    private static void clearBackground(final GraphicsContext graphics, final double width, final double height) {
        graphics.setFill(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
    }

    /**
     * Chooses a visually pleasing grid step near the requested step (1/2/5 * 10^n).
     *
     * @param rawStep requested step in world units
     * @return a "nice" step size
     */
    private static double chooseNiceStep(final double rawStep) {
        if (rawStep <= 0.0 || Double.isNaN(rawStep) || Double.isInfinite(rawStep)) {
            return 1.0;
        }

        final double exponent = Math.floor(Math.log10(rawStep));
        final double base = rawStep / Math.pow(10.0, exponent);

        final double niceBase;
        if (base <= 1.0) {
            niceBase = 1.0;
        } else if (base <= 2.0) {
            niceBase = 2.0;
        } else if (base <= 5.0) {
            niceBase = 5.0;
        } else {
            niceBase = 10.0;
        }

        return niceBase * Math.pow(10.0, exponent);
    }

    /**
     * Floors a value to the nearest lower multiple of a step.
     *
     * @param value value to floor
     * @param step  step size
     * @return floored value
     */
    private static double floorToStep(final double value, final double step) {
        return Math.floor(value / step) * step;
    }

    /**
     * Clamps a value into an inclusive interval.
     *
     * @param value value to clamp
     * @param min   minimum allowed value
     * @param max   maximum allowed value
     * @return clamped value
     */
    private static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Binds the internal canvas to the pane's width/height and triggers redraw when resized.
     */
    private void bindCanvasSizeToPane() {
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        widthProperty().addListener((obs, oldValue, newValue) -> redraw());
        heightProperty().addListener((obs, oldValue, newValue) -> redraw());
    }

    /**
     * Installs mouse handlers that implement panning by dragging with the primary mouse button.
     */
    private void installPanHandlers() {
        setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            lastDragPoint = new Point2D(event.getX(), event.getY());
        });

        setOnMouseDragged(event -> {
            if (lastDragPoint == null) {
                return;
            }

            final Point2D currentPoint = new Point2D(event.getX(), event.getY());
            final Point2D delta = currentPoint.subtract(lastDragPoint);

            offsetX += delta.getX();
            offsetY += delta.getY();

            lastDragPoint = currentPoint;
            redraw();
        });

        setOnMouseReleased(event -> lastDragPoint = null);
    }

    /**
     * Installs a scroll handler that performs smooth zooming towards the cursor position.
     */
    private void installZoomHandlers() {
        addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() == 0.0) {
                return;
            }

            final double oldScale = scale;
            final double zoomFactor = computeZoomFactor(event.getDeltaY());
            final double newScale = clamp(oldScale * zoomFactor, MIN_SCALE, MAX_SCALE);

            applyZoomTowardsCursor(event.getX(), event.getY(), oldScale, newScale);

            redraw();
            event.consume();
        });
    }

    /**
     * Applies zoom while keeping the world coordinate under the cursor visually stable.
     *
     * @param mouseX   cursor x position in pane coordinates
     * @param mouseY   cursor y position in pane coordinates
     * @param oldScale the previous scale
     * @param newScale the new scale
     */
    private void applyZoomTowardsCursor(final double mouseX, final double mouseY, final double oldScale, final double newScale) {
        final double worldXBefore = screenToWorldX(mouseX, oldScale, offsetX);
        final double worldYBefore = screenToWorldY(mouseY, oldScale, offsetY);

        scale = newScale;

        offsetX = mouseX - (getWidth() / 2.0) - (worldXBefore * newScale);
        offsetY = mouseY - (getHeight() / 2.0) + (worldYBefore * newScale);
    }

    /**
     * Redraws the entire canvas (background, grid, axes, optional demo curve, HUD).
     */
    private void redraw() {
        final double width = canvas.getWidth();
        final double height = canvas.getHeight();

        if (width <= 0.0 || height <= 0.0) {
            return;
        }

        final GraphicsContext graphics = canvas.getGraphicsContext2D();

        clearBackground(graphics, width, height);

        final VisibleWorldBounds bounds = computeVisibleWorldBounds(width, height);
        final double gridStep = chooseNiceStep(TARGET_GRID_PIXEL_STEP / scale);

        drawGrid(graphics, width, height, bounds, gridStep);
        drawAxes(graphics, width, height);

        drawHud(graphics, gridStep);
    }

    /**
     * Computes the visible world coordinate range for the current view transform.
     *
     * @param width  canvas width
     * @param height canvas height
     * @return visible world bounds (never {@code null})
     */
    private VisibleWorldBounds computeVisibleWorldBounds(final double width, final double height) {
        final double minWorldX = screenToWorldX(0, scale, offsetX);
        final double maxWorldX = screenToWorldX(width, scale, offsetX);

        final double maxWorldY = screenToWorldY(0, scale, offsetY);
        final double minWorldY = screenToWorldY(height, scale, offsetY);

        return new VisibleWorldBounds(minWorldX, maxWorldX, minWorldY, maxWorldY);
    }

    /**
     * Draws the coordinate grid for the given visible bounds.
     *
     * @param graphics graphics context
     * @param width    canvas width
     * @param height   canvas height
     * @param bounds   visible world bounds
     * @param step     grid step size in world units
     */
    private void drawGrid(final GraphicsContext graphics, final double width, final double height, final VisibleWorldBounds bounds, final double step) {
        graphics.setStroke(Color.rgb(220, 220, 220));
        graphics.setLineWidth(1.0);

        drawVerticalGridLines(graphics, width, height, bounds.minX(), bounds.maxX(), step);
        drawHorizontalGridLines(graphics, width, height, bounds.minY(), bounds.maxY(), step);
    }

    /**
     * Draws vertical grid lines within the visible x-range.
     *
     * @param graphics  graphics context
     * @param width     canvas width
     * @param height    canvas height
     * @param minWorldX minimum visible world x
     * @param maxWorldX maximum visible world x
     * @param step      grid step size in world units
     */
    private void drawVerticalGridLines(final GraphicsContext graphics, final double width, final double height, final double minWorldX, final double maxWorldX, final double step) {
        final double startX = floorToStep(minWorldX, step);

        for (double x = startX; x <= maxWorldX; x += step) {
            final double screenX = worldToScreenX(x, scale, offsetX, width);
            graphics.strokeLine(screenX, 0, screenX, height);
        }
    }

    /**
     * Draws horizontal grid lines within the visible y-range.
     *
     * @param graphics  graphics context
     * @param width     canvas width
     * @param height    canvas height
     * @param minWorldY minimum visible world y
     * @param maxWorldY maximum visible world y
     * @param step      grid step size in world units
     */
    private void drawHorizontalGridLines(final GraphicsContext graphics, final double width, final double height, final double minWorldY, final double maxWorldY, final double step) {
        final double startY = floorToStep(minWorldY, step);

        for (double y = startY; y <= maxWorldY; y += step) {
            final double screenY = worldToScreenY(y, scale, offsetY, height);
            graphics.strokeLine(0, screenY, width, screenY);
        }
    }

    /**
     * Draws the x-axis (y=0) and y-axis (x=0).
     *
     * @param graphics graphics context
     * @param width    canvas width
     * @param height   canvas height
     */
    private void drawAxes(final GraphicsContext graphics, final double width, final double height) {
        final double xAxisScreenY = worldToScreenY(0.0, scale, offsetY, height);
        final double yAxisScreenX = worldToScreenX(0.0, scale, offsetX, width);

        graphics.setStroke(Color.rgb(120, 120, 120));
        graphics.setLineWidth(2.0);

        graphics.strokeLine(0, xAxisScreenY, width, xAxisScreenY);
        graphics.strokeLine(yAxisScreenX, 0, yAxisScreenX, height);
    }

    /**
     * Draws a small HUD (scale and grid step) in the top-left corner.
     *
     * @param graphics graphics context
     * @param gridStep current grid step size
     */
    private void drawHud(final GraphicsContext graphics, final double gridStep) {
        graphics.setFill(Color.rgb(20, 20, 20, 0.85));
        graphics.setFont(Font.font("Consolas", 13));

        final String text = String.format("scale=%.2f px/unit | gridStep=%.4g", scale, gridStep);
        graphics.fillText(text, 12, 20);
    }

    /**
     * Converts a world x-coordinate to screen x-coordinate.
     *
     * @param worldX world x
     * @param scale  pixels per world unit
     * @param panX   pixel pan offset on x-axis
     * @param width  canvas width
     * @return screen x
     */
    private double worldToScreenX(final double worldX, final double scale, final double panX, final double width) {
        return (width / 2.0) + (worldX * scale) + panX;
    }

    /**
     * Converts a world y-coordinate to screen y-coordinate.
     *
     * @param worldY world y
     * @param scale  pixels per world unit
     * @param panY   pixel pan offset on y-axis
     * @param height canvas height
     * @return screen y
     */
    private double worldToScreenY(final double worldY, final double scale, final double panY, final double height) {
        return (height / 2.0) - (worldY * scale) + panY;
    }

    /**
     * Converts a screen x-coordinate to world x-coordinate.
     *
     * @param screenX screen x
     * @param scale   pixels per world unit
     * @param panX    pixel pan offset on x-axis
     * @return world x
     */
    private double screenToWorldX(final double screenX, final double scale, final double panX) {
        return (screenX - (getWidth() / 2.0) - panX) / scale;
    }

    /**
     * Converts a screen y-coordinate to world y-coordinate.
     *
     * @param screenY screen y
     * @param scale   pixels per world unit
     * @param panY    pixel pan offset on y-axis
     * @return world y
     */
    private double screenToWorldY(final double screenY, final double scale, final double panY) {
        return ((getHeight() / 2.0) - screenY + panY) / scale;
    }

    /**
     * Immutable container describing the currently visible world coordinate range.
     *
     * @param minX minimum visible world x
     * @param maxX maximum visible world x
     * @param minY minimum visible world y
     * @param maxY maximum visible world y
     */
    private record VisibleWorldBounds(double minX, double maxX, double minY, double maxY) {
    }

}
