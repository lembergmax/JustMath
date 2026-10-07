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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards the generated reference documents. The test fails when {@code docs/units.md} differs from the unit registry.
 * Run it with {@code -Dupdate.docs=true} to write the current output to the file.
 */
class ReferenceDocumentsTest {

    private static final Path UNITS_DOCUMENT = Path.of("docs", "units.md");

    private static final String UPDATE_PROPERTY = "update.docs";

    private static void assertCurrent(final Path document, final String expected) throws IOException {
        if (Boolean.getBoolean(UPDATE_PROPERTY)) {
            Files.writeString(document, expected, StandardCharsets.UTF_8);
            return;
        }
        final String actual = Files.readString(document, StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertEquals(expected, actual, document + " is out of date. Run ./mvnw test -Dtest=ReferenceDocumentsTest -Dupdate.docs=true");
    }

    @Test
    @DisplayName("docs/units.md matches the unit registry")
    void unitsReferenceIsCurrent() throws IOException {
        assertCurrent(UNITS_DOCUMENT, ReferenceDocuments.renderUnits());
    }
}
