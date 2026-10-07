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

package io.github.lembergmax.justmath;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Keeps the package structure that {@code docs/architecture.md} describes. The test reads the imports of the main
 * sources and fails when a package starts to depend on a package that it must not know, for example when the unit
 * converter imports the calculator or when the expression model imports the pipeline.
 */
class PackageDependencyTest {

    private static final String ROOT_PACKAGE = "io.github.lembergmax.justmath";

    private static final Path SOURCE_ROOT = Path.of("src", "main", "java", "io", "github", "lembergmax", "justmath");

    private static final Pattern IMPORT = Pattern.compile("^import\\s+(?:static\\s+)?(io\\.github\\.lembergmax\\.justmath(?:\\.[a-z][a-z0-9]*)*)(?:\\.[A-Z][\\w]*)?", Pattern.MULTILINE);

    private static final Map<String, Set<String>> ALLOWED = Map.ofEntries(
            Map.entry("exceptions", Set.of()),
            Map.entry("bignumber.algorithms", Set.of("bignumber")),
            Map.entry("bignumber.matrix", Set.of("bignumber")),
            Map.entry("bignumber.math.exceptions", Set.of("calculator.errors", "exceptions")),
            Map.entry("calculator.errors", Set.of("calculator.exceptions", "exceptions")),
            Map.entry("calculator.exceptions", Set.of("calculator.errors", "exceptions")),
            Map.entry("calculator.expression.operations", Set.of("bignumber")),
            Map.entry("calculator.expression.operations.function", Set.of("bignumber", "calculator.internal")),
            Map.entry("calculator.expression.operations.operator", Set.of("bignumber")),
            Map.entry("converter", Set.of("bignumber", "bignumber.internal", "converter.exception")),
            Map.entry("converter.exception", Set.of("exceptions")));

    private static Map<String, Set<String>> readDependencies() throws IOException {
        final Map<String, Set<String>> dependencies = new TreeMap<>();
        try (Stream<Path> files = Files.walk(SOURCE_ROOT)) {
            for (final Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                final String packageName = packageOf(file);
                final String source = Files.readString(file);
                final Matcher matcher = IMPORT.matcher(source);
                while (matcher.find()) {
                    final String imported = relative(matcher.group(1));
                    if (!imported.equals(packageName)) {
                        dependencies.computeIfAbsent(packageName, key -> new TreeSet<>()).add(imported);
                    }
                }
            }
        }
        return dependencies;
    }

    private static String packageOf(final Path file) {
        final Path directory = SOURCE_ROOT.relativize(file.getParent());
        return directory.toString().replace('\\', '.').replace('/', '.');
    }

    private static String relative(final String packageName) {
        return packageName.equals(ROOT_PACKAGE) ? "" : packageName.substring(ROOT_PACKAGE.length() + 1);
    }

    @Test
    @DisplayName("a package depends only on the packages that docs/architecture.md allows")
    void packagesDependOnlyOnAllowedPackages() throws IOException {
        final Map<String, Set<String>> dependencies = readDependencies();
        final List<String> violations = new ArrayList<>();

        for (final Map.Entry<String, Set<String>> rule : ALLOWED.entrySet()) {
            for (final String dependency : dependencies.getOrDefault(rule.getKey(), Set.of())) {
                if (!rule.getValue().contains(dependency)) {
                    violations.add(rule.getKey() + " must not depend on " + dependency);
                }
            }
        }

        assertTrue(violations.isEmpty(), () -> String.join(System.lineSeparator(), violations));
    }

    @Test
    @DisplayName("no package of the library depends on the root package that holds the demo class")
    void nothingDependsOnTheRootPackage() throws IOException {
        final List<String> violations = new ArrayList<>();

        for (final Map.Entry<String, Set<String>> entry : readDependencies().entrySet()) {
            if (entry.getValue().contains("")) {
                violations.add(entry.getKey() + " depends on the root package");
            }
        }

        assertTrue(violations.isEmpty(), () -> String.join(System.lineSeparator(), violations));
    }

    @Test
    @DisplayName("the expression model does not reach into the pipeline of tokenizer, parser and evaluator")
    void theExpressionModelDoesNotKnowThePipeline() throws IOException {
        final List<String> violations = new ArrayList<>();
        final Pattern pipelineImport = Pattern.compile("^import\\s+io\\.github\\.lembergmax\\.justmath\\.calculator\\.(Tokenizer|PostfixParser|Evaluator|Token|CalculatorEngine\\w*)\\b", Pattern.MULTILINE);

        try (Stream<Path> files = Files.walk(SOURCE_ROOT.resolve("calculator").resolve("expression"))) {
            for (final Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                if (pipelineImport.matcher(Files.readString(file)).find()) {
                    violations.add(SOURCE_ROOT.relativize(file) + " imports a pipeline class");
                }
            }
        }

        assertTrue(violations.isEmpty(), () -> String.join(System.lineSeparator(), violations));
    }
}
