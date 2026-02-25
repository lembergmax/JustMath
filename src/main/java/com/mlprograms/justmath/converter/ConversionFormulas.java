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

import java.math.BigDecimal;

/**
 * Factory methods for creating common {@link ConversionFormula} implementations.
 * <p>
 * This class is a small public entry point so users can create their own formulas if needed
 * (even though the built-in units are statically defined).
 * </p>
 */
@UtilityClass
public class ConversionFormulas {

    /**
     * Creates a purely linear conversion formula:
     * <pre>
     * base = value * scale
     * </pre>
     *
     * @param scale multiplicative factor as decimal string; must not be {@code null}
     * @return conversion formula; never {@code null}
     */
    public static ConversionFormula linear(@NonNull final BigNumber scale) {
        return affine(scale, BigNumbers.ZERO);
    }

    /**
     * Creates an affine conversion formula:
     * <pre>
     * base = value * scale + offset
     * </pre>
     *
     * @param scale  multiplicative factor as decimal string; must not be {@code null}
     * @param offset additive offset as decimal string; must not be {@code null}
     * @return conversion formula; never {@code null}
     */
    public static ConversionFormula affine(@NonNull final BigNumber scale, @NonNull final BigNumber offset) {
        return new AffineConversionFormula(scale, offset);
    }

}