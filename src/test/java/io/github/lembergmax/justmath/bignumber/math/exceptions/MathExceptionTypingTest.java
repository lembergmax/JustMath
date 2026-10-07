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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.stream.Stream;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Domain errors in the math layer must carry their {@link CalculatorErrorCode}, so that no caller has
 * to guess it from the message text, and they must stay catchable as the JDK exception types that
 * earlier versions threw.
 */
class MathExceptionTypingTest {

    private static BigNumber number(final String value) {
        return new BigNumber(value);
    }

    private static Arguments arithmetic(final String name, final Executable operation, final CalculatorErrorCode expectedCode) {
        return Arguments.of(name, operation, ArithmeticException.class, MathArithmeticException.class, expectedCode);
    }

    private static Arguments argument(final String name, final Executable operation, final CalculatorErrorCode expectedCode) {
        return Arguments.of(name, operation, IllegalArgumentException.class, MathArgumentException.class, expectedCode);
    }

    static Stream<Arguments> failures() {
        return Stream.of(
                arithmetic("1 / 0", () -> number("1").divide(number("0")), CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO),
                arithmetic("0 ^ -1.5", () -> number("0").power(number("-1.5")), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                arithmetic("9 ^ 9999999999", () -> number("9").power(number("9999999999")), CalculatorErrorCode.MATH_OVERFLOW),
                arithmetic("100001!", () -> number("100001").factorial(), CalculatorErrorCode.MATH_OVERFLOW),
                arithmetic("ln(0)", () -> number("0").ln(), CalculatorErrorCode.MATH_LOG_NON_POSITIVE),
                arithmetic("log2(0)", () -> number("0").log2(), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                arithmetic("log10(-5)", () -> number("-5").log10(), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                arithmetic("logBase(8; 1)", () -> number("8").logBase(number("1")), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                arithmetic("tan(90 deg)", () -> number("90").tan(TrigonometricMode.DEG), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                arithmetic("cot(0 deg)", () -> number("0").cot(TrigonometricMode.DEG), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                arithmetic("acot(0)", () -> number("0").acot(), CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO),
                arithmetic("comb(999999999; 400000000)", () -> number("999999999").combination(number("400000000")), CalculatorErrorCode.MATH_OVERFLOW),

                argument("5 mod 0", () -> number("5").modulo(number("0")), CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO),
                argument("5 rem 0", () -> number("5").remainder(number("0")), CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO),
                argument("2.5!", () -> number("2.5").factorial(), CalculatorErrorCode.MATH_FACTORIAL_NON_INTEGER),
                argument("-3!", () -> number("-3").factorial(), CalculatorErrorCode.MATH_FACTORIAL_NEGATIVE),
                argument("2nd root of -4", () -> number("-4").nthRoot(number("2")), CalculatorErrorCode.MATH_ROOT_OF_NEGATIVE),
                argument("0th root of 8", () -> number("8").nthRoot(number("0")), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("coth(0)", () -> number("0").coth(), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("acosh(0.5)", () -> number("0.5").acosh(), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("atanh(2)", () -> number("2").atanh(), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("acoth(0.5)", () -> number("0.5").acoth(), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("atan2(0; 0)", () -> number("0").atan2(number("0")), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("comb(2.5; 1)", () -> number("2.5").combination(number("1")), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("comb(3; 5)", () -> number("3").combination(number("5")), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("perm(3; 5)", () -> number("3").permutation(number("5")), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("gcd(1.5; 2)", () -> number("1.5").gcd(number("2")), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("lcm(1.5; 2)", () -> number("1.5").lcm(number("2")), CalculatorErrorCode.PROCESSING_DOMAIN_ERROR),
                argument("random integer in [5, 1)",
                        () -> BigNumbers.randomIntegerBigNumberInRange(number("5"), number("1"), Locale.US),
                        CalculatorErrorCode.PROCESSING_DOMAIN_ERROR)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("failures")
    @DisplayName("a domain error is a typed exception with its error code and still the JDK type it used to be")
    void domainErrorCarriesItsCodeAndKeepsTheJdkType(
            final String name,
            final Executable operation,
            final Class<? extends RuntimeException> jdkType,
            final Class<? extends RuntimeException> typedType,
            final CalculatorErrorCode expectedCode
    ) {
        final RuntimeException thrown = assertThrows(jdkType, operation, name);

        assertInstanceOf(typedType, thrown, name);
        assertEquals(expectedCode, ((ErrorCodeProvider) thrown).getErrorCode(), name);
    }

}
