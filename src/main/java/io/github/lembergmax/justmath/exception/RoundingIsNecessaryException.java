package io.github.lembergmax.justmath.exception;

import java.util.HashMap;
import java.util.Locale;

// TODO: javadoc
public class RoundingIsNecessaryException extends JustMathException {

    private static final HashMap<Locale, String> TEXT_TRANSLATIONS = new HashMap<>();
    static {
        TEXT_TRANSLATIONS.put(
                Locale.ENGLISH,
                "The result needs rounding but the rounding mode is UNNECESSARY."
        );
    }

    public RoundingIsNecessaryException(
            final String... args
    ) {
        this(Locale.ENGLISH, args);
    }

    public RoundingIsNecessaryException(
            final Locale locale,
            final String... args
    ) {
        super(getLocaleSpecificTranslation(TEXT_TRANSLATIONS, locale, args));
    }

}
