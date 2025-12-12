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

import lombok.NonNull;

import javax.imageio.ImageIO;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Export helpers for PNG/SVG/CSV/JSON.
 */
public final class GraphExportService {

    private GraphExportService() {
    }

    /**
     * Exports a Swing component to PNG.
     *
     * @param component component
     * @param file      output file
     * @throws IOException io
     */
    public static void exportPng(@NonNull final Component component, @NonNull final File file) throws IOException {
        final BufferedImage img = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = img.createGraphics();
        try {
            component.paint(g2);
        } finally {
            g2.dispose();
        }
        ImageIO.write(img, "png", file);
    }

    /**
     * Exports sampled segments to CSV.
     *
     * @param segments segments
     * @param writer   writer
     * @throws IOException io
     */
    public static void exportCsv(@NonNull final List<List<GraphPoint>> segments, @NonNull final Writer writer) throws IOException {
        writer.write("segment,x,y\n");
        for (int s = 0; s < segments.size(); s++) {
            for (final GraphPoint p : segments.get(s)) {
                writer.write(s + "," + p.getX() + "," + p.getY() + "\n");
            }
        }
    }

    /**
     * Exports sampled segments to JSON.
     *
     * @param segments segments
     * @param writer   writer
     * @throws IOException io
     */
    public static void exportJson(@NonNull final List<List<GraphPoint>> segments, @NonNull final Writer writer) throws IOException {
        writer.write("{\"segments\":[");
        for (int s = 0; s < segments.size(); s++) {
            if (s > 0) {
                writer.write(",");
            }
            writer.write("[");
            final List<GraphPoint> seg = segments.get(s);
            for (int i = 0; i < seg.size(); i++) {
                if (i > 0) {
                    writer.write(",");
                }
                final GraphPoint p = seg.get(i);
                writer.write("[" + p.getX() + "," + p.getY() + "]");
            }
            writer.write("]");
        }
        writer.write("]}");
    }

    /**
     * Exports sampled segments as SVG polyline paths in screen coordinates.
     *
     * @param width    svg width
     * @param height   svg height
     * @param pathsSvg svg path fragments
     * @param file     file
     * @throws IOException io
     */
    public static void exportSvgRaw(@NonNull final String width, @NonNull final String height, @NonNull final String pathsSvg, @NonNull final File file) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + width + "\" height=\"" + height + "\">");
            w.write(pathsSvg);
            w.write("</svg>");
        }
    }

}