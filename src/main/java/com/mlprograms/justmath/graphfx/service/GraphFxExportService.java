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

package com.mlprograms.justmath.graphfx.service;

import com.mlprograms.justmath.graphfx.model.GraphFxFunction;
import com.mlprograms.justmath.graphfx.model.GraphFxModel;
import com.mlprograms.justmath.graphfx.model.GraphPoint;
import com.mlprograms.justmath.graphfx.view.GraphFxGraphView;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Utility class that exports the current graph view to common file formats.
 * <p>
 * Supported formats:
 * <ul>
 *     <li>PNG: snapshot of the complete view</li>
 *     <li>CSV: sampled polyline points of the selected function</li>
 *     <li>JSON: sampled polyline points of the selected function</li>
 *     <li>SVG: vector paths representing sampled polyline segments of the selected function</li>
 * </ul>
 * <p>
 * CSV/JSON/SVG exports require a selected function. If none is selected, the service attempts to select one.
 * If multiple functions exist, a dialog asks the user which function to export.
 */
@NoArgsConstructor
public final class GraphFxExportService {

    private final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private final String FILE_PREFIX = "justmath";
    private final String DEFAULT_FUNCTION_FILE_PART = "graph";

    /**
     * Exports a PNG snapshot of the given view.
     *
     * @param view  the graph view to snapshot (must not be {@code null})
     * @param model the graph model (must not be {@code null}); used for naming only
     */
    public void exportPng(@NonNull final GraphFxGraphView view, @NonNull final GraphFxModel model) {
        final String defaultFileName = createBaseFileName(model, "graph") + ".png";
        final File targetFile = chooseTargetFile("Export PNG", "png", defaultFileName);
        if (targetFile == null) {
            return;
        }

        try {
            final WritableImage snapshot = view.snapshot(new SnapshotParameters(), null);
            writeWritableImageAsPng(snapshot, targetFile);
        } catch (final Exception exception) {
            showExportError(exception);
        }
    }

    /**
     * Exports the sampled polyline points of the selected function as CSV.
     * <p>
     * Format:
     * <pre>
     * segment,x,y
     * 0, ...
     * 1, ...
     * </pre>
     *
     * @param view  the graph view providing the sampled polyline (must not be {@code null})
     * @param model the graph model providing selection and function list (must not be {@code null})
     */
    public void exportCsv(@NonNull final GraphFxGraphView view, @NonNull final GraphFxModel model) {
        final GraphFxFunction exportFunction = resolveExportFunction(model);
        if (exportFunction == null) {
            return;
        }

        final GraphFxGraphView.GraphPolyline polyline = view.getPolylineForSelectedFunction();
        if (polyline == null) {
            return;
        }

        final String defaultFileName = createBaseFileName(model, "points") + ".csv";
        final File targetFile = chooseTargetFile("Export CSV", "csv", defaultFileName);
        if (targetFile == null) {
            return;
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8)) {
            writer.write("segment,x,y\n");

            int segmentIndex = 0;
            for (final List<GraphPoint> segment : polyline.segments()) {
                for (final GraphPoint point : segment) {
                    writer.write(segmentIndex + "," + point.getX() + "," + point.getY() + "\n");
                }
                segmentIndex++;
            }
        } catch (final Exception exception) {
            showExportError(exception);
        }
    }

    /**
     * Exports the sampled polyline points of the selected function as JSON.
     * <p>
     * Format:
     * <pre>
     * {"segments":[ [[x,y],[x,y]], [[x,y]] ]}
     * </pre>
     *
     * @param view  the graph view providing the sampled polyline (must not be {@code null})
     * @param model the graph model providing selection and function list (must not be {@code null})
     */
    public void exportJson(@NonNull final GraphFxGraphView view, @NonNull final GraphFxModel model) {
        final GraphFxFunction exportFunction = resolveExportFunction(model);
        if (exportFunction == null) {
            return;
        }

        final GraphFxGraphView.GraphPolyline polyline = view.getPolylineForSelectedFunction();
        if (polyline == null) {
            return;
        }

        final String defaultFileName = createBaseFileName(model, "points") + ".json";
        final File targetFile = chooseTargetFile("Export JSON", "json", defaultFileName);
        if (targetFile == null) {
            return;
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8)) {
            writer.write("{\"segments\":[");

            for (int segmentIndex = 0; segmentIndex < polyline.segments().size(); segmentIndex++) {
                if (segmentIndex > 0) {
                    writer.write(",");
                }

                writer.write("[");
                final List<GraphPoint> segment = polyline.segments().get(segmentIndex);

                for (int pointIndex = 0; pointIndex < segment.size(); pointIndex++) {
                    if (pointIndex > 0) {
                        writer.write(",");
                    }

                    final GraphPoint point = segment.get(pointIndex);
                    writer.write("[" + point.getX() + "," + point.getY() + "]");
                }

                writer.write("]");
            }

            writer.write("]}");
        } catch (final Exception exception) {
            showExportError(exception);
        }
    }

    /**
     * Exports the selected function as SVG path segments.
     * <p>
     * The SVG uses the view's current size for its {@code width} and {@code height}. Points are mapped from
     * world coordinates to screen coordinates based on the current {@link GraphFxGraphView.WorldView}.
     *
     * @param view  the graph view providing the viewport and sampled polyline (must not be {@code null})
     * @param model the graph model providing selection and function properties (must not be {@code null})
     */
    public void exportSvg(@NonNull final GraphFxGraphView view, @NonNull final GraphFxModel model) {
        final GraphFxFunction exportFunction = resolveExportFunction(model);
        if (exportFunction == null) {
            return;
        }

        final GraphFxGraphView.GraphPolyline polyline = view.getPolylineForSelectedFunction();
        final GraphFxFunction selectedFunction = model.getSelectedFunction();
        if (polyline == null || selectedFunction == null) {
            return;
        }

        final String defaultFileName = createBaseFileName(model, "path") + ".svg";
        final File targetFile = chooseTargetFile("Export SVG", "svg", defaultFileName);
        if (targetFile == null) {
            return;
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8)) {
            final double svgWidth = Math.max(1, view.getWidth());
            final double svgHeight = Math.max(1, view.getHeight());
            final String strokeColorHex = selectedFunction.colorHexProperty().get();

            writer.write("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + svgWidth + "\" height=\"" + svgHeight + "\">");
            writer.write("<g fill=\"none\" stroke=\"" + strokeColorHex + "\" stroke-width=\"" + selectedFunction.getStrokeWidth() + "\">");

            for (final List<GraphPoint> segment : polyline.segments()) {
                if (segment.size() < 2) {
                    continue;
                }

                writer.write("<path d=\"");

                final GraphPoint firstPoint = segment.getFirst();
                writer.write("M " + worldToSvgX(view, firstPoint.getX()) + " " + worldToSvgY(view, firstPoint.getY()) + " ");

                for (int i = 1; i < segment.size(); i++) {
                    final GraphPoint point = segment.get(i);
                    writer.write("L " + worldToSvgX(view, point.getX()) + " " + worldToSvgY(view, point.getY()) + " ");
                }

                writer.write("\"/>");
            }

            writer.write("</g></svg>");
        } catch (final Exception exception) {
            showExportError(exception);
        }
    }

    /**
     * Resolves the function that should be exported.
     * <p>
     * Behavior:
     * <ul>
     *     <li>If a function is already selected, it is returned.</li>
     *     <li>If there are no functions, {@code null} is returned.</li>
     *     <li>If there is exactly one function, it is selected and returned.</li>
     *     <li>If there are multiple functions, a dialog prompts the user to choose one.</li>
     * </ul>
     *
     * @param model model containing function list and selected function (must not be {@code null})
     * @return the function to export, or {@code null} if none can be resolved
     */
    private GraphFxFunction resolveExportFunction(@NonNull final GraphFxModel model) {
        GraphFxFunction selectedFunction = model.getSelectedFunction();
        if (selectedFunction != null) {
            return selectedFunction;
        }

        if (model.getFunctions().isEmpty()) {
            return null;
        }

        if (model.getFunctions().size() == 1) {
            selectedFunction = model.getFunctions().getFirst();
            model.setSelectedFunction(selectedFunction);
            return selectedFunction;
        }

        final ChoiceDialog<GraphFxFunction> selectionDialog =
                new ChoiceDialog<>(model.getFunctions().getFirst(), model.getFunctions());
        selectionDialog.setTitle("Select function");
        selectionDialog.setHeaderText("Choose a function to export");
        selectionDialog.setContentText("Function:");

        return selectionDialog.showAndWait().orElse(null);
    }

    /**
     * Builds a base file name for exports.
     * <p>
     * The base name includes the selected function name (sanitized), the export kind, and a timestamp.
     *
     * @param model export context model (must not be {@code null})
     * @param kind  export kind (e.g. {@code "points"}, {@code "path"}, {@code "graph"}) (must not be {@code null})
     * @return base file name without extension
     */
    private String createBaseFileName(@NonNull final GraphFxModel model, @NonNull final String kind) {
        final GraphFxFunction selectedFunction = model.getSelectedFunction();
        final String functionPart = selectedFunction == null
                ? DEFAULT_FUNCTION_FILE_PART
                : sanitizeFileNamePart(selectedFunction.getName());

        return FILE_PREFIX + "_" + functionPart + "_" + kind + "_" + TIMESTAMP_FORMAT.format(LocalDateTime.now());
    }

    /**
     * Sanitizes an arbitrary string so it can be safely used as part of a file name across platforms.
     * <p>
     * Allowed characters: A-Z, a-z, 0-9, dot, underscore, hyphen. Everything else is replaced with {@code _}.
     * If the input is {@code null} or blank, {@code "f"} is returned.
     *
     * @param rawPart raw text (nullable)
     * @return sanitized file name part (never {@code null})
     */
    private String sanitizeFileNamePart(final String rawPart) {
        final String safe = (rawPart == null || rawPart.isBlank()) ? "f" : rawPart.trim();
        return safe.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /**
     * Writes a JavaFX {@link WritableImage} to a PNG file using a lossless ARGB buffer.
     *
     * @param image image to write (must not be {@code null})
     * @param file  destination file (must not be {@code null})
     * @throws Exception if writing fails
     */
    private void writeWritableImageAsPng(@NonNull final WritableImage image, @NonNull final File file) throws Exception {
        final int width = (int) image.getWidth();
        final int height = (int) image.getHeight();

        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, pixelReader.getArgb(x, y));
            }
        }

        ImageIO.write(bufferedImage, "png", file);
    }

    /**
     * Maps a world x-coordinate to the SVG x-coordinate in pixels for the current view and view width.
     *
     * @param view   graph view providing viewport and size (must not be {@code null})
     * @param worldX world x-coordinate
     * @return mapped x-coordinate in pixels
     */
    private double worldToSvgX(@NonNull final GraphFxGraphView view, final double worldX) {
        final GraphFxGraphView.WorldView viewport = view.getView();
        final double t = (worldX - viewport.xMin()) / (viewport.xMax() - viewport.xMin());
        return t * Math.max(1, view.getWidth());
    }

    /**
     * Maps a world y-coordinate to the SVG y-coordinate in pixels for the current view and view height.
     * <p>
     * Note that SVG (like screen coordinates) has the y-axis increasing downwards, so this mapping inverts y.
     *
     * @param view   graph view providing viewport and size (must not be {@code null})
     * @param worldY world y-coordinate
     * @return mapped y-coordinate in pixels
     */
    private double worldToSvgY(@NonNull final GraphFxGraphView view, final double worldY) {
        final GraphFxGraphView.WorldView viewport = view.getView();
        final double t = (worldY - viewport.yMin()) / (viewport.yMax() - viewport.yMin());
        final double height = Math.max(1, view.getHeight());
        return height - (t * height);
    }

    /**
     * Shows a save dialog configured for the given extension and ensures that the returned file path
     * has the expected extension.
     *
     * @param title           dialog title (must not be {@code null})
     * @param extension       file extension without dot (must not be {@code null})
     * @param initialFileName suggested initial file name (must not be {@code null})
     * @return chosen file or {@code null} if the dialog was cancelled
     */
    private File chooseTargetFile(@NonNull final String title, @NonNull final String extension, @NonNull final String initialFileName) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialFileName(initialFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*." + extension, "*." + extension));

        final File chosenFile = fileChooser.showSaveDialog(null);
        if (chosenFile == null) {
            return null;
        }

        final String lowercaseName = chosenFile.getName().toLowerCase(Locale.ROOT);
        if (lowercaseName.endsWith("." + extension)) {
            return chosenFile;
        }

        return new File(chosenFile.getParentFile(), chosenFile.getName() + "." + extension);
    }

    /**
     * Displays an error dialog informing the user that the export failed.
     *
     * @param exception failure cause (must not be {@code null})
     */
    private void showExportError(@NonNull final Exception exception) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage(), ButtonType.OK);
        alert.setHeaderText("Export failed");
        alert.showAndWait();
    }

}
