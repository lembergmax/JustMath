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
import lombok.NonNull;

/**
 * Runtime exception representing a library-defined error category combined with an optional
 * human-readable detailed description.
 *
 * <p>
 * This exception type is intended to be thrown by the JustMath library when an operation cannot
 * be completed due to invalid input, inconsistent state, or a processing failure.
 * </p>
 *
 * <h2>Design goals</h2>
 * <ul>
 *   <li><strong>Consistency:</strong> every error has a standardized base message provided by
 *       {@link CustomExceptionMessages}.</li>
 *   <li><strong>Context:</strong> optional additional details can be provided via
 *       {@link #getDetailedMessage()} without changing the stable base message.</li>
 *   <li><strong>Ease of use:</strong> callers can catch one type ({@link CustomErrorException})
 *       while still inspecting the specific {@link #getCustomExceptionMessages()} value.</li>
 * </ul>
 *
 * <p>
 * The exception message returned by {@link #getMessage()} is set to the default message of
 * the given {@link CustomExceptionMessages} constant. The {@link #getDetailedMessage()} field
 * is intended for extra context such as:
 * </p>
 * <ul>
 *   <li>the offending token or expression snippet</li>
 *   <li>the variable name that caused a failure</li>
 *   <li>the phase of processing that failed</li>
 * </ul>
 *
 * <p>
 * This class is immutable. Once constructed, both the error category and the detailed message
 * cannot be changed.
 * </p>
 */
@Getter
public class CustomErrorException extends RuntimeException {

    /**
     * The standardized error category associated with this exception.
     * <p>
     * This field allows callers to implement structured error handling based on a stable enum
     * rather than parsing message strings.
     * </p>
     */
    @NonNull
    private final CustomExceptionMessages customExceptionMessages;

    /**
     * Additional context about the failure.
     * <p>
     * This message is intended for debugging/logging and can contain a more specific explanation
     * of what went wrong than the generic {@link CustomExceptionMessages#getMessage()} value.
     * </p>
     *
     * <p>
     * If no detail is provided by the caller, a default placeholder is used.
     * </p>
     */
    @NonNull
    private final String detailedMessage;

    /**
     * Creates a new exception for the given error category using a default detailed message.
     *
     * <p>
     * The exception's primary message ({@link #getMessage()}) will be set to
     * {@code customExceptionMessages.getMessage()}.
     * </p>
     *
     * @param customExceptionMessages the standardized error category; must not be {@code null}
     */
    public CustomErrorException(@NonNull final CustomExceptionMessages customExceptionMessages) {
        super(customExceptionMessages.getMessage());
        this.customExceptionMessages = customExceptionMessages;
        this.detailedMessage = "Detailed Message was not specified.";
    }

    /**
     * Creates a new exception for the given error category with an explicit detailed message.
     *
     * <p>
     * The exception's primary message ({@link #getMessage()}) will be set to
     * {@code customExceptionMessages.getMessage()}.
     * </p>
     *
     * @param customExceptionMessages the standardized error category; must not be {@code null}
     * @param detailedMessage additional context describing the failure; must not be {@code null}
     */
    public CustomErrorException(
            @NonNull final CustomExceptionMessages customExceptionMessages,
            @NonNull final String detailedMessage
    ) {
        super(customExceptionMessages.getMessage());
        this.customExceptionMessages = customExceptionMessages;
        this.detailedMessage = detailedMessage;
    }

}