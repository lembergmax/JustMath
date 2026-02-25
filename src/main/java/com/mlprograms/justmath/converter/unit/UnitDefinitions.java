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
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Single Source of Truth für alle Standard-Einheiten.
 */
@UtilityClass
class UnitDefinitions {

    static List<Unit> defaults() {
        return List.of(
                // LENGTH (base: meter)
                linear(UnitCategory.LENGTH, "Kilometer", "km", "1000"),
                linear(UnitCategory.LENGTH, "Hectometer", "hm", "100"),
                linear(UnitCategory.LENGTH, "Meter", "m", "1"),
                linear(UnitCategory.LENGTH, "Decimeter", "dm", "0.1"),
                linear(UnitCategory.LENGTH, "Centimeter", "cm", "0.01"),
                linear(UnitCategory.LENGTH, "Millimeter", "mm", "0.001"),
                linear(UnitCategory.LENGTH, "Micrometer", "um", "0.000001"),
                linear(UnitCategory.LENGTH, "Nanometer", "nm", "0.000000001"),
                linear(UnitCategory.LENGTH, "Angstrom", "A", "0.0000000001"),
                linear(UnitCategory.LENGTH, "Picometer", "pm", "0.000000000001"),
                linear(UnitCategory.LENGTH, "Femtometer", "fm", "0.000000000000001"),
                linear(UnitCategory.LENGTH, "Inch", "in", "0.0254"),
                linear(UnitCategory.LENGTH, "Foot", "ft", "0.3048"),
                linear(UnitCategory.LENGTH, "Yard", "yd", "0.9144"),
                linear(UnitCategory.LENGTH, "Mile", "mi", "1609.344"),
                linear(UnitCategory.LENGTH, "Nautical Mile", "nmi", "1852"),
                linear(UnitCategory.LENGTH, "Light Year", "ly", "9460730472580800"),
                linear(UnitCategory.LENGTH, "Parsec", "pc", "30856775814913700"),
                linear(UnitCategory.LENGTH, "Pixel", "px", "0.0002645833333333"),
                linear(UnitCategory.LENGTH, "Point", "pt", "0.0003527777777778"),
                linear(UnitCategory.LENGTH, "Pica", "pc_typ", "0.0042333333333333"),
                linear(UnitCategory.LENGTH, "Em", "em", "0.0042333333333333"),

                // MASS (base: kilogram)
                linear(UnitCategory.MASS, "Tonne", "t", "1000"),
                linear(UnitCategory.MASS, "Kilogram", "kg", "1"),
                linear(UnitCategory.MASS, "Gram", "g", "0.001"),
                linear(UnitCategory.MASS, "Milligram", "mg", "0.000001"),
                linear(UnitCategory.MASS, "Pound", "lb", "0.45359237"),

                // TEMPERATURE (base: kelvin)
                linear(UnitCategory.TEMPERATURE, "Kelvin", "K", "1"),
                affine(UnitCategory.TEMPERATURE, "Celsius", "°C", "1", "273.15"),
                affine(UnitCategory.TEMPERATURE, "Fahrenheit", "°F", "0.5555555555555556", "255.3722222222222222")
        );
    }

    private static Unit linear(final UnitCategory category,
                               final String displayName,
                               final String symbol,
                               final String factorToBase) {
        return new Unit(
                category,
                displayName,
                symbol,
                new BigNumber(factorToBase),
                ConversionFormulas.linear(factorToBase)
        );
    }

    private static Unit affine(final UnitCategory category,
                               final String displayName,
                               final String symbol,
                               final String scale,
                               final String offset) {
        return new Unit(
                category,
                displayName,
                symbol,
                new BigNumber(scale),
                ConversionFormulas.affine(scale, offset)
        );
    }

}
