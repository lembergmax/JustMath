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

package io.github.lembergmax.justmath.bignumber.math.exceptions;

import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Thrown when a calculation has no result: a division by zero, an argument outside the domain of a
 * function, or a result that exceeds a documented size limit.
 *
 * <p>This type extends {@link ArithmeticException}, so code that already catches
 * {@code ArithmeticException} keeps working. It adds the {@link CalculatorErrorCode} that describes
 * the failure.</p>
 */
@Getter
public final class MathArithmeticException extends ArithmeticException implements ErrorCodeProvider {

    private static final long serialVersionUID = 1L;

    /**
     * The structured error code of this failure; never {@code null}.
     */
    private final CalculatorErrorCode errorCode;

    /**
     * Creates an exception for a calculation that has no result.
     *
     * @param errorCode the structured error code; must not be {@code null}
     * @param message   technical English detail; must not be {@code null}
     */
    public MathArithmeticException(
            @NonNull final CalculatorErrorCode errorCode,
            @NonNull final String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }

}
