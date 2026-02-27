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
import lombok.experimental.UtilityClass;

/**
 * Factory methods for creating common {@link ConversionFormula} implementations.
 *
 * <p>
 * This class is intentionally small and stable: it provides reusable conversion strategies
 * that can be referenced by the internal unit catalog as well as by library consumers who
 * want to define their own unit registries or custom units outside the built-in catalog.
 * </p>
 *
 * <p>
 * The returned {@link ConversionFormula} implementations are immutable and thread-safe.
 * </p>
 */
@UtilityClass
public class ConversionFormulas {

    /**
     * Creates a purely linear conversion formula of the form:
     *
     * <pre>
     * base = value * scale
     * </pre>
     *
     * <p>
     * This is equivalent to an affine conversion with an offset of {@code 0}.
     * </p>
     *
     * @param scale multiplicative factor into the base unit; must not be {@code null} and must not be zero
     * @return a conversion formula implementing the linear mapping; never {@code null}
     * @throws IllegalArgumentException if {@code scale} is zero
     */
    public static ConversionFormula linear(@NonNull final BigNumber scale) {
        return affine(scale, BigNumbers.ZERO);
    }

    /**
     * Creates an affine conversion formula of the form:
     *
     * <pre>
     * base = value * scale + offset
     * </pre>
     *
     * <p>
     * The inverse conversion is:
     * </p>
     *
     * <pre>
     * value = (base - offset) / scale
     * </pre>
     *
     * @param scale  multiplicative factor into the base unit; must not be {@code null} and must not be zero
     * @param offset additive offset into the base unit; must not be {@code null}
     * @return a conversion formula implementing the affine mapping; never {@code null}
     * @throws IllegalArgumentException if {@code scale} is zero
     */
    public static ConversionFormula affine(@NonNull final BigNumber scale, @NonNull final BigNumber offset) {
        return new AffineConversionFormula(scale, offset);
    }

}