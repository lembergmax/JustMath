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

package com.mlprograms.justmath.graph.fx;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graph.GraphPoint;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class GraphFxGraphView extends StackPane {

    public enum ToolMode {
        MOVE,
        ZOOM_BOX,
        POINT_ON_FUNCTION,
        TANGENT,
        NORMAL,
        ROOT,
        INTERSECTION,
        INTEGRAL
    }

    public interface StatusListener {
        void onCursorMoved(double x, double y);
    }

    private static final MathContext MC = MathContext.DECIMAL128;

    private final GraphFxModel model;
    private final CalculatorEngine engine;

    private final Canvas canvas = new Canvas(900, 700);
    private final DecimalFormat labelFormat = new DecimalFormat("0.########");

    private final PauseTransition renderDebounce = new PauseTransition(Duration.millis(45));
    private final ScheduledExecutorService samplerExec = Executors.newSingleThreadScheduledExecutor(r -> {
        final Thread t = new Thread(r, "fx-graph-sampler");
        t.setDaemon(true);
        return t;
    });

    private final Map<UUID, FunctionCache> cache = new HashMap<>();

    private ToolMode toolMode = ToolMode.MOVE;
    private boolean interactiveMode = false;

    private WorldView view = new WorldView(-10, 10, -10, 10);

    private final Deque<WorldView> undo = new ArrayDeque<>();
    private final Deque<WorldView> redo = new ArrayDeque<>();

    private StatusListener statusListener;

    private double dragStartX;
    private double dragStartY;
    private boolean dragging;

    private double zoomBoxX;
    private double zoomBoxY;
    private double zoomBoxW;
    private double zoomBoxH;

    private UUID intersectionFirst;
    private UUID integralFunction;
    private BigDecimal integralStart;

    public GraphFxGraphView(final GraphFxModel model, final CalculatorEngine engine) {
        this.model = model;
        this.engine = engine;

        setPadding(new Insets(0));
        getChildren().add(canvas);

        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        final ChangeListener<Number> resize = (obs, o, n) -> {
            enforceAspectExpandOnly();
            requestRender();
        };
        canvas.widthProperty().addListener(resize);
        canvas.heightProperty().addListener(resize);

        renderDebounce.setOnFinished(e -> requestRenderNow());

        model.revisionProperty().addListener((obs, o, n) -> requestRender());

        model.getSettings().showGridProperty().addListener((obs, o, n) -> requestRenderNow());
        model.getSettings().showAxesProperty().addListener((obs, o, n) -> requestRenderNow());
        model.getSettings().targetGridLinesProperty().addListener((obs, o, n) -> requestRenderNow());

        installMouseHandlers();
        installKeyHandlers();

        enforceAspectExpandOnly();
        requestRenderNow();
    }

    public void setStatusListener(final StatusListener listener) {
        this.statusListener = listener;
    }

    public void setToolMode(final ToolMode toolMode) {
        this.toolMode = toolMode;
        requestRender();
    }

    public void setInteractiveMode(final boolean interactiveMode) {
        this.interactiveMode = interactiveMode;
        requestRender();
    }

    public void resetView() {
        pushUndo();
        view = new WorldView(-10, 10, -10, 10);
        enforceAspectExpandOnly();
        requestRenderNow();
    }

    public void fitToData() {
        final BigDecimal xMin = BigDecimal.valueOf(view.xMin());
        final BigDecimal xMax = BigDecimal.valueOf(view.xMax());

        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;

        final Map<String, String> vars = model.variablesAsStringMap();
        final int samples = Math.max(400, (int) canvas.getWidth());

        final BigDecimal step = xMax.subtract(xMin, MC).divide(BigDecimal.valueOf(samples), MC);

        for (final GraphFxFunction f : model.getFunctions()) {
            if (!f.isVisible()) continue;

            for (int i = 0; i <= samples; i++) {
                final BigDecimal x = (i == samples) ? xMax : xMin.add(step.multiply(BigDecimal.valueOf(i), MC), MC);
                final Double y = GraphFxAnalysisMath.evalY(engine, f.getExpression(), vars, x);
                if (y == null) continue;
                yMin = Math.min(yMin, y);
                yMax = Math.max(yMax, y);
            }
        }

        if (!Double.isFinite(yMin) || !Double.isFinite(yMax) || yMin == yMax) {
            return;
        }

        pushUndo();
        view = new WorldView(view.xMin(), view.xMax(), yMin, yMax).pad(0.08);
        enforceAspectExpandOnly();
        redo.clear();
        requestRenderNow();
    }

    private void installMouseHandlers() {
        setFocusTraversable(true);

        addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            final double wx = screenToWorldX(e.getX());
            final double wy = screenToWorldY(e.getY());
            if (statusListener != null) {
                statusListener.onCursorMoved(wx, wy);
            }
        });

        addEventHandler(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() == 0) return;

            final double factor = e.getDeltaY() > 0 ? 0.90 : 1.10;
            final double ax = screenToWorldX(e.getX());
            final double ay = screenToWorldY(e.getY());

            pushUndo();
            view = view.zoom(ax, ay, factor);
            enforceAspectExact();
            redo.clear();
            requestRenderNow();
        });

        addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            requestFocus();
            dragStartX = e.getX();
            dragStartY = e.getY();
            dragging = true;

            if (toolMode == ToolMode.ZOOM_BOX) {
                zoomBoxX = dragStartX;
                zoomBoxY = dragStartY;
                zoomBoxW = 0;
                zoomBoxH = 0;
                requestRenderNow();
                return;
            }

            if (toolMode == ToolMode.INTEGRAL) {
                final GraphFxFunction f = pickFunction(e.getX(), e.getY());
                if (f == null) return;
                integralFunction = f.getId();
                integralStart = BigDecimal.valueOf(screenToWorldX(e.getX()));
                return;
            }

            if (toolMode != ToolMode.MOVE) {
                handleToolClick(e.getX(), e.getY());
            }
        });

        addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!dragging) return;

            if (toolMode == ToolMode.ZOOM_BOX) {
                zoomBoxX = Math.min(dragStartX, e.getX());
                zoomBoxY = Math.min(dragStartY, e.getY());
                zoomBoxW = Math.abs(e.getX() - dragStartX);
                zoomBoxH = Math.abs(e.getY() - dragStartY);
                requestRenderNow();
                return;
            }

            if (toolMode == ToolMode.MOVE && e.isPrimaryButtonDown()) {
                final double dx = e.getX() - dragStartX;
                final double dy = e.getY() - dragStartY;

                final double worldDx = -dx * (view.xMax() - view.xMin()) / Math.max(1, canvas.getWidth());
                final double worldDy = dy * (view.yMax() - view.yMin()) / Math.max(1, canvas.getHeight());

                pushUndo();
                view = view.pan(worldDx, worldDy);
                enforceAspectExact();
                redo.clear();

                dragStartX = e.getX();
                dragStartY = e.getY();

                requestRenderNow();
            }
        });

        addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            dragging = false;

            if (toolMode == ToolMode.ZOOM_BOX) {
                applyZoomBox();
                zoomBoxW = 0;
                zoomBoxH = 0;
                requestRenderNow();
                return;
            }

            if (toolMode == ToolMode.INTEGRAL && integralStart != null && integralFunction != null) {
                finalizeIntegral(BigDecimal.valueOf(screenToWorldX(e.getX())));
                integralStart = null;
                integralFunction = null;
                requestRenderNow();
            }
        });
    }

    private void installKeyHandlers() {
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN).match(e)) {
                undoView();
                e.consume();
            }
            if (new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN).match(e)) {
                redoView();
                e.consume();
            }
            if (e.getCode() == KeyCode.F) {
                fitToData();
                e.consume();
            }
        });
    }

    private void undoView() {
        if (undo.isEmpty()) return;
        redo.push(view);
        view = undo.pop();
        enforceAspectExact();
        requestRenderNow();
    }

    private void redoView() {
        if (redo.isEmpty()) return;
        undo.push(view);
        view = redo.pop();
        enforceAspectExact();
        requestRenderNow();
    }

    private void pushUndo() {
        undo.push(view);
        if (undo.size() > 200) {
            while (undo.size() > 150) {
                undo.removeLast();
            }
        }
    }

    private void requestRender() {
        renderDebounce.playFromStart();
    }

    private void requestRenderNow() {
        for (final GraphFxFunction f : model.getFunctions()) {
            cache.computeIfAbsent(f.getId(), k -> new FunctionCache());
            cache.get(f.getId()).markDirty();
        }
        render();
        resampleIfNeeded();
    }

    private void render() {
        final GraphicsContext g = canvas.getGraphicsContext2D();
        final double w = canvas.getWidth();
        final double h = canvas.getHeight();

        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        g.setFont(Font.font(13));

        if (model.getSettings().isShowGrid()) {
            drawGrid(g);
        }
        if (model.getSettings().isShowAxes()) {
            drawAxes(g);
            drawTicks(g);
        }

        drawFunctions(g);
        drawObjects(g);

        if (toolMode == ToolMode.ZOOM_BOX && zoomBoxW > 4 && zoomBoxH > 4) {
            g.setFill(Color.rgb(60, 130, 255, 0.18));
            g.fillRect(zoomBoxX, zoomBoxY, zoomBoxW, zoomBoxH);
            g.setStroke(Color.rgb(60, 130, 255, 0.9));
            g.setLineWidth(1.4);
            g.strokeRect(zoomBoxX, zoomBoxY, zoomBoxW, zoomBoxH);
        }
    }

    private void drawGrid(final GraphicsContext g) {
        final double w = canvas.getWidth();
        final double h = canvas.getHeight();

        final double baseStep = GraphFxNiceTicks.niceStep(view.xMin(), view.xMax(), model.getSettings().getTargetGridLines());
        final double minorStep = baseStep / 5.0;

        drawGridLines(g, w, h, minorStep, Color.rgb(235, 235, 235), 1.0);
        drawGridLines(g, w, h, baseStep, Color.rgb(210, 210, 210), 1.0);
    }

    private void drawGridLines(final GraphicsContext g, final double w, final double h, final double step, final Color color, final double width) {
        if (!(step > 0) || !Double.isFinite(step)) return;

        g.setStroke(color);
        g.setLineWidth(width);

        final double xStart = Math.floor(view.xMin() / step) * step;
        for (double x = xStart; x <= view.xMax(); x += step) {
            final double px = worldToScreenX(x);
            g.strokeLine(px, 0, px, h);
        }

        final double yStart = Math.floor(view.yMin() / step) * step;
        for (double y = yStart; y <= view.yMax(); y += step) {
            final double py = worldToScreenY(y);
            g.strokeLine(0, py, w, py);
        }
    }

    private void drawAxes(final GraphicsContext g) {
        final double w = canvas.getWidth();
        final double h = canvas.getHeight();

        g.setStroke(Color.rgb(120, 120, 120));
        g.setLineWidth(1.8);

        if (view.containsX(0)) {
            final double x0 = worldToScreenX(0);
            g.strokeLine(x0, 0, x0, h);
        }

        if (view.containsY(0)) {
            final double y0 = worldToScreenY(0);
            g.strokeLine(0, y0, w, y0);
        }
    }

    private void drawTicks(final GraphicsContext g) {
        final double w = canvas.getWidth();
        final double h = canvas.getHeight();

        final double step = GraphFxNiceTicks.niceStep(view.xMin(), view.xMax(), model.getSettings().getTargetGridLines());
        if (!(step > 0) || !Double.isFinite(step)) return;

        g.setStroke(Color.rgb(110, 110, 110));
        g.setFill(Color.rgb(90, 90, 90));
        g.setLineWidth(1.0);

        final double yAxisPx = view.containsX(0) ? worldToScreenX(0) : 50;
        final double xAxisPy = view.containsY(0) ? worldToScreenY(0) : h - 35;

        final double xStart = Math.floor(view.xMin() / step) * step;
        for (double x = xStart; x <= view.xMax(); x += step) {
            final double px = worldToScreenX(x);
            g.strokeLine(px, xAxisPy - 4, px, xAxisPy + 4);
            g.fillText(labelFormat.format(x), px + 4, xAxisPy + 18);
        }

        final double yStart = Math.floor(view.yMin() / step) * step;
        for (double y = yStart; y <= view.yMax(); y += step) {
            final double py = worldToScreenY(y);
            g.strokeLine(yAxisPx - 4, py, yAxisPx + 4, py);
            g.fillText(labelFormat.format(y), yAxisPx + 8, py - 5);
        }
    }

    private void drawFunctions(final GraphicsContext g) {
        for (final GraphFxFunction f : model.getFunctions()) {
            if (!f.isVisible()) continue;

            final FunctionCache fc = cache.computeIfAbsent(f.getId(), k -> new FunctionCache());
            final GraphPolyline poly = fc.lastPolyline;

            g.setStroke(f.getColor());
            g.setLineWidth(f.getStrokeWidth());

            if (poly != null) {
                for (final List<GraphPoint> seg : poly.segments) {
                    drawSegment(g, seg);
                }
            }
        }
    }

    private void drawObjects(final GraphicsContext g) {
        for (final GraphFxObject o : model.getObjects()) {
            if (!o.visible()) continue;

            if (o instanceof GraphFxPointObject p) {
                g.setFill(p.style().colorWithAlpha());
                final double px = worldToScreenX(p.x().doubleValue());
                final double py = worldToScreenY(p.y().doubleValue());
                g.fillOval(px - 4.5, py - 4.5, 9, 9);
                g.setFill(Color.rgb(60, 60, 60));
                g.fillText(p.name(), px + 10, py - 8);
            }

            if (o instanceof GraphFxLineObject l) {
                final double xMin = view.xMin();
                final double xMax = view.xMax();
                final double x0 = l.x0().doubleValue();
                final double y0 = l.y0().doubleValue();
                final double m = l.slope().doubleValue();

                final double yLeft = m * (xMin - x0) + y0;
                final double yRight = m * (xMax - x0) + y0;

                g.setStroke(l.style().colorWithAlpha());
                g.setLineWidth(l.style().strokeWidth());
                g.strokeLine(worldToScreenX(xMin), worldToScreenY(yLeft), worldToScreenX(xMax), worldToScreenY(yRight));
            }

            if (o instanceof GraphFxIntegralObject it) {
                final GraphFxFunction f = model.getFunctions().stream()
                        .filter(ff -> ff.getId().equals(it.functionId()))
                        .findFirst().orElse(null);
                if (f == null) continue;

                final BigDecimal a = it.a();
                final BigDecimal b = it.b();
                final BigDecimal min = a.min(b);
                final BigDecimal max = a.max(b);

                final int n = interactiveMode ? 160 : 720;
                final BigDecimal step = max.subtract(min, MC).divide(BigDecimal.valueOf(n), MC);
                final Map<String, String> vars = model.variablesAsStringMap();

                g.setFill(it.style().colorWithAlpha());

                final List<Double> xs = new ArrayList<>();
                final List<Double> ys = new ArrayList<>();

                xs.add(worldToScreenX(min.doubleValue()));
                ys.add(worldToScreenY(0));

                for (int i = 0; i <= n; i++) {
                    final BigDecimal x = (i == n) ? max : min.add(step.multiply(BigDecimal.valueOf(i), MC), MC);
                    final Double y = GraphFxAnalysisMath.evalY(engine, f.getExpression(), vars, x);
                    if (y == null) continue;
                    xs.add(worldToScreenX(x.doubleValue()));
                    ys.add(worldToScreenY(y));
                }

                xs.add(worldToScreenX(max.doubleValue()));
                ys.add(worldToScreenY(0));

                if (xs.size() >= 4) {
                    final double[] xa = xs.stream().mapToDouble(Double::doubleValue).toArray();
                    final double[] ya = ys.stream().mapToDouble(Double::doubleValue).toArray();
                    g.fillPolygon(xa, ya, xa.length);
                }

                g.setFill(Color.rgb(55, 55, 55));
                g.fillText("∫ = " + it.value().stripTrailingZeros().toPlainString(),
                        worldToScreenX(min.doubleValue()) + 10,
                        worldToScreenY(0) - 10);
            }
        }
    }

    private void drawSegment(final GraphicsContext g, final List<GraphPoint> points) {
        if (points.size() < 2) return;

        final GraphPoint first = points.get(0);
        g.beginPath();
        g.moveTo(worldToScreenX(first.getX()), worldToScreenY(first.getY()));

        for (int i = 1; i < points.size(); i++) {
            final GraphPoint p = points.get(i);
            g.lineTo(worldToScreenX(p.getX()), worldToScreenY(p.getY()));
        }
        g.stroke();
    }

    private void resampleIfNeeded() {
        final long rev = model.revisionProperty().get();
        final WorldView viewSnapshot = view;
        final Map<String, String> varsSnapshot = model.variablesAsStringMap();
        final int samples = interactiveMode ? Math.max(420, (int) canvas.getWidth()) : Math.max(1000, (int) canvas.getWidth() * 2);

        for (final GraphFxFunction f : model.getFunctions()) {
            if (!f.isVisible()) continue;

            final FunctionCache fc = cache.computeIfAbsent(f.getId(), k -> new FunctionCache());
            if (!fc.shouldSchedule(rev, viewSnapshot, interactiveMode)) continue;

            fc.cancelScheduled();

            final long token = fc.token.incrementAndGet();
            final long delayMs = interactiveMode ? 25 : 65;

            fc.scheduled = samplerExec.schedule(() -> {
                final GraphPolyline poly = sampleFunction(f, viewSnapshot, varsSnapshot, samples);

                Platform.runLater(() -> {
                    if (token != fc.token.get()) {
                        return;
                    }
                    fc.lastPolyline = poly;
                    fc.lastRevision = rev;
                    fc.lastView = viewSnapshot;
                    fc.lastInteractive = interactiveMode;
                    render();
                });
            }, delayMs, TimeUnit.MILLISECONDS);
        }
    }

    private GraphPolyline sampleFunction(final GraphFxFunction f, final WorldView view, final Map<String, String> vars, final int samples) {
        final BigDecimal xMin = BigDecimal.valueOf(view.xMin());
        final BigDecimal xMax = BigDecimal.valueOf(view.xMax());
        final BigDecimal step = xMax.subtract(xMin, MC).divide(BigDecimal.valueOf(samples), MC);

        final List<List<GraphPoint>> segs = new ArrayList<>();
        final List<GraphPoint> current = new ArrayList<>();

        Double lastY = null;

        for (int i = 0; i <= samples; i++) {
            final BigDecimal x = (i == samples) ? xMax : xMin.add(step.multiply(BigDecimal.valueOf(i), MC), MC);
            final Double y = GraphFxAnalysisMath.evalY(engine, f.getExpression(), vars, x);

            if (y == null || !Double.isFinite(y) || Math.abs(y) > 1_000_000d) {
                flush(segs, current);
                lastY = null;
                continue;
            }

            if (lastY != null && Math.abs(y - lastY) > 150_000d) {
                flush(segs, current);
            }

            current.add(new GraphPoint(x.doubleValue(), y));
            lastY = y;
        }

        flush(segs, current);
        return new GraphPolyline(segs);
    }

    private static void flush(final List<List<GraphPoint>> segs, final List<GraphPoint> current) {
        if (current.size() >= 2) {
            segs.add(List.copyOf(current));
        }
        current.clear();
    }

    private void handleToolClick(final double sx, final double sy) {
        final GraphFxFunction f = pickFunction(sx, sy);
        if (f == null) return;

        model.setSelectedFunction(f);

        final BigDecimal x = BigDecimal.valueOf(screenToWorldX(sx));
        final Map<String, String> vars = model.variablesAsStringMap();

        if (toolMode == ToolMode.POINT_ON_FUNCTION) {
            final Double y = GraphFxAnalysisMath.evalY(engine, f.getExpression(), vars, x);
            if (y == null) return;
            model.addObject(GraphFxPointObject.of("P", x, BigDecimal.valueOf(y), f.getId()));
            return;
        }

        if (toolMode == ToolMode.TANGENT || toolMode == ToolMode.NORMAL) {
            final Double y = GraphFxAnalysisMath.evalY(engine, f.getExpression(), vars, x);
            final BigDecimal slope = GraphFxAnalysisMath.derivative(engine, f.getExpression(), vars, x);
            if (y == null || slope == null) return;

            final BigDecimal m = toolMode == ToolMode.TANGENT ? slope : invertSlope(slope);
            if (m == null) return;

            model.addObject(GraphFxLineObject.of(toolMode == ToolMode.TANGENT ? "t" : "n", x, BigDecimal.valueOf(y), m));
            return;
        }

        if (toolMode == ToolMode.ROOT) {
            final BigDecimal range = BigDecimal.valueOf(view.xMax() - view.xMin());
            final BigDecimal a = x.subtract(range.multiply(new BigDecimal("0.08")), MC);
            final BigDecimal b = x.add(range.multiply(new BigDecimal("0.08")), MC);

            final List<BigDecimal> roots = GraphFxAnalysisMath.rootsInRange(engine, f.getExpression(), vars, a, b, 260);
            if (roots.isEmpty()) return;

            final BigDecimal r = closest(roots, x);
            model.addObject(GraphFxPointObject.of("x₀", r, BigDecimal.ZERO, f.getId()));
            return;
        }

        if (toolMode == ToolMode.INTERSECTION) {
            if (intersectionFirst == null) {
                intersectionFirst = f.getId();
                return;
            }

            if (!intersectionFirst.equals(f.getId())) {
                final GraphFxFunction f1 = model.getFunctions().stream().filter(ff -> ff.getId().equals(intersectionFirst)).findFirst().orElse(null);
                final GraphFxFunction f2 = f;

                if (f1 != null) {
                    final BigDecimal xMin = BigDecimal.valueOf(view.xMin());
                    final BigDecimal xMax = BigDecimal.valueOf(view.xMax());
                    final List<BigDecimal> xs = GraphFxAnalysisMath.intersectionsInRange(engine, f1.getExpression(), f2.getExpression(), vars, xMin, xMax, 1600);

                    for (final BigDecimal xx : xs) {
                        final Double yy = GraphFxAnalysisMath.evalY(engine, f1.getExpression(), vars, xx);
                        if (yy != null) {
                            model.addObject(GraphFxPointObject.of("S", xx, BigDecimal.valueOf(yy), null));
                        }
                    }
                }
                intersectionFirst = null;
            }
        }
    }

    private void finalizeIntegral(final BigDecimal endX) {
        final GraphFxFunction f = model.getFunctions().stream().filter(ff -> ff.getId().equals(integralFunction)).findFirst().orElse(null);
        if (f == null) return;

        final BigDecimal a = integralStart;
        final BigDecimal b = endX;

        final BigDecimal value = GraphFxAnalysisMath.integralSimpson(
                engine,
                f.getExpression(),
                model.variablesAsStringMap(),
                a, b,
                interactiveMode ? 220 : 900
        );
        if (value == null) return;

        model.addObject(GraphFxIntegralObject.of(f.getId(), a, b, value, f.getColor()));
    }

    private GraphFxFunction pickFunction(final double sx, final double sy) {
        final double x = screenToWorldX(sx);
        final Map<String, String> vars = model.variablesAsStringMap();

        GraphFxFunction best = null;
        double bestDist = Double.POSITIVE_INFINITY;

        for (final GraphFxFunction f : model.getFunctions()) {
            if (!f.isVisible()) continue;

            final Double yy = GraphFxAnalysisMath.evalY(engine, f.getExpression(), vars, BigDecimal.valueOf(x));
            if (yy == null) continue;

            final double fy = worldToScreenY(yy);
            final double dist = Math.abs(fy - sy);
            if (dist < bestDist) {
                bestDist = dist;
                best = f;
            }
        }

        return bestDist <= 14 ? best : null;
    }

    private void applyZoomBox() {
        if (zoomBoxW < 10 || zoomBoxH < 10) return;

        final double x1 = screenToWorldX(zoomBoxX);
        final double x2 = screenToWorldX(zoomBoxX + zoomBoxW);

        final double y1 = screenToWorldY(zoomBoxY + zoomBoxH);
        final double y2 = screenToWorldY(zoomBoxY);

        pushUndo();
        view = new WorldView(Math.min(x1, x2), Math.max(x1, x2), Math.min(y1, y2), Math.max(y1, y2));
        enforceAspectExpandOnly();
        redo.clear();
    }

    private double worldToScreenX(final double x) {
        final double t = (x - view.xMin()) / (view.xMax() - view.xMin());
        return t * canvas.getWidth();
    }

    private double worldToScreenY(final double y) {
        final double t = (y - view.yMin()) / (view.yMax() - view.yMin());
        return canvas.getHeight() - (t * canvas.getHeight());
    }

    private double screenToWorldX(final double px) {
        final double t = px / Math.max(1, canvas.getWidth());
        return view.xMin() + t * (view.xMax() - view.xMin());
    }

    private double screenToWorldY(final double py) {
        final double t = (canvas.getHeight() - py) / Math.max(1, canvas.getHeight());
        return view.yMin() + t * (view.yMax() - view.yMin());
    }

    private void enforceAspectExact() {
        view = view.lockAspectExact(canvas.getWidth(), canvas.getHeight());
    }

    private void enforceAspectExpandOnly() {
        view = view.lockAspectExpandOnly(canvas.getWidth(), canvas.getHeight());
    }

    private static BigDecimal invertSlope(final BigDecimal slope) {
        if (slope.compareTo(BigDecimal.ZERO) == 0) return null;
        return BigDecimal.ONE.divide(slope, MC).negate();
    }

    private static BigDecimal closest(final List<BigDecimal> xs, final BigDecimal target) {
        BigDecimal best = xs.get(0);
        BigDecimal bestD = best.subtract(target).abs();
        for (final BigDecimal x : xs) {
            final BigDecimal d = x.subtract(target).abs();
            if (d.compareTo(bestD) < 0) {
                best = x;
                bestD = d;
            }
        }
        return best;
    }

    public WorldView getView() {
        return view;
    }

    public GraphPolyline getPolylineForSelectedFunction() {
        final GraphFxFunction f = model.getSelectedFunction();
        if (f == null) return null;
        final FunctionCache fc = cache.get(f.getId());
        return fc == null ? null : fc.lastPolyline;
    }

    public record GraphPolyline(List<List<GraphPoint>> segments) {
    }

    public record WorldView(double xMin, double xMax, double yMin, double yMax) {
        public WorldView {
            if (xMin >= xMax) throw new IllegalArgumentException("xMin must be < xMax");
            if (yMin >= yMax) throw new IllegalArgumentException("yMin must be < yMax");
        }

        public boolean containsX(final double x) {
            return x >= xMin && x <= xMax;
        }

        public boolean containsY(final double y) {
            return y >= yMin && y <= yMax;
        }

        public WorldView pan(final double dx, final double dy) {
            return new WorldView(xMin + dx, xMax + dx, yMin + dy, yMax + dy);
        }

        public WorldView zoom(final double ax, final double ay, final double factor) {
            final double newW = (xMax - xMin) * factor;
            final double newH = (yMax - yMin) * factor;

            final double relX = (ax - xMin) / (xMax - xMin);
            final double relY = (ay - yMin) / (yMax - yMin);

            final double nxMin = ax - relX * newW;
            final double nxMax = nxMin + newW;

            final double nyMin = ay - relY * newH;
            final double nyMax = nyMin + newH;

            return new WorldView(nxMin, nxMax, nyMin, nyMax);
        }

        public WorldView pad(final double factor) {
            final double w = xMax - xMin;
            final double h = yMax - yMin;
            return new WorldView(xMin - w * factor, xMax + w * factor, yMin - h * factor, yMax + h * factor);
        }

        public WorldView lockAspectExact(final double canvasW, final double canvasH) {
            if (!(canvasW > 0) || !(canvasH > 0)) return this;

            final double xRange = xMax - xMin;
            final double centerY = (yMin + yMax) / 2.0;

            final double desiredYRange = xRange * canvasH / canvasW;
            final double nyMin = centerY - desiredYRange / 2.0;
            final double nyMax = centerY + desiredYRange / 2.0;

            return new WorldView(xMin, xMax, nyMin, nyMax);
        }

        public WorldView lockAspectExpandOnly(final double canvasW, final double canvasH) {
            if (!(canvasW > 0) || !(canvasH > 0)) return this;

            final double xRange = xMax - xMin;
            final double yRange = yMax - yMin;

            final double desiredYRangeFromX = xRange * canvasH / canvasW;
            final double desiredXRangeFromY = yRange * canvasW / canvasH;

            final double cx = (xMin + xMax) / 2.0;
            final double cy = (yMin + yMax) / 2.0;

            if (desiredYRangeFromX >= yRange) {
                final double nyMin = cy - desiredYRangeFromX / 2.0;
                final double nyMax = cy + desiredYRangeFromX / 2.0;
                return new WorldView(xMin, xMax, nyMin, nyMax);
            }

            final double nxMin = cx - desiredXRangeFromY / 2.0;
            final double nxMax = cx + desiredXRangeFromY / 2.0;
            return new WorldView(nxMin, nxMax, yMin, yMax);
        }
    }

    private static final class FunctionCache {
        private final AtomicLong token = new AtomicLong(0);

        private volatile boolean dirty = true;
        private volatile long lastRevision = -1;
        private volatile WorldView lastView;
        private volatile boolean lastInteractive;

        private volatile ScheduledFuture<?> scheduled;
        private volatile GraphPolyline lastPolyline;

        void markDirty() {
            dirty = true;
        }

        boolean shouldSchedule(final long rev, final WorldView view, final boolean interactive) {
            if (dirty) return true;
            if (lastRevision != rev) return true;
            if (!Objects.equals(lastView, view)) return true;
            return lastInteractive != interactive;
        }

        void cancelScheduled() {
            final ScheduledFuture<?> s = scheduled;
            if (s != null) {
                s.cancel(true);
            }
        }
    }

}
