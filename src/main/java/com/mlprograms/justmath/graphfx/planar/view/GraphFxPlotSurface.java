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

    // --- in GraphFxPlotSurface ---

    private void renderGrid(final double width, final double height) {
        final GridSteps steps = computeGridSteps();
        if (!steps.isValid()) {
            return;
        }

        final ViewportBounds bounds = computeVisibleBounds(width, height);

        // Vertical minor + major lines (anchored to 0 by using integer indices)
        final long startXIndex = (long) Math.floor(bounds.minX / steps.minorStepWorld);
        final long endXIndex = (long) Math.ceil(bounds.maxX / steps.minorStepWorld);

        for (long i = startXIndex; i <= endXIndex; i++) {
            final boolean isMajor = (Math.floorMod(i, (long) steps.majorEvery) == 0L);
            final double x = i * steps.minorStepWorld;

            backgroundGraphics.setStroke(isMajor ? style.getMajorGridColor() : style.getMinorGridColor());
            final double strokeWidth = isMajor ? style.getMajorGridStrokeWidthInPixels() : style.getMinorGridStrokeWidthInPixels();
            backgroundGraphics.setLineWidth(strokeWidth);

            final double sx = snapForCrispStroke(worldToScreenX(x, width), strokeWidth);
            backgroundGraphics.strokeLine(sx, 0, sx, height);
        }

        // Horizontal minor + major lines
        final long startYIndex = (long) Math.floor(bounds.minY / steps.minorStepWorld);
        final long endYIndex = (long) Math.ceil(bounds.maxY / steps.minorStepWorld);

        for (long i = startYIndex; i <= endYIndex; i++) {
            final boolean isMajor = (Math.floorMod(i, (long) steps.majorEvery) == 0L);
            final double y = i * steps.minorStepWorld;

            backgroundGraphics.setStroke(isMajor ? style.getMajorGridColor() : style.getMinorGridColor());
            final double strokeWidth = isMajor ? style.getMajorGridStrokeWidthInPixels() : style.getMinorGridStrokeWidthInPixels();
            backgroundGraphics.setLineWidth(strokeWidth);

            final double sy = snapForCrispStroke(worldToScreenY(y, height), strokeWidth);
            backgroundGraphics.strokeLine(0, sy, width, sy);
        }
    }

    private void renderAxisLabels(final double width, final double height) {
        final GridSteps steps = computeGridSteps();
        if (!steps.isValid()) {
            return;
        }

        final ViewportBounds bounds = computeVisibleBounds(width, height);

        final boolean xAxisVisible = bounds.minY <= 0.0 && bounds.maxY >= 0.0;
        final boolean yAxisVisible = bounds.minX <= 0.0 && bounds.maxX >= 0.0;

        if (!xAxisVisible && !yAxisVisible) {
            return;
        }

        backgroundGraphics.setFont(style.getAxisLabelFont());
        backgroundGraphics.setFill(style.getAxisLabelColor());

        final double axisX = worldToScreenX(0.0, width);
        final double axisY = worldToScreenY(0.0, height);

        final double tickLength = viewConfiguration.getAxisTickLengthInPixels();
        final double labelOffset = viewConfiguration.getAxisLabelOffsetInPixels();

        // We label at "labelStepWorld", which is an integer multiple of majorStepWorld
        // => labels always sit on major grid corners/lines.
        final double labelStepWorld = steps.labelStepWorld;

        // ---------- X axis ----------
        if (xAxisVisible) {
            backgroundGraphics.setTextAlign(TextAlignment.CENTER);
            backgroundGraphics.setTextBaseline(VPos.TOP);

            final long startIndex = (long) Math.floor(bounds.minX / labelStepWorld);
            final long endIndex = (long) Math.ceil(bounds.maxX / labelStepWorld);

            for (long i = startIndex; i <= endIndex; i++) {
                final double x = i * labelStepWorld;
                final double sx = snapForCrispStroke(worldToScreenX(x, width), 1.5);

                if (sx < 0.0 || sx > width) {
                    continue;
                }

                // Tick mark on x-axis
                backgroundGraphics.setStroke(style.getAxisColor());
                backgroundGraphics.setLineWidth(1.5);

                final double y1 = snapForCrispStroke(axisY - tickLength / 2.0, 1.5);
                final double y2 = snapForCrispStroke(axisY + tickLength / 2.0, 1.5);
                backgroundGraphics.strokeLine(sx, y1, sx, y2);

                // Label directly under x-axis
                final String label = formatAxisNumber(x, style.getAxisLabelLocale());

                // Avoid double "0" at origin: keep only on x-axis
                if ("0".equals(label) || isNearZero(x)) {
                    // keep origin label on X-axis (fine)
                }

                double labelY = axisY + tickLength / 2.0 + labelOffset;
                labelY = clamp(labelY, 0.0, height - 2.0);

                backgroundGraphics.fillText(label, sx, labelY);
            }
        }

        // ---------- Y axis ----------
        if (yAxisVisible) {
            backgroundGraphics.setTextAlign(TextAlignment.LEFT);
            backgroundGraphics.setTextBaseline(VPos.CENTER);

            final long startIndex = (long) Math.floor(bounds.minY / labelStepWorld);
            final long endIndex = (long) Math.ceil(bounds.maxY / labelStepWorld);

            for (long i = startIndex; i <= endIndex; i++) {
                final double y = i * labelStepWorld;
                final double sy = snapForCrispStroke(worldToScreenY(y, height), 1.5);

                if (sy < 0.0 || sy > height) {
                    continue;
                }

                // Tick mark on y-axis
                backgroundGraphics.setStroke(style.getAxisColor());
                backgroundGraphics.setLineWidth(1.5);

                final double x1 = snapForCrispStroke(axisX - tickLength / 2.0, 1.5);
                final double x2 = snapForCrispStroke(axisX + tickLength / 2.0, 1.5);
                backgroundGraphics.strokeLine(x1, sy, x2, sy);

                // Label directly right of y-axis
                final String label = formatAxisNumber(y, style.getAxisLabelLocale());

                // Avoid double "0" at origin: do NOT show 0 on Y-axis
                if ("0".equals(label) || isNearZero(y)) {
                    continue;
                }

                double labelX = axisX + tickLength / 2.0 + labelOffset;
                labelX = clamp(labelX, 0.0, width - 2.0);

                backgroundGraphics.fillText(label, labelX, sy);
            }
        }
    }

    /**
     * Computes consistent minor/major/label steps for the current zoom level.
     *
     * <p>
     * Key rule for stable alignment:
     * <ul>
     *   <li>minorStepWorld defines the grid base (all grid corners).</li>
     *   <li>majorStepWorld is an integer multiple of minorStepWorld.</li>
     *   <li>labelStepWorld is an integer multiple of minorStepWorld (NOT majorStepWorld),
     *       so labels can be denser but still always land on grid corners.</li>
     * </ul>
     * </p>
     */
    private GridSteps computeGridSteps() {
        final double minorStepWorld = chooseNiceStep(
                viewConfiguration.getTargetMinorGridSpacingInPixels() / pixelsPerWorldUnit
        );

        if (!(minorStepWorld > 0.0) || !Double.isFinite(minorStepWorld)) {
            return GridSteps.invalid();
        }

        final int majorEvery = Math.max(1, viewConfiguration.getMinorLinesPerMajorLine());
        final double majorStepWorld = minorStepWorld * majorEvery;

        final double minorStepPixels = minorStepWorld * pixelsPerWorldUnit;

        // ↓ This value controls label density on screen.
        // If your labels still feel too far apart: lower this in your config (e.g. 30–40).
        final double minLabelPixels = Math.max(1.0, viewConfiguration.getMinimumAxisLabelSpacingInPixels());

        long labelEveryMinor = (long) Math.ceil(minLabelPixels / Math.max(1e-9, minorStepPixels));
        if (labelEveryMinor < 1L) {
            labelEveryMinor = 1L;
        }

        // label step is aligned to grid corners (minor grid intersection points)
        final double labelStepWorld = minorStepWorld * labelEveryMinor;

        return new GridSteps(minorStepWorld, majorEvery, majorStepWorld, labelEveryMinor, labelStepWorld);
    }

    private record GridSteps(
            double minorStepWorld,
            int majorEvery,
            double majorStepWorld,
            long labelEveryMinor,
            double labelStepWorld
    ) {
        static GridSteps invalid() {
            return new GridSteps(Double.NaN, 1, Double.NaN, 1L, Double.NaN);
        }

        boolean isValid() {
            return minorStepWorld > 0.0
                    && majorEvery >= 1
                    && majorStepWorld > 0.0
                    && labelEveryMinor >= 1L
                    && labelStepWorld > 0.0
                    && Double.isFinite(minorStepWorld)
                    && Double.isFinite(majorStepWorld)
                    && Double.isFinite(labelStepWorld);
        }
    }

    private boolean isNearZero(final double value) {
        return Math.abs(value) < EPSILON_FOR_ZERO;
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