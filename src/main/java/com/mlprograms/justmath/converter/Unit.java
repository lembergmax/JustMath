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
public interface Unit {

    /**
     * Length unit identifiers (base unit: meter).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     *
     * <p><b>Ordering</b></p>
     * <ul>
     *   <li><b>Metric (SI + prefixes)</b> from largest to smallest.</li>
     *   <li><b>Very small / physics</b> (Planck length, radii, etc.).</li>
     *   <li><b>Astronomy</b> (AU, Earth–Sun distance, parsec, light-year, etc.).</li>
     *   <li><b>Imperial / US customary</b> (mile → inch) + historical.</li>
     *   <li><b>Typographic / CSS</b> units (pixel, point, pica, em).</li>
     * </ul>
     */
    enum Length implements Unit {

        /**
         * Exameter (Em).
         */
        EXAMETER,
        /**
         * Petameter (Pm).
         */
        PETAMETER,
        /**
         * Terameter (Tm).
         */
        TERAMETER,
        /**
         * Gigameter (Gm).
         */
        GIGAMETER,
        /**
         * Megameter (Mm).
         */
        MEGAMETER,
        /**
         * Kilometer (km).
         */
        KILOMETER,
        /**
         * Hectometer (hm).
         */
        HECTOMETER,
        /**
         * Dekameter (dam).
         */
        DEKAMETER,
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
         * Micrometer (µm).
         */
        MICROMETER,
        /**
         * Micron (µm) – alias/common name for micrometer.
         */
        MICRON,
        /**
         * Nanometer (nm).
         */
        NANOMETER,
        /**
         * Ångström (Å).
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
         * Attometer (am).
         */
        ATTOMETER,

        /**
         * Planck length (ℓP).
         */
        PLANCK_LENGTH,
        /**
         * Classical electron radius (re).
         */
        ELECTRON_RADIUS,
        /**
         * Bohr radius (a0).
         */
        BOHR_RADIUS,
        /**
         * X unit (xu) – crystallography X-ray wavelength unit.
         */
        X_UNIT,
        /**
         * Fermi (fm) – common name for femtometer.
         */
        FERMI,

        /**
         * Sun radius (R☉).
         */
        SUN_RADIUS,
        /**
         * Earth equatorial radius (a).
         */
        EARTH_EQUATORIAL_RADIUS,
        /**
         * Earth polar radius (b).
         */
        EARTH_POLAR_RADIUS,
        /**
         * Astronomical unit (au).
         */
        ASTRONOMICAL_UNIT,
        /**
         * Earth distance from Sun – usually ~1 au (mean).
         */
        EARTH_DISTANCE_FROM_SUN,
        /**
         * Kiloparsec (kpc).
         */
        KILOPARSEC,
        /**
         * Megaparsec (Mpc).
         */
        MEGAPARSEC,
        /**
         * Parsec (pc).
         */
        PARSEC,
        /**
         * Light year (ly).
         */
        LIGHT_YEAR,

        /**
         * League (general; historically variable).
         */
        LEAGUE,
        /**
         * Nautical league (International; historically variable).
         */
        NAUTICAL_LEAGUE_INTERNATIONAL,
        /**
         * Nautical league (UK; historically variable).
         */
        NAUTICAL_LEAGUE_UK,

        /**
         * Nautical mile (generic).
         */
        NAUTICAL_MILE,
        /**
         * Nautical mile (International).
         */
        NAUTICAL_MILE_INTERNATIONAL,
        /**
         * Nautical mile (UK).
         */
        NAUTICAL_MILE_UK,

        /**
         * Mile (generic).
         */
        MILE,
        /**
         * Roman mile (mille passus; historical).
         */
        MILE_ROMAN,

        /**
         * Kiloyard (kyd).
         */
        KILOYARD,
        /**
         * Furlong (fur).
         */
        FURLONG,
        /**
         * Chain (ch).
         */
        CHAIN,
        /**
         * Rope (rope) – historical surveying length (often 20 yd / 100 links).
         */
        ROPE,
        /**
         * Rod (rd).
         */
        ROD,
        /**
         * Fathom (ftm).
         */
        FATHOM,
        /**
         * Ell (ell) – historical cloth measure (varies).
         */
        ELL,
        /**
         * Link (li) – surveying (often 1/100 chain; varies by system).
         */
        LINK,
        /**
         * Cubit (UK) – historical (varies).
         */
        CUBIT_UK,
        /**
         * Hand (hand).
         */
        HAND,
        /**
         * Handbreadth (hb) – historical/anthropometric (varies).
         */
        HANDBREADTH,
        /**
         * Fingerbreadth (fb) – historical/anthropometric (varies).
         */
        FINGERBREADTH,
        /**
         * Span (cloth) – historical/anthropometric (varies).
         */
        SPAN_CLOTH,
        /**
         * Finger (cloth) – historical (varies).
         */
        FINGER_CLOTH,
        /**
         * Nail (cloth) – historical (varies).
         */
        NAIL_COTH,
        /**
         * Barleycorn – historical (often 1/3 inch; varies).
         */
        BARLEYCORN,

        /**
         * Yard (yd).
         */
        YARD,
        /**
         * Foot (ft).
         */
        FEET,
        /**
         * Inch (in).
         */
        INCH,

        /**
         * Caliber (cal) – firearms/engineering; context-dependent.
         */
        CALIBER,
        /**
         * Centiinch (cin) – 1/100 inch.
         */
        CENTIINCH,
        /**
         * Mil (mil) – 1/1000 inch (thou).
         */
        MIL,
        /**
         * Microinch (µin).
         */
        MICROINCH,
        /**
         * Twip (twp) – 1/20 point (typography/UI).
         */
        TWIP,

        /**
         * Arpent (arp) – French/Canadian land measure (varies).
         */
        ARPENT,
        /**
         * Aln (aln) – Scandinavian ell (historical; varies).
         */
        ALN,
        /**
         * Famn (famn) – Swedish fathom (historical; varies).
         */
        FAMN,
        /**
         * Ken (ken) – Japanese unit (varies by context/period).
         */
        KEN,

        /**
         * CSS pixel (px).
         */
        PIXEL,
        /**
         * Typographic point (pt).
         */
        POINT,
        /**
         * Typographic pica (pc / pica).
         */
        PICA,
        /**
         * Em (em) – relative to current font size.
         */
        EM

    }

    /**
     * Area unit identifiers (base unit: square meter).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Area implements Unit {

        /**
         * Square kilometer (km^2).
         */
        SQUARE_KILOMETER,
        /**
         * Square hectometer (hm^2).
         */
        SQUARE_HECTOMETER,
        /**
         * Square dekameter (dam^2).
         */
        SQUARE_DEKAMETER,
        /**
         * Square meter (m^2), base unit of the AREA category.
         */
        SQUARE_METER,
        /**
         * Square decimeter (dm^2).
         */
        SQUARE_DECIMETER,
        /**
         * Square centimeter (cm^2).
         */
        SQUARE_CENTIMETER,
        /**
         * Square millimeter (mm^2).
         */
        SQUARE_MILLIMETER,
        /**
         * Square micrometer (µm^2).
         */
        SQUARE_MICROMETER,
        /**
         * Square nanometer (nm^2).
         */
        SQUARE_NANOMETER,

        /**
         * Hectare (ha).
         */
        HECTARE,
        /**
         * Are (a).
         */
        ARE,

        /**
         * Barn (b).
         */
        BARN,
        /**
         * Electron cross section (Thomson cross section, σT).
         */
        ELECTRON_CROSS_SECTION,

        /**
         * Township (twp).
         */
        TOWNSHIP,
        /**
         * Section (sec).
         */
        SECTION,
        /**
         * Homestead (hstd).
         */
        HOMESTEAD,

        /**
         * Square mile (mi^2).
         */
        SQUARE_MILE,
        /**
         * Acre (ac).
         */
        ACRE,
        /**
         * Rood (rood).
         */
        ROOD,

        /**
         * Square chain (ch^2).
         */
        SQUARE_CHAIN,
        /**
         * Square rod (rd^2).
         */
        SQUARE_ROD,
        /**
         * Square pole (pole^2).
         */
        SQUARE_POLE,
        /**
         * Square rope (rope^2).
         */
        SQUARE_ROPE,

        /**
         * Square yard (yd^2).
         */
        SQUARE_YARD,
        /**
         * Square foot (ft^2).
         */
        SQUARE_FOOT,
        /**
         * Square inch (in^2).
         */
        SQUARE_INCH,

        /**
         * Arpent (arp).
         */
        ARPENT,
        /**
         * Cuerda (cda).
         */
        CUERDA,
        /**
         * Plaza (plz).
         */
        PLAZA

    }

    /**
     * Mass unit identifiers (base unit: kilogram).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Mass implements Unit {

        /**
         * Metric tonne (t).
         */
        TON,
        /**
         * Kilogram (kg), base unit of the MASS category.
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
         * Long ton (lt) – UK ton.
         */
        LONG_TON,
        /**
         * Short ton (st) – US ton.
         */
        SHORT_TON,
        /**
         * Pound (lb).
         */
        POUND,
        /**
         * Ounce (oz).
         */
        OUNCE,

        /**
         * Carat (ct) – 0.2 g (gem mass).
         */
        CARRAT,
        /**
         * Unified atomic mass unit (u) – also known as Dalton (Da).
         */
        ATOMIC_MASS_UNIT

    }

    /**
     * Temperature unit identifiers.
     *
     * <p>
     * The base unit of this group is {@code KELVIN}. Unlike length/mass, temperature conversions
     * generally require an <em>offset</em> (affine transformation). The exact conversion formulas
     * are defined in {@link UnitRegistry}.
     * </p>
     */
    enum Temperature implements Unit {

        /**
         * Kelvin (base unit).
         */
        KELVIN,
        /**
         * Degrees Celsius.
         */
        CELSIUS,
        /**
         * Degrees Fahrenheit.
         */
        FAHRENHEIT

    }

}