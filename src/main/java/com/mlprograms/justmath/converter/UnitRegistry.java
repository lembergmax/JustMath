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

import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.*;

/**
 * Internal single source of truth for all built-in unit definitions.
 *
 * <p>
 * This registry is intentionally package-private to keep the public API surface minimal and stable.
 * Public access is provided via {@link UnitElements}.
 * </p>
 *
 * <h2>Design goals</h2>
 * <ul>
 *   <li><strong>Enums are identifiers only</strong>: {@link Unit.Length} and {@link Unit.Mass} do not carry metadata.</li>
 *   <li><strong>Single place to edit</strong>: add/remove units by editing one list ({@link #BUILT_IN}).</li>
 *   <li><strong>Deterministic and validated</strong>: unit symbols are unique and group mappings are consistent.</li>
 *   <li><strong>Thread-safe</strong>: all registries are immutable after class initialization.</li>
 * </ul>
 *
 * <h2>How to add a new unit</h2>
 * <p>
 * Add exactly one entry to {@link #BUILT_IN}. That is the only place you should need to touch.
 * </p>
 *
 * <pre>
 * define(Unit.Length.MEGAMETER, "Megameter", "Mm", "1000000", "0")
 * </pre>
 *
 * <p>
 * The {@code scaleToBase} and {@code offsetToBase} parameters define the mapping into the group base unit:
 * </p>
 *
 * <pre>
 * base = value * scaleToBase + offsetToBase
 * </pre>
 *
 * <p>
 * For purely linear conversions, use {@code offsetToBase = "0"}.
 * </p>
 */
@UtilityClass
class UnitRegistry {

    /**
     * Declarative list of all built-in units and their definitions.
     *
     * <p>
     * This list is the <strong>only</strong> place you need to edit to add or modify units.
     * The rest of the registry is derived from this list and validated at startup.
     * </p>
     * <a href="https://www.unitconverters.net/length-converter.html">Get the scaleToBase from this website</a>
     */
    private static final List<UnitSpec> BUILT_IN = List.of(
            // =========================
            // LENGTH (base: meter)
            // =========================
            define(Unit.Length.EXAMETER, "Exameter", "Em", "1000000000000000000"),
            define(Unit.Length.PETAMETER, "Petameter", "Pm", "1000000000000000"),
            define(Unit.Length.TERAMETER, "Terameter", "Tm", "1000000000000"),
            define(Unit.Length.GIGAMETER, "Gigameter", "Gm", "1000000000"),
            define(Unit.Length.MEGAMETER, "Megameter", "Mm", "1000000"),
            define(Unit.Length.KILOMETER, "Kilometer", "km", "1000"),
            define(Unit.Length.HECTOMETER, "Hectometer", "hm", "100"),
            define(Unit.Length.DEKAMETER, "Dekameter", "dam", "10"),
            define(Unit.Length.METER, "Meter", "m"),
            define(Unit.Length.DECIMETER, "Decimeter", "dm", "0.1"),
            define(Unit.Length.CENTIMETER, "Centimeter", "cm", "0.01"),
            define(Unit.Length.MILLIMETER, "Millimeter", "mm", "0.001"),
            define(Unit.Length.MICROMETER, "Micrometer", "um", "0.000001"),
            define(Unit.Length.MICRON, "Micron", "µm", "0.000001"),
            define(Unit.Length.NANOMETER, "Nanometer", "nm", "9.999999999E-10"),
            define(Unit.Length.ANGSTROM, "Angstrom", "Å", "9.999999999E-11"),
            define(Unit.Length.PICOMETER, "Picometer", "pm", "1.E-12"),
            define(Unit.Length.FEMTOMETER, "Femtometer", "fm", "9.999999999E-16"),
            define(Unit.Length.ATTOMETER, "Attometer", "am", "1.E-18"),

            define(Unit.Length.PLANCK_LENGTH, "Planck Length", "lP", "1.616049999E-35"),
            define(Unit.Length.ELECTRON_RADIUS, "Electron Radius", "re", "2.81794092E-15"),
            define(Unit.Length.BOHR_RADIUS, "Bohr Radius", "a0", "5.29177249E-11"),
            define(Unit.Length.X_UNIT, "X Unit", "xu", "1.002079999E-13"),
            define(Unit.Length.FERMI, "Fermi", "frm", "9.999999999E-16"),

            define(Unit.Length.SUN_RADIUS, "Sun Radius", "Rsun", "696000000"),
            define(Unit.Length.EARTH_EQUATORIAL_RADIUS, "Earth Equatorial Radius", "R_earth_eq", "6378160"),
            define(Unit.Length.EARTH_POLAR_RADIUS, "Earth Polar Radius", "R_earth_p", "6356777"),
            define(Unit.Length.ASTRONOMICAL_UNIT, "Astronomical Unit", "au", "149597870691"),
            define(Unit.Length.EARTH_DISTANCE_FROM_SUN, "Earth Distance from Sun", "AU", "149600000000"),
            define(Unit.Length.KILOPARSEC, "Kiloparsec", "kpc", "30856775812799586000"),
            define(Unit.Length.MEGAPARSEC, "Megaparsec", "Mpc", "3.085677581E+22"),
            define(Unit.Length.PARSEC, "Parsec", "pc", "30856775812799588"),
            define(Unit.Length.LIGHT_YEAR, "Light Year", "ly", "9460730472580044"),

            define(Unit.Length.LEAGUE, "League", "lea", "4828.032"),
            define(Unit.Length.NAUTICAL_LEAGUE_INTERNATIONAL, "Nautical League", "NL", "55565556"),
            define(Unit.Length.NAUTICAL_LEAGUE_UK, "Nautical League (UK)", "NL (UK)", "5559.552"),
            define(Unit.Length.NAUTICAL_MILE, "Nautical Mile", "nmi", "1852"),
            define(Unit.Length.NAUTICAL_MILE_UK, "Nautical Mile (UK)", "nmi (UK)", "1853.184"),

            define(Unit.Length.MILE, "Mile", "mi", "1609.344"),
            define(Unit.Length.MILE_ROMAN, "Roman Mile", "m.p.", "1479.804"),
            define(Unit.Length.KILOYARD, "Kiloyard", "kyd", "914.4"),
            define(Unit.Length.FURLONG, "Furlong", "fur", "201.168"),
            define(Unit.Length.CHAIN, "Chain", "ch", "20.1168"),
            define(Unit.Length.ROPE, "Rope", "rope", "6.096"),
            define(Unit.Length.ROD, "Rod", "rod", "5.0292"),
            define(Unit.Length.FATHOM, "Fathom", "ftm", "1.8288"),
            define(Unit.Length.FAMN, "Famn", "famn", "1.7813333333"),
            define(Unit.Length.ELL, "Ell", "ell", "1.143"),
            define(Unit.Length.ALN, "Aln", "aln", "0.5937777778"),
            define(Unit.Length.CUBIT_UK, "Cubit (UK)", "cubit", "0.4572"),
            define(Unit.Length.SPAN_CLOTH, "Span (cloth)", "span", "0.2286"),
            define(Unit.Length.LINK, "Link", "li", "0.201168"),
            define(Unit.Length.FINGER_CLOTH, "Finger (cloth)", "finger", "0.1143"),
            define(Unit.Length.HAND, "Hand", "hand", "0.1016"),
            define(Unit.Length.HANDBREADTH, "Handbreadth", "hb", "0.0762"),
            define(Unit.Length.NAIL_COTH, "Nail (cloth)", "nail", "0.05715"),
            define(Unit.Length.FINGERBREADTH, "Fingerbreadth", "fb", "0.01905"),
            define(Unit.Length.BARLEYCORN, "Barleycorn", "barleycorn", "0.0084666667"),
            define(Unit.Length.YARD, "Yard", "yd", "0.9144"),
            define(Unit.Length.FEET, "Foot", "ft", "0.3048"),
            define(Unit.Length.INCH, "Inch", "in", "0.0254"),
            define(Unit.Length.CENTIINCH, "Centiinch", "cin", "0.000254"),
            define(Unit.Length.CALIBER, "Caliber", "cl", "0.000254"),
            define(Unit.Length.MIL, "Mil", "mil", "0.0000254"),
            define(Unit.Length.MICROINCH, "Microinch", "µin", "2.54E-8"),

            define(Unit.Length.ARPENT, "Arpent", "arp", "58.5216"),
            define(Unit.Length.KEN, "Ken", "ken", "2.11836"),

            define(Unit.Length.PIXEL, "Pixel", "px", "0.0002645833"),
            define(Unit.Length.POINT, "Point", "pt", "0.0003527778"),
            define(Unit.Length.PICA, "Pica", "pica", "0.0042333333"),
            define(Unit.Length.EM, "Em", "em", "0.0042175176"),
            define(Unit.Length.TWIP, "Twip", "twip", "0.0000176389"),

            // =========================
            // AREA (base: square meter)
            // =========================
            define(Unit.Area.SQUARE_KILOMETER, "Square Kilometer", "km^2", "1000000"),
            define(Unit.Area.SQUARE_HECTOMETER, "Square Hectometer", "hm^2", "10000"),
            define(Unit.Area.SQUARE_DEKAMETER, "Square Dekameter", "dam^2", "100"),
            define(Unit.Area.SQUARE_METER, "Square Meter", "m^2"),
            define(Unit.Area.SQUARE_DECIMETER, "Square Decimeter", "dm^2", "0.01"),
            define(Unit.Area.SQUARE_CENTIMETER, "Square Centimeter", "cm^2", "0.0001"),
            define(Unit.Area.SQUARE_MILLIMETER, "Square Millimeter", "mm^2", "0.000001"),
            define(Unit.Area.SQUARE_MICROMETER, "Square Micrometer", "µm^2", "1.E-12"),
            define(Unit.Area.SQUARE_NANOMETER, "Square Nanometer", "nm^2", "1.E-18"),

            define(Unit.Area.HECTARE, "Hectare", "ha", "10000"),
            define(Unit.Area.ARE, "Are", "a", "100"),

            define(Unit.Area.BARN, "Barn", "b", "1.E-28"),
            define(Unit.Area.ELECTRON_CROSS_SECTION, "Thomson Cross Section", "σT", "6.652461599E-29"),

            define(Unit.Area.TOWNSHIP, "Township", "twp", "93239571.972"),
            define(Unit.Area.SECTION, "Section", "sec", "2589988.1103"),
            define(Unit.Area.HOMESTEAD, "Homestead", "hstd", "647497.02758"),

            define(Unit.Area.SQUARE_MILE, "Square Mile", "mi^2", "6.4516E-10"),
            define(Unit.Area.ACRE, "Acre", "ac", "4046.8564224"),
            define(Unit.Area.ROOD, "Rood", "rood", "1011.7141056"),

            define(Unit.Area.SQUARE_CHAIN, "Square Chain", "ch^2", "404.68564224"),
            define(Unit.Area.SQUARE_ROD, "Square Rod", "rd^2", "25.29285264"),
            define(Unit.Area.SQUARE_POLE, "Square Pole", "pole^2", "25.29285264"),
            define(Unit.Area.SQUARE_ROPE, "Square Rope", "rope^2", "37.161216"),

            define(Unit.Area.SQUARE_YARD, "Square Yard", "yd^2", "0.83612736"),
            define(Unit.Area.SQUARE_FOOT, "Square Foot", "ft^2", "0.09290304"),
            define(Unit.Area.SQUARE_INCH, "Square Inch", "in^2", "0.00064516"),

            define(Unit.Area.ARPENT, "Arpent", "arp_area", "3418.8929237"),
            define(Unit.Area.CUERDA, "Cuerda", "cda", "3930.395625"),
            define(Unit.Area.PLAZA, "Plaza", "plz", "6400"),

            // =========================
            // VOLUME (base: cubic meter)
            // =========================
            define(Unit.Volume.CUBIC_KILOMETER, "Cubic Kilometer", "km^3", "1000000000"),
            define(Unit.Volume.CUBIC_METER, "Cubic Meter", "m^3"),
            define(Unit.Volume.CUBIC_DECIMETER, "Cubic Decimeter", "dm^3", "0.001"),
            define(Unit.Volume.CUBIC_CENTIMETER, "Cubic Centimeter", "cm^3", "0.000001"),
            define(Unit.Volume.CUBIC_MILLIMETER, "Cubic Millimeter", "mm^3", "1.E-9"),

            define(Unit.Volume.EXALITER, "Exaliter", "EL", "1000000000000000"),
            define(Unit.Volume.PETALITER, "Petaliter", "PL", "1000000000000"),
            define(Unit.Volume.TERALITER, "Teraliter", "TL", "1000000000"),
            define(Unit.Volume.GIGALITER, "Gigaliter", "GL", "1000000"),
            define(Unit.Volume.MEGALITER, "Megaliter", "ML", "1000"),
            define(Unit.Volume.KILOLITER, "Kiloliter", "kL", "1"),
            define(Unit.Volume.HECTOLITER, "Hectoliter", "hL", "0.1"),
            define(Unit.Volume.DEKALITER, "Dekaliter", "daL", "0.01"),
            define(Unit.Volume.LITER, "Liter", "L", "0.001"),
            define(Unit.Volume.DECILITER, "Deciliter", "dL", "0.0001"),
            define(Unit.Volume.CENTILITER, "Centiliter", "cL", "0.00001"),
            define(Unit.Volume.MILLILITER, "Milliliter", "mL", "0.000001"),
            define(Unit.Volume.MICROLITER, "Microliter", "uL", "1.E-9"),
            define(Unit.Volume.NANOLITER, "Nanoliter", "nL", "1.E-12"),
            define(Unit.Volume.PICOLITER, "Picoliter", "pL", "9.999999999E-16"),
            define(Unit.Volume.FEMTOLITER, "Femtoliter", "fL", "1.E-18"),
            define(Unit.Volume.ATTOLITER, "Attoliter", "aL", "1.E-21"),

            define(Unit.Volume.METRIC_CUP, "Cup (Metric)", "cup_metric", "0.00025"),
            define(Unit.Volume.METRIC_TABLESPOON, "Tablespoon (Metric)", "tbsp_metric", "0.000015"),
            define(Unit.Volume.METRIC_TEASPOON, "Teaspoon (Metric)", "tsp_metric", "0.000005"),

            define(Unit.Volume.US_GALLON, "Gallon (United States)", "gal_us", "0.0037854118"),
            define(Unit.Volume.US_QUART, "Quart (United States)", "qt_us", "0.0009463529"),
            define(Unit.Volume.US_PINT, "Pint (United States)", "pt_us", "0.0004731765"),
            define(Unit.Volume.US_CUP, "Cup (United States)", "cup_us", "0.0002365882"),
            define(Unit.Volume.US_FLUID_OUNCE, "Fluid Ounce (United States)", "floz_us", "0.0000295735"),
            define(Unit.Volume.US_TABLESPOON, "Tablespoon (United States)", "tbsp_us", "0.0000147868"),
            define(Unit.Volume.US_DESSERTSPOON, "Dessertspoon (United States)", "dsp_us", "0.0000098578"),
            define(Unit.Volume.US_TEASPOON, "Teaspoon (United States)", "tsp_us", "0.0000049289"),
            define(Unit.Volume.US_GILL, "Gill (United States)", "gi_us", "0.0001182941"),
            define(Unit.Volume.US_MINIM, "Minim (United States)", "minim_us", "6.161151992E-8"),
            define(Unit.Volume.US_BARREL, "Barrel (United States)", "bbl_us", "0.1192404712"),

            define(Unit.Volume.IMPERIAL_GALLON, "Gallon (United Kingdom)", "gal_uk", "0.00454609"),
            define(Unit.Volume.IMPERIAL_QUART, "Quart (United Kingdom)", "qt_uk", "0.0011365225"),
            define(Unit.Volume.IMPERIAL_PINT, "Pint (United Kingdom)", "pt_uk", "0.0005682613"),
            define(Unit.Volume.IMPERIAL_CUP, "Cup (United Kingdom)", "cup_uk", "0.0002841306"),
            define(Unit.Volume.IMPERIAL_FLUID_OUNCE, "Fluid Ounce (United Kingdom)", "floz_uk", "0.0000284131"),
            define(Unit.Volume.IMPERIAL_TABLESPOON, "Tablespoon (United Kingdom)", "tbsp_uk", "0.0000177582"),
            define(Unit.Volume.IMPERIAL_DESSERTSPOON, "Dessertspoon (United Kingdom)", "dsp_uk", "0.0000118388"),
            define(Unit.Volume.IMPERIAL_TEASPOON, "Teaspoon (United Kingdom)", "tsp_uk", "0.0000059194"),
            define(Unit.Volume.IMPERIAL_GILL, "Gill (United Kingdom)", "gi_uk", "0.0001420653"),
            define(Unit.Volume.IMPERIAL_MINIM, "Minim (United Kingdom)", "minim_uk", "5.91938802E-8"),
            define(Unit.Volume.IMPERIAL_BARREL, "Barrel (United Kingdom)", "bbl_uk", "0.16365924"),

            define(Unit.Volume.CUBIC_MILE, "Cubic Mile", "mi^3", "4168181825.4"),
            define(Unit.Volume.CUBIC_YARD, "Cubic Yard", "yd^3", "0.764554858"),
            define(Unit.Volume.CUBIC_FOOT, "Cubic Foot", "ft^3", "0.0283168466"),
            define(Unit.Volume.CUBIC_INCH, "Cubic Inch", "in^3", "0.0000163871"),

            define(Unit.Volume.HUNDRED_CUBIC_FOOT, "Hundred Cubic Foot", "hundred_cubic_foot", "2.8316846592"),
            define(Unit.Volume.TON_REGISTER, "Ton Register", "ton_reg", "2.8316846592"),
            define(Unit.Volume.ACRE_FOOT, "Acre-Foot", "ac*ft", "1233.4818375"),
            define(Unit.Volume.ACRE_INCH, "Acre-Inch", "ac*in", "102.79015313"),
            define(Unit.Volume.BOARD_FOOT, "Board Foot", "board_foot", "0.0023597372"),
            define(Unit.Volume.STERE, "Stere", "stere", "1"),
            define(Unit.Volume.DEKASTERE, "Dekastere", "dekastere", "10"),
            define(Unit.Volume.DECISTERE, "Decistere", "decistere", "0.1"),
            define(Unit.Volume.CORD, "Cord", "cord", "3.6245563638"),

            define(Unit.Volume.DROP, "Drop", "drop", "5.E-8"),
            define(Unit.Volume.OIL_BARREL, "Barrel (Oil)", "bbl_oil", "0.1589872949"),
            define(Unit.Volume.TUN, "Tun", "tun", "0.9539237696"),
            define(Unit.Volume.HOGSHEAD, "Hogshead", "hogshead", "0.2384809424"),
            define(Unit.Volume.DRAM, "Dram", "dr", "0.0000036967"),
            define(Unit.Volume.SPANISH_TAZA, "Taza (Spanish)", "taza", "0.0002365882"),

            define(Unit.Volume.BIBLICAL_COR, "Cor (Biblical)", "cor_biblical", "0.22"),
            define(Unit.Volume.BIBLICAL_HOMER, "Homer (Biblical)", "homer_biblical", "0.22"),
            define(Unit.Volume.BIBLICAL_BATH, "Bath (Biblical)", "bath_biblical", "0.022"),
            define(Unit.Volume.BIBLICAL_HIN, "Hin (Biblical)", "hin_biblical", "0.0036666667"),
            define(Unit.Volume.BIBLICAL_CAB, "Cab (Biblical)", "cab_biblical", "0.0012222222"),
            define(Unit.Volume.BIBLICAL_LOG, "Log (Biblical)", "log_biblical", "0.0003055556"),

            define(Unit.Volume.EARTH_VOLUME, "Earth's Volume", "earth_volume", "1.082999999E+21"),

            // =========================
            // MASS (base: kilogram)
            // =========================
            define(Unit.Mass.EXAGRAM, "Exagram", "exagram", "1.0E+15"),
            define(Unit.Mass.PETAGRAM, "Petagram", "petagram", "1000000000000"),
            define(Unit.Mass.TERAGRAM, "Teragram", "teragram", "1000000000"),
            define(Unit.Mass.GIGAGRAM, "Gigagram", "gigagram", "1000000"),
            define(Unit.Mass.MEGAGRAM, "Megagram", "megagram", "1000"),
            define(Unit.Mass.KILOTON, "Kiloton (Metric)", "kiloton_metric", "1000000"),
            define(Unit.Mass.QUINTAL_METRIC, "Quintal (Metric)", "quintal_metric", "100"),
            define(Unit.Mass.TON, "Tonne (Metric)", "tonne_metric", "1000"),

            define(Unit.Mass.KILOGRAM, "Kilogram", "kilogram", ""),
            define(Unit.Mass.HECTOGRAM, "Hectogram", "hectogram", "0.1"),
            define(Unit.Mass.DEKAGRAM, "Dekagram", "dekagram", "0.01"),
            define(Unit.Mass.GRAM, "Gram", "gram", "0.001"),
            define(Unit.Mass.DECIGRAM, "Decigram", "decigram", "0.0001"),
            define(Unit.Mass.CENTIGRAM, "Centigram", "centigram", "1.0E-5"),
            define(Unit.Mass.MILLIGRAM, "Milligram", "milligram", "1.0E-6"),
            define(Unit.Mass.MICROGRAM, "Microgram", "microgram", "1.0E-9"),
            define(Unit.Mass.GAMMA, "Gamma", "gamma", "1.0E-9"),
            define(Unit.Mass.NANOGRAM, "Nanogram", "nanogram", "1.0E-12"),
            define(Unit.Mass.PICOGRAM, "Picogram", "picogram", "1.0E-15"),
            define(Unit.Mass.FEMTOGRAM, "Femtogram", "femtogram", "1.0E-18"),
            define(Unit.Mass.ATTOGRAM, "Attogram", "attogram", "1.0E-21"),

            define(Unit.Mass.CARRAT, "Carat", "carat", "0.0002"),
            define(Unit.Mass.GRAIN, "Grain", "grain", "6.47989E-5"),
            define(Unit.Mass.PENNYWEIGHT, "Pennyweight", "pennyweight", "0.0015551738"),
            define(Unit.Mass.SCRUPLE_APOTHECARY, "Scruple (Apothecary)", "scruple_apothecary", "0.0012959782"),
            define(Unit.Mass.POUND_TROY_APOTHECARY, "Pound (Troy or Apothecary)", "pound_troy_apothecary", "0.3732417216"),

            define(Unit.Mass.LONG_TON, "Ton (Long)", "ton_long", "1016.0469088"),
            define(Unit.Mass.SHORT_TON, "Ton (Short)", "ton_short", "907.18474"),
            define(Unit.Mass.POUND, "Pound", "pound", "0.45359237"),
            define(Unit.Mass.OUNCE, "Ounce", "ounce", "0.0283495231"),

            define(Unit.Mass.HUNDREDWEIGHT_UNITED_STATES, "Hundredweight (United States)", "hundredweight_united_states", "45.359237"),
            define(Unit.Mass.HUNDREDWEIGHT_UNITED_KINGDOM, "Hundredweight (United Kingdom)", "hundredweight_united_kingdom", "50.80234544"),
            define(Unit.Mass.QUARTER_UNITED_STATES, "Quarter (United States)", "quarter_united_states", "11.33980925"),
            define(Unit.Mass.QUARTER_UNITED_KINGDOM, "Quarter (United Kingdom)", "quarter_united_kingdom", "12.70058636"),
            define(Unit.Mass.STONE_UNITED_STATES, "Stone (United States)", "stone_united_states", "5.669904625"),
            define(Unit.Mass.STONE_UNITED_KINGDOM, "Stone (United Kingdom)", "stone_united_kingdom", "6.35029318"),

            define(Unit.Mass.KILOGRAM_FORCE_SECOND_SQUARED_PER_METER, "Kilogram-Force Square Second per Meter", "kilogram_force_square_second_per_meter", "9.80665"),
            define(Unit.Mass.POUND_FORCE_SECOND_SQUARED_PER_FOOT, "Pound-Force Square Second per Foot", "pound_force_square_second_per_foot", "14.5939029372"),
            define(Unit.Mass.SLUG, "Slug", "slug", "14.5939029372"),
            define(Unit.Mass.POUNDAL, "Poundal", "poundal", "0.0140867196"),
            define(Unit.Mass.KILOPOUND, "Kilopound", "kilopound", "453.59237"),

            define(Unit.Mass.ASSAY_TON_UNITED_STATES, "Ton (Assay) (United States)", "ton_assay_united_states", "0.02916667"),
            define(Unit.Mass.ASSAY_TON_UNITED_KINGDOM, "Ton (Assay) (United Kingdom)", "ton_assay_united_kingdom", "0.0326666667"),

            define(Unit.Mass.ATOMIC_MASS_UNIT, "Atomic Mass Unit", "atomic_mass_unit", "1.6605402E-27"),
            define(Unit.Mass.DALTON, "Dalton", "dalton", "1.6605300000013E-27"),

            define(Unit.Mass.PLANCK_MASS, "Planck Mass", "planck_mass", "2.17671E-8"),
            define(Unit.Mass.ELECTRON_REST_MASS, "Electron Mass (Rest)", "electron_rest_mass", "9.1093897E-31"),
            define(Unit.Mass.MUON_MASS, "Muon Mass", "muon_mass", "1.8835327E-28"),
            define(Unit.Mass.PROTON_MASS, "Proton Mass", "proton_mass", "1.6726231E-27"),
            define(Unit.Mass.NEUTRON_MASS, "Neutron Mass", "neutron_mass", "1.6749286E-27"),
            define(Unit.Mass.DEUTERON_MASS, "Deuteron Mass", "deuteron_mass", "3.343586E-27"),

            define(Unit.Mass.EARTH_MASS, "Earth's Mass", "earth_mass", "5.9760000000002E+24"),
            define(Unit.Mass.SUN_MASS, "Sun's Mass", "sun_mass", "2.0E+30"),

            define(Unit.Mass.BIBLICAL_HEBREW_TALENT, "Talent (Biblical Hebrew)", "talent_biblical_hebrew", "34.2"),
            define(Unit.Mass.BIBLICAL_HEBREW_MINA, "Mina (Biblical Hebrew)", "mina_biblical_hebrew", "0.57"),
            define(Unit.Mass.BIBLICAL_HEBREW_SHEKEL, "Shekel (Biblical Hebrew)", "shekel_biblical_hebrew", "0.0114"),
            define(Unit.Mass.BIBLICAL_HEBREW_BEKAN, "Bekan (Biblical Hebrew)", "bekan_biblical_hebrew", "0.0057"),
            define(Unit.Mass.BIBLICAL_HEBREW_GERAH, "Gerah (Biblical Hebrew)", "gerah_biblical_hebrew", "0.00057"),

            define(Unit.Mass.BIBLICAL_GREEK_TALENT, "Talent (Biblical Greek)", "talent_biblical_greek", "20.4"),
            define(Unit.Mass.BIBLICAL_GREEK_MINA, "Mina (Biblical Greek)", "mina_biblical_greek", "0.34"),
            define(Unit.Mass.BIBLICAL_GREEK_TETRADRACHMA, "Tetradrachma (Biblical Greek)", "tetradrachma_biblical_greek", "0.0136"),
            define(Unit.Mass.BIBLICAL_GREEK_DIDRACHMA, "Didrachma (Biblical Greek)", "didrachma_biblical_greek", "0.0068"),
            define(Unit.Mass.BIBLICAL_GREEK_DRACHMA, "Drachma (Biblical Greek)", "drachma_biblical_greek", "0.0034"),

            define(Unit.Mass.BIBLICAL_ROMAN_DENARIUS, "Denarius (Biblical Roman)", "denarius_biblical_roman", "0.00385"),
            define(Unit.Mass.BIBLICAL_ROMAN_ASSARION, "Assarion (Biblical Roman)", "assarion_biblical_roman", "0.000240625"),
            define(Unit.Mass.BIBLICAL_ROMAN_QUADRANS, "Quadrans (Biblical Roman)", "quadrans_biblical_roman", "6.01563E-5"),
            define(Unit.Mass.BIBLICAL_ROMAN_LEPTON, "Lepton (Biblical Roman)", "lepton_biblical_roman", "3.00781E-5"),

            // =========================
            // TEMPERATURE (base: celsius)
            // =========================
            define(Unit.Temperature.KELVIN, "Kelvin", "K", "1", "-273.15"),
            define(Unit.Temperature.CELSIUS, "Celsius", "°C"),
            define(Unit.Temperature.FAHRENHEIT, "Fahrenheit", "°F", "1", "17.777777778"),

            // =========================
            // PRESSURE (base: pascal)
            // =========================
            define(Unit.Pressure.EXAPASCAL, "Exapascal", "exapascal", "1.0E+18"),
            define(Unit.Pressure.PETAPASCAL, "Petapascal", "petapascal", "1.0E+15"),
            define(Unit.Pressure.TERAPASCAL, "Terapascal", "terapascal", "1000000000000"),
            define(Unit.Pressure.GIGAPASCAL, "Gigapascal", "gigapascal", "1000000000"),
            define(Unit.Pressure.MEGAPASCAL, "Megapascal", "megapascal", "1000000"),
            define(Unit.Pressure.KILOPASCAL, "Kilopascal", "kilopascal", "1000"),
            define(Unit.Pressure.HECTOPASCAL, "Hectopascal", "hectopascal", "100"),
            define(Unit.Pressure.DEKAPASCAL, "Dekapascal", "dekapascal", "10"),

            define(Unit.Pressure.PASCAL, "Pascal", "pascal"),

            define(Unit.Pressure.DECIPASCAL, "Decipascal", "decipascal", "0.1"),
            define(Unit.Pressure.CENTIPASCAL, "Centipascal", "centipascal", "0.01"),
            define(Unit.Pressure.MILLIPASCAL, "Millipascal", "millipascal", "0.001"),
            define(Unit.Pressure.MICROPASCAL, "Micropascal", "micropascal", "1.0E-6"),
            define(Unit.Pressure.NANOPASCAL, "Nanopascal", "nanopascal", "1.0E-9"),
            define(Unit.Pressure.PICOPASCAL, "Picopascal", "picopascal", "1.0E-12"),
            define(Unit.Pressure.FEMTOPASCAL, "Femtopascal", "femtopascal", "1.0E-15"),
            define(Unit.Pressure.ATTOPASCAL, "Attopascal", "attopascal", "1.0E-18"),

            define(Unit.Pressure.BAR, "Bar", "bar", "100000"),
            define(Unit.Pressure.MILLIBAR, "Millibar", "millibar", "100"),
            define(Unit.Pressure.MICROBAR, "Microbar", "microbar", "0.1"),
            define(Unit.Pressure.STANDARD_ATMOSPHERE, "Standard Atmosphere", "standard_atmosphere", "101325"),
            define(Unit.Pressure.TECHNICAL_ATMOSPHERE, "Atmosphere (Technical)", "technical_atmosphere", "98066.500000003"),
            define(Unit.Pressure.PSI, "Pounds per Square Inch", "psi", "6894.7572931783"),
            define(Unit.Pressure.KSI, "Kips per Square Inch", "ksi", "6894757.2931783"),
            define(Unit.Pressure.TORR, "Torr", "torr", "133.3223684211"),

            define(Unit.Pressure.NEWTON_PER_SQUARE_METER, "Newton per Square Meter", "newton_per_square_meter", "1"),
            define(Unit.Pressure.NEWTON_PER_SQUARE_CENTIMETER, "Newton per Square Centimeter", "newton_per_square_centimeter", "10000"),
            define(Unit.Pressure.NEWTON_PER_SQUARE_MILLIMETER, "Newton per Square Millimeter", "newton_per_square_millimeter", "1000000"),
            define(Unit.Pressure.KILONEWTON_PER_SQUARE_METER, "Kilonewton per Square Meter", "kilonewton_per_square_meter", "1000"),
            define(Unit.Pressure.DYNE_PER_SQUARE_CENTIMETER, "Dyne per Square Centimeter", "dyne_per_square_centimeter", "0.1"),

            define(Unit.Pressure.KILOGRAM_FORCE_PER_SQUARE_METER, "Kilogram-Force per Square Meter", "kilogram_force_per_square_meter", "9.80665"),
            define(Unit.Pressure.KILOGRAM_FORCE_PER_SQUARE_CENTIMETER, "Kilogram-Force per Square Centimeter", "kilogram_force_per_square_centimeter", "98066.5"),
            define(Unit.Pressure.KILOGRAM_FORCE_PER_SQUARE_MILLIMETER, "Kilogram-Force per Square Millimeter", "kilogram_force_per_square_millimeter", "9806650"),
            define(Unit.Pressure.GRAM_FORCE_PER_SQUARE_CENTIMETER, "Gram-Force per Square Centimeter", "gram_force_per_square_centimeter", "98.0665"),

            define(Unit.Pressure.SHORT_TON_FORCE_PER_SQUARE_FOOT, "Ton-Force (Short) per Square Foot", "short_ton_force_per_square_foot", "95760.517960678"),
            define(Unit.Pressure.SHORT_TON_FORCE_PER_SQUARE_INCH, "Ton-Force (Short) per Square Inch", "short_ton_force_per_square_inch", "13789514.586338"),
            define(Unit.Pressure.LONG_TON_FORCE_PER_SQUARE_FOOT, "Ton-Force (Long) per Square Foot", "long_ton_force_per_square_foot", "107251.78011595"),
            define(Unit.Pressure.LONG_TON_FORCE_PER_SQUARE_INCH, "Ton-Force (Long) per Square Inch", "long_ton_force_per_square_inch", "15444256.336697"),

            define(Unit.Pressure.KIP_FORCE_PER_SQUARE_INCH, "Kip-Force per Square Inch", "kip_force_per_square_inch", "6894757.2931783"),
            define(Unit.Pressure.POUND_FORCE_PER_SQUARE_FOOT, "Pound-Force per Square Foot", "pound_force_per_square_foot", "47.8802589804"),
            define(Unit.Pressure.POUND_FORCE_PER_SQUARE_INCH, "Pound-Force per Square Inch", "pound_force_per_square_inch", "6894.7572931783"),
            define(Unit.Pressure.POUNDAL_PER_SQUARE_FOOT, "Poundal per Square Foot", "poundal_per_square_foot", "1.4881639436"),

            define(Unit.Pressure.CENTIMETER_OF_MERCURY_0C, "Centimeter of Mercury (0°C)", "centimeter_of_mercury_0c", "1333.22"),
            define(Unit.Pressure.MILLIMETER_OF_MERCURY_0C, "Millimeter of Mercury (0°C)", "millimeter_of_mercury_0c", "133.322"),
            define(Unit.Pressure.INCH_OF_MERCURY_32F, "Inch of Mercury (32°F)", "inch_of_mercury_32f", "3386.38"),
            define(Unit.Pressure.INCH_OF_MERCURY_60F, "Inch of Mercury (60°F)", "inch_of_mercury_60f", "3376.85"),

            define(Unit.Pressure.CENTIMETER_OF_WATER_4C, "Centimeter of Water (4°C)", "centimeter_of_water_4c", "98.0638"),
            define(Unit.Pressure.MILLIMETER_OF_WATER_4C, "Millimeter of Water (4°C)", "millimeter_of_water_4c", "9.80638"),
            define(Unit.Pressure.INCH_OF_WATER_4C, "Inch of Water (4°C)", "inch_of_water_4c", "249.082"),
            define(Unit.Pressure.FOOT_OF_WATER_4C, "Foot of Water (4°C)", "foot_of_water_4c", "2988.98"),
            define(Unit.Pressure.INCH_OF_WATER_60F, "Inch of Water (60°F)", "inch_of_water_60f", "248.843"),
            define(Unit.Pressure.FOOT_OF_WATER_60F, "Foot of Water (60°F)", "foot_of_water_60f", "2986.116"),

            // =========================
            // ENERGY (base: joule)
            // =========================
            define(Unit.Energy.GIGAJOULE, "Gigajoule", "gigajoule", "1000000000"),
            define(Unit.Energy.MEGAJOULE, "Megajoule", "megajoule", "1000000"),
            define(Unit.Energy.KILOJOULE, "Kilojoule", "kilojoule", "1000"),

            define(Unit.Energy.JOULE, "Joule", "joule"),

            define(Unit.Energy.MILLIJOULE, "Millijoule", "millijoule", "0.001"),
            define(Unit.Energy.MICROJOULE, "Microjoule", "microjoule", "1.0E-6"),
            define(Unit.Energy.NANOJOULE, "Nanojoule", "nanojoule", "1.0E-9"),
            define(Unit.Energy.ATTOJOULE, "Attojoule", "attojoule", "1.0E-18"),

            define(Unit.Energy.GIGAWATT_HOUR, "Gigawatt-Hour", "gigawatt_hour", "3600000000000"),
            define(Unit.Energy.MEGAWATT_HOUR, "Megawatt-Hour", "megawatt_hour", "3600000000"),
            define(Unit.Energy.KILOWATT_HOUR, "Kilowatt-Hour", "kilowatt_hour", "3600000"),
            define(Unit.Energy.WATT_HOUR, "Watt-Hour", "watt_hour", "3600"),
            define(Unit.Energy.KILOWATT_SECOND, "Kilowatt-Second", "kilowatt_second", "1000"),
            define(Unit.Energy.WATT_SECOND, "Watt-Second", "watt_second", "1"),

            define(Unit.Energy.CALORIE_NUTRITIONAL, "Calorie (Nutritional)", "calorie_nutritional", "4186.8"),
            define(Unit.Energy.KILOCALORIE_IT, "Kilocalorie (IT)", "kilocalorie_it", "4186.8"),
            define(Unit.Energy.KILOCALORIE_TH, "Kilocalorie (th)", "kilocalorie_th", "4184"),
            define(Unit.Energy.CALORIE_IT, "Calorie (IT)", "calorie_it", "4.1868"),
            define(Unit.Energy.CALORIE_TH, "Calorie (th)", "calorie_th", "4.184"),

            define(Unit.Energy.BTU_IT, "Btu (IT)", "btu_it", "1055.05585262"),
            define(Unit.Energy.BTU_TH, "Btu (th)", "btu_th", "1054.3499999744"),
            define(Unit.Energy.MEGA_BTU_IT, "Mega Btu (IT)", "mega_btu_it", "1055055852.62"),

            define(Unit.Energy.THERM, "Therm", "therm", "105505600"),
            define(Unit.Energy.THERM_EC, "Therm (EC)", "therm_ec", "105505600"),
            define(Unit.Energy.THERM_US, "Therm (US)", "therm_us", "105480400"),

            define(Unit.Energy.TON_HOUR_REFRIGERATION, "Ton-Hour (Refrigeration)", "ton_hour_refrigeration", "12660670.23144"),

            define(Unit.Energy.HORSEPOWER_METRIC_HOUR, "Horsepower (Metric) Hour", "horsepower_metric_hour", "2647795.5"),
            define(Unit.Energy.HORSEPOWER_HOUR, "Horsepower Hour", "horsepower_hour", "2684519.5368856"),

            define(Unit.Energy.MEGAELECTRON_VOLT, "Megaelectron-Volt", "megaelectron_volt", "1.6021766339999E-13"),
            define(Unit.Energy.KILOELECTRON_VOLT, "Kiloelectron-Volt", "kiloelectron_volt", "1.6021766339999E-16"),
            define(Unit.Energy.ELECTRON_VOLT, "Electron-Volt", "electron_volt", "1.6021766339999E-19"),

            define(Unit.Energy.HARTREE_ENERGY, "Hartree Energy", "hartree_energy", "4.3597482E-18"),
            define(Unit.Energy.RYDBERG_CONSTANT, "Rydberg Constant", "rydberg_constant", "2.1798741E-18"),

            define(Unit.Energy.ERG, "Erg", "erg", "1.0E-7"),

            define(Unit.Energy.NEWTON_METER, "Newton Meter", "newton_meter", "1"),
            define(Unit.Energy.DYNE_CENTIMETER, "Dyne Centimeter", "dyne_centimeter", "1.0E-7"),

            define(Unit.Energy.GRAM_FORCE_METER, "Gram-Force Meter", "gram_force_meter", "0.00980665"),
            define(Unit.Energy.GRAM_FORCE_CENTIMETER, "Gram-Force Centimeter", "gram_force_centimeter", "9.80665E-5"),
            define(Unit.Energy.KILOGRAM_FORCE_CENTIMETER, "Kilogram-Force Centimeter", "kilogram_force_centimeter", "0.0980665"),
            define(Unit.Energy.KILOGRAM_FORCE_METER, "Kilogram-Force Meter", "kilogram_force_meter", "9.8066499997"),
            define(Unit.Energy.KILOPOND_METER, "Kilopond Meter", "kilopond_meter", "9.8066499997"),

            define(Unit.Energy.POUND_FORCE_FOOT, "Pound-Force Foot", "pound_force_foot", "1.3558179483"),
            define(Unit.Energy.POUND_FORCE_INCH, "Pound-Force Inch", "pound_force_inch", "0.112984829"),
            define(Unit.Energy.OUNCE_FORCE_INCH, "Ounce-Force Inch", "ounce_force_inch", "0.0070615518"),

            define(Unit.Energy.FOOT_POUND, "Foot-Pound", "foot_pound", "1.3558179483"),
            define(Unit.Energy.INCH_POUND, "Inch-Pound", "inch_pound", "0.112984829"),
            define(Unit.Energy.INCH_OUNCE, "Inch-Ounce", "inch_ounce", "0.0070615518"),

            define(Unit.Energy.POUNDAL_FOOT, "Poundal Foot", "poundal_foot", "0.04214011"),

            define(Unit.Energy.GIGATON_TNT, "Gigaton (TNT Equivalent)", "gigaton_tnt", "4.184E+18"),
            define(Unit.Energy.MEGATON_TNT, "Megaton (TNT Equivalent)", "megaton_tnt", "4.184E+15"),
            define(Unit.Energy.KILOTON_TNT, "Kiloton (TNT Equivalent)", "kiloton_tnt", "4184000000000"),
            define(Unit.Energy.TON_TNT, "Ton (Explosives)", "ton_tnt", "4184000000"),

            define(Unit.Energy.FUEL_OIL_EQUIVALENT_KILOLITER, "Fuel Oil Equivalent at Kiloliter", "fuel_oil_equivalent_kiloliter", "40197627984.822"),
            define(Unit.Energy.FUEL_OIL_EQUIVALENT_US_BARREL, "Fuel Oil Equivalent at Barrel (United States)", "fuel_oil_equivalent_us_barrel", "6383087908.3509"),

            // =========================
            // POWER (base: watt)
            // =========================
            define(Unit.Power.EXAWATT, "Exawatt", "exawatt", "1.0E+18"),
            define(Unit.Power.PETAWATT, "Petawatt", "petawatt", "1.0E+15"),
            define(Unit.Power.TERAWATT, "Terawatt", "terawatt", "1000000000000"),
            define(Unit.Power.GIGAWATT, "Gigawatt", "gigawatt", "1000000000"),
            define(Unit.Power.MEGAWATT, "Megawatt", "megawatt", "1000000"),
            define(Unit.Power.KILOWATT, "Kilowatt", "kilowatt", "1000"),
            define(Unit.Power.HECTOWATT, "Hectowatt", "hectowatt", "100"),
            define(Unit.Power.DEKAWATT, "Dekawatt", "dekawatt", "10"),

            define(Unit.Power.WATT, "Watt", "watt"),

            define(Unit.Power.DECIWATT, "Deciwatt", "deciwatt", "0.1"),
            define(Unit.Power.CENTIWATT, "Centiwatt", "centiwatt", "0.01"),
            define(Unit.Power.MILLIWATT, "Milliwatt", "milliwatt", "0.001"),
            define(Unit.Power.MICROWATT, "Microwatt", "microwatt", "1.0E-6"),
            define(Unit.Power.NANOWATT, "Nanowatt", "nanowatt", "1.0E-9"),
            define(Unit.Power.PICOWATT, "Picowatt", "picowatt", "1.0E-12"),
            define(Unit.Power.FEMTOWATT, "Femtowatt", "femtowatt", "1.0E-15"),
            define(Unit.Power.ATTOWATT, "Attowatt", "attowatt", "1.0E-18"),

            define(Unit.Power.HORSEPOWER, "Horsepower", "horsepower", "745.6998715823"),
            define(Unit.Power.HORSEPOWER_MECHANICAL_550_FTLBF_PER_S, "Horsepower (550 ft*lbf/s)", "horsepower_mechanical_550_ftlbf_per_s", "745.6998715823"),
            define(Unit.Power.HORSEPOWER_METRIC, "Horsepower (Metric)", "horsepower_metric", "735.49875"),
            define(Unit.Power.HORSEPOWER_BOILER, "Horsepower (Boiler)", "horsepower_boiler", "9809.5000000002"),
            define(Unit.Power.HORSEPOWER_ELECTRIC, "Horsepower (Electric)", "horsepower_electric", "746"),
            define(Unit.Power.HORSEPOWER_WATER, "Horsepower (Water)", "horsepower_water", "746.043"),
            define(Unit.Power.PFERDESTAERKE, "Pferdestarke", "pferdestarke", "735.49875"),

            define(Unit.Power.BTU_IT_PER_HOUR, "Btu (IT) per Hour", "btu_it_per_hour", "0.2930710702"),
            define(Unit.Power.BTU_IT_PER_MINUTE, "Btu (IT) per Minute", "btu_it_per_minute", "17.5842642103"),
            define(Unit.Power.BTU_IT_PER_SECOND, "Btu (IT) per Second", "btu_it_per_second", "1055.05585262"),
            define(Unit.Power.BTU_TH_PER_HOUR, "Btu (th) per Hour", "btu_th_per_hour", "0.292875"),
            define(Unit.Power.BTU_TH_PER_MINUTE, "Btu (th) per Minute", "btu_th_per_minute", "17.5724999996"),
            define(Unit.Power.BTU_TH_PER_SECOND, "Btu (th) per Second", "btu_th_per_second", "1054.3499999744"),

            define(Unit.Power.MEGA_BTU_IT_PER_HOUR, "Mega Btu (IT) per Hour", "mega_btu_it_per_hour", "293071.07017222"),
            define(Unit.Power.MBH, "MBH", "mbh", "293.0710701722"),
            define(Unit.Power.TON_REFRIGERATION, "Ton (Refrigeration)", "ton_refrigeration", "3516.8528420667"),

            define(Unit.Power.KILOCALORIE_IT_PER_HOUR, "Kilocalorie (IT) per Hour", "kilocalorie_it_per_hour", "1.163"),
            define(Unit.Power.KILOCALORIE_IT_PER_MINUTE, "Kilocalorie (IT) per Minute", "kilocalorie_it_per_minute", "69.78"),
            define(Unit.Power.KILOCALORIE_IT_PER_SECOND, "Kilocalorie (IT) per Second", "kilocalorie_it_per_second", "4186.8"),
            define(Unit.Power.KILOCALORIE_TH_PER_HOUR, "Kilocalorie (th) per Hour", "kilocalorie_th_per_hour", "1.1622222222"),
            define(Unit.Power.KILOCALORIE_TH_PER_MINUTE, "Kilocalorie (th) per Minute", "kilocalorie_th_per_minute", "69.7333333333"),
            define(Unit.Power.KILOCALORIE_TH_PER_SECOND, "Kilocalorie (th) per Second", "kilocalorie_th_per_second", "4184"),

            define(Unit.Power.CALORIE_IT_PER_HOUR, "Calorie (IT) per Hour", "calorie_it_per_hour", "0.001163"),
            define(Unit.Power.CALORIE_IT_PER_MINUTE, "Calorie (IT) per Minute", "calorie_it_per_minute", "0.06978"),
            define(Unit.Power.CALORIE_IT_PER_SECOND, "Calorie (IT) per Second", "calorie_it_per_second", "4.1868"),
            define(Unit.Power.CALORIE_TH_PER_HOUR, "Calorie (th) per Hour", "calorie_th_per_hour", "0.0011622222"),
            define(Unit.Power.CALORIE_TH_PER_MINUTE, "Calorie (th) per Minute", "calorie_th_per_minute", "0.0697333333"),
            define(Unit.Power.CALORIE_TH_PER_SECOND, "Calorie (th) per Second", "calorie_th_per_second", "4.184"),

            define(Unit.Power.FOOT_POUND_FORCE_PER_HOUR, "Foot Pound-Force per Hour", "foot_pound_force_per_hour", "0.0003766161"),
            define(Unit.Power.FOOT_POUND_FORCE_PER_MINUTE, "Foot Pound-Force per Minute", "foot_pound_force_per_minute", "0.0225969658"),
            define(Unit.Power.FOOT_POUND_FORCE_PER_SECOND, "Foot Pound-Force per Second", "foot_pound_force_per_second", "1.3558179483"),
            define(Unit.Power.POUND_FOOT_PER_HOUR, "Pound-Foot per Hour", "pound_foot_per_hour", "0.0003766161"),
            define(Unit.Power.POUND_FOOT_PER_MINUTE, "Pound-Foot per Minute", "pound_foot_per_minute", "0.0225969658"),
            define(Unit.Power.POUND_FOOT_PER_SECOND, "Pound-Foot per Second", "pound_foot_per_second", "1.3558179483"),

            define(Unit.Power.ERG_PER_SECOND, "Erg per Second", "erg_per_second", "1.0E-7"),

            define(Unit.Power.KILOVOLT_AMPERE, "Kilovolt-Ampere", "kilovolt_ampere", "1000"),
            define(Unit.Power.VOLT_AMPERE, "Volt-Ampere", "volt_ampere", "1"),

            define(Unit.Power.NEWTON_METER_PER_SECOND, "Newton Meter per Second", "newton_meter_per_second", "1"),

            define(Unit.Power.JOULE_PER_SECOND, "Joule per Second", "joule_per_second", "1"),
            define(Unit.Power.EXAJOULE_PER_SECOND, "Exajoule per Second", "exajoule_per_second", "1.0E+18"),
            define(Unit.Power.PETAJOULE_PER_SECOND, "Petajoule per Second", "petajoule_per_second", "1.0E+15"),
            define(Unit.Power.TERAJOULE_PER_SECOND, "Terajoule per Second", "terajoule_per_second", "1000000000000"),
            define(Unit.Power.GIGAJOULE_PER_SECOND, "Gigajoule per Second", "gigajoule_per_second", "1000000000"),
            define(Unit.Power.MEGAJOULE_PER_SECOND, "Megajoule per Second", "megajoule_per_second", "1000000"),
            define(Unit.Power.KILOJOULE_PER_SECOND, "Kilojoule per Second", "kilojoule_per_second", "1000"),
            define(Unit.Power.HECTOJOULE_PER_SECOND, "Hectojoule per Second", "hectojoule_per_second", "100"),
            define(Unit.Power.DEKAJOULE_PER_SECOND, "Dekajoule per Second", "dekajoule_per_second", "10"),
            define(Unit.Power.DECIJOULE_PER_SECOND, "Decijoule per Second", "decijoule_per_second", "0.1"),
            define(Unit.Power.CENTIJOULE_PER_SECOND, "Centijoule per Second", "centijoule_per_second", "0.01"),
            define(Unit.Power.MILLIJOULE_PER_SECOND, "Millijoule per Second", "millijoule_per_second", "0.001"),
            define(Unit.Power.MICROJOULE_PER_SECOND, "Microjoule per Second", "microjoule_per_second", "1.0E-6"),
            define(Unit.Power.NANOJOULE_PER_SECOND, "Nanojoule per Second", "nanojoule_per_second", "1.0E-9"),
            define(Unit.Power.PICOJOULE_PER_SECOND, "Picojoule per Second", "picojoule_per_second", "1.0E-12"),
            define(Unit.Power.FEMTOJOULE_PER_SECOND, "Femtojoule per Second", "femtojoule_per_second", "1.0E-15"),
            define(Unit.Power.ATTOJOULE_PER_SECOND, "Attojoule per Second", "attojoule_per_second", "1.0E-18"),

            define(Unit.Power.JOULE_PER_HOUR, "Joule per Hour", "joule_per_hour", "0.0002777778"),
            define(Unit.Power.JOULE_PER_MINUTE, "Joule per Minute", "joule_per_minute", "0.0166666667"),
            define(Unit.Power.KILOJOULE_PER_HOUR, "Kilojoule per Hour", "kilojoule_per_hour", "0.2777777778"),
            define(Unit.Power.KILOJOULE_PER_MINUTE, "Kilojoule per Minute", "kilojoule_per_minute", "16.6666666667")
    );

    /**
     * Map from unit identifier to its immutable {@link UnitDefinition}.
     *
     * <p>
     * This is the canonical lookup structure used by the public facade.
     * </p>
     */
    private static final Map<Unit, UnitDefinition> BY_UNIT;

    /**
     * Map from unit symbol to unit identifier.
     *
     * <p>
     * Symbols are treated as case-sensitive because some real-world symbols are case-sensitive
     * (e.g., {@code "K"} vs {@code "k"}). Validation ensures every symbol is unique.
     * </p>
     */
    private static final Map<String, Unit> BY_SYMBOL;

    /**
     * Map from unit group type (e.g., {@code Unit.Length.class}) to an immutable list of units
     * belonging to that group.
     *
     * <p>
     * This mapping enables compatibility checks and group-wise listing without a separate
     * {@code UnitCategory} enum.
     * </p>
     */
    private static final Map<Class<? extends Unit>, List<Unit>> BY_GROUP;

    static {
        final Map<Unit, UnitDefinition> byUnit = new LinkedHashMap<>();
        final Map<String, Unit> bySymbol = new HashMap<>();
        final Map<Class<? extends Unit>, List<Unit>> byGroup = new LinkedHashMap<>();

        for (final UnitSpec spec : BUILT_IN) {
            final Unit unit = spec.unit();
            final UnitDefinition definition = spec.definition();

            final UnitDefinition previousDef = byUnit.put(unit, definition);
            if (previousDef != null) {
                throw new IllegalStateException("Duplicate unit definition detected for: " + unit);
            }

            final Unit previousUnit = bySymbol.put(definition.symbol(), unit);
            if (previousUnit != null) {
                throw new IllegalStateException(
                        "Duplicate unit symbol detected: '" + definition.symbol() + "' used by " + previousUnit + " and " + unit
                );
            }

            final Class<? extends Unit> groupType = groupTypeOf(unit);
            byGroup.computeIfAbsent(groupType, ignored -> new ArrayList<>()).add(unit);
        }

        BY_UNIT = Map.copyOf(byUnit);
        BY_SYMBOL = Map.copyOf(bySymbol);

        final Map<Class<? extends Unit>, List<Unit>> immutableGroupMap = new LinkedHashMap<>();
        for (final Map.Entry<Class<? extends Unit>, List<Unit>> entry : byGroup.entrySet()) {
            immutableGroupMap.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        BY_GROUP = Map.copyOf(immutableGroupMap);
    }

    /**
     * Returns the immutable {@link UnitDefinition} for a given unit identifier or throws
     * if no definition exists.
     *
     * @param unit the unit identifier; must not be {@code null}
     * @return immutable unit definition; never {@code null}
     * @throws IllegalStateException if no definition exists for {@code unit}
     */
    static UnitDefinition requireDefinition(@NonNull final Unit unit) {
        final UnitDefinition definition = BY_UNIT.get(unit);
        if (definition == null) {
            throw new IllegalStateException("No unit definition found for unit: " + unit);
        }
        return definition;
    }

    /**
     * Finds a unit identifier by its symbol.
     *
     * @param symbol the symbol to look up (e.g., {@code "km"}, {@code "kg"}); may be {@code null}
     * @return optional unit identifier; empty if {@code symbol} is {@code null} or unknown
     */
    static Optional<Unit> findBySymbol(final String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_SYMBOL.get(symbol));
    }

    /**
     * Returns all units of the given group (e.g., {@code Unit.Length.class}).
     *
     * @param groupType the group type; must not be {@code null}
     * @return immutable list of units; never {@code null}
     */
    static List<Unit> unitsOfGroup(@NonNull final Class<? extends Unit> groupType) {
        return BY_GROUP.getOrDefault(groupType, List.of());
    }

    /**
     * Returns all built-in units in deterministic registry order.
     *
     * <p>
     * The order is the order of {@link #BUILT_IN}, which is intended to be stable and human-controlled.
     * </p>
     *
     * @return immutable list of all units; never {@code null}
     */
    static List<Unit> allUnits() {
        return List.copyOf(BY_UNIT.keySet());
    }

    /**
     * Returns the display name of a unit.
     *
     * @param unit the unit identifier; must not be {@code null}
     * @return display name; never {@code null}
     */
    static String displayName(@NonNull final Unit unit) {
        return requireDefinition(unit).displayName();
    }

    /**
     * Returns the symbol of a unit.
     *
     * @param unit the unit identifier; must not be {@code null}
     * @return symbol; never {@code null}
     */
    static String symbol(@NonNull final Unit unit) {
        return requireDefinition(unit).symbol();
    }

    /**
     * Checks whether two units belong to the same group type.
     *
     * <p>
     * This replaces the need for a separate {@code UnitCategory} enum. Group identity is derived
     * from the unit's declaring enum type (e.g., {@code Unit.Length} or {@code Unit.Mass}).
     * </p>
     *
     * @param left  the left unit; must not be {@code null}
     * @param right the right unit; must not be {@code null}
     * @return {@code true} if both units are in the same group; otherwise {@code false}
     */
    static boolean areCompatible(@NonNull final Unit left, @NonNull final Unit right) {
        return groupTypeOf(left).equals(groupTypeOf(right));
    }

    /**
     * Creates one declarative built-in definition entry.
     *
     * <p>
     * The conversion is defined by the affine mapping into the group base unit:
     * </p>
     *
     * <pre>
     * base = value * scaleToBase + offsetToBase
     * </pre>
     *
     * @param unit        the unit identifier; must not be {@code null}
     * @param displayName human-readable display name; must not be {@code null}
     * @param symbol      unit symbol; must not be {@code null}
     * @return immutable unit spec entry; never {@code null}
     */
    private static UnitSpec define(
            @NonNull final Unit unit,
            @NonNull final String displayName,
            @NonNull final String symbol
    ) {
        return define(unit, displayName, symbol, "1", "0");
    }

    /**
     * Creates one declarative built-in definition entry.
     *
     * <p>
     * The conversion is defined by the affine mapping into the group base unit:
     * </p>
     *
     * <pre>
     * base = value * scaleToBase + offsetToBase
     * </pre>
     *
     * @param unit        the unit identifier; must not be {@code null}
     * @param displayName human-readable display name; must not be {@code null}
     * @param symbol      unit symbol; must not be {@code null}
     * @param scaleToBase multiplicative factor into base unit; must not be {@code null}
     * @return immutable unit spec entry; never {@code null}
     */
    private static UnitSpec define(
            @NonNull final Unit unit,
            @NonNull final String displayName,
            @NonNull final String symbol,
            @NonNull final String scaleToBase
    ) {
        return define(unit, displayName, symbol, scaleToBase, "0");
    }

    /**
     * Creates one declarative built-in definition entry.
     *
     * <p>
     * The conversion is defined by the affine mapping into the group base unit:
     * </p>
     *
     * <pre>
     * base = value * scaleToBase + offsetToBase
     * </pre>
     *
     * @param unit         the unit identifier; must not be {@code null}
     * @param displayName  human-readable display name; must not be {@code null}
     * @param symbol       unit symbol; must not be {@code null}
     * @param scaleToBase  multiplicative factor into base unit; must not be {@code null}
     * @param offsetToBase additive offset into base unit; must not be {@code null}
     * @return immutable unit spec entry; never {@code null}
     */
    private static UnitSpec define(
            @NonNull final Unit unit,
            @NonNull final String displayName,
            @NonNull final String symbol,
            @NonNull final String scaleToBase,
            @NonNull final String offsetToBase
    ) {
        final BigNumber scale = new BigNumber(scaleToBase);
        final BigNumber offset = new BigNumber(offsetToBase);

        final ConversionFormula formula = ConversionFormulas.affine(scale, offset);
        final UnitDefinition definition = new UnitDefinition(displayName, symbol, formula);

        return new UnitSpec(unit, definition);
    }

    /**
     * Determines the logical group type of a unit.
     *
     * <p>
     * For enum-based units, the group type is the declaring enum type (e.g., {@code Unit.Length.class}).
     * This makes grouping stable and independent of enum constant-specific classes.
     * </p>
     *
     * @param unit unit identifier; must not be {@code null}
     * @return the group type; never {@code null}
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Unit> groupTypeOf(@NonNull final Unit unit) {
        if (unit instanceof Enum<?> enumValue) {
            final Class<?> declaring = enumValue.getDeclaringClass();
            return (Class<? extends Unit>) declaring;
        }
        return unit.getClass();
    }

    /**
     * Internal immutable pair of a unit identifier and its {@link UnitDefinition}.
     *
     * <p>
     * This is purely a registry construction artifact to keep {@link #BUILT_IN} readable.
     * </p>
     *
     * @param unit       the unit identifier
     * @param definition the unit definition
     */
    private record UnitSpec(Unit unit, UnitDefinition definition) {
    }

}