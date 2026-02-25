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

import com.mlprograms.justmath.converter.exception.UnitConversionException;
import lombok.experimental.UtilityClass;

/**
 * Test-only helper utilities for unit tests.
 *
 * <p>
 * This class centralizes parsing and formatting logic used by CSV-like test rows.
 * Keeping this logic in one place makes failures easier to interpret and reduces duplication.
 * </p>
 */
@UtilityClass
final class UnitTestSupport {

    /**
     * Parses a qualified unit name used in CSV-like test rows, e.g. {@code "Unit.Length.METER"} or {@code "Unit.Mass.KILOGRAM"}.
     *
     * <p>
     * Supported formats:
     * </p>
     * <ul>
     *   <li>{@code Unit.<Group>.<UNIT>}</li>
     *   <li>{@code <Group>.<UNIT>} (prefix {@code Unit.} optional)</li>
     * </ul>
     *
     * @param qualifiedName qualified unit name; must not be {@code null} or blank
     * @return parsed unit identifier; never {@code null}
     * @throws UnitConversionException if the name is invalid or group is unknown
     */
    static Unit parseQualifiedUnitName(final String qualifiedName) {
        if (qualifiedName == null) {
            throw new UnitConversionException("Unit name must not be null.");
        }

        final String trimmed = qualifiedName.trim();
        if (trimmed.isEmpty()) {
            throw new UnitConversionException("Unit name must not be blank.");
        }

        final String withoutPrefix = trimmed.startsWith("Unit.") ? trimmed.substring("Unit.".length()) : trimmed;
        final String[] parts = withoutPrefix.split("\\.");
        if (parts.length != 2) {
            throw new UnitConversionException(
                    "Invalid unit name '" + qualifiedName + "'. Expected format: 'Unit.<Group>.<UNIT>'."
            );
        }

        final String group = parts[0];
        final String unitName = parts[1];

        return switch (group) {
            case "Length" -> Unit.Length.valueOf(unitName);
            case "Mass" -> Unit.Mass.valueOf(unitName);
            default -> throw new UnitConversionException("Unknown unit group in '" + qualifiedName + "': " + group);
        };
    }

    /**
     * Returns a qualified unit name that matches the CSV-like test row format.
     *
     * @param unit unit identifier; must not be {@code null}
     * @return qualified name such as {@code "Unit.Length.METER"}; never {@code null}
     */
    static String qualifiedName(final Unit unit) {
        if (unit instanceof Unit.Length length) {
            return "Unit.Length." + length.name();
        }
        if (unit instanceof Unit.Mass mass) {
            return "Unit.Mass." + mass.name();
        }
        return "Unit.<Unknown>." + unit;
    }

}