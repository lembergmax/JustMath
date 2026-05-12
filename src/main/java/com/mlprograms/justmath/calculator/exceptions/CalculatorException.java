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

package com.mlprograms.justmath.calculator.exceptions;

import com.mlprograms.justmath.calculator.errors.CalculatorError;
import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.exceptions.CustomErrorException;
import com.mlprograms.justmath.exceptions.CustomExceptionMessages;
import lombok.Getter;
import lombok.NonNull;

/**
 * Common base class for every exception thrown by the {@code CalculatorEngine}.
 *
 * <p>
 * This class extends {@link CustomErrorException} with a structured {@link CalculatorError}
 * descriptor that carries the {@link CalculatorErrorCode}, named parameters and an optional
 * position. The descriptor enables localized, user-facing formatting through
 * {@link CalculatorError#format}.
 * </p>
 *
 * <p>
 * Existing consumers that only inspect {@link Throwable#getMessage()} or
 * {@link CustomErrorException#getCustomExceptionMessages()} remain fully compatible: the
 * primary message still resolves to the category default (for example {@code "Syntax Error"}
 * or {@code "Processing Error"}).
 * </p>
 */
@Getter
public abstract class CalculatorException extends CustomErrorException {

    /**
     * Structured error descriptor when the exception was created from a typed code, or
     * {@code null} when the exception was produced by one of the legacy string constructors.
     */
    private final CalculatorError calculatorError;

    /**
     * Constructor used by the legacy string-based factory paths.
     *
     * @param category        category that determines {@link Throwable#getMessage()}; must not be {@code null}
     * @param detailedMessage technical English detail; must not be {@code null}
     */
    protected CalculatorException(
            @NonNull final CustomExceptionMessages category,
            @NonNull final String detailedMessage
    ) {
        super(category, detailedMessage);
        this.calculatorError = null;
    }

    /**
     * Constructor that wraps a fully populated {@link CalculatorError}.
     *
     * @param calculatorError structured error descriptor; must not be {@code null}
     */
    protected CalculatorException(@NonNull final CalculatorError calculatorError) {
        super(calculatorError.code().getCategory(), calculatorError.technicalDetail());
        this.calculatorError = calculatorError;
    }

    /**
     * Convenience constructor that builds a {@link CalculatorError} without parameters and
     * without a position.
     *
     * @param code            the structured error code; must not be {@code null}
     * @param technicalDetail technical English detail; must not be {@code null}
     */
    protected CalculatorException(
            @NonNull final CalculatorErrorCode code,
            @NonNull final String technicalDetail
    ) {
        this(new CalculatorError(code, technicalDetail));
    }
}
