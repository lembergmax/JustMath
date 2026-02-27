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
            define(Unit.Length.MICROMETER, "Micrometer", "µm", "0.000001"),
            define(Unit.Length.MICRON, "Micron", "um", "0.000001"),
            define(Unit.Length.NANOMETER, "Nanometer", "nm", "9.999999999E-10"),
            define(Unit.Length.ANGSTROM, "Angstrom", "Å", "9.999999999E-11"),
            define(Unit.Length.PICOMETER, "Picometer", "pm", "1.E-12"),
            define(Unit.Length.FEMTOMETER, "Femtometer", "fm", "9.999999999E-16"),
            define(Unit.Length.ATTOMETER, "Attometer", "am", "1.E-18"),

            define(Unit.Length.PLANCK_LENGTH, "Planck Length", "lP", "1.616049999E-35"),
            define(Unit.Length.ELECTRON_RADIUS, "Electron Radius", "re", "2.81794092E-15"),
            define(Unit.Length.BOHR_RADIUS, "Bohr Radius", "a0", "5.29177249E-11"),
            define(Unit.Length.X_UNIT, "X Unit", "xu", "1.002079999E-13"),
            define(Unit.Length.FERMI, "Fermi", "fermi", "9.999999999E-16"),

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
            define(Unit.Volume.MICROLITER, "Microliter", "µL", "1.E-9"),
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
            define(Unit.Mass.EXAGRAM, "Exagram", "Eg", "1.0E+15"),
            define(Unit.Mass.PETAGRAM, "Petagram", "Pg", "1000000000000"),
            define(Unit.Mass.TERAGRAM, "Teragram", "Tg", "1000000000"),
            define(Unit.Mass.GIGAGRAM, "Gigagram", "Gg", "1000000"),
            define(Unit.Mass.MEGAGRAM, "Megagram", "Mg", "1000"),
            define(Unit.Mass.KILOTON, "Kiloton (Metric)", "kt", "1000000"),
            define(Unit.Mass.QUINTAL_METRIC, "Quintal (Metric)", "q", "100"),
            define(Unit.Mass.TON, "Tonne (Metric)", "t", "1000"),

            define(Unit.Mass.KILOGRAM, "Kilogram", "kg"),
            define(Unit.Mass.HECTOGRAM, "Hectogram", "hg", "0.1"),
            define(Unit.Mass.DEKAGRAM, "Dekagram", "dag", "0.01"),
            define(Unit.Mass.GRAM, "Gram", "g", "0.001"),
            define(Unit.Mass.DECIGRAM, "Decigram", "dg", "0.0001"),
            define(Unit.Mass.CENTIGRAM, "Centigram", "cg", "1.0E-5"),
            define(Unit.Mass.MILLIGRAM, "Milligram", "mg", "1.0E-6"),
            define(Unit.Mass.MICROGRAM, "Microgram", "µg", "1.0E-9"),
            define(Unit.Mass.GAMMA, "Gamma", "γ", "1.0E-9"),
            define(Unit.Mass.NANOGRAM, "Nanogram", "ng", "1.0E-12"),
            define(Unit.Mass.PICOGRAM, "Picogram", "pg", "1.0E-15"),
            define(Unit.Mass.FEMTOGRAM, "Femtogram", "fg", "1.0E-18"),
            define(Unit.Mass.ATTOGRAM, "Attogram", "ag", "1.0E-21"),

            define(Unit.Mass.CARRAT, "Carat", "ct", "0.0002"),
            define(Unit.Mass.GRAIN, "Grain", "gr", "6.47989E-5"),
            define(Unit.Mass.PENNYWEIGHT, "Pennyweight", "dwt", "0.0015551738"),
            define(Unit.Mass.SCRUPLE_APOTHECARY, "Scruple (Apothecary)", "℈", "0.0012959782"),
            define(Unit.Mass.POUND_TROY_APOTHECARY, "Pound (Troy or Apothecary)", "lb_t", "0.3732417216"),

            define(Unit.Mass.LONG_TON, "Ton (Long)", "LT", "1016.0469088"),
            define(Unit.Mass.SHORT_TON, "Ton (Short)", "ST", "907.18474"),
            define(Unit.Mass.POUND, "Pound", "lb", "0.45359237"),
            define(Unit.Mass.OUNCE, "Ounce", "oz", "0.0283495231"),

            define(Unit.Mass.HUNDREDWEIGHT_UNITED_STATES, "Hundredweight (United States)", "cwt(US)", "45.359237"),
            define(Unit.Mass.HUNDREDWEIGHT_UNITED_KINGDOM, "Hundredweight (United Kingdom)", "cwt(UK)", "50.80234544"),
            define(Unit.Mass.QUARTER_UNITED_STATES, "Quarter (United States)", "qr(US)", "11.33980925"),
            define(Unit.Mass.QUARTER_UNITED_KINGDOM, "Quarter (United Kingdom)", "qr(UK)", "12.70058636"),
            define(Unit.Mass.STONE_UNITED_STATES, "Stone (United States)", "st(US)", "5.669904625"),
            define(Unit.Mass.STONE_UNITED_KINGDOM, "Stone (United Kingdom)", "st(UK)", "6.35029318"),

            define(Unit.Mass.KILOGRAM_FORCE_SECOND_SQUARED_PER_METER, "Kilogram-Force Square Second per Meter", "kgf*s^2/m", "9.80665"),
            define(Unit.Mass.POUND_FORCE_SECOND_SQUARED_PER_FOOT, "Pound-Force Square Second per Foot", "lbf*s^2/ft", "14.5939029372"),
            define(Unit.Mass.SLUG, "Slug", "slug", "14.5939029372"),
            define(Unit.Mass.KILOPOUND, "Kilopound", "klb", "453.59237"),

            define(Unit.Mass.ASSAY_TON_UNITED_STATES, "Ton (Assay) (United States)", "AT(US)", "0.02916667"),
            define(Unit.Mass.ASSAY_TON_UNITED_KINGDOM, "Ton (Assay) (United Kingdom)", "AT(UK)", "0.0326666667"),

            define(Unit.Mass.ATOMIC_MASS_UNIT, "Atomic Mass Unit", "u", "1.6605402E-27"),
            define(Unit.Mass.DALTON, "Dalton", "Da", "1.6605300000013E-27"),

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
            define(Unit.Pressure.EXAPASCAL, "Exapascal", "EPa", "1.0E+18"),
            define(Unit.Pressure.PETAPASCAL, "Petapascal", "PPa", "1.0E+15"),
            define(Unit.Pressure.TERAPASCAL, "Terapascal", "TPa", "1000000000000"),
            define(Unit.Pressure.GIGAPASCAL, "Gigapascal", "GPa", "1000000000"),
            define(Unit.Pressure.MEGAPASCAL, "Megapascal", "MPa", "1000000"),
            define(Unit.Pressure.KILOPASCAL, "Kilopascal", "kPa", "1000"),
            define(Unit.Pressure.HECTOPASCAL, "Hectopascal", "hPa", "100"),
            define(Unit.Pressure.DEKAPASCAL, "Dekapascal", "daPa", "10"),

            define(Unit.Pressure.PASCAL, "Pascal", "Pa"),

            define(Unit.Pressure.DECIPASCAL, "Decipascal", "dPa", "0.1"),
            define(Unit.Pressure.CENTIPASCAL, "Centipascal", "cPa", "0.01"),
            define(Unit.Pressure.MILLIPASCAL, "Millipascal", "mPa", "0.001"),
            define(Unit.Pressure.MICROPASCAL, "Micropascal", "µPa", "1.0E-6"),
            define(Unit.Pressure.NANOPASCAL, "Nanopascal", "nPa", "1.0E-9"),
            define(Unit.Pressure.PICOPASCAL, "Picopascal", "pPa", "1.0E-12"),
            define(Unit.Pressure.FEMTOPASCAL, "Femtopascal", "fPa", "1.0E-15"),
            define(Unit.Pressure.ATTOPASCAL, "Attopascal", "aPa", "1.0E-18"),

            define(Unit.Pressure.BAR, "Bar", "bar", "100000"),
            define(Unit.Pressure.MILLIBAR, "Millibar", "mbar", "100"),
            define(Unit.Pressure.MICROBAR, "Microbar", "µbar", "0.1"),
            define(Unit.Pressure.STANDARD_ATMOSPHERE, "Standard Atmosphere", "atm", "101325"),
            define(Unit.Pressure.TECHNICAL_ATMOSPHERE, "Atmosphere (Technical)", "at", "98066.500000003"),
            define(Unit.Pressure.PSI, "Pounds per Square Inch", "psi", "6894.7572931783"),
            define(Unit.Pressure.KSI, "Kips per Square Inch", "ksi", "6894757.2931783"),
            define(Unit.Pressure.TORR, "Torr", "Torr", "133.3223684211"),

            define(Unit.Pressure.NEWTON_PER_SQUARE_METER, "Newton per Square Meter", "N/m^2", "1"),
            define(Unit.Pressure.NEWTON_PER_SQUARE_CENTIMETER, "Newton per Square Centimeter", "N/cm^2", "10000"),
            define(Unit.Pressure.NEWTON_PER_SQUARE_MILLIMETER, "Newton per Square Millimeter", "N/mm^2", "1000000"),
            define(Unit.Pressure.KILONEWTON_PER_SQUARE_METER, "Kilonewton per Square Meter", "kN/m^2", "1000"),
            define(Unit.Pressure.DYNE_PER_SQUARE_CENTIMETER, "Dyne per Square Centimeter", "dyn/cm^2", "0.1"),

            define(Unit.Pressure.KILOGRAM_FORCE_PER_SQUARE_METER, "Kilogram-Force per Square Meter", "kgf/m^2", "9.80665"),
            define(Unit.Pressure.KILOGRAM_FORCE_PER_SQUARE_CENTIMETER, "Kilogram-Force per Square Centimeter", "kgf/cm^2", "98066.5"),
            define(Unit.Pressure.KILOGRAM_FORCE_PER_SQUARE_MILLIMETER, "Kilogram-Force per Square Millimeter", "kgf/mm^2", "9806650"),
            define(Unit.Pressure.GRAM_FORCE_PER_SQUARE_CENTIMETER, "Gram-Force per Square Centimeter", "gf/cm^2", "98.0665"),

            define(Unit.Pressure.SHORT_TON_FORCE_PER_SQUARE_FOOT, "Ton-Force (Short) per Square Foot", "tonf(short)/ft^2", "95760.517960678"),
            define(Unit.Pressure.SHORT_TON_FORCE_PER_SQUARE_INCH, "Ton-Force (Short) per Square Inch", "tonf(short)/in^2", "13789514.586338"),
            define(Unit.Pressure.LONG_TON_FORCE_PER_SQUARE_FOOT, "Ton-Force (Long) per Square Foot", "tonf(long)/ft^2", "107251.78011595"),
            define(Unit.Pressure.LONG_TON_FORCE_PER_SQUARE_INCH, "Ton-Force (Long) per Square Inch", "tonf(long)/in^2", "15444256.336697"),

            define(Unit.Pressure.KIP_FORCE_PER_SQUARE_INCH, "Kip-Force per Square Inch", "kipf/in^2", "6894757.2931783"),
            define(Unit.Pressure.POUND_FORCE_PER_SQUARE_FOOT, "Pound-Force per Square Foot", "lbf/ft^2", "47.8802589804"),
            define(Unit.Pressure.POUND_FORCE_PER_SQUARE_INCH, "Pound-Force per Square Inch", "lbf/in^2", "6894.7572931783"),
            define(Unit.Pressure.POUNDAL_PER_SQUARE_FOOT, "Poundal per Square Foot", "pdl/ft^2", "1.4881639436"),

            define(Unit.Pressure.CENTIMETER_OF_MERCURY_0C, "Centimeter of Mercury (0°C)", "cmHg", "1333.22"),
            define(Unit.Pressure.MILLIMETER_OF_MERCURY_0C, "Millimeter of Mercury (0°C)", "mmHg", "133.322"),
            define(Unit.Pressure.INCH_OF_MERCURY_32F, "Inch of Mercury (32°F)", "inHg(32F)", "3386.38"),
            define(Unit.Pressure.INCH_OF_MERCURY_60F, "Inch of Mercury (60°F)", "inHg(60F)", "3376.85"),

            define(Unit.Pressure.CENTIMETER_OF_WATER_4C, "Centimeter of Water (4°C)", "cmH2O(4C)", "98.0638"),
            define(Unit.Pressure.MILLIMETER_OF_WATER_4C, "Millimeter of Water (4°C)", "mmH2O(4C)", "9.80638"),
            define(Unit.Pressure.INCH_OF_WATER_4C, "Inch of Water (4°C)", "inAq(4C)", "249.082"),
            define(Unit.Pressure.FOOT_OF_WATER_4C, "Foot of Water (4°C)", "ftAq(4C)", "2988.98"),
            define(Unit.Pressure.INCH_OF_WATER_60F, "Inch of Water (60°F)", "inAq(60F)", "248.843"),
            define(Unit.Pressure.FOOT_OF_WATER_60F, "Foot of Water (60°F)", "ftAq(60F)", "2986.116"),

            // =========================
            // ENERGY (base: joule)
            // =========================
            define(Unit.Energy.GIGAJOULE, "Gigajoule", "GJ", "1000000000"),
            define(Unit.Energy.MEGAJOULE, "Megajoule", "MJ", "1000000"),
            define(Unit.Energy.KILOJOULE, "Kilojoule", "kJ", "1000"),

            define(Unit.Energy.JOULE, "Joule", "J"),

            define(Unit.Energy.MILLIJOULE, "Millijoule", "mJ", "0.001"),
            define(Unit.Energy.MICROJOULE, "Microjoule", "µJ", "1.0E-6"),
            define(Unit.Energy.NANOJOULE, "Nanojoule", "nJ", "1.0E-9"),
            define(Unit.Energy.ATTOJOULE, "Attojoule", "aJ", "1.0E-18"),

            define(Unit.Energy.GIGAWATT_HOUR, "Gigawatt-Hour", "GW*h", "3600000000000"),
            define(Unit.Energy.MEGAWATT_HOUR, "Megawatt-Hour", "MW*h", "3600000000"),
            define(Unit.Energy.KILOWATT_HOUR, "Kilowatt-Hour", "kW*h", "3600000"),
            define(Unit.Energy.WATT_HOUR, "Watt-Hour", "W*h", "3600"),
            define(Unit.Energy.KILOWATT_SECOND, "Kilowatt-Second", "kW*s", "1000"),
            define(Unit.Energy.WATT_SECOND, "Watt-Second", "W*s", "1"),

            define(Unit.Energy.CALORIE_NUTRITIONAL, "Calorie (Nutritional)", "Cal", "4186.8"),
            define(Unit.Energy.KILOCALORIE_IT, "Kilocalorie (IT)", "kcal(IT)", "4186.8"),
            define(Unit.Energy.KILOCALORIE_TH, "Kilocalorie (th)", "kcal(th)", "4184"),
            define(Unit.Energy.CALORIE_IT, "Calorie (IT)", "cal", "4.1868"),
            define(Unit.Energy.CALORIE_TH, "Calorie (th)", "cal(th)", "4.184"),

            define(Unit.Energy.BTU_IT, "Btu (IT)", "Btu", "1055.05585262"),
            define(Unit.Energy.BTU_TH, "Btu (th)", "Btu(th)", "1054.3499999744"),
            define(Unit.Energy.MEGA_BTU_IT, "Mega Btu (IT)", "MBtu", "1055055852.62"),

            define(Unit.Energy.THERM, "Therm", "therm", "105505600"),
            define(Unit.Energy.THERM_EC, "Therm (EC)", "therm(EC)", "105505600"),
            define(Unit.Energy.THERM_US, "Therm (US)", "therm(US)", "105480400"),

            define(Unit.Energy.TON_HOUR_REFRIGERATION, "Ton-Hour (Refrigeration)", "ton_ref*h", "12660670.23144"),

            define(Unit.Energy.HORSEPOWER_METRIC_HOUR, "Horsepower (Metric) Hour", "hp(metric)*h", "2647795.5"),
            define(Unit.Energy.HORSEPOWER_HOUR, "Horsepower Hour", "hp*h", "2684519.5368856"),

            define(Unit.Energy.MEGAELECTRON_VOLT, "Megaelectron-Volt", "MeV", "1.6021766339999E-13"),
            define(Unit.Energy.KILOELECTRON_VOLT, "Kiloelectron-Volt", "keV", "1.6021766339999E-16"),
            define(Unit.Energy.ELECTRON_VOLT, "Electron-Volt", "eV", "1.6021766339999E-19"),

            define(Unit.Energy.HARTREE_ENERGY, "Hartree Energy", "Eh", "4.3597482E-18"),
            define(Unit.Energy.RYDBERG_CONSTANT, "Rydberg Constant", "Ry", "2.1798741E-18"),

            define(Unit.Energy.ERG, "Erg", "erg", "1.0E-7"),

            define(Unit.Energy.NEWTON_METER, "Newton Meter", "N*m", "1"),
            define(Unit.Energy.DYNE_CENTIMETER, "Dyne Centimeter", "dyn*cm", "1.0E-7"),

            define(Unit.Energy.GRAM_FORCE_METER, "Gram-Force Meter", "gf*m", "0.00980665"),
            define(Unit.Energy.GRAM_FORCE_CENTIMETER, "Gram-Force Centimeter", "gf*cm", "9.80665E-5"),
            define(Unit.Energy.KILOGRAM_FORCE_CENTIMETER, "Kilogram-Force Centimeter", "kgf*cm", "0.0980665"),
            define(Unit.Energy.KILOGRAM_FORCE_METER, "Kilogram-Force Meter", "kgf*m", "9.8066499997"),
            define(Unit.Energy.KILOPOND_METER, "Kilopond Meter", "kp*m", "9.8066499997"),

            define(Unit.Energy.POUND_FORCE_FOOT, "Pound-Force Foot", "lbf*ft", "1.3558179483"),
            define(Unit.Energy.POUND_FORCE_INCH, "Pound-Force Inch", "lbf*in", "0.112984829"),
            define(Unit.Energy.OUNCE_FORCE_INCH, "Ounce-Force Inch", "ozf*in", "0.0070615518"),

            define(Unit.Energy.FOOT_POUND, "Foot-Pound", "ft*lbf", "1.3558179483"),
            define(Unit.Energy.INCH_POUND, "Inch-Pound", "in*lbf", "0.112984829"),
            define(Unit.Energy.INCH_OUNCE, "Inch-Ounce", "in*ozf", "0.0070615518"),

            define(Unit.Energy.POUNDAL_FOOT, "Poundal Foot", "pdl*ft", "0.04214011"),

            define(Unit.Energy.GIGATON_TNT, "Gigaton (TNT Equivalent)", "Gton", "4.184E+18"),
            define(Unit.Energy.MEGATON_TNT, "Megaton (TNT Equivalent)", "Mton", "4.184E+15"),
            define(Unit.Energy.KILOTON_TNT, "Kiloton (TNT Equivalent)", "kton", "4184000000000"),
            define(Unit.Energy.TON_TNT, "Ton (Explosives)", "ton_TNT", "4184000000"),

            define(Unit.Energy.FUEL_OIL_EQUIVALENT_KILOLITER, "Fuel Oil Equivalent at Kiloliter", "foe@kL", "40197627984.822"),
            define(Unit.Energy.FUEL_OIL_EQUIVALENT_US_BARREL, "Fuel Oil Equivalent at Barrel (United States)", "foe@bbl(US)", "6383087908.3509"),

            // =========================
            // POWER (base: watt)
            // =========================
            define(Unit.Power.EXAWATT, "Exawatt", "EW", "1.0E+18"),
            define(Unit.Power.PETAWATT, "Petawatt", "PW", "1.0E+15"),
            define(Unit.Power.TERAWATT, "Terawatt", "TW", "1000000000000"),
            define(Unit.Power.GIGAWATT, "Gigawatt", "GW", "1000000000"),
            define(Unit.Power.MEGAWATT, "Megawatt", "MW", "1000000"),
            define(Unit.Power.KILOWATT, "Kilowatt", "kW", "1000"),
            define(Unit.Power.HECTOWATT, "Hectowatt", "hW", "100"),
            define(Unit.Power.DEKAWATT, "Dekawatt", "daW", "10"),

            define(Unit.Power.WATT, "Watt", "W"),

            define(Unit.Power.DECIWATT, "Deciwatt", "dW", "0.1"),
            define(Unit.Power.CENTIWATT, "Centiwatt", "cW", "0.01"),
            define(Unit.Power.MILLIWATT, "Milliwatt", "mW", "0.001"),
            define(Unit.Power.MICROWATT, "Microwatt", "µW", "1.0E-6"),
            define(Unit.Power.NANOWATT, "Nanowatt", "nW", "1.0E-9"),
            define(Unit.Power.PICOWATT, "Picowatt", "pW", "1.0E-12"),
            define(Unit.Power.FEMTOWATT, "Femtowatt", "fW", "1.0E-15"),
            define(Unit.Power.ATTOWATT, "Attowatt", "aW", "1.0E-18"),

            define(Unit.Power.HORSEPOWER, "Horsepower", "hp", "745.6998715823"),
            define(Unit.Power.HORSEPOWER_MECHANICAL_550_FTLBF_PER_S, "Horsepower (550 ft*lbf/s)", "hp(550ft*lbf/s)", "745.6998715823"),
            define(Unit.Power.HORSEPOWER_METRIC, "Horsepower (Metric)", "hp(metric)", "735.49875"),
            define(Unit.Power.HORSEPOWER_BOILER, "Horsepower (Boiler)", "hp(boiler)", "9809.5000000002"),
            define(Unit.Power.HORSEPOWER_ELECTRIC, "Horsepower (Electric)", "hp(electric)", "746"),
            define(Unit.Power.HORSEPOWER_WATER, "Horsepower (Water)", "hp(water)", "746.043"),
            define(Unit.Power.PFERDESTAERKE, "Pferdestarke", "PS", "735.49875"),

            define(Unit.Power.BTU_IT_PER_HOUR, "Btu (IT) per Hour", "Btu/h", "0.2930710702"),
            define(Unit.Power.BTU_IT_PER_MINUTE, "Btu (IT) per Minute", "Btu/min", "17.5842642103"),
            define(Unit.Power.BTU_IT_PER_SECOND, "Btu (IT) per Second", "Btu/s", "1055.05585262"),
            define(Unit.Power.BTU_TH_PER_HOUR, "Btu (th) per Hour", "Btu(th)/h", "0.292875"),
            define(Unit.Power.BTU_TH_PER_MINUTE, "Btu (th) per Minute", "Btu(th)/min", "17.5724999996"),
            define(Unit.Power.BTU_TH_PER_SECOND, "Btu (th) per Second", "Btu(th)/s", "1054.3499999744"),

            define(Unit.Power.MEGA_BTU_IT_PER_HOUR, "Mega Btu (IT) per Hour", "MBtu/h", "293071.07017222"),
            define(Unit.Power.MBH, "MBH", "MBH", "293.0710701722"),
            define(Unit.Power.TON_REFRIGERATION, "Ton (Refrigeration)", "TR", "3516.8528420667"),

            define(Unit.Power.KILOCALORIE_IT_PER_HOUR, "Kilocalorie (IT) per Hour", "kcal(IT)/h", "1.163"),
            define(Unit.Power.KILOCALORIE_IT_PER_MINUTE, "Kilocalorie (IT) per Minute", "kcal(IT)/min", "69.78"),
            define(Unit.Power.KILOCALORIE_IT_PER_SECOND, "Kilocalorie (IT) per Second", "kcal(IT)/s", "4186.8"),
            define(Unit.Power.KILOCALORIE_TH_PER_HOUR, "Kilocalorie (th) per Hour", "kcal(th)/h", "1.1622222222"),
            define(Unit.Power.KILOCALORIE_TH_PER_MINUTE, "Kilocalorie (th) per Minute", "kcal(th)/min", "69.7333333333"),
            define(Unit.Power.KILOCALORIE_TH_PER_SECOND, "Kilocalorie (th) per Second", "kcal(th)/s", "4184"),

            define(Unit.Power.CALORIE_IT_PER_HOUR, "Calorie (IT) per Hour", "cal/h", "0.001163"),
            define(Unit.Power.CALORIE_IT_PER_MINUTE, "Calorie (IT) per Minute", "cal/min", "0.06978"),
            define(Unit.Power.CALORIE_IT_PER_SECOND, "Calorie (IT) per Second", "cal/s", "4.1868"),
            define(Unit.Power.CALORIE_TH_PER_HOUR, "Calorie (th) per Hour", "cal(th)/h", "0.0011622222"),
            define(Unit.Power.CALORIE_TH_PER_MINUTE, "Calorie (th) per Minute", "cal(th)/min", "0.0697333333"),
            define(Unit.Power.CALORIE_TH_PER_SECOND, "Calorie (th) per Second", "cal(th)/s", "4.184"),

            define(Unit.Power.FOOT_POUND_FORCE_PER_HOUR, "Foot Pound-Force per Hour", "ft*lbf/h", "0.0003766161"),
            define(Unit.Power.FOOT_POUND_FORCE_PER_MINUTE, "Foot Pound-Force per Minute", "ft*lbf/min", "0.0225969658"),
            define(Unit.Power.FOOT_POUND_FORCE_PER_SECOND, "Foot Pound-Force per Second", "ft*lbf/s", "1.3558179483"),
            define(Unit.Power.POUND_FOOT_PER_HOUR, "Pound-Foot per Hour", "lbf*ft/h", "0.0003766161"),
            define(Unit.Power.POUND_FOOT_PER_MINUTE, "Pound-Foot per Minute", "lbf*ft/min", "0.0225969658"),
            define(Unit.Power.POUND_FOOT_PER_SECOND, "Pound-Foot per Second", "lbf*ft/s", "1.3558179483"),

            define(Unit.Power.ERG_PER_SECOND, "Erg per Second", "erg/s", "1.0E-7"),

            define(Unit.Power.KILOVOLT_AMPERE, "Kilovolt-Ampere", "kV*A", "1000"),
            define(Unit.Power.VOLT_AMPERE, "Volt-Ampere", "V*A", "1"),

            define(Unit.Power.NEWTON_METER_PER_SECOND, "Newton Meter per Second", "N*m/s", "1"),

            define(Unit.Power.JOULE_PER_SECOND, "Joule per Second", "J/s", "1"),
            define(Unit.Power.EXAJOULE_PER_SECOND, "Exajoule per Second", "EJ/s", "1.0E+18"),
            define(Unit.Power.PETAJOULE_PER_SECOND, "Petajoule per Second", "PJ/s", "1.0E+15"),
            define(Unit.Power.TERAJOULE_PER_SECOND, "Terajoule per Second", "TJ/s", "1000000000000"),
            define(Unit.Power.GIGAJOULE_PER_SECOND, "Gigajoule per Second", "GJ/s", "1000000000"),
            define(Unit.Power.MEGAJOULE_PER_SECOND, "Megajoule per Second", "MJ/s", "1000000"),
            define(Unit.Power.KILOJOULE_PER_SECOND, "Kilojoule per Second", "kJ/s", "1000"),
            define(Unit.Power.HECTOJOULE_PER_SECOND, "Hectojoule per Second", "hJ/s", "100"),
            define(Unit.Power.DEKAJOULE_PER_SECOND, "Dekajoule per Second", "daJ/s", "10"),
            define(Unit.Power.DECIJOULE_PER_SECOND, "Decijoule per Second", "dJ/s", "0.1"),
            define(Unit.Power.CENTIJOULE_PER_SECOND, "Centijoule per Second", "cJ/s", "0.01"),
            define(Unit.Power.MILLIJOULE_PER_SECOND, "Millijoule per Second", "mJ/s", "0.001"),
            define(Unit.Power.MICROJOULE_PER_SECOND, "Microjoule per Second", "µJ/s", "1.0E-6"),
            define(Unit.Power.NANOJOULE_PER_SECOND, "Nanojoule per Second", "nJ/s", "1.0E-9"),
            define(Unit.Power.PICOJOULE_PER_SECOND, "Picojoule per Second", "pJ/s", "1.0E-12"),
            define(Unit.Power.FEMTOJOULE_PER_SECOND, "Femtojoule per Second", "fJ/s", "1.0E-15"),
            define(Unit.Power.ATTOJOULE_PER_SECOND, "Attojoule per Second", "aJ/s", "1.0E-18"),

            define(Unit.Power.JOULE_PER_HOUR, "Joule per Hour", "J/h", "0.0002777778"),
            define(Unit.Power.JOULE_PER_MINUTE, "Joule per Minute", "J/min", "0.0166666667"),
            define(Unit.Power.KILOJOULE_PER_HOUR, "Kilojoule per Hour", "kJ/h", "0.2777777778"),
            define(Unit.Power.KILOJOULE_PER_MINUTE, "Kilojoule per Minute", "kJ/min", "16.6666666667"),

            // =========================
            // TIME (base: second)
            // =========================
            define(Unit.Time.MILLENNIUM, "Millennium", "millennium", "31557600000"),
            define(Unit.Time.CENTURY, "Century", "century", "3155760000"),
            define(Unit.Time.DECADE, "Decade", "decade", "315576000"),
            define(Unit.Time.YEAR, "Year", "y", "31557600"),
            define(Unit.Time.MONTH, "Month", "month", "2628000"),
            define(Unit.Time.WEEK, "Week", "week", "604800"),
            define(Unit.Time.DAY, "Day", "d", "86400"),
            define(Unit.Time.HOUR, "Hour", "h", "3600"),
            define(Unit.Time.MINUTE, "Minute", "min", "60"),

            define(Unit.Time.SECOND, "Second", "s"),

            define(Unit.Time.MILLISECOND, "Millisecond", "ms", "0.001"),
            define(Unit.Time.MICROSECOND, "Microsecond", "µs", "1.0E-6"),
            define(Unit.Time.NANOSECOND, "Nanosecond", "ns", "1.0E-9"),
            define(Unit.Time.PICOSECOND, "Picosecond", "ps", "1.0E-12"),
            define(Unit.Time.FEMTOSECOND, "Femtosecond", "fs", "1.0E-15"),
            define(Unit.Time.ATTOSECOND, "Attosecond", "as", "1.0E-18"),

            define(Unit.Time.SHAKE, "Shake", "shake", "1.0E-8"),

            define(Unit.Time.MONTH_SYNODIC, "Month (Synodic)", "month (synodic)", "2551443.84"),

            define(Unit.Time.YEAR_JULIAN, "Year (Julian)", "year (Julian)", "31557600"),
            define(Unit.Time.YEAR_LEAP, "Year (Leap)", "year (leap)", "31622400"),
            define(Unit.Time.YEAR_TROPICAL, "Year (Tropical)", "year (tropical)", "31556930"),
            define(Unit.Time.YEAR_SIDEREAL, "Year (Sidereal)", "year (sidereal)", "31558149.54"),

            define(Unit.Time.DAY_SIDEREAL, "Day (Sidereal)", "day (sidereal)", "86164.09"),
            define(Unit.Time.HOUR_SIDEREAL, "Hour (Sidereal)", "hour (sidereal)", "3590.1704166667"),
            define(Unit.Time.MINUTE_SIDEREAL, "Minute (Sidereal)", "minute (sidereal)", "59.8361736111"),
            define(Unit.Time.SECOND_SIDEREAL, "Second (Sidereal)", "second (sidereal)", "0.9972695602"),

            define(Unit.Time.FORTNIGHT, "Fortnight", "fortnight", "1209600"),

            define(Unit.Time.SEPTENNIAL, "Septennial", "septennial", "220752000"),
            define(Unit.Time.OCTENNIAL, "Octennial", "octennial", "252288000"),
            define(Unit.Time.NOVENNIAL, "Novennial", "novennial", "283824000"),
            define(Unit.Time.QUINDECENNIAL, "Quindecennial", "quindecennial", "473040000"),
            define(Unit.Time.QUINQUENNIAL, "Quinquennial", "quinquennial", "157680000"),

            define(Unit.Time.PLANCK_TIME, "Planck Time", "Planck time", "5.39056E-44"),

            // =========================
            // FORCE (base: newton)
            // =========================
            define(Unit.Force.EXANEWTON, "Exanewton", "EN", "1.0E+18"),
            define(Unit.Force.PETANEWTON, "Petanewton", "PN", "1.0E+15"),
            define(Unit.Force.TERANEWTON, "Teranewton", "TN", "1000000000000"),
            define(Unit.Force.GIGANEWTON, "Giganewton", "GN", "1000000000"),
            define(Unit.Force.MEGANEWTON, "Meganewton", "MN", "1000000"),
            define(Unit.Force.KILONEWTON, "Kilonewton", "kN", "1000"),
            define(Unit.Force.HECTONEWTON, "Hectonewton", "hN", "100"),
            define(Unit.Force.DEKANEWTON, "Dekanewton", "daN", "10"),

            define(Unit.Force.NEWTON, "Newton", "N"),

            define(Unit.Force.DECINEWTON, "Decinewton", "dN", "0.1"),
            define(Unit.Force.CENTINEWTON, "Centinewton", "cN", "0.01"),
            define(Unit.Force.MILLINEWTON, "Millinewton", "mN", "0.001"),
            define(Unit.Force.MICRONEWTON, "Micronewton", "µN", "1.0E-6"),
            define(Unit.Force.NANONEWTON, "Nanonewton", "nN", "1.0E-9"),
            define(Unit.Force.PICONEWTON, "Piconewton", "pN", "1.0E-12"),
            define(Unit.Force.FEMTONEWTON, "Femtonewton", "fN", "1.0E-15"),
            define(Unit.Force.ATTONEWTON, "Attonewton", "aN", "1.0E-18"),

            define(Unit.Force.DYNE, "Dyne", "dyn", "1.0E-5"),

            define(Unit.Force.JOULE_PER_METER, "Joule per Meter", "J/m", "1"),
            define(Unit.Force.JOULE_PER_CENTIMETER, "Joule per Centimeter", "J/cm", "0.01"),

            define(Unit.Force.GRAM_FORCE, "Gram-Force", "gf", "0.00980665"),
            define(Unit.Force.KILOGRAM_FORCE, "Kilogram-Force", "kgf", "9.80665"),
            define(Unit.Force.TON_FORCE_METRIC, "Ton-Force (Metric)", "tf", "9806.65"),

            define(Unit.Force.TON_FORCE_SHORT, "Ton-Force (Short)", "ton-force (short)", "8896.443230521"),
            define(Unit.Force.TON_FORCE_LONG, "Ton-Force (Long)", "tonf (UK)", "9964.0164181707"),

            define(Unit.Force.KIP_FORCE, "Kip-Force", "klbf", "4448.2216152548"),
            define(Unit.Force.KILOPOUND_FORCE, "Kilopound-Force", "kipf", "4448.2216152548"),

            define(Unit.Force.POUND_FORCE, "Pound-Force", "lbf", "4.4482216153"),
            define(Unit.Force.OUNCE_FORCE, "Ounce-Force", "ozf", "0.278013851"),

            define(Unit.Force.POUNDAL, "Poundal", "pdl", "0.1382549544"),
            define(Unit.Force.POUND_FOOT_PER_SQUARE_SECOND, "Pound Foot per Square Second", "pound foot/square second", "0.1382549544"),

            define(Unit.Force.POND, "Pond", "p", "0.00980665"),
            define(Unit.Force.KILOPOND, "Kilopond", "kp", "9.80665"),

            // =========================
            // SPEED (base: meter per second)
            // =========================
            define(Unit.Speed.KILOMETER_PER_HOUR, "Kilometer per Hour", "km/h", "0.2777777778"),
            define(Unit.Speed.MILE_PER_HOUR, "Mile per Hour", "mi/h", "0.44704"),
            define(Unit.Speed.METER_PER_HOUR, "Meter per Hour", "m/h", "0.0002777778"),
            define(Unit.Speed.METER_PER_MINUTE, "Meter per Minute", "m/min", "0.0166666667"),
            define(Unit.Speed.KILOMETER_PER_MINUTE, "Kilometer per Minute", "km/min", "16.6666666667"),
            define(Unit.Speed.KILOMETER_PER_SECOND, "Kilometer per Second", "km/s", "1000"),

            define(Unit.Speed.CENTIMETER_PER_HOUR, "Centimeter per Hour", "cm/h", "2.7777777777778E-6"),
            define(Unit.Speed.CENTIMETER_PER_MINUTE, "Centimeter per Minute", "cm/min", "0.0001666667"),
            define(Unit.Speed.CENTIMETER_PER_SECOND, "Centimeter per Second", "cm/s", "0.01"),

            define(Unit.Speed.MILLIMETER_PER_HOUR, "Millimeter per Hour", "mm/h", "2.7777777777778E-7"),
            define(Unit.Speed.MILLIMETER_PER_MINUTE, "Millimeter per Minute", "mm/min", "1.66667E-5"),
            define(Unit.Speed.MILLIMETER_PER_SECOND, "Millimeter per Second", "mm/s", "0.001"),

            define(Unit.Speed.FOOT_PER_HOUR, "Foot per Hour", "ft/h", "8.46667E-5"),
            define(Unit.Speed.FOOT_PER_MINUTE, "Foot per Minute", "ft/min", "0.00508"),
            define(Unit.Speed.FOOT_PER_SECOND, "Foot per Second", "ft/s", "0.3048"),

            define(Unit.Speed.YARD_PER_HOUR, "Yard per Hour", "yd/h", "0.000254"),
            define(Unit.Speed.YARD_PER_MINUTE, "Yard per Minute", "yd/min", "0.01524"),
            define(Unit.Speed.YARD_PER_SECOND, "Yard per Second", "yd/s", "0.9144"),

            define(Unit.Speed.MILE_PER_MINUTE, "Mile per Minute", "mi/min", "26.8224"),
            define(Unit.Speed.MILE_PER_SECOND, "Mile per Second", "mi/s", "1609.344"),

            define(Unit.Speed.KNOT, "Knot", "kn", "0.5144444444"),
            define(Unit.Speed.KNOT_UK, "Knot (UK)", "kn_UK", "0.5147733333"),

            define(Unit.Speed.SPEED_OF_LIGHT_VACUUM, "Velocity of Light in Vacuum", "c", "299792458"),
            define(Unit.Speed.COSMIC_VELOCITY_FIRST, "Cosmic Velocity (First)", "v1", "7899.9999999999"),
            define(Unit.Speed.COSMIC_VELOCITY_SECOND, "Cosmic Velocity (Second)", "v2", "11200"),
            define(Unit.Speed.COSMIC_VELOCITY_THIRD, "Cosmic Velocity (Third)", "v3", "16670"),
            define(Unit.Speed.EARTHS_VELOCITY, "Earth's Velocity", "v_earth", "29765"),
            define(Unit.Speed.SPEED_OF_SOUND_PURE_WATER, "Velocity of Sound in Pure Water", "v_sound_water", "1482.6999999998"),
            define(Unit.Speed.SPEED_OF_SOUND_SEA_WATER_20C_10M, "Velocity of Sound in Sea Water (20°C, 10 Meter Deep)", "v_sound_sea", "1521.6"),
            define(Unit.Speed.MACH_20C_1ATM, "Mach (20°C, 1 atm)", "Ma(20°C)", "343.6"),
            define(Unit.Speed.MACH_SI_STANDARD, "Mach (SI Standard)", "Ma(SI)", "295.0464000003"),

            define(Unit.Speed.METER_PER_SECOND, "Meter per Second", "m/s"),

            // =========================
            // FUEL CONSUMPTION (base: meter per liter)
            // =========================
            define(Unit.FuelConsumption.EXAMETER_PER_LITER, "Exameter per Liter", "Em/L", "1.0E+18"),
            define(Unit.FuelConsumption.PETAMETER_PER_LITER, "Petameter per Liter", "Pm/L", "1.0E+15"),
            define(Unit.FuelConsumption.TERAMETER_PER_LITER, "Terameter per Liter", "Tm/L", "1000000000000"),
            define(Unit.FuelConsumption.GIGAMETER_PER_LITER, "Gigameter per Liter", "Gm/L", "1000000000"),
            define(Unit.FuelConsumption.MEGAMETER_PER_LITER, "Megameter per Liter", "Mm/L", "1000000"),
            define(Unit.FuelConsumption.KILOMETER_PER_LITER, "Kilometer per Liter", "km/L", "1000"),
            define(Unit.FuelConsumption.HECTOMETER_PER_LITER, "Hectometer per Liter", "hm/L", "100"),
            define(Unit.FuelConsumption.DEKAMETER_PER_LITER, "Dekameter per Liter", "dam/L", "10"),
            define(Unit.FuelConsumption.CENTIMETER_PER_LITER, "Centimeter per Liter", "cm/L", "0.01"),

            define(Unit.FuelConsumption.MILE_US_PER_LITER, "Mile (US) per Liter", "mi/L", "1609.344"),
            define(Unit.FuelConsumption.NAUTICAL_MILE_PER_LITER, "Nautical Mile per Liter", "nmi/L", "1853.24496"),

            define(Unit.FuelConsumption.NAUTICAL_MILE_PER_GALLON_US, "Nautical Mile per Gallon (US)", "nmi/gal(US)", "489.5755247"),
            define(Unit.FuelConsumption.KILOMETER_PER_GALLON_US, "Kilometer per Gallon (US)", "km/gal (US)", "264.1720524"),
            define(Unit.FuelConsumption.METER_PER_GALLON_US, "Meter per Gallon (US)", "m/gal (US)", "0.2641720524"),
            define(Unit.FuelConsumption.METER_PER_GALLON_UK, "Meter per Gallon (UK)", "m/gal (UK)", "0.2199687986"),
            define(Unit.FuelConsumption.MILE_PER_GALLON_US, "Mile per Gallon (US)", "mi/gal (US)", "425.1437075"),
            define(Unit.FuelConsumption.MILE_PER_GALLON_UK, "Mile per Gallon (UK)", "mi/gal (UK)", "354.00619"),

            define(Unit.FuelConsumption.METER_PER_CUBIC_METER, "Meter per Cubic Meter", "m/m^3", "0.001"),
            define(Unit.FuelConsumption.METER_PER_CUBIC_CENTIMETER, "Meter per Cubic Centimeter", "m/cm^3", "1000"),
            define(Unit.FuelConsumption.METER_PER_CUBIC_YARD, "Meter per Cubic Yard", "m/yd^3", "0.0013079506"),
            define(Unit.FuelConsumption.METER_PER_CUBIC_FOOT, "Meter per Cubic Foot", "m/ft^3", "0.0353146667"),
            define(Unit.FuelConsumption.METER_PER_CUBIC_INCH, "Meter per Cubic Inch", "m/in^3", "61.02374409"),

            define(Unit.FuelConsumption.METER_PER_QUART_US, "Meter per Quart (US)", "m/qt (US)", "1.056688209"),
            define(Unit.FuelConsumption.METER_PER_QUART_UK, "Meter per Quart (UK)", "m/qt (UK)", "0.8798751948"),
            define(Unit.FuelConsumption.METER_PER_PINT_US, "Meter per Pint (US)", "m/pt (US)", "2.113376419"),
            define(Unit.FuelConsumption.METER_PER_PINT_UK, "Meter per Pint (UK)", "m/pt (UK)", "1.759750389"),
            define(Unit.FuelConsumption.METER_PER_CUP_US, "Meter per Cup (US)", "m/cup (US)", "4.226752838"),
            define(Unit.FuelConsumption.METER_PER_CUP_UK, "Meter per Cup (UK)", "m/cup (UK)", "3.519500777"),
            define(Unit.FuelConsumption.METER_PER_FLUID_OUNCE_US, "Meter per Fluid Ounce (US)", "m/fl oz (US)", "33.8140227"),
            define(Unit.FuelConsumption.METER_PER_FLUID_OUNCE_UK, "Meter per Fluid Ounce (UK)", "m/fl oz (UK)", "35.19500777"),

            define(Unit.FuelConsumption.LITER_PER_METER, "Liter per Meter", "L/m", "1"),
            define(Unit.FuelConsumption.LITER_PER_100_KILOMETER, "Liter per 100 Kilometer", "L/100 km", "100000"),
            define(Unit.FuelConsumption.GALLON_US_PER_MILE, "Gallon (US) per Mile", "gal (US)/mi", "425.1437074976"),
            define(Unit.FuelConsumption.GALLON_US_PER_100_MILES, "Gallon (US) per 100 Miles", "gal (US)/100 mi", "42514.370749763"),
            define(Unit.FuelConsumption.GALLON_UK_PER_MILE, "Gallon (UK) per Mile", "gal (UK)/mi", "354.0061899559"),
            define(Unit.FuelConsumption.GALLON_UK_PER_100_MILES, "Gallon (UK) per 100 Miles", "gal (UK)/100 mi", "35400.618995592"),

            define(Unit.FuelConsumption.METER_PER_LITER, "Meter per Liter", "m/L"),

            // =========================
            // DATA STORAGE (base: bit)
            // =========================
            define(Unit.DataStorage.BIT, "Bit", "bit"),

            define(Unit.DataStorage.NIBBLE, "Nibble", "nibble", "4"),
            define(Unit.DataStorage.BYTE, "Byte", "B", "8"),
            define(Unit.DataStorage.CHARACTER, "Character", "char", "8"),
            define(Unit.DataStorage.WORD, "Word", "word", "16"),
            define(Unit.DataStorage.MAPM_WORD, "MAPM-Word", "MAPM-word", "32"),
            define(Unit.DataStorage.QUADRUPLE_WORD, "Quadruple-Word", "quadruple-word", "64"),
            define(Unit.DataStorage.BLOCK, "Block", "block", "4096"),

            define(Unit.DataStorage.KILOBIT, "Kilobit", "Kibit", "1024"),
            define(Unit.DataStorage.KILOBYTE, "Kilobyte", "KiB", "8192"),
            define(Unit.DataStorage.MEGABIT, "Megabit", "Mibit", "1048576"),
            define(Unit.DataStorage.MEGABYTE, "Megabyte", "MiB", "8388608"),
            define(Unit.DataStorage.GIGABIT, "Gigabit", "Gibit", "1073741824"),
            define(Unit.DataStorage.GIGABYTE, "Gigabyte", "GiB", "8589934592"),
            define(Unit.DataStorage.TERABIT, "Terabit", "Tibit", "1099511627776"),
            define(Unit.DataStorage.TERABYTE, "Terabyte", "TiB", "8796093022208"),
            define(Unit.DataStorage.PETABIT, "Petabit", "Pibit", "1.1258999068426E+15"),
            define(Unit.DataStorage.PETABYTE, "Petabyte", "PiB", "9.007199254741E+15"),
            define(Unit.DataStorage.EXABIT, "Exabit", "Eibit", "1.1529215046068E+18"),
            define(Unit.DataStorage.EXABYTE, "Exabyte", "EiB", "9.2233720368548E+18"),

            define(Unit.DataStorage.KILOBYTE_DECIMAL, "Kilobyte (10^3 bytes)", "kB", "8000"),
            define(Unit.DataStorage.MEGABYTE_DECIMAL, "Megabyte (10^6 bytes)", "MB", "8000000"),
            define(Unit.DataStorage.GIGABYTE_DECIMAL, "Gigabyte (10^9 bytes)", "GB", "8000000000"),
            define(Unit.DataStorage.TERABYTE_DECIMAL, "Terabyte (10^12 bytes)", "TB", "8000000000000"),
            define(Unit.DataStorage.PETABYTE_DECIMAL, "Petabyte (10^15 bytes)", "PB", "8.0E+15"),
            define(Unit.DataStorage.EXABYTE_DECIMAL, "Exabyte (10^18 bytes)", "EB", "8.0E+18"),

            define(Unit.DataStorage.FLOPPY_35_DD, "Floppy Disk (3.5\", DD)", "floppy_3.5_DD", "5830656"),
            define(Unit.DataStorage.FLOPPY_35_HD, "Floppy Disk (3.5\", HD)", "floppy_3.5_HD", "11661312"),
            define(Unit.DataStorage.FLOPPY_35_ED, "Floppy Disk (3.5\", ED)", "floppy_3.5_ED", "23322624"),
            define(Unit.DataStorage.FLOPPY_525_DD, "Floppy Disk (5.25\", DD)", "floppy_5.25_DD", "2915328"),
            define(Unit.DataStorage.FLOPPY_525_HD, "Floppy Disk (5.25\", HD)", "floppy_5.25_HD", "9711616"),

            define(Unit.DataStorage.ZIP_100, "Zip 100", "zip_100", "803454976"),
            define(Unit.DataStorage.ZIP_250, "Zip 250", "zip_250", "2008637440"),
            define(Unit.DataStorage.JAZ_1GB, "Jaz 1GB", "jaz_1GB", "8589934592"),
            define(Unit.DataStorage.JAZ_2GB, "Jaz 2GB", "jaz_2GB", "17179869184"),

            define(Unit.DataStorage.CD_74_MIN, "CD (74 minute)", "cd_74_min", "5448466432"),
            define(Unit.DataStorage.CD_80_MIN, "CD (80 minute)", "cd_80_min", "5890233976"),
            define(Unit.DataStorage.DVD_1L_1S, "DVD (1 layer, 1 side)", "dvd_1L_1S", "40372692582.4"),
            define(Unit.DataStorage.DVD_2L_1S, "DVD (2 layer, 1 side)", "dvd_2L_1S", "73014444032"),
            define(Unit.DataStorage.DVD_1L_2S, "DVD (1 layer, 2 side)", "dvd_1L_2S", "80745385164.8"),
            define(Unit.DataStorage.DVD_2L_2S, "DVD (2 layer, 2 side)", "dvd_2L_2S", "146028888064")
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