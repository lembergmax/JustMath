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
package com.mlprograms.justmath.graphfx.planar.view;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphfx.planar.model.PlotLine;
import com.mlprograms.justmath.graphfx.planar.model.PlotPoint;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;

import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Internal high-performance plot surface with pan/zoom, grid and axis labeling.
 *
 * <p>
 * This class only renders data from {@link PlotResult}. It does not compute plot data.
 * </p>
 *
 * <p><strong>Design notes (library-grade):</strong></p>
 * <ul>
 *     <li>Viewport is represented by {@code (centerWorldX, centerWorldY, pixelsPerWorldUnit)} for stability on resize.</li>
 *     <li>Rendering is coalesced (single JavaFX pulse) to avoid redraw storms.</li>
 *     <li>Grid step and label step are auto-selected based on zoom level.</li>
 * </ul>
 */
final class GraphFxPlotSurface extends Region {

    private static final double EPSILON_FOR_ZERO = 1e-12;

    private final Canvas backgroundCanvas;
    private final Canvas plotCanvas;

    private final GraphicsContext backgroundGraphics;
    private final GraphicsContext plotGraphics;

    private PlotResult plotResult;

    private GraphFxViewerStyle style;
    private GraphFxViewConfiguration viewConfiguration;

    /**
     * World-space center coordinates. Stored as double for stable/fast transforms.
     */
    private double centerWorldX;
    private double centerWorldY;

    /**
     * Zoom level: pixels per world unit.
     */
    private double pixelsPerWorldUnit;

    private Point2D lastPanMousePoint;

    private boolean renderScheduled;

    GraphFxPlotSurface(
            final GraphFxViewerStyle style,
            final GraphFxViewConfiguration viewConfiguration
    ) {
        this.style = Objects.requireNonNull(style, "style must not be null");
        this.viewConfiguration = Objects.requireNonNull(viewConfiguration, "viewConfiguration must not be null");

        this.backgroundCanvas = new Canvas();
        this.plotCanvas = new Canvas();

        this.backgroundGraphics = backgroundCanvas.getGraphicsContext2D();
        this.plotGraphics = plotCanvas.getGraphicsContext2D();

        this.plotResult = new PlotResult();

        // Default viewport: center at origin, reasonable zoom.
        this.centerWorldX = 0.0;
        this.centerWorldY = 0.0;
        this.pixelsPerWorldUnit = 80.0;

        getChildren().addAll(backgroundCanvas, plotCanvas);

        installInteractions();
    }

    /**
     * Updates the plot data and schedules a redraw.
     *
     * @param plotResult plot data (must not be null)
     */
    void setPlotResult(final PlotResult plotResult) {
        this.plotResult = Objects.requireNonNull(plotResult, "plotResult must not be null");
        requestRender();
    }

    /**
     * Clears all plot data and schedules a redraw.
     */
    void clearPlot() {
        this.plotResult = new PlotResult();
        requestRender();
    }

    /**
     * Sets a new visual style.
     *
     * @param style style (must not be null)
     */
    void setStyle(final GraphFxViewerStyle style) {
        this.style = Objects.requireNonNull(style, "style must not be null");
        requestRender();
    }

    /**
     * Sets view configuration (grid, pan/zoom, spacing).
     *
     * @param viewConfiguration configuration (must not be null)
     */
    void setViewConfiguration(final GraphFxViewConfiguration viewConfiguration) {
        this.viewConfiguration = Objects.requireNonNull(viewConfiguration, "viewConfiguration must not be null");
        requestRender();
    }

    /**
     * Returns the current visible world bounds as {@link ViewportSnapshot}.
     *
     * @return viewport snapshot
     */
    ViewportSnapshot snapshotViewport() {
        double width = backgroundCanvas.getWidth();
        double height = backgroundCanvas.getHeight();

        if (!(width > 0.0) || !(height > 0.0) || !(pixelsPerWorldUnit > 0.0)) {
            return new ViewportSnapshot(new BigNumber("0"), new BigNumber("0"), new BigNumber("0"), new BigNumber("0"));
        }

        double halfWorldWidth = (width / 2.0) / pixelsPerWorldUnit;
        double halfWorldHeight = (height / 2.0) / pixelsPerWorldUnit;

        double minX = centerWorldX - halfWorldWidth;
        double maxX = centerWorldX + halfWorldWidth;
        double minY = centerWorldY - halfWorldHeight;
        double maxY = centerWorldY + halfWorldHeight;

        return new ViewportSnapshot(
                new BigNumber(Double.toString(minX), Locale.ROOT),
                new BigNumber(Double.toString(maxX), Locale.ROOT),
                new BigNumber(Double.toString(minY), Locale.ROOT),
                new BigNumber(Double.toString(maxY), Locale.ROOT)
        );
    }

    /**
     * Fits the viewport to the given world bounds.
     *
     * <p>
     * This method keeps aspect ratio by choosing the smaller of the two scales.
     * </p>
     *
     * @param viewportSnapshot world bounds (must not be null)
     */
    void fitViewport(final ViewportSnapshot viewportSnapshot) {
        Objects.requireNonNull(viewportSnapshot, "viewportSnapshot must not be null");

        double minX = bigNumberToDouble(viewportSnapshot.minX());
        double maxX = bigNumberToDouble(viewportSnapshot.maxX());
        double minY = bigNumberToDouble(viewportSnapshot.minY());
        double maxY = bigNumberToDouble(viewportSnapshot.maxY());

        if (!(maxX > minX) || !(maxY > minY)) {
            return;
        }

        double width = Math.max(1.0, backgroundCanvas.getWidth());
        double height = Math.max(1.0, backgroundCanvas.getHeight());

        double worldWidth = maxX - minX;
        double worldHeight = maxY - minY;

        double scaleX = width / worldWidth;
        double scaleY = height / worldHeight;

        centerWorldX = (minX + maxX) / 2.0;
        centerWorldY = (minY + maxY) / 2.0;

        pixelsPerWorldUnit = clamp(
                Math.min(scaleX, scaleY),
                viewConfiguration.getMinimumPixelsPerWorldUnit(),
                viewConfiguration.getMaximumPixelsPerWorldUnit()
        );

        requestRender();
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();

        backgroundCanvas.setWidth(width);
        backgroundCanvas.setHeight(height);

        plotCanvas.setWidth(width);
        plotCanvas.setHeight(height);

        // Stretch to fill.
        backgroundCanvas.relocate(0, 0);
        plotCanvas.relocate(0, 0);

        requestRender();
    }

    private void installInteractions() {
        setOnMousePressed(event -> {
            if (!viewConfiguration.isPanEnabled()) {
                return;
            }
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            lastPanMousePoint = new Point2D(event.getX(), event.getY());
        });

        setOnMouseDragged(event -> {
            if (!viewConfiguration.isPanEnabled()) {
                return;
            }
            if (lastPanMousePoint == null) {
                return;
            }

            Point2D current = new Point2D(event.getX(), event.getY());
            Point2D delta = current.subtract(lastPanMousePoint);

            panByPixels(delta.getX(), delta.getY());

            lastPanMousePoint = current;
            requestRender();
        });

        setOnMouseReleased(event -> lastPanMousePoint = null);
        setOnMouseExited(event -> lastPanMousePoint = null);

        addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!viewConfiguration.isZoomEnabled()) {
                return;
            }
            if (event.getDeltaY() == 0.0) {
                return;
            }

            double width = backgroundCanvas.getWidth();
            double height = backgroundCanvas.getHeight();
            if (!(width > 0.0) || !(height > 0.0)) {
                return;
            }

            zoomTowardsCursor(event.getX(), event.getY(), event.getDeltaY());
            requestRender();
            event.consume();
        });
    }

    private void panByPixels(final double deltaPixelsX, final double deltaPixelsY) {
        if (!(pixelsPerWorldUnit > 0.0)) {
            return;
        }

        // Screen X increases to the right: dragging right means we move the "camera" left (center decreases).
        centerWorldX -= deltaPixelsX / pixelsPerWorldUnit;

        // Screen Y increases downward: dragging down means we move the "camera" up (center increases).
        centerWorldY += deltaPixelsY / pixelsPerWorldUnit;
    }

    private void zoomTowardsCursor(final double cursorX, final double cursorY, final double wheelDeltaY) {
        double width = backgroundCanvas.getWidth();
        double height = backgroundCanvas.getHeight();

        double oldScale = pixelsPerWorldUnit;
        double zoomFactor = Math.pow(viewConfiguration.getMouseWheelZoomExponent(), wheelDeltaY);

        double newScale = clamp(
                oldScale * zoomFactor,
                viewConfiguration.getMinimumPixelsPerWorldUnit(),
                viewConfiguration.getMaximumPixelsPerWorldUnit()
        );

        if (Math.abs(newScale - oldScale) < 1e-9) {
            return;
        }

        // World coordinate under cursor before zoom.
        double worldX = screenToWorldX(cursorX, width, oldScale);
        double worldY = screenToWorldY(cursorY, height, oldScale);

        // Adjust center so that (worldX, worldY) stays under the cursor.
        centerWorldX = worldX - (cursorX - (width / 2.0)) / newScale;
        centerWorldY = worldY + (cursorY - (height / 2.0)) / newScale;

        pixelsPerWorldUnit = newScale;
    }

    private double screenToWorldX(final double screenX, final double canvasWidth, final double scale) {
        return centerWorldX + (screenX - (canvasWidth / 2.0)) / scale;
    }

    private double screenToWorldY(final double screenY, final double canvasHeight, final double scale) {
        return centerWorldY - (screenY - (canvasHeight / 2.0)) / scale;
    }

    private double worldToScreenX(final double worldX, final double canvasWidth) {
        return (canvasWidth / 2.0) + (worldX - centerWorldX) * pixelsPerWorldUnit;
    }

    private double worldToScreenY(final double worldY, final double canvasHeight) {
        return (canvasHeight / 2.0) - (worldY - centerWorldY) * pixelsPerWorldUnit;
    }

    private void requestRender() {
        if (renderScheduled) {
            return;
        }
        renderScheduled = true;
        Platform.runLater(() -> {
            renderScheduled = false;
            renderNow();
        });
    }

    private void renderNow() {
        double width = backgroundCanvas.getWidth();
        double height = backgroundCanvas.getHeight();

        if (!(width > 0.0) || !(height > 0.0)) {
            return;
        }

        // Background layer: clear + grid + axes + labels.
        backgroundGraphics.setFill(style.getBackgroundColor());
        backgroundGraphics.fillRect(0, 0, width, height);

        if (viewConfiguration.isGridVisible()) {
            renderGrid(width, height);
        }

        if (viewConfiguration.isAxesVisible()) {
            renderAxes(width, height);
        }

        if (viewConfiguration.isAxesVisible() && viewConfiguration.isAxisLabelsVisible()) {
            renderAxisLabels(width, height);
        }

        // Plot layer: clear + lines + points.
        plotGraphics.clearRect(0, 0, width, height);
        renderPlot(width, height);
    }

    private void renderGrid(final double width, final double height) {
        double minorStepWorld = chooseNiceStep(viewConfiguration.getTargetMinorGridSpacingInPixels() / pixelsPerWorldUnit);
        if (!(minorStepWorld > 0.0) || Double.isInfinite(minorStepWorld) || Double.isNaN(minorStepWorld)) {
            return;
        }

        int majorEvery = Math.max(1, viewConfiguration.getMinorLinesPerMajorLine());
        double majorStepWorld = minorStepWorld * majorEvery;

        ViewportBounds bounds = computeVisibleBounds(width, height);

        // Vertical lines
        double startX = floorToStep(bounds.minX, minorStepWorld);
        for (double x = startX; x <= bounds.maxX; x += minorStepWorld) {
            boolean isMajor = isMultipleOf(x, majorStepWorld);

            backgroundGraphics.setStroke(isMajor ? style.getMajorGridColor() : style.getMinorGridColor());
            backgroundGraphics.setLineWidth(isMajor ? style.getMajorGridStrokeWidthInPixels() : style.getMinorGridStrokeWidthInPixels());

            double sx = snapForCrispStroke(worldToScreenX(x, width), backgroundGraphics.getLineWidth());
            backgroundGraphics.strokeLine(sx, 0, sx, height);
        }

        // Horizontal lines
        double startY = floorToStep(bounds.minY, minorStepWorld);
        for (double y = startY; y <= bounds.maxY; y += minorStepWorld) {
            boolean isMajor = isMultipleOf(y, majorStepWorld);

            backgroundGraphics.setStroke(isMajor ? style.getMajorGridColor() : style.getMinorGridColor());
            backgroundGraphics.setLineWidth(isMajor ? style.getMajorGridStrokeWidthInPixels() : style.getMinorGridStrokeWidthInPixels());

            double sy = snapForCrispStroke(worldToScreenY(y, height), backgroundGraphics.getLineWidth());
            backgroundGraphics.strokeLine(0, sy, width, sy);
        }
    }

    private void renderAxes(final double width, final double height) {
        ViewportBounds bounds = computeVisibleBounds(width, height);

        boolean xAxisVisible = bounds.minY <= 0.0 && bounds.maxY >= 0.0;
        boolean yAxisVisible = bounds.minX <= 0.0 && bounds.maxX >= 0.0;

        backgroundGraphics.setStroke(style.getAxisColor());
        backgroundGraphics.setLineWidth(style.getAxisStrokeWidthInPixels());

        if (xAxisVisible) {
            double sy = snapForCrispStroke(worldToScreenY(0.0, height), style.getAxisStrokeWidthInPixels());
            backgroundGraphics.strokeLine(0, sy, width, sy);
        }
        if (yAxisVisible) {
            double sx = snapForCrispStroke(worldToScreenX(0.0, width), style.getAxisStrokeWidthInPixels());
            backgroundGraphics.strokeLine(sx, 0, sx, height);
        }
    }

    private void renderAxisLabels(final double width, final double height) {
        ViewportBounds bounds = computeVisibleBounds(width, height);

        boolean xAxisVisible = bounds.minY <= 0.0 && bounds.maxY >= 0.0;
        boolean yAxisVisible = bounds.minX <= 0.0 && bounds.maxX >= 0.0;

        // Requirement from you: labels on the thick axes in the middle (x=0 / y=0).
        // If an axis is not visible, we don't render its labels (clean + consistent).
        if (!xAxisVisible && !yAxisVisible) {
            return;
        }

        backgroundGraphics.setFont(style.getAxisLabelFont());
        backgroundGraphics.setFill(style.getAxisLabelColor());

        double labelStepWorld = chooseNiceStep(viewConfiguration.getMinimumAxisLabelSpacingInPixels() / pixelsPerWorldUnit);
        if (!(labelStepWorld > 0.0) || Double.isInfinite(labelStepWorld) || Double.isNaN(labelStepWorld)) {
            return;
        }

        double tickLen = viewConfiguration.getAxisTickLengthInPixels();
        double labelOffset = viewConfiguration.getAxisLabelOffsetInPixels();

        // X axis labels
        if (xAxisVisible) {
            double axisY = worldToScreenY(0.0, height);

            backgroundGraphics.setTextAlign(TextAlignment.CENTER);
            backgroundGraphics.setTextBaseline(VPos.TOP);

            double startX = floorToStep(bounds.minX, labelStepWorld);
            for (double x = startX; x <= bounds.maxX; x += labelStepWorld) {
                double sx = worldToScreenX(x, width);
                if (sx < 0 || sx > width) {
                    continue;
                }

                // Major tick
                backgroundGraphics.setStroke(style.getAxisColor());
                backgroundGraphics.setLineWidth(1.5);
                backgroundGraphics.strokeLine(
                        snapForCrispStroke(sx, 1.5),
                        snapForCrispStroke(axisY - tickLen / 2.0, 1.5),
                        snapForCrispStroke(sx, 1.5),
                        snapForCrispStroke(axisY + tickLen / 2.0, 1.5)
                );

                // Label
                String label = formatAxisNumber(x, style.getAxisLabelLocale());
                if (label.equals("0") && yAxisVisible) {
                    // avoid double "0" at origin (we keep it on the x axis only)
                    // if you prefer the other way, swap the condition.
                }

                double labelY = axisY + tickLen / 2.0 + labelOffset;
                // Keep label inside canvas
                labelY = clamp(labelY, 0.0, height - 2.0);

                backgroundGraphics.fillText(label, sx, labelY);
            }
        }

        // Y axis labels
        if (yAxisVisible) {
            double axisX = worldToScreenX(0.0, width);

            backgroundGraphics.setTextAlign(TextAlignment.LEFT);
            backgroundGraphics.setTextBaseline(VPos.CENTER);

            double startY = floorToStep(bounds.minY, labelStepWorld);
            for (double y = startY; y <= bounds.maxY; y += labelStepWorld) {
                double sy = worldToScreenY(y, height);
                if (sy < 0 || sy > height) {
                    continue;
                }

                // Major tick
                backgroundGraphics.setStroke(style.getAxisColor());
                backgroundGraphics.setLineWidth(1.5);
                backgroundGraphics.strokeLine(
                        snapForCrispStroke(axisX - tickLen / 2.0, 1.5),
                        snapForCrispStroke(sy, 1.5),
                        snapForCrispStroke(axisX + tickLen / 2.0, 1.5),
                        snapForCrispStroke(sy, 1.5)
                );

                // Label
                String label = formatAxisNumber(y, style.getAxisLabelLocale());
                if (label.equals("0")) {
                    // keep "0" only on x axis (cleaner at the origin)
                    continue;
                }

                double labelX = axisX + tickLen / 2.0 + labelOffset;
                labelX = clamp(labelX, 0.0, width - 2.0);

                backgroundGraphics.fillText(label, labelX, sy);
            }
        }
    }

    private void renderPlot(final double width, final double height) {
        List<PlotLine> lines = plotResult.plotLines();
        if (!lines.isEmpty()) {
            plotGraphics.setStroke(style.getPlotLineColor());
            plotGraphics.setLineWidth(style.getPlotLineStrokeWidthInPixels());

            for (PlotLine line : lines) {
                List<PlotPoint> points = line.plotPoints();
                if (points == null || points.size() < 2) {
                    continue;
                }

                double[] xs = new double[points.size()];
                double[] ys = new double[points.size()];
                int count = 0;

                for (PlotPoint p : points) {
                    double wx = bigNumberToDouble(p.x());
                    double wy = bigNumberToDouble(p.y());
                    if (!isFinite(wx) || !isFinite(wy)) {
                        continue;
                    }
                    xs[count] = worldToScreenX(wx, width);
                    ys[count] = worldToScreenY(wy, height);
                    count++;
                }

                if (count >= 2) {
                    plotGraphics.strokePolyline(xs, ys, count);
                }
            }
        }

        List<PlotPoint> plotPoints = plotResult.plotPoints();
        if (!plotPoints.isEmpty()) {
            plotGraphics.setFill(style.getPlotPointColor());

            double r = Math.max(0.5, style.getPlotPointRadiusInPixels());
            double d = r * 2.0;

            for (PlotPoint p : plotPoints) {
                double wx = bigNumberToDouble(p.x());
                double wy = bigNumberToDouble(p.y());
                if (!isFinite(wx) || !isFinite(wy)) {
                    continue;
                }

                double sx = worldToScreenX(wx, width);
                double sy = worldToScreenY(wy, height);

                plotGraphics.fillOval(sx - r, sy - r, d, d);
            }
        }
    }

    private ViewportBounds computeVisibleBounds(final double width, final double height) {
        double halfWorldWidth = (width / 2.0) / pixelsPerWorldUnit;
        double halfWorldHeight = (height / 2.0) / pixelsPerWorldUnit;

        return new ViewportBounds(
                centerWorldX - halfWorldWidth,
                centerWorldX + halfWorldWidth,
                centerWorldY - halfWorldHeight,
                centerWorldY + halfWorldHeight
        );
    }

    private double chooseNiceStep(final double rawStep) {
        if (!(rawStep > 0.0) || Double.isNaN(rawStep) || Double.isInfinite(rawStep)) {
            return 1.0;
        }

        double exponent = Math.floor(Math.log10(rawStep));
        double base = rawStep / Math.pow(10.0, exponent);

        double niceBase;
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

    private double floorToStep(final double value, final double step) {
        return Math.floor(value / step) * step;
    }

    private boolean isMultipleOf(final double value, final double step) {
        if (!(step > 0.0)) {
            return false;
        }
        double ratio = value / step;
        double nearest = Math.rint(ratio);
        return Math.abs(ratio - nearest) < 1e-9;
    }

    private double snapForCrispStroke(final double coordinate, final double strokeWidth) {
        // For odd-ish stroke widths, snapping to .5 improves crispness for 1px lines.
        if (strokeWidth <= 1.6) {
            return Math.floor(coordinate) + 0.5;
        }
        return coordinate;
    }

    private String formatAxisNumber(final double value, final Locale locale) {
        double normalized = Math.abs(value) < EPSILON_FOR_ZERO ? 0.0 : value;

        // Use BigNumber as requested (string-based), but keep input stable.
        BigNumber bn = new BigNumber(Double.toString(normalized), Locale.ROOT);
        String raw = bn.toString();

        char decimalSeparator = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
        if (decimalSeparator != '.') {
            raw = raw.replace('.', decimalSeparator);
        }

        // Avoid "-0"
        if (raw.equals("-0") || raw.equals("-0" + decimalSeparator + "0")) {
            return "0";
        }

        return raw;
    }

    private double bigNumberToDouble(final BigNumber value) {
        Objects.requireNonNull(value, "value must not be null");
        // Rendering uses double for performance. Plotting typically uses human-scale ranges.
        return Double.parseDouble(value.toString());
    }

    private boolean isFinite(final double value) {
        return Double.isFinite(value);
    }

    private double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record ViewportBounds(double minX, double maxX, double minY, double maxY) {
    }

}