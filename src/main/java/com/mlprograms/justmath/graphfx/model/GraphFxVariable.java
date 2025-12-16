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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;

/**
 * Represents a named variable that can be referenced by GraphFX function expressions.
 * <p>
 * The variable exposes its state exclusively through JavaFX properties to allow UI bindings
 * (e.g., text fields, sliders) without additional glue code.
 * </p>
 *
 * <h2>Value synchronization</h2>
 * <p>
 * The variable maintains the numeric value in two synchronized forms:
 * </p>
 * <ul>
 *     <li>{@link #valueProperty()} for precise numeric computations ({@link BigDecimal})</li>
 *     <li>{@link #valueStringProperty()} for text-based editing in the UI</li>
 * </ul>
 * <p>
 * Whenever one representation changes, the other is updated accordingly.
 * </p>
 *
 * <h2>Slider configuration</h2>
 * <p>
 * A variable can optionally be controlled by a slider. For this purpose, it stores minimum, maximum and step
 * values as both numeric properties and synchronized string properties.
 * </p>
 *
 * <p><b>Note:</b> The string-to-decimal listeners assume valid numeric input. If the UI can provide invalid
 * strings (empty, non-numeric), validation should occur before setting these properties.</p>
 */
public class GraphFxVariable {

    private final StringProperty name = new SimpleStringProperty("");
    private final ObjectProperty<BigDecimal> value = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty valueString = new SimpleStringProperty("0");

    private final BooleanProperty sliderEnabled = new SimpleBooleanProperty(false);
    private final ObjectProperty<BigDecimal> sliderMin = new SimpleObjectProperty<>(new BigDecimal("-10"));
    private final ObjectProperty<BigDecimal> sliderMax = new SimpleObjectProperty<>(new BigDecimal("10"));
    private final ObjectProperty<BigDecimal> sliderStep = new SimpleObjectProperty<>(new BigDecimal("0.1"));

    private final StringProperty sliderMinString = new SimpleStringProperty("-10");
    private final StringProperty sliderMaxString = new SimpleStringProperty("10");
    private final StringProperty sliderStepString = new SimpleStringProperty("0.1");

    /**
     * Creates a new variable with default values and installs internal listeners that keep numeric and
     * string properties synchronized.
     * <p>
     * The constructor is private to enforce usage of the factory method {@link #create(String, BigDecimal)},
     * which guarantees that a name and initial value are assigned consistently.
     * </p>
     */
    private GraphFxVariable() {
        value.addListener((observable, oldValue, newValue) ->
                valueString.set(newValue.stripTrailingZeros().toPlainString()));
        valueString.addListener((observable, oldValue, newValue) ->
                value.set(new BigDecimal(newValue.trim())));

        sliderMin.addListener((observable, oldValue, newValue) ->
                sliderMinString.set(newValue.stripTrailingZeros().toPlainString()));
        sliderMax.addListener((observable, oldValue, newValue) ->
                sliderMaxString.set(newValue.stripTrailingZeros().toPlainString()));
        sliderStep.addListener((observable, oldValue, newValue) ->
                sliderStepString.set(newValue.stripTrailingZeros().toPlainString()));

        sliderMinString.addListener((observable, oldValue, newValue) ->
                sliderMin.set(new BigDecimal(newValue.trim())));
        sliderMaxString.addListener((observable, oldValue, newValue) ->
                sliderMax.set(new BigDecimal(newValue.trim())));
        sliderStepString.addListener((observable, oldValue, newValue) ->
                sliderStep.set(new BigDecimal(newValue.trim())));
    }

    /**
     * Creates a variable with the given name and initial value.
     * <p>
     * The returned instance has all synchronization listeners installed and will keep its string properties
     * aligned with the numeric values (and vice versa).
     * </p>
     *
     * @param name         the variable name (may be {@code null} or blank; caller decides policy)
     * @param initialValue the initial numeric value; must not be {@code null}
     * @return a fully initialized {@link GraphFxVariable} instance
     * @throws NullPointerException if {@code initialValue} is {@code null}
     */
    public static GraphFxVariable create(final String name, final BigDecimal initialValue) {
        final GraphFxVariable variable = new GraphFxVariable();
        variable.setName(name);
        variable.setValue(initialValue);
        return variable;
    }

    /**
     * Exposes the variable name as a JavaFX property for UI bindings.
     *
     * @return the name property (never {@code null})
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Exposes the numeric value as a JavaFX property for computations and bindings.
     *
     * @return the numeric value property (never {@code null})
     */
    public ObjectProperty<BigDecimal> valueProperty() {
        return value;
    }

    /**
     * Exposes the string representation of {@link #valueProperty()} for text editing.
     *
     * @return the value string property (never {@code null})
     */
    public StringProperty valueStringProperty() {
        return valueString;
    }

    /**
     * Exposes whether a slider is enabled for this variable.
     *
     * @return slider enabled property (never {@code null})
     */
    public BooleanProperty sliderEnabledProperty() {
        return sliderEnabled;
    }

    /**
     * Exposes the numeric minimum value for the variable slider.
     *
     * @return slider minimum property (never {@code null})
     */
    public ObjectProperty<BigDecimal> sliderMinProperty() {
        return sliderMin;
    }

    /**
     * Exposes the numeric maximum value for the variable slider.
     *
     * @return slider maximum property (never {@code null})
     */
    public ObjectProperty<BigDecimal> sliderMaxProperty() {
        return sliderMax;
    }

    /**
     * Exposes the numeric step size for the variable slider.
     *
     * @return slider step property (never {@code null})
     */
    public ObjectProperty<BigDecimal> sliderStepProperty() {
        return sliderStep;
    }

    /**
     * Exposes the string representation of {@link #sliderMinProperty()} for text editing.
     *
     * @return slider minimum string property (never {@code null})
     */
    public StringProperty sliderMinStringProperty() {
        return sliderMinString;
    }

    /**
     * Exposes the string representation of {@link #sliderMaxProperty()} for text editing.
     *
     * @return slider maximum string property (never {@code null})
     */
    public StringProperty sliderMaxStringProperty() {
        return sliderMaxString;
    }

    /**
     * Exposes the string representation of {@link #sliderStepProperty()} for text editing.
     *
     * @return slider step string property (never {@code null})
     */
    public StringProperty sliderStepStringProperty() {
        return sliderStepString;
    }

    /**
     * Returns the current variable name.
     *
     * @return current name value (may be {@code null} depending on how the property was set)
     */
    public String getName() {
        return name.get();
    }

    /**
     * Returns the current numeric value of the variable.
     *
     * @return current {@link BigDecimal} value (may be {@code null} depending on how the property was set)
     */
    public BigDecimal getValue() {
        return value.get();
    }

    /**
     * Returns the current string representation of the variable value.
     *
     * @return current value string (never {@code null} unless externally set to {@code null})
     */
    public String getValueString() {
        return valueString.get();
    }

    /**
     * Returns whether the slider is enabled for this variable.
     *
     * @return {@code true} if the slider should be shown/used, otherwise {@code false}
     */
    public boolean isSliderEnabled() {
        return sliderEnabled.get();
    }

    /**
     * Returns the current numeric minimum configured for the slider.
     *
     * @return slider minimum value (may be {@code null} depending on how the property was set)
     */
    public BigDecimal getSliderMin() {
        return sliderMin.get();
    }

    /**
     * Returns the current numeric maximum configured for the slider.
     *
     * @return slider maximum value (may be {@code null} depending on how the property was set)
     */
    public BigDecimal getSliderMax() {
        return sliderMax.get();
    }

    /**
     * Returns the current numeric step size configured for the slider.
     *
     * @return slider step size (may be {@code null} depending on how the property was set)
     */
    public BigDecimal getSliderStep() {
        return sliderStep.get();
    }

    /**
     * Updates the variable name.
     * <p>
     * This updates only the name property and does not affect any other state.
     * </p>
     *
     * @param name new name to set (may be {@code null})
     */
    public void setName(final String name) {
        this.name.set(name);
    }

    /**
     * Updates the variable value.
     * <p>
     * Due to internal synchronization listeners, updating this value also updates
     * {@link #valueStringProperty()} with a plain, trimmed representation.
     * </p>
     *
     * @param newValue new numeric value to set
     */
    public void setValue(final BigDecimal newValue) {
        value.set(newValue);
    }

    /**
     * Updates the slider minimum value.
     * <p>
     * Due to internal synchronization listeners, updating this value also updates
     * {@link #sliderMinStringProperty()}.
     * </p>
     *
     * @param minimum new slider minimum
     */
    public void setSliderMin(final BigDecimal minimum) {
        sliderMin.set(minimum);
    }

    /**
     * Updates the slider maximum value.
     * <p>
     * Due to internal synchronization listeners, updating this value also updates
     * {@link #sliderMaxStringProperty()}.
     * </p>
     *
     * @param maximum new slider maximum
     */
    public void setSliderMax(final BigDecimal maximum) {
        sliderMax.set(maximum);
    }

    /**
     * Updates the slider step size.
     * <p>
     * Due to internal synchronization listeners, updating this value also updates
     * {@link #sliderStepStringProperty()}.
     * </p>
     *
     * @param stepSize new slider step size
     */
    public void setSliderStep(final BigDecimal stepSize) {
        sliderStep.set(stepSize);
    }

}
