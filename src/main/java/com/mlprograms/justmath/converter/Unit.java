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
     * Volume unit identifiers (base unit: cubic meter).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Volume implements Unit {

        /**
         * Cubic kilometer (km^3).
         */
        CUBIC_KILOMETER,
        /**
         * Cubic meter (m^3), base unit of the VOLUME category.
         */
        CUBIC_METER,
        /**
         * Cubic decimeter (dm^3) – equals 1 liter.
         */
        CUBIC_DECIMETER,
        /**
         * Cubic centimeter (cm^3) – equals 1 milliliter.
         */
        CUBIC_CENTIMETER,
        /**
         * Cubic millimeter (mm^3).
         */
        CUBIC_MILLIMETER,

        /**
         * Exaliter (EL).
         */
        EXALITER,
        /**
         * Petaliter (PL).
         */
        PETALITER,
        /**
         * Teraliter (TL).
         */
        TERALITER,
        /**
         * Gigaliter (GL).
         */
        GIGALITER,
        /**
         * Megaliter (ML).
         */
        MEGALITER,
        /**
         * Kiloliter (kL).
         */
        KILOLITER,
        /**
         * Hectoliter (hL).
         */
        HECTOLITER,
        /**
         * Dekaliter (daL).
         */
        DEKALITER,
        /**
         * Liter (L, l).
         */
        LITER,
        /**
         * Deciliter (dL).
         */
        DECILITER,
        /**
         * Centiliter (cL).
         */
        CENTILITER,
        /**
         * Milliliter (mL).
         */
        MILLILITER,
        /**
         * Microliter (µL).
         */
        MICROLITER,
        /**
         * Nanoliter (nL).
         */
        NANOLITER,
        /**
         * Picoliter (pL).
         */
        PICOLITER,
        /**
         * Femtoliter (fL).
         */
        FEMTOLITER,
        /**
         * Attoliter (aL).
         */
        ATTOLITER,

        /**
         * Cup (metric).
         */
        METRIC_CUP,
        /**
         * Tablespoon (metric).
         */
        METRIC_TABLESPOON,
        /**
         * Teaspoon (metric).
         */
        METRIC_TEASPOON,

        /**
         * Gallon (US) (gal (US)).
         */
        US_GALLON,
        /**
         * Quart (US) (qt (US)).
         */
        US_QUART,
        /**
         * Pint (US) (pt (US)).
         */
        US_PINT,
        /**
         * Cup (US).
         */
        US_CUP,
        /**
         * Fluid ounce (US) (fl oz (US)).
         */
        US_FLUID_OUNCE,
        /**
         * Tablespoon (US).
         */
        US_TABLESPOON,
        /**
         * Dessertspoon (US).
         */
        US_DESSERTSPOON,
        /**
         * Teaspoon (US).
         */
        US_TEASPOON,
        /**
         * Gill (US) (gi).
         */
        US_GILL,
        /**
         * Minim (US).
         */
        US_MINIM,
        /**
         * Barrel (US) (bbl (US)).
         */
        US_BARREL,

        /**
         * Gallon (UK) / Imperial (gal (UK)).
         */
        IMPERIAL_GALLON,
        /**
         * Quart (UK) / Imperial (qt (UK)).
         */
        IMPERIAL_QUART,
        /**
         * Pint (UK) / Imperial (pt (UK)).
         */
        IMPERIAL_PINT,
        /**
         * Cup (UK).
         */
        IMPERIAL_CUP,
        /**
         * Fluid ounce (UK) / Imperial (fl oz (UK)).
         */
        IMPERIAL_FLUID_OUNCE,
        /**
         * Tablespoon (UK).
         */
        IMPERIAL_TABLESPOON,
        /**
         * Dessertspoon (UK).
         */
        IMPERIAL_DESSERTSPOON,
        /**
         * Teaspoon (UK).
         */
        IMPERIAL_TEASPOON,
        /**
         * Gill (UK) (gi (UK)).
         */
        IMPERIAL_GILL,
        /**
         * Minim (UK).
         */
        IMPERIAL_MINIM,
        /**
         * Barrel (UK) (bbl (UK)).
         */
        IMPERIAL_BARREL,

        /**
         * Cubic mile (mi^3).
         */
        CUBIC_MILE,
        /**
         * Cubic yard (yd^3).
         */
        CUBIC_YARD,
        /**
         * Cubic foot (ft^3).
         */
        CUBIC_FOOT,
        /**
         * Cubic inch (in^3).
         */
        CUBIC_INCH,

        /**
         * Hundred cubic foot (100 ft^3).
         */
        HUNDRED_CUBIC_FOOT,
        /**
         * Ton register (ton reg).
         */
        TON_REGISTER,
        /**
         * Acre-foot (ac*ft).
         */
        ACRE_FOOT,
        /**
         * Acre-foot (US survey).
         */
        ACRE_FOOT_US_SURVEY,
        /**
         * Acre-inch (ac*in).
         */
        ACRE_INCH,
        /**
         * Board foot.
         */
        BOARD_FOOT,
        /**
         * Stere (st) – commonly 1 m^3 (stacked wood).
         */
        STERE,
        /**
         * Dekastere – 10 stere.
         */
        DEKASTERE,
        /**
         * Decistere – 0.1 stere.
         */
        DECISTERE,
        /**
         * Cord (cd).
         */
        CORD,

        /**
         * Drop.
         */
        DROP,
        /**
         * Barrel (oil) (bbl (oil)).
         */
        OIL_BARREL,
        /**
         * Tun.
         */
        TUN,
        /**
         * Hogshead.
         */
        HOGSHEAD,
        /**
         * Dram (dr).
         */
        DRAM,
        /**
         * Taza (Spanish).
         */
        SPANISH_TAZA,

        /**
         * Cor (Biblical).
         */
        BIBLICAL_COR,
        /**
         * Homer (Biblical).
         */
        BIBLICAL_HOMER,
        /**
         * Bath (Biblical).
         */
        BIBLICAL_BATH,
        /**
         * Hin (Biblical).
         */
        BIBLICAL_HIN,
        /**
         * Cab (Biblical).
         */
        BIBLICAL_CAB,
        /**
         * Log (Biblical).
         */
        BIBLICAL_LOG,

        /**
         * Earth's volume.
         */
        EARTH_VOLUME

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
         * Exagram.
         */
        EXAGRAM,
        /**
         * Petagram.
         */
        PETAGRAM,
        /**
         * Teragram.
         */
        TERAGRAM,
        /**
         * Gigagram.
         */
        GIGAGRAM,
        /**
         * Megagram.
         */
        MEGAGRAM,
        /**
         * Kiloton (metric).
         */
        KILOTON,
        /**
         * Quintal (metric).
         */
        QUINTAL_METRIC,
        /**
         * Metric tonne (t).
         */
        TON,

        /**
         * Kilogram, base unit of the MASS category.
         */
        KILOGRAM,
        /**
         * Hectogram.
         */
        HECTOGRAM,
        /**
         * Dekagram.
         */
        DEKAGRAM,
        /**
         * Gram.
         */
        GRAM,
        /**
         * Decigram.
         */
        DECIGRAM,
        /**
         * Centigram.
         */
        CENTIGRAM,
        /**
         * Milligram.
         */
        MILLIGRAM,
        /**
         * Microgram.
         */
        MICROGRAM,
        /**
         * Gamma (microgram alias used in some contexts).
         */
        GAMMA,
        /**
         * Nanogram.
         */
        NANOGRAM,
        /**
         * Picogram.
         */
        PICOGRAM,
        /**
         * Femtogram.
         */
        FEMTOGRAM,
        /**
         * Attogram.
         */
        ATTOGRAM,

        /**
         * Carat.
         */
        CARRAT,
        /**
         * Grain.
         */
        GRAIN,
        /**
         * Pennyweight.
         */
        PENNYWEIGHT,
        /**
         * Scruple (apothecary).
         */
        SCRUPLE_APOTHECARY,
        /**
         * Pound (troy or apothecary).
         */
        POUND_TROY_APOTHECARY,

        /**
         * Ton (long).
         */
        LONG_TON,
        /**
         * Ton (short).
         */
        SHORT_TON,
        /**
         * Pound.
         */
        POUND,
        /**
         * Ounce.
         */
        OUNCE,

        /**
         * Hundredweight (United States).
         */
        HUNDREDWEIGHT_UNITED_STATES,
        /**
         * Hundredweight (United Kingdom).
         */
        HUNDREDWEIGHT_UNITED_KINGDOM,
        /**
         * Quarter (United States).
         */
        QUARTER_UNITED_STATES,
        /**
         * Quarter (United Kingdom).
         */
        QUARTER_UNITED_KINGDOM,
        /**
         * Stone (United States).
         */
        STONE_UNITED_STATES,
        /**
         * Stone (United Kingdom).
         */
        STONE_UNITED_KINGDOM,

        /**
         * Kilogram-force square second per meter.
         */
        KILOGRAM_FORCE_SECOND_SQUARED_PER_METER,
        /**
         * Pound-force square second per foot.
         */
        POUND_FORCE_SECOND_SQUARED_PER_FOOT,
        /**
         * Slug.
         */
        SLUG,
        /**
         * Poundal.
         */
        POUNDAL,
        /**
         * Kilopound.
         */
        KILOPOUND,

        /**
         * Ton (assay) (United States).
         */
        ASSAY_TON_UNITED_STATES,
        /**
         * Ton (assay) (United Kingdom).
         */
        ASSAY_TON_UNITED_KINGDOM,

        /**
         * Atomic mass unit (u).
         */
        ATOMIC_MASS_UNIT,
        /**
         * Dalton.
         */
        DALTON,

        /**
         * Planck mass.
         */
        PLANCK_MASS,
        /**
         * Electron mass (rest).
         */
        ELECTRON_REST_MASS,
        /**
         * Muon mass.
         */
        MUON_MASS,
        /**
         * Proton mass.
         */
        PROTON_MASS,
        /**
         * Neutron mass.
         */
        NEUTRON_MASS,
        /**
         * Deuteron mass.
         */
        DEUTERON_MASS,

        /**
         * Earth's mass.
         */
        EARTH_MASS,
        /**
         * Sun's mass.
         */
        SUN_MASS,

        /**
         * Talent (Biblical Hebrew).
         */
        BIBLICAL_HEBREW_TALENT,
        /**
         * Mina (Biblical Hebrew).
         */
        BIBLICAL_HEBREW_MINA,
        /**
         * Shekel (Biblical Hebrew).
         */
        BIBLICAL_HEBREW_SHEKEL,
        /**
         * Bekan (Biblical Hebrew).
         */
        BIBLICAL_HEBREW_BEKAN,
        /**
         * Gerah (Biblical Hebrew).
         */
        BIBLICAL_HEBREW_GERAH,

        /**
         * Talent (Biblical Greek).
         */
        BIBLICAL_GREEK_TALENT,
        /**
         * Mina (Biblical Greek).
         */
        BIBLICAL_GREEK_MINA,
        /**
         * Tetradrachma (Biblical Greek).
         */
        BIBLICAL_GREEK_TETRADRACHMA,
        /**
         * Didrachma (Biblical Greek).
         */
        BIBLICAL_GREEK_DIDRACHMA,
        /**
         * Drachma (Biblical Greek).
         */
        BIBLICAL_GREEK_DRACHMA,

        /**
         * Denarius (Biblical Roman).
         */
        BIBLICAL_ROMAN_DENARIUS,
        /**
         * Assarion (Biblical Roman).
         */
        BIBLICAL_ROMAN_ASSARION,
        /**
         * Quadrans (Biblical Roman).
         */
        BIBLICAL_ROMAN_QUADRANS,
        /**
         * Lepton (Biblical Roman).
         */
        BIBLICAL_ROMAN_LEPTON

    }

    /**
     * Temperature unit identifiers (base unit: celsius).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
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

    /**
     * Pressure unit identifiers (base unit: pascal).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Pressure implements Unit {

        /**
         * Exapascal.
         */
        EXAPASCAL,
        /**
         * Petapascal.
         */
        PETAPASCAL,
        /**
         * Terapascal.
         */
        TERAPASCAL,
        /**
         * Gigapascal.
         */
        GIGAPASCAL,
        /**
         * Megapascal.
         */
        MEGAPASCAL,
        /**
         * Kilopascal.
         */
        KILOPASCAL,
        /**
         * Hectopascal.
         */
        HECTOPASCAL,
        /**
         * Dekapascal.
         */
        DEKAPASCAL,

        /**
         * Pascal, base unit of the PRESSURE category.
         */
        PASCAL,

        /**
         * Decipascal.
         */
        DECIPASCAL,
        /**
         * Centipascal.
         */
        CENTIPASCAL,
        /**
         * Millipascal.
         */
        MILLIPASCAL,
        /**
         * Micropascal.
         */
        MICROPASCAL,
        /**
         * Nanopascal.
         */
        NANOPASCAL,
        /**
         * Picopascal.
         */
        PICOPASCAL,
        /**
         * Femtopascal.
         */
        FEMTOPASCAL,
        /**
         * Attopascal.
         */
        ATTOPASCAL,

        /**
         * Bar.
         */
        BAR,
        /**
         * Millibar.
         */
        MILLIBAR,
        /**
         * Microbar.
         */
        MICROBAR,
        /**
         * Standard atmosphere.
         */
        STANDARD_ATMOSPHERE,
        /**
         * Technical atmosphere.
         */
        TECHNICAL_ATMOSPHERE,
        /**
         * Pounds per square inch.
         */
        PSI,
        /**
         * Kips per square inch (also commonly used as "ksi").
         */
        KSI,
        /**
         * Torr.
         */
        TORR,

        /**
         * Newton per square meter.
         */
        NEWTON_PER_SQUARE_METER,
        /**
         * Newton per square centimeter.
         */
        NEWTON_PER_SQUARE_CENTIMETER,
        /**
         * Newton per square millimeter.
         */
        NEWTON_PER_SQUARE_MILLIMETER,
        /**
         * Kilonewton per square meter.
         */
        KILONEWTON_PER_SQUARE_METER,
        /**
         * Dyne per square centimeter.
         */
        DYNE_PER_SQUARE_CENTIMETER,

        /**
         * Kilogram-force per square meter.
         */
        KILOGRAM_FORCE_PER_SQUARE_METER,
        /**
         * Kilogram-force per square centimeter.
         */
        KILOGRAM_FORCE_PER_SQUARE_CENTIMETER,
        /**
         * Kilogram-force per square millimeter.
         */
        KILOGRAM_FORCE_PER_SQUARE_MILLIMETER,
        /**
         * Gram-force per square centimeter.
         */
        GRAM_FORCE_PER_SQUARE_CENTIMETER,

        /**
         * Short ton-force per square foot.
         */
        SHORT_TON_FORCE_PER_SQUARE_FOOT,
        /**
         * Short ton-force per square inch.
         */
        SHORT_TON_FORCE_PER_SQUARE_INCH,
        /**
         * Long ton-force per square foot.
         */
        LONG_TON_FORCE_PER_SQUARE_FOOT,
        /**
         * Long ton-force per square inch.
         */
        LONG_TON_FORCE_PER_SQUARE_INCH,

        /**
         * Kip-force per square inch.
         */
        KIP_FORCE_PER_SQUARE_INCH,
        /**
         * Pound-force per square foot.
         */
        POUND_FORCE_PER_SQUARE_FOOT,
        /**
         * Pound-force per square inch.
         */
        POUND_FORCE_PER_SQUARE_INCH,
        /**
         * Poundal per square foot.
         */
        POUNDAL_PER_SQUARE_FOOT,

        /**
         * Centimeter of mercury at 0°C.
         */
        CENTIMETER_OF_MERCURY_0C,
        /**
         * Millimeter of mercury at 0°C.
         */
        MILLIMETER_OF_MERCURY_0C,
        /**
         * Inch of mercury at 32°F.
         */
        INCH_OF_MERCURY_32F,
        /**
         * Inch of mercury at 60°F.
         */
        INCH_OF_MERCURY_60F,

        /**
         * Centimeter of water at 4°C.
         */
        CENTIMETER_OF_WATER_4C,
        /**
         * Millimeter of water at 4°C.
         */
        MILLIMETER_OF_WATER_4C,
        /**
         * Inch of water at 4°C.
         */
        INCH_OF_WATER_4C,
        /**
         * Foot of water at 4°C.
         */
        FOOT_OF_WATER_4C,
        /**
         * Inch of water at 60°F.
         */
        INCH_OF_WATER_60F,
        /**
         * Foot of water at 60°F.
         */
        FOOT_OF_WATER_60F

    }

    /**
     * Energy unit identifiers (base unit: joule).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Energy implements Unit {

        /**
         * Gigajoule.
         */
        GIGAJOULE,
        /**
         * Megajoule.
         */
        MEGAJOULE,
        /**
         * Kilojoule.
         */
        KILOJOULE,

        /**
         * Joule, base unit of the ENERGY category.
         */
        JOULE,

        /**
         * Millijoule.
         */
        MILLIJOULE,
        /**
         * Microjoule.
         */
        MICROJOULE,
        /**
         * Nanojoule.
         */
        NANOJOULE,
        /**
         * Attojoule.
         */
        ATTOJOULE,

        /**
         * Gigawatt-hour.
         */
        GIGAWATT_HOUR,
        /**
         * Megawatt-hour.
         */
        MEGAWATT_HOUR,
        /**
         * Kilowatt-hour.
         */
        KILOWATT_HOUR,
        /**
         * Watt-hour.
         */
        WATT_HOUR,
        /**
         * Kilowatt-second.
         */
        KILOWATT_SECOND,
        /**
         * Watt-second.
         */
        WATT_SECOND,

        /**
         * Calorie (nutritional).
         */
        CALORIE_NUTRITIONAL,
        /**
         * Kilocalorie (IT).
         */
        KILOCALORIE_IT,
        /**
         * Kilocalorie (th).
         */
        KILOCALORIE_TH,
        /**
         * Calorie (IT).
         */
        CALORIE_IT,
        /**
         * Calorie (th).
         */
        CALORIE_TH,

        /**
         * British thermal unit (IT).
         */
        BTU_IT,
        /**
         * British thermal unit (th).
         */
        BTU_TH,
        /**
         * Mega British thermal unit (IT).
         */
        MEGA_BTU_IT,

        /**
         * Therm.
         */
        THERM,
        /**
         * Therm (EC).
         */
        THERM_EC,
        /**
         * Therm (US).
         */
        THERM_US,

        /**
         * Ton-hour (refrigeration).
         */
        TON_HOUR_REFRIGERATION,

        /**
         * Horsepower (metric) hour.
         */
        HORSEPOWER_METRIC_HOUR,
        /**
         * Horsepower hour.
         */
        HORSEPOWER_HOUR,

        /**
         * Megaelectron-volt.
         */
        MEGAELECTRON_VOLT,
        /**
         * Kiloelectron-volt.
         */
        KILOELECTRON_VOLT,
        /**
         * Electron-volt.
         */
        ELECTRON_VOLT,

        /**
         * Hartree energy.
         */
        HARTREE_ENERGY,
        /**
         * Rydberg constant (energy equivalent).
         */
        RYDBERG_CONSTANT,

        /**
         * Erg.
         */
        ERG,

        /**
         * Newton meter.
         */
        NEWTON_METER,
        /**
         * Dyne centimeter.
         */
        DYNE_CENTIMETER,

        /**
         * Gram-force meter.
         */
        GRAM_FORCE_METER,
        /**
         * Gram-force centimeter.
         */
        GRAM_FORCE_CENTIMETER,
        /**
         * Kilogram-force centimeter.
         */
        KILOGRAM_FORCE_CENTIMETER,
        /**
         * Kilogram-force meter.
         */
        KILOGRAM_FORCE_METER,
        /**
         * Kilopond meter.
         */
        KILOPOND_METER,

        /**
         * Pound-force foot.
         */
        POUND_FORCE_FOOT,
        /**
         * Pound-force inch.
         */
        POUND_FORCE_INCH,
        /**
         * Ounce-force inch.
         */
        OUNCE_FORCE_INCH,

        /**
         * Foot-pound.
         */
        FOOT_POUND,
        /**
         * Inch-pound.
         */
        INCH_POUND,
        /**
         * Inch-ounce.
         */
        INCH_OUNCE,

        /**
         * Poundal foot.
         */
        POUNDAL_FOOT,

        /**
         * Gigaton (TNT equivalent).
         */
        GIGATON_TNT,
        /**
         * Megaton (TNT equivalent).
         */
        MEGATON_TNT,
        /**
         * Kiloton (TNT equivalent).
         */
        KILOTON_TNT,
        /**
         * Ton (explosives, TNT equivalent).
         */
        TON_TNT,

        /**
         * Fuel oil equivalent at kiloliter.
         */
        FUEL_OIL_EQUIVALENT_KILOLITER,
        /**
         * Fuel oil equivalent at barrel (US).
         */
        FUEL_OIL_EQUIVALENT_US_BARREL

    }

    /**
     * Power unit identifiers (base unit: watt).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Power implements Unit {

        /**
         * Exawatt.
         */
        EXAWATT,
        /**
         * Petawatt.
         */
        PETAWATT,
        /**
         * Terawatt.
         */
        TERAWATT,
        /**
         * Gigawatt.
         */
        GIGAWATT,
        /**
         * Megawatt.
         */
        MEGAWATT,
        /**
         * Kilowatt.
         */
        KILOWATT,
        /**
         * Hectowatt.
         */
        HECTOWATT,
        /**
         * Dekawatt.
         */
        DEKAWATT,

        /**
         * Watt, base unit of the POWER category.
         */
        WATT,

        /**
         * Deciwatt.
         */
        DECIWATT,
        /**
         * Centiwatt.
         */
        CENTIWATT,
        /**
         * Milliwatt.
         */
        MILLIWATT,
        /**
         * Microwatt.
         */
        MICROWATT,
        /**
         * Nanowatt.
         */
        NANOWATT,
        /**
         * Picowatt.
         */
        PICOWATT,
        /**
         * Femtowatt.
         */
        FEMTOWATT,
        /**
         * Attowatt.
         */
        ATTOWATT,

        /**
         * Horsepower (general / UK).
         */
        HORSEPOWER,
        /**
         * Horsepower (550 ft·lbf/s) – mechanical horsepower.
         */
        HORSEPOWER_MECHANICAL_550_FTLBF_PER_S,
        /**
         * Horsepower (metric).
         */
        HORSEPOWER_METRIC,
        /**
         * Horsepower (boiler).
         */
        HORSEPOWER_BOILER,
        /**
         * Horsepower (electric).
         */
        HORSEPOWER_ELECTRIC,
        /**
         * Horsepower (water).
         */
        HORSEPOWER_WATER,
        /**
         * Pferdestärke (PS).
         */
        PFERDESTAERKE,

        /**
         * Btu (IT) per hour.
         */
        BTU_IT_PER_HOUR,
        /**
         * Btu (IT) per minute.
         */
        BTU_IT_PER_MINUTE,
        /**
         * Btu (IT) per second.
         */
        BTU_IT_PER_SECOND,

        /**
         * Btu (th) per hour.
         */
        BTU_TH_PER_HOUR,
        /**
         * Btu (th) per minute.
         */
        BTU_TH_PER_MINUTE,
        /**
         * Btu (th) per second.
         */
        BTU_TH_PER_SECOND,

        /**
         * Mega Btu (IT) per hour.
         */
        MEGA_BTU_IT_PER_HOUR,
        /**
         * MBH (thousand Btu per hour).
         */
        MBH,
        /**
         * Ton (refrigeration).
         */
        TON_REFRIGERATION,

        /**
         * Kilocalorie (IT) per hour.
         */
        KILOCALORIE_IT_PER_HOUR,
        /**
         * Kilocalorie (IT) per minute.
         */
        KILOCALORIE_IT_PER_MINUTE,
        /**
         * Kilocalorie (IT) per second.
         */
        KILOCALORIE_IT_PER_SECOND,

        /**
         * Kilocalorie (th) per hour.
         */
        KILOCALORIE_TH_PER_HOUR,
        /**
         * Kilocalorie (th) per minute.
         */
        KILOCALORIE_TH_PER_MINUTE,
        /**
         * Kilocalorie (th) per second.
         */
        KILOCALORIE_TH_PER_SECOND,

        /**
         * Calorie (IT) per hour.
         */
        CALORIE_IT_PER_HOUR,
        /**
         * Calorie (IT) per minute.
         */
        CALORIE_IT_PER_MINUTE,
        /**
         * Calorie (IT) per second.
         */
        CALORIE_IT_PER_SECOND,

        /**
         * Calorie (th) per hour.
         */
        CALORIE_TH_PER_HOUR,
        /**
         * Calorie (th) per minute.
         */
        CALORIE_TH_PER_MINUTE,
        /**
         * Calorie (th) per second.
         */
        CALORIE_TH_PER_SECOND,

        /**
         * Foot pound-force per hour.
         */
        FOOT_POUND_FORCE_PER_HOUR,
        /**
         * Foot pound-force per minute.
         */
        FOOT_POUND_FORCE_PER_MINUTE,
        /**
         * Foot pound-force per second.
         */
        FOOT_POUND_FORCE_PER_SECOND,

        /**
         * Pound-foot per hour.
         */
        POUND_FOOT_PER_HOUR,
        /**
         * Pound-foot per minute.
         */
        POUND_FOOT_PER_MINUTE,
        /**
         * Pound-foot per second.
         */
        POUND_FOOT_PER_SECOND,

        /**
         * Erg per second.
         */
        ERG_PER_SECOND,

        /**
         * Kilovolt-ampere.
         */
        KILOVOLT_AMPERE,
        /**
         * Volt-ampere.
         */
        VOLT_AMPERE,

        /**
         * Newton meter per second.
         */
        NEWTON_METER_PER_SECOND,

        /**
         * Joule per second.
         */
        JOULE_PER_SECOND,
        /**
         * Exajoule per second.
         */
        EXAJOULE_PER_SECOND,
        /**
         * Petajoule per second.
         */
        PETAJOULE_PER_SECOND,
        /**
         * Terajoule per second.
         */
        TERAJOULE_PER_SECOND,
        /**
         * Gigajoule per second.
         */
        GIGAJOULE_PER_SECOND,
        /**
         * Megajoule per second.
         */
        MEGAJOULE_PER_SECOND,
        /**
         * Kilojoule per second.
         */
        KILOJOULE_PER_SECOND,
        /**
         * Hectojoule per second.
         */
        HECTOJOULE_PER_SECOND,
        /**
         * Dekajoule per second.
         */
        DEKAJOULE_PER_SECOND,
        /**
         * Decijoule per second.
         */
        DECIJOULE_PER_SECOND,
        /**
         * Centijoule per second.
         */
        CENTIJOULE_PER_SECOND,
        /**
         * Millijoule per second.
         */
        MILLIJOULE_PER_SECOND,
        /**
         * Microjoule per second.
         */
        MICROJOULE_PER_SECOND,
        /**
         * Nanojoule per second.
         */
        NANOJOULE_PER_SECOND,
        /**
         * Picojoule per second.
         */
        PICOJOULE_PER_SECOND,
        /**
         * Femtojoule per second.
         */
        FEMTOJOULE_PER_SECOND,
        /**
         * Attojoule per second.
         */
        ATTOJOULE_PER_SECOND,

        /**
         * Joule per hour.
         */
        JOULE_PER_HOUR,
        /**
         * Joule per minute.
         */
        JOULE_PER_MINUTE,
        /**
         * Kilojoule per hour.
         */
        KILOJOULE_PER_HOUR,
        /**
         * Kilojoule per minute.
         */
        KILOJOULE_PER_MINUTE

    }

}