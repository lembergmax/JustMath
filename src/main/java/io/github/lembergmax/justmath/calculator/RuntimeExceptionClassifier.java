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

import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.exceptions.ProcessingErrorException;
import lombok.NonNull;

/**
 * Maps unchecked exceptions that escape the math layer to typed {@link ProcessingErrorException}s.
 *
 * <p>This class is stateless and package-private; {@link CalculatorEngine} is its only caller.</p>
 */
final class RuntimeExceptionClassifier {

    private RuntimeExceptionClassifier() {
    }

    /**
     * Maps an unchecked runtime exception that bubbled out of the lower-level math layer
     * (typically {@link ArithmeticException} or {@link IllegalArgumentException} from
     * {@code BigNumber} operations) to a typed {@link ProcessingErrorException} with a
     * matching {@link CalculatorErrorCode} so that downstream formatting can resolve a
     * localized template. Without this mapping such exceptions would surface verbatim in
     * English and bypass the {@code i18n/calculator_errors_*.properties} bundles entirely.
     *
     * @param throwable the unchecked exception thrown during evaluation; must not be {@code null}
     * @return a localized-friendly {@link ProcessingErrorException}; never {@code null}
     */
    static ProcessingErrorException classify(@NonNull final Throwable throwable) {
        final String message = Objects.requireNonNullElse(throwable.getMessage(), "");
        final String lower = message.toLowerCase(Locale.ROOT);
        final CalculatorErrorCode code;
        if (lower.contains("division by zero")
                || lower.contains("divisor zero")
                || lower.contains("undefined for value 0")
                || lower.contains("undefined for x = 0")
                || lower.contains("normalize list with sum 0")) {
            code = CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO;
        } else if (lower.contains("factorial") && lower.contains("non-negative")) {
            code = CalculatorErrorCode.MATH_FACTORIAL_NEGATIVE;
        } else if (lower.contains("factorial") && lower.contains("integer")) {
            code = CalculatorErrorCode.MATH_FACTORIAL_NON_INTEGER;
        } else if (lower.contains("ln(x) undefined")
                || lower.contains("number must be positive")
                || lower.contains("base must be positive")
                || ((lower.contains("log") || lower.contains("logarith")) && lower.contains("positive"))) {
            code = CalculatorErrorCode.MATH_LOG_NON_POSITIVE;
        } else if (lower.contains("root of a negative")
                || lower.contains("sqrt is only defined for non-negative")) {
            code = CalculatorErrorCode.MATH_ROOT_OF_NEGATIVE;
        } else if (lower.contains("overflow") || lower.contains("too large")) {
            code = CalculatorErrorCode.MATH_OVERFLOW;
        } else if (throwable instanceof ArithmeticException
                || lower.contains("undefined")
                || lower.contains("only defined")
                || lower.contains("must be")
                || lower.contains("must satisfy")
                || lower.contains("cannot be")
                || lower.contains("non-negative")
                || lower.contains("not a real number")
                || lower.contains("must not be")
                || lower.contains("only positive")
                || lower.contains("greater than")
                || lower.contains("less than")) {
            code = CalculatorErrorCode.PROCESSING_DOMAIN_ERROR;
        } else {
            code = CalculatorErrorCode.PROCESSING_INTERNAL;
        }
        return new ProcessingErrorException(code, message.isEmpty() ? "Processing error" : message);
    }

}
