/*
 * Copyright (c) 2025 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.graphfx.api.plot;

import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable plot request describing what to plot and how dense the sampling should be.
 * <p>
 * A request contains:
 * <ul>
 *   <li>the expression to evaluate ({@link #expression()})</li>
 *   <li>variable bindings ({@link #variables()})</li>
 *   <li>world bounds ({@link #worldBounds()})</li>
 *   <li>pixel size of the target area ({@link #pixelWidth()}, {@link #pixelHeight()})</li>
 * </ul>
 *
 * <h2>Pixel size</h2>
 * The pixel dimensions are used to derive a sampling density. Plot engines typically compute one sample per pixel
 * column (explicit functions) or a grid derived from the pixel dimensions (implicit functions).
 *
 * <h2>Constraints</h2>
 * <ul>
 *   <li>{@code expression} must not be blank</li>
 *   <li>{@code variables} must not contain {@code null} keys or values</li>
 *   <li>{@code worldBounds} must not be {@code null}</li>
 *   <li>{@code pixelWidth} and {@code pixelHeight} must be {@code >= 1}</li>
 * </ul>
 *
 * @param expression  expression to evaluate (syntax depends on the {@link GraphFxPlotEngine} implementation)
 * @param variables   variable values as strings (engine-specific parsing; typically decimal numbers)
 * @param worldBounds world coordinate bounds
 * @param pixelWidth  width of the target area in pixels (>= 1)
 * @param pixelHeight height of the target area in pixels (>= 1)
 *
 * @since 1.0
 */
public record GraphFxPlotRequest(
        @NonNull String expression,
        @NonNull Map<String, String> variables,
        @NonNull GraphFxWorldBounds worldBounds,
        int pixelWidth,
        int pixelHeight
) {

    /**
     * Returns the world bounds associated with this request.
     * <p>
     * This is a compatibility alias for older code that used {@code request.bounds()} instead of
     * {@link #worldBounds()}.
     *
     * @return the requested world bounds
     *
     * @since 1.0
     */
    public GraphFxWorldBounds bounds() {
        return worldBounds;
    }

    /**
     * Canonical constructor that validates and defensively copies inputs.
     *
     * @throws NullPointerException     if any required argument is {@code null}
     * @throws IllegalArgumentException if constraints are violated
     */
    public GraphFxPlotRequest {
        Objects.requireNonNull(expression, "expression must not be null.");
        Objects.requireNonNull(variables, "variables must not be null.");
        Objects.requireNonNull(worldBounds, "worldBounds must not be null.");

        final String trimmedExpression = expression.trim();
        if (trimmedExpression.isEmpty()) {
            throw new IllegalArgumentException("expression must not be blank.");
        }

        if (pixelWidth < 1) {
            throw new IllegalArgumentException("pixelWidth must be >= 1.");
        }
        if (pixelHeight < 1) {
            throw new IllegalArgumentException("pixelHeight must be >= 1.");
        }

        validateVariables(variables);

        expression = trimmedExpression;
        variables = Collections.unmodifiableMap(new HashMap<>(variables));
    }

    private static void validateVariables(@NonNull final Map<String, String> variables) {
        for (final Map.Entry<String, String> entry : variables.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("variables must not contain null keys.");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("variables must not contain null values (variable '" + entry.getKey() + "').");
            }
            if (entry.getKey().trim().isEmpty()) {
                throw new IllegalArgumentException("variables must not contain blank keys.");
            }
        }
    }
}
