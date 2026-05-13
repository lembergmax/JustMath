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

package com.mlprograms.justmath.calculator.exceptions;

import com.mlprograms.justmath.calculator.errors.CalculatorError;
import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.exceptions.CustomExceptionMessages;

import java.util.Map;

import lombok.NonNull;

/**
 * Exception raised for processing problems that occur while evaluating an already parsed
 * expression — for example division by zero, a value outside a function's mathematical
 * domain or an internal inconsistency in the evaluator.
 *
 * <p>
 * The legacy string-based constructors are kept for backwards compatibility. New code should
 * prefer the typed constructors that take a {@link CalculatorErrorCode}.
 * </p>
 */
public class ProcessingErrorException extends CalculatorException {

    /**
     * Creates an exception with a default technical detail message.
     */
    public ProcessingErrorException() {
        super(CustomExceptionMessages.PROCESSING_ERROR, "Detailed Message was not specified.");
    }

    /**
     * Legacy constructor that creates an exception from a free-form English detail message.
     *
     * @param detailedMessage technical English detail; must not be {@code null}
     */
    public ProcessingErrorException(@NonNull final String detailedMessage) {
        super(CustomExceptionMessages.PROCESSING_ERROR, detailedMessage);
    }

    /**
     * Creates an exception from a structured error code without parameters.
     *
     * @param code            the structured error code; must not be {@code null}
     * @param technicalDetail technical English detail; must not be {@code null}
     */
    public ProcessingErrorException(
            @NonNull final CalculatorErrorCode code,
            @NonNull final String technicalDetail
    ) {
        super(code, technicalDetail);
    }

    /**
     * Creates an exception from a structured error code with named parameters.
     *
     * @param code            the structured error code; must not be {@code null}
     * @param params          named substitution parameters; must not be {@code null}
     * @param technicalDetail technical English detail; must not be {@code null}
     */
    public ProcessingErrorException(
            @NonNull final CalculatorErrorCode code,
            @NonNull final Map<String, String> params,
            @NonNull final String technicalDetail
    ) {
        super(new CalculatorError(code, params, technicalDetail));
    }

}
