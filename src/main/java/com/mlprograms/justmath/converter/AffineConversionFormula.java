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
import lombok.NonNull;

/**
 * Immutable affine conversion formula of the form:
 *
 * <pre>
 * base = value * scale + offset
 * </pre>
 *
 * <p>
 * and its inverse:
 * </p>
 *
 * <pre>
 * value = (base - offset) / scale
 * </pre>
 *
 * <p>
 * This formula can represent both:
 * </p>
 * <ul>
 *   <li>pure linear scaling (offset = 0)</li>
 *   <li>affine conversions (e.g., temperature conversions, if you later add temperature units)</li>
 * </ul>
 *
 * <p>
 * The type is package-private on purpose to keep the public API minimal and stable.
 * Library users are expected to obtain instances via {@link ConversionFormulas}.
 * </p>
 *
 * <p>
 * This implementation is immutable and thread-safe.
 * </p>
 */
final class AffineConversionFormula implements ConversionFormula {

    /**
     * Multiplicative factor used to scale values into the base unit.
     *
     * <p>
     * Must not be zero, otherwise the inverse conversion would be undefined.
     * </p>
     */
    private final BigNumber scale;

    /**
     * Additive offset applied after scaling to shift values into the base unit.
     */
    private final BigNumber offset;

    /**
     * Creates a new affine conversion formula.
     *
     * @param scale  multiplicative factor into base units; must not be {@code null} and must not be zero
     * @param offset additive offset into base units; must not be {@code null}
     * @throws IllegalArgumentException if {@code scale} is zero
     */
    AffineConversionFormula(@NonNull final BigNumber scale, @NonNull final BigNumber offset) {
        if (scale.compareTo(BigNumbers.ZERO) == 0) {
            throw new IllegalArgumentException("scale must not be zero");
        }
        this.scale = scale;
        this.offset = offset;
    }

    /**
     * Converts a value expressed in a concrete unit to the base unit using:
     *
     * <pre>
     * base = value * scale + offset
     * </pre>
     *
     * @param value       input value expressed in the concrete unit; must not be {@code null}
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return value expressed in the base unit; never {@code null}
     */
    @Override
    public BigNumber toBase(@NonNull final BigNumber value, @NonNull final java.math.MathContext mathContext) {
        return value.multiply(scale).add(offset);
    }

    /**
     * Converts a value expressed in the base unit back to the concrete unit using:
     *
     * <pre>
     * value = (base - offset) / scale
     * </pre>
     *
     * @param baseValue   input value expressed in the base unit; must not be {@code null}
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return value expressed in the concrete unit; never {@code null}
     */
    @Override
    public BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final java.math.MathContext mathContext) {
        return baseValue.subtract(offset).divide(scale, mathContext);
    }

}