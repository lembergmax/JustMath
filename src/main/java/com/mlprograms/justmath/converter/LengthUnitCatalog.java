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

/**
 * Dedicated data catalog for all length unit definitions.
 */
@UtilityClass
public class LengthUnitCatalog {

    public static List<UnitDefinition> definitions() {
        return List.of(
                UnitDefinition.builder().type(Length.KILOMETER).displayName("Kilometer").symbol("km").factorToBase("1000").alias("kilometer").alias("kilometre").build(),
                UnitDefinition.builder().type(Length.HECTOMETER).displayName("Hektometer").symbol("hm").factorToBase("100").alias("hectometer").alias("hectometre").build(),
                UnitDefinition.builder().type(Length.METER).displayName("Meter").symbol("m").factorToBase("1").alias("meter").alias("metre").build(),
                UnitDefinition.builder().type(Length.DECIMETER).displayName("Dezimeter").symbol("dm").factorToBase("0.1").alias("decimeter").alias("decimetre").build(),
                UnitDefinition.builder().type(Length.CENTIMETER).displayName("Zentimeter").symbol("cm").factorToBase("0.01").alias("centimeter").alias("centimetre").build(),
                UnitDefinition.builder().type(Length.MILLIMETER).displayName("Millimeter").symbol("mm").factorToBase("0.001").alias("millimeter").alias("millimetre").build(),
                UnitDefinition.builder().type(Length.MICROMETER).displayName("Mikrometer").symbol("µm").factorToBase("0.000001").alias("micrometer").alias("micrometre").alias("um").build(),
                UnitDefinition.builder().type(Length.NANOMETER).displayName("Nanometer").symbol("nm").factorToBase("0.000000001").alias("nanometer").alias("nanometre").build(),
                UnitDefinition.builder().type(Length.ANGSTROM).displayName("Ångström").symbol("Å").factorToBase("0.0000000001").alias("angstrom").build(),
                UnitDefinition.builder().type(Length.PICOMETER).displayName("Pikometer").symbol("pm").factorToBase("0.000000000001").alias("picometer").alias("picometre").build(),
                UnitDefinition.builder().type(Length.FEMTOMETER).displayName("Femtometer").symbol("fm").factorToBase("0.000000000000001").alias("femtometer").alias("femtometre").build(),
                UnitDefinition.builder().type(Length.INCH).displayName("Inch").symbol("in").factorToBase("0.0254").alias("inch").alias("inches").alias("zoll").build(),
                UnitDefinition.builder().type(Length.FEET).displayName("Foot").symbol("ft").factorToBase("0.3048").alias("foot").alias("feet").build(),
                UnitDefinition.builder().type(Length.YARD).displayName("Yard").symbol("yd").factorToBase("0.9144").alias("yard").build(),
                UnitDefinition.builder().type(Length.MILE).displayName("Mile").symbol("mi").factorToBase("1609.344").alias("mile").build(),
                UnitDefinition.builder().type(Length.NAUTICAL_MILE).displayName("Nautical Mile").symbol("nmi").factorToBase("1852").alias("nauticalmile").alias("seemeile").build(),
                UnitDefinition.builder().type(Length.LIGHT_YEAR).displayName("Lichtjahr").symbol("ly").factorToBase("9460730472580800").alias("lightyear").build(),
                UnitDefinition.builder().type(Length.PARSEC).displayName("Parsec").symbol("pc").factorToBase("30856775814913673").alias("parsec").build(),
                UnitDefinition.builder().type(Length.PIXEL).displayName("Pixel").symbol("px").factorToBase("0.0002645833333333").alias("pixel").build(),
                UnitDefinition.builder().type(Length.POINT).displayName("Point").symbol("pt").factorToBase("0.0003527777777778").alias("point").build(),
                UnitDefinition.builder().type(Length.PICA).displayName("Pica").symbol("pc_typ").factorToBase("0.0042333333333333").alias("pica").build(),
                UnitDefinition.builder().type(Length.EM).displayName("Em").symbol("em").factorToBase("0.0042333333333333").alias("em").build()
        );
    }

}
