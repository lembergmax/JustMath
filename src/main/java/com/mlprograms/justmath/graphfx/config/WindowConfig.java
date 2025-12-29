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

package com.mlprograms.justmath.graphfx.config;

import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WindowConfig {

    // --- Zoom settings ---
    public static final double INITIAL_SCALE_PX_PER_UNIT = 80.0;
    public static final double MIN_SCALE_PX_PER_UNIT = 10.0;
    public static final double MAX_SCALE_PX_PER_UNIT = 2000.0;

    public static final double ZOOM_SENSITIVITY = 0.0015;

    // --- Grid settings ---
    public static final double TARGET_MAJOR_GRID_SPACING_PX = 80.0;
    public static final int MINOR_GRID_DIVISIONS = 5;

    // --- Rendering helpers ---
    public static final double PIXEL_SNAP_OFFSET = 0.5;
    public static final double LABEL_ZERO_EPSILON = 1e-12;

    // --- Stroke widths ---
    public static final double GRID_LINE_WIDTH = 1.0;
    public static final double AXIS_LINE_WIDTH = 1.5;

    // --- Label spacing ---
    public static final double LABEL_EDGE_PADDING_PX = 6.0;
    public static final double LABEL_X_OFFSET_PX = 3.0;
    public static final double LABEL_Y_OFFSET_PX = 3.0;
    public static final double LABEL_AXIS_OFFSET_X_PX = 4.0;
    public static final double LABEL_AXIS_OFFSET_Y_PX = 4.0;

    // --- Text rendering ---
    public static final String TICK_LABEL_FORMAT_PATTERN = "0.########";
    public static final String LABEL_FONT_FAMILY = "Consolas";
    public static final double LABEL_FONT_SIZE = 12.0;

    // --- Standalone window defaults ---
    public static final String DEFAULT_WINDOW_TITLE = "GraphFx Plot Viewer";
    public static final double DEFAULT_WINDOW_WIDTH = 1000.0;
    public static final double DEFAULT_WINDOW_HEIGHT = 700.0;

    // --- Theme palettes ---
    public static final ThemePalette LIGHT_THEME = new ThemePalette(Color.rgb(250, 250, 252), Color.rgb(0, 0, 0, 0.06), Color.rgb(0, 0, 0, 0.12), Color.rgb(0, 0, 0, 0.55), Color.rgb(0, 0, 0, 0.55));
    public static final ThemePalette DARK_THEME = new ThemePalette(Color.rgb(16, 16, 18), Color.rgb(255, 255, 255, 0.06), Color.rgb(255, 255, 255, 0.12), Color.rgb(255, 255, 255, 0.60), Color.rgb(255, 255, 255, 0.55));

    public record ThemePalette(Color background, Color minorGrid, Color majorGrid, Color axis, Color label) {
    }

}