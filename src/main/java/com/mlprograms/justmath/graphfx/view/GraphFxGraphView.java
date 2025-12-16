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

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graphfx.model.*;
import com.mlprograms.justmath.graphfx.service.GraphFxAnalysisMath;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JavaFX view responsible for rendering a cartesian coordinate system, a set of user-defined functions and
 * analysis objects (points, tangents, normals, intersections, integrals).
 * <p>
 * The view maintains a {@link WorldView} which defines the visible world-coordinate rectangle and provides
 * conversion functions between world and screen coordinates.
 * <p>
 * For performance reasons, function sampling is performed asynchronously on a dedicated background thread.
 * Sampled polylines are cached per function and updated when the model revision changes, the viewport changes,
 * or interactive quality settings change.
 */
public class GraphFxGraphView extends StackPane {

    /**
     * Describes how mouse interactions are interpreted.
     */
    public enum ToolMode {
        /**
         * Pan the viewport using mouse drag; mouse wheel zooms.
         */
        MOVE,
        /**
         * Draw a rectangle on drag and zoom to its bounds on release.
         */
        ZOOM_BOX,
        /**
         * Place a point on the selected function at the clicked x-position.
         */
        POINT_ON_FUNCTION,
        /**
         * Create a tangent line at the clicked x-position on the selected function.
         */
        TANGENT,
        /**
         * Create a normal line at the clicked x-position on the selected function.
         */
        NORMAL,
        /**
         * Find a nearby root around the clicked x-position and place a root marker.
         */
        ROOT,
        /**
         * Pick two different functions and place intersection markers in the current x-range.
         */
        INTERSECTION,
        /**
         * Click-drag to create an integral object between start and end x on a function.
         */
        INTEGRAL
    }

    /**
     * Listener for cursor movement in world coordinates (e.g. to display coordinates in a status bar).
     */
    public interface StatusListener {

        /**
         * Called whenever the cursor moves over the graph canvas.
         *
         * @param x world x-coordinate under the cursor
         * @param y world y-coordinate under the cursor
         */
        void onCursorMoved(double x, double y);
    }

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    private static final WorldView DEFAULT_VIEW = new WorldView(-10, 10, -10, 10);

    private static final int MAX_UNDO_ENTRIES = 200;
    private static final int UNDO_SHRINK_TO = 150;

    private static final double ZOOM_FACTOR_IN = 0.90;
    private static final double ZOOM_FACTOR_OUT = 1.10;

    private static final double FUNCTION_PICK_DISTANCE_PX = 14.0;
    private static final double POLYLINE_DISCONTINUITY_Y_JUMP = 150_000d;
    private static final double POLYLINE_Y_ABS_LIMIT = 1_000_000d;

    private static final int MIN_FIT_SAMPLES = 400;
    private static final int MIN_INTERACTIVE_SAMPLES = 420;
    private static final int MIN_QUALITY_SAMPLES = 1000;

    private static final int INTEGRAL_STEPS_INTERACTIVE = 220;
    private static final int INTEGRAL_STEPS_QUALITY = 900;

    private static final int INTEGRAL_FILL_SAMPLES_INTERACTIVE = 160;
    private static final int INTEGRAL_FILL_SAMPLES_QUALITY = 720;

    private static final int ROOT_SEARCH_STEPS = 260;
    private static final int INTERSECTION_SEARCH_STEPS = 1600;

    private static final int RENDER_DEBOUNCE_MILLIS = 45;
    private static final int SAMPLE_DELAY_INTERACTIVE_MS = 25;
    private static final int SAMPLE_DELAY_QUALITY_MS = 65;

    private final GraphFxAnalysisMath graphFxAnalysisMath = new GraphFxAnalysisMath();
    private final GraphFxModel model;
    private final CalculatorEngine calculatorEngine;

    private final Canvas plotCanvas = new Canvas(900, 700);
    private final DecimalFormat axisLabelFormat = new DecimalFormat("0.########");

    private final PauseTransition renderDebounceTransition = new PauseTransition(Duration.millis(RENDER_DEBOUNCE_MILLIS));
    private final ScheduledExecutorService samplingExecutor = Executors.newSingleThreadScheduledExecutor(createThreadFactory("fx-graph-sampler"));

    private final Map<UUID, FunctionCache> functionCacheById = new HashMap<>();

    private ToolMode activeToolMode = ToolMode.MOVE;
    private boolean isInteractiveModeEnabled;

    private WorldView worldView = DEFAULT_VIEW;

    private final Deque<WorldView> undoStack = new ArrayDeque<>();
    private final Deque<WorldView> redoStack = new ArrayDeque<>();

    private StatusListener cursorStatusListener;

    private double dragStartScreenX;
    private double dragStartScreenY;
    private boolean isDragging;

    private double zoomBoxScreenX;
    private double zoomBoxScreenY;
    private double zoomBoxScreenWidth;
    private double zoomBoxScreenHeight;

    private UUID intersectionFirstFunctionId;

    private UUID integralFunctionId;
    private BigDecimal integralStartX;

    /**
     * Creates a graph view that renders the provided model and evaluates expressions using the given engine.
     *
     * @param model            data model containing functions, objects and settings (must not be {@code null})
     * @param calculatorEngine engine used to evaluate expressions and analysis helpers (must not be {@code null})
     */
    public GraphFxGraphView(@NonNull final GraphFxModel model, @NonNull final CalculatorEngine calculatorEngine) {
        this.model = model;
        this.calculatorEngine = calculatorEngine;

        setPadding(new Insets(0));
        getChildren().add(plotCanvas);

        bindCanvasToContainer();
        installResizeHandlers();
        installModelListeners();
        installMouseHandlers();
        installKeyHandlers();
        configureRenderDebounce();

        enforceAspectExpandOnly();
        renderImmediately();
    }

    /**
     * Sets a listener that will receive world-coordinate cursor updates.
     * <p>
     * Passing {@code null} removes the listener.
     *
     * @param listener listener to set (nullable)
     */
    public void setStatusListener(final StatusListener listener) {
        this.cursorStatusListener = listener;
    }

    /**
     * Changes the active tool mode and schedules a debounced re-render.
     *
     * @param toolMode new tool mode (must not be {@code null})
     */
    public void setToolMode(@NonNull final ToolMode toolMode) {
        this.activeToolMode = toolMode;
        scheduleRender();
    }

    /**
     * Enables or disables interactive mode.
     * <p>
     * When enabled, sampling and certain computations use fewer steps to remain responsive.
     *
     * @param interactiveMode {@code true} to enable interactive mode, {@code false} to render at higher quality
     */
    public void setInteractiveMode(final boolean interactiveMode) {
        this.isInteractiveModeEnabled = interactiveMode;
        scheduleRender();
    }

    /**
     * Resets the viewport to the default world bounds and triggers an immediate render.
     * <p>
     * The previous view is pushed onto the undo stack.
     */
    public void resetView() {
        pushUndoView();
        worldView = DEFAULT_VIEW;
        enforceAspectExpandOnly();
        renderImmediately();
    }

    /**
     * Adjusts the viewport so that all visible functions are reasonably within view on the y-axis,
     * while keeping the current x-range.
     * <p>
     * The method samples all visible functions across the current x-range. If at least one finite y-value
     * is found, it sets the y-limits to the min/max and applies a small padding for better framing.
     * <p>
     * If sampling yields no finite values or only a constant value, the method returns without changing
     * the current view.
     */
    public void fitToData() {
        final BigDecimal xMin = BigDecimal.valueOf(worldView.xMin());
        final BigDecimal xMax = BigDecimal.valueOf(worldView.xMax());

        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        final Map<String, String> variables = model.variablesAsStringMap();
        final int sampleCount = Math.max(MIN_FIT_SAMPLES, (int) plotCanvas.getWidth());
        final BigDecimal step = xMax.subtract(xMin, MATH_CONTEXT).divide(BigDecimal.valueOf(sampleCount), MATH_CONTEXT);

        for (final GraphFxFunction function : model.getFunctions()) {
            if (!function.isVisible()) {
                continue;
            }

            for (int i = 0; i <= sampleCount; i++) {
                final BigDecimal x = (i == sampleCount)
                        ? xMax
                        : xMin.add(step.multiply(BigDecimal.valueOf(i), MATH_CONTEXT), MATH_CONTEXT);

                final Double y = graphFxAnalysisMath.evalY(calculatorEngine, function.getExpression(), variables, x);
                if (y == null) {
                    continue;
                }

                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }

        if (!Double.isFinite(minY) || !Double.isFinite(maxY) || minY == maxY) {
            return;
        }

        pushUndoView();
        worldView = new WorldView(worldView.xMin(), worldView.xMax(), minY, maxY).pad(0.08);
        enforceAspectExpandOnly();
        redoStack.clear();
        renderImmediately();
    }

    /**
     * Releases resources held by this view.
     * <p>
     * This cancels pending sampling tasks and stops the sampling executor thread. Call this method when the view
     * is no longer needed (e.g., on window close) to avoid thread leaks.
     */
    public void dispose() {
        for (final FunctionCache cache : functionCacheById.values()) {
            cache.cancelScheduledSampling();
        }
        samplingExecutor.shutdownNow();
    }

    /**
     * Returns the current world viewport.
     *
     * @return current {@link WorldView} (never {@code null})
     */
    public WorldView getView() {
        return worldView;
    }

    /**
     * Returns the cached polyline for the currently selected function, if available.
     *
     * @return polyline for the selected function, or {@code null} if not sampled / no function selected
     */
    public GraphPolyline getPolylineForSelectedFunction() {
        final GraphFxFunction selectedFunction = model.getSelectedFunction();
        if (selectedFunction == null) {
            return null;
        }
        final FunctionCache cache = functionCacheById.get(selectedFunction.getId());
        return cache == null ? null : cache.lastPolyline;
    }

    /**
     * Represents a sampled polyline of a function.
     * <p>
     * Each entry in {@link #segments()} is a contiguous list of points. Segments are separated whenever
     * the function becomes undefined or a discontinuity is detected.
     *
     * @param segments contiguous segments that form the full polyline
     */
    public record GraphPolyline(List<List<GraphPoint>> segments) {
    }

    /**
     * Defines the visible world rectangle in cartesian coordinates.
     *
     * @param xMin minimum visible x (must be strictly smaller than {@code xMax})
     * @param xMax maximum visible x (must be strictly greater than {@code xMin})
     * @param yMin minimum visible y (must be strictly smaller than {@code yMax})
     * @param yMax maximum visible y (must be strictly greater than {@code yMin})
     */
    public record WorldView(double xMin, double xMax, double yMin, double yMax) {

        /**
         * Validates that the view bounds define a non-empty rectangle.
         *
         * @throws IllegalArgumentException if {@code xMin >= xMax} or {@code yMin >= yMax}
         */
        public WorldView {
            if (xMin >= xMax) {
                throw new IllegalArgumentException("xMin must be < xMax");
            }
            if (yMin >= yMax) {
                throw new IllegalArgumentException("yMin must be < yMax");
            }
        }

        /**
         * Checks whether the provided x-coordinate is inside the view.
         *
         * @param x world x-coordinate
         * @return {@code true} if the coordinate is inside the visible x-range
         */
        public boolean containsX(final double x) {
            return x >= xMin && x <= xMax;
        }

        /**
         * Checks whether the provided y-coordinate is inside the view.
         *
         * @param y world y-coordinate
         * @return {@code true} if the coordinate is inside the visible y-range
         */
        public boolean containsY(final double y) {
            return y >= yMin && y <= yMax;
        }

        /**
         * Returns a new view shifted by the given world deltas.
         *
         * @param dx delta to add to x bounds
         * @param dy delta to add to y bounds
         * @return panned view
         */
        public WorldView pan(final double dx, final double dy) {
            return new WorldView(xMin + dx, xMax + dx, yMin + dy, yMax + dy);
        }

        /**
         * Returns a new view zoomed around an anchor point in world coordinates.
         *
         * @param anchorX anchor x in world coordinates
         * @param anchorY anchor y in world coordinates
         * @param factor  zoom factor (values &lt; 1 zoom in, values &gt; 1 zoom out)
         * @return zoomed view
         */
        public WorldView zoom(final double anchorX, final double anchorY, final double factor) {
            final double newWidth = (xMax - xMin) * factor;
            final double newHeight = (yMax - yMin) * factor;

            final double relativeX = (anchorX - xMin) / (xMax - xMin);
            final double relativeY = (anchorY - yMin) / (yMax - yMin);

            final double newXMin = anchorX - relativeX * newWidth;
            final double newXMax = newXMin + newWidth;

            final double newYMin = anchorY - relativeY * newHeight;
            final double newYMax = newYMin + newHeight;

            return new WorldView(newXMin, newXMax, newYMin, newYMax);
        }

        /**
         * Returns a new view that expands the bounds by the given factor on all sides.
         *
         * @param factor padding factor relative to current width/height (e.g. {@code 0.1} adds 10%)
         * @return padded view
         */
        public WorldView pad(final double factor) {
            final double width = xMax - xMin;
            final double height = yMax - yMin;
            return new WorldView(
                    xMin - width * factor,
                    xMax + width * factor,
                    yMin - height * factor,
                    yMax + height * factor
            );
        }

        /**
         * Returns a new view that exactly matches the canvas aspect ratio by adjusting only y-bounds.
         * <p>
         * This keeps the x-range intact and centers y around its midpoint.
         *
         * @param canvasWidth  canvas width in pixels
         * @param canvasHeight canvas height in pixels
         * @return aspect-locked view or {@code this} if canvas size is not valid
         */
        public WorldView lockAspectExact(final double canvasWidth, final double canvasHeight) {
            if (!(canvasWidth > 0) || !(canvasHeight > 0)) {
                return this;
            }

            final double xRange = xMax - xMin;
            final double centerY = (yMin + yMax) / 2.0;

            final double desiredYRange = xRange * canvasHeight / canvasWidth;
            final double newYMin = centerY - desiredYRange / 2.0;
            final double newYMax = centerY + desiredYRange / 2.0;

            return new WorldView(xMin, xMax, newYMin, newYMax);
        }

        /**
         * Returns a new view that preserves aspect ratio while only expanding bounds if needed.
         * <p>
         * If the current view is too narrow for the canvas ratio, y-range is expanded. Otherwise, x-range is
         * expanded. No shrinking occurs.
         *
         * @param canvasWidth  canvas width in pixels
         * @param canvasHeight canvas height in pixels
         * @return aspect-expanded view or {@code this} if canvas size is not valid
         */
        public WorldView lockAspectExpandOnly(final double canvasWidth, final double canvasHeight) {
            if (!(canvasWidth > 0) || !(canvasHeight > 0)) {
                return this;
            }

            final double xRange = xMax - xMin;
            final double yRange = yMax - yMin;

            final double desiredYRangeFromX = xRange * canvasHeight / canvasWidth;
            final double desiredXRangeFromY = yRange * canvasWidth / canvasHeight;

            final double centerX = (xMin + xMax) / 2.0;
            final double centerY = (yMin + yMax) / 2.0;

            if (desiredYRangeFromX >= yRange) {
                final double newYMin = centerY - desiredYRangeFromX / 2.0;
                final double newYMax = centerY + desiredYRangeFromX / 2.0;
                return new WorldView(xMin, xMax, newYMin, newYMax);
            }

            final double newXMin = centerX - desiredXRangeFromY / 2.0;
            final double newXMax = centerX + desiredXRangeFromY / 2.0;
            return new WorldView(newXMin, newXMax, yMin, yMax);
        }
    }

    /**
     * Binds the plot canvas size to the size of this container.
     * <p>
     * This ensures the canvas always matches the available UI space.
     */
    private void bindCanvasToContainer() {
        plotCanvas.widthProperty().bind(widthProperty());
        plotCanvas.heightProperty().bind(heightProperty());
    }

    /**
     * Installs listeners that react to canvas size changes.
     * <p>
     * On resize, the view is adjusted to preserve aspect ratio and a render is scheduled.
     */
    private void installResizeHandlers() {
        final ChangeListener<Number> resizeListener = (obs, oldValue, newValue) -> {
            enforceAspectExpandOnly();
            scheduleRender();
        };
        plotCanvas.widthProperty().addListener(resizeListener);
        plotCanvas.heightProperty().addListener(resizeListener);
    }

    /**
     * Configures the render debounce transition.
     * <p>
     * The debounce prevents frequent expensive renders when multiple changes occur in quick succession.
     */
    private void configureRenderDebounce() {
        renderDebounceTransition.setOnFinished(event -> renderImmediately());
    }

    /**
     * Installs listeners on the model and settings that trigger renders when relevant state changes.
     */
    private void installModelListeners() {
        model.revisionProperty().addListener((obs, oldRevision, newRevision) -> scheduleRender());

        model.getSettings().showGridProperty().addListener((obs, o, n) -> renderImmediately());
        model.getSettings().showAxesProperty().addListener((obs, o, n) -> renderImmediately());
        model.getSettings().targetGridLinesProperty().addListener((obs, o, n) -> renderImmediately());
    }

    /**
     * Installs mouse handlers for coordinate display, zooming, panning and tool interactions.
     */
    private void installMouseHandlers() {
        setFocusTraversable(true);

        addEventHandler(MouseEvent.MOUSE_MOVED, event -> notifyCursorMoved(event.getX(), event.getY()));
        addEventHandler(ScrollEvent.SCROLL, this::handleScrollZoom);

        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> handleMousePressed(event.getX(), event.getY(), event.isPrimaryButtonDown()));
        addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> handleMouseDragged(event.getX(), event.getY(), event.isPrimaryButtonDown()));
        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> handleMouseReleased(event.getX(), event.getY()));
    }

    /**
     * Installs keyboard shortcuts (undo/redo, fit-to-data).
     */
    private void installKeyHandlers() {
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN).match(event)) {
                undoView();
                event.consume();
                return;
            }

            if (new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN).match(event)) {
                redoView();
                event.consume();
                return;
            }

            if (event.getCode() == KeyCode.F) {
                fitToData();
                event.consume();
            }
        });
    }

    /**
     * Sends a world-coordinate cursor update to the registered status listener, if present.
     *
     * @param screenX x-position in screen coordinates (canvas pixels)
     * @param screenY y-position in screen coordinates (canvas pixels)
     */
    private void notifyCursorMoved(final double screenX, final double screenY) {
        final StatusListener listener = cursorStatusListener;
        if (listener == null) {
            return;
        }

        final double worldX = screenToWorldX(screenX);
        final double worldY = screenToWorldY(screenY);
        listener.onCursorMoved(worldX, worldY);
    }

    /**
     * Handles scroll-wheel zooming around the cursor position.
     * <p>
     * The previous view is pushed onto the undo stack.
     *
     * @param event scroll event
     */
    private void handleScrollZoom(@NonNull final ScrollEvent event) {
        if (event.getDeltaY() == 0) {
            return;
        }

        final double zoomFactor = event.getDeltaY() > 0 ? ZOOM_FACTOR_IN : ZOOM_FACTOR_OUT;

        final double anchorWorldX = screenToWorldX(event.getX());
        final double anchorWorldY = screenToWorldY(event.getY());

        pushUndoView();
        worldView = worldView.zoom(anchorWorldX, anchorWorldY, zoomFactor);
        enforceAspectExact();
        redoStack.clear();
        renderImmediately();
    }

    /**
     * Handles mouse press events and dispatches to the active tool mode.
     *
     * @param screenX           x-position in screen coordinates
     * @param screenY           y-position in screen coordinates
     * @param primaryButtonDown whether the primary button is pressed
     */
    private void handleMousePressed(final double screenX, final double screenY, final boolean primaryButtonDown) {
        requestFocus();

        dragStartScreenX = screenX;
        dragStartScreenY = screenY;
        isDragging = true;

        if (activeToolMode == ToolMode.ZOOM_BOX) {
            beginZoomBox(screenX, screenY);
            return;
        }

        if (activeToolMode == ToolMode.INTEGRAL) {
            beginIntegralSelection(screenX, screenY);
            return;
        }

        if (activeToolMode != ToolMode.MOVE && primaryButtonDown) {
            handleToolClick(screenX, screenY);
        }
    }

    /**
     * Handles mouse drag events for panning and zoom-box drawing.
     *
     * @param screenX           x-position in screen coordinates
     * @param screenY           y-position in screen coordinates
     * @param primaryButtonDown whether the primary button is pressed
     */
    private void handleMouseDragged(final double screenX, final double screenY, final boolean primaryButtonDown) {
        if (!isDragging) {
            return;
        }

        if (activeToolMode == ToolMode.ZOOM_BOX) {
            updateZoomBox(screenX, screenY);
            return;
        }

        if (activeToolMode == ToolMode.MOVE && primaryButtonDown) {
            panViewByDragDelta(screenX, screenY);
        }
    }

    /**
     * Handles mouse release events, finalizing operations like zoom-box and integral selection.
     *
     * @param screenX x-position in screen coordinates
     * @param screenY y-position in screen coordinates
     */
    private void handleMouseReleased(final double screenX, final double screenY) {
        isDragging = false;

        if (activeToolMode == ToolMode.ZOOM_BOX) {
            applyZoomBoxIfValid();
            zoomBoxScreenWidth = 0;
            zoomBoxScreenHeight = 0;
            renderImmediately();
            return;
        }

        if (activeToolMode == ToolMode.INTEGRAL && integralStartX != null && integralFunctionId != null) {
            finalizeIntegral(BigDecimal.valueOf(screenToWorldX(screenX)));
            integralStartX = null;
            integralFunctionId = null;
            renderImmediately();
        }
    }

    /**
     * Initializes zoom-box tracking starting at the given screen position.
     *
     * @param screenX starting x in screen coordinates
     * @param screenY starting y in screen coordinates
     */
    private void beginZoomBox(final double screenX, final double screenY) {
        zoomBoxScreenX = screenX;
        zoomBoxScreenY = screenY;
        zoomBoxScreenWidth = 0;
        zoomBoxScreenHeight = 0;
        renderImmediately();
    }

    /**
     * Updates the zoom-box rectangle based on current mouse position.
     *
     * @param currentScreenX current x in screen coordinates
     * @param currentScreenY current y in screen coordinates
     */
    private void updateZoomBox(final double currentScreenX, final double currentScreenY) {
        zoomBoxScreenX = Math.min(dragStartScreenX, currentScreenX);
        zoomBoxScreenY = Math.min(dragStartScreenY, currentScreenY);
        zoomBoxScreenWidth = Math.abs(currentScreenX - dragStartScreenX);
        zoomBoxScreenHeight = Math.abs(currentScreenY - dragStartScreenY);
        renderImmediately();
    }

    /**
     * Starts integral selection by picking a function under the cursor and storing the start x-value.
     *
     * @param screenX x-position in screen coordinates
     * @param screenY y-position in screen coordinates
     */
    private void beginIntegralSelection(final double screenX, final double screenY) {
        final GraphFxFunction pickedFunction = pickFunction(screenX, screenY);
        if (pickedFunction == null) {
            return;
        }

        integralFunctionId = pickedFunction.getId();
        integralStartX = BigDecimal.valueOf(screenToWorldX(screenX));
    }

    /**
     * Applies the current zoom-box to the viewport if the box is large enough to be meaningful.
     * <p>
     * The previous view is pushed onto the undo stack. Aspect ratio is preserved by expanding bounds
     * as needed.
     */
    private void applyZoomBoxIfValid() {
        if (zoomBoxScreenWidth < 10 || zoomBoxScreenHeight < 10) {
            return;
        }

        final double worldX1 = screenToWorldX(zoomBoxScreenX);
        final double worldX2 = screenToWorldX(zoomBoxScreenX + zoomBoxScreenWidth);

        final double worldY1 = screenToWorldY(zoomBoxScreenY + zoomBoxScreenHeight);
        final double worldY2 = screenToWorldY(zoomBoxScreenY);

        pushUndoView();
        worldView = new WorldView(
                Math.min(worldX1, worldX2),
                Math.max(worldX1, worldX2),
                Math.min(worldY1, worldY2),
                Math.max(worldY1, worldY2)
        );

        enforceAspectExpandOnly();
        redoStack.clear();
    }

    /**
     * Pans the viewport according to the delta between the last drag position and the new one.
     * <p>
     * The previous view is pushed onto the undo stack to support incremental undo while dragging.
     *
     * @param currentScreenX current x in screen coordinates
     * @param currentScreenY current y in screen coordinates
     */
    private void panViewByDragDelta(final double currentScreenX, final double currentScreenY) {
        final double deltaScreenX = currentScreenX - dragStartScreenX;
        final double deltaScreenY = currentScreenY - dragStartScreenY;

        final double worldDeltaX = -deltaScreenX * (worldView.xMax() - worldView.xMin()) / Math.max(1, plotCanvas.getWidth());
        final double worldDeltaY = deltaScreenY * (worldView.yMax() - worldView.yMin()) / Math.max(1, plotCanvas.getHeight());

        pushUndoView();
        worldView = worldView.pan(worldDeltaX, worldDeltaY);
        enforceAspectExact();
        redoStack.clear();

        dragStartScreenX = currentScreenX;
        dragStartScreenY = currentScreenY;

        renderImmediately();
    }

    /**
     * Undoes the last viewport change if available.
     * <p>
     * The current view is pushed onto the redo stack.
     */
    private void undoView() {
        if (undoStack.isEmpty()) {
            return;
        }
        redoStack.push(worldView);
        worldView = undoStack.pop();
        enforceAspectExact();
        renderImmediately();
    }

    /**
     * Redoes the last undone viewport change if available.
     * <p>
     * The current view is pushed onto the undo stack.
     */
    private void redoView() {
        if (redoStack.isEmpty()) {
            return;
        }
        undoStack.push(worldView);
        worldView = redoStack.pop();
        enforceAspectExact();
        renderImmediately();
    }

    /**
     * Pushes the current view onto the undo stack and keeps the stack size bounded.
     * <p>
     * If the stack exceeds {@link #MAX_UNDO_ENTRIES}, it is shrunk down to {@link #UNDO_SHRINK_TO}.
     */
    private void pushUndoView() {
        undoStack.push(worldView);

        if (undoStack.size() <= MAX_UNDO_ENTRIES) {
            return;
        }

        while (undoStack.size() > UNDO_SHRINK_TO) {
            undoStack.removeLast();
        }
    }

    /**
     * Schedules a render using a debounce timer.
     * <p>
     * Multiple calls within a short time window collapse into a single render, improving performance.
     */
    private void scheduleRender() {
        renderDebounceTransition.playFromStart();
    }

    /**
     * Performs an immediate render and schedules re-sampling of visible functions if needed.
     * <p>
     * This method marks all function caches dirty to ensure consistent visuals after major changes.
     */
    private void renderImmediately() {
        for (final GraphFxFunction function : model.getFunctions()) {
            functionCacheById.computeIfAbsent(function.getId(), id -> new FunctionCache()).markDirty();
        }

        render();
        resampleFunctionsIfNeeded();
    }

    /**
     * Renders the entire scene (background, grid, axes, functions, objects, and tool overlays).
     */
    private void render() {
        final GraphicsContext graphics = plotCanvas.getGraphicsContext2D();
        final double canvasWidth = plotCanvas.getWidth();
        final double canvasHeight = plotCanvas.getHeight();

        graphics.setFill(Color.WHITE);
        graphics.fillRect(0, 0, canvasWidth, canvasHeight);

        graphics.setFont(Font.font(13));

        if (model.getSettings().isShowGrid()) {
            drawGrid(graphics);
        }

        if (model.getSettings().isShowAxes()) {
            drawAxes(graphics);
            drawAxisTicks(graphics);
        }

        drawFunctions(graphics);
        drawObjects(graphics);
        drawToolOverlays(graphics);
    }

    /**
     * Draws the currently active tool overlay (e.g., zoom-box rectangle).
     *
     * @param graphics graphics context to draw on
     */
    private void drawToolOverlays(@NonNull final GraphicsContext graphics) {
        if (activeToolMode != ToolMode.ZOOM_BOX) {
            return;
        }

        if (zoomBoxScreenWidth <= 4 || zoomBoxScreenHeight <= 4) {
            return;
        }

        graphics.setFill(Color.rgb(60, 130, 255, 0.18));
        graphics.fillRect(zoomBoxScreenX, zoomBoxScreenY, zoomBoxScreenWidth, zoomBoxScreenHeight);

        graphics.setStroke(Color.rgb(60, 130, 255, 0.9));
        graphics.setLineWidth(1.4);
        graphics.strokeRect(zoomBoxScreenX, zoomBoxScreenY, zoomBoxScreenWidth, zoomBoxScreenHeight);
    }

    /**
     * Draws the background grid (minor and major lines).
     *
     * @param graphics graphics context to draw on
     */
    private void drawGrid(@NonNull final GraphicsContext graphics) {
        final double canvasWidth = plotCanvas.getWidth();
        final double canvasHeight = plotCanvas.getHeight();

        final double majorStep = GraphFxNiceTicks.niceStep(worldView.xMin(), worldView.xMax(), model.getSettings().getTargetGridLines());
        final double minorStep = majorStep / 5.0;

        drawGridLines(graphics, canvasWidth, canvasHeight, minorStep, Color.rgb(235, 235, 235), 1.0);
        drawGridLines(graphics, canvasWidth, canvasHeight, majorStep, Color.rgb(210, 210, 210), 1.0);
    }

    /**
     * Draws grid lines for the given step size.
     *
     * @param graphics     graphics context
     * @param canvasWidth  canvas width in pixels
     * @param canvasHeight canvas height in pixels
     * @param step         distance between lines in world coordinates
     * @param color        line color
     * @param lineWidth    stroke width in pixels
     */
    private void drawGridLines(
            @NonNull final GraphicsContext graphics,
            final double canvasWidth,
            final double canvasHeight,
            final double step,
            @NonNull final Color color,
            final double lineWidth
    ) {
        if (!(step > 0) || !Double.isFinite(step)) {
            return;
        }

        graphics.setStroke(color);
        graphics.setLineWidth(lineWidth);

        final double xStart = Math.floor(worldView.xMin() / step) * step;
        for (double x = xStart; x <= worldView.xMax(); x += step) {
            final double screenX = worldToScreenX(x);
            graphics.strokeLine(screenX, 0, screenX, canvasHeight);
        }

        final double yStart = Math.floor(worldView.yMin() / step) * step;
        for (double y = yStart; y <= worldView.yMax(); y += step) {
            final double screenY = worldToScreenY(y);
            graphics.strokeLine(0, screenY, canvasWidth, screenY);
        }
    }

    /**
     * Draws x- and y-axis lines if the world view contains 0 on the respective axis.
     *
     * @param graphics graphics context
     */
    private void drawAxes(@NonNull final GraphicsContext graphics) {
        final double canvasWidth = plotCanvas.getWidth();
        final double canvasHeight = plotCanvas.getHeight();

        graphics.setStroke(Color.rgb(120, 120, 120));
        graphics.setLineWidth(1.8);

        if (worldView.containsX(0)) {
            final double xAxisScreenX = worldToScreenX(0);
            graphics.strokeLine(xAxisScreenX, 0, xAxisScreenX, canvasHeight);
        }

        if (worldView.containsY(0)) {
            final double yAxisScreenY = worldToScreenY(0);
            graphics.strokeLine(0, yAxisScreenY, canvasWidth, yAxisScreenY);
        }
    }

    /**
     * Draws tick marks and numeric labels along the axes.
     *
     * @param graphics graphics context
     */
    private void drawAxisTicks(@NonNull final GraphicsContext graphics) {
        final double canvasHeight = plotCanvas.getHeight();

        final double step = GraphFxNiceTicks.niceStep(worldView.xMin(), worldView.xMax(), model.getSettings().getTargetGridLines());
        if (!(step > 0) || !Double.isFinite(step)) {
            return;
        }

        graphics.setStroke(Color.rgb(110, 110, 110));
        graphics.setFill(Color.rgb(90, 90, 90));
        graphics.setLineWidth(1.0);

        final double yAxisScreenX = worldView.containsX(0) ? worldToScreenX(0) : 50;
        final double xAxisScreenY = worldView.containsY(0) ? worldToScreenY(0) : canvasHeight - 35;

        final double xStart = Math.floor(worldView.xMin() / step) * step;
        for (double x = xStart; x <= worldView.xMax(); x += step) {
            final double screenX = worldToScreenX(x);
            graphics.strokeLine(screenX, xAxisScreenY - 4, screenX, xAxisScreenY + 4);
            graphics.fillText(axisLabelFormat.format(x), screenX + 4, xAxisScreenY + 18);
        }

        final double yStart = Math.floor(worldView.yMin() / step) * step;
        for (double y = yStart; y <= worldView.yMax(); y += step) {
            final double screenY = worldToScreenY(y);
            graphics.strokeLine(yAxisScreenX - 4, screenY, yAxisScreenX + 4, screenY);
            graphics.fillText(axisLabelFormat.format(y), yAxisScreenX + 8, screenY - 5);
        }
    }

    /**
     * Draws all visible functions using their cached polylines (if available).
     *
     * @param graphics graphics context
     */
    private void drawFunctions(@NonNull final GraphicsContext graphics) {
        for (final GraphFxFunction function : model.getFunctions()) {
            if (!function.isVisible()) {
                continue;
            }

            final FunctionCache cache = functionCacheById.computeIfAbsent(function.getId(), id -> new FunctionCache());
            final GraphPolyline polyline = cache.lastPolyline;

            graphics.setStroke(function.getColor());
            graphics.setLineWidth(function.getStrokeWidth());

            if (polyline == null) {
                continue;
            }

            for (final List<GraphPoint> segment : polyline.segments) {
                drawPolylineSegment(graphics, segment);
            }
        }
    }

    /**
     * Draws all visible analysis objects (points, lines, integrals).
     *
     * @param graphics graphics context
     */
    private void drawObjects(@NonNull final GraphicsContext graphics) {
        for (final GraphFxObject object : model.getObjects()) {
            if (!object.visible()) {
                continue;
            }

            if (object instanceof GraphFxPointObject point) {
                drawPointObject(graphics, point);
            }

            if (object instanceof GraphFxLineObject line) {
                drawLineObject(graphics, line);
            }

            if (object instanceof GraphFxIntegralObject integral) {
                drawIntegralObject(graphics, integral);
            }
        }
    }

    /**
     * Draws a point object (marker and name label).
     *
     * @param graphics graphics context
     * @param point    point object to draw
     */
    private void drawPointObject(@NonNull final GraphicsContext graphics, @NonNull final GraphFxPointObject point) {
        graphics.setFill(point.style().colorWithAlpha());

        final double screenX = worldToScreenX(point.x().doubleValue());
        final double screenY = worldToScreenY(point.y().doubleValue());

        graphics.fillOval(screenX - 4.5, screenY - 4.5, 9, 9);

        graphics.setFill(Color.rgb(60, 60, 60));
        graphics.fillText(point.name(), screenX + 10, screenY - 8);
    }

    /**
     * Draws a line object across the full visible x-range.
     *
     * @param graphics graphics context
     * @param line     line object to draw
     */
    private void drawLineObject(@NonNull final GraphicsContext graphics, @NonNull final GraphFxLineObject line) {
        final double xMin = worldView.xMin();
        final double xMax = worldView.xMax();

        final double x0 = line.x0().doubleValue();
        final double y0 = line.y0().doubleValue();
        final double slope = line.slope().doubleValue();

        final double yAtLeft = slope * (xMin - x0) + y0;
        final double yAtRight = slope * (xMax - x0) + y0;

        graphics.setStroke(line.style().colorWithAlpha());
        graphics.setLineWidth(line.style().strokeWidth());
        graphics.strokeLine(
                worldToScreenX(xMin),
                worldToScreenY(yAtLeft),
                worldToScreenX(xMax),
                worldToScreenY(yAtRight)
        );
    }

    /**
     * Draws an integral object as a filled polygon between the function and the x-axis.
     * <p>
     * This uses a simple sampling approach for visualization; the numeric value is taken from the object.
     *
     * @param graphics graphics context
     * @param integral integral object to draw
     */
    private void drawIntegralObject(@NonNull final GraphicsContext graphics, @NonNull final GraphFxIntegralObject integral) {
        final GraphFxFunction function = model.getFunctions().stream()
                .filter(f -> f.getId().equals(integral.functionId()))
                .findFirst()
                .orElse(null);

        if (function == null) {
            return;
        }

        final BigDecimal a = integral.a();
        final BigDecimal b = integral.b();
        final BigDecimal min = a.min(b);
        final BigDecimal max = a.max(b);

        final int fillSamples = isInteractiveModeEnabled ? INTEGRAL_FILL_SAMPLES_INTERACTIVE : INTEGRAL_FILL_SAMPLES_QUALITY;
        final BigDecimal step = max.subtract(min, MATH_CONTEXT).divide(BigDecimal.valueOf(fillSamples), MATH_CONTEXT);
        final Map<String, String> variables = model.variablesAsStringMap();

        graphics.setFill(integral.style().colorWithAlpha());

        final List<Double> polygonXs = new ArrayList<>();
        final List<Double> polygonYs = new ArrayList<>();

        polygonXs.add(worldToScreenX(min.doubleValue()));
        polygonYs.add(worldToScreenY(0));

        for (int i = 0; i <= fillSamples; i++) {
            final BigDecimal x = (i == fillSamples)
                    ? max
                    : min.add(step.multiply(BigDecimal.valueOf(i), MATH_CONTEXT), MATH_CONTEXT);

            final Double y = graphFxAnalysisMath.evalY(calculatorEngine, function.getExpression(), variables, x);
            if (y == null) {
                continue;
            }

            polygonXs.add(worldToScreenX(x.doubleValue()));
            polygonYs.add(worldToScreenY(y));
        }

        polygonXs.add(worldToScreenX(max.doubleValue()));
        polygonYs.add(worldToScreenY(0));

        if (polygonXs.size() >= 4) {
            final double[] xa = polygonXs.stream().mapToDouble(Double::doubleValue).toArray();
            final double[] ya = polygonYs.stream().mapToDouble(Double::doubleValue).toArray();
            graphics.fillPolygon(xa, ya, xa.length);
        }

        graphics.setFill(Color.rgb(55, 55, 55));
        graphics.fillText(
                "∫ = " + integral.value().stripTrailingZeros().toPlainString(),
                worldToScreenX(min.doubleValue()) + 10,
                worldToScreenY(0) - 10
        );
    }

    /**
     * Draws a polyline segment given a list of points.
     *
     * @param graphics graphics context
     * @param points   contiguous segment points
     */
    private void drawPolylineSegment(@NonNull final GraphicsContext graphics, @NonNull final List<GraphPoint> points) {
        if (points.size() < 2) {
            return;
        }

        final GraphPoint firstPoint = points.getFirst();
        graphics.beginPath();
        graphics.moveTo(worldToScreenX(firstPoint.getX()), worldToScreenY(firstPoint.getY()));

        for (int i = 1; i < points.size(); i++) {
            final GraphPoint point = points.get(i);
            graphics.lineTo(worldToScreenX(point.getX()), worldToScreenY(point.getY()));
        }

        graphics.stroke();
    }

    /**
     * Schedules sampling tasks for visible functions whose caches are outdated.
     * <p>
     * Sampling is performed on a background executor. Only the most recent scheduled token per function
     * is allowed to publish results to the UI thread.
     */
    private void resampleFunctionsIfNeeded() {
        final long currentRevision = model.revisionProperty().get();
        final WorldView viewSnapshot = worldView;
        final Map<String, String> variablesSnapshot = model.variablesAsStringMap();

        final int desiredSamples = isInteractiveModeEnabled
                ? Math.max(MIN_INTERACTIVE_SAMPLES, (int) plotCanvas.getWidth())
                : Math.max(MIN_QUALITY_SAMPLES, (int) plotCanvas.getWidth() * 2);

        for (final GraphFxFunction function : model.getFunctions()) {
            if (!function.isVisible()) {
                continue;
            }

            final FunctionCache cache = functionCacheById.computeIfAbsent(function.getId(), id -> new FunctionCache());
            if (!cache.shouldSchedule(currentRevision, viewSnapshot, isInteractiveModeEnabled)) {
                continue;
            }

            cache.cancelScheduledSampling();

            final long token = cache.token.incrementAndGet();
            final long delayMillis = isInteractiveModeEnabled ? SAMPLE_DELAY_INTERACTIVE_MS : SAMPLE_DELAY_QUALITY_MS;

            cache.scheduledSampling = samplingExecutor.schedule(() -> {
                final GraphPolyline polyline = sampleFunctionPolyline(function, viewSnapshot, variablesSnapshot, desiredSamples);

                Platform.runLater(() -> {
                    if (token != cache.token.get()) {
                        return;
                    }

                    cache.lastPolyline = polyline;
                    cache.lastRevision = currentRevision;
                    cache.lastView = viewSnapshot;
                    cache.lastInteractive = isInteractiveModeEnabled;

                    render();
                });
            }, delayMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Samples the given function across the current view's x-range and creates a polyline representation.
     * <p>
     * The polyline is split into segments whenever the function becomes undefined, non-finite, exceeds a large
     * threshold, or a large discontinuity between adjacent samples is detected.
     *
     * @param function  function to sample
     * @param view      view snapshot to sample within
     * @param variables variable map snapshot used for evaluation
     * @param samples   number of samples across the x-range
     * @return sampled polyline with one or more segments
     */
    private GraphPolyline sampleFunctionPolyline(
            @NonNull final GraphFxFunction function,
            @NonNull final WorldView view,
            @NonNull final Map<String, String> variables,
            final int samples
    ) {
        final BigDecimal xMin = BigDecimal.valueOf(view.xMin());
        final BigDecimal xMax = BigDecimal.valueOf(view.xMax());
        final BigDecimal step = xMax.subtract(xMin, MATH_CONTEXT).divide(BigDecimal.valueOf(samples), MATH_CONTEXT);

        final List<List<GraphPoint>> segments = new ArrayList<>();
        final List<GraphPoint> currentSegment = new ArrayList<>();

        Double lastY = null;

        for (int i = 0; i <= samples; i++) {
            final BigDecimal x = (i == samples)
                    ? xMax
                    : xMin.add(step.multiply(BigDecimal.valueOf(i), MATH_CONTEXT), MATH_CONTEXT);

            final Double y = graphFxAnalysisMath.evalY(calculatorEngine, function.getExpression(), variables, x);

            if (y == null || !Double.isFinite(y) || Math.abs(y) > POLYLINE_Y_ABS_LIMIT) {
                flushSegment(segments, currentSegment);
                lastY = null;
                continue;
            }

            if (lastY != null && Math.abs(y - lastY) > POLYLINE_DISCONTINUITY_Y_JUMP) {
                flushSegment(segments, currentSegment);
            }

            currentSegment.add(new GraphPoint(x.doubleValue(), y));
            lastY = y;
        }

        flushSegment(segments, currentSegment);
        return new GraphPolyline(segments);
    }

    /**
     * Flushes the current segment into the segment list if it contains at least 2 points, then clears it.
     *
     * @param segments       list of completed segments
     * @param currentSegment currently collected segment points
     */
    private static void flushSegment(@NonNull final List<List<GraphPoint>> segments, @NonNull final List<GraphPoint> currentSegment) {
        if (currentSegment.size() >= 2) {
            segments.add(List.copyOf(currentSegment));
        }
        currentSegment.clear();
    }

    /**
     * Dispatches a click to the currently active analysis tool (except MOVE/ZOOM_BOX/INTEGRAL which have special flows).
     *
     * @param screenX x-position in screen coordinates
     * @param screenY y-position in screen coordinates
     */
    private void handleToolClick(final double screenX, final double screenY) {
        final GraphFxFunction function = pickFunction(screenX, screenY);
        if (function == null) {
            return;
        }

        model.setSelectedFunction(function);

        final BigDecimal worldX = BigDecimal.valueOf(screenToWorldX(screenX));
        final Map<String, String> variables = model.variablesAsStringMap();

        if (activeToolMode == ToolMode.POINT_ON_FUNCTION) {
            final Double y = graphFxAnalysisMath.evalY(calculatorEngine, function.getExpression(), variables, worldX);
            if (y == null) {
                return;
            }
            model.addObject(GraphFxPointObject.of("P", worldX, BigDecimal.valueOf(y), function.getId()));
            return;
        }

        if (activeToolMode == ToolMode.TANGENT || activeToolMode == ToolMode.NORMAL) {
            final Double y = graphFxAnalysisMath.evalY(calculatorEngine, function.getExpression(), variables, worldX);
            final BigDecimal slope = graphFxAnalysisMath.derivative(calculatorEngine, function.getExpression(), variables, worldX);

            if (y == null || slope == null) {
                return;
            }

            final BigDecimal lineSlope = activeToolMode == ToolMode.TANGENT ? slope : invertSlope(slope);
            if (lineSlope == null) {
                return;
            }

            model.addObject(GraphFxLineObject.of(activeToolMode == ToolMode.TANGENT ? "t" : "n", worldX, BigDecimal.valueOf(y), lineSlope));
            return;
        }

        if (activeToolMode == ToolMode.ROOT) {
            final BigDecimal xRange = BigDecimal.valueOf(worldView.xMax() - worldView.xMin());
            final BigDecimal left = worldX.subtract(xRange.multiply(new BigDecimal("0.08")), MATH_CONTEXT);
            final BigDecimal right = worldX.add(xRange.multiply(new BigDecimal("0.08")), MATH_CONTEXT);

            final List<BigDecimal> roots = graphFxAnalysisMath.rootsInRange(
                    calculatorEngine,
                    function.getExpression(),
                    variables,
                    left,
                    right,
                    ROOT_SEARCH_STEPS
            );

            if (roots.isEmpty()) {
                return;
            }

            final BigDecimal root = closestTo(roots, worldX);
            model.addObject(GraphFxPointObject.of("x₀", root, BigDecimal.ZERO, function.getId()));
            return;
        }

        if (activeToolMode == ToolMode.INTERSECTION) {
            handleIntersectionClick(function, variables);
        }
    }

    /**
     * Handles the INTERSECTION tool flow:
     * <ul>
     *     <li>First click selects the first function id.</li>
     *     <li>Second click on a different function computes intersection x-values in the current x-range and adds points.</li>
     * </ul>
     *
     * @param secondFunction function clicked on (candidate for first or second selection)
     * @param variables      current variable map
     */
    private void handleIntersectionClick(@NonNull final GraphFxFunction secondFunction, @NonNull final Map<String, String> variables) {
        if (intersectionFirstFunctionId == null) {
            intersectionFirstFunctionId = secondFunction.getId();
            return;
        }

        if (intersectionFirstFunctionId.equals(secondFunction.getId())) {
            return;
        }

        final GraphFxFunction firstFunction = model.getFunctions().stream()
                .filter(f -> f.getId().equals(intersectionFirstFunctionId))
                .findFirst()
                .orElse(null);

        if (firstFunction == null) {
            intersectionFirstFunctionId = null;
            return;
        }

        final BigDecimal xMin = BigDecimal.valueOf(worldView.xMin());
        final BigDecimal xMax = BigDecimal.valueOf(worldView.xMax());

        final List<BigDecimal> intersectionXs = graphFxAnalysisMath.intersectionsInRange(
                calculatorEngine,
                firstFunction.getExpression(),
                secondFunction.getExpression(),
                variables,
                xMin,
                xMax,
                INTERSECTION_SEARCH_STEPS
        );

        for (final BigDecimal x : intersectionXs) {
            final Double y = graphFxAnalysisMath.evalY(calculatorEngine, firstFunction.getExpression(), variables, x);
            if (y == null) {
                continue;
            }
            model.addObject(GraphFxPointObject.of("S", x, BigDecimal.valueOf(y), null));
        }

        intersectionFirstFunctionId = null;
    }

    /**
     * Finalizes an integral selection by computing its value and adding a {@link GraphFxIntegralObject} to the model.
     * <p>
     * The integral is evaluated numerically using Simpson's rule via {@link GraphFxAnalysisMath}.
     *
     * @param endX world x-coordinate where the selection ends
     */
    private void finalizeIntegral(@NonNull final BigDecimal endX) {
        final GraphFxFunction function = model.getFunctions().stream()
                .filter(f -> f.getId().equals(integralFunctionId))
                .findFirst()
                .orElse(null);

        if (function == null) {
            return;
        }

        final BigDecimal a = integralStartX;
        final BigDecimal b = endX;

        final int steps = isInteractiveModeEnabled ? INTEGRAL_STEPS_INTERACTIVE : INTEGRAL_STEPS_QUALITY;

        final BigDecimal value = graphFxAnalysisMath.integralSimpson(
                calculatorEngine,
                function.getExpression(),
                model.variablesAsStringMap(),
                a,
                b,
                steps
        );

        if (value == null) {
            return;
        }

        model.addObject(GraphFxIntegralObject.of(function.getId(), a, b, value, function.getColor()));
    }

    /**
     * Picks the nearest visible function under the given screen coordinate by evaluating y at the clicked world x
     * and measuring screen-space distance to the cursor.
     *
     * @param screenX x-position in screen coordinates
     * @param screenY y-position in screen coordinates
     * @return nearest function if within {@link #FUNCTION_PICK_DISTANCE_PX}, otherwise {@code null}
     */
    private GraphFxFunction pickFunction(final double screenX, final double screenY) {
        final double worldX = screenToWorldX(screenX);
        final Map<String, String> variables = model.variablesAsStringMap();

        GraphFxFunction bestFunction = null;
        double bestScreenDistance = Double.POSITIVE_INFINITY;

        for (final GraphFxFunction function : model.getFunctions()) {
            if (!function.isVisible()) {
                continue;
            }

            final Double functionY = graphFxAnalysisMath.evalY(
                    calculatorEngine,
                    function.getExpression(),
                    variables,
                    BigDecimal.valueOf(worldX)
            );

            if (functionY == null) {
                continue;
            }

            final double functionScreenY = worldToScreenY(functionY);
            final double distance = Math.abs(functionScreenY - screenY);

            if (distance < bestScreenDistance) {
                bestScreenDistance = distance;
                bestFunction = function;
            }
        }

        return bestScreenDistance <= FUNCTION_PICK_DISTANCE_PX ? bestFunction : null;
    }

    /**
     * Converts a world x-coordinate to a screen x-coordinate.
     *
     * @param x world x-coordinate
     * @return screen x-coordinate in pixels
     */
    private double worldToScreenX(final double x) {
        final double t = (x - worldView.xMin()) / (worldView.xMax() - worldView.xMin());
        return t * plotCanvas.getWidth();
    }

    /**
     * Converts a world y-coordinate to a screen y-coordinate.
     *
     * @param y world y-coordinate
     * @return screen y-coordinate in pixels
     */
    private double worldToScreenY(final double y) {
        final double t = (y - worldView.yMin()) / (worldView.yMax() - worldView.yMin());
        return plotCanvas.getHeight() - (t * plotCanvas.getHeight());
    }

    /**
     * Converts a screen x-coordinate to a world x-coordinate.
     *
     * @param screenX screen x-coordinate in pixels
     * @return world x-coordinate
     */
    private double screenToWorldX(final double screenX) {
        final double t = screenX / Math.max(1, plotCanvas.getWidth());
        return worldView.xMin() + t * (worldView.xMax() - worldView.xMin());
    }

    /**
     * Converts a screen y-coordinate to a world y-coordinate.
     *
     * @param screenY screen y-coordinate in pixels
     * @return world y-coordinate
     */
    private double screenToWorldY(final double screenY) {
        final double t = (plotCanvas.getHeight() - screenY) / Math.max(1, plotCanvas.getHeight());
        return worldView.yMin() + t * (worldView.yMax() - worldView.yMin());
    }

    /**
     * Forces the current view to exactly match the canvas aspect ratio (by adjusting y-range).
     */
    private void enforceAspectExact() {
        worldView = worldView.lockAspectExact(plotCanvas.getWidth(), plotCanvas.getHeight());
    }

    /**
     * Preserves aspect ratio by expanding the view bounds if required (never shrinking).
     */
    private void enforceAspectExpandOnly() {
        worldView = worldView.lockAspectExpandOnly(plotCanvas.getWidth(), plotCanvas.getHeight());
    }

    /**
     * Computes the slope of a normal line from a tangent slope.
     * <p>
     * For a tangent slope {@code m}, the normal slope is {@code -1/m}. If {@code m} is zero, no finite normal
     * exists and {@code null} is returned.
     *
     * @param slope tangent slope
     * @return normal slope or {@code null} if not defined
     */
    private static BigDecimal invertSlope(@NonNull final BigDecimal slope) {
        if (slope.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return BigDecimal.ONE.divide(slope, MATH_CONTEXT).negate();
    }

    /**
     * Returns the value in {@code values} that is closest to {@code target} (by absolute difference).
     *
     * @param values list of values (must contain at least one element)
     * @param target target value
     * @return closest value
     */
    private static BigDecimal closestTo(@NonNull final List<BigDecimal> values, @NonNull final BigDecimal target) {
        BigDecimal best = values.getFirst();
        BigDecimal bestDistance = best.subtract(target).abs();

        for (final BigDecimal value : values) {
            final BigDecimal distance = value.subtract(target).abs();
            if (distance.compareTo(bestDistance) < 0) {
                best = value;
                bestDistance = distance;
            }
        }

        return best;
    }

    /**
     * Creates a daemon thread factory with a stable name prefix.
     *
     * @param threadName name to assign to the created thread
     * @return thread factory
     */
    private static ThreadFactory createThreadFactory(@NonNull final String threadName) {
        return runnable -> {
            final Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Cache container for a single function's sampled polyline plus scheduling metadata.
     * <p>
     * The cache is updated asynchronously and published back onto the JavaFX Application Thread.
     */
    private static final class FunctionCache {

        /**
         * Monotonic token to invalidate older scheduled results when new sampling is started.
         */
        private final AtomicLong token = new AtomicLong(0);

        private volatile boolean isDirty = true;
        private volatile long lastRevision = -1;
        private volatile WorldView lastView;
        private volatile boolean lastInteractive;

        private volatile ScheduledFuture<?> scheduledSampling;
        private volatile GraphPolyline lastPolyline;

        /**
         * Marks this cache as dirty so that sampling will be scheduled on the next render pass.
         */
        void markDirty() {
            isDirty = true;
        }

        /**
         * Determines whether sampling should be scheduled for this cache.
         * <p>
         * Sampling is needed when:
         * <ul>
         *     <li>the cache is dirty,</li>
         *     <li>the model revision changed,</li>
         *     <li>the viewport changed, or</li>
         *     <li>interactive mode changed.</li>
         * </ul>
         *
         * @param revision    current model revision
         * @param view        current viewport snapshot
         * @param interactive whether interactive mode is enabled
         * @return {@code true} if sampling should be scheduled, {@code false} otherwise
         */
        boolean shouldSchedule(final long revision, @NonNull final WorldView view, final boolean interactive) {
            if (isDirty) {
                return true;
            }
            if (lastRevision != revision) {
                return true;
            }
            if (!Objects.equals(lastView, view)) {
                return true;
            }
            return lastInteractive != interactive;
        }

        /**
         * Cancels the currently scheduled sampling task, if any.
         * <p>
         * Cancellation is best-effort; a running task may still complete, but token checks prevent
         * stale results from being applied.
         */
        void cancelScheduledSampling() {
            final ScheduledFuture<?> scheduled = scheduledSampling;
            if (scheduled != null) {
                scheduled.cancel(true);
            }
        }
    }

}
