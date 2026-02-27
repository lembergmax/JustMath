/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package com.mlprograms.justmath.exceptions;

import lombok.Getter;

/**
 * Centralized, strongly typed catalog of human-readable error messages used by the JustMath library.
 *
 * <p>
 * This enum exists to avoid scattering hard-coded ("magic") strings across the code base and to provide
 * a single authoritative place where library-wide error message texts are defined.
 * </p>
 *
 * <p>
 * The enum constants represent coarse-grained categories of failures that may occur while parsing,
 * evaluating, or otherwise processing mathematical expressions and related inputs.
 * Each constant is associated with a stable default message intended for:
 * </p>
 * <ul>
 *   <li>exception summaries (e.g., the {@link Throwable#getMessage()} of an exception)</li>
 *   <li>UI display, logging, or debugging output</li>
 *   <li>consistent messaging across different modules of the library</li>
 * </ul>
 *
 * <p>
 * Note: The message strings are intentionally kept simple and generic. For more context (e.g., the
 * exact token or variable name), use a dedicated details field such as
 * {@link CustomErrorException#getDetailedMessage()}.
 * </p>
 */
public enum CustomExceptionMessages {

    /**
     * Indicates that the input could not be parsed because it violates the expected syntax.
     * <p>
     * Typical examples:
     * </p>
     * <ul>
     *   <li>unexpected characters</li>
     *   <li>unbalanced parentheses</li>
     *   <li>invalid token sequences</li>
     * </ul>
     */
    SYNTAX_ERROR("Syntax Error"),

    /**
     * Indicates a general processing failure that occurred while evaluating or transforming data.
     * <p>
     * This constant should be used when a more specific message category is not available,
     * but the operation could not be completed successfully.
     * </p>
     */
    PROCESSING_ERROR("Processing Error"),

    /**
     * Indicates that a variable resolution/evaluation encountered a cyclic reference.
     * <p>
     * Typical examples:
     * </p>
     * <ul>
     *   <li>{@code a = b + 1} and {@code b = a + 1}</li>
     *   <li>{@code a = a + 1}</li>
     * </ul>
     */
    CYCLIC_VARIABLE_REFERENCE("Cyclic Variable Reference");

    /**
     * The default human-readable message text for this error category.
     * <p>
     * This text is typically used as the primary exception message and is meant to be stable
     * and understandable without additional context.
     * </p>
     */
    @Getter
    private final String message;

    /**
     * Creates an enum constant with the given default message text.
     *
     * @param message the human-readable default message for this constant; must not be {@code null}
     */
    CustomExceptionMessages(final String message) {
        this.message = message;
    }

}