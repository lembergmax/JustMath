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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the relative links of the Markdown documents: the target file exists and, for a link into a Markdown file,
 * so does the heading that the anchor names. Links to other sites are not checked here, because a test must not depend
 * on the network; the workflow {@code links.yml} checks them.
 */
class MarkdownLinksTest {

    private static final Pattern LINK = Pattern.compile("!?\\[[^\\]]*]\\(([^)\\s]+)(?:\\s+\"[^\"]*\")?\\)");

    private static final Pattern HEADING = Pattern.compile("^#{1,6}\\s+(.+?)\\s*#*\\s*$");

    private static final Pattern FENCE = Pattern.compile("^\\s*(```|~~~)");

    private static List<Path> documents() throws IOException {
        final List<Path> documents = new ArrayList<>();
        for (final String name : List.of("README.md", "CHANGELOG.md", "SECURITY.md", "CONTRIBUTING.md", "CODE_OF_CONDUCT.md", "SUPPORT.md")) {
            if (Files.exists(Path.of(name))) {
                documents.add(Path.of(name));
            }
        }
        try (Stream<Path> files = Files.walk(Path.of("docs"))) {
            files.filter(path -> path.toString().endsWith(".md")).sorted().forEach(documents::add);
        }
        return documents;
    }

    private static List<String> linesOutsideCodeBlocks(final Path document) throws IOException {
        final List<String> lines = new ArrayList<>();
        boolean inFence = false;
        for (final String line : Files.readAllLines(document, StandardCharsets.UTF_8)) {
            if (FENCE.matcher(line).find()) {
                inFence = !inFence;
                continue;
            }
            if (!inFence) {
                lines.add(line);
            }
        }
        return lines;
    }

    static Set<String> anchorsOf(final Path document) throws IOException {
        final Set<String> anchors = new HashSet<>();
        final Set<String> seen = new HashSet<>();
        for (final String line : linesOutsideCodeBlocks(document)) {
            final Matcher heading = HEADING.matcher(line);
            if (heading.matches()) {
                final String base = anchorOf(heading.group(1));
                String anchor = base;
                for (int duplicate = 1; !seen.add(anchor); duplicate++) {
                    anchor = base + "-" + duplicate;
                }
                anchors.add(anchor);
            }
        }
        return anchors;
    }

    static String anchorOf(final String heading) {
        final String withoutMarkup = heading.replaceAll("`", "").replaceAll("\\[([^\\]]*)]\\([^)]*\\)", "$1").replaceAll("[*_]", "");
        final StringBuilder anchor = new StringBuilder();
        for (final char character : withoutMarkup.toLowerCase(Locale.ROOT).toCharArray()) {
            if (Character.isLetterOrDigit(character) || character == '-' || character == '_') {
                anchor.append(character);
            } else if (character == ' ') {
                anchor.append('-');
            }
        }
        return anchor.toString();
    }

    @Test
    @DisplayName("every relative link of the Markdown documents points to an existing file and heading")
    void relativeLinksResolve() throws IOException {
        final List<String> broken = new ArrayList<>();

        for (final Path document : documents()) {
            for (final String line : linesOutsideCodeBlocks(document)) {
                final Matcher matcher = LINK.matcher(line);
                while (matcher.find()) {
                    final String target = matcher.group(1);
                    if (target.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")) {
                        continue;
                    }
                    final int hash = target.indexOf('#');
                    final String path = URLDecoder.decode(hash < 0 ? target : target.substring(0, hash), StandardCharsets.UTF_8);
                    final String anchor = hash < 0 ? "" : target.substring(hash + 1);
                    final Path resolved = path.isEmpty() ? document : document.resolveSibling(path).normalize();
                    if (!Files.exists(resolved)) {
                        broken.add(document + ": " + target + " (no such file)");
                    } else if (!anchor.isEmpty() && resolved.toString().endsWith(".md") && !anchorsOf(resolved).contains(anchor)) {
                        broken.add(document + ": " + target + " (no such heading)");
                    }
                }
            }
        }

        assertTrue(broken.isEmpty(), () -> "broken links:" + System.lineSeparator() + String.join(System.lineSeparator(), broken));
    }
}
