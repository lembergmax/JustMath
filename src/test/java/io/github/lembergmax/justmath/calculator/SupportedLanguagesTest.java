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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import io.github.lembergmax.justmath.calculator.errors.CalculatorError;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.errors.ErrorMode;

/**
 * Verifies that every language advertised by {@link SupportedLanguages} is actually shipped
 * complete (no missing or untranslated keys) and that locale-aware number formatting follows
 * the selected locale. These tests are the safety net for the i18n expansion: a new language
 * cannot be added to the registry without a fully populated bundle, or this suite turns red.
 */
class SupportedLanguagesTest {

    private static final String CALC_BASE_PATH = "/i18n/calculator_errors";
    private static final String MATRIX_BASE_PATH = "/i18n/matrix_errors";

    /**
     * The full set of message keys that every calculator bundle must define, read from the
     * default (English) bundle so the test stays in sync automatically when keys are added.
     */
    private static final Set<String> CANONICAL_CALC_KEYS =
            loadProperties(CALC_BASE_PATH + ".properties").stringPropertyNames();

    private static final Set<String> CANONICAL_MATRIX_KEYS =
            loadProperties(MATRIX_BASE_PATH + ".properties").stringPropertyNames();

    static List<Locale> supportedLocales() {
        return SupportedLanguages.all();
    }

    @Nested
    @DisplayName("registry")
    class Registry {

        @Test
        @DisplayName("all() is non-empty, immutable and contains the requested languages")
        void allContainsRequestedLanguages() {
            List<Locale> all = SupportedLanguages.all();
            assertFalse(all.isEmpty(), "supported languages must not be empty");
            assertThrows(UnsupportedOperationException.class,
                    () -> all.add(Locale.JAPANESE), "supported language list must be immutable");

            for (String tag : new String[]{
                    "en", "de", "de-AT", "de-CH", "es", "fr", "fr-BE", "it",
                    "pt", "pt-PT", "pt-BR", "nl", "nl-BE", "cs", "da", "sv", "no", "nb", "fi", "pl"}) {
                assertTrue(all.contains(Locale.forLanguageTag(tag)),
                        "registry must list " + tag);
            }
        }

        @Test
        @DisplayName("CalculatorEngine.getSupportedLanguages() delegates to the registry")
        void engineDelegatesToRegistry() {
            assertEquals(SupportedLanguages.all(), CalculatorEngine.getSupportedLanguages());
        }

        @Test
        @DisplayName("isSupported() matches exact locales and falls back to language level")
        void isSupportedMatchesLanguage() {
            assertTrue(SupportedLanguages.isSupported(Locale.forLanguageTag("pt-BR")));
            assertTrue(SupportedLanguages.isSupported(Locale.FRANCE),    // fr-FR -> fr
                    "country variant of a supported language must be supported");
            assertTrue(SupportedLanguages.isSupported(Locale.forLanguageTag("fr-CA")));
            assertTrue(CalculatorEngine.isLanguageSupported(Locale.ITALIAN));
            assertFalse(SupportedLanguages.isSupported(Locale.JAPANESE));
            assertFalse(SupportedLanguages.isSupported(Locale.forLanguageTag("zh-CN")));
        }
    }

    @Nested
    @DisplayName("bundle completeness")
    class Completeness {

        @ParameterizedTest(name = "{0}")
        @MethodSource("io.github.lembergmax.justmath.calculator.SupportedLanguagesTest#supportedLocales")
        @DisplayName("calculator bundle defines every key with a non-blank translation")
        void calculatorBundleComplete(Locale locale) {
            Properties bundle = loadBundleFile(CALC_BASE_PATH, locale);
            assertMissingKeysEmpty(CANONICAL_CALC_KEYS, bundle, "calculator", locale);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("io.github.lembergmax.justmath.calculator.SupportedLanguagesTest#supportedLocales")
        @DisplayName("matrix bundle defines every key with a non-blank translation")
        void matrixBundleComplete(Locale locale) {
            Properties bundle = loadBundleFile(MATRIX_BASE_PATH, locale);
            assertMissingKeysEmpty(CANONICAL_MATRIX_KEYS, bundle, "matrix", locale);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("io.github.lembergmax.justmath.calculator.SupportedLanguagesTest#supportedLocales")
        @DisplayName("every CalculatorErrorCode resolves to a localized, non-blank message")
        void everyErrorCodeResolves(Locale locale) {
            for (CalculatorErrorCode code : CalculatorErrorCode.values()) {
                CalculatorError error = new CalculatorError(code, "technical");
                String localized = error.format(locale, ErrorMode.USER_FRIENDLY);
                assertNotNull(localized);
                assertFalse(localized.isBlank(), "blank message for " + code + " / " + locale);
                assertFalse(localized.toLowerCase(Locale.ROOT).contains("exception"),
                        "message leaked an exception keyword for " + code + " / " + locale);
            }
        }
    }

    @Nested
    @DisplayName("translations actually differ from English")
    class ActuallyTranslated {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
                "de, Division durch Null ist nicht erlaubt.",
                "es, No se permite la división por cero.",
                "fr, La division par zéro n'est pas autorisée.",
                "it, La divisione per zero non è consentita.",
                "pt, A divisão por zero não é permitida.",
                "nl, Delen door nul is niet toegestaan.",
                "cs, Dělení nulou není povoleno.",
                "da, Division med nul er ikke tilladt.",
                "sv, Division med noll är inte tillåten.",
                "no, Divisjon med null er ikke tillatt.",
                "fi, Nollalla jakaminen ei ole sallittua.",
                "pl, Dzielenie przez zero jest niedozwolone."
        })
        @DisplayName("division-by-zero is translated per language")
        void divisionByZeroIsTranslated(String tag, String expected) {
            CalculatorError error = new CalculatorError(
                    CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO, "Division by zero");
            assertEquals(expected,
                    error.format(Locale.forLanguageTag(tag), ErrorMode.USER_FRIENDLY));
        }

        @Test
        @DisplayName("Swiss German writes ss instead of ß")
        void swissGermanUsesSs() {
            CalculatorError error = new CalculatorError(
                    CalculatorErrorCode.PROCESSING_DOMAIN_ERROR, "domain");
            String swiss = error.format(Locale.forLanguageTag("de-CH"), ErrorMode.USER_FRIENDLY);
            assertTrue(swiss.contains("ausserhalb"), "expected ss spelling, got: " + swiss);
            assertFalse(swiss.contains("ß"), "Swiss German must not contain ß: " + swiss);
        }
    }

    @Nested
    @DisplayName("locale-aware number formatting")
    class NumberFormatting {

        // Expected values are single-quoted because the comma is both the CSV delimiter and the
        // decimal separator under test; without quoting, "de, 0,125" would split into three columns.
        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
                "en, '0.125'", "de, '0,125'", "fr, '0,125'", "es, '0,125'", "it, '0,125'",
                "pt, '0,125'", "nl, '0,125'", "cs, '0,125'", "da, '0,125'", "sv, '0,125'",
                "no, '0,125'", "fi, '0,125'", "pl, '0,125'", "de-CH, '0.125'"
        })
        @DisplayName("decimal separator follows the output locale")
        void decimalSeparatorFollowsLocale(String tag, String expected) {
            CalculatorEngine engine = new CalculatorEngine().setLocale(Locale.forLanguageTag(tag));
            assertEquals(expected, engine.evaluateToString("1/8"),
                    "wrong decimal separator for " + tag);
        }

        @Test
        @DisplayName("pretty output groups thousands per locale")
        void prettyGroupingFollowsLocale() {
            // 100000 / 8 = 12500
            assertEquals("12,500",
                    new CalculatorEngine().setLocale(Locale.US).evaluateToPrettyString("100000/8"));
            assertEquals("12.500",
                    new CalculatorEngine().setLocale(Locale.GERMANY).evaluateToPrettyString("100000/8"));
        }
    }

    // --- helpers ---------------------------------------------------------------------------

    private static void assertMissingKeysEmpty(
            Set<String> requiredKeys, Properties bundle, String family, Locale locale) {
        for (String key : requiredKeys) {
            String value = bundle.getProperty(key);
            assertNotNull(value, "missing " + family + " key '" + key + "' for locale " + locale);
            assertFalse(value.isBlank(),
                    "blank " + family + " value for key '" + key + "' / locale " + locale);
        }
    }

    private static Properties loadBundleFile(String basePath, Locale locale) {
        String suffix = locale.toString(); // e.g. "en", "de", "de_AT", "pt_BR"
        return loadProperties(basePath + "_" + suffix + ".properties");
    }

    private static Properties loadProperties(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream in = SupportedLanguagesTest.class.getResourceAsStream(resourcePath)) {
            assertNotNull(in, "missing bundle resource: " + resourcePath);
            // Properties files are read as UTF-8 (matching java.util.PropertyResourceBundle on JDK 9+).
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new AssertionError("could not load " + resourcePath, e);
        }
        return properties;
    }

}
