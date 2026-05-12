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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regressions-Tests für numerische Korrektheit der Umrechnungen im {@link UnitRegistry}.
 *
 * <p>
 * Diese Tests prüfen Zahlenwerte, nicht nur Strukturkonsistenz wie {@link UnitRegistryTest}.
 * Sie schützen vor stillschweigender Re-Korruption der Tabellenwerte.
 * </p>
 */
final class UnitConversionRegressionTest {

    private static final MathContext MC = new MathContext(50);
    private static final BigDecimal TOLERANCE = new BigDecimal("1E-6");

    private final UnitConverter converter = new UnitConverter(MC);

    private void assertCloseTo(
            final String expected,
            final BigNumber actual,
            final String message
    ) {
        final BigDecimal e = new BigDecimal(expected);
        final BigDecimal a = actual.toBigDecimal();
        final BigDecimal diff = e.subtract(a).abs();
        assertTrue(
                diff.compareTo(TOLERANCE) <= 0,
                message + " — expected ≈ " + expected + " but got " + a + " (diff=" + diff + ")"
        );
    }

    private void assertCloseRel(
            final String expected,
            final BigNumber actual,
            final BigDecimal relativeTolerance,
            final String message
    ) {
        final BigDecimal e = new BigDecimal(expected);
        final BigDecimal a = actual.toBigDecimal();
        final BigDecimal diff = e.subtract(a).abs();
        final BigDecimal allowed = e.abs().multiply(relativeTolerance);
        assertTrue(
                diff.compareTo(allowed) <= 0,
                message + " — expected ≈ " + expected + " but got " + a + " (diff=" + diff + ", allowed=" + allowed + ")"
        );
    }

    // ===================================================================
    // TEMPERATURE — affine mit Offset; früher 100% kaputt für Fahrenheit
    // ===================================================================

    @Test
    void fahrenheit32EqualsCelsius0() {
        final BigNumber c = converter.convertToBigNumber("32", Unit.Temperature.FAHRENHEIT, Unit.Temperature.CELSIUS);
        assertCloseTo("0", c, "32 °F → 0 °C");
    }

    @Test
    void fahrenheit212EqualsCelsius100() {
        final BigNumber c = converter.convertToBigNumber("212", Unit.Temperature.FAHRENHEIT, Unit.Temperature.CELSIUS);
        assertCloseTo("100", c, "212 °F → 100 °C");
    }

    @Test
    void celsius0EqualsFahrenheit32() {
        final BigNumber f = converter.convertToBigNumber("0", Unit.Temperature.CELSIUS, Unit.Temperature.FAHRENHEIT);
        assertCloseTo("32", f, "0 °C → 32 °F");
    }

    @Test
    void celsius100EqualsFahrenheit212() {
        final BigNumber f = converter.convertToBigNumber("100", Unit.Temperature.CELSIUS, Unit.Temperature.FAHRENHEIT);
        assertCloseTo("212", f, "100 °C → 212 °F");
    }

    @Test
    void kelvinAbsoluteZeroEqualsCelsiusMinus273_15() {
        final BigNumber c = converter.convertToBigNumber("0", Unit.Temperature.KELVIN, Unit.Temperature.CELSIUS);
        assertCloseTo("-273.15", c, "0 K → -273.15 °C");
    }

    @Test
    void fahrenheitMinus40EqualsCelsiusMinus40() {
        final BigNumber c = converter.convertToBigNumber("-40", Unit.Temperature.FAHRENHEIT, Unit.Temperature.CELSIUS);
        assertCloseTo("-40", c, "−40 °F → −40 °C (Fixpunkt)");
    }

    @Test
    void kelvinFahrenheitRoundtripPreservesValue() {
        final BigNumber c1 = converter.convertToBigNumber("25", Unit.Temperature.CELSIUS, Unit.Temperature.FAHRENHEIT);
        final BigNumber c2 = converter.convertToBigNumber(c1, Unit.Temperature.FAHRENHEIT, Unit.Temperature.KELVIN);
        final BigNumber c3 = converter.convertToBigNumber(c2, Unit.Temperature.KELVIN, Unit.Temperature.CELSIUS);
        assertCloseTo("25", c3, "Celsius → F → K → C Roundtrip");
    }

    // ===================================================================
    // AREA — SQUARE_MILE war 6.4516E-10 statt 2.589e6
    // ===================================================================

    @Test
    void squareMileEquals2589988SquareMeter() {
        final BigNumber m2 = converter.convertToBigNumber("1", Unit.Area.SQUARE_MILE, Unit.Area.SQUARE_METER);
        assertCloseTo("2589988.110336", m2, "1 mi² in m²");
    }

    @Test
    void squareMileEquals640Acres() {
        final BigNumber ac = converter.convertToBigNumber("1", Unit.Area.SQUARE_MILE, Unit.Area.ACRE);
        assertCloseTo("640", ac, "1 mi² in acres");
    }

    @Test
    void sectionEqualsOneSquareMile() {
        final BigNumber mi2 = converter.convertToBigNumber("1", Unit.Area.SECTION, Unit.Area.SQUARE_MILE);
        assertCloseTo("1", mi2, "1 section = 1 mi²");
    }

    @Test
    void townshipEquals36Sections() {
        final BigNumber sec = converter.convertToBigNumber("1", Unit.Area.TOWNSHIP, Unit.Area.SECTION);
        assertCloseTo("36", sec, "1 township = 36 sections");
    }

    // ===================================================================
    // LENGTH — Cardinal-Pairs / Präzision
    // ===================================================================

    @Test
    void oneKilometerEquals1000Meter() {
        final BigNumber m = converter.convertToBigNumber("1", Unit.Length.KILOMETER, Unit.Length.METER);
        assertCloseTo("1000", m, "1 km in m");
    }

    @Test
    void oneMileEquals1609_344Meter() {
        final BigNumber m = converter.convertToBigNumber("1", Unit.Length.MILE, Unit.Length.METER);
        assertCloseTo("1609.344", m, "1 mi in m");
    }

    @Test
    void oneNanometerEquals1eMinus9Meter() {
        final BigNumber m = converter.convertToBigNumber("1", Unit.Length.NANOMETER, Unit.Length.METER);
        assertCloseRel("1E-9", m, new BigDecimal("1E-14"), "1 nm exakt");
    }

    @Test
    void oneAngstromEquals1eMinus10Meter() {
        final BigNumber m = converter.convertToBigNumber("1", Unit.Length.ANGSTROM, Unit.Length.METER);
        assertCloseRel("1E-10", m, new BigDecimal("1E-14"), "1 Å exakt");
    }

    @Test
    void oneFemtometerEquals1eMinus15Meter() {
        final BigNumber m = converter.convertToBigNumber("1", Unit.Length.FEMTOMETER, Unit.Length.METER);
        assertCloseRel("1E-15", m, new BigDecimal("1E-14"), "1 fm exakt");
    }

    @Test
    void kiloparsecEquals1000Parsec() {
        final BigNumber pc = converter.convertToBigNumber("1", Unit.Length.KILOPARSEC, Unit.Length.PARSEC);
        assertCloseRel("1000", pc, new BigDecimal("1E-14"), "1 kpc = 1000 pc");
    }

    @Test
    void emEqualsPicaDtpSystem() {
        // 1 em (12pt DTP) sollte == 1 pica == 12 * point
        final BigNumber pica = converter.convertToBigNumber("1", Unit.Length.EM, Unit.Length.PICA);
        assertCloseRel("1", pica, new BigDecimal("1E-6"), "1 em = 1 pica (DTP)");
    }

    // ===================================================================
    // VOLUME — Präzision CUBIC_FOOT konsistent mit HUNDRED_CUBIC_FOOT
    // ===================================================================

    @Test
    void hundredCubicFootEquals100CubicFoot() {
        final BigNumber ft3 = converter.convertToBigNumber("1", Unit.Volume.HUNDRED_CUBIC_FOOT, Unit.Volume.CUBIC_FOOT);
        assertCloseTo("100", ft3, "1 hundred-cubic-foot = 100 ft³");
    }

    @Test
    void cubicMeterEquals1000Liter() {
        final BigNumber l = converter.convertToBigNumber("1", Unit.Volume.CUBIC_METER, Unit.Volume.LITER);
        assertCloseTo("1000", l, "1 m³ = 1000 L");
    }

    @Test
    void picoliterEquals1eMinus15CubicMeter() {
        final BigNumber m3 = converter.convertToBigNumber("1", Unit.Volume.PICOLITER, Unit.Volume.CUBIC_METER);
        assertCloseRel("1E-15", m3, new BigDecimal("1E-14"), "1 pL exakt");
    }

    // ===================================================================
    // MASS — bekannte Cardinal-Pairs
    // ===================================================================

    @Test
    void oneKilogramEquals1000Gram() {
        final BigNumber g = converter.convertToBigNumber("1", Unit.Mass.KILOGRAM, Unit.Mass.GRAM);
        assertCloseTo("1000", g, "1 kg = 1000 g");
    }

    @Test
    void onePoundEquals453_59237Gram() {
        final BigNumber g = converter.convertToBigNumber("1", Unit.Mass.POUND, Unit.Mass.GRAM);
        assertCloseTo("453.59237", g, "1 lb = 453.59237 g");
    }

    @Test
    void oneTonneEquals1000Kilogram() {
        final BigNumber kg = converter.convertToBigNumber("1", Unit.Mass.TON, Unit.Mass.KILOGRAM);
        assertCloseTo("1000", kg, "1 t = 1000 kg");
    }

    // ===================================================================
    // PRESSURE — Atmosphäre, PSI
    // ===================================================================

    @Test
    void oneAtmosphereEquals101325Pascal() {
        final BigNumber pa = converter.convertToBigNumber("1", Unit.Pressure.STANDARD_ATMOSPHERE, Unit.Pressure.PASCAL);
        assertCloseTo("101325", pa, "1 atm = 101325 Pa");
    }

    @Test
    void technicalAtmosphereEqualsExact98066_5() {
        final BigNumber pa = converter.convertToBigNumber("1", Unit.Pressure.TECHNICAL_ATMOSPHERE, Unit.Pressure.PASCAL);
        assertCloseTo("98066.5", pa, "1 at = 98066.5 Pa exakt");
    }

    // ===================================================================
    // SPEED — runde Konstanten
    // ===================================================================

    @Test
    void oneKnotEquals1_852KilometerPerHour() {
        final BigNumber kmh = converter.convertToBigNumber("1", Unit.Speed.KNOT, Unit.Speed.KILOMETER_PER_HOUR);
        assertCloseRel("1.852", kmh, new BigDecimal("1E-6"), "1 kn = 1.852 km/h");
    }

    @Test
    void cosmicVelocityFirstIsExact7900() {
        final BigNumber ms = converter.convertToBigNumber("1", Unit.Speed.COSMIC_VELOCITY_FIRST, Unit.Speed.METER_PER_SECOND);
        assertCloseTo("7900", ms, "v1 = 7900 m/s exakt");
    }

    // ===================================================================
    // DATA STORAGE — exakte 2^n Werte
    // ===================================================================

    @Test
    void oneKibibyteEqualsExact8192Bit() {
        final BigNumber bits = converter.convertToBigNumber("1", Unit.DataStorage.KILOBYTE, Unit.DataStorage.BIT);
        assertCloseTo("8192", bits, "1 KiB = 8192 bit");
    }

    @Test
    void onePebibyteEqualsExact2to53Bit() {
        // 2^53 = 9007199254740992
        final BigNumber bits = converter.convertToBigNumber("1", Unit.DataStorage.PETABYTE, Unit.DataStorage.BIT);
        assertCloseTo("9007199254740992", bits, "1 PiB = 2^53 bit exakt");
    }

    @Test
    void oneExbibyteEqualsExact2to63Bit() {
        // 2^63 = 9223372036854775808
        final BigNumber bits = converter.convertToBigNumber("1", Unit.DataStorage.EXABYTE, Unit.DataStorage.BIT);
        assertCloseTo("9223372036854775808", bits, "1 EiB = 2^63 bit exakt");
    }

    @Test
    void oneMegabyteDecimalEqualsExact1Million() {
        final BigNumber b = converter.convertToBigNumber("1", Unit.DataStorage.MEGABYTE_DECIMAL, Unit.DataStorage.BYTE);
        assertCloseTo("1000000", b, "1 MB (10^6) = 1_000_000 B");
    }

    // ===================================================================
    // FUEL CONSUMPTION — reziproke Einheiten, früher kaputt für value != 1
    // ===================================================================

    @Test
    void fiveLitersPer100KmIsTwentyKilometerPerLiter() {
        // 5 L/100km == 20 km/L
        final BigNumber kmPerL = converter.convertToBigNumber("5", Unit.FuelConsumption.LITER_PER_100_KILOMETER, Unit.FuelConsumption.KILOMETER_PER_LITER);
        assertCloseRel("20", kmPerL, new BigDecimal("1E-6"), "5 L/100km = 20 km/L");
    }

    @Test
    void tenLitersPer100KmIsTenKilometerPerLiter() {
        final BigNumber kmPerL = converter.convertToBigNumber("10", Unit.FuelConsumption.LITER_PER_100_KILOMETER, Unit.FuelConsumption.KILOMETER_PER_LITER);
        assertCloseRel("10", kmPerL, new BigDecimal("1E-6"), "10 L/100km = 10 km/L");
    }

    @Test
    void thirtyMpgUsRoundsTripToL100km() {
        // 30 mi/gal(US) ≈ 7.8405 L/100km (Standardrechnung: 235.215/mpg)
        final BigNumber l100 = converter.convertToBigNumber(
                "30",
                Unit.FuelConsumption.MILE_PER_GALLON_US,
                Unit.FuelConsumption.LITER_PER_100_KILOMETER
        );
        assertCloseRel("7.840486", l100, new BigDecimal("1E-4"), "30 mpg(US) ≈ 7.84 L/100km");
    }

    @Test
    void litersPer100kmReciprocalDoublesIfHalved() {
        // 5 L/100km → X km/L; 10 L/100km → X/2 km/L (reziprok)
        final BigNumber a = converter.convertToBigNumber("5", Unit.FuelConsumption.LITER_PER_100_KILOMETER, Unit.FuelConsumption.METER_PER_LITER);
        final BigNumber b = converter.convertToBigNumber("10", Unit.FuelConsumption.LITER_PER_100_KILOMETER, Unit.FuelConsumption.METER_PER_LITER);
        final BigDecimal ratio = a.toBigDecimal().divide(b.toBigDecimal(), MC);
        assertEquals(0, ratio.compareTo(new BigDecimal("2")), "5 L/100km / 10 L/100km == 2 in m/L");
    }

    @Test
    void roundtripLitersPer100km() {
        final BigNumber back = converter.convertToBigNumber(
                converter.convertToBigNumber("7.5", Unit.FuelConsumption.LITER_PER_100_KILOMETER, Unit.FuelConsumption.METER_PER_LITER),
                Unit.FuelConsumption.METER_PER_LITER,
                Unit.FuelConsumption.LITER_PER_100_KILOMETER
        );
        assertCloseRel("7.5", back, new BigDecimal("1E-10"), "Roundtrip 7.5 L/100km");
    }

    @Test
    void roundtripGallonUsPerMile() {
        final BigNumber back = converter.convertToBigNumber(
                converter.convertToBigNumber("0.04", Unit.FuelConsumption.GALLON_US_PER_MILE, Unit.FuelConsumption.METER_PER_LITER),
                Unit.FuelConsumption.METER_PER_LITER,
                Unit.FuelConsumption.GALLON_US_PER_MILE
        );
        assertCloseRel("0.04", back, new BigDecimal("1E-10"), "Roundtrip 0.04 gal(US)/mi");
    }

    // ===================================================================
    // ENERGY — Elektronenvolt CODATA exakt
    // ===================================================================

    @Test
    void electronVoltCodataExact() {
        final BigNumber j = converter.convertToBigNumber("1", Unit.Energy.ELECTRON_VOLT, Unit.Energy.JOULE);
        assertCloseRel("1.602176634E-19", j, new BigDecimal("1E-10"), "1 eV = 1.602176634e-19 J (CODATA exakt)");
    }
}
