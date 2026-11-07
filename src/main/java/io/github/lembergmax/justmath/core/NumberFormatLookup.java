package io.github.lembergmax.justmath.core;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormatLookup {

    /**
     * Returns the decimal separator of a locale, for example "," for {@link Locale#GERMAN}.
     *
     * @param locale the locale to look up
     * @return the decimal separator
     */
    public static String getDecimalSeparatorForLocale(
            final Locale locale
    ) {
        return String.valueOf(getDecimalFormatSymbols(locale).getDecimalSeparator());
    }

    /**
     * Returns the grouping separator of a locale, for example "." for {@link Locale#GERMAN}.
     *
     * @param locale the locale to look up
     * @return the grouping separator
     */
    public static String getGroupingSeparatorForLocale(
            final Locale locale
    ) {
        return String.valueOf(getDecimalFormatSymbols(locale).getGroupingSeparator());
    }

    private static DecimalFormatSymbols getDecimalFormatSymbols(
            final Locale locale
    ) {
        final DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
        return format.getDecimalFormatSymbols();
    }

}
