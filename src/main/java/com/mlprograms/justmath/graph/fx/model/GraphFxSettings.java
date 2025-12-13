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
package com.mlprograms.justmath.graph.fx.model;
import javafx.beans.property.*;

public class GraphFxSettings {

    private final BooleanProperty showGrid = new SimpleBooleanProperty(true);
    private final BooleanProperty showAxes = new SimpleBooleanProperty(true);
    private final IntegerProperty targetGridLines = new SimpleIntegerProperty(10);

    public BooleanProperty showGridProperty() {
        return showGrid;
    }

    public BooleanProperty showAxesProperty() {
        return showAxes;
    }

    public IntegerProperty targetGridLinesProperty() {
        return targetGridLines;
    }

    public boolean isShowGrid() {
        return showGrid.get();
    }

    public boolean isShowAxes() {
        return showAxes.get();
    }

    public int getTargetGridLines() {
        return targetGridLines.get();
    }
}

