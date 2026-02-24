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

import java.util.List;

/**
 * Unit model with enum-based categories and units.
 * <p>
 * The intended usage is {@code Unit.Type.Length.METER}.
 */
public final class Unit {

    private Unit() {
    }

    public interface UnitType {

        String displayName();

        String symbol();

        List<String> aliases();

        String factorToBase();

    }

    public static final class Type {

        private Type() {
        }

        public enum Length implements UnitType {

            KILOMETER("Kilometer", "km", List.of("kilometer", "kilometre"), "1000"),
            HECTOMETER("Hektometer", "hm", List.of("hectometer", "hectometre"), "100"),
            METER("Meter", "m", List.of("meter", "metre"), "1"),
            DECIMETER("Dezimeter", "dm", List.of("decimeter", "decimetre"), "0.1"),
            CENTIMETER("Zentimeter", "cm", List.of("centimeter", "centimetre"), "0.01"),
            MILLIMETER("Millimeter", "mm", List.of("millimeter", "millimetre"), "0.001"),
            MICROMETER("Mikrometer", "µm", List.of("micrometer", "micrometre", "um"), "0.000001"),
            NANOMETER("Nanometer", "nm", List.of("nanometer", "nanometre"), "0.000000001"),
            ANGSTROM("Ångström", "Å", List.of("angstrom"), "0.0000000001"),
            PICOMETER("Pikometer", "pm", List.of("picometer", "picometre"), "0.000000000001"),
            FEMTOMETER("Femtometer", "fm", List.of("femtometer", "femtometre"), "0.000000000000001"),
            INCH("Inch", "in", List.of("inch", "inches", "zoll"), "0.0254"),
            FEET("Foot", "ft", List.of("foot", "feet"), "0.3048"),
            YARD("Yard", "yd", List.of("yard"), "0.9144"),
            MILE("Mile", "mi", List.of("mile"), "1609.344"),
            NAUTICAL_MILE("Nautical Mile", "nmi", List.of("nauticalmile", "seemeile"), "1852"),
            LIGHT_YEAR("Lichtjahr", "ly", List.of("lightyear"), "9460730472580800"),
            PARSEC("Parsec", "pc", List.of("parsec"), "30856775814913673"),
            PIXEL("Pixel", "px", List.of("pixel"), "0.0002645833333333"),
            POINT("Point", "pt", List.of("point"), "0.0003527777777778"),
            PICA("Pica", "pc_typ", List.of("pica"), "0.0042333333333333"),
            EM("Em", "em", List.of("em"), "0.0042333333333333");

            private final String displayName;
            private final String symbol;
            private final List<String> aliases;
            private final String factorToBase;

            Length(final String displayName, final String symbol, final List<String> aliases, final String factorToBase) {
                this.displayName = displayName;
                this.symbol = symbol;
                this.aliases = aliases;
                this.factorToBase = factorToBase;
            }

            @Override
            public String displayName() {
                return displayName;
            }

            @Override
            public String symbol() {
                return symbol;
            }

            @Override
            public List<String> aliases() {
                return aliases;
            }

            @Override
            public String factorToBase() {
                return factorToBase;
            }
        }
    }
}
