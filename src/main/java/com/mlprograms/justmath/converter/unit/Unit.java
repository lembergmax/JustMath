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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Unit {

    @NonNull
    private final UnitCategory category;
    @NonNull
    private final String displayName;
    @NonNull
    private final String symbol;
    @NonNull
    private final BigNumber factorToBase;

    private static Unit create(final UnitCategory category,
                               final String displayName,
                               final String symbol,
                               final String factorToBase) {
        return new Unit(category, displayName, symbol, new BigNumber(factorToBase));
    }

    @UtilityClass
    public static class LENGTH {

        public static final Unit KILOMETER = create(UnitCategory.LENGTH, "Kilometer", "km", "1000");
        public static final Unit HECTOMETER = create(UnitCategory.LENGTH, "Hectometer", "hm", "100");
        public static final Unit METER = create(UnitCategory.LENGTH, "Meter", "m", "1");
        public static final Unit DECIMETER = create(UnitCategory.LENGTH, "Decimeter", "dm", "0.1");
        public static final Unit CENTIMETER = create(UnitCategory.LENGTH, "Centimeter", "cm", "0.01");
        public static final Unit MILLIMETER = create(UnitCategory.LENGTH, "Millimeter", "mm", "0.001");
        public static final Unit MICROMETER = create(UnitCategory.LENGTH, "Micrometer", "um", "0.000001");
        public static final Unit NANOMETER = create(UnitCategory.LENGTH, "Nanometer", "nm", "0.000000001");
        public static final Unit ANGSTROM = create(UnitCategory.LENGTH, "Angstrom", "A", "0.0000000001");
        public static final Unit PICOMETER = create(UnitCategory.LENGTH, "Picometer", "pm", "0.000000000001");
        public static final Unit FEMTOMETER = create(UnitCategory.LENGTH, "Femtometer", "fm", "0.000000000000001");
        public static final Unit INCH = create(UnitCategory.LENGTH, "Inch", "in", "0.0254");
        public static final Unit FEET = create(UnitCategory.LENGTH, "Foot", "ft", "0.3048");
        public static final Unit YARD = create(UnitCategory.LENGTH, "Yard", "yd", "0.9144");
        public static final Unit MILE = create(UnitCategory.LENGTH, "Mile", "mi", "1609.344");
        public static final Unit NAUTICAL_MILE = create(UnitCategory.LENGTH, "Nautical Mile", "nmi", "1852");
        public static final Unit LIGHT_YEAR = create(UnitCategory.LENGTH, "Light Year", "ly", "9460730472580800");
        public static final Unit PARSEC = create(UnitCategory.LENGTH, "Parsec", "pc", "30856775814913673");
        public static final Unit PIXEL = create(UnitCategory.LENGTH, "Pixel", "px", "0.0002645833333333");
        public static final Unit POINT = create(UnitCategory.LENGTH, "Point", "pt", "0.0003527777777778");
        public static final Unit PICA = create(UnitCategory.LENGTH, "Pica", "pc_typ", "0.0042333333333333");
        public static final Unit EM = create(UnitCategory.LENGTH, "Em", "em", "0.0042333333333333");

        public static List<Unit> all() {
            return List.of(
                    KILOMETER,
                    HECTOMETER,
                    METER,
                    DECIMETER,
                    CENTIMETER,
                    MILLIMETER,
                    MICROMETER,
                    NANOMETER,
                    ANGSTROM,
                    PICOMETER,
                    FEMTOMETER,
                    INCH,
                    FEET,
                    YARD,
                    MILE,
                    NAUTICAL_MILE,
                    LIGHT_YEAR,
                    PARSEC,
                    PIXEL,
                    POINT,
                    PICA,
                    EM
            );
        }
    }

}
