/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 */

package com.mlprograms.justmath.converter.units;

import com.mlprograms.justmath.converter.UnitCategory;

public enum Length implements UnitType {

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
    EM;

    @Override
    public String key() {
        return name();
    }

    @Override
    public UnitCategory category() {
        return UnitCategory.LENGTH;
    }

}
