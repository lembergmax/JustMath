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

import com.mlprograms.justmath.graphfx.config.WindowConfig;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.NonNull;

import java.text.DecimalFormat;

/**
 * A lightweight JavaFX {@link Region} that renders an interactive Cartesian coordinate system onto a {@link Canvas}.
 * <p>
 * The pane supports:
 * </p>
 * <ul>
 *   <li><strong>Panning</strong> by dragging (configurable primary mouse button support, plus middle/right by default)</li>
 *   <li><strong>Zooming</strong> using the mouse wheel (zoom is centered around the cursor position)</li>
 *   <li><strong>Theming</strong> via {@link DisplayTheme} and the corresponding {@link WindowConfig.ThemePalette}</li>
 *   <li><strong>Automatic redraw</strong> when size, scale, or origin offsets change</li>
 * </ul>
 *
 * <h2>Coordinate systems</h2>
 * <p>
 * This class maintains a clear separation between:
 * </p>
 * <ul>
 *   <li><em>World coordinates</em> (mathematical plane; x increases to the right, y increases upwards)</li>
 *   <li><em>Screen coordinates</em> (JavaFX pixel space; x increases to the right, y increases downwards)</li>
 * </ul>
 * <p>
 * The mapping is defined by:
 * </p>
 * <ul>
 *   <li>{@link #scalePxPerUnit} — pixels per world unit</li>
 *   <li>{@link #originOffsetX} / {@link #originOffsetY} — origin position in screen pixels</li>
 * </ul>
 *
 * <h2>Threading expectations</h2>
 * <p>
 * All modifications and drawing operations are expected to occur on the JavaFX Application Thread.
 * Methods that explicitly call {@link FxBootstrap#runLater(Runnable)} ({@link #centerOrigin()}) are safe
 * to invoke from any thread, but typical usage is still within the JavaFX thread.
 * </p>
 *
 * <h2>Extensibility</h2>
 * <p>
 * This pane focuses on rendering a grid, axes, and tick labels. It is commonly used as a base layer
 * behind other overlays (function plots, annotations, cursor readouts, etc.) by adding additional
 * drawing logic or stacking nodes above this region.
 * </p>
 */
@Getter
final class GraphFxDisplayPane extends Region {

    /**
     * The backing canvas used for all drawing operations.
     * <p>
     * The canvas is resized to match the region bounds in {@link #resizeCanvas()} and redrawn on relevant
     * property changes.
     * </p>
     */
    private final Canvas canvas;

    /**
     * The current scale expressed as pixels per world unit.
     * <p>
     * Larger values zoom in, smaller values zoom out. The value is clamped to {@link #minScalePxPerUnit} and
     * {@link #maxScalePxPerUnit}.
     * </p>
     */
    private final DoubleProperty scalePxPerUnit;

    /**
     * The screen-space X coordinate (pixels) of the world origin (x=0,y=0).
     * <p>
     * Panning changes this value. Zooming also adjusts it to keep the world point under the cursor stable.
     * </p>
     */
    private final DoubleProperty originOffsetX;

    /**
     * The screen-space Y coordinate (pixels) of the world origin (x=0,y=0).
     * <p>
     * Because JavaFX screen coordinates increase downwards, the world-to-screen transform in Y is inverted.
     * </p>
     */
    private final DoubleProperty originOffsetY;

    /**
     * The minimum allowed zoom level, expressed as pixels per world unit.
     * <p>
     * Prevents zooming out too far which would reduce usability and potentially cause expensive draw loops
     * due to extremely dense grid line counts.
     * </p>
     */
    private final double minScalePxPerUnit;

    /**
     * The maximum allowed zoom level, expressed as pixels per world unit.
     * <p>
     * Prevents zooming in too far which can result in poor navigation and excessive precision requirements.
     * </p>
     */
    private final double maxScalePxPerUnit;

    /**
     * Controls whether panning with the primary mouse button (usually left click) is enabled.
     * <p>
     * Middle and secondary buttons are always considered panning buttons (see {@link #isPanningButton(MouseButton)}).
     * This property allows applications to reserve primary-click interactions for selection, point placement,
     * or other tools.
     * </p>
     */
    private final BooleanProperty primaryButtonPanningEnabled;

    /**
     * The current display theme property.
     * <p>
     * Theme changes update {@link #activePalette} and trigger a redraw.
     * </p>
     */
    private final ObjectProperty<DisplayTheme> theme;

    /**
     * Formatter used for major tick labels.
     * <p>
     * The pattern is provided by {@link WindowConfig#TICK_LABEL_FORMAT_PATTERN}. This keeps formatting consistent
     * across the application (e.g., limiting decimals and avoiding scientific notation unless desired).
     * </p>
     */
    private final DecimalFormat tickLabelFormat;

    /**
     * Cached palette derived from the active {@link #theme}.
     * <p>
     * This palette is used for background, grid, axes, and label colors.
     * </p>
     */
    private WindowConfig.ThemePalette activePalette;

    /**
     * Stores the last drag position while panning is active.
     * <p>
     * When {@code null}, no panning gesture is in progress. When non-null, the delta to the current mouse position
     * is applied to {@link #originOffsetX} and {@link #originOffsetY}.
     * </p>
     */
    private Point2D lastDragPoint;

    /**
     * Creates a display pane using default scale and theme values from {@link WindowConfig}.
     * <p>
     * The initial origin is centered automatically once the pane has non-zero layout bounds.
     * </p>
     */
    public GraphFxDisplayPane() {
        this(WindowConfig.INITIAL_SCALE_PX_PER_UNIT, WindowConfig.MIN_SCALE_PX_PER_UNIT, WindowConfig.MAX_SCALE_PX_PER_UNIT, DisplayTheme.LIGHT);
    }

    /**
     * Creates a display pane using default scale bounds from {@link WindowConfig} and the specified initial theme.
     * <p>
     * A {@code null} theme is normalized to {@link DisplayTheme#LIGHT}.
     * </p>
     *
     * @param theme the initial theme to apply; {@code null} is treated as {@link DisplayTheme#LIGHT}
     */
    public GraphFxDisplayPane(@NonNull final DisplayTheme theme) {
        this(WindowConfig.INITIAL_SCALE_PX_PER_UNIT, WindowConfig.MIN_SCALE_PX_PER_UNIT, WindowConfig.MAX_SCALE_PX_PER_UNIT, normalizeTheme(theme));
    }

    /**
     * Creates a display pane with explicit initial scale and scale bounds, using {@link DisplayTheme#LIGHT}.
     * <p>
     * The {@code initialScalePxPerUnit} is clamped into the provided bounds.
     * </p>
     *
     * @param initialScalePxPerUnit the initial zoom level (pixels per world unit)
     * @param minScalePxPerUnit     the minimum allowed zoom level (pixels per world unit)
     * @param maxScalePxPerUnit     the maximum allowed zoom level (pixels per world unit)
     */
    public GraphFxDisplayPane(final double initialScalePxPerUnit, final double minScalePxPerUnit, final double maxScalePxPerUnit) {
        this(initialScalePxPerUnit, minScalePxPerUnit, maxScalePxPerUnit, DisplayTheme.LIGHT);
    }

    /**
     * Creates a display pane with explicit scale configuration and an initial theme.
     * <p>
     * The internal canvas is created and registered as the only child of this {@link Region}. The class sets up:
     * </p>
     * <ul>
     *   <li>Scale and origin properties</li>
     *   <li>Theme binding and palette selection</li>
     *   <li>Automatic redraw triggers</li>
     *   <li>Mouse handlers for panning and wheel zoom</li>
     * </ul>
     *
     * <p>
     * The origin is centered lazily after layout when size becomes available (see {@link #registerRedrawTriggers()}).
     * </p>
     *
     * @param initialScalePxPerUnit the initial zoom level in pixels per world unit
     * @param minScalePxPerUnit     the minimum zoom level in pixels per world unit
     * @param maxScalePxPerUnit     the maximum zoom level in pixels per world unit
     * @param initialTheme          the initial theme; {@code null} is treated as {@link DisplayTheme#LIGHT}
     */
    public GraphFxDisplayPane(final double initialScalePxPerUnit, final double minScalePxPerUnit, final double maxScalePxPerUnit, final DisplayTheme initialTheme) {
        this.canvas = new Canvas();

        this.scalePxPerUnit = new SimpleDoubleProperty(clampScale(initialScalePxPerUnit, minScalePxPerUnit, maxScalePxPerUnit));
        this.originOffsetX = new SimpleDoubleProperty(0.0);
        this.originOffsetY = new SimpleDoubleProperty(0.0);

        this.minScalePxPerUnit = minScalePxPerUnit;
        this.maxScalePxPerUnit = maxScalePxPerUnit;

        this.primaryButtonPanningEnabled = new SimpleBooleanProperty(true);
        this.theme = new SimpleObjectProperty<>(normalizeTheme(initialTheme));

        this.tickLabelFormat = new DecimalFormat(WindowConfig.TICK_LABEL_FORMAT_PATTERN);

        applyTheme(this.theme.get());
        this.theme.addListener((obs, oldTheme, newTheme) -> {
            applyTheme(normalizeTheme(newTheme));
            draw();
        });

        getChildren().add(canvas);

        registerRedrawTriggers();
        registerMouseHandlers();

        setCursor(Cursor.OPEN_HAND);
    }

    /**
     * Updates the theme of this pane.
     * <p>
     * The value is normalized so that {@code null} becomes {@link DisplayTheme#LIGHT}. A theme change triggers
     * an immediate redraw via the theme listener set up in the constructor.
     * </p>
     *
     * @param theme the new theme; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public void setTheme(@NonNull final DisplayTheme theme) {
        this.theme.set(normalizeTheme(theme));
    }

    /**
     * Enables or disables panning with the primary mouse button.
     * <p>
     * When disabled, panning is still available using the middle mouse button or the secondary (right) button.
     * </p>
     *
     * @param enabled {@code true} to allow panning with the primary button; {@code false} to restrict panning
     *                to middle/secondary buttons
     */
    public void setPrimaryButtonPanningEnabled(final boolean enabled) {
        primaryButtonPanningEnabled.set(enabled);
    }

    /**
     * Returns whether panning with the primary mouse button is enabled.
     *
     * @return {@code true} if primary-button panning is enabled; {@code false} otherwise
     */
    public boolean isPrimaryButtonPanningEnabled() {
        return primaryButtonPanningEnabled.get();
    }

    /**
     * Centers the world origin (0,0) in the middle of this pane.
     * <p>
     * The operation is scheduled on the JavaFX Application Thread via {@link FxBootstrap#runLater(Runnable)}.
     * This makes the method safe to call from background threads, although typical usage is from UI code.
     * </p>
     */
    public void centerOrigin() {
        FxBootstrap.runLater(() -> {
            originOffsetX.set(getWidth() / 2.0);
            originOffsetY.set(getHeight() / 2.0);
        });
    }

    /**
     * Lays out the single canvas child so it fills the region.
     * <p>
     * The canvas is relocated to (0,0). Actual size is controlled by {@link #resizeCanvas()},
     * which reacts to width/height changes.
     * </p>
     */
    @Override
    protected void layoutChildren() {
        canvas.relocate(0, 0);
        super.layoutChildren();
    }

    /**
     * Registers listeners that trigger canvas resizing and/or repainting.
     * <p>
     * The pane redraws when:
     * </p>
     * <ul>
     *   <li>Width or height changes (resizes the canvas and redraws)</li>
     *   <li>Scale changes</li>
     *   <li>Origin offsets change</li>
     * </ul>
     *
     * <p>
     * Additionally, after the pane first acquires valid layout bounds, the origin is centered if both offsets
     * are still at their initial zero values. This provides a predictable "centered origin" default without
     * requiring callers to explicitly call {@link #centerOrigin()} after adding the pane to a scene.
     * </p>
     */
    private void registerRedrawTriggers() {
        final InvalidationListener redraw = obs -> draw();

        widthProperty().addListener((obs, oldValue, newValue) -> resizeCanvas());
        heightProperty().addListener((obs, oldValue, newValue) -> resizeCanvas());

        scalePxPerUnit.addListener(redraw);
        originOffsetX.addListener(redraw);
        originOffsetY.addListener(redraw);

        layoutBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
                return;
            }
            if (originOffsetX.get() == 0.0 && originOffsetY.get() == 0.0) {
                centerOrigin();
            }
        });
    }

    /**
     * Registers mouse input handlers for zooming and panning.
     * <p>
     * Zooming uses the scroll wheel and applies an exponential factor (for smooth scaling). Panning uses
     * a drag gesture and changes the origin offsets by the drag delta. Cursor feedback is provided by
     * switching between {@link Cursor#OPEN_HAND} and {@link Cursor#CLOSED_HAND}.
     * </p>
     */
    private void registerMouseHandlers() {
        addEventFilter(ScrollEvent.SCROLL, this::handleZoomScroll);

        setOnMousePressed(e -> {
            if (!isPanningButton(e.getButton())) {
                return;
            }

            lastDragPoint = new Point2D(e.getX(), e.getY());
            setCursor(Cursor.CLOSED_HAND);
            e.consume();
        });

        setOnMouseDragged(e -> {
            if (lastDragPoint == null) {
                return;
            }

            final Point2D current = new Point2D(e.getX(), e.getY());
            final Point2D delta = current.subtract(lastDragPoint);

            originOffsetX.set(originOffsetX.get() + delta.getX());
            originOffsetY.set(originOffsetY.get() + delta.getY());

            lastDragPoint = current;
            e.consume();
        });

        setOnMouseReleased(e -> {
            lastDragPoint = null;
            setCursor(Cursor.OPEN_HAND);
        });
    }

    /**
     * Determines whether the given mouse button should initiate a panning gesture.
     * <p>
     * Middle and secondary buttons always pan. The primary button pans only if
     * {@link #isPrimaryButtonPanningEnabled()} returns {@code true}.
     * </p>
     *
     * @param button the mouse button to evaluate; must not be {@code null}
     * @return {@code true} if this button should start panning; {@code false} otherwise
     * @throws NullPointerException if {@code button} is {@code null}
     */
    private boolean isPanningButton(@NonNull final MouseButton button) {
        if (button == MouseButton.MIDDLE || button == MouseButton.SECONDARY) {
            return true;
        }
        return button == MouseButton.PRIMARY && isPrimaryButtonPanningEnabled();
    }

    /**
     * Resizes the canvas to match the current region size and triggers a redraw.
     * <p>
     * The method enforces a minimum size of 1 pixel in each dimension to avoid zero-sized canvas operations.
     * </p>
     */
    private void resizeCanvas() {
        final double width = Math.max(1.0, getWidth());
        final double height = Math.max(1.0, getHeight());

        canvas.setWidth(width);
        canvas.setHeight(height);

        draw();
    }

    /**
     * Normalizes a potentially null theme value.
     *
     * @param theme the theme to normalize (may be {@code null})
     * @return {@link DisplayTheme#LIGHT} if {@code theme} is {@code null}; otherwise {@code theme}
     */
    private static DisplayTheme normalizeTheme(final DisplayTheme theme) {
        return theme == null ? DisplayTheme.LIGHT : theme;
    }

    /**
     * Applies the palette that corresponds to the given theme.
     * <p>
     * This selects either {@link WindowConfig#DARK_THEME} or {@link WindowConfig#LIGHT_THEME} and caches it
     * into {@link #activePalette}. Drawing methods reference this cache for consistent styling.
     * </p>
     *
     * @param theme the theme to apply; must not be {@code null}
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    private void applyTheme(@NonNull final DisplayTheme theme) {
        activePalette = theme == DisplayTheme.DARK ? WindowConfig.DARK_THEME : WindowConfig.LIGHT_THEME;
    }

    /**
     * Handles a scroll-wheel zoom event.
     * <p>
     * Zoom is performed with an exponential factor derived from {@link ScrollEvent#getDeltaY()} and
     * {@link WindowConfig#ZOOM_SENSITIVITY}. The resulting scale is clamped to the configured bounds.
     * </p>
     *
     * <h3>Cursor-centered zoom</h3>
     * <p>
     * The method keeps the world point under the mouse cursor stable by:
     * </p>
     * <ol>
     *   <li>Computing the world coordinate currently under the mouse</li>
     *   <li>Applying the new scale</li>
     *   <li>Adjusting the origin offsets so the same world coordinate maps back under the cursor</li>
     * </ol>
     *
     * @param event the scroll event; must not be {@code null}
     * @throws NullPointerException if {@code event} is {@code null}
     */
    private void handleZoomScroll(@NonNull final ScrollEvent event) {
        final double zoomFactor = Math.exp(event.getDeltaY() * WindowConfig.ZOOM_SENSITIVITY);

        final double currentScale = scalePxPerUnit.get();
        final double targetScale = clamp(currentScale * zoomFactor, minScalePxPerUnit, maxScalePxPerUnit);

        if (Double.compare(targetScale, currentScale) == 0) {
            return;
        }

        final double mouseX = event.getX();
        final double mouseY = event.getY();

        final double worldXUnderMouse = screenToWorldX(mouseX);
        final double worldYUnderMouse = screenToWorldY(mouseY);

        scalePxPerUnit.set(targetScale);

        originOffsetX.set(mouseX - worldXUnderMouse * targetScale);
        originOffsetY.set(mouseY + worldYUnderMouse * targetScale);

        event.consume();
    }

    /**
     * Redraws the complete background layer (grid + axes + labels) on the canvas.
     * <p>
     * The method clears the canvas with the palette background color and then draws:
     * </p>
     * <ol>
     *   <li>Grid lines (minor and major)</li>
     *   <li>Axes (if visible in the current viewport)</li>
     * </ol>
     *
     * <p>
     * Drawing is performed in immediate mode using {@link GraphicsContext}. This method is intentionally
     * self-contained so that redraw triggers can call it safely and consistently.
     * </p>
     */
    private void draw() {
        final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        final double width = canvas.getWidth();
        final double height = canvas.getHeight();

        graphicsContext.setFill(activePalette.background());
        graphicsContext.fillRect(0, 0, width, height);

        graphicsContext.setFont(Font.font(WindowConfig.LABEL_FONT_FAMILY, WindowConfig.LABEL_FONT_SIZE));

        drawGrid(graphicsContext, width, height);
        drawAxes(graphicsContext, width, height);
    }

    /**
     * Draws the grid (minor and major lines) and the corresponding major tick labels.
     * <p>
     * The grid spacing is adaptive and chosen to produce visually "nice" steps based on
     * {@link WindowConfig#TARGET_MAJOR_GRID_SPACING_PX}. Minor lines subdivide major steps using
     * {@link WindowConfig#MINOR_GRID_DIVISIONS}.
     * </p>
     *
     * @param graphicsContext the graphics context to draw with; must not be {@code null}
     * @param width           the canvas width in pixels
     * @param height          the canvas height in pixels
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawGrid(@NonNull final GraphicsContext graphicsContext, final double width, final double height) {
        final double pxPerUnit = scalePxPerUnit.get();

        final double majorStepWorld = niceStep(WindowConfig.TARGET_MAJOR_GRID_SPACING_PX / pxPerUnit);
        final double minorStepWorld = majorStepWorld / WindowConfig.MINOR_GRID_DIVISIONS;

        final double minWorldX = screenToWorldX(0);
        final double maxWorldX = screenToWorldX(width);
        final double minWorldY = screenToWorldY(height);
        final double maxWorldY = screenToWorldY(0);

        graphicsContext.setStroke(activePalette.minorGrid());
        graphicsContext.setLineWidth(WindowConfig.GRID_LINE_WIDTH);
        drawVerticalLines(graphicsContext, height, minWorldX, maxWorldX, minorStepWorld);
        drawHorizontalLines(graphicsContext, width, minWorldY, maxWorldY, minorStepWorld);

        graphicsContext.setStroke(activePalette.majorGrid());
        graphicsContext.setLineWidth(WindowConfig.GRID_LINE_WIDTH);
        drawVerticalLines(graphicsContext, height, minWorldX, maxWorldX, majorStepWorld);
        drawHorizontalLines(graphicsContext, width, minWorldY, maxWorldY, majorStepWorld);

        graphicsContext.setFill(activePalette.label());
        drawTickLabels(graphicsContext, width, height, minWorldX, maxWorldX, minWorldY, maxWorldY, majorStepWorld);
    }

    /**
     * Draws the X and Y axes if they are visible within the current viewport.
     * <p>
     * The axes are drawn at world coordinates x=0 and y=0. Visibility is determined by checking whether
     * the corresponding screen position lies within the canvas bounds. The axis position is snapped to
     * the pixel center to produce crisp 1px lines when possible.
     * </p>
     *
     * @param graphicsContext the graphics context to draw with; must not be {@code null}
     * @param width           the canvas width in pixels
     * @param height          the canvas height in pixels
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawAxes(@NonNull final GraphicsContext graphicsContext, final double width, final double height) {
        final double xAxisScreenY = worldToScreenY(0);
        final double yAxisScreenX = worldToScreenX(0);

        graphicsContext.setStroke(activePalette.axis());
        graphicsContext.setLineWidth(WindowConfig.AXIS_LINE_WIDTH);

        if (isBetween(yAxisScreenX, 0, width)) {
            final double snapped = snapToPixelCenter(yAxisScreenX);
            graphicsContext.strokeLine(snapped, 0, snapped, height);
        }

        if (isBetween(xAxisScreenY, 0, height)) {
            final double snapped = snapToPixelCenter(xAxisScreenY);
            graphicsContext.strokeLine(0, snapped, width, snapped);
        }
    }

    /**
     * Draws vertical grid lines at the given world-step spacing across the visible range.
     *
     * @param graphicsContext the graphics context to draw with; must not be {@code null}
     * @param canvasHeight    the canvas height in pixels
     * @param minWorldX       the minimum visible world X coordinate
     * @param maxWorldX       the maximum visible world X coordinate
     * @param stepWorld       the spacing between lines in world units; values {@code <= 0} are ignored
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawVerticalLines(@NonNull final GraphicsContext graphicsContext, final double canvasHeight, final double minWorldX, final double maxWorldX, final double stepWorld) {
        if (stepWorld <= 0) {
            return;
        }

        final double start = Math.floor(minWorldX / stepWorld) * stepWorld;
        for (double worldX = start; worldX <= maxWorldX; worldX += stepWorld) {
            final double screenX = worldToScreenX(worldX);
            final double snapped = snapToPixelCenter(screenX);
            graphicsContext.strokeLine(snapped, 0, snapped, canvasHeight);
        }
    }

    /**
     * Draws horizontal grid lines at the given world-step spacing across the visible range.
     *
     * @param graphicsContext the graphics context to draw with; must not be {@code null}
     * @param canvasWidth     the canvas width in pixels
     * @param minWorldY       the minimum visible world Y coordinate
     * @param maxWorldY       the maximum visible world Y coordinate
     * @param stepWorld       the spacing between lines in world units; values {@code <= 0} are ignored
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawHorizontalLines(@NonNull final GraphicsContext graphicsContext, final double canvasWidth, final double minWorldY, final double maxWorldY, final double stepWorld) {
        if (stepWorld <= 0) {
            return;
        }

        final double start = Math.floor(minWorldY / stepWorld) * stepWorld;
        for (double worldY = start; worldY <= maxWorldY; worldY += stepWorld) {
            final double screenY = worldToScreenY(worldY);
            final double snapped = snapToPixelCenter(screenY);
            graphicsContext.strokeLine(0, snapped, canvasWidth, snapped);
        }
    }

    /**
     * Draws tick labels for major grid steps along the axes (or near the edges if an axis is not visible).
     * <p>
     * Labels at world coordinate 0 are omitted to avoid clutter at the origin. A small epsilon
     * (see {@link WindowConfig#LABEL_ZERO_EPSILON}) is used to treat near-zero values as zero.
     * </p>
     *
     * <h3>Placement strategy</h3>
     * <ul>
     *   <li>If the x-axis is visible, x tick labels are placed near y=0; otherwise they are placed near the bottom edge.</li>
     *   <li>If the y-axis is visible, y tick labels are placed near x=0; otherwise they are placed near the left edge.</li>
     * </ul>
     *
     * @param graphicsContext the graphics context to draw with; must not be {@code null}
     * @param width           the canvas width in pixels
     * @param height          the canvas height in pixels
     * @param minWorldX       minimum visible world X coordinate
     * @param maxWorldX       maximum visible world X coordinate
     * @param minWorldY       minimum visible world Y coordinate
     * @param maxWorldY       maximum visible world Y coordinate
     * @param stepWorld       major step spacing in world units
     * @throws NullPointerException if {@code graphicsContext} is {@code null}
     */
    private void drawTickLabels(@NonNull final GraphicsContext graphicsContext, final double width, final double height, final double minWorldX, final double maxWorldX, final double minWorldY, final double maxWorldY, final double stepWorld) {
        final boolean isYAxisVisible = isBetween(worldToScreenX(0), 0, width);
        final boolean isXAxisVisible = isBetween(worldToScreenY(0), 0, height);

        final double labelBaselineY = isXAxisVisible ? worldToScreenY(0) : (height - WindowConfig.LABEL_EDGE_PADDING_PX);
        final double labelAnchorX = isYAxisVisible ? worldToScreenX(0) : WindowConfig.LABEL_EDGE_PADDING_PX;

        final double xStart = Math.floor(minWorldX / stepWorld) * stepWorld;
        for (double worldX = xStart; worldX <= maxWorldX; worldX += stepWorld) {
            if (isNearZero(worldX)) {
                continue;
            }

            final double screenX = worldToScreenX(worldX);
            if (!isBetween(screenX, 0, width)) {
                continue;
            }

            graphicsContext.fillText(tickLabelFormat.format(worldX), screenX + WindowConfig.LABEL_X_OFFSET_PX, labelBaselineY - WindowConfig.LABEL_Y_OFFSET_PX);
        }

        final double yStart = Math.floor(minWorldY / stepWorld) * stepWorld;
        for (double worldY = yStart; worldY <= maxWorldY; worldY += stepWorld) {
            if (isNearZero(worldY)) {
                continue;
            }

            final double screenY = worldToScreenY(worldY);
            if (!isBetween(screenY, 0, height)) {
                continue;
            }

            graphicsContext.fillText(tickLabelFormat.format(worldY), labelAnchorX + WindowConfig.LABEL_AXIS_OFFSET_X_PX, screenY - WindowConfig.LABEL_AXIS_OFFSET_Y_PX);
        }
    }

    /**
     * Converts a world X coordinate to its screen (pixel) X coordinate.
     *
     * @param worldX the world x coordinate
     * @return the corresponding screen x coordinate in pixels
     */
    private double worldToScreenX(final double worldX) {
        return originOffsetX.get() + worldX * scalePxPerUnit.get();
    }

    /**
     * Converts a world Y coordinate to its screen (pixel) Y coordinate.
     * <p>
     * This transform is inverted relative to screen coordinates because world Y increases upwards.
     * </p>
     *
     * @param worldY the world y coordinate
     * @return the corresponding screen y coordinate in pixels
     */
    private double worldToScreenY(final double worldY) {
        return originOffsetY.get() - worldY * scalePxPerUnit.get();
    }

    /**
     * Converts a screen (pixel) X coordinate to its world X coordinate.
     *
     * @param screenX the screen x coordinate in pixels
     * @return the corresponding world x coordinate
     */
    private double screenToWorldX(final double screenX) {
        return (screenX - originOffsetX.get()) / scalePxPerUnit.get();
    }

    /**
     * Converts a screen (pixel) Y coordinate to its world Y coordinate.
     * <p>
     * This transform inverts the Y axis so that screen down becomes world negative.
     * </p>
     *
     * @param screenY the screen y coordinate in pixels
     * @return the corresponding world y coordinate
     */
    private double screenToWorldY(final double screenY) {
        return (originOffsetY.get() - screenY) / scalePxPerUnit.get();
    }

    /**
     * Determines whether a numeric value should be treated as zero for labeling purposes.
     * <p>
     * Uses {@link WindowConfig#LABEL_ZERO_EPSILON} to account for floating-point rounding.
     * </p>
     *
     * @param value the value to test
     * @return {@code true} if the value is close enough to zero; {@code false} otherwise
     */
    private boolean isNearZero(final double value) {
        return Math.abs(value) < WindowConfig.LABEL_ZERO_EPSILON;
    }

    /**
     * Snaps a coordinate to the nearest pixel center to improve crispness of strokes.
     * <p>
     * For thin lines, drawing at half-integer positions often produces sharper results due to how
     * rasterization aligns to pixel boundaries. The specific offset is configured via
     * {@link WindowConfig#PIXEL_SNAP_OFFSET}.
     * </p>
     *
     * @param value the coordinate to snap
     * @return the snapped coordinate
     */
    private double snapToPixelCenter(final double value) {
        return Math.round(value) + WindowConfig.PIXEL_SNAP_OFFSET;
    }

    /**
     * Computes a "nice" step size for grid spacing from a raw desired step.
     * <p>
     * The algorithm selects a step of 1, 2, 5, or 10 times a power of ten to produce readable grid intervals.
     * This yields consistent tick labels and predictable navigation across zoom levels.
     * </p>
     *
     * @param rawStep the desired step in world units
     * @return a normalized "nice" step in world units; returns {@code 1.0} for non-positive inputs
     */
    private double niceStep(final double rawStep) {
        if (rawStep <= 0) {
            return 1.0;
        }

        final double exponent = Math.floor(Math.log10(rawStep));
        final double normalized = rawStep / Math.pow(10, exponent);

        final double niceNormalized;
        if (normalized < 1.5) {
            niceNormalized = 1.0;
        } else if (normalized < 3.5) {
            niceNormalized = 2.0;
        } else if (normalized < 7.5) {
            niceNormalized = 5.0;
        } else {
            niceNormalized = 10.0;
        }

        return niceNormalized * Math.pow(10, exponent);
    }

    /**
     * Clamps a value to the inclusive range {@code [min, max]}.
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @return the clamped value
     */
    private double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Returns {@code true} if {@code value} lies within the inclusive range {@code [min, max]}.
     *
     * @param value the value to test
     * @param min   inclusive lower bound
     * @param max   inclusive upper bound
     * @return {@code true} if value is in range; {@code false} otherwise
     */
    private boolean isBetween(final double value, final double min, final double max) {
        return value >= min && value <= max;
    }

    /**
     * Clamps an initial scale value to a safe min/max range.
     * <p>
     * This method tolerates reversed scale bounds by computing an ordered range internally.
     * </p>
     *
     * @param initialScale the initial scale value to clamp
     * @param minScale     the minimum scale bound (may be greater than {@code maxScale})
     * @param maxScale     the maximum scale bound (may be less than {@code minScale})
     * @return the clamped initial scale within the ordered bounds
     */
    private double clampScale(final double initialScale, final double minScale, final double maxScale) {
        final double safeMin = Math.min(minScale, maxScale);
        final double safeMax = Math.max(minScale, maxScale);
        return clamp(initialScale, safeMin, safeMax);
    }

}
