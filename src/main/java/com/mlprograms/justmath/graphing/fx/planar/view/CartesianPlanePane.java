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

package com.mlprograms.justmath.graphing.fx.planar.view;

import com.mlprograms.justmath.graphing.fx.planar.model.PlotResult;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;

/**
 * JavaFX pane providing a panning and zoomable Cartesian plane.
 * <p>
 * This pane is a pure view component. It maintains view state (center, scale) and renders a
 * {@link PlotResult} using a {@link PlaneRenderer}.
 * </p>
 */
public final class CartesianPlanePane extends StackPane {

    /**
     * Default minimum pixels per world unit.
     */
    private static final double MIN_PIXELS_PER_UNIT = 5.0d;

    /**
     * Default maximum pixels per world unit.
     */
    private static final double MAX_PIXELS_PER_UNIT = 800.0d;

    /**
     * Canvas used for all rendering.
     */
    private final Canvas canvas;

    /**
     * Renderer responsible for drawing axes, grid and plot primitives.
     */
    private final PlaneRenderer renderer;

    /**
     * Info overlay label.
     */
    private final Label infoLabel;

    /**
     * Current world center x-coordinate.
     */
    @Getter
    private double centerX;

    /**
     * Current world center y-coordinate.
     */
    @Getter
    private double centerY;

    /**
     * Current scale expressed as pixels per world unit.
     */
    @Getter
    private double pixelsPerUnit;

    /**
     * Last pressed mouse x position for panning.
     */
    private double lastMouseX;

    /**
     * Last pressed mouse y position for panning.
     */
    private double lastMouseY;

    /**
     * Last rendered plot result.
     */
    private PlotResult plotResult;

    /**
     * Creates a pane with default view parameters.
     */
    public CartesianPlanePane() {
        this.canvas = new Canvas(1200, 800);
        this.renderer = new PlaneRenderer();
        this.infoLabel = new Label();

        this.centerX = 0.0d;
        this.centerY = 0.0d;
        this.pixelsPerUnit = 60.0d;

        this.plotResult = new PlotResult();

        setPadding(new Insets(0));
        setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        infoLabel.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-padding: 6 10; -fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 6; -fx-background-radius: 6;");
        StackPane.setMargin(infoLabel, new Insets(10));

        getChildren().addAll(canvas, infoLabel);

        bindCanvasToPaneSize();
        installPanHandlers();
        installZoomHandlers();

        redraw();
    }

    /**
     * Sets the plot result that should be rendered.
     *
     * @param plotResult plot result (must not be {@code null})
     */
    public void setPlotResult(@NonNull final PlotResult plotResult) {
        this.plotResult = Objects.requireNonNull(plotResult, "plotResult must not be null");
        redraw();
    }

    /**
     * Returns the currently visible world bounds based on pane size and view state.
     *
     * @return bounds record
     */
    public VisibleWorldBounds visibleWorldBounds() {
        final double halfWidthWorld = (canvas.getWidth() / 2.0d) / pixelsPerUnit;
        final double halfHeightWorld = (canvas.getHeight() / 2.0d) / pixelsPerUnit;

        return new VisibleWorldBounds(
                centerX - halfWidthWorld,
                centerX + halfWidthWorld,
                centerY - halfHeightWorld,
                centerY + halfHeightWorld
        );
    }

    /**
     * Converts a screen x-coordinate (pixels) to a world x-coordinate.
     *
     * @param screenX screen x in pixels
     * @return world x coordinate
     */
    public double screenToWorldX(final double screenX) {
        return (screenX - canvas.getWidth() / 2.0d) / pixelsPerUnit + centerX;
    }

    /**
     * Converts a screen y-coordinate (pixels) to a world y-coordinate.
     *
     * @param screenY screen y in pixels
     * @return world y coordinate
     */
    public double screenToWorldY(final double screenY) {
        return (canvas.getHeight() / 2.0d - screenY) / pixelsPerUnit + centerY;
    }

    /**
     * Converts a world x-coordinate to a screen x-coordinate (pixels).
     *
     * @param worldX world x
     * @return screen x
     */
    public double worldToScreenX(final double worldX) {
        return (worldX - centerX) * pixelsPerUnit + canvas.getWidth() / 2.0d;
    }

    /**
     * Converts a world y-coordinate to a screen y-coordinate (pixels).
     *
     * @param worldY world y
     * @return screen y
     */
    public double worldToScreenY(final double worldY) {
        return canvas.getHeight() / 2.0d - (worldY - centerY) * pixelsPerUnit;
    }

    /**
     * Redraws the plane.
     */
    public void redraw() {
        final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        final VisibleWorldBounds bounds = visibleWorldBounds();

        renderer.render(graphicsContext, canvas.getWidth(), canvas.getHeight(), bounds, pixelsPerUnit, plotResult);

        infoLabel.setText("Center: (" + format(centerX) + ", " + format(centerY) + ")   Scale: " + format(pixelsPerUnit) + " px/unit");
    }

    /**
     * Binds the canvas size to the pane size.
     */
    private void bindCanvasToPaneSize() {
        widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            redraw();
        });
        heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            redraw();
        });
    }

    /**
     * Installs mouse handlers for panning.
     */
    private void installPanHandlers() {
        setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        setOnMouseDragged(event -> {
            final double deltaX = event.getX() - lastMouseX;
            final double deltaY = event.getY() - lastMouseY;

            centerX -= deltaX / pixelsPerUnit;
            centerY += deltaY / pixelsPerUnit;

            lastMouseX = event.getX();
            lastMouseY = event.getY();

            redraw();
        });
    }

    /**
     * Installs scroll wheel zoom handler.
     */
    private void installZoomHandlers() {
        addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() == 0) {
                return;
            }

            final double zoomFactor = Math.pow(1.0015d, event.getDeltaY());
            final double oldScale = pixelsPerUnit;
            final double newScale = clamp(oldScale * zoomFactor, MIN_PIXELS_PER_UNIT, MAX_PIXELS_PER_UNIT);

            final double mouseWorldXBefore = screenToWorldX(event.getX());
            final double mouseWorldYBefore = screenToWorldY(event.getY());

            pixelsPerUnit = newScale;

            final double mouseWorldXAfter = screenToWorldX(event.getX());
            final double mouseWorldYAfter = screenToWorldY(event.getY());

            centerX += (mouseWorldXBefore - mouseWorldXAfter);
            centerY += (mouseWorldYBefore - mouseWorldYAfter);

            redraw();
            event.consume();
        });
    }

    /**
     * Clamps a value to a min/max range.
     *
     * @param value value
     * @param min   minimum
     * @param max   maximum
     * @return clamped value
     */
    private static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Formats a double for display.
     *
     * @param value numeric value
     * @return formatted string
     */
    private static String format(final double value) {
        return String.format(java.util.Locale.ROOT, "%.4f", value);
    }
}
