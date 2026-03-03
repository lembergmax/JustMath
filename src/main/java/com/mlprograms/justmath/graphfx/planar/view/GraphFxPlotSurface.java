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

import static com.mlprograms.justmath.bignumber.BigNumbers.*;

/**
 * High-performance plot surface with pan/zoom, grid, axes and axis labeling.
 *
 * <p>
 * This class is an internal rendering component used by {@link GraphFxViewer}. It is not designed to be instantiated
 * directly by library users.
 * </p>
 *
 * <p><strong>Core responsibilities:</strong></p>
 * <ul>
 *     <li>Maintain the viewport (center + zoom) as world-space values.</li>
 *     <li>Render background (grid, axes, labels) to a cached canvas.</li>
 *     <li>Render plot data (lines and points) to a separate canvas.</li>
 *     <li>Provide stable, jitter-free pan/zoom interactions.</li>
 * </ul>
 *
 * <p><strong>Performance notes:</strong></p>
 * <ul>
 *     <li>Rendering is coalesced: multiple state changes during one JavaFX pulse cause only one redraw.</li>
 *     <li>Background and plot data are drawn on separate canvases to avoid redundant background work.</li>
 *     <li>Internal math uses {@link BigNumber} for viewport computations and label formatting; screen transforms use
 *         {@code double} for speed.</li>
 * </ul>
 */
final class GraphFxPlotSurface extends Region {

    /**
     * Epsilon used for treating values as zero in comparisons (to avoid "-0" labels).
     */
    private static final double EPSILON_FOR_ZERO = 1e-12;

    /**
     * Canvas used for rendering the static background layer (grid, axes, labels).
     */
    private final Canvas backgroundCanvas;

    /**
     * Canvas used for rendering the dynamic plot layer (lines, points).
     */
    private final Canvas plotCanvas;

    /**
     * Graphics context for the background layer.
     */
    private final GraphicsContext backgroundGraphicsContext;

    /**
     * Graphics context for the plot layer.
     */
    private final GraphicsContext plotGraphicsContext;

    /**
     * The plot data currently rendered by this surface.
     */
    private PlotResult plotResult;

    /**
     * Current style configuration (colors, fonts, stroke widths).
     */
    private GraphFxViewerStyle viewerStyle;

    /**
     * Current view configuration (grid spacing, interaction toggles).
     */
    private GraphFxViewConfiguration viewConfiguration;

    /**
     * X coordinate of the viewport center in world units.
     */
    private BigNumber centerWorldX;

    /**
     * Y coordinate of the viewport center in world units.
     */
    private BigNumber centerWorldY;

    /**
     * Zoom level (pixels per one world unit).
     */
    private BigNumber pixelsPerWorldUnit;

    /**
     * Last mouse position while panning (screen coordinates).
     */
    private Point2D lastPanMousePoint;

    /**
     * Flag to coalesce renders into a single JavaFX pulse.
     */
    private boolean renderScheduled;

    /**
     * Creates a plot surface with initial configuration.
     *
     * @param viewerStyle       viewer style configuration (must not be null)
     * @param viewConfiguration view configuration (must not be null)
     */
    GraphFxPlotSurface(
            final GraphFxViewerStyle viewerStyle,
            final GraphFxViewConfiguration viewConfiguration
    ) {
        this.viewerStyle = Objects.requireNonNull(viewerStyle, "viewerStyle must not be null");
        this.viewConfiguration = Objects.requireNonNull(viewConfiguration, "viewConfiguration must not be null");

        this.backgroundCanvas = new Canvas();
        this.plotCanvas = new Canvas();

        this.backgroundGraphicsContext = backgroundCanvas.getGraphicsContext2D();
        this.plotGraphicsContext = plotCanvas.getGraphicsContext2D();

        this.plotResult = new PlotResult();

        this.centerWorldX = ZERO;
        this.centerWorldY = ZERO;
        this.pixelsPerWorldUnit = new BigNumber("80", Locale.ROOT);

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
     * Applies a new visual style and schedules a redraw.
     *
     * @param viewerStyle new style (must not be null)
     */
    void setStyle(final GraphFxViewerStyle viewerStyle) {
        this.viewerStyle = Objects.requireNonNull(viewerStyle, "viewerStyle must not be null");
        requestRender();
    }

    /**
     * Applies a new view configuration and schedules a redraw.
     *
     * @param viewConfiguration new configuration (must not be null)
     */
    void setViewConfiguration(final GraphFxViewConfiguration viewConfiguration) {
        this.viewConfiguration = Objects.requireNonNull(viewConfiguration, "viewConfiguration must not be null");
        requestRender();
    }

    /**
     * Creates a snapshot of the currently visible world bounds.
     *
     * @return immutable viewport snapshot
     */
    ViewportSnapshot snapshotViewport() {
        final double canvasWidth = backgroundCanvas.getWidth();
        final double canvasHeight = backgroundCanvas.getHeight();

        if (!(canvasWidth > 0.0) || !(canvasHeight > 0.0) || !isPositive(pixelsPerWorldUnit)) {
            return new ViewportSnapshot(ZERO, ZERO, ZERO, ZERO);
        }

        final BigNumber halfWorldWidth = new BigNumber(Double.toString(canvasWidth), Locale.ROOT)
                .divide(TWO, DEFAULT_MATH_CONTEXT)
                .divide(pixelsPerWorldUnit, DEFAULT_MATH_CONTEXT);

        final BigNumber halfWorldHeight = new BigNumber(Double.toString(canvasHeight), Locale.ROOT)
                .divide(TWO, DEFAULT_MATH_CONTEXT)
                .divide(pixelsPerWorldUnit, DEFAULT_MATH_CONTEXT);

        final BigNumber minX = centerWorldX.subtract(halfWorldWidth);
        final BigNumber maxX = centerWorldX.add(halfWorldWidth);

        final BigNumber minY = centerWorldY.subtract(halfWorldHeight);
        final BigNumber maxY = centerWorldY.add(halfWorldHeight);

        return new ViewportSnapshot(minX, maxX, minY, maxY);
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

        final BigNumber minX = viewportSnapshot.minX();
        final BigNumber maxX = viewportSnapshot.maxX();
        final BigNumber minY = viewportSnapshot.minY();
        final BigNumber maxY = viewportSnapshot.maxY();

        if (!(maxX.compareTo(minX) > 0) || !(maxY.compareTo(minY) > 0)) {
            return;
        }

        final double canvasWidth = Math.max(1.0, backgroundCanvas.getWidth());
        final double canvasHeight = Math.max(1.0, backgroundCanvas.getHeight());

        final BigNumber worldWidth = maxX.subtract(minX);
        final BigNumber worldHeight = maxY.subtract(minY);

        final BigNumber pixelsPerWorldUnitX = new BigNumber(Double.toString(canvasWidth), Locale.ROOT)
                .divide(worldWidth, DEFAULT_MATH_CONTEXT);

        final BigNumber pixelsPerWorldUnitY = new BigNumber(Double.toString(canvasHeight), Locale.ROOT)
                .divide(worldHeight, DEFAULT_MATH_CONTEXT);

        this.centerWorldX = minX.add(maxX).divide(TWO, DEFAULT_MATH_CONTEXT);
        this.centerWorldY = minY.add(maxY).divide(TWO, DEFAULT_MATH_CONTEXT);

        final BigNumber unclamped = minBigNumber(pixelsPerWorldUnitX, pixelsPerWorldUnitY);
        this.pixelsPerWorldUnit = clampPixelsPerWorldUnit(unclamped);

        requestRender();
    }

    /**
     * Lays out the child canvases to fill this region.
     */
    @Override
    protected void layoutChildren() {
        final double width = getWidth();
        final double height = getHeight();

        backgroundCanvas.setWidth(width);
        backgroundCanvas.setHeight(height);

        plotCanvas.setWidth(width);
        plotCanvas.setHeight(height);

        backgroundCanvas.relocate(0, 0);
        plotCanvas.relocate(0, 0);

        requestRender();
    }

    /**
     * Installs mouse interactions for panning and zooming.
     */
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

            final Point2D currentMousePoint = new Point2D(event.getX(), event.getY());
            final Point2D deltaPixels = currentMousePoint.subtract(lastPanMousePoint);

            panByPixels(deltaPixels.getX(), deltaPixels.getY());

            lastPanMousePoint = currentMousePoint;
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

            final double width = backgroundCanvas.getWidth();
            final double height = backgroundCanvas.getHeight();
            if (!(width > 0.0) || !(height > 0.0)) {
                return;
            }

            zoomTowardsCursor(event.getX(), event.getY(), event.getDeltaY());
            requestRender();
            event.consume();
        });
    }

    /**
     * Pans the viewport by the given pixel delta.
     *
     * @param deltaPixelsX horizontal drag delta in pixels (positive means dragging right)
     * @param deltaPixelsY vertical drag delta in pixels (positive means dragging down)
     */
    private void panByPixels(final double deltaPixelsX, final double deltaPixelsY) {
        if (!isPositive(pixelsPerWorldUnit)) {
            return;
        }

        final BigNumber deltaWorldX = new BigNumber(Double.toString(deltaPixelsX), Locale.ROOT)
                .divide(pixelsPerWorldUnit, DEFAULT_MATH_CONTEXT);

        final BigNumber deltaWorldY = new BigNumber(Double.toString(deltaPixelsY), Locale.ROOT)
                .divide(pixelsPerWorldUnit, DEFAULT_MATH_CONTEXT);

        // Dragging right moves the "camera" left.
        centerWorldX = centerWorldX.subtract(deltaWorldX);

        // Dragging down moves the "camera" up.
        centerWorldY = centerWorldY.add(deltaWorldY);
    }

    /**
     * Zooms the viewport while keeping the world coordinate under the cursor stationary.
     *
     * @param cursorX     cursor x in screen coordinates (pixels)
     * @param cursorY     cursor y in screen coordinates (pixels)
     * @param wheelDeltaY mouse wheel delta (positive for zoom in, negative for zoom out)
     */
    private void zoomTowardsCursor(final double cursorX, final double cursorY, final double wheelDeltaY) {
        final double oldScaleDouble = bigNumberToDouble(pixelsPerWorldUnit);
        final double zoomFactor = Math.pow(viewConfiguration.getMouseWheelZoomExponent(), wheelDeltaY);
        final double newScaleDouble = clampDouble(
                oldScaleDouble * zoomFactor,
                viewConfiguration.getMinimumPixelsPerWorldUnit(),
                viewConfiguration.getMaximumPixelsPerWorldUnit()
        );

        if (Math.abs(newScaleDouble - oldScaleDouble) < 1e-12) {
            return;
        }

        final double width = backgroundCanvas.getWidth();
        final double height = backgroundCanvas.getHeight();

        // World coordinate under cursor before zoom.
        final double worldXBefore = screenToWorldX(cursorX, width, oldScaleDouble);
        final double worldYBefore = screenToWorldY(cursorY, height, oldScaleDouble);

        // Adjust center so that the same world coordinate stays under the cursor.
        final double centerWorldXAfter = worldXBefore - (cursorX - (width / 2.0)) / newScaleDouble;
        final double centerWorldYAfter = worldYBefore + (cursorY - (height / 2.0)) / newScaleDouble;

        this.centerWorldX = new BigNumber(Double.toString(centerWorldXAfter), Locale.ROOT);
        this.centerWorldY = new BigNumber(Double.toString(centerWorldYAfter), Locale.ROOT);
        this.pixelsPerWorldUnit = clampPixelsPerWorldUnit(new BigNumber(Double.toString(newScaleDouble), Locale.ROOT));
    }

    /**
     * Converts a screen x-coordinate into a world x-coordinate.
     *
     * @param screenX     screen x in pixels
     * @param canvasWidth width of the drawing surface in pixels
     * @param scale       pixels per world unit
     * @return world x coordinate
     */
    private double screenToWorldX(final double screenX, final double canvasWidth, final double scale) {
        return bigNumberToDouble(centerWorldX) + (screenX - (canvasWidth / 2.0)) / scale;
    }

    /**
     * Converts a screen y-coordinate into a world y-coordinate.
     *
     * @param screenY      screen y in pixels
     * @param canvasHeight height of the drawing surface in pixels
     * @param scale        pixels per world unit
     * @return world y coordinate
     */
    private double screenToWorldY(final double screenY, final double canvasHeight, final double scale) {
        return bigNumberToDouble(centerWorldY) - (screenY - (canvasHeight / 2.0)) / scale;
    }

    /**
     * Converts a world x-coordinate into a screen x-coordinate.
     *
     * @param worldX      world x coordinate
     * @param canvasWidth width of the drawing surface in pixels
     * @return screen x coordinate in pixels
     */
    private double worldToScreenX(final double worldX, final double canvasWidth) {
        return (canvasWidth / 2.0) + (worldX - bigNumberToDouble(centerWorldX)) * bigNumberToDouble(pixelsPerWorldUnit);
    }

    /**
     * Converts a world y-coordinate into a screen y-coordinate.
     *
     * @param worldY       world y coordinate
     * @param canvasHeight height of the drawing surface in pixels
     * @return screen y coordinate in pixels
     */
    private double worldToScreenY(final double worldY, final double canvasHeight) {
        return (canvasHeight / 2.0) - (worldY - bigNumberToDouble(centerWorldY)) * bigNumberToDouble(pixelsPerWorldUnit);
    }

    /**
     * Requests a render; multiple calls before the next JavaFX pulse are coalesced.
     */
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

    /**
     * Renders the current state immediately.
     */
    private void renderNow() {
        final double width = backgroundCanvas.getWidth();
        final double height = backgroundCanvas.getHeight();

        if (!(width > 0.0) || !(height > 0.0)) {
            return;
        }

        // Background (grid, axes, labels)
        clearBackground(width, height);

        if (viewConfiguration.isGridVisible()) {
            renderGrid(width, height);
        }

        if (viewConfiguration.isAxesVisible()) {
            renderAxes(width, height);
        }

        if (viewConfiguration.isAxesVisible() && viewConfiguration.isAxisLabelsVisible()) {
            renderAxisLabels(width, height);
        }

        // Plot layer
        plotGraphicsContext.clearRect(0, 0, width, height);
        renderPlot(width, height);
    }

    /**
     * Clears the background canvas using {@link GraphFxViewerStyle#getBackgroundColor()}.
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     */
    private void clearBackground(final double width, final double height) {
        backgroundGraphicsContext.setFill(viewerStyle.getBackgroundColor());
        backgroundGraphicsContext.fillRect(0, 0, width, height);
    }

    /**
     * Renders grid lines aligned to world-space multiples of the computed step size.
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     */
    private void renderGrid(final double width, final double height) {
        final GridSteps gridSteps = computeGridSteps();
        if (!gridSteps.isValid()) {
            return;
        }

        final ViewportBounds viewportBounds = computeVisibleBounds(width, height);

        // Vertical lines (world x = i * minorStepWorld)
        final long startXIndex = (long) Math.floor(viewportBounds.minX / gridSteps.minorStepWorld);
        final long endXIndex = (long) Math.ceil(viewportBounds.maxX / gridSteps.minorStepWorld);

        for (long index = startXIndex; index <= endXIndex; index++) {
            final boolean isMajor = Math.floorMod(index, (long) gridSteps.majorEvery) == 0L;
            final double worldX = index * gridSteps.minorStepWorld;

            final double strokeWidth = isMajor
                    ? viewerStyle.getMajorGridStrokeWidthInPixels()
                    : viewerStyle.getMinorGridStrokeWidthInPixels();

            backgroundGraphicsContext.setStroke(isMajor ? viewerStyle.getMajorGridColor() : viewerStyle.getMinorGridColor());
            backgroundGraphicsContext.setLineWidth(strokeWidth);

            final double screenX = snapForCrispStroke(worldToScreenX(worldX, width), strokeWidth);
            backgroundGraphicsContext.strokeLine(screenX, 0, screenX, height);
        }

        // Horizontal lines (world y = i * minorStepWorld)
        final long startYIndex = (long) Math.floor(viewportBounds.minY / gridSteps.minorStepWorld);
        final long endYIndex = (long) Math.ceil(viewportBounds.maxY / gridSteps.minorStepWorld);

        for (long index = startYIndex; index <= endYIndex; index++) {
            final boolean isMajor = Math.floorMod(index, (long) gridSteps.majorEvery) == 0L;
            final double worldY = index * gridSteps.minorStepWorld;

            final double strokeWidth = isMajor
                    ? viewerStyle.getMajorGridStrokeWidthInPixels()
                    : viewerStyle.getMinorGridStrokeWidthInPixels();

            backgroundGraphicsContext.setStroke(isMajor ? viewerStyle.getMajorGridColor() : viewerStyle.getMinorGridColor());
            backgroundGraphicsContext.setLineWidth(strokeWidth);

            final double screenY = snapForCrispStroke(worldToScreenY(worldY, height), strokeWidth);
            backgroundGraphicsContext.strokeLine(0, screenY, width, screenY);
        }
    }

    /**
     * Renders the x-axis (y=0) and y-axis (x=0) if they are visible within the current viewport.
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     */
    private void renderAxes(final double width, final double height) {
        final ViewportBounds viewportBounds = computeVisibleBounds(width, height);

        final boolean xAxisVisible = viewportBounds.minY <= 0.0 && viewportBounds.maxY >= 0.0;
        final boolean yAxisVisible = viewportBounds.minX <= 0.0 && viewportBounds.maxX >= 0.0;

        backgroundGraphicsContext.setStroke(viewerStyle.getAxisColor());
        backgroundGraphicsContext.setLineWidth(viewerStyle.getAxisStrokeWidthInPixels());

        if (xAxisVisible) {
            final double screenY = snapForCrispStroke(worldToScreenY(0.0, height), viewerStyle.getAxisStrokeWidthInPixels());
            backgroundGraphicsContext.strokeLine(0, screenY, width, screenY);
        }

        if (yAxisVisible) {
            final double screenX = snapForCrispStroke(worldToScreenX(0.0, width), viewerStyle.getAxisStrokeWidthInPixels());
            backgroundGraphicsContext.strokeLine(screenX, 0, screenX, height);
        }
    }

    /**
     * Renders axis labels placed on grid intersections.
     *
     * <p>
     * Labels are computed on a world-space step that is an integer multiple of the minor grid step.
     * This ensures labels always land on grid corners and never "float" between intersections.
     * </p>
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     */
    private void renderAxisLabels(final double width, final double height) {
        final GridSteps gridSteps = computeGridSteps();
        if (!gridSteps.isValid()) {
            return;
        }

        final ViewportBounds viewportBounds = computeVisibleBounds(width, height);

        final boolean xAxisVisible = viewportBounds.minY <= 0.0 && viewportBounds.maxY >= 0.0;
        final boolean yAxisVisible = viewportBounds.minX <= 0.0 && viewportBounds.maxX >= 0.0;

        if (!xAxisVisible && !yAxisVisible) {
            return;
        }

        backgroundGraphicsContext.setFont(viewerStyle.getAxisLabelFont());
        backgroundGraphicsContext.setFill(viewerStyle.getAxisLabelColor());

        final double axisScreenX = worldToScreenX(0.0, width);
        final double axisScreenY = worldToScreenY(0.0, height);

        final double tickLengthPixels = viewConfiguration.getAxisTickLengthInPixels();
        final double labelOffsetPixels = viewConfiguration.getAxisLabelOffsetInPixels();

        final double labelStepWorld = gridSteps.labelStepWorld;

        // ---- X axis labels ----
        if (xAxisVisible) {
            backgroundGraphicsContext.setTextAlign(TextAlignment.CENTER);
            backgroundGraphicsContext.setTextBaseline(VPos.TOP);

            final long startIndex = (long) Math.floor(viewportBounds.minX / labelStepWorld);
            final long endIndex = (long) Math.ceil(viewportBounds.maxX / labelStepWorld);

            for (long index = startIndex; index <= endIndex; index++) {
                final double worldX = index * labelStepWorld;
                final double screenX = snapForCrispStroke(worldToScreenX(worldX, width), 1.5);

                if (screenX < 0.0 || screenX > width) {
                    continue;
                }

                drawAxisTickOnXAxis(screenX, axisScreenY, tickLengthPixels);

                final String label = formatAxisNumber(worldX, viewerStyle.getAxisLabelLocale());
                final double labelY = clampDouble(axisScreenY + tickLengthPixels / 2.0 + labelOffsetPixels, 0.0, height - 2.0);
                backgroundGraphicsContext.fillText(label, screenX, labelY);
            }
        }

        // ---- Y axis labels ----
        if (yAxisVisible) {
            backgroundGraphicsContext.setTextAlign(TextAlignment.LEFT);
            backgroundGraphicsContext.setTextBaseline(VPos.CENTER);

            final long startIndex = (long) Math.floor(viewportBounds.minY / labelStepWorld);
            final long endIndex = (long) Math.ceil(viewportBounds.maxY / labelStepWorld);

            for (long index = startIndex; index <= endIndex; index++) {
                final double worldY = index * labelStepWorld;
                final double screenY = snapForCrispStroke(worldToScreenY(worldY, height), 1.5);

                if (screenY < 0.0 || screenY > height) {
                    continue;
                }

                // Avoid a double "0" label at the origin: keep origin label on X axis only.
                if (isNearZero(worldY)) {
                    continue;
                }

                drawAxisTickOnYAxis(axisScreenX, screenY, tickLengthPixels);

                final String label = formatAxisNumber(worldY, viewerStyle.getAxisLabelLocale());
                final double labelX = clampDouble(axisScreenX + tickLengthPixels / 2.0 + labelOffsetPixels, 0.0, width - 2.0);
                backgroundGraphicsContext.fillText(label, labelX, screenY);
            }
        }
    }

    /**
     * Draws a tick mark on the x-axis at the specified x coordinate.
     *
     * @param screenX          x position in pixels
     * @param axisScreenY      y position of the x-axis in pixels
     * @param tickLengthPixels tick length in pixels
     */
    private void drawAxisTickOnXAxis(final double screenX, final double axisScreenY, final double tickLengthPixels) {
        backgroundGraphicsContext.setStroke(viewerStyle.getAxisColor());
        backgroundGraphicsContext.setLineWidth(1.5);

        final double y1 = snapForCrispStroke(axisScreenY - tickLengthPixels / 2.0, 1.5);
        final double y2 = snapForCrispStroke(axisScreenY + tickLengthPixels / 2.0, 1.5);

        backgroundGraphicsContext.strokeLine(screenX, y1, screenX, y2);
    }

    /**
     * Draws a tick mark on the y-axis at the specified y coordinate.
     *
     * @param axisScreenX      x position of the y-axis in pixels
     * @param screenY          y position in pixels
     * @param tickLengthPixels tick length in pixels
     */
    private void drawAxisTickOnYAxis(final double axisScreenX, final double screenY, final double tickLengthPixels) {
        backgroundGraphicsContext.setStroke(viewerStyle.getAxisColor());
        backgroundGraphicsContext.setLineWidth(1.5);

        final double x1 = snapForCrispStroke(axisScreenX - tickLengthPixels / 2.0, 1.5);
        final double x2 = snapForCrispStroke(axisScreenX + tickLengthPixels / 2.0, 1.5);

        backgroundGraphicsContext.strokeLine(x1, screenY, x2, screenY);
    }

    /**
     * Renders plot lines and points from {@link #plotResult} to the plot canvas.
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     */
    private void renderPlot(final double width, final double height) {
        final List<PlotLine> plotLines = plotResult.plotLines();
        if (!plotLines.isEmpty()) {
            plotGraphicsContext.setStroke(viewerStyle.getPlotLineColor());
            plotGraphicsContext.setLineWidth(viewerStyle.getPlotLineStrokeWidthInPixels());

            for (final PlotLine plotLine : plotLines) {
                if (plotLine == null) {
                    continue;
                }

                final List<PlotPoint> plotPoints = plotLine.plotPoints();
                if (plotPoints == null || plotPoints.size() < 2) {
                    continue;
                }

                final double[] xPixels = new double[plotPoints.size()];
                final double[] yPixels = new double[plotPoints.size()];

                int count = 0;
                for (final PlotPoint plotPoint : plotPoints) {
                    if (plotPoint == null) {
                        continue;
                    }

                    final double worldX = bigNumberToDouble(plotPoint.x());
                    final double worldY = bigNumberToDouble(plotPoint.y());

                    if (!Double.isFinite(worldX) || !Double.isFinite(worldY)) {
                        continue;
                    }

                    xPixels[count] = worldToScreenX(worldX, width);
                    yPixels[count] = worldToScreenY(worldY, height);
                    count++;
                }

                if (count >= 2) {
                    plotGraphicsContext.strokePolyline(xPixels, yPixels, count);
                }
            }
        }

        final List<PlotPoint> plotPoints = plotResult.plotPoints();
        if (!plotPoints.isEmpty()) {
            plotGraphicsContext.setFill(viewerStyle.getPlotPointColor());

            final double radiusPixels = Math.max(0.5, viewerStyle.getPlotPointRadiusInPixels());
            final double diameterPixels = radiusPixels * 2.0;

            for (final PlotPoint plotPoint : plotPoints) {
                if (plotPoint == null) {
                    continue;
                }

                final double worldX = bigNumberToDouble(plotPoint.x());
                final double worldY = bigNumberToDouble(plotPoint.y());

                if (!Double.isFinite(worldX) || !Double.isFinite(worldY)) {
                    continue;
                }

                final double screenX = worldToScreenX(worldX, width);
                final double screenY = worldToScreenY(worldY, height);

                plotGraphicsContext.fillOval(
                        screenX - radiusPixels,
                        screenY - radiusPixels,
                        diameterPixels,
                        diameterPixels
                );
            }
        }
    }

    /**
     * Computes visible world bounds for the current viewport.
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     * @return visible bounds in world coordinates
     */
    private ViewportBounds computeVisibleBounds(final double width, final double height) {
        final double pixelsPerWorldUnitDouble = bigNumberToDouble(pixelsPerWorldUnit);

        final double halfWorldWidth = (width / 2.0) / pixelsPerWorldUnitDouble;
        final double halfWorldHeight = (height / 2.0) / pixelsPerWorldUnitDouble;

        final double centerX = bigNumberToDouble(centerWorldX);
        final double centerY = bigNumberToDouble(centerWorldY);

        return new ViewportBounds(
                centerX - halfWorldWidth,
                centerX + halfWorldWidth,
                centerY - halfWorldHeight,
                centerY + halfWorldHeight
        );
    }

    /**
     * Computes consistent minor/major/label steps for the current zoom level.
     *
     * <p>
     * Key rule for stable alignment:
     * </p>
     * <ul>
     *     <li>{@code minorStepWorld} defines the grid base (all intersections).</li>
     *     <li>{@code majorStepWorld} is an integer multiple of {@code minorStepWorld}.</li>
     *     <li>{@code labelStepWorld} is an integer multiple of {@code minorStepWorld},
     *         ensuring labels always land on grid corners.</li>
     * </ul>
     *
     * @return computed grid steps
     */
    private GridSteps computeGridSteps() {
        final double pixelsPerWorldUnitDouble = bigNumberToDouble(pixelsPerWorldUnit);

        final double minorStepWorld = chooseNiceStep(viewConfiguration.getTargetMinorGridSpacingInPixels() / pixelsPerWorldUnitDouble);
        if (!(minorStepWorld > 0.0) || !Double.isFinite(minorStepWorld)) {
            return GridSteps.invalid();
        }

        final int majorEvery = Math.max(1, viewConfiguration.getMinorLinesPerMajorLine());
        final double majorStepWorld = minorStepWorld * majorEvery;

        final double minorStepPixels = minorStepWorld * pixelsPerWorldUnitDouble;
        final double minimumLabelSpacingPixels = Math.max(1.0, viewConfiguration.getMinimumAxisLabelSpacingInPixels());

        long labelEveryMinor = (long) Math.ceil(minimumLabelSpacingPixels / Math.max(1e-9, minorStepPixels));
        if (labelEveryMinor < 1L) {
            labelEveryMinor = 1L;
        }

        final double labelStepWorld = minorStepWorld * labelEveryMinor;

        return new GridSteps(minorStepWorld, majorEvery, majorStepWorld, labelEveryMinor, labelStepWorld);
    }

    /**
     * Selects a "nice" step size for grid spacing.
     *
     * <p>
     * The returned value is of the form {@code 1, 2, 5} multiplied by a power of 10.
     * </p>
     *
     * @param rawStep raw step candidate in world units
     * @return normalized "nice" step in world units
     */
    private double chooseNiceStep(final double rawStep) {
        if (!(rawStep > 0.0) || Double.isNaN(rawStep) || Double.isInfinite(rawStep)) {
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
     * Snaps coordinates for crisp rendering of thin strokes.
     *
     * <p>
     * For 1px-ish lines, snapping to {@code n + 0.5} tends to produce the best results.
     * </p>
     *
     * @param coordinate  coordinate in pixels
     * @param strokeWidth stroke width in pixels
     * @return snapped coordinate
     */
    private double snapForCrispStroke(final double coordinate, final double strokeWidth) {
        if (strokeWidth <= 1.6) {
            return Math.floor(coordinate) + 0.5;
        }
        return coordinate;
    }

    /**
     * Formats an axis number in a locale-aware way.
     *
     * <p>
     * This uses {@link BigNumber} for formatting to align with JustMath's numeric representation.
     * The input is still a {@code double} because axis ticks are determined in double world units.
     * </p>
     *
     * @param value  world coordinate value
     * @param locale locale used to select the decimal separator
     * @return formatted axis label
     */
    private String formatAxisNumber(final double value, final Locale locale) {
        final double normalized = isNearZero(value) ? 0.0 : value;

        final BigNumber bigNumber = new BigNumber(Double.toString(normalized), Locale.ROOT);
        String raw = bigNumber.toString();

        final char decimalSeparator = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
        if (decimalSeparator != '.') {
            raw = raw.replace('.', decimalSeparator);
        }

        if (raw.equals("-0") || raw.equals("-0" + decimalSeparator + "0")) {
            return "0";
        }

        return raw;
    }

    /**
     * Checks whether a {@code double} value should be treated as zero.
     *
     * @param value value to check
     * @return {@code true} if the absolute value is smaller than {@link #EPSILON_FOR_ZERO}
     */
    private boolean isNearZero(final double value) {
        return Math.abs(value) < EPSILON_FOR_ZERO;
    }

    /**
     * Converts a {@link BigNumber} to a {@code double} for rendering computations.
     *
     * <p>
     * Rendering uses double for performance. The public API still uses {@link BigNumber}.
     * </p>
     *
     * @param value big number to convert (must not be null)
     * @return parsed double value
     */
    private double bigNumberToDouble(final BigNumber value) {
        Objects.requireNonNull(value, "value must not be null");
        return Double.parseDouble(value.toString());
    }

    /**
     * Clamps a primitive double value to a range.
     *
     * @param value candidate value
     * @param min   minimum value
     * @param max   maximum value
     * @return clamped value
     */
    private double clampDouble(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Returns {@code true} if the given {@link BigNumber} is strictly greater than zero.
     *
     * @param value big number to check (must not be null)
     * @return {@code true} if value > 0
     */
    private boolean isPositive(final BigNumber value) {
        Objects.requireNonNull(value, "value must not be null");
        return value.compareTo(ZERO) > 0;
    }

    /**
     * Clamps a pixels-per-world-unit value to the configured min/max values.
     *
     * @param pixelsPerWorldUnitCandidate candidate value
     * @return clamped value
     */
    private BigNumber clampPixelsPerWorldUnit(final BigNumber pixelsPerWorldUnitCandidate) {
        Objects.requireNonNull(pixelsPerWorldUnitCandidate, "pixelsPerWorldUnitCandidate must not be null");

        final BigNumber minimum = new BigNumber(Double.toString(viewConfiguration.getMinimumPixelsPerWorldUnit()), Locale.ROOT);
        final BigNumber maximum = new BigNumber(Double.toString(viewConfiguration.getMaximumPixelsPerWorldUnit()), Locale.ROOT);

        if (pixelsPerWorldUnitCandidate.compareTo(minimum) < 0) {
            return minimum;
        }
        if (pixelsPerWorldUnitCandidate.compareTo(maximum) > 0) {
            return maximum;
        }
        return pixelsPerWorldUnitCandidate;
    }

    /**
     * Returns the smaller of two {@link BigNumber} values.
     *
     * @param first  first value (must not be null)
     * @param second second value (must not be null)
     * @return the minimum value
     */
    private BigNumber minBigNumber(final BigNumber first, final BigNumber second) {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
        return first.compareTo(second) <= 0 ? first : second;
    }

    /**
     * Record holding computed grid step information for the current zoom.
     *
     * @param minorStepWorld  minor grid step in world units
     * @param majorEvery      number of minor lines per major line
     * @param majorStepWorld  major grid step in world units
     * @param labelEveryMinor number of minor steps between labels
     * @param labelStepWorld  label step in world units
     */
    private record GridSteps(
            /** Minor grid step in world units. */
            double minorStepWorld,
            /** Number of minor lines per major line. */
            int majorEvery,
            /** Major grid step in world units. */
            double majorStepWorld,
            /** Number of minor steps between labels. */
            long labelEveryMinor,
            /** Label step in world units (aligned to minor grid). */
            double labelStepWorld
    ) {

        /**
         * Creates an invalid grid steps instance.
         *
         * @return invalid steps
         */
        static GridSteps invalid() {
            return new GridSteps(Double.NaN, 1, Double.NaN, 1L, Double.NaN);
        }

        /**
         * Returns whether this steps object is valid for rendering.
         *
         * @return {@code true} if all step values are finite and strictly positive
         */
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

    /**
     * Simple immutable container for visible world bounds.
     *
     * @param minX minimum world x
     * @param maxX maximum world x
     * @param minY minimum world y
     * @param maxY maximum world y
     */
    private record ViewportBounds(
            /** Minimum visible world x. */
            double minX,
            /** Maximum visible world x. */
            double maxX,
            /** Minimum visible world y. */
            double minY,
            /** Maximum visible world y. */
            double maxY
    ) {
    }
}
