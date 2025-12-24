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

@Getter
public final class GraphFxOverlayPlot {

    private final GraphFxDisplayPane displayPane;
    private final Canvas overlayCanvas;
    private final StackPane root;

    private final List<Point2D> pointsWorld = new ArrayList<>();
    private final List<Point2D> polylineWorld = new ArrayList<>();

    private Color pointColor = Color.RED;
    private Color lineColor = Color.DODGERBLUE;
    private double pointRadiusPx = 4.0;
    private double lineWidthPx = 2.0;

    public GraphFxOverlayPlot(@NonNull final GraphFxDisplayPane displayPane) {
        this.displayPane = displayPane;
        this.overlayCanvas = new Canvas();
        this.root = new StackPane(displayPane, overlayCanvas);

        overlayCanvas.setMouseTransparent(true);
        overlayCanvas.widthProperty().bind(displayPane.widthProperty());
        overlayCanvas.heightProperty().bind(displayPane.heightProperty());

        final InvalidationListener redraw = obs -> redraw();
        overlayCanvas.widthProperty().addListener(redraw);
        overlayCanvas.heightProperty().addListener(redraw);

        displayPane.getScalePxPerUnit().addListener(redraw);
        displayPane.getOriginOffsetX().addListener(redraw);
        displayPane.getOriginOffsetY().addListener(redraw);

        redraw();
    }

    public Parent asNode() {
        return root;
    }

    public void setPoints(@NonNull final List<Point2D> worldPoints) {
        pointsWorld.clear();
        pointsWorld.addAll(worldPoints);
        redraw();
    }

    public void setPolyline(@NonNull final List<Point2D> worldPolyline) {
        polylineWorld.clear();
        polylineWorld.addAll(worldPolyline);
        redraw();
    }

    public void clear() {
        pointsWorld.clear();
        polylineWorld.clear();
        redraw();
    }

    public void setPointStyle(@NonNull final Color color, final double radiusPx) {
        this.pointColor = color;
        this.pointRadiusPx = Math.max(0.5, radiusPx);
        redraw();
    }

    public void setLineStyle(@NonNull final Color color, final double widthPx) {
        this.lineColor = color;
        this.lineWidthPx = Math.max(0.5, widthPx);
        redraw();
    }

    private void redraw() {
        final double width = overlayCanvas.getWidth();
        final double height = overlayCanvas.getHeight();

        final GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        drawPolyline(gc);
        drawPoints(gc);
    }

    private void drawPoints(@NonNull final GraphicsContext graphicsContext) {
        if (pointsWorld.isEmpty()) {
            return;
        }

        graphicsContext.setFill(pointColor);

        for (final Point2D world : pointsWorld) {
            final Point2D screen = worldToScreen(world);
            final double x = screen.getX() - pointRadiusPx;
            final double y = screen.getY() - pointRadiusPx;
            graphicsContext.fillOval(x, y, pointRadiusPx * 2.0, pointRadiusPx * 2.0);
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
            final Point2D p = worldToScreen(polylineWorld.get(i));
            graphicsContext.lineTo(p.getX(), p.getY());
        }

        graphicsContext.stroke();
    }

    private Point2D worldToScreen(@NonNull final Point2D world) {
        return getPoint2D(world, displayPane);
    }

    static Point2D getPoint2D(@NonNull Point2D world, GraphFxDisplayPane displayPane) {
        final double scale = displayPane.getScalePxPerUnit().get();
        final double originX = displayPane.getOriginOffsetX().get();
        final double originY = displayPane.getOriginOffsetY().get();

        final double screenX = originX + world.getX() * scale;
        final double screenY = originY - world.getY() * scale;

        return new Point2D(screenX, screenY);
    }

}
