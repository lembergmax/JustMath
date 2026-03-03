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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.mlprograms.justmath.bignumber.BigNumbers.*;

/**
 * Dedicated JavaFX {@link Region} that renders a 2D cartesian plot with:
 * <ul>
 *     <li>world-to-screen coordinate mapping (viewport center + zoom)</li>
 *     <li>grid lines, axes and axis tick labels</li>
 *     <li>plot lines and plot points</li>
 *     <li>mouse interactions (pan + zoom)</li>
 * </ul>
 *
 * <p>
 * This class is a low-level rendering building block used by {@link GraphFxViewer}. Library users typically interact
 * with higher-level APIs (viewer + configuration), not with this surface directly.
 * </p>
 *
 * <p><strong>Precision model</strong></p>
 * <ul>
 *     <li>All world-space state is stored as {@link BigNumber} to match the precision goals of JustMath.</li>
 *     <li>JavaFX drawing APIs require {@code double}. Conversion happens only at the final rendering step.</li>
 * </ul>
 *
 * <p><strong>Performance model</strong></p>
 * <ul>
 *     <li>Rendering is coalesced via {@link Platform#runLater(Runnable)} to avoid redundant redraws per pulse.</li>
 *     <li>Background (grid/axes/labels) and plot are rendered to separate canvases.</li>
 * </ul>
 */
final class GraphFxPlotSurface extends Region {

    /**
     * Numeric epsilon used to decide whether a value is "close enough" to zero for display purposes.
     *
     * <p>
     * This avoids rendering "-0" or jittering labels around the origin caused by floating-point conversion when
     * a mathematically exact zero becomes a tiny non-zero double.
     * </p>
     */
    private static final double EPSILON_FOR_ZERO = 1e-12;

    /**
     * Default zoom level expressed as "pixels per one world unit".
     *
     * <p>
     * Example: {@code 80} means one unit in world space corresponds to 80 screen pixels.
     * </p>
     */
    private static final BigNumber DEFAULT_PIXELS_PER_WORLD_UNIT = new BigNumber("80", Locale.ROOT);

    /**
     * Canvas that holds all background visuals (solid background, grid, axes, labels).
     *
     * <p>
     * Keeping background on its own canvas makes redraw cheaper when only plot data changes.
     * </p>
     */
    private final Canvas backgroundCanvas;

    /**
     * Canvas that holds all plot visuals (lines and points).
     *
     * <p>
     * Keeping plot content on its own canvas allows background to remain untouched if only plot changes.
     * </p>
     */
    private final Canvas plotCanvas;

    /**
     * Graphics context used for drawing onto {@link #backgroundCanvas}.
     *
     * <p>
     * This is cached for performance and to avoid repeated {@code getGraphicsContext2D()} calls.
     * </p>
     */
    private final GraphicsContext backgroundGraphicsContext;

    /**
     * Graphics context used for drawing onto {@link #plotCanvas}.
     *
     * <p>
     * This is cached for performance and to avoid repeated {@code getGraphicsContext2D()} calls.
     * </p>
     */
    private final GraphicsContext plotGraphicsContext;

    /**
     * Current plot output (lines and points) that should be rendered on {@link #plotCanvas}.
     *
     * <p>
     * If no plot has been provided yet, this defaults to an empty result.
     * </p>
     */
    private PlotResult plotResult;

    /**
     * Visual style object (colors, stroke widths, fonts, locale for labels, etc.).
     *
     * <p>
     * This is typically controlled by {@link GraphFxViewer} and can be swapped at runtime.
     * </p>
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private GraphFxViewerStyle viewerStyle;

    /**
     * View configuration object (interaction toggles, spacing rules, clamping, etc.).
     *
     * <p>
     * This is typically controlled by {@link GraphFxViewer} and can be swapped at runtime.
     * </p>
     */
    @Getter
    private GraphFxViewConfiguration viewConfiguration;

    /**
     * X coordinate of the viewport center in world space.
     *
     * <p>
     * World space is a pure mathematical coordinate system (cartesian plane).
     * </p>
     */
    private BigNumber centerWorldX;

    /**
     * Y coordinate of the viewport center in world space.
     *
     * <p>
     * World space is a pure mathematical coordinate system (cartesian plane).
     * </p>
     */
    private BigNumber centerWorldY;

    /**
     * Zoom level expressed as "pixels per one world unit".
     *
     * <p>
     * Higher values mean zoomed in (more pixels per unit). Lower values mean zoomed out.
     * </p>
     */
    private BigNumber pixelsPerWorldUnit;

    /**
     * Last mouse position used for panning.
     *
     * <p>
     * While dragging the primary mouse button, we store the last cursor position and translate the view according
     * to pixel deltas.
     * </p>
     */
    private Point2D lastPanMousePoint;

    /**
     * Flag used for render coalescing.
     *
     * <p>
     * When true, a render has already been scheduled via {@link Platform#runLater(Runnable)} and subsequent requests
     * are ignored until the render executed.
     * </p>
     */
    private boolean renderScheduled;

    /**
     * Clamp helper for {@link #pixelsPerWorldUnit}, derived from {@link #viewConfiguration}.
     *
     * <p>
     * This keeps zoom within a sane range and prevents extreme values that can degrade interaction or rendering.
     * </p>
     */
    private PixelsPerWorldUnitClamp pixelsPerWorldUnitClamp;

    /**
     * Creates a new plot surface with the given style and view configuration.
     *
     * @param viewerStyle       style information (colors, stroke widths, fonts, label locale)
     * @param viewConfiguration view configuration (interaction toggles, spacing rules, clamping bounds)
     */
    GraphFxPlotSurface(
            @NonNull final GraphFxViewerStyle viewerStyle,
            @NonNull final GraphFxViewConfiguration viewConfiguration
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

        this.pixelsPerWorldUnit = DEFAULT_PIXELS_PER_WORLD_UNIT;
        this.pixelsPerWorldUnitClamp = PixelsPerWorldUnitClamp.from(viewConfiguration);

        getChildren().addAll(backgroundCanvas, plotCanvas);
        installInteractions();
    }

    /**
     * Replaces the currently rendered plot data.
     *
     * <p>
     * Calling this method schedules a redraw. The background is still redrawn as part of a full render pass.
     * </p>
     *
     * @param plotResult the new plot result to render (must not be {@code null})
     */
    void setPlotResult(@NonNull final PlotResult plotResult) {
        this.plotResult = Objects.requireNonNull(plotResult, "plotResult must not be null");
        requestRender();
    }

    /**
     * Clears the current plot by replacing the plot result with an empty instance.
     *
     * <p>
     * Calling this method schedules a redraw.
     * </p>
     */
    void clearPlot() {
        this.plotResult = new PlotResult();
        requestRender();
    }

    /**
     * Updates the viewer style and schedules a redraw.
     *
     * @param viewerStyle the new style instance (must not be {@code null})
     */
    void setStyle(@NonNull final GraphFxViewerStyle viewerStyle) {
        this.viewerStyle = Objects.requireNonNull(viewerStyle, "viewerStyle must not be null");
        requestRender();
    }

    /**
     * Updates the view configuration and schedules a redraw.
     *
     * <p>
     * This also recalculates zoom clamps and immediately clamps the current zoom level.
     * </p>
     *
     * @param viewConfiguration the new configuration instance (must not be {@code null})
     */
    void setViewConfiguration(@NonNull final GraphFxViewConfiguration viewConfiguration) {
        this.viewConfiguration = Objects.requireNonNull(viewConfiguration, "viewConfiguration must not be null");
        this.pixelsPerWorldUnitClamp = PixelsPerWorldUnitClamp.from(viewConfiguration);
        this.pixelsPerWorldUnit = pixelsPerWorldUnitClamp.clamp(pixelsPerWorldUnit);
        requestRender();
    }

    /**
     * Creates a snapshot of the current visible viewport bounds in world coordinates.
     *
     * <p>
     * The viewport is determined by:
     * </p>
     * <ul>
     *     <li>canvas pixel dimensions</li>
     *     <li>{@link #centerWorldX} and {@link #centerWorldY}</li>
     *     <li>{@link #pixelsPerWorldUnit}</li>
     * </ul>
     *
     * @return an immutable snapshot containing min/max bounds for x and y in world coordinates
     */
    ViewportSnapshot snapshotViewport() {
        final BigNumber canvasWidthPixels = new BigNumber(backgroundCanvas.getWidth());
        final BigNumber canvasHeightPixels = new BigNumber(backgroundCanvas.getHeight());

        if (canvasWidthPixels.isNegative()
                || canvasHeightPixels.isNegative()
                || pixelsPerWorldUnit.isNegative()) {
            return new ViewportSnapshot(ZERO, ZERO, ZERO, ZERO);
        }

        final BigNumber halfWorldWidth = canvasWidthPixels
                .divide(TWO, DEFAULT_MATH_CONTEXT)
                .divide(pixelsPerWorldUnit, DEFAULT_MATH_CONTEXT);

        final BigNumber halfWorldHeight = canvasHeightPixels
                .divide(TWO, DEFAULT_MATH_CONTEXT)
                .divide(pixelsPerWorldUnit, DEFAULT_MATH_CONTEXT);

        final BigNumber minX = centerWorldX.subtract(halfWorldWidth);
        final BigNumber maxX = centerWorldX.add(halfWorldWidth);

        final BigNumber minY = centerWorldY.subtract(halfWorldHeight);
        final BigNumber maxY = centerWorldY.add(halfWorldHeight);

        return new ViewportSnapshot(minX, maxX, minY, maxY);
    }

    /**
     * Adjusts the viewport so that the given world-space bounds become visible.
     *
     * <p>
     * This sets:
     * </p>
     * <ul>
     *     <li>viewport center to the midpoint of the bounds</li>
     *     <li>zoom level so the bounds fit inside the current canvas (clamped)</li>
     * </ul>
     *
     * @param viewportSnapshot desired world-space bounds to fit into the canvas (must not be {@code null})
     */
    void fitViewport(@NonNull final ViewportSnapshot viewportSnapshot) {
        Objects.requireNonNull(viewportSnapshot, "viewportSnapshot must not be null");

        final BigNumber minX = viewportSnapshot.minX();
        final BigNumber maxX = viewportSnapshot.maxX();
        final BigNumber minY = viewportSnapshot.minY();
        final BigNumber maxY = viewportSnapshot.maxY();

        if (!(maxX.compareTo(minX) > 0) || !(maxY.compareTo(minY) > 0)) {
            return;
        }

        final BigNumber canvasWidthPixels = new BigNumber(Math.max(1.0, backgroundCanvas.getWidth()));
        final BigNumber canvasHeightPixels = new BigNumber(Math.max(1.0, backgroundCanvas.getHeight()));

        final BigNumber worldWidth = maxX.subtract(minX);
        final BigNumber worldHeight = maxY.subtract(minY);

        final BigNumber pixelsPerWorldUnitX = canvasWidthPixels.divide(worldWidth, DEFAULT_MATH_CONTEXT);
        final BigNumber pixelsPerWorldUnitY = canvasHeightPixels.divide(worldHeight, DEFAULT_MATH_CONTEXT);

        this.centerWorldX = minX.add(maxX).divide(TWO, DEFAULT_MATH_CONTEXT);
        this.centerWorldY = minY.add(maxY).divide(TWO, DEFAULT_MATH_CONTEXT);

        final BigNumber unclamped = pixelsPerWorldUnitX.min(pixelsPerWorldUnitY);
        this.pixelsPerWorldUnit = pixelsPerWorldUnitClamp.clamp(unclamped);

        requestRender();
    }

    /**
     * JavaFX layout hook.
     *
     * <p>
     * Resizes and relocates both canvases to match this region's width and height, then schedules a redraw.
     * </p>
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
     * Installs mouse interactions on this region based on {@link #viewConfiguration}.
     *
     * <p>
     * Supported interactions:
     * </p>
     * <ul>
     *     <li>Pan: left mouse drag</li>
     *     <li>Zoom: mouse wheel (zoom towards cursor)</li>
     * </ul>
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
     * Translates the viewport center by the given pixel delta.
     *
     * <p>
     * Pixel deltas are converted into world deltas using {@link #pixelsPerWorldUnit}.
     * </p>
     *
     * @param deltaPixelsX delta in pixels along the X axis (positive means mouse moved right)
     * @param deltaPixelsY delta in pixels along the Y axis (positive means mouse moved down)
     */
    private void panByPixels(final double deltaPixelsX, final double deltaPixelsY) {
        if (pixelsPerWorldUnit.isNegative()) {
            return;
        }

        final BigNumber deltaWorldX = new BigNumber(deltaPixelsX)
                .divide(pixelsPerWorldUnit, DEFAULT_MATH_CONTEXT);

        final BigNumber deltaWorldY = new BigNumber(deltaPixelsY)
                .divide(pixelsPerWorldUnit, DEFAULT_MATH_CONTEXT);

        // Dragging right moves the camera left.
        centerWorldX = centerWorldX.subtract(deltaWorldX);

        // Dragging down moves the camera up (screen Y increases downward).
        centerWorldY = centerWorldY.add(deltaWorldY);
    }

    /**
     * Zooms the view towards the given cursor position.
     *
     * <p>
     * The method keeps the world-space coordinate currently under the cursor fixed after zooming:
     * </p>
     * <ul>
     *     <li>compute world coordinate at cursor before zoom</li>
     *     <li>apply new scale (clamped)</li>
     *     <li>solve new center so the same world coordinate remains under the cursor</li>
     * </ul>
     *
     * @param cursorX     cursor X position in canvas/region coordinates (pixels)
     * @param cursorY     cursor Y position in canvas/region coordinates (pixels)
     * @param wheelDeltaY scroll delta from JavaFX (positive/negative depends on wheel direction)
     */
    private void zoomTowardsCursor(final double cursorX, final double cursorY, final double wheelDeltaY) {
        if (pixelsPerWorldUnit.isNegative()) {
            return;
        }

        final double zoomFactor = Math.pow(viewConfiguration.getMouseWheelZoomExponent(), wheelDeltaY);

        final BigNumber oldScale = pixelsPerWorldUnit;
        final BigNumber newScaleCandidate = oldScale.multiply(new BigNumber(zoomFactor));
        final BigNumber newScale = pixelsPerWorldUnitClamp.clamp(newScaleCandidate);

        final double oldScaleDouble = oldScale.doubleValue();
        final double newScaleDouble = newScale.doubleValue();

        if (Math.abs(newScaleDouble - oldScaleDouble) < 1e-12) {
            return;
        }

        final BigNumber widthPixels = new BigNumber(backgroundCanvas.getWidth());
        final BigNumber heightPixels = new BigNumber(backgroundCanvas.getHeight());

        final BigNumber cursorXPixels = new BigNumber(cursorX);
        final BigNumber cursorYPixels = new BigNumber(cursorY);

        final BigNumber halfWidthPixels = widthPixels.divide(TWO, DEFAULT_MATH_CONTEXT);
        final BigNumber halfHeightPixels = heightPixels.divide(TWO, DEFAULT_MATH_CONTEXT);

        // worldBefore = center + (cursor - halfCanvas) / scale   (Y inverted below)
        final BigNumber worldXBefore = centerWorldX.add(
                cursorXPixels.subtract(halfWidthPixels).divide(oldScale, DEFAULT_MATH_CONTEXT)
        );

        final BigNumber worldYBefore = centerWorldY.subtract(
                cursorYPixels.subtract(halfHeightPixels).divide(oldScale, DEFAULT_MATH_CONTEXT)
        );

        // centerAfter such that worldBefore stays under cursor.
        final BigNumber centerXAfter = worldXBefore.subtract(
                cursorXPixels.subtract(halfWidthPixels).divide(newScale, DEFAULT_MATH_CONTEXT)
        );

        // Screen Y grows downwards; world Y grows upwards -> sign differs from X.
        final BigNumber centerYAfter = worldYBefore.add(
                cursorYPixels.subtract(halfHeightPixels).divide(newScale, DEFAULT_MATH_CONTEXT)
        );

        this.centerWorldX = centerXAfter;
        this.centerWorldY = centerYAfter;
        this.pixelsPerWorldUnit = newScale;
    }

    /**
     * Schedules a render pass on the next JavaFX pulse.
     *
     * <p>
     * Multiple calls before the scheduled render executes will be coalesced into a single render pass.
     * </p>
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
     * Performs an immediate render pass.
     *
     * <p>
     * This method draws:
     * </p>
     * <ol>
     *     <li>background color</li>
     *     <li>grid (optional)</li>
     *     <li>axes (optional)</li>
     *     <li>axis labels (optional)</li>
     *     <li>plot lines + points</li>
     * </ol>
     */
    private void renderNow() {
        final double width = backgroundCanvas.getWidth();
        final double height = backgroundCanvas.getHeight();

        if (!(width > 0.0) || !(height > 0.0)) {
            return;
        }

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

        plotGraphicsContext.clearRect(0, 0, width, height);
        renderPlot(width, height);
    }

    /**
     * Clears and redraws the background fill.
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     */
    private void clearBackground(final double width, final double height) {
        backgroundGraphicsContext.setFill(viewerStyle.getBackgroundColor());
        backgroundGraphicsContext.fillRect(0, 0, width, height);
    }

    /**
     * Draws the grid lines according to the current zoom and configuration.
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

        final double minorStepWorld = gridSteps.minorStepWorldDouble();
        final long majorEvery = gridSteps.majorEvery();

        // Vertical lines: world x = i * minorStepWorld
        final long startXIndex = (long) Math.floor(viewportBounds.minX() / minorStepWorld);
        final long endXIndex = (long) Math.ceil(viewportBounds.maxX() / minorStepWorld);

        for (long index = startXIndex; index <= endXIndex; index++) {
            final boolean isMajor = Math.floorMod(index, majorEvery) == 0L;
            final BigNumber worldX = gridSteps.minorStepWorld().multiply(new BigNumber(index));

            final double strokeWidth = isMajor
                    ? viewerStyle.getMajorGridStrokeWidthInPixels()
                    : viewerStyle.getMinorGridStrokeWidthInPixels();

            backgroundGraphicsContext.setStroke(isMajor ? viewerStyle.getMajorGridColor() : viewerStyle.getMinorGridColor());
            backgroundGraphicsContext.setLineWidth(strokeWidth);

            final double screenX = snapForCrispStroke(worldToScreenX(worldX, width), strokeWidth);
            backgroundGraphicsContext.strokeLine(screenX, 0, screenX, height);
        }

        // Horizontal lines: world y = i * minorStepWorld
        final long startYIndex = (long) Math.floor(viewportBounds.minY() / minorStepWorld);
        final long endYIndex = (long) Math.ceil(viewportBounds.maxY() / minorStepWorld);

        for (long index = startYIndex; index <= endYIndex; index++) {
            final boolean isMajor = Math.floorMod(index, majorEvery) == 0L;
            final BigNumber worldY = gridSteps.minorStepWorld().multiply(new BigNumber(index));

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
     * Draws the X and Y axes if they are visible within the current viewport.
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     */
    private void renderAxes(final double width, final double height) {
        final ViewportBounds viewportBounds = computeVisibleBounds(width, height);

        final boolean xAxisVisible = viewportBounds.minY() <= 0.0 && viewportBounds.maxY() >= 0.0;
        final boolean yAxisVisible = viewportBounds.minX() <= 0.0 && viewportBounds.maxX() >= 0.0;

        backgroundGraphicsContext.setStroke(viewerStyle.getAxisColor());
        backgroundGraphicsContext.setLineWidth(viewerStyle.getAxisStrokeWidthInPixels());

        if (xAxisVisible) {
            final double screenY = snapForCrispStroke(worldToScreenY(ZERO, height), viewerStyle.getAxisStrokeWidthInPixels());
            backgroundGraphicsContext.strokeLine(0, screenY, width, screenY);
        }

        if (yAxisVisible) {
            final double screenX = snapForCrispStroke(worldToScreenX(ZERO, width), viewerStyle.getAxisStrokeWidthInPixels());
            backgroundGraphicsContext.strokeLine(screenX, 0, screenX, height);
        }
    }

    /**
     * Draws axis ticks and numeric labels along the axes.
     *
     * <p>
     * Label positions depend on computed grid steps and {@link GraphFxViewConfiguration} spacing rules.
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

        final boolean xAxisVisible = viewportBounds.minY() <= 0.0 && viewportBounds.maxY() >= 0.0;
        final boolean yAxisVisible = viewportBounds.minX() <= 0.0 && viewportBounds.maxX() >= 0.0;

        if (!xAxisVisible && !yAxisVisible) {
            return;
        }

        backgroundGraphicsContext.setFont(viewerStyle.getAxisLabelFont());
        backgroundGraphicsContext.setFill(viewerStyle.getAxisLabelColor());

        final double axisScreenX = worldToScreenX(ZERO, width);
        final double axisScreenY = worldToScreenY(ZERO, height);

        final double tickLengthPixels = viewConfiguration.getAxisTickLengthInPixels();
        final double labelOffsetPixels = viewConfiguration.getAxisLabelOffsetInPixels();

        final double labelStepWorldDouble = gridSteps.labelStepWorldDouble();

        if (xAxisVisible) {
            backgroundGraphicsContext.setTextAlign(TextAlignment.CENTER);
            backgroundGraphicsContext.setTextBaseline(VPos.TOP);

            final long startIndex = (long) Math.floor(viewportBounds.minX() / labelStepWorldDouble);
            final long endIndex = (long) Math.ceil(viewportBounds.maxX() / labelStepWorldDouble);

            for (long index = startIndex; index <= endIndex; index++) {
                final BigNumber worldX = gridSteps.labelStepWorld().multiply(new BigNumber(index));
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

        if (yAxisVisible) {
            backgroundGraphicsContext.setTextAlign(TextAlignment.LEFT);
            backgroundGraphicsContext.setTextBaseline(VPos.CENTER);

            final long startIndex = (long) Math.floor(viewportBounds.minY() / labelStepWorldDouble);
            final long endIndex = (long) Math.ceil(viewportBounds.maxY() / labelStepWorldDouble);

            for (long index = startIndex; index <= endIndex; index++) {
                final BigNumber worldY = gridSteps.labelStepWorld().multiply(new BigNumber(index));
                final double screenY = snapForCrispStroke(worldToScreenY(worldY, height), 1.5);

                if (screenY < 0.0 || screenY > height) {
                    continue;
                }

                // Avoid double "0" at origin: keep origin label on X axis only.
                if (isNearZero(worldY.doubleValue())) {
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
     * Draws a tick mark on the X axis.
     *
     * @param screenX          tick position in screen coordinates (pixels)
     * @param axisScreenY      Y coordinate of the X axis in screen coordinates (pixels)
     * @param tickLengthPixels total tick length in pixels
     */
    private void drawAxisTickOnXAxis(final double screenX, final double axisScreenY, final double tickLengthPixels) {
        backgroundGraphicsContext.setStroke(viewerStyle.getAxisColor());
        backgroundGraphicsContext.setLineWidth(1.5);

        final double y1 = snapForCrispStroke(axisScreenY - tickLengthPixels / 2.0, 1.5);
        final double y2 = snapForCrispStroke(axisScreenY + tickLengthPixels / 2.0, 1.5);

        backgroundGraphicsContext.strokeLine(screenX, y1, screenX, y2);
    }

    /**
     * Draws a tick mark on the Y axis.
     *
     * @param axisScreenX      X coordinate of the Y axis in screen coordinates (pixels)
     * @param screenY          tick position in screen coordinates (pixels)
     * @param tickLengthPixels total tick length in pixels
     */
    private void drawAxisTickOnYAxis(final double axisScreenX, final double screenY, final double tickLengthPixels) {
        backgroundGraphicsContext.setStroke(viewerStyle.getAxisColor());
        backgroundGraphicsContext.setLineWidth(1.5);

        final double x1 = snapForCrispStroke(axisScreenX - tickLengthPixels / 2.0, 1.5);
        final double x2 = snapForCrispStroke(axisScreenX + tickLengthPixels / 2.0, 1.5);

        backgroundGraphicsContext.strokeLine(x1, screenY, x2, screenY);
    }

    /**
     * Renders the plot lines and points onto {@link #plotCanvas}.
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

                    final double worldX = plotPoint.x().doubleValue();
                    final double worldY = plotPoint.y().doubleValue();

                    if (!Double.isFinite(worldX) || !Double.isFinite(worldY)) {
                        continue;
                    }

                    xPixels[count] = worldToScreenX(plotPoint.x(), width);
                    yPixels[count] = worldToScreenY(plotPoint.y(), height);
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

                final double worldX = plotPoint.x().doubleValue();
                final double worldY = plotPoint.y().doubleValue();

                if (!Double.isFinite(worldX) || !Double.isFinite(worldY)) {
                    continue;
                }

                final double screenX = worldToScreenX(plotPoint.x(), width);
                final double screenY = worldToScreenY(plotPoint.y(), height);

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
     * Computes the currently visible viewport bounds in world coordinates.
     *
     * <p>
     * This method operates in {@code double} because it is used for rendering decisions (visibility, line loops)
     * rather than high-precision math.
     * </p>
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     * @return bounds object containing min/max in world space (as doubles)
     */
    private ViewportBounds computeVisibleBounds(final double width, final double height) {
        final double pixelsPerWorldUnitDouble = pixelsPerWorldUnit.doubleValue();

        final double halfWorldWidth = (width / 2.0) / pixelsPerWorldUnitDouble;
        final double halfWorldHeight = (height / 2.0) / pixelsPerWorldUnitDouble;

        final double centerX = centerWorldX.doubleValue();
        final double centerY = centerWorldY.doubleValue();

        return new ViewportBounds(
                centerX - halfWorldWidth,
                centerX + halfWorldWidth,
                centerY - halfWorldHeight,
                centerY + halfWorldHeight
        );
    }

    /**
     * Computes the step sizes for:
     * <ul>
     *     <li>minor grid lines</li>
     *     <li>major grid lines</li>
     *     <li>axis labels</li>
     * </ul>
     *
     * <p>
     * A "nice number" heuristic is used (1/2/5 * 10^n), currently implemented using {@code double} arithmetic
     * because BigNumber does not provide logarithms/powers out of the box.
     * </p>
     *
     * @return computed grid steps; if invalid, {@link GridSteps#invalid()} is returned
     */
    private GridSteps computeGridSteps() {
        final double pixelsPerWorldUnitDouble = pixelsPerWorldUnit.doubleValue();
        if (!(pixelsPerWorldUnitDouble > 0.0) || !Double.isFinite(pixelsPerWorldUnitDouble)) {
            return GridSteps.invalid();
        }

        final double rawMinorStepWorld = viewConfiguration.getTargetMinorGridSpacingInPixels() / pixelsPerWorldUnitDouble;
        final double minorStepWorldDouble = chooseNiceStep(rawMinorStepWorld);

        if (!(minorStepWorldDouble > 0.0) || !Double.isFinite(minorStepWorldDouble)) {
            return GridSteps.invalid();
        }

        final long majorEvery = Math.max(1, viewConfiguration.getMinorLinesPerMajorLine());
        final double minorStepPixels = minorStepWorldDouble * pixelsPerWorldUnitDouble;
        final double minimumLabelSpacingPixels = Math.max(1.0, viewConfiguration.getMinimumAxisLabelSpacingInPixels());

        long labelEveryMinor = (long) Math.ceil(minimumLabelSpacingPixels / Math.max(1e-9, minorStepPixels));
        if (labelEveryMinor < 1L) {
            labelEveryMinor = 1L;
        }

        final double labelStepWorldDouble = minorStepWorldDouble * labelEveryMinor;

        return new GridSteps(
                new BigNumber(minorStepWorldDouble),
                minorStepWorldDouble,
                majorEvery,
                labelEveryMinor,
                new BigNumber(labelStepWorldDouble),
                labelStepWorldDouble
        );
    }

    /**
     * Picks a "nice" step size near the given raw step.
     *
     * <p>
     * The returned value is one of {@code 1, 2, 5, 10} times a power of ten.
     * </p>
     *
     * @param rawStep raw step size in world units (must be positive)
     * @return a "nice" step size; returns {@code 1.0} as a safe fallback for invalid input
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
     * Converts a world X coordinate into a screen X coordinate.
     *
     * <p>
     * Screen space origin is at the top-left corner of the canvas.
     * </p>
     *
     * @param worldX      world-space x coordinate (must not be {@code null})
     * @param canvasWidth canvas width in pixels
     * @return x coordinate in pixels relative to canvas origin (left)
     */
    private double worldToScreenX(@NonNull final BigNumber worldX, final double canvasWidth) {
        final BigNumber halfWidthPixels = new BigNumber(canvasWidth).divide(TWO, DEFAULT_MATH_CONTEXT);

        final BigNumber pixelOffset = worldX
                .subtract(centerWorldX)
                .multiply(pixelsPerWorldUnit);

        return halfWidthPixels.add(pixelOffset).doubleValue();
    }

    /**
     * Converts a world Y coordinate into a screen Y coordinate.
     *
     * <p>
     * Screen Y increases downwards, while world Y increases upwards.
     * </p>
     *
     * @param worldY       world-space y coordinate (must not be {@code null})
     * @param canvasHeight canvas height in pixels
     * @return y coordinate in pixels relative to canvas origin (top)
     */
    private double worldToScreenY(@NonNull final BigNumber worldY, final double canvasHeight) {
        final BigNumber halfHeightPixels = new BigNumber(canvasHeight).divide(TWO, DEFAULT_MATH_CONTEXT);

        final BigNumber pixelOffset = worldY
                .subtract(centerWorldY)
                .multiply(pixelsPerWorldUnit);

        return halfHeightPixels.subtract(pixelOffset).doubleValue();
    }

    /**
     * Snaps a coordinate to half-pixels for crisp 1px-ish strokes.
     *
     * <p>
     * For line widths close to 1, drawing at {@code n + 0.5} avoids blurry anti-aliased lines.
     * For thicker lines, snapping is not applied because blur is less visible and snapping could shift visuals.
     * </p>
     *
     * @param coordinate  coordinate to snap (pixels)
     * @param strokeWidth current stroke width (pixels)
     * @return snapped coordinate (pixels)
     */
    private double snapForCrispStroke(final double coordinate, final double strokeWidth) {
        if (strokeWidth <= 1.6) {
            return Math.floor(coordinate) + 0.5;
        }
        return coordinate;
    }

    /**
     * Formats a world-space number for axis labels, using the given locale's decimal separator.
     *
     * <p>
     * This method currently converts the {@link BigNumber} to {@code double} and re-wraps it into a {@link BigNumber}
     * purely for consistent formatting via {@link BigNumber#toString()}.
     * </p>
     *
     * @param value  value to format (must not be {@code null})
     * @param locale locale used to select the decimal separator (must not be {@code null})
     * @return formatted number string suitable for axis labels
     */
    private String formatAxisNumber(@NonNull final BigNumber value, @NonNull final Locale locale) {
        final double valueDouble = value.doubleValue();
        final double normalized = isNearZero(valueDouble) ? 0.0 : valueDouble;

        String raw = new BigNumber(Double.toString(normalized), Locale.ROOT).toString();

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
     * Checks whether the provided {@code double} is close enough to zero to be treated as zero.
     *
     * @param value input value
     * @return {@code true} if {@code |value| < EPSILON_FOR_ZERO}, otherwise {@code false}
     */
    private boolean isNearZero(final double value) {
        return Math.abs(value) < EPSILON_FOR_ZERO;
    }

    /**
     * Clamps the given value into the inclusive range {@code [min, max]}.
     *
     * @param value the value to clamp
     * @param min   inclusive lower bound
     * @param max   inclusive upper bound
     * @return clamped value
     */
    private double clampDouble(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Immutable container that groups the computed step sizes used for grid rendering and label placement.
     *
     * @param minorStepWorld       minor grid spacing in world units as {@link BigNumber} (precision-friendly representation)
     * @param minorStepWorldDouble minor grid spacing in world units as {@code double} (rendering loops and indexing)
     * @param majorEvery           every N-th minor line is treated as a major line (must be &gt;= 1)
     * @param labelEveryMinor      labels are drawn every N-th minor step (must be &gt;= 1)
     * @param labelStepWorld       label spacing in world units as {@link BigNumber}
     * @param labelStepWorldDouble label spacing in world units as {@code double}
     */
    private record GridSteps(
            BigNumber minorStepWorld,
            double minorStepWorldDouble,
            long majorEvery,
            long labelEveryMinor,
            BigNumber labelStepWorld,
            double labelStepWorldDouble
    ) {

        /**
         * Factory for an invalid marker instance.
         *
         * <p>
         * Used when zoom or configuration yields unusable values.
         * </p>
         *
         * @return invalid grid step container
         */
        static GridSteps invalid() {
            return new GridSteps(
                    new BigNumber(Double.NaN),
                    Double.NaN,
                    1L,
                    1L,
                    new BigNumber(Double.NaN),
                    Double.NaN
            );
        }

        /**
         * Validity check for computed steps.
         *
         * @return {@code true} if all required values are finite and positive, otherwise {@code false}
         */
        boolean isValid() {
            return minorStepWorldDouble > 0.0
                    && majorEvery >= 1L
                    && labelEveryMinor >= 1L
                    && labelStepWorldDouble > 0.0
                    && Double.isFinite(minorStepWorldDouble)
                    && Double.isFinite(labelStepWorldDouble);
        }
    }

    /**
     * Visible viewport bounds in world coordinates (rendering-friendly doubles).
     *
     * @param minX minimum X visible in world space
     * @param maxX maximum X visible in world space
     * @param minY minimum Y visible in world space
     * @param maxY maximum Y visible in world space
     */
    private record ViewportBounds(
            double minX,
            double maxX,
            double minY,
            double maxY
    ) {
    }

    /**
     * Clamp helper for {@link #pixelsPerWorldUnit}, derived from {@link GraphFxViewConfiguration}.
     *
     * <p>
     * This ensures zoom stays within the configured range to:
     * </p>
     * <ul>
     *     <li>avoid extreme values that break label/grid spacing</li>
     *     <li>avoid costly renders when zoom is too high (many pixels per unit)</li>
     *     <li>avoid numerical issues when zoom is too low (almost zero pixels per unit)</li>
     * </ul>
     *
     * @param minimum minimum allowed pixels-per-world-unit (inclusive)
     * @param maximum maximum allowed pixels-per-world-unit (inclusive)
     */
    private record PixelsPerWorldUnitClamp(BigNumber minimum, BigNumber maximum) {

        /**
         * Creates a clamp instance based on the given configuration.
         *
         * @param viewConfiguration configuration source (must not be {@code null})
         * @return clamp instance derived from config min/max
         */
        static PixelsPerWorldUnitClamp from(@NonNull final GraphFxViewConfiguration viewConfiguration) {
            return new PixelsPerWorldUnitClamp(
                    new BigNumber(viewConfiguration.getMinimumPixelsPerWorldUnit()),
                    new BigNumber(viewConfiguration.getMaximumPixelsPerWorldUnit())
            );
        }

        /**
         * Clamps the given candidate zoom level into the configured range.
         *
         * @param candidate candidate pixels-per-world-unit value (must not be {@code null})
         * @return clamped value (never {@code null})
         */
        BigNumber clamp(@NonNull final BigNumber candidate) {
            if (candidate.compareTo(minimum) < 0) {
                return minimum;
            }
            if (candidate.compareTo(maximum) > 0) {
                return maximum;
            }
            return candidate;
        }
    }

}