/*
 * Copyright (c) 2026 Max Lemberg
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

package com.mlprograms.justmath.graphing.api;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only evaluation context for compiled plot expressions.
 * <p>
 * The context provides constant variable bindings (e.g. {@code a=2.0}) that may be used by an expression
 * while sampling points. The independent variable {@code x} is passed separately to
 * {@link CompiledPlotExpression#evaluate(double, PlotEvaluationContext)}.
 * </p>
 */
public interface PlotEvaluationContext {

    /**
     * Returns an unmodifiable view of all variables bound in this context.
     *
     * @return unmodifiable variable map (never {@code null})
     */
    Map<String, Double> variables();

    /**
     * Returns the variable value if present or a default value otherwise.
     *
     * @param variableName the variable name (must not be {@code null})
     * @param defaultValue the value returned when the variable does not exist
     * @return the stored variable value or {@code defaultValue}
     */
    default double getOrDefault(final String variableName, final double defaultValue) {
        Objects.requireNonNull(variableName, "variableName must not be null");
        final Double value = variables().get(variableName);
        return value != null ? value : defaultValue;
    }

    /**
     * Creates a minimal immutable {@link PlotEvaluationContext} from a map.
     * <p>
     * The provided map is defensively copied and wrapped as unmodifiable.
     * </p>
     *
     * @param variables variable bindings (must not be {@code null})
     * @return immutable evaluation context
     */
    static PlotEvaluationContext of(final Map<String, Double> variables) {
        Objects.requireNonNull(variables, "variables must not be null");
        final Map<String, Double> copy = Map.copyOf(variables);
        return () -> Collections.unmodifiableMap(copy);
    }
}
