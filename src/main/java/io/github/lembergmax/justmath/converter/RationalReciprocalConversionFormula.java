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
 * Immutable reciprocal conversion formula whose scale is expressed as an exact rational pair:
 *
 * <pre>
 * base  = (numerator / denominator) / value  = numerator / (value * denominator)
 * value = (numerator / denominator) / base   = numerator / (base  * denominator)
 * </pre>
 *
 * <p>
 * This formula is the reciprocal analogue of {@link RationalConversionFormula}.
 * It is required when the reciprocal scale itself has no finite decimal representation
 * (e.g. {@code mile / US_gallon = 1609.344 / 3.785411784}, which has factor {@code 3} in
 * the denominator and therefore cannot be stored exactly as a pre-rounded decimal). Storing
 * the factor as an exact ratio avoids accumulated rounding errors: the multiplications are
 * carried out first, the single division is deferred to conversion time and uses the
 * caller-supplied {@link MathContext}.
 * </p>
 *
 * <p>
 * Package-private on purpose; instances are obtained via
 * {@link ConversionFormulas#reciprocal(BigNumber, BigNumber)}.
 * </p>
 *
 * <p>
 * Immutable and thread-safe.
 * </p>
 */
final class RationalReciprocalConversionFormula implements ConversionFormula {

    /**
     * Numerator of the reciprocal scale; must not be zero.
     */
    private final BigNumber numerator;

    /**
     * Denominator of the reciprocal scale; must not be zero.
     */
    private final BigNumber denominator;

    /**
     * Creates a new exact rational reciprocal conversion formula.
     *
     * @param numerator   numerator of the reciprocal scale; must not be {@code null} and must not be zero
     * @param denominator denominator of the reciprocal scale; must not be {@code null} and must not be zero
     * @throws IllegalArgumentException if {@code numerator} or {@code denominator} is zero
     */
    RationalReciprocalConversionFormula(@NonNull final BigNumber numerator, @NonNull final BigNumber denominator) {
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
     * base = numerator / (value * denominator)
     * </pre>
     *
     * @param value       input value expressed in the concrete unit; must not be {@code null} and must not be zero
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return value expressed in the base unit; never {@code null}
     * @throws ArithmeticException if {@code value} is zero
     */
    @Override
    public BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        return numerator.divide(value.multiply(denominator), mathContext);
    }

    /**
     * Converts a value expressed in the base unit back to the concrete unit using:
     *
     * <pre>
     * value = numerator / (base * denominator)
     * </pre>
     *
     * @param baseValue   input value expressed in the base unit; must not be {@code null} and must not be zero
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return value expressed in the concrete unit; never {@code null}
     * @throws ArithmeticException if {@code baseValue} is zero
     */
    @Override
    public BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        return numerator.divide(baseValue.multiply(denominator), mathContext);
    }

}
