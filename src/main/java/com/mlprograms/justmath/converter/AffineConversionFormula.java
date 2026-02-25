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
 * Affine conversion formula of the form:
 * <pre>
 * base = value * scale + offset
 * </pre>
 * and its inverse:
 * <pre>
 * value = (base - offset) / scale
 * </pre>
 *
 * <p>
 * This formula covers both:
 * </p>
 * <ul>
 *   <li>pure linear scaling (offset = 0)</li>
 *   <li>affine conversions such as temperature conversions</li>
 * </ul>
 *
 * <p>
 * This type is package-private by design to keep the public API surface minimal and stable.
 * </p>
 */
final class AffineConversionFormula implements ConversionFormula {

    /**
     * Multiplicative factor used to scale values into base units.
     */
    private final BigNumber scale;

    /**
     * Additive offset used to shift values into base units.
     */
    private final BigNumber offset;

    /**
     * Creates a new affine conversion formula.
     *
     * @param scale  multiplicative factor; must not be {@code null} and must not be zero
     * @param offset additive offset; must not be {@code null}
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
     * Converts a concrete unit value to base units using:
     * <pre>
     * base = value * scale + offset
     * </pre>
     *
     * @param value       concrete unit value; must not be {@code null}
     * @return base unit value; never {@code null}
     */
    @Override
    public BigNumber toBase(@NonNull final BigNumber value) {
        return value.multiply(scale).add(offset);
    }

    /**
     * Converts a base unit value to the concrete unit using:
     * <pre>
     * value = (base - offset) / scale
     * </pre>
     *
     * @param baseValue   base unit value; must not be {@code null}
     * @return concrete unit value; never {@code null}
     */
    @Override
    public BigNumber fromBase(@NonNull final BigNumber baseValue) {
        return baseValue.subtract(offset).divide(scale);
    }

}