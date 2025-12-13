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
import javafx.scene.paint.Color;

import java.util.UUID;

public class GraphFxFunction {

    private final UUID id;

    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty expression = new SimpleStringProperty("");

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.DODGERBLUE);
    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(2.2);

    private final StringProperty colorHex = new SimpleStringProperty("#000000");

    private GraphFxFunction(final UUID id) {
        this.id = id;
        color.addListener((obs, o, n) -> colorHex.set(toHex(n)));
        colorHex.set(toHex(color.get()));
    }

    public static GraphFxFunction create(final String name, final String expr, final Color color) {
        final GraphFxFunction f = new GraphFxFunction(UUID.randomUUID());
        f.setName(name);
        f.setExpression(expr);
        f.setColor(color);
        return f;
    }

    public UUID getId() {
        return id;
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty expressionProperty() {
        return expression;
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public DoubleProperty strokeWidthProperty() {
        return strokeWidth;
    }

    public StringProperty colorHexProperty() {
        return colorHex;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public String getName() {
        return name.get();
    }

    public String getExpression() {
        return expression.get();
    }

    public Color getColor() {
        return color.get();
    }

    public double getStrokeWidth() {
        return strokeWidth.get();
    }

    public void setName(final String v) {
        name.set(v);
    }

    public void setExpression(final String v) {
        expression.set(v);
    }

    public void setColor(final Color c) {
        color.set(c);
    }

    
    
    public String toString() {
        return getName() == null ? "" : getName();
    }

private static String toHex(final Color c) {
        final int r = (int) Math.round(c.getRed() * 255);
        final int g = (int) Math.round(c.getGreen() * 255);
        final int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }
}

