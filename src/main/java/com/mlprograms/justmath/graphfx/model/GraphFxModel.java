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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.*;

/**
 * Central data model for the GraphFX component.
 * <p>
 * The model keeps track of:
 * <ul>
 *     <li>functions to be rendered ({@link GraphFxFunction})</li>
 *     <li>named variables that can be referenced from expressions ({@link GraphFxVariable})</li>
 *     <li>derived graphical objects such as points, lines and integrals ({@link GraphFxObject})</li>
 *     <li>global rendering settings ({@link GraphFxSettings})</li>
 * </ul>
 * Changes to the collections or to individual properties increment an internal {@code revision}
 * counter which can be used by views to schedule re-rendering.
 * </p>
 * <p>
 * This class does not know anything about JavaFX controls; it only exposes JavaFX properties
 * and observable lists that can be bound to from the UI layer.
 * </p>
 */
@NoArgsConstructor
public class GraphFxModel {

    /**
     * -- GETTER --
     * Returns the list of functions that should be rendered.
     * <p>
     * The list is observable and can be bound to UI controls such as tables or combo boxes.
     * </p>
     *
     * @return an observable list of functions
     */
    @Getter
    private final ObservableList<GraphFxFunction> functions = FXCollections.observableArrayList();
    /**
     * -- GETTER --
     * Returns the list of variables that can be referenced by expressions.
     *
     * @return an observable list of variables
     */
    @Getter
    private final ObservableList<GraphFxVariable> variables = FXCollections.observableArrayList();
    /**
     * -- GETTER --
     * Returns the list of graphical objects created by tools (points, lines, integrals, ...).
     *
     * @return an observable list of graphical objects
     */
    @Getter
    private final ObservableList<GraphFxObject> objects = FXCollections.observableArrayList();

    /**
     * -- GETTER --
     * Returns the global rendering/settings object.
     *
     * @return the settings instance
     */
    @Getter
    private final GraphFxSettings settings = new GraphFxSettings();

    private final LongProperty revision = new SimpleLongProperty(0);
    private final ObjectProperty<GraphFxFunction> selectedFunction = new SimpleObjectProperty<>(null);

    /**
     * Returns a property that is incremented whenever a relevant change occurs in the model.
     * <p>
     * Views can observe this property to trigger re-rendering.
     * </p>
     *
     * @return the revision property
     */
    public LongProperty revisionProperty() {
        return revision;
    }

    /**
     * Returns the currently selected function or {@code null} if none is selected.
     *
     * @return the selected function or {@code null}
     */
    public GraphFxFunction getSelectedFunction() {
        return selectedFunction.get();
    }

    /**
     * Sets the currently selected function.
     *
     * @param function the function to select; may be {@code null}
     */
    public void setSelectedFunction(final GraphFxFunction function) {
        selectedFunction.set(function);
    }

    /**
     * Adds a new function to the model.
     * <p>
     * A default color is chosen by {@link GraphFxPalette#colorForIndex(int)} based on the current function count.
     * The function is wired so that any property change will increment the model revision.
     * </p>
     *
     * @param name       human-readable name of the function
     * @param expression expression string in the calculator language
     * @return the created function instance
     * @throws IllegalArgumentException if name or expression is blank
     */
    public GraphFxFunction addFunction(final String name, final String expression) {
        final String normalizedName = requireNonBlank(name, "Function name must not be empty.");
        final String normalizedExpression = requireNonBlank(expression, "Function expression must not be empty.");

        final int functionIndex = functions.size();
        final GraphFxFunction function = GraphFxFunction.create(normalizedName, normalizedExpression, GraphFxPalette.colorForIndex(functionIndex));
        functions.add(function);

        function.visibleProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        function.nameProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        function.expressionProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        function.colorProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        function.strokeWidthProperty().addListener((obs, oldValue, newValue) -> bumpRevision());

        bumpRevision();
        return function;
    }

    /**
     * Removes a function from the model.
     * <p>
     * All graphical objects that reference the function are removed as well. If the function was selected,
     * the selection is cleared.
     * </p>
     *
     * @param function the function to remove; may be {@code null}
     */
    public void removeFunction(final GraphFxFunction function) {
        if (function == null) {
            return;
        }
        objects.removeIf(object -> Objects.equals(object.referencesFunctionId(), function.getId()));
        functions.remove(function);
        if (selectedFunction.get() == function) {
            selectedFunction.set(null);
        }
        bumpRevision();
    }

    /**
     * Adds a new variable to the model.
     *
     * @param name         variable name (must be a valid identifier and must not be "x")
     * @param initialValue initial numeric value; if {@code null}, zero is used
     * @return the created variable instance
     * @throws IllegalArgumentException if the name is invalid or already in use
     */
    public GraphFxVariable addVariable(final String name, final BigDecimal initialValue) {
        final String validatedName = requireValidVariableName(name);
        if ("x".equalsIgnoreCase(validatedName)) {
            throw new IllegalArgumentException("The variable name 'x' is reserved for function input.");
        }
        if (variables.stream().anyMatch(existingVariable -> existingVariable.getName().equals(validatedName))) {
            throw new IllegalArgumentException("A variable with this name already exists: " + validatedName);
        }

        final GraphFxVariable variable = GraphFxVariable.create(validatedName, initialValue == null ? BigDecimal.ZERO : initialValue);
        variables.add(variable);

        variable.nameProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.valueStringProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.sliderEnabledProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.sliderMinStringProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.sliderMaxStringProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.sliderStepStringProperty().addListener((obs, oldValue, newValue) -> bumpRevision());

        bumpRevision();
        return variable;
    }

    /**
     * Removes the given variable from the model.
     *
     * @param variable the variable to remove; may be {@code null}
     */
    public void removeVariable(final GraphFxVariable variable) {
        if (variable == null) {
            return;
        }
        variables.remove(variable);
        bumpRevision();
    }

    /**
     * Renames an existing variable while ensuring name validity and uniqueness.
     *
     * @param variable    the variable to rename; may be {@code null}
     * @param newNameText the new variable name
     * @throws IllegalArgumentException if the new name is invalid, reserved, or already taken
     */
    public void renameVariable(final GraphFxVariable variable, final String newNameText) {
        if (variable == null) {
            return;
        }
        final String normalizedName = requireValidVariableName(newNameText);
        if ("x".equalsIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("The variable name 'x' is reserved for function input.");
        }
        if (variables.stream().anyMatch(otherVariable -> otherVariable != variable && otherVariable.getName().equals(normalizedName))) {
            throw new IllegalArgumentException("A variable with this name already exists: " + normalizedName);
        }
        variable.setName(normalizedName);
        bumpRevision();
    }

    /**
     * Updates the value of a variable based on a textual representation.
     *
     * @param variable  the variable to update; may be {@code null}
     * @param valueText the new value as a string
     * @throws IllegalArgumentException if {@code valueText} cannot be parsed as a {@link BigDecimal}
     */
    public void setVariableValue(final GraphFxVariable variable, final String valueText) {
        if (variable == null) {
            return;
        }
        variable.setValue(parseNumber(valueText, "Invalid variable value."));
        bumpRevision();
    }

    /**
     * Updates the value of a variable.
     *
     * @param variable the variable to update; may be {@code null}
     * @param value    the new numeric value; if {@code null}, zero is used
     */
    public void setVariableValue(final GraphFxVariable variable, final BigDecimal value) {
        if (variable == null) {
            return;
        }
        variable.setValue(value == null ? BigDecimal.ZERO : value);
        bumpRevision();
    }

    /**
     * Updates the minimum bound of the slider attached to the given variable using a textual value.
     *
     * @param variable    the variable whose slider minimum should be updated; may be {@code null}
     * @param minimumText textual representation of the new minimum
     * @throws IllegalArgumentException if the text cannot be parsed or results in an invalid slider range
     */
    public void setSliderMin(final GraphFxVariable variable, final String minimumText) {
        if (variable == null) {
            return;
        }
        variable.setSliderMin(parseNumber(minimumText, "Invalid slider minimum."));
        validateSlider(variable);
        bumpRevision();
    }

    /**
     * Updates the maximum bound of the slider attached to the given variable using a textual value.
     *
     * @param variable    the variable whose slider maximum should be updated; may be {@code null}
     * @param maximumText textual representation of the new maximum
     * @throws IllegalArgumentException if the text cannot be parsed or results in an invalid slider range
     */
    public void setSliderMax(final GraphFxVariable variable, final String maximumText) {
        if (variable == null) {
            return;
        }
        variable.setSliderMax(parseNumber(maximumText, "Invalid slider maximum."));
        validateSlider(variable);
        bumpRevision();
    }

    /**
     * Updates the step size of the slider attached to the given variable using a textual value.
     *
     * @param variable the variable whose slider step size should be updated; may be {@code null}
     * @param stepText textual representation of the new step size
     * @throws IllegalArgumentException if the text cannot be parsed or results in an invalid slider configuration
     */
    public void setSliderStep(final GraphFxVariable variable, final String stepText) {
        if (variable == null) {
            return;
        }
        variable.setSliderStep(parseNumber(stepText, "Invalid slider step."));
        validateSlider(variable);
        bumpRevision();
    }

    /**
     * Returns the current variable values as a {@link Map} of name to string representation.
     * <p>
     * The returned map is detached from the underlying model and represents a snapshot at the
     * time of the call.
     * </p>
     *
     * @return a new map containing variable values as strings
     */
    public Map<String, String> variablesAsStringMap() {
        final Map<String, String> variableValuesByName = new HashMap<>();
        for (final GraphFxVariable variable : variables) {
            variableValuesByName.put(variable.getName(), variable.getValue().stripTrailingZeros().toPlainString());
        }
        return variableValuesByName;
    }

    /**
     * Adds a new graphical object to the model.
     *
     * @param object the object to add
     */
    public void addObject(final GraphFxObject object) {
        objects.add(object);
        bumpRevision();
    }

    /**
     * Removes all graphical objects from the model.
     */
    public void clearObjects() {
        objects.clear();
        bumpRevision();
    }

    /**
     * Suggests the next available variable name.
     * <p>
     * Lowercase letters from {@code a} to {@code z} are used first (excluding {@code x}).
     * If all are taken, names of the form {@code kN} are generated.
     * </p>
     *
     * @return a suggested variable name that is currently unused
     */
    public String nextSuggestedVariableName() {
        for (char candidate = 'a'; candidate <= 'z'; candidate++) {
            final String nameCandidate = String.valueOf(candidate);
            if (variables.stream().noneMatch(variable -> variable.getName().equals(nameCandidate)) && !"x".equalsIgnoreCase(nameCandidate)) {
                return nameCandidate;
            }
        }
        return "k" + (variables.size() + 1);
    }

    private void bumpRevision() {
        revision.set(revision.get() + 1);
    }

    private static String requireValidVariableName(final String name) {
        final String trimmedName = name == null ? "" : name.trim();
        if (!trimmedName.matches("[A-Za-z][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid variable name: " + trimmedName);
        }
        return trimmedName;
    }

    private static String requireNonBlank(final String value, final String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static BigDecimal parseNumber(final String value, final String message) {
        try {
            final String trimmed = value == null ? "" : value.trim();
            return new BigDecimal(trimmed);
        } catch (Exception exception) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validateSlider(final GraphFxVariable variable) {
        final BigDecimal minimum = variable.getSliderMin();
        final BigDecimal maximum = variable.getSliderMax();
        final BigDecimal stepSize = variable.getSliderStep();

        if (maximum.compareTo(minimum) <= 0) {
            throw new IllegalArgumentException("Slider range is invalid: Max must be greater than Min.");
        }
        if (stepSize.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Slider step is invalid: Step must be greater than 0.");
        }
    }

}
