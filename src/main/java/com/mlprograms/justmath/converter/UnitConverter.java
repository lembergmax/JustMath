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
import com.mlprograms.justmath.calculator.Evaluator;
import com.mlprograms.justmath.calculator.PostfixParser;
import com.mlprograms.justmath.calculator.Tokenizer;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.converter.unit.Unit;
import com.mlprograms.justmath.converter.unit.UnitDefinition;
import com.mlprograms.justmath.converter.unit.UnitType;
import lombok.NonNull;

import java.math.MathContext;
import java.util.List;
import java.util.Objects;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;
import static com.mlprograms.justmath.calculator.CalculatorEngine.getDefaultMathContext;

/**
 * Core conversion logic for precision-safe unit conversions.
 */
public class UnitConverter {

    /**
     * Math context specifying the precision and rounding mode for calculations.
     */
    private MathContext mathContext;

    /**
     * Constructs a CalculatorEngine with default division precision and default trigonometric mode (DEG).
     */
    public UnitConverter() {
        this(DEFAULT_DIVISION_PRECISION);
    }

    /**
     * Constructs a CalculatorEngine with the specified division precision.
     *
     * @param divisionPrecision the precision for division operations
     */
    public UnitConverter(int divisionPrecision) {
        this(getDefaultMathContext(divisionPrecision));
    }

    /**
     * Constructs a UnitConverter with the specified MathContext.
     *
     * @param mathContext       the MathContext specifying precision and rounding mode
     */
    public UnitConverter(@NonNull final MathContext mathContext) {
        this.mathContext = mathContext;
    }

    public BigNumber convert(@NonNull final BigNumber amount, @NonNull final UnitType fromUnit, @NonNull final UnitType toUnit) {
        UnitDefinition fromDefinition = resolveUnit(fromUnit);
        UnitDefinition toDefinition = resolveUnit(toUnit);

        if (fromDefinition.getType().category() != toDefinition.getType().category()) {
            throw new IllegalArgumentException("Cannot convert between different categories: "
                    + fromDefinition.getType().category() + " -> " + toDefinition.getType().category());
        }

        BigNumber fromFactor = new BigNumber(fromDefinition.getFactorToBase(), amount.getLocale(), amount.getMathContext(), amount.getTrigonometricMode());
        BigNumber toFactor = new BigNumber(toDefinition.getFactorToBase(), amount.getLocale(), amount.getMathContext(), amount.getTrigonometricMode());

        return amount.multiply(fromFactor).divide(toFactor);
    }

    private UnitDefinition resolveUnit(final UnitType unitType) {
        return Unit
    }

}
