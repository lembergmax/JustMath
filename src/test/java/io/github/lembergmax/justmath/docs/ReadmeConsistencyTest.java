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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.lembergmax.justmath.calculator.CalculatorEngine;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.expression.ExpressionElements;
import io.github.lembergmax.justmath.converter.UnitElements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Keeps the README true: the version in the install snippet, the counts that it states and the list of the functions
 * and the error codes are compared with the code, so that the README cannot drift when a function, a unit or a code is
 * added.
 */
class ReadmeConsistencyTest {

    private static final Pattern PROJECT_VERSION = Pattern.compile("<artifactId>justmath</artifactId>\\s*<version>([^<]+)</version>");

    private static final String INVERSE_MARK = "⁻¹";

    private static String readme;

    @BeforeAll
    static void readReadme() throws IOException {
        readme = Files.readString(Path.of("README.md"), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("the install snippets name the version of the project")
    void installSnippetsNameTheProjectVersion() throws IOException {
        final Matcher version = PROJECT_VERSION.matcher(Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8));
        assertTrue(version.find(), "pom.xml has no project version");

        assertTrue(readme.contains("<version>" + version.group(1) + "</version>"), "the Maven snippet does not name " + version.group(1));
        assertTrue(readme.contains("io.github.lembergmax:justmath:" + version.group(1)), "the Gradle snippet does not name " + version.group(1));
    }

    @Test
    @DisplayName("every operator, constant and function of the registry is in the README")
    void everyRegisteredSymbolIsDocumented() {
        final List<String> missing = new ArrayList<>();

        for (final String symbol : ExpressionElements.getRegistry().keySet()) {
            final boolean punctuation = symbol.equals("(") || symbol.equals(")") || symbol.equals(";") || symbol.equals("|");
            final boolean inverseAlias = symbol.contains(INVERSE_MARK);
            if (!punctuation && !inverseAlias && !readme.contains(symbol)) {
                missing.add(symbol);
            }
        }

        assertTrue(missing.isEmpty(), "symbols missing from the README: " + missing);
        assertTrue(readme.contains(INVERSE_MARK), "the README does not mention the inverse notation");
    }

    @Test
    @DisplayName("the README states the number of units and groups of the registry")
    void unitCountsMatchTheRegistry() {
        final long groups = UnitElements.all().stream().map(UnitElements::getGroup).distinct().count();

        assertTrue(readme.contains(UnitElements.all().size() + " units in " + groups + " groups"),
                "the README must say '" + UnitElements.all().size() + " units in " + groups + " groups'");
    }

    @Test
    @DisplayName("every error code and the number of codes are in the README")
    void errorCodesMatchTheEnum() {
        final List<String> missing = new ArrayList<>();
        for (final CalculatorErrorCode code : CalculatorErrorCode.values()) {
            if (!readme.contains("`" + code.name() + "`")) {
                missing.add(code.name());
            }
        }

        assertTrue(missing.isEmpty(), "error codes missing from the README: " + missing);
        assertTrue(readme.contains("There are " + CalculatorErrorCode.values().length + " codes"), "the README must say 'There are " + CalculatorErrorCode.values().length + " codes'");
    }

    @Test
    @DisplayName("the README states the number of supported locales")
    void supportedLocalesMatchTheRegistry() {
        assertTrue(readme.contains("20 locales of 13 languages") && CalculatorEngine.getSupportedLanguages().size() == 20,
                "the README and CalculatorEngine.getSupportedLanguages() must agree on 20 locales");
    }
}
