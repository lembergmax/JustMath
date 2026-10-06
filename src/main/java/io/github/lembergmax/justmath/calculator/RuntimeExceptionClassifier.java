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

package io.github.lembergmax.justmath.calculator;

import java.util.Locale;
import java.util.Objects;

import io.github.lembergmax.justmath.bignumber.math.exceptions.ErrorCodeProvider;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.exceptions.ProcessingErrorException;
import lombok.NonNull;

/**
 * Maps unchecked exceptions that escape the math layer to typed {@link ProcessingErrorException}s.
 *
 * <p>This class is stateless and package-private; {@link CalculatorEngine} is its only caller.</p>
 */
final class RuntimeExceptionClassifier {

    private static final String BIG_DECIMAL_DIVISION_BY_ZERO = "division by zero";

    private static final String BIG_DECIMAL_OVERFLOW = "overflow";

    private RuntimeExceptionClassifier() {
    }

    /**
     * Maps an unchecked exception that escaped the math layer to a typed
     * {@link ProcessingErrorException}, so that downstream formatting can resolve a localized
     * template instead of showing the English technical message.
     *
     * <p>A math exception that implements {@link ErrorCodeProvider} supplies its own code and is never
     * classified by its message. Only exceptions from other libraries, such as {@link java.math.BigDecimal},
     * go through {@link #classifyForeignException}.</p>
     *
     * @param throwable the unchecked exception thrown during evaluation; must not be {@code null}
     * @return a localized-friendly {@link ProcessingErrorException}; never {@code null}
     */
    static ProcessingErrorException classify(@NonNull final Throwable throwable) {
        final String message = Objects.requireNonNullElse(throwable.getMessage(), "");
        final CalculatorErrorCode code = throwable instanceof ErrorCodeProvider errorCodeProvider
                ? errorCodeProvider.getErrorCode()
                : classifyForeignException(throwable, message);
        return new ProcessingErrorException(code, message.isEmpty() ? "Processing error" : message);
    }

    /**
     * Classifies an exception that does not carry an error code. The JDK and the libraries the math
     * layer builds on report these cases only through the exception type and message:
     * {@code BigDecimal} reports {@code "Division by zero"} and {@code "Overflow"}, and any other
     * {@link ArithmeticException} means the calculation has no result.
     *
     * @param throwable the exception to classify; must not be {@code null}
     * @param message   the message of {@code throwable}, empty if it has none; must not be {@code null}
     * @return the error code; never {@code null}
     */
    private static CalculatorErrorCode classifyForeignException(final Throwable throwable, final String message) {
        final String lowerCaseMessage = message.toLowerCase(Locale.ROOT);
        if (lowerCaseMessage.contains(BIG_DECIMAL_DIVISION_BY_ZERO)) {
            return CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO;
        }
        if (lowerCaseMessage.contains(BIG_DECIMAL_OVERFLOW)) {
            return CalculatorErrorCode.MATH_OVERFLOW;
        }
        if (throwable instanceof ArithmeticException) {
            return CalculatorErrorCode.PROCESSING_DOMAIN_ERROR;
        }
        return CalculatorErrorCode.PROCESSING_INTERNAL;
    }

}
