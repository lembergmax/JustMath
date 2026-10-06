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

package io.github.lembergmax.justmath.bignumber.math;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards the rule that a domain error in the math layer is thrown as a
 * {@code MathArithmeticException} or a {@code MathArgumentException}, never as a bare JDK exception.
 * The {@code CalculatorEngine} reads the error code from the typed exception. A bare
 * {@code ArithmeticException} or {@code IllegalArgumentException} would surface as an internal error
 * with no code to localize.
 *
 * <p>Internal preconditions are the only exception. They guard against bugs in the library, not
 * against bad input, and are listed in {@link #INTERNAL_PRECONDITIONS}.</p>
 */
class UntypedMathExceptionGuardTest {

    private static final Path SOURCE_ROOT = Path.of("src", "main", "java", "io", "github", "lembergmax", "justmath", "bignumber");

    private static final List<Path> GUARDED_SOURCES = List.of(SOURCE_ROOT.resolve("math"), SOURCE_ROOT.resolve("BigNumbers.java"));

    private static final Pattern UNTYPED_THROW = Pattern.compile("throw\\s+new\\s+(ArithmeticException|IllegalArgumentException)\\s*\\(([^;]*);");

    private static final List<String> INTERNAL_PRECONDITIONS = List.of(
            "\"Empty number string\"",
            "\"Exponent notation is not supported: \"",
            "\"Invalid number string: \"",
            "\"Invalid digit: '\"",
            "\"Divisor must be positive.\"",
            "\"digitValue must be in range 0..9\"",
            "\"buffer is too small: required at least \"",
            "\"Exponent must be an integer for integer power.\"",
            "\"Expected scalar BigNumber but got: \"",
            "MatrixMessages.get("
    );

    @Test
    @DisplayName("the math layer throws no bare ArithmeticException or IllegalArgumentException for a domain error")
    void noUntypedDomainErrors() {
        final List<String> violations = new ArrayList<>();
        for (final Path file : javaFilesUnder(GUARDED_SOURCES)) {
            collectViolations(file, violations);
        }

        assertTrue(violations.isEmpty(), () -> "Throw MathArithmeticException or MathArgumentException with a "
                + "CalculatorErrorCode instead, or list the site in INTERNAL_PRECONDITIONS if it guards against a "
                + "bug in the library:\n" + String.join("\n", violations));
    }

    private static List<Path> javaFilesUnder(final List<Path> roots) {
        final List<Path> files = new ArrayList<>();
        for (final Path root : roots) {
            assertTrue(Files.exists(root), () -> root + " must exist; run the tests from the project root");
            try (Stream<Path> walk = Files.walk(root)) {
                walk.filter(path -> path.toString().endsWith(".java")).forEach(files::add);
            } catch (final IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
        return files;
    }

    private static void collectViolations(final Path file, final List<String> violations) {
        final String source = read(file);
        final Matcher matcher = UNTYPED_THROW.matcher(source);
        while (matcher.find()) {
            final String exceptionType = matcher.group(1);
            final String arguments = matcher.group(2).strip();
            if (isInternalPrecondition(exceptionType, arguments)) {
                continue;
            }
            final long line = source.substring(0, matcher.start()).chars().filter(character -> character == '\n').count() + 1;
            violations.add(file + ":" + line + "  new " + exceptionType + "(" + abbreviate(arguments) + ")");
        }
    }

    private static boolean isInternalPrecondition(final String exceptionType, final String arguments) {
        return "IllegalArgumentException".equals(exceptionType)
                && INTERNAL_PRECONDITIONS.stream().anyMatch(arguments::startsWith);
    }

    private static String abbreviate(final String arguments) {
        final String singleLine = arguments.replaceAll("\\s+", " ");
        return singleLine.length() <= 80 ? singleLine : singleLine.substring(0, 80) + "...";
    }

    private static String read(final Path file) {
        try {
            return Files.readString(file);
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

}
