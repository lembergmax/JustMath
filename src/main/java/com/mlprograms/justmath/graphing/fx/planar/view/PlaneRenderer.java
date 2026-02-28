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

package com.mlprograms.justmath.graphing.fx.planar.view;

import com.mlprograms.justmath.graphing.fx.planar.model.PlotLine;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotPoint;
import com.mlprograms.justmath.graphing.fx.planar.model.PlotResult;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.NonNull;

import java.util.List;

/**
 * Renderer for Cartesian plane visuals.
 * <p>
 * Handles:
 * <ul>
 *     <li>Background clearing</li>
 *     <li>Grid lines and axes</li>
 *     <li>Plot primitives (lines, points)</li>
 * </ul>
 * </p>
 */
public final class PlaneRenderer {

    /**
     * Renders the entire plane for the provided viewport.
     *
     * @param graphicsContext drawing context (non-null)
     * @param widthPx         canvas width in pixels
     * @param heightPx        canvas height in pixels
     * @param bounds          visible world bounds
     * @param pixelsPerUnit   scale factor
     * @param plotResult      plot primitives to render
     */
    public void render(@NonNull final GraphicsContext graphicsContext,
                       final double widthPx,
                       final double heightPx,
                       @NonNull final VisibleWorldBounds bounds,
                       final double pixelsPerUnit,
                       @NonNull final PlotResult plotResult) {
        clear(graphicsContext, widthPx, heightPx);
        drawGrid(graphicsContext, widthPx, heightPx, bounds, pixelsPerUnit);
        drawAxes(graphicsContext, widthPx, heightPx, bounds, pixelsPerUnit);
        drawPlot(graphicsContext, widthPx, heightPx, bounds, pixelsPerUnit, plotResult);
    }

    /**
     * Clears the canvas.
     */
    private void clear(final GraphicsContext graphicsContext, final double widthPx, final double heightPx) {
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(0, 0, widthPx, heightPx);
    }

    /**
     * Draws the grid.
     */
    private void drawGrid(final GraphicsContext graphicsContext,
                          final double widthPx,
                          final double heightPx,
                          final VisibleWorldBounds bounds,
                          final double pixelsPerUnit) {
        final double gridStep = chooseGridStep(pixelsPerUnit);

        graphicsContext.setStroke(Color.rgb(0, 0, 0, 0.08));
        graphicsContext.setLineWidth(1.0);

        final double startX = Math.floor(bounds.minX() / gridStep) * gridStep;
        for (double x = startX; x <= bounds.maxX(); x += gridStep) {
            final double sx = worldToScreenX(x, widthPx, bounds, pixelsPerUnit);
            graphicsContext.strokeLine(sx, 0, sx, heightPx);
        }

        final double startY = Math.floor(bounds.minY() / gridStep) * gridStep;
        for (double y = startY; y <= bounds.maxY(); y += gridStep) {
            final double sy = worldToScreenY(y, heightPx, bounds, pixelsPerUnit);
            graphicsContext.strokeLine(0, sy, widthPx, sy);
        }
    }

    /**
     * Draws axes through world origin.
     */
    private void drawAxes(final GraphicsContext graphicsContext,
                          final double widthPx,
                          final double heightPx,
                          final VisibleWorldBounds bounds,
                          final double pixelsPerUnit) {
        graphicsContext.setStroke(Color.rgb(0, 0, 0, 0.35));
        graphicsContext.setLineWidth(1.5);

        final double originScreenX = worldToScreenX(0.0, widthPx, bounds, pixelsPerUnit);
        final double originScreenY = worldToScreenY(0.0, heightPx, bounds, pixelsPerUnit);

        graphicsContext.strokeLine(originScreenX, 0, originScreenX, heightPx);
        graphicsContext.strokeLine(0, originScreenY, widthPx, originScreenY);
    }

    /**
     * Draws plot lines and points.
     */
    private void drawPlot(final GraphicsContext graphicsContext,
                          final double widthPx,
                          final double heightPx,
                          final VisibleWorldBounds bounds,
                          final double pixelsPerUnit,
                          final PlotResult plotResult) {
        final List<PlotLine> lines = plotResult.plotLines();
        final List<PlotPoint> points = plotResult.plotPoints();

        graphicsContext.setStroke(Color.rgb(70, 20, 200, 0.90));
        graphicsContext.setLineWidth(2.0);

        for (final PlotLine line : lines) {
            final List<PlotPoint> poly = line.plotPoints();
            for (int i = 1; i < poly.size(); i++) {
                final PlotPoint p0 = poly.get(i - 1);
                final PlotPoint p1 = poly.get(i);

                final double x0 = worldToScreenX(p0.x().doubleValue(), widthPx, bounds, pixelsPerUnit);
                final double y0 = worldToScreenY(p0.y().doubleValue(), heightPx, bounds, pixelsPerUnit);
                final double x1 = worldToScreenX(p1.x().doubleValue(), widthPx, bounds, pixelsPerUnit);
                final double y1 = worldToScreenY(p1.y().doubleValue(), heightPx, bounds, pixelsPerUnit);

                graphicsContext.strokeLine(x0, y0, x1, y1);
            }
        }

        graphicsContext.setFill(Color.rgb(70, 20, 200, 0.85));
        for (final PlotPoint point : points) {
            final double x = worldToScreenX(point.x().doubleValue(), widthPx, bounds, pixelsPerUnit);
            final double y = worldToScreenY(point.y().doubleValue(), heightPx, bounds, pixelsPerUnit);
            graphicsContext.fillOval(x - 2.0, y - 2.0, 4.0, 4.0);
        }
    }

    /**
     * Chooses a grid step size based on the zoom level.
     */
    private static double chooseGridStep(final double pixelsPerUnit) {
        final double targetPixels = 90.0d;
        final double worldUnits = targetPixels / pixelsPerUnit;

        final double base = Math.pow(10, Math.floor(Math.log10(worldUnits)));
        final double fraction = worldUnits / base;

        final double rounded;
        if (fraction < 2.0) {
            rounded = 2.0;
        } else if (fraction < 5.0) {
            rounded = 5.0;
        } else {
            rounded = 10.0;
        }

        return rounded * base;
    }

    /**
     * Converts world x to screen x.
     */
    private static double worldToScreenX(final double worldX,
                                         final double widthPx,
                                         final VisibleWorldBounds bounds,
                                         final double pixelsPerUnit) {
        final double centerX = (bounds.minX() + bounds.maxX()) / 2.0;
        return (worldX - centerX) * pixelsPerUnit + widthPx / 2.0;
    }

    /**
     * Converts world y to screen y.
     */
    private static double worldToScreenY(final double worldY,
                                         final double heightPx,
                                         final VisibleWorldBounds bounds,
                                         final double pixelsPerUnit) {
        final double centerY = (bounds.minY() + bounds.maxY()) / 2.0;
        return heightPx / 2.0 - (worldY - centerY) * pixelsPerUnit;
    }
}
