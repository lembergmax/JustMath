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

import javafx.beans.property.*;

import java.math.BigDecimal;

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
        value.addListener((obs, o, n) -> valueString.set(n.stripTrailingZeros().toPlainString()));
        valueString.addListener((obs, o, n) -> value.set(new BigDecimal(n.trim())));

        sliderMin.addListener((obs, o, n) -> sliderMinString.set(n.stripTrailingZeros().toPlainString()));
        sliderMax.addListener((obs, o, n) -> sliderMaxString.set(n.stripTrailingZeros().toPlainString()));
        sliderStep.addListener((obs, o, n) -> sliderStepString.set(n.stripTrailingZeros().toPlainString()));

        sliderMinString.addListener((obs, o, n) -> sliderMin.set(new BigDecimal(n.trim())));
        sliderMaxString.addListener((obs, o, n) -> sliderMax.set(new BigDecimal(n.trim())));
        sliderStepString.addListener((obs, o, n) -> sliderStep.set(new BigDecimal(n.trim())));
    }

    public static GraphFxVariable create(final String name, final BigDecimal value) {
        final GraphFxVariable v = new GraphFxVariable();
        v.setName(name);
        v.setValue(value);
        return v;
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

    public void setName(final String v) {
        name.set(v);
    }

    public void setValue(final BigDecimal v) {
        value.set(v);
    }

    public void setSliderMin(final BigDecimal v) {
        sliderMin.set(v);
    }

    public void setSliderMax(final BigDecimal v) {
        sliderMax.set(v);
    }

    public void setSliderStep(final BigDecimal v) {
        sliderStep.set(v);
    }
}

