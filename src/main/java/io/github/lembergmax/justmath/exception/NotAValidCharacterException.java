package io.github.lembergmax.justmath.exception;

import java.util.HashMap;
import java.util.Locale;

/**
 * Thrown when the text of a number contains a character that is neither a digit, a decimal or grouping
 * separator nor a sign at the very first position. The message names the character as {0} and can be
 * translated with the locale constructor.
 */
public class NotAValidCharacterException extends JustMathException {

    private static final HashMap<Locale, String> TEXT_TRANSLATIONS = new HashMap<>();
    static {
        TEXT_TRANSLATIONS.put(
                Locale.ENGLISH,
                "'{0}' is not a valid character."
        );
    }

    public NotAValidCharacterException(
            final String... args
    ) {
        this(Locale.ENGLISH, args);
    }

    public NotAValidCharacterException(
            final Locale locale,
            final String... args
    ) {
        super(getLocaleSpecificTranslation(TEXT_TRANSLATIONS, locale, args));
    }

}
