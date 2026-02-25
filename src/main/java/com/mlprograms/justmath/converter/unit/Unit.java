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

import java.math.MathContext;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Unit {

    @NonNull
    private final UnitCategory category;
    @NonNull
    private final String displayName;
    @NonNull
    private final String symbol;
    /**
     * Rückwärtskompatibilität für bestehende API (lineare Einheiten).
     */
    @NonNull
    private final BigNumber factorToBase;
    @NonNull
    private final ConversionFormula conversionFormula;

    public BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        return conversionFormula.toBase(value, mathContext);
    }

    public BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        return conversionFormula.fromBase(baseValue, mathContext);
    }

    @UtilityClass
    public static class LENGTH {

        public static final Unit KILOMETER = UnitElements.requireBySymbol("km");
        public static final Unit HECTOMETER = UnitElements.requireBySymbol("hm");
        public static final Unit METER = UnitElements.requireBySymbol("m");
        public static final Unit DECIMETER = UnitElements.requireBySymbol("dm");
        public static final Unit CENTIMETER = UnitElements.requireBySymbol("cm");
        public static final Unit MILLIMETER = UnitElements.requireBySymbol("mm");
        public static final Unit MICROMETER = UnitElements.requireBySymbol("um");
        public static final Unit NANOMETER = UnitElements.requireBySymbol("nm");
        public static final Unit ANGSTROM = UnitElements.requireBySymbol("A");
        public static final Unit PICOMETER = UnitElements.requireBySymbol("pm");
        public static final Unit FEMTOMETER = UnitElements.requireBySymbol("fm");
        public static final Unit INCH = UnitElements.requireBySymbol("in");
        public static final Unit FEET = UnitElements.requireBySymbol("ft");
        public static final Unit YARD = UnitElements.requireBySymbol("yd");
        public static final Unit MILE = UnitElements.requireBySymbol("mi");
        public static final Unit NAUTICAL_MILE = UnitElements.requireBySymbol("nmi");
        public static final Unit LIGHT_YEAR = UnitElements.requireBySymbol("ly");
        public static final Unit PARSEC = UnitElements.requireBySymbol("pc");
        public static final Unit PIXEL = UnitElements.requireBySymbol("px");
        public static final Unit POINT = UnitElements.requireBySymbol("pt");
        public static final Unit PICA = UnitElements.requireBySymbol("pc_typ");
        public static final Unit EM = UnitElements.requireBySymbol("em");

        public static List<Unit> all() {
            return UnitElements.unitsByCategory(UnitCategory.LENGTH);
        }
    }

    @UtilityClass
    public static class MASS {

        public static final Unit KILOGRAM = UnitElements.requireBySymbol("kg");
        public static final Unit GRAM = UnitElements.requireBySymbol("g");
        public static final Unit MILLIGRAM = UnitElements.requireBySymbol("mg");
        public static final Unit TONNE = UnitElements.requireBySymbol("t");
        public static final Unit POUND = UnitElements.requireBySymbol("lb");

        public static List<Unit> all() {
            return UnitElements.unitsByCategory(UnitCategory.MASS);
        }
    }

    @UtilityClass
    public static class TEMPERATURE {

        public static final Unit KELVIN = UnitElements.requireBySymbol("K");
        public static final Unit CELSIUS = UnitElements.requireBySymbol("°C");
        public static final Unit FAHRENHEIT = UnitElements.requireBySymbol("°F");

        public static List<Unit> all() {
            return UnitElements.unitsByCategory(UnitCategory.TEMPERATURE);
        }
    }

}
