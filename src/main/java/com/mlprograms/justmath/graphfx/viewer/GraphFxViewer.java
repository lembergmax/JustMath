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

package com.mlprograms.justmath.graphfx.viewer;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphfx.element.GraphFxLine;
import com.mlprograms.justmath.graphfx.element.GraphFxPoint;
import com.mlprograms.justmath.graphfx.element.GraphFxPolyline;
import com.mlprograms.justmath.graphfx.runtime.JavaFxRuntime;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;

import java.math.MathContext;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A high-performance, library-friendly JavaFX 2D plotting surface for rendering already computed points.
 *
 * <h2>Design goals</h2>
 * <ul>
 *     <li><b>Library usability:</b> instantiate and call {@link #show()} without extending {@code Application}.</li>
 *     <li><b>Strict responsibilities:</b> no math / sampling / function evaluation. This viewer only draws.</li>
 *     <li><b>Professional UI:</b> labeled grid lines, stable layout while panning/zooming, crisp strokes.</li>
 *     <li><b>Performance:</b> grid is rendered on a dedicated canvas layer; elements are rendered separately.</li>
 * </ul>
 *
 * <h2>Coordinate model</h2>
 * <p>
 * A {@link GraphFxViewport} defines the visible world rectangle. All elements are provided in world coordinates
 * ({@link BigNumber}). Rendering maps those coordinates into pixels via an internal transform.
 * </p>
 *
 * <h2>Important rendering guarantee</h2>
 * <p>
 * The viewer keeps the plot rectangle <b>layout-stable</b> while panning/zooming. Tick labels are drawn <b>inside</b>
 * the grid cells (plot area) and therefore do not require dynamic gutters. This prevents the common "everything shifts"
 * bug where the plot area changes size while dragging.
 * </p>
 *
 * <h2>Threading</h2>
 * <p>
 * All public API calls are safe to invoke from any thread. Internally, the viewer marshals work onto the JavaFX thread
 * using {@link JavaFxRuntime}.
 * </p>
 */
public final class GraphFxViewer extends StackPane {

    /**
     * Default window title used by {@link #show()}.
     */
    private static final String DEFAULT_WINDOW_TITLE = "GraphFx Viewer";

    /**
     * Default window width used by {@link #show()}.
     */
    private static final double DEFAULT_WINDOW_WIDTH_IN_PIXELS = 1000.0;

    /**
     * Default window height used by {@link #show()}.
     */
    private static final double DEFAULT_WINDOW_HEIGHT_IN_PIXELS = 720.0;

    /**
     * Minimal canvas dimension guard.
     */
    private static final double MINIMUM_CANVAS_SIZE_IN_PIXELS = 2.0;

    /**
     * Pre-created BigNumber constant 0.
     */
    private static final BigNumber BIG_NUMBER_ZERO = new BigNumber("0");

    /**
     * Pre-created BigNumber constant -1.
     */
    private static final BigNumber BIG_NUMBER_NEGATIVE_ONE = new BigNumber("-1");

    /**
     * Inner padding inside label boxes (tick labels and element labels).
     */
    private static final double LABEL_PADDING_IN_PIXELS = 4.0;

    /**
     * Corner arc radius of label boxes.
     */
    private static final double LABEL_BOX_ARC_IN_PIXELS = 6.0;

    /**
     * Inner distance (pixels) used to draw grid coordinate labels inside the plot area.
     */
    /**
     * Grid canvas layer that contains: background, plot background, grid lines, axes, tick labels.
     */
    private final Canvas gridCanvas;

    /**
     * Graphics context used to render the grid canvas layer.
     */
    private final GraphicsContext gridGraphicsContext;

    /**
     * Overlay canvas layer that contains: points, lines, polylines and their labels.
     *
     * <p>
     * This layer is clipped to the plot rectangle so that no elements render outside the plotting region.
     * </p>
     */
    private final Canvas overlayCanvas;

    /**
     * Graphics context used to render the overlay canvas layer.
     */
    private final GraphicsContext overlayGraphicsContext;

    /**
     * Clip rectangle applied to {@link #overlayCanvas}.
     */
    private final Rectangle overlayClipRectangle;

    /**
     * Reusable text instance for measuring label sizes (avoids frequent Text node allocations).
     */
    private final Text labelMeasurer;

    /**
     * Current viewport (visible world rectangle).
     */
    @Getter
    private GraphFxViewport viewport;

    /**
     * Current grid configuration.
     */
    @Getter
    private GraphFxGridConfiguration gridConfiguration;

    /**
     * Current viewer configuration.
     */
    @Getter
    private GraphFxViewerConfiguration viewerConfiguration;

    /**
     * Renderable points (immutable; the viewer does not mutate elements).
     */
    private final List<GraphFxPoint> pointElements;

    /**
     * Renderable lines (immutable; the viewer does not mutate elements).
     */
    private final List<GraphFxLine> lineElements;

    /**
     * Renderable polylines (immutable; the viewer does not mutate elements).
     */
    private final List<GraphFxPolyline> polylineElements;

    /**
     * JavaFX stage created by {@link #show()}.
     */
    private Stage viewerStage;

    /**
     * Enables/disables scroll-wheel zoom.
     */
    private boolean isZoomEnabled;

    /**
     * Enables/disables mouse-drag panning.
     */
    private boolean isPanEnabled;

    /**
     * True while the user is currently dragging to pan.
     */
    private boolean isCurrentlyPanning;

    /**
     * Last mouse X position (pixels) used during panning.
     */
    private double lastPanMouseScreenX;

    /**
     * Last mouse Y position (pixels) used during panning.
     */
    private double lastPanMouseScreenY;

    /**
     * Render scheduler flag (coalesces renders per JavaFX pulse).
     */
    private boolean isRenderScheduled;

    /**
     * Dirty flag indicating the grid layer must be re-rendered.
     */
    private boolean isGridDirty;

    /**
     * Dirty flag indicating the overlay layer must be re-rendered.
     */
    private boolean isOverlayDirty;

    /**
     * Creates a new viewer using default configurations.
     */
    public GraphFxViewer() {
        this(GraphFxViewport.DEFAULT, GraphFxGridConfiguration.DEFAULT, GraphFxViewerConfiguration.DEFAULT);
    }

    /**
     * Creates a new viewer using the given configurations.
     *
     * @param viewport            initial viewport (world bounds)
     * @param gridConfiguration   initial grid configuration
     * @param viewerConfiguration initial viewer configuration
     */
    public GraphFxViewer(
            @NonNull GraphFxViewport viewport,
            @NonNull GraphFxGridConfiguration gridConfiguration,
            @NonNull GraphFxViewerConfiguration viewerConfiguration
    ) {
        JavaFxRuntime.ensureToolkitInitialized();

        this.gridCanvas = new Canvas();
        this.gridGraphicsContext = this.gridCanvas.getGraphicsContext2D();

        this.overlayCanvas = new Canvas();
        this.overlayGraphicsContext = this.overlayCanvas.getGraphicsContext2D();

        this.overlayClipRectangle = new Rectangle();
        this.overlayCanvas.setClip(this.overlayClipRectangle);

        this.labelMeasurer = new Text();
        this.labelMeasurer.setManaged(false);

        this.viewport = viewport;
        this.gridConfiguration = gridConfiguration;
        this.viewerConfiguration = viewerConfiguration;

        this.pointElements = new ArrayList<>();
        this.lineElements = new ArrayList<>();
        this.polylineElements = new ArrayList<>();

        this.isZoomEnabled = true;
        this.isPanEnabled = true;

        this.isGridDirty = true;
        this.isOverlayDirty = true;

        JavaFxRuntime.runOnFxThreadAndWait(this::initializeUserInterface);
    }

    /**
     * Shows the viewer window using default size and title.
     */
    public void show() {
        show(DEFAULT_WINDOW_TITLE, DEFAULT_WINDOW_WIDTH_IN_PIXELS, DEFAULT_WINDOW_HEIGHT_IN_PIXELS);
    }

    /**
     * Shows the viewer window using the given window title and size.
     *
     * @param windowTitle         window title
     * @param windowWidthInPixels window width (pixels)
     * @param windowHeightInPixels window height (pixels)
     */
    public void show(@NonNull String windowTitle, double windowWidthInPixels, double windowHeightInPixels) {
        validateFinitePositive(windowWidthInPixels, "windowWidthInPixels");
        validateFinitePositive(windowHeightInPixels, "windowHeightInPixels");

        JavaFxRuntime.runOnFxThread(() -> {
            if (getParent() != null) {
                throw new IllegalStateException("This GraphFxViewer is already attached to a parent node.");
            }

            if (viewerStage == null) {
                Scene scene = new Scene(this, windowWidthInPixels, windowHeightInPixels);
                scene.setFill(viewerConfiguration.getBackgroundColor());

                viewerStage = new Stage();
                viewerStage.setScene(scene);
            } else {
                viewerStage.getScene().setFill(viewerConfiguration.getBackgroundColor());
            }

            viewerStage.setTitle(windowTitle);
            viewerStage.show();
            viewerStage.toFront();

            requestGridRender();
            requestOverlayRender();
        });
    }

    /**
     * Closes the viewer stage (if open).
     */
    public void close() {
        JavaFxRuntime.runOnFxThread(() -> {
            if (viewerStage != null) {
                viewerStage.close();
            }
        });
    }

    /**
     * Updates the viewport and triggers a full render.
     *
     * @param viewport new viewport
     */
    public void setViewport(@NonNull GraphFxViewport viewport) {
        JavaFxRuntime.runOnFxThread(() -> {
            this.viewport = viewport;
            requestGridRender();
            requestOverlayRender();
        });
    }

    /**
     * Updates grid configuration and triggers a grid render (overlay is unchanged).
     *
     * @param gridConfiguration new grid configuration
     */
    public void setGridConfiguration(@NonNull GraphFxGridConfiguration gridConfiguration) {
        JavaFxRuntime.runOnFxThread(() -> {
            this.gridConfiguration = gridConfiguration;
            requestGridRender();
        });
    }

    /**
     * Updates viewer configuration and triggers a full render.
     *
     * @param viewerConfiguration new viewer configuration
     */
    public void setViewerConfiguration(@NonNull GraphFxViewerConfiguration viewerConfiguration) {
        JavaFxRuntime.runOnFxThread(() -> {
            this.viewerConfiguration = viewerConfiguration;

            // Ensure the StackPane background always matches the configured background.
            setBackground(new Background(new BackgroundFill(
                    this.viewerConfiguration.getBackgroundColor(),
                    CornerRadii.EMPTY,
                    javafx.geometry.Insets.EMPTY
            )));

            // Keep the scene background in sync (prevents white borders during resizing).
            if (viewerStage != null && viewerStage.getScene() != null) {
                viewerStage.getScene().setFill(this.viewerConfiguration.getBackgroundColor());
            }

            requestGridRender();
            requestOverlayRender();
        });
    }

    /**
     * Enables or disables zoom behavior.
     *
     * @param isZoomEnabled true to enable zoom
     */
    public void setZoomEnabled(boolean isZoomEnabled) {
        JavaFxRuntime.runOnFxThread(() -> this.isZoomEnabled = isZoomEnabled);
    }

    /**
     * Enables or disables panning behavior.
     *
     * @param isPanEnabled true to enable panning
     */
    public void setPanEnabled(boolean isPanEnabled) {
        JavaFxRuntime.runOnFxThread(() -> this.isPanEnabled = isPanEnabled);
    }

    /**
     * Removes all elements from the viewer and triggers an overlay render.
     */
    public void clear() {
        JavaFxRuntime.runOnFxThread(() -> {
            pointElements.clear();
            lineElements.clear();
            polylineElements.clear();
            requestOverlayRender();
        });
    }

    /**
     * Adds a point element and returns a handle that can remove it later.
     *
     * @param point point to draw
     * @return handle that can remove the element
     */
    public GraphFxPlotHandle addPoint(@NonNull GraphFxPoint point) {
        return addAndReturnHandle(pointElements, point);
    }

    /**
     * Adds a line element and returns a handle that can remove it later.
     *
     * @param line line to draw
     * @return handle that can remove the element
     */
    public GraphFxPlotHandle addLine(@NonNull GraphFxLine line) {
        return addAndReturnHandle(lineElements, line);
    }

    /**
     * Adds a polyline element and returns a handle that can remove it later.
     *
     * @param polyline polyline to draw
     * @return handle that can remove the element
     */
    public GraphFxPlotHandle addPolyline(@NonNull GraphFxPolyline polyline) {
        if (polyline.getPolylinePoints().size() < 2) {
            throw new IllegalArgumentException("A polyline must contain at least 2 points.");
        }
        return addAndReturnHandle(polylineElements, polyline);
    }

    /**
     * Forces a re-render of all dirty layers.
     */
    public void render() {
        requestGridRender();
        requestOverlayRender();
    }

    private void initializeUserInterface() {
        setBackground(new Background(new BackgroundFill(
                viewerConfiguration.getBackgroundColor(),
                CornerRadii.EMPTY,
                javafx.geometry.Insets.EMPTY
        )));

        getChildren().addAll(gridCanvas, overlayCanvas);

        gridCanvas.widthProperty().bind(widthProperty());
        gridCanvas.heightProperty().bind(heightProperty());

        overlayCanvas.widthProperty().bind(widthProperty());
        overlayCanvas.heightProperty().bind(heightProperty());

        gridCanvas.widthProperty().addListener((o, a, b) -> requestGridRender());
        gridCanvas.heightProperty().addListener((o, a, b) -> requestGridRender());

        overlayCanvas.widthProperty().addListener((o, a, b) -> requestOverlayRender());
        overlayCanvas.heightProperty().addListener((o, a, b) -> requestOverlayRender());

        installDefaultInteractions();

        requestGridRender();
        requestOverlayRender();
    }

    /**
     * Installs scroll-wheel zoom and primary-button panning.
     */
    private void installDefaultInteractions() {
        addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!isZoomEnabled || event.getDeltaY() == 0.0) {
                return;
            }

            RenderContext context = createRenderContext();
            if (context == null) {
                return;
            }

            if (!context.isInsidePlotArea(event.getX(), event.getY())) {
                return;
            }

            BigNumber anchorWorldX = screenToWorldX(event.getX(), context);
            BigNumber anchorWorldY = screenToWorldY(event.getY(), context);

            double zoomFactor = event.getDeltaY() > 0.0
                    ? viewerConfiguration.getZoomInFactor()
                    : viewerConfiguration.getZoomOutFactor();

            viewport = zoomViewportAroundAnchor(viewport, anchorWorldX, anchorWorldY, zoomFactor);

            requestGridRender();
            requestOverlayRender();
            event.consume();
        });

        setOnMousePressed(event -> {
            if (!isPanEnabled || event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            RenderContext context = createRenderContext();
            if (context == null) {
                return;
            }

            if (!context.isInsidePlotArea(event.getX(), event.getY())) {
                return;
            }

            isCurrentlyPanning = true;
            lastPanMouseScreenX = event.getX();
            lastPanMouseScreenY = event.getY();
        });

        setOnMouseDragged(event -> {
            if (!isPanEnabled || !isCurrentlyPanning) {
                return;
            }

            RenderContext context = createRenderContext();
            if (context == null) {
                return;
            }

            double deltaScreenX = event.getX() - lastPanMouseScreenX;
            double deltaScreenY = event.getY() - lastPanMouseScreenY;

            viewport = panViewportByScreenDelta(viewport, deltaScreenX, deltaScreenY, context);

            lastPanMouseScreenX = event.getX();
            lastPanMouseScreenY = event.getY();

            requestGridRender();
            requestOverlayRender();
        });

        setOnMouseReleased(e -> isCurrentlyPanning = false);
        setOnMouseExited(e -> isCurrentlyPanning = false);
    }

    /**
     * Marks the grid layer as dirty and schedules a render.
     */
    private void requestGridRender() {
        JavaFxRuntime.runOnFxThread(() -> {
            isGridDirty = true;
            scheduleRenderIfRequired();
        });
    }

    /**
     * Marks the overlay layer as dirty and schedules a render.
     */
    private void requestOverlayRender() {
        JavaFxRuntime.runOnFxThread(() -> {
            isOverlayDirty = true;
            scheduleRenderIfRequired();
        });
    }

    /**
     * Coalesces grid/overlay renders within one JavaFX pulse and guarantees that both layers use the same render context
     * whenever both are dirty (prevents drifting / shifting artifacts while panning).
     */
    private void scheduleRenderIfRequired() {
        if (isRenderScheduled) {
            return;
        }
        isRenderScheduled = true;

        Platform.runLater(() -> {
            isRenderScheduled = false;

            boolean renderGrid = isGridDirty;
            boolean renderOverlay = isOverlayDirty;

            isGridDirty = false;
            isOverlayDirty = false;

            RenderContext context = createRenderContext();
            if (context == null) {
                return;
            }

            if (renderGrid) {
                renderGridLayer(context);
            }
            if (renderOverlay) {
                renderOverlayLayer(context);
            }
        });
    }

    /**
     * Renders the grid layer (background + grid + axes + coordinate labels).
     *
     * @param context render context
     */
    private void renderGridLayer(@NonNull RenderContext context) {
        // Fill entire canvas (prevents any visible white borders).
        gridGraphicsContext.setFill(viewerConfiguration.getBackgroundColor());
        gridGraphicsContext.fillRect(0, 0, context.canvasWidthInPixels, context.canvasHeightInPixels);

        // Plot background (optional).
        if (!viewerConfiguration.getPlotBackgroundColor().equals(viewerConfiguration.getBackgroundColor())) {
            gridGraphicsContext.setFill(viewerConfiguration.getPlotBackgroundColor());
            gridGraphicsContext.fillRect(
                    context.plotLeftInPixels,
                    context.plotTopInPixels,
                    context.plotWidthInPixels(),
                    context.plotHeightInPixels()
            );
        }

        if (gridConfiguration.isGridVisible()) {
            renderGridLines(context);
        }

        if (gridConfiguration.isAreAxesVisible()) {
            renderAxes(context);
        }

        if (gridConfiguration.isAreTickLabelsVisible()) {
            renderAxisCoordinateLabels(context);
        }

        if (gridConfiguration.isAreAxesVisible() && viewerConfiguration.isAxisNameVisible()) {
            renderAxisNames(context);
        }
    }

    /**
     * Renders the overlay layer (elements + element labels).
     *
     * @param context render context
     */
    private void renderOverlayLayer(@NonNull RenderContext context) {
        overlayClipRectangle.setX(context.plotLeftInPixels);
        overlayClipRectangle.setY(context.plotTopInPixels);
        overlayClipRectangle.setWidth(context.plotWidthInPixels());
        overlayClipRectangle.setHeight(context.plotHeightInPixels());

        overlayGraphicsContext.clearRect(0, 0, context.canvasWidthInPixels, context.canvasHeightInPixels);

        renderPolylines(context);
        renderLines(context);
        renderPoints(context);
    }

    private void renderGridLines(@NonNull RenderContext context) {
        BigNumber stepX = gridConfiguration.getGridSpacingXValue().abs();
        BigNumber stepY = gridConfiguration.getGridSpacingYValue().abs();

        if (isZero(stepX) || isZero(stepY)) {
            return;
        }

        int maxLines = Math.max(1, viewerConfiguration.getMaximumGridLineCountPerAxis());

        double stepXInPixels = context.scaleXInPixelsPerWorldUnit * bigNumberToDouble(stepX);
        double stepYInPixels = context.scaleYInPixelsPerWorldUnit * bigNumberToDouble(stepY);

        if (!Double.isFinite(stepXInPixels) || !Double.isFinite(stepYInPixels)) {
            return;
        }

        gridGraphicsContext.setStroke(viewerConfiguration.getGridColor());
        gridGraphicsContext.setLineWidth(viewerConfiguration.getGridStrokeWidthInPixels());

        BigNumber tickX = alignStartTick(context.minimumWorldXValue, stepX);
        double screenX = worldToScreenX(tickX, context);

        int countX = 0;
        while (tickX.compareTo(context.maximumWorldXValue) <= 0 && countX < maxLines) {
            double crispX = crispCoordinate(screenX, viewerConfiguration.getGridStrokeWidthInPixels());
            gridGraphicsContext.strokeLine(crispX, context.plotTopInPixels, crispX, context.plotBottomInPixels);

            tickX = tickX.add(stepX);
            screenX += stepXInPixels;
            countX++;
        }

        BigNumber tickY = alignStartTick(context.minimumWorldYValue, stepY);
        double screenY = worldToScreenY(tickY, context);

        int countY = 0;
        while (tickY.compareTo(context.maximumWorldYValue) <= 0 && countY < maxLines) {
            double crispY = crispCoordinate(screenY, viewerConfiguration.getGridStrokeWidthInPixels());
            gridGraphicsContext.strokeLine(context.plotLeftInPixels, crispY, context.plotRightInPixels, crispY);

            tickY = tickY.add(stepY);
            screenY -= stepYInPixels;
            countY++;
        }
    }

    private void renderAxes(@NonNull RenderContext context) {
        gridGraphicsContext.setStroke(viewerConfiguration.getAxesColor());
        gridGraphicsContext.setLineWidth(viewerConfiguration.getAxesStrokeWidthInPixels());

        if (context.isXAxisVisible) {
            double crispY = crispCoordinate(context.xAxisScreenYInPixels, viewerConfiguration.getAxesStrokeWidthInPixels());
            gridGraphicsContext.strokeLine(context.plotLeftInPixels, crispY, context.plotRightInPixels, crispY);
        }

        if (context.isYAxisVisible) {
            double crispX = crispCoordinate(context.yAxisScreenXInPixels, viewerConfiguration.getAxesStrokeWidthInPixels());
            gridGraphicsContext.strokeLine(crispX, context.plotTopInPixels, crispX, context.plotBottomInPixels);
        }
    }

    /**
     * Renders coordinate labels directly on the central axes (x = 0 and y = 0) whenever these axes are visible.
     *
     * <p>
     * Labels are placed at axis tick positions aligned with the configured grid spacing. If the configured spacing
     * results in labels that would be too dense in screen space, the viewer automatically skips ticks so that labels
     * remain readable and do not overlap.
     * </p>
     *
     * <p>
     * This method is strictly presentational: it never computes values, it only renders the already-known tick
     * coordinate values.
     * </p>
     *
     * @param context render context
     */
    
    private void renderAxisCoordinateLabels(@NonNull RenderContext context) {
        if (!context.isXAxisVisible && !context.isYAxisVisible) {
            return;
        }

        BigNumber stepX = gridConfiguration.getGridSpacingXValue().abs();
        BigNumber stepY = gridConfiguration.getGridSpacingYValue().abs();

        if (isZero(stepX) || isZero(stepY)) {
            return;
        }

        int maxTicks = Math.max(1, viewerConfiguration.getMaximumGridLineCountPerAxis());

        Font labelFont = viewerConfiguration.getLabelFont();
        Locale locale = viewerConfiguration.getLabelLocale();

        gridGraphicsContext.setFont(labelFont);

        Color labelTextColor = viewerConfiguration.getLabelColor();

        // Approximate label sizes based on viewport extremes (cheap + stable).
        String sampleXMin = formatBigNumberLabel(viewport.getWorldMinimumXValue(), locale);
        String sampleXMax = formatBigNumberLabel(viewport.getWorldMaximumXValue(), locale);
        TextMetrics sampleXMinMetrics = measureText(sampleXMin, labelFont);
        TextMetrics sampleXMaxMetrics = measureText(sampleXMax, labelFont);

        String sampleYMin = formatBigNumberLabel(viewport.getWorldMinimumYValue(), locale);
        String sampleYMax = formatBigNumberLabel(viewport.getWorldMaximumYValue(), locale);
        TextMetrics sampleYMinMetrics = measureText(sampleYMin, labelFont);
        TextMetrics sampleYMaxMetrics = measureText(sampleYMax, labelFont);

        double approxMaxXLabelWidth = Math.max(sampleXMinMetrics.width, sampleXMaxMetrics.width);
        double approxMaxYLabelWidth = Math.max(sampleYMinMetrics.width, sampleYMaxMetrics.width);
        double approxMaxLabelHeight = Math.max(
                Math.max(sampleXMinMetrics.height, sampleXMaxMetrics.height),
                Math.max(sampleYMinMetrics.height, sampleYMaxMetrics.height)
        );

        double stepXInPixels = context.scaleXInPixelsPerWorldUnit * bigNumberToDouble(stepX);
        double stepYInPixels = context.scaleYInPixelsPerWorldUnit * bigNumberToDouble(stepY);

        if (!Double.isFinite(stepXInPixels) || !Double.isFinite(stepYInPixels)) {
            return;
        }

        // Skip labels if ticks are too close (avoid overlap).
        int labelEveryX = computeLabelEvery(stepXInPixels, approxMaxXLabelWidth + 14.0);
        int labelEveryY = computeLabelEvery(stepYInPixels, approxMaxLabelHeight + 14.0);

        // Major tick step (aligned to 0): label ticks are placed at multiples of (gridStep * labelEvery).
        BigNumber majorStepX = stepX.multiply(new BigNumber(Integer.toString(labelEveryX)));
        BigNumber majorStepY = stepY.multiply(new BigNumber(Integer.toString(labelEveryY)));

        double majorStepXInPixels = stepXInPixels * labelEveryX;
        double majorStepYInPixels = stepYInPixels * labelEveryY;

        // Tick mark styling (uses axis color for visual consistency).
        gridGraphicsContext.setStroke(viewerConfiguration.getAxesColor());
        gridGraphicsContext.setLineWidth(1.0);

        final double axisInsetInPixels = 6.0;
        final double majorTickLengthInPixels = 12.0;

        double axisY = Double.NaN;
        double axisX = Double.NaN;

        if (context.isXAxisVisible) {
            axisY = crispCoordinate(context.xAxisScreenYInPixels, viewerConfiguration.getAxesStrokeWidthInPixels());
        }
        if (context.isYAxisVisible) {
            axisX = crispCoordinate(context.yAxisScreenXInPixels, viewerConfiguration.getAxesStrokeWidthInPixels());
        }

        // Decide once where to place x-axis labels (same style as y-axis deciding left/right once).
        boolean placeBelowXAxis = context.isXAxisVisible
                && (axisY + axisInsetInPixels + approxMaxLabelHeight <= context.plotBottomInPixels - 1.0);

        // -----------------------
        // X axis labels (on y = 0)
        // -----------------------
        if (context.isXAxisVisible && !isZero(majorStepX) && (majorStepXInPixels > 0.0)) {
            BigNumber tickX = alignStartTick(context.minimumWorldXValue, majorStepX);
            double screenX = worldToScreenX(tickX, context);

            int countX = 0;
            while (tickX.compareTo(context.maximumWorldXValue) <= 0 && countX < maxTicks) {
                boolean isOrigin = isZero(tickX);

                // Tick mark (vertical) on the x-axis
                double crispX = crispCoordinate(screenX, 1.0);
                gridGraphicsContext.strokeLine(
                        crispX,
                        axisY - majorTickLengthInPixels / 2.0,
                        crispX,
                        axisY + majorTickLengthInPixels / 2.0
                );

                if (!isOrigin) {
                    String label = formatBigNumberLabel(tickX, locale);
                    TextMetrics metrics = measureText(label, labelFont);

                    double centerY = placeBelowXAxis
                            ? axisY + axisInsetInPixels + metrics.height / 2.0
                            : axisY - axisInsetInPixels - metrics.height / 2.0;

                    drawAxisLabelTextCenteredClampedToPlot(
                            gridGraphicsContext,
                            label,
                            screenX,
                            centerY,
                            VPos.CENTER,
                            labelFont,
                            labelTextColor,
                            context
                    );
                }

tickX = tickX.add(majorStepX);
                screenX += majorStepXInPixels;
                countX++;
            }
        }

        // -----------------------
        // Y axis labels (on x = 0)
        // -----------------------
        if (context.isYAxisVisible && !isZero(majorStepY) && (majorStepYInPixels > 0.0)) {
            // Decide once whether labels can be placed left of the axis without overflowing.
            boolean placeLeftOfAxis = axisX - axisInsetInPixels - approxMaxYLabelWidth >= context.plotLeftInPixels + 1.0;

            BigNumber tickY = alignStartTick(context.minimumWorldYValue, majorStepY);
            double screenY = worldToScreenY(tickY, context);

            int countY = 0;
            while (tickY.compareTo(context.maximumWorldYValue) <= 0 && countY < maxTicks) {
                boolean isOrigin = isZero(tickY);

                // Tick mark (horizontal) on the y-axis
                double crispY = crispCoordinate(screenY, 1.0);
                gridGraphicsContext.strokeLine(
                        axisX - majorTickLengthInPixels / 2.0,
                        crispY,
                        axisX + majorTickLengthInPixels / 2.0,
                        crispY
                );

                if (!isOrigin) {
                    String label = formatBigNumberLabel(tickY, locale);

                    if (placeLeftOfAxis) {
                        drawAxisLabelTextRightAlignedClampedToPlot(
                                gridGraphicsContext,
                                label,
                                axisX - axisInsetInPixels,
                                screenY,
                                labelFont,
                                labelTextColor,
                                context
                        );
                    } else {
                        drawAxisLabelTextLeftAlignedClampedToPlot(
                                gridGraphicsContext,
                                label,
                                axisX + axisInsetInPixels,
                                screenY,
                                labelFont,
                                labelTextColor,
                                context
                        );
                    }
                }

                tickY = tickY.add(majorStepY);
                screenY -= majorStepYInPixels;
                countY++;
            }
        }

        // -----------------------
        // Origin label (drawn once)
        // -----------------------
        if (context.isXAxisVisible && context.isYAxisVisible) {
            String originLabel = formatBigNumberLabel(BIG_NUMBER_ZERO, locale);
            drawAxisLabelTextLeftAlignedClampedToPlot(
                    gridGraphicsContext,
                    originLabel,
                    axisX + axisInsetInPixels,
                    axisY + axisInsetInPixels,
                    labelFont,
                    labelTextColor,
                    context,
                    VPos.TOP
            );
        }
    }



private void renderAxisNames(@NonNull RenderContext context) {
        Font font = viewerConfiguration.getAxisNameFont();
        gridGraphicsContext.setFont(font);
        gridGraphicsContext.setFill(viewerConfiguration.getLabelColor());

        if (context.isXAxisVisible) {
            String xName = viewerConfiguration.getXAxisName();
            double x = context.plotRightInPixels - 4.0;
            double y = context.xAxisScreenYInPixels - 18.0;
            gridGraphicsContext.setTextAlign(TextAlignment.RIGHT);
            gridGraphicsContext.setTextBaseline(VPos.TOP);
            gridGraphicsContext.fillText(xName, x, y);
        }

        if (context.isYAxisVisible) {
            String yName = viewerConfiguration.getYAxisName();
            double x = context.yAxisScreenXInPixels + 6.0;
            double y = context.plotTopInPixels + 4.0;
            gridGraphicsContext.setTextAlign(TextAlignment.LEFT);
            gridGraphicsContext.setTextBaseline(VPos.TOP);
            gridGraphicsContext.fillText(yName, x, y);
        }
    }

    private void renderPolylines(@NonNull RenderContext context) {
        for (GraphFxPolyline polyline : polylineElements) {
            overlayGraphicsContext.setStroke(polyline.getPolylineColor());
            overlayGraphicsContext.setLineWidth(polyline.getPolylineStrokeWidthInPixels());

            GraphFxPolyline.GraphFxPolylinePoint first = polyline.getPolylinePoints().get(0);

            double previousX = worldToScreenX(first.getWorldXValue(), context);
            double previousY = worldToScreenY(first.getWorldYValue(), context);

            for (int index = 1; index < polyline.getPolylinePoints().size(); index++) {
                GraphFxPolyline.GraphFxPolylinePoint current = polyline.getPolylinePoints().get(index);

                double currentX = worldToScreenX(current.getWorldXValue(), context);
                double currentY = worldToScreenY(current.getWorldYValue(), context);

                overlayGraphicsContext.strokeLine(previousX, previousY, currentX, currentY);

                previousX = currentX;
                previousY = currentY;
            }

            String labelText = firstNonBlank(polyline.getPolylineLabelText(), polyline.getPolylineName());
            if (!labelText.isBlank()) {
                Point2D p = clampLabelAnchorToPlot(previousX + 8.0, previousY - 10.0, context);
                drawOverlayLabel(labelText, p.getX(), p.getY());
            }
        }
    }

    private void renderLines(@NonNull RenderContext context) {
        for (GraphFxLine line : lineElements) {
            double startX = worldToScreenX(line.getStartWorldXValue(), context);
            double startY = worldToScreenY(line.getStartWorldYValue(), context);

            double endX = worldToScreenX(line.getEndWorldXValue(), context);
            double endY = worldToScreenY(line.getEndWorldYValue(), context);

            overlayGraphicsContext.setStroke(line.getLineColor());
            overlayGraphicsContext.setLineWidth(line.getLineStrokeWidthInPixels());
            overlayGraphicsContext.strokeLine(startX, startY, endX, endY);

            String labelText = firstNonBlank(line.getLineLabelText(), line.getLineName());
            if (!labelText.isBlank()) {
                Point2D midpoint = new Point2D((startX + endX) / 2.0, (startY + endY) / 2.0);
                Point2D p = clampLabelAnchorToPlot(midpoint.getX() + 8.0, midpoint.getY() - 10.0, context);
                drawOverlayLabel(labelText, p.getX(), p.getY());
            }
        }
    }

    private void renderPoints(@NonNull RenderContext context) {
        for (GraphFxPoint point : pointElements) {
            double x = worldToScreenX(point.getWorldXValue(), context);
            double y = worldToScreenY(point.getWorldYValue(), context);

            overlayGraphicsContext.setFill(point.getPointColor());
            overlayGraphicsContext.fillOval(
                    x - point.getPointRadiusInPixels(),
                    y - point.getPointRadiusInPixels(),
                    point.getPointRadiusInPixels() * 2.0,
                    point.getPointRadiusInPixels() * 2.0
            );

            String label = point.getPointName() == null ? "" : point.getPointName();
            if (!label.isBlank()) {
                Point2D p = clampLabelAnchorToPlot(x + 10.0, y - 14.0, context);
                drawOverlayLabel(label, p.getX(), p.getY());
            }
        }
    }

    private void drawOverlayLabel(@NonNull String text, double anchorX, double anchorY) {
        Font font = viewerConfiguration.getLabelFont();
        overlayGraphicsContext.setFont(font);
        overlayGraphicsContext.setTextAlign(TextAlignment.LEFT);
        overlayGraphicsContext.setTextBaseline(VPos.TOP);

        TextMetrics metrics = measureText(text, font);

        double boxX = anchorX;
        double boxY = anchorY;
        double boxW = metrics.width + LABEL_PADDING_IN_PIXELS * 2.0;
        double boxH = metrics.height + LABEL_PADDING_IN_PIXELS * 2.0;

        overlayGraphicsContext.setFill(viewerConfiguration.getLabelBackgroundColor());
        overlayGraphicsContext.fillRoundRect(boxX, boxY, boxW, boxH, LABEL_BOX_ARC_IN_PIXELS, LABEL_BOX_ARC_IN_PIXELS);

        overlayGraphicsContext.setFill(viewerConfiguration.getLabelColor());
        overlayGraphicsContext.fillText(text, boxX + LABEL_PADDING_IN_PIXELS, boxY + LABEL_PADDING_IN_PIXELS);
    }

    



    



    



    /**
     * Draws a centered axis label inside the plot rectangle without any background box.
     *
     * <p>
     * This is used for x-axis tick labels to keep the visual style consistent with the y-axis labels:
     * simple typography directly on the axis without the previous "white pill" background.
     * </p>
     *
     * @param graphicsContext graphics context
     * @param text label text
     * @param centerX desired x position (centered)
     * @param baselineY desired y position for the provided {@code textBaseline}
     * @param textBaseline JavaFX text baseline mode
     * @param font font used for drawing
     * @param textColor text color
     * @param context render context
     */
    private void drawAxisLabelTextCenteredClampedToPlot(
            @NonNull GraphicsContext graphicsContext,
            @NonNull String text,
            double centerX,
            double baselineY,
            @NonNull VPos textBaseline,
            @NonNull Font font,
            @NonNull Color textColor,
            @NonNull RenderContext context
    ) {
        TextMetrics metrics = measureText(text, font);

        double halfWidth = metrics.width / 2.0;

        double minCenterX = context.plotLeftInPixels + halfWidth + 1.0;
        double maxCenterX = context.plotRightInPixels - halfWidth - 1.0;
        double clampedCenterX = clampDouble(centerX, minCenterX, maxCenterX);

        double clampedBaselineY = clampBaselineYToPlot(baselineY, metrics.height, textBaseline, context);

        graphicsContext.setFont(font);
        graphicsContext.setFill(textColor);
        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setTextBaseline(textBaseline);
        graphicsContext.fillText(text, clampedCenterX, clampedBaselineY);
    }

    /**
     * Draws a right-aligned y-axis label inside the plot rectangle without any background box.
     *
     * @param graphicsContext graphics context
     * @param text label text
     * @param rightX desired x position (right edge)
     * @param centerY desired y position (vertical center)
     * @param font font used for drawing
     * @param textColor text color
     * @param context render context
     */
    private void drawAxisLabelTextRightAlignedClampedToPlot(
            @NonNull GraphicsContext graphicsContext,
            @NonNull String text,
            double rightX,
            double centerY,
            @NonNull Font font,
            @NonNull Color textColor,
            @NonNull RenderContext context
    ) {
        drawAxisLabelTextRightAlignedClampedToPlot(graphicsContext, text, rightX, centerY, font, textColor, context, VPos.CENTER);
    }

    /**
     * Draws a left-aligned axis label inside the plot rectangle without any background box.
     *
     * @param graphicsContext graphics context
     * @param text label text
     * @param leftX desired x position (left edge)
     * @param centerY desired y position (vertical center)
     * @param font font used for drawing
     * @param textColor text color
     * @param context render context
     */
    private void drawAxisLabelTextLeftAlignedClampedToPlot(
            @NonNull GraphicsContext graphicsContext,
            @NonNull String text,
            double leftX,
            double centerY,
            @NonNull Font font,
            @NonNull Color textColor,
            @NonNull RenderContext context
    ) {
        drawAxisLabelTextLeftAlignedClampedToPlot(graphicsContext, text, leftX, centerY, font, textColor, context, VPos.CENTER);
    }

    /**
     * Draws a right-aligned axis label inside the plot rectangle without any background box.
     *
     * @param graphicsContext graphics context
     * @param text label text
     * @param rightX desired x position (right edge)
     * @param baselineY desired y position for the provided {@code textBaseline}
     * @param font font used for drawing
     * @param textColor text color
     * @param context render context
     * @param textBaseline JavaFX text baseline mode
     */
    private void drawAxisLabelTextRightAlignedClampedToPlot(
            @NonNull GraphicsContext graphicsContext,
            @NonNull String text,
            double rightX,
            double baselineY,
            @NonNull Font font,
            @NonNull Color textColor,
            @NonNull RenderContext context,
            @NonNull VPos textBaseline
    ) {
        TextMetrics metrics = measureText(text, font);

        double minRightX = context.plotLeftInPixels + metrics.width + 1.0;
        double maxRightX = context.plotRightInPixels - 1.0;
        double clampedRightX = clampDouble(rightX, minRightX, maxRightX);

        double clampedBaselineY = clampBaselineYToPlot(baselineY, metrics.height, textBaseline, context);

        graphicsContext.setFont(font);
        graphicsContext.setFill(textColor);
        graphicsContext.setTextAlign(TextAlignment.RIGHT);
        graphicsContext.setTextBaseline(textBaseline);
        graphicsContext.fillText(text, clampedRightX, clampedBaselineY);
    }

    /**
     * Draws a left-aligned axis label inside the plot rectangle without any background box.
     *
     * @param graphicsContext graphics context
     * @param text label text
     * @param leftX desired x position (left edge)
     * @param baselineY desired y position for the provided {@code textBaseline}
     * @param font font used for drawing
     * @param textColor text color
     * @param context render context
     * @param textBaseline JavaFX text baseline mode
     */
    private void drawAxisLabelTextLeftAlignedClampedToPlot(
            @NonNull GraphicsContext graphicsContext,
            @NonNull String text,
            double leftX,
            double baselineY,
            @NonNull Font font,
            @NonNull Color textColor,
            @NonNull RenderContext context,
            @NonNull VPos textBaseline
    ) {
        TextMetrics metrics = measureText(text, font);

        double minLeftX = context.plotLeftInPixels + 1.0;
        double maxLeftX = context.plotRightInPixels - metrics.width - 1.0;
        double clampedLeftX = clampDouble(leftX, minLeftX, maxLeftX);

        double clampedBaselineY = clampBaselineYToPlot(baselineY, metrics.height, textBaseline, context);

        graphicsContext.setFont(font);
        graphicsContext.setFill(textColor);
        graphicsContext.setTextAlign(TextAlignment.LEFT);
        graphicsContext.setTextBaseline(textBaseline);
        graphicsContext.fillText(text, clampedLeftX, clampedBaselineY);
    }

    /**
     * Clamps a label baseline Y coordinate so that the text stays fully inside the plot rectangle.
     *
     * @param desiredBaselineY desired baseline y coordinate
     * @param textHeight text height in pixels
     * @param textBaseline text baseline mode
     * @param context render context
     * @return clamped baseline y coordinate
     */
    private double clampBaselineYToPlot(double desiredBaselineY, double textHeight, @NonNull VPos textBaseline, @NonNull RenderContext context) {
        if (textBaseline == VPos.TOP) {
            double minY = context.plotTopInPixels + 1.0;
            double maxY = context.plotBottomInPixels - textHeight - 1.0;
            return clampDouble(desiredBaselineY, minY, maxY);
        }
        if (textBaseline == VPos.BOTTOM) {
            double minY = context.plotTopInPixels + textHeight + 1.0;
            double maxY = context.plotBottomInPixels - 1.0;
            return clampDouble(desiredBaselineY, minY, maxY);
        }

        // VPos.CENTER (and any future baseline mode): treat as centered.
        double half = textHeight / 2.0;
        double minY = context.plotTopInPixels + half + 1.0;
        double maxY = context.plotBottomInPixels - half - 1.0;
        return clampDouble(desiredBaselineY, minY, maxY);
    }

    /**
     * Simple helper to clamp a double value.
     *
     * @param value input value
     * @param min minimum allowed value
     * @param max maximum allowed value
     * @return clamped value
     */
    private double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }


    private TextMetrics measureText(@NonNull String text, @NonNull Font font) {
        labelMeasurer.setFont(font);
        labelMeasurer.setText(text);
        double w = Math.ceil(labelMeasurer.getLayoutBounds().getWidth());
        double h = Math.ceil(labelMeasurer.getLayoutBounds().getHeight());
        return new TextMetrics(Math.max(1.0, w), Math.max(1.0, h));
    }

    private RenderContext createRenderContext() {
        double canvasWidth = gridCanvas.getWidth();
        double canvasHeight = gridCanvas.getHeight();

        if (canvasWidth < MINIMUM_CANVAS_SIZE_IN_PIXELS || canvasHeight < MINIMUM_CANVAS_SIZE_IN_PIXELS) {
            return null;
        }

        // Fixed plot paddings (layout stability during panning/zooming).
        double plotLeft = viewerConfiguration.getMinimumLeftGutterInPixels();
        double plotRight = canvasWidth - viewerConfiguration.getRightPaddingInPixels();
        double plotTop = viewerConfiguration.getTopPaddingInPixels();
        double plotBottom = canvasHeight - viewerConfiguration.getMinimumBottomGutterInPixels();

        if (plotRight <= plotLeft + 1.0) {
            plotRight = plotLeft + 1.0;
        }
        if (plotBottom <= plotTop + 1.0) {
            plotBottom = plotTop + 1.0;
        }

        BigNumber minX = viewport.getWorldMinimumXValue();
        BigNumber maxX = viewport.getWorldMaximumXValue();
        BigNumber minY = viewport.getWorldMinimumYValue();
        BigNumber maxY = viewport.getWorldMaximumYValue();

        BigNumber width = maxX.subtract(minX);
        BigNumber height = maxY.subtract(minY);

        if (isZero(width) || isZero(height)) {
            throw new IllegalStateException("Viewport width/height must not be zero.");
        }

        double worldWidth = bigNumberToDouble(width);
        double worldHeight = bigNumberToDouble(height);

        double plotWidth = plotRight - plotLeft;
        double plotHeight = plotBottom - plotTop;

        double scaleX = plotWidth / worldWidth;
        double scaleY = plotHeight / worldHeight;

        boolean xAxisVisible = minY.compareTo(BIG_NUMBER_ZERO) <= 0 && maxY.compareTo(BIG_NUMBER_ZERO) >= 0;
        boolean yAxisVisible = minX.compareTo(BIG_NUMBER_ZERO) <= 0 && maxX.compareTo(BIG_NUMBER_ZERO) >= 0;

        double minXDouble = bigNumberToDouble(minX);
        double minYDouble = bigNumberToDouble(minY);

        double xAxisScreenY = xAxisVisible
                ? plotBottom - (0.0 - minYDouble) * scaleY
                : plotBottom;

        double yAxisScreenX = yAxisVisible
                ? plotLeft + (0.0 - minXDouble) * scaleX
                : plotLeft;

        return new RenderContext(
                canvasWidth,
                canvasHeight,
                plotLeft,
                plotRight,
                plotTop,
                plotBottom,
                minX,
                maxX,
                minY,
                maxY,
                minXDouble,
                minYDouble,
                scaleX,
                scaleY,
                xAxisVisible,
                yAxisVisible,
                xAxisScreenY,
                yAxisScreenX
        );
    }

    private double worldToScreenX(@NonNull BigNumber worldXValue, @NonNull RenderContext context) {
        double worldXDouble = bigNumberToDouble(worldXValue);
        return context.plotLeftInPixels + (worldXDouble - context.minimumWorldXAsDouble) * context.scaleXInPixelsPerWorldUnit;
    }

    private double worldToScreenY(@NonNull BigNumber worldYValue, @NonNull RenderContext context) {
        double worldYDouble = bigNumberToDouble(worldYValue);
        return context.plotBottomInPixels - (worldYDouble - context.minimumWorldYAsDouble) * context.scaleYInPixelsPerWorldUnit;
    }

    private BigNumber screenToWorldX(double screenX, @NonNull RenderContext context) {
        MathContext mathContext = viewerConfiguration.getRenderingMathContext();

        double localX = screenX - context.plotLeftInPixels;
        double plotWidth = context.plotWidthInPixels();

        BigNumber relative = bigNumberFromDouble(localX).divide(bigNumberFromDouble(plotWidth), mathContext);
        BigNumber range = context.maximumWorldXValue.subtract(context.minimumWorldXValue);

        return context.minimumWorldXValue.add(relative.multiply(range));
    }

    private BigNumber screenToWorldY(double screenY, @NonNull RenderContext context) {
        MathContext mathContext = viewerConfiguration.getRenderingMathContext();

        double inverted = context.plotBottomInPixels - screenY;
        double plotHeight = context.plotHeightInPixels();

        BigNumber relative = bigNumberFromDouble(inverted).divide(bigNumberFromDouble(plotHeight), mathContext);
        BigNumber range = context.maximumWorldYValue.subtract(context.minimumWorldYValue);

        return context.minimumWorldYValue.add(relative.multiply(range));
    }

    private GraphFxViewport panViewportByScreenDelta(
            @NonNull GraphFxViewport currentViewport,
            double deltaScreenX,
            double deltaScreenY,
            @NonNull RenderContext context
    ) {
        MathContext mathContext = viewerConfiguration.getRenderingMathContext();

        BigNumber minX = currentViewport.getWorldMinimumXValue();
        BigNumber maxX = currentViewport.getWorldMaximumXValue();
        BigNumber minY = currentViewport.getWorldMinimumYValue();
        BigNumber maxY = currentViewport.getWorldMaximumYValue();

        BigNumber worldWidth = maxX.subtract(minX);
        BigNumber worldHeight = maxY.subtract(minY);

        double plotWidthInPixels = context.plotWidthInPixels();
        double plotHeightInPixels = context.plotHeightInPixels();

        BigNumber ratioX = bigNumberFromDouble(deltaScreenX).divide(bigNumberFromDouble(plotWidthInPixels), mathContext);
        BigNumber ratioY = bigNumberFromDouble(deltaScreenY).divide(bigNumberFromDouble(plotHeightInPixels), mathContext);

        BigNumber deltaWorldX = worldWidth.multiply(ratioX).multiply(BIG_NUMBER_NEGATIVE_ONE);
        BigNumber deltaWorldY = worldHeight.multiply(ratioY);

        return GraphFxViewport.builder()
                .worldMinimumXValue(minX.add(deltaWorldX))
                .worldMaximumXValue(maxX.add(deltaWorldX))
                .worldMinimumYValue(minY.add(deltaWorldY))
                .worldMaximumYValue(maxY.add(deltaWorldY))
                .build();
    }

    private GraphFxViewport zoomViewportAroundAnchor(
            @NonNull GraphFxViewport currentViewport,
            @NonNull BigNumber anchorWorldX,
            @NonNull BigNumber anchorWorldY,
            double zoomFactor
    ) {
        MathContext mathContext = viewerConfiguration.getRenderingMathContext();

        BigNumber minX = currentViewport.getWorldMinimumXValue();
        BigNumber maxX = currentViewport.getWorldMaximumXValue();
        BigNumber minY = currentViewport.getWorldMinimumYValue();
        BigNumber maxY = currentViewport.getWorldMaximumYValue();

        BigNumber width = maxX.subtract(minX);
        BigNumber height = maxY.subtract(minY);

        if (isZero(width) || isZero(height)) {
            return currentViewport;
        }

        BigNumber zoomFactorBigNumber = bigNumberFromDouble(zoomFactor);

        BigNumber newWidth = width.multiply(zoomFactorBigNumber);
        BigNumber newHeight = height.multiply(zoomFactorBigNumber);

        BigNumber relativeX = anchorWorldX.subtract(minX).divide(width, mathContext);
        BigNumber relativeY = anchorWorldY.subtract(minY).divide(height, mathContext);

        BigNumber newMinX = anchorWorldX.subtract(relativeX.multiply(newWidth));
        BigNumber newMaxX = newMinX.add(newWidth);

        BigNumber newMinY = anchorWorldY.subtract(relativeY.multiply(newHeight));
        BigNumber newMaxY = newMinY.add(newHeight);

        return GraphFxViewport.builder()
                .worldMinimumXValue(newMinX)
                .worldMaximumXValue(newMaxX)
                .worldMinimumYValue(newMinY)
                .worldMaximumYValue(newMaxY)
                .build();
    }

    private <T> GraphFxPlotHandle addAndReturnHandle(@NonNull List<T> elementList, @NonNull T element) {
        Runnable removeAction = () -> JavaFxRuntime.runOnFxThread(() -> {
            elementList.remove(element);
            requestOverlayRender();
        });

        JavaFxRuntime.runOnFxThread(() -> {
            elementList.add(element);
            requestOverlayRender();
        });

        return new GraphFxPlotHandle(removeAction);
    }

    private BigNumber alignStartTick(@NonNull BigNumber minimum, @NonNull BigNumber step) {
        BigNumber remainder = minimum.modulo(step);
        BigNumber start = minimum.subtract(remainder);
        if (start.compareTo(minimum) > 0) {
            start = start.subtract(step);
        }
        return start;
    }

    private int computeLabelEvery(double stepInPixels, double requiredSpacingInPixels) {
        if (!(stepInPixels > 0.0) || !Double.isFinite(stepInPixels)) {
            return Integer.MAX_VALUE;
        }
        int every = (int) Math.ceil(requiredSpacingInPixels / stepInPixels);
        return Math.max(1, every);
    }

    private double crispCoordinate(double coordinate, double strokeWidthInPixels) {
        if (strokeWidthInPixels <= 0.0) {
            return coordinate;
        }
        // Pixel snapping: 1px lines look best at N + 0.5, thicker lines at integer boundaries.
        if (strokeWidthInPixels <= 1.0) {
            return Math.floor(coordinate) + 0.5;
        }
        return Math.round(coordinate);
    }

    private String formatBigNumberLabel(@NonNull BigNumber value, @NonNull Locale locale) {
        String raw = value.toString();
        char decimalSeparator = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
        return decimalSeparator == '.' ? raw : raw.replace('.', decimalSeparator);
    }

    private BigNumber bigNumberFromDouble(double value) {
        return new BigNumber(Double.toString(value));
    }

    private double bigNumberToDouble(@NonNull BigNumber value) {
        return Double.parseDouble(value.toString());
    }

    private boolean isZero(@NonNull BigNumber value) {
        return value.compareTo(BIG_NUMBER_ZERO) == 0;
    }

    private void validateFinite(double value, @NonNull String parameterName) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(parameterName + " must be finite.");
        }
    }

    private void validateFinitePositive(double value, @NonNull String parameterName) {
        validateFinite(value, parameterName);
        if (!(value > 0.0)) {
            throw new IllegalArgumentException(parameterName + " must be > 0.");
        }
    }

    private String firstNonBlank(String first, String fallback) {
        String a = first == null ? "" : first.trim();
        if (!a.isBlank()) {
            return a;
        }
        String b = fallback == null ? "" : fallback.trim();
        return b.isBlank() ? "" : b;
    }

    private Point2D clampLabelAnchorToPlot(double proposedX, double proposedY, @NonNull RenderContext context) {
        double x = Math.max(context.plotLeftInPixels + 2.0, Math.min(proposedX, context.plotRightInPixels - 2.0));
        double y = Math.max(context.plotTopInPixels + 2.0, Math.min(proposedY, context.plotBottomInPixels - 2.0));
        return new Point2D(x, y);
    }

    private record TextMetrics(double width, double height) {
    }

    private record RenderContext(
            double canvasWidthInPixels,
            double canvasHeightInPixels,
            double plotLeftInPixels,
            double plotRightInPixels,
            double plotTopInPixels,
            double plotBottomInPixels,
            BigNumber minimumWorldXValue,
            BigNumber maximumWorldXValue,
            BigNumber minimumWorldYValue,
            BigNumber maximumWorldYValue,
            double minimumWorldXAsDouble,
            double minimumWorldYAsDouble,
            double scaleXInPixelsPerWorldUnit,
            double scaleYInPixelsPerWorldUnit,
            boolean isXAxisVisible,
            boolean isYAxisVisible,
            double xAxisScreenYInPixels,
            double yAxisScreenXInPixels
    ) {
        /**
         * Returns plot width in pixels.
         */
        double plotWidthInPixels() {
            return plotRightInPixels - plotLeftInPixels;
        }

        /**
         * Returns plot height in pixels.
         */
        double plotHeightInPixels() {
            return plotBottomInPixels - plotTopInPixels;
        }

        /**
         * Checks whether the given pixel coordinate lies inside the plot rectangle.
         */
        boolean isInsidePlotArea(double x, double y) {
            return x >= plotLeftInPixels && x <= plotRightInPixels && y >= plotTopInPixels && y <= plotBottomInPixels;
        }
    }
}
