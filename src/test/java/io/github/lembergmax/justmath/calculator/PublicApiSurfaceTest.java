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

package io.github.lembergmax.justmath.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the public types of the calculator packages. The tokenizer, the parser and the helpers of the
 * evaluation pipeline are implementation details and must not be reachable from outside the package, so
 * that they can change in a minor release.
 */
class PublicApiSurfaceTest {

    private static final String CALCULATOR_PACKAGE = "io.github.lembergmax.justmath.calculator";

    private static final String INTERNAL_PACKAGE = CALCULATOR_PACKAGE + ".internal";

    @Test
    @DisplayName("the calculator package exposes only the engine and the language registry")
    void calculatorPackageExposesOnlyTheEngineAndTheLanguageRegistry() {
        assertEquals(Set.of("CalculatorEngine", "SupportedLanguages"), publicTopLevelTypes(CALCULATOR_PACKAGE));
    }

    @Test
    @DisplayName("the internal package exposes only the two enums that appear in public signatures")
    void internalPackageExposesOnlyTheEnumsOfPublicSignatures() {
        assertEquals(Set.of("CoordinateType", "TrigonometricMode"), publicTopLevelTypes(INTERNAL_PACKAGE));
    }

    private static Set<String> publicTopLevelTypes(final String packageName) {
        final Set<String> publicTypes = new TreeSet<>();
        for (final String simpleName : topLevelTypeNames(packageName)) {
            if (Modifier.isPublic(load(packageName + "." + simpleName).getModifiers())) {
                publicTypes.add(simpleName);
            }
        }
        return publicTypes;
    }

    private static Set<String> topLevelTypeNames(final String packageName) {
        final Path packageDirectory = classesDirectory().resolve(packageName.replace('.', '/'));
        try (Stream<Path> files = Files.list(packageDirectory)) {
            final Set<String> names = new TreeSet<>();
            files.map(path -> path.getFileName().toString())
                    .filter(fileName -> fileName.endsWith(".class"))
                    .filter(fileName -> !fileName.contains("$"))
                    .filter(fileName -> !fileName.equals("package-info.class"))
                    .map(fileName -> fileName.substring(0, fileName.length() - ".class".length()))
                    .forEach(names::add);
            return names;
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static Path classesDirectory() {
        try {
            final Path location = Path.of(CalculatorEngine.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            assertTrue(Files.isDirectory(location), "the library classes must be a directory, was " + location);
            return location;
        } catch (final URISyntaxException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Class<?> load(final String className) {
        try {
            return Class.forName(className, false, CalculatorEngine.class.getClassLoader());
        } catch (final ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

}
