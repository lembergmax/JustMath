package io.github.lembergmax.exception;

import java.util.HashMap;
import java.util.Locale;

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
