package io.github.lembergmax.justmath.core;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormatLookup {

    // TODO: javadoc
    public static String getDecimalSeparatorForLocale(
            final Locale locale
    ) {
        return String.valueOf(getDecimalFormatSymbols(locale).getDecimalSeparator());
    }

    // TODO: javadoc
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
