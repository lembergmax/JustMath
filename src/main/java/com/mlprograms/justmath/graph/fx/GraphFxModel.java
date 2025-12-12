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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.util.*;

public class GraphFxModel {

    private final ObservableList<GraphFxFunction> functions = FXCollections.observableArrayList();
    private final ObservableList<GraphFxVariable> variables = FXCollections.observableArrayList();
    private final ObservableList<GraphFxObject> objects = FXCollections.observableArrayList();

    private final GraphFxSettings settings = new GraphFxSettings();

    private final LongProperty revision = new SimpleLongProperty(0);
    private final ObjectProperty<GraphFxFunction> selectedFunction = new SimpleObjectProperty<>(null);

    public GraphFxModel() {
        addVariable("a", new BigDecimal("1"));
        addVariable("b", new BigDecimal("1"));
        addVariable("c", new BigDecimal("0"));
    }

    public ObservableList<GraphFxFunction> getFunctions() {
        return functions;
    }

    public ObservableList<GraphFxVariable> getVariables() {
        return variables;
    }

    public ObservableList<GraphFxObject> getObjects() {
        return objects;
    }

    public GraphFxSettings getSettings() {
        return settings;
    }

    public LongProperty revisionProperty() {
        return revision;
    }

    public ObjectProperty<GraphFxFunction> selectedFunctionProperty() {
        return selectedFunction;
    }

    public GraphFxFunction getSelectedFunction() {
        return selectedFunction.get();
    }

    public void setSelectedFunction(final GraphFxFunction f) {
        selectedFunction.set(f);
    }

    public GraphFxFunction addFunction(final String name, final String expression) {
        final int idx = functions.size();
        final GraphFxFunction f = GraphFxFunction.create(name, expression, GraphFxPalette.colorForIndex(idx));
        functions.add(f);

        f.visibleProperty().addListener((obs, o, n) -> bump());
        f.nameProperty().addListener((obs, o, n) -> bump());
        f.expressionProperty().addListener((obs, o, n) -> bump());

        bump();
        return f;
    }

    public void removeFunction(final GraphFxFunction f) {
        if (f == null) {
            return;
        }
        objects.removeIf(o -> Objects.equals(o.referencesFunctionId(), f.getId()));
        functions.remove(f);
        if (selectedFunction.get() == f) {
            selectedFunction.set(null);
        }
        bump();
    }

    public GraphFxVariable addVariable(final String name, final BigDecimal value) {
        final String n = requireValidVarName(name);
        if ("x".equalsIgnoreCase(n)) {
            throw new IllegalArgumentException("Variable 'x' ist reserviert.");
        }
        if (variables.stream().anyMatch(v -> v.getName().equals(n))) {
            throw new IllegalArgumentException("Variable existiert bereits: " + n);
        }

        final GraphFxVariable v = GraphFxVariable.create(n, value);
        variables.add(v);

        v.nameProperty().addListener((obs, o, nn) -> bump());
        v.valueStringProperty().addListener((obs, o, nn) -> bump());
        v.sliderEnabledProperty().addListener((obs, o, nn) -> bump());
        v.sliderMinStringProperty().addListener((obs, o, nn) -> bump());
        v.sliderMaxStringProperty().addListener((obs, o, nn) -> bump());
        v.sliderStepStringProperty().addListener((obs, o, nn) -> bump());

        bump();
        return v;
    }

    public void removeVariable(final GraphFxVariable v) {
        if (v == null) {
            return;
        }
        variables.remove(v);
        bump();
    }

    public void renameVariable(final GraphFxVariable v, final String newName) {
        if (v == null) {
            return;
        }
        final String nn = requireValidVarName(newName);
        if ("x".equalsIgnoreCase(nn)) {
            throw new IllegalArgumentException("Variable 'x' ist reserviert.");
        }
        if (variables.stream().anyMatch(o -> o != v && o.getName().equals(nn))) {
            throw new IllegalArgumentException("Variable existiert bereits: " + nn);
        }
        v.setName(nn);
        bump();
    }

    public void setVariableValue(final GraphFxVariable v, final String valueString) {
        if (v == null) {
            return;
        }
        v.setValue(new BigDecimal(valueString.trim()));
        bump();
    }

    public void setVariableValue(final GraphFxVariable v, final BigDecimal value) {
        if (v == null) {
            return;
        }
        v.setValue(value);
        bump();
    }

    public void setSliderMin(final GraphFxVariable v, final String s) {
        v.setSliderMin(new BigDecimal(s.trim()));
        bump();
    }

    public void setSliderMax(final GraphFxVariable v, final String s) {
        v.setSliderMax(new BigDecimal(s.trim()));
        bump();
    }

    public void setSliderStep(final GraphFxVariable v, final String s) {
        v.setSliderStep(new BigDecimal(s.trim()));
        bump();
    }

    public Map<String, String> variablesAsStringMap() {
        final Map<String, String> map = new HashMap<>();
        for (final GraphFxVariable v : variables) {
            map.put(v.getName(), v.getValue().stripTrailingZeros().toPlainString());
        }
        return map;
    }

    public void addObject(final GraphFxObject obj) {
        objects.add(obj);
        bump();
    }

    public void removeObject(final UUID id) {
        objects.removeIf(o -> o.id().equals(id));
        bump();
    }

    private void bump() {
        revision.set(revision.get() + 1);
    }

    private static String requireValidVarName(final String name) {
        final String n = name == null ? "" : name.trim();
        if (!n.matches("[A-Za-z][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Ungültiger Variablenname: " + n);
        }
        return n;
    }

}

