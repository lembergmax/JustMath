package io.github.lembergmax.justmath.exception;

import java.util.HashMap;
import java.util.Locale;

/**
 * Thrown when the text of a number contains the decimal separator more than once, for example "1,2,3" in German.
 * The message names the separator as {0} and can be translated with the locale constructor.
 */
public class DuplicateDecimalSeparatorException extends JustMathException {

    private static final HashMap<Locale, String> TEXT_TRANSLATIONS = new HashMap<>();
    static {
        TEXT_TRANSLATIONS.put(Locale.ENGLISH,
                "The decimal separator '{0}' occurs more than once."
        );
    }

    public DuplicateDecimalSeparatorException(
            final String... args
    ) {
        this(Locale.ENGLISH, args);
    }

    public DuplicateDecimalSeparatorException(
            final Locale locale,
            final String... args
    ) {
        super(getLocaleSpecificTranslation(TEXT_TRANSLATIONS, locale, args));
    }

}
