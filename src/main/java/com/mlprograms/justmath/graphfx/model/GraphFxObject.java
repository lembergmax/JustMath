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

import java.util.UUID;

/**
 * Base contract for drawable objects on the GraphFX canvas.
 * <p>
 * Implementations represent visual entities such as points, lines, or integral areas.
 * The graph view renders these objects based on their {@link #style()} and {@link #visible()} flag.
 * </p>
 *
 * <h2>Identity and lifecycle</h2>
 * <p>
 * Each object has a stable {@link #id()} to support updates, selection, and removal without relying on
 * object identity. Objects can optionally reference a function via {@link #referencesFunctionId()} so that
 * dependent objects can be cleaned up automatically when a function is removed.
 * </p>
 *
 * <h2>Sealed hierarchy</h2>
 * <p>
 * This interface is sealed to keep the set of drawable object types explicit and controlled.
 * Add new object types by extending the permits list and implementing all required members.
 * </p>
 */
public sealed interface GraphFxObject
        permits GraphFxPointObject, GraphFxLineObject, GraphFxIntegralObject {

    /**
     * Returns the unique identifier of this object.
     *
     * @return a stable {@link UUID} for this object
     */
    UUID id();

    /**
     * Returns a short name/label for this object, typically rendered near it.
     *
     * @return the object label
     */
    String name();

    /**
     * Indicates whether this object should be rendered.
     *
     * @return {@code true} if visible, otherwise {@code false}
     */
    boolean visible();

    /**
     * Returns the rendering style used by the view (e.g., color, stroke width, opacity).
     *
     * @return the object's {@link GraphFxStyle}
     */
    GraphFxStyle style();

    /**
     * Returns the identifier of a function that this object depends on, if any.
     * <p>
     * For example, a point-on-function, tangent line, or integral region may reference the function it was
     * derived from. The model can use this relationship to remove dependent objects when the referenced
     * function is deleted.
     * </p>
     *
     * @return the referenced function id, or {@code null} if the object is independent
     */
    UUID referencesFunctionId();

}
