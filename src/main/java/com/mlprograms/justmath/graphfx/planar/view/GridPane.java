package com.mlprograms.justmath.graphfx.planar.view;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphfx.planar.model.PlotLine;
import com.mlprograms.justmath.graphfx.planar.model.PlotPoint;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

final class GridPane extends Pane {

    private final double PLOT_PIXEL_STEP = 4.0;
    private final double TARGET_GRID_PIXEL_STEP = 100.0;
    private final double MIN_SCALE = 10.0;
    private final double MAX_SCALE = 5000.0;

    private final double LABEL_MIN_PIXEL_SPACING = 60.0;
    private final double LABEL_PADDING = 6.0;
    private final double LABEL_BASELINE_OFFSET = 14.0;

    private final Font LABEL_FONT = Font.font("Consolas", 12);
    private final Font HUD_FONT = Font.font("Consolas", 13);

    private PlotResult plotResult = new PlotResult();

    private final Canvas canvas = new Canvas();

    private double scale = 80.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    private Point2D lastDragPoint;

    GridPane() {
        getChildren().add(canvas);

        bindCanvasSizeToPane();
        installPanHandlers();
        installZoomHandlers();

        redraw();
    }

    private void bindCanvasSizeToPane() {
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        widthProperty().addListener((obs, oldValue, newValue) -> redraw());
        heightProperty().addListener((obs, oldValue, newValue) -> redraw());
    }

    private void installPanHandlers() {
        setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            lastDragPoint = new Point2D(event.getX(), event.getY());
        });

        setOnMouseDragged(event -> {
            if (lastDragPoint == null) {
                return;
            }

            final Point2D currentPoint = new Point2D(event.getX(), event.getY());
            final Point2D delta = currentPoint.subtract(lastDragPoint);

            offsetX += delta.getX();
            offsetY += delta.getY();

            lastDragPoint = currentPoint;
            redraw();
        });

        setOnMouseReleased(event -> lastDragPoint = null);
    }

    private void installZoomHandlers() {
        addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() == 0.0) {
                return;
            }

            final double oldScale = scale;
            final double zoomFactor = computeZoomFactor(event.getDeltaY());
            final double newScale = clamp(oldScale * zoomFactor, MIN_SCALE, MAX_SCALE);

            applyZoomTowardsCursor(event.getX(), event.getY(), oldScale, newScale);

            redraw();
            event.consume();
        });
    }

    private double computeZoomFactor(final double deltaY) {
        return Math.pow(1.0015, deltaY);
    }

    private void applyZoomTowardsCursor(final double mouseX, final double mouseY, final double oldScale, final double newScale) {
        final double worldXBefore = screenToWorldX(mouseX, oldScale, offsetX);
        final double worldYBefore = screenToWorldY(mouseY, oldScale, offsetY);

        scale = newScale;

        offsetX = mouseX - (getWidth() / 2.0) - (worldXBefore * newScale);
        offsetY = mouseY - (getHeight() / 2.0) + (worldYBefore * newScale);
    }

    private void redraw() {
        final double width = canvas.getWidth();
        final double height = canvas.getHeight();

        if (width <= 0.0 || height <= 0.0) {
            return;
        }

        final GraphicsContext graphics = canvas.getGraphicsContext2D();

        clearBackground(graphics, width, height);

        final VisibleWorldBounds bounds = computeVisibleWorldBounds(width, height);
        final double gridStep = chooseNiceStep(TARGET_GRID_PIXEL_STEP / scale);

        drawGrid(graphics, width, height, bounds, gridStep);
        drawAxes(graphics, width, height);
        drawAxisLabels(graphics, width, height, bounds, gridStep);

        drawPlot(graphics, width, height);
        drawHud(graphics, gridStep);
    }

    private void clearBackground(final GraphicsContext graphics, final double width, final double height) {
        graphics.setFill(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
    }

    private VisibleWorldBounds computeVisibleWorldBounds(final double width, final double height) {
        final double minWorldX = screenToWorldX(0, scale, offsetX);
        final double maxWorldX = screenToWorldX(width, scale, offsetX);

        final double maxWorldY = screenToWorldY(0, scale, offsetY);
        final double minWorldY = screenToWorldY(height, scale, offsetY);

        return new VisibleWorldBounds(minWorldX, maxWorldX, minWorldY, maxWorldY);
    }

    private void drawPlot(final GraphicsContext graphics, final double width, final double height) {
        final List<PlotLine> lines = plotResult.plotLines();
        if (lines.isEmpty()) {
            return;
        }

        graphics.setLineWidth(2.0);

        for (final PlotLine line : lines) {
            final List<PlotPoint> points = line.plotPoints();
            if (points.size() < 2) {
                continue;
            }

            graphics.setStroke(Color.rgb(30, 30, 200));
            drawPolyline(graphics, width, height, points);
        }
    }

    private void drawPolyline(final GraphicsContext graphics, final double width, final double height, final List<PlotPoint> points) {
        final double[] xPixels = new double[points.size()];
        final double[] yPixels = new double[points.size()];

        int count = 0;

        for (final PlotPoint point : points) {
            final double worldX = point.x().doubleValue();
            final double worldY = point.y().doubleValue();

            if (!isFinite(worldX) || !isFinite(worldY)) {
                continue;
            }

            xPixels[count] = worldToScreenX(worldX, scale, offsetX, width);
            yPixels[count] = worldToScreenY(worldY, scale, offsetY, height);
            count++;
        }

        if (count >= 2) {
            graphics.strokePolyline(xPixels, yPixels, count);
        }
    }

    private boolean isFinite(final double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private void drawGrid(final GraphicsContext graphics, final double width, final double height, final VisibleWorldBounds bounds, final double step) {
        graphics.setStroke(Color.rgb(220, 220, 220));
        graphics.setLineWidth(1.0);

        drawVerticalGridLines(graphics, width, height, bounds.minX(), bounds.maxX(), step);
        drawHorizontalGridLines(graphics, width, height, bounds.minY(), bounds.maxY(), step);
    }

    private void drawVerticalGridLines(final GraphicsContext graphics, final double width, final double height, final double minWorldX, final double maxWorldX, final double step) {
        final double startX = floorToStep(minWorldX, step);

        for (double x = startX; x <= maxWorldX; x += step) {
            final double screenX = worldToScreenX(x, scale, offsetX, width);
            graphics.strokeLine(screenX, 0, screenX, height);
        }
    }

    private void drawHorizontalGridLines(final GraphicsContext graphics, final double width, final double height, final double minWorldY, final double maxWorldY, final double step) {
        final double startY = floorToStep(minWorldY, step);

        for (double y = startY; y <= maxWorldY; y += step) {
            final double screenY = worldToScreenY(y, scale, offsetY, height);
            graphics.strokeLine(0, screenY, width, screenY);
        }
    }

    private void drawAxes(final GraphicsContext graphics, final double width, final double height) {
        final double xAxisScreenY = worldToScreenY(0.0, scale, offsetY, height);
        final double yAxisScreenX = worldToScreenX(0.0, scale, offsetX, width);

        graphics.setStroke(Color.rgb(120, 120, 120));
        graphics.setLineWidth(2.0);

        graphics.strokeLine(0, xAxisScreenY, width, xAxisScreenY);
        graphics.strokeLine(yAxisScreenX, 0, yAxisScreenX, height);
    }

    private void drawAxisLabels(final GraphicsContext graphics, final double width, final double height, final VisibleWorldBounds bounds, final double step) {
        graphics.setFill(Color.rgb(40, 40, 40));
        graphics.setFont(LABEL_FONT);

        final double xAxisScreenY = worldToScreenY(0.0, scale, offsetY, height);
        final double yAxisScreenX = worldToScreenX(0.0, scale, offsetX, width);

        final boolean xAxisVisible = isInsideInclusive(xAxisScreenY, 0.0, height);
        final boolean yAxisVisible = isInsideInclusive(yAxisScreenX, 0.0, width);

        final double xLabelBaselineY = xAxisVisible
                ? clamp(xAxisScreenY + LABEL_BASELINE_OFFSET, LABEL_BASELINE_OFFSET, height - LABEL_PADDING)
                : height - LABEL_PADDING;

        final double yLabelBaselineX = yAxisVisible
                ? clamp(yAxisScreenX + LABEL_PADDING, LABEL_PADDING, width - LABEL_PADDING)
                : LABEL_PADDING;

        final int labelStepMultiplier = computeLabelStepMultiplier(step);

        drawXAxisLabels(graphics, width, xLabelBaselineY, bounds.minX(), bounds.maxX(), step, labelStepMultiplier);
        drawYAxisLabels(graphics, height, yLabelBaselineX, bounds.minY(), bounds.maxY(), step, labelStepMultiplier);
    }

    private void drawXAxisLabels(final GraphicsContext graphics, final double width, final double baselineY,
                                 final double minWorldX, final double maxWorldX, final double step, final int labelStepMultiplier) {
        final double labelStep = step * labelStepMultiplier;
        final double startX = floorToStep(minWorldX, labelStep);

        for (double x = startX; x <= maxWorldX; x += labelStep) {
            if (isNearZero(x)) {
                continue;
            }

            final double screenX = worldToScreenX(x, scale, offsetX, width);
            if (!isInsideInclusive(screenX, 0.0, width)) {
                continue;
            }

            final String label = formatAxisNumber(x);
            graphics.fillText(label, screenX + 2.0, baselineY);
        }

        final double originX = worldToScreenX(0.0, scale, offsetX, width);
        if (isInsideInclusive(originX, 0.0, width)) {
            graphics.fillText("0", originX + 2.0, baselineY);
        }
    }

    private void drawYAxisLabels(final GraphicsContext graphics, final double height, final double baselineX,
                                 final double minWorldY, final double maxWorldY, final double step, final int labelStepMultiplier) {
        final double labelStep = step * labelStepMultiplier;
        final double startY = floorToStep(minWorldY, labelStep);

        for (double y = startY; y <= maxWorldY; y += labelStep) {
            final double screenY = worldToScreenY(y, scale, offsetY, height);
            if (!isInsideInclusive(screenY, 0.0, height)) {
                continue;
            }

            final String label = formatAxisNumber(y);
            graphics.fillText(label, baselineX, screenY - 2.0);
        }
    }

    private int computeLabelStepMultiplier(final double gridStep) {
        final double pixelStep = gridStep * scale;
        if (pixelStep <= 0.0) {
            return 1;
        }

        final int multiplier = (int) Math.ceil(LABEL_MIN_PIXEL_SPACING / pixelStep);
        return Math.max(1, multiplier);
    }

    private String formatAxisNumber(final double value) {
        final double normalized = isNearZero(value) ? 0.0 : value;

        final double abs = Math.abs(normalized);
        if (abs >= 1_000_000 || (abs > 0.0 && abs < 0.0001)) {
            return String.format(Locale.ROOT, "%.4g", normalized);
        }

        final String fixed = String.format(Locale.ROOT, "%.6f", normalized);
        return trimTrailingZeros(fixed);
    }

    private String trimTrailingZeros(final String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0) {
            final char last = value.charAt(end - 1);
            if (last == '.' || last == ',') {
                end--;
            }
        }
        return value.substring(0, Math.max(1, end));
    }

    private boolean isNearZero(final double value) {
        return Math.abs(value) < 1e-12;
    }

    private boolean isInsideInclusive(final double value, final double min, final double max) {
        return value >= min && value <= max;
    }

    private void drawHud(final GraphicsContext graphics, final double gridStep) {
        graphics.setFill(Color.rgb(20, 20, 20, 0.85));
        graphics.setFont(HUD_FONT);
        final String text = String.format("scale=%.2f px/unit | gridStep=%.4g", scale, gridStep);
        graphics.fillText(text, 12, 20);
    }

    private double worldToScreenX(final double worldX, final double scale, final double panX, final double width) {
        return (width / 2.0) + (worldX * scale) + panX;
    }

    private double worldToScreenY(final double worldY, final double scale, final double panY, final double height) {
        return (height / 2.0) - (worldY * scale) + panY;
    }

    private double screenToWorldX(final double screenX, final double scale, final double panX) {
        return (screenX - (getWidth() / 2.0) - panX) / scale;
    }

    private double screenToWorldY(final double screenY, final double scale, final double panY) {
        return ((getHeight() / 2.0) - screenY + panY) / scale;
    }

    private double chooseNiceStep(final double rawStep) {
        if (rawStep <= 0.0 || Double.isNaN(rawStep) || Double.isInfinite(rawStep)) {
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

    private double floorToStep(final double value, final double step) {
        return Math.floor(value / step) * step;
    }

    private double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    Optional<ViewportSnapshot> tryCreateViewportSnapshot() {
        final double width = canvas.getWidth();
        final double height = canvas.getHeight();

        if (width <= 0.0 || height <= 0.0) {
            return Optional.empty();
        }

        final VisibleWorldBounds bounds = computeVisibleWorldBounds(width, height);
        final double cellSizeWorld = Math.max(1e-6, PLOT_PIXEL_STEP / scale);

        final ViewportSnapshot viewportSnapshot = new ViewportSnapshot(
                new BigNumber(Double.toString(bounds.minX()), Locale.ROOT),
                new BigNumber(Double.toString(bounds.maxX()), Locale.ROOT),
                new BigNumber(Double.toString(bounds.minY()), Locale.ROOT),
                new BigNumber(Double.toString(bounds.maxY()), Locale.ROOT),
                new BigNumber(Double.toString(cellSizeWorld), Locale.ROOT)
        );

        return Optional.of(viewportSnapshot);
    }

    void setPlotResult(final PlotResult plotResult) {
        this.plotResult = Objects.requireNonNull(plotResult, "plotResult must not be null");
        redraw();
    }

    void clearPlot() {
        this.plotResult = new PlotResult();
        redraw();
    }
}
