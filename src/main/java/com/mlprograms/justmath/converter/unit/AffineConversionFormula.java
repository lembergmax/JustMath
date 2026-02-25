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

package com.mlprograms.justmath.converter.unit;

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Allgemeine affine Umrechnung: base = value * scale + offset.
 */
@RequiredArgsConstructor
final class AffineConversionFormula implements ConversionFormula {

    @NonNull
    private final BigDecimal scale;
    @NonNull
    private final BigDecimal offset;

    @Override
    public BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        final BigDecimal result = value.toBigDecimal().multiply(scale, mathContext).add(offset, mathContext);
        return new BigNumber(result.toPlainString(), value.getLocale(), mathContext);
    }

    @Override
    public BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        final BigDecimal numerator = baseValue.toBigDecimal().subtract(offset, mathContext);
        final BigDecimal result = numerator.divide(scale, mathContext);
        return new BigNumber(result.toPlainString(), baseValue.getLocale(), mathContext);
    }

}
