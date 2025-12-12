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


import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.List;

/**
 * Swing panel that renders GraphData and supports pan/zoom interactions.
 */
public class SwingGraphPanel extends JPanel {

    @Getter
    @Setter
    private GraphData graphData;

    @Getter
    private WorldView worldView;

    private Point lastDragPoint;

    public SwingGraphPanel() {
        this.worldView = new WorldView(-10, 10, -10, 10);
        setBackground(Color.WHITE);
        setOpaque(true);
        registerInteractions();
    }

    /**
     * Fits the current view to the bounds of the loaded GraphData.
     */
    public void fitToData() {
        if (graphData == null) {
            return;
        }
        this.worldView = WorldView.fromBounds(graphData.getBounds());
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawGrid(g2);
            drawAxes(g2);
            drawGraph(g2);

        } finally {
            g2.dispose();
        }
    }

    private void drawGrid(@NonNull final Graphics2D g2) {
        final int w = getWidth();
        final int h = getHeight();

        if (w <= 0 || h <= 0) {
            return;
        }

        g2.setColor(new Color(240, 240, 240));

        final double xStep = niceStep(worldView.getXMin(), worldView.getXMax(), 10);
        final double yStep = niceStep(worldView.getYMin(), worldView.getYMax(), 10);

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

    private void drawGraph(@NonNull final Graphics2D g2) {
        if (graphData == null) {
            return;
        }

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(0, 102, 204));

        for (final GraphSegment segment : graphData.getSegments()) {
            drawSegment(g2, segment.getPoints());
        }
    }

    private void drawSegment(@NonNull final Graphics2D g2, @NonNull final List<GraphPoint> points) {
        if (points.size() < 2) {
            return;
        }

        final Path2D path = new Path2D.Double();
        final GraphPoint first = points.get(0);

        path.moveTo(worldToScreenX(first.getX()), worldToScreenY(first.getY()));

        for (int i = 1; i < points.size(); i++) {
            final GraphPoint p = points.get(i);
            path.lineTo(worldToScreenX(p.getX()), worldToScreenY(p.getY()));
        }

        g2.draw(path);
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
                lastDragPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                lastDragPoint = null;
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
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
        });
    }

    private void onMouseWheel(@NonNull final MouseWheelEvent e) {
        final double factor = (e.getWheelRotation() < 0) ? 0.9 : 1.1;

        final double anchorX = screenToWorldX(e.getX());
        final double anchorY = screenToWorldY(e.getY());

        this.worldView = worldView.zoom(anchorX, anchorY, factor);
        repaint();
    }

    private void onMouseDragged(@NonNull final MouseEvent e) {
        if (lastDragPoint == null) {
            lastDragPoint = e.getPoint();
            return;
        }

        final int dx = e.getX() - lastDragPoint.x;
        final int dy = e.getY() - lastDragPoint.y;

        final double worldDx = -dx * (worldView.getXMax() - worldView.getXMin()) / Math.max(1, getWidth());
        final double worldDy = dy * (worldView.getYMax() - worldView.getYMin()) / Math.max(1, getHeight());

        this.worldView = worldView.pan(worldDx, worldDy);
        lastDragPoint = e.getPoint();
        repaint();
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

    /**
     * Immutable representation of the visible world rectangle.
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

        public static WorldView fromBounds(@NonNull final GraphData.GraphWorldBounds bounds) {
            final double width = Math.max(1e-9, bounds.width());
            final double height = Math.max(1e-9, bounds.height());

            final double padX = width * 0.05;
            final double padY = height * 0.05;

            return new WorldView(bounds.getXMin() - padX, bounds.getXMax() + padX, bounds.getYMin() - padY, bounds.getYMax() + padY);
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
    }

}
