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
import com.mlprograms.justmath.converter.units.UnitType;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;

/**
 * Provides precision-safe conversion between registered unit definitions.
 */
@UtilityClass
public class UnitConverter {

    public static BigNumber convert(final BigNumber amount, final UnitType fromUnit, final UnitType toUnit) {
        Objects.requireNonNull(amount, "Amount must not be null.");
        Objects.requireNonNull(fromUnit, "From unit must not be null.");
        Objects.requireNonNull(toUnit, "To unit must not be null.");

        UnitDefinition fromDefinition = resolveUnit(fromUnit);
        UnitDefinition toDefinition = resolveUnit(toUnit);

        if (fromDefinition.getType().category() != toDefinition.getType().category()) {
            throw new IllegalArgumentException("Cannot convert between different categories: "
                    + fromDefinition.getType().category() + " -> " + toDefinition.getType().category());
        }

        BigNumber fromFactor = new BigNumber(fromDefinition.getFactorToBase(), amount.getLocale(), amount.getMathContext(), amount.getTrigonometricMode());
        BigNumber toFactor = new BigNumber(toDefinition.getFactorToBase(), amount.getLocale(), amount.getMathContext(), amount.getTrigonometricMode());

        return amount.multiply(fromFactor).divide(toFactor, amount.getMathContext(), amount.getLocale());
    }

    private static UnitDefinition resolveUnit(final UnitType unitType) {
        List<UnitDefinition> categoryDefinitions = switch (unitType.category()) {
            case LENGTH -> Unit.lengthDefinitions();
        };

        return categoryDefinitions.stream()
                .filter(definition -> definition.getType() == unitType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown unit enum: " + unitType));
    }

}
