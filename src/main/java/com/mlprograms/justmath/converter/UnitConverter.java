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
import com.mlprograms.justmath.calculator.CalculatorEngineUtils;
import com.mlprograms.justmath.converter.unit.Unit;
import com.mlprograms.justmath.converter.unit.UnitElements;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.math.MathContext;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;

@AllArgsConstructor
public class UnitConverter {

    @Getter
    @NonNull
    private final MathContext mathContext;

    public UnitConverter() {
        this(DEFAULT_DIVISION_PRECISION);
    }

    public UnitConverter(final int divisionPrecision) {
        this(CalculatorEngineUtils.getDefaultMathContext(divisionPrecision));
    }

    public BigNumber convert(@NonNull final BigNumber value, @NonNull final Unit fromUnit, @NonNull final Unit toUnit) {
        final BigNumber valueInBaseUnit = UnitElements.toBase(fromUnit, value, mathContext);
        return UnitElements.fromBase(toUnit, valueInBaseUnit, mathContext);
    }

}
