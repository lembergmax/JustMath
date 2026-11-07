package io.github.lembergmax.justmath.exception;

import java.util.HashMap;
import java.util.Locale;

/**
 * Thrown when a number is divided by zero. A zero with a minus counts as zero.
 * The message can be translated with the locale constructor.
 */
public class DivisionByZeroException extends JustMathException {

    private static final HashMap<Locale, String> TEXT_TRANSLATIONS = new HashMap<>();
    static {
        TEXT_TRANSLATIONS.put(
                Locale.ENGLISH,
                "Division durch 0 ist nicht möglich."
        );
    }

    public DivisionByZeroException(
            final String... args
    ) {
        this(Locale.ENGLISH, args);
    }

    public DivisionByZeroException(
            final Locale locale,
            final String... args
    ) {
        super(getLocaleSpecificTranslation(TEXT_TRANSLATIONS, locale, args));
    }

}
