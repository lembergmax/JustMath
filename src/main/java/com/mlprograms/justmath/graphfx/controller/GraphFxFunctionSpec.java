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

package com.mlprograms.justmath.graphfx.controller;

import javafx.scene.paint.Color;

/**
 * Immutable specification describing a function to be added to a {@code GraphFX} model/controller.
 * <p>
 * This record is typically used at API boundaries (e.g., controller constructors or import/export code)
 * to pass the minimal information required to create a function:
 * </p>
 * <ul>
 *     <li>a human-readable {@link #name()}</li>
 *     <li>an {@link #expression()} string in the calculator language</li>
 *     <li>a {@link #color()} used for rendering</li>
 * </ul>
 *
 * <p>
 * The canonical constructor enforces that {@code name} and {@code expression} are not blank.
 * Color is optional and may be {@code null} if the caller wants the system to pick a default.
 * </p>
 *
 * @param name       function name shown in the UI (must not be blank)
 * @param expression expression string in the calculator language (must not be blank)
 * @param color      preferred function color; may be {@code null} to indicate "use default"
 */
public record GraphFxFunctionSpec(String name, String expression, Color color) {

    /**
     * Creates a new {@link GraphFxFunctionSpec} and validates required fields.
     * <p>
     * This compact constructor ensures that {@link #name()} and {@link #expression()} contain meaningful
     * content. It does not validate the syntax of {@link #expression()} because that responsibility belongs
     * to the evaluation engine / parser.
     * </p>
     *
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank, or if {@code expression} is
     *                                  {@code null} or blank
     */
    public GraphFxFunctionSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Function name must not be empty.");
        }
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Function expression must not be empty.");
        }
    }

}
