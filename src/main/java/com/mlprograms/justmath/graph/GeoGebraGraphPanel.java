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

package com.mlprograms.justmath.graph;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.graph.GraphPoint;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

/**
 * GeoGebra-like Swing panel with pan/zoom, tools, objects and exports.
 */
public class GeoGebraGraphPanel extends JPanel {

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

    @Getter
    @Setter
    @NonNull
    private GraphScene scene;

    @Getter
    @Setter
    @NonNull
    private CalculatorEngine calculatorEngine;

    @Getter
    @Setter
    private ToolMode toolMode = ToolMode.MOVE;

    private final ExecutorService samplerExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "graph-sampler");
        t.setDaemon(true);
        return t;
    });

    private volatile WorldView worldView = new WorldView(-10, 10, -10, 10);
    private final Deque<WorldView> undoView = new ArrayDeque<>();
    private final Deque<WorldView> redoView = new ArrayDeque<>();

    private Point dragStart;
    private Point lastDrag;
    private Rectangle zoomRect;

    private UUID selectedFunctionId;
    private UUID selectedObjectId;

    private long lastSeenRevision = -1;

    private final Map<UUID, FunctionCache> functionCache = new HashMap<>();
    private final DecimalFormat labelFormat = new DecimalFormat("0.########");

    private StatusListener statusListener;

    public GeoGebraGraphPanel(@NonNull final GraphScene scene, @NonNull final CalculatorEngine calculatorEngine) {
        this.scene = scene;
        this.calculatorEngine = calculatorEngine;

        setBackground(Color.WHITE);
        setOpaque(true);

        scene.addListener(c -> SwingUtilities.invokeLater(() -> {
            lastSeenRevision = -1;
            invalidateCaches();
            repaint();
        }));

        registerInteractions();
    }

    /**
     * Sets a status listener to receive live cursor coordinates.
     *
     * @param listener listener
     */
    public void setStatusListener(final StatusListener listener) {
        this.statusListener = listener;
    }

    /**
     * Fits the view to y-bounds of all visible functions in current x-range.
     */
    public void fitToData() {
        final BigDecimal xMin = BigDecimal.valueOf(worldView.getXMin());
        final BigDecimal xMax = BigDecimal.valueOf(worldView.getXMax());

        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;

        final Map<String, String> vars = buildVariables();

        for (final GraphScene.FunctionEntry f : snapshotFunctions()) {
            if (!f.isVisible()) {
                continue;
            }
            final int samples = Math.max(200, getWidth());
            final BigDecimal step = xMax.subtract(xMin).divide(BigDecimal.valueOf(samples), MathContext.DECIMAL128);

            for (int i = 0; i <= samples; i++) {
                final BigDecimal x = (i == samples) ? xMax : xMin.add(step.multiply(BigDecimal.valueOf(i), MathContext.DECIMAL128), MathContext.DECIMAL128);
                final Double y = GraphAnalysisMath.evalY(calculatorEngine, f.getExpression(), vars, x);
                if (y == null) {
                    continue;
                }
                yMin = Math.min(yMin, y);
                yMax = Math.max(yMax, y);
            }
        }

        if (!Double.isFinite(yMin) || !Double.isFinite(yMax) || yMin == yMax) {
            return;
        }

        pushUndoView();
        worldView = new WorldView(worldView.getXMin(), worldView.getXMax(), yMin, yMax).pad(0.08);
        redoView.clear();
        repaint();
    }

    /**
     * Undo last view change.
     */
    public void undoView() {
        if (undoView.isEmpty()) {
            return;
        }
        redoView.push(worldView);
        worldView = undoView.pop();
        repaint();
    }

    /**
     * Redo view change.
     */
    public void redoView() {
        if (redoView.isEmpty()) {
            return;
        }
        undoView.push(worldView);
        worldView = redoView.pop();
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            refreshCachesIfNeeded();

            if (scene.getSettings().isShowGrid()) {
                drawGrid(g2);
            }
            if (scene.getSettings().isShowAxes()) {
                drawAxes(g2);
                drawTicksAndLabels(g2);
            }

            drawFunctions(g2);
            drawObjects(g2);

            if (zoomRect != null) {
                drawZoomRect(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void refreshCachesIfNeeded() {
        final long rev = scene.getRevision();
        if (rev == lastSeenRevision) {
            return;
        }
        lastSeenRevision = rev;

        for (final GraphScene.FunctionEntry f : snapshotFunctions()) {
            functionCache.computeIfAbsent(f.getId(), k -> new FunctionCache());
            functionCache.get(f.getId()).invalidate();
        }
    }

    private void invalidateCaches() {
        for (final FunctionCache c : functionCache.values()) {
            c.invalidate();
        }
    }

    private void drawGrid(@NonNull final Graphics2D g2) {
        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        g2.setColor(new Color(240, 240, 240));

        final double xStep = niceStep(worldView.getXMin(), worldView.getXMax(), scene.getSettings().getTargetGridLines());
        final double yStep = niceStep(worldView.getYMin(), worldView.getYMax(), scene.getSettings().getTargetGridLines());

        final double xStart = Math.floor(worldView.getXMin() / xStep) * xStep;
        final double yStart = Math.floor(worldView.getYMin() / yStep) * yStep;

        for (double x = xStart; x <= worldView.getXMax(); x += xStep) {
            final int px = worldToScreenX(x);
            g2.drawLine(px, 0, px, h);
        }

        for (double y = yStart; y <= worldView.getYMax(); y += yStep) {
            final int py = worldToScreenY(y);
            g2.drawLine(0, py, w, py);
        }
    }

    private void drawAxes(@NonNull final Graphics2D g2) {
        final int w = getWidth();
        final int h = getHeight();

        g2.setColor(new Color(180, 180, 180));

        if (worldView.containsX(0)) {
            final int x0 = worldToScreenX(0);
            g2.drawLine(x0, 0, x0, h);
        }

        if (worldView.containsY(0)) {
            final int y0 = worldToScreenY(0);
            g2.drawLine(0, y0, w, y0);
        }
    }

    private void drawTicksAndLabels(@NonNull final Graphics2D g2) {
        final int w = getWidth();
        final int h = getHeight();

        final double xStep = niceStep(worldView.getXMin(), worldView.getXMax(), scene.getSettings().getTargetGridLines());
        final double yStep = niceStep(worldView.getYMin(), worldView.getYMax(), scene.getSettings().getTargetGridLines());

        final double xStart = Math.floor(worldView.getXMin() / xStep) * xStep;
        final double yStart = Math.floor(worldView.getYMin() / yStep) * yStep;

        g2.setColor(new Color(140, 140, 140));
        g2.setFont(g2.getFont().deriveFont(11f));

        final int yAxisPx = worldView.containsX(0) ? worldToScreenX(0) : 30;
        final int xAxisPy = worldView.containsY(0) ? worldToScreenY(0) : h - 20;

        for (double x = xStart; x <= worldView.getXMax(); x += xStep) {
            final int px = worldToScreenX(x);
            g2.drawLine(px, xAxisPy - 3, px, xAxisPy + 3);
            final String label = labelFormat.format(x);
            g2.drawString(label, px + 2, xAxisPy + 14);
        }

        for (double y = yStart; y <= worldView.getYMax(); y += yStep) {
            final int py = worldToScreenY(y);
            g2.drawLine(yAxisPx - 3, py, yAxisPx + 3, py);
            final String label = labelFormat.format(y);
            g2.drawString(label, yAxisPx + 6, py - 2);
        }
    }

    private void drawFunctions(@NonNull final Graphics2D g2) {
        for (final GraphScene.FunctionEntry f : snapshotFunctions()) {
            if (!f.isVisible()) {
                continue;
            }

            final FunctionCache cache = functionCache.computeIfAbsent(f.getId(), k -> new FunctionCache());
            final GraphPolyline poly = cache.getOrSchedule(this, f);
            if (poly == null) {
                continue;
            }

            g2.setStroke(strokeOf(f.getStyle()));
            g2.setColor(f.getStyle().toAwtColor());

            for (final List<GraphPoint> seg : poly.segments) {
                drawSegment(g2, seg);
            }
        }
    }

    private void drawObjects(@NonNull final Graphics2D g2) {
        for (final GraphScene.SceneObject o : snapshotObjects()) {
            if (!o.isVisible()) {
                continue;
            }

            if (o instanceof GraphScene.PointObject p) {
                drawPoint(g2, p);
            } else if (o instanceof GraphScene.LineObject l) {
                drawLine(g2, l);
            } else if (o instanceof GraphScene.IntegralObject it) {
                drawIntegral(g2, it);
            }
        }
    }

    private void drawPoint(@NonNull final Graphics2D g2, @NonNull final GraphScene.PointObject p) {
        final int px = worldToScreenX(p.getX().doubleValue());
        final int py = worldToScreenY(p.getY().doubleValue());

        g2.setColor(p.getStyle().toAwtColor());
        final int r = 4;
        g2.fillOval(px - r, py - r, 2 * r, 2 * r);

        if (p.isTraceEnabled() && !p.getTrace().isEmpty()) {
            g2.setStroke(new BasicStroke(1f));
            final Path2D path = new Path2D.Double();
            final GraphPoint first = p.getTrace().getFirst();
            path.moveTo(worldToScreenX(first.getX()), worldToScreenY(first.getY()));
            for (int i = 1; i < p.getTrace().size(); i++) {
                final GraphPoint tp = p.getTrace().get(i);
                path.lineTo(worldToScreenX(tp.getX()), worldToScreenY(tp.getY()));
            }
            g2.draw(path);
        }

        g2.setColor(new Color(80, 80, 80));
        g2.setFont(g2.getFont().deriveFont(12f));
        g2.drawString(p.getName(), px + 6, py - 6);
    }

    private void drawLine(@NonNull final Graphics2D g2, @NonNull final GraphScene.LineObject l) {
        final double xMin = worldView.getXMin();
        final double xMax = worldView.getXMax();
        final double x0 = l.getX0().doubleValue();
        final double y0 = l.getY0().doubleValue();
        final double m = l.getSlope().doubleValue();

        final double yLeft = m * (xMin - x0) + y0;
        final double yRight = m * (xMax - x0) + y0;

        g2.setStroke(strokeOf(l.getStyle()));
        g2.setColor(l.getStyle().toAwtColor());

        g2.drawLine(worldToScreenX(xMin), worldToScreenY(yLeft), worldToScreenX(xMax), worldToScreenY(yRight));

        g2.setColor(new Color(80, 80, 80));
        g2.setFont(g2.getFont().deriveFont(12f));
        g2.drawString(l.getName(), worldToScreenX(xMin) + 8, worldToScreenY(yLeft) - 8);
    }

    private void drawIntegral(@NonNull final Graphics2D g2, @NonNull final GraphScene.IntegralObject it) {
        final GraphScene.FunctionEntry f = findFunction(it.getFunctionId());
        if (f == null) {
            return;
        }

        final int n = Math.max(120, getWidth() / 4);
        final BigDecimal a = it.getA();
        final BigDecimal b = it.getB();
        final BigDecimal min = a.min(b);
        final BigDecimal max = a.max(b);

        final Map<String, String> vars = buildVariables();

        final Path2D area = new Path2D.Double();
        boolean started = false;

        final BigDecimal step = max.subtract(min).divide(BigDecimal.valueOf(n), MathContext.DECIMAL128);
        for (int i = 0; i <= n; i++) {
            final BigDecimal x = (i == n) ? max : min.add(step.multiply(BigDecimal.valueOf(i), MathContext.DECIMAL128), MathContext.DECIMAL128);
            final Double y = GraphAnalysisMath.evalY(calculatorEngine, f.getExpression(), vars, x);
            if (y == null) {
                continue;
            }

            final int px = worldToScreenX(x.doubleValue());
            final int py = worldToScreenY(y);

            if (!started) {
                area.moveTo(px, worldToScreenY(0));
                area.lineTo(px, py);
                started = true;
            } else {
                area.lineTo(px, py);
            }
        }

        if (!started) {
            return;
        }

        area.lineTo(worldToScreenX(max.doubleValue()), worldToScreenY(0));
        area.closePath();

        g2.setColor(it.getStyle().toAwtColor());
        g2.fill(area);

        g2.setColor(new Color(60, 60, 60));
        g2.setFont(g2.getFont().deriveFont(12f));
        final String label = it.getName() + " = " + it.getValue().stripTrailingZeros().toPlainString();
        g2.drawString(label, worldToScreenX(min.doubleValue()) + 6, worldToScreenY(0) - 8);
    }

    private void drawSegment(@NonNull final Graphics2D g2, @NonNull final List<GraphPoint> points) {
        if (points.size() < 2) {
            return;
        }

        final Path2D path = new Path2D.Double();
        final GraphPoint first = points.getFirst();

        path.moveTo(worldToScreenX(first.getX()), worldToScreenY(first.getY()));

        for (int i = 1; i < points.size(); i++) {
            final GraphPoint p = points.get(i);
            path.lineTo(worldToScreenX(p.getX()), worldToScreenY(p.getY()));
        }

        g2.draw(path);
    }

    private void drawZoomRect(@NonNull final Graphics2D g2) {
        g2.setColor(new Color(0, 102, 204, 80));
        g2.fill(zoomRect);
        g2.setColor(new Color(0, 102, 204, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(zoomRect);
    }

    private BasicStroke strokeOf(@NonNull final GraphScene.Style style) {
        if (!style.isDashed()) {
            return new BasicStroke(style.getStrokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        return new BasicStroke(style.getStrokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{6f, 6f}, 0f);
    }

    private int worldToScreenX(final double x) {
        final double t = (x - worldView.getXMin()) / (worldView.getXMax() - worldView.getXMin());
        return (int) Math.round(t * getWidth());
    }

    private int worldToScreenY(final double y) {
        final double t = (y - worldView.getYMin()) / (worldView.getYMax() - worldView.getYMin());
        return (int) Math.round(getHeight() - (t * getHeight()));
    }

    private double screenToWorldX(final int px) {
        final double t = px / (double) Math.max(1, getWidth());
        return worldView.getXMin() + t * (worldView.getXMax() - worldView.getXMin());
    }

    private double screenToWorldY(final int py) {
        final double t = (getHeight() - py) / (double) Math.max(1, getHeight());
        return worldView.getYMin() + t * (worldView.getYMax() - worldView.getYMin());
    }

    private void registerInteractions() {
        addMouseWheelListener(this::onMouseWheel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                onMousePressed(e);
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                onMouseReleased(e);
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e.getX(), e.getY());
                    return;
                }
                if (e.getClickCount() == 2) {
                    fitToData();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                onMouseDragged(e);
            }

            @Override
            public void mouseMoved(final MouseEvent e) {
                onMouseMoved(e);
            }
        });

        registerKeyBindings();
    }

    private void registerKeyBindings() {
        final InputMap im = getInputMap(WHEN_FOCUSED);
        final ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undoView");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redoView");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "toggleGrid");

        am.put("undoView", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                undoView();
            }
        });
        am.put("redoView", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                redoView();
            }
        });
        am.put("toggleGrid", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                scene.getSettings().setShowGrid(!scene.getSettings().isShowGrid());
                repaint();
            }
        });
    }

    private void onMouseWheel(@NonNull final MouseWheelEvent e) {
        final double factor = (e.getWheelRotation() < 0) ? 0.9 : 1.1;

        final double anchorX = screenToWorldX(e.getX());
        final double anchorY = screenToWorldY(e.getY());

        pushUndoView();
        worldView = worldView.zoom(anchorX, anchorY, factor);
        redoView.clear();
        repaint();
    }

    private void onMousePressed(@NonNull final MouseEvent e) {
        requestFocusInWindow();

        dragStart = e.getPoint();
        lastDrag = e.getPoint();

        if (toolMode == ToolMode.ZOOM_BOX && SwingUtilities.isLeftMouseButton(e)) {
            zoomRect = new Rectangle(dragStart);
        }

        if (toolMode == ToolMode.MOVE && SwingUtilities.isLeftMouseButton(e)) {
            selectedObjectId = pickObjectAt(e.getX(), e.getY());
            selectedFunctionId = pickFunctionAt(e.getX(), e.getY());
        }

        if (SwingUtilities.isLeftMouseButton(e) && toolMode != ToolMode.MOVE && toolMode != ToolMode.ZOOM_BOX) {
            handleToolClick(e.getX(), e.getY());
        }
    }

    private void onMouseReleased(@NonNull final MouseEvent e) {
        if (toolMode == ToolMode.ZOOM_BOX && zoomRect != null) {
            applyZoomRect();
            zoomRect = null;
            repaint();
        }
        dragStart = null;
        lastDrag = null;
    }

    private void onMouseDragged(@NonNull final MouseEvent e) {
        if (dragStart == null || lastDrag == null) {
            return;
        }

        if (toolMode == ToolMode.ZOOM_BOX && zoomRect != null) {
            zoomRect.setBounds(
                    Math.min(dragStart.x, e.getX()),
                    Math.min(dragStart.y, e.getY()),
                    Math.abs(e.getX() - dragStart.x),
                    Math.abs(e.getY() - dragStart.y)
            );
            repaint();
            return;
        }

        if (toolMode != ToolMode.MOVE) {
            return;
        }

        if (selectedObjectId != null) {
            dragSelectedPointIfPossible(e.getX(), e.getY());
            lastDrag = e.getPoint();
            repaint();
            return;
        }

        final int dx = e.getX() - lastDrag.x;
        final int dy = e.getY() - lastDrag.y;

        final double worldDx = -dx * (worldView.getXMax() - worldView.getXMin()) / Math.max(1, getWidth());
        final double worldDy = dy * (worldView.getYMax() - worldView.getYMin()) / Math.max(1, getHeight());

        pushUndoView();
        worldView = worldView.pan(worldDx, worldDy);
        redoView.clear();

        lastDrag = e.getPoint();
        repaint();
    }

    private void onMouseMoved(@NonNull final MouseEvent e) {
        final double x = screenToWorldX(e.getX());
        final double y = screenToWorldY(e.getY());
        if (statusListener != null) {
            statusListener.onCursorMoved(x, y);
        }
    }

    private void dragSelectedPointIfPossible(final int px, final int py) {
        final GraphScene.SceneObject o = findObject(selectedObjectId);
        if (!(o instanceof GraphScene.PointObject p)) {
            return;
        }

        final BigDecimal x = BigDecimal.valueOf(screenToWorldX(px));
        final BigDecimal y = BigDecimal.valueOf(screenToWorldY(py));

        final BigDecimal snappedX = scene.getSettings().isSnapToGrid() ? snap(x, true) : x;
        final BigDecimal snappedY = scene.getSettings().isSnapToGrid() ? snap(y, false) : y;

        GraphScene.PointObject updated = p.toBuilder().x(snappedX).y(snappedY).build();

        if (p.getFunctionId() != null) {
            final GraphScene.FunctionEntry f = findFunction(p.getFunctionId());
            if (f != null) {
                final Double yy = GraphAnalysisMath.evalY(calculatorEngine, f.getExpression(), buildVariables(), snappedX);
                if (yy != null) {
                    updated = updated.toBuilder().y(BigDecimal.valueOf(yy)).build();
                }
            }
        }

        if (updated.isTraceEnabled()) {
            final List<GraphPoint> trace = new ArrayList<>(updated.getTrace());
            trace.add(new GraphPoint(updated.getX().doubleValue(), updated.getY().doubleValue()));
            updated = updated.toBuilder().trace(trace).build();
        }

        replaceObject(updated);
    }

    private void applyZoomRect() {
        if (zoomRect.width < 10 || zoomRect.height < 10) {
            return;
        }

        final double x1 = screenToWorldX(zoomRect.x);
        final double x2 = screenToWorldX(zoomRect.x + zoomRect.width);

        final double y1 = screenToWorldY(zoomRect.y + zoomRect.height);
        final double y2 = screenToWorldY(zoomRect.y);

        pushUndoView();
        worldView = new WorldView(Math.min(x1, x2), Math.max(x1, x2), Math.min(y1, y2), Math.max(y1, y2));
        redoView.clear();
    }

    private void pushUndoView() {
        undoView.push(worldView);
        if (undoView.size() > 200) {
            while (undoView.size() > 150) {
                undoView.removeLast();
            }
        }
    }

    private void handleToolClick(final int px, final int py) {
        final BigDecimal x = BigDecimal.valueOf(screenToWorldX(px));
        final BigDecimal snappedX = scene.getSettings().isSnapToGrid() ? snap(x, true) : x;

        final UUID fId = pickFunctionAt(px, py);
        if (fId == null) {
            return;
        }
        selectedFunctionId = fId;

        final GraphScene.FunctionEntry f = findFunction(fId);
        if (f == null) {
            return;
        }

        final Map<String, String> vars = buildVariables();

        if (toolMode == ToolMode.POINT_ON_FUNCTION) {
            final Double y = GraphAnalysisMath.evalY(calculatorEngine, f.getExpression(), vars, snappedX);
            if (y == null) {
                return;
            }
            final GraphScene.PointObject p = GraphScene.PointObject.free("P", snappedX, BigDecimal.valueOf(y))
                    .toBuilder()
                    .functionId(fId)
                    .build();
            scene.addObject(p);
            selectedObjectId = p.getId();
            return;
        }

        if (toolMode == ToolMode.TANGENT || toolMode == ToolMode.NORMAL) {
            final Double y = GraphAnalysisMath.evalY(calculatorEngine, f.getExpression(), vars, snappedX);
            final BigDecimal slope = GraphAnalysisMath.derivative(calculatorEngine, f.getExpression(), vars, snappedX);
            if (y == null || slope == null) {
                return;
            }

            final BigDecimal m = toolMode == ToolMode.TANGENT ? slope : invertSlope(slope);
            if (m == null) {
                return;
            }

            final GraphScene.LineObject line = GraphScene.LineObject.of(
                    toolMode == ToolMode.TANGENT ? "t" : "n",
                    snappedX,
                    BigDecimal.valueOf(y),
                    m
            );
            scene.addObject(line);
            selectedObjectId = line.getId();
            return;
        }

        if (toolMode == ToolMode.ROOT) {
            final BigDecimal range = BigDecimal.valueOf(worldView.getXMax() - worldView.getXMin());
            final BigDecimal a = snappedX.subtract(range.multiply(new BigDecimal("0.08")), MathContext.DECIMAL128);
            final BigDecimal b = snappedX.add(range.multiply(new BigDecimal("0.08")), MathContext.DECIMAL128);

            final List<BigDecimal> roots = GraphAnalysisMath.rootsInRange(calculatorEngine, f.getExpression(), vars, a, b, 200);
            if (roots.isEmpty()) {
                return;
            }
            final BigDecimal r = closest(roots, snappedX);
            final GraphScene.PointObject p = GraphScene.PointObject.free("x0", r, BigDecimal.ZERO)
                    .toBuilder()
                    .functionId(fId)
                    .build();
            scene.addObject(p);
            selectedObjectId = p.getId();
            return;
        }

        if (toolMode == ToolMode.INTEGRAL) {
            integralDragStartX = snappedX;
            integralFunctionId = fId;
            return;
        }

        if (toolMode == ToolMode.INTERSECTION) {
            if (intersectionFirstFunctionId == null) {
                intersectionFirstFunctionId = fId;
                return;
            }
            if (!intersectionFirstFunctionId.equals(fId)) {
                final GraphScene.FunctionEntry f1 = findFunction(intersectionFirstFunctionId);
                final GraphScene.FunctionEntry f2 = findFunction(fId);
                if (f1 != null && f2 != null) {
                    final BigDecimal xMin = BigDecimal.valueOf(worldView.getXMin());
                    final BigDecimal xMax = BigDecimal.valueOf(worldView.getXMax());
                    final List<BigDecimal> xs = GraphAnalysisMath.intersectionsInRange(calculatorEngine, f1.getExpression(), f2.getExpression(), vars, xMin, xMax, 1200);
                    for (final BigDecimal xx : xs) {
                        final Double yy = GraphAnalysisMath.evalY(calculatorEngine, f1.getExpression(), vars, xx);
                        if (yy != null) {
                            scene.addObject(GraphScene.PointObject.free("S", xx, BigDecimal.valueOf(yy)).toBuilder().build());
                        }
                    }
                }
                intersectionFirstFunctionId = null;
            }
        }
    }

    private BigDecimal integralDragStartX;
    private UUID integralFunctionId;
    private UUID intersectionFirstFunctionId;

    private void showContextMenu(final int px, final int py) {
        final UUID fId = pickFunctionAt(px, py);
        final UUID oId = pickObjectAt(px, py);
        if (fId != null) {
            selectedFunctionId = fId;
        }
        if (oId != null) {
            selectedObjectId = oId;
        }

        final JPopupMenu menu = new JPopupMenu();

        final JMenuItem fit = new JMenuItem("Fit to data");
        fit.addActionListener(e -> fitToData());
        menu.add(fit);

        final JMenuItem toggleSnap = new JMenuItem(scene.getSettings().isSnapToGrid() ? "Snap to grid: off" : "Snap to grid: on");
        toggleSnap.addActionListener(e -> {
            scene.getSettings().setSnapToGrid(!scene.getSettings().isSnapToGrid());
            repaint();
        });
        menu.add(toggleSnap);

        menu.addSeparator();

        final JMenuItem valueTable = new JMenuItem("Value table…");
        valueTable.addActionListener(e -> showValueTable());
        menu.add(valueTable);

        final JMenuItem exportPng = new JMenuItem("Export PNG…");
        exportPng.addActionListener(e -> exportPng());
        menu.add(exportPng);

        final JMenuItem exportSvg = new JMenuItem("Export SVG…");
        exportSvg.addActionListener(e -> exportSvg());
        menu.add(exportSvg);

        final JMenuItem exportCsv = new JMenuItem("Export CSV…");
        exportCsv.addActionListener(e -> exportCsv());
        menu.add(exportCsv);

        final JMenuItem exportJson = new JMenuItem("Export JSON…");
        exportJson.addActionListener(e -> exportJson());
        menu.add(exportJson);

        if (oId != null) {
            menu.addSeparator();

            final JMenuItem delete = new JMenuItem("Delete object");
            delete.addActionListener(e -> scene.removeObject(oId));
            menu.add(delete);

            final GraphScene.SceneObject o = findObject(oId);
            if (o instanceof GraphScene.PointObject p) {
                final JMenuItem trace = new JMenuItem(p.isTraceEnabled() ? "Trace: off" : "Trace: on");
                trace.addActionListener(e -> replaceObject(p.toBuilder().traceEnabled(!p.isTraceEnabled()).build()));
                menu.add(trace);

                final JMenuItem clearTrace = new JMenuItem("Clear trace");
                clearTrace.addActionListener(e -> replaceObject(p.toBuilder().trace(new ArrayList<>()).build()));
                menu.add(clearTrace);
            }
        }

        menu.show(this, px, py);
    }

    private void showValueTable() {
        final GraphScene.FunctionEntry f = selectedFunctionId != null ? findFunction(selectedFunctionId) : null;
        if (f == null) {
            return;
        }

        final BigDecimal xMin = BigDecimal.valueOf(worldView.getXMin());
        final BigDecimal xMax = BigDecimal.valueOf(worldView.getXMax());
        final int rows = 40;
        final BigDecimal step = xMax.subtract(xMin).divide(BigDecimal.valueOf(rows), MathContext.DECIMAL128);

        final Map<String, String> vars = buildVariables();

        final String[] cols = {"x", "f(x)"};
        final Object[][] data = new Object[rows + 1][2];

        for (int i = 0; i <= rows; i++) {
            final BigDecimal x = (i == rows) ? xMax : xMin.add(step.multiply(BigDecimal.valueOf(i), MathContext.DECIMAL128), MathContext.DECIMAL128);
            final Double y = GraphAnalysisMath.evalY(calculatorEngine, f.getExpression(), vars, x);
            data[i][0] = x.stripTrailingZeros().toPlainString();
            data[i][1] = y == null ? "—" : labelFormat.format(y);
        }

        final JTable table = new JTable(data, cols);
        table.setFillsViewportHeight(true);

        final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Value table: " + f.getName(), Dialog.ModalityType.MODELESS);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void exportPng() {
        final File f = chooseFile("Export PNG", "png");
        if (f == null) {
            return;
        }
        try {
            GraphExportService.exportPng(this, f);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void exportSvg() {
        final File file = chooseFile("Export SVG", "svg");
        if (file == null) {
            return;
        }

        try {
            final String paths = buildSvgPaths();
            GraphExportService.exportSvgRaw(String.valueOf(getWidth()), String.valueOf(getHeight()), paths, file);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private String buildSvgPaths() {
        final StringBuilder sb = new StringBuilder();

        for (final GraphScene.FunctionEntry f : snapshotFunctions()) {
            if (!f.isVisible()) {
                continue;
            }

            final FunctionCache cache = functionCache.computeIfAbsent(f.getId(), k -> new FunctionCache());
            final GraphPolyline poly = cache.lastPolyline;
            if (poly == null) {
                continue;
            }

            final String color = String.format("#%02x%02x%02x", f.getStyle().getColor().getRed(), f.getStyle().getColor().getGreen(), f.getStyle().getColor().getBlue());
            final float stroke = f.getStyle().getStrokeWidth();

            for (final List<GraphPoint> seg : poly.segments) {
                if (seg.size() < 2) {
                    continue;
                }

                sb.append("<path fill=\"none\" stroke=\"").append(color).append("\" stroke-width=\"").append(stroke).append("\" d=\"");

                final GraphPoint first = seg.getFirst();
                sb.append("M ").append(worldToScreenX(first.getX())).append(" ").append(worldToScreenY(first.getY())).append(" ");

                for (int i = 1; i < seg.size(); i++) {
                    final GraphPoint p = seg.get(i);
                    sb.append("L ").append(worldToScreenX(p.getX())).append(" ").append(worldToScreenY(p.getY())).append(" ");
                }

                sb.append("\"/>\n");
            }
        }

        return sb.toString();
    }

    private void exportCsv() {
        final GraphScene.FunctionEntry f = selectedFunctionId != null ? findFunction(selectedFunctionId) : null;
        if (f == null) {
            return;
        }

        final FunctionCache cache = functionCache.computeIfAbsent(f.getId(), k -> new FunctionCache());
        final GraphPolyline poly = cache.lastPolyline;
        if (poly == null) {
            return;
        }

        final File out = chooseFile("Export CSV", "csv");
        if (out == null) {
            return;
        }

        try (var w = new java.io.FileWriter(out)) {
            GraphExportService.exportCsv(poly.segments, w);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void exportJson() {
        final GraphScene.FunctionEntry f = selectedFunctionId != null ? findFunction(selectedFunctionId) : null;
        if (f == null) {
            return;
        }

        final FunctionCache cache = functionCache.computeIfAbsent(f.getId(), k -> new FunctionCache());
        final GraphPolyline poly = cache.lastPolyline;
        if (poly == null) {
            return;
        }

        final File out = chooseFile("Export JSON", "json");
        if (out == null) {
            return;
        }

        try (var w = new java.io.FileWriter(out)) {
            GraphExportService.exportJson(poly.segments, w);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private File chooseFile(@NonNull final String title, @NonNull final String ext) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase(Locale.ROOT).endsWith("." + ext)) {
            f = new File(f.getParentFile(), f.getName() + "." + ext);
        }
        return f;
    }

    private void showError(@NonNull final Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private UUID pickFunctionAt(final int px, final int py) {
        final double x = screenToWorldX(px);
        final double y = screenToWorldY(py);

        final Map<String, String> vars = buildVariables();

        UUID best = null;
        double bestDist = Double.POSITIVE_INFINITY;

        for (final GraphScene.FunctionEntry f : snapshotFunctions()) {
            if (!f.isVisible()) {
                continue;
            }

            final Double yy = GraphAnalysisMath.evalY(calculatorEngine, f.getExpression(), vars, BigDecimal.valueOf(x));
            if (yy == null) {
                continue;
            }

            final int fy = worldToScreenY(yy);
            final double distPx = Math.abs(fy - py);
            if (distPx < bestDist) {
                bestDist = distPx;
                best = f.getId();
            }
        }

        return (bestDist <= 12) ? best : null;
    }

    private UUID pickObjectAt(final int px, final int py) {
        final double x = screenToWorldX(px);
        final double y = screenToWorldY(py);

        UUID best = null;
        double bestDist = Double.POSITIVE_INFINITY;

        for (final GraphScene.SceneObject o : snapshotObjects()) {
            if (!o.isVisible()) {
                continue;
            }
            if (o instanceof GraphScene.PointObject p) {
                final double dx = p.getX().doubleValue() - x;
                final double dy = p.getY().doubleValue() - y;
                final double d = Math.hypot(dx, dy);
                if (d < bestDist) {
                    bestDist = d;
                    best = o.getId();
                }
            }
        }

        final double tolWorld = (worldView.getXMax() - worldView.getXMin()) * 0.01;
        return (bestDist <= tolWorld) ? best : null;
    }

    private void replaceObject(@NonNull final GraphScene.SceneObject updated) {
        synchronized (scene) {
            final List<GraphScene.SceneObject> list = scene.getObjects();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(updated.getId())) {
                    list.set(i, updated);
                    break;
                }
            }
        }
        repaint();
    }

    private GraphScene.FunctionEntry findFunction(@NonNull final UUID id) {
        for (final GraphScene.FunctionEntry f : snapshotFunctions()) {
            if (f.getId().equals(id)) {
                return f;
            }
        }
        return null;
    }

    private GraphScene.SceneObject findObject(@NonNull final UUID id) {
        for (final GraphScene.SceneObject o : snapshotObjects()) {
            if (o.getId().equals(id)) {
                return o;
            }
        }
        return null;
    }

    private List<GraphScene.FunctionEntry> snapshotFunctions() {
        synchronized (scene) {
            return List.copyOf(scene.getFunctions());
        }
    }

    private List<GraphScene.SceneObject> snapshotObjects() {
        synchronized (scene) {
            return List.copyOf(scene.getObjects());
        }
    }

    private Map<String, String> buildVariables() {
        final Map<String, String> vars = new HashMap<>();
        synchronized (scene) {
            for (final GraphScene.VariableEntry v : scene.getVariables().values()) {
                vars.put(v.getName(), v.getValue().stripTrailingZeros().toPlainString());
            }
        }
        return vars;
    }

    private BigDecimal snap(@NonNull final BigDecimal v, final boolean xAxis) {
        final double step = niceStep(xAxis ? worldView.getXMin() : worldView.getYMin(), xAxis ? worldView.getXMax() : worldView.getYMax(), scene.getSettings().getTargetGridLines());
        final BigDecimal s = BigDecimal.valueOf(step);
        if (s.compareTo(BigDecimal.ZERO) == 0) {
            return v;
        }
        return v.divide(s, 0, java.math.RoundingMode.HALF_UP).multiply(s);
    }

    private static BigDecimal invertSlope(@NonNull final BigDecimal slope) {
        if (slope.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return BigDecimal.ONE.divide(slope, MathContext.DECIMAL128).negate();
    }

    private static BigDecimal closest(@NonNull final List<BigDecimal> xs, @NonNull final BigDecimal target) {
        BigDecimal best = xs.getFirst();
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

    private static double niceStep(final double min, final double max, final int targetLines) {
        final double range = Math.abs(max - min);
        if (range == 0d || !Double.isFinite(range)) {
            return 1d;
        }

        final double rough = range / Math.max(2, targetLines);
        final double pow10 = Math.pow(10, Math.floor(Math.log10(rough)));
        final double norm = rough / pow10;

        final double nice;
        if (norm < 1.5) {
            nice = 1;
        } else if (norm < 3) {
            nice = 2;
        } else if (norm < 7) {
            nice = 5;
        } else {
            nice = 10;
        }
        return nice * pow10;
    }

    private static final class GraphPolyline {
        private final List<List<GraphPoint>> segments;

        private GraphPolyline(@NonNull final List<List<GraphPoint>> segments) {
            this.segments = segments;
        }
    }

    private final class FunctionCache {
        private volatile long computedForRevision = -1;
        private volatile WorldView computedForView;
        private volatile GraphPolyline lastPolyline;
        private volatile Future<?> runningJob;

        private void invalidate() {
            computedForRevision = -1;
            computedForView = null;
            lastPolyline = null;
            final Future<?> job = runningJob;
            if (job != null) {
                job.cancel(true);
            }
            runningJob = null;
        }

        private GraphPolyline getOrSchedule(@NonNull final GeoGebraGraphPanel panel, @NonNull final GraphScene.FunctionEntry f) {
            final long rev = scene.getRevision();
            if (lastPolyline != null && computedForRevision == rev && computedForView != null && computedForView.equals(worldView)) {
                return lastPolyline;
            }

            if (runningJob == null || runningJob.isDone() || runningJob.isCancelled()) {
                final WorldView viewSnapshot = worldView;
                final Map<String, String> vars = buildVariables();
                runningJob = samplerExecutor.submit(() -> {
                    final GraphPolyline poly = sampleFunction(f, viewSnapshot, vars);
                    SwingUtilities.invokeLater(() -> {
                        lastPolyline = poly;
                        computedForRevision = rev;
                        computedForView = viewSnapshot;
                        repaint();
                    });
                });
            }

            return lastPolyline;
        }
    }

    private GraphPolyline sampleFunction(@NonNull final GraphScene.FunctionEntry f, @NonNull final WorldView view, @NonNull final Map<String, String> vars) {
        final int samples = Math.max(300, getWidth() * 2);
        final BigDecimal xMin = BigDecimal.valueOf(view.getXMin());
        final BigDecimal xMax = BigDecimal.valueOf(view.getXMax());
        final BigDecimal step = xMax.subtract(xMin).divide(BigDecimal.valueOf(samples), MathContext.DECIMAL128);

        final List<List<GraphPoint>> segs = new ArrayList<>();
        List<GraphPoint> current = new ArrayList<>();

        Double lastY = null;
        for (int i = 0; i <= samples; i++) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            final BigDecimal x = (i == samples) ? xMax : xMin.add(step.multiply(BigDecimal.valueOf(i), MathContext.DECIMAL128), MathContext.DECIMAL128);
            final Double y = GraphAnalysisMath.evalY(calculatorEngine, f.getExpression(), vars, x);

            if (y == null || Math.abs(y) > 1_000_000) {
                flushSegment(segs, current);
                lastY = null;
                continue;
            }

            if (lastY != null && Math.abs(y - lastY) > 1_000_000) {
                flushSegment(segs, current);
            }

            current.add(new GraphPoint(x.doubleValue(), y));
            lastY = y;
        }

        flushSegment(segs, current);
        return new GraphPolyline(segs);
    }

    private static void flushSegment(@NonNull final List<List<GraphPoint>> segs, @NonNull final List<GraphPoint> current) {
        if (current.size() >= 2) {
            segs.add(List.copyOf(current));
        }
        current.clear();
    }

    /**
     * Listener for cursor coordinates.
     */
    public interface StatusListener {
        void onCursorMoved(double x, double y);
    }

    /**
     * Immutable visible world rectangle.
     */
    @Getter
    public static final class WorldView {
        private final double xMin;
        private final double xMax;
        private final double yMin;
        private final double yMax;

        public WorldView(final double xMin, final double xMax, final double yMin, final double yMax) {
            if (xMin >= xMax) {
                throw new IllegalArgumentException("xMin must be < xMax");
            }
            if (yMin >= yMax) {
                throw new IllegalArgumentException("yMin must be < yMax");
            }
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
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

        public WorldView zoom(final double anchorX, final double anchorY, final double factor) {
            final double newWidth = (xMax - xMin) * factor;
            final double newHeight = (yMax - yMin) * factor;

            final double relX = (anchorX - xMin) / (xMax - xMin);
            final double relY = (anchorY - yMin) / (yMax - yMin);

            final double nxMin = anchorX - relX * newWidth;
            final double nxMax = nxMin + newWidth;

            final double nyMin = anchorY - relY * newHeight;
            final double nyMax = nyMin + newHeight;

            return new WorldView(nxMin, nxMax, nyMin, nyMax);
        }

        public WorldView pad(final double factor) {
            final double w = xMax - xMin;
            final double h = yMax - yMin;
            final double px = w * factor;
            final double py = h * factor;
            return new WorldView(xMin - px, xMax + px, yMin - py, yMax + py);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof WorldView other)) return false;
            return Double.compare(xMin, other.xMin) == 0
                    && Double.compare(xMax, other.xMax) == 0
                    && Double.compare(yMin, other.yMin) == 0
                    && Double.compare(yMax, other.yMax) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(xMin, xMax, yMin, yMax);
        }
    }

}
