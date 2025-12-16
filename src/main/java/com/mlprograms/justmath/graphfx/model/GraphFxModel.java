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

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Central data model for the GraphFX component.
 * <p>
 * The model keeps track of:
 * </p>
 * <ul>
 *     <li>functions to be rendered ({@link GraphFxFunction})</li>
 *     <li>named variables referenced by expressions ({@link GraphFxVariable})</li>
 *     <li>derived graphical objects such as points, lines and integrals ({@link GraphFxObject})</li>
 *     <li>global rendering settings ({@link GraphFxSettings})</li>
 * </ul>
 *
 * <h2>Revision tracking</h2>
 * <p>
 * Every relevant change increments {@link #getRevision()} so views can react efficiently
 * (e.g., re-render, resample, or update UI).
 * </p>
 *
 * <p>
 * This model is UI-agnostic: it only exposes JavaFX properties and observable lists that can be bound to.
 * </p>
 */
public final class GraphFxModel {

    private static final String RESERVED_FUNCTION_INPUT_VARIABLE = "x";
    private static final String VARIABLE_NAME_PATTERN = "[A-Za-z][A-Za-z0-9_]*";

    private final ObservableList<GraphFxFunction> functions = FXCollections.observableArrayList();
    private final ObservableList<GraphFxVariable> variables = FXCollections.observableArrayList();
    private final ObservableList<GraphFxObject> objects = FXCollections.observableArrayList();
    private final GraphFxSettings settings = new GraphFxSettings();

    private final LongProperty revision = new SimpleLongProperty(0L);
    private final ObjectProperty<GraphFxFunction> selectedFunction = new SimpleObjectProperty<>(null);

    /**
     * Creates an empty model and installs listeners that bump the revision for global settings changes.
     */
    public GraphFxModel() {
        settings.showGridProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        settings.showAxesProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        settings.targetGridLinesProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        selectedFunction.addListener((obs, oldValue, newValue) -> bumpRevision());
    }

    /**
     * Returns the currently selected function.
     *
     * @return the selected function, or {@code null} if none is selected
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
     * A default color is chosen via {@link GraphFxPalette#colorForIndex(int)} based on the current function count.
     * The function is registered so that any property change bumps the model revision.
     * </p>
     *
     * @param name       the human-readable function name (must not be blank)
     * @param expression the expression string in the calculator language (must not be blank)
     * @return the created {@link GraphFxFunction} instance
     * @throws IllegalArgumentException if {@code name} or {@code expression} is blank
     */
    public GraphFxFunction addFunction(final String name, final String expression) {
        final String normalizedName = requireNonBlank(name, "Function name must not be empty.");
        final String normalizedExpression = requireNonBlank(expression, "Function expression must not be empty.");

        final int functionIndex = functions.size();
        final GraphFxFunction function = GraphFxFunction.create(
                normalizedName,
                normalizedExpression,
                GraphFxPalette.colorForIndex(functionIndex)
        );

        functions.add(function);
        registerFunctionRevisionListeners(function);

        bumpRevision();
        return function;
    }

    /**
     * Removes a function from the model.
     * <p>
     * All objects referencing the function are removed as well. If the function was selected, the selection is cleared.
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
     * <p>
     * Variable names must follow {@link #VARIABLE_NAME_PATTERN}, must not be the reserved {@code "x"}, and must be unique.
     * The variable is registered so that any property change bumps the model revision.
     * </p>
     *
     * @param name         variable name (must be a valid identifier and must not be {@code "x"})
     * @param initialValue initial numeric value; if {@code null}, {@link BigDecimal#ZERO} is used
     * @return the created {@link GraphFxVariable} instance
     * @throws IllegalArgumentException if the name is invalid, reserved, or already in use
     */
    public GraphFxVariable addVariable(final String name, final BigDecimal initialValue) {
        final String validatedName = requireValidVariableName(name);

        if (isReservedVariableName(validatedName)) {
            throw new IllegalArgumentException("The variable name 'x' is reserved for function input.");
        }
        if (isVariableNameTaken(validatedName, null)) {
            throw new IllegalArgumentException("A variable with this name already exists: " + validatedName);
        }

        final BigDecimal normalizedInitialValue = (initialValue == null) ? BigDecimal.ZERO : initialValue;
        final GraphFxVariable variable = GraphFxVariable.create(validatedName, normalizedInitialValue);

        variables.add(variable);
        registerVariableRevisionListeners(variable);

        bumpRevision();
        return variable;
    }

    /**
     * Removes a variable from the model.
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
     * @param newNameText the new variable name (must be a valid identifier and must not be {@code "x"})
     * @throws IllegalArgumentException if the new name is invalid, reserved, or already taken
     */
    public void renameVariable(final GraphFxVariable variable, final String newNameText) {
        if (variable == null) {
            return;
        }

        final String validatedName = requireValidVariableName(newNameText);

        if (isReservedVariableName(validatedName)) {
            throw new IllegalArgumentException("The variable name 'x' is reserved for function input.");
        }
        if (isVariableNameTaken(validatedName, variable)) {
            throw new IllegalArgumentException("A variable with this name already exists: " + validatedName);
        }

        variable.setName(validatedName);
        bumpRevision();
    }

    /**
     * Updates the value of a variable from a textual representation.
     *
     * @param variable  the variable to update; may be {@code null}
     * @param valueText the new value as text
     * @throws IllegalArgumentException if {@code valueText} cannot be parsed as {@link BigDecimal}
     */
    public void setVariableValue(final GraphFxVariable variable, final String valueText) {
        if (variable == null) {
            return;
        }

        final BigDecimal parsedValue = parseBigDecimal(valueText, "Invalid variable value.");
        variable.setValue(parsedValue);
        bumpRevision();
    }

    /**
     * Updates the value of a variable.
     *
     * @param variable the variable to update; may be {@code null}
     * @param value    the new numeric value; if {@code null}, {@link BigDecimal#ZERO} is used
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
     * @throws IllegalArgumentException if the text cannot be parsed or results in an invalid slider configuration
     */
    public void setSliderMin(final GraphFxVariable variable, final String minimumText) {
        if (variable == null) {
            return;
        }

        variable.setSliderMin(parseBigDecimal(minimumText, "Invalid slider minimum."));
        validateSliderConfiguration(variable);
        bumpRevision();
    }

    /**
     * Updates the maximum bound of the slider attached to the given variable using a textual value.
     *
     * @param variable    the variable whose slider maximum should be updated; may be {@code null}
     * @param maximumText textual representation of the new maximum
     * @throws IllegalArgumentException if the text cannot be parsed or results in an invalid slider configuration
     */
    public void setSliderMax(final GraphFxVariable variable, final String maximumText) {
        if (variable == null) {
            return;
        }

        variable.setSliderMax(parseBigDecimal(maximumText, "Invalid slider maximum."));
        validateSliderConfiguration(variable);
        bumpRevision();
    }

    /**
     * Updates the step size of the slider attached to the given variable using a textual value.
     *
     * @param variable the variable whose slider step should be updated; may be {@code null}
     * @param stepText textual representation of the new step size
     * @throws IllegalArgumentException if the text cannot be parsed or results in an invalid slider configuration
     */
    public void setSliderStep(final GraphFxVariable variable, final String stepText) {
        if (variable == null) {
            return;
        }

        variable.setSliderStep(parseBigDecimal(stepText, "Invalid slider step."));
        validateSliderConfiguration(variable);
        bumpRevision();
    }

    /**
     * Returns a snapshot map of the current variables (name → value as plain string).
     * <p>
     * The returned map is detached from the model and safe to pass into calculators/evaluators.
     * </p>
     *
     * @return a new map containing variable values as strings (never {@code null})
     */
    public Map<String, String> variablesAsStringMap() {
        final Map<String, String> variableValuesByName = new HashMap<>();

        for (final GraphFxVariable variable : variables) {
            final String variableName = variable.getName();
            final BigDecimal variableValue = variable.getValue();
            variableValuesByName.put(variableName, variableValue.stripTrailingZeros().toPlainString());
        }

        return variableValuesByName;
    }

    /**
     * Adds a new graphical object (point, line, integral, ...) to the model.
     *
     * @param object the object to add (must not be {@code null})
     * @throws NullPointerException if {@code object} is {@code null}
     */
    public void addObject(@NonNull final GraphFxObject object) {
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
     * The method prefers single lowercase letters from {@code a} to {@code z}, excluding {@code x}.
     * If all are taken, it falls back to names of the form {@code kN}.
     * </p>
     *
     * @return a currently unused variable name suggestion (never {@code null})
     */
    public String nextSuggestedVariableName() {
        for (char candidate = 'a'; candidate <= 'z'; candidate++) {
            final String candidateName = String.valueOf(candidate);
            if (!isReservedVariableName(candidateName) && !isVariableNameTaken(candidateName, null)) {
                return candidateName;
            }
        }

        return "k" + (variables.size() + 1);
    }

    /**
     * Registers listeners on a function so that any function property change bumps the model revision.
     *
     * @param function the function to register (must not be {@code null})
     */
    private void registerFunctionRevisionListeners(@NonNull final GraphFxFunction function) {
        function.visibleProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        function.nameProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        function.expressionProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        function.colorProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        function.strokeWidthProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
    }

    /**
     * Registers listeners on a variable so that any variable property change bumps the model revision.
     *
     * @param variable the variable to register (must not be {@code null})
     */
    private void registerVariableRevisionListeners(@NonNull final GraphFxVariable variable) {
        variable.nameProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.valueStringProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.sliderEnabledProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.sliderMinStringProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.sliderMaxStringProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
        variable.sliderStepStringProperty().addListener((obs, oldValue, newValue) -> bumpRevision());
    }

    /**
     * Increments the revision counter by one.
     */
    private void bumpRevision() {
        revision.set(revision.get() + 1L);
    }

    /**
     * Validates that a variable name is syntactically valid and returns a normalized form (trimmed).
     *
     * @param name raw variable name input
     * @return trimmed variable name (never {@code null})
     * @throws IllegalArgumentException if the name is {@code null}, blank, or does not match {@link #VARIABLE_NAME_PATTERN}
     */
    private String requireValidVariableName(final String name) {
        final String trimmedName = (name == null) ? "" : name.trim();
        if (!trimmedName.matches(VARIABLE_NAME_PATTERN)) {
            throw new IllegalArgumentException("Invalid variable name: " + trimmedName);
        }
        return trimmedName;
    }

    /**
     * Validates that a string is not blank and returns the trimmed value.
     *
     * @param value   raw input value
     * @param message exception message used if validation fails
     * @return trimmed, non-blank value
     * @throws IllegalArgumentException if {@code value} is {@code null} or blank
     */
    private String requireNonBlank(final String value, final String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    /**
     * Parses a {@link BigDecimal} from a textual representation.
     *
     * @param valueText raw text input
     * @param message   exception message used if parsing fails
     * @return parsed {@link BigDecimal}
     * @throws IllegalArgumentException if the text is {@code null}, blank, or not a valid decimal number
     */
    private BigDecimal parseBigDecimal(final String valueText, final String message) {
        try {
            final String trimmedText = (valueText == null) ? "" : valueText.trim();
            return new BigDecimal(trimmedText);
        } catch (final Exception exception) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates the slider configuration of a variable.
     * <p>
     * Rules:
     * </p>
     * <ul>
     *     <li>{@code max > min}</li>
     *     <li>{@code step > 0}</li>
     * </ul>
     *
     * @param variable variable whose slider values are validated (must not be {@code null})
     * @throws IllegalArgumentException if the slider configuration is invalid
     */
    private void validateSliderConfiguration(@NonNull final GraphFxVariable variable) {
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

    /**
     * Checks whether a variable name is reserved for internal usage.
     *
     * @param variableName candidate name
     * @return {@code true} if reserved, otherwise {@code false}
     */
    private boolean isReservedVariableName(@NonNull final String variableName) {
        return RESERVED_FUNCTION_INPUT_VARIABLE.equalsIgnoreCase(variableName);
    }

    /**
     * Checks whether a variable name is already used by another variable in the model.
     *
     * @param variableName    candidate name (must not be {@code null})
     * @param ignoredVariable a variable to ignore during the search (useful for rename operations); may be {@code null}
     * @return {@code true} if another variable already uses the name, otherwise {@code false}
     */
    private boolean isVariableNameTaken(@NonNull final String variableName, final GraphFxVariable ignoredVariable) {
        return variables.stream()
                .anyMatch(existing -> existing != ignoredVariable && variableName.equals(existing.getName()));
    }

    public ObservableList<GraphFxFunction> getFunctions() {
        return this.functions;
    }

    public ObservableList<GraphFxVariable> getVariables() {
        return this.variables;
    }

    public ObservableList<GraphFxObject> getObjects() {
        return this.objects;
    }

    public GraphFxSettings getSettings() {
        return this.settings;
    }

    public LongProperty getRevision() {
        return this.revision;
    }
}
