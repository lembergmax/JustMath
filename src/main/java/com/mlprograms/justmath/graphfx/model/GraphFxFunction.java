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

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.UUID;

/**
 * Immutable identity and mutable properties of a single function displayed in GraphFX.
 * <p>
 * A function consists of:
 * <ul>
 *     <li>a unique identifier ({@link #getId()}) used to associate derived objects</li>
 *     <li>a {@code visible} flag controlling rendering</li>
 *     <li>a human-readable {@code name}</li>
 *     <li>an {@code expression} string in the calculator language</li>
 *     <li>styling information such as {@code color} and {@code strokeWidth}</li>
 * </ul>
 * All user-facing fields are implemented as JavaFX properties for convenient binding.
 * </p>
 */
public class GraphFxFunction {

    @Getter
    private final UUID id;

    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty expression = new SimpleStringProperty("");

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.DODGERBLUE);
    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(2.2);

    private final StringProperty colorHex = new SimpleStringProperty("#000000");

    private GraphFxFunction(final UUID id) {
        this.id = id;
        color.addListener((observable, oldColor, newColor) -> colorHex.set(toHex(newColor)));
        colorHex.set(toHex(color.get()));
    }

    /**
     * Factory method that creates a new function with the given properties.
     *
     * @param name       function name used in the UI
     * @param expression expression string in the calculator language
     * @param color      base color used for rendering the function
     * @return a newly created function instance
     */
    public static GraphFxFunction create(final String name, final String expression, final Color color) {
        final GraphFxFunction function = new GraphFxFunction(UUID.randomUUID());
        function.setName(name);
        function.setExpression(expression);
        function.setColor(color);
        return function;
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

    public void setName(final String value) {
        name.set(value);
    }

    public void setExpression(final String value) {
        expression.set(value);
    }

    public void setColor(final Color value) {
        color.set(value);
    }

    @Override
    public String toString() {
        return getName() == null ? "" : getName();
    }

    private static String toHex(final Color color) {
        final int red = (int) Math.round(color.getRed() * 255);
        final int green = (int) Math.round(color.getGreen() * 255);
        final int blue = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", red, green, blue);
    }
}
