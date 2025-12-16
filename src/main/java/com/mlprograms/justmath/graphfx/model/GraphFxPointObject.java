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
 * Immutable graph object representing a labeled point in world coordinates.
 * <p>
 * A point can optionally be associated with a function (via {@link #functionId()}) to indicate that it was
 * created from or belongs to a specific function (e.g., point-on-function, root marker, intersection marker).
 * </p>
 *
 * <p>
 * The {@link #style()} controls how the point is rendered (fill/stroke color, opacity, etc.), while
 * {@link #visible()} allows toggling the point without removing it from the model.
 * </p>
 *
 * @param id         unique identifier of the object
 * @param name       short label shown near the point (e.g., {@code "P"}, {@code "S"}, {@code "x₀"})
 * @param visible    whether the point should be rendered
 * @param style      rendering style (color, stroke width, alpha)
 * @param x          x-coordinate in world space
 * @param y          y-coordinate in world space
 * @param functionId optional reference to the function this point belongs to (nullable)
 */
public record GraphFxPointObject(@NonNull UUID id, @NonNull String name, boolean visible, @NonNull GraphFxStyle style,
                                 @NonNull BigDecimal x, @NonNull BigDecimal y,
                                 UUID functionId) implements GraphFxObject {

    /**
     * Creates a visible point object with a default visual style.
     * <p>
     * The returned point:
     * </p>
     * <ul>
     *     <li>gets a random {@link UUID} as identifier</li>
     *     <li>is marked as {@code visible}</li>
     *     <li>uses a default black {@link GraphFxStyle} with stroke width {@code 2.0} and alpha {@code 1.0}</li>
     * </ul>
     *
     * @param name       the label of the point (must not be {@code null})
     * @param x          x-coordinate in world space (must not be {@code null})
     * @param y          y-coordinate in world space (must not be {@code null})
     * @param functionId the referenced function id, or {@code null} if the point is not bound to a function
     * @return a new {@link GraphFxPointObject} instance
     */
    public static GraphFxPointObject of(@NonNull final String name, @NonNull final BigDecimal x, @NonNull final BigDecimal y, final UUID functionId) {
        return new GraphFxPointObject(UUID.randomUUID(), name, true, new GraphFxStyle(Color.BLACK, 2.0, 1.0), x, y, functionId);
    }

    /**
     * Returns the function id referenced by this object, if any.
     * <p>
     * This is used by the model to determine which objects should be removed/updated when a function
     * is deleted or changed.
     * </p>
     *
     * @return the referenced function id or {@code null} if this point is not associated with a function
     */
    @Override
    public UUID referencesFunctionId() {
        return functionId;
    }

}
