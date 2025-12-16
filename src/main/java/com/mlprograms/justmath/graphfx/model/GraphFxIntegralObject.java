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

import javafx.scene.paint.Color;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Immutable graph object representing a definite integral region of a function.
 * <p>
 * The object stores the integration bounds {@link #a()} and {@link #b()} in world coordinates and the
 * precomputed integral {@link #value()} (typically created by a numerical method such as Simpson's rule).
 * The view can render the shaded area between the function and the x-axis over the interval.
 * </p>
 *
 * <h2>Function association</h2>
 * <p>
 * An integral object is inherently tied to a specific function, referenced by {@link #functionId()}.
 * This allows the model to remove associated integral objects automatically if the function is removed.
 * </p>
 *
 * @param id         unique identifier of the object
 * @param name       label of the object (commonly {@code "∫"})
 * @param visible    whether the integral region should be rendered
 * @param style      rendering style (color, stroke width, opacity); typically semi-transparent fill
 * @param functionId id of the function this integral belongs to
 * @param a          first integration bound (not necessarily smaller than {@code b})
 * @param b          second integration bound (not necessarily larger than {@code a})
 * @param value      computed integral value for the interval {@code [a..b]}
 */
public record GraphFxIntegralObject(@NonNull UUID id, @NonNull String name, boolean visible,
                                    @NonNull GraphFxStyle style, @NonNull UUID functionId, @NonNull BigDecimal a,
                                    @NonNull BigDecimal b, @NonNull BigDecimal value) implements GraphFxObject {

    /**
     * Creates a visible integral object with a default integral label and a semi-transparent style.
     * <p>
     * The returned object:
     * </p>
     * <ul>
     *     <li>gets a random {@link UUID} as identifier</li>
     *     <li>uses {@code "∫"} as its label</li>
     *     <li>is marked as {@code visible}</li>
     *     <li>uses the given {@code color} with a default alpha of {@code 0.20} for shaded rendering</li>
     * </ul>
     *
     * @param functionId the id of the function the integral is associated with (must not be {@code null})
     * @param a          first integration bound (must not be {@code null})
     * @param b          second integration bound (must not be {@code null})
     * @param value      computed integral value (must not be {@code null})
     * @param color      base color used for the shaded area (must not be {@code null})
     * @return a new {@link GraphFxIntegralObject} instance
     */
    public static GraphFxIntegralObject of(@NonNull final UUID functionId, @NonNull final BigDecimal a, @NonNull final BigDecimal b, @NonNull final BigDecimal value, @NonNull final Color color) {
        return new GraphFxIntegralObject(UUID.randomUUID(), "∫", true, new GraphFxStyle(color, 1.5, 0.20), functionId, a, b, value);
    }

    /**
     * Returns the identifier of the function referenced by this object.
     * <p>
     * Integral objects are tied to a function and should typically be removed if that function is removed.
     * </p>
     *
     * @return the referenced function id (never {@code null})
     */
    @Override
    public UUID referencesFunctionId() {
        return functionId;
    }

}
