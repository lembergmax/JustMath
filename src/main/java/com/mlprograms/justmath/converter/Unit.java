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

    /**
     * Time unit identifiers (base unit: second).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Time implements Unit {

        /**
         * Second (base unit).
         */
        SECOND,

        /**
         * Millisecond: 1 ms = 0.001 s.
         */
        MILLISECOND,

        /**
         * Microsecond: 1 µs = 1e-6 s.
         */
        MICROSECOND,

        /**
         * Nanosecond: 1 ns = 1e-9 s.
         */
        NANOSECOND,

        /**
         * Picosecond: 1 ps = 1e-12 s.
         */
        PICOSECOND,

        /**
         * Femtosecond: 1 fs = 1e-15 s.
         */
        FEMTOSECOND,

        /**
         * Attosecond: 1 as = 1e-18 s.
         */
        ATTOSECOND,

        /**
         * Minute: 1 min = 60 s.
         */
        MINUTE,

        /**
         * Hour: 1 h = 3600 s.
         */
        HOUR,

        /**
         * Day: 1 d = 86400 s.
         */
        DAY,

        /**
         * Week: 1 week = 604800 s.
         */
        WEEK,

        /**
         * Month: 1 month = 2628000 s.
         */
        MONTH,

        /**
         * Year: 1 y = 31557600 s.
         */
        YEAR,

        /**
         * Decade: 1 decade = 315576000 s.
         */
        DECADE,

        /**
         * Century: 1 century = 3155760000 s.
         */
        CENTURY,

        /**
         * Millennium: 1 millennium = 31557600000 s.
         */
        MILLENNIUM,

        /**
         * Shake: 1 shake = 1e-8 s.
         */
        SHAKE,

        /**
         * Synodic month: 1 month (synodic) = 2551443.84 s.
         */
        MONTH_SYNODIC,

        /**
         * Julian year: 1 year (Julian) = 31557600 s.
         */
        YEAR_JULIAN,

        /**
         * Leap year: 1 year (leap) = 31622400 s.
         */
        YEAR_LEAP,

        /**
         * Tropical year: 1 year (tropical) = 31556930 s.
         */
        YEAR_TROPICAL,

        /**
         * Sidereal year: 1 year (sidereal) = 31558149.54 s.
         */
        YEAR_SIDEREAL,

        /**
         * Sidereal day: 1 day (sidereal) = 86164.09 s.
         */
        DAY_SIDEREAL,

        /**
         * Sidereal hour: 1 hour (sidereal) = 3590.1704166667 s.
         */
        HOUR_SIDEREAL,

        /**
         * Sidereal minute: 1 minute (sidereal) = 59.8361736111 s.
         */
        MINUTE_SIDEREAL,

        /**
         * Sidereal second: 1 second (sidereal) = 0.9972695602 s.
         */
        SECOND_SIDEREAL,

        /**
         * Fortnight: 1 fortnight = 1209600 s.
         */
        FORTNIGHT,

        /**
         * Septennial: 1 septennial = 220752000 s.
         */
        SEPTENNIAL,

        /**
         * Octennial: 1 octennial = 252288000 s.
         */
        OCTENNIAL,

        /**
         * Novennial: 1 novennial = 283824000 s.
         */
        NOVENNIAL,

        /**
         * Quindecennial: 1 quindecennial = 473040000 s.
         */
        QUINDECENNIAL,

        /**
         * Quinquennial: 1 quinquennial = 157680000 s.
         */
        QUINQUENNIAL,

        /**
         * Planck time: 1 Planck time = 5.39056e-44 s.
         */
        PLANCK_TIME

    }

    /**
     * Force unit identifiers (base unit: newton).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Force implements Unit {

        /**
         * Newton (base unit).
         */
        NEWTON,

        /**
         * Kilonewton: 1 kN = 1000 N.
         */
        KILONEWTON,

        /**
         * Exanewton: 1 EN = 1.0E+18 N.
         */
        EXANEWTON,

        /**
         * Petanewton: 1 PT = 1.0E+15 N.
         */
        PETANEWTON,

        /**
         * Teranewton: 1 TN = 1,000,000,000,000 N.
         */
        TERANEWTON,

        /**
         * Giganewton: 1 GN = 1,000,000,000 N.
         */
        GIGANEWTON,

        /**
         * Meganewton: 1 MN = 1,000,000 N.
         */
        MEGANEWTON,

        /**
         * Hectonewton: 1 hN = 100 N.
         */
        HECTONEWTON,

        /**
         * Dekanewton: 1 daN = 10 N.
         */
        DEKANEWTON,

        /**
         * Decinewton: 1 dN = 0.1 N.
         */
        DECINEWTON,

        /**
         * Centinewton: 1 cN = 0.01 N.
         */
        CENTINEWTON,

        /**
         * Millinewton: 1 mN = 0.001 N.
         */
        MILLINEWTON,

        /**
         * Micronewton: 1 µN = 1.0E-6 N.
         */
        MICRONEWTON,

        /**
         * Nanonewton: 1 nN = 1.0E-9 N.
         */
        NANONEWTON,

        /**
         * Piconewton: 1 pN = 1.0E-12 N.
         */
        PICONEWTON,

        /**
         * Femtonewton: 1 fN = 1.0E-15 N.
         */
        FEMTONEWTON,

        /**
         * Attonewton: 1 aN = 1.0E-18 N.
         */
        ATTONEWTON,

        /**
         * Dyne: 1 dyn = 1.0E-5 N.
         */
        DYNE,

        /**
         * Gram-force: 1 gf = 0.00980665 N.
         */
        GRAM_FORCE,

        /**
         * Kilogram-force: 1 kgf = 9.80665 N.
         */
        KILOGRAM_FORCE,

        /**
         * Ton-force (metric): 1 tf = 9806.65 N.
         */
        TON_FORCE_METRIC,

        /**
         * Ton-force (short): 1 ton-force (short) = 8896.443230521 N.
         */
        TON_FORCE_SHORT,

        /**
         * Ton-force (long) / tonf (UK): 1 tonf (UK) = 9964.0164181707 N.
         */
        TON_FORCE_LONG,

        /**
         * Kip-force: 1 kipf = 4448.2216152548 N.
         */
        KIP_FORCE,

        /**
         * Kilopound-force: 1 kipf = 4448.2216152548 N.
         */
        KILOPOUND_FORCE,

        /**
         * Pound-force: 1 lbf = 4.4482216153 N.
         */
        POUND_FORCE,

        /**
         * Ounce-force: 1 ozf = 0.278013851 N.
         */
        OUNCE_FORCE,

        /**
         * Poundal: 1 pdl = 0.1382549544 N.
         */
        POUNDAL,

        /**
         * Pound foot per square second: 1 (pound foot/square second) = 0.1382549544 N.
         */
        POUND_FOOT_PER_SQUARE_SECOND,

        /**
         * Joule per meter: 1 J/m = 1 N.
         */
        JOULE_PER_METER,

        /**
         * Joule per centimeter: 1 J/cm = 0.01 N.
         */
        JOULE_PER_CENTIMETER,

        /**
         * Pond: 1 p = 0.00980665 N.
         */
        POND,

        /**
         * Kilopond: 1 kp = 9.80665 N.
         */
        KILOPOND

    }

    /**
     * Speed unit identifiers (base unit: meter per second).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum Speed implements Unit {

        /**
         * Meter per second (base unit).
         */
        METER_PER_SECOND,

        /**
         * Kilometer per hour: 1 km/h = 0.2777777778 m/s.
         */
        KILOMETER_PER_HOUR,

        /**
         * Mile per hour: 1 mi/h = 0.44704 m/s.
         */
        MILE_PER_HOUR,

        /**
         * Meter per hour: 1 m/h = 0.0002777778 m/s.
         */
        METER_PER_HOUR,

        /**
         * Meter per minute: 1 m/min = 0.0166666667 m/s.
         */
        METER_PER_MINUTE,

        /**
         * Kilometer per minute: 1 km/min = 16.6666666667 m/s.
         */
        KILOMETER_PER_MINUTE,

        /**
         * Kilometer per second: 1 km/s = 1000 m/s.
         */
        KILOMETER_PER_SECOND,

        /**
         * Centimeter per hour: 1 cm/h = 2.7777777777778E-6 m/s.
         */
        CENTIMETER_PER_HOUR,

        /**
         * Centimeter per minute: 1 cm/min = 0.0001666667 m/s.
         */
        CENTIMETER_PER_MINUTE,

        /**
         * Centimeter per second: 1 cm/s = 0.01 m/s.
         */
        CENTIMETER_PER_SECOND,

        /**
         * Millimeter per hour: 1 mm/h = 2.7777777777778E-7 m/s.
         */
        MILLIMETER_PER_HOUR,

        /**
         * Millimeter per minute: 1 mm/min = 1.66667E-5 m/s.
         */
        MILLIMETER_PER_MINUTE,

        /**
         * Millimeter per second: 1 mm/s = 0.001 m/s.
         */
        MILLIMETER_PER_SECOND,

        /**
         * Foot per hour: 1 ft/h = 8.46667E-5 m/s.
         */
        FOOT_PER_HOUR,

        /**
         * Foot per minute: 1 ft/min = 0.00508 m/s.
         */
        FOOT_PER_MINUTE,

        /**
         * Foot per second: 1 ft/s = 0.3048 m/s.
         */
        FOOT_PER_SECOND,

        /**
         * Yard per hour: 1 yd/h = 0.000254 m/s.
         */
        YARD_PER_HOUR,

        /**
         * Yard per minute: 1 yd/min = 0.01524 m/s.
         */
        YARD_PER_MINUTE,

        /**
         * Yard per second: 1 yd/s = 0.9144 m/s.
         */
        YARD_PER_SECOND,

        /**
         * Mile per minute: 1 mi/min = 26.8224 m/s.
         */
        MILE_PER_MINUTE,

        /**
         * Mile per second: 1 mi/s = 1609.344 m/s.
         */
        MILE_PER_SECOND,

        /**
         * Knot: 1 kn = 0.5144444444 m/s.
         */
        KNOT,

        /**
         * Knot (UK): 1 kt (UK) = 0.5147733333 m/s.
         */
        KNOT_UK,

        /**
         * Speed of light in vacuum: 1 c = 299792458 m/s.
         */
        SPEED_OF_LIGHT_VACUUM,

        /**
         * Cosmic velocity (first): 1 v1 = 7899.9999999999 m/s.
         */
        COSMIC_VELOCITY_FIRST,

        /**
         * Cosmic velocity (second): 1 v2 = 11200 m/s.
         */
        COSMIC_VELOCITY_SECOND,

        /**
         * Cosmic velocity (third): 1 v3 = 16670 m/s.
         */
        COSMIC_VELOCITY_THIRD,

        /**
         * Earth's velocity: 1 v_earth = 29765 m/s.
         */
        EARTHS_VELOCITY,

        /**
         * Velocity of sound in pure water: 1 v_sound_water = 1482.6999999998 m/s.
         */
        SPEED_OF_SOUND_PURE_WATER,

        /**
         * Velocity of sound in sea water (20°C, 10 meter deep): 1 v_sound_sea = 1521.6 m/s.
         */
        SPEED_OF_SOUND_SEA_WATER_20C_10M,

        /**
         * Mach (20°C, 1 atm): 1 Ma(20°C) = 343.6 m/s.
         */
        MACH_20C_1ATM,

        /**
         * Mach (SI standard): 1 Ma(SI) = 295.0464000003 m/s.
         */
        MACH_SI_STANDARD

    }

    /**
     * FuelConsumption unit identifiers (base unit: meter per liter).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum FuelConsumption implements Unit {

        /**
         * Meter per liter (base unit).
         */
        METER_PER_LITER,

        /**
         * Exameter per liter: 1 Em/L = 1.0E+18 m/L.
         */
        EXAMETER_PER_LITER,

        /**
         * Petameter per liter: 1 Pm/L = 1.0E+15 m/L.
         */
        PETAMETER_PER_LITER,

        /**
         * Terameter per liter: 1 Tm/L = 1000000000000 m/L.
         */
        TERAMETER_PER_LITER,

        /**
         * Gigameter per liter: 1 Gm/L = 1000000000 m/L.
         */
        GIGAMETER_PER_LITER,

        /**
         * Megameter per liter: 1 Mm/L = 1000000 m/L.
         */
        MEGAMETER_PER_LITER,

        /**
         * Kilometer per liter: 1 km/L = 1000 m/L.
         */
        KILOMETER_PER_LITER,

        /**
         * Hectometer per liter: 1 hm/L = 100 m/L.
         */
        HECTOMETER_PER_LITER,

        /**
         * Dekameter per liter: 1 dam/L = 10 m/L.
         */
        DEKAMETER_PER_LITER,

        /**
         * Centimeter per liter: 1 cm/L = 0.01 m/L.
         */
        CENTIMETER_PER_LITER,

        /**
         * Mile (US) per liter: 1 mi/L = 1609.344 m/L.
         */
        MILE_US_PER_LITER,

        /**
         * Nautical mile per liter: 1 n.mile/L = 1853.24496 m/L.
         */
        NAUTICAL_MILE_PER_LITER,

        /**
         * Nautical mile per gallon (US): 1 n.mile/gal(US) = 489.5755247 m/L.
         */
        NAUTICAL_MILE_PER_GALLON_US,

        /**
         * Kilometer per gallon (US): 1 km/gal(US) = 264.1720524 m/L.
         */
        KILOMETER_PER_GALLON_US,

        /**
         * Meter per gallon (US): 1 m/gal(US) = 0.2641720524 m/L.
         */
        METER_PER_GALLON_US,

        /**
         * Meter per gallon (UK): 1 m/gal(UK) = 0.2199687986 m/L.
         */
        METER_PER_GALLON_UK,

        /**
         * Mile per gallon (US): 1 mpg(US) = 425.1437075 m/L.
         */
        MILE_PER_GALLON_US,

        /**
         * Mile per gallon (UK): 1 mpg(UK) = 354.00619 m/L.
         */
        MILE_PER_GALLON_UK,

        /**
         * Meter per cubic meter: 1 m/m^3 = 0.001 m/L.
         */
        METER_PER_CUBIC_METER,

        /**
         * Meter per cubic centimeter: 1 m/cm^3 = 1000 m/L.
         */
        METER_PER_CUBIC_CENTIMETER,

        /**
         * Meter per cubic yard: 1 m/yd^3 = 0.0013079506 m/L.
         */
        METER_PER_CUBIC_YARD,

        /**
         * Meter per cubic foot: 1 m/ft^3 = 0.0353146667 m/L.
         */
        METER_PER_CUBIC_FOOT,

        /**
         * Meter per cubic inch: 1 m/in^3 = 61.02374409 m/L.
         */
        METER_PER_CUBIC_INCH,

        /**
         * Meter per quart (US): 1 m/qt(US) = 1.056688209 m/L.
         */
        METER_PER_QUART_US,

        /**
         * Meter per quart (UK): 1 m/qt(UK) = 0.8798751948 m/L.
         */
        METER_PER_QUART_UK,

        /**
         * Meter per pint (US): 1 m/pt(US) = 2.113376419 m/L.
         */
        METER_PER_PINT_US,

        /**
         * Meter per pint (UK): 1 m/pt(UK) = 1.759750389 m/L.
         */
        METER_PER_PINT_UK,

        /**
         * Meter per cup (US): 1 m/cup(US) = 4.226752838 m/L.
         */
        METER_PER_CUP_US,

        /**
         * Meter per cup (UK): 1 m/cup(UK) = 3.519500777 m/L.
         */
        METER_PER_CUP_UK,

        /**
         * Meter per fluid ounce (US): 1 m/fl oz(US) = 33.8140227 m/L.
         */
        METER_PER_FLUID_OUNCE_US,

        /**
         * Meter per fluid ounce (UK): 1 m/fl oz(UK) = 35.19500777 m/L.
         */
        METER_PER_FLUID_OUNCE_UK,

        /**
         * Liter per meter: 1 L/m = 1 m/L.
         */
        LITER_PER_METER,

        /**
         * Liter per 100 kilometer: 1 L/100 km = 100000 m/L.
         */
        LITER_PER_100_KILOMETER,

        /**
         * Gallon (US) per mile: 1 gal(US)/mi = 425.1437074976 m/L.
         */
        GALLON_US_PER_MILE,

        /**
         * Gallon (US) per 100 miles: 1 gal(US)/100 mi = 42514.370749763 m/L.
         */
        GALLON_US_PER_100_MILES,

        /**
         * Gallon (UK) per mile: 1 gal(UK)/mi = 354.0061899559 m/L.
         */
        GALLON_UK_PER_MILE,

        /**
         * Gallon (UK) per 100 miles: 1 gal(UK)/100 mi = 35400.618995592 m/L.
         */
        GALLON_UK_PER_100_MILES

    }

    /**
     * DataStorage unit identifiers (base unit: bit).
     *
     * <p>
     * This enum intentionally contains no metadata fields. Metadata is stored in the internal registry.
     * </p>
     */
    enum DataStorage implements Unit {

        /**
         * Base unit: 1 bit (b).
         */
        BIT,

        /**
         * 1 nibble = 4 bits (b).
         */
        NIBBLE,

        /**
         * 1 byte = 8 bits (B).
         */
        BYTE,

        /**
         * 1 character = 8 bits.
         */
        CHARACTER,

        /**
         * 1 word = 16 bits.
         */
        WORD,

        /**
         * 1 MAPM-word = 32 bits.
         */
        MAPM_WORD,

        /**
         * 1 quadruple-word = 64 bits.
         */
        QUADRUPLE_WORD,

        /**
         * 1 block = 4096 bits.
         */
        BLOCK,

        /**
         * 1 kilobit (kb) = 1024 bits (binary).
         */
        KILOBIT,

        /**
         * 1 kilobyte (kB) = 8192 bits = 1024 bytes (binary).
         */
        KILOBYTE,

        /**
         * 1 kilobyte (10^3 bytes) = 8000 bits = 1000 bytes (decimal).
         */
        KILOBYTE_DECIMAL,

        /**
         * 1 megabit (Mb) = 1,048,576 bits (binary).
         */
        MEGABIT,

        /**
         * 1 megabyte (MB) = 8,388,608 bits = 1,048,576 bytes (binary).
         */
        MEGABYTE,

        /**
         * 1 megabyte (10^6 bytes) = 8,000,000 bits = 1,000,000 bytes (decimal).
         */
        MEGABYTE_DECIMAL,

        /**
         * 1 gigabit (Gb) = 1,073,741,824 bits (binary).
         */
        GIGABIT,

        /**
         * 1 gigabyte (GB) = 8,589,934,592 bits = 1,073,741,824 bytes (binary).
         */
        GIGABYTE,

        /**
         * 1 gigabyte (10^9 bytes) = 8,000,000,000 bits = 1,000,000,000 bytes (decimal).
         */
        GIGABYTE_DECIMAL,

        /**
         * 1 terabit (Tb) = 1,099,511,627,776 bits (binary).
         */
        TERABIT,

        /**
         * 1 terabyte (TB) = 8,796,093,022,208 bits = 1,099,511,627,776 bytes (binary).
         */
        TERABYTE,

        /**
         * 1 terabyte (10^12 bytes) = 8,000,000,000,000 bits = 1,000,000,000,000 bytes (decimal).
         */
        TERABYTE_DECIMAL,

        /**
         * 1 petabit (Pb) = 1,125,899,906,842,624 bits (binary).
         */
        PETABIT,

        /**
         * 1 petabyte (PB) = 9,007,199,254,740,992 bits = 1,125,899,906,842,624 bytes (binary).
         */
        PETABYTE,

        /**
         * 1 petabyte (10^15 bytes) = 8.0E+15 bits = 1,000,000,000,000,000 bytes (decimal).
         */
        PETABYTE_DECIMAL,

        /**
         * 1 exabit (Eb) = 1,152,921,504,606,846,976 bits (binary).
         */
        EXABIT,

        /**
         * 1 exabyte (EB) = 9,223,372,036,854,775,808 bits = 1,152,921,504,606,846,976 bytes (binary).
         */
        EXABYTE,

        /**
         * 1 exabyte (10^18 bytes) = 8.0E+18 bits = 1,000,000,000,000,000,000 bytes (decimal).
         */
        EXABYTE_DECIMAL,

        /**
         * Floppy disk (3.5", DD) = 5,830,656 bits.
         */
        FLOPPY_35_DD,

        /**
         * Floppy disk (3.5", HD) = 11,661,312 bits.
         */
        FLOPPY_35_HD,

        /**
         * Floppy disk (3.5", ED) = 23,322,624 bits.
         */
        FLOPPY_35_ED,

        /**
         * Floppy disk (5.25", DD) = 2,915,328 bits.
         */
        FLOPPY_525_DD,

        /**
         * Floppy disk (5.25", HD) = 9,711,616 bits.
         */
        FLOPPY_525_HD,

        /**
         * Zip 100 = 803,454,976 bits.
         */
        ZIP_100,

        /**
         * Zip 250 = 2,008,637,440 bits.
         */
        ZIP_250,

        /**
         * Jaz 1GB = 8,589,934,592 bits.
         */
        JAZ_1GB,

        /**
         * Jaz 2GB = 17,179,869,184 bits.
         */
        JAZ_2GB,

        /**
         * CD (74 minute) = 5,448,466,432 bits.
         */
        CD_74_MIN,

        /**
         * CD (80 minute) = 5,890,233,976 bits.
         */
        CD_80_MIN,

        /**
         * DVD (1 layer, 1 side) = 40,372,692,582.4 bits.
         */
        DVD_1L_1S,

        /**
         * DVD (2 layer, 1 side) = 73,014,444,032 bits.
         */
        DVD_2L_1S,

        /**
         * DVD (1 layer, 2 side) = 80,745,385,164.8 bits.
         */
        DVD_1L_2S,

        /**
         * DVD (2 layer, 2 side) = 146,028,888,064 bits.
         */
        DVD_2L_2S

    }

}