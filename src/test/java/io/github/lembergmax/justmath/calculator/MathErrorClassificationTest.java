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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.errors.CalculatorResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Pins the {@link CalculatorErrorCode} that the engine reports for every kind of mathematical
 * failure that can be triggered from an expression. The code decides which localized message
 * the user sees, so it must not change by accident.
 */
class MathErrorClassificationTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @DisplayName("an expression that fails in the math layer reports the expected error code")
    @CsvSource(delimiter = '|', textBlock = """
            1/0                           | PROCESSING_DIVISION_BY_ZERO
            5%0                           | PROCESSING_DIVISION_BY_ZERO
            1/(1-1)                       | PROCESSING_DIVISION_BY_ZERO
            0^(-1)                        | PROCESSING_DIVISION_BY_ZERO
            acot(0)                       | PROCESSING_DIVISION_BY_ZERO
            coth(0)                       | PROCESSING_DOMAIN_ERROR
            tan(90)                       | PROCESSING_DOMAIN_ERROR
            tan(270)                      | PROCESSING_DOMAIN_ERROR
            cot(0)                        | PROCESSING_DOMAIN_ERROR
            cot(180)                      | PROCESSING_DOMAIN_ERROR
            atan2(0;0)                    | PROCESSING_DOMAIN_ERROR
            Pol(0;0)                      | PROCESSING_DOMAIN_ERROR
            Rec(-1;30)                    | PROCESSING_DOMAIN_ERROR
            ln(0)                         | MATH_LOG_NON_POSITIVE
            ln(-1)                        | MATH_LOG_NON_POSITIVE
            log2(0)                       | PROCESSING_DOMAIN_ERROR
            log10(-5)                     | PROCESSING_DOMAIN_ERROR
            logbase(8;1)                  | PROCESSING_DOMAIN_ERROR
            logbase(8;0)                  | PROCESSING_DOMAIN_ERROR
            logbase(0;2)                  | PROCESSING_DOMAIN_ERROR
            sqrt(-4)                      | MATH_ROOT_OF_NEGATIVE
            rootn(-8;2)                   | MATH_ROOT_OF_NEGATIVE
            rootn(-8;0.5)                 | MATH_ROOT_OF_NEGATIVE
            rootn(8;0)                    | PROCESSING_DOMAIN_ERROR
            (0-3)!                        | MATH_FACTORIAL_NEGATIVE
            2.5!                          | MATH_FACTORIAL_NON_INTEGER
            100001!                       | MATH_OVERFLOW
            1000000!                      | MATH_OVERFLOW
            10000000000000!               | MATH_OVERFLOW
            9^9999999999                  | MATH_OVERFLOW
            comb(2.5;1)                   | PROCESSING_DOMAIN_ERROR
            comb(0-1;1)                   | PROCESSING_DOMAIN_ERROR
            comb(3;5)                     | PROCESSING_DOMAIN_ERROR
            perm(3;5)                     | PROCESSING_DOMAIN_ERROR
            perm(2.5;1)                   | PROCESSING_DOMAIN_ERROR
            comb(999999999;400000000)     | MATH_OVERFLOW
            perm(999999999;999999999)     | MATH_OVERFLOW
            gcd(1.5;2)                    | PROCESSING_DOMAIN_ERROR
            lcm(1.5;2)                    | PROCESSING_DOMAIN_ERROR
            acosh(0.5)                    | PROCESSING_DOMAIN_ERROR
            atanh(2)                      | PROCESSING_DOMAIN_ERROR
            acoth(0.5)                    | PROCESSING_DOMAIN_ERROR
            asin(2)                       | PROCESSING_DOMAIN_ERROR
            acos(2)                       | PROCESSING_DOMAIN_ERROR
            beta(0;1)                     | PROCESSING_DOMAIN_ERROR
            gamma(0)                      | PROCESSING_DOMAIN_ERROR
            gamma(0-1)                    | PROCESSING_DOMAIN_ERROR
            summation(1;0;k)              | PROCESSING_DOMAIN_ERROR
            summation(1.5;3;k)            | PROCESSING_DOMAIN_ERROR
            summation(1;3;5)              | PROCESSING_DOMAIN_ERROR
            summation(1;100000000000;k)   | MATH_OVERFLOW
            RandInt(5;1)                  | PROCESSING_DOMAIN_ERROR
            RandInt(1.5;3)                | PROCESSING_DOMAIN_ERROR
            sqrt(1/0)                     | PROCESSING_DIVISION_BY_ZERO
            atan(0)/0                     | PROCESSING_DIVISION_BY_ZERO
            """)
    void expressionFailureReportsExpectedCode(final String expression, final CalculatorErrorCode expectedCode) {
        final CalculatorResult<?> result = new CalculatorEngine().evaluateSafe(expression);

        assertTrue(result.error().isPresent(), () -> expression + " must fail");
        assertEquals(expectedCode, result.error().orElseThrow().code(), expression);
    }

}
