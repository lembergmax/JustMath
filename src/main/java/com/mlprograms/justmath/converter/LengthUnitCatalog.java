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

import com.mlprograms.justmath.converter.units.Length;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

/**
 * Dedicated catalog for all length unit metadata.
 */
@UtilityClass
public class LengthUnitCatalog {

    private static final Map<Length, UnitDefinition> DEFINITIONS_BY_TYPE = Map.ofEntries(
            Map.entry(Length.KILOMETER, definition(Length.KILOMETER, "Kilometer", "km", "1000")),
            Map.entry(Length.HECTOMETER, definition(Length.HECTOMETER, "Hectometer", "hm", "100")),
            Map.entry(Length.METER, definition(Length.METER, "Meter", "m", "1")),
            Map.entry(Length.DECIMETER, definition(Length.DECIMETER, "Decimeter", "dm", "0.1")),
            Map.entry(Length.CENTIMETER, definition(Length.CENTIMETER, "Centimeter", "cm", "0.01")),
            Map.entry(Length.MILLIMETER, definition(Length.MILLIMETER, "Millimeter", "mm", "0.001")),
            Map.entry(Length.MICROMETER, definition(Length.MICROMETER, "Micrometer", "µm", "0.000001")),
            Map.entry(Length.NANOMETER, definition(Length.NANOMETER, "Nanometer", "nm", "0.000000001")),
            Map.entry(Length.ANGSTROM, definition(Length.ANGSTROM, "Angstrom", "Å", "0.0000000001")),
            Map.entry(Length.PICOMETER, definition(Length.PICOMETER, "Picometer", "pm", "0.000000000001")),
            Map.entry(Length.FEMTOMETER, definition(Length.FEMTOMETER, "Femtometer", "fm", "0.000000000000001")),
            Map.entry(Length.INCH, definition(Length.INCH, "Inch", "in", "0.0254")),
            Map.entry(Length.FEET, definition(Length.FEET, "Foot", "ft", "0.3048")),
            Map.entry(Length.YARD, definition(Length.YARD, "Yard", "yd", "0.9144")),
            Map.entry(Length.MILE, definition(Length.MILE, "Mile", "mi", "1609.344")),
            Map.entry(Length.NAUTICAL_MILE, definition(Length.NAUTICAL_MILE, "Nautical Mile", "nmi", "1852")),
            Map.entry(Length.LIGHT_YEAR, definition(Length.LIGHT_YEAR, "Light Year", "ly", "9460730472580800")),
            Map.entry(Length.PARSEC, definition(Length.PARSEC, "Parsec", "pc", "30856775814913673")),
            Map.entry(Length.PIXEL, definition(Length.PIXEL, "Pixel", "px", "0.0002645833333333")),
            Map.entry(Length.POINT, definition(Length.POINT, "Point", "pt", "0.0003527777777778")),
            Map.entry(Length.PICA, definition(Length.PICA, "Pica", "pc_typ", "0.0042333333333333")),
            Map.entry(Length.EM, definition(Length.EM, "Em", "em", "0.0042333333333333"))
    );

    public static List<UnitDefinition> definitions() {
        return List.of(Length.values()).stream()
                .map(type -> DEFINITIONS_BY_TYPE.get(type))
                .toList();
    }

    private static UnitDefinition definition(final Length type, final String displayName, final String symbol, final String factorToBase) {
        return UnitDefinition.builder()
                .type(type)
                .displayName(displayName)
                .symbol(symbol)
                .factorToBase(factorToBase)
                .build();
    }

}
