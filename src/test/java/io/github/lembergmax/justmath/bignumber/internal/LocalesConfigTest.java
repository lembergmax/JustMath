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
package io.github.lembergmax.justmath.bignumber.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

/**
 * Regression tests for the locale list used by the number parser. The list used to be a public
 * {@code Locale[]}: any code in the JVM could overwrite an element and silently change how every
 * {@code new BigNumber(String)} detects its locale.
 */
class LocalesConfigTest {

    @Test
    @DisplayName("SUPPORTED_LOCALES cannot be modified by callers")
    void supportedLocalesAreUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> LocalesConfig.SUPPORTED_LOCALES.set(0, Locale.GERMANY));
        assertThrows(UnsupportedOperationException.class, () -> LocalesConfig.SUPPORTED_LOCALES.add(Locale.GERMANY));
        assertThrows(UnsupportedOperationException.class, () -> LocalesConfig.SUPPORTED_LOCALES.remove(0));
        assertThrows(UnsupportedOperationException.class, LocalesConfig.SUPPORTED_LOCALES::clear);
    }

    @Test
    @DisplayName("an attempted modification does not change how a plain number is parsed")
    void attemptedModificationDoesNotChangeParsing() {
        assertThrows(UnsupportedOperationException.class, () -> LocalesConfig.SUPPORTED_LOCALES.set(0, Locale.GERMANY));

        assertEquals(Locale.US, new BigNumber("1234").getLocale());
    }

    @Test
    @DisplayName("the preferred locale comes first and the list has no duplicates or nulls")
    void listIsOrderedAndUnique() {
        assertEquals(Locale.US, LocalesConfig.SUPPORTED_LOCALES.getFirst());
        assertEquals(LocalesConfig.SUPPORTED_LOCALES.size(), new HashSet<>(LocalesConfig.SUPPORTED_LOCALES).size());
        assertTrue(LocalesConfig.SUPPORTED_LOCALES.stream().noneMatch(Objects::isNull));
    }
}
