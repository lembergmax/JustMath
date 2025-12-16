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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * Represents a single function displayed in GraphFX.
 * <p>
 * The function has an immutable identity ({@link #getId()}) and exposes all user-editable attributes as JavaFX
 * properties so UI controls can bind to them directly.
 * </p>
 *
 * <h2>Contained state</h2>
 * <ul>
 *     <li>{@link #visibleProperty()}: whether the function should be rendered</li>
 *     <li>{@link #nameProperty()}: human-readable name shown in UI lists</li>
 *     <li>{@link #expressionProperty()}: expression string evaluated by the calculator engine</li>
 *     <li>{@link #colorProperty()}: stroke color used for rendering</li>
 *     <li>{@link #strokeWidthProperty()}: stroke width used for rendering</li>
 *     <li>{@link #colorHexProperty()}: derived hex representation of {@link #colorProperty()} for exports/UI</li>
 * </ul>
 */
public final class GraphFxFunction {

    /**
     * Stable identifier used to associate derived objects (e.g., points, integrals) with this function.
     */
    @Getter
    private final UUID id;

    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty expression = new SimpleStringProperty("");

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.DODGERBLUE);
    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(2.2);

    private final StringProperty colorHex = new SimpleStringProperty("#000000");

    /**
     * Creates a new function instance with a fixed identifier and installs internal listeners for derived state.
     * <p>
     * The {@link #colorHexProperty()} is kept in sync with {@link #colorProperty()}.
     * </p>
     *
     * @param id unique identifier to assign (must not be {@code null})
     */
    private GraphFxFunction(@NonNull final UUID id) {
        this.id = id;

        color.addListener((observable, oldColor, newColor) -> colorHex.set(toHex(newColor)));
        colorHex.set(toHex(color.get()));
    }

    /**
     * Factory method that creates a new function with the given properties.
     * <p>
     * A random {@link UUID} is generated for the function id. The returned instance is visible by default.
     * </p>
     *
     * @param name       function name used in the UI (must not be {@code null})
     * @param expression expression string in the calculator language (must not be {@code null})
     * @param color      base color used for rendering the function (must not be {@code null})
     * @return a new {@link GraphFxFunction} instance
     */
    public static GraphFxFunction create(
            @NonNull final String name,
            @NonNull final String expression,
            @NonNull final Color color
    ) {
        final GraphFxFunction function = new GraphFxFunction(UUID.randomUUID());
        function.setName(name);
        function.setExpression(expression);
        function.setColor(color);
        return function;
    }

    /**
     * Exposes whether the function should be rendered.
     *
     * @return the visibility property (never {@code null})
     */
    public BooleanProperty visibleProperty() {
        return visible;
    }

    /**
     * Exposes the human-readable name of the function.
     *
     * @return the name property (never {@code null})
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Exposes the expression string that defines the function.
     *
     * @return the expression property (never {@code null})
     */
    public StringProperty expressionProperty() {
        return expression;
    }

    /**
     * Exposes the stroke color used to render the function.
     *
     * @return the color property (never {@code null})
     */
    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    /**
     * Exposes the stroke width used to render the function.
     *
     * @return the stroke width property (never {@code null})
     */
    public DoubleProperty strokeWidthProperty() {
        return strokeWidth;
    }

    /**
     * Exposes the hexadecimal RGB representation of the current {@link #colorProperty()}.
     * <p>
     * This property is derived from {@link #colorProperty()} and updated automatically when the color changes.
     * The format is {@code #RRGGBB} (alpha is not included).
     * </p>
     *
     * @return the color hex property (never {@code null})
     */
    public StringProperty colorHexProperty() {
        return colorHex;
    }

    /**
     * Returns whether the function is currently visible.
     *
     * @return {@code true} if visible, otherwise {@code false}
     */
    public boolean isVisible() {
        return visible.get();
    }

    /**
     * Returns the current function name.
     *
     * @return the name value (never {@code null} unless externally set to {@code null})
     */
    public String getName() {
        return name.get();
    }

    /**
     * Returns the current expression string.
     *
     * @return the expression value (never {@code null} unless externally set to {@code null})
     */
    public String getExpression() {
        return expression.get();
    }

    /**
     * Returns the current stroke color.
     *
     * @return the color value (never {@code null} unless externally set to {@code null})
     */
    public Color getColor() {
        return color.get();
    }

    /**
     * Returns the current stroke width.
     *
     * @return the stroke width in pixels
     */
    public double getStrokeWidth() {
        return strokeWidth.get();
    }

    /**
     * Updates the function name.
     *
     * @param value new name to set (must not be {@code null})
     */
    public void setName(@NonNull final String value) {
        name.set(value);
    }

    /**
     * Updates the expression string defining the function.
     *
     * @param value new expression to set (must not be {@code null})
     */
    public void setExpression(@NonNull final String value) {
        expression.set(value);
    }

    /**
     * Updates the stroke color used to render the function.
     * <p>
     * Updating this property also updates {@link #colorHexProperty()} automatically.
     * </p>
     *
     * @param value new color to set (must not be {@code null})
     */
    public void setColor(@NonNull final Color value) {
        color.set(value);
    }

    /**
     * Returns the string representation used by UI controls (e.g., choice dialogs).
     *
     * @return the function name, or an empty string if the name is {@code null}
     */
    @Override
    public String toString() {
        return getName() == null ? "" : getName();
    }

    /**
     * Converts a JavaFX {@link Color} into a hexadecimal RGB string of the form {@code #RRGGBB}.
     * <p>
     * The alpha component is intentionally ignored. Color channels are rounded to the nearest integer
     * in {@code [0..255]}.
     * </p>
     *
     * @param color the color to convert (must not be {@code null})
     * @return a hex string in the format {@code #RRGGBB}
     */
    private static String toHex(@NonNull final Color color) {
        final int red = (int) Math.round(color.getRed() * 255);
        final int green = (int) Math.round(color.getGreen() * 255);
        final int blue = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", red, green, blue);
    }

}
