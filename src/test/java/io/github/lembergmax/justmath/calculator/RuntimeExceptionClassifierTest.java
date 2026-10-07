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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArgumentException;
import io.github.lembergmax.justmath.bignumber.math.exceptions.MathArithmeticException;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RuntimeExceptionClassifierTest {

    private static CalculatorErrorCode codeOf(final Throwable throwable) {
        return RuntimeExceptionClassifier.classify(throwable).getCalculatorError().code();
    }

    @Nested
    @DisplayName("typed math exceptions")
    class TypedMathExceptions {

        @Test
        @DisplayName("the code of a MathArithmeticException is used whatever the message says")
        void arithmeticExceptionCodeWinsOverMessage() {
            final Throwable misleading = new MathArithmeticException(
                    CalculatorErrorCode.MATH_OVERFLOW, "Division by zero");

            assertEquals(CalculatorErrorCode.MATH_OVERFLOW, codeOf(misleading));
        }

        @Test
        @DisplayName("the code of a MathArgumentException is used whatever the message says")
        void argumentExceptionCodeWinsOverMessage() {
            final Throwable misleading = new MathArgumentException(
                    CalculatorErrorCode.MATH_LOG_NON_POSITIVE, "factorial must be non-negative");

            assertEquals(CalculatorErrorCode.MATH_LOG_NON_POSITIVE, codeOf(misleading));
        }

        @Test
        @DisplayName("a message that no keyword matches still resolves to the code of the exception")
        void rewordedMessageKeepsTheCode() {
            final Throwable reworded = new MathArithmeticException(
                    CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO, "the divisor was nil");

            assertEquals(CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO, codeOf(reworded));
        }

        @Test
        @DisplayName("the technical detail of the exception is kept")
        void technicalDetailIsKept() {
            final Throwable thrown = new MathArgumentException(
                    CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "Index must not be zero");

            assertEquals("Index must not be zero",
                    RuntimeExceptionClassifier.classify(thrown).getCalculatorError().technicalDetail());
        }

    }

    @Nested
    @DisplayName("exceptions from other libraries")
    class ForeignExceptions {

        @Test
        @DisplayName("BigDecimal's division by zero is a division by zero")
        void bigDecimalDivisionByZero() {
            assertEquals(CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO,
                    codeOf(new ArithmeticException("Division by zero")));
        }

        @Test
        @DisplayName("BigDecimal's scale overflow is an overflow")
        void bigDecimalScaleOverflow() {
            assertEquals(CalculatorErrorCode.MATH_OVERFLOW, codeOf(new ArithmeticException("Overflow")));
        }

        @Test
        @DisplayName("any other ArithmeticException is a domain error")
        void otherArithmeticExceptionIsDomainError() {
            assertEquals(CalculatorErrorCode.PROCESSING_DOMAIN_ERROR,
                    codeOf(new ArithmeticException("Illegal log(x) for x <= 0: x = 0")));
        }

        @Test
        @DisplayName("an unrecognised exception is an internal error")
        void unrecognisedExceptionIsInternalError() {
            assertEquals(CalculatorErrorCode.PROCESSING_INTERNAL, codeOf(new IllegalStateException("boom")));
            assertEquals(CalculatorErrorCode.PROCESSING_INTERNAL,
                    codeOf(new IllegalArgumentException("value must be positive")));
        }

        @Test
        @DisplayName("an exception without a message still yields a typed error")
        void exceptionWithoutMessage() {
            assertEquals(CalculatorErrorCode.PROCESSING_INTERNAL, codeOf(new IllegalStateException()));
        }

    }

}
