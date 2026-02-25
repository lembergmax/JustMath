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
 * Marker type for all units supported by the converter module.
 *
 * <p>
 * The public API exposes units as strongly typed identifiers (not data holders). All metadata such as
 * symbols, display names, categories, and conversion behavior is stored externally and accessed via
 * {@link UnitElements}.
 * </p>
 *
 * <p>
 * Units are grouped by domain category using nested enums, enabling readable and type-safe references like
 * {@code Unit.Length.METER} or {@code Unit.Mass.KILOGRAM}.
 * </p>
 */
public sealed interface Unit permits Unit.Length, Unit.Mass {

    /**
     * Length unit identifiers (base unit: meter).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Length implements Unit {
        /**
         * Kilometer (km).
         */
        KILOMETER,
        /**
         * Hectometer (hm).
         */
        HECTOMETER,
        /**
         * Meter (m), base unit of the LENGTH category.
         */
        METER,
        /**
         * Decimeter (dm).
         */
        DECIMETER,
        /**
         * Centimeter (cm).
         */
        CENTIMETER,
        /**
         * Millimeter (mm).
         */
        MILLIMETER,
        /**
         * Micrometer (um).
         */
        MICROMETER,
        /**
         * Nanometer (nm).
         */
        NANOMETER,
        /**
         * Ångström (A).
         */
        ANGSTROM,
        /**
         * Picometer (pm).
         */
        PICOMETER,
        /**
         * Femtometer (fm).
         */
        FEMTOMETER,
        /**
         * Inch (in).
         */
        INCH,
        /**
         * Foot (ft).
         */
        FEET,
        /**
         * Yard (yd).
         */
        YARD,
        /**
         * Mile (mi).
         */
        MILE,
        /**
         * Nautical mile (nmi).
         */
        NAUTICAL_MILE,
        /**
         * Light year (ly).
         */
        LIGHT_YEAR,
        /**
         * Parsec (pc).
         */
        PARSEC,
        /**
         * CSS pixel (px).
         */
        PIXEL,
        /**
         * Typographic point (pt).
         */
        POINT,
        /**
         * Typographic pica (pica).
         */
        PICA,
        /**
         * Em (em).
         */
        EM
    }

    /**
     * Mass unit identifiers (base unit: meter).
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Mass implements Unit {

        /**
         * Metric tonne (t).
         */
        TONNE,

        /**
         * Kilogram (kg).
         */
        KILOGRAM,

        /**
         * Gram (g).
         */
        GRAM,

        /**
         * Milligram (mg).
         */
        MILLIGRAM,

        /**
         * Pound (lb).
         */
        POUND,

        /**
         * Ounce (oz).
         */
        OUNCE
    }

}