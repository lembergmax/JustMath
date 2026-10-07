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

package io.github.lembergmax.justmath.docs;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.converter.Unit;
import io.github.lembergmax.justmath.converter.UnitElements;

/**
 * Renders the reference documents that are generated from the library, so that they cannot drift from the code.
 * {@code ReferenceDocumentsTest} compares the output with the files in {@code docs/}.
 */
final class ReferenceDocuments {

    private static final String DEFINITIONS_RESOURCE = "/unit-audit/unit-definitions.tsv";

    private static final String EXCEPTIONS_RESOURCE = "/unit-audit/unit-exceptions.tsv";

    private static final MathContext CONVERSION_PRECISION = new MathContext(40, RoundingMode.HALF_EVEN);

    private static final MathContext DISPLAY_PRECISION = new MathContext(12, RoundingMode.HALF_EVEN);

    private static final BigDecimal PLAIN_NOTATION_MIN = new BigDecimal("0.0001");

    private static final BigDecimal PLAIN_NOTATION_MAX = new BigDecimal("1000000000");

    private static final List<Class<? extends Unit>> GROUP_ORDER = List.of(
            Unit.Length.class, Unit.Area.class, Unit.Volume.class, Unit.Mass.class, Unit.Temperature.class, Unit.Pressure.class,
            Unit.Energy.class, Unit.Power.class, Unit.Time.class, Unit.Force.class, Unit.Speed.class, Unit.FuelConsumption.class,
            Unit.DataStorage.class);

    private ReferenceDocuments() {
    }

    /**
     * Renders {@code docs/units.md}: one table per unit group with the value of one unit in the base unit of the group.
     *
     * @return the Markdown text with Unix line endings; never {@code null}
     */
    static String renderUnits() {
        final Map<String, String> basis = readBasis();
        final Map<Class<? extends Unit>, List<Unit>> groups = groupedInDocumentOrder();

        final StringBuilder text = new StringBuilder();
        text.append("# Units reference\n\n");
        text.append("This file is generated from the unit registry by `ReferenceDocumentsTest`. Do not edit it by hand: run\n");
        text.append("`./mvnw test -Dtest=ReferenceDocumentsTest -Dupdate.docs=true` after you change a unit.\n\n");
        text.append("JustMath has ").append(UnitElements.all().size()).append(" units in ").append(groups.size()).append(" groups. ");
        text.append("A conversion is only defined inside a group: it goes through the base unit of the group.\n\n");
        text.append("The value column is the value of one unit in the base unit, rounded to 12 significant digits for this table. ");
        text.append("The registry holds the exact definition. For a temperature, the column is the value of one unit in degrees Celsius; ");
        text.append("for a unit of fuel consumption that is written as a quantity per distance, such as `L/100 km`, it is the value ");
        text.append("of the reciprocal form.\n\n");
        text.append("The basis column says where the value comes from. *exact* is a definition (SI, the international yard and pound, ");
        text.append("US customary or imperial measures) that the registry holds as an exact ratio, *measured* is a physical or ");
        text.append("astronomical constant with the digits of the source (CODATA 2022, WGS 84, IAU), *exact (pi)* contains pi, ");
        text.append("and *convention* is a historic, regional or rounded value without a definition. ");
        text.append("[unit-audit.md](unit-audit.md) lists the sources and the units of the last class with the reason.\n\n");

        text.append("| Group | Units | Base unit |\n| --- | ---: | --- |\n");
        for (final Map.Entry<Class<? extends Unit>, List<Unit>> group : groups.entrySet()) {
            final Unit base = UnitElements.getBaseUnit(group.getKey());
            text.append("| [").append(group.getKey().getSimpleName()).append("](#").append(anchor(group.getKey().getSimpleName())).append(") | ")
                    .append(group.getValue().size()).append(" | ").append(UnitElements.getDisplayName(base)).append(" (`")
                    .append(UnitElements.getSymbol(base)).append("`) |\n");
        }

        for (final Map.Entry<Class<? extends Unit>, List<Unit>> group : groups.entrySet()) {
            final Unit base = UnitElements.getBaseUnit(group.getKey());
            text.append("\n## ").append(group.getKey().getSimpleName()).append("\n\n");
            text.append("Enum `Unit.").append(group.getKey().getSimpleName()).append("`, base unit ").append(UnitElements.getDisplayName(base))
                    .append(" (`").append(UnitElements.getSymbol(base)).append("`).\n\n");
            text.append("| Constant | Name | Symbol | 1 unit in base | Basis |\n| --- | --- | --- | ---: | --- |\n");
            for (final Unit unit : group.getValue()) {
                final String constant = ((Enum<?>) unit).name();
                final String key = group.getKey().getSimpleName() + "." + constant;
                text.append("| `").append(constant).append("` | ").append(escape(UnitElements.getDisplayName(unit))).append(" | `")
                        .append(escape(UnitElements.getSymbol(unit))).append("` | ").append(display(UnitElements.toBase(unit, new BigNumber("1"), CONVERSION_PRECISION).toBigDecimal()))
                        .append(" | ").append(basis.getOrDefault(key, "convention")).append(" |\n");
            }
        }
        return text.toString();
    }

    private static Map<Class<? extends Unit>, List<Unit>> groupedInDocumentOrder() {
        final Map<Class<? extends Unit>, List<Unit>> groups = new LinkedHashMap<>();
        for (final Class<? extends Unit> group : GROUP_ORDER) {
            groups.put(group, new java.util.ArrayList<>());
        }
        for (final Unit unit : UnitElements.all()) {
            groups.get(UnitElements.getGroup(unit)).add(unit);
        }
        return groups;
    }

    private static Map<String, String> readBasis() {
        final Map<String, String> basis = new LinkedHashMap<>();
        for (final String[] row : readRows(DEFINITIONS_RESOURCE)) {
            basis.put(row[0], switch (row[1]) {
                case "EXACT" -> "exact";
                case "DECIMAL" -> "measured";
                case "IRRATIONAL" -> "exact (pi)";
                default -> throw new IllegalStateException("unknown kind " + row[1]);
            });
        }
        for (final String[] row : readRows(EXCEPTIONS_RESOURCE)) {
            basis.put(row[0], "convention");
        }
        return basis;
    }

    private static List<String[]> readRows(final String resource) {
        try (InputStream stream = ReferenceDocuments.class.getResourceAsStream(resource)) {
            final String content = new String(java.util.Objects.requireNonNull(stream, resource).readAllBytes(), StandardCharsets.UTF_8);
            final List<String[]> rows = new java.util.ArrayList<>();
            for (final String line : content.split("\\R")) {
                if (!line.isBlank() && !line.startsWith("#")) {
                    rows.add(line.split("\t", -1));
                }
            }
            return rows;
        } catch (final IOException exception) {
            throw new IllegalStateException("cannot read " + resource, exception);
        }
    }

    private static String display(final BigDecimal value) {
        if (value.signum() == 0) {
            return "0";
        }
        final BigDecimal rounded = value.round(DISPLAY_PRECISION).stripTrailingZeros();
        final BigDecimal magnitude = rounded.abs();
        if (magnitude.compareTo(PLAIN_NOTATION_MIN) >= 0 && magnitude.compareTo(PLAIN_NOTATION_MAX) < 0) {
            return rounded.toPlainString();
        }
        return rounded.toString().replace("E+", "E");
    }

    private static String escape(final String text) {
        return text.replace("|", "\\|");
    }

    private static String anchor(final String heading) {
        return heading.toLowerCase(java.util.Locale.ROOT).replace(' ', '-');
    }
}
