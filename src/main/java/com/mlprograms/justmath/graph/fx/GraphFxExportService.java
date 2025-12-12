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

import com.mlprograms.justmath.graph.GraphPoint;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public final class GraphFxExportService {

    private GraphFxExportService() {
    }

    public static void exportPng(final GraphFxGraphView view) {
        final File file = chooseFile("Export PNG", "png");
        if (file == null) return;

        try {
            final WritableImage img = view.snapshot(new SnapshotParameters(), null);
            writePng(img, file);
        } catch (Exception ex) {
            error(ex);
        }
    }

    public static void exportCsv(final GraphFxGraphView view, final GraphFxModel model) {
        final var poly = view.getPolylineForSelectedFunction();
        if (poly == null) return;

        final File file = chooseFile("Export CSV", "csv");
        if (file == null) return;

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write("segment,x,y\n");
            int s = 0;
            for (final List<GraphPoint> seg : poly.segments()) {
                for (final GraphPoint p : seg) {
                    w.write(s + "," + p.getX() + "," + p.getY() + "\n");
                }
                s++;
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    public static void exportJson(final GraphFxGraphView view, final GraphFxModel model) {
        final var poly = view.getPolylineForSelectedFunction();
        if (poly == null) return;

        final File file = chooseFile("Export JSON", "json");
        if (file == null) return;

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write("{\"segments\":[");
            for (int s = 0; s < poly.segments().size(); s++) {
                if (s > 0) w.write(",");
                w.write("[");
                final var seg = poly.segments().get(s);
                for (int i = 0; i < seg.size(); i++) {
                    if (i > 0) w.write(",");
                    final GraphPoint p = seg.get(i);
                    w.write("[" + p.getX() + "," + p.getY() + "]");
                }
                w.write("]");
            }
            w.write("]}");
        } catch (Exception ex) {
            error(ex);
        }
    }

    public static void exportSvg(final GraphFxGraphView view, final GraphFxModel model) {
        final var poly = view.getPolylineForSelectedFunction();
        final var f = model.getSelectedFunction();
        if (poly == null || f == null) return;

        final File file = chooseFile("Export SVG", "svg");
        if (file == null) return;

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            final double width = Math.max(1, view.getWidth());
            final double height = Math.max(1, view.getHeight());
            final String stroke = f.colorHexProperty().get();

            w.write("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + width + "\" height=\"" + height + "\">");
            w.write("<g fill=\"none\" stroke=\"" + stroke + "\" stroke-width=\"" + f.getStrokeWidth() + "\">");

            for (final List<GraphPoint> seg : poly.segments()) {
                if (seg.size() < 2) continue;
                w.write("<path d=\"");
                final GraphPoint first = seg.getFirst();
                w.write("M " + mapX(view, first.getX()) + " " + mapY(view, first.getY()) + " ");
                for (int i = 1; i < seg.size(); i++) {
                    final GraphPoint p = seg.get(i);
                    w.write("L " + mapX(view, p.getX()) + " " + mapY(view, p.getY()) + " ");
                }
                w.write("\"/>");
            }

            w.write("</g></svg>");
        } catch (Exception ex) {
            error(ex);
        }
    }

    private static void writePng(final WritableImage image, final File file) throws Exception {
        final int w = (int) image.getWidth();
        final int h = (int) image.getHeight();

        final BufferedImage buffered = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final PixelReader reader = image.getPixelReader();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                buffered.setRGB(x, y, reader.getArgb(x, y));
            }
        }

        ImageIO.write(buffered, "png", file);
    }

    private static double mapX(final GraphFxGraphView view, final double worldX) {
        final var v = view.getView();
        final double t = (worldX - v.xMin()) / (v.xMax() - v.xMin());
        return t * Math.max(1, view.getWidth());
    }

    private static double mapY(final GraphFxGraphView view, final double worldY) {
        final var v = view.getView();
        final double t = (worldY - v.yMin()) / (v.yMax() - v.yMin());
        return Math.max(1, view.getHeight()) - (t * Math.max(1, view.getHeight()));
    }

    private static File chooseFile(final String title, final String ext) {
        final FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("*." + ext, "*." + ext));
        final File chosen = fc.showSaveDialog(null);
        if (chosen == null) return null;

        final String name = chosen.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith("." + ext)) return chosen;

        return new File(chosen.getParentFile(), chosen.getName() + "." + ext);
    }

    private static void error(final Exception ex) {
        final Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
        a.setHeaderText("Export failed");
        a.showAndWait();
    }
}
