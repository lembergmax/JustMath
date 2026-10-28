package io.github.lembergmax.justmath.core.exception;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JustMathExceptionTest {

    private static final Map<Locale, String> TRANSLATIONS = Map.of(
            Locale.ENGLISH, "English {0}",
            Locale.GERMAN, "Deutsch {0}",
            Locale.GERMANY, "Deutschland {0}"
    );

    @Test
    public void exactLocaleIsPreferredTest() {
        assertEquals("Deutschland x", translate(Locale.GERMANY, "x"));
        assertEquals("Deutsch x", translate(Locale.GERMAN, "x"));
        assertEquals("English x", translate(Locale.ENGLISH, "x"));
    }

    @Test
    public void fallsBackToLanguageTest() {
        assertEquals("Deutsch x", translate(Locale.of("de", "AT"), "x"));
        assertEquals("English x", translate(Locale.US, "x"));
    }

    @Test
    public void fallsBackToEnglishTest() {
        assertEquals("English x", translate(Locale.FRENCH, "x"));
        assertEquals("English x", translate(Locale.ROOT, "x"));
    }

    @Test
    public void englishIsMandatoryTest() {
        final Map<Locale, String> withoutEnglish = Map.of(Locale.GERMAN, "Deutsch {0}");

        assertThrows(
                IllegalStateException.class,
                () -> JustMathException.getLocaleSpecificTranslation(withoutEnglish, Locale.GERMAN, new String[]{"x"})
        );
    }

    @Test
    public void placeholdersTest() {
        final Map<Locale, String> translations = Map.of(Locale.ENGLISH, "{1}-{0}-{1}-{2}");

        assertEquals(
                "b-a-b-{2}",
                JustMathException.getLocaleSpecificTranslation(translations, Locale.ENGLISH, new String[]{"a", "b"})
        );
        assertEquals(
                "{1}-{0}-{1}-{2}",
                JustMathException.getLocaleSpecificTranslation(translations, Locale.ENGLISH, new String[]{})
        );
    }

    @Test
    public void argumentLooksLikePlaceholderTest() {
        assertEquals("English {0}", translate(Locale.ENGLISH, "{0}"));
        assertEquals("English $1\\", translate(Locale.ENGLISH, "$1\\"));
    }

    @Test
    public void duplicateDecimalSeparatorMessageTest() {
        assertEquals(
                "The decimal separator ',' occurs more than once.",
                new DuplicateDecimalSeparatorException(",").getMessage()
        );
        assertEquals(
                "The decimal separator ',' occurs more than once.",
                new DuplicateDecimalSeparatorException(Locale.GERMAN, ",").getMessage()
        );
    }

    @Test
    public void notAValidCharacterMessageTest() {
        assertEquals("'a' is not a valid character.", new NotAValidCharacterException("a").getMessage());
        assertEquals("'a' is not a valid character.", new NotAValidCharacterException(Locale.GERMAN, "a").getMessage());
    }

    private String translate(
            final Locale locale,
            final String arg
    ) {
        return JustMathException.getLocaleSpecificTranslation(TRANSLATIONS, locale, new String[]{arg});
    }

}
