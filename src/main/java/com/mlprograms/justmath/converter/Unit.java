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

/**
 * Technical unit identifiers used by the converter module.
 * <p>
 * This enum intentionally contains <strong>no metadata</strong> and <strong>no conversion values</strong>.
 * All unit details (display name, symbol, category, conversion behavior) are defined externally
 * and retrieved via {@link UnitElements}.
 * </p>
 */
public enum Unit {

    /**
     * Kilometer.
     */
    KILOMETER,
    /**
     * Hectometer.
     */
    HECTOMETER,
    /**
     * Meter (base unit of {@link UnitCategory#LENGTH}).
     */
    METER,
    /**
     * Decimeter.
     */
    DECIMETER,
    /**
     * Centimeter.
     */
    CENTIMETER,
    /**
     * Millimeter.
     */
    MILLIMETER,
    /**
     * Micrometer (ASCII symbol "um").
     */
    MICROMETER,
    /**
     * Nanometer.
     */
    NANOMETER,
    /**
     * Ångström (ASCII symbol "A").
     */
    ANGSTROM,
    /**
     * Picometer.
     */
    PICOMETER,
    /**
     * Femtometer.
     */
    FEMTOMETER,
    /**
     * Inch.
     */
    INCH,
    /**
     * Foot.
     */
    FEET,
    /**
     * Yard.
     */
    YARD,
    /**
     * Mile.
     */
    MILE,
    /**
     * Nautical mile.
     */
    NAUTICAL_MILE,
    /**
     * Light year.
     */
    LIGHT_YEAR,
    /**
     * Parsec.
     */
    PARSEC,
    /**
     * CSS pixel.
     */
    PIXEL,
    /**
     * Typographic point.
     */
    POINT,
    /**
     * Typographic pica.
     */
    PICA,
    /**
     * EM unit (mapped to a fixed physical length in the built-in catalog).
     */
    EM,

}