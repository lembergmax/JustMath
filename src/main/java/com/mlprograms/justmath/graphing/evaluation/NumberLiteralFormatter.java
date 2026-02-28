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

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * Utility for producing numeric literals that are safe to inject into the JustMath {@code CalculatorEngine}.
 * <p>
 * Problem this solves:
 * {@link Double#toString(double)} may produce scientific notation like {@code 1.0E7}.
 * The JustMath tokenizer can interpret {@code E} as a variable, which then triggers errors such as:
 * {@code IllegalArgumentException: Variable 'E' is not defined.}
 * <p>
 * Therefore, all doubles used as variable bindings for the calculator must be formatted as
 * <b>plain decimal</b> strings without exponent notation.
 */
@UtilityClass
public class NumberLiteralFormatter {

    /**
     * Converts a finite {@code double} into a calculator-safe decimal literal (no exponent).
     *
     * @param value finite double value
     * @return decimal literal usable as variable value in {@code CalculatorEngine} (never {@code null})
     * @throws IllegalArgumentException if the value is {@code NaN} or infinite
     */
    public static String toCalculatorLiteral(final double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be finite");
        }

        // BigDecimal.valueOf uses a canonical string representation of the double.
        // toPlainString removes exponent notation, which is critical for CalculatorEngine variable replacement.
        final BigDecimal decimal = BigDecimal.valueOf(value).stripTrailingZeros();
        final String plain = decimal.toPlainString();

        // BigDecimal can return "-0" after stripTrailingZeros for tiny negative values.
        if ("-0".equals(plain)) {
            return "0";
        }
        return plain;
    }
}
