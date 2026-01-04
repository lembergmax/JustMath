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

package com.mlprograms.justmath.graphfx.view;

import com.mlprograms.justmath.graphfx.config.WindowConfig;
import com.mlprograms.justmath.graphfx.core.Point;
import com.mlprograms.justmath.graphfx.internal.FxBootstrap;
import com.mlprograms.justmath.graphfx.api.DisplayTheme;
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
 *
 * <p>The pane supports:</p>
 * <ul>
 *   <li><strong>Panning</strong> by dragging (configurable primary mouse button support, plus middle/right by default)</li>
 *   <li><strong>Zooming</strong> using the mouse wheel (zoom is centered around the cursor position)</li>
 *   <li><strong>Theming</strong> via {@link DisplayTheme} and the corresponding {@link WindowConfig.ThemePalette}</li>
 *   <li><strong>Automatic redraw</strong> when size, scale, or origin offsets change</li>
 * </ul>
 *
 * <h2>Coordinate systems</h2>
 * <p>This class maintains a clear separation between:</p>
 * <ul>
 *   <li><em>World coordinates</em> (mathematical plane; x increases to the right, y increases upwards)</li>
 *   <li><em>Screen coordinates</em> (JavaFX pixel space; x increases to the right, y increases downwards)</li>
 * </ul>
 *
 * <h2>Threading expectations</h2>
 * <p>
 * All modifications and drawing operations are expected to occur on the JavaFX Application Thread.
 * Methods that explicitly call {@link FxBootstrap#runLater(Runnable)} ({@link #centerOrigin()}) are safe
 * to invoke from any thread.
 * </p>
 */
public final class GraphFxDisplayPane extends Region {

    private final Canvas canvas;

    @Getter
    private final DoubleProperty scalePxPerUnit;
    @Getter
    private final DoubleProperty originOffsetX;
    @Getter
    private final DoubleProperty originOffsetY;

    private final double minScalePxPerUnit;
    private final double maxScalePxPerUnit;

    private final BooleanProperty primaryButtonPanningEnabled;
    private final ObjectProperty<DisplayTheme> theme;

    private final DecimalFormat tickLabelFormat;

    private WindowConfig.ThemePalette activePalette;

    private Point2D lastDragPoint;

    /**
     * Creates a display pane using default scale and theme values from {@link WindowConfig}.
     */
    public GraphFxDisplayPane() {
        this(WindowConfig.INITIAL_SCALE_PX_PER_UNIT, WindowConfig.MIN_SCALE_PX_PER_UNIT, WindowConfig.MAX_SCALE_PX_PER_UNIT, DisplayTheme.LIGHT);
    }

    /**
     * Creates a display pane using default scale bounds and the specified initial theme.
     *
     * <p>If {@code theme} is {@code null}, {@link DisplayTheme#LIGHT} is used.</p>
     *
     * @param theme initial theme (nullable)
     */
    public GraphFxDisplayPane(final DisplayTheme theme) {
        this(WindowConfig.INITIAL_SCALE_PX_PER_UNIT, WindowConfig.MIN_SCALE_PX_PER_UNIT, WindowConfig.MAX_SCALE_PX_PER_UNIT, normalizeTheme(theme));
    }

    /**
     * Creates a display pane with explicit initial scale and scale bounds, using {@link DisplayTheme#LIGHT}.
     *
     * @param initialScalePxPerUnit initial zoom level (pixels per world unit)
     * @param minScalePxPerUnit minimum zoom level (pixels per world unit)
     * @param maxScalePxPerUnit maximum zoom level (pixels per world unit)
     */
    public GraphFxDisplayPane(final double initialScalePxPerUnit, final double minScalePxPerUnit, final double maxScalePxPerUnit) {
        this(initialScalePxPerUnit, minScalePxPerUnit, maxScalePxPerUnit, DisplayTheme.LIGHT);
    }

    /**
     * Creates a display pane with explicit scale configuration and an initial theme.
     *
     * @param initialScalePxPerUnit initial zoom level in pixels per world unit
     * @param minScalePxPerUnit minimum zoom level in pixels per world unit
     * @param maxScalePxPerUnit maximum zoom level in pixels per world unit
     * @param initialTheme initial theme (nullable)
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
     *
     * <p>If {@code theme} is {@code null}, {@link DisplayTheme#LIGHT} is used.</p>
     *
     * @param theme new theme (nullable)
     */
    public void setTheme(final DisplayTheme theme) {
        this.theme.set(normalizeTheme(theme));
    }

    /**
     * Enables or disables panning with the primary mouse button.
     *
     * @param enabled {@code true} to allow panning with primary button; {@code false} otherwise
     */
    public void setPrimaryButtonPanningEnabled(final boolean enabled) {
        primaryButtonPanningEnabled.set(enabled);
    }

    /**
     * Returns whether panning with the primary mouse button is enabled.
     *
     * @return {@code true} if enabled; {@code false} otherwise
     */
    public boolean isPrimaryButtonPanningEnabled() {
        return primaryButtonPanningEnabled.get();
    }

    /**
     * Centers the world origin (0,0) in the middle of this pane.
     *
     * <p>The operation is scheduled on the JavaFX Application Thread.</p>
     */
    public void centerOrigin() {
        FxBootstrap.runLater(() -> {
            originOffsetX.set(getWidth() / 2.0);
            originOffsetY.set(getHeight() / 2.0);
        });
    }

    /**
     * Converts a {@link Point} in world coordinates to a screen pixel position.
     *
     * @param worldPoint world point (must not be {@code null})
     * @return screen point in pixels
     * @throws NullPointerException if {@code worldPoint} is {@code null}
     */
    public Point2D worldToScreen(@NonNull final Point worldPoint) {
        final double scalePxPerUnitValue = scalePxPerUnit.get();
        final double originX = originOffsetX.get();
        final double originY = originOffsetY.get();

        final double screenX = originX + worldPoint.x() * scalePxPerUnitValue;
        final double screenY = originY - worldPoint.y() * scalePxPerUnitValue;

        return new Point2D(screenX, screenY);
    }

    /**
     * Converts a screen pixel position to a {@link Point} in world coordinates.
     *
     * @param screenPoint screen point in pixels (must not be {@code null})
     * @return world point in world units
     * @throws NullPointerException if {@code screenPoint} is {@code null}
     */
    public Point screenToWorld(@NonNull final Point2D screenPoint) {
        final double scalePxPerUnitValue = scalePxPerUnit.get();
        final double originX = originOffsetX.get();
        final double originY = originOffsetY.get();

        final double worldX = (screenPoint.getX() - originX) / scalePxPerUnitValue;
        final double worldY = (originY - screenPoint.getY()) / scalePxPerUnitValue;

        return new Point(worldX, worldY);
    }

    /**
     * Lays out the internal canvas so it fills this region.
     * <p>
     * The canvas is relocated to {@code (0,0)}. Its size is controlled by {@link #resizeCanvas()}, which reacts
     * to changes of this region's width and height.
     * </p>
     *
     * <p><strong>Thread-safety:</strong> This method is called by the JavaFX layout system and therefore runs on the
     * JavaFX Application Thread.</p>
     */
    @Override
    protected void layoutChildren() {
        canvas.relocate(0, 0);
        super.layoutChildren();
    }

    /**
     * Registers listeners that trigger canvas resizing and/or repainting.
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
     *
     * @param button mouse button
     * @return {@code true} if this button should start panning
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
     * @param theme theme (nullable)
     * @return light theme if {@code theme} is {@code null}
     */
    private static DisplayTheme normalizeTheme(final DisplayTheme theme) {
        return theme == null ? DisplayTheme.LIGHT : theme;
    }

    /**
     * Applies the palette for the given theme.
     *
     * @param theme theme to apply
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    private void applyTheme(@NonNull final DisplayTheme theme) {
        activePalette = theme == DisplayTheme.DARK ? WindowConfig.DARK_THEME : WindowConfig.LIGHT_THEME;
    }

    /**
     * Handles a scroll-wheel zoom event with cursor-centered zoom behavior.
     *
     * @param event scroll event
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
     * Redraws the grid, axes and labels.
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
     * Draws grid lines and tick labels.
     *
     * @param graphicsContext graphics context
     * @param width canvas width
     * @param height canvas height
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
     * Draws x and y axes when visible.
     *
     * @param graphicsContext graphics context
     * @param width canvas width
     * @param height canvas height
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
     * Draws vertical grid lines.
     *
     * @param graphicsContext graphics context
     * @param canvasHeight canvas height
     * @param minWorldX min world x
     * @param maxWorldX max world x
     * @param stepWorld step size in world units
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
     * Draws horizontal grid lines.
     *
     * @param graphicsContext graphics context
     * @param canvasWidth canvas width
     * @param minWorldY min world y
     * @param maxWorldY max world y
     * @param stepWorld step size in world units
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
     * Draws major tick labels.
     *
     * @param graphicsContext graphics context
     * @param width canvas width
     * @param height canvas height
     * @param minWorldX min world x
     * @param maxWorldX max world x
     * @param minWorldY min world y
     * @param maxWorldY max world y
     * @param stepWorld major step size in world units
     */
    private void drawTickLabels(@NonNull final GraphicsContext graphicsContext,
                                final double width,
                                final double height,
                                final double minWorldX,
                                final double maxWorldX,
                                final double minWorldY,
                                final double maxWorldY,
                                final double stepWorld) {
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
     * Converts world x to screen x.
     *
     * @param worldX world x
     * @return screen x in pixels
     */
    private double worldToScreenX(final double worldX) {
        return originOffsetX.get() + worldX * scalePxPerUnit.get();
    }

    /**
     * Converts world y to screen y (inverted).
     *
     * @param worldY world y
     * @return screen y in pixels
     */
    private double worldToScreenY(final double worldY) {
        return originOffsetY.get() - worldY * scalePxPerUnit.get();
    }

    /**
     * Converts screen x to world x.
     *
     * @param screenX screen x in pixels
     * @return world x
     */
    private double screenToWorldX(final double screenX) {
        return (screenX - originOffsetX.get()) / scalePxPerUnit.get();
    }

    /**
     * Converts screen y to world y (inverted).
     *
     * @param screenY screen y in pixels
     * @return world y
     */
    private double screenToWorldY(final double screenY) {
        return (originOffsetY.get() - screenY) / scalePxPerUnit.get();
    }

    /**
     * Checks if value is near zero for labeling.
     *
     * @param value value
     * @return {@code true} if value is close to zero
     */
    private boolean isNearZero(final double value) {
        return Math.abs(value) < WindowConfig.LABEL_ZERO_EPSILON;
    }

    /**
     * Snaps coordinate to pixel center.
     *
     * @param value value
     * @return snapped value
     */
    private double snapToPixelCenter(final double value) {
        return Math.round(value) + WindowConfig.PIXEL_SNAP_OFFSET;
    }

    /**
     * Computes a nice step size (1/2/5/10 * pow10).
     *
     * @param rawStep raw step
     * @return nice step
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
     * Clamps a value to an inclusive range.
     *
     * @param value value
     * @param min min
     * @param max max
     * @return clamped value
     */
    private double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Checks if value lies in an inclusive range.
     *
     * @param value value
     * @param min min
     * @param max max
     * @return {@code true} if in range
     */
    private boolean isBetween(final double value, final double min, final double max) {
        return value >= min && value <= max;
    }

    /**
     * Clamps an initial scale to safe min/max bounds.
     *
     * @param initialScale initial scale
     * @param minScale min scale
     * @param maxScale max scale
     * @return clamped initial scale
     */
    private double clampScale(final double initialScale, final double minScale, final double maxScale) {
        final double safeMin = Math.min(minScale, maxScale);
        final double safeMax = Math.max(minScale, maxScale);
        return clamp(initialScale, safeMin, safeMax);
    }

}
