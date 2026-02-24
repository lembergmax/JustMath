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
import com.mlprograms.justmath.converter.unit.Unit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.MathContext;

@RequiredArgsConstructor
public class UnitConverter {

    @NonNull
    private final MathContext mathContext;

    public BigNumber convert(@NonNull final BigNumber value, @NonNull final Unit fromUnit, @NonNull final Unit toUnit) {
        if (fromUnit.getCategory() != toUnit.getCategory()) {
            throw new IllegalArgumentException("Units must share the same category");
        }

        return value.multiply(fromUnit.getFactorToBase()).divide(toUnit.getFactorToBase(), mathContext);
    }

}
