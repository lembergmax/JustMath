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

import java.math.BigDecimal;

/**
 * Represents a named variable that can be referenced by GraphFX function expressions.
 * <p>
 * A variable maintains its numeric value as a {@link BigDecimal} and also as a synchronized
 * string representation for use in text fields. It can optionally be associated with a slider
 * range ({@code sliderMin}, {@code sliderMax}, {@code sliderStep}) and a flag indicating whether
 * a slider is enabled.
 * </p>
 * <p>
 * All fields are implemented as JavaFX properties which allows the UI layer to bind controls
 * directly to this model.
 * </p>
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

    private GraphFxVariable() {
        value.addListener((observable, oldValue, newValue) -> valueString.set(newValue.stripTrailingZeros().toPlainString()));
        valueString.addListener((observable, oldValue, newValue) -> value.set(new BigDecimal(newValue.trim())));

        sliderMin.addListener((observable, oldValue, newValue) -> sliderMinString.set(newValue.stripTrailingZeros().toPlainString()));
        sliderMax.addListener((observable, oldValue, newValue) -> sliderMaxString.set(newValue.stripTrailingZeros().toPlainString()));
        sliderStep.addListener((observable, oldValue, newValue) -> sliderStepString.set(newValue.stripTrailingZeros().toPlainString()));

        sliderMinString.addListener((observable, oldValue, newValue) -> sliderMin.set(new BigDecimal(newValue.trim())));
        sliderMaxString.addListener((observable, oldValue, newValue) -> sliderMax.set(new BigDecimal(newValue.trim())));
        sliderStepString.addListener((observable, oldValue, newValue) -> sliderStep.set(new BigDecimal(newValue.trim())));
    }

    /**
     * Factory method that creates a variable with the given name and initial value.
     *
     * @param name         variable name
     * @param initialValue initial value; must not be {@code null}
     * @return the created variable
     */
    public static GraphFxVariable create(final String name, final BigDecimal initialValue) {
        final GraphFxVariable variable = new GraphFxVariable();
        variable.setName(name);
        variable.setValue(initialValue);
        return variable;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public ObjectProperty<BigDecimal> valueProperty() {
        return value;
    }

    public StringProperty valueStringProperty() {
        return valueString;
    }

    public BooleanProperty sliderEnabledProperty() {
        return sliderEnabled;
    }

    public ObjectProperty<BigDecimal> sliderMinProperty() {
        return sliderMin;
    }

    public ObjectProperty<BigDecimal> sliderMaxProperty() {
        return sliderMax;
    }

    public ObjectProperty<BigDecimal> sliderStepProperty() {
        return sliderStep;
    }

    public StringProperty sliderMinStringProperty() {
        return sliderMinString;
    }

    public StringProperty sliderMaxStringProperty() {
        return sliderMaxString;
    }

    public StringProperty sliderStepStringProperty() {
        return sliderStepString;
    }

    public String getName() {
        return name.get();
    }

    public BigDecimal getValue() {
        return value.get();
    }

    public String getValueString() {
        return valueString.get();
    }

    public boolean isSliderEnabled() {
        return sliderEnabled.get();
    }

    public BigDecimal getSliderMin() {
        return sliderMin.get();
    }

    public BigDecimal getSliderMax() {
        return sliderMax.get();
    }

    public BigDecimal getSliderStep() {
        return sliderStep.get();
    }

    public void setName(final String name) {
        this.name.set(name);
    }

    public void setValue(final BigDecimal newValue) {
        value.set(newValue);
    }

    public void setSliderMin(final BigDecimal minimum) {
        sliderMin.set(minimum);
    }

    public void setSliderMax(final BigDecimal maximum) {
        sliderMax.set(maximum);
    }

    public void setSliderStep(final BigDecimal stepSize) {
        sliderStep.set(stepSize);
    }

}
