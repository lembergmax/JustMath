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
package io.github.lembergmax.justmath.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.BigNumber;

/**
 * Exact-precision tests that guarantee unit conversions produce no rounding artifacts
 * like {@code 12.500000001} when the mathematical result is a terminating decimal.
 *
 * <p>
 * These tests directly target the regression that arose from storing scale factors as
 * pre-rounded decimals (e.g. {@code 0.2777777778} for {@code km/h -> m/s}). The fix
 * replaces such factors with exact rational pairs (e.g. {@code 1000/3600}), so the
 * division is deferred to conversion time and operates on integers wherever possible.
 * </p>
 */
final class UnitConversionPrecisionTest {

    private static final MathContext MC = new MathContext(50);

    private final UnitConverter converter = new UnitConverter(MC);

    private void assertExact(final String expected, final BigNumber actual, final String message) {
        final BigDecimal e = new BigDecimal(expected);
        final BigDecimal a = actual.toBigDecimal();
        assertEquals(
                0,
                e.compareTo(a),
                message + " — expected exactly " + expected + " but got " + a
        );
    }

    // ===================================================================
    // SPEED — km/h <-> m/s, the original reported bug
    // ===================================================================

    @Test
    void fortyFiveKilometerPerHourEqualsExactly12_5MeterPerSecond() {
        final BigNumber ms = converter.convertToBigNumber(
                "45", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
        );
        assertExact("12.5", ms, "45 km/h must produce exactly 12.5 m/s, not 12.500000001");
    }

    @Test
    void ninetyKilometerPerHourEqualsExactly25MeterPerSecond() {
        final BigNumber ms = converter.convertToBigNumber(
                "90", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
        );
        assertExact("25", ms, "90 km/h = 25 m/s exactly");
    }

    @Test
    void thirtySixKilometerPerHourEqualsExactly10MeterPerSecond() {
        final BigNumber ms = converter.convertToBigNumber(
                "36", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
        );
        assertExact("10", ms, "36 km/h = 10 m/s exactly");
    }

    @Test
    void tenMeterPerSecondEqualsExactly36KilometerPerHour() {
        final BigNumber kmh = converter.convertToBigNumber(
                "10", Unit.Speed.METER_PER_SECOND, Unit.Speed.KILOMETER_PER_HOUR
        );
        assertExact("36", kmh, "10 m/s = 36 km/h exactly");
    }

    @Test
    void zeroKilometerPerHourEqualsExactlyZeroMeterPerSecond() {
        final BigNumber ms = converter.convertToBigNumber(
                "0", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
        );
        assertExact("0", ms, "0 km/h = 0 m/s exactly");
    }

    @Test
    void kilometerPerHourRoundtripIsIdentity() {
        final BigNumber roundtrip = converter.convertToBigNumber(
                converter.convertToBigNumber(
                        "123.456", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
                ),
                Unit.Speed.METER_PER_SECOND,
                Unit.Speed.KILOMETER_PER_HOUR
        );
        assertExact("123.456", roundtrip, "km/h -> m/s -> km/h must be identity");
    }

    // ===================================================================
    // TIME — exact integer ratios
    // ===================================================================

    @Test
    void oneHourEqualsExactly3600Seconds() {
        final BigNumber s = converter.convertToBigNumber("1", Unit.Time.HOUR, Unit.Time.SECOND);
        assertExact("3600", s, "1 h = 3600 s exactly");
    }

    @Test
    void oneMinuteEqualsExactly60Seconds() {
        final BigNumber s = converter.convertToBigNumber("1", Unit.Time.MINUTE, Unit.Time.SECOND);
        assertExact("60", s, "1 min = 60 s exactly");
    }

    // ===================================================================
    // LENGTH — exact terminating imperial constants
    // ===================================================================

    @Test
    void oneFootEqualsExactly0_3048Meter() {
        final BigNumber m = converter.convertToBigNumber("1", Unit.Length.FEET, Unit.Length.METER);
        assertExact("0.3048", m, "1 ft = 0.3048 m exactly");
    }

    @Test
    void oneMileEqualsExactly1609_344Meter() {
        final BigNumber m = converter.convertToBigNumber("1", Unit.Length.MILE, Unit.Length.METER);
        assertExact("1609.344", m, "1 mi = 1609.344 m exactly");
    }

    @Test
    void oneInchEqualsExactly0_0254Meter() {
        final BigNumber m = converter.convertToBigNumber("1", Unit.Length.INCH, Unit.Length.METER);
        assertExact("0.0254", m, "1 in = 0.0254 m exactly");
    }

    // ===================================================================
    // SPEED derived units that previously stored rounded 1/60 / 1/3600 factors
    // ===================================================================

    @Test
    void oneKilometerPerMinuteEqualsExactlyOneThousandSixtieth() {
        // 1 km/min = 1000/60 m/s = 16.666... — non-terminating, so verify
        // forward then back to make sure no spurious tail is introduced.
        final BigNumber roundtrip = converter.convertToBigNumber(
                converter.convertToBigNumber("12", Unit.Speed.KILOMETER_PER_MINUTE, Unit.Speed.METER_PER_SECOND),
                Unit.Speed.METER_PER_SECOND,
                Unit.Speed.KILOMETER_PER_MINUTE
        );
        assertExact("12", roundtrip, "km/min roundtrip must be identity");
    }

    @Test
    void sixtyMeterPerMinuteEqualsExactlyOneMeterPerSecond() {
        final BigNumber ms = converter.convertToBigNumber(
                "60", Unit.Speed.METER_PER_MINUTE, Unit.Speed.METER_PER_SECOND
        );
        assertExact("1", ms, "60 m/min = 1 m/s exactly");
    }

    @Test
    void oneKnotEqualsExactlyOnePoint852KilometerPerHour() {
        // 1 kn = 1852/3600 m/s, back via km/h: 1852/3600 * 3600/1000 = 1.852 exactly.
        final BigNumber kmh = converter.convertToBigNumber(
                "1", Unit.Speed.KNOT, Unit.Speed.KILOMETER_PER_HOUR
        );
        assertExact("1.852", kmh, "1 kn = 1.852 km/h exactly");
    }

    // ===================================================================
    // POWER — kJ/h <-> W, exact integer-derived ratio
    // ===================================================================

    @Test
    void threeThousandSixHundredKilojoulePerHourEqualsExactlyOneKilowatt() {
        final BigNumber w = converter.convertToBigNumber(
                "3600", Unit.Power.KILOJOULE_PER_HOUR, Unit.Power.WATT
        );
        assertExact("1000", w, "3600 kJ/h = 1000 W exactly");
    }

    @Test
    void oneKilocalorieThPerHourRoundtripIsWithinUlp() {
        // 1 kcal(th)/h = 4184/3600 W. The forward division is non-terminating
        // (4184/3600 = 1.16222...), so the roundtrip can lose at most one ULP at the
        // tail of the active MathContext precision. The conversion factor itself is
        // still stored exactly as the rational pair 4184/3600 — no pre-rounded
        // decimal artifact.
        final BigNumber back = converter.convertToBigNumber(
                converter.convertToBigNumber("1", Unit.Power.KILOCALORIE_TH_PER_HOUR, Unit.Power.WATT),
                Unit.Power.WATT,
                Unit.Power.KILOCALORIE_TH_PER_HOUR
        );
        final BigDecimal diff = back.toBigDecimal().subtract(BigDecimal.ONE).abs();
        org.junit.jupiter.api.Assertions.assertTrue(
                diff.compareTo(new BigDecimal("1E-45")) < 0,
                "kcal(th)/h roundtrip within 1e-45, got " + back
        );
    }

    // ===================================================================
    // TEMPERATURE — affine conversions must still work
    // ===================================================================

    @Test
    void temperatureConversionsStillWork() {
        final BigNumber c = converter.convertToBigNumber("32", Unit.Temperature.FAHRENHEIT, Unit.Temperature.CELSIUS);
        // Affine math via Fahrenheit's 0.555...5556 scale: tolerate the smallest tail
        final BigDecimal a = c.toBigDecimal();
        final BigDecimal diff = a.subtract(BigDecimal.ZERO).abs();
        org.junit.jupiter.api.Assertions.assertTrue(
                diff.compareTo(new BigDecimal("1E-20")) < 0,
                "32 °F → 0 °C within 1e-20, got " + a
        );

        final BigNumber f = converter.convertToBigNumber("100", Unit.Temperature.CELSIUS, Unit.Temperature.FAHRENHEIT);
        final BigDecimal diffF = f.toBigDecimal().subtract(new BigDecimal("212")).abs();
        org.junit.jupiter.api.Assertions.assertTrue(
                diffF.compareTo(new BigDecimal("1E-20")) < 0,
                "100 °C → 212 °F within 1e-20, got " + f
        );
    }

    // ===================================================================
    // FUEL CONSUMPTION — reciprocal formula must still work
    // ===================================================================

    @Test
    void reciprocalFuelConsumptionStillWorks() {
        // 5 L/100km = 20 km/L is a well-known reciprocal identity
        final BigNumber kmPerL = converter.convertToBigNumber(
                "5",
                Unit.FuelConsumption.LITER_PER_100_KILOMETER,
                Unit.FuelConsumption.KILOMETER_PER_LITER
        );
        final BigDecimal diff = kmPerL.toBigDecimal().subtract(new BigDecimal("20")).abs();
        org.junit.jupiter.api.Assertions.assertTrue(
                diff.compareTo(new BigDecimal("1E-20")) < 0,
                "5 L/100km = 20 km/L within 1e-20, got " + kmPerL
        );
    }

    // ===================================================================
    // EXTREME MAGNITUDES — exact for very large and very small inputs
    // ===================================================================

    @Test
    void veryLargeKilometerPerHourConvertsExactly() {
        final BigNumber ms = converter.convertToBigNumber(
                "3600000000000000", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
        );
        // 3.6e15 km/h * 1000/3600 = 1e15 m/s exactly
        assertExact("1000000000000000", ms, "3.6e15 km/h = 1e15 m/s exactly");
    }

    @Test
    void verySmallKilometerPerHourConvertsExactly() {
        final BigNumber ms = converter.convertToBigNumber(
                "0.0000036", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
        );
        // 3.6e-6 km/h * 1000/3600 = 1e-6 m/s exactly = 0.000001
        assertExact("0.000001", ms, "3.6e-6 km/h = 1e-6 m/s exactly");
    }

    // ===================================================================
    // EXACT FRACTION FACTORY — direct test
    // ===================================================================

    @Test
    void rationalLinearFactoryAvoidsRoundingArtifacts() {
        final ConversionFormula formula = ConversionFormulas.linear(new BigNumber("1000"), new BigNumber("3600"));
        final BigNumber ms = formula.toBase(new BigNumber("45"), MC);
        assertExact("12.5", ms, "linear(1000, 3600).toBase(45) must be exactly 12.5");

        final BigNumber kmh = formula.fromBase(new BigNumber("12.5"), MC);
        assertExact("45", kmh, "linear(1000, 3600).fromBase(12.5) must be exactly 45");
    }

    // ===================================================================
    // EXACT FRACTION RECIPROCAL FACTORY — direct test
    // ===================================================================

    @Test
    void rationalReciprocalFactoryAvoidsRoundingArtifacts() {
        // 1 mi / 1 gal_US = 1609.344 / 3.785411784 m/L
        final ConversionFormula formula = ConversionFormulas.reciprocal(
                new BigNumber("1609.344"), new BigNumber("3.785411784")
        );
        // base = 1609.344 / (1 * 3.785411784) ≈ 425.1437074976...
        // Roundtrip: value = 1609.344 / (base * 3.785411784) must return exactly 1
        final BigNumber base = formula.toBase(new BigNumber("1"), MC);
        final BigNumber back = formula.fromBase(base, MC);
        // Reciprocal of a non-terminating ratio loses at most one ULP at MC precision tail
        final BigDecimal diff = back.toBigDecimal().subtract(BigDecimal.ONE).abs();
        assertTrue(diff.compareTo(new BigDecimal("1E-45")) < 0,
                "reciprocal(1609.344, 3.785411784) roundtrip within 1e-45, got " + back);
    }

    // ===================================================================
    // VOLUME — US gallon family is exact now (terminating)
    // ===================================================================

    @Test
    void oneUsGallonEqualsExactly0_003785411784CubicMeter() {
        final BigNumber m3 = converter.convertToBigNumber("1", Unit.Volume.US_GALLON, Unit.Volume.CUBIC_METER);
        assertExact("0.003785411784", m3, "1 US gal = 0.003785411784 m³ exactly");
    }

    @Test
    void oneUsGallonEqualsExactly4UsQuarts() {
        final BigNumber qt = converter.convertToBigNumber("1", Unit.Volume.US_GALLON, Unit.Volume.US_QUART);
        assertExact("4", qt, "1 US gal = 4 US qt exactly");
    }

    @Test
    void oneUsGallonEqualsExactly128UsFluidOunces() {
        final BigNumber fl = converter.convertToBigNumber("1", Unit.Volume.US_GALLON, Unit.Volume.US_FLUID_OUNCE);
        assertExact("128", fl, "1 US gal = 128 US fl oz exactly");
    }

    @Test
    void oneUsBarrelEqualsExactly31_5UsGallons() {
        final BigNumber gal = converter.convertToBigNumber("1", Unit.Volume.US_BARREL, Unit.Volume.US_GALLON);
        assertExact("31.5", gal, "1 US bbl = 31.5 US gal exactly");
    }

    @Test
    void oneTunEqualsExactly252UsGallons() {
        final BigNumber gal = converter.convertToBigNumber("1", Unit.Volume.TUN, Unit.Volume.US_GALLON);
        assertExact("252", gal, "1 tun = 252 US gal exactly");
    }

    @Test
    void oneHogsheadEqualsExactly63UsGallons() {
        final BigNumber gal = converter.convertToBigNumber("1", Unit.Volume.HOGSHEAD, Unit.Volume.US_GALLON);
        assertExact("63", gal, "1 hogshead = 63 US gal exactly");
    }

    @Test
    void oneOilBarrelEqualsExactly42UsGallons() {
        final BigNumber gal = converter.convertToBigNumber("1", Unit.Volume.OIL_BARREL, Unit.Volume.US_GALLON);
        assertExact("42", gal, "1 oil bbl = 42 US gal exactly");
    }

    // ===================================================================
    // VOLUME — UK gallon family
    // ===================================================================

    @Test
    void oneImperialGallonEqualsExactly4ImperialQuarts() {
        final BigNumber qt = converter.convertToBigNumber("1", Unit.Volume.IMPERIAL_GALLON, Unit.Volume.IMPERIAL_QUART);
        assertExact("4", qt, "1 UK gal = 4 UK qt exactly");
    }

    @Test
    void oneImperialGallonEqualsExactly160ImperialFluidOunces() {
        final BigNumber fl = converter.convertToBigNumber("1", Unit.Volume.IMPERIAL_GALLON, Unit.Volume.IMPERIAL_FLUID_OUNCE);
        assertExact("160", fl, "1 UK gal = 160 UK fl oz exactly");
    }

    @Test
    void oneImperialBarrelEqualsExactly36ImperialGallons() {
        final BigNumber gal = converter.convertToBigNumber("1", Unit.Volume.IMPERIAL_BARREL, Unit.Volume.IMPERIAL_GALLON);
        assertExact("36", gal, "1 UK bbl = 36 UK gal exactly");
    }

    // ===================================================================
    // VOLUME — cubic imperials
    // ===================================================================

    @Test
    void oneCubicFootEqualsExactly0_028316846592CubicMeter() {
        final BigNumber m3 = converter.convertToBigNumber("1", Unit.Volume.CUBIC_FOOT, Unit.Volume.CUBIC_METER);
        assertExact("0.028316846592", m3, "1 ft³ = 0.028316846592 m³ exactly");
    }

    @Test
    void oneCubicInchEqualsExactly0_000016387064CubicMeter() {
        final BigNumber m3 = converter.convertToBigNumber("1", Unit.Volume.CUBIC_INCH, Unit.Volume.CUBIC_METER);
        assertExact("0.000016387064", m3, "1 in³ = 0.000016387064 m³ exactly");
    }

    @Test
    void oneCubicYardEqualsExactly27CubicFeet() {
        final BigNumber ft3 = converter.convertToBigNumber("1", Unit.Volume.CUBIC_YARD, Unit.Volume.CUBIC_FOOT);
        assertExact("27", ft3, "1 yd³ = 27 ft³ exactly");
    }

    @Test
    void oneCordEqualsExactly128CubicFeet() {
        final BigNumber ft3 = converter.convertToBigNumber("1", Unit.Volume.CORD, Unit.Volume.CUBIC_FOOT);
        assertExact("128", ft3, "1 cord = 128 ft³ exactly");
    }

    @Test
    void oneBoardFootEqualsExactly144CubicInches() {
        final BigNumber in3 = converter.convertToBigNumber("1", Unit.Volume.BOARD_FOOT, Unit.Volume.CUBIC_INCH);
        assertExact("144", in3, "1 board foot = 144 in³ exactly");
    }

    @Test
    void oneAcreFootEqualsExactly43560CubicFeet() {
        // 1 acre = 43560 ft²; 1 acre·ft = 43560 ft³ exactly
        final BigNumber ft3 = converter.convertToBigNumber("1", Unit.Volume.ACRE_FOOT, Unit.Volume.CUBIC_FOOT);
        assertExact("43560", ft3, "1 acre·ft = 43560 ft³ exactly");
    }

    // ===================================================================
    // MASS — grain / ounce family
    // ===================================================================

    @Test
    void onePoundEqualsExactly16Ounces() {
        final BigNumber oz = converter.convertToBigNumber("1", Unit.Mass.POUND, Unit.Mass.OUNCE);
        assertExact("16", oz, "1 lb = 16 oz exactly");
    }

    @Test
    void onePoundEqualsExactly7000Grains() {
        final BigNumber gr = converter.convertToBigNumber("1", Unit.Mass.POUND, Unit.Mass.GRAIN);
        assertExact("7000", gr, "1 lb = 7000 grains exactly");
    }

    @Test
    void onePennyweightEqualsExactly24Grains() {
        final BigNumber gr = converter.convertToBigNumber("1", Unit.Mass.PENNYWEIGHT, Unit.Mass.GRAIN);
        assertExact("24", gr, "1 pennyweight = 24 grains exactly");
    }

    @Test
    void assayTonUsRationalRoundtripIsIdentity() {
        // Stored as defineFraction("0.175","6") — non-terminating decimal but exact rational
        final BigNumber back = converter.convertToBigNumber(
                converter.convertToBigNumber("3", Unit.Mass.ASSAY_TON_UNITED_STATES, Unit.Mass.KILOGRAM),
                Unit.Mass.KILOGRAM,
                Unit.Mass.ASSAY_TON_UNITED_STATES
        );
        final BigDecimal diff = back.toBigDecimal().subtract(new BigDecimal("3")).abs();
        assertTrue(diff.compareTo(new BigDecimal("1E-45")) < 0,
                "assay ton US roundtrip within 1e-45, got " + back);
    }

    @Test
    void assayTonUkRationalRoundtripIsIdentity() {
        // Stored as defineFraction("0.098","3") — non-terminating but exact rational
        final BigNumber back = converter.convertToBigNumber(
                converter.convertToBigNumber("3", Unit.Mass.ASSAY_TON_UNITED_KINGDOM, Unit.Mass.KILOGRAM),
                Unit.Mass.KILOGRAM,
                Unit.Mass.ASSAY_TON_UNITED_KINGDOM
        );
        final BigDecimal diff = back.toBigDecimal().subtract(new BigDecimal("3")).abs();
        assertTrue(diff.compareTo(new BigDecimal("1E-45")) < 0,
                "assay ton UK roundtrip within 1e-45, got " + back);
    }

    // ===================================================================
    // FUEL CONSUMPTION — rational linear factory in action
    // ===================================================================

    @Test
    void oneMeterPerGallonUsRoundtripIsIdentity() {
        final BigNumber back = converter.convertToBigNumber(
                converter.convertToBigNumber("1", Unit.FuelConsumption.METER_PER_GALLON_US, Unit.FuelConsumption.METER_PER_LITER),
                Unit.FuelConsumption.METER_PER_LITER,
                Unit.FuelConsumption.METER_PER_GALLON_US
        );
        final BigDecimal diff = back.toBigDecimal().subtract(BigDecimal.ONE).abs();
        assertTrue(diff.compareTo(new BigDecimal("1E-45")) < 0,
                "m/gal(US) roundtrip within 1e-45, got " + back);
    }

    @Test
    void oneKilometerPerGallonUsEqualsExactly1000MeterPerGallonUs() {
        final BigNumber mPerGal = converter.convertToBigNumber(
                "1", Unit.FuelConsumption.KILOMETER_PER_GALLON_US, Unit.FuelConsumption.METER_PER_GALLON_US
        );
        assertExact("1000", mPerGal, "1 km/gal(US) = 1000 m/gal(US) exactly");
    }

    @Test
    void milePerGallonUsRoundtripIsIdentity() {
        final BigNumber back = converter.convertToBigNumber(
                converter.convertToBigNumber("30", Unit.FuelConsumption.MILE_PER_GALLON_US, Unit.FuelConsumption.METER_PER_LITER),
                Unit.FuelConsumption.METER_PER_LITER,
                Unit.FuelConsumption.MILE_PER_GALLON_US
        );
        final BigDecimal diff = back.toBigDecimal().subtract(new BigDecimal("30")).abs();
        assertTrue(diff.compareTo(new BigDecimal("1E-43")) < 0,
                "30 mpg(US) roundtrip within 1e-43, got " + back);
    }

    @Test
    void fiveLitersPer100KmConvertsToExactlyTwentyKmPerLiter() {
        // 5 L/100km == 20 km/L via integer-only intermediate (100000 / 5 = 20000 m/L)
        final BigNumber kmPerL = converter.convertToBigNumber(
                "5",
                Unit.FuelConsumption.LITER_PER_100_KILOMETER,
                Unit.FuelConsumption.KILOMETER_PER_LITER
        );
        assertExact("20", kmPerL, "5 L/100km = 20 km/L exactly");
    }

    @Test
    void gallonUsPerMileRoundtripIsIdentity() {
        // Rational reciprocal: num=1609.344, den=3.785411784
        final BigNumber back = converter.convertToBigNumber(
                converter.convertToBigNumber("0.04", Unit.FuelConsumption.GALLON_US_PER_MILE, Unit.FuelConsumption.METER_PER_LITER),
                Unit.FuelConsumption.METER_PER_LITER,
                Unit.FuelConsumption.GALLON_US_PER_MILE
        );
        final BigDecimal diff = back.toBigDecimal().subtract(new BigDecimal("0.04")).abs();
        assertTrue(diff.compareTo(new BigDecimal("1E-45")) < 0,
                "0.04 gal(US)/mi roundtrip within 1e-45, got " + back);
    }

    @Test
    void gallonUkPer100MilesAndMilePerGallonUkAreReciprocal() {
        // 5 gal(UK)/100mi <-> 1/5 × 100 = 20 mi/gal(UK)
        final BigNumber mpg = converter.convertToBigNumber(
                "5",
                Unit.FuelConsumption.GALLON_UK_PER_100_MILES,
                Unit.FuelConsumption.MILE_PER_GALLON_UK
        );
        final BigDecimal diff = mpg.toBigDecimal().subtract(new BigDecimal("20")).abs();
        assertTrue(diff.compareTo(new BigDecimal("1E-40")) < 0,
                "5 gal(UK)/100mi → 20 mi/gal(UK) within 1e-40, got " + mpg);
    }

    // ===================================================================
    // LOCALE-AWARE UI FORMATTING — UnitValue must match the calling locale
    // ===================================================================

    @Test
    void unitValueToDisplayStringRespectsGermanLocale() {
        final BigNumber ms = converter.convertToBigNumber(
                "45", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
        );
        final UnitValue result = new UnitValue(ms, Unit.Speed.METER_PER_SECOND);
        final String german = result.toDisplayString(Locale.GERMANY);
        assertEquals("12,5 m/s", german, "German locale must render 12.5 as '12,5'");
    }

    @Test
    void unitValueToDisplayStringRespectsUsLocale() {
        final BigNumber ms = converter.convertToBigNumber(
                "45", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
        );
        final UnitValue result = new UnitValue(ms, Unit.Speed.METER_PER_SECOND);
        final String us = result.toDisplayString(Locale.US);
        assertEquals("12.5 m/s", us, "US locale must render 12.5 as '12.5'");
    }

    @Test
    void unitValuePrettyDisplayStringGroupsDigitsByLocale() {
        // 1 cubic mile is a large terminating value (4_168_181_825.440579584 m³)
        final BigNumber m3 = converter.convertToBigNumber(
                "1", Unit.Volume.CUBIC_MILE, Unit.Volume.CUBIC_METER
        );
        final UnitValue result = new UnitValue(m3, Unit.Volume.CUBIC_METER);
        final String pretty = result.toPrettyDisplayString(Locale.GERMANY);
        assertTrue(pretty.startsWith("4.168.181.825,440579584"),
                "Pretty German output should group with '.' and decimal ',' — got: " + pretty);
    }

    @Test
    void unitValueCompactStringWithLocale() {
        final BigNumber ms = converter.convertToBigNumber(
                "45", Unit.Speed.KILOMETER_PER_HOUR, Unit.Speed.METER_PER_SECOND
        );
        final UnitValue result = new UnitValue(ms, Unit.Speed.METER_PER_SECOND);
        assertEquals("12,5m/s", result.toCompactString(Locale.GERMANY), "Compact German: '12,5m/s'");
    }
}
