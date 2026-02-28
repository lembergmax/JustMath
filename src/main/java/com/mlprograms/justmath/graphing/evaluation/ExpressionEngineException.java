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

package com.mlprograms.justmath.graphing.evaluation;

/**
 * Base unchecked exception for failures inside the expression engine.
 * <p>
 * This type intentionally hides internal exceptions from the underlying calculator engine behind a stable API,
 * while still preserving the original cause for debugging purposes.
 */
public class ExpressionEngineException extends RuntimeException {

    /**
     * Creates an exception with message only.
     *
     * @param message message describing the failure (must not be {@code null})
     */
    public ExpressionEngineException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with message and cause.
     *
     * @param message message describing the failure (must not be {@code null})
     * @param cause   original cause (can be {@code null})
     */
    public ExpressionEngineException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
