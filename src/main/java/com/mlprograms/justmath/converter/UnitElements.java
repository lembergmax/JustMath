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

import lombok.Getter;

import java.util.*;

/**
 * Registry for units and conversion factors (data storage only).
 * <p>
 * This class intentionally does not perform conversions yet. It only provides
 * structured storage and lookup similar to {@code ExpressionElements}.
 */
public final class UnitElements {

    public static final Map<String, UnitDefinition> registry = new HashMap<>();
    public static final Map<UnitCategory, String> categoryBaseUnitKeys = new EnumMap<>(UnitCategory.class);

    @Getter
    private static int maxTokenLength = -1;

    public static final String UNIT_METER = "m";
    public static final String UNIT_KILOMETER = "km";
    public static final String UNIT_HECTOMETER = "hm";
    public static final String UNIT_DECIMETER = "dm";
    public static final String UNIT_CENTIMETER = "cm";
    public static final String UNIT_MILLIMETER = "mm";
    public static final String UNIT_MICROMETER = "µm";
    public static final String UNIT_NANOMETER = "nm";
    public static final String UNIT_ANGSTROM = "Å";
    public static final String UNIT_PICOMETER = "pm";
    public static final String UNIT_FEMTOMETER = "fm";

    public static final String UNIT_INCH = "in";
    public static final String UNIT_FEET = "ft";
    public static final String UNIT_YARD = "yd";
    public static final String UNIT_MILE = "mi";
    public static final String UNIT_NAUTICAL_MILE = "nmi";

    public static final String UNIT_LIGHT_YEAR = "ly";
    public static final String UNIT_PARSEC = "pc";

    public static final String UNIT_PIXEL = "px";
    public static final String UNIT_POINT = "pt";
    public static final String UNIT_PICA = "pc_typ";
    public static final String UNIT_EM = "em";

    public static final String SOURCE_SI = "BIPM SI Brochure (9th edition)";
    public static final String SOURCE_NIST = "NIST Special Publication 811";
    public static final String SOURCE_IAU = "IAU 2015 Resolution B2";
    public static final String SOURCE_CSS = "W3C CSS Values and Units Level 4";

    static {
        categoryBaseUnitKeys.put(UnitCategory.LENGTH, "METER");

        List<UnitDefinition> unitDefinitions = List.of(
                new UnitDefinition("METER", UNIT_METER, List.of("meter", "metre"), UnitCategory.LENGTH, "1", SOURCE_SI),
                new UnitDefinition("KILOMETER", UNIT_KILOMETER, List.of("kilometer", "kilometre"), UnitCategory.LENGTH, "1000", SOURCE_SI),
                new UnitDefinition("HECTOMETER", UNIT_HECTOMETER, List.of("hectometer", "hectometre"), UnitCategory.LENGTH, "100", SOURCE_SI),
                new UnitDefinition("DECIMETER", UNIT_DECIMETER, List.of("decimeter", "decimetre"), UnitCategory.LENGTH, "0.1", SOURCE_SI),
                new UnitDefinition("CENTIMETER", UNIT_CENTIMETER, List.of("centimeter", "centimetre"), UnitCategory.LENGTH, "0.01", SOURCE_SI),
                new UnitDefinition("MILLIMETER", UNIT_MILLIMETER, List.of("millimeter", "millimetre"), UnitCategory.LENGTH, "0.001", SOURCE_SI),
                new UnitDefinition("MICROMETER", UNIT_MICROMETER, List.of("micrometer", "micrometre", "um"), UnitCategory.LENGTH, "0.000001", SOURCE_SI),
                new UnitDefinition("NANOMETER", UNIT_NANOMETER, List.of("nanometer", "nanometre"), UnitCategory.LENGTH, "0.000000001", SOURCE_SI),
                new UnitDefinition("ANGSTROM", UNIT_ANGSTROM, List.of("angstrom"), UnitCategory.LENGTH, "0.0000000001", SOURCE_NIST),
                new UnitDefinition("PICOMETER", UNIT_PICOMETER, List.of("picometer", "picometre"), UnitCategory.LENGTH, "0.000000000001", SOURCE_SI),
                new UnitDefinition("FEMTOMETER", UNIT_FEMTOMETER, List.of("femtometer", "femtometre"), UnitCategory.LENGTH, "0.000000000000001", SOURCE_SI),

                new UnitDefinition("INCH", UNIT_INCH, List.of("inch", "inches", "zoll"), UnitCategory.LENGTH, "0.0254", SOURCE_NIST),
                new UnitDefinition("FEET", UNIT_FEET, List.of("foot", "feet"), UnitCategory.LENGTH, "0.3048", SOURCE_NIST),
                new UnitDefinition("YARD", UNIT_YARD, List.of("yard"), UnitCategory.LENGTH, "0.9144", SOURCE_NIST),
                new UnitDefinition("MILE", UNIT_MILE, List.of("mile"), UnitCategory.LENGTH, "1609.344", SOURCE_NIST),
                new UnitDefinition("NAUTICAL_MILE", UNIT_NAUTICAL_MILE, List.of("nauticalmile", "seemeile"), UnitCategory.LENGTH, "1852", SOURCE_NIST),

                new UnitDefinition("LIGHT_YEAR", UNIT_LIGHT_YEAR, List.of("lightyear"), UnitCategory.LENGTH, "9460730472580800", SOURCE_IAU),
                new UnitDefinition("PARSEC", UNIT_PARSEC, List.of("parsec"), UnitCategory.LENGTH, "30856775814913673", SOURCE_IAU),

                new UnitDefinition("PIXEL", UNIT_PIXEL, List.of("pixel"), UnitCategory.LENGTH, "0.0002645833333333", SOURCE_CSS),
                new UnitDefinition("POINT", UNIT_POINT, List.of("point"), UnitCategory.LENGTH, "0.0003527777777778", SOURCE_CSS),
                new UnitDefinition("PICA", UNIT_PICA, List.of("pica"), UnitCategory.LENGTH, "0.0042333333333333", SOURCE_CSS),
                new UnitDefinition("EM", UNIT_EM, List.of("em"), UnitCategory.LENGTH, "0.0042333333333333", SOURCE_CSS)
        );

        for (UnitDefinition unitDefinition : unitDefinitions) {
            register(unitDefinition);
        }
    }

    private UnitElements() {
    }

    public static Optional<UnitDefinition> find(final String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(registry.get(token.toLowerCase(Locale.ROOT)));
    }

    private static void register(final UnitDefinition unitDefinition) {
        put(unitDefinition.displaySymbol(), unitDefinition);
        put(unitDefinition.key(), unitDefinition);

        for (String alias : unitDefinition.aliases()) {
            put(alias, unitDefinition);
        }
    }

    private static void put(final String token, final UnitDefinition unitDefinition) {
        String normalized = token.toLowerCase(Locale.ROOT);
        registry.put(normalized, unitDefinition);
        maxTokenLength = Math.max(maxTokenLength, normalized.length());
    }

}
