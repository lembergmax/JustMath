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

package io.github.lembergmax.justmath.converter;

import java.math.MathContext;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import lombok.NonNull;

/**
 * Immutable linear conversion formula expressed as an exact rational scale:
 *
 * <pre>
 * base  = value * numerator / denominator
 * value = base  * denominator / numerator
 * </pre>
 *
 * <p>
 * This formula exists so that conversion factors with non-terminating decimal
 * representations (e.g. {@code 1/3}, {@code 1000/3600 = 5/18} for {@code km/h -> m/s})
 * can be stored exactly. The actual division is deferred and executed at
 * conversion time using the caller-supplied {@link MathContext}, which avoids the
 * accumulated rounding artifacts caused by pre-rounded decimal scale factors
 * such as {@code 0.2777777778}.
 * </p>
 *
 * <p>
 * The type is package-private on purpose to keep the public API minimal and stable.
 * Library users obtain instances via {@link ConversionFormulas#linear(BigNumber, BigNumber)}.
 * </p>
 *
 * <p>
 * This implementation is immutable and thread-safe.
 * </p>
 */
final class RationalConversionFormula implements ConversionFormula {

    /**
     * Numerator of the exact rational scale-to-base factor.
     *
     * <p>
     * Must not be zero, otherwise both the forward and inverse conversions would be undefined.
     * </p>
     */
    private final BigNumber numerator;

    /**
     * Denominator of the exact rational scale-to-base factor.
     *
     * <p>
     * Must not be zero, otherwise the forward conversion would be undefined.
     * </p>
     */
    private final BigNumber denominator;

    /**
     * Creates a new rational conversion formula.
     *
     * @param numerator   numerator of the scale factor into base units; must not be {@code null} and must not be zero
     * @param denominator denominator of the scale factor into base units; must not be {@code null} and must not be zero
     * @throws IllegalArgumentException if {@code numerator} or {@code denominator} is zero
     */
    RationalConversionFormula(@NonNull final BigNumber numerator, @NonNull final BigNumber denominator) {
        if (numerator.compareTo(BigNumbers.ZERO) == 0) {
            throw new IllegalArgumentException("numerator must not be zero");
        }
        if (denominator.compareTo(BigNumbers.ZERO) == 0) {
            throw new IllegalArgumentException("denominator must not be zero");
        }
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Converts a value expressed in the concrete unit to the base unit using:
     *
     * <pre>
     * base = value * numerator / denominator
     * </pre>
     *
     * <p>
     * The division is executed last so the caller-supplied {@link MathContext}
     * is applied to a numerically simpler intermediate {@code value * numerator}.
     * </p>
     *
     * @param value       input value expressed in the concrete unit; must not be {@code null}
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return value expressed in the base unit; never {@code null}
     */
    @Override
    public BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        return value.multiply(numerator).divide(denominator, mathContext);
    }

    /**
     * Converts a value expressed in the base unit back to the concrete unit using:
     *
     * <pre>
     * value = base * denominator / numerator
     * </pre>
     *
     * @param baseValue   input value expressed in the base unit; must not be {@code null}
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return value expressed in the concrete unit; never {@code null}
     */
    @Override
    public BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        return baseValue.multiply(denominator).divide(numerator, mathContext);
    }

}
