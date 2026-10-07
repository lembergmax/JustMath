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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks every built-in unit against an independent definition.
 *
 * <p>{@code unit-definitions.tsv} lists the units whose value follows from a definition, with the value derived
 * from the defining constants (inch, pound, standard gravity, the calorie, the IEC prefixes and so on) and the
 * source. {@code unit-exceptions.tsv} lists the units that no definition fixes, with the reason. Every unit of
 * {@link UnitElements#all()} must be in exactly one of the two files, so a new unit forces a decision.</p>
 */
final class UnitDefinitionAuditTest {

    private static final String DEFINITIONS_RESOURCE = "/unit-audit/unit-definitions.tsv";

    private static final String EXCEPTIONS_RESOURCE = "/unit-audit/unit-exceptions.tsv";

    private static final MathContext CONVERSION_PRECISION = new MathContext(120);

    private static final MathContext REFERENCE_PRECISION = new MathContext(130);

    private static final BigDecimal EXACT_TOLERANCE = new BigDecimal("1E-100");

    private static final BigDecimal IRRATIONAL_TOLERANCE = new BigDecimal("1E-40");

    private enum Kind {
        EXACT, DECIMAL, IRRATIONAL
    }

    private record Definition(String constant, Kind kind, String value, String source) {

        @Override
        public String toString() {
            return constant + " (" + kind + ")";
        }
    }

    private static List<String[]> readRows(final String resource) throws IOException {
        try (InputStream stream = UnitDefinitionAuditTest.class.getResourceAsStream(resource)) {
            assertTrue(stream != null, resource + " must be on the test classpath");
            final String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            final List<String[]> rows = new ArrayList<>();
            for (final String line : content.split("\\R")) {
                if (!line.isBlank() && !line.startsWith("#")) {
                    rows.add(line.split("\t", -1));
                }
            }
            return rows;
        }
    }

    private static List<Definition> readDefinitions() throws IOException {
        final List<Definition> definitions = new ArrayList<>();
        for (final String[] row : readRows(DEFINITIONS_RESOURCE)) {
            definitions.add(new Definition(row[0], Kind.valueOf(row[1]), row[2], row[3]));
        }
        return definitions;
    }

    static Stream<Definition> definitions() throws IOException {
        return readDefinitions().stream();
    }

    private static String keyOf(final Unit unit) {
        final Enum<?> constant = (Enum<?>) unit;
        return constant.getDeclaringClass().getSimpleName() + "." + constant.name();
    }

    private static Unit unitOf(final String key) {
        return UnitElements.all().stream()
                .filter(unit -> keyOf(unit).equals(key))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no unit " + key));
    }

    private static BigDecimal expectedValue(final Definition definition) {
        final String value = definition.value();
        final int slash = value.indexOf('/');
        if (slash < 0) {
            return new BigDecimal(value);
        }
        return new BigDecimal(value.substring(0, slash)).divide(new BigDecimal(value.substring(slash + 1)), REFERENCE_PRECISION);
    }

    private static BigDecimal valueInBaseUnit(final Unit unit) {
        return UnitElements.toBase(unit, new BigNumber("1"), CONVERSION_PRECISION).toBigDecimal();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("definitions")
    @DisplayName("a unit has the value that its definition gives")
    void unitMatchesItsDefinition(final Definition definition) {
        final BigDecimal expected = expectedValue(definition);
        final BigDecimal actual = valueInBaseUnit(unitOf(definition.constant()));

        if (definition.kind() == Kind.DECIMAL) {
            assertEquals(0, expected.compareTo(actual),
                    definition.constant() + ": expected " + expected.toPlainString() + " (" + definition.source() + ") but was " + actual.toPlainString());
            return;
        }

        final BigDecimal tolerance = definition.kind() == Kind.EXACT ? EXACT_TOLERANCE : IRRATIONAL_TOLERANCE;
        final BigDecimal allowedError = expected.abs().multiply(tolerance);
        assertTrue(expected.subtract(actual).abs().compareTo(allowedError) <= 0,
                definition.constant() + ": expected " + expected.round(new MathContext(40)) + " (" + definition.source() + ") but was " + actual.round(new MathContext(40)));
    }

    @Test
    @DisplayName("every unit is either checked against a definition or listed as an exception, never both")
    void everyUnitIsClassifiedExactlyOnce() throws IOException {
        final Set<String> allUnits = new TreeSet<>();
        UnitElements.all().forEach(unit -> allUnits.add(keyOf(unit)));

        final List<String> defined = new ArrayList<>();
        readDefinitions().forEach(definition -> defined.add(definition.constant()));
        final List<String> excepted = new ArrayList<>();
        readRows(EXCEPTIONS_RESOURCE).forEach(row -> excepted.add(row[0]));

        final Set<String> classified = new TreeSet<>(defined);
        classified.addAll(excepted);

        assertEquals(defined.size(), new TreeSet<>(defined).size(), "a unit is defined twice");
        assertEquals(excepted.size(), new TreeSet<>(excepted).size(), "a unit is listed as an exception twice");
        assertEquals(defined.size() + excepted.size(), classified.size(), "a unit is both defined and an exception");

        final Set<String> unclassified = new TreeSet<>(allUnits);
        unclassified.removeAll(classified);
        assertTrue(unclassified.isEmpty(), "units without a definition or an exception: " + unclassified);

        final Set<String> unknown = new TreeSet<>(classified);
        unknown.removeAll(allUnits);
        assertTrue(unknown.isEmpty(), "audit files list units that do not exist: " + unknown);
    }

    @Test
    @DisplayName("every exception states why the unit has no definition to check against")
    void everyExceptionHasAReason() throws IOException {
        for (final String[] row : readRows(EXCEPTIONS_RESOURCE)) {
            assertEquals(2, row.length, "exception rows have a constant and a reason: " + String.join(" | ", row));
            assertFalse(row[1].isBlank(), row[0] + " has no reason");
        }
    }

    @Test
    @DisplayName("every definition names its source")
    void everyDefinitionHasASource() throws IOException {
        for (final Definition definition : readDefinitions()) {
            assertFalse(definition.source().isBlank(), definition.constant() + " has no source");
        }
    }

    @Test
    @DisplayName("the dalton and the unified atomic mass unit are the same unit")
    void daltonEqualsAtomicMassUnit() {
        assertEquals(0, valueInBaseUnit(Unit.Mass.DALTON).compareTo(valueInBaseUnit(Unit.Mass.ATOMIC_MASS_UNIT)));
    }

    @Test
    @DisplayName("a US ton of TNT and a thermochemical kilocalorie follow from the thermochemical calorie")
    void thermochemicalUnitsShareTheCalorie() {
        final BigDecimal calorie = valueInBaseUnit(Unit.Energy.CALORIE_TH);
        assertEquals(0, new BigDecimal("4.184").compareTo(calorie));
        assertEquals(0, calorie.multiply(new BigDecimal(1000)).compareTo(valueInBaseUnit(Unit.Energy.KILOCALORIE_TH)));
        assertEquals(0, calorie.multiply(new BigDecimal(1_000_000_000)).compareTo(valueInBaseUnit(Unit.Energy.TON_TNT)));
    }

    @Test
    @DisplayName("the ken is six shaku of 10/33 m, about 1.818 m")
    void kenIsSixShaku() {
        final BigDecimal ken = valueInBaseUnit(Unit.Length.KEN);
        final BigDecimal sixShaku = new BigDecimal(60).divide(new BigDecimal(33), CONVERSION_PRECISION);

        assertTrue(ken.subtract(sixShaku).abs().compareTo(new BigDecimal("1E-100")) <= 0, "1 ken in m was " + ken.round(new MathContext(20)));
    }
}
