package io.github.lembergmax.justmath.exception;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class JustMathException extends RuntimeException {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+)}");

    public JustMathException(
            final String message
    ) {
        super(message);
    }

    static String getLocaleSpecificTranslation(
            final Map<Locale, String> translations,
            final Locale locale,
            final String[] args
    ) {
        final String englishText = translations.get(Locale.ENGLISH);
        if (englishText == null) {
            throw new IllegalStateException("The translations must contain a text for Locale.ENGLISH, but only had: " + translations.keySet());
        }

        final String text = translations.getOrDefault(
                locale,
                translations.getOrDefault(Locale.of(locale.getLanguage()), englishText)
        );

        return replacePlaceholders(text, args);
    }

    private static String replacePlaceholders(
            final String text,
            final String[] args
    ) {
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        final StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            final int argIndex = Integer.parseInt(matcher.group(1));
            final String replacement = argIndex < args.length ? args[argIndex] : matcher.group();
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

}
