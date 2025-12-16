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

package com.mlprograms.justmath.graphfx.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Stores user-configurable display settings for the GraphFX graph view.
 * <p>
 * The settings are exposed as JavaFX properties so UI controls can bind directly to them
 * (e.g., checkboxes for grid/axes visibility and a slider/spinner for grid density).
 * </p>
 *
 * <p>
 * This class is intentionally lightweight and contains no validation logic. If the UI allows
 * values outside an intended range (e.g., negative grid line counts), validation should be
 * enforced by the controller or the UI component before setting the properties.
 * </p>
 */
public final class GraphFxSettings {

    private final BooleanProperty showGrid = new SimpleBooleanProperty(true);
    private final BooleanProperty showAxes = new SimpleBooleanProperty(true);
    private final IntegerProperty targetGridLines = new SimpleIntegerProperty(10);

    /**
     * Returns the JavaFX property that controls whether the grid is rendered.
     *
     * @return the grid visibility property (never {@code null})
     */
    public BooleanProperty showGridProperty() {
        return showGrid;
    }

    /**
     * Returns the JavaFX property that controls whether the coordinate axes are rendered.
     *
     * @return the axes visibility property (never {@code null})
     */
    public BooleanProperty showAxesProperty() {
        return showAxes;
    }

    /**
     * Returns the JavaFX property that defines the target number of major grid lines.
     * <p>
     * The graph view may treat this as a hint rather than a strict requirement, depending
     * on the tick calculation strategy.
     * </p>
     *
     * @return the target grid line count property (never {@code null})
     */
    public IntegerProperty targetGridLinesProperty() {
        return targetGridLines;
    }

    /**
     * Returns whether the grid should be shown.
     *
     * @return {@code true} if the grid is enabled, otherwise {@code false}
     */
    public boolean isShowGrid() {
        return showGrid.get();
    }

    /**
     * Returns whether the coordinate axes should be shown.
     *
     * @return {@code true} if axes are enabled, otherwise {@code false}
     */
    public boolean isShowAxes() {
        return showAxes.get();
    }

    /**
     * Returns the target number of major grid lines.
     *
     * @return the target grid line count
     */
    public int getTargetGridLines() {
        return targetGridLines.get();
    }

}
