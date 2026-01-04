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

package com.mlprograms.justmath.graphfx.view.internal;

import com.mlprograms.justmath.graphfx.api.plot.GraphFxLineSegment;
import com.mlprograms.justmath.graphfx.core.GraphFxPoint;
import com.mlprograms.justmath.graphfx.view.GraphFxDisplayPane;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX-only overlay canvas that can render multiple plot series (polylines and segments) on top of a {@link GraphFxDisplayPane}.
 *
 * <p>This class is intentionally placed in an {@code internal} package. It is not part of the stable public API and
 * may change without notice.</p>
 */
public final class GraphFxSeriesOverlay extends Canvas {

    /**
     * A drawable plot series snapshot.
     *
     * @param color stroke color
     * @param strokeWidth stroke width in pixels
     * @param polyline explicit polyline points (may contain NaN breaks)
     * @param segments implicit line segments
     */
    public record Series(Color color, double strokeWidth, List<GraphFxPoint> polyline, List<GraphFxLineSegment> segments) {

        /**
         * Canonical constructor that copies lists.
         *
         * @param color stroke color
         * @param strokeWidth stroke width in pixels
         * @param polyline polyline points
         * @param segments segments
         */
        public Series {
            polyline = List.copyOf(polyline == null ? List.of() : polyline);
            segments = List.copyOf(segments == null ? List.of() : segments);
        }
    }

    private final GraphFxDisplayPane displayPane;

    private List<Series> series;

    /**
     * Creates a new overlay bound to the given display pane.
     *
     * @param displayPane non-null display pane
     * @throws NullPointerException if {@code displayPane} is {@code null}
     */
    public GraphFxSeriesOverlay(@NonNull final GraphFxDisplayPane displayPane) {
        this.displayPane = displayPane;
        this.series = List.of();

        setMouseTransparent(true);

        widthProperty().bind(displayPane.widthProperty());
        heightProperty().bind(displayPane.heightProperty());

        widthProperty().addListener(obs -> redraw());
        heightProperty().addListener(obs -> redraw());

        displayPane.getScalePxPerUnit().addListener(obs -> redraw());
        displayPane.getOriginOffsetX().addListener(obs -> redraw());
        displayPane.getOriginOffsetY().addListener(obs -> redraw());
    }

    /**
     * Replaces all currently rendered series and redraws immediately.
     *
     * @param newSeries series list (nullable -> treated as empty)
     */
    public void setSeries(final List<Series> newSeries) {
        this.series = List.copyOf(newSeries == null ? List.of() : newSeries);
        redraw();
    }

    /**
     * Clears the overlay content.
     */
    public void clear() {
        setSeries(List.of());
    }

    /**
     * Redraws all series to the canvas.
     */
    public void redraw() {
        final GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        for (final Series s : series) {
            drawSegments(gc, s);
            drawPolyline(gc, s);
        }
    }

    private void drawSegments(@NonNull final GraphicsContext gc, @NonNull final Series s) {
        if (s.segments().isEmpty()) {
            return;
        }

        gc.setStroke(s.color());
        gc.setLineWidth(Math.max(0.5, s.strokeWidth()));

        for (final GraphFxLineSegment seg : s.segments()) {
            final Point2D a = displayPane.worldToScreen(seg.start());
            final Point2D b = displayPane.worldToScreen(seg.end());
            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
    }

    private void drawPolyline(@NonNull final GraphicsContext gc, @NonNull final Series s) {
        if (s.polyline().size() < 2) {
            return;
        }

        gc.setStroke(s.color());
        gc.setLineWidth(Math.max(0.5, s.strokeWidth()));

        boolean pathStarted = false;

        for (final GraphFxPoint p : s.polyline()) {
            if (!isFinite(p)) {
                if (pathStarted) {
                    gc.stroke();
                    pathStarted = false;
                }
                continue;
            }

            final Point2D screen = displayPane.worldToScreen(p);
            if (!pathStarted) {
                gc.beginPath();
                gc.moveTo(screen.getX(), screen.getY());
                pathStarted = true;
            } else {
                gc.lineTo(screen.getX(), screen.getY());
            }
        }

        if (pathStarted) {
            gc.stroke();
        }
    }

    private static boolean isFinite(@NonNull final GraphFxPoint p) {
        return Double.isFinite(p.x()) && Double.isFinite(p.y());
    }
}
