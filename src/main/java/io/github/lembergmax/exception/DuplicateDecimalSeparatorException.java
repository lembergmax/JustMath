package io.github.lembergmax.exception;

import java.util.HashMap;
import java.util.Locale;

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
