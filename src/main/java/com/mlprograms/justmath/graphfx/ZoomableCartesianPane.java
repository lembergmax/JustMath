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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.text.DecimalFormat;

public final class ZoomableCartesianPane extends Region {

    private final Canvas canvas;

    private final DoubleProperty scalePxPerUnit;
    private final DoubleProperty offsetX;
    private final DoubleProperty offsetY;

    private final double minScalePxPerUnit;
    private final double maxScalePxPerUnit;

    private final BooleanProperty primaryButtonPanningEnabled;

    private final ObjectProperty<CartesianTheme> theme;

    private final DecimalFormat tickFormat;

    private Color backgroundColor;
    private Color majorGridColor;
    private Color minorGridColor;
    private Color axisColor;
    private Color labelColor;

    private Point2D lastDragPoint;

    public ZoomableCartesianPane() {
        this(80.0, 10.0, 2000.0, CartesianTheme.LIGHT);
    }

    public ZoomableCartesianPane(final CartesianTheme theme) {
        this(80.0, 10.0, 2000.0, theme);
    }

    public ZoomableCartesianPane(final double initialScalePxPerUnit,
                                 final double minScalePxPerUnit,
                                 final double maxScalePxPerUnit) {
        this(initialScalePxPerUnit, minScalePxPerUnit, maxScalePxPerUnit, CartesianTheme.LIGHT);
    }

    public ZoomableCartesianPane(final double initialScalePxPerUnit,
                                 final double minScalePxPerUnit,
                                 final double maxScalePxPerUnit,
                                 final CartesianTheme initialTheme) {
        this.canvas = new Canvas();

        this.scalePxPerUnit = new SimpleDoubleProperty(initialScalePxPerUnit);
        this.offsetX = new SimpleDoubleProperty(0.0);
        this.offsetY = new SimpleDoubleProperty(0.0);

        this.minScalePxPerUnit = minScalePxPerUnit;
        this.maxScalePxPerUnit = maxScalePxPerUnit;

        this.primaryButtonPanningEnabled = new SimpleBooleanProperty(true);

        this.theme = new SimpleObjectProperty<>(initialTheme == null ? CartesianTheme.LIGHT : initialTheme);

        this.tickFormat = new DecimalFormat("0.########");

        applyTheme(this.theme.get());
        this.theme.addListener((obs, oldTheme, newTheme) -> {
            applyTheme(newTheme == null ? CartesianTheme.LIGHT : newTheme);
            draw();
        });

        getChildren().add(canvas);

        final InvalidationListener redraw = obs -> draw();

        widthProperty().addListener((obs, o, n) -> resizeCanvas());
        heightProperty().addListener((obs, o, n) -> resizeCanvas());

        scalePxPerUnit.addListener(redraw);
        offsetX.addListener(redraw);
        offsetY.addListener(redraw);

        layoutBoundsProperty().addListener((obs, o, bounds) -> {
            if (bounds.getWidth() > 0 && bounds.getHeight() > 0 && offsetX.get() == 0.0 && offsetY.get() == 0.0) {
                centerOrigin();
            }
        });

        addEventFilter(ScrollEvent.SCROLL, this::onScrollZoom);

        setCursor(Cursor.OPEN_HAND);

        setOnMousePressed(e -> {
            if (isPanningButton(e.getButton())) {
                lastDragPoint = new Point2D(e.getX(), e.getY());
                setCursor(Cursor.CLOSED_HAND);
                e.consume();
            }
        });

        setOnMouseDragged(e -> {
            if (lastDragPoint == null) {
                return;
            }

            final Point2D current = new Point2D(e.getX(), e.getY());
            final Point2D delta = current.subtract(lastDragPoint);

            offsetX.set(offsetX.get() + delta.getX());
            offsetY.set(offsetY.get() + delta.getY());

            lastDragPoint = current;

            e.consume();
        });

        setOnMouseReleased(e -> {
            lastDragPoint = null;
            setCursor(Cursor.OPEN_HAND);
        });
    }

    public ObjectProperty<CartesianTheme> themeProperty() {
        return theme;
    }

    public CartesianTheme getTheme() {
        return theme.get();
    }

    public void setTheme(final CartesianTheme theme) {
        this.theme.set(theme == null ? CartesianTheme.LIGHT : theme);
    }

    public BooleanProperty primaryButtonPanningEnabledProperty() {
        return primaryButtonPanningEnabled;
    }

    public void setPrimaryButtonPanningEnabled(final boolean enabled) {
        primaryButtonPanningEnabled.set(enabled);
    }

    public boolean isPrimaryButtonPanningEnabled() {
        return primaryButtonPanningEnabled.get();
    }

    public void centerOrigin() {
        offsetX.set(getWidth() / 2.0);
        offsetY.set(getHeight() / 2.0);
    }

    public DoubleProperty scalePxPerUnitProperty() {
        return scalePxPerUnit;
    }

    public DoubleProperty offsetXProperty() {
        return offsetX;
    }

    public DoubleProperty offsetYProperty() {
        return offsetY;
    }

    @Override
    protected void layoutChildren() {
        canvas.relocate(0, 0);
        super.layoutChildren();
    }

    private void applyTheme(final CartesianTheme theme) {
        if (theme == CartesianTheme.DARK) {
            backgroundColor = Color.rgb(16, 16, 18);
            minorGridColor = Color.rgb(255, 255, 255, 0.06);
            majorGridColor = Color.rgb(255, 255, 255, 0.12);
            axisColor = Color.rgb(255, 255, 255, 0.60);
            labelColor = Color.rgb(255, 255, 255, 0.55);
        } else {
            backgroundColor = Color.rgb(250, 250, 252);
            minorGridColor = Color.rgb(0, 0, 0, 0.06);
            majorGridColor = Color.rgb(0, 0, 0, 0.12);
            axisColor = Color.rgb(0, 0, 0, 0.55);
            labelColor = Color.rgb(0, 0, 0, 0.55);
        }
    }

    private void resizeCanvas() {
        final double width = Math.max(1, getWidth());
        final double height = Math.max(1, getHeight());

        canvas.setWidth(width);
        canvas.setHeight(height);

        draw();
    }

    private boolean isPanningButton(final MouseButton button) {
        if (button == MouseButton.MIDDLE || button == MouseButton.SECONDARY) {
            return true;
        }
        return button == MouseButton.PRIMARY && isPrimaryButtonPanningEnabled();
    }

    private void onScrollZoom(final ScrollEvent e) {
        final double zoomFactor = Math.exp(e.getDeltaY() * 0.0015);

        final double oldScale = scalePxPerUnit.get();
        final double newScale = clamp(oldScale * zoomFactor, minScalePxPerUnit, maxScalePxPerUnit);

        if (newScale == oldScale) {
            return;
        }

        final double mouseX = e.getX();
        final double mouseY = e.getY();

        final double worldXUnderMouse = screenToWorldX(mouseX);
        final double worldYUnderMouse = screenToWorldY(mouseY);

        scalePxPerUnit.set(newScale);

        offsetX.set(mouseX - worldXUnderMouse * newScale);
        offsetY.set(mouseY + worldYUnderMouse * newScale);

        e.consume();
    }

    private void draw() {
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        final double w = canvas.getWidth();
        final double h = canvas.getHeight();

        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, w, h);

        gc.setFont(Font.font("Consolas", 12));

        drawGrid(gc, w, h);
        drawAxes(gc, w, h);
    }

    private void drawGrid(final GraphicsContext gc, final double w, final double h) {
        final double pxPerUnit = scalePxPerUnit.get();
        final double targetGridSpacingPx = 80.0;

        final double majorStepWorld = niceStep(targetGridSpacingPx / pxPerUnit);
        final double minorStepWorld = majorStepWorld / 5.0;

        final double minWorldX = screenToWorldX(0);
        final double maxWorldX = screenToWorldX(w);
        final double minWorldY = screenToWorldY(h);
        final double maxWorldY = screenToWorldY(0);

        gc.setStroke(minorGridColor);
        gc.setLineWidth(1);
        drawVerticalLines(gc, h, minWorldX, maxWorldX, minorStepWorld);
        drawHorizontalLines(gc, w, minWorldY, maxWorldY, minorStepWorld);

        gc.setStroke(majorGridColor);
        gc.setLineWidth(1);
        drawVerticalLines(gc, h, minWorldX, maxWorldX, majorStepWorld);
        drawHorizontalLines(gc, w, minWorldY, maxWorldY, majorStepWorld);

        gc.setFill(labelColor);
        drawTickLabels(gc, w, h, minWorldX, maxWorldX, minWorldY, maxWorldY, majorStepWorld);
    }

    private void drawAxes(final GraphicsContext gc, final double w, final double h) {
        final double xAxisY = worldToScreenY(0);
        final double yAxisX = worldToScreenX(0);

        gc.setStroke(axisColor);
        gc.setLineWidth(1.5);

        if (isBetween(yAxisX, 0, w)) {
            final double snapped = Math.round(yAxisX) + 0.5;
            gc.strokeLine(snapped, 0, snapped, h);
        }

        if (isBetween(xAxisY, 0, h)) {
            final double snapped = Math.round(xAxisY) + 0.5;
            gc.strokeLine(0, snapped, w, snapped);
        }
    }

    private void drawVerticalLines(final GraphicsContext gc,
                                   final double canvasHeight,
                                   final double minWorldX,
                                   final double maxWorldX,
                                   final double stepWorld) {
        if (stepWorld <= 0) {
            return;
        }

        final double start = Math.floor(minWorldX / stepWorld) * stepWorld;
        for (double x = start; x <= maxWorldX; x += stepWorld) {
            final double sx = worldToScreenX(x);
            final double snapped = Math.round(sx) + 0.5;
            gc.strokeLine(snapped, 0, snapped, canvasHeight);
        }
    }

    private void drawHorizontalLines(final GraphicsContext gc,
                                     final double canvasWidth,
                                     final double minWorldY,
                                     final double maxWorldY,
                                     final double stepWorld) {
        if (stepWorld <= 0) {
            return;
        }

        final double start = Math.floor(minWorldY / stepWorld) * stepWorld;
        for (double y = start; y <= maxWorldY; y += stepWorld) {
            final double sy = worldToScreenY(y);
            final double snapped = Math.round(sy) + 0.5;
            gc.strokeLine(0, snapped, canvasWidth, snapped);
        }
    }

    private void drawTickLabels(final GraphicsContext gc,
                                final double w,
                                final double h,
                                final double minWorldX,
                                final double maxWorldX,
                                final double minWorldY,
                                final double maxWorldY,
                                final double stepWorld) {

        final boolean yAxisVisible = isBetween(worldToScreenX(0), 0, w);
        final boolean xAxisVisible = isBetween(worldToScreenY(0), 0, h);

        final double labelBaselineY = xAxisVisible ? worldToScreenY(0) : (h - 6.0);
        final double labelAnchorX = yAxisVisible ? worldToScreenX(0) : 6.0;

        final double startX = Math.floor(minWorldX / stepWorld) * stepWorld;
        for (double x = startX; x <= maxWorldX; x += stepWorld) {
            if (Math.abs(x) < 1e-12) {
                continue;
            }

            final double sx = worldToScreenX(x);
            if (!isBetween(sx, 0, w)) {
                continue;
            }

            gc.fillText(tickFormat.format(x), sx + 3, labelBaselineY - 3);
        }

        final double startY = Math.floor(minWorldY / stepWorld) * stepWorld;
        for (double y = startY; y <= maxWorldY; y += stepWorld) {
            if (Math.abs(y) < 1e-12) {
                continue;
            }

            final double sy = worldToScreenY(y);
            if (!isBetween(sy, 0, h)) {
                continue;
            }

            gc.fillText(tickFormat.format(y), labelAnchorX + 4, sy - 4);
        }
    }

    private double worldToScreenX(final double worldX) {
        return offsetX.get() + worldX * scalePxPerUnit.get();
    }

    private double worldToScreenY(final double worldY) {
        return offsetY.get() - worldY * scalePxPerUnit.get();
    }

    private double screenToWorldX(final double screenX) {
        return (screenX - offsetX.get()) / scalePxPerUnit.get();
    }

    private double screenToWorldY(final double screenY) {
        return (offsetY.get() - screenY) / scalePxPerUnit.get();
    }

    private static double niceStep(final double rawStep) {
        if (rawStep <= 0) {
            return 1.0;
        }

        final double exp = Math.floor(Math.log10(rawStep));
        final double base = rawStep / Math.pow(10, exp);

        final double niceBase;
        if (base < 1.5) {
            niceBase = 1.0;
        } else if (base < 3.5) {
            niceBase = 2.0;
        } else if (base < 7.5) {
            niceBase = 5.0;
        } else {
            niceBase = 10.0;
        }

        return niceBase * Math.pow(10, exp);
    }

    private static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean isBetween(final double value, final double min, final double max) {
        return value >= min && value <= max;
    }

}
