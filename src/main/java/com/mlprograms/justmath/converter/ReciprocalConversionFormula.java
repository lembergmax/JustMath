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

package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;

import java.math.MathContext;

import lombok.NonNull;

/**
 * Immutable reciprocal conversion formula of the form:
 *
 * <pre>
 * base = scale / value
 * </pre>
 *
 * <p>
 * The inverse conversion is symmetric:
 * </p>
 *
 * <pre>
 * value = scale / base
 * </pre>
 *
 * <p>
 * This formula is required for units whose source quantity is the reciprocal of the category
 * base quantity. The typical example is fuel consumption: the category base is {@code m/L}
 * (distance per litre), so a source unit such as {@code L/100 km} is its reciprocal. The
 * standard affine form {@code base = value * scale + offset} cannot express this relationship
 * mathematically; it only happens to produce the correct value for {@code value = 1}.
 * </p>
 *
 * <p>
 * The class is intentionally package-private to keep the public API small. External
 * consumers obtain instances through {@link ConversionFormulas#reciprocal(BigNumber)}.
 * </p>
 *
 * <p>
 * This implementation is immutable and thread-safe.
 * </p>
 */
final class ReciprocalConversionFormula implements ConversionFormula {

    /**
     * Multiplicative factor used to scale values via {@code base = scale / value}.
     *
     * <p>
     * Must not be zero, otherwise both the forward and inverse conversions would be undefined.
     * </p>
     */
    private final BigNumber scale;

    /**
     * Creates a new reciprocal conversion formula.
     *
     * @param scale the scaling factor; must not be {@code null} and must not be zero
     * @throws IllegalArgumentException if {@code scale} is zero
     */
    ReciprocalConversionFormula(@NonNull final BigNumber scale) {
        if (scale.compareTo(BigNumbers.ZERO) == 0) {
            throw new IllegalArgumentException("scale must not be zero");
        }
        this.scale = scale;
    }

    /**
     * Converts a value expressed in the concrete unit into the base unit using:
     *
     * <pre>
     * base = scale / value
     * </pre>
     *
     * @param value       input value in the concrete unit; must not be {@code null}
     * @param mathContext math context controlling precision and rounding; must not be {@code null}
     * @return the value expressed in the base unit; never {@code null}
     * @throws ArithmeticException if {@code value} is zero
     */
    @Override
    public BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        return scale.divide(value, mathContext);
    }

    /**
     * Converts a value expressed in the base unit back to the concrete unit using:
     *
     * <pre>
     * value = scale / base
     * </pre>
     *
     * @param baseValue   input value in the base unit; must not be {@code null}
     * @param mathContext math context controlling precision and rounding; must not be {@code null}
     * @return the value expressed in the concrete unit; never {@code null}
     * @throws ArithmeticException if {@code baseValue} is zero
     */
    @Override
    public BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        return scale.divide(baseValue, mathContext);
    }

}
